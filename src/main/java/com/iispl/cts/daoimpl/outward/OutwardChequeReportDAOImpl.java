package com.iispl.cts.daoimpl.outward;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.Date;
import java.util.HashMap;
import java.util.Map;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.OutwardChequeReportDAO;

import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;

/*
 * @author Anandhu Jayakumar
 */

public class OutwardChequeReportDAOImpl implements OutwardChequeReportDAO {

    // Report template paths
    private static final String SETTLEMENT_REPORT_PATH = "/reports/cts_checker_settlement_report.jrxml";
    private static final String REJECTIONS_REPORT_PATH = "/reports/cts_checker_rejections_report.jrxml";
    private static final String LEGACY_FALLBACK_PATH   = "/reports/outward/outward_cheque_report.jrxml";

    @Override
    public byte[] generateReport(Date fromDate, Date toDate, String reportType) throws Exception {

        // Validate date parameters
        if (fromDate == null) throw new IllegalArgumentException("From date is required.");
        if (toDate == null) throw new IllegalArgumentException("To date is required.");
        if (fromDate.after(toDate)) throw new IllegalArgumentException("From date cannot be later than To date.");

        // Resolve template path by report type
        String targetPath = "OUTWARD_REJECTIONS_AUDIT".equals(reportType) 
                ? REJECTIONS_REPORT_PATH 
                : SETTLEMENT_REPORT_PATH;

        // Load primary template with fallback
        InputStream reportStream = getClass().getResourceAsStream(targetPath);
        if (reportStream == null) {
            reportStream = getClass().getResourceAsStream(LEGACY_FALLBACK_PATH);
        }
        if (reportStream == null) {
            throw new IllegalStateException("Jasper report template not found at: " + targetPath + " or fallback: " + LEGACY_FALLBACK_PATH);
        }

        // Compile jrxml template
        JasperReport jasperReport;
        try (InputStream stream = reportStream) {
            jasperReport = JasperCompileManager.compileReport(stream);
        }

        // Populate report parameters
        Map<String, Object> parameters = new HashMap<>();
        parameters.put("FROM_DATE", fromDate);
        parameters.put("TO_DATE", toDate);
        parameters.put("GENERATED_BY", "CHECKER");
        parameters.put("GENERATION_DATE", new java.util.Date());

        // Fill report via JDBC connection and export to PDF byte array
        try (Connection connection = DBConnection.getConnection()) {
            if (connection == null) {
                throw new IllegalStateException("Database connection is null.");
            }

            JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, connection);

            if (jasperPrint.getPages().isEmpty()) {
                return new byte[0];
            }

            byte[] pdfBytes = JasperExportManager.exportReportToPdf(jasperPrint);
            if (pdfBytes == null || pdfBytes.length == 0) {
                throw new IllegalStateException("Generated PDF is empty.");
            }

            return pdfBytes;
        }
    }
}