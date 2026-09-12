package com.iispl.cts.utility.inward;

import java.io.ByteArrayOutputStream;
import java.io.UnsupportedEncodingException;
import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;

import com.iispl.cts.dto.InwardReportChequeDTO;

public class ReportXmlGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");

    private ReportXmlGenerator() {
    }

    // ============================================================
    // RRF XML GENERATION
    // ============================================================

    public static String generateRrfXml(String batchId, List<InwardReportChequeDTO> rejectedCheques, String generatedBy) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLStreamWriter writer = null;

        try {
            XMLOutputFactory factory = XMLOutputFactory.newFactory();
            writer = factory.createXMLStreamWriter(outputStream, StandardCharsets.UTF_8.name());

            writer.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
            writer.writeCharacters("\n");

            // ====================================================
            // RRF REPORT
            // ====================================================

            writer.writeStartElement("RRFReport");

            // ====================================================
            // REPORT INFORMATION
            // ====================================================

            writer.writeCharacters("\n    ");
            writer.writeStartElement("ReportInformation");

            writeElement(writer, "RRFReferenceNo", generateRrfReferenceNo(batchId), 8);
            writeElement(writer, "BatchId", batchId, 8);
            writeElement(writer, "GenerationDate", LocalDate.now().format(DATE_FORMATTER), 8);
            writeElement(writer, "GeneratedBy", generatedBy, 8);

            writer.writeCharacters("\n    ");
            writer.writeEndElement();

            // ====================================================
            // SUMMARY
            // ====================================================

            int totalRejectedCheques = rejectedCheques == null ? 0 : rejectedCheques.size();
            BigDecimal totalRejectedAmount = BigDecimal.ZERO;

            if (rejectedCheques != null) {
                for (InwardReportChequeDTO cheque : rejectedCheques) {
                    if (cheque != null && cheque.getChequeAmount() != null) {
                        totalRejectedAmount = totalRejectedAmount.add(cheque.getChequeAmount());
                    }
                }
            }

            writer.writeCharacters("\n    ");
            writer.writeStartElement("Summary");

            writeElement(writer, "TotalRejectedCheques", totalRejectedCheques, 8);
            writeElement(writer, "TotalRejectedAmount", formatAmount(totalRejectedAmount), 8);

            writer.writeCharacters("\n    ");
            writer.writeEndElement();

            // ====================================================
            // REJECTED CHEQUES
            // ====================================================

            writer.writeCharacters("\n    ");
            writer.writeStartElement("RejectedCheques");

            if (rejectedCheques != null) {
                for (InwardReportChequeDTO cheque : rejectedCheques) {
                    if (cheque != null) {
                        writeCheque(writer, cheque, true);
                    }
                }
            }

            writer.writeCharacters("\n    ");
            writer.writeEndElement();

            // ====================================================
            // CLOSE RRF REPORT
            // ====================================================

            writer.writeCharacters("\n");
            writer.writeEndElement();
            writer.writeCharacters("\n");
            writer.writeEndDocument();
            writer.flush();

            return outputStream.toString(StandardCharsets.UTF_8.name());

        } catch (XMLStreamException e) {
            throw new RuntimeException("Error while generating RRF XML", e);

        } finally {
            closeWriter(writer);
        }
    }

    // ============================================================
    // BATCH SUMMARY XML GENERATION
    // ============================================================

    public static String generateBatchSummaryXml(String batchId, List<InwardReportChequeDTO> cheques, String generatedBy) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLStreamWriter writer = null;

        try {
            XMLOutputFactory factory = XMLOutputFactory.newFactory();
            writer = factory.createXMLStreamWriter(outputStream, StandardCharsets.UTF_8.name());

            writer.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
            writer.writeCharacters("\n");

            // ====================================================
            // BATCH SUMMARY REPORT
            // ====================================================

            writer.writeStartElement("BatchSummaryReport");

            // ====================================================
            // REPORT INFORMATION
            // ====================================================

            writer.writeCharacters("\n    ");
            writer.writeStartElement("ReportInformation");

            writeElement(writer, "BatchId", batchId, 8);
            writeElement(writer, "GeneratedAt", LocalDate.now().format(DATE_FORMATTER), 8);
            writeElement(writer, "GeneratedBy", generatedBy, 8);

            writer.writeCharacters("\n    ");
            writer.writeEndElement();

            // ====================================================
            // SUMMARY CALCULATION
            // ====================================================

            int totalCheques = 0;
            int approvedCheques = 0;
            int rejectedCheques = 0;

            BigDecimal totalAmount = BigDecimal.ZERO;
            BigDecimal approvedAmount = BigDecimal.ZERO;
            BigDecimal rejectedAmount = BigDecimal.ZERO;

            if (cheques != null) {
                for (InwardReportChequeDTO cheque : cheques) {

                    if (cheque == null) {
                        continue;
                    }

                    totalCheques++;

                    BigDecimal amount = cheque.getChequeAmount() == null ? BigDecimal.ZERO : cheque.getChequeAmount();
                    totalAmount = totalAmount.add(amount);

                    String status = cheque.getChequeStatus();

                    if ("ACCEPTED".equalsIgnoreCase(status)) {
                        approvedCheques++;
                        approvedAmount = approvedAmount.add(amount);

                    } else if ("REJECTED".equalsIgnoreCase(status)) {
                        rejectedCheques++;
                        rejectedAmount = rejectedAmount.add(amount);
                    }
                }
            }

            // ====================================================
            // SUMMARY
            // ====================================================

            writer.writeCharacters("\n    ");
            writer.writeStartElement("Summary");

            writeElement(writer, "TotalCheques", totalCheques, 8);
            writeElement(writer, "ApprovedCheques", approvedCheques, 8);
            writeElement(writer, "RejectedCheques", rejectedCheques, 8);
            writeElement(writer, "TotalAmount", formatAmount(totalAmount), 8);
            writeElement(writer, "ApprovedAmount", formatAmount(approvedAmount), 8);
            writeElement(writer, "RejectedAmount", formatAmount(rejectedAmount), 8);

            writer.writeCharacters("\n    ");
            writer.writeEndElement();

            // ====================================================
            // APPROVED CHEQUES
            // ====================================================

            writer.writeCharacters("\n    ");
            writer.writeStartElement("ApprovedCheques");

            if (cheques != null) {
                for (InwardReportChequeDTO cheque : cheques) {

                    if (cheque == null) {
                        continue;
                    }

                    if ("ACCEPTED".equalsIgnoreCase(cheque.getChequeStatus())) {
                        writeCheque(writer, cheque, false);
                    }
                }
            }

            writer.writeCharacters("\n    ");
            writer.writeEndElement();

            // ====================================================
            // REJECTED CHEQUES
            // ====================================================

            writer.writeCharacters("\n    ");
            writer.writeStartElement("RejectedCheques");

            if (cheques != null) {
                for (InwardReportChequeDTO cheque : cheques) {

                    if (cheque == null) {
                        continue;
                    }

                    if ("REJECTED".equalsIgnoreCase(cheque.getChequeStatus())) {
                        writeCheque(writer, cheque, true);
                    }
                }
            }

            writer.writeCharacters("\n    ");
            writer.writeEndElement();

            // ====================================================
            // CLOSE BATCH SUMMARY REPORT
            // ====================================================

            writer.writeCharacters("\n");
            writer.writeEndElement();
            writer.writeCharacters("\n");
            writer.writeEndDocument();
            writer.flush();

            return outputStream.toString(StandardCharsets.UTF_8.name());

        } catch (XMLStreamException e) {
            throw new RuntimeException("Error while generating Batch Summary XML", e);

        } finally {
            closeWriter(writer);
        }
    }

    // ============================================================
    // WRITE CHEQUE
    // ============================================================

    private static void writeCheque(XMLStreamWriter writer, InwardReportChequeDTO cheque, boolean includeRejection) throws XMLStreamException {

        writer.writeCharacters("\n        ");
        writer.writeStartElement("Cheque");

        writeElement(writer, "ChequeId", cheque.getInwardChequeId(), 12);
        writeElement(writer, "ChequeNumber", cheque.getChequeNumber(), 12);
        writeElement(writer, "MICRCode", cheque.getMicrCode(), 12);
        writeElement(writer, "DraweeName", cheque.getDraweeName(), 12);
        writeElement(writer, "DraweeAccountNumber", cheque.getDraweeAccountNumber(), 12);
        writeElement(writer, "DraweeBank", cheque.getDraweeBank(), 12);
        writeElement(writer, "PayeeName", cheque.getPayeeName(), 12);
        writeElement(writer, "PayeeAccountNumber", cheque.getPayeeAccountNumber(), 12);
        writeElement(writer, "PresentingBank", cheque.getPresentingBank(), 12);
        writeElement(writer, "ChequeAmount", formatAmount(cheque.getChequeAmount()), 12);
        writeElement(writer, "ChequeDate", formatDate(cheque.getChequeDate()), 12);
        writeElement(writer, "Status", cheque.getChequeStatus(), 12);

        if (includeRejection) {

            writer.writeCharacters("\n            ");
            writer.writeStartElement("Rejection");

            writeElement(writer, "ReasonCode", cheque.getRejectedReasonCode(), 16);
            writeElement(writer, "Reason", cheque.getRejectedReasonName(), 16);
            writeElement(writer, "Remarks", cheque.getRemarks(), 16);
            writeElement(writer, "RejectedBy", cheque.getRejectedBy(), 16);
            writeElement(writer, "RejectedAt", formatDate(cheque.getRejectedAt()), 16);

            writer.writeCharacters("\n            ");
            writer.writeEndElement();
        }

        writer.writeCharacters("\n        ");
        writer.writeEndElement();
    }

    // ============================================================
    // WRITE XML ELEMENT
    // ============================================================

    private static void writeElement(XMLStreamWriter writer, String elementName, Object value, int indentation) throws XMLStreamException {

        writer.writeCharacters("\n");
        writer.writeCharacters(spaces(indentation));
        writer.writeStartElement(elementName);

        if (value != null) {
            writer.writeCharacters(String.valueOf(value));
        }

        writer.writeEndElement();
    }

    // ============================================================
    // FORMAT AMOUNT
    // ============================================================

    private static String formatAmount(BigDecimal amount) {

        if (amount == null) {
            return "0.00";
        }

        return amount.setScale(2).toPlainString();
    }

    // ============================================================
    // FORMAT DATE
    // ============================================================

    private static String formatDate(LocalDateTime dateTime) {

        if (dateTime == null) {
            return "";
        }

        return dateTime.toLocalDate().format(DATE_FORMATTER);
    }

    // ============================================================
    // GENERATE RRF REFERENCE NUMBER
    // ============================================================

    private static String generateRrfReferenceNo(String batchId) {
        return "RRF-" + safe(batchId);
    }

    // ============================================================
    // NULL SAFE VALUE
    // ============================================================

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    // ============================================================
    // CREATE SPACES
    // ============================================================

    private static String spaces(int count) {

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < count; i++) {
            builder.append(' ');
        }

        return builder.toString();
    }

    // ============================================================
    // CLOSE XML WRITER
    // ============================================================

    private static void closeWriter(XMLStreamWriter writer) {

        if (writer == null) {
            return;
        }

        try {
            writer.close();

        } catch (XMLStreamException e) {
            // Ignore close exception
        }
    }
}