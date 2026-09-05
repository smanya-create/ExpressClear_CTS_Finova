package com.iispl.cts.parser;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.entity.inward.InwardChequeImage;
import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

public class InwardBatchXmlParser {

	public ParsedBatchData parse(String npciXmlPath, String ocrXmlPath) throws Exception {

		if (npciXmlPath == null || npciXmlPath.trim().isEmpty()) {
			throw new Exception("NPCI XML path is missing");
		}

		if (ocrXmlPath == null || ocrXmlPath.trim().isEmpty()) {
			throw new Exception("OCR XML path is missing");
		}

		Map<Integer, OcrData> ocrDataMap = parseOcrXml(ocrXmlPath);

		return parseNpciXml(npciXmlPath, ocrDataMap);
	}

	private ParsedBatchData parseNpciXml(String npciXmlPath, Map<Integer, OcrData> ocrDataMap) throws Exception {

		VTDGen vtdGen = new VTDGen();

		parseFile(vtdGen, npciXmlPath);

		VTDNav vn = vtdGen.getNav();

		String batchId = getAbsoluteText(vn, "/*[local-name()='ChequeBatchTransmission']"
				+ "/*[local-name()='BatchHeader']" + "/*[local-name()='ScannedBatchId']");

		String batchReferenceId = getAbsoluteText(vn, "/*[local-name()='ChequeBatchTransmission']"
				+ "/*[local-name()='BatchHeader']" + "/*[local-name()='BatchReferenceId']");

		String chequeCountText = getAbsoluteText(vn, "/*[local-name()='ChequeBatchTransmission']"
				+ "/*[local-name()='BatchHeader']" + "/*[local-name()='ActualChequeCount']");

		String totalAmountText = getAbsoluteText(vn, "/*[local-name()='ChequeBatchTransmission']"
				+ "/*[local-name()='BatchHeader']" + "/*[local-name()='ActualTotalAmount']");

		String stagingStatus = getAbsoluteText(vn, "/*[local-name()='ChequeBatchTransmission']"
				+ "/*[local-name()='BatchHeader']" + "/*[local-name()='StagingStatus']");

		String batchStatus = getAbsoluteText(vn, "/*[local-name()='ChequeBatchTransmission']"
				+ "/*[local-name()='BatchHeader']" + "/*[local-name()='BatchStatus']");

		String uploadedBy = getAbsoluteText(vn, "/*[local-name()='ChequeBatchTransmission']"
				+ "/*[local-name()='BatchHeader']" + "/*[local-name()='UploadedBy']");

		String uploadedAtText = getAbsoluteText(vn, "/*[local-name()='ChequeBatchTransmission']"
				+ "/*[local-name()='BatchHeader']" + "/*[local-name()='UploadedAt']");

		if (batchId == null || batchId.isEmpty()) {

			throw new Exception("ScannedBatchId is missing in NPCI XML");
		}

		if (batchReferenceId == null || batchReferenceId.isEmpty()) {

			throw new Exception("BatchReferenceId is missing for batch " + batchId);
		}

		if (chequeCountText == null || chequeCountText.isEmpty()) {

			throw new Exception("ActualChequeCount is missing for batch " + batchId);
		}

		if (totalAmountText == null || totalAmountText.isEmpty()) {

			throw new Exception("ActualTotalAmount is missing for batch " + batchId);
		}

		int actualChequeCount;

		try {

			actualChequeCount = Integer.parseInt(chequeCountText.trim());

		} catch (NumberFormatException e) {

			throw new Exception("Invalid ActualChequeCount for batch " + batchId + ": " + chequeCountText, e);
		}

		BigDecimal actualTotalAmount;

		try {

			actualTotalAmount = new BigDecimal(totalAmountText.trim());

		} catch (NumberFormatException e) {

			throw new Exception("Invalid ActualTotalAmount for batch " + batchId + ": " + totalAmountText, e);
		}

		Timestamp uploadedAt = parseTimestamp(uploadedAtText);

		InwardBatch inwardBatch = new InwardBatch();

		inwardBatch.setInwardBatchId(batchId);

		inwardBatch.setBatchReferenceId(batchReferenceId);

		inwardBatch.setActualChequeCount(actualChequeCount);

		inwardBatch.setActualTotalAmount(actualTotalAmount);

		inwardBatch.setBatchStatus(batchStatus);

		inwardBatch.setUploadedBy(uploadedBy);

		inwardBatch.setUploadedAt(uploadedAt);

		List<InwardCheque> inwardCheques = new ArrayList<InwardCheque>();

		List<InwardChequeImage> inwardChequeImages = new ArrayList<InwardChequeImage>();

		AutoPilot chequeAp = new AutoPilot(vn);

		chequeAp.selectXPath("/*[local-name()='ChequeBatchTransmission']" + "/*[local-name()='Cheques']"
				+ "/*[local-name()='ChequeItem']");

		int parsedChequeCount = 0;

		int chequeIndex;

		while ((chequeIndex = chequeAp.evalXPath()) != -1) {

			parsedChequeCount++;

			String scannedChequeId = getCurrentText(vn, "./*[local-name()='ScannedChequeId']");

			String itemSequenceText = getCurrentText(vn, "./*[local-name()='ItemSequenceNumber']");

			String chequeNumber = getCurrentText(vn, "./*[local-name()='ChequeNumber']");

			String chequeDateText = getCurrentText(vn, "./*[local-name()='ChequeDate']");

			String amountText = getCurrentText(vn, "./*[local-name()='Amount']");

			String chequeStatus = getCurrentText(vn, "./*[local-name()='ChequeStatus']");

			String cityCode = getCurrentText(vn, "./*[local-name()='MICRDetails']" + "/*[local-name()='CityCode']");

			String bankCode = getCurrentText(vn, "./*[local-name()='MICRDetails']" + "/*[local-name()='BankCode']");

			String branchCode = getCurrentText(vn, "./*[local-name()='MICRDetails']" + "/*[local-name()='BranchCode']");

			String fullMicr = getCurrentText(vn, "./*[local-name()='MICRDetails']" + "/*[local-name()='FullMICR']");

			String transactionCode = getCurrentText(vn,
					"./*[local-name()='MICRDetails']" + "/*[local-name()='TransactionCode']");

			String draweeName = getCurrentText(vn,
					"./*[local-name()='Drawee']" + "/*[local-name()='AccountHolderName']");

			String draweeAccountNumber = getCurrentText(vn,
					"./*[local-name()='Drawee']" + "/*[local-name()='AccountNumber']");

			String payeeName = getCurrentText(vn, "./*[local-name()='Payee']" + "/*[local-name()='AccountHolderName']");

			String payeeAccountNumber = getCurrentText(vn,
					"./*[local-name()='Payee']" + "/*[local-name()='AccountNumber']");

			if (scannedChequeId == null || scannedChequeId.isEmpty()) {

				throw new Exception(
						"ScannedChequeId is missing in batch " + batchId + " at cheque " + parsedChequeCount);
			}

			if (itemSequenceText == null || itemSequenceText.isEmpty()) {

				throw new Exception("ItemSequenceNumber is missing for cheque " + scannedChequeId);
			}

			int itemSequenceNumber;

			try {

				itemSequenceNumber = Integer.parseInt(itemSequenceText.trim());

			} catch (NumberFormatException e) {

				throw new Exception("Invalid ItemSequenceNumber for cheque " + scannedChequeId, e);
			}

			if (amountText == null || amountText.isEmpty()) {

				throw new Exception("Amount is missing for cheque " + scannedChequeId);
			}

			BigDecimal chequeAmount;

			try {

				chequeAmount = new BigDecimal(amountText.trim());

			} catch (NumberFormatException e) {

				throw new Exception("Invalid Amount for cheque " + scannedChequeId + ": " + amountText, e);
			}

			Date chequeDate = parseDate(chequeDateText);

			OcrData ocrData = ocrDataMap.get(itemSequenceNumber);

			String finalChequeStatus = chequeStatus;

			if (ocrData != null && ocrData.needsRepair) {

				finalChequeStatus = "MICR_REPAIR_REQUIRED";
			}

			InwardCheque inwardCheque = new InwardCheque();

			inwardCheque.setInwardChequeId(scannedChequeId);

			inwardCheque.setInwardBatchId(batchId);

			inwardCheque.setChequeNumber(chequeNumber);

			inwardCheque.setMicrCode(fullMicr);

			inwardCheque.setDraweeName(draweeName);

			inwardCheque.setDraweeAccountNumber(draweeAccountNumber);

			inwardCheque.setPayeeName(payeeName);

			inwardCheque.setPayeeAccountNumber(payeeAccountNumber);

			inwardCheque.setChequeAmount(chequeAmount);

			inwardCheque.setChequeDate(chequeDate);

			inwardCheque.setChequeStatus(finalChequeStatus);

			inwardCheque.setAccountId(draweeAccountNumber);

			inwardCheque.setCreatedAt(new Timestamp(System.currentTimeMillis()));

			inwardCheque.setCityCode(cityCode);

			inwardCheque.setBankCode(bankCode);

			inwardCheque.setBranchCode(branchCode);

			inwardCheque.setTransactionCode(transactionCode);

			inwardCheque.setItemSequenceNumber(itemSequenceNumber);

			String frontImageId = getCurrentAttribute(vn,
					"./*[local-name()='ChequeImages']" + "/*[local-name()='FrontImage']", "imageId");

			String frontImagePath = getCurrentAttribute(vn,
					"./*[local-name()='ChequeImages']" + "/*[local-name()='FrontImage']", "path");

			String frontImageType = getCurrentAttribute(vn,
					"./*[local-name()='ChequeImages']" + "/*[local-name()='FrontImage']", "type");

			String backImageId = getCurrentAttribute(vn,
					"./*[local-name()='ChequeImages']" + "/*[local-name()='BackImage']", "imageId");

			String backImagePath = getCurrentAttribute(vn,
					"./*[local-name()='ChequeImages']" + "/*[local-name()='BackImage']", "path");

			String backImageType = getCurrentAttribute(vn,
					"./*[local-name()='ChequeImages']" + "/*[local-name()='BackImage']", "type");

			inwardCheque.setChequeImageFront(frontImagePath);

			inwardCheque.setChequeImageBack(backImagePath);

			inwardCheques.add(inwardCheque);

			if (frontImageId != null && !frontImageId.isEmpty()) {

				InwardChequeImage frontImage = new InwardChequeImage();

				frontImage.setInwardImageId(frontImageId);

				frontImage.setInwardChequeId(scannedChequeId);

				frontImage.setImageType(frontImageType);

				frontImage.setImagePath(frontImagePath);

				frontImage.setCreatedAt(new Timestamp(System.currentTimeMillis()));

				inwardChequeImages.add(frontImage);
			}

			if (backImageId != null && !backImageId.isEmpty()) {

				InwardChequeImage backImage = new InwardChequeImage();

				backImage.setInwardImageId(backImageId);

				backImage.setInwardChequeId(scannedChequeId);

				backImage.setImageType(backImageType);

				backImage.setImagePath(backImagePath);

				backImage.setCreatedAt(new Timestamp(System.currentTimeMillis()));

				inwardChequeImages.add(backImage);
			}
		}

		chequeAp.resetXPath();

		if (parsedChequeCount != actualChequeCount) {

			throw new Exception("Cheque count mismatch for batch " + batchId + ". XML says " + actualChequeCount
					+ " but parsed " + parsedChequeCount);
		}

		if (ocrDataMap.size() != actualChequeCount) {

			throw new Exception("OCR cheque count mismatch for batch " + batchId + ". NPCI says " + actualChequeCount
					+ " but OCR contains " + ocrDataMap.size());
		}

		return new ParsedBatchData(inwardBatch, inwardCheques, inwardChequeImages, stagingStatus);
	}

