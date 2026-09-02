package com.iispl.cts.serviceimpl.inward;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.entity.inward.InwardChequeImage;

public class InwardBatchXmlParser {

	private static final String XML_NAMESPACE = "urn:iso:std:iso:20022:tech:xsd:cts.cheque.clearing";

	public ParsedBatchData parse(String xmlFilePath) throws Exception {

		Document document = parseDocument(xmlFilePath);

		Element batchHeader = getFirstElement(document, "BatchHeader");

		if (batchHeader == null) {
			throw new Exception("BatchHeader not found in XML");
		}

		String inwardBatchId = getValue(batchHeader, "ScannedBatchId");

		String batchReferenceId = getValue(batchHeader, "BatchReferenceId");

		int actualChequeCount = Integer.parseInt(getValue(batchHeader, "ActualChequeCount"));

		BigDecimal actualTotalAmount = new BigDecimal(getValue(batchHeader, "ActualTotalAmount"));

		String stagingStatus = getValue(batchHeader, "StagingStatus");

		String batchStatus = getValue(batchHeader, "BatchStatus");

		String uploadedBy = getValue(batchHeader, "UploadedBy");

		Timestamp uploadedAt = parseTimestamp(getValue(batchHeader, "UploadedAt"));

		/*
		 * Create Inward Batch
		 */
		InwardBatch inwardBatch = new InwardBatch();

		inwardBatch.setInwardBatchId(inwardBatchId);
		inwardBatch.setBatchReferenceId(batchReferenceId);
		inwardBatch.setActualChequeCount(actualChequeCount);
		inwardBatch.setActualTotalAmount(actualTotalAmount);
		inwardBatch.setBatchStatus(batchStatus);
		inwardBatch.setUploadedBy(uploadedBy);
		inwardBatch.setUploadedAt(uploadedAt);

		/*
		 * Lists for cheques and images
		 */
		List<InwardCheque> inwardCheques = new ArrayList<>();

		List<InwardChequeImage> inwardChequeImages = new ArrayList<>();

		/*
		 * Read all ChequeItem elements
		 */
		NodeList chequeNodes = document.getElementsByTagNameNS(XML_NAMESPACE, "ChequeItem");

		for (int i = 0; i < chequeNodes.getLength(); i++) {

			Node node = chequeNodes.item(i);

			if (node.getNodeType() != Node.ELEMENT_NODE) {
				continue;
			}

			Element chequeElement = (Element) node;

			InwardCheque inwardCheque = new InwardCheque();

			String scannedChequeId = getValue(chequeElement, "ScannedChequeId");

			String chequeNumber = getValue(chequeElement, "ChequeNumber");

			String chequeDateValue = getValue(chequeElement, "ChequeDate");

			BigDecimal chequeAmount = new BigDecimal(getValue(chequeElement, "Amount"));

			String chequeStatus = getValue(chequeElement, "ChequeStatus");

			/*
			 * MICR
			 */
			Element micrDetails = getFirstChildElement(chequeElement, "MICRDetails");

			/*
			 * Drawee
			 */
			Element drawee = getFirstChildElement(chequeElement, "Drawee");

			/*
			 * Payee
			 */
			Element payee = getFirstChildElement(chequeElement, "Payee");

			String micrCode = "";

			if (micrDetails != null) {
				micrCode = getValue(micrDetails, "FullMICR");
			}

			String draweeName = "";
			String draweeAccountNumber = "";

			if (drawee != null) {

				draweeName = getValue(drawee, "AccountHolderName");

				draweeAccountNumber = getValue(drawee, "AccountNumber");
			}

			String payeeName = "";
			String payeeAccountNumber = "";

			if (payee != null) {

				payeeName = getValue(payee, "AccountHolderName");

				payeeAccountNumber = getValue(payee, "AccountNumber");
			}

			/*
			 * Set cheque data
			 */
			inwardCheque.setInwardChequeId(scannedChequeId);

			inwardCheque.setInwardBatchId(inwardBatchId);

			inwardCheque.setChequeNumber(chequeNumber);

			inwardCheque.setMicrCode(micrCode);

			inwardCheque.setDraweeName(draweeName);

			inwardCheque.setDraweeAccountNumber(draweeAccountNumber);

			inwardCheque.setPayeeName(payeeName);

			inwardCheque.setPayeeAccountNumber(payeeAccountNumber);

			inwardCheque.setChequeAmount(chequeAmount);

			inwardCheque.setChequeDate(Date.valueOf(LocalDate.parse(chequeDateValue)));

			inwardCheque.setChequeStatus(chequeStatus);

			inwardCheque.setAccountId(null);

			inwardCheque.setCreatedAt(new Timestamp(System.currentTimeMillis()));

			inwardCheques.add(inwardCheque);

			/*
			 * Read Cheque Images
			 */
			Element chequeImages = getFirstChildElement(chequeElement, "ChequeImages");

			if (chequeImages != null) {

				/*
				 * Front Image
				 */
				Element frontImage = getFirstChildElement(chequeImages, "FrontImage");

				if (frontImage != null) {

					InwardChequeImage image = createImage(frontImage, scannedChequeId);

					inwardChequeImages.add(image);
				}

				/*
				 * Back Image
				 */
				Element backImage = getFirstChildElement(chequeImages, "BackImage");

				if (backImage != null) {

					InwardChequeImage image = createImage(backImage, scannedChequeId);

					inwardChequeImages.add(image);
				}
			}
		}

		return new ParsedBatchData(inwardBatch, inwardCheques, inwardChequeImages, stagingStatus);
	}

