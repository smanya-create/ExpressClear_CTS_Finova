package com.iispl.cts.parser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;
import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

public class BatchXmlParser {

	private static final String CTS_NS_PREFIX = "cts";
	private static final String CTS_NS_URI = "urn:iso:std:iso:20022:tech:xsd:cts.cheque.clearing";

	public static class ParsedBatchData {

		private final ScanBatch scanBatch;
		private final List<ScanCheque> chequeList;

		public ParsedBatchData(ScanBatch scanBatch, List<ScanCheque> chequeList) {
			this.scanBatch = scanBatch;
			this.chequeList = chequeList;
		}

		public ScanBatch getScanBatch() {
			return scanBatch;
		}

		public List<ScanCheque> getChequeList() {
			return chequeList;
		}
	}

	public ParsedBatchData parse(String zipFilePath) throws Exception {
		File zipFile = new File(zipFilePath);

		try (ZipFile zip = new ZipFile(zipFile)) {
			ZipEntry xmlEntry = findXmlEntry(zip);
			if (xmlEntry == null) {
				throw new IllegalStateException("No XML file found inside archive: " + zipFilePath);
			}

			try (InputStream xmlInputStream = zip.getInputStream(xmlEntry)) {
				byte[] xmlBytes = readXmlBytes(xmlInputStream);

				VTDGen vtdGen = new VTDGen();
				vtdGen.setDoc(xmlBytes);
				vtdGen.parse(true);

				VTDNav vn = vtdGen.getNav();
				AutoPilot namespacePilot = createNamespacePilot(vn);

				ScanBatch scanBatch = parseScanBatch(vn, namespacePilot);
				List<ScanCheque> chequeList = parseChequeList(vn, namespacePilot, scanBatch.getScannedBatchId());

				return new ParsedBatchData(scanBatch, chequeList);
			}
		}
	}

	private ScanBatch parseScanBatch(VTDNav vn, AutoPilot namespacePilot) throws Exception {
		vn.toElement(VTDNav.ROOT);

		ScanBatch scanBatch = new ScanBatch();
		scanBatch.setScannedBatchId(getValue(vn, namespacePilot, "/cts:ChequeBatchTransmission/cts:BatchHeader/cts:ScannedBatchId"));
		scanBatch.setBatchReferenceId(getValue(vn, namespacePilot, "/cts:ChequeBatchTransmission/cts:BatchHeader/cts:BatchReferenceId"));
		scanBatch.setActualChequeCount(parseInteger(getValue(vn, namespacePilot, "/cts:ChequeBatchTransmission/cts:BatchHeader/cts:ActualChequeCount")));
		scanBatch.setActualTotalAmount(parseBigDecimal(getValue(vn, namespacePilot, "/cts:ChequeBatchTransmission/cts:BatchHeader/cts:ActualTotalAmount")));
		scanBatch.setStagingStatus(getValue(vn, namespacePilot, "/cts:ChequeBatchTransmission/cts:BatchHeader/cts:StagingStatus"));
		scanBatch.setBatchStatus(getValue(vn, namespacePilot, "/cts:ChequeBatchTransmission/cts:BatchHeader/cts:BatchStatus"));
		scanBatch.setUploadedBy(getValue(vn, namespacePilot, "/cts:ChequeBatchTransmission/cts:BatchHeader/cts:UploadedBy"));
		scanBatch.setUploadedAt(parseTimestamp(getValue(vn, namespacePilot, "/cts:ChequeBatchTransmission/cts:BatchHeader/cts:UploadedAt")));

		return scanBatch;
	}

	private List<ScanCheque> parseChequeList(VTDNav vn, AutoPilot namespacePilot, String scannedBatchId) throws Exception {
		List<ScanCheque> chequeList = new ArrayList<>();
		vn.toElement(VTDNav.ROOT);

		AutoPilot chequePilot = new AutoPilot(vn);
		chequePilot.declareXPathNameSpace(CTS_NS_PREFIX, CTS_NS_URI);
		chequePilot.selectXPath("/cts:ChequeBatchTransmission/cts:Cheques/cts:ChequeItem");

		while (chequePilot.evalXPath() != -1) {
			ScanCheque cheque = parseCheque(vn, namespacePilot);
			cheque.setScannedBatchId(scannedBatchId);
			chequeList.add(cheque);
		}

		return chequeList;
	}

