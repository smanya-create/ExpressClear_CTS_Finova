package com.iispl.cts.controller.inward.maker;

import java.util.ArrayList;
import java.io.InputStream;
import org.zkoss.image.AImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.service.inward.InwardChequeService;
import com.iispl.cts.serviceimpl.inward.InwardChequeServiceImpl;
import com.iispl.cts.entity.inward.InwardChequeImage;
import com.iispl.cts.validator.MICRValidator;
import com.iispl.cts.validatorimpl.MICRValidatorImpl;
import com.iispl.cts.entity.RejectedReason;
import com.iispl.cts.service.RejectedReasonService;
import com.iispl.cts.serviceimpl.RejectedReasonServiceImpl;
import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.service.inward.InwardBatchService;
import com.iispl.cts.serviceimpl.inward.InwardBatchServiceImpl;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import org.zkoss.zk.ui.event.InputEvent;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Combobox;

public class InwardMicrRepairControllerr extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;

	private Label lblBatchId;
	private Label lblRecordPosition;
	private Label lblProgress;
	private Label lblRepairStatus;

	private Window rejectRequestWindow;
	private Image chequeImage;
	private Groupbox emptyImageState;

	private Vlayout micrRepairCompletedState;
	private Label lblCompletedBatchId;

	private Textbox txtChequeNumber;
	private Textbox txtCityCode;
	private Textbox txtBankCode;
	private Textbox txtBranchCode;
	private Textbox txtCurrentMicr;
	private Textbox txtTransactionCode;
	private Textbox txtCorrectedMicr;
	private Textbox txtRemarks;

	private Progressmeter progressMeter;

	private int currentRecord = 0;
	private int totalRecords = 0;

	private InwardChequeService inwardChequeService;

	private InwardBatchService inwardBatchService;

	private Label lblBatchSource;
	private Label lblTotalCheques;
	private Label lblHeaderChequeNo;
	private Label lblHeaderItemStatus;
	private Label lblReceivedDate;

	private MICRValidator micrValidator;

	private List<InwardCheque> repairCheques;

	private RejectedReasonService rejectedReasonService;

	private List<RejectedReason> rejectedReasons;

	private Combobox cmbRejectReason;

	private InwardCheque currentCheque;

	private String ocrSortCode = "";

	private boolean cityCodeError;
	private boolean bankCodeError;
	private boolean branchCodeError;

	@Override
	public void doAfterCompose(Component comp) throws Exception {

		super.doAfterCompose(comp);

		inwardChequeService = new InwardChequeServiceImpl();

		inwardBatchService = new InwardBatchServiceImpl();

		rejectedReasonService = RejectedReasonServiceImpl.getInstance();

		micrValidator = new MICRValidatorImpl();

		loadRejectedReasons();
		loadRepairRecord();
	}

	private void loadRejectedReasons() {
		rejectedReasons = rejectedReasonService.getAllRejectedReasons();

		if (cmbRejectReason == null) {
			return;
		}

		cmbRejectReason.getItems().clear();

		if (rejectedReasons == null) {
			return;
		}

		for (RejectedReason reason : rejectedReasons) {
			Comboitem item = new Comboitem();

			item.setLabel(reason.getRejectedReasonCode() + " - " + reason.getRejectedReasonName());

			item.setValue(reason.getRejectedReasonId());

			cmbRejectReason.appendChild(item);
		}
	}

	private void loadRepairRecord() {

		try {

			String batchId = Executions.getCurrent().getParameter("batchId");

			if (batchId == null || batchId.trim().isEmpty()) {

				Object sessionBatchId = Executions.getCurrent().getSession().getAttribute("MICR_REPAIR_BATCH_ID");

				if (sessionBatchId != null) {
					batchId = String.valueOf(sessionBatchId);
				}
			}

			if (batchId == null || batchId.trim().isEmpty()) {

				Messagebox.show("No MICR repair batch was selected.", "MICR Repair", Messagebox.OK,
						Messagebox.EXCLAMATION);

				return;
			}

			batchId = batchId.trim();

			List<InwardCheque> batchCheques = inwardChequeService.getChequesByBatchAndStatus(batchId, null);

			repairCheques = new ArrayList<>();

			if (batchCheques != null) {

				for (InwardCheque cheque : batchCheques) {

					if (cheque == null) {
						continue;
					}

					String status = cheque.getChequeStatus();

					if ("MICR_REPAIR_PENDING".equalsIgnoreCase(status)
					        || "MICR_REPAIR_IN_PROGRESS".equalsIgnoreCase(status)
					        || "MICR_REPAIR_REQUIRED".equalsIgnoreCase(status)
					        || "SEND_BACK_TO_MAKER".equalsIgnoreCase(status)) {

					    repairCheques.add(cheque);
					}
				}
			}

			if (repairCheques.isEmpty()) {

				totalRecords = 0;
				currentRecord = 0;
				currentCheque = null;

				clearRecordFields();
				updateNavigation();
				loadChequeImage(null);

				return;
			}

			totalRecords = repairCheques.size();

			if (currentRecord < 0) {
				currentRecord = 0;
			}

			if (currentRecord >= totalRecords) {
				currentRecord = totalRecords - 1;
			}

			currentCheque = repairCheques.get(currentRecord);

			if (micrRepairCompletedState != null) {
				micrRepairCompletedState.setVisible(false);
			}

			loadBatchSummary(currentCheque);

			populateChequeFields(currentCheque);

			updateNavigation();

			loadChequeImage(currentCheque.getInwardChequeId());

		} catch (Exception e) {

			e.printStackTrace();

			totalRecords = 0;
			currentRecord = 0;
			currentCheque = null;

			clearRecordFields();
			updateNavigation();
			loadChequeImage(null);

			Messagebox.show("Unable to load MICR repair records.", "Error", Messagebox.OK, Messagebox.ERROR);
		}
	}

	private void loadBatchSummary(InwardCheque cheque) {

		if (cheque == null) {
			return;
		}

		InwardBatch batch = inwardBatchService.getBatchById(cheque.getInwardBatchId());

		if (batch == null) {
			return;
		}

		if (lblBatchId != null) {
			lblBatchId.setValue(String.valueOf(batch.getInwardBatchId()));
		}

		if (lblBatchSource != null) {
			lblBatchSource.setValue("CHI");
		}

		if (lblTotalCheques != null) {
			lblTotalCheques.setValue(String.valueOf(batch.getActualChequeCount()));
		}

		if (lblHeaderChequeNo != null) {
			lblHeaderChequeNo.setValue(cheque.getChequeNumber());
		}

		if (lblHeaderItemStatus != null) {
			lblHeaderItemStatus.setValue(cheque.getChequeStatus());
		}

		if (lblReceivedDate != null && batch.getUploadedAt() != null) {
			lblReceivedDate.setValue(new java.text.SimpleDateFormat("dd-MM-yyyy").format(batch.getUploadedAt()));
		}
	}

	private void clearRecordFields() {

		if (lblBatchId != null) {
			lblBatchId.setValue("BATCH: —");
		}

		if (txtChequeNumber != null) {
			txtChequeNumber.setValue("");
		}

		if (txtCityCode != null) {
			txtCityCode.setValue("");
		}

		if (txtBankCode != null) {
			txtBankCode.setValue("");
		}

		if (txtBranchCode != null) {
			txtBranchCode.setValue("");
		}

		if (txtCurrentMicr != null) {
			txtCurrentMicr.setValue("");
		}

		if (txtTransactionCode != null) {
			txtTransactionCode.setValue("");
		}

		if (txtCorrectedMicr != null) {
			txtCorrectedMicr.setValue("");
		}

		if (txtRemarks != null) {
			txtRemarks.setValue("");
		}

		if (lblRepairStatus != null) {
			lblRepairStatus.setValue("MICR ERROR");
		}
	}

	private void updateNavigation() {

		if (lblRecordPosition != null) {

			if (totalRecords == 0) {
				lblRecordPosition.setValue("No records");
			} else {
				lblRecordPosition.setValue("Record " + (currentRecord + 1) + " of " + totalRecords);
			}
		}

		if (lblProgress != null) {
			lblProgress.setValue((totalRecords == 0 ? 0 : currentRecord + 1) + " / " + totalRecords);
		}

		if (progressMeter != null) {

			int progress = totalRecords == 0 ? 0 : ((currentRecord + 1) * 100) / totalRecords;

			progressMeter.setValue(progress);
		}
	}

	private void loadChequeImage(String inwardChequeId) {

		if (chequeImage != null) {
			chequeImage.setVisible(false);
			chequeImage.setSrc(null);
		}

		if (emptyImageState != null) {
			emptyImageState.setVisible(true);
		}

		if (inwardChequeId == null || inwardChequeId.trim().isEmpty()) {
			return;
		}

		try {

			InwardChequeImage image = inwardChequeService.getFrontImage(inwardChequeId);

			if (image == null || image.getImagePath() == null || image.getImagePath().trim().isEmpty()) {

				System.out.println("MICR Repair: No front image found for cheque -> " + inwardChequeId);

				return;
			}

			String imagePath = image.getImagePath().trim();

			String imageSrc = "/Inward-data/" + imagePath;

			System.out.println("MICR Repair: Loading image URL -> " + imageSrc);

			chequeImage.setSrc(imageSrc);
			chequeImage.setVisible(true);

			if (emptyImageState != null) {
				emptyImageState.setVisible(false);
			}

		} catch (Exception e) {

			System.err.println("MICR Repair: Failed to load image for cheque -> " + inwardChequeId);

			e.printStackTrace();

			if (chequeImage != null) {
				chequeImage.setVisible(false);
			}

			if (emptyImageState != null) {
				emptyImageState.setVisible(true);
			}
		}
	}

	public void onClick$btnPrevious() {

		if (totalRecords == 0 || currentRecord <= 0) {
			return;
		}

		currentRecord--;
		loadRepairRecord();
	}

	public void onClick$btnNext() {

		if (totalRecords == 0 || currentRecord >= totalRecords - 1) {
			return;
		}

		currentRecord++;
		loadRepairRecord();
	}

	public void onClick$btnSaveAndNext() {

		if (totalRecords == 0 || currentCheque == null) {

			Messagebox.show("No MICR repair record is available.", "MICR Repair", Messagebox.OK,
					Messagebox.INFORMATION);

			return;
		}

		String correctedMicr = txtCorrectedMicr != null ? txtCorrectedMicr.getValue() : "";

		if (correctedMicr == null || correctedMicr.trim().isEmpty()) {

			if (txtCorrectedMicr != null) {
				txtCorrectedMicr.setErrorMessage("Corrected MICR code is required.");
			}

			return;
		}

		correctedMicr = correctedMicr.trim();

		if (!micrValidator.isValid(correctedMicr)) {

			if (txtCorrectedMicr != null) {
				txtCorrectedMicr.setErrorMessage("MICR code must contain exactly 9 digits.");
			}

			return;
		}

		String originalMicr = ocrSortCode;

		String repairedBy = (String) Executions.getCurrent().getSession().getAttribute("USER_ID");

		String remarks = txtRemarks != null ? txtRemarks.getValue() : "";

		boolean updated = inwardChequeService.updateMicrRepair(currentCheque.getInwardChequeId(),
				currentCheque.getInwardBatchId(), originalMicr, correctedMicr, "DATA_ENTRY_PENDING", repairedBy,
				remarks);

		if (!updated) {

			Messagebox.show("Unable to save MICR correction.", "MICR Repair", Messagebox.OK, Messagebox.ERROR);

			return;
		}

		Messagebox.show("MICR correction saved successfully.", "MICR Repair", Messagebox.OK, Messagebox.INFORMATION);

		String batchId = (String) Executions.getCurrent().getSession().getAttribute("MICR_REPAIR_BATCH_ID");

		List<InwardCheque> batchCheques = inwardChequeService.getChequesByBatchAndStatus(batchId, null);

		repairCheques = new ArrayList<>();

		if (batchCheques != null) {

			for (InwardCheque cheque : batchCheques) {

				if (cheque == null) {
					continue;
				}

				String status = cheque.getChequeStatus();
				if ("MICR_REPAIR_PENDING".equalsIgnoreCase(status)
				        || "MICR_REPAIR_IN_PROGRESS".equalsIgnoreCase(status)
				        || "MICR_REPAIR_REQUIRED".equalsIgnoreCase(status)
				        || "SEND_BACK_TO_MAKER".equalsIgnoreCase(status)) {

				    repairCheques.add(cheque);
				}
			}
		}

		totalRecords = repairCheques.size();

		if (totalRecords == 0) {

			currentRecord = 0;
			currentCheque = null;

			String completedBatchId = batchId;

			clearRecordFields();

			if (lblBatchId != null) {
				lblBatchId.setValue("BATCH: " + completedBatchId);
			}

			if (lblHeaderItemStatus != null) {
				lblHeaderItemStatus.setValue("MICR REPAIR COMPLETED");
			}

			if (lblRepairStatus != null) {
				lblRepairStatus.setValue("MICR REPAIR COMPLETED");
			}

			if (lblCompletedBatchId != null) {
				lblCompletedBatchId.setValue("Batch ID: " + completedBatchId);
			}

			updateNavigation();

			if (chequeImage != null) {
				chequeImage.setVisible(false);
			}

			if (emptyImageState != null) {
				emptyImageState.setVisible(false);
			}

			if (micrRepairCompletedState != null) {
				micrRepairCompletedState.setVisible(true);
			}

			return;
		}

		if (currentRecord >= totalRecords) {
			currentRecord = totalRecords - 1;
		}

		loadRepairRecord();
	}

	public void onClick$btnRejectRequest() {
		if (currentCheque == null) {
			Messagebox.show("No cheque is selected.", "Reject Request", Messagebox.OK, Messagebox.INFORMATION);
			return;
		}

		if (rejectRequestWindow != null) {
			rejectRequestWindow.doModal();
		}
	}

	public void onClick$btnCancelReject() {
		if (rejectRequestWindow != null) {
			rejectRequestWindow.setVisible(false);
		}
	}

	public void onClick$btnConfirmReject() {

		if (currentCheque == null) {
			return;
		}

		if (cmbRejectReason == null || cmbRejectReason.getSelectedItem() == null) {
			Messagebox.show("Please select a rejection reason.", "Reject Request", Messagebox.OK,
					Messagebox.EXCLAMATION);
			return;
		}

		Comboitem selectedItem = cmbRejectReason.getSelectedItem();

		String rejectedReasonId = String.valueOf(selectedItem.getValue());

		if (rejectedReasonId == null || rejectedReasonId.trim().isEmpty()) {
			Messagebox.show("Invalid rejection reason.", "Reject Request", Messagebox.OK, Messagebox.EXCLAMATION);
			return;
		}

		Messagebox.show("Reject request submitted successfully.", "Reject Request", Messagebox.OK,
				Messagebox.INFORMATION);

		if (rejectRequestWindow != null) {
			rejectRequestWindow.setVisible(false);
		}
	}

	public void onClick$btnBackToList() {

		Executions.sendRedirect("/inward/maker/index.zul");
	}

	private String loadOcrSortCode(InwardCheque cheque) {

		if (cheque == null || cheque.getInwardBatchId() == null || cheque.getItemSequenceNumber() == null) {

			return "";
		}

		try {

			InwardBatch batch = inwardBatchService.getBatchById(cheque.getInwardBatchId());

			if (batch == null || batch.getBatchReferenceId() == null || batch.getBatchReferenceId().trim().isEmpty()) {

				return "";
			}

			String resourcePath = "/Inward-data/" + batch.getBatchReferenceId().trim() + "/OCR_Mock.xml";

			InputStream inputStream = Executions.getCurrent().getDesktop().getWebApp()
					.getResourceAsStream(resourcePath);

			if (inputStream == null) {

				System.err.println("MICR Repair: OCR resource not found -> " + resourcePath);

				return "";
			}

			try (InputStream stream = inputStream) {

				Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);

				NodeList chequeNodes = document.getElementsByTagName("OCRCheque");

				for (int i = 0; i < chequeNodes.getLength(); i++) {

					Element ocrCheque = (Element) chequeNodes.item(i);

					String sequenceText = getXmlValue(ocrCheque, "ItemSequenceNumber");

					if (sequenceText == null || sequenceText.trim().isEmpty()) {
						continue;
					}

					int sequence = Integer.parseInt(sequenceText.trim());

					if (sequence != cheque.getItemSequenceNumber()) {
						continue;
					}

					Element rawMicr = getChildElement(ocrCheque, "RawMICRRead");

					if (rawMicr == null) {
						return "";
					}

					String sortCode = getXmlValue(rawMicr, "SortCode");

					return sortCode != null ? sortCode.trim() : "";
				}

			}

		} catch (Exception e) {

			System.err.println("MICR Repair: Unable to read OCR data for cheque " + cheque.getInwardChequeId());

			e.printStackTrace();
		}

		return "";
	}

	private String getXmlValue(Element parent, String tagName) {

		if (parent == null) {
			return "";
		}

		NodeList nodes = parent.getElementsByTagName(tagName);

		if (nodes.getLength() == 0) {
			return "";
		}

		if (nodes.item(0).getTextContent() == null) {
			return "";
		}

		return nodes.item(0).getTextContent().trim();
	}

	private Element getChildElement(Element parent, String tagName) {

		if (parent == null) {
			return null;
		}

		NodeList nodes = parent.getElementsByTagName(tagName);

		if (nodes.getLength() == 0) {
			return null;
		}

		return (Element) nodes.item(0);
	}

	private void populateChequeFields(InwardCheque cheque) {

		if (cheque == null) {
			clearRecordFields();
			return;
		}

		if (txtChequeNumber != null) {
			txtChequeNumber.setValue(cheque.getChequeNumber() != null ? cheque.getChequeNumber() : "");
		}

		if (txtTransactionCode != null) {
			txtTransactionCode.setValue(cheque.getTransactionCode() != null ? cheque.getTransactionCode() : "");
		}

		if (txtRemarks != null) {
			txtRemarks.setValue("");
		}

		prepareMicrRepairFields(cheque);
	}

	private void prepareMicrRepairFields(InwardCheque cheque) {

		cityCodeError = false;
		bankCodeError = false;
		branchCodeError = false;

		ocrSortCode = loadOcrSortCode(cheque);

		String expectedCity = safe(cheque != null ? cheque.getCityCode() : "");

		String expectedBank = safe(cheque != null ? cheque.getBankCode() : "");

		String expectedBranch = safe(cheque != null ? cheque.getBranchCode() : "");

		String expectedSortCode = expectedCity + expectedBank + expectedBranch;

		if (ocrSortCode == null || ocrSortCode.trim().isEmpty()) {

			ocrSortCode = safe(cheque != null ? cheque.getMicrCode() : "");
		}

		ocrSortCode = ocrSortCode.replaceAll("\\s+", "");

		StringBuilder normalized = new StringBuilder();

		for (int i = 0; i < expectedSortCode.length(); i++) {

			if (i < ocrSortCode.length()) {

				char ocrChar = ocrSortCode.charAt(i);

				char expectedChar = expectedSortCode.charAt(i);

				if (ocrChar == '?' || !Character.isDigit(ocrChar)) {

					normalized.append('?');

				} else {

					normalized.append(ocrChar);

					if (ocrChar != expectedChar) {

						markMicrPositionError(i);
					}
				}

			} else {

				normalized.append('?');

				markMicrPositionError(i);
			}
		}

		ocrSortCode = normalized.toString();

		if (ocrSortCode.length() > 9) {
			ocrSortCode = ocrSortCode.substring(0, 9);
		}

		for (int i = 0; i < ocrSortCode.length() && i < 9; i++) {

			if (ocrSortCode.charAt(i) == '?') {
				markMicrPositionError(i);
			}
		}

		String cityDisplay = getMicrPart(ocrSortCode, 0, 3);
		String bankDisplay = getMicrPart(ocrSortCode, 3, 6);
		String branchDisplay = getMicrPart(ocrSortCode, 6, 9);

		txtCityCode.setValue(cityDisplay);
		txtBankCode.setValue(bankDisplay);
		txtBranchCode.setValue(branchDisplay);

		txtCityCode.setReadonly(!cityCodeError);
		txtBankCode.setReadonly(!bankCodeError);
		txtBranchCode.setReadonly(!branchCodeError);

		txtCityCode.setSclass(cityCodeError ? "repair-editable-field" : "ocr-field");

		txtBankCode.setSclass(bankCodeError ? "repair-editable-field" : "ocr-field");

		txtBranchCode.setSclass(branchCodeError ? "repair-editable-field" : "ocr-field");

		txtCurrentMicr.setValue(ocrSortCode);
		txtCurrentMicr.setSclass("error-field");

		txtCorrectedMicr.setValue("");
		txtCorrectedMicr.clearErrorMessage();

		txtTransactionCode.setReadonly(true);
		txtTransactionCode.setSclass("ocr-field");
	}

	private void markMicrPositionError(int position) {

		if (position >= 0 && position < 3) {
			cityCodeError = true;
		} else if (position >= 3 && position < 6) {
			bankCodeError = true;
		} else if (position >= 6 && position < 9) {
			branchCodeError = true;
		}
	}

	private String getMicrPart(String value, int start, int end) {

		StringBuilder result = new StringBuilder();

		for (int i = start; i < end; i++) {

			if (value != null && i < value.length()) {
				result.append(value.charAt(i));
			} else {
				result.append('?');
			}
		}

		return result.toString();
	}

	private String safe(String value) {

		return value == null ? "" : value.trim();
	}

	public void onChanging$txtCityCode(InputEvent event) {
		updateCorrectedMicr(event.getValue(), null, null);
	}

	public void onChanging$txtBankCode(InputEvent event) {
		updateCorrectedMicr(null, event.getValue(), null);
	}

	public void onChanging$txtBranchCode(InputEvent event) {
		updateCorrectedMicr(null, null, event.getValue());
	}

	private void updateCorrectedMicr(String cityOverride, String bankOverride, String branchOverride) {

		if (currentCheque == null) {
			return;
		}

		String city = cityOverride != null ? cityOverride : txtCityCode.getValue();

		String bank = bankOverride != null ? bankOverride : txtBankCode.getValue();

		String branch = branchOverride != null ? branchOverride : txtBranchCode.getValue();

		city = safe(city);
		bank = safe(bank);
		branch = safe(branch);

		if (city.matches("\\d{3}") && bank.matches("\\d{3}") && branch.matches("\\d{3}")) {

			String correctedMicr = city + bank + branch;

			txtCorrectedMicr.setValue(correctedMicr);

			txtCorrectedMicr.clearErrorMessage();

		} else {

			txtCorrectedMicr.setValue("");
		}
	}
}