	/*
	 * ========================================================= CREATE IMAGE
	 * =========================================================
	 */
	private InwardChequeImage createImage(Element imageElement, String inwardChequeId) {

		/*
		 * Try to read imageId from XML
		 */
		String imageId = imageElement.getAttribute("imageId");

		/*
		 * Read image path and type
		 */
		String imagePath = imageElement.getAttribute("path");

		String imageType = imageElement.getAttribute("type");

		/*
		 * If imageId is missing in XML, generate one automatically.
		 */
		if (imageId == null || imageId.trim().isEmpty()) {

			if (imageType == null || imageType.trim().isEmpty()) {

				imageType = "IMAGE";
			}

			imageId = inwardChequeId + "_" + imageType;
		}

		/*
		 * Create image entity
		 */
		InwardChequeImage image = new InwardChequeImage();

		image.setInwardImageId(imageId);

		image.setInwardChequeId(inwardChequeId);

		image.setImageType(imageType);

		image.setImagePath(imagePath);

		image.setCreatedAt(new Timestamp(System.currentTimeMillis()));

		return image;
	}

	/*
	 * ========================================================= PARSE XML DOCUMENT
	 * =========================================================
	 */
	private Document parseDocument(String xmlFilePath) throws Exception {

		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		factory.setNamespaceAware(true);

		factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

		factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);

		factory.setFeature("http://xml.org/sax/features/external-general-entities", false);

		factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);

		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");

		factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");

		DocumentBuilder builder = factory.newDocumentBuilder();

		InputStream inputStream = null;

		try {

			File file = new File(xmlFilePath);

			if (file.exists()) {

				inputStream = Files.newInputStream(file.toPath());

			} else {

				inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlFilePath);

				if (inputStream == null) {

					inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("/" + xmlFilePath);
				}
			}

			if (inputStream == null) {

				throw new Exception("XML file not found: " + xmlFilePath);
			}

			return builder.parse(inputStream);

		} finally {

			if (inputStream != null) {
				inputStream.close();
			}
		}
	}

	/*
	 * ========================================================= GET FIRST ELEMENT
	 * =========================================================
	 */
	private Element getFirstElement(Document document, String tagName) {

		NodeList nodes = document.getElementsByTagNameNS(XML_NAMESPACE, tagName);

		if (nodes.getLength() == 0) {
			return null;
		}

		return (Element) nodes.item(0);
	}

	/*
	 * ========================================================= GET FIRST CHILD
	 * ELEMENT =========================================================
	 */
	private Element getFirstChildElement(Element parent, String tagName) {

		NodeList nodes = parent.getElementsByTagNameNS(XML_NAMESPACE, tagName);

		if (nodes.getLength() == 0) {
			return null;
		}

		return (Element) nodes.item(0);
	}

	/*
	 * ========================================================= GET VALUE
	 * =========================================================
	 */
	private String getValue(Element parent, String tagName) {

		NodeList nodes = parent.getElementsByTagNameNS(XML_NAMESPACE, tagName);

		if (nodes.getLength() == 0) {
			return "";
		}

		return nodes.item(0).getTextContent().trim();
	}

	/*
	 * ========================================================= PARSE TIMESTAMP
	 * =========================================================
	 */
	private Timestamp parseTimestamp(String value) {

		LocalDateTime dateTime = LocalDateTime.parse(value);

		return Timestamp.valueOf(dateTime);
	}

	/*
	 * ========================================================= PARSED BATCH DATA
	 * =========================================================
	 */
	public static class ParsedBatchData {

		private final InwardBatch inwardBatch;

		private final List<InwardCheque> inwardCheques;

		private final List<InwardChequeImage> inwardChequeImages;

		private final String stagingStatus;

		public ParsedBatchData(InwardBatch inwardBatch, List<InwardCheque> inwardCheques,
				List<InwardChequeImage> inwardChequeImages, String stagingStatus) {

			this.inwardBatch = inwardBatch;

			this.inwardCheques = inwardCheques;

			this.inwardChequeImages = inwardChequeImages;

			this.stagingStatus = stagingStatus;
		}

		public InwardBatch getInwardBatch() {
			return inwardBatch;
		}

		public List<InwardCheque> getInwardCheques() {
			return inwardCheques;
		}

		public List<InwardChequeImage> getInwardChequeImages() {
			return inwardChequeImages;
		}

		public String getStagingStatus() {
			return stagingStatus;
		}
	}
}