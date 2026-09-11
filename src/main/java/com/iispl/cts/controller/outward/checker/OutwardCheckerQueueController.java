package com.iispl.cts.controller.outward.checker;

import java.net.URLEncoder;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
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
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.OutwardChequeImage;
import com.iispl.cts.entity.outward.OutwardRejectedCheques;
import com.iispl.cts.entity.outward.RejectedReason;
import com.iispl.cts.entity.outward.SendBackReason;
import com.iispl.cts.service.outward.OutwardCheckerQueueService;
import com.iispl.cts.serviceimpl.outward.OutwardCheckerQueueServiceImpl;

/**
 * ============================================================ OUTWARD CHECKER
 * QUEUE CONTROLLER ============================================================
 *
 * Flow:
 *
 * 1. Load selected batch. 2. Display cheque. 3. Account initially shows NOT
 * VERIFIED. 4. Checker clicks VERIFY. 5. Payee account number is checked in DB.
 * 6. If account exists: Account = VALID 7. If account does not exist: Account =
 * INVALID 8. After every cheque is VALID: Batch status = VERIFIED XML
 * Generation button = ENABLED
 *
 * Account validation is NOT performed automatically while loading/displaying a
 * cheque.
 *
 * ============================================================
 */
public class OutwardCheckerQueueController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;

	/*
	 * ============================================================ ROOT COMPONENT
	 * ============================================================
	 */

	private Component self;

	/*
	 * ============================================================ LABELS
	 * ============================================================
	 */

	private Label lblBatchNo;
	private Label lblChequeNo;
	private Label lblQueueStatus;
	private Label lblCurrentCheque;
	private Label lblRemaining;
	private Label lblZoom;
	private Label lblAccountValidation;
	private Label lblCurrentChequeNavigation;

	/*
	 * ============================================================ CHEQUE IMAGE
	 * ============================================================
	 */

	private Image imgCheque;

	private Button btnImageSide;

	private boolean showingBackImage = false;

	private String frontImagePath;
	private String backImagePath;

	/*
	 * ============================================================ CHEQUE FIELDS
	 * ============================================================
	 */

	private Textbox txtChequeNo;
	private Textbox txtMicr;
	private Textbox txtAccountNo;
	private Textbox txtAmount;
	private Textbox txtChequeDate;
	private Textbox txtpayeeName;

	/*
	 * ============================================================ NAVIGATION
	 * ============================================================
	 */

	private Button btnPrevious;
	private Button btnNext;

	/*
	 * ============================================================ XML GENERATION
	 * ============================================================
	 */

	private Button btnXmlGeneration;

	/*
	 * ============================================================ ACTION BUTTONS
	 * ============================================================
	 */

	private Button btnVerified;
	private Button btnReturn;
	private Button btnReject;

	/*
	 * ============================================================ ZOOM BUTTONS
	 * ============================================================
	 */

	private Button btnZoomIn;
	private Button btnZoomOut;
	private Button btnZoomReset;

	/*
	 * ============================================================ RETURN TO MAKER
	 * POPUP ============================================================
	 */

	private Window returnMakerWindow;

	private Label lblReturnBatch;
	private Label lblReturnCheque;

	private Combobox cmbSendBackReason;

	private Textbox txtReturnRemarks;

	private Button btnReturnConfirm;
	private Button btnReturnCancel;

	/*
	 * ============================================================ NO BATCH / MAIN
	 * CONTENT ============================================================
	 */

	private Vlayout noBatchMessage;

	private Div checkerQueueContent;

	/*
	 * ============================================================ ACCOUNT
	 * VALIDATION POPUP ============================================================
	 */

	private Window accountValidationWindow;

	private Label lblAccountValidationPopupIcon;

	private Label lblAccountValidationPopupTitle;

	private Label lblAccountValidationPopupMessage;

	private Button btnAccountValidationOk;

	
	
	// ============================================================
	// REJECT CHEQUE POPUP
	// ============================================================

	private Window rejectWindow;

	private Label lblRejectBatch;

	private Label lblRejectCheque;

	private Combobox cmbRejectReason;

	private Textbox txtRejectRemarks;

	private Button btnRejectConfirm;

	private Button btnRejectCancel;
	/*
	 * ============================================================ ACCOUNT
	 * VALIDATION ============================================================
	 */

	/*
	 * true = current account is valid false = current account is invalid
	 */
	private boolean accountValidationPassed = false;

	/*
	 * Current cheque waiting for popup confirmation.
	 */
	private OutwardCheque chequeWaitingForVerification;

	/*
	 * Stores account validation result for every cheque.
	 *
	 * Key: outward cheque ID
	 *
	 * Value: VALID / INVALID
	 */
	private Map<String, String> accountValidationResults = new HashMap<>();

	/*
	 * ============================================================ CHEQUE DATA
	 * ============================================================
	 */

	private List<OutwardCheque> cheques = new ArrayList<>();

	private int currentIndex = 0;

	/*
	 * ============================================================ BATCH
	 * ============================================================
	 */

	private String batchId;

	private String batchNo;

	private String batchStatus;

	/*
	 * ============================================================ SERVICE
	 * ============================================================
	 */

	private OutwardCheckerQueueService outwardCheckerQueueService;

	/*
	 * ============================================================ ZOOM
	 * ============================================================
	 */

	private double zoomLevel = 1.0;

	/*
	 * ============================================================ DO AFTER COMPOSE
	 * ============================================================
	 */

	@Override
	public void doAfterCompose(Component comp) throws Exception {

		super.doAfterCompose(comp);

		/*
		 * Save root component.
		 *
		 * This is used when creating the custom popup.
		 */
		self = comp;

		/*
		 * Initialize service.
		 */
		outwardCheckerQueueService = new OutwardCheckerQueueServiceImpl();

		System.out.println("==========================================");

		System.out.println("OUTWARD CHECKER QUEUE");

		System.out.println("==========================================");

		/*
		 * XML button:
		 *
		 * Visible = YES Disabled = YES
		 *
		 * It will be enabled only after all accounts are VALID and batch status becomes
		 * VERIFIED.
		 */
		if (btnXmlGeneration != null) {

			btnXmlGeneration.setVisible(true);

			btnXmlGeneration.setDisabled(true);
		}

		/*
		 * ======================================================== GET BATCH ID
		 * ========================================================
		 */

		Object sessionBatchId = Sessions.getCurrent().getAttribute("SELECTED_OUTWARD_BATCH_ID");

		if (sessionBatchId != null) {

			batchId = String.valueOf(sessionBatchId);

		} else {

			String requestBatchId = Executions.getCurrent().getParameter("batchId");

			if (requestBatchId != null && !requestBatchId.trim().isEmpty()) {

				batchId = requestBatchId.trim();
			}
		}

		/*
		 * ======================================================== NO BATCH
		 * ========================================================
		 */

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

		/*
		 * Your current page uses batch ID as batch number.
		 */
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

		/*
		 * ======================================================== RETURN POPUP
		 * ========================================================
		 */

		createReturnMakerWindow();

		/*
		 * ======================================================== LOAD CHEQUES
		 * ========================================================
		 */

		loadCheques();

		/*
		 * ======================================================== TARGET CHEQUE
		 * ========================================================
		 */

		String targetChequeNo = Executions.getCurrent().getParameter("chequeNo");

		String targetChequeId = Executions.getCurrent().getParameter("chequeId");

		/*
		 * Session fallback for cheque number.
		 */
		if (targetChequeNo == null || targetChequeNo.trim().isEmpty()) {

			Object selectedChequeNo = Sessions.getCurrent().getAttribute("SELECTED_VERIFY_CHEQUE_NO");

			if (selectedChequeNo != null) {

				targetChequeNo = String.valueOf(selectedChequeNo);

				Sessions.getCurrent().removeAttribute("SELECTED_VERIFY_CHEQUE_NO");
			}
		}

		/*
		 * Session fallback for cheque ID.
		 */
		if (targetChequeId == null || targetChequeId.trim().isEmpty()) {

			Object selectedChequeId = Sessions.getCurrent().getAttribute("SELECTED_VERIFY_CHEQUE_ID");

			if (selectedChequeId != null) {

				targetChequeId = String.valueOf(selectedChequeId);

				Sessions.getCurrent().removeAttribute("SELECTED_VERIFY_CHEQUE_ID");
			}
		}

		/*
		 * Select specific cheque if supplied.
		 */
		selectSpecificCheque(targetChequeNo, targetChequeId);

		/*
		 * Update XML button.
		 */
		updateXmlGenerationButton();
		createReturnMakerWindow();
		createRejectWindow();
	}
	
	// ============================================================
	// LOAD REJECTED REASONS
	// ============================================================

	private void loadRejectedReasons() {

	    try {

	        List<RejectedReason> reasons =
	                outwardCheckerQueueService
	                        .getRejectedReasons();

	        cmbRejectReason.getItems().clear();

	        for (RejectedReason reason : reasons) {

	            Comboitem item =
	                    new Comboitem();

	            item.setLabel(
	                    nullSafe(
	                            reason.getRejectedReasonName()
	                    )
	            );

	            /*
	             * Store rejected reason ID.
	             */
	            item.setValue(
	                    reason.getRejectedReasonId()
	            );

	            cmbRejectReason.appendChild(item);
	        }

	        System.out.println(
	                "Rejected reasons loaded = "
	                + reasons.size()
	        );

	    } catch (Exception e) {

	        e.printStackTrace();

	        showError(
	                "Unable to load rejection reasons.",
	                e
	        );
	    }
	}

	/*
	 * ============================================================ CREATE RETURN
	 * MAKER WINDOW ============================================================
	 */

	private void createReturnMakerWindow() {

		try {

			if (returnMakerWindow != null) {

				return;
			}

			returnMakerWindow = (Window) Executions.createComponents("/outward/checker/return-to-maker.zul", null,
					null);

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

				btnReturnCancel.addEventListener("onClick", event -> {

					if (returnMakerWindow != null) {

						returnMakerWindow.setVisible(false);
					}
				});
			}

			returnMakerWindow.setVisible(false);

		} catch (Exception e) {

			e.printStackTrace();	
		}
	}

	/*
	 * ============================================================ LOAD CHEQUES
	 * ============================================================
	 */

	private void loadCheques() {

		try {

			System.out.println("Loading cheques for batch = " + batchId);

			cheques = outwardCheckerQueueService.getChequesByBatchId(batchId);

			if (cheques == null) {

				cheques = new ArrayList<>();
			}

			/*
			 * Clear validation map when loading batch.
			 *
			 * Initially every cheque is NOT VERIFIED.
			 */
			accountValidationResults.clear();

			System.out.println("Total cheques = " + cheques.size());

			/*
			 * Load actual batch status from DB.
			 */
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
			 * IMPORTANT:
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

	/*
	 * ============================================================ LOAD BATCH
	 * STATUS ============================================================
	 */

	private void loadBatchStatus() {

		try {

			if (batchId == null || batchId.trim().isEmpty()) {

				return;
			}

			batchStatus = outwardCheckerQueueService.getBatchStatus(batchId);

			if (batchStatus == null || batchStatus.trim().isEmpty()) {

				batchStatus = "-";

			} else {

				batchStatus = batchStatus.trim().toUpperCase();
			}

			System.out.println("Batch ID = " + batchId);

			System.out.println("Batch Status = " + batchStatus);

			if (lblQueueStatus != null) {

				lblQueueStatus.setValue(batchStatus);
			}

		} catch (Exception e) {

			e.printStackTrace();

			batchStatus = "-";

			if (lblQueueStatus != null) {

				lblQueueStatus.setValue("-");
			}
		}
	}

	/*
	 * ============================================================ SELECT SPECIFIC
	 * CHEQUE ============================================================
	 */

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

	/*
	 * ============================================================ DISPLAY CHEQUE
	 * ============================================================
	 */

	private void displayCheque() {

		if (cheques == null || cheques.isEmpty() || currentIndex < 0 || currentIndex >= cheques.size()) {

			return;
		}

		try {

			OutwardCheque cheque = cheques.get(currentIndex);

			if (cheque == null) {

				return;
			}

			System.out.println("Displaying cheque = " + cheque.getChequeNumber());

			/*
			 * ==================================================== CHEQUE NUMBER
			 * ====================================================
			 */

			if (lblChequeNo != null) {

				lblChequeNo.setValue(nullSafe(cheque.getChequeNumber()));
			}

			if (txtChequeNo != null) {

				txtChequeNo.setValue(nullSafe(cheque.getChequeNumber()));
			}

			/*
			 * ==================================================== MICR
			 * ====================================================
			 */

			if (txtMicr != null) {

				txtMicr.setValue(nullSafe(cheque.getMicrCode()));
			}

			/*
			 * ==================================================== ACCOUNT NUMBER
			 * ====================================================
			 */

			if (txtAccountNo != null) {

				txtAccountNo.setValue(nullSafe(cheque.getPayeeAccountNumber()));
			}

			/*
			 * ==================================================== AMOUNT
			 * ====================================================
			 */

			if (txtAmount != null) {

				if (cheque.getChequeAmount() != null) {

					txtAmount.setValue(String.valueOf(cheque.getChequeAmount()));

				} else {

					txtAmount.setValue("");
				}
			}

			/*
			 * ==================================================== CHEQUE DATE
			 * ====================================================
			 */

			if (txtChequeDate != null) {

				if (cheque.getChequeDate() != null) {

					txtChequeDate.setValue(new SimpleDateFormat("dd-MM-yyyy").format(cheque.getChequeDate()));

				} else {

					txtChequeDate.setValue("");
				}
			}

			/*
			 * ==================================================== PAYEE NAME
			 * ====================================================
			 */

			if (txtpayeeName != null) {

				txtpayeeName.setValue(nullSafe(cheque.getPayeeName()));
			}

			/*
			 * ==================================================== ACCOUNT VALIDATION LABEL
			 * ====================================================
			 */

			String chequeId = nullSafe(cheque.getOutwardChequeId());

			String validationResult = accountValidationResults.get(chequeId);

			/*
			 * IMPORTANT:
			 *
			 * We DO NOT call isPayeeAccountExists() here.
			 *
			 * Account is checked only when VERIFY button is clicked.
			 */
			if (validationResult == null) {

				setAccountValidationLabel("NOT VERIFIED");

			} else if ("VALID".equalsIgnoreCase(validationResult)) {

				setAccountValidationLabel("VALID");

			} else {

				setAccountValidationLabel("INVALID");
			}

			/*
			 * ==================================================== LOAD CHEQUE IMAGES
			 * ====================================================
			 */

			frontImagePath = convertImagePath(cheque.getChequeImageFront());

			backImagePath = convertImagePath(cheque.getChequeImageBack());

			/*
			 * ==================================================== SHOW FRONT IMAGE
			 * ====================================================
			 */

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

			/*
			 * ==================================================== NAVIGATION
			 * ====================================================
			 */

			updateNavigation();

			/*
			 * ==================================================== XML BUTTON
			 * ====================================================
			 */

			updateXmlGenerationButton();

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to display cheque.\n\n" + e.getMessage(), "Checker Queue", Messagebox.OK,
					Messagebox.ERROR);
		}
	}

	/*
	 * ============================================================ SET ACCOUNT
	 * VALIDATION LABEL ============================================================
	 */

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

	/*
	 * ============================================================ VERIFY BUTTON
	 * ============================================================
	 */

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

			/*
			 * Save current cheque.
			 */
			chequeWaitingForVerification = cheque;

			accountValidationPassed = false;

			/*
			 * Before checking:
			 *
			 * NOT VERIFIED
			 */
			setAccountValidationLabel("NOT VERIFIED");

			/*
			 * Get payee account number.
			 */
			String payeeAccountNumber = nullSafe(cheque.getPayeeAccountNumber()).trim();

			/*
			 * ==================================================== ACCOUNT NUMBER MISSING
			 * ====================================================
			 */

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

			/*
			 * CHECK ACCOUNT IN DATABASE.
			 */
			boolean accountExists = outwardCheckerQueueService.isPayeeAccountExists(payeeAccountNumber);

			if (accountExists) {

				/*
				 * ================================================= VALID
				 * =================================================
				 */

				accountValidationPassed = true;

				saveAccountValidationResult(cheque, "VALID");

				showAccountValidationPopup(true, "Account number " + payeeAccountNumber + " is valid.");

			} else {

				/*
				 * ================================================= INVALID
				 * =================================================
				 */

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

	/*
	 * ============================================================ SAVE ACCOUNT
	 * VALIDATION RESULT
	 * ============================================================
	 */

	private void saveAccountValidationResult(OutwardCheque cheque, String result) {

		if (cheque == null) {

			return;
		}

		String chequeId = nullSafe(cheque.getOutwardChequeId());

		accountValidationResults.put(chequeId, result);

		/*
		 * Immediately change current label.
		 */
		setAccountValidationLabel(result);

		System.out.println("Cheque " + cheque.getChequeNumber() + " = " + result);

		/*
		 * Update XML state.
		 *
		 * This will remain disabled until ALL cheques are VALID.
		 */
		updateXmlGenerationButton();
	}

	/*
	 * ============================================================ ACCOUNT
	 * VALIDATION POPUP ============================================================
	 */

	private void showAccountValidationPopup(boolean valid, String message) {

		try {

			/*
			 * Remove previous popup if any.
			 */
			if (accountValidationWindow != null) {

				accountValidationWindow.detach();

				accountValidationWindow = null;
			}

			/*
			 * ==================================================== CREATE WINDOW
			 * ====================================================
			 */

			accountValidationWindow = new Window();

			accountValidationWindow.setWidth("430px");

			accountValidationWindow.setHeight("250px");

			accountValidationWindow.setBorder("none");

			accountValidationWindow.setClosable(false);

			accountValidationWindow.setSizable(false);

			accountValidationWindow.setPosition("center");

			accountValidationWindow.setStyle("background:#ffffff;" + "border-radius:12px;" + "box-shadow:"
					+ "0 10px 35px " + "rgba(0,0,0,0.20);");

			/*
			 * ==================================================== CONTAINER
			 * ====================================================
			 */

			Div container = new Div();

			container.setStyle("width:100%;" + "height:100%;" + "box-sizing:border-box;" + "padding:25px 30px;"
					+ "text-align:center;");

			/*
			 * ==================================================== ICON
			 * ====================================================
			 */

			lblAccountValidationPopupIcon = new Label();

			lblAccountValidationPopupIcon.setStyle("display:block;" + "width:52px;" + "height:52px;"
					+ "line-height:52px;" + "margin:0 auto 14px;" + "border-radius:50%;"
					+ (valid ? "background:#dcfce7;" + "color:#16a34a;" : "background:#fee2e2;" + "color:#dc2626;")
					+ "font-size:28px;" + "font-weight:bold;");

			lblAccountValidationPopupIcon.setValue(valid ? "✓" : "!");

			/*
			 * ==================================================== TITLE
			 * ====================================================
			 */

			lblAccountValidationPopupTitle = new Label();

			lblAccountValidationPopupTitle.setValue(valid ? "Account Number Valid" : "Invalid Account Number");

			lblAccountValidationPopupTitle.setStyle("display:block;" + "margin-bottom:10px;" + "font-size:20px;"
					+ "font-weight:700;" + "color:#1f2937;");

			/*
			 * ==================================================== MESSAGE
			 * ====================================================
			 */

			lblAccountValidationPopupMessage = new Label();

			lblAccountValidationPopupMessage.setValue(message);

			lblAccountValidationPopupMessage.setStyle("display:block;" + "width:100%;" + "margin-bottom:22px;"
					+ "font-size:14px;" + "line-height:22px;" + "color:#6b7280;" + "text-align:center;");

			/*
			 * ==================================================== OK BUTTON
			 * ====================================================
			 */

			btnAccountValidationOk = new Button("OK");

			btnAccountValidationOk.setStyle(
					"width:90px;" + "height:36px;" + "border:none;" + "border-radius:6px;" + "background:#2563eb;"
							+ "color:#ffffff;" + "font-size:14px;" + "font-weight:600;" + "cursor:pointer;");

			btnAccountValidationOk.addEventListener("onClick", e -> {

				if (accountValidationWindow != null) {

					accountValidationWindow.detach();

					accountValidationWindow = null;
				}

				/*
				 * If account is VALID, check whether ALL cheques are now VALID.
				 */
				if (accountValidationPassed) {

					checkAllAccountsValid();
				}
			});

			/*
			 * ==================================================== APPEND COMPONENTS
			 * ====================================================
			 */

			container.appendChild(lblAccountValidationPopupIcon);

			container.appendChild(lblAccountValidationPopupTitle);

			container.appendChild(lblAccountValidationPopupMessage);

			container.appendChild(btnAccountValidationOk);

			accountValidationWindow.appendChild(container);

			/*
			 * IMPORTANT:
			 *
			 * self.appendChild() is used instead of an undefined variable.
			 */
			if (self != null) {

				self.appendChild(accountValidationWindow);
			}

			accountValidationWindow.doModal();

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to display account validation popup.", "Error", Messagebox.OK, Messagebox.ERROR);
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

		boolean allValid = true;

		for (OutwardCheque cheque : cheques) {

			if (cheque == null) {

				allValid = false;

				break;
			}

			String chequeId = nullSafe(cheque.getOutwardChequeId());

			String result = accountValidationResults.get(chequeId);

			System.out.println("Cheque " + cheque.getChequeNumber() + " = " + result);

			/*
			 * Every cheque must be VALID.
			 */
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

		/*
		 * Every cheque has VALID account.
		 */
		System.out.println("ALL CHEQUES ARE VALID.");

		/*
		 * Change batch status.
		 */
		updateBatchStatusToVerified();
	}

	/*
	 * ============================================================ UPDATE BATCH
	 * STATUS ============================================================
	 */

	private void updateBatchStatusToVerified() {

		try {

			if (batchId == null || batchId.trim().isEmpty()) {

				return;
			}

			/*
			 * Double-check all accounts.
			 */
			if (!areAllAccountsValid()) {

				System.out.println("Cannot verify batch. " + "Not all accounts are valid.");

				updateXmlGenerationButton();

				return;
			}

			/*
			 * Update database.
			 */
			outwardCheckerQueueService.updateBatchStatus(batchId, "VERIFIED");

			batchStatus = "VERIFIED";

			if (lblQueueStatus != null) {

				lblQueueStatus.setValue("VERIFIED");
			}

			/*
			 * Enable XML.
			 */
			updateXmlGenerationButton();

			System.out.println("==========================================");

			System.out.println("ALL CHEQUE ACCOUNTS ARE VALID");

			System.out.println("BATCH STATUS = VERIFIED");

			System.out.println("XML GENERATION ENABLED");

			System.out.println("==========================================");

			Messagebox.show("All cheques in this batch are valid.\n\n"

					+ "XML Generation is now available.", "Batch Verified", Messagebox.OK, Messagebox.INFORMATION);

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("All cheque accounts are valid, " + "but the batch status could not be updated.\n\n"
					+ e.getMessage(), "Batch Status Error", Messagebox.OK, Messagebox.ERROR);
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

		for (OutwardCheque cheque : cheques) {

			if (cheque == null) {

				return false;
			}

			String chequeId = nullSafe(cheque.getOutwardChequeId());

			String result = accountValidationResults.get(chequeId);

			if (!"VALID".equalsIgnoreCase(nullSafe(result))) {

				return false;
			}
		}

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

		boolean allAccountsValid = areAllAccountsValid();

		boolean batchVerified = "VERIFIED".equalsIgnoreCase(nullSafe(batchStatus));

		boolean enableXml = allAccountsValid && batchVerified;

		/*
		 * XML button remains visible.
		 */
		btnXmlGeneration.setVisible(true);

		/*
		 * Enabled only when both conditions are true.
		 */
		btnXmlGeneration.setDisabled(!enableXml);

		System.out.println("XML enabled = " + enableXml);
	}

	/*
	 * ============================================================ XML GENERATION
	 * ============================================================
	 */

	public void onClick$btnXmlGeneration(Event event) {

		try {

			/*
			 * Check all accounts.
			 */
			if (!areAllAccountsValid()) {

				Messagebox.show("All cheques must have VALID accounts " + "before XML generation.", "XML Generation",
						Messagebox.OK, Messagebox.EXCLAMATION);

				return;
			}

			/*
			 * Get latest batch status from DB.
			 */
			loadBatchStatus();

			if (!"VERIFIED".equalsIgnoreCase(nullSafe(batchStatus))) {

				Messagebox.show("Batch is not VERIFIED yet.", "XML Generation", Messagebox.OK, Messagebox.EXCLAMATION);

				return;
			}

			if (batchId == null || batchId.trim().isEmpty()) {

				Messagebox.show("Batch ID is missing.", "XML Generation", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			String encodedBatchId = URLEncoder.encode(batchId, "UTF-8");

			Executions.sendRedirect("/outward/checker/xml-generation.zul" + "?batchId=" + encodedBatchId);

		} catch (Exception e) {

			e.printStackTrace();

			showError("Unable to open XML Generation.", e);
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

		int total = cheques.size();

		int current = currentIndex + 1;

		int remaining = total - current;

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

	/*
	 * ============================================================ PREVIOUS
	 * ============================================================
	 */

	public void onClick$btnPrevious(Event event) {

		if (cheques == null || cheques.isEmpty()) {

			return;
		}

		if (currentIndex > 0) {

			currentIndex--;

			displayCheque();
		}
	}

	/*
	 * ============================================================ NEXT
	 * ============================================================
	 */

	public void onClick$btnNext(Event event) {

		if (cheques == null || cheques.isEmpty()) {

			return;
		}

		if (currentIndex < cheques.size() - 1) {

			currentIndex++;

			displayCheque();
		}
	}

	/*
	 * ============================================================ RETURN TO MAKER
	 * ============================================================
	 */

	public void onClick$btnReturn(Event event) {

		if (cheques == null || cheques.isEmpty() || currentIndex < 0 || currentIndex >= cheques.size()) {

			return;
		}

		try {

			OutwardCheque cheque = cheques.get(currentIndex);

			if (lblReturnBatch != null) {

				lblReturnBatch.setValue(batchNo);
			}

			if (lblReturnCheque != null) {

				lblReturnCheque.setValue(nullSafe(cheque.getChequeNumber()));
			}

			if (cmbSendBackReason != null) {

				cmbSendBackReason.getItems().clear();

				loadSendBackReasons();
			}

			if (txtReturnRemarks != null) {

				txtReturnRemarks.setValue("");
			}

			if (returnMakerWindow != null) {

				returnMakerWindow.setVisible(true);

				returnMakerWindow.doModal();
			}

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

	    if (cheques == null || cheques.isEmpty()
	            || currentIndex < 0
	            || currentIndex >= cheques.size()) {
	        return;
	    }

	    try {

	        // ============================================================
	        // CHECK SEND BACK REASON
	        // ============================================================

	        if (cmbSendBackReason == null
	                || cmbSendBackReason.getSelectedItem() == null) {

	            Messagebox.show(
	                    "Please select a send back reason.",
	                    "Return to Maker",
	                    Messagebox.OK,
	                    Messagebox.EXCLAMATION
	            );

	            return;
	        }

	        // ============================================================
	        // CURRENT CHEQUE
	        // ============================================================

	        OutwardCheque cheque = cheques.get(currentIndex);

	        Comboitem selectedReason =
	                cmbSendBackReason.getSelectedItem();

	        // ============================================================
	        // GET REASON ID
	        // ============================================================

	        String reasonId = selectedReason.getValue().toString().trim();
	        
	        // Reason name is stored as Comboitem label
	        String reasonName =
	                selectedReason.getLabel();

	        // Remarks
	        String remarks =
	                txtReturnRemarks != null
	                        ? txtReturnRemarks.getValue()
	                        : "";

	        // ============================================================
	        // CONFIRM RETURN
	        // ============================================================

	        Messagebox.show(
	                "Return cheque "
	                        + cheque.getChequeNumber()
	                        + " to Maker?",
	                "Confirm Return",
	                Messagebox.YES | Messagebox.NO,
	                Messagebox.QUESTION,
	                confirmEvent -> {

	                    if (Messagebox.ON_YES.equals(
	                            confirmEvent.getName())) {

	                        try {

	                            // ====================================================
	                            // DETERMINE CHEQUE STATUS FROM REASON ID
	                            // ====================================================

	                            String chequeStatus;

	                            if ("11".equals(reasonId)
	                                    || "12".equals(reasonId)) {

	                                chequeStatus =
	                                        "PENDING_MICR_REPAIR";

	                            } else {

	                                chequeStatus =
	                                        "PENDING_DATA_ENTRY";
	                            }

	                            // ====================================================
	                            // UPDATE CHEQUE STATUS
	                            // ====================================================

	                            outwardCheckerQueueService.updateChequeStatus(
	                                    cheque.getChequeNumber(),
	                                    chequeStatus
	                            );

	                            // ====================================================
	                            // PUT BATCH ON HOLD
	                            // ====================================================

	                            outwardCheckerQueueService.updateBatchStatus(
	                                    batchId,
	                                    "ON_HOLD"
	                            );

	                            // ====================================================
	                            // UPDATE CONTROLLER MEMORY
	                            // ====================================================

	                            cheque.setChequeStatus(chequeStatus);

	                            batchStatus = "ON_HOLD";

	                            // ====================================================
	                            // REMOVE PREVIOUS ACCOUNT VALIDATION
	                            // ====================================================

	                            accountValidationResults.remove(
	                                    nullSafe(
	                                            cheque.getOutwardChequeId()
	                                    )
	                            );

	                            // ====================================================
	                            // RESET ACCOUNT VALIDATION LABEL
	                            // ====================================================

	                            setAccountValidationLabel(
	                                    "NOT VERIFIED"
	                            );

	                            // ====================================================
	                            // UPDATE BATCH STATUS LABEL
	                            // ====================================================

	                            if (lblQueueStatus != null) {

	                                lblQueueStatus.setValue(
	                                        "ON_HOLD"
	                                );
	                            }

	                            // ====================================================
	                            // XML BUTTON MUST REMAIN DISABLED
	                            // ====================================================

	                            updateXmlGenerationButton();

	                            // ====================================================
	                            // LOG
	                            // ====================================================

	                            System.out.println(
	                                    "Cheque returned to Maker"
	                            );

	                            System.out.println(
	                                    "Cheque Number = "
	                                            + cheque.getChequeNumber()
	                            );

	                            System.out.println(
	                                    "Reason ID = "
	                                            + reasonId
	                            );

	                            System.out.println(
	                                    "Reason Name = "
	                                            + reasonName
	                            );

	                            System.out.println(
	                                    "Remarks = "
	                                            + remarks
	                            );

	                            System.out.println(
	                                    "Cheque Status = "
	                                            + chequeStatus
	                            );

	                            System.out.println(
	                                    "Batch Status = ON_HOLD"
	                            );

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
	                                 lblCurrentChequeNavigation.setValue(
	                                         "Cheque 0 of 0 · 0 remaining"
	                                 );
	                             }

	                             updateXmlGenerationButton();

	                             // --------------------------------------------------------
	                             // REFRESH BATCH STATUS
	                             // --------------------------------------------------------

	                             loadBatchStatus();

	                             // --------------------------------------------------------
	                             // GO TO CHECKER DASHBOARD
	                             // --------------------------------------------------------

	                             Executions.sendRedirect(
	                                     "/outward/checker/dashboard.zul"
	                             );

	                             return;
	                         }

	                         // ============================================================
	                         // CHEQUE(S) ARE STILL REMAINING
	                         // ============================================================

	                         /*
	                          * The returned cheque was removed from the list.
	                          *
	                          * Example:
	                          *
	                          * Before:
	                          * CH001
	                          * CH002  <-- currentIndex = 1
	                          * CH003
	                          *
	                          * After removing CH002:
	                          *
	                          * CH001
	                          * CH003  <-- currentIndex is still 1
	                          *
	                          * Therefore CH003 becomes the next cheque automatically.
	                          */

	                         // If the removed cheque was the last cheque,
	                         // move the index to the new last cheque.

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

	                         loadBatchStatus();

	                         // ============================================================
	                         // UPDATE XML BUTTON
	                         // ============================================================

	                         updateXmlGenerationButton();
	                            Messagebox.show(
	                                    "Cheque returned to Maker.\n\n"
	                                            + "Cheque Status: "
	                                            + chequeStatus
	                                            + "\n"
	                                            + "Batch Status: ON_HOLD",
	                                    "Return to Maker",
	                                    Messagebox.OK,
	                                    Messagebox.INFORMATION
	                            );

	                        } catch (Exception e) {

	                            e.printStackTrace();

	                            showError(
	                                    "Unable to return cheque to Maker.",
	                                    e
	                            );
	                        }
	                    }
	                }
	        );

	    } catch (Exception e) {

	        e.printStackTrace();

	        showError(
	                "Unable to return cheque to Maker.",
	                e
	        );
	    }
	}
	/*
	 * ============================================================ REJECT
	 * ============================================================
	 */

	// ============================================================
	// REJECT BUTTON
	// ============================================================

	public void onClick$btnReject(Event event) {

	    if (cheques == null
	            || cheques.isEmpty()
	            || currentIndex < 0
	            || currentIndex >= cheques.size()) {

	        return;
	    }

	    try {

	        OutwardCheque cheque =
	                cheques.get(currentIndex);

	        // --------------------------------------------------------
	        // SET BATCH ID
	        // --------------------------------------------------------

	        lblRejectBatch.setValue(
	                nullSafe(batchId)
	        );

	        // --------------------------------------------------------
	        // SET CHEQUE NUMBER
	        // --------------------------------------------------------

	        lblRejectCheque.setValue(
	                nullSafe(
	                        cheque.getChequeNumber()
	                )
	        );

	        // --------------------------------------------------------
	        // CLEAR OLD VALUES
	        // --------------------------------------------------------

	        cmbRejectReason.setSelectedItem(null);

	        if (txtRejectRemarks != null) {
	            txtRejectRemarks.setValue("");
	        }

	        // --------------------------------------------------------
	        // SHOW POPUP
	        // --------------------------------------------------------

	        rejectWindow.setVisible(true);

	    } catch (Exception e) {

	        e.printStackTrace();

	        showError(
	                "Unable to open Reject Cheque popup.",
	                e
	        );
	    }
	}
	
	// ============================================================
	// CREATE REJECT WINDOW
	// ============================================================

	private void createRejectWindow() {

	    try {

	        rejectWindow =
	                (Window) Executions.createComponents(
	                        "/outward/checker/reject-cheque.zul",
	                        self,
	                        null
	                );

	        lblRejectBatch =
	                (Label) rejectWindow.getFellow(
	                        "lblRejectBatch"
	                );

	        lblRejectCheque =
	                (Label) rejectWindow.getFellow(
	                        "lblRejectCheque"
	                );

	        cmbRejectReason =
	                (Combobox) rejectWindow.getFellow(
	                        "cmbRejectReason"
	                );

	        txtRejectRemarks =
	                (Textbox) rejectWindow.getFellow(
	                        "txtRejectRemarks"
	                );

	        btnRejectConfirm =
	                (Button) rejectWindow.getFellow(
	                        "btnRejectConfirm"
	                );

	        btnRejectCancel =
	                (Button) rejectWindow.getFellow(
	                        "btnRejectCancel"
	                );

	        btnRejectConfirm.addEventListener(
	                "onClick",
	                event -> confirmRejectCheque()
	        );

	        btnRejectCancel.addEventListener(
	                "onClick",
	                event -> closeRejectWindow()
	        );

	        rejectWindow.addEventListener(
	                "onCancel",
	                event -> closeRejectWindow()
	        );

	        rejectWindow.setVisible(false);

	        loadRejectedReasons();

	    } catch (Exception e) {

	        e.printStackTrace();

	        showError(
	                "Unable to create Reject Cheque popup.",
	                e
	        );
	    }
	}
	
	// ============================================================
	// CONFIRM REJECT CHEQUE
	// ============================================================

	private void confirmRejectCheque() {

	    if (cheques == null
	            || cheques.isEmpty()
	            || currentIndex < 0
	            || currentIndex >= cheques.size()) {

	        return;
	    }

	    try {

	        // --------------------------------------------------------
	        // CHECK REASON
	        // --------------------------------------------------------

	        if (cmbRejectReason == null
	                || cmbRejectReason.getSelectedItem() == null) {

	            Messagebox.show(
	                    "Please select a rejection reason.",
	                    "Reject Cheque",
	                    Messagebox.OK,
	                    Messagebox.EXCLAMATION
	            );

	            return;
	        }

	        // --------------------------------------------------------
	        // CURRENT CHEQUE
	        // --------------------------------------------------------

	        OutwardCheque cheque =
	                cheques.get(currentIndex);

	        Comboitem selectedReason =
	                cmbRejectReason.getSelectedItem();

	        // --------------------------------------------------------
	        // GET REASON ID
	        // --------------------------------------------------------

	        Object reasonValue =
	                selectedReason.getValue();

	        String reasonId =
	                reasonValue == null
	                        ? ""
	                        : reasonValue.toString().trim();

	        String reasonName =
	                selectedReason.getLabel();

	        // --------------------------------------------------------
	        // GET REMARKS
	        // --------------------------------------------------------

	     // --------------------------------------------------------
	     // GET REMARKS
	     // --------------------------------------------------------

	     String remarks = "";

	     if (txtRejectRemarks != null) {

	         remarks = txtRejectRemarks.getValue();

	         if (remarks == null) {
	             remarks = "";
	         }

	         remarks = remarks.trim();
	     }

	     // --------------------------------------------------------
	     // CHECK REMARKS - MANDATORY
	     // --------------------------------------------------------
	  // --------------------------------------------------------
	  // CHECK REMARKS - MANDATORY
	  // --------------------------------------------------------

	  if (remarks.isEmpty()) {

	      Messagebox.show(
	              "Please enter remarks before rejecting the cheque.",
	              "Reject Cheque",
	              Messagebox.OK,
	              Messagebox.EXCLAMATION
	      );

	      if (txtRejectRemarks != null) {
	          txtRejectRemarks.focus();
	      }

	      return;
	  
	     }

	     final String finalRemarks = remarks;

	        // --------------------------------------------------------
	        // CONFIRMATION POPUP
	        // --------------------------------------------------------

	        Messagebox.show(
	                "Reject cheque "
	                        + cheque.getChequeNumber()
	                        + "?",
	                "Confirm Reject",
	                Messagebox.YES | Messagebox.NO,
	                Messagebox.QUESTION,
	                confirmEvent -> {

	                    if (!Messagebox.ON_YES.equals(
	                            confirmEvent.getName())) {

	                        return;
	                    }

	                    try {

	                        // ========================================================
	                        // CREATE REJECTED CHEQUE ENTITY
	                        // ========================================================

	                        OutwardRejectedCheques rejectedCheque =
	                                new OutwardRejectedCheques();

	                        // --------------------------------------------------------
	                        // OUTWARD CHEQUE ID
	                        // --------------------------------------------------------

	                        rejectedCheque.setOutwardChequeId(
	                                cheque.getOutwardChequeId()
	                        );

	                        // --------------------------------------------------------
	                        // REJECTED BY
	                        //
	                        // This is the USERNAME.
	                        // DAO will convert username -> user_id.
	                        //
	                        // ochecker -> USR1003
	                        // --------------------------------------------------------

	                        rejectedCheque.setRejectedBy(
	                                getCurrentUsername()
	                        );

	                        // --------------------------------------------------------
	                        // REJECTED DATE
	                        // --------------------------------------------------------

	                        rejectedCheque.setRejectedDate(
	                                new Timestamp(
	                                        System.currentTimeMillis()
	                                )
	                        );

	                        // --------------------------------------------------------
	                        // BATCH ID
	                        // --------------------------------------------------------

	                        rejectedCheque.setOutwardBatchId(
	                                cheque.getOutwardBatchId()
	                        );

	                        // --------------------------------------------------------
	                        // CHEQUE AMOUNT
	                        // --------------------------------------------------------

	                        rejectedCheque.setChequeAmount(
	                                cheque.getChequeAmount()
	                        );

	                        // --------------------------------------------------------
	                        // REMARKS
	                        //
	                        // Store rejection reason + checker remarks.
	                        // --------------------------------------------------------

	                        String rejectionRemarks =
	                                "Rejection Reason: "
	                                + reasonName
	                                + " | Remarks: "
	                                + finalRemarks;
	                        rejectedCheque.setRemarks(
	                                rejectionRemarks
	                        );

	                        // ========================================================
	                        // DEBUG
	                        // ========================================================

	                        System.out.println(
	                                "================================="
	                        );

	                        System.out.println(
	                                "REJECT CHEQUE"
	                        );

	                        System.out.println(
	                                "Cheque ID = "
	                                + rejectedCheque.getOutwardChequeId()
	                        );

	                        System.out.println(
	                                "Cheque Number = "
	                                + cheque.getChequeNumber()
	                        );

	                        System.out.println(
	                                "Username = "
	                                + rejectedCheque.getRejectedBy()
	                        );

	                        System.out.println(
	                                "Batch ID = "
	                                + rejectedCheque.getOutwardBatchId()
	                        );

	                        System.out.println(
	                                "Amount = "
	                                + rejectedCheque.getChequeAmount()
	                        );

	                        System.out.println(
	                                "Reason = "
	                                + reasonName
	                        );

	                        System.out.println(
	                                "Remarks = "
	                                + rejectionRemarks
	                        );

	                        System.out.println(
	                                "================================="
	                        );

	                        // ========================================================
	                        // 1. SAVE INTO OUTWARD REJECTED CHEQUES
	                        // ========================================================

	                        outwardCheckerQueueService
	                                .saveRejectedCheque(
	                                        rejectedCheque
	                                );

	                        // ========================================================
	                        // 2. CHANGE OUTWARD CHEQUE STATUS
	                        // ========================================================

	                        outwardCheckerQueueService
	                                .updateChequeStatus(
	                                        cheque.getChequeNumber(),
	                                        "REJECTED"
	                                );

	                        // ========================================================
	                        // 3. UPDATE LOCAL OBJECT
	                        // ========================================================

	                        cheque.setChequeStatus(
	                                "REJECTED"
	                        );

	                        // ========================================================
	                        // 4. REMOVE ACCOUNT VALIDATION
	                        // ========================================================

	                        accountValidationResults.remove(
	                                nullSafe(
	                                        cheque.getOutwardChequeId()
	                                )
	                        );

	                        // ========================================================
	                        // 5. CLOSE REJECT POPUP
	                        // ========================================================

	                        closeRejectWindow();

	                        // ========================================================
	                        // 6. REMOVE CHEQUE FROM CURRENT QUEUE
	                        // ========================================================

	                        cheques.remove(currentIndex);

	                        // ========================================================
	                        // 7. SHOW NEXT CHEQUE
	                        // ========================================================

	                        if (cheques.isEmpty()) {

	                            currentIndex = 0;

	                            if (lblCurrentCheque != null) {
	                                lblCurrentCheque.setValue("0");
	                            }

	                            if (lblRemaining != null) {
	                                lblRemaining.setValue("0");
	                            }

	                            if (lblCurrentChequeNavigation != null) {

	                                lblCurrentChequeNavigation.setValue(
	                                        "Cheque 0 of 0 · 0 remaining"
	                                );
	                            }

	                            updateXmlGenerationButton();

	                        } else {

	                            if (currentIndex >= cheques.size()) {

	                                currentIndex =
	                                        cheques.size() - 1;
	                            }

	                            displayCheque();
	                        }

	                        // ========================================================
	                        // SUCCESS MESSAGE
	                        // ========================================================

	                        Messagebox.show(
	                                "Cheque rejected successfully.\n\n"
	                                + "Cheque Number: "
	                                + cheque.getChequeNumber()
	                                + "\n"
	                                + "Reason: "
	                                + reasonName,
	                                "Reject Cheque",
	                                Messagebox.OK,
	                                Messagebox.INFORMATION
	                        );

	                    } catch (Exception e) {

	                        e.printStackTrace();

	                        showError(
	                                "Unable to reject cheque.",
	                                e
	                        );
	                    }
	                }
	        );

	    } catch (Exception e) {

	        e.printStackTrace();

	        showError(
	                "Unable to reject cheque.",
	                e
	        );
	    }
	}
	
	// ============================================================
	// GET CURRENT USERNAME
	// ============================================================

	private String getCurrentUsername() {

	    Object username =
	            Sessions.getCurrent()
	                    .getAttribute("CTS_USERNAME");

	    if (username == null) {
	        return "UNKNOWN";
	    }

	    return username.toString();
	}
	
	// ============================================================
	// CLOSE REJECT WINDOW
	// ============================================================

	private void closeRejectWindow() {

	    if (rejectWindow != null) {
	        rejectWindow.setVisible(false);
	    }
	}

	/*
	 * ============================================================ ZOOM IN
	 * ============================================================
	 */

	public void onClick$btnZoomIn(Event event) {

		if (zoomLevel < 3.0) {

			zoomLevel += 0.25;

			applyZoom();
		}
	}

	/*
	 * ============================================================ ZOOM OUT
	 * ============================================================
	 */

	public void onClick$btnZoomOut(Event event) {

		if (zoomLevel > 0.5) {

			zoomLevel -= 0.25;

			applyZoom();
		}
	}

	/*
	 * ============================================================ ZOOM RESET
	 * ============================================================
	 */

	public void onClick$btnZoomReset(Event event) {

		zoomLevel = 1.0;

		applyZoom();
	}

	/*
	 * ============================================================ APPLY ZOOM
	 * ============================================================
	 */

	private void applyZoom() {

		if (imgCheque != null) {

			imgCheque.setStyle("transform:scale(" + zoomLevel + ");" + "transform-origin:" + "center center;");
		}

		if (lblZoom != null) {

			lblZoom.setValue(String.format("%.0f%%", zoomLevel * 100));
		}
	}

	/*
	 * ============================================================ BACK TO
	 * DASHBOARD ============================================================
	 */

	public void onClick$btnBackToDashboard(Event event) {

		Executions.sendRedirect("/outward/checker/dashboard.zul");
	}

	/*
	 * ============================================================ SHOW ERROR
	 * ============================================================
	 */

	private void showError(String message, Exception e) {

		String errorMessage = message;

		if (e != null && e.getMessage() != null && !e.getMessage().trim().isEmpty()) {

			errorMessage += "\n\n" + e.getMessage();
		}

		Messagebox.show(errorMessage, "Error", Messagebox.OK, Messagebox.ERROR);
	}

	/*
	 * ============================================================ NULL SAFE
	 * ============================================================
	 */

	private String nullSafe(String value) {

		return value == null ? "" : value;
	}
}