package com.iispl.cts.controller.admin;

import com.iispl.cts.common.config.DBConnection;
import net.sf.jasperreports.engine.*;
import net.sf.jasperreports.engine.export.JRPdfExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput;
import net.sf.jasperreports.export.SimplePdfExporterConfiguration;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class AdminReportController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

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
        System.out.println(">>> 0. GENERATE BUTTON CLICKED");

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

            try (Connection conn = DBConnection.getConnection()) {
                System.out.println(">>> 1. Connection acquired from pool");

                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, conn);
                System.out.println(">>> 2. Report filled. Page count: " + jasperPrint.getPages().size());

                if (jasperPrint.getPages().isEmpty()) {
                    Messagebox.show("No clearing records found for the selected date range.", "No Data", Messagebox.OK, Messagebox.INFORMATION);
                    return;
                }

                // ==========================================
                // ZERO iText / ZERO FopGlyphProcessor EXPORT
                // ==========================================
                ByteArrayOutputStream htmlOut = new ByteArrayOutputStream();
                net.sf.jasperreports.engine.export.HtmlExporter htmlExporter = new net.sf.jasperreports.engine.export.HtmlExporter();
                htmlExporter.setExporterInput(new net.sf.jasperreports.export.SimpleExporterInput(jasperPrint));
                htmlExporter.setExporterOutput(new net.sf.jasperreports.export.SimpleHtmlExporterOutput(htmlOut));
                htmlExporter.exportReport();

                byte[] htmlBytes = htmlOut.toByteArray();
                System.out.println(">>> 3. HTML generated successfully, byte size: " + htmlBytes.length);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMdd");
                String fileName = "CTS_Report_" + sdf.format(fromDate) + "_to_" + sdf.format(toDate) + ".html";

                Filedownload.save(htmlBytes, "text/html", fileName);
                System.out.println(">>> 4. Download delivered to browser");
            }

        } catch (Throwable t) {
            System.err.println(">>> CRITICAL ERROR:");
            t.printStackTrace();
            Messagebox.show("Export failed: " + t.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
        }
    }

}