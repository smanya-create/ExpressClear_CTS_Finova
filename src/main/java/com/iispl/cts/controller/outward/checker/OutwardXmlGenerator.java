package com.iispl.cts.controller.outward.checker;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamWriter;

import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.entity.outward.OutwardCheque;

public class OutwardXmlGenerator {

    private static final String NAMESPACE =
            "urn:iso:std:iso:20022:tech:xsd:cts.cheque.clearing";

    private static final DateTimeFormatter FILE_DATE_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd");

    private static final DateTimeFormatter DATE_TIME_FORMAT =
            DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss");

    public static Path generateXml(
            OutwardBatch batch,
            List<OutwardCheque> cheques,
            String outputDirectory) throws Exception {

        String fileName =
                batch.getOutwardBatchId()
                + "_"
                + LocalDateTime.now().format(FILE_DATE_FORMAT)
                + ".xml";

        Path directory = Paths.get(outputDirectory);

        Files.createDirectories(directory);

        Path xmlFile = directory.resolve(fileName);

        XMLOutputFactory factory =
                XMLOutputFactory.newInstance();

        try (java.io.OutputStream outputStream =
                     Files.newOutputStream(xmlFile)) {

            XMLStreamWriter writer =
                    factory.createXMLStreamWriter(outputStream, "UTF-8");

            writer.writeStartDocument("UTF-8", "1.0");

            writer.writeStartElement("ChequeBatchTransmission");

            writer.writeDefaultNamespace(NAMESPACE);

            writeBatchHeader(writer, batch, cheques);

            writer.writeStartElement("Cheques");

            for (OutwardCheque cheque : cheques) {
                writeCheque(writer, cheque);
            }

            writer.writeEndElement();

            writer.writeEndElement();

            writer.writeEndDocument();

            writer.flush();
            writer.close();
        }

        return xmlFile;
    }

    private static void writeBatchHeader(
            XMLStreamWriter writer,
            OutwardBatch batch,
            List<OutwardCheque> cheques) throws Exception {

        writer.writeStartElement("BatchHeader");

        writeElement(
                writer,
                "ScannedBatchId",
                batch.getOutwardBatchId());

        writeElement(
                writer,
                "BatchReferenceId",
                batch.getBatchReferenceId());

        writeElement(
                writer,
                "ActualChequeCount",
                String.valueOf(cheques.size()));

        BigDecimal totalAmount = BigDecimal.ZERO;

        for (OutwardCheque cheque : cheques) {

            if (cheque.getChequeAmount() != null) {

                totalAmount = totalAmount.add(
                        cheque.getChequeAmount());
            }
        }

        writer.writeStartElement("ActualTotalAmount");

        writer.writeAttribute("currency", "INR");

        writer.writeCharacters(
                totalAmount.toPlainString());

        writer.writeEndElement();

        writeElement(
                writer,
                "BatchStatus",
                batch.getBatchStatus());

        writeElement(
                writer,
                "UploadedBy",
                batch.getUploadedBy());

        if (batch.getUploadedAt() != null) {

            writeElement(
                    writer,
                    "UploadedAt",
                    batch.getUploadedAt()
                            .toLocalDateTime()
                            .format(DATE_TIME_FORMAT));
        }

        writer.writeEndElement();
    }

    private static void writeCheque(
            XMLStreamWriter writer,
            OutwardCheque cheque) throws Exception {

        writer.writeStartElement("ChequeItem");

        writeElement(
                writer,
                "ScannedChequeId",
                cheque.getOutwardChequeId());

        writeElement(
                writer,
                "ChequeNumber",
                cheque.getChequeNumber());

        if (cheque.getChequeAmount() != null) {

            writer.writeStartElement("Amount");

            writer.writeAttribute("currency", "INR");

            writer.writeCharacters(
                    cheque.getChequeAmount()
                            .toPlainString());

            writer.writeEndElement();
        }

        writer.writeStartElement("MICRDetails");

        writeElement(
                writer,
                "FullMICR",
                cheque.getMicrCode());

        writer.writeEndElement();

        writer.writeStartElement("Drawee");

        writeElement(
                writer,
                "AccountHolderName",
                cheque.getDraweeName());

        writeElement(
                writer,
                "AccountNumber",
                cheque.getDraweeAccountNumber());

        writer.writeEndElement();

        writer.writeStartElement("Payee");

        writeElement(
                writer,
                "AccountHolderName",
                cheque.getPayeeName());

        writeElement(
                writer,
                "AccountNumber",
                cheque.getPayeeAccountNumber());

        writer.writeEndElement();

        writer.writeEndElement();
    }

    private static void writeElement(
            XMLStreamWriter writer,
            String name,
            String value) throws Exception {

        writer.writeStartElement(name);

        if (value != null) {
            writer.writeCharacters(value);
        }

        writer.writeEndElement();
    }
}