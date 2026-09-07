package com.iispl.cts.controller.admin;

import com.iispl.cts.common.config.DBConnection;
import net.sf.jasperreports.engine.*;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import java.io.InputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AdminReportController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // Auto-wired by id from reports.zul
    private Datebox dtFromDate;
    private Datebox dtToDate;
    private Button btnGenerate;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        Date today = new Date();
        if (dtFromDate != null) {
            dtFromDate.setValue(today);
        }
        if (dtToDate != null) {
            dtToDate.setValue(today);
        }
    }

    private InputStream locateReportStream(String reportPath) {
        try {
            if (Executions.getCurrent() != null && Executions.getCurrent().getDesktop() != null) {
                InputStream webStream = Executions.getCurrent()
                        .getDesktop()
                        .getWebApp()
                        .getResourceAsStream(reportPath.startsWith("/") ? reportPath : "/" + reportPath);
                if (webStream != null) {
                    return webStream;
                }
            }
        } catch (Exception ignored) {
        }

        String noSlashPath = reportPath.startsWith("/") ? reportPath.substring(1) : reportPath;
        InputStream stream = Thread.currentThread().getContextClassLoader().getResourceAsStream(noSlashPath);
        if (stream != null) {
            return stream;
        }

        stream = AdminReportController.class.getClassLoader().getResourceAsStream(noSlashPath);
        if (stream != null) {
            return stream;
        }

        String withSlashPath = reportPath.startsWith("/") ? reportPath : "/" + reportPath;
        return getClass().getResourceAsStream(withSlashPath);
    }

    public void onClick$btnGenerate() {
        Date fromDate = dtFromDate.getValue();
        Date toDate = dtToDate.getValue();

        if (fromDate == null || toDate == null) {
            Messagebox.show("Please select both From Date and To Date.", "Validation Error", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        if (fromDate.after(toDate)) {
            Messagebox.show("From Date cannot be later than To Date.", "Validation Error", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        try (InputStream reportStream = locateReportStream("/reports/cts_consolidated_report.jrxml")) {
            if (reportStream == null) {
                Messagebox.show(
                    "Report template not found under /reports/cts_consolidated_report.jrxml",
                    "Template Missing", Messagebox.OK, Messagebox.ERROR
                );
                return;
            }

            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            Map<String, Object> parameters = new HashMap<>();
            parameters.put("FROM_DATE", new java.sql.Date(fromDate.getTime()));
            parameters.put("TO_DATE", new java.sql.Date(toDate.getTime()));
            parameters.put("GENERATED_BY", "ADMIN");

            // Direct HikariCP pool connection via DBConnection
            try (Connection conn = DBConnection.getConnection()) {
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);

                if (jasperPrint.getPages().isEmpty()) {
                    Messagebox.show("No clearing records found for the selected date range.", "No Data", Messagebox.OK, Messagebox.INFORMATION);
                    return;
                }
                System.out.println("=== PHRASE LOADED FROM: " + 
                	    com.lowagie.text.Phrase.class.getProtectionDomain().getCodeSource().getLocation());

                byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String fileName = "CTS_Report_" + sdf.format(fromDate) + "_to_" + sdf.format(toDate) + ".pdf";

                Filedownload.save(pdfBytes, "application/pdf", fileName);
            }

        } catch (Exception ex) {
            ex.printStackTrace();
            Throwable root = ex;
            while (root.getCause() != null) {
                root = root.getCause();
            }
            System.err.println("DATABASE ERROR ROOT CAUSE: " + root.getMessage());
            Messagebox.show("SQL Error: " + root.getMessage(), "Database Error", Messagebox.OK, Messagebox.ERROR);
        }
    }
}