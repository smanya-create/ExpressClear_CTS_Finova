package com.iispl.cts.controller.outward.checker;

import java.io.InputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import com.iispl.cts.common.config.DBConnection;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

public class OutwardChequeReportController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;

	/*
	 * ============================================================ REPORT PATH
	 * ============================================================
	 *
	 * Your JRXML file is:
	 *
	 * src/main/webapp/ reports/ outward/ outward_cheque_report.jrxml
	 *
	 */
	private static final String REPORT_PATH = "/reports/outward/outward_cheque_report.jrxml";

	/*
	 * ============================================================ ZUL COMPONENTS
	 * ============================================================
	 */

	private Datebox dateFrom;
	private Datebox dateTo;

	private Button btnGenerateReport;

	/*
	 * ============================================================ AFTER COMPOSE
	 * ============================================================
	 */

	@Override
	public void doAfterCompose(Component comp) throws Exception {

		super.doAfterCompose(comp);

		/*
		 * Set today's date by default
		 */
		Date today = new Date();

		if (dateFrom != null) {
			dateFrom.setValue(today);
		}

		if (dateTo != null) {
			dateTo.setValue(today);
		}

		System.out.println("======================================");

		System.out.println(" OUTWARD CHEQUE REPORT CONTROLLER");

		System.out.println(" Controller initialized successfully");

		System.out.println(" Report Path : " + REPORT_PATH);

		System.out.println("======================================");
	}

	/*
	 * ============================================================ FIND JRXML FILE
	 * ============================================================
	 *
	 * First tries ZK WebApp because your JRXML is inside:
	 *
	 * src/main/webapp/reports/outward/
	 *
	 * Then tries classloader as fallback.
	 *
	 */

	private InputStream locateReportStream(String reportPath) {

		/*
		 * -------------------------------------------------------- METHOD 1 ZK WebApp
		 * resource --------------------------------------------------------
		 */

		try {

			if (Executions.getCurrent() != null && Executions.getCurrent().getDesktop() != null
					&& Executions.getCurrent().getDesktop().getWebApp() != null) {

				String normalizedPath;

				if (reportPath.startsWith("/")) {
					normalizedPath = reportPath;
				} else {
					normalizedPath = "/" + reportPath;
				}

				InputStream stream = Executions.getCurrent().getDesktop().getWebApp()
						.getResourceAsStream(normalizedPath);

				if (stream != null) {

					System.out.println("JRXML FOUND USING ZK WEBAPP:");

					System.out.println(normalizedPath);

					return stream;
				}
			}

		} catch (Exception e) {

			System.out.println("ZK WebApp resource lookup failed.");

			e.printStackTrace();
		}

		/*
		 * -------------------------------------------------------- METHOD 2 Context
		 * ClassLoader --------------------------------------------------------
		 */

		try {

			String noSlashPath;

			if (reportPath.startsWith("/")) {
				noSlashPath = reportPath.substring(1);
			} else {
				noSlashPath = reportPath;
			}

			InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(noSlashPath);

			if (stream != null) {

				System.out.println("JRXML FOUND USING CONTEXT CLASSLOADER:");

				System.out.println(noSlashPath);

				return stream;
			}

		} catch (Exception e) {

			System.out.println("Context ClassLoader lookup failed.");

			e.printStackTrace();
		}

		/*
		 * -------------------------------------------------------- METHOD 3 Controller
		 * ClassLoader --------------------------------------------------------
		 */

		try {

			String noSlashPath;

			if (reportPath.startsWith("/")) {
				noSlashPath = reportPath.substring(1);
			} else {
				noSlashPath = reportPath;
			}

			InputStream stream = OutwardChequeReportController.class.getClassLoader().getResourceAsStream(noSlashPath);

			if (stream != null) {

				System.out.println("JRXML FOUND USING CLASSLOADER:");

				System.out.println(noSlashPath);

				return stream;
			}

		} catch (Exception e) {

			System.out.println("ClassLoader lookup failed.");

			e.printStackTrace();
		}

		/*
		 * -------------------------------------------------------- METHOD 4
		 * getResourceAsStream --------------------------------------------------------
		 */

		try {

			String withSlashPath;

			if (reportPath.startsWith("/")) {
				withSlashPath = reportPath;
			} else {
				withSlashPath = "/" + reportPath;
			}

			InputStream stream = getClass().getResourceAsStream(withSlashPath);

			if (stream != null) {

				System.out.println("JRXML FOUND USING getResourceAsStream:");

				System.out.println(withSlashPath);

				return stream;
			}

		} catch (Exception e) {

			System.out.println("getResourceAsStream lookup failed.");

			e.printStackTrace();
		}

		/*
		 * -------------------------------------------------------- NOT FOUND
		 * --------------------------------------------------------
		 */

		System.out.println("======================================");

		System.out.println("JRXML FILE NOT FOUND");

		System.out.println("Expected Path:");

		System.out.println(REPORT_PATH);

		System.out.println("======================================");

		return null;
	}

	/*
	 * ============================================================ GENERATE REPORT
	 * ============================================================
	 */

	public void onClick$btnGenerateReport() {

		Date fromDate = null;
		Date toDate = null;

		/*
		 * -------------------------------------------------------- GET FROM DATE
		 * --------------------------------------------------------
		 */

		if (dateFrom != null) {
			fromDate = dateFrom.getValue();
		}

		/*
		 * -------------------------------------------------------- GET TO DATE
		 * --------------------------------------------------------
		 */

		if (dateTo != null) {
			toDate = dateTo.getValue();
		}

		/*
		 * -------------------------------------------------------- VALIDATION
		 * --------------------------------------------------------
		 */

		if (fromDate == null || toDate == null) {

			Messagebox.show("Please select both From Date and To Date.", "Validation Error", Messagebox.OK,
					Messagebox.EXCLAMATION);

			return;
		}

		/*
		 * From date cannot be greater than To date
		 */

		if (fromDate.after(toDate)) {

			Messagebox.show("From Date cannot be later than To Date.", "Validation Error", Messagebox.OK,
					Messagebox.EXCLAMATION);

			return;
		}

		/*
		 * Disable button while generating report
		 */

		if (btnGenerateReport != null) {
			btnGenerateReport.setDisabled(true);
		}

		/*
		 * ======================================================== GENERATE REPORT
		 * ========================================================
		 */

		try {

			System.out.println("======================================");

			System.out.println("STARTING OUTWARD CHEQUE REPORT");

			System.out.println("From Date : " + fromDate);

			System.out.println("To Date   : " + toDate);

			System.out.println("======================================");

			/*
			 * ---------------------------------------------------- LOAD JRXML
			 * ----------------------------------------------------
			 */

			InputStream reportStream = locateReportStream(REPORT_PATH);

			if (reportStream == null) {

				Messagebox.show("Report template not found.\n\n" + "Expected location:\n" + REPORT_PATH + "\n\n"
						+ "Please check whether the JRXML " + "file is present in the deployed " + "ZK application.",
						"Template Missing", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			/*
			 * ---------------------------------------------------- COMPILE JRXML
			 * ----------------------------------------------------
			 */

			JasperReport jasperReport;

			try (InputStream stream = reportStream) {

				System.out.println("Compiling JRXML...");

				jasperReport = JasperCompileManager.compileReport(stream);
			}

			System.out.println("JRXML compiled successfully.");

			/*
			 * ---------------------------------------------------- PARAMETERS
			 * ----------------------------------------------------
			 */

			Map<String, Object> parameters = new HashMap<>();

			java.sql.Date sqlFromDate = new java.sql.Date(fromDate.getTime());

			java.sql.Date sqlToDate = new java.sql.Date(toDate.getTime());

			parameters.put("FROM_DATE", sqlFromDate);

			parameters.put("TO_DATE", sqlToDate);

			parameters.put("GENERATED_BY", "CHECKER");

			parameters.put("GENERATION_DATE", new Date());

			System.out.println("Jasper parameters created.");

			/*
			 * ---------------------------------------------------- DATABASE CONNECTION
			 * ----------------------------------------------------
			 */

			try (Connection conn = DBConnection.getConnection()) {

				if (conn == null) {

					throw new IllegalStateException("Database connection is null.");
				}

				System.out.println("Database connection established.");

				/*
				 * ------------------------------------------------ FILL REPORT
				 * ------------------------------------------------
				 */

				JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);

				System.out.println("Jasper report filled successfully.");

				/*
				 * ------------------------------------------------ CHECK DATA
				 * ------------------------------------------------
				 */

				if (jasperPrint.getPages().isEmpty()) {

					Messagebox.show("No outward cheque records found " + "for the selected date range.", "No Data",
							Messagebox.OK, Messagebox.INFORMATION);

					return;
				}

				/*
				 * ------------------------------------------------ EXPORT PDF
				 * ------------------------------------------------
				 */

				byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

				if (pdfBytes == null || pdfBytes.length == 0) {

					throw new IllegalStateException("Generated PDF is empty.");
				}

				System.out.println("PDF generated successfully.");

				System.out.println("PDF size: " + pdfBytes.length + " bytes");

				/*
				 * ------------------------------------------------ FILE NAME
				 * ------------------------------------------------
				 */

				SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");

				String fileName = "Outward_Cheque_Report_" + sdf.format(fromDate) + "_to_" + sdf.format(toDate)
						+ ".pdf";

				/*
				 * ------------------------------------------------ DOWNLOAD PDF
				 * ------------------------------------------------
				 */

				Filedownload.save(pdfBytes, "application/pdf", fileName);

				System.out.println("Download started successfully.");

				System.out.println("File Name: " + fileName);

			}

		} catch (Exception ex) {

			/*
			 * ==================================================== ERROR HANDLING
			 * ====================================================
			 */

			ex.printStackTrace();

			Throwable root = ex;

			while (root.getCause() != null) {
				root = root.getCause();
			}

			System.err.println("======================================");

			System.err.println("OUTWARD REPORT ERROR");

			System.err.println("======================================");

			System.err.println("Exception : " + ex.getClass().getName());

			System.err.println("Message   : " + ex.getMessage());

			System.err.println("Root Cause: " + root.getClass().getName());

			System.err.println("Root Msg  : " + root.getMessage());

			System.err.println("======================================");

			/*
			 * ---------------------------------------------------- GET ERROR MESSAGE
			 * ----------------------------------------------------
			 */

			String errorMessage = root.getMessage();

			if (errorMessage == null || errorMessage.trim().isEmpty()) {

				errorMessage = ex.getMessage();
			}

			if (errorMessage == null || errorMessage.trim().isEmpty()) {

				errorMessage = ex.getClass().getSimpleName();
			}

			/*
			 * ---------------------------------------------------- SPECIAL MESSAGE FOR
			 * MISSING DIGESTER ----------------------------------------------------
			 */

			if (errorMessage.contains("org/apache/commons/digester/Digester")
					|| errorMessage.contains("commons.digester")) {

				errorMessage = "Apache Commons Digester is " + "missing from the application.\n\n"
						+ "Please add the required " + "Commons Digester JAR to:\n" + "WEB-INF/lib\n\n"
						+ "Your JasperReports JAR requires " + "this dependency.";
			}

			Messagebox.show("Unable to generate report.\n\n" + errorMessage, "Report Error", Messagebox.OK,
					Messagebox.ERROR);

		} finally {

			/*
			 * Re-enable button
			 */

			if (btnGenerateReport != null) {
				btnGenerateReport.setDisabled(false);
			}
			
		}
	}
}