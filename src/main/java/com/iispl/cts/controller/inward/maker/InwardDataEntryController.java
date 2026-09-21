package com.iispl.cts.controller.inward.maker;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.common.util.SecurityUtil;
import com.iispl.cts.dto.InwardSendBackRequestDTO;
import com.iispl.cts.entity.RejectedReason;
import com.iispl.cts.entity.User;
import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.entity.inward.InwardChequeImage;
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

public class InwardDataEntryController extends GenericForwardComposer<Component> {

	
	private final InwardBatchService batchService = new InwardBatchServiceImpl();
	private final InwardChequeService chequeService = new InwardChequeServiceImpl();
	private final InwardSendBackRequestService sendBackRequestService = new InwardSendBackRequestServiceImpl();
	private final RejectedReasonService rejectedReasonService = RejectedReasonServiceImpl.getInstance();
	private final NotificationService notificationService = NotificationServiceImpl.getInstance();

	// Top Metadata Card Labels
	private Label lblBatchId;
	private Label lblSource;
	private Label lblTotalCheques;
	private Label lblChequeNo;
	private Label lblDataStatus;
	private Label lblReceivedDate;

	// Navigation & Submission Controls
	private Progressmeter pmBatchProgress;
	private Label lblProgressText;
	private Label lblChequePosition;
	private Button btnPrevCheque;
	private Button btnNextCheque;
	private Button btnSubmitToChecker;

	// Viewer Controls
	private Image imgCheque;
	private Button btnViewFront;
	private Button btnViewBack;
	private Button btnZoomIn;
	private Button btnZoomOut;
	private Button btnZoomFit;
	private Button btnRotate;

	// Dynamic Reason Alert Banner Controls
	private Div dataEntryAlertBox;
	private Label lblDataEntryAlertTitle;
	private Label lblDataEntryCodeTitle;
	private Label lblDataEntryReasonCode;
	private Label lblDataEntryNameTitle;
	private Label lblDataEntryReasonName;
	private Label lblDataEntryRemarksTitle;
	private Label lblDataEntryRemarks;

	// Form Fields
	private Textbox txtChequeNumber;
	private Textbox txtChequeDate;
	private Textbox txtAmount;
	private Textbox txtAmountInWords;
	private Textbox txtDraweeAccount;
	private Textbox txtDraweeBankName;
	private Textbox txtPayeeName;
	private Textbox txtEntryRemark;

	// Form Action Buttons
	private Button btnCancel;
	private Button btnRequestRejection;
	private Button btnApproveCheque;

	// Rejection Modal Controls
	private Div winRejectionModal;
	private Combobox cmbModalRejectionReason;
	private Textbox txtModalRejectionRemark;
	private Button btnCancelModalReject;
	private Button btnConfirmModalReject;

	// Checker Submission Modal Controls
	private Div winCompletionConfirmModal;
	private Label lblModalTotal;
	private Label lblModalAccepted;
	private Label lblModalRejected;
	private Button btnCancelCompletionModal;
	private Button btnConfirmCompletionModal;

	private List<InwardCheque> activeQueue;
	private int currentIndex = 0;
	private String currentBatchId;

	// Viewer Transformation State
	private boolean isViewingFront = true;
	private int zoomLevel = 100;
	private int rotationAngle = 0;

	private boolean isReworkBatch = false;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		
	
		
		super.doAfterCompose(comp);

		if (btnConfirmCompletionModal != null) {
			btnConfirmCompletionModal.addEventListener("onClick", event -> executeSubmitToChecker());
		}
		if (btnCancelCompletionModal != null) {
			btnCancelCompletionModal.addEventListener("onClick", event -> {
				if (winCompletionConfirmModal != null) {
					winCompletionConfirmModal.setVisible(false);
				}
			});
		}

		String sessionBatch = (String) Sessions.getCurrent().getAttribute("ACTIVE_INWARD_BATCH_ID");
		if (sessionBatch != null && !sessionBatch.trim().isEmpty()) {
			currentBatchId = sessionBatch.trim();
		} else {
			String paramBatch = execution.getParameter("batchId");
			if (paramBatch != null && !paramBatch.trim().isEmpty()) {
				currentBatchId = paramBatch.trim();
			}
		}

