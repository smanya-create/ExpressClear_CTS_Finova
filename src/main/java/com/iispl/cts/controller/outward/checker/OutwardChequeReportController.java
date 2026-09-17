package com.iispl.cts.controller.outward.checker;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.common.util.SecurityUtil;

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
	private Button btnExportCsv;
	private Button btnExportPdf;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		
		super.doAfterCompose(comp);

		java.util.Date today = new java.util.Date();
		if (dateFrom != null) dateFrom.setValue(today);
		if (dateTo != null) dateTo.setValue(today);

		if (cmbReportType != null && cmbReportType.getItemCount() > 0) {
			cmbReportType.setSelectedIndex(0);
		}
	}

	// =========================================================================
	// 1. CSV EXPORT DISPATCHER
	// =========================================================================
	public void onClick$btnExportCsv() {
		java.util.Date fromDate = dateFrom.getValue();
		java.util.Date toDate = dateTo.getValue();

		if (!validateDates(fromDate, toDate)) return;

		String reportType = getSelectedReportType();
		if ("OUTWARD_REJECTIONS_AUDIT".equals(reportType)) {
			exportRejectionAuditCsv(fromDate, toDate);
		} else {
			exportClearingSettlementCsv(fromDate, toDate);
		}
	}

	// CSV 1: OUTWARD CLEARING & BATCH SETTLEMENT
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
				+ "WHERE ob.uploaded_at::date >= ? AND ob.uploaded_at::date <= ? "
				+ "ORDER BY ob.uploaded_at DESC";

		String chequeSql = "SELECT oc.outward_batch_id, oc.cheque_number, "
				+ "       COALESCE(oc.drawee_account_number, '-') AS drawee_account_number, "
				+ "       oc.cheque_amount, oc.cheque_status, oc.created_at "
				+ "FROM outward_cheque oc "
				+ "WHERE oc.created_at::date >= ? AND oc.created_at::date <= ? "
				+ "ORDER BY oc.created_at DESC";

		try (Connection conn = DBConnection.getConnection()) {
			Date sqlFrom = new Date(fromDate.getTime());
			Date sqlTo = new Date(toDate.getTime());

			try (PreparedStatement psBatch = conn.prepareStatement(batchSql)) {
				psBatch.setDate(1, sqlFrom);
				psBatch.setDate(2, sqlTo);
				try (ResultSet rs = psBatch.executeQuery()) {
					boolean hasBatches = false;
					while (rs.next()) {
						hasBatches = true;
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
					if (!hasBatches) sb.append("\"No outward batches found for the selected date range.\",,,,,,\n");
				}
			}

			sb.append("\n\nSECTION 2: PROCESSED & VERIFIED INSTRUMENTS\n");
			sb.append("Batch ID,Cheque Number,Drawee Account No,Amount (INR),Cheque Status,Created At\n");

			try (PreparedStatement psCheque = conn.prepareStatement(chequeSql)) {
				psCheque.setDate(1, sqlFrom);
				psCheque.setDate(2, sqlTo);
				try (ResultSet rs = psCheque.executeQuery()) {
					boolean hasCheques = false;
					while (rs.next()) {
						hasCheques = true;
						String ts = rs.getTimestamp("created_at") != null ? sdf.format(rs.getTimestamp("created_at")) : "-";
						sb.append(String.format("\"%s\",=\"%s\",=\"%s\",\"%s\",\"%s\",=\"%s\"\n",
								rs.getString("outward_batch_id"),
								rs.getString("cheque_number"),
								rs.getString("drawee_account_number"),
								df.format(rs.getDouble("cheque_amount")),
								rs.getString("cheque_status"),
								ts));
					}
					if (!hasCheques) sb.append("\"No outward cheques found for the selected date range.\",,,,,\n");
				}
			}

			SimpleDateFormat fSdf = new SimpleDateFormat("yyyyMMdd");
			String fileName = "Checker_Clearing_Settlement_" + fSdf.format(fromDate) + "_to_" + fSdf.format(toDate) + ".csv";
			Filedownload.save(sb.toString().getBytes(StandardCharsets.UTF_8), "text/csv", fileName);

		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("Failed to export Settlement CSV: " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
		}
	}

	// CSV 2: REJECTED CHEQUES AUDIT (JOINED WITH OUTWARD_CHEQUE)
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
				+ "WHERE rc.rejected_date::date >= ? AND rc.rejected_date::date <= ? "
				+ "ORDER BY rc.rejected_date DESC";

		try (Connection conn = DBConnection.getConnection();
		     PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setDate(1, new Date(fromDate.getTime()));
			ps.setDate(2, new Date(toDate.getTime()));

			try (ResultSet rs = ps.executeQuery()) {
				boolean hasRejections = false;
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
				if (!hasRejections) sb.append("\"No rejected cheques found for the selected date range.\",,,,,,, \n");
			}

			SimpleDateFormat fSdf = new SimpleDateFormat("yyyyMMdd");
			String fileName = "Checker_Rejections_Audit_" + fSdf.format(fromDate) + "_to_" + fSdf.format(toDate) + ".csv";
			Filedownload.save(sb.toString().getBytes(StandardCharsets.UTF_8), "text/csv", fileName);

		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("Failed to export Rejections CSV: " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
		}
	}

	// =========================================================================
	// 2. PDF EXPORT (DIRECT MULTI-LOADER JASPER EXECUTION)
	// =========================================================================
	public void onClick$btnExportPdf() {
		java.util.Date fromDate = dateFrom.getValue();
		java.util.Date toDate = dateTo.getValue();

		if (!validateDates(fromDate, toDate)) return;

		String reportType = getSelectedReportType();
		String targetPath = "OUTWARD_REJECTIONS_AUDIT".equals(reportType) 
				? REJECTIONS_REPORT_PATH 
				: SETTLEMENT_REPORT_PATH;

		try {
			if (btnExportPdf != null) btnExportPdf.setDisabled(true);

			InputStream stream = locateReportStream(targetPath);
			if (stream == null) {
				stream = locateReportStream(LEGACY_FALLBACK_PATH);
			}

			if (stream == null) {
				Messagebox.show("Jasper report template not found at:\n" + targetPath 
						+ "\n\nPlease ensure the .jrxml is deployed in src/main/webapp/reports/", 
						"Template Not Found", Messagebox.OK, Messagebox.ERROR);
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
					Messagebox.show("No records found for the selected date range.", "No Data", Messagebox.OK, Messagebox.INFORMATION);
					return;
				}
				pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
			}

			SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
			String prefix = "OUTWARD_REJECTIONS_AUDIT".equals(reportType) ? "Checker_Rejections_Report_" : "Checker_Settlement_Report_";
			String fileName = prefix + sdf.format(fromDate) + "_to_" + sdf.format(toDate) + ".pdf";

			Filedownload.save(pdfBytes, "application/pdf", fileName);

		} catch (Exception ex) {
			ex.printStackTrace();
			Messagebox.show("PDF export failed: " + ex.getMessage(), "Export Error", Messagebox.OK, Messagebox.ERROR);
		} finally {
			if (btnExportPdf != null) btnExportPdf.setDisabled(false);
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

	private boolean validateDates(java.util.Date fromDate, java.util.Date toDate) {
		if (fromDate == null || toDate == null) {
			Messagebox.show("Please select both From Date and To Date.", "Validation Error", Messagebox.OK, Messagebox.EXCLAMATION);
			return false;
		}
		if (fromDate.after(toDate)) {
			Messagebox.show("From Date cannot be later than To Date.", "Validation Error", Messagebox.OK, Messagebox.EXCLAMATION);
			return false;
		}
		return true;
	}

	private String getSelectedReportType() {
		if (cmbReportType != null && cmbReportType.getSelectedItem() != null) {
			return cmbReportType.getSelectedItem().getValue();
		}
		return "OUTWARD_CLEARING_SETTLEMENT";
	}
}