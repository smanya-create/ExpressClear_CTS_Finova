package com.iispl.cts.utility.inward;

import java.io.ByteArrayOutputStream;
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
import com.iispl.cts.enums.inward.InwardChequeStatus;

public class ReportXmlGenerator {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd-MM-yyyy");

    private ReportXmlGenerator() {
    }

    public static String generateRrfXml(String batchId, List<InwardReportChequeDTO> rejectedCheques) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLStreamWriter writer = null;

        try {
            XMLOutputFactory factory = XMLOutputFactory.newFactory();
            writer = factory.createXMLStreamWriter(outputStream, StandardCharsets.UTF_8.name());

            writer.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
            writer.writeCharacters("\n");
        
            writer.writeStartElement("RRFReport");
    
            writer.writeCharacters("\n    ");
            

            writer.writeCharacters("\n    ");

            int totalRejectedCheques = rejectedCheques == null ? 0 : rejectedCheques.size();
            BigDecimal totalRejectedAmount = BigDecimal.ZERO;

            if (rejectedCheques != null) {
                for (InwardReportChequeDTO cheque : rejectedCheques) {
                    if (cheque != null && cheque.getChequeAmount() != null) {
                        totalRejectedAmount = totalRejectedAmount.add(cheque.getChequeAmount());
                    }
                }
            }
            writer.writeStartElement("ReportInformation");

            writeElement(writer, "RRFReferenceNo", getRrfReferenceNo(batchId), 8);
            writeElement(writer, "BatchId", batchId, 8);
            writeElement(writer, "TotalRejectedCheques", totalRejectedCheques, 8);
            writeElement(writer, "TotalRejectedAmount", formatAmount(totalRejectedAmount), 8);
            writeElement(writer, "GeneratedDate", LocalDate.now().format(DATE_FORMATTER), 8);
            writer.writeEndElement();
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

    public static String generateConfirmationFileXml(String batchId, List<InwardReportChequeDTO> cheques) throws Exception {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        XMLStreamWriter writer = null;

        try {
            // 1. Calculate totals FIRST before writing XML elements
            int totalConfirmedCheques = 0;
            BigDecimal totalConfirmedAmount = BigDecimal.ZERO;

            if (cheques != null) {
                for (InwardReportChequeDTO cheque : cheques) {
                    if (cheque != null && InwardChequeStatus.ACCEPTED.toString().equalsIgnoreCase(cheque.getChequeStatus())) {
                        totalConfirmedCheques++;
                        BigDecimal amount = cheque.getChequeAmount() == null ? BigDecimal.ZERO : cheque.getChequeAmount();
                        totalConfirmedAmount = totalConfirmedAmount.add(amount);
                    }
                }
            }

            XMLOutputFactory factory = XMLOutputFactory.newFactory();
            writer = factory.createXMLStreamWriter(outputStream, StandardCharsets.UTF_8.name());

            writer.writeStartDocument(StandardCharsets.UTF_8.name(), "1.0");
            writer.writeCharacters("\n");

            writer.writeStartElement("ConfirmationReport");

            writer.writeCharacters("\n    ");
            writer.writeStartElement("ReportInformation");

            writeElement(writer, "BatchId", batchId, 8);
            writeElement(writer, "TotalConfirmedCheques", totalConfirmedCheques, 8);
            writeElement(writer, "TotalConfirmedAmount", formatAmount(totalConfirmedAmount), 8);
            writeElement(writer, "GeneratedAt", LocalDate.now().format(DATE_FORMATTER), 8);

            writer.writeCharacters("\n    ");
            writer.writeEndElement();

            writer.writeCharacters("\n    ");
            writer.writeStartElement("ConfirmedCheques");

            // 2. Output the cheque details
            if (cheques != null) {
                for (InwardReportChequeDTO cheque : cheques) {
                    if (cheque != null && InwardChequeStatus.ACCEPTED.toString().equalsIgnoreCase(cheque.getChequeStatus())) {
                        writeCheque(writer, cheque, false);
                    }
                }
            }

            writer.writeCharacters("\n    ");
            writer.writeEndElement();

            writer.writeCharacters("\n");
            writer.writeEndElement();
            writer.writeCharacters("\n");
            writer.writeEndDocument();
            writer.flush();

            return outputStream.toString(StandardCharsets.UTF_8.name());

        } catch (XMLStreamException e) {
            throw new RuntimeException("Error while generating Confirmation File XML", e);

        } finally {
            closeWriter(writer);
        }
    }
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

    private static void writeElement(XMLStreamWriter writer, String elementName, Object value, int indentation) throws XMLStreamException {

        writer.writeCharacters("\n");
        writer.writeCharacters(spaces(indentation));
        writer.writeStartElement(elementName);

        if (value != null) {
            writer.writeCharacters(String.valueOf(value));
        }

        writer.writeEndElement();
    }

    private static String formatAmount(BigDecimal amount) {

        if (amount == null) {
            return "0.00";
        }

        return amount.setScale(2).toPlainString();
    }

    private static String formatDate(LocalDateTime dateTime) {

        if (dateTime == null) {
            return "";
        }

        return dateTime.toLocalDate().format(DATE_FORMATTER);
    }

    private static String getRrfReferenceNo(String batchId) {
        return "RRF-" + safe(batchId);
    }

    private static String safe(String value) {
        return value == null ? "" : value;
    }

    private static String spaces(int count) {

        StringBuilder builder = new StringBuilder();

        for (int i = 0; i < count; i++) {
            builder.append(' ');
        }

        return builder.toString();
    }

    private static void closeWriter(XMLStreamWriter writer) {

        if (writer == null) {
            return;
        }

        try {
            writer.close();

        } catch (XMLStreamException e) {
           e.printStackTrace();
        }
    }
}