		if (currentBatchId != null) {
			loadBatch(currentBatchId);
		}
	}

	private void populateRejectedReasons() {
		if (cmbModalRejectionReason == null)
			return;
		cmbModalRejectionReason.getChildren().clear();

		List<RejectedReason> reasons = rejectedReasonService.getAllRejectedReasons();
		if (reasons != null) {
			for (RejectedReason r : reasons) {
				String label = "[" + r.getRejectedReasonCode() + "] " + r.getRejectedReasonName();
				Comboitem item = new Comboitem(label);
				item.setValue(r.getRejectedReasonId());
				item.setTooltiptext(r.getRejectedReasonDescription());
				cmbModalRejectionReason.appendChild(item);
			}
		}
	}

	public void loadBatch(String batchId) {
		this.currentBatchId = batchId;

		InwardBatch batch = batchService.getBatchById(batchId);
		if (batch != null) {
			if ("CHECKER_PROCESSING_PENDING".equalsIgnoreCase(batch.getBatchStatus())) {
				Messagebox.show("This batch is currently under Checker review. Data Entry is locked in view-only mode.",
						"Batch Locked", Messagebox.OK, Messagebox.INFORMATION, evt -> {
							Executions.sendRedirect("/inward/maker/index.zul?page=batch-details&batchId=" + batchId);
						});
				return;
			}

			if (lblBatchId != null)
				lblBatchId.setValue(batch.getInwardBatchId());
			if (lblTotalCheques != null)
				lblTotalCheques.setValue(String.valueOf(batch.getActualChequeCount()));
			if (lblReceivedDate != null && batch.getUploadedAt() != null) {
				lblReceivedDate.setValue(new SimpleDateFormat("dd-MM-yyyy").format(batch.getUploadedAt()));
			}
		}

		List<InwardCheque> allCheques = chequeService.getChequesByBatchAndStatus(batchId, null);
		this.activeQueue = new ArrayList<>();

		if (allCheques != null && !allCheques.isEmpty()) {

			this.isReworkBatch = allCheques.stream().anyMatch(c -> c != null && isSentBackStatus(c.getChequeStatus()));

			for (InwardCheque c : allCheques) {

				if (c == null || c.getChequeStatus() == null) {
					continue;
				}

				String st = c.getChequeStatus().trim();

				if (InwardChequeStatus.DATA_ENTRY_PENDING.name().equalsIgnoreCase(st)
						|| InwardChequeStatus.DATA_ENTRY_IN_PROGRESS.name().equalsIgnoreCase(st)
						|| "DATA_ENTRY_COMPLETED".equalsIgnoreCase(st)) {

					this.activeQueue.add(c);
					continue;
				}

				if (isSentBackStatus(st)) {
					this.activeQueue.add(c);
					continue;
				}

				if (InwardChequeStatus.REJECTION_REQUESTED.name().equalsIgnoreCase(st)
						&& isDataEntryRejectionRequest(c)) {

					this.activeQueue.add(c);
				}
			}
		}

		//

		String targetChequeId = execution.getParameter("chequeId");
		if (targetChequeId == null || targetChequeId.trim().isEmpty()) {
			targetChequeId = execution.getParameter("amp;chequeId");
		}
		if (targetChequeId == null || targetChequeId.trim().isEmpty()) {
			Object sessChq = Sessions.getCurrent().getAttribute("TARGET_CHEQUE_ID");
			if (sessChq == null)
				sessChq = Sessions.getCurrent().getAttribute("DATA_ENTRY_CHEQUE_ID");
			if (sessChq == null)
				sessChq = Sessions.getCurrent().getAttribute("chequeId");
			if (sessChq != null)
				targetChequeId = sessChq.toString().trim();
		}

		int targetIndex = -1;
		if (targetChequeId != null && !targetChequeId.isEmpty() && this.activeQueue != null) {
			for (int i = 0; i < this.activeQueue.size(); i++) {
				if (targetChequeId.equalsIgnoreCase(this.activeQueue.get(i).getInwardChequeId())) {
					targetIndex = i;
					break;
				}
			}
		}

		Sessions.getCurrent().removeAttribute("TARGET_CHEQUE_ID");
		Sessions.getCurrent().removeAttribute("DATA_ENTRY_CHEQUE_ID");

		if (targetIndex != -1) {
			this.currentIndex = targetIndex;
		} else {
			this.currentIndex = findFirstPendingIndex();
		}

		displayCurrentCheque();
		updateProgressBar();
	}

	private boolean isSentBackStatus(String status) {
		if (status == null)
			return false;
		return InwardChequeStatus.SEND_BACK_TO_MAKER_DATA_ENTRY.name().equalsIgnoreCase(status)
				|| InwardChequeStatus.SEND_BACK_TO_MAKER.name().equalsIgnoreCase(status);
	}

	private boolean isMicrRepairStatus(String status) {
		if (status == null)
			return false;
		String s = status.trim().toUpperCase();
		return "MICR_REPAIR_PENDING".equals(s) || "MICR_REPAIR_IN_PROGRESS".equals(s)
				|| "MICR_REPAIR_REQUIRED".equals(s) || "SEND_BACK_TO_MAKER_MICR".equals(s);
	}

	private boolean isDataEntryRejectionRequest(InwardCheque cheque) {

		if (cheque == null || cheque.getInwardChequeId() == null
				|| !InwardChequeStatus.REJECTION_REQUESTED.name().equalsIgnoreCase(cheque.getChequeStatus())) {
			return false;
		}

		String sql = "SELECT EXISTS (" + "SELECT 1 " + "FROM inward_cheque_rejection_request "
				+ "WHERE inward_cheque_id = ? " + "AND request_stage IN ('DATA_ENTRY', 'MICR_REPAIR') "
				+ "AND request_status = 'PENDING'" + ")";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, cheque.getInwardChequeId());

			try (ResultSet rs = ps.executeQuery()) {
				return rs.next() && rs.getBoolean(1);
			}

		} catch (Exception e) {

			System.err.println(
					"Unable to determine Data Entry rejection ownership for cheque " + cheque.getInwardChequeId());

			e.printStackTrace();
			return false;
		}
	}

	private int findFirstPendingIndex() {
		if (activeQueue == null || activeQueue.isEmpty())
			return 0;

		for (int i = 0; i < activeQueue.size(); i++) {
			String status = activeQueue.get(i).getChequeStatus();
			boolean isNeedsWork = InwardChequeStatus.DATA_ENTRY_PENDING.name().equalsIgnoreCase(status)
					|| InwardChequeStatus.DATA_ENTRY_IN_PROGRESS.name().equalsIgnoreCase(status)
					|| InwardChequeStatus.SEND_BACK_TO_MAKER_DATA_ENTRY.name().equalsIgnoreCase(status)
					|| "PENDING".equalsIgnoreCase(status) || "RAW".equalsIgnoreCase(status);

			if (isNeedsWork) {
				return i;
			}
		}

		return 0;
	}

	private void displayCurrentCheque() {
		if (activeQueue == null || activeQueue.isEmpty()) {
			clearForm();
			if (lblChequeNo != null)
				lblChequeNo.setValue("-");
			if (lblDataStatus != null) {
				lblDataStatus.setValue("NO CHEQUES");
				lblDataStatus.setStyle(null);
			}
			if (lblChequePosition != null)
				lblChequePosition.setValue("0 of 0");
			hideAlertBox();
			updateNavigationState();
			updateProgressBar();
			return;
		}

		if (currentIndex < 0)
			currentIndex = 0;
		if (currentIndex >= activeQueue.size())
			currentIndex = activeQueue.size() - 1;

		InwardCheque item = activeQueue.get(currentIndex);

		if (lblChequeNo != null)
			lblChequeNo.setValue(item.getChequeNumber() != null ? item.getChequeNumber() : "-");
		if (lblChequePosition != null)
			lblChequePosition.setValue((currentIndex + 1) + " of " + activeQueue.size());

		String status = item.getChequeStatus() != null ? item.getChequeStatus().trim().toUpperCase() : "";
		if (lblDataStatus != null) {
			lblDataStatus.setStyle(null); // Clear inline gradient style so the CSS peach pill is respected

			if (isSentBackStatus(status)) {
				lblDataStatus.setValue("Sent Back");
			} else if (InwardChequeStatus.MAKER_RETURNED.name().equalsIgnoreCase(status)) {
				lblDataStatus.setValue("Returned To Checker");
			} else if (InwardChequeStatus.REJECTION_REQUESTED.name().equalsIgnoreCase(status)
					|| "REJECTION REQUESTED".equalsIgnoreCase(status) || "REJECT REQ".equalsIgnoreCase(status)) {
				lblDataStatus.setValue("Rejection Requested");
			} else if (InwardChequeStatus.REJECTED.name().equalsIgnoreCase(status)) {
				lblDataStatus.setValue("Rejected");
			} else if (InwardChequeStatus.DATA_ENTRY_IN_PROGRESS.name().equalsIgnoreCase(status)) {
				lblDataStatus.setValue("In Progress");
			} else if (InwardChequeStatus.CHECKER_PROCESSING_PENDING.name().equalsIgnoreCase(status)
					|| InwardChequeStatus.COMPLETED.name().equalsIgnoreCase(status)
					|| "ACCEPTED".equalsIgnoreCase(status) || "DATA_ENTRY_COMPLETED".equalsIgnoreCase(status)) {
				lblDataStatus.setValue("Checker Pending");
			} else {
				lblDataStatus.setValue("Pending");
			}
		}

		loadChequeAlertReason(item);

		isViewingFront = true;
		resetImageTransformations();
		updateDisplayedImage(item);

		if (txtChequeNumber != null)
			txtChequeNumber.setValue(item.getChequeNumber() != null ? item.getChequeNumber() : "");
		if (txtChequeDate != null)
			txtChequeDate.setValue(item.getChequeDate() != null ? item.getChequeDate().toString() : "");
		if (txtAmount != null)
			txtAmount.setValue(item.getChequeAmount() != null ? "₹ " + item.getChequeAmount().toPlainString() : "");
		if (txtAmountInWords != null)
			txtAmountInWords.setValue(convertToIndianCurrencyWords(item.getChequeAmount()));
		if (txtDraweeAccount != null)
			txtDraweeAccount.setValue(item.getDraweeAccountNumber() != null ? item.getDraweeAccountNumber() : "");
		if (txtDraweeBankName != null)
			txtDraweeBankName.setValue(item.getDraweeName() != null ? item.getDraweeName() : "");
		if (txtPayeeName != null)
			txtPayeeName.setValue(item.getPayeeName() != null ? item.getPayeeName() : "");
		if (txtEntryRemark != null)
			txtEntryRemark.setValue("");

		updateNavigationState();
		updateProgressBar();
	}

	private void loadChequeAlertReason(InwardCheque item) {
		if (dataEntryAlertBox == null || item == null)
			return;

		String status = item.getChequeStatus() != null ? item.getChequeStatus().trim().toUpperCase() : "";

		// 1. Case: Checker Return / Send-Back
		if (isSentBackStatus(status) || InwardChequeStatus.MAKER_RETURNED.name().equalsIgnoreCase(status)) {
			try {
				InwardSendBackRequestDTO dto = sendBackRequestService
						.getLatestPendingByChequeId(item.getInwardChequeId());
				if (dto != null) {
					if (lblDataEntryAlertTitle != null)
						lblDataEntryAlertTitle.setValue("CHECKER SEND BACK");
					if (lblDataEntryCodeTitle != null)
						lblDataEntryCodeTitle.setValue("Reason Code:");
					if (lblDataEntryReasonCode != null)
						lblDataEntryReasonCode.setValue(dto.getReasonCode() != null ? dto.getReasonCode().trim() : "-");
					if (lblDataEntryNameTitle != null)
						lblDataEntryNameTitle.setValue("Reason:");
					if (lblDataEntryReasonName != null)
						lblDataEntryReasonName.setValue(
								dto.getReasonName() != null ? dto.getReasonName().trim() : "Send Back to Maker");
					if (lblDataEntryRemarksTitle != null)
						lblDataEntryRemarksTitle.setValue("Remarks:");
					if (lblDataEntryRemarks != null) {
						String rem = dto.getRemarks();
						lblDataEntryRemarks
								.setValue(rem != null && !rem.trim().isEmpty() ? rem.trim() : "None provided");
					}
					dataEntryAlertBox.setVisible(true);
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
						if (lblDataEntryAlertTitle != null)
							lblDataEntryAlertTitle.setValue("MAKER REJECTION REQUEST");
						if (lblDataEntryCodeTitle != null)
							lblDataEntryCodeTitle.setValue("Reason Code:");
						if (lblDataEntryReasonCode != null)
							lblDataEntryReasonCode.setValue(
									rs.getString("rejected_reason_code") != null ? rs.getString("rejected_reason_code")
											: "-");
						if (lblDataEntryNameTitle != null)
							lblDataEntryNameTitle.setValue("Reason:");
						if (lblDataEntryReasonName != null)
							lblDataEntryReasonName.setValue(
									rs.getString("rejected_reason_name") != null ? rs.getString("rejected_reason_name")
											: "Rejection Requested");
						if (lblDataEntryRemarksTitle != null)
							lblDataEntryRemarksTitle.setValue("Remarks:");
						if (lblDataEntryRemarks != null) {
							String rem = rs.getString("remarks");
							lblDataEntryRemarks
									.setValue(rem != null && !rem.trim().isEmpty() ? rem.trim() : "None provided");
						}
						dataEntryAlertBox.setVisible(true);
						return;
					}
				}
			} catch (Exception ignored) {
			}
		}

		hideAlertBox();
	}

	private void hideAlertBox() {
		if (dataEntryAlertBox != null) {
			dataEntryAlertBox.setVisible(false);
		}
	}

	private void updateProgressBar() {
		List<InwardCheque> fullBatchCheques = chequeService.getChequesByBatchAndStatus(this.currentBatchId, null);
		if (fullBatchCheques == null || fullBatchCheques.isEmpty()) {
			if (pmBatchProgress != null)
				pmBatchProgress.setValue(0);
			if (lblProgressText != null)
				lblProgressText.setValue("0/0 (0%)");
			if (btnSubmitToChecker != null)
				btnSubmitToChecker.setDisabled(true);
			return;
		}

		int totalInBatch = fullBatchCheques.size();
		long resolvedInBatch = fullBatchCheques.stream()
				.filter(c -> InwardChequeStatus.CHECKER_PROCESSING_PENDING.name().equalsIgnoreCase(c.getChequeStatus())
						|| InwardChequeStatus.MAKER_RETURNED.name().equalsIgnoreCase(c.getChequeStatus())
						|| InwardChequeStatus.REJECTION_REQUESTED.name().equalsIgnoreCase(c.getChequeStatus())
						|| InwardChequeStatus.REJECTED.name().equalsIgnoreCase(c.getChequeStatus())
						|| InwardChequeStatus.COMPLETED.name().equalsIgnoreCase(c.getChequeStatus())
						|| "ACCEPTED".equalsIgnoreCase(c.getChequeStatus())
						|| "DATA_ENTRY_COMPLETED".equalsIgnoreCase(c.getChequeStatus()))
				.count();


		int percentage = (int) Math.round(((double) resolvedInBatch / totalInBatch) * 100);

		if (pmBatchProgress != null)
			pmBatchProgress.setValue(percentage);
		if (lblProgressText != null)
			lblProgressText.setValue(resolvedInBatch + "/" + totalInBatch);

		boolean allResolved = (resolvedInBatch == totalInBatch);
		if (btnSubmitToChecker != null) {
			btnSubmitToChecker.setDisabled(!allResolved);
		}
	}

	private void updateNavigationState() {
		if (btnPrevCheque != null)
			btnPrevCheque.setDisabled(activeQueue == null || currentIndex <= 0);
		if (btnNextCheque != null)
			btnNextCheque.setDisabled(activeQueue == null || currentIndex >= activeQueue.size() - 1);
	}

	private void saveCurrentChequeStateSilently() {
		if (activeQueue == null || activeQueue.isEmpty() || currentIndex >= activeQueue.size())
			return;

		InwardCheque current = activeQueue.get(currentIndex);

		if (txtChequeNumber != null)
			current.setChequeNumber(txtChequeNumber.getValue().trim());
		if (txtDraweeAccount != null)
			current.setDraweeAccountNumber(txtDraweeAccount.getValue().trim());
		if (txtDraweeBankName != null)
			current.setDraweeName(txtDraweeBankName.getValue().trim());
		if (txtPayeeName != null)
			current.setPayeeName(txtPayeeName.getValue().trim());

		if (txtAmount != null && !txtAmount.getValue().trim().isEmpty()) {
			String rawAmount = txtAmount.getValue().replace("₹", "").replace(",", "").trim();
			try {
				current.setChequeAmount(new BigDecimal(rawAmount));
			} catch (Exception ignored) {
			}
		}

		if (txtChequeDate != null && !txtChequeDate.getValue().trim().isEmpty()) {
			try {
				current.setChequeDate(Date.valueOf(txtChequeDate.getValue().trim()));
			} catch (Exception ignored) {
			}
		}

		chequeService.updateChequeDetails(current);
	}

	public void onClick$btnPrevCheque() {
		if (activeQueue != null && currentIndex > 0) {
			saveCurrentChequeStateSilently();
			currentIndex--;
			displayCurrentCheque();
		}
	}

	public void onClick$btnNextCheque() {
		if (activeQueue != null && currentIndex < activeQueue.size() - 1) {
			saveCurrentChequeStateSilently();
			currentIndex++;
			displayCurrentCheque();
		}
	}

	private void updateDisplayedImage(InwardCheque item) {
		if (imgCheque == null || item == null) {
			return;
		}

		try {
			String chequeId = item.getInwardChequeId();
			if (chequeId == null || chequeId.trim().isEmpty()) {
				imgCheque.setSrc(null);
				return;
			}

			InwardChequeImage image;
			if (isViewingFront) {
				image = chequeService.getFrontImage(chequeId);
			} else {
				image = chequeService.getBackImage(chequeId);
			}

			if (image == null || image.getImagePath() == null || image.getImagePath().trim().isEmpty()) {
				imgCheque.setSrc(null);
				return;
			}

			String imagePath = image.getImagePath().trim();
			String imageSrc = "/Inward-data/" + imagePath;
			imgCheque.setSrc(imageSrc);

		} catch (Exception e) {
			imgCheque.setSrc(null);
		}

		applyImageStyle();
	}

	private void resetImageTransformations() {
		zoomLevel = 100;
		rotationAngle = 0;
		applyImageStyle();
	}

	private void applyImageStyle() {
		if (imgCheque != null) {
			imgCheque.setStyle("width: " + zoomLevel + "%; transform: rotate(" + rotationAngle
					+ "deg); transition: transform 0.2s, width 0.2s; object-fit: contain;");
		}
	}

	public void onClick$btnViewFront() {
		if (activeQueue != null && currentIndex < activeQueue.size()) {
			isViewingFront = true;
			btnViewFront.setStyle("font-weight: 700;");
			btnViewBack.setStyle("font-weight: 400;");
			updateDisplayedImage(activeQueue.get(currentIndex));
		}
	}

	public void onClick$btnViewBack() {
		if (activeQueue != null && currentIndex < activeQueue.size()) {
			isViewingFront = false;
			btnViewBack.setStyle("font-weight: 700;");
			btnViewFront.setStyle("font-weight: 400;");
			updateDisplayedImage(activeQueue.get(currentIndex));
		}
	}

	public void onClick$btnZoomIn() {
		if (zoomLevel < 200) {
			zoomLevel += 20;
			applyImageStyle();
		}
	}

	public void onClick$btnZoomOut() {
		if (zoomLevel > 60) {
			zoomLevel -= 20;
			applyImageStyle();
		}
	}

	public void onClick$btnZoomFit() {
		resetImageTransformations();
	}

	public void onClick$btnRotate() {
		rotationAngle = (rotationAngle + 90) % 360;
		applyImageStyle();
	}

	public void onClick$btnApproveCheque() {
		if (activeQueue == null || activeQueue.isEmpty())
			return;

		String validationError = validateFormFields();
		if (validationError != null) {
			Messagebox.show(validationError, "Validation Error", Messagebox.OK, Messagebox.EXCLAMATION);
			return;
		}

		InwardCheque current = activeQueue.get(currentIndex);
		current.setChequeNumber(txtChequeNumber.getValue().trim());
		current.setDraweeAccountNumber(txtDraweeAccount.getValue().trim());
		current.setDraweeName(txtDraweeBankName.getValue().trim());
		current.setPayeeName(txtPayeeName.getValue().trim());

		String rawAmount = txtAmount.getValue().replace("₹", "").replace(",", "").trim();
		current.setChequeAmount(new BigDecimal(rawAmount));
		current.setChequeDate(Date.valueOf(txtChequeDate.getValue().trim()));

		boolean wasSentBack = isSentBackStatus(current.getChequeStatus());

		if (wasSentBack) {
			current.setChequeStatus(InwardChequeStatus.MAKER_RETURNED.name());
			try {
				User currentUser = (User) Sessions.getCurrent().getAttribute("LOGGED_IN_USER");
				String userId = (currentUser != null && currentUser.getUserId() != null) ? currentUser.getUserId()
						: "Maker";
				sendBackRequestService.markRequestResolved(current.getInwardChequeId(), userId);
			} catch (Exception ex) {
				System.err.println("WARN: Could not mark send-back request resolved: " + ex.getMessage());
			}
		} else {
			current.setChequeStatus("DATA_ENTRY_COMPLETED");
		}
		chequeService.updateChequeDetails(current);

		if (currentIndex < activeQueue.size() - 1) {
			currentIndex++;
		}
		displayCurrentCheque();
	}

	public void onClick$btnCancel() {
		if (activeQueue != null && currentIndex < activeQueue.size()) {
			displayCurrentCheque();
		}
	}

	public void onClick$btnRequestRejection() {
		if (activeQueue == null || activeQueue.isEmpty())
			return;

		if (cmbModalRejectionReason != null && cmbModalRejectionReason.getItemCount() == 0) {
			populateRejectedReasons();
		}

		if (cmbModalRejectionReason != null) {
			cmbModalRejectionReason.setValue("");
			cmbModalRejectionReason.setSelectedIndex(-1);
		}
		if (txtModalRejectionRemark != null) {
			txtModalRejectionRemark.setValue("");
		}

		if (winRejectionModal != null) {
			winRejectionModal.setVisible(true);
		}
	}

	public void onClick$btnCancelModalReject() {
		if (winRejectionModal != null) {
			winRejectionModal.setVisible(false);
		}
	}

	public void onClick$btnConfirmModalReject() {
		if (cmbModalRejectionReason == null || cmbModalRejectionReason.getSelectedItem() == null) {
			Messagebox.show("Please select a valid rejection reason / return code.", "Reason Required", Messagebox.OK,
					Messagebox.EXCLAMATION);
			if (cmbModalRejectionReason != null)
				cmbModalRejectionReason.focus();
			return;
		}

		InwardCheque current = activeQueue.get(currentIndex);
		String reasonId = cmbModalRejectionReason.getSelectedItem().getValue();
		String remarks = txtModalRejectionRemark != null ? txtModalRejectionRemark.getValue().trim() : "";

		User currentUser = (User) Sessions.getCurrent().getAttribute("LOGGED_IN_USER");
		String userId = currentUser != null ? currentUser.getUserId() : "SYSTEM";

		current.setChequeStatus(InwardChequeStatus.REJECTION_REQUESTED.name());
		chequeService.updateChequeDetails(current);

		saveRejectionRequestRecord(current.getInwardChequeId(), this.currentBatchId, reasonId, remarks, userId,
				"DATA_ENTRY");

		String reasonLabel = cmbModalRejectionReason.getSelectedItem().getLabel();
		String notifMsg = "Rejection requested for Cheque #" + current.getChequeNumber() + " in Batch "
				+ this.currentBatchId + " (" + reasonLabel + ") by Maker " + userId + ". Stage: DATA_ENTRY.";

		notificationService.sendNotification("INWARD_CHECKER", null, notifMsg);

		if (winRejectionModal != null) {
			winRejectionModal.setVisible(false);
		}

		if (currentIndex < activeQueue.size() - 1) {
			currentIndex++;
		}
		displayCurrentCheque();
	}

	private void saveRejectionRequestRecord(String chequeId, String batchId, String reasonId, String remarks,
			String userId, String stage) {
		String sql = "INSERT INTO inward_cheque_rejection_request "
				+ "(inward_cheque_id, inward_batch_id, rejected_reason_id, remarks, requested_by, request_stage, request_status, requested_at) "
				+ "VALUES (?, ?, ?, ?, ?, ?, 'PENDING', CURRENT_TIMESTAMP)";
		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, chequeId);
			ps.setString(2, batchId);
			ps.setString(3, reasonId);
			ps.setString(4, remarks);
			ps.setString(5, userId);
			ps.setString(6, stage);
			ps.executeUpdate();
		} catch (Exception e) {
			System.err.println("WARN: Failed to persist inward_cheque_rejection_request: " + e.getMessage());
		}
	}

	public void onClick$btnSubmitToChecker() {
		List<InwardCheque> fullBatchCheques = chequeService.getChequesByBatchAndStatus(this.currentBatchId, null);
		if (fullBatchCheques == null || fullBatchCheques.isEmpty())
			return;

		long accepted = fullBatchCheques.stream()
				.filter(c -> InwardChequeStatus.CHECKER_PROCESSING_PENDING.name().equalsIgnoreCase(c.getChequeStatus())
						|| InwardChequeStatus.MAKER_RETURNED.name().equalsIgnoreCase(c.getChequeStatus())
						|| "ACCEPTED".equalsIgnoreCase(c.getChequeStatus())
						|| "DATA_ENTRY_COMPLETED".equalsIgnoreCase(c.getChequeStatus()))
				.count();
		long rejected = fullBatchCheques.stream()
				.filter(c -> InwardChequeStatus.REJECTION_REQUESTED.name().equalsIgnoreCase(c.getChequeStatus())
						|| InwardChequeStatus.REJECTED.name().equalsIgnoreCase(c.getChequeStatus()))
				.count();

		if (lblModalTotal != null)
			lblModalTotal.setValue(String.valueOf(fullBatchCheques.size()));
		if (lblModalAccepted != null)
			lblModalAccepted.setValue(String.valueOf(accepted));
		if (lblModalRejected != null)
			lblModalRejected.setValue(String.valueOf(rejected));

		if (winCompletionConfirmModal != null) {
			winCompletionConfirmModal.setVisible(true);
		}
	}

	public void onClick$btnCancelCompletionModal() {
		if (winCompletionConfirmModal != null) {
			winCompletionConfirmModal.setVisible(false);
		}
	}

	public void onClick$btnConfirmCompletionModal() {
		executeSubmitToChecker();
	}

	private void executeSubmitToChecker() {
		if (winCompletionConfirmModal != null) {
			winCompletionConfirmModal.setVisible(false);
		}

		try {

			batchService.updateBatchStatus(this.currentBatchId, "CHECKER_PROCESSING_PENDING");

			User currentUser = (User) Sessions.getCurrent().getAttribute("LOGGED_IN_USER");
			String userId = (currentUser != null && currentUser.getUserId() != null) ? currentUser.getUserId()
					: "Maker";
			String notifMsg = this.isReworkBatch
					? "Rework for Batch " + currentBatchId + " completed and submitted to Checker by Maker (" + userId
							+ ")."
					: "Batch " + currentBatchId + " submitted to Checker by Maker (" + userId + ").";

			notificationService.sendNotification("INWARD_CHECKER", null, notifMsg);
		} catch (Exception e) {
			e.printStackTrace();
		}

		Sessions.getCurrent().removeAttribute("ACTIVE_INWARD_BATCH_ID");

		Include mainInclude = null;
		try {
			mainInclude = (Include) Path.getComponent("/inwardMakerRootWin/mainContentArea");
		} catch (Exception ignored) {
		}

		if (mainInclude == null && self != null && self.getDesktop() != null) {
			for (org.zkoss.zk.ui.Page p : self.getDesktop().getPages()) {
				Component comp = p.getFellowIfAny("mainContentArea", true);
				if (comp instanceof Include) {
					mainInclude = (Include) comp;
					break;
				}
			}
		}

		if (mainInclude != null) {
			mainInclude.invalidate();
			mainInclude.setSrc("/inward/maker/data-entry/data-entry-batches.zul");
		}
	}

	private String validateFormFields() {
		String chqNo = txtChequeNumber != null ? txtChequeNumber.getValue().trim() : "";
		if (chqNo.isEmpty())
			return "Cheque Number is mandatory.";
		if (!chqNo.matches("\\d{6}"))
			return "Cheque Number must be exactly 6 digits.";

		String dtStr = txtChequeDate != null ? txtChequeDate.getValue().trim() : "";
		if (dtStr.isEmpty())
			return "Cheque Date is mandatory.";
		try {
			LocalDate.parse(dtStr);
		} catch (Exception e) {
			return "Invalid Cheque Date format. Expected format: YYYY-MM-DD.";
		}

		String amtStr = txtAmount != null ? txtAmount.getValue().replace("₹", "").replace(",", "").trim() : "";
		if (amtStr.isEmpty())
			return "Cheque Amount is mandatory.";
		try {
			BigDecimal amt = new BigDecimal(amtStr);
			if (amt.compareTo(BigDecimal.ZERO) <= 0)
				return "Cheque Amount must be greater than zero.";
		} catch (Exception e) {
			return "Invalid numerical amount.";
		}

		String drwAcc = txtDraweeAccount != null ? txtDraweeAccount.getValue().trim() : "";
		if (drwAcc.isEmpty())
			return "Drawee Account Number is mandatory.";

		return null;
	}

	private void clearForm() {
		if (imgCheque != null)
			imgCheque.setSrc(null);
		if (txtChequeNumber != null)
			txtChequeNumber.setValue("");
		if (txtChequeDate != null)
			txtChequeDate.setValue("");
		if (txtAmount != null)
			txtAmount.setValue("");
		if (txtAmountInWords != null)
			txtAmountInWords.setValue("");
		if (txtDraweeAccount != null)
			txtDraweeAccount.setValue("");
		if (txtDraweeBankName != null)
			txtDraweeBankName.setValue("");
		if (txtPayeeName != null)
			txtPayeeName.setValue("");
		if (txtEntryRemark != null)
			txtEntryRemark.setValue("");
	}

	private static final String[] UNITS = { "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine",
			"Ten", "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen",
			"Nineteen" };

	private static final String[] TENS = { "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty",
			"Ninety" };

	public static String convertToIndianCurrencyWords(BigDecimal amount) {
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0)
			return "";
		long wholeNumber = amount.longValue();
		int paise = amount.remainder(BigDecimal.ONE).movePointRight(2).intValue();
		StringBuilder words = new StringBuilder("Rupees ");
		words.append(convertNumberToWords(wholeNumber));
		if (paise > 0) {
			words.append(" and ").append(convertNumberToWords(paise)).append(" Paise");
		}
		words.append(" Only");
		return words.toString();
	}

	private static String convertNumberToWords(long n) {
		if (n == 0)
			return "Zero";
		if (n < 0)
			return "Minus " + convertNumberToWords(Math.abs(n));
		StringBuilder sb = new StringBuilder();
		if ((n / 10000000) > 0) {
			sb.append(convertNumberToWords(n / 10000000)).append(" Crore ");
			n %= 10000000;
		}
		if ((n / 100000) > 0) {
			sb.append(convertNumberToWords(n / 100000)).append(" Lakh ");
			n %= 100000;
		}
		if ((n / 1000) > 0) {
			sb.append(convertNumberToWords(n / 1000)).append(" Thousand ");
			n %= 1000;
		}
		if ((n / 100) > 0) {
			sb.append(convertNumberToWords(n / 100)).append(" Hundred ");
			n %= 100;
		}
		if (n > 0) {
			if (n < 20) {
				sb.append(UNITS[(int) n]).append(" ");
			} else {
				sb.append(TENS[(int) (n / 10)]).append(" ");
				if ((n % 10) > 0)
					sb.append(UNITS[(int) (n % 10)]).append(" ");
			}
		}
		return sb.toString().trim();
	}

	public void onChange$txtAmount() {
		if (txtAmount == null || txtAmountInWords == null)
			return;
		String raw = txtAmount.getValue().replace("₹", "").replace(",", "").trim();
		try {
			BigDecimal val = new BigDecimal(raw);
			txtAmountInWords.setValue(convertToIndianCurrencyWords(val));
		} catch (Exception e) {
			txtAmountInWords.setValue("Invalid Amount");
		}
	}
	
	public void onClick$btnBackToList() {
		Sessions.getCurrent().removeAttribute("ACTIVE_INWARD_BATCH_ID");

		Include mainInclude = null;
		try {
			mainInclude = (Include) Path.getComponent("/inwardMakerRootWin/mainContentArea");
		} catch (Exception ignored) {}

		if (mainInclude == null && self != null && self.getDesktop() != null) {
			for (org.zkoss.zk.ui.Page p : self.getDesktop().getPages()) {
				Component comp = p.getFellowIfAny("mainContentArea", true);
				if (comp instanceof Include) {
					mainInclude = (Include) comp;
					break;
				}
			}
		}

		if (mainInclude != null) {
			mainInclude.invalidate();
			mainInclude.setSrc("/inward/maker/data-entry/data-entry-batches.zul");
		} else {
			Executions.sendRedirect("/inward/maker/index.zul?page=data-entry-batches");
		}
	}
}