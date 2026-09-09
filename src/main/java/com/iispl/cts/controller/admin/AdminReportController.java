package com.iispl.cts.controller.admin;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.common.util.SecurityUtil;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AdminReportController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    private Datebox dtFromDate;
    private Datebox dtToDate;
    private Button btnExportCsv;
    private Button btnExportPdf;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        if (!SecurityUtil.checkAccess(null)) {
            return;
        }
        super.doAfterCompose(comp);

        Date today = new Date();
        dtFromDate.setValue(today);
        dtToDate.setValue(today);
    }

    // ==========================================
    // OPTION 1: CSV EXPORT
    // ==========================================
    public void onClick$btnExportCsv() {
        Date fromDate = dtFromDate.getValue();
        Date toDate = dtToDate.getValue();

        if (fromDate == null || toDate == null) {
            Messagebox.show("Please select both From Date and To Date.", "Validation", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        if (fromDate.after(toDate)) {
            Messagebox.show("From Date cannot be later than To Date.", "Validation", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        StringBuilder sb = new StringBuilder();
        // UTF-8 BOM so Excel opens special characters and formatting cleanly
        sb.append("\uFEFF");

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat df = new DecimalFormat("#,##0.00");

        try (Connection conn = DBConnection.getConnection()) {

            // Section 1: Outward Batches
            sb.append("========================================================================================\n");
            sb.append("                         EXPRESS CLEAR CTS - OUTWARD CLEARING BATCHES                   \n");
            sb.append("========================================================================================\n");
            sb.append("Batch ID,Reference ID,Uploaded At,Cheque Count,Total Amount (INR),Uploaded By,Status\n");

            String batchSql = "SELECT ob.outward_batch_id, ob.batch_reference_id, ob.uploaded_at, "
                            + "       COALESCE(ob.actual_cheque_count, 0) AS cheque_count, "
                            + "       COALESCE(ob.actual_total_amount, 0.00) AS total_amount, "
                            + "       COALESCE(u.username, ob.uploaded_by) AS uploaded_by, "
                            + "       COALESCE(ob.batch_status, 'Pending') AS batch_status "
                            + "FROM outward_batch ob "
                            + "LEFT JOIN users u ON ob.uploaded_by = u.user_id "
                            + "WHERE ob.uploaded_at::date >= ? AND ob.uploaded_at::date <= ? "
                            + "ORDER BY ob.uploaded_at DESC";

            try (PreparedStatement psBatch = conn.prepareStatement(batchSql)) {
                psBatch.setDate(1, new java.sql.Date(fromDate.getTime()));
                psBatch.setDate(2, new java.sql.Date(toDate.getTime()));

                try (ResultSet rs = psBatch.executeQuery()) {
                    boolean hasBatches = false;
                    while (rs.next()) {
                        hasBatches = true;
                        String uploadTime = rs.getTimestamp("uploaded_at") != null 
                                ? sdf.format(rs.getTimestamp("uploaded_at")) : "-";

                        // Using ="value" prevents Excel from turning the timestamp into ###
                        String formattedTime = uploadTime.equals("-") ? "-" : "=\"" + uploadTime + "\"";

                        sb.append(String.format("\"%s\",\"%s\",%s,\"%d\",\"%s\",\"%s\",\"%s\"\n",
                                rs.getString("outward_batch_id"),
                                rs.getString("batch_reference_id"),
                                formattedTime,
                                rs.getInt("cheque_count"),
                                df.format(rs.getDouble("total_amount")),
                                rs.getString("uploaded_by"),
                                rs.getString("batch_status")));
                    }
                    if (!hasBatches) {
                        sb.append("\"No outward clearing batches found for the selected date range.\",,,,,,\n");
                    }
                }
            }

            // Section 2: Audit Logs
            sb.append("\n\n");
            sb.append("========================================================================================\n");
            sb.append("                       SYSTEM AUDIT LOGS & USER AUTHENTICATION TRAIL                    \n");
            sb.append("========================================================================================\n");
            sb.append("Timestamp,User ID,Username,Role,Module,Action,Details,IP Address,Status\n");

            String auditSql = "SELECT al.\"timestamp\", COALESCE(al.user_id, '-') AS user_id, "
                            + "       COALESCE(al.username, 'SYSTEM') AS username, "
                            + "       COALESCE(al.role_name, '-') AS role_name, "
                            + "       COALESCE(al.module, 'GENERAL') AS module, "
                            + "       COALESCE(al.action, '-') AS action, "
                            + "       COALESCE(al.details, '-') AS details, "
                            + "       COALESCE(al.ip_address, '-') AS ip_address, "
                            + "       COALESCE(al.status, 'SUCCESS') AS status "
                            + "FROM audit_logs al "
                            + "WHERE al.\"timestamp\"::date >= ? AND al.\"timestamp\"::date <= ? "
                            + "ORDER BY al.\"timestamp\" DESC, al.audit_id DESC";

            try (PreparedStatement psAudit = conn.prepareStatement(auditSql)) {
                psAudit.setDate(1, new java.sql.Date(fromDate.getTime()));
                psAudit.setDate(2, new java.sql.Date(toDate.getTime()));

                try (ResultSet rs = psAudit.executeQuery()) {
                    boolean hasLogs = false;
                    while (rs.next()) {
                        hasLogs = true;
                        String ts = rs.getTimestamp("timestamp") != null ? sdf.format(rs.getTimestamp("timestamp")) : "-";
                        String formattedTs = ts.equals("-") ? "-" : "=\"" + ts + "\"";
                        
                        String userId = rs.getString("user_id");
                        String username = rs.getString("username");
                        String role = rs.getString("role_name");
                        String module = rs.getString("module");
                        String action = rs.getString("action");
                        String details = rs.getString("details") != null ? rs.getString("details").replace("\"", "\"\"") : "-";
                        String ip = rs.getString("ip_address");
                        String status = rs.getString("status");

                        sb.append(String.format("%s,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                                formattedTs, userId, username, role, module, action, details, ip, status));
                    }
                    if (!hasLogs) {
                        sb.append("\"No audit logs found for the selected date range.\",,,,,,,,\n");
                    }
                }
            }

            SimpleDateFormat fSdf = new SimpleDateFormat("yyyyMMdd");
            String fileName = "CTS_Consolidated_Admin_Report_" + fSdf.format(fromDate) + "_to_" + fSdf.format(toDate) + ".csv";
            Filedownload.save(sb.toString().getBytes(StandardCharsets.UTF_8), "text/csv", fileName);

        } catch (Exception e) {
            e.printStackTrace();
            Messagebox.show("Failed to export CSV: " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
        }
    }

    // ==========================================
    // OPTION 2: PDF EXPORT (Via JasperReports)
    // ==========================================
    public void onClick$btnExportPdf() {
        Date fromDate = dtFromDate.getValue();
        Date toDate = dtToDate.getValue();

        if (fromDate == null || toDate == null) {
            Messagebox.show("Please select both From Date and To Date.", "Validation", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        if (fromDate.after(toDate)) {
            Messagebox.show("From Date cannot be later than To Date.", "Validation", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        try (Connection conn = DBConnection.getConnection()) {
            String reportPath = Executions.getCurrent().getDesktop().getWebApp().getRealPath("/reports/cts_consolidated_report.jrxml");
            File jrxmlFile = new File(reportPath);

            if (!jrxmlFile.exists()) {
                Messagebox.show("Report template not found at: " + reportPath, "Error", Messagebox.OK, Messagebox.ERROR);
                return;
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportPath);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("FROM_DATE", new java.sql.Date(fromDate.getTime()));
            parameters.put("TO_DATE", new java.sql.Date(toDate.getTime()));
            parameters.put("GENERATED_BY", "ADMIN");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

            SimpleDateFormat fSdf = new SimpleDateFormat("yyyyMMdd");
            String fileName = "CTS_Consolidated_Admin_Report_" + fSdf.format(fromDate) + "_to_" + fSdf.format(toDate) + ".pdf";

            Filedownload.save(pdfBytes, "application/pdf", fileName);

        } catch (Exception e) {
            e.printStackTrace();
            Messagebox.show("PDF export failed: " + e.getMessage(), "Export Error", Messagebox.OK, Messagebox.ERROR);
        }
    }
}