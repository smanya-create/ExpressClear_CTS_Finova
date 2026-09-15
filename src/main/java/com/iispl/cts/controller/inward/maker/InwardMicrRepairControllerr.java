package com.iispl.cts.controller.inward.maker;

import java.io.InputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dto.InwardSendBackRequestDTO;
import com.iispl.cts.entity.RejectedReason;
import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.entity.inward.InwardChequeImage;
import com.iispl.cts.entity.inward.InwardChequeRejectionRequest;
import com.iispl.cts.enums.inward.InwardChequeStatus;
import com.iispl.cts.service.NotificationService;
import com.iispl.cts.service.RejectedReasonService;
import com.iispl.cts.service.inward.InwardBatchService;
import com.iispl.cts.service.inward.InwardChequeService;
import com.iispl.cts.service.inward.InwardSendBackRequestService;
import com.iispl.cts.serviceimpl.NotificationServiceImpl;
import com.iispl.cts.serviceimpl.RejectedReasonServiceImpl;
import com.iispl.cts.serviceimpl.inward.InwardBatchServiceImpl;
import com.iispl.cts.serviceimpl.inward.InwardChequeServiceImpl;
import com.iispl.cts.serviceimpl.inward.InwardSendBackRequestServiceImpl;
import com.iispl.cts.validator.MICRValidator;
import com.iispl.cts.validatorimpl.MICRValidatorImpl;

