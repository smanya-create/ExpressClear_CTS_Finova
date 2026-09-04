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

	private static final String NAMESPACE = "urn:iso:std:iso:20022:tech:xsd:cts.cheque.clearing";

	private static final String INDENT = "    ";

	public static Path generateXml(OutwardBatch batch, List<OutwardCheque> cheques, String outputDirectory)
			throws Exception {

		String fileName = "bxf_" + batch.getOutwardBatchId() + "_"
				+ LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd")) + ".xml";

		Path directory = Paths.get(outputDirectory);

		if (!Files.exists(directory)) {
			Files.createDirectories(directory);
		}

		Path xmlFile = directory.resolve(fileName);

		XMLOutputFactory factory = XMLOutputFactory.newInstance();

		XMLStreamWriter writer = factory.createXMLStreamWriter(Files.newOutputStream(xmlFile), "UTF-8");

		writer.writeStartDocument("UTF-8", "1.0");

		writer.writeStartElement("ChequeBatchTransmission");
		writer.writeDefaultNamespace(NAMESPACE);
		writer.writeCharacters("\n");

		writeBatchHeader(writer, batch, cheques);

		writer.writeCharacters("\n");

		writeIndent(writer, 0);
		writer.writeStartElement("Cheques");
		writer.writeCharacters("\n");

		for (OutwardCheque cheque : cheques) {
			writeCheque(writer, cheque);
			writer.writeCharacters("\n");
		}

		writeIndent(writer, 0);
		writer.writeEndElement();

		writer.writeCharacters("\n");

		writeIndent(writer, 0);
		writer.writeEndElement();

		writer.writeCharacters("\n");

		writer.writeEndDocument();

		writer.flush();
		writer.close();

		return xmlFile;
	}

	private static void writeBatchHeader(XMLStreamWriter writer, OutwardBatch batch, List<OutwardCheque> cheques)
			throws Exception {

		writeIndent(writer, 1);
		writer.writeStartElement("BatchHeader");
		writer.writeCharacters("\n");

		writeSimpleElement(writer, 2, "ScannedBatchId", batch.getOutwardBatchId());

		writeSimpleElement(writer, 2, "BatchReferenceId", batch.getBatchReferenceId());

		writeSimpleElement(writer, 2, "ActualChequeCount", String.valueOf(cheques.size()));

		BigDecimal totalAmount = BigDecimal.ZERO;

		for (OutwardCheque cheque : cheques) {
			if (cheque.getChequeAmount() != null) {
				totalAmount = totalAmount.add(cheque.getChequeAmount());
			}
		}

		writeIndent(writer, 2);
		writer.writeStartElement("ActualTotalAmount");
		writer.writeAttribute("currency", "INR");
		writer.writeCharacters(totalAmount.toPlainString());
		writer.writeEndElement();
		writer.writeCharacters("\n");

		writeSimpleElement(writer, 2, "BatchStatus", batch.getBatchStatus());

		writeSimpleElement(writer, 2, "UploadedBy", batch.getUploadedBy());

		if (batch.getUploadedAt() != null) {
			writeSimpleElement(writer, 2, "UploadedAt", batch.getUploadedAt().toLocalDateTime()
					.format(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")));
		}

		writeIndent(writer, 1);
		writer.writeEndElement();
	}

	private static void writeCheque(XMLStreamWriter writer, OutwardCheque cheque) throws Exception {

		writeIndent(writer, 1);
		writer.writeStartElement("ChequeItem");
		writer.writeCharacters("\n");

		writeSimpleElement(writer, 2, "ScannedChequeId", cheque.getOutwardChequeId());

		writeSimpleElement(writer, 2, "ChequeNumber", cheque.getChequeNumber());

		if (cheque.getChequeDate() != null) {
			writeSimpleElement(writer, 2, "ChequeDate", cheque.getChequeDate().toString());
		}

		if (cheque.getChequeAmount() != null) {
			writeIndent(writer, 2);
			writer.writeStartElement("Amount");
			writer.writeAttribute("currency", "INR");
			writer.writeCharacters(cheque.getChequeAmount().toPlainString());
			writer.writeEndElement();
			writer.writeCharacters("\n");
		}

		writeSimpleElement(writer, 2, "ChequeStatus", cheque.getChequeStatus());

		writeIndent(writer, 2);
		writer.writeStartElement("MICRDetails");
		writer.writeCharacters("\n");

		writeSimpleElement(writer, 3, "FullMICR", cheque.getMicrCode());

//		writeSimpleElement(writer, 3, "CityCode", cheque.getCityCode());
//
//		writeSimpleElement(writer, 3, "BankCode", cheque.getBankCode());
//
//		writeSimpleElement(writer, 3, "BranchCode", cheque.getBranchCode());

		writeIndent(writer, 2);
		writer.writeEndElement();
		writer.writeCharacters("\n");

		writeIndent(writer, 2);
		writer.writeStartElement("Drawee");
		writer.writeCharacters("\n");

		writeSimpleElement(writer, 3, "Name", cheque.getDraweeName());

		writeSimpleElement(writer, 3, "AccountNumber", cheque.getDraweeAccountNumber());

		writeIndent(writer, 2);
		writer.writeEndElement();
		writer.writeCharacters("\n");

		writeIndent(writer, 2);
		writer.writeStartElement("Payee");
		writer.writeCharacters("\n");

		writeSimpleElement(writer, 3, "Name", cheque.getPayeeName());

		writeSimpleElement(writer, 3, "AccountNumber", cheque.getPayeeAccountNumber());

		writeIndent(writer, 2);
		writer.writeEndElement();
		writer.writeCharacters("\n");

		writeIndent(writer, 1);
		writer.writeEndElement();
	}

	private static void writeSimpleElement(XMLStreamWriter writer, int level, String name, String value)
			throws Exception {

		writeIndent(writer, level);

		writer.writeStartElement(name);

		if (value != null) {
			writer.writeCharacters(value);
		}

		writer.writeEndElement();
		writer.writeCharacters("\n");
	}

	private static void writeIndent(XMLStreamWriter writer, int level) throws Exception {

		for (int i = 0; i < level; i++) {
			writer.writeCharacters(INDENT);
		}
	}
}