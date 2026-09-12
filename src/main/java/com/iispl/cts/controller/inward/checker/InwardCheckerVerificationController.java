package com.iispl.cts.controller.inward.checker;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.text.SimpleDateFormat;

import org.zkoss.image.AImage;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.daoimpl.inward.InwardChequeImageDAOImpl;
import com.iispl.cts.entity.RejectedReason;
import com.iispl.cts.entity.SendBackReason;
import com.iispl.cts.entity.inward.CbsValidationResult;
import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.entity.inward.InwardChequeImage;
import com.iispl.cts.service.RejectedReasonService;
import com.iispl.cts.service.SendBackReasonService;
import com.iispl.cts.service.inward.InwardBatchService;
import com.iispl.cts.service.inward.InwardChequeService;
import com.iispl.cts.serviceimpl.RejectedReasonServiceImpl;
import com.iispl.cts.serviceimpl.SendBackReasonServiceImpl;
import com.iispl.cts.serviceimpl.inward.InwardChequeServiceImpl;

public class InwardCheckerVerificationController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;
	private Label lblBatchId;
	private Label lblTotalCheques;
	private Label lblChequeNumber;
	private Label lblChequeStatus;
	private Vlayout makerRejectionRequestSection;

	private Label lblMakerRejectionRemarks;
	private Label lblReceivedDate;
	private Label lblVerification;
	private SendBackReasonService sendBackReasonService;

	private Label lblMicrCode;
	private Label lblBankCode;
	private Label lblBranchCode;
	private Label lblTransactionCode;

	private Label lblPresentingBank;
	private Label lblVerificationChequeNumber;
	private Label lblChequeDate;
	private Label lblAmount;

	private Label lblDraweeName;
	private Label lblDraweeAccountNumber;
	private Label lblAccountBalance;

	private Button btnPrevious;
	private Button btnNext;

	private Button btnAccept;
	private Button btnReturn;
	private Button btnSendBack;
	private Label lblChequePosition;
	private Label lblVerificationStatus;

	private InwardChequeService inwardChequeService;
	private RejectedReasonService rejectedReasonService = RejectedReasonServiceImpl.getInstance();
	private List<InwardCheque> currentBatchCheques = new ArrayList<>();
	private String currentChequeId;
	private String currentBatchId;
	private int currentChequeIndex = 0;
	private Button btnSubmitVerification;

	private Window verificationSummaryWindow;

	private Label lblSummaryTotal;
	private Label lblSummaryAccepted;
	private Label lblSummaryRejected;

	private Button btnStayVerification;
	private Button btnSubmitBatch;

	private InwardBatchService inwardBatchService;
	@Wire
	private Window rejectReasonWindow;

	@Wire
	Combobox cmbRejectedReason;
	@Wire
	private Textbox txtRejectRemarks;
	@Wire
	private Button btnCancelReject;
	@Wire
	private Button btnProceedReject;
	private Component pageRoot;
	@Wire
	private Image chequeImage;
	@Wire
	private Groupbox emptyImageState;
	@Wire
	private Window sendBackReasonWindow;

	@Wire
	private Combobox cmbSendBackReason;
	@Wire
	private Component noChequesContainer;

	@Wire
	private Textbox txtSendBackRemarks;
	private double zoomScale = 1.0;
	private int rotationAngle = 0;
	private double panX = 0;
	private double panY = 0;
	@Wire
	private Div batchInfoCard;

	@Wire
	private Hlayout verificationContent;

	@Wire
	private Hlayout verificationNavigation;

	@Wire
	private Label lblNoCheques;

	@Wire
	private Combobox cmbChequeStatus;

	@Wire
	private Button btnFilterCheque;

	@Wire
	private Button btnResetFilter;

	@Wire
	private Vlayout rejectionDetailsSection;

	@Wire
	private Label lblRejectedReasonCode;

	@Wire
	private Label lblRejectedReasonName;

	@Wire
	private Label lblRejectedReasonRemarks;
	
	@Wire
	private Label lblMakerRejectionReasonCode;

	@Wire
	private Label lblMakerRejectionReasonName;

	private InwardChequeImageDAOImpl inwardChequeImageDAO;
	private boolean verificationOrderInitialized = false;

	@Override
	public void doAfterCompose(Component comp) throws Exception {

		super.doAfterCompose(comp);

		pageRoot = comp.getPage().getFirstRoot();

		inwardChequeService = new InwardChequeServiceImpl();

		sendBackReasonService = new SendBackReasonServiceImpl();

		// Get reject popup
		Window window = (Window) pageRoot.getFellow("rejectReasonWindow");

		// Get Proceed button from popup ID space
		Button proceedButton = (Button) window.getFellow("btnProceedReject");

		// Get Cancel button from popup ID space
		Button cancelButton = (Button) window.getFellow("btnCancelReject");

		// Manually register events because popup has its own ID space
		proceedButton.addEventListener(Events.ON_CLICK, event -> onClick$btnProceedReject());

		cancelButton.addEventListener(Events.ON_CLICK, event -> onClick$btnCancelReject());
		Window sendBackWindow = (Window) pageRoot.getFellow("sendBackReasonWindow");

		Button proceedSendBackButton = (Button) sendBackWindow.getFellow("btnProceedSendBack");

		Button cancelSendBackButton = (Button) sendBackWindow.getFellow("btnCancelSendBack");

		proceedSendBackButton.addEventListener(Events.ON_CLICK, event -> proceedSendBack());

		cancelSendBackButton.addEventListener(Events.ON_CLICK, event -> onClick$btnCancelSendBack());

		verificationSummaryWindow = (Window) pageRoot.getFellow("verificationSummaryWindow");

		lblSummaryTotal = (Label) verificationSummaryWindow.getFellow("lblSummaryTotal");

		lblSummaryAccepted = (Label) verificationSummaryWindow.getFellow("lblSummaryAccepted");

		lblSummaryRejected = (Label) verificationSummaryWindow.getFellow("lblSummaryRejected");

		btnStayVerification = (Button) verificationSummaryWindow.getFellow("btnStayVerification");

		btnSubmitBatch = (Button) verificationSummaryWindow.getFellow("btnSubmitBatch");

		btnStayVerification.addEventListener(Events.ON_CLICK, event -> onClick$btnStayVerification());

		btnSubmitBatch.addEventListener(Events.ON_CLICK, event -> onClick$btnSubmitBatch());

		String batchId = Executions.getCurrent().getParameter("batchId");

		if (batchId != null && !batchId.trim().isEmpty()) {

			currentBatchId = batchId.trim();

			Sessions.getCurrent().setAttribute("ACTIVE_VERIFICATION_BATCH_ID", currentBatchId);
		} else {
			String sessionBatch = (String) Sessions.getCurrent().getAttribute("ACTIVE_VERIFICATION_BATCH_ID");

			if (sessionBatch != null && !sessionBatch.trim().isEmpty()) {

				currentBatchId = sessionBatch.trim();

			} else {
				showNoChequesToVerify();
				return;
			}
		}
		System.out.println("Selected Verification Batch: " + currentBatchId);
		loadSelectedBatch();
	}

	private void loadSelectedBatch() {

		if (currentBatchId == null || currentBatchId.trim().isEmpty()) {

			Messagebox.show("No batch selected for verification.", "Verification", Messagebox.OK,
					Messagebox.EXCLAMATION);

			return;
		}

		verificationOrderInitialized = false;

		currentBatchCheques = inwardChequeService.getChequesByBatchAndStatus(currentBatchId, null);

		if (currentBatchCheques == null || currentBatchCheques.isEmpty()) {

			Messagebox.show("No cheques found for batch: " + currentBatchId, "Verification", Messagebox.OK,
					Messagebox.EXCLAMATION);

			return;
		}

		currentBatchCheques = prioritizeRejectionRequests(currentBatchCheques);

		verificationOrderInitialized = true;
		currentChequeIndex = 0;

		currentChequeId = currentBatchCheques.get(currentChequeIndex).getInwardChequeId();

		loadChequeDetails(currentChequeId);

		updateChequePosition();
		updateVerificationCount();
	}

	private void loadChequeDetails(String inwardChequeId) {

		try {

			clearCbsFieldHighlights();

			InwardCheque cheque = inwardChequeService.findById(inwardChequeId);

			if (cheque == null) {

				Messagebox.show("Cheque not found: " + inwardChequeId, "Verification", Messagebox.OK,
						Messagebox.EXCLAMATION);

				return;
			}

			loadChequeImage(cheque, "FRONT");
			currentChequeId = cheque.getInwardChequeId();
			currentBatchId = cheque.getInwardBatchId();
			if (!verificationOrderInitialized) {
				currentBatchCheques = inwardChequeService.getChequesByBatchAndStatus(currentBatchId, null);
			}
			int foundIndex = -1;

			for (int i = 0; i < currentBatchCheques.size(); i++) {

				InwardCheque batchCheque = currentBatchCheques.get(i);

				if (batchCheque.getInwardChequeId().equalsIgnoreCase(currentChequeId)) {

					foundIndex = i;
					break;
				}
			}

			if (foundIndex >= 0) {
				currentChequeIndex = foundIndex;
			}

			if (lblBatchId != null) {
				lblBatchId.setValue(safeValue(cheque.getInwardBatchId()));
			}

			if (lblTotalCheques != null) {

				lblTotalCheques.setValue(String.valueOf(currentBatchCheques.size()));
			}

			if (lblChequeNumber != null) {

				lblChequeNumber.setValue(safeValue(cheque.getChequeNumber()));
			}

			if (lblVerificationChequeNumber != null) {

				lblVerificationChequeNumber.setValue(safeValue(cheque.getChequeNumber()));
			}

			if (lblChequeStatus != null) {

				lblChequeStatus.setValue(safeValue(cheque.getChequeStatus()));
			}

			loadMakerRejectionRemarks(cheque);
			loadRejectedReason(cheque);

			if (lblReceivedDate != null && cheque.getCreatedAt() != null) {
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");

				lblReceivedDate.setValue(dateFormat.format(cheque.getCreatedAt()));
			}
			updateVerificationCount();

			if (lblMicrCode != null) {

				lblMicrCode.setValue(safeValue(cheque.getMicrCode()));
			}
			if (lblBankCode != null) {

				lblBankCode.setValue(cheque.getBankCode());
			}
			if (lblBranchCode != null) {
				lblBranchCode.setValue(cheque.getBranchCode());
			}
			if (lblTransactionCode != null) {
				lblTransactionCode.setValue(cheque.getTransactionCode());
			}

			if (lblPresentingBank != null) {

				String bankName = inwardChequeService.getBankNameByCode(cheque.getBankCode());

				if (bankName != null && !bankName.trim().isEmpty()) {
					lblPresentingBank.setValue(bankName);
				} else {
					lblPresentingBank.setValue("-");
				}
			}

			if (lblChequeDate != null && cheque.getChequeDate() != null) {

				lblChequeDate.setValue(cheque.getChequeDate().toString());
			}

			if (lblAmount != null && cheque.getChequeAmount() != null) {

				lblAmount.setValue("₹" + cheque.getChequeAmount().toString());
			}

			if (lblDraweeName != null) {

				lblDraweeName.setValue(safeValue(cheque.getDraweeName()));
			}

			if (lblDraweeAccountNumber != null) {

				lblDraweeAccountNumber.setValue(safeValue(cheque.getDraweeAccountNumber()));
			}

			if (lblAccountBalance != null) {

				BigDecimal accountBalance = inwardChequeService.getAccountBalance(cheque.getDraweeAccountNumber());

				if (accountBalance != null) {

					lblAccountBalance.setValue("₹" + accountBalance.toString());

				} else {

					lblAccountBalance.setValue("₹0.00");
				}
			}

			if (lblVerificationStatus != null) {

				String status = cheque.getChequeStatus();

				if ("ACCEPTED".equalsIgnoreCase(status) || "REJECTED".equalsIgnoreCase(status)) {

					lblVerificationStatus.setValue("VERIFIED");

					lblVerificationStatus.setStyle("background:#E8F5E9;" + "color:#198754;"
							+ "border:1px solid #198754;" + "border-radius:12px;" + "padding:3px 10px;"
							+ "font-size:11px;" + "font-weight:600;");

				} else {

					lblVerificationStatus.setValue("PENDING");
					lblVerificationStatus.setSclass("verification-status-badge");
					lblVerificationStatus.setStyle("");
				}
			}
			updateChequePosition();

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to load cheque details.\n" + e.getMessage(), "Verification Error", Messagebox.OK,
					Messagebox.ERROR);
		}
	}

	private void loadMakerRejectionRemarks(InwardCheque cheque) {

	    if (makerRejectionRequestSection != null) {
	        makerRejectionRequestSection.setVisible(false);
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

	    // Show only for Maker rejection-requested cheques
	    if (!"REJECTION_REQUESTED".equalsIgnoreCase(status)) {
	        return;
	    }

	    String chequeId = cheque.getInwardChequeId();

	    if (chequeId == null || chequeId.trim().isEmpty()) {
	        return;
	    }

	    try {

	        // Get details from Service -> DAO
	        String details =
	                inwardChequeService.getMakerRejectionRequestDetails(
	                        chequeId.trim());

	        if (details == null || details.trim().isEmpty()) {

	            System.out.println(
	                    "DEBUG: No pending Maker rejection request for cheque: "
	                    + chequeId);

	            return;
	        }

	        /*
	         * DAO returns:
	         *
	         * rejectedReasonId||remarks
	         *
	         * Example:
	         * 6||check again
	         */

	        String[] parts = details.split("\\|\\|", -1);

	        String rejectedReasonId =
	                parts.length > 0 ? parts[0].trim() : "";

	        String remarks =
	                parts.length > 1 ? parts[1].trim() : "";

	        // Find reason using existing RejectedReasonService
	        RejectedReason reason = null;

	        List<RejectedReason> reasons =
	                rejectedReasonService.getAllRejectedReasons();

	        if (reasons != null) {

	            for (RejectedReason r : reasons) {

	                if (r != null
	                        && r.getRejectedReasonId() != null
	                        && r.getRejectedReasonId()
	                                .equals(rejectedReasonId)) {

	                    reason = r;
	                    break;
	                }
	            }
	        }

	        // Reason Code
	        if (lblMakerRejectionReasonCode != null) {

	            if (reason != null) {
	                lblMakerRejectionReasonCode.setValue(
	                        "Reason Code: "
	                        + reason.getRejectedReasonCode());
	            } else {
	                lblMakerRejectionReasonCode.setValue(
	                        "Reason Code: " + rejectedReasonId);
	            }
	        }

	        // Reason Name
	        if (lblMakerRejectionReasonName != null) {

	            if (reason != null) {
	                lblMakerRejectionReasonName.setValue(
	                        "Reason: "
	                        + reason.getRejectedReasonName());
	            } else {
	                lblMakerRejectionReasonName.setValue(
	                        "Reason: Reason not found");
	            }
	        }

	        // Remarks
	        if (lblMakerRejectionRemarks != null) {

	            if (remarks != null && !remarks.isEmpty()) {

	                lblMakerRejectionRemarks.setValue(
	                        "Remarks: " + remarks);

	            } else {

	                lblMakerRejectionRemarks.setValue(
	                        "Remarks: No remarks provided");
	            }
	        }

	        // Show Maker Rejection Request section
	        if (makerRejectionRequestSection != null) {
	            makerRejectionRequestSection.setVisible(true);
	        }

	        System.out.println(
	                "DEBUG: Maker rejection request loaded for cheque: "
	                + chequeId);

	        System.out.println(
	                "DEBUG: Rejected Reason ID: "
	                + rejectedReasonId);

	        System.out.println(
	                "DEBUG: Rejected Reason: "
	                + (reason != null
	                    ? reason.getRejectedReasonCode()
	                        + " - "
	                        + reason.getRejectedReasonName()
	                    : "NOT FOUND"));

	        System.out.println(
	                "DEBUG: Maker Remarks: " + remarks);

	    } catch (Exception e) {

	        e.printStackTrace();

	        if (makerRejectionRequestSection != null) {
	            makerRejectionRequestSection.setVisible(false);
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
	    }
	}
	
	private void loadChequeImage(InwardCheque cheque, String imageType) {

	    if (chequeImage == null) {
	        return;
	    }

	    // Reset image state
	    chequeImage.setVisible(false);
	    chequeImage.setSrc(null);

	    if (cheque == null) {
	        return;
	    }

	    try {

	        String inwardChequeId = cheque.getInwardChequeId();

	        if (inwardChequeId == null || inwardChequeId.trim().isEmpty()) {
	            System.out.println("Verification: Cheque ID is empty.");
	            return;
	        }

	        InwardChequeImage image;

	        if ("BACK".equalsIgnoreCase(imageType)) {

	            image = inwardChequeService.getBackImage(inwardChequeId);

	        } else {

	            image = inwardChequeService.getFrontImage(inwardChequeId);
	        }

	        if (image == null) {

	            System.out.println(
	                "Verification: No "
	                + imageType
	                + " image found for cheque -> "
	                + inwardChequeId
	            );

	            return;
	        }

	        String imagePath = image.getImagePath();

	        if (imagePath == null || imagePath.trim().isEmpty()) {

	            System.out.println(
	                "Verification: Image path is empty for cheque -> "
	                + inwardChequeId
	            );

	            return;
	        }

	        imagePath = imagePath.trim();

	        // Remove leading slash if DB already contains one
	        if (imagePath.startsWith("/")) {
	            imagePath = imagePath.substring(1);
	        }

	        // MICR uses this exact URL structure
	        String imageSrc = "/Inward-data/" + imagePath;

	        System.out.println(
	            "Verification: Loading image URL -> "
	            + imageSrc
	        );

	        chequeImage.setSrc(imageSrc);
	        chequeImage.setVisible(true);

	    } catch (Exception e) {

	        System.err.println(
	            "Verification: Failed to load "
	            + imageType
	            + " image for cheque -> "
	            + cheque.getInwardChequeId()
	        );

	        e.printStackTrace();

	        chequeImage.setVisible(false);
	        chequeImage.setSrc(null);
	    }
	}	
	private List<InwardCheque> prioritizeRejectionRequests(List<InwardCheque> cheques) {

		if (cheques == null || cheques.isEmpty()) {
			return cheques;
		}

		List<InwardCheque> makerReturnedCheques = new ArrayList<>();

		List<InwardCheque> rejectionRequests = new ArrayList<>();

		List<InwardCheque> normalCheques = new ArrayList<>();

		for (InwardCheque cheque : cheques) {

			if (cheque == null) {
				continue;
			}

			String status = cheque.getChequeStatus();

			if ("MAKER_RETURNED".equalsIgnoreCase(status)) {

				makerReturnedCheques.add(cheque);

			}

			else if ("REJECTION_REQUESTED".equalsIgnoreCase(status) || "REJECTED_REQUESTED".equalsIgnoreCase(status)) {

				rejectionRequests.add(cheque);

			}

			else {

				normalCheques.add(cheque);
			}
		}

		List<InwardCheque> finalOrder = new ArrayList<>();

		finalOrder.addAll(makerReturnedCheques);
		finalOrder.addAll(rejectionRequests);
		finalOrder.addAll(normalCheques);

		System.out.println("DEBUG: Maker Returned count = " + makerReturnedCheques.size());

		System.out.println("DEBUG: Rejection Request count = " + rejectionRequests.size());

		System.out.println("DEBUG: Remaining cheque count = " + normalCheques.size());

		System.out.println("DEBUG: Final verification order:");

		for (int i = 0; i < finalOrder.size(); i++) {

			InwardCheque cheque = finalOrder.get(i);

			System.out.println((i + 1) + " -> " + cheque.getInwardChequeId() + " / " + cheque.getChequeNumber() + " / "
					+ cheque.getChequeStatus());
		}

		return finalOrder;
	}

	public void onClick$btnFront() {

		if (currentChequeId != null) {

			InwardCheque cheque = inwardChequeService.findById(currentChequeId);

			if (cheque != null) {
				loadChequeImage(cheque, "FRONT");
			}
		}
	}

	public void onClick$btnBack() {

		if (currentChequeId != null) {

			InwardCheque cheque = inwardChequeService.findById(currentChequeId);

			if (cheque != null) {
				loadChequeImage(cheque, "BACK");
			}
		}
	}

	public void onClick$btnAccept() {

		try {

			clearCbsFieldHighlights();

			InwardCheque cheque = inwardChequeService.findById(currentChequeId);

			if (cheque == null) {

				Messagebox.show("Cheque not found.", "Verification", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			String currentStatus = cheque.getChequeStatus();

			if ("ACCEPTED".equalsIgnoreCase(currentStatus) || "REJECTED".equalsIgnoreCase(currentStatus)) {

				Messagebox.show("This cheque is already verified.", "Verification", Messagebox.OK,
						Messagebox.EXCLAMATION);

				return;
			}

			CbsValidationResult cbsResult = runCbsValidation(cheque);

			if (!cbsResult.isPassed()) {

				String failureReason = cbsResult.getReason();

				highlightFailedCbsField(failureReason);

				String cbsReasonId = getCbsRejectedReasonId();

				if (cbsReasonId == null) {

					Messagebox.show("CBS validation failed, but " + "CBS rejection reason is not configured "
							+ "in rejected_reasons.", "CBS Validation", Messagebox.OK, Messagebox.ERROR);

					return;
				}

				Object userObject = Sessions.getCurrent().getAttribute("CTS_USERNAME");

				String rejectedBy;

				if (userObject != null) {
					rejectedBy = userObject.toString();
				} else {
					rejectedBy = "SYSTEM";
				}

				boolean rejectionSaved = inwardChequeService.saveRejection(cheque.getInwardChequeId(), cbsReasonId,
						failureReason, rejectedBy);

				if (!rejectionSaved) {

					Messagebox.show("CBS validation failed, but " + "rejection details could not be saved.",
							"CBS Validation", Messagebox.OK, Messagebox.ERROR);

					return;
				}

				cheque.setChequeStatus("REJECTED");

				boolean updated = inwardChequeService.updateChequeDetails(cheque);

				if (!updated) {

					Messagebox.show("CBS validation failed and " + "cheque status could not be updated.",
							"CBS Validation", Messagebox.OK, Messagebox.ERROR);

					return;
				}

				if (lblChequeStatus != null) {

					lblChequeStatus.setValue("REJECTED");

					lblChequeStatus.setSclass("status-badge mismatch");
				}

				if (lblVerificationStatus != null) {

					lblVerificationStatus.setValue("VERIFIED");

					lblVerificationStatus.setStyle("background:#E8F5E9;" + "color:#198754;"
							+ "border:1px solid #198754;" + "border-radius:12px;" + "padding:3px 10px;"
							+ "font-size:11px;" + "font-weight:600;");
				}

				updateVerificationCount();

				Messagebox.show(
						"CBS validation failed.\n\n" + "Cheque has been rejected.\n\n" + "Reason: " + failureReason,
						"CBS Validation Failed", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			cheque.setChequeStatus("ACCEPTED");

			boolean updated = inwardChequeService.updateChequeDetails(cheque);

			if (!updated) {

				Messagebox.show("CBS validation passed, " + "but cheque status could not be updated.", "Verification",
						Messagebox.OK, Messagebox.ERROR);

				return;
			}

			if (lblChequeStatus != null) {

				lblChequeStatus.setValue("ACCEPTED");

				lblChequeStatus.setSclass("status-badge");
			}

			if (lblVerificationStatus != null) {

				lblVerificationStatus.setValue("VERIFIED");

				lblVerificationStatus.setStyle("background:#E8F5E9;" + "color:#198754;" + "border:1px solid #198754;"
						+ "border-radius:12px;" + "padding:3px 10px;" + "font-size:11px;" + "font-weight:600;");
			}

			updateVerificationCount();

			Messagebox.show("Cheque accepted successfully.", "Verification", Messagebox.OK, Messagebox.INFORMATION,
					event -> moveToNextCheque());

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to complete verification.\n" + e.getMessage(), "Verification Error", Messagebox.OK,
					Messagebox.ERROR);
		}
	}

	private void highlightCbsField(Label field) {

		if (field == null) {
			return;
		}

		field.setStyle("background:#FFE5E5;" + "border:1px solid #DC3545;" + "color:#B02A37;" + "padding:2px 5px;"
				+ "border-radius:4px;" + "font-weight:700;");
	}

	private void loadRejectedReason(InwardCheque cheque) {

		if (rejectionDetailsSection != null) {
			rejectionDetailsSection.setVisible(false);
		}

		if (lblRejectedReasonCode != null) {
			lblRejectedReasonCode.setValue("");
		}

		if (lblRejectedReasonName != null) {
			lblRejectedReasonName.setValue("");
		}

		if (lblRejectedReasonRemarks != null) {
			lblRejectedReasonRemarks.setValue("");
		}

		if (cheque == null) {
			return;
		}

		String status = cheque.getChequeStatus();

		if (!"REJECTED".equalsIgnoreCase(status)) {
			return;
		}

		String details = inwardChequeService.getRejectedReasonDetails(cheque.getInwardChequeId());

		if (details == null || details.trim().isEmpty()) {
			return;
		}

		String reasonPart = "";
		String remarksPart = "";

		String[] lines = details.split("\\n");

		for (String line : lines) {

			if (line.startsWith("Reason:")) {
				reasonPart = line.substring("Reason:".length()).trim();

			} else if (line.startsWith("Remarks:")) {
				remarksPart = line.substring("Remarks:".length()).trim();
			}
		}
		String reasonCode = "";
		String reasonName = "";

		if (reasonPart.contains(" - ")) {

			String[] reasonParts = reasonPart.split(" - ", 2);

			reasonCode = reasonParts[0].trim();
			reasonName = reasonParts[1].trim();

		} else {

			reasonName = reasonPart;
		}

		if (lblRejectedReasonCode != null) {
			lblRejectedReasonCode.setValue("Reason Code: " + reasonCode);
		}

		if (lblRejectedReasonName != null) {
			lblRejectedReasonName.setValue("Reason: " + reasonName);
		}

		if (lblRejectedReasonRemarks != null) {
			lblRejectedReasonRemarks
					.setValue("Remarks: " + (remarksPart.isEmpty() ? "No remarks provided" : remarksPart));
		}

		if (rejectionDetailsSection != null) {
			rejectionDetailsSection.setVisible(true);
		}
	}

	private String getCbsRejectedReasonId() {

		List<RejectedReason> reasons = rejectedReasonService.getAllRejectedReasons();

		if (reasons == null) {
			return null;
		}

		for (RejectedReason reason : reasons) {

			if (reason == null) {
				continue;
			}

			if ("CBS_VALIDATION_FAILED".equalsIgnoreCase(reason.getRejectedReasonCode())) {

				return reason.getRejectedReasonId();
			}
		}

		return null;
	}

	private void highlightFailedCbsField(String reason) {

		clearCbsFieldHighlights();

		if (reason == null) {
			return;
		}

		String r = reason.toLowerCase();

		if (r.contains("micr")) {

			highlightCbsField(lblMicrCode);

		} else if (r.contains("bank code")) {

			highlightCbsField(lblBankCode);

		} else if (r.contains("branch code")) {

			highlightCbsField(lblBranchCode);

		} else if (r.contains("transaction code")) {

			highlightCbsField(lblTransactionCode);

		} else if (r.contains("account holder") || r.contains("name mismatch")) {

			highlightCbsField(lblDraweeName);

		} else if (r.contains("account not found") || r.contains("account is")) {

			highlightCbsField(lblDraweeAccountNumber);

		} else if (r.contains("cheque number")) {

			highlightCbsField(lblVerificationChequeNumber);

		} else if (r.contains("cheque date") || r.contains("postdated")) {

			highlightCbsField(lblChequeDate);

		} else if (r.contains("balance") || r.contains("insufficient")) {

			highlightCbsField(lblAccountBalance);

		}
	}

	private void clearCbsFieldHighlights() {

		if (lblMicrCode != null) {
			lblMicrCode.setStyle("");
		}

		if (lblBankCode != null) {
			lblBankCode.setStyle("");
		}

		if (lblBranchCode != null) {
			lblBranchCode.setStyle("");
		}

		if (lblTransactionCode != null) {
			lblTransactionCode.setStyle("");
		}

		if (lblVerificationChequeNumber != null) {
			lblVerificationChequeNumber.setStyle("");
		}

		if (lblChequeDate != null) {
			lblChequeDate.setStyle("");
		}

		if (lblAmount != null) {
			lblAmount.setStyle("");
		}

		if (lblDraweeName != null) {
			lblDraweeName.setStyle("");
		}

		if (lblDraweeAccountNumber != null) {
			lblDraweeAccountNumber.setStyle("");
		}

		if (lblAccountBalance != null) {
			lblAccountBalance.setStyle("");
		}
	}

	public void onClick$btnReturn() {

		try {

			InwardCheque cheque = inwardChequeService.findById(currentChequeId);

			if (cheque == null) {

				Messagebox.show("Cheque not found.", "Verification", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			String currentStatus = cheque.getChequeStatus();

			if ("ACCEPTED".equalsIgnoreCase(currentStatus) || "REJECTED".equalsIgnoreCase(currentStatus)) {

				Messagebox.show("This cheque is already verified.", "Verification", Messagebox.OK,
						Messagebox.EXCLAMATION);

				return;
			}
			Window window = (Window) pageRoot.getFellow("rejectReasonWindow");

			Combobox comboBox = (Combobox) window.getFellow("cmbRejectedReason");

			Textbox remarks = (Textbox) window.getFellow("txtRejectRemarks");

			loadRejectedReasons(comboBox);

			comboBox.setSelectedItem(null);

			remarks.setValue("");

			window.doModal();

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to open reject window.\n" + e.getMessage(), "Verification Error", Messagebox.OK,
					Messagebox.ERROR);
		}
	}

	private void loadChequeStatusFilter() {

		if (cmbChequeStatus == null) {
			return;
		}

		cmbChequeStatus.getItems().clear();

		Comboitem allItem = new Comboitem();
		allItem.setLabel("All Status");
		allItem.setValue("ALL");
		cmbChequeStatus.appendChild(allItem);

		Comboitem pendingItem = new Comboitem();
		pendingItem.setLabel("Pending");
		pendingItem.setValue("CHECKER_PROCESSING_PENDING");
		cmbChequeStatus.appendChild(pendingItem);

		Comboitem acceptedItem = new Comboitem();
		acceptedItem.setLabel("Accepted");
		acceptedItem.setValue("ACCEPTED");
		cmbChequeStatus.appendChild(acceptedItem);

		Comboitem rejectedItem = new Comboitem();
		rejectedItem.setLabel("Rejected");
		rejectedItem.setValue("REJECTED");
		cmbChequeStatus.appendChild(rejectedItem);

		Comboitem micrItem = new Comboitem();
		micrItem.setLabel("Sent Back - MICR");
		micrItem.setValue("SEND_BACK_TO_MAKER_MICR");
		cmbChequeStatus.appendChild(micrItem);

		Comboitem dataEntryItem = new Comboitem();
		dataEntryItem.setLabel("Sent Back - Data Entry");
		dataEntryItem.setValue("SEND_BACK_TO_MAKER_DATA_ENTRY");
		cmbChequeStatus.appendChild(dataEntryItem);

		cmbChequeStatus.setSelectedItem(allItem);
	}

	private void loadRejectedReasons(Combobox comboBox) {

		comboBox.getItems().clear();

		List<RejectedReason> reasons = rejectedReasonService.getAllRejectedReasons();

		for (RejectedReason reason : reasons) {

			Comboitem item = new Comboitem();

			item.setLabel(reason.getRejectedReasonCode() + " - " + reason.getRejectedReasonName());

			item.setValue(reason);

			comboBox.appendChild(item);
		}
	}

	private void moveToNextCheque() {

		if (currentBatchCheques == null || currentBatchCheques.isEmpty()) {
			return;
		}

		if (currentChequeIndex < currentBatchCheques.size() - 1) {

			currentChequeIndex++;

			InwardCheque nextCheque = currentBatchCheques.get(currentChequeIndex);

			if (nextCheque == null || nextCheque.getInwardChequeId() == null) {
				return;
			}

			currentChequeId = nextCheque.getInwardChequeId();

			loadChequeDetails(currentChequeId);

			updateChequePosition();

			return;
		}

		updateChequePosition();

		if (areAllChequesVerified()) {

			Messagebox.show("All cheques in this batch have been verified.", "Verification Completed", Messagebox.OK,
					Messagebox.INFORMATION);
		}
	}

	private boolean areAllChequesVerified() {

		if (currentBatchId == null || currentBatchId.trim().isEmpty()) {

			return false;
		}

		List<InwardCheque> batchCheques = inwardChequeService.getChequesByBatchAndStatus(currentBatchId, null);

		if (batchCheques == null || batchCheques.isEmpty()) {

			return false;
		}

		for (InwardCheque cheque : batchCheques) {

			if (cheque == null) {
				continue;
			}

			String status = cheque.getChequeStatus();

			if (!"ACCEPTED".equalsIgnoreCase(status) && !"REJECTED".equalsIgnoreCase(status)) {

				return false;
			}
		}

		return true;
	}

	private void updateChequePosition() {

		if (lblChequePosition == null) {
			return;
		}

		int total = currentBatchCheques.size();

		if (total == 0) {

			lblChequePosition.setValue("0/0");

			if (btnPrevious != null) {
				btnPrevious.setDisabled(true);
			}

			if (btnNext != null) {
				btnNext.setDisabled(true);
			}

			return;
		}

		lblChequePosition.setValue((currentChequeIndex + 1) + "/" + total);

		if (btnPrevious != null) {

			btnPrevious.setDisabled(currentChequeIndex <= 0);
		}

		if (btnNext != null) {

			btnNext.setDisabled(currentChequeIndex >= total - 1);
		}
	}

	private String safeValue(String value) {

		return value != null ? value : "";
	}

	private void updateVerificationCount() {

		if (lblVerification == null || currentBatchId == null) {
			return;
		}

		try {

			List<InwardCheque> batchCheques = inwardChequeService.getChequesByBatchAndStatus(currentBatchId, null);

			int total = batchCheques != null ? batchCheques.size() : 0;

			int accepted = 0;
			int rejected = 0;

			if (batchCheques != null) {

				for (InwardCheque cheque : batchCheques) {

					if (cheque == null) {
						continue;
					}

					String status = cheque.getChequeStatus();

					if ("ACCEPTED".equalsIgnoreCase(status)) {
						accepted++;

					} else if ("REJECTED".equalsIgnoreCase(status)) {
						rejected++;
					}
				}
			}

			int verified = accepted + rejected;

			lblVerification.setValue(verified + "/" + total);

			if (btnSubmitVerification != null) {

				btnSubmitVerification.setVisible(total > 0 && verified == total);
			}

		} catch (Exception e) {

			e.printStackTrace();

			lblVerification.setValue("0/0");

			if (btnSubmitVerification != null) {
				btnSubmitVerification.setVisible(false);
			}
		}
	}

	public void onClick$btnNext() {

		if (currentBatchCheques == null || currentBatchCheques.isEmpty()) {
			return;
		}

		if (currentChequeIndex >= currentBatchCheques.size() - 1) {
			return;
		}

		currentChequeIndex++;

		InwardCheque nextCheque = currentBatchCheques.get(currentChequeIndex);

		currentChequeId = nextCheque.getInwardChequeId();

		loadChequeDetails(currentChequeId);

		updateChequePosition();
	}

	public void onClick$btnPrevious() {

		if (currentBatchCheques == null || currentBatchCheques.isEmpty()) {
			return;
		}

		if (currentChequeIndex <= 0) {
			return;
		}

		currentChequeIndex--;

		InwardCheque previousCheque = currentBatchCheques.get(currentChequeIndex);

		currentChequeId = previousCheque.getInwardChequeId();

		loadChequeDetails(currentChequeId);

		updateChequePosition();
	}

	public void onClick$btnProceedReject() {

		try {

			Window window = (Window) pageRoot.getFellow("rejectReasonWindow");

			Combobox comboBox = (Combobox) window.getFellow("cmbRejectedReason");

			Textbox remarksBox = (Textbox) window.getFellow("txtRejectRemarks");

			Comboitem selectedItem = comboBox.getSelectedItem();

			if (selectedItem == null) {

				Messagebox.show("Please select a rejected reason.", "Validation", Messagebox.OK,
						Messagebox.EXCLAMATION);

				return;
			}

			RejectedReason reason = (RejectedReason) selectedItem.getValue();

			if (reason == null) {

				Messagebox.show("Invalid rejected reason selected.", "Validation", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			String remarks = remarksBox.getValue();

			if (remarks != null) {
				remarks = remarks.trim();
			}

			InwardCheque cheque = inwardChequeService.findById(currentChequeId);

			if (cheque == null) {

				Messagebox.show("Cheque not found.", "Verification", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			String currentStatus = cheque.getChequeStatus();

			if ("ACCEPTED".equalsIgnoreCase(currentStatus) || "REJECTED".equalsIgnoreCase(currentStatus)) {

				window.setVisible(false);

				Messagebox.show("This cheque is already verified.", "Verification", Messagebox.OK,
						Messagebox.EXCLAMATION);

				return;
			}

			Object userObject = Sessions.getCurrent().getAttribute("CTS_USERNAME");

			String rejectedBy;

			if (userObject != null) {

				rejectedBy = userObject.toString();

			} else {

				rejectedBy = "Alex";
			}

			boolean rejectionSaved = inwardChequeService.saveRejection(cheque.getInwardChequeId(),
					reason.getRejectedReasonId(), remarks, rejectedBy);

			if (!rejectionSaved) {

				Messagebox.show("Unable to save rejection details.", "Verification", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			cheque.setChequeStatus("REJECTED");

			boolean chequeUpdated = inwardChequeService.updateChequeDetails(cheque);

			if (!chequeUpdated) {

				Messagebox.show("Rejection details were saved, " + "but cheque status could not be updated.",
						"Verification", Messagebox.OK, Messagebox.ERROR);

				return;
			}
			if (lblChequeStatus != null) {

				lblChequeStatus.setValue("REJECTED");

				lblChequeStatus.setSclass("status-badge mismatch");
			}

			if (lblVerificationStatus != null) {

				lblVerificationStatus.setValue("VERIFIED");

				lblVerificationStatus.setStyle("background:#E8F5E9;" + "color:#198754;" + "border:1px solid #198754;"
						+ "border-radius:12px;" + "padding:3px 10px;" + "font-size:11px;" + "font-weight:600;");
			}

			window.setVisible(false);
			comboBox.setSelectedItem(null);
			remarksBox.setValue("");
			updateVerificationCount();
			Messagebox.show("Cheque rejected successfully.", "Verification", Messagebox.OK, Messagebox.INFORMATION,
					event -> moveToNextCheque());

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to process rejection.\n" + e.getMessage(), "Verification Error", Messagebox.OK,
					Messagebox.ERROR);
		}
	}

	public void onClick$btnCancelReject() {
		try {
			Window window = (Window) pageRoot.getFellow("rejectReasonWindow");
			Combobox comboBox = (Combobox) window.getFellow("cmbRejectedReason");
			Textbox remarksBox = (Textbox) window.getFellow("txtRejectRemarks");
			comboBox.setSelectedItem(null);
			remarksBox.setValue("");
			window.setVisible(false);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private CbsValidationResult runCbsValidation(InwardCheque cheque) {

		if (cheque == null) {
			return new CbsValidationResult(false, "Cheque information is not available.");
		}
		return inwardChequeService.validateCbs(cheque);
	}

	public void onClick$btnSendBack() {

		try {

			InwardCheque cheque = inwardChequeService.findById(currentChequeId);

			if (cheque == null) {
				Messagebox.show("Cheque not found.", "Send Back", Messagebox.OK, Messagebox.ERROR);
				return;
			}

			String status = cheque.getChequeStatus();

			if ("ACCEPTED".equalsIgnoreCase(status) || "REJECTED".equalsIgnoreCase(status)) {

				Messagebox.show(
						"This cheque is already verified.\n"
								+ "Accepted or rejected cheques cannot be sent back to Maker.",
						"Send Back", Messagebox.OK, Messagebox.EXCLAMATION);

				return;
			}

			Window window = (Window) pageRoot.getFellow("sendBackReasonWindow");

			Combobox comboBox = (Combobox) window.getFellow("cmbSendBackReason");

			Textbox remarks = (Textbox) window.getFellow("txtSendBackRemarks");

			loadSendBackReasons(comboBox);

			comboBox.setSelectedItem(null);
			remarks.setValue("");

			window.doModal();

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to open Send Back window.\n" + e.getMessage(), "Send Back Error", Messagebox.OK,
					Messagebox.ERROR);
		}
	}

	public void proceedSendBack() {

		try {

			Window window = (Window) pageRoot.getFellow("sendBackReasonWindow");

			Combobox comboBox = (Combobox) window.getFellow("cmbSendBackReason");

			Comboitem selectedItem = comboBox.getSelectedItem();

			if (selectedItem == null) {

				Messagebox.show("Please select a send back reason.", "Validation", Messagebox.OK,
						Messagebox.EXCLAMATION);

				return;
			}

			InwardCheque cheque = inwardChequeService.findById(currentChequeId);

			if (cheque == null) {

				Messagebox.show("Cheque not found.", "Send Back", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			if (selectedItem == null) {
				Messagebox.show("Please select a send back reason.", "Send Back", Messagebox.OK,
						Messagebox.EXCLAMATION);
				return;
			}

			SendBackReason selectedReason = (SendBackReason) selectedItem.getValue();

			if (selectedReason == null) {
				Messagebox.show("Invalid send back reason.", "Send Back", Messagebox.OK, Messagebox.ERROR);
				return;
			}

			String reasonCode = selectedReason.getReasonCode();

			String sendBackStatus;

			if ("SBN_MICR_CHEQUE_NO".equalsIgnoreCase(reasonCode) || "SBN_MICR_SORT_CODE".equalsIgnoreCase(reasonCode)
					|| "SBN_MICR_SAN_TC".equalsIgnoreCase(reasonCode)) {

				sendBackStatus = "SEND_BACK_TO_MAKER_MICR";

			} else if ("SBN_AMOUNT_MISMATCH".equalsIgnoreCase(reasonCode)
					|| "SBN_ACC_NO_INVALID".equalsIgnoreCase(reasonCode)
					|| "SBN_DATE_ENTRY_ERROR".equalsIgnoreCase(reasonCode)
					|| "SBN_PAYEE_NAME_ERROR".equalsIgnoreCase(reasonCode)) {

				sendBackStatus = "SEND_BACK_TO_MAKER_DATA_ENTRY";

			} else {
				Messagebox.show("This send back reason has not been mapped to a Maker queue yet.", "Send Back",
						Messagebox.OK, Messagebox.EXCLAMATION);
				return;
			}

			cheque.setChequeStatus(sendBackStatus);

			boolean updated = inwardChequeService.updateChequeDetails(cheque);

			if (!updated) {

				Messagebox.show("Unable to send the cheque back to Maker.", "Send Back", Messagebox.OK,
						Messagebox.ERROR);

				return;
			}

			window.setVisible(false);

			Messagebox.show("Cheque has been sent back to Maker successfully.", "Send Back", Messagebox.OK,
					Messagebox.INFORMATION, event -> moveToNextCheque());

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to process Send Back.\n" + e.getMessage(), "Send Back Error", Messagebox.OK,
					Messagebox.ERROR);
		}
	}

	public void onClick$btnCancelSendBack() {

		Window window = (Window) pageRoot.getFellow("sendBackReasonWindow");

		window.setVisible(false);
	}

	private void loadSendBackReasons(Combobox comboBox) {

		comboBox.getItems().clear();

		List<SendBackReason> reasons = sendBackReasonService.getAllSendBackReasons();

		for (SendBackReason reason : reasons) {

			Comboitem item = new Comboitem();

			item.setLabel(reason.getReasonCode() + " - " + reason.getReasonName());

			item.setValue(reason);

			comboBox.appendChild(item);
		}
	}

	public void onClick$btnZoomOut() {

		if (chequeImage == null || !chequeImage.isVisible()) {
			return;
		}

		zoomScale -= 0.1;

		if (zoomScale < 0.5) {
			zoomScale = 0.5;
		}

		applyImageTransform();
	}

	public void onClick$btnZoomReset() {

		if (chequeImage == null || !chequeImage.isVisible()) {
			return;
		}

		zoomScale = 1.0;
		rotationAngle = 0;
		panX = 0;
		panY = 0;

		applyImageTransform();
	}

	public void onClick$btnZoomIn() {

		if (chequeImage == null || !chequeImage.isVisible()) {
			return;
		}

		zoomScale += 0.1;

		if (zoomScale > 3.0) {
			zoomScale = 3.0;
		}

		applyImageTransform();
	}

	public void onClick$btnZoomFit() {

		if (chequeImage == null || !chequeImage.isVisible()) {
			return;
		}

		zoomScale = 1.0;
		panX = 0;
		panY = 0;

		applyImageTransform();
	}

	public void onClick$btnRotate() {

		if (chequeImage == null || !chequeImage.isVisible()) {
			return;
		}

		rotationAngle += 90;

		if (rotationAngle >= 360) {
			rotationAngle = 0;
		}

		applyImageTransform();
	}

	private void applyImageTransform() {

		if (chequeImage == null) {
			return;
		}

		String imageUuid = chequeImage.getUuid();

		String script = "(function(){" + "var img=document.getElementById('" + imageUuid + "');" + "if(!img) return;"

				+ "var panX=" + panX + ";" + "var panY=" + panY + ";"

				+ "img.dataset.panX=panX;" + "img.dataset.panY=panY;" + "img.dataset.zoomScale=" + zoomScale + ";"

				+ "img.style.width='100%';" + "img.style.height='100%';" + "img.style.objectFit='contain';"

				+ "img.style.transform=" + "'translate('+panX+'px,'+panY+'px) " + "scale(" + zoomScale + ") "
				+ "rotate(" + rotationAngle + "deg)';"

				+ "img.style.transformOrigin='center center';"

				+ "img.style.cursor='default';"

				+ "})();";

		org.zkoss.zk.ui.util.Clients.evalJavaScript(script);

		enableKeyboardImageNavigation();
		enableMouseWheelZoom();
	}

	private void enableKeyboardImageNavigation() {

		if (chequeImage == null) {
			return;
		}

		String imageUuid = chequeImage.getUuid();

		String script = "(function(){" + "var img=document.getElementById('" + imageUuid + "');" + "if(!img) return;"

				+ "if(img.dataset.keyboardEnabled==='true') return;" + "img.dataset.keyboardEnabled='true';"

				+ "document.addEventListener('keydown',function(e){"

				+ "var scale=parseFloat(img.dataset.zoomScale || '1');"

				+ "if(scale <= 1) return;"

				+ "var x=parseFloat(img.dataset.panX || '0');" + "var y=parseFloat(img.dataset.panY || '0');"

				+ "var step=30;"

				+ "if(e.key==='ArrowLeft'){" + "x=x+step;" + "}"

				+ "else if(e.key==='ArrowRight'){" + "x=x-step;" + "}"

				+ "else if(e.key==='ArrowUp'){" + "y=y+step;" + "}"

				+ "else if(e.key==='ArrowDown'){" + "y=y-step;" + "}"

				+ "else {" + "return;" + "}"

				+ "e.preventDefault();"

				+ "img.dataset.panX=x;" + "img.dataset.panY=y;"

				+ "img.style.transform=" + "'translate('+x+'px,'+y+'px) " + "scale('+scale+') " + "rotate("
				+ rotationAngle + "deg)';"

				+ "});"

				+ "})();";

		org.zkoss.zk.ui.util.Clients.evalJavaScript(script);
	}

	public void onClick$btnSubmitVerification() {

		openVerificationSummary();
	}

	public void onClick$btnStayVerification() {

		if (verificationSummaryWindow != null) {
			verificationSummaryWindow.setVisible(false);
		}
	}

	private void openVerificationSummary() {

		try {

			if (currentBatchId == null) {
				return;
			}

			List<InwardCheque> batchCheques = inwardChequeService.getChequesByBatchAndStatus(currentBatchId, null);

			int total = 0;
			int accepted = 0;
			int rejected = 0;

			if (batchCheques != null) {

				total = batchCheques.size();

				for (InwardCheque cheque : batchCheques) {

					if (cheque == null) {
						continue;
					}

					String status = cheque.getChequeStatus();

					if ("ACCEPTED".equalsIgnoreCase(status)) {
						accepted++;

					} else if ("REJECTED".equalsIgnoreCase(status)) {
						rejected++;
					}
				}
			}

			int verified = accepted + rejected;

			// Safety check
			if (total == 0 || verified != total) {

				Messagebox.show("All cheques must be verified before submitting the batch.", "Verification",
						Messagebox.OK, Messagebox.EXCLAMATION);

				return;
			}

			if (lblSummaryTotal != null) {
				lblSummaryTotal.setValue(String.valueOf(total));
			}

			if (lblSummaryAccepted != null) {
				lblSummaryAccepted.setValue(String.valueOf(accepted));
			}

			if (lblSummaryRejected != null) {
				lblSummaryRejected.setValue(String.valueOf(rejected));
			}

			if (verificationSummaryWindow != null) {
				verificationSummaryWindow.doModal();
			}

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to open verification summary.\n" + e.getMessage(), "Verification Error",
					Messagebox.OK, Messagebox.ERROR);
		}
	}

	public void onClick$btnSubmitBatch() {

		try {

			if (currentBatchId == null || currentBatchId.trim().isEmpty()) {

				Messagebox.show("Batch ID is not available.", "Submit Batch", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			List<InwardCheque> batchCheques = inwardChequeService.getChequesByBatchAndStatus(currentBatchId, null);

			int total = 0;
			int verified = 0;

			if (batchCheques != null) {

				total = batchCheques.size();

				for (InwardCheque cheque : batchCheques) {

					if (cheque == null) {
						continue;
					}

					String status = cheque.getChequeStatus();

					if ("ACCEPTED".equalsIgnoreCase(status) || "REJECTED".equalsIgnoreCase(status)) {

						verified++;
					}
				}
			}

			if (total == 0 || verified != total) {

				Messagebox.show("Cannot submit the batch.\n" + "All cheques must be verified first.", "Submit Batch",
						Messagebox.OK, Messagebox.EXCLAMATION);

				return;
			}

			InwardBatch batch = inwardBatchService.getBatchById(currentBatchId);

			if (batch == null) {

				Messagebox.show("Batch not found: " + currentBatchId, "Submit Batch", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			batch.setBatchStatus("COMPLETED");

			boolean updated = inwardBatchService.updateBatch(batch);

			if (!updated) {

				Messagebox.show("Unable to mark batch as COMPLETED.", "Submit Batch", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			if (verificationSummaryWindow != null) {
				verificationSummaryWindow.setVisible(false);
			}
			currentBatchCheques.clear();
			currentChequeId = null;
			currentBatchId = null;
			currentChequeIndex = 0;
			verificationOrderInitialized = false;

			if (btnSubmitVerification != null) {
				btnSubmitVerification.setVisible(false);
			}

			showNoChequesToVerify();

			Messagebox.show(
					"Batch " + batch.getInwardBatchId() + " has been submitted successfully.\n\n"
							+ "Batch status: COMPLETED",
					"Verification Completed", Messagebox.OK, Messagebox.INFORMATION);

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to submit batch.\n" + e.getMessage(), "Submit Batch", Messagebox.OK,
					Messagebox.ERROR);
		}
	}

	private void showNoChequesToVerify() {

		if (verificationNavigation != null) {
			verificationNavigation.setVisible(false);
		}

		if (batchInfoCard != null) {
			batchInfoCard.setVisible(false);
		}

		if (verificationContent != null) {
			verificationContent.setVisible(false);
		}

		if (noChequesContainer != null) {
			noChequesContainer.setVisible(true);
		}

	}

	public void onClick$btnGoToDashboard() {
		Executions.getCurrent().sendRedirect("/inward/checker/dashboard.zul");
	}

	private void enableMouseWheelZoom() {

		if (chequeImage == null) {
			return;
		}

		String imageUuid = chequeImage.getUuid();

		String script = "(function(){" + "var img=document.getElementById('" + imageUuid + "');" + "if(!img) return;"

				+ "if(img.dataset.mouseWheelZoomEnabled==='true') return;" + "img.dataset.mouseWheelZoomEnabled='true';"

				+ "img.addEventListener('wheel', function(e){"

				+ "e.preventDefault();"

				+ "var scale=parseFloat(img.dataset.zoomScale || '1');"

				+ "if(e.deltaY < 0){" + "scale += 0.1;" + "}else{" + "scale -= 0.1;" + "}"

				+ "if(scale < 0.5) scale=0.5;"

				+ "if(scale > 3.0) scale=3.0;"

				+ "img.dataset.zoomScale=scale;"

				+ "var panX=parseFloat(img.dataset.panX || '0');" + "var panY=parseFloat(img.dataset.panY || '0');"

				+ "img.style.transform=" + "'translate('+panX+'px,'+panY+'px) " + "scale('+scale+') " + "rotate("
				+ rotationAngle + "deg)';"

				+ "}, {passive:false});"

				+ "})();";

		Clients.evalJavaScript(script);
	}

}