	private Map<Integer, OcrData> parseOcrXml(String ocrXmlPath) throws Exception {

		VTDGen vtdGen = new VTDGen();

		parseFile(vtdGen, ocrXmlPath);

		VTDNav vn = vtdGen.getNav();

		String ocrBatchReferenceId = getAbsoluteText(vn, "/*[local-name()='CTSBatchOCR']"
				+ "/*[local-name()='BatchHeader']" + "/*[local-name()='BatchReferenceId']");

		if (ocrBatchReferenceId == null || ocrBatchReferenceId.isEmpty()) {

			throw new Exception("OCR BatchReferenceId is missing");
		}

		Map<Integer, OcrData> result = new HashMap<Integer, OcrData>();

		AutoPilot ocrAp = new AutoPilot(vn);

		ocrAp.selectXPath(
				"/*[local-name()='CTSBatchOCR']" + "/*[local-name()='Instruments']" + "/*[local-name()='OCRCheque']");

		int index;

		while ((index = ocrAp.evalXPath()) != -1) {

			String sequenceText = getCurrentText(vn, "./*[local-name()='ItemSequenceNumber']");

			if (sequenceText == null || sequenceText.isEmpty()) {

				throw new Exception("OCR ItemSequenceNumber is missing");
			}

			int sequenceNumber;

			try {

				sequenceNumber = Integer.parseInt(sequenceText.trim());

			} catch (NumberFormatException e) {

				throw new Exception("Invalid OCR ItemSequenceNumber: " + sequenceText, e);
			}

			String chequeNumber = getCurrentText(vn, "./*[local-name()='ChequeNumber']");

			String needsRepairText = getCurrentText(vn,
					"./*[local-name()='RawMICRRead']" + "/*[local-name()='NeedsRepair']");

			String repairReason = getCurrentText(vn,
					"./*[local-name()='RawMICRRead']" + "/*[local-name()='RepairReason']");

			String confidenceText = getCurrentText(vn,
					"./*[local-name()='RawMICRRead']" + "/*[local-name()='OcrConfidenceScore']");

			OcrData data = new OcrData();

			data.sequenceNumber = sequenceNumber;

			data.chequeNumber = chequeNumber;

			data.needsRepair = "true".equalsIgnoreCase(needsRepairText);

			data.repairReason = repairReason;

			if (confidenceText != null && !confidenceText.isEmpty()) {

				try {

					data.confidence = Double.parseDouble(confidenceText);

				} catch (NumberFormatException e) {

					data.confidence = 0.0;
				}
			}

			result.put(sequenceNumber, data);
		}

		ocrAp.resetXPath();

		return result;
	}