	private ScanCheque parseCheque(VTDNav vn, AutoPilot namespacePilot) throws Exception {
		ScanCheque cheque = new ScanCheque();

		cheque.setScannedChequeId(getValue(vn, namespacePilot, "./cts:ScannedChequeId"));
		cheque.setChequeNumber(getValue(vn, namespacePilot, "./cts:ChequeNumber"));
		cheque.setChequeDate(parseDate(getValue(vn, namespacePilot, "./cts:ChequeDate")));
		cheque.setChequeAmount(parseBigDecimal(getValue(vn, namespacePilot, "./cts:Amount")));
		cheque.setChequeStatus(getValue(vn, namespacePilot, "./cts:ChequeStatus"));

		cheque.setMicrCode(getValue(vn, namespacePilot, "./cts:MICRDetails/cts:FullMICR"));
		cheque.setCityCode(getValue(vn, namespacePilot, "./cts:MICRDetails/cts:CityCode"));
		cheque.setBankCode(getValue(vn, namespacePilot, "./cts:MICRDetails/cts:BankCode"));
		cheque.setBranchCode(getValue(vn, namespacePilot, "./cts:MICRDetails/cts:BranchCode"));

		cheque.setDraweeName(getValue(vn, namespacePilot, "./cts:Drawee/cts:AccountHolderName"));
		cheque.setDraweeAccountNumber(getValue(vn, namespacePilot, "./cts:Drawee/cts:AccountNumber"));

		cheque.setPayeeName(getValue(vn, namespacePilot, "./cts:Payee/cts:AccountHolderName"));
		cheque.setPayeeAccountNumber(getValue(vn, namespacePilot, "./cts:Payee/cts:AccountNumber"));

		cheque.setChequeImageFront(getAttributeValue(vn, namespacePilot, "./cts:ChequeImages/cts:FrontImage", "path"));
		cheque.setChequeImageBack(getAttributeValue(vn, namespacePilot, "./cts:ChequeImages/cts:BackImage", "path"));

		cheque.setAccountId(null);
		cheque.setCreatedAt(null);

		return cheque;
	}

	private AutoPilot createNamespacePilot(VTDNav vn) {
		AutoPilot pilot = new AutoPilot(vn);
		pilot.declareXPathNameSpace(CTS_NS_PREFIX, CTS_NS_URI);
		return pilot;
	}

	private String getValue(VTDNav vn, AutoPilot namespacePilot, String xpath) throws Exception {
		vn.push();
		try {
			namespacePilot.bind(vn);
			namespacePilot.selectXPath(xpath);

			int index = namespacePilot.evalXPath();
			if (index == -1) {
				return null;
			}

			int textIndex = vn.getText();
			if (textIndex == -1) {
				return null;
			}

			return vn.toString(textIndex).trim();
		} finally {
			vn.pop();
		}
	}

	private String getAttributeValue(VTDNav vn, AutoPilot namespacePilot, String xpath, String attributeName) throws Exception {
		vn.push();
		try {
			namespacePilot.bind(vn);
			namespacePilot.selectXPath(xpath);

			int index = namespacePilot.evalXPath();
			if (index == -1) {
				return null;
			}

			int attrIndex = vn.getAttrVal(attributeName);
			if (attrIndex == -1) {
				return null;
			}

			return vn.toString(attrIndex).trim();
		} finally {
			vn.pop();
		}
	}

	private ZipEntry findXmlEntry(ZipFile zip) {
		Enumeration<? extends ZipEntry> entries = zip.entries();
		while (entries.hasMoreElements()) {
			ZipEntry entry = entries.nextElement();
			if (!entry.isDirectory() && entry.getName().toLowerCase().endsWith(".xml")) {
				return entry;
			}
		}
		return null;
	}

	private int parseInteger(String value) {
		if (value == null || value.trim().isEmpty()) {
			return 0;
		}
		try {
			return Integer.parseInt(value.trim());
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	private BigDecimal parseBigDecimal(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return new BigDecimal(value.trim());
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private Date parseDate(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return Date.valueOf(value.trim());
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private Timestamp parseTimestamp(String value) {
		if (value == null || value.trim().isEmpty()) {
			return null;
		}
		try {
			return Timestamp.valueOf(value.trim().replace("T", " "));
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	private byte[] readXmlBytes(InputStream inputStream) throws Exception {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		byte[] buffer = new byte[4096];
		int length;
		while ((length = inputStream.read(buffer)) != -1) {
			outputStream.write(buffer, 0, length);
		}
		return outputStream.toByteArray();
	}
}