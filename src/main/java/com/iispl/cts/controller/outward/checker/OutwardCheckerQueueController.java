package com.iispl.cts.controller.outward.checker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dto.RejectRequestDTO;
import com.iispl.cts.entity.User;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.OutwardRejectedCheques;
import com.iispl.cts.entity.outward.RejectedReason;
import com.iispl.cts.entity.outward.SendBackReason;
import com.iispl.cts.service.outward.OutwardCheckerQueueService;
import com.iispl.cts.serviceimpl.outward.OutwardCheckerQueueServiceImpl;

public class OutwardCheckerQueueController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;

	private Component self;

	private Label lblChequeCount;
	private Label lblBatchNo;
	private Label lblChequeNo;
	private Label lblQueueStatus;
	private Label lblCurrentCheque;
	private Label lblRemaining;
	private Label lblZoom;
	private Label lblAccountValidation;
	private Label lblCurrentChequeNavigation;

	private Image imgCheque;

	private Button btnImageSide;

	private boolean showingBackImage = false;

	private String frontImagePath;
	private String backImagePath;

	private Textbox txtChequeNo;
	private Textbox txtMicr;
	private Textbox txtAccountNo;
	private Textbox txtAmount;
	private Textbox txtChequeDate;
	private Textbox txtpayeeName;

	private Progressmeter progressBar;
	private Label lblProgress;

	private Button btnPrevious;
	private Button btnNext;

	private Button btnXmlGeneration;

	private Button btnVerified;
	private Button btnReturn;
	private Button btnReject;

	private Button btnZoomIn;
	private Button btnZoomOut;
	private Button btnZoomReset;

	private Window returnMakerWindow;

	private Label lblReturnBatch;
	private Label lblReturnCheque;

	private Combobox cmbSendBackReason;

	private Textbox txtReturnRemarks;

	private Button btnReturnConfirm;
	private Button btnReturnCancel;

	private Vlayout noBatchMessage;

	private Div checkerQueueContent;

	private Window accountValidationWindow;

	private Label lblAccountValidationPopupIcon;

	private Label lblAccountValidationPopupTitle;

	private Label lblAccountValidationPopupMessage;

	private Button btnAccountValidationOk;

	private Window rejectWindow;

	private Label lblRejectBatch;
	private Label lblRejectCheque;

	private Combobox cmbRejectReason;

	private Textbox txtRejectRemarks;

	private Button btnRejectConfirm;
	private Button btnRejectCancel;

	private Vlayout makerRejectionRequestSection;

	private Label lblMakerStatusHeading;
	private Label lblMakerRejectionReasonCode;
	private Label lblMakerRejectionReasonName;
	private Label lblMakerRejectionRemarks;

	private OutwardCheque currentCheque;

	private boolean accountValidationPassed = false;

	private OutwardCheque chequeWaitingForVerification;

	private Map<String, String> accountValidationResults = new HashMap<>();

	private List<OutwardCheque> cheques = new ArrayList<>();

	private int currentIndex = 0;

	private String batchId;

	private String batchNo;

	private String batchStatus;

	private OutwardCheckerQueueService outwardCheckerQueueService;

	private double zoomLevel = 1.0;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		

		super.doAfterCompose(comp);

		self = comp;

		makerRejectionRequestSection = (Vlayout) self.getFellowIfAny("makerRejectionRequestSection");

		lblMakerStatusHeading = (Label) self.getFellowIfAny("lblMakerStatusHeading");

		lblMakerRejectionReasonCode = (Label) self.getFellowIfAny("lblMakerRejectionReasonCode");

		lblMakerRejectionReasonName = (Label) self.getFellowIfAny("lblMakerRejectionReasonName");

		lblMakerRejectionRemarks = (Label) self.getFellowIfAny("lblMakerRejectionRemarks");

		outwardCheckerQueueService = new OutwardCheckerQueueServiceImpl();

		System.out.println("==========================================");
		System.out.println("OUTWARD CHECKER QUEUE");
		System.out.println("==========================================");

		if (btnXmlGeneration != null) {

			btnXmlGeneration.setVisible(true);
			btnXmlGeneration.setDisabled(true);
		}

		Object sessionBatchId = Sessions.getCurrent().getAttribute("SELECTED_OUTWARD_BATCH_ID");

		if (sessionBatchId != null) {

			batchId = String.valueOf(sessionBatchId);

		} else {

			String requestBatchId = Executions.getCurrent().getParameter("batchId");

			if (requestBatchId != null && !requestBatchId.trim().isEmpty()) {

				batchId = requestBatchId.trim();
			}
		}

		if (batchId == null || batchId.trim().isEmpty()) {

			System.out.println("No batch selected.");

			if (noBatchMessage != null) {
				noBatchMessage.setVisible(true);
			}

			if (checkerQueueContent != null) {
				checkerQueueContent.setVisible(false);
			}

			return;
		}

		batchNo = batchId;

		if (lblBatchNo != null) {
			lblBatchNo.setValue(batchNo);
		}

		if (noBatchMessage != null) {
			noBatchMessage.setVisible(false);
		}

		if (checkerQueueContent != null) {
			checkerQueueContent.setVisible(true);
		}

		createReturnMakerWindow();

		loadCheques();

		updatePaginationProgress();

		String targetChequeNo = Executions.getCurrent().getParameter("chequeNo");

		String targetChequeId = Executions.getCurrent().getParameter("chequeId");

		if (targetChequeNo == null || targetChequeNo.trim().isEmpty()) {

			Object selectedChequeNo = Sessions.getCurrent().getAttribute("SELECTED_VERIFY_CHEQUE_NO");

			if (selectedChequeNo != null) {

				targetChequeNo = String.valueOf(selectedChequeNo);

				Sessions.getCurrent().removeAttribute("SELECTED_VERIFY_CHEQUE_NO");
			}
		}

		if (targetChequeId == null || targetChequeId.trim().isEmpty()) {

			Object selectedChequeId = Sessions.getCurrent().getAttribute("SELECTED_VERIFY_CHEQUE_ID");

			if (selectedChequeId != null) {

				targetChequeId = String.valueOf(selectedChequeId);

				Sessions.getCurrent().removeAttribute("SELECTED_VERIFY_CHEQUE_ID");
			}
		}

		selectSpecificCheque(targetChequeNo, targetChequeId);

		updateXmlGenerationButton();

		createRejectWindow();

	}

	private void loadRejectRequestDetails(OutwardCheque cheque) {

		if (makerRejectionRequestSection != null) {
			makerRejectionRequestSection.setVisible(false);
		}

		if (lblMakerStatusHeading != null) {
			lblMakerStatusHeading.setValue("");
		}

		if (lblMakerRejectionReasonCode != null) {
			lblMakerRejectionReasonCode.setValue("");
		}

		if (lblMakerRejectionReasonName != null) {
			lblMakerRejectionReasonName.setValue("");
		}

		if (lblMakerRejectionRemarks != null) {
			lblMakerRejectionRemarks.setValue("");
		}

		if (cheque == null) {
			return;
		}

		String status = cheque.getChequeStatus();

		if (status == null || status.trim().isEmpty()) {
			return;
		}

		status = status.trim();

		System.out.println("==========================================");
		System.out.println("CHECKER MESSAGE STATUS");
		System.out.println("Cheque No : " + cheque.getChequeNumber());
		System.out.println("Status    : " + status);
		System.out.println("==========================================");

		if ("PENDING_VERIFICATION".equalsIgnoreCase(status)) {

			if (makerRejectionRequestSection == null) {
				return;
			}

			makerRejectionRequestSection.setVisible(true);

			makerRejectionRequestSection.setStyle("margin:6px 0 6px 0;" + "padding:8px 10px;" + "background:#ECFDF5;"
					+ "border-left:3px solid #22C55E;" + "border-radius:3px;");

			// Heading
			if (lblMakerStatusHeading != null) {

				lblMakerStatusHeading.setValue("APPROVED BY MAKER");

				lblMakerStatusHeading.setStyle("font-size:10px;" + "font-weight:700;" + "color:#15803D;");
			}

			// Message
			if (lblMakerRejectionReasonCode != null) {

				lblMakerRejectionReasonCode.setValue("Maker verification complete");

				lblMakerRejectionReasonCode.setStyle("font-size:11px;" + "color:#475569;" + "white-space:normal;");
			}

			if (lblMakerRejectionReasonName != null) {
				lblMakerRejectionReasonName.setValue("");
			}

			if (lblMakerRejectionRemarks != null) {
				lblMakerRejectionRemarks.setValue("");
			}

			return;
		}

		if ("REJECT_REQUEST".equalsIgnoreCase(status)) {

			try {

				RejectRequestDTO request = outwardCheckerQueueService
						.getRejectRequestByChequeId(cheque.getOutwardChequeId());

				if (request == null) {
					return;
				}

				if (makerRejectionRequestSection != null) {

					makerRejectionRequestSection.setVisible(true);

					makerRejectionRequestSection.setStyle("margin:6px 0 6px 0;" + "padding:8px 10px;"
							+ "background:#FFF7ED;" + "border-left:3px solid #F59E0B;" + "border-radius:3px;");
				}

				// Heading
				if (lblMakerStatusHeading != null) {

					lblMakerStatusHeading.setValue("REJECTED BY MAKER");

					lblMakerStatusHeading.setStyle("font-size:10px;" + "font-weight:700;" + "color:#B45309;");
				}

				// Reason code
				if (lblMakerRejectionReasonCode != null) {

					lblMakerRejectionReasonCode
							.setValue("Reject Code : "+(request.getRejectedReasonId() != null ? request.getRejectedReasonId() : "-"));
				}

				// Reason name
				if (lblMakerRejectionReasonName != null) {

					lblMakerRejectionReasonName
							.setValue("Reject reason : "+(request.getRejectedReasonName() != null ? request.getRejectedReasonName() : "-"));
				}

				// Remarks
				if (lblMakerRejectionRemarks != null) {

					lblMakerRejectionRemarks.setValue("Remarks : "+(request.getRemarks() != null ? request.getRemarks() : "-"));
				}

			} catch (Exception e) {

				e.printStackTrace();

				if (makerRejectionRequestSection != null) {
					makerRejectionRequestSection.setVisible(false);
				}
			}

			return;
		}

		if ("VERIFIED_BY_CHECKER".equalsIgnoreCase(status)) {

			if (makerRejectionRequestSection == null) {
				return;
			}

			// SHOW MESSAGE
			makerRejectionRequestSection.setVisible(true);

			// GREEN MESSAGE BOX
			makerRejectionRequestSection.setStyle("margin:6px 0 6px 0;" + "padding:8px 10px;" + "background:#ECFDF5;"
					+ "border-left:3px solid #22C55E;" + "border-radius:3px;");

			// Heading
			if (lblMakerStatusHeading != null) {

				lblMakerStatusHeading.setValue("APPROVED BY CHECKER");

				lblMakerStatusHeading.setStyle("font-size:10px;" + "font-weight:700;" + "color:#15803D;");
			}

			// Message
			if (lblMakerRejectionReasonCode != null) {

				lblMakerRejectionReasonCode.setValue("Checker verification completed");

				lblMakerRejectionReasonCode.setStyle("font-size:11px;" + "color:#475569;" + "white-space:normal;");
			}

			// Clear unused fields
			if (lblMakerRejectionReasonName != null) {
				lblMakerRejectionReasonName.setValue("");
			}

			if (lblMakerRejectionRemarks != null) {
				lblMakerRejectionRemarks.setValue("");
			}

			System.out.println("Cheque " + cheque.getChequeNumber() + " is VERIFIED_BY_CHECKER"
					+ " -> showing APPROVED BY CHECKER");

			return;
		}

		if (makerRejectionRequestSection != null) {
			makerRejectionRequestSection.setVisible(false);
		}
	}

	private void updatePaginationProgress() {

		if (cheques == null || cheques.isEmpty()) {

			progressBar.setValue(0);

			lblProgress.setValue("0/0 (0%)");

			lblCurrentChequeNavigation.setValue("0 of 0");

			btnPrevious.setDisabled(true);

			btnNext.setDisabled(true);

			return;
		}

		int total = cheques.size();

		int current = currentIndex + 1;

		int percentage = (current * 100) / total;

		progressBar.setValue(percentage);

		lblProgress.setValue(current + "/" + total + " (" + percentage + "%)");

		lblCurrentChequeNavigation.setValue(current + " of " + total);

		btnPrevious.setDisabled(currentIndex <= 0);

		btnNext.setDisabled(currentIndex >= total - 1);
	}

	private void loadRejectedReasons() {

		try {

			List<RejectedReason> reasons = outwardCheckerQueueService.getRejectedReasons();

			cmbRejectReason.getItems().clear();

			for (RejectedReason reason : reasons) {

				Comboitem item = new Comboitem();

				item.setLabel(nullSafe(reason.getRejectedReasonName()));

				item.setValue(reason.getRejectedReasonCode());

				cmbRejectReason.appendChild(item);
			}

			System.out.println("Rejected reasons loaded = " + reasons.size());

		} catch (Exception e) {

			e.printStackTrace();

			showError("Unable to load rejection reasons.", e);
		}
	}

	private void createReturnMakerWindow() {

		try {

			if (returnMakerWindow != null) {

				try {

					if (returnMakerWindow.getPage() != null) {
						returnMakerWindow.detach();
					}

				} catch (Exception e) {
					e.printStackTrace();
				}

				returnMakerWindow = null;
			}

			returnMakerWindow = (Window) Executions.createComponents("/outward/checker/return-to-maker.zul", null,
					null);

			if (self != null && self.getPage() != null) {

				returnMakerWindow.setPage(self.getPage());

			} else {

				throw new IllegalStateException("Current page is not available.");
			}

			lblReturnBatch = (Label) returnMakerWindow.getFellowIfAny("lblReturnBatch");

			lblReturnCheque = (Label) returnMakerWindow.getFellowIfAny("lblReturnCheque");

			cmbSendBackReason = (Combobox) returnMakerWindow.getFellowIfAny("cmbSendBackReason");

			txtReturnRemarks = (Textbox) returnMakerWindow.getFellowIfAny("txtReturnRemarks");

			btnReturnConfirm = (Button) returnMakerWindow.getFellowIfAny("btnReturnConfirm");

			btnReturnCancel = (Button) returnMakerWindow.getFellowIfAny("btnReturnCancel");

			if (btnReturnConfirm != null) {

				btnReturnConfirm.addEventListener("onClick", event -> confirmReturnToMaker());
			}

			if (btnReturnCancel != null) {

				btnReturnCancel.addEventListener("onClick", event -> closeReturnMakerWindow());
			}

			/*
			 * Initially hidden.
			 */
			returnMakerWindow.setVisible(false);

			System.out.println("Return to Maker window created and attached.");

		} catch (Exception e) {

			e.printStackTrace();

			returnMakerWindow = null;

			Messagebox.show("Unable to create Return to Maker window.\n\n" + e.getMessage(), "Return to Maker",
					Messagebox.OK, Messagebox.ERROR);
		}
	}

	private void closeReturnMakerWindow() {

		try {

			if (returnMakerWindow != null) {

				if (returnMakerWindow.getPage() != null) {

					returnMakerWindow.detach();

				}

				returnMakerWindow = null;
			}

		} catch (Exception e) {

			e.printStackTrace();

			returnMakerWindow = null;
		}
	}

	private void loadCheques() {

		try {

			System.out.println("Loading cheques for batch = " + batchId);

			cheques = outwardCheckerQueueService.getChequesByBatchId(batchId);

			if (cheques == null) {

				cheques = new ArrayList<>();
			}

			accountValidationResults.clear();

			for (OutwardCheque loadedCheque : cheques) {

				if (loadedCheque == null) {
					continue;
				}

				String loadedChequeId = nullSafe(loadedCheque.getOutwardChequeId());

				if ("VERIFIED_BY_CHECKER".equalsIgnoreCase(nullSafe(loadedCheque.getChequeStatus()))) {

					accountValidationResults.put(loadedChequeId, "VALID");

					System.out.println(
							"Cheque " + loadedCheque.getChequeNumber() + " is already VERIFIED_BY_CHECKER -> VALID");
				}
			}

			loadBatchStatus();

			if (cheques.isEmpty()) {

				if (lblCurrentCheque != null) {
					lblCurrentCheque.setValue("0");
				}

				if (lblRemaining != null) {
					lblRemaining.setValue("0");
				}

				updateXmlGenerationButton();

				return;
			}

			currentIndex = 0;

			/*
			 * 
			 * displayCheque() does NOT validate account number.
			 */
			displayCheque();

			updateXmlGenerationButton();

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to load cheques.\n\n" + e.getMessage(), "Checker Queue", Messagebox.OK,
					Messagebox.ERROR);
		}
	}

	private void loadChequeStatus() {

		try {

			if (currentCheque == null) {

				lblQueueStatus.setValue("-");
				lblQueueStatus.setSclass("status-pending");

				return;
			}

			String chequeStatus = currentCheque.getChequeStatus();

			if (chequeStatus == null || chequeStatus.trim().isEmpty()) {

				lblQueueStatus.setValue("-");
				lblQueueStatus.setSclass("status-pending");

				return;
			}

			// Remove unnecessary spaces
			chequeStatus = chequeStatus.trim();

			if ("VERIFIED_BY_CHECKER".equalsIgnoreCase(chequeStatus)) {

				lblQueueStatus.setValue("Verified checker");

				lblQueueStatus.setSclass("status-verified");

				return;
			}

			String displayStatus = chequeStatus.toLowerCase().replace("_", " ");

			if (!displayStatus.isEmpty()) {

				displayStatus = Character.toUpperCase(displayStatus.charAt(0)) + displayStatus.substring(1);
			}

			lblQueueStatus.setValue(displayStatus);

			lblQueueStatus.setSclass("status-pending");

		} catch (Exception e) {

			e.printStackTrace();

			lblQueueStatus.setValue("-");
			lblQueueStatus.setSclass("status-pending");
		}
	}

	private void selectSpecificCheque(String targetChequeNo, String targetChequeId) {

		if (cheques == null || cheques.isEmpty()) {

			return;
		}

		if ((targetChequeNo == null || targetChequeNo.trim().isEmpty())
				&& (targetChequeId == null || targetChequeId.trim().isEmpty())) {

			return;
		}

		for (int i = 0; i < cheques.size(); i++) {

			OutwardCheque cheque = cheques.get(i);

			if (cheque == null) {

				continue;
			}

			boolean matchChequeNo = targetChequeNo != null && !targetChequeNo.trim().isEmpty()
					&& targetChequeNo.equals(cheque.getChequeNumber());

			boolean matchChequeId = targetChequeId != null && !targetChequeId.trim().isEmpty()
					&& targetChequeId.equals(cheque.getOutwardChequeId());

			if (matchChequeNo || matchChequeId) {

				currentIndex = i;

				displayCheque();

				System.out.println("Selected cheque = " + cheque.getChequeNumber());

				return;
			}
		}
	}

	private void displayCheque() {

		if (cheques == null || cheques.isEmpty() || currentIndex < 0 || currentIndex >= cheques.size()) {

			return;
		}

		try {

			OutwardCheque cheque = cheques.get(currentIndex);
			currentCheque = cheque;

			if (cheque == null) {

				return;
			}

			System.out.println("Displaying cheque = " + cheque.getChequeNumber());

			if (lblChequeNo != null) {

				lblChequeNo.setValue(nullSafe(cheque.getChequeNumber()));
			}

			if (txtChequeNo != null) {

				txtChequeNo.setValue(nullSafe(cheque.getChequeNumber()));
			}

			if (txtMicr != null) {

				txtMicr.setValue(nullSafe(cheque.getMicrCode()));
			}

			if (txtAccountNo != null) {

				txtAccountNo.setValue(nullSafe(cheque.getPayeeAccountNumber()));
			}

			if (txtAmount != null) {

				if (cheque.getChequeAmount() != null) {

					txtAmount.setValue(String.valueOf(cheque.getChequeAmount()));

				} else {

					txtAmount.setValue("");
				}
			}

			if (txtChequeDate != null) {

				if (cheque.getChequeDate() != null) {

					txtChequeDate.setValue(new SimpleDateFormat("dd-MM-yyyy").format(cheque.getChequeDate()));

				} else {

					txtChequeDate.setValue("");
				}
			}

			if (txtpayeeName != null) {

				txtpayeeName.setValue(nullSafe(cheque.getPayeeName()));
			}

			loadRejectRequestDetails(cheque);

			String chequeId = nullSafe(cheque.getOutwardChequeId());

			String chequeStatus = nullSafe(cheque.getChequeStatus()).trim();

			String validationResult = accountValidationResults.get(chequeId);

			if ("VERIFIED_BY_CHECKER".equalsIgnoreCase(chequeStatus)) {

				accountValidationResults.put(chequeId, "VALID");

				accountValidationPassed = true;

				setAccountValidationLabel("VALID");

				System.out.println(
						"Cheque " + cheque.getChequeNumber() + " status = VERIFIED_BY_CHECKER" + " -> Account = VALID");
			}

			else if ("VALID".equalsIgnoreCase(validationResult)) {

				accountValidationPassed = true;

				setAccountValidationLabel("VALID");
			}

			else if ("INVALID".equalsIgnoreCase(validationResult)) {

				accountValidationPassed = false;

				setAccountValidationLabel("INVALID");
			}

			else {

				accountValidationPassed = false;

				setAccountValidationLabel("NOT VERIFIED");
			}

			if (btnVerified != null) {

				boolean alreadyVerified = "VERIFIED_BY_CHECKER".equalsIgnoreCase(chequeStatus);

				btnVerified.setDisabled(alreadyVerified);
			}

			frontImagePath = convertImagePath(cheque.getChequeImageFront());

			backImagePath = convertImagePath(cheque.getChequeImageBack());

			showingBackImage = false;

			if (imgCheque != null) {

				if (frontImagePath != null && !frontImagePath.isEmpty()) {

					imgCheque.setSrc(frontImagePath);

				} else {

					imgCheque.setSrc("");
				}

				applyZoom();
			}

			if (btnImageSide != null) {

				btnImageSide.setLabel("BACK SIDE");
			}

			updateNavigation();

			// Load cheque status
			loadChequeStatus();

			updateXmlGenerationButton();

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to display cheque.\n\n" + e.getMessage(), "Checker Queue", Messagebox.OK,
					Messagebox.ERROR);
		}
	}

	private void setAccountValidationLabel(String status) {

		if (lblAccountValidation == null) {

			return;
		}

		lblAccountValidation.setValue(status);

		if ("VALID".equalsIgnoreCase(status)) {

			lblAccountValidation.setSclass("valid-badge");

		} else if ("INVALID".equalsIgnoreCase(status)) {

			lblAccountValidation.setSclass("invalid-badge");

		} else {

			lblAccountValidation.setSclass("validation-pending-badge");
		}
	}

	public void onClick$btnVerified(Event event) {

		if (cheques == null || cheques.isEmpty() || currentIndex < 0 || currentIndex >= cheques.size()) {

			Messagebox.show("No cheque is currently selected.", "Verify Cheque", Messagebox.OK, Messagebox.EXCLAMATION);

			return;
		}

		try {

			OutwardCheque cheque = cheques.get(currentIndex);

			if (cheque == null) {
				return;
			}

			if ("VERIFIED_BY_CHECKER".equalsIgnoreCase(nullSafe(cheque.getChequeStatus()))) {

				System.out.println("Cheque " + cheque.getChequeNumber() + " is already validated.");

				// Keep validation status as VALID
				accountValidationPassed = true;

				saveAccountValidationResult(cheque, "VALID");

				setAccountValidationLabel("VALID");

				// Refresh checker approval message
				loadRejectRequestDetails(cheque);

				Messagebox.show("This cheque is already validated.", "Already Validated", Messagebox.OK,
						Messagebox.INFORMATION);

				return;
			}

			chequeWaitingForVerification = cheque;

			accountValidationPassed = false;

			setAccountValidationLabel("NOT VERIFIED");

			String payeeAccountNumber = nullSafe(cheque.getPayeeAccountNumber()).trim();

			if (payeeAccountNumber.isEmpty()) {

				saveAccountValidationResult(cheque, "INVALID");

				accountValidationPassed = false;

				showAccountValidationPopup(false, "Payee account number is missing.");

				return;
			}

			System.out.println("==========================================");

			System.out.println("ACCOUNT VALIDATION");

			System.out.println("Cheque Number = " + cheque.getChequeNumber());

			System.out.println("Account Number = " + payeeAccountNumber);

			boolean accountExists = outwardCheckerQueueService.isPayeeAccountExists(payeeAccountNumber);

			if (accountExists) {

				accountValidationPassed = true;

				saveAccountValidationResult(cheque, "VALID");

				outwardCheckerQueueService.updateChequeStatus(cheque.getChequeNumber(), "VERIFIED_BY_CHECKER");

				cheque.setChequeStatus("VERIFIED_BY_CHECKER");

				loadRejectRequestDetails(cheque);

				loadChequeStatus();

				showAccountValidationPopup(true,
						"Account number " + payeeAccountNumber + " is valid. Cheque verified successfully.");

			} else {

				accountValidationPassed = false;

				saveAccountValidationResult(cheque, "INVALID");

				showAccountValidationPopup(false, "Account number " + payeeAccountNumber + " is invalid.");
			}

		} catch (Exception e) {

			e.printStackTrace();

			accountValidationPassed = false;

			setAccountValidationLabel("NOT VERIFIED");

			showError("Unable to validate payee account.", e);
		}
	}

	private void saveAccountValidationResult(OutwardCheque cheque, String result) {

		if (cheque == null) {

			return;
		}

		String chequeId = nullSafe(cheque.getOutwardChequeId());

		accountValidationResults.put(chequeId, result);

		setAccountValidationLabel(result);

		System.out.println("Cheque " + cheque.getChequeNumber() + " = " + result);

		updateXmlGenerationButton();
	}

	private void showAccountValidationPopup(boolean valid, String message) {

		try {

			System.out.println("==========================================");
			System.out.println("OPEN ACCOUNT VALIDATION POPUP");
			System.out.println("Valid  = " + valid);
			System.out.println("Message = " + message);
			System.out.println("==========================================");

			if (accountValidationWindow != null) {

				try {
					accountValidationWindow.detach();
				} catch (Exception ex) {
					ex.printStackTrace();
				}

				accountValidationWindow = null;
			}

			accountValidationWindow = new Window();

			accountValidationWindow.setWidth("430px");
			accountValidationWindow.setHeight("250px");
			accountValidationWindow.setBorder("none");
			accountValidationWindow.setClosable(false);
			accountValidationWindow.setSizable(false);
			accountValidationWindow.setPosition("center");

			accountValidationWindow.setStyle(
					"background:#ffffff;" + "border-radius:12px;" + "box-shadow:0 10px 35px rgba(0,0,0,0.20);");

			/*
			 * -------------------------------------------------------- MAIN CONTAINER
			 * --------------------------------------------------------
			 */
			Div container = new Div();

			container.setStyle("width:100%;" + "height:100%;" + "box-sizing:border-box;" + "padding:25px 30px;"
					+ "text-align:center;");

			/*
			 * -------------------------------------------------------- ICON
			 * --------------------------------------------------------
			 */
			lblAccountValidationPopupIcon = new Label();

			lblAccountValidationPopupIcon.setValue(valid ? "✓" : "!");

			lblAccountValidationPopupIcon.setStyle("display:block;" + "width:52px;" + "height:52px;"
					+ "line-height:52px;" + "margin:0 auto 14px;" + "border-radius:50%;"
					+ (valid ? "background:#dcfce7;color:#16a34a;" : "background:#fee2e2;color:#dc2626;")
					+ "font-size:28px;" + "font-weight:bold;");

			/*
			 * -------------------------------------------------------- TITLE
			 * --------------------------------------------------------
			 */
			lblAccountValidationPopupTitle = new Label();

			lblAccountValidationPopupTitle.setValue(valid ? "Account Number Valid" : "Invalid Account Number");

			lblAccountValidationPopupTitle.setStyle("display:block;" + "margin-bottom:10px;" + "font-size:20px;"
					+ "font-weight:700;" + "color:#1f2937;");

			/*
			 * -------------------------------------------------------- MESSAGE
			 * --------------------------------------------------------
			 */
			lblAccountValidationPopupMessage = new Label();

			lblAccountValidationPopupMessage.setValue(message);

			lblAccountValidationPopupMessage.setStyle("display:block;" + "width:100%;" + "margin-bottom:22px;"
					+ "font-size:14px;" + "line-height:22px;" + "color:#6b7280;" + "text-align:center;");

			/*
			 * -------------------------------------------------------- OK BUTTON
			 * --------------------------------------------------------
			 */
			btnAccountValidationOk = new Button("OK");

			btnAccountValidationOk.setStyle(
					"width:90px;" + "height:36px;" + "border:none;" + "border-radius:6px;" + "background:#2563eb;"
							+ "color:#ffffff;" + "font-size:14px;" + "font-weight:600;" + "cursor:pointer;");

			btnAccountValidationOk.addEventListener("onClick", event -> {

				if (accountValidationWindow != null) {

					accountValidationWindow.detach();
					accountValidationWindow = null;
				}

				/*
				 * If account is VALID, check whether all cheques are valid.
				 */
				if (accountValidationPassed) {
					checkAllAccountsValid();
				}
			});

			/*
			 * -------------------------------------------------------- ADD COMPONENTS TO
			 * CONTAINER --------------------------------------------------------
			 */
			container.appendChild(lblAccountValidationPopupIcon);
			container.appendChild(lblAccountValidationPopupTitle);
			container.appendChild(lblAccountValidationPopupMessage);
			container.appendChild(btnAccountValidationOk);

			/*
			 * -------------------------------------------------------- ADD CONTAINER TO
			 * WINDOW --------------------------------------------------------
			 */
			accountValidationWindow.appendChild(container);

			/*
			 * ======================================================== IMPORTANT FIX
			 * ========================================================
			 *
			 * DO NOT DO:
			 *
			 * self.appendChild(accountValidationWindow);
			 *
			 * because self is the checkerQueueRoot borderlayout.
			 *
			 * Instead attach the Window directly to the current Page. This makes it a
			 * top-level Window. ========================================================
			 */

			if (self == null || self.getPage() == null) {

				System.out.println("ERROR: self or self.getPage() is NULL");

				Messagebox.show("Unable to open account validation popup.", "Error", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			/*
			 * Attach Window directly to current page.
			 */
			accountValidationWindow.setPage(self.getPage());

			/*
			 * -------------------------------------------------------- OPEN MODAL WINDOW
			 * --------------------------------------------------------
			 */
			accountValidationWindow.doModal();

			System.out.println("Account validation popup opened successfully.");

		} catch (Exception e) {

			System.out.println("ERROR WHILE DISPLAYING ACCOUNT VALIDATION POPUP");

			e.printStackTrace();

			accountValidationWindow = null;

			Messagebox.show(
					"Unable to display account validation popup.\n" + "Please check the server console for details.",
					"Error", Messagebox.OK, Messagebox.ERROR);
		}
	}

	/*
	 * ============================================================ CHECK ALL
	 * ACCOUNTS ============================================================
	 */

	private void checkAllAccountsValid() {

		if (cheques == null || cheques.isEmpty()) {
			return;
		}

		System.out.println("==========================================");
		System.out.println("CHECKING ALL CHEQUES");
		System.out.println("==========================================");

		boolean allValid = true;

		for (OutwardCheque cheque : cheques) {

			if (cheque == null) {
				allValid = false;
				break;
			}

			String chequeId = nullSafe(cheque.getOutwardChequeId());

			String status = nullSafe(cheque.getChequeStatus());

			String result = accountValidationResults.get(chequeId);

			/*
			 * Already verified by Checker.
			 */
			if ("VERIFIED_BY_CHECKER".equalsIgnoreCase(status)) {

				System.out.println("Cheque " + cheque.getChequeNumber() + " = VALID (VERIFIED_BY_CHECKER)");

				continue;
			}

			/*
			 * Normal validation result.
			 */
			System.out.println("Cheque " + cheque.getChequeNumber() + " = " + result);

			if (!"VALID".equalsIgnoreCase(nullSafe(result))) {

				allValid = false;
				break;
			}
		}

		if (!allValid) {

			System.out.println("Not all cheques are VALID yet.");

			updateXmlGenerationButton();
			return;
		}

		System.out.println("ALL CHEQUES ARE VALID.");

		updateXmlGenerationButton();

		System.out.println("APPROVE BATCH button is now ENABLED.");
	}

	/*
	 * ============================================================ UPDATE BATCH
	 * STATUS ============================================================
	 */

	private void updateBatchStatusToVerified() {

		try {

			if (batchId == null || batchId.trim().isEmpty()) {

				Messagebox.show("Batch ID is missing.", "Error", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			/*
			 * Before approving the batch, make sure that all eligible cheques have been
			 * verified.
			 */
			if (!areAllAccountsValid()) {

				Messagebox.show("All cheques in this batch must be verified before approving the batch.",
						"Batch Not Ready", Messagebox.OK, Messagebox.EXCLAMATION);

				return;
			}

			/*
			 * Update batch status in database.
			 */
			outwardCheckerQueueService.updateBatchStatus(batchId, "VERIFIED");

			/*
			 * Update local status.
			 */
			batchStatus = "VERIFIED";

			if (lblQueueStatus != null) {
				lblQueueStatus.setValue("VERIFIED");
			}

			System.out.println("Batch " + batchId + " status updated to VERIFIED.");

			/*
			 * Refresh cheque/batch status information.
			 */
			loadChequeStatus();

			/*
			 * Move to XML Generation page.
			 */
			Executions.sendRedirect("/outward/checker/xml-generation.zul?batchId=" + batchId);

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to approve batch.\n\n" + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
		}
	}

	/*
	 * ============================================================ ARE ALL ACCOUNTS
	 * VALID ============================================================
	 */

	private boolean areAllAccountsValid() {

		if (cheques == null || cheques.isEmpty()) {
			return false;
		}

		/*
		 * Always get the latest batch status from database.
		 */
		loadBatchStatus();

		/*
		 * ============================================================ BATCH ON HOLD
		 * ============================================================
		 *
		 * This happens when Checker returns a cheque to Maker.
		 *
		 * Maker must fix the cheque and send it back.
		 */
		if ("ON_HOLD".equalsIgnoreCase(nullSafe(batchStatus))) {

			System.out.println("Batch " + batchId + " is ON_HOLD." + " Approval is blocked.");

			return false;
		}

		/*
		 * ============================================================ CHECK ALL
		 * CHEQUES ============================================================
		 */
		for (OutwardCheque cheque : cheques) {

			if (cheque == null) {
				return false;
			}

			String chequeId = nullSafe(cheque.getOutwardChequeId());

			String status = nullSafe(cheque.getChequeStatus());

			String result = accountValidationResults.get(chequeId);

			/*
			 * Already verified by Checker.
			 */
			if ("VERIFIED_BY_CHECKER".equalsIgnoreCase(status)) {

				System.out.println("Cheque " + cheque.getChequeNumber() + " = VERIFIED_BY_CHECKER");

				continue;
			}

			/*
			 * Any cheque that is still pending, returned to Maker, rejected, or not
			 * validated must block approval.
			 */
			if (!"VALID".equalsIgnoreCase(nullSafe(result))) {

				System.out.println("Cheque " + cheque.getChequeNumber() + " is NOT VALID.");

				return false;
			}
		}

		System.out.println("ALL CHEQUES ARE VALID " + "AND BATCH IS NOT ON_HOLD.");

		return true;
	}

	/*
	 * ============================================================ XML BUTTON STATE
	 * ============================================================
	 */

	private void updateXmlGenerationButton() {

		if (btnXmlGeneration == null) {
			return;
		}

		/*
		 * Always refresh latest batch status.
		 */
		loadBatchStatus();

		btnXmlGeneration.setVisible(true);

		/*
		 * ============================================================ BATCH ON HOLD
		 * ============================================================
		 *
		 * Returned cheque is still with Maker.
		 */
		if ("ON_HOLD".equalsIgnoreCase(nullSafe(batchStatus))) {

			btnXmlGeneration.setDisabled(true);

			btnXmlGeneration.setSclass("xml-generation-button");

			System.out.println("Batch is ON_HOLD." + " APPROVE BATCH button DISABLED.");

			return;
		}

		/*
		 * ============================================================ CHECK ALL
		 * ACCOUNTS ============================================================
		 */
		boolean allAccountsValid = areAllAccountsValidWithoutBatchCheck();

		if (allAccountsValid) {

			btnXmlGeneration.setDisabled(false);

			btnXmlGeneration.setSclass("xml-generation-button-enabled");

			System.out.println("All cheque accounts are VALID." + " APPROVE BATCH button ENABLED.");

		} else {

			btnXmlGeneration.setDisabled(true);

			btnXmlGeneration.setSclass("xml-generation-button");

			System.out.println("Not all cheque accounts are VALID." + " APPROVE BATCH button DISABLED.");
		}
	}

	private boolean areAllAccountsValidWithoutBatchCheck() {

		if (cheques == null || cheques.isEmpty()) {
			return false;
		}

		for (OutwardCheque cheque : cheques) {

			if (cheque == null) {
				return false;
			}

			String chequeId = nullSafe(cheque.getOutwardChequeId());

			String status = nullSafe(cheque.getChequeStatus());

			String result = accountValidationResults.get(chequeId);

			if ("VERIFIED_BY_CHECKER".equalsIgnoreCase(status)) {
				continue;
			}

			if (!"VALID".equalsIgnoreCase(nullSafe(result))) {

				return false;
			}
		}

		return true;
	}

	public void onClick$btnXmlGeneration(Event event) {

		try {

			if (!areAllAccountsValid()) {

				Messagebox.show("All cheques in this batch must have VALID accounts before approving the batch.",
						"Batch Not Ready", Messagebox.OK, Messagebox.EXCLAMATION);

				return;
			}

			if (batchId == null || batchId.trim().isEmpty()) {

				Messagebox.show("Batch ID is missing.", "Error", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			Messagebox.show("Are you sure you want to approve this batch?", "Approve Batch",
					new Messagebox.Button[] { Messagebox.Button.YES, Messagebox.Button.NO }, Messagebox.QUESTION,
					new org.zkoss.zk.ui.event.EventListener<Messagebox.ClickEvent>() {

						@Override
						public void onEvent(Messagebox.ClickEvent event) throws Exception {

							if (Messagebox.Button.YES.equals(event.getButton())) {

								updateBatchStatusToVerified();
							}
						}
					});

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to approve batch.\n\n" + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
		}
	}

	private void loadBatchStatus() {

		try {

			if (batchId == null || batchId.trim().isEmpty()) {
				batchStatus = null;
				return;
			}

			batchStatus = outwardCheckerQueueService.getBatchStatus(batchId);

			System.out.println("Loaded batch status: " + batchStatus);

		} catch (Exception e) {

			e.printStackTrace();
			batchStatus = null;
		}
	}

	/*
	 * ============================================================ IMAGE SIDE
	 * ============================================================
	 */

	public void onClick$btnImageSide(Event event) {

		if (imgCheque == null) {

			return;
		}

		try {

			if (!showingBackImage) {

				/*
				 * FRONT -> BACK
				 */
				if (backImagePath == null || backImagePath.isEmpty()) {

					Messagebox.show("Back side image is not available.", "Cheque Image", Messagebox.OK,
							Messagebox.EXCLAMATION);

					return;
				}

				imgCheque.setSrc(backImagePath);

				showingBackImage = true;

				if (btnImageSide != null) {

					btnImageSide.setLabel("FRONT SIDE");
				}

			} else {

				/*
				 * BACK -> FRONT
				 */
				if (frontImagePath == null || frontImagePath.isEmpty()) {

					Messagebox.show("Front side image is not available.", "Cheque Image", Messagebox.OK,
							Messagebox.EXCLAMATION);

					return;
				}

				imgCheque.setSrc(frontImagePath);

				showingBackImage = false;

				if (btnImageSide != null) {

					btnImageSide.setLabel("BACK SIDE");
				}
			}

			applyZoom();

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	/*
	 * ============================================================ IMAGE PATH
	 * ============================================================
	 */

	private String convertImagePath(String path) {

		if (path == null || path.trim().isEmpty()) {

			return null;
		}

		path = path.trim();

		// Convert Windows path separators to URL path separators.
		path = path.replace("\\", "/");

		// Keep external image URLs unchanged.
		if (path.startsWith("http://") || path.startsWith("https://")) {

			return path;
		}

		// ZK image src should start from the web application root.
		if (!path.startsWith("/")) {

			path = "/" + path;
		}

		System.out.println("Converted cheque image path = " + path);

		return path;
	}

	/*
	 * ============================================================ NAVIGATION
	 * ============================================================
	 */

	private void updateNavigation() {

		if (cheques == null || cheques.isEmpty()) {
			return;
		}

		// Total number of cheques in the batch
		int total = cheques.size();

		// Current cheque position
		int current = currentIndex + 1;

		// Remaining cheques
		int remaining = total - current;

		/*
		 * ============================================================ CHEQUE COUNT
		 * Example: 4 ============================================================
		 */
		if (lblChequeCount != null) {

			lblChequeCount.setValue(String.valueOf(total));
		}

		
		if (lblChequeNo != null && currentIndex >= 0 && currentIndex < cheques.size()) {

			OutwardCheque cheque = cheques.get(currentIndex);

			if (cheque != null) {

				lblChequeNo.setValue(nullSafe(cheque.getChequeNumber()));
			}
		}

		
		if (lblCurrentCheque != null) {

			lblCurrentCheque.setValue(String.valueOf(current));
		}

		
		if (lblRemaining != null) {

			lblRemaining.setValue(String.valueOf(remaining));
		}

		
		if (lblCurrentChequeNavigation != null) {

			lblCurrentChequeNavigation.setValue(current + " / " + total);
		}

		
		if (btnPrevious != null) {

			btnPrevious.setDisabled(currentIndex <= 0);
		}

		
		if (btnNext != null) {

			btnNext.setDisabled(currentIndex >= total - 1);
		}
	}
	
	public void onClick$btnPrevious(Event event) {

		if (cheques == null || cheques.isEmpty()) {
			return;
		}

		if (currentIndex > 0) {

			currentIndex--;

			displayCheque();

			// Update pagination and progress bar
			updatePaginationProgress();
		}
	}

	
	public void onClick$btnNext(Event event) {

		if (cheques == null || cheques.isEmpty()) {
			return;
		}

		if (currentIndex < cheques.size() - 1) {

			currentIndex++;

			displayCheque();

			// Update pagination and progress bar
			updatePaginationProgress();
		}
	}

	

	public void onClick$btnReturn(Event event) {

		if (cheques == null || cheques.isEmpty() || currentIndex < 0 || currentIndex >= cheques.size()) {

			return;
		}

		try {

			OutwardCheque cheque = cheques.get(currentIndex);

			if (cheque == null) {
				return;
			}

			String chequeStatus = nullSafe(cheque.getChequeStatus()).trim();

			System.out.println("====================================");
			System.out.println("RETURN TO MAKER");
			System.out.println("Cheque No : " + cheque.getChequeNumber());
			System.out.println("Status    : " + chequeStatus);
			System.out.println("====================================");

		
			if ("PENDING_MICR_REPAIR".equalsIgnoreCase(chequeStatus)
					|| "PENDING_DATA_ENTRY".equalsIgnoreCase(chequeStatus)) {

				Messagebox.show("Already rejected.", "Return to Maker", Messagebox.OK, Messagebox.EXCLAMATION);

				return;
			}

		
			createReturnMakerWindow();

			if (returnMakerWindow == null) {
				return;
			}

			
			if (lblReturnBatch != null) {

				lblReturnBatch.setValue(nullSafe(batchNo));
			}

		
			if (lblReturnCheque != null) {

				lblReturnCheque.setValue(nullSafe(cheque.getChequeNumber()));
			}

			
			if (cmbSendBackReason != null) {

				cmbSendBackReason.getItems().clear();

				cmbSendBackReason.setValue("");

				loadSendBackReasons();
			}

		
			if (txtReturnRemarks != null) {

				txtReturnRemarks.setValue("");
			}

			
			returnMakerWindow.setVisible(true);

			returnMakerWindow.doModal();

		} catch (Exception e) {

			e.printStackTrace();

			showError("Unable to open Return to Maker.", e);
		}
	}

	/*
	 * ============================================================ SEND BACK
	 * REASONS ============================================================
	 */

	private void loadSendBackReasons() {

		try {

			List<SendBackReason> reasons = outwardCheckerQueueService.getSendBackReasons();

			if (reasons == null || cmbSendBackReason == null) {

				return;
			}

			for (SendBackReason reason : reasons) {

				if (reason == null) {

					continue;
				}

				Comboitem item = new Comboitem();

				item.setLabel(nullSafe(reason.getReasonName()));

				item.setValue(String.valueOf(reason.getReasonId()));

				cmbSendBackReason.appendChild(item);
			}

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to load send back reasons.\n\n" + e.getMessage(), "Return to Maker", Messagebox.OK,
					Messagebox.ERROR);
		}
	}

	/*
	 * ============================================================ CONFIRM RETURN
	 * TO MAKER ============================================================
	 */

	private void confirmReturnToMaker() {

		if (cheques == null || cheques.isEmpty() || currentIndex < 0 || currentIndex >= cheques.size()) {
			return;
		}

		try {

			// ============================================================
			// CHECK SEND BACK REASON
			// ============================================================

			if (cmbSendBackReason == null || cmbSendBackReason.getSelectedItem() == null) {

				Messagebox.show("Please select a send back reason.", "Return to Maker", Messagebox.OK,
						Messagebox.EXCLAMATION);

				return;
			}

			// ============================================================
			// CURRENT CHEQUE
			// ============================================================

			OutwardCheque cheque = cheques.get(currentIndex);

			Comboitem selectedReason = cmbSendBackReason.getSelectedItem();

			// ============================================================
			// GET REASON ID
			// ============================================================

			String reasonId = selectedReason.getValue().toString().trim();

			// Reason name is stored as Comboitem label
			String reasonName = selectedReason.getLabel();

			// Remarks
			// Remarks
			String remarks = "";

			if (txtReturnRemarks != null) {

				remarks = txtReturnRemarks.getValue();

				if (remarks == null) {
					remarks = "";
				}

				remarks = remarks.trim();
			}

			// ============================================================
			// CHECK REMARKS - MANDATORY
			// ============================================================

			if (remarks.isEmpty()) {

				Messagebox.show("Please enter remarks before returning the cheque to Maker.", "Return to Maker",
						Messagebox.OK, Messagebox.EXCLAMATION);

				if (txtReturnRemarks != null) {
					txtReturnRemarks.focus();
				}

				return;
			}

			// ============================================================
			// FINAL REMARKS FOR CONFIRMATION
			// ============================================================

			final String finalRemarks = remarks;

			// ============================================================
			// CONFIRM RETURN
			// ============================================================

			Messagebox.show("Return cheque " + cheque.getChequeNumber() + " to Maker?", "Confirm Return",
					Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, confirmEvent -> {

						if (Messagebox.ON_YES.equals(confirmEvent.getName())) {

							try {

								// ====================================================
								// DETERMINE CHEQUE STATUS FROM REASON ID
								// ====================================================

								String chequeStatus;

								if ("11".equals(reasonId) || "12".equals(reasonId)) {

									chequeStatus = "PENDING_MICR_REPAIR";

								} else {

									chequeStatus = "PENDING_DATA_ENTRY";
								}

								// ====================================================
								// UPDATE CHEQUE STATUS
								// ====================================================

								outwardCheckerQueueService.returnChequeToMaker(cheque.getChequeNumber(), reasonId,
										finalRemarks);

								outwardCheckerQueueService.updateBatchStatus(batchId, "ON_HOLD");
								
								
								// Send notification to Maker
								sendReturnToMakerNotificationAsync(cheque, reasonId);

								// ====================================================
								// UPDATE CONTROLLER MEMORY
								// ====================================================

								cheque.setChequeStatus(chequeStatus);

								batchStatus = "ON_HOLD";

								// ====================================================
								// REMOVE PREVIOUS ACCOUNT VALIDATION
								// ====================================================

								accountValidationResults.remove(nullSafe(cheque.getOutwardChequeId()));

								// ====================================================
								// RESET ACCOUNT VALIDATION LABEL
								// ====================================================

								setAccountValidationLabel("NOT VERIFIED");

								// ====================================================
								// UPDATE BATCH STATUS LABEL
								// ====================================================

								if (lblQueueStatus != null) {

									lblQueueStatus.setValue("ON_HOLD");
								}

								// ====================================================
								// XML BUTTON MUST REMAIN DISABLED
								// ====================================================

								updateXmlGenerationButton();

								// ====================================================
								// LOG
								// ====================================================

								System.out.println("Cheque returned to Maker");

								System.out.println("Cheque Number = " + cheque.getChequeNumber());

								System.out.println("Reason ID = " + reasonId);

								System.out.println("Reason Name = " + reasonName);

								System.out.println("Remarks = " + finalRemarks);

								System.out.println("Cheque Status = " + chequeStatus);

								System.out.println("Batch Status = ON_HOLD");

								// ====================================================
								// CLOSE RETURN WINDOW
								// ============================================================

								if (returnMakerWindow != null) {

									returnMakerWindow.setVisible(false);
									returnMakerWindow.detach();
									returnMakerWindow = null;
								}

								// ============================================================
								// REMOVE RETURNED CHEQUE FROM CURRENT QUEUE
								// ============================================================

								cheques.remove(currentIndex);

								// ============================================================
								// CHECK IF ANY CHEQUE IS REMAINING
								// ============================================================

								if (cheques.isEmpty()) {

									// --------------------------------------------------------
									// NO CHEQUE REMAINING
									// --------------------------------------------------------

									currentIndex = 0;

									if (lblCurrentCheque != null) {
										lblCurrentCheque.setValue("0");
									}

									if (lblRemaining != null) {
										lblRemaining.setValue("0");
									}

									if (lblCurrentChequeNavigation != null) {
										lblCurrentChequeNavigation.setValue("Cheque 0 of 0 · 0 remaining");
									}

									updateXmlGenerationButton();

									// --------------------------------------------------------
									// REFRESH BATCH STATUS
									// --------------------------------------------------------

									loadChequeStatus();

									// --------------------------------------------------------
									// GO TO CHECKER DASHBOARD
									// --------------------------------------------------------

									Executions.sendRedirect("/outward/checker/dashboard.zul");

									return;
								}

							
								if (currentIndex >= cheques.size()) {

									currentIndex = cheques.size() - 1;
								}

								// ============================================================
								// DISPLAY NEXT CHEQUE
								// ============================================================

								displayCheque();

								// ============================================================
								// REFRESH BATCH STATUS
								// ============================================================

								loadChequeStatus();

								// ============================================================
								// UPDATE XML BUTTON
								// ============================================================

								updateXmlGenerationButton();
								Messagebox.show(
										"Cheque returned to Maker.\n\n" + "Cheque Status: " + chequeStatus + "\n"
												+ "Batch Status: ON_HOLD",
										"Return to Maker", Messagebox.OK, Messagebox.INFORMATION);

							} catch (Exception e) {

								e.printStackTrace();

								showError("Unable to return cheque to Maker.", e);
							}
						}
					});

		} catch (Exception e) {

			e.printStackTrace();

			showError("Unable to return cheque to Maker.", e);
		}
	}
	
	private void sendReturnToMakerNotificationAsync(
	        OutwardCheque cheque,
	        String reasonId) {

	    Thread notificationThread = new Thread(() -> {

	        String returnType;

	        if ("11".equals(reasonId)
	                || "12".equals(reasonId)) {

	            returnType = "MICR Repair";

	        } else {

	            returnType = "Data Entry";
	        }

	        String message =
	                "Cheque "
	                + cheque.getChequeNumber()
	                + " has been returned to Maker for "
	                + returnType
	                + ".";

	        String sql =
	                "INSERT INTO public.notifications "
	              + "(recipient_role, recipient_user_id, message, "
	              + "is_read, created_at) "
	              + "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";

	        try (Connection conn = DBConnection.getConnection();
	             PreparedStatement ps = conn.prepareStatement(sql)) {

	            ps.setString(1, "OUTWARD_MAKER");
	            ps.setString(2, null);
	            ps.setString(3, message);
	            ps.setBoolean(4, false);

	            ps.executeUpdate();

	        } catch (Exception e) {

	            e.printStackTrace();
	        }
	    });

	    notificationThread.setName(
	            "ReturnToMakerNotification-"
	            + cheque.getChequeNumber());

	    notificationThread.start();
	}
	// ============================================================
	// REJECT BUTTON
	// ============================================================
	public void onClick$btnReject(Event event) {

		System.out.println("====================================");
		System.out.println("REJECT BUTTON CLICKED");
		System.out.println("====================================");

		try {

			// ---------------------------------------------------------
			// CHECK CHEQUE LIST
			// ---------------------------------------------------------

			if (cheques == null || cheques.isEmpty()) {

				showError("No cheque is selected.", new IllegalStateException("Cheque list is empty."));

				return;
			}

			// ---------------------------------------------------------
			// CHECK CURRENT INDEX
			// ---------------------------------------------------------

			if (currentIndex < 0 || currentIndex >= cheques.size()) {

				showError("No cheque is selected.", new IllegalStateException("Invalid current cheque index."));

				return;
			}

			// ---------------------------------------------------------
			// GET CURRENT CHEQUE
			// ---------------------------------------------------------

			OutwardCheque cheque = cheques.get(currentIndex);

			if (cheque == null) {

				showError("No cheque is selected.", new IllegalStateException("Current cheque is null."));

				return;
			}

			System.out.println("Selected Cheque = " + cheque.getChequeNumber());

			System.out.println("Batch ID = " + batchId);

			// ---------------------------------------------------------
			// CREATE POPUP IF REQUIRED
			// ---------------------------------------------------------

			if (rejectWindow == null || rejectWindow.getPage() == null) {

				System.out.println("Reject window does not exist. Creating...");

				createRejectWindow();
			}

			// ---------------------------------------------------------
			// FINAL CHECK
			// ---------------------------------------------------------

			if (rejectWindow == null) {

				System.out.println("ERROR: rejectWindow is NULL.");

				showError("Unable to open Reject Cheque popup.",
						new IllegalStateException("Reject window was not created."));

				return;
			}

			if (rejectWindow.getPage() == null) {

				System.out.println("ERROR: rejectWindow has no Page.");

				showError("Unable to open Reject Cheque popup.",
						new IllegalStateException("Reject window is not attached to the current Page."));

				return;
			}

			// ---------------------------------------------------------
			// SET POPUP DATA
			// ---------------------------------------------------------

			lblRejectBatch.setValue(nullSafe(batchId));

			lblRejectCheque.setValue(nullSafe(cheque.getChequeNumber()));

			// ---------------------------------------------------------
			// CLEAR OLD VALUES
			// ---------------------------------------------------------

			cmbRejectReason.setSelectedItem(null);
			cmbRejectReason.setValue("");

			txtRejectRemarks.setValue("");

			// ---------------------------------------------------------
			// SHOW POPUP
			// ---------------------------------------------------------

			rejectWindow.setVisible(true);

			rejectWindow.doModal();

			System.out.println("Reject Cheque window opened successfully.");

		} catch (Exception e) {

			e.printStackTrace();

			showError("Unable to open Reject Cheque popup.", e);
		}
	}

	
	private void createRejectWindow() {

		System.out.println("====================================");
		System.out.println("CREATE REJECT CHEQUE WINDOW");
		System.out.println("====================================");

		try {

			// ---------------------------------------------------------
			// CHECK CURRENT PAGE
			// ---------------------------------------------------------

			if (self == null) {
				throw new IllegalStateException("Checker Queue root component 'self' is NULL.");
			}

			if (self.getPage() == null) {
				throw new IllegalStateException("Checker Queue root component is not attached to a Page.");
			}

			System.out.println("Current Page found successfully.");

			// ---------------------------------------------------------
			// IF WINDOW ALREADY EXISTS
			// ---------------------------------------------------------

			if (rejectWindow != null) {

				System.out.println("Existing rejectWindow found. Page = " + rejectWindow.getPage());

				if (rejectWindow.getPage() == null) {

					rejectWindow.setPage(self.getPage());

				}

				return;
			}

			// ---------------------------------------------------------
			// CREATE ZUL COMPONENT
			// ---------------------------------------------------------

			Component component = Executions.createComponents("/outward/checker/reject-cheque.zul", null, null);

			System.out.println(
					"Created component type = " + (component == null ? "NULL" : component.getClass().getName()));

			// ---------------------------------------------------------
			// CHECK COMPONENT
			// ---------------------------------------------------------

			if (component == null) {

				throw new IllegalStateException("reject-cheque.zul returned NULL component.");

			}

			if (!(component instanceof Window)) {

				throw new IllegalStateException(
						"reject-cheque.zul root must be Window. " + "Actual type = " + component.getClass().getName());

			}

			// ---------------------------------------------------------
			// ASSIGN WINDOW
			// ---------------------------------------------------------

			rejectWindow = (Window) component;

			System.out.println("Reject Window object created successfully.");

			// ---------------------------------------------------------
			// ATTACH WINDOW TO CURRENT PAGE
			// ---------------------------------------------------------

			rejectWindow.setPage(self.getPage());

			System.out.println("Reject Window attached to current Page.");

			System.out.println("Reject Window Page = " + rejectWindow.getPage());

			// ---------------------------------------------------------
			// GET COMPONENTS
			// ---------------------------------------------------------

			lblRejectBatch = (Label) rejectWindow.getFellowIfAny("lblRejectBatch");

			lblRejectCheque = (Label) rejectWindow.getFellowIfAny("lblRejectCheque");

			cmbRejectReason = (Combobox) rejectWindow.getFellowIfAny("cmbRejectReason");

			txtRejectRemarks = (Textbox) rejectWindow.getFellowIfAny("txtRejectRemarks");

			btnRejectConfirm = (Button) rejectWindow.getFellowIfAny("btnRejectConfirm");

			btnRejectCancel = (Button) rejectWindow.getFellowIfAny("btnRejectCancel");

			Button btnRejectClose = (Button) rejectWindow.getFellowIfAny("btnRejectClose");

			// ---------------------------------------------------------
			// PRINT COMPONENT STATUS
			// ---------------------------------------------------------

			System.out.println("lblRejectBatch    = " + (lblRejectBatch != null));

			System.out.println("lblRejectCheque   = " + (lblRejectCheque != null));

			System.out.println("cmbRejectReason   = " + (cmbRejectReason != null));

			System.out.println("txtRejectRemarks  = " + (txtRejectRemarks != null));

			System.out.println("btnRejectConfirm  = " + (btnRejectConfirm != null));

			System.out.println("btnRejectCancel   = " + (btnRejectCancel != null));

			System.out.println("btnRejectClose    = " + (btnRejectClose != null));

			
			if (lblRejectBatch == null) {
				throw new IllegalStateException("lblRejectBatch not found in reject-cheque.zul.");
			}

			if (lblRejectCheque == null) {
				throw new IllegalStateException("lblRejectCheque not found in reject-cheque.zul.");
			}

			if (cmbRejectReason == null) {
				throw new IllegalStateException("cmbRejectReason not found in reject-cheque.zul.");
			}

			if (txtRejectRemarks == null) {
				throw new IllegalStateException("txtRejectRemarks not found in reject-cheque.zul.");
			}

			if (btnRejectConfirm == null) {
				throw new IllegalStateException("btnRejectConfirm not found in reject-cheque.zul.");
			}

			if (btnRejectCancel == null) {
				throw new IllegalStateException("btnRejectCancel not found in reject-cheque.zul.");
			}

			btnRejectConfirm.addEventListener("onClick", event -> confirmRejectCheque());

			btnRejectCancel.addEventListener("onClick", event -> closeRejectWindow());

			if (btnRejectClose != null) {

				btnRejectClose.addEventListener("onClick", event -> closeRejectWindow());

			}

			
			try {

				loadRejectedReasons();

				System.out.println("Rejected reasons loaded successfully.");

			} catch (Exception reasonException) {

				System.out.println("WARNING: Could not load rejected reasons.");

				reasonException.printStackTrace();

			}

		
			rejectWindow.setVisible(false);

			System.out.println("Reject window created successfully.");

		} catch (Exception e) {

			System.out.println("ERROR WHILE CREATING REJECT WINDOW");

			e.printStackTrace();

		
			if (rejectWindow != null) {

				try {

					if (rejectWindow.getPage() != null) {
						rejectWindow.detach();
					}

				} catch (Exception detachException) {

					detachException.printStackTrace();

				}

			}

			rejectWindow = null;

			lblRejectBatch = null;
			lblRejectCheque = null;
			cmbRejectReason = null;
			txtRejectRemarks = null;
			btnRejectConfirm = null;
			btnRejectCancel = null;

			System.out.println("Reject window creation FAILED.");
		}
	}

	private void confirmRejectCheque() {

		if (cheques == null || cheques.isEmpty() || currentIndex < 0 || currentIndex >= cheques.size()) {

			return;
		}

		try {

		
			if (cmbRejectReason == null || cmbRejectReason.getSelectedItem() == null) {

				Messagebox.show("Please select a rejection reason.", "Reject Cheque", Messagebox.OK,
						Messagebox.EXCLAMATION);

				return;
			}

		
			OutwardCheque cheque = cheques.get(currentIndex);

			Comboitem selectedReason = cmbRejectReason.getSelectedItem();

			Object reasonValue = selectedReason.getValue();

			String reasonId = reasonValue == null ? "" : reasonValue.toString().trim();

			String reasonName = selectedReason.getLabel();

		
			String remarks = "";

			if (txtRejectRemarks != null) {

				remarks = txtRejectRemarks.getValue();

				if (remarks == null) {
					remarks = "";
				}

				remarks = remarks.trim();
			}

			
			if (remarks.isEmpty()) {

				Messagebox.show("Please enter remarks before rejecting the cheque.", "Reject Cheque", Messagebox.OK,
						Messagebox.EXCLAMATION);

				if (txtRejectRemarks != null) {
					txtRejectRemarks.focus();
				}

				return;

			}

			final String finalRemarks = remarks;

			Messagebox.show("Reject cheque " + cheque.getChequeNumber() + "?", "Confirm Reject",
					Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, confirmEvent -> {

						if (!Messagebox.ON_YES.equals(confirmEvent.getName())) {

							return;
						}

						try {

						
							OutwardRejectedCheques rejectedCheque = new OutwardRejectedCheques();

							
							rejectedCheque.setOutwardChequeId(cheque.getOutwardChequeId());

						
							Session session = Sessions.getCurrent();

							User loggedInUser = (User) session.getAttribute("USER_OBJ");

							if (loggedInUser == null || loggedInUser.getUserId() == null
									|| loggedInUser.getUserId().trim().isEmpty()) {

								showError("Logged-in user information not found in session.", null);
								return;
							}

							String userId = loggedInUser.getUserId().trim();

							System.out.println("Logged-in Username = " + loggedInUser.getUsername());
							System.out.println("Logged-in User ID = " + userId);

							rejectedCheque.setRejectedBy(userId);
						
							rejectedCheque.setRejectedDate(new Timestamp(System.currentTimeMillis()));

							rejectedCheque.setOutwardBatchId(cheque.getOutwardBatchId());

							
							rejectedCheque.setChequeAmount(cheque.getChequeAmount());

						
							String rejectionRemarks = "Rejection Reason: " + reasonName + " | Remarks: " + finalRemarks;
							// Reason ID / CODE
							rejectedCheque.setRejectedReasonId(reasonId);

							// Reason name
							rejectedCheque.setRejectedReasonName(reasonName);

							// Checker remarks only
							rejectedCheque.setRemarks(finalRemarks);

							
							System.out.println("=================================");

							System.out.println("REJECT CHEQUE");

							System.out.println("Cheque ID = " + rejectedCheque.getOutwardChequeId());

							System.out.println("Cheque Number = " + cheque.getChequeNumber());

							System.out.println("Username = " + rejectedCheque.getRejectedBy());

							System.out.println("Batch ID = " + rejectedCheque.getOutwardBatchId());

							System.out.println("Amount = " + rejectedCheque.getChequeAmount());

							System.out.println("Reason ID   = " + reasonId);
							System.out.println("Reason Name = " + reasonName);
							System.out.println("Remarks     = " + finalRemarks);

							System.out.println("=================================");

							outwardCheckerQueueService.saveRejectedCheque(rejectedCheque);

							outwardCheckerQueueService.updateChequeStatus(cheque.getChequeNumber(), "REJECTED");

							cheque.setChequeStatus("REJECTED");

							accountValidationResults.remove(nullSafe(cheque.getOutwardChequeId()));

							closeRejectWindow();

							cheques.remove(currentIndex);

							if (cheques.isEmpty()) {

								currentIndex = 0;

								if (lblCurrentCheque != null) {
									lblCurrentCheque.setValue("0");
								}

								if (lblRemaining != null) {
									lblRemaining.setValue("0");
								}

								if (lblCurrentChequeNavigation != null) {

									lblCurrentChequeNavigation.setValue("Cheque 0 of 0 · 0 remaining");
								}

								updateXmlGenerationButton();

							} else {

								if (currentIndex >= cheques.size()) {

									currentIndex = cheques.size() - 1;
								}

								displayCheque();
							}

							Messagebox.show(
									"Cheque rejected successfully.\n\n" + "Cheque Number: " + cheque.getChequeNumber()
											+ "\n" + "Reason: " + reasonName,
									"Reject Cheque", Messagebox.OK, Messagebox.INFORMATION);

						} catch (Exception e) {

							e.printStackTrace();

							showError("Unable to reject cheque.", e);
						}
					});

		} catch (Exception e) {

			e.printStackTrace();

			showError("Unable to reject cheque.", e);
		}
	}

	private String getCurrentUsername() {

		Object username = Sessions.getCurrent().getAttribute("CTS_USERNAME");

		if (username == null) {
			return "UNKNOWN";
		}

		return username.toString();
	}

	private void closeRejectWindow() {

		if (rejectWindow != null) {

			rejectWindow.setVisible(false);

		}

	}

	public void onClick$btnZoomIn(Event event) {

		if (zoomLevel < 3.0) {

			zoomLevel += 0.25;

			applyZoom();
		}
	}

	public void onClick$btnZoomOut(Event event) {

		if (zoomLevel > 0.5) {

			zoomLevel -= 0.25;

			applyZoom();
		}
	}

	public void onClick$btnZoomReset(Event event) {

		zoomLevel = 1.0;

		applyZoom();
	}

	private void applyZoom() {

		if (imgCheque != null) {

			imgCheque.setStyle("transform:scale(" + zoomLevel + ");" + "transform-origin:" + "center center;");
		}

		if (lblZoom != null) {

			lblZoom.setValue(String.format("%.0f%%", zoomLevel * 100));
		}
	}

	public void onClick$btnBackToDashboard(Event event) {

		Executions.sendRedirect("/outward/checker/dashboard.zul");
	}

	private void showError(String message, Exception e) {

		String errorMessage = message;

		if (e != null && e.getMessage() != null && !e.getMessage().trim().isEmpty()) {

			errorMessage += "\n\n" + e.getMessage();
		}

		Messagebox.show(errorMessage, "Error", Messagebox.OK, Messagebox.ERROR);
	}

	private String nullSafe(String value) {

		return value == null ? "" : value;
	}
}