	private String getAbsoluteText(VTDNav vn, String xpath) throws Exception {

		AutoPilot ap = new AutoPilot(vn);

		ap.selectXPath(xpath);

		String value = ap.evalXPathToString();

		ap.resetXPath();

		if (value == null) {
			return null;
		}

		value = value.trim();

		if (value.isEmpty()) {
			return null;
		}

		return value;
	}

	private String getCurrentText(VTDNav vn, String xpath) throws Exception {

		vn.push();

		try {

			AutoPilot ap = new AutoPilot(vn);

			ap.selectXPath(xpath);

			int index = ap.evalXPath();

			if (index == -1) {
				ap.resetXPath();
				return null;
			}

			int textIndex = vn.getText();

			String value = null;

			if (textIndex != -1) {

				value = vn.toNormalizedString(textIndex);
			}

			ap.resetXPath();

			if (value == null) {
				return null;
			}

			value = value.trim();

			return value.isEmpty() ? null : value;

		} finally {

			vn.pop();
		}
	}

	private String getCurrentAttribute(VTDNav vn, String xpath, String attributeName) throws Exception {

		vn.push();

		try {

			AutoPilot ap = new AutoPilot(vn);

			ap.selectXPath(xpath);

			int index = ap.evalXPath();

			if (index == -1) {

				ap.resetXPath();

				return null;
			}

			int attributeIndex = vn.getAttrVal(attributeName);

			String value = null;

			if (attributeIndex != -1) {

				value = vn.toNormalizedString(attributeIndex);
			}

			ap.resetXPath();

			if (value == null) {
				return null;
			}

			value = value.trim();

			return value.isEmpty() ? null : value;

		} finally {

			vn.pop();
		}
	}

	private void parseFile(VTDGen vtdGen, String xmlFilePath) throws Exception {

		File file = new File(xmlFilePath);

		if (file.exists() && file.isFile()) {

			if (!vtdGen.parseFile(file.getAbsolutePath(), true)) {

				throw new Exception("Unable to parse XML file: " + xmlFilePath);
			}

			return;
		}

		InputStream inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream(xmlFilePath);

		if (inputStream == null) {

			inputStream = Thread.currentThread().getContextClassLoader().getResourceAsStream("/" + xmlFilePath);
		}

		if (inputStream == null) {

			throw new Exception("XML file not found: " + xmlFilePath);
		}

		try {

			byte[] bytes = inputStream.readAllBytes();

			vtdGen.setDoc(bytes);

			try {
			    vtdGen.parse(true);
			} catch (Exception e) {
			    throw new Exception("Unable to parse XML file: " + xmlFilePath, e);
			}

		} finally {

			inputStream.close();
		}
	}

	private Date parseDate(String value) throws Exception {

		if (value == null || value.trim().isEmpty()) {

			return null;
		}

		try {

			return Date.valueOf(LocalDate.parse(value.trim()));

		} catch (Exception e) {

			throw new Exception("Invalid cheque date: " + value, e);
		}
	}

	private Timestamp parseTimestamp(String value) throws Exception {

		if (value == null || value.trim().isEmpty()) {

			return null;
		}

		try {

			return Timestamp.valueOf(LocalDateTime.parse(value.trim()));

		} catch (Exception e) {

			try {

				return Timestamp.valueOf(value.trim().replace("T", " "));

			} catch (Exception ex) {

				throw new Exception("Invalid UploadedAt value: " + value, ex);
			}
		}
	}

	private static class OcrData {

		private int sequenceNumber;

		private String chequeNumber;

		private boolean needsRepair;

		private String repairReason;

		private double confidence;
	}

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