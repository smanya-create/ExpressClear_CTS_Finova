package com.iispl.cts.controller.outward.maker;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.service.outward.MakerReportService;
import com.iispl.cts.serviceimpl.outward.MakerReportServiceImpl;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Radiogroup;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.Date;

public class MakerReportController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    private Combobox cmbReportType;
    private Datebox dtFromDate;
    private Datebox dtToDate;
    private Radiogroup rgExportFormat;
    private Button btnGenerateReport;

    private final MakerReportService makerReportService = new MakerReportServiceImpl();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        // Default combobox to first item
        if (cmbReportType != null && cmbReportType.getItemCount() > 0) {
            cmbReportType.setSelectedIndex(0);
        }

        // Initialize default dates to the session clearing date
        LocalDate clearingDate = getClearingDate();
        Date defaultDate = Date.from(clearingDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
        if (dtFromDate != null) dtFromDate.setValue(defaultDate);
        if (dtToDate != null) dtToDate.setValue(defaultDate);
    }

    private LocalDate getClearingDate() {
        Object sessionDateObj = Sessions.getCurrent().getAttribute("CTS_CLEARING_DATE");
        if (sessionDateObj instanceof LocalDate) {
            return (LocalDate) sessionDateObj;
        } else if (sessionDateObj instanceof java.sql.Date) {
            return ((java.sql.Date) sessionDateObj).toLocalDate();
        }
        return LocalDate.now();
    }

    private String getMakerId() {
        String makerId = (String) Sessions.getCurrent().getAttribute("USER_ID");
        if (makerId == null || makerId.trim().isEmpty()) {
            makerId = (String) Sessions.getCurrent().getAttribute("CTS_USER_ID");
        }
        if (makerId == null || makerId.trim().isEmpty()) {
            makerId = (String) Sessions.getCurrent().getAttribute("userId");
        }
        if (makerId == null || makerId.trim().isEmpty()) {
            makerId = "USR1001";
        }
        return makerId;
    }

    private String getSelectedReportType() {
        if (cmbReportType != null && cmbReportType.getSelectedItem() != null) {
            return cmbReportType.getSelectedItem().getValue();
        }
        return "MICR_REPAIRS";
    }

    // =========================================================================
    // UNIFIED REPORT GENERATION DISPATCHER
    // =========================================================================
    public void onClick$btnGenerateReport(Event event) {
        Date fromDate = dtFromDate.getValue();
        Date toDate = dtToDate.getValue();

        // 1. Strict validation
        if (!validateDates(fromDate, toDate)) return;

        String makerId = getMakerId();
        String reportType = getSelectedReportType();

        // 2. Pre-check: triggers the exact warning banner if count == 0
        if (!hasMakerReportData(makerId, reportType, fromDate, toDate)) {
            Clients.showNotification("No records found for the selected date range. Report cannot be generated.", 
                    "warning", null, "top_center", 3500);
            return;
        }

        String selectedFormat = (rgExportFormat != null && rgExportFormat.getSelectedItem() != null)
                              ? rgExportFormat.getSelectedItem().getValue() : "PDF";

        SimpleDateFormat fileSdf = new SimpleDateFormat("yyyyMMdd");
        String dateSuffix = fileSdf.format(fromDate) + "_to_" + fileSdf.format(toDate);

        try {
            if ("CSV".equalsIgnoreCase(selectedFormat)) {
                byte[] csvBytes = makerReportService.generateMakerReportCsv(makerId, reportType, fromDate, toDate);
                
                // Block header-only CSVs
                if (csvBytes == null || csvBytes.length == 0 || isCsvEmpty(csvBytes)) {
                    Clients.showNotification("No records found for the selected date range. Report cannot be generated.", 
                            "warning", null, "top_center", 3500);
                    return;
                }
                
                String fileName = "Maker_" + reportType + "_" + makerId + "_" + dateSuffix + ".csv";
                Filedownload.save(csvBytes, "text/csv", fileName);

            } else {
                byte[] pdfBytes = makerReportService.generateMakerReportPdf(makerId, reportType, fromDate, toDate);
                
                if (pdfBytes == null || pdfBytes.length == 0) {
                    Clients.showNotification("No records found for the selected date range. Report cannot be generated.", 
                            "warning", null, "top_center", 3500);
                    return;
                }
                
                String fileName = "Maker_" + reportType + "_" + makerId + "_" + dateSuffix + ".pdf";
                Filedownload.save(pdfBytes, "application/pdf", fileName);
            }
        } catch (Throwable t) {
            t.printStackTrace();
            Clients.showNotification("Failed to generate report: " + t.getMessage(), "error", null, "top_center", 3500);
        }
    }

    private boolean isCsvEmpty(byte[] csvBytes) {
        String content = new String(csvBytes, java.nio.charset.StandardCharsets.UTF_8).trim();
        long lines = content.lines().count();
        return lines <= 4 || content.contains("No records found");
    }
  
 // =========================================================================
    // PRE-CHECK DATA AVAILABILITY (ZERO-DATA GUARD)
    // =========================================================================
    private boolean hasMakerReportData(String makerId, String reportType, Date fromDate, Date toDate) {
        LocalDate fromLocal = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate toLocal = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        Timestamp startTs = Timestamp.valueOf(fromLocal.atStartOfDay());
        Timestamp endTs = Timestamp.valueOf(toLocal.atTime(LocalTime.MAX));

        String sql;
        switch (reportType) {
            case "MICR_REPAIRS":
                sql = "SELECT COUNT(*) FROM scan_cheque sc " +
                      "JOIN scan_batch sb ON sc.batch_id = sb.batch_id " +
                      "WHERE UPPER(sc.micr_status) = 'REPAIRED' " +
                      "AND sb.scanned_at >= ? AND sb.scanned_at <= ?";
                break;

            case "DATA_ENTRY":
                sql = "SELECT COUNT(*) FROM scan_cheque sc " +
                      "JOIN scan_batch sb ON sc.batch_id = sb.batch_id " +
                      "WHERE UPPER(sc.cheque_status) LIKE '%DATA_ENTRY%' " +
                      "AND sb.scanned_at >= ? AND sb.scanned_at <= ?";
                break;

            case "REQUEST_REJECTED":
                sql = "SELECT COUNT(*) FROM scan_cheque sc " +
                      "JOIN scan_batch sb ON sc.batch_id = sb.batch_id " +
                      "WHERE UPPER(sc.cheque_status) LIKE '%REJECT%' " +
                      "AND sb.scanned_at >= ? AND sb.scanned_at <= ?";
                break;

            case "SUBMITTED_CHECKER":
                sql = "SELECT COUNT(*) FROM outward_batch ob " +
                      "WHERE ob.uploaded_at >= ? AND ob.uploaded_at <= ?";
                break;

            default:
                sql = "SELECT COUNT(*) FROM outward_batch ob " +
                      "WHERE ob.uploaded_at >= ? AND ob.uploaded_at <= ?";
                break;
        }

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, startTs);
            ps.setTimestamp(2, endTs);

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    int count = rs.getInt(1);
                    return count > 0;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            return false; // Crucial: Always return false on error so it never creates an empty file
        }
        return false;
    }
    // =========================================================================
    // STRICT BANKING DATE VALIDATION
    // =========================================================================
    private boolean validateDates(Date fromDate, Date toDate) {
        if (cmbReportType.getSelectedItem() == null) {
            Clients.showNotification("Please select a report type.", "error", cmbReportType, "top_center", 2500);
            cmbReportType.focus();
            return false;
        }
        if (fromDate == null) {
            Clients.showNotification("From Date is required.", "error", dtFromDate, "top_center", 2500);
            dtFromDate.focus();
            return false;
        }
        if (toDate == null) {
            Clients.showNotification("To Date is required.", "error", dtToDate, "top_center", 2500);
            dtToDate.focus();
            return false;
        }
        if (fromDate.after(toDate)) {
            Clients.showNotification("From Date cannot be later than To Date.", "error", dtFromDate, "top_center", 3000);
            dtFromDate.focus();
            return false;
        }

        // Strict future date check against active session clearing date
        LocalDate clearingDate = getClearingDate();
        LocalDate fromLocal = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
        LocalDate toLocal = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

        if (fromLocal.isAfter(clearingDate)) {
            Clients.showNotification("From Date cannot be beyond current clearing date (" + clearingDate + ").", 
                    "error", dtFromDate, "top_center", 3000);
            dtFromDate.focus();
            return false;
        }
        if (toLocal.isAfter(clearingDate)) {
            Clients.showNotification("To Date cannot be beyond current clearing date (" + clearingDate + ").", 
                    "error", dtToDate, "top_center", 3000);
            dtToDate.focus();
            return false;
        }

        return true;
    }
}