package com.iispl.cts.serviceimpl.outward;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.MakerReportDAO;
import com.iispl.cts.daoimpl.outward.MakerReportDAOImpl;
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
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MakerReportServiceImpl implements MakerReportService {

    private static final String REPORT_PATH = "/reports/cts_maker_summary_report.jrxml";
    private final MakerReportDAO makerReportDAO = new MakerReportDAOImpl();

    @Override
    public byte[] generateMakerReportPdf(String makerId, String reportType, Date fromDate, Date toDate) throws Exception {
        try (InputStream reportStream = locateReportStream(REPORT_PATH)) {
            if (reportStream == null) {
                throw new IllegalStateException("Maker report template not found: " + REPORT_PATH);
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> params = new HashMap<>();
            params.put("FROM_DATE", new java.sql.Date(fromDate.getTime()));
            params.put("TO_DATE", new java.sql.Date(toDate.getTime()));
            params.put("MAKER_ID", makerId);
            params.put("REPORT_TYPE", reportType);

            try (Connection conn = DBConnection.getConnection()) {
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);
                return JasperExportManager.exportReportToPdf(jasperPrint);
            }
        }
    }

    @Override
    public byte[] generateMakerReportCsv(String makerId, String reportType, Date fromDate, Date toDate) throws Exception {
        StringBuilder sb = new StringBuilder();
        sb.append("\uFEFF"); // UTF-8 BOM

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat df = new DecimalFormat("#,##0.00");
        java.sql.Date sqlFrom = new java.sql.Date(fromDate.getTime());
        java.sql.Date sqlTo = new java.sql.Date(toDate.getTime());

        switch (reportType) {
            case "MICR_REPAIRS": {
                sb.append("========================================================================================\n");
                sb.append("                         BATCHES WITH MICR REPAIRS                                      \n");
                sb.append("========================================================================================\n");
                sb.append("Batch ID,Batch Reference,Total Cheques,Repaired Count,Total Amount (INR),Uploaded Time\n");

                List<Map<String, Object>> rows = makerReportDAO.getMicrRepairsReport(makerId, sqlFrom, sqlTo);
                if (rows.isEmpty()) {
                    sb.append("\"No MICR repairs found for this period.\",,,,,\n");
                } else {
                    for (Map<String, Object> r : rows) {
                        String ts = r.get("uploaded_at") != null ? "=\"" + sdf.format(r.get("uploaded_at")) + "\"" : "-";
                        sb.append(String.format("\"%s\",\"%s\",\"%s\",\"%s Items\",\"%s\",%s\n",
                                r.get("scanned_batch_id"),
                                r.get("batch_reference_id"),
                                r.get("total_cheques"),
                                r.get("repaired_count"),
                                df.format(Double.parseDouble(String.valueOf(r.get("total_amount")))),
                                ts));
                    }
                }
                break;
            }

            case "DATA_ENTRY": {
                sb.append("========================================================================================\n");
                sb.append("               BATCHES GONE FOR DATA ENTRY (CAR/LAR KEYING)                             \n");
                sb.append("========================================================================================\n");
                sb.append("Batch ID,Batch Reference,Pending Cheques,Status,Uploaded Time\n");

                List<Map<String, Object>> rows = makerReportDAO.getDataEntryReport(makerId, sqlFrom, sqlTo);
                if (rows.isEmpty()) {
                    sb.append("\"No batches pending data entry for this period.\",,,,\n");
                } else {
                    for (Map<String, Object> r : rows) {
                        String ts = r.get("uploaded_at") != null ? "=\"" + sdf.format(r.get("uploaded_at")) + "\"" : "-";
                        sb.append(String.format("\"%s\",\"%s\",\"%s Items\",\"%s\",%s\n",
                                r.get("scanned_batch_id"),
                                r.get("batch_reference_id"),
                                r.get("pending_items"),
                                r.get("batch_status"),
                                ts));
                    }
                }
                break;
            }

            case "REQUEST_REJECTED": {
                sb.append("========================================================================================\n");
                sb.append("                         REQUEST REJECTED CHEQUES REPORT                                \n");
                sb.append("========================================================================================\n");
                sb.append("Request ID,Batch ID,Cheque ID,Cheque No,Reason,Remarks,Timestamp\n");

                List<Map<String, Object>> rows = makerReportDAO.getRequestRejectedChequesReport(makerId, sqlFrom, sqlTo);
                if (rows == null || rows.isEmpty()) {
                    sb.append("\"No request rejected cheques found for this period.\",,,,,,\n");
                } else {
                    for (Map<String, Object> r : rows) {
                        // Safe Cheque Number
                        String chkNo = r.get("cheque_number") != null ? r.get("cheque_number").toString() : String.valueOf(r.get("cheque_id"));

                        // Safe Timestamp
                        String ts = "-";
                        if (r.get("time_stamp") != null) {
                            ts = "=\"" + r.get("time_stamp").toString() + "\"";
                        }

                        sb.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s\n",
                                r.get("request_id") != null ? r.get("request_id") : "-",
                                r.get("batch_id") != null ? r.get("batch_id") : "-",
                                r.get("cheque_id") != null ? r.get("cheque_id") : "-",
                                chkNo,
                                r.get("reason") != null ? r.get("reason") : "-",
                                r.get("remarks") != null ? r.get("remarks") : "-",
                                ts));
                    }
                }
                break;
            }

            case "SUBMITTED_CHECKER": {
                sb.append("========================================================================================\n");
                sb.append("                         BATCHES SUBMITTED TO CHECKER                                   \n");
                sb.append("========================================================================================\n");
                sb.append("Batch ID,Batch Reference,Total Cheques,Total Amount (INR),Status,Submitted Time\n");

                List<Map<String, Object>> rows = makerReportDAO.getSubmittedToCheckerReport(makerId, sqlFrom, sqlTo);
                if (rows.isEmpty()) {
                    sb.append("\"No batches submitted to Checker for this period.\",,,,,\n");
                } else {
                    for (Map<String, Object> r : rows) {
                        String ts = r.get("uploaded_at") != null ? "=\"" + sdf.format(r.get("uploaded_at")) + "\"" : "-";
                        sb.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",%s\n",
                                r.get("outward_batch_id"),
                                r.get("batch_reference_id"),
                                r.get("actual_cheque_count"),
                                df.format(Double.parseDouble(String.valueOf(r.get("actual_total_amount")))),
                                r.get("batch_status"),
                                ts));
                    }
                }
                break;
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