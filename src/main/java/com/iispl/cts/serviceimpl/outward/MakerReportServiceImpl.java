package com.iispl.cts.serviceimpl.outward;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.service.outward.MakerReportService;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import org.zkoss.zk.ui.Executions;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MakerReportServiceImpl implements MakerReportService {

    private static final String REPORT_PATH = "/reports/cts_maker_summary_report.jrxml";

    @Override
    public byte[] generateMakerReportPdf(String makerId, Date fromDate, Date toDate) throws Exception {
        try (InputStream reportStream = locateReportStream(REPORT_PATH)) {
            if (reportStream == null) {
                throw new IllegalStateException("Maker report template not found: " + REPORT_PATH);
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> params = new HashMap<>();
            params.put("FROM_DATE", new java.sql.Date(fromDate.getTime()));
            params.put("TO_DATE", new java.sql.Date(toDate.getTime()));
            params.put("MAKER_ID", makerId);

            try (Connection conn = DBConnection.getConnection()) {
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);
                return JasperExportManager.exportReportToPdf(jasperPrint);
            }
        }
    }

    @Override
    public byte[] generateMakerReportCsv(String makerId, Date fromDate, Date toDate) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("\uFEFF"); // UTF-8 BOM to prevent Excel display issues

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat df = new DecimalFormat("#,##0.00");

        try (Connection conn = DBConnection.getConnection()) {

            // SECTION 1: Batches with MICR Repairs
            sb.append("========================================================================================\n");
            sb.append("                         1. BATCHES WITH MICR REPAIRS                                   \n");
            sb.append("========================================================================================\n");
            sb.append("Batch ID,Batch Reference,Total Cheques,Repaired Count,Total Amount (INR),Uploaded Time\n");

            String sql1 = "SELECT ob.outward_batch_id, ob.batch_reference_id, "
                        + "       COUNT(oc.outward_cheque_id) AS total_cheques, "
                        + "       COUNT(CASE WHEN UPPER(oc.cheque_status) LIKE '%REPAIR%' "
                        + "                    OR UPPER(oc.cheque_status) LIKE '%MICR%' THEN 1 END) AS repaired_count, "
                        + "       COALESCE(SUM(oc.cheque_amount), 0.00) AS total_amount, "
                        + "       ob.uploaded_at "
                        + "FROM outward_batch ob "
                        + "JOIN outward_cheque oc ON ob.outward_batch_id = oc.outward_batch_id "
                        + "WHERE ob.uploaded_by = ? "
                        + "  AND CAST(ob.uploaded_at AS DATE) BETWEEN ? AND ? "
                        + "GROUP BY ob.outward_batch_id, ob.batch_reference_id, ob.uploaded_at "
                        + "HAVING COUNT(CASE WHEN UPPER(oc.cheque_status) LIKE '%REPAIR%' "
                        + "                    OR UPPER(oc.cheque_status) LIKE '%MICR%' THEN 1 END) > 0 "
                        + "ORDER BY ob.uploaded_at DESC";

            try (PreparedStatement ps = conn.prepareStatement(sql1)) {
                ps.setString(1, makerId);
                ps.setDate(2, new java.sql.Date(fromDate.getTime()));
                ps.setDate(3, new java.sql.Date(toDate.getTime()));

                try (ResultSet rs = ps.executeQuery()) {
                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        String ts = rs.getTimestamp("uploaded_at") != null 
                                ? "=\"" + sdf.format(rs.getTimestamp("uploaded_at")) + "\"" : "-";
                        sb.append(String.format("\"%s\",\"%s\",\"%d\",\"%d Items\",\"%s\",%s\n",
                                rs.getString("outward_batch_id"),
                                rs.getString("batch_reference_id"),
                                rs.getLong("total_cheques"),
                                rs.getLong("repaired_count"),
                                df.format(rs.getDouble("total_amount")),
                                ts));
                    }
                    if (!found) sb.append("\"No MICR repairs found for this period.\",,,,,\n");
                }
            }

            // SECTION 2: Data Entry (CAR/LAR Keying)
            sb.append("\n\n========================================================================================\n");
            sb.append("               2. BATCHES GONE FOR DATA ENTRY (CAR/LAR KEYING)                          \n");
            sb.append("========================================================================================\n");
            sb.append("Batch ID,Batch Reference,Pending Cheques,Status,Uploaded Time\n");

            String sql2 = "SELECT ob.outward_batch_id, ob.batch_reference_id, "
                        + "       COUNT(oc.outward_cheque_id) AS pending_items, "
                        + "       ob.batch_status, ob.uploaded_at "
                        + "FROM outward_batch ob "
                        + "JOIN outward_cheque oc ON ob.outward_batch_id = oc.outward_batch_id "
                        + "WHERE ob.uploaded_by = ? "
                        + "  AND UPPER(oc.cheque_status) IN ('DATA_ENTRY', 'PENDING_DATA_ENTRY', 'KEYING_PENDING') "
                        + "  AND CAST(ob.uploaded_at AS DATE) BETWEEN ? AND ? "
                        + "GROUP BY ob.outward_batch_id, ob.batch_reference_id, ob.batch_status, ob.uploaded_at "
                        + "ORDER BY ob.uploaded_at DESC";

            try (PreparedStatement ps = conn.prepareStatement(sql2)) {
                ps.setString(1, makerId);
                ps.setDate(2, new java.sql.Date(fromDate.getTime()));
                ps.setDate(3, new java.sql.Date(toDate.getTime()));

                try (ResultSet rs = ps.executeQuery()) {
                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        String ts = rs.getTimestamp("uploaded_at") != null 
                                ? "=\"" + sdf.format(rs.getTimestamp("uploaded_at")) + "\"" : "-";
                        sb.append(String.format("\"%s\",\"%s\",\"%d Items\",\"%s\",%s\n",
                                rs.getString("outward_batch_id"),
                                rs.getString("batch_reference_id"),
                                rs.getLong("pending_items"),
                                rs.getString("batch_status"),
                                ts));
                    }
                    if (!found) sb.append("\"No batches pending data entry for this period.\",,,,\n");
                }
            }

            // SECTION 3: Unprocessed Cheques Audit
            sb.append("\n\n========================================================================================\n");
            sb.append("                         3. UNPROCESSED CHEQUES AUDIT                                   \n");
            sb.append("========================================================================================\n");
            sb.append("Batch ID,Cheque ID,Cheque No,MICR Code,Drawee Acc No,Amount (INR),Status\n");

            String sql3 = "SELECT oc.outward_batch_id, oc.outward_cheque_id, "
                        + "       COALESCE(oc.cheque_number, 'UNREADABLE') AS cheque_number, "
                        + "       COALESCE(oc.micr_code, 'UNREADABLE') AS micr_code, "
                        + "       COALESCE(oc.drawee_account_number, 'UNREADABLE') AS drawee_account_number, "
                        + "       oc.cheque_amount, oc.cheque_status "
                        + "FROM outward_cheque oc "
                        + "JOIN outward_batch ob ON oc.outward_batch_id = ob.outward_batch_id "
                        + "WHERE ob.uploaded_by = ? "
                        + "  AND UPPER(oc.cheque_status) IN ('UNPROCESSED', 'PENDING', 'OCR_FAILED', 'IMAGE_REJECTED', 'FAILED') "
                        + "  AND CAST(ob.uploaded_at AS DATE) BETWEEN ? AND ? "
                        + "ORDER BY oc.outward_batch_id, oc.outward_cheque_id";

            try (PreparedStatement ps = conn.prepareStatement(sql3)) {
                ps.setString(1, makerId);
                ps.setDate(2, new java.sql.Date(fromDate.getTime()));
                ps.setDate(3, new java.sql.Date(toDate.getTime()));

                try (ResultSet rs = ps.executeQuery()) {
                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        sb.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                                rs.getString("outward_batch_id"),
                                rs.getString("outward_cheque_id"),
                                rs.getString("cheque_number"),
                                rs.getString("micr_code"),
                                rs.getString("drawee_account_number"),
                                df.format(rs.getDouble("cheque_amount")),
                                rs.getString("cheque_status")));
                    }
                    if (!found) sb.append("\"No unprocessed cheques found for this period.\",,,,,,\n");
                }
            }

            // SECTION 4: Batches Submitted to Checker
            sb.append("\n\n========================================================================================\n");
            sb.append("                         4. BATCHES SUBMITTED TO CHECKER                                \n");
            sb.append("========================================================================================\n");
            sb.append("Batch ID,Batch Reference,Total Cheques,Total Amount (INR),Status,Submitted Time\n");

            String sql4 = "SELECT outward_batch_id, batch_reference_id, actual_cheque_count, "
                        + "       actual_total_amount, uploaded_at, batch_status "
                        + "FROM outward_batch "
                        + "WHERE uploaded_by = ? "
                        + "  AND UPPER(batch_status) IN ('SUBMITTED_TO_CHECKER', 'PENDING_VERIFICATION', 'SUBMITTED', 'PENDING') "
                        + "  AND CAST(uploaded_at AS DATE) BETWEEN ? AND ? "
                        + "ORDER BY uploaded_at DESC";

            try (PreparedStatement ps = conn.prepareStatement(sql4)) {
                ps.setString(1, makerId);
                ps.setDate(2, new java.sql.Date(fromDate.getTime()));
                ps.setDate(3, new java.sql.Date(toDate.getTime()));

                try (ResultSet rs = ps.executeQuery()) {
                    boolean found = false;
                    while (rs.next()) {
                        found = true;
                        String ts = rs.getTimestamp("uploaded_at") != null 
                                ? "=\"" + sdf.format(rs.getTimestamp("uploaded_at")) + "\"" : "-";
                        sb.append(String.format("\"%s\",\"%s\",\"%d\",\"%s\",\"%s\",%s\n",
                                rs.getString("outward_batch_id"),
                                rs.getString("batch_reference_id"),
                                rs.getInt("actual_cheque_count"),
                                df.format(rs.getDouble("actual_total_amount")),
                                rs.getString("batch_status"),
                                ts));
                    }
                    if (!found) sb.append("\"No batches submitted to Checker for this period.\",,,,,\n");
                }
            }
        }

        return sb.toString().getBytes(StandardCharsets.UTF_8);
    }

    private InputStream locateReportStream(String path) {
        try {
            if (Executions.getCurrent() != null && Executions.getCurrent().getDesktop() != null) {
                InputStream webStream = Executions.getCurrent()
                        .getDesktop()
                        .getWebApp()
                        .getResourceAsStream(path.startsWith("/") ? path : "/" + path);
                if (webStream != null) {
                    return webStream;
                }
            }
        } catch (Exception ignored) {
        }

        String trimmed = path.startsWith("/") ? path.substring(1) : path;
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(trimmed);
        if (stream != null) return stream;

        stream = MakerReportServiceImpl.class.getClassLoader().getResourceAsStream(trimmed);
        if (stream != null) return stream;

        return getClass().getResourceAsStream(path.startsWith("/") ? path : "/" + path);
    }
}