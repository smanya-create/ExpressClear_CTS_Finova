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

    private Combobox cmbReportType;
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

        if (cmbReportType != null && cmbReportType.getItemCount() > 0) {
            cmbReportType.setSelectedIndex(0);
        }

        Date today = new Date();
        dtFromDate.setValue(today);
        dtToDate.setValue(today);
    }

    // =========================================================================
    // 1. CSV EXPORT DISPATCHER
    // =========================================================================
    public void onClick$btnExportCsv(org.zkoss.zk.ui.event.Event event) {
        System.out.println(">>> [AdminReportController] CSV Export Clicked! <<<");
        
        Date fromDate = dtFromDate.getValue();
        Date toDate = dtToDate.getValue();

        if (!validateDates(fromDate, toDate)) return;

        String reportType = getSelectedReportType();
        System.out.println(">>> Report Type Selected for CSV: " + reportType);

        if ("SESSION_LIFECYCLE".equals(reportType)) {
            exportSessionLifecycleCsv(fromDate, toDate);
        } else {
            // Default to USER_AUDIT
            exportUserAuditCsv(fromDate, toDate);
        }
    }

    // =========================================================================
    // 2. PDF EXPORT DISPATCHER
    // =========================================================================
    public void onClick$btnExportPdf(org.zkoss.zk.ui.event.Event event) {
        System.out.println(">>> [AdminReportController] PDF Export Clicked! <<<");
        
        Date fromDate = dtFromDate.getValue();
        Date toDate = dtToDate.getValue();

        if (!validateDates(fromDate, toDate)) return;

        String reportType = getSelectedReportType();
        System.out.println(">>> Report Type Selected for PDF: " + reportType);

        if ("SESSION_LIFECYCLE".equals(reportType)) {
            exportReportPdf("/reports/cts_consolidated_report.jrxml", fromDate, toDate, "CTS_Batch_Lifecycle");
        } else {
            exportReportPdf("/reports/cts_consolidated_report.jrxml", fromDate, toDate, "User_Security_Audit");
        }
    }

    // =========================================================================
    // REPORT 1: USER ACTIVITY & SECURITY AUDIT CSV
    // =========================================================================
    private void exportUserAuditCsv(Date fromDate, Date toDate) {
        StringBuilder sb = new StringBuilder("\uFEFF");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        sb.append("========================================================================================\n");
        sb.append("                    EXPRESS CLEAR CTS - USER ACTIVITY & SECURITY AUDIT                   \n");
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

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(auditSql)) {

            ps.setDate(1, new java.sql.Date(fromDate.getTime()));
            ps.setDate(2, new java.sql.Date(toDate.getTime()));

            try (ResultSet rs = ps.executeQuery()) {
                boolean hasLogs = false;
                while (rs.next()) {
                    hasLogs = true;
                    String ts = rs.getTimestamp("timestamp") != null ? sdf.format(rs.getTimestamp("timestamp")) : "-";
                    String formattedTs = ts.equals("-") ? "-" : "=\"" + ts + "\"";

                    sb.append(String.format("%s,\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\",\"%s\"\n",
                            formattedTs,
                            rs.getString("user_id"),
                            rs.getString("username"),
                            rs.getString("role_name"),
                            rs.getString("module"),
                            rs.getString("action"),
                            rs.getString("details") != null ? rs.getString("details").replace("\"", "\"\"") : "-",
                            rs.getString("ip_address"),
                            rs.getString("status")));
                }
                if (!hasLogs) {
                    sb.append("\"No user audit activity logs found for the selected date range.\",,,,,,,,\n");
                }
            }

            SimpleDateFormat fSdf = new SimpleDateFormat("yyyyMMdd");
            String fileName = "CTS_User_Audit_Report_" + fSdf.format(fromDate) + "_to_" + fSdf.format(toDate) + ".csv";
            Filedownload.save(sb.toString().getBytes(StandardCharsets.UTF_8), "text/csv", fileName);

        } catch (Exception e) {
            e.printStackTrace();
            Messagebox.show("Failed to export Audit CSV: " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
        }
    }

    // =========================================================================
    // REPORT 2: BOD / EOD CLEARING & BATCH LIFECYCLE CSV
    // =========================================================================
 // =========================================================================
    // REPORT 2: BOD / EOD CLEARING SESSION LIFECYCLE & BATCH RECONCILIATION
    // =========================================================================
    private void exportSessionLifecycleCsv(Date fromDate, Date toDate) {
        StringBuilder sb = new StringBuilder("\uFEFF");
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        DecimalFormat df = new DecimalFormat("#,##0.00");

        sb.append("========================================================================================\n");
        sb.append("        EXPRESS CLEAR CTS - BOD / EOD CLEARING SESSION LIFECYCLE & RECONCILIATION        \n");
        sb.append("========================================================================================\n");

        // Section 1: Clearing Sessions (BOD / EOD Lifecycle)
        sb.append("Session ID,Clearing Date,Session Status,BOD Opened By,BOD Opened At,EOD Closed By,EOD Closed At,Remarks\n");

        String sessionSql = "SELECT cs.session_id, cs.clearing_date, cs.session_status, "
                          + "       COALESCE(u1.username, cs.opened_by) AS opened_by_user, "
                          + "       cs.opened_at, "
                          + "       COALESCE(u2.username, cs.closed_by) AS closed_by_user, "
                          + "       cs.closed_at, "
                          + "       COALESCE(cs.remarks, '-') AS remarks "
                          + "FROM clearing_session cs "
                          + "LEFT JOIN users u1 ON cs.opened_by = u1.user_id "
                          + "LEFT JOIN users u2 ON cs.closed_by = u2.user_id "
                          + "WHERE cs.clearing_date >= ? AND cs.clearing_date <= ? "
                          + "ORDER BY cs.clearing_date DESC, cs.session_id DESC";

        try (Connection conn = DBConnection.getConnection()) {

            try (PreparedStatement psSession = conn.prepareStatement(sessionSql)) {
                psSession.setDate(1, new java.sql.Date(fromDate.getTime()));
                psSession.setDate(2, new java.sql.Date(toDate.getTime()));

                try (ResultSet rs = psSession.executeQuery()) {
                    boolean hasSessions = false;
                    while (rs.next()) {
                        hasSessions = true;

                        String openedAt = rs.getTimestamp("opened_at") != null 
                                ? sdf.format(rs.getTimestamp("opened_at")) : "-";
                        String closedAt = rs.getTimestamp("closed_at") != null 
                                ? sdf.format(rs.getTimestamp("closed_at")) : "-";

                        String formattedOpenedAt = openedAt.equals("-") ? "\"-\"" : "=\"" + openedAt + "\"";
                        String formattedClosedAt = closedAt.equals("-") ? "\"-\"" : "=\"" + closedAt + "\"";
                        String remarksEscaped = rs.getString("remarks").replace("\"", "\"\"");

                        sb.append(String.format("\"%s\",\"%s\",\"%s\",\"%s\",%s,\"%s\",%s,\"%s\"\n",
                                rs.getString("session_id"),
                                rs.getDate("clearing_date"),
                                rs.getString("session_status"),
                                rs.getString("opened_by_user") != null ? rs.getString("opened_by_user") : "-",
                                formattedOpenedAt,
                                rs.getString("closed_by_user") != null ? rs.getString("closed_by_user") : "-",
                                formattedClosedAt,
                                remarksEscaped));
                    }
                    if (!hasSessions) {
                        sb.append("\"No clearing sessions found for the selected date range.\",,,,,,, \n");
                    }
                }
            }

            // Section 2: Outward Clearing Batches Processed
            sb.append("\n\n");
            sb.append("========================================================================================\n");
            sb.append("                     OUTWARD CLEARING BATCHES PROCESSED IN PERIOD                       \n");
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

                try (ResultSet rsBatch = psBatch.executeQuery()) {
                    boolean hasBatches = false;
                    while (rsBatch.next()) {
                        hasBatches = true;
                        String uploadTime = rsBatch.getTimestamp("uploaded_at") != null 
                                ? sdf.format(rsBatch.getTimestamp("uploaded_at")) : "-";
                        String formattedTime = uploadTime.equals("-") ? "\"-\"" : "=\"" + uploadTime + "\"";

                        sb.append(String.format("\"%s\",\"%s\",%s,\"%d\",\"%s\",\"%s\",\"%s\"\n",
                                rsBatch.getString("outward_batch_id"),
                                rsBatch.getString("batch_reference_id"),
                                formattedTime,
                                rsBatch.getInt("cheque_count"),
                                df.format(rsBatch.getDouble("total_amount")),
                                rsBatch.getString("uploaded_by"),
                                rsBatch.getString("batch_status")));
                    }
                    if (!hasBatches) {
                        sb.append("\"No outward batches found for the selected date range.\",,,,,,\n");
                    }
                }
            }

            SimpleDateFormat fSdf = new SimpleDateFormat("yyyyMMdd");
            String fileName = "CTS_BOD_EOD_Clearing_Report_" + fSdf.format(fromDate) + "_to_" + fSdf.format(toDate) + ".csv";
            Filedownload.save(sb.toString().getBytes(StandardCharsets.UTF_8), "text/csv", fileName);

        } catch (Exception e) {
            e.printStackTrace();
            Messagebox.show("Failed to export Clearing Lifecycle CSV: " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
        }
    }

    // =========================================================================
    // REUSABLE PDF EXPORTER (JASPERREPORTS)
    // =========================================================================
    private void exportReportPdf(String jrxmlRelativePath, Date fromDate, Date toDate, String filePrefix) {
        try (Connection conn = DBConnection.getConnection()) {
            String reportPath = Executions.getCurrent().getDesktop().getWebApp().getRealPath(jrxmlRelativePath);
            File jrxmlFile = new File(reportPath);

            if (!jrxmlFile.exists()) {
                // Fallback to existing consolidated jrxml if specific template doesn't exist
                reportPath = Executions.getCurrent().getDesktop().getWebApp().getRealPath("/reports/cts_consolidated_report.jrxml");
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportPath);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("FROM_DATE", new java.sql.Date(fromDate.getTime()));
            parameters.put("TO_DATE", new java.sql.Date(toDate.getTime()));
            parameters.put("GENERATED_BY", "ADMIN");

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

            SimpleDateFormat fSdf = new SimpleDateFormat("yyyyMMdd");
            String fileName = filePrefix + "_" + fSdf.format(fromDate) + "_to_" + fSdf.format(toDate) + ".pdf";

            Filedownload.save(pdfBytes, "application/pdf", fileName);

        } catch (Exception e) {
            e.printStackTrace();
            Messagebox.show("PDF export failed: " + e.getMessage(), "Export Error", Messagebox.OK, Messagebox.ERROR);
        }
    }

    private boolean validateDates(Date fromDate, Date toDate) {
        if (fromDate == null || toDate == null) {
            Messagebox.show("Please select both From Date and To Date.", "Validation", Messagebox.OK, Messagebox.EXCLAMATION);
            return false;
        }
        if (fromDate.after(toDate)) {
            Messagebox.show("From Date cannot be later than To Date.", "Validation", Messagebox.OK, Messagebox.EXCLAMATION);
            return false;
        }
        return true;
    }

    private String getSelectedReportType() {
        if (cmbReportType != null && cmbReportType.getSelectedItem() != null) {
            return cmbReportType.getSelectedItem().getValue();
        }
        return "USER_AUDIT";
    }
}