public class InwardMicrRepairControllerr extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;

	private Label lblBatchId;
	private Label lblRecordPosition;
	private Label lblProgress;
	private Label lblRepairStatus;

	private Window rejectRequestWindow;
	private Image chequeImage;
	private Groupbox emptyImageState;

	// Viewer Controls
	private Button btnZoom;
	private Button btnZoomOut;
	private Button btnZoomReset;
	private Button btnRotate;
	private Button btnViewFront;
	private Button btnViewBack;

	private Button btnSaveAndNext;
	private Button btnRejectRequest;
	private Button btnSubmitToDataEntry;

	private Label lblChequeImageTitle;

	private double zoomLevel = 1.0;
	private int rotation = 0;
	private boolean showingBackImage = false;

	private Vlayout micrRepairCompletedState;
	private Label lblCompletedBatchId;

	// Dynamic Alert Banner Controls
	private Div micrAlertBox;
	private Label lblMicrAlertTitle;
	private Label lblMicrCodeTitle;
	private Label lblMicrReasonCode;
	private Label lblMicrNameTitle;
	private Label lblMicrReasonName;
	private Label lblMicrRemarksTitle;
	private Label lblMicrRemarks;

	private Textbox txtChequeNumber;
	private Textbox txtCityCode;
	private Textbox txtBankCode;
	private Textbox txtBranchCode;
	private Textbox txtCurrentMicr;
	private Textbox txtTransactionCode;
	private Textbox txtCorrectedMicr;
	private Textbox txtRemarks;
	private Textbox txtModalRejectionRemark;

	private Progressmeter progressMeter;

	private int currentRecord = 0;
	private int totalRecords = 0;
	private boolean initialTargetResolved = false;

	private InwardChequeService inwardChequeService;
	private InwardBatchService inwardBatchService;
	private InwardSendBackRequestService sendBackRequestService;

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
		sendBackRequestService = new InwardSendBackRequestServiceImpl();
		rejectedReasonService = RejectedReasonServiceImpl.getInstance();
		notificationService = NotificationServiceImpl.getInstance();
		micrValidator = new MICRValidatorImpl();

		if (rejectRequestWindow != null) {
			cmbRejectReason = (Combobox) rejectRequestWindow.getFellowIfAny("cmbRejectReason");
			txtModalRejectionRemark = (Textbox) rejectRequestWindow.getFellowIfAny("txtModalRejectionRemark");
			Button confirmButton = (Button) rejectRequestWindow.getFellowIfAny("btnConfirmReject");
			Button cancelButton = (Button) rejectRequestWindow.getFellowIfAny("btnCancelReject");

			if (confirmButton != null) {
				confirmButton.addEventListener(Events.ON_CLICK, event -> onClick$btnConfirmReject());
			}
			if (cancelButton != null) {
				cancelButton.addEventListener(Events.ON_CLICK, event -> onClick$btnCancelReject());
			}
		}

		loadRepairRecord();
	}

	private void loadRejectedReasons() {
		rejectedReasons = rejectedReasonService.getAllRejectedReasons();
		if (cmbRejectReason == null)
			return;
		cmbRejectReason.getItems().clear();
		if (rejectedReasons == null)
			return;

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
				if (sessionBatchId != null)
					batchId = String.valueOf(sessionBatchId);
			}

			if (batchId == null || batchId.trim().isEmpty()) {
				Messagebox.show("No MICR repair batch was selected.", "MICR Repair", Messagebox.OK,
						Messagebox.EXCLAMATION);
				return;
			}

			batchId = batchId.trim();

			InwardBatch batch = inwardBatchService.getBatchById(batchId);
			if (batch != null && "CHECKER_PROCESSING_PENDING".equalsIgnoreCase(batch.getBatchStatus())) {
				Messagebox.show(
						"This batch is currently under Checker review. MICR repair is locked in view-only mode.",
						"Batch Locked", Messagebox.OK, Messagebox.INFORMATION, evt -> {
							Executions.sendRedirect(
									"/inward/maker/index.zul?page=batch-details&batchId=" + batch.getInwardBatchId());
						});
				return;
			}

			List<InwardCheque> batchCheques = inwardChequeService.getChequesByBatchAndStatus(batchId, null);
			repairCheques = new ArrayList<>();

			if (batchCheques != null) {
				for (InwardCheque cheque : batchCheques) {
					if (cheque == null)
						continue;
					String status = cheque.getChequeStatus();
					if ("MICR_REPAIR_PENDING".equalsIgnoreCase(status)
							|| "MICR_REPAIR_IN_PROGRESS".equalsIgnoreCase(status)
							|| "MICR_REPAIR_COMPLETED".equalsIgnoreCase(status)
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

			if (!initialTargetResolved) {
				String targetChequeId = Executions.getCurrent().getParameter("chequeId");
				if (targetChequeId == null || targetChequeId.trim().isEmpty()) {
					targetChequeId = Executions.getCurrent().getParameter("amp;chequeId");
				}
				if (targetChequeId == null || targetChequeId.trim().isEmpty()) {
					Object sessChq = Executions.getCurrent().getSession().getAttribute("TARGET_CHEQUE_ID");
					if (sessChq == null)
						sessChq = Executions.getCurrent().getSession().getAttribute("MICR_REPAIR_CHEQUE_ID");
					if (sessChq == null)
						sessChq = Executions.getCurrent().getSession().getAttribute("chequeId");
					if (sessChq != null)
						targetChequeId = String.valueOf(sessChq).trim();
				}

				int targetIndex = -1;
				if (targetChequeId != null && !targetChequeId.isEmpty()) {
					for (int i = 0; i < repairCheques.size(); i++) {
						if (targetChequeId.equalsIgnoreCase(repairCheques.get(i).getInwardChequeId())) {
							targetIndex = i;
							break;
						}
					}
				}

				Executions.getCurrent().getSession().removeAttribute("TARGET_CHEQUE_ID");
				Executions.getCurrent().getSession().removeAttribute("MICR_REPAIR_CHEQUE_ID");

				if (targetIndex != -1) {
					currentRecord = targetIndex;
				} else {
					currentRecord = 0;
				}
				initialTargetResolved = true;
			}

			if (currentRecord < 0)
				currentRecord = 0;
			if (currentRecord >= totalRecords)
				currentRecord = totalRecords - 1;

			displayCurrentCheque();

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

	private void displayCurrentCheque() {
		if (repairCheques == null || repairCheques.isEmpty())
			return;

		if (currentRecord < 0)
			currentRecord = 0;
		if (currentRecord >= repairCheques.size())
			currentRecord = repairCheques.size() - 1;

		currentCheque = repairCheques.get(currentRecord);

		if (micrRepairCompletedState != null) {
			micrRepairCompletedState.setVisible(false);
		}

		String currentStatus = currentCheque.getChequeStatus() != null ? currentCheque.getChequeStatus().trim() : "";

		boolean actionAlreadyCompleted = "MICR_REPAIR_COMPLETED".equalsIgnoreCase(currentStatus)
				|| "REJECTION_REQUESTED".equalsIgnoreCase(currentStatus);

		if (btnSaveAndNext != null) {
			btnSaveAndNext.setDisabled(actionAlreadyCompleted);
		}

		if (btnRejectRequest != null) {
			btnRejectRequest.setDisabled(actionAlreadyCompleted);
		}

		loadBatchSummary(currentCheque);
		populateChequeFields(currentCheque);
		loadChequeAlertReason(currentCheque);
		updateNavigation();
		loadChequeImage(currentCheque.getInwardChequeId());
	}

	private void loadChequeAlertReason(InwardCheque item) {
		if (micrAlertBox == null || item == null)
			return;

		String status = item.getChequeStatus() != null ? item.getChequeStatus().trim().toUpperCase() : "";

		// 1. Case: Checker Return / Send-Back
		if (status.contains("SEND_BACK") || status.contains("SENT_BACK") || "MAKER_RETURNED".equals(status)) {
			try {
				InwardSendBackRequestDTO dto = sendBackRequestService
						.getLatestPendingByChequeId(item.getInwardChequeId());
				if (dto != null) {
					if (lblMicrAlertTitle != null)
						lblMicrAlertTitle.setValue("CHECKER SEND BACK");
					if (lblMicrCodeTitle != null)
						lblMicrCodeTitle.setValue("Reason Code:");
					if (lblMicrReasonCode != null)
						lblMicrReasonCode.setValue(dto.getReasonCode() != null ? dto.getReasonCode().trim() : "-");
					if (lblMicrNameTitle != null)
						lblMicrNameTitle.setValue("Reason:");
					if (lblMicrReasonName != null)
						lblMicrReasonName.setValue(
								dto.getReasonName() != null ? dto.getReasonName().trim() : "Send Back to Maker");
					if (lblMicrRemarksTitle != null)
						lblMicrRemarksTitle.setValue("Remarks:");
					if (lblMicrRemarks != null) {
						String rem = dto.getRemarks();
						lblMicrRemarks.setValue(rem != null && !rem.trim().isEmpty() ? rem.trim() : "None provided");
					}
					micrAlertBox.setVisible(true);
					return;
				}
			} catch (Exception ignored) {
			}
		}

		// 2. Case: Maker Rejection Request
		if (InwardChequeStatus.REJECTION_REQUESTED.name().equalsIgnoreCase(status)) {
			String sql = "SELECT rr.rejected_reason_code, rr.rejected_reason_name, r.remarks "
					+ "FROM inward_cheque_rejection_request r "
					+ "LEFT JOIN rejected_reasons rr ON rr.rejected_reason_id::text = r.rejected_reason_id::text "
					+ "WHERE r.inward_cheque_id = ? " + "ORDER BY r.requested_at DESC LIMIT 1";
			try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
				ps.setString(1, item.getInwardChequeId());
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						if (lblMicrAlertTitle != null)
							lblMicrAlertTitle.setValue("MAKER REJECTION REQUEST");
						if (lblMicrCodeTitle != null)
							lblMicrCodeTitle.setValue("Reason Code:");
						if (lblMicrReasonCode != null)
							lblMicrReasonCode.setValue(
									rs.getString("rejected_reason_code") != null ? rs.getString("rejected_reason_code")
											: "-");
						if (lblMicrNameTitle != null)
							lblMicrNameTitle.setValue("Reason:");
						if (lblMicrReasonName != null)
							lblMicrReasonName.setValue(
									rs.getString("rejected_reason_name") != null ? rs.getString("rejected_reason_name")
											: "Rejection Requested");
						if (lblMicrRemarksTitle != null)
							lblMicrRemarksTitle.setValue("Remarks:");
						if (lblMicrRemarks != null) {
							String rem = rs.getString("remarks");
							lblMicrRemarks
									.setValue(rem != null && !rem.trim().isEmpty() ? rem.trim() : "None provided");
						}
						micrAlertBox.setVisible(true);
						return;
					}
				}
			} catch (Exception ignored) {
			}
		}

		if (micrAlertBox != null) {
			micrAlertBox.setVisible(false);
		}
	}

	private void loadBatchSummary(InwardCheque cheque) {
		if (cheque == null)
			return;
		InwardBatch batch = inwardBatchService.getBatchById(cheque.getInwardBatchId());
		if (batch == null)
			return;

		if (lblBatchId != null)
			lblBatchId.setValue(String.valueOf(batch.getInwardBatchId()));
		if (lblBatchSource != null)
			lblBatchSource.setValue("CHI");
		if (lblTotalCheques != null)
			lblTotalCheques.setValue(String.valueOf(batch.getActualChequeCount()));
		if (lblHeaderChequeNo != null)
			lblHeaderChequeNo.setValue(cheque.getChequeNumber() != null ? cheque.getChequeNumber() : "-");
		if (lblHeaderItemStatus != null) {

			String status = cheque.getChequeStatus() != null ? cheque.getChequeStatus().trim() : "MICR_REPAIR";

			lblHeaderItemStatus.setValue(status);

			if ("MICR_REPAIR_COMPLETED".equalsIgnoreCase(status)) {
				lblHeaderItemStatus.setSclass("cts-badge-micr-completed");
			} else {
				lblHeaderItemStatus.setSclass("cts-badge-micr");
			}
		}
		if (lblReceivedDate != null && batch.getUploadedAt() != null) {
			lblReceivedDate.setValue(new java.text.SimpleDateFormat("dd-MM-yyyy").format(batch.getUploadedAt()));
		}
	}

	private void clearRecordFields() {
		if (lblBatchId != null)
			lblBatchId.setValue("-");
		if (txtChequeNumber != null)
			txtChequeNumber.setValue("");
		if (txtCityCode != null)
			txtCityCode.setValue("");
		if (txtBankCode != null)
			txtBankCode.setValue("");
		if (txtBranchCode != null)
			txtBranchCode.setValue("");
		if (txtCurrentMicr != null)
			txtCurrentMicr.setValue("");
		if (txtTransactionCode != null)
			txtTransactionCode.setValue("");
		if (txtCorrectedMicr != null)
			txtCorrectedMicr.setValue("");
		if (txtRemarks != null)
			txtRemarks.setValue("");
		if (lblRepairStatus != null)
			lblRepairStatus.setValue("MICR ERROR");
		if (micrAlertBox != null)
			micrAlertBox.setVisible(false);
	}

	private void updateNavigation() {

		if (lblRecordPosition != null) {
			lblRecordPosition.setValue(totalRecords == 0 ? "0 of 0" : (currentRecord + 1) + " of " + totalRecords);
		}

		int completedRecords = 0;

		if (repairCheques != null) {
			for (InwardCheque cheque : repairCheques) {

				if (cheque == null || cheque.getChequeStatus() == null) {
					continue;
				}

				String status = cheque.getChequeStatus().trim();

				if ("MICR_REPAIR_COMPLETED".equalsIgnoreCase(status)) {
					completedRecords++;
				}
			}
		}

		int progress = totalRecords == 0 ? 0 : (completedRecords * 100) / totalRecords;

		if (lblProgress != null) {
			lblProgress.setValue(completedRecords + "/" + totalRecords + " (" + progress + "%)");
		}

		if (progressMeter != null) {
			progressMeter.setValue(progress);
		}

		updateSubmitButtonState();
	}

	private void updateSubmitButtonState() {

		if (btnSubmitToDataEntry == null) {
			return;
		}

		boolean allCompleted = repairCheques != null && !repairCheques.isEmpty();

		if (allCompleted) {

			for (InwardCheque cheque : repairCheques) {

				if (cheque == null || cheque.getChequeStatus() == null) {
					allCompleted = false;
					break;
				}

				String status = cheque.getChequeStatus().trim();

				boolean completed = "MICR_REPAIR_COMPLETED".equalsIgnoreCase(status);

				if (!completed) {
					allCompleted = false;
					break;
				}
			}
		}

		btnSubmitToDataEntry.setDisabled(!allCompleted);
	}

	private void loadChequeImage(String inwardChequeId) {
		zoomLevel = 1.0;
		rotation = 0;
		showingBackImage = false;

		updateSideButtonStyles();

		if (chequeImage != null) {
			chequeImage.setVisible(false);
			chequeImage.setSrc(null);
		}

		if (lblChequeImageTitle != null) {
			lblChequeImageTitle.setValue("Front and back scans captured by clearing house.");
		}

		if (emptyImageState != null)
			emptyImageState.setVisible(true);

		setImageControlsEnabled(false);
		applyImageTransform();

		if (inwardChequeId == null || inwardChequeId.trim().isEmpty())
			return;

		try {
			InwardChequeImage image = inwardChequeService.getFrontImage(inwardChequeId);
			if (image == null || image.getImagePath() == null || image.getImagePath().trim().isEmpty()) {
				return;
			}

			String imageSrc = "/Inward-data/" + image.getImagePath().trim();
			chequeImage.setSrc(imageSrc);
			chequeImage.setVisible(true);

			if (emptyImageState != null)
				emptyImageState.setVisible(false);

			setImageControlsEnabled(true);
			applyImageTransform();

		} catch (Exception e) {
			e.printStackTrace();
			if (chequeImage != null)
				chequeImage.setVisible(false);
			if (emptyImageState != null)
				emptyImageState.setVisible(true);
			setImageControlsEnabled(false);
		}
	}

	private void updateSideButtonStyles() {
		if (btnViewFront != null) {
			btnViewFront.setStyle(showingBackImage ? "height: 26px; padding: 0 8px; font-size: 11px; font-weight: 400;"
					: "height: 26px; padding: 0 8px; font-size: 11px; font-weight: 700;");
		}
		if (btnViewBack != null) {
			btnViewBack.setStyle(showingBackImage ? "height: 26px; padding: 0 8px; font-size: 11px; font-weight: 700;"
					: "height: 26px; padding: 0 8px; font-size: 11px; font-weight: 400;");
		}
	}

	private void setImageControlsEnabled(boolean enabled) {
		if (btnZoom != null)
			btnZoom.setDisabled(!enabled);
		if (btnZoomOut != null)
			btnZoomOut.setDisabled(!enabled);
		if (btnZoomReset != null)
			btnZoomReset.setDisabled(!enabled);
		if (btnRotate != null)
			btnRotate.setDisabled(!enabled);
		if (btnViewFront != null)
			btnViewFront.setDisabled(!enabled);
		if (btnViewBack != null)
			btnViewBack.setDisabled(!enabled);
	}

	public void onClick$btnZoom() {
		if (chequeImage == null || !chequeImage.isVisible())
			return;
		zoomLevel += 0.25;
		if (zoomLevel > 3.0)
			zoomLevel = 3.0;
		applyImageTransform();
	}

	public void onClick$btnZoomOut() {
		if (chequeImage == null || !chequeImage.isVisible())
			return;
		zoomLevel -= 0.25;
		if (zoomLevel < 0.6)
			zoomLevel = 0.6;
		applyImageTransform();
	}

	public void onClick$btnZoomReset() {
		if (chequeImage == null || !chequeImage.isVisible())
			return;
		zoomLevel = 1.0;
		rotation = 0;
		applyImageTransform();
	}

	public void onClick$btnRotate() {
		if (chequeImage == null || !chequeImage.isVisible())
			return;
		rotation = (rotation + 90) % 360;
		applyImageTransform();
	}

	public void onClick$btnViewFront() {
		if (currentCheque == null)
			return;
		showingBackImage = false;
		updateSideButtonStyles();
		loadChequeImageSide(currentCheque.getInwardChequeId(), false);
	}

	public void onClick$btnViewBack() {
		if (currentCheque == null)
			return;
		showingBackImage = true;
		updateSideButtonStyles();
		loadChequeImageSide(currentCheque.getInwardChequeId(), true);
	}

	private void loadChequeImageSide(String inwardChequeId, boolean back) {
		if (chequeImage != null) {
			chequeImage.setVisible(false);
			chequeImage.setSrc(null);
		}

		try {
			InwardChequeImage image = back ? inwardChequeService.getBackImage(inwardChequeId)
					: inwardChequeService.getFrontImage(inwardChequeId);

			if (image == null || image.getImagePath() == null || image.getImagePath().trim().isEmpty()) {
				if (emptyImageState != null)
					emptyImageState.setVisible(true);
				return;
			}

			String imageSrc = "/Inward-data/" + image.getImagePath().trim();
			chequeImage.setSrc(imageSrc);
			chequeImage.setVisible(true);

			if (emptyImageState != null)
				emptyImageState.setVisible(false);

			applyImageTransform();

		} catch (Exception e) {
			e.printStackTrace();
			if (chequeImage != null)
				chequeImage.setVisible(false);
			if (emptyImageState != null)
				emptyImageState.setVisible(true);
		}
	}

	private void applyImageTransform() {
		if (chequeImage == null)
			return;
		chequeImage.setStyle("transform: scale(" + zoomLevel + ") rotate(" + rotation
				+ "deg); transform-origin: center center; transition: transform 0.2s ease, width 0.2s ease; max-height: 100%; object-fit: contain;");
	}

	public void onClick$btnPrevious() {
		if (totalRecords == 0 || currentRecord <= 0)
			return;
		currentRecord--;
		displayCurrentCheque();
	}

	public void onClick$btnNext() {
		if (totalRecords == 0 || currentRecord >= totalRecords - 1)
			return;
		currentRecord++;
		displayCurrentCheque();
	}

	private String getNextMicrRepairStatus() {
		return InwardChequeStatus.MICR_REPAIR_COMPLETED.name();
	}

	public void onClick$btnSaveAndNext() {

		if (currentCheque != null && currentCheque.getChequeStatus() != null) {

			String status = currentCheque.getChequeStatus().trim();

			if ("MICR_REPAIR_COMPLETED".equalsIgnoreCase(status) || "REJECTION_REQUESTED".equalsIgnoreCase(status)) {

				return;
			}
		}

		String nextMicrRepairStatus = getNextMicrRepairStatus();

		if (totalRecords == 0 || currentCheque == null) {
			Messagebox.show("No MICR repair record is available.", "MICR Repair", Messagebox.OK,
					Messagebox.INFORMATION);
			return;
		}

		String correctedMicr = txtCorrectedMicr != null ? txtCorrectedMicr.getValue() : "";
		if (correctedMicr == null || correctedMicr.trim().isEmpty()) {
			if (txtCorrectedMicr != null)
				txtCorrectedMicr.setErrorMessage("Corrected MICR code is required.");
			return;
		}

		correctedMicr = correctedMicr.trim();
		if (!micrValidator.isValid(correctedMicr)) {
			if (txtCorrectedMicr != null)
				txtCorrectedMicr.setErrorMessage("MICR code must contain exactly 9 digits.");
			return;
		}

		String expectedMicr = safe(currentCheque.getCityCode()) + safe(currentCheque.getBankCode())
				+ safe(currentCheque.getBranchCode());
		expectedMicr = expectedMicr.replaceAll("\\s+", "");

		if (!correctedMicr.equals(expectedMicr)) {
			if (txtCorrectedMicr != null) {
				txtCorrectedMicr
						.setErrorMessage("Incorrect MICR code. Please enter the MICR exactly as shown on the cheque.");
			}
			return;
		}

		String originalMicr = ocrSortCode;
		String repairedBy = (String) Executions.getCurrent().getSession().getAttribute("USER_ID");
		String remarks = txtRemarks != null ? txtRemarks.getValue() : "";

		boolean updated = inwardChequeService.updateMicrRepair(currentCheque.getInwardChequeId(),
				currentCheque.getInwardBatchId(), originalMicr, correctedMicr, nextMicrRepairStatus, repairedBy,
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
				if (cheque == null)
					continue;
				String status = cheque.getChequeStatus();
				if ("MICR_REPAIR_PENDING".equalsIgnoreCase(status) || "MICR_REPAIR_IN_PROGRESS".equalsIgnoreCase(status)
						|| "MICR_REPAIR_COMPLETED".equalsIgnoreCase(status)
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
			if (lblBatchId != null)
				lblBatchId.setValue(completedBatchId);
			if (lblHeaderItemStatus != null)
				lblHeaderItemStatus.setValue("COMPLETED");
			if (lblRepairStatus != null)
				lblRepairStatus.setValue("REPAIRED");
			if (lblCompletedBatchId != null)
				lblCompletedBatchId.setValue("Batch ID: " + completedBatchId);

			updateNavigation();
			if (chequeImage != null)
				chequeImage.setVisible(false);
			if (emptyImageState != null)
				emptyImageState.setVisible(false);
			if (micrRepairCompletedState != null)
				micrRepairCompletedState.setVisible(true);
			return;
		}

		if (currentRecord >= totalRecords) {
			currentRecord = totalRecords - 1;
		}

		displayCurrentCheque();
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
		if (txtModalRejectionRemark != null) {
			txtModalRejectionRemark.setValue("");
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

		String modalRemark = (txtModalRejectionRemark != null && !txtModalRejectionRemark.getValue().trim().isEmpty())
				? txtModalRejectionRemark.getValue().trim()
				: (txtRemarks != null ? txtRemarks.getValue() : "");
		request.setRemarks(modalRemark);

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

		String chequeNumber = currentCheque.getChequeNumber() != null ? currentCheque.getChequeNumber()
				: currentCheque.getInwardChequeId();
		String message = "Rejection request raised by Maker for cheque " + chequeNumber + " in batch "
				+ currentCheque.getInwardBatchId() + ". Reason: " + selectedItem.getLabel() + ". Stage: MICR Repair.";
		notificationService.sendNotification("INWARD_CHECKER", null, message);

		if (cmbRejectReason != null)
			cmbRejectReason.setSelectedItem(null);
		if (txtModalRejectionRemark != null)
			txtModalRejectionRemark.setValue("");
		if (rejectRequestWindow != null)
			rejectRequestWindow.setVisible(false);

		Messagebox.show("Reject request submitted successfully to the Checker.", "Reject Request", Messagebox.OK,
				Messagebox.INFORMATION);

		updateNavigation();

		if (currentRecord < totalRecords - 1) {
			currentRecord++;
			displayCurrentCheque();
		} else {
			displayCurrentCheque();
		}
	}

	public void onClick$btnBackToList() {
		Executions.sendRedirect("/inward/maker/index.zul?page=batch-details&batchId=" + lblBatchId.getValue());
	}

	private String loadOcrSortCode(InwardCheque cheque) {
		if (cheque == null || cheque.getInwardBatchId() == null || cheque.getItemSequenceNumber() == null)
			return "";
		try {
			InwardBatch batch = inwardBatchService.getBatchById(cheque.getInwardBatchId());
			if (batch == null || batch.getBatchReferenceId() == null || batch.getBatchReferenceId().trim().isEmpty())
				return "";

			String resourcePath = "/Inward-data/" + batch.getBatchReferenceId().trim() + "/OCR_Mock.xml";
			InputStream inputStream = Executions.getCurrent().getDesktop().getWebApp()
					.getResourceAsStream(resourcePath);
			if (inputStream == null)
				return "";

			try (InputStream stream = inputStream) {
				Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(stream);
				NodeList chequeNodes = document.getElementsByTagName("OCRCheque");

				for (int i = 0; i < chequeNodes.getLength(); i++) {
					Element ocrCheque = (Element) chequeNodes.item(i);
					String sequenceText = getXmlValue(ocrCheque, "ItemSequenceNumber");
					if (sequenceText == null || sequenceText.trim().isEmpty())
						continue;

					int sequence = Integer.parseInt(sequenceText.trim());
					if (sequence != cheque.getItemSequenceNumber())
						continue;

					Element rawMicr = getChildElement(ocrCheque, "RawMICRRead");
					if (rawMicr == null)
						return "";
					String sortCode = getXmlValue(rawMicr, "SortCode");
					return sortCode != null ? sortCode.trim() : "";
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return "";
	}

	private String getXmlValue(Element parent, String tagName) {
		if (parent == null)
			return "";
		NodeList nodes = parent.getElementsByTagName(tagName);
		if (nodes.getLength() == 0 || nodes.item(0).getTextContent() == null)
			return "";
		return nodes.item(0).getTextContent().trim();
	}

	private Element getChildElement(Element parent, String tagName) {
		if (parent == null)
			return null;
		NodeList nodes = parent.getElementsByTagName(tagName);
		if (nodes.getLength() == 0)
			return null;
		return (Element) nodes.item(0);
	}

	private void populateChequeFields(InwardCheque cheque) {
		if (cheque == null) {
			clearRecordFields();
			return;
		}

		if (txtChequeNumber != null)
			txtChequeNumber.setValue(cheque.getChequeNumber() != null ? cheque.getChequeNumber() : "");
		if (txtTransactionCode != null)
			txtTransactionCode.setValue(cheque.getTransactionCode() != null ? cheque.getTransactionCode() : "");
		if (txtRemarks != null)
			txtRemarks.setValue("");

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

		boolean completed = InwardChequeStatus.MICR_REPAIR_COMPLETED.name().equalsIgnoreCase(cheque.getChequeStatus());

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
					if (ocrChar != expectedChar)
						markMicrPositionError(i);
				}
			} else {
				normalized.append('?');
				markMicrPositionError(i);
			}
		}

		ocrSortCode = normalized.toString();
		if (ocrSortCode.length() > 9)
			ocrSortCode = ocrSortCode.substring(0, 9);

		for (int i = 0; i < ocrSortCode.length() && i < 9; i++) {
			if (ocrSortCode.charAt(i) == '?')
				markMicrPositionError(i);
		}

		String cityDisplay = getMicrPart(ocrSortCode, 0, 3);
		String bankDisplay = getMicrPart(ocrSortCode, 3, 6);
		String branchDisplay = getMicrPart(ocrSortCode, 6, 9);

		if (completed) {
			txtCityCode.setValue(expectedCity);
			txtBankCode.setValue(expectedBank);
			txtBranchCode.setValue(expectedBranch);
		} else {
			txtCityCode.setValue(cityDisplay);
			txtBankCode.setValue(bankDisplay);
			txtBranchCode.setValue(branchDisplay);
		}

		boolean checkerReturnedMicr = InwardChequeStatus.SEND_BACK_TO_MAKER_MICR.name()
				.equalsIgnoreCase(cheque.getChequeStatus());

		boolean micrHasError = cityCodeError || bankCodeError || branchCodeError;

		if (lblRepairStatus != null) {
			if ("MICR_REPAIR_COMPLETED".equalsIgnoreCase(cheque.getChequeStatus())
					|| (checkerReturnedMicr && !micrHasError)) {

				lblRepairStatus.setValue("MICR OK");
				lblRepairStatus.setSclass("cts-badge-micr-completed");

			} else {

				lblRepairStatus.setValue("MICR ERROR");
				lblRepairStatus.setSclass("cts-badge-micr");
			}
		}

		boolean cityEditable = cityCodeError || checkerReturnedMicr;
		boolean bankEditable = bankCodeError || checkerReturnedMicr;
		boolean branchEditable = branchCodeError || checkerReturnedMicr;

		txtCityCode.setReadonly(!cityEditable);
		txtBankCode.setReadonly(!bankEditable);
		txtBranchCode.setReadonly(!branchEditable);

		txtCityCode.setSclass(cityEditable ? "repair-editable-field" : "ocr-field");
		txtBankCode.setSclass(bankEditable ? "repair-editable-field" : "ocr-field");
		txtBranchCode.setSclass(branchEditable ? "repair-editable-field" : "ocr-field");

		txtCurrentMicr.setValue(completed ? expectedSortCode : ocrSortCode);
		txtCurrentMicr.setSclass("error-field");

		if (!cityCodeError && !bankCodeError && !branchCodeError) {
			txtCorrectedMicr.setValue(expectedSortCode);
		} else {
			txtCorrectedMicr.setValue("");
		}

		txtCorrectedMicr.clearErrorMessage();

		txtTransactionCode.setReadonly(true);
		txtTransactionCode.setSclass("ocr-field");
	}

	private void markMicrPositionError(int position) {
		if (position >= 0 && position < 3)
			cityCodeError = true;
		else if (position >= 3 && position < 6)
			bankCodeError = true;
		else if (position >= 6 && position < 9)
			branchCodeError = true;
	}

	private String getMicrPart(String value, int start, int end) {
		StringBuilder result = new StringBuilder();
		for (int i = start; i < end; i++) {
			if (value != null && i < value.length())
				result.append(value.charAt(i));
			else
				result.append('?');
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
		if (currentCheque == null)
			return;

		String city = safe(cityOverride != null ? cityOverride : txtCityCode.getValue());
		String bank = safe(bankOverride != null ? bankOverride : txtBankCode.getValue());
		String branch = safe(branchOverride != null ? branchOverride : txtBranchCode.getValue());

		if (city.matches("\\d{3}") && bank.matches("\\d{3}") && branch.matches("\\d{3}")) {
			txtCorrectedMicr.setValue(city + bank + branch);
			txtCorrectedMicr.clearErrorMessage();
		} else {
			txtCorrectedMicr.setValue("");
		}
	}

	public void onClick$btnSubmitToDataEntry() {

		if (currentCheque == null || currentCheque.getInwardBatchId() == null) {
			Messagebox.show("No MICR repair batch is available for submission.", "Submit to Data Entry", Messagebox.OK,
					Messagebox.INFORMATION);
			return;
		}

		String batchId = currentCheque.getInwardBatchId();

		boolean submitted = inwardChequeService.submitMicrRepairBatchToDataEntry(batchId);

		if (!submitted) {

			Messagebox.show(
					"The MICR Repair batch cannot be submitted yet. " + "Please complete all MICR repair actions.",
					"Submit to Data Entry", Messagebox.OK, Messagebox.EXCLAMATION);

			return;
		}

		Messagebox.show("MICR Repair batch submitted successfully to Data Entry.", "Submit to Data Entry",
				Messagebox.OK, Messagebox.INFORMATION, event -> {
					Executions.sendRedirect("/inward/maker/index.zul?page=batch-details&batchId=" + batchId);
				});
	}
}