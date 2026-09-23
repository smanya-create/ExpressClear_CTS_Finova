package com.iispl.cts.controller.outward.checker;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Radiogroup;

import com.iispl.cts.common.config.DBConnection;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

public class OutwardChequeReportController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;

	private static final String SETTLEMENT_REPORT_PATH = "/reports/cts_checker_settlement_report.jrxml";
	private static final String REJECTIONS_REPORT_PATH = "/reports/cts_checker_rejections_report.jrxml";
	private static final String LEGACY_FALLBACK_PATH = "/reports/outward/outward_cheque_report.jrxml";

	private Combobox cmbReportType;
	private Datebox dateFrom;
	private Datebox dateTo;
	private Radiogroup rgExportFormat;
	private Button btnGenerateReport;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		// Synchronize default dates with the active CTS clearing session
		LocalDate clearingDate = getClearingDate();
		java.util.Date defaultDate = java.util.Date.from(clearingDate.atStartOfDay(ZoneId.systemDefault()).toInstant());
		if (dateFrom != null) dateFrom.setValue(defaultDate);
		if (dateTo != null) dateTo.setValue(defaultDate);

		if (cmbReportType != null && cmbReportType.getItemCount() > 0) {
			cmbReportType.setSelectedIndex(0);
		}
	}

	private LocalDate getClearingDate() {
		Object sessionDateObj = Sessions.getCurrent().getAttribute("CTS_CLEARING_DATE");
		if (sessionDateObj instanceof LocalDate) {
			return (LocalDate) sessionDateObj;
		} else if (sessionDateObj instanceof Date) {
			return ((Date) sessionDateObj).toLocalDate();
		}
		return LocalDate.now();
	}

	private String getSelectedReportType() {
		if (cmbReportType != null && cmbReportType.getSelectedItem() != null) {
			return cmbReportType.getSelectedItem().getValue();
		}
		return "OUTWARD_CLEARING_SETTLEMENT";
	}

	// =========================================================================
	// UNIFIED REPORT GENERATION DISPATCHER
	// =========================================================================
	public void onClick$btnGenerateReport(Event event) {
		java.util.Date fromDate = dateFrom.getValue();
		java.util.Date toDate = dateTo.getValue();

		// 1. Strict parameter & date validations
		if (!validateDates(fromDate, toDate)) return;

		String reportType = getSelectedReportType();

		// 2. Zero-data pre-check guard (Prevents downloading blank reports)
		if (!hasCheckerReportData(reportType, fromDate, toDate)) {
			Clients.showNotification("No records found for the selected date range. Report cannot be generated.", 
					"warning", null, "top_center", 3500);
			return;
		}

		// 3. Resolve selected format from RadioGroup
		String selectedFormat = (rgExportFormat != null && rgExportFormat.getSelectedItem() != null)
				? rgExportFormat.getSelectedItem().getValue() : "PDF";

		if ("CSV".equalsIgnoreCase(selectedFormat)) {
			if ("OUTWARD_REJECTIONS_AUDIT".equals(reportType)) {
				exportRejectionAuditCsv(fromDate, toDate);
			} else {
				exportClearingSettlementCsv(fromDate, toDate);
			}
		} else {
			exportReportPdf(fromDate, toDate, reportType);
		}
	}

	// =========================================================================
	// PRE-CHECK DATA AVAILABILITY (ZERO-DATA GUARD)
	// =========================================================================
	private boolean hasCheckerReportData(String reportType, java.util.Date fromDate, java.util.Date toDate) {
		LocalDate fromLocal = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate toLocal = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		Timestamp startTs = Timestamp.valueOf(fromLocal.atStartOfDay());
		Timestamp endTs = Timestamp.valueOf(toLocal.atTime(LocalTime.MAX));

		try (Connection conn = DBConnection.getConnection()) {
			if ("OUTWARD_REJECTIONS_AUDIT".equals(reportType)) {
				String countSql = "SELECT COUNT(*) FROM outward_rejected_cheques WHERE rejected_date >= ? AND rejected_date <= ?";
				try (PreparedStatement ps = conn.prepareStatement(countSql)) {
					ps.setTimestamp(1, startTs);
					ps.setTimestamp(2, endTs);
					try (ResultSet rs = ps.executeQuery()) {
						if (rs.next() && rs.getInt(1) > 0) return true;
					}
				}
			} else {
				String countBatchSql = "SELECT COUNT(*) FROM outward_batch WHERE uploaded_at >= ? AND uploaded_at <= ?";
				try (PreparedStatement ps = conn.prepareStatement(countBatchSql)) {
					ps.setTimestamp(1, startTs);
					ps.setTimestamp(2, endTs);
					try (ResultSet rs = ps.executeQuery()) {
						if (rs.next() && rs.getInt(1) > 0) return true;
					}
				}

				String countChequeSql = "SELECT COUNT(*) FROM outward_cheque WHERE created_at >= ? AND created_at <= ?";
				try (PreparedStatement ps = conn.prepareStatement(countChequeSql)) {
					ps.setTimestamp(1, startTs);
					ps.setTimestamp(2, endTs);
					try (ResultSet rs = ps.executeQuery()) {
						if (rs.next() && rs.getInt(1) > 0) return true;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
		return false;
	}

	// =========================================================================
	// CSV 1: OUTWARD CLEARING & BATCH SETTLEMENT
	// =========================================================================
	private void exportClearingSettlementCsv(java.util.Date fromDate, java.util.Date toDate) {
		StringBuilder sb = new StringBuilder("\uFEFF");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DecimalFormat df = new DecimalFormat("#,##0.00");

		sb.append("========================================================================================\n");
		sb.append("         EXPRESS CLEAR CTS - OUTWARD CLEARING & BATCH SETTLEMENT REPORT                 \n");
		sb.append("========================================================================================\n");

		sb.append("SECTION 1: BATCH LIFECYCLE SUMMARY\n");
		sb.append("Batch ID,Reference ID,Uploaded At,Cheques,Total Amount (INR),Uploaded By,Batch Status\n");

		String batchSql = "SELECT ob.outward_batch_id, ob.batch_reference_id, ob.uploaded_at, "
				+ "       COALESCE(ob.actual_cheque_count, 0) AS cheque_count, "
				+ "       COALESCE(ob.actual_total_amount, 0.00) AS total_amount, "
				+ "       COALESCE(u.username, ob.uploaded_by) AS uploaded_by, "
				+ "       COALESCE(ob.batch_status, 'Pending') AS batch_status "
				+ "FROM outward_batch ob "
				+ "LEFT JOIN users u ON ob.uploaded_by = u.user_id "
				+ "WHERE ob.uploaded_at >= ? AND ob.uploaded_at <= ? "
				+ "ORDER BY ob.uploaded_at DESC";

		String chequeSql = "SELECT oc.outward_batch_id, oc.cheque_number, "
				+ "       COALESCE(oc.drawee_account_number, '-') AS drawee_account_number, "
				+ "       oc.cheque_amount, oc.cheque_status, oc.created_at "
				+ "FROM outward_cheque oc "
				+ "WHERE oc.created_at >= ? AND oc.created_at <= ? "
				+ "ORDER BY oc.created_at DESC";

		LocalDate fromLocal = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate toLocal = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		Timestamp startTs = Timestamp.valueOf(fromLocal.atStartOfDay());
		Timestamp endTs = Timestamp.valueOf(toLocal.atTime(LocalTime.MAX));

		try (Connection conn = DBConnection.getConnection()) {
			boolean hasData = false;

			try (PreparedStatement psBatch = conn.prepareStatement(batchSql)) {
				psBatch.setTimestamp(1, startTs);
				psBatch.setTimestamp(2, endTs);
				try (ResultSet rs = psBatch.executeQuery()) {
					while (rs.next()) {
						hasData = true;
						String upTime = rs.getTimestamp("uploaded_at") != null ? sdf.format(rs.getTimestamp("uploaded_at")) : "-";
						sb.append(String.format("\"%s\",\"%s\",=\"%s\",%d,\"%s\",\"%s\",\"%s\"\n",
								rs.getString("outward_batch_id"),
								rs.getString("batch_reference_id"),
								upTime,
								rs.getInt("cheque_count"),
								df.format(rs.getDouble("total_amount")),
								rs.getString("uploaded_by"),
								rs.getString("batch_status")));
					}
				}
			}

			sb.append("\n\nSECTION 2: PROCESSED & VERIFIED INSTRUMENTS\n");
			sb.append("Batch ID,Cheque Number,Drawee Account No,Amount (INR),Cheque Status,Created At\n");

			try (PreparedStatement psCheque = conn.prepareStatement(chequeSql)) {
				psCheque.setTimestamp(1, startTs);
				psCheque.setTimestamp(2, endTs);
				try (ResultSet rs = psCheque.executeQuery()) {
					while (rs.next()) {
						hasData = true;
						String ts = rs.getTimestamp("created_at") != null ? sdf.format(rs.getTimestamp("created_at")) : "-";
						sb.append(String.format("\"%s\",=\"%s\",=\"%s\",\"%s\",\"%s\",=\"%s\"\n",
								rs.getString("outward_batch_id"),
								rs.getString("cheque_number"),
								rs.getString("drawee_account_number"),
								df.format(rs.getDouble("cheque_amount")),
								rs.getString("cheque_status"),
								ts));
					}
				}
			}

			byte[] csvBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
			if (!hasData || isCsvEmpty(csvBytes)) {
				Clients.showNotification("No records found for the selected date range. Report cannot be generated.", 
						"warning", null, "top_center", 3500);
				return;
			}

			SimpleDateFormat fSdf = new SimpleDateFormat("yyyyMMdd");
			String fileName = "Checker_Clearing_Settlement_" + fSdf.format(fromDate) + "_to_" + fSdf.format(toDate) + ".csv";
			Filedownload.save(csvBytes, "text/csv", fileName);

		} catch (Exception e) {
			e.printStackTrace();
			Clients.showNotification("Failed to export Settlement CSV: " + e.getMessage(), "error", null, "top_center", 3500);
		}
	}

	// =========================================================================
	// CSV 2: REJECTED CHEQUES AUDIT
	// =========================================================================
	private void exportRejectionAuditCsv(java.util.Date fromDate, java.util.Date toDate) {
		StringBuilder sb = new StringBuilder("\uFEFF");
		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		DecimalFormat df = new DecimalFormat("#,##0.00");

		sb.append("========================================================================================\n");
		sb.append("              EXPRESS CLEAR CTS - OUTWARD CHEQUE REJECTION AUDIT REPORT                 \n");
		sb.append("========================================================================================\n");
		sb.append("Rejection ID,Batch ID,Cheque Number,Drawee Account No,Amount (INR),Remarks/Reason,Rejected By,Rejected Date\n");

		String sql = "SELECT rc.outward_rejected_cheque_id, oc.outward_batch_id, oc.cheque_number, "
				+ "       COALESCE(oc.drawee_account_number, '-') AS drawee_account_number, "
				+ "       COALESCE(oc.cheque_amount, 0.00) AS cheque_amount, "
				+ "       COALESCE(rc.remarks, 'Rejected by Checker') AS remarks, "
				+ "       COALESCE(u.username, rc.rejected_by) AS rejected_by_user, "
				+ "       rc.rejected_date "
				+ "FROM outward_rejected_cheques rc "
				+ "INNER JOIN outward_cheque oc ON rc.outward_cheque_id = oc.outward_cheque_id "
				+ "LEFT JOIN users u ON rc.rejected_by = u.user_id "
				+ "WHERE rc.rejected_date >= ? AND rc.rejected_date <= ? "
				+ "ORDER BY rc.rejected_date DESC";

		LocalDate fromLocal = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate toLocal = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		Timestamp startTs = Timestamp.valueOf(fromLocal.atStartOfDay());
		Timestamp endTs = Timestamp.valueOf(toLocal.atTime(LocalTime.MAX));

		try (Connection conn = DBConnection.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setTimestamp(1, startTs);
			ps.setTimestamp(2, endTs);

			boolean hasRejections = false;
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					hasRejections = true;
					String ts = rs.getTimestamp("rejected_date") != null ? sdf.format(rs.getTimestamp("rejected_date")) : "-";
					sb.append(String.format("\"%s\",\"%s\",=\"%s\",=\"%s\",\"%s\",\"%s\",\"%s\",=\"%s\"\n",
							rs.getString("outward_rejected_cheque_id"),
							rs.getString("outward_batch_id"),
							rs.getString("cheque_number"),
							rs.getString("drawee_account_number"),
							df.format(rs.getDouble("cheque_amount")),
							rs.getString("remarks").replace("\"", "\"\""),
							rs.getString("rejected_by_user"),
							ts));
				}
			}

			byte[] csvBytes = sb.toString().getBytes(StandardCharsets.UTF_8);
			if (!hasRejections || isCsvEmpty(csvBytes)) {
				Clients.showNotification("No records found for the selected date range. Report cannot be generated.", 
						"warning", null, "top_center", 3500);
				return;
			}

			SimpleDateFormat fSdf = new SimpleDateFormat("yyyyMMdd");
			String fileName = "Checker_Rejections_Audit_" + fSdf.format(fromDate) + "_to_" + fSdf.format(toDate) + ".csv";
			Filedownload.save(csvBytes, "text/csv", fileName);

		} catch (Exception e) {
			e.printStackTrace();
			Clients.showNotification("Failed to export Rejections CSV: " + e.getMessage(), "error", null, "top_center", 3500);
		}
	}

	// =========================================================================
	// PDF EXPORT (DIRECT JASPER EXECUTION)
	// =========================================================================
	private void exportReportPdf(java.util.Date fromDate, java.util.Date toDate, String reportType) {
		String targetPath = "OUTWARD_REJECTIONS_AUDIT".equals(reportType) 
				? REJECTIONS_REPORT_PATH 
				: SETTLEMENT_REPORT_PATH;

		try {
			InputStream stream = locateReportStream(targetPath);
			if (stream == null) {
				stream = locateReportStream(LEGACY_FALLBACK_PATH);
			}

			if (stream == null) {
				Clients.showNotification("Jasper report template not found at: " + targetPath, "error", null, "top_center", 3500);
				return;
			}

			JasperReport jasperReport;
			try (InputStream is = stream) {
				jasperReport = JasperCompileManager.compileReport(is);
			}

			String loggedUser = (String) Sessions.getCurrent().getAttribute("CTS_USERNAME");
			if (loggedUser == null) loggedUser = "CHECKER";

			Map<String, Object> parameters = new HashMap<>();
			parameters.put("FROM_DATE", new Date(fromDate.getTime()));
			parameters.put("TO_DATE", new Date(toDate.getTime()));
			parameters.put("GENERATED_BY", loggedUser);

			byte[] pdfBytes;
			try (Connection conn = DBConnection.getConnection()) {
				JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
				if (jasperPrint.getPages().isEmpty()) {
					Clients.showNotification("No records found for the selected date range. Report cannot be generated.", 
							"warning", null, "top_center", 3500);
					return;
				}
				pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
			}

			if (pdfBytes == null || pdfBytes.length == 0) {
				Clients.showNotification("No records found for the selected date range. Report cannot be generated.", 
						"warning", null, "top_center", 3500);
				return;
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String prefix = "OUTWARD_REJECTIONS_AUDIT".equals(reportType) ? "Checker_Rejections_Report_" : "Checker_Settlement_Report_";
			String fileName = prefix + sdf.format(fromDate) + "_to_" + sdf.format(toDate) + ".pdf";

			Filedownload.save(pdfBytes, "application/pdf", fileName);

		} catch (Exception ex) {
			ex.printStackTrace();
			Clients.showNotification("PDF export failed: " + ex.getMessage(), "error", null, "top_center", 3500);
		}
	}

	private InputStream locateReportStream(String path) {
		try {
			if (Executions.getCurrent() != null && Executions.getCurrent().getDesktop() != null
					&& Executions.getCurrent().getDesktop().getWebApp() != null) {
				String norm = path.startsWith("/") ? path : "/" + path;
				InputStream is = Executions.getCurrent().getDesktop().getWebApp().getResourceAsStream(norm);
				if (is != null) return is;
			}
		} catch (Exception ignored) {}

		try {
			String noSlash = path.startsWith("/") ? path.substring(1) : path;
			InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(noSlash);
			if (is != null) return is;
		} catch (Exception ignored) {}

		try {
			InputStream is = getClass().getResourceAsStream(path);
			if (is != null) return is;
		} catch (Exception ignored) {}

		return null;
	}

	// =========================================================================
	// STRICT BANKING DATE VALIDATION
	// =========================================================================
	private boolean validateDates(java.util.Date fromDate, java.util.Date toDate) {
		if (cmbReportType.getSelectedItem() == null) {
			Clients.showNotification("Please select a report type.", "error", cmbReportType, "top_center", 2500);
			cmbReportType.focus();
			return false;
		}
		if (fromDate == null) {
			Clients.showNotification("From Date is required.", "error", dateFrom, "top_center", 2500);
			dateFrom.focus();
			return false;
		}
		if (toDate == null) {
			Clients.showNotification("To Date is required.", "error", dateTo, "top_center", 2500);
			dateTo.focus();
			return false;
		}
		if (fromDate.after(toDate)) {
			Clients.showNotification("From Date cannot be later than To Date.", "error", dateFrom, "top_center", 3000);
			dateFrom.focus();
			return false;
		}

		// Future date restriction relative to active clearing session
		LocalDate clearingDate = getClearingDate();
		LocalDate fromLocal = fromDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
		LocalDate toLocal = toDate.toInstant().atZone(ZoneId.systemDefault()).toLocalDate();

		if (fromLocal.isAfter(clearingDate)) {
			Clients.showNotification("From Date cannot be beyond current clearing date (" + clearingDate + ").", 
					"error", dateFrom, "top_center", 3000);
			dateFrom.focus();
			return false;
		}
		if (toLocal.isAfter(clearingDate)) {
			Clients.showNotification("To Date cannot be beyond current clearing date (" + clearingDate + ").", 
					"error", dateTo, "top_center", 3000);
			dateTo.focus();
			return false;
		}

		return true;
	}

	private boolean isCsvEmpty(byte[] csvBytes) {
		String content = new String(csvBytes, StandardCharsets.UTF_8).trim();
		long lines = content.lines().count();
		return lines <= 4 || content.contains("No records found");
	}
}