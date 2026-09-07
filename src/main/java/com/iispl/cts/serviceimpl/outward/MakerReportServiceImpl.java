package com.iispl.cts.serviceimpl.outward;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.service.outward.MakerReportService;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.export.HtmlExporter;
import net.sf.jasperreports.export.SimpleExporterInput;
import net.sf.jasperreports.export.SimpleHtmlExporterOutput;
import net.sf.jasperreports.export.SimpleHtmlReportConfiguration;

import org.zkoss.zk.ui.Executions;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.sql.Connection;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class MakerReportServiceImpl implements MakerReportService {

    @Override
    public byte[] generateMakerReportHtml(String makerId, Date fromDate, Date toDate) throws Exception {
        
        try (InputStream reportStream = locateReportStream("/reports/cts_maker_summary_report.jrxml")) {
            if (reportStream == null) {
                throw new IllegalStateException("Report template not found: /reports/cts_maker_summary_report.jrxml");
            }

            // 1. Compile JRXML template
            JasperReport jasperReport = JasperCompileManager.compileReport(reportStream);

            // 2. Set Parameters (Passed to sub-datasets inside JRXML)
            Map<String, Object> params = new HashMap<>();
            params.put("FROM_DATE", new java.sql.Date(fromDate.getTime()));
            params.put("TO_DATE", new java.sql.Date(toDate.getTime()));
            params.put("MAKER_ID", makerId);

            // 3. Fill Report using JDBC Connection
            try (Connection conn = DBConnection.getConnection()) {
                JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, params, conn);

                if (jasperPrint.getPages().isEmpty()) {
                    throw new RuntimeException("No records found for Maker " + makerId + " in the selected date range.");
                }

                // 4. Export to standalone HTML
                ByteArrayOutputStream htmlOut = new ByteArrayOutputStream();
                HtmlExporter exporter = new HtmlExporter();
                exporter.setExporterInput(new SimpleExporterInput(jasperPrint));
                exporter.setExporterOutput(new SimpleHtmlExporterOutput(htmlOut));

                SimpleHtmlReportConfiguration reportConfig = new SimpleHtmlReportConfiguration();
                reportConfig.setWhitePageBackground(true);
                reportConfig.setRemoveEmptySpaceBetweenRows(true);
                exporter.setConfiguration(reportConfig);

                exporter.exportReport();

                return htmlOut.toByteArray();
            }
        }
    }

    /**
     * Fallback resolver to locate JRXML in WebApp context, ClassLoader, or classpath.
     */
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