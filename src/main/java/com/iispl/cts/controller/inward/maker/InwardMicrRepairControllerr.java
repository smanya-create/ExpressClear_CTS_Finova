package com.iispl.cts.controller.inward.maker;

import java.util.ArrayList;

import java.io.InputStream;
import org.zkoss.image.AImage;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.entity.inward.InwardChequeRejectionRequest;
import com.iispl.cts.service.NotificationService;
import com.iispl.cts.enums.inward.InwardChequeStatus;
import com.iispl.cts.serviceimpl.NotificationServiceImpl;
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
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zul.Button;
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

	private Button btnZoom;
	private Button btnZoomOut;
	private Button btnZoomReset;
	private Button btnRotate;
	private Button btnImageToggle;

	private Label lblChequeImageTitle;
	private Label lblChequeNavigation;

	private double zoomLevel = 1.0;
	private int rotation = 0;
	private boolean showingBackImage = false;

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

	private NotificationService notificationService;

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

		notificationService = NotificationServiceImpl.getInstance();

		micrValidator = new MICRValidatorImpl();

		if (rejectRequestWindow != null) {

			cmbRejectReason = (Combobox) rejectRequestWindow.getFellow("cmbRejectReason");

			Button confirmButton = (Button) rejectRequestWindow.getFellow("btnConfirmReject");

			Button cancelButton = (Button) rejectRequestWindow.getFellow("btnCancelReject");

			confirmButton.addEventListener(Events.ON_CLICK, event -> onClick$btnConfirmReject());

			cancelButton.addEventListener(Events.ON_CLICK, event -> onClick$btnCancelReject());
		}

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
							|| "SEND_BACK_TO_MAKER_MICR".equalsIgnoreCase(status)) {

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

		if (lblChequeNavigation != null) {

			if (totalRecords == 0) {
				lblChequeNavigation.setValue("");
			} else {
				lblChequeNavigation.setValue((currentRecord + 1) + " / " + totalRecords);
			}
		}

		if (progressMeter != null) {

			int progress = totalRecords == 0 ? 0 : ((currentRecord + 1) * 100) / totalRecords;

			progressMeter.setValue(progress);
		}
	}

	private void loadChequeImage(String inwardChequeId) {

		zoomLevel = 1.0;
		rotation = 0;
		showingBackImage = false;

		if (btnImageToggle != null) {
			btnImageToggle.setLabel("View Back");
		}

		if (chequeImage != null) {
			chequeImage.setVisible(false);
			chequeImage.setSrc(null);
		}

		if (lblChequeImageTitle != null) {
			lblChequeImageTitle.setValue("Cheque Front Image");
		}

		if (emptyImageState != null) {
			emptyImageState.setVisible(true);
		}

		setImageControlsEnabled(false);
		applyImageTransform();

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

			if (lblChequeImageTitle != null) {
				lblChequeImageTitle.setValue("Cheque Front Image");
			}

			setImageControlsEnabled(true);
			applyImageTransform();

		} catch (Exception e) {

			System.err.println("MICR Repair: Failed to load image for cheque -> " + inwardChequeId);

			e.printStackTrace();

			if (chequeImage != null) {
				chequeImage.setVisible(false);
			}

			if (emptyImageState != null) {
				emptyImageState.setVisible(true);
			}

			setImageControlsEnabled(false);
		}
	}

	private void setImageControlsEnabled(boolean enabled) {

		if (btnZoom != null) {
			btnZoom.setDisabled(!enabled);
		}

		if (btnZoomOut != null) {
			btnZoomOut.setDisabled(!enabled);
		}

		if (btnZoomReset != null) {
			btnZoomReset.setDisabled(!enabled);
		}

		if (btnRotate != null) {
			btnRotate.setDisabled(!enabled);
		}

		if (btnImageToggle != null) {
			btnImageToggle.setDisabled(!enabled);
		}
	}

	public void onClick$btnZoom() {

		if (chequeImage == null || !chequeImage.isVisible()) {
			return;
		}

		zoomLevel += 0.25;

		if (zoomLevel > 3.0) {
			zoomLevel = 3.0;
		}

		applyImageTransform();
	}

	public void onClick$btnZoomOut() {

		if (chequeImage == null || !chequeImage.isVisible()) {
			return;
		}

		zoomLevel -= 0.25;

		if (zoomLevel < 1.0) {
			zoomLevel = 1.0;
		}

		applyImageTransform();
	}

	public void onClick$btnZoomReset() {

		if (chequeImage == null || !chequeImage.isVisible()) {
			return;
		}

		zoomLevel = 1.0;
		rotation = 0;

		applyImageTransform();
	}

	public void onClick$btnRotate() {

		if (chequeImage == null || !chequeImage.isVisible()) {
			return;
		}

		rotation += 90;

		if (rotation >= 360) {
			rotation = 0;
		}

		applyImageTransform();
	}

	public void onClick$btnImageToggle() {

		if (currentCheque == null) {
			return;
		}

		zoomLevel = 1.0;
		rotation = 0;

		if (showingBackImage) {

			showingBackImage = false;

			if (btnImageToggle != null) {
				btnImageToggle.setLabel("View Back");
			}

			loadChequeImageSide(currentCheque.getInwardChequeId(), false);

		} else {

			showingBackImage = true;

			if (btnImageToggle != null) {
				btnImageToggle.setLabel("View Front");
			}

			loadChequeImageSide(currentCheque.getInwardChequeId(), true);
		}
	}

	private void loadChequeImageSide(String inwardChequeId, boolean back) {

		if (chequeImage != null) {
			chequeImage.setVisible(false);
			chequeImage.setSrc(null);
		}

		try {

			InwardChequeImage image;

			if (back) {
				image = inwardChequeService.getBackImage(inwardChequeId);
			} else {
				image = inwardChequeService.getFrontImage(inwardChequeId);
			}

			if (image == null || image.getImagePath() == null || image.getImagePath().trim().isEmpty()) {

				if (lblChequeImageTitle != null) {
					lblChequeImageTitle
							.setValue(back ? "Cheque Back Image Not Available" : "Cheque Front Image Not Available");
				}

				if (emptyImageState != null) {
					emptyImageState.setVisible(true);
				}

				return;
			}

			String imageSrc = "/Inward-data/" + image.getImagePath().trim();

			chequeImage.setSrc(imageSrc);
			chequeImage.setVisible(true);

			if (emptyImageState != null) {
				emptyImageState.setVisible(false);
			}

			if (lblChequeImageTitle != null) {
				lblChequeImageTitle.setValue(back ? "Cheque Back Image" : "Cheque Front Image");
			}

			applyImageTransform();

		} catch (Exception e) {

			System.err.println("MICR Repair: Failed to load " + (back ? "back" : "front") + " image for cheque -> "
					+ inwardChequeId);

			e.printStackTrace();

			if (chequeImage != null) {
				chequeImage.setVisible(false);
			}

			if (emptyImageState != null) {
				emptyImageState.setVisible(true);
			}
		}
	}

	private void applyImageTransform() {

		if (chequeImage == null) {
			return;
		}

		chequeImage.setStyle(
				"transform: scale(" + zoomLevel + ") rotate(" + rotation + "deg);" + "transform-origin:center center;"
						+ "transition:transform 0.15s ease;" + "user-select:none;" + "-webkit-user-select:none;");
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
				if ("MICR_REPAIR_PENDING".equalsIgnoreCase(status) || "MICR_REPAIR_IN_PROGRESS".equalsIgnoreCase(status)
						|| "MICR_REPAIR_REQUIRED".equalsIgnoreCase(status)
						|| "SEND_BACK_TO_MAKER_MICR".equalsIgnoreCase(status)) {

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

		loadRejectedReasons();

		if (cmbRejectReason != null) {
			cmbRejectReason.setSelectedItem(null);
		}

		if (rejectRequestWindow != null) {
			rejectRequestWindow.setVisible(true);
		}
	}

	public void onClick$btnCancelReject() {

		if (rejectRequestWindow != null) {
			rejectRequestWindow.setVisible(false);
		}
	}

	public void onClick$btnConfirmReject() {

		if (currentCheque == null) {

			Messagebox.show("No cheque is selected.", "Reject Request", Messagebox.OK, Messagebox.INFORMATION);

			return;
		}

		if (cmbRejectReason == null || cmbRejectReason.getSelectedItem() == null) {

			Messagebox.show("Please select a rejection reason.", "Reject Request", Messagebox.OK,
					Messagebox.EXCLAMATION);

			return;
		}

		Comboitem selectedItem = cmbRejectReason.getSelectedItem();

		Object reasonValue = selectedItem.getValue();

		String rejectedReasonId = reasonValue != null ? reasonValue.toString() : null;

		if (rejectedReasonId == null || rejectedReasonId.trim().isEmpty()) {

			Messagebox.show("Invalid rejection reason.", "Reject Request", Messagebox.OK, Messagebox.ERROR);

			return;
		}

		String requestedBy = (String) Executions.getCurrent().getSession().getAttribute("USER_ID");

		if (requestedBy == null || requestedBy.trim().isEmpty()) {

			Messagebox.show("Unable to identify the logged-in Maker.", "Reject Request", Messagebox.OK,
					Messagebox.ERROR);

			return;
		}

		InwardChequeRejectionRequest request = new InwardChequeRejectionRequest();

		request.setInwardChequeId(currentCheque.getInwardChequeId());

		request.setInwardBatchId(currentCheque.getInwardBatchId());

		request.setRejectedReasonId(rejectedReasonId);

		request.setRemarks(txtRemarks != null ? txtRemarks.getValue() : "");

		request.setRequestedBy(requestedBy);

		request.setRequestStage("MICR_REPAIR");

		request.setRequestStatus("PENDING");

		boolean requestSaved = inwardChequeService.saveRejectionRequest(request);

		if (!requestSaved) {

			Messagebox.show("Unable to submit the rejection request. Please try again.", "Reject Request",
					Messagebox.OK, Messagebox.ERROR);

			return;
		}

		boolean statusUpdated = inwardChequeService.updateChequeStatus(currentCheque.getInwardChequeId(),
				InwardChequeStatus.REJECTION_REQUESTED.name());

		if (!statusUpdated) {

			Messagebox.show("The rejection request was saved, but the cheque status could not be updated.",
					"Reject Request", Messagebox.OK, Messagebox.ERROR);

			return;
		}

		String chequeNumber = currentCheque.getChequeNumber();

		if (chequeNumber == null || chequeNumber.trim().isEmpty()) {

			chequeNumber = currentCheque.getInwardChequeId();
		}

		String message = "Rejection request raised by Maker for cheque " + chequeNumber + " in batch "
				+ currentCheque.getInwardBatchId() + ". Reason: " + selectedItem.getLabel() + ". Stage: MICR Repair.";

		boolean notificationSent = notificationService.sendNotification("INWARD_CHECKER", null, message);

		if (!notificationSent) {

			System.err.println("MICR Repair: rejection request saved and status updated, "
					+ "but Checker notification could not be created.");
		}

		if (cmbRejectReason != null) {
			cmbRejectReason.setSelectedItem(null);
		}

		if (rejectRequestWindow != null) {
			rejectRequestWindow.setVisible(false);
		}

		Messagebox.show("Reject request submitted successfully to the Checker.", "Reject Request", Messagebox.OK,
				Messagebox.INFORMATION);
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