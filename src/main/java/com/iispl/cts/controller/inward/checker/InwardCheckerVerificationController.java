package com.iispl.cts.controller.inward.checker;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
import java.io.File;
import java.io.InputStream;

import org.zkoss.image.AImage;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.annotation.Wire;
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
import com.iispl.cts.serviceimpl.inward.InwardBatchServiceImpl;
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

	private InwardChequeImageDAOImpl inwardChequeImageDAO;

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

		String batchId =
		        Executions.getCurrent()
		                .getParameter("batchId");

		if (batchId != null && !batchId.trim().isEmpty()) {

		    currentBatchId = batchId.trim();

		    Sessions.getCurrent().setAttribute(
		            "ACTIVE_VERIFICATION_BATCH_ID",
		            currentBatchId);
		} else {
		    String sessionBatch =
		            (String) Sessions.getCurrent()
		                    .getAttribute("ACTIVE_VERIFICATION_BATCH_ID");

		    if (sessionBatch != null &&
		            !sessionBatch.trim().isEmpty()) {

		        currentBatchId = sessionBatch.trim();

		    } else {
		        showNoChequesToVerify();
		        return;
		    }
		}
		System.out.println(
		        "Selected Verification Batch: "
		        + currentBatchId
		);
		loadSelectedBatch();
	}	
	private void loadSelectedBatch() {

	    if (currentBatchId == null ||
	            currentBatchId.trim().isEmpty()) {

	        Messagebox.show(
	                "No batch selected for verification.",
	                "Verification",
	                Messagebox.OK,
	                Messagebox.EXCLAMATION
	        );

	        return;
	    }

	    currentBatchCheques =
	            inwardChequeService
	                    .getChequesByBatchAndStatus(
	                            currentBatchId,
	                            null
	                    );

	    if (currentBatchCheques == null ||
	            currentBatchCheques.isEmpty()) {

	        Messagebox.show(
	                "No cheques found for batch: "
	                        + currentBatchId,
	                "Verification",
	                Messagebox.OK,
	                Messagebox.EXCLAMATION
	        );

	        return;
	    }

	    currentChequeIndex = 0;

	    currentChequeId =
	            currentBatchCheques
	                    .get(currentChequeIndex)
	                    .getInwardChequeId();

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
			currentBatchCheques = inwardChequeService.getChequesByBatchAndStatus(currentBatchId, null);

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

			loadMakerRejectionRemarks(cheque.getInwardChequeId());

			if (lblReceivedDate != null && cheque.getCreatedAt() != null) {

				lblReceivedDate.setValue(cheque.getCreatedAt().toString());
			}

			updateVerificationCount();

			if (lblMicrCode != null) {

				lblMicrCode.setValue(safeValue(cheque.getMicrCode()));
			}

			lblBankCode.setValue(cheque.getBankCode());
			lblBranchCode.setValue(cheque.getBranchCode());
			lblTransactionCode.setValue(cheque.getTransactionCode());

			if (lblPresentingBank != null) {
				lblPresentingBank.setValue("NPCI");
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

			// Temporary
			if (lblAccountBalance != null) {
				lblAccountBalance.setValue("₹50,000.00");
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

	private void loadMakerRejectionRemarks(String chequeId) {

		if (makerRejectionRequestSection != null) {
			makerRejectionRequestSection.setVisible(false);
		}

		if (lblMakerRejectionRemarks != null) {
			lblMakerRejectionRemarks.setValue("");
		}

		if (chequeId == null || chequeId.trim().isEmpty()) {
			return;
		}

		String sql = "SELECT remarks " + "FROM inward_cheque_rejection_request " + "WHERE inward_cheque_id = ? "
				+ "AND request_status = 'PENDING' " + "ORDER BY request_id DESC " + "LIMIT 1";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, chequeId);

			try (ResultSet rs = ps.executeQuery()) {

				if (rs.next()) {

					String remarks = rs.getString("remarks");

					if (remarks != null && !remarks.trim().isEmpty()) {

						if (lblMakerRejectionRemarks != null) {
							lblMakerRejectionRemarks.setValue(remarks.trim());
						}

						if (makerRejectionRequestSection != null) {
							makerRejectionRequestSection.setVisible(true);
						}
					}
				}
			}

		} catch (Exception e) {

			e.printStackTrace();

			System.err.println("Unable to load Maker rejection remarks for cheque: " + chequeId);
		}
	}

	private void loadChequeImage(InwardCheque cheque, String imageType) {

	    zoomScale = 1.0;
	    rotationAngle = 0;
	    panX = 0;
	    panY = 0;

	    if (chequeImage != null) {
	        chequeImage.setVisible(false);
	        chequeImage.setSrc(null);
	        chequeImage.setContent((org.zkoss.image.Image)null);
	    }

	    if (emptyImageState != null) {
	        emptyImageState.setVisible(true);
	    }

	    if (cheque == null) {
	        return;
	    }

	    String frontImg = cheque.getChequeImageFront();
	    String backImg = cheque.getChequeImageBack();

	    String rawPath;

	    if ("FRONT".equalsIgnoreCase(imageType)) {
	        rawPath = frontImg;
	    } else {
	        rawPath = backImg;
	    }

	    if (rawPath == null || rawPath.trim().isEmpty()) {
	        System.err.println(
	                "ERROR: No " + imageType
	                + " image found for cheque: "
	                + cheque.getChequeNumber());

	        return;
	    }

	    rawPath = rawPath.trim();

	    if (rawPath.startsWith("/")) {
	        rawPath = rawPath.substring(1);
	    }

	  
	    String resourcePath = rawPath;

	    if (!resourcePath.startsWith("Inward-data/")) {
	        resourcePath = "Inward-data/" + resourcePath;
	    }

	    System.out.println(
	            "DEBUG: Verification image loading");

	    System.out.println(
	            "DEBUG: Cheque = "
	            + cheque.getChequeNumber());

	    System.out.println(
	            "DEBUG: Image type = "
	            + imageType);

	    System.out.println(
	            "DEBUG: Raw image path = "
	            + rawPath);

	    System.out.println(
	            "DEBUG: Resource path = "
	            + resourcePath);

	    InputStream is =
	            Thread.currentThread()
	                    .getContextClassLoader()
	                    .getResourceAsStream(resourcePath);

	    if (is == null) {
	        is = getClass()
	                .getClassLoader()
	                .getResourceAsStream(resourcePath);
	    }

	    if (is != null) {

	        try {

	            AImage aImage =
	                    new AImage(rawPath, is);

	            chequeImage.setContent(aImage);
	            chequeImage.setVisible(true);

	            if (emptyImageState != null) {
	                emptyImageState.setVisible(false);
	            }

	            System.out.println(
	                    "DEBUG: Verification image loaded "
	                    + "successfully from classpath.");

	        } catch (Exception e) {

	            System.err.println(
	                    "ERROR: Failed to construct AImage: "
	                    + e.getMessage());

	            chequeImage.setSrc(null);

	        } finally {

	            try {
	                is.close();
	            } catch (Exception ignored) {
	            }
	        }

	    } else {

	    	String diskPath =
	    	        "/home/administrator/snap/eclipse/common/git/"
	    	        + "ExpressClear_CTS_Finova/"
	    	        + "src/main/resources/"
	    	        + resourcePath;
	        File file = new File(diskPath);

	        System.out.println(
	                "DEBUG: Checking disk image: "
	                + file.getAbsolutePath());

	        if (file.exists()) {

	            try {

	                AImage aImage =
	                        new AImage(file);

	                chequeImage.setContent(aImage);
	                chequeImage.setVisible(true);

	                if (emptyImageState != null) {
	                    emptyImageState.setVisible(false);
	                }

	                System.out.println(
	                        "DEBUG: Verification image loaded "
	                        + "successfully from disk.");

	            } catch (Exception e) {

	                e.printStackTrace();

	                chequeImage.setSrc(null);
	            }

	        } else {

	            System.err.println(
	                    "ERROR: Verification image not found.");

	            System.err.println(
	                    "Cheque = "
	                    + cheque.getChequeNumber());

	            System.err.println(
	                    "Image type = "
	                    + imageType);

	            System.err.println(
	                    "Resource path = "
	                    + resourcePath);

	            System.err.println(
	                    "Disk path = "
	                    + file.getAbsolutePath());

	            chequeImage.setSrc(null);
	        }
	    }

	   
	}
	public void onClick$btnFront() {

	    if (currentChequeId != null) {

	        InwardCheque cheque =
	                inwardChequeService.findById(currentChequeId);

	        if (cheque != null) {
	            loadChequeImage(cheque, "FRONT");
	        }
	    }
	}
	public void onClick$btnBack() {

	    if (currentChequeId != null) {

	        InwardCheque cheque =
	                inwardChequeService.findById(currentChequeId);

	        if (cheque != null) {
	            loadChequeImage(cheque, "BACK");
	        }
	    }
	}
	public void onClick$btnAccept() {

	    try {

	        clearCbsFieldHighlights();

	        InwardCheque cheque =
	                inwardChequeService.findById(currentChequeId);

	        if (cheque == null) {

	            Messagebox.show(
	                    "Cheque not found.",
	                    "Verification",
	                    Messagebox.OK,
	                    Messagebox.ERROR);

	            return;
	        }

	        String currentStatus =
	                cheque.getChequeStatus();

	        // Already verified
	        if ("ACCEPTED".equalsIgnoreCase(currentStatus)
	                || "REJECTED".equalsIgnoreCase(currentStatus)) {

	            Messagebox.show(
	                    "This cheque is already verified.",
	                    "Verification",
	                    Messagebox.OK,
	                    Messagebox.EXCLAMATION);

	            return;
	        }

	        /*
	         * CBS VALIDATION
	         */
	        CbsValidationResult cbsResult =
	                runCbsValidation(cheque);
	        
	        if (!cbsResult.isPassed()) {

	            String failureReason =
	                    cbsResult.getReason();

	            highlightFailedCbsField(failureReason);

	            String cbsReasonId =
	                    getCbsRejectedReasonId();

	            if (cbsReasonId == null) {

	                Messagebox.show(
	                        "CBS validation failed, but "
	                        + "CBS rejection reason is not configured "
	                        + "in rejected_reasons.",
	                        "CBS Validation",
	                        Messagebox.OK,
	                        Messagebox.ERROR);

	                return;
	            }

	            Object userObject =
	                    Sessions.getCurrent()
	                            .getAttribute("CTS_USERNAME");

	            String rejectedBy;

	            if (userObject != null) {
	                rejectedBy = userObject.toString();
	            } else {
	                rejectedBy = "SYSTEM";
	            }

	            boolean rejectionSaved =
	                    inwardChequeService.saveRejection(
	                            cheque.getInwardChequeId(),
	                            cbsReasonId,
	                            failureReason,
	                            rejectedBy);

	            if (!rejectionSaved) {

	                Messagebox.show(
	                        "CBS validation failed, but "
	                        + "rejection details could not be saved.",
	                        "CBS Validation",
	                        Messagebox.OK,
	                        Messagebox.ERROR);

	                return;
	            }

	            cheque.setChequeStatus("REJECTED");

	            boolean updated =
	                    inwardChequeService.updateChequeDetails(
	                            cheque);

	            if (!updated) {

	                Messagebox.show(
	                        "CBS validation failed and "
	                        + "cheque status could not be updated.",
	                        "CBS Validation",
	                        Messagebox.OK,
	                        Messagebox.ERROR);

	                return;
	            }

	          
	            if (lblChequeStatus != null) {

	                lblChequeStatus.setValue("REJECTED");

	                lblChequeStatus.setSclass(
	                        "status-badge mismatch");
	            }

	            if (lblVerificationStatus != null) {

	                lblVerificationStatus.setValue("VERIFIED");

	                lblVerificationStatus.setStyle(
	                        "background:#E8F5E9;"
	                        + "color:#198754;"
	                        + "border:1px solid #198754;"
	                        + "border-radius:12px;"
	                        + "padding:3px 10px;"
	                        + "font-size:11px;"
	                        + "font-weight:600;"
	                );
	            }

	            updateVerificationCount();

	            Messagebox.show(
	                    "CBS validation failed.\n\n"
	                    + "Cheque has been rejected.\n\n"
	                    + "Reason: "
	                    + failureReason,
	                    "CBS Validation Failed",
	                    Messagebox.OK,
	                    Messagebox.ERROR);

	            return;
	        }

	      
	        cheque.setChequeStatus("ACCEPTED");

	        boolean updated =
	                inwardChequeService.updateChequeDetails(
	                        cheque);

	        if (!updated) {

	            Messagebox.show(
	                    "CBS validation passed, "
	                    + "but cheque status could not be updated.",
	                    "Verification",
	                    Messagebox.OK,
	                    Messagebox.ERROR);

	            return;
	        }

	       
	        if (lblChequeStatus != null) {

	            lblChequeStatus.setValue("ACCEPTED");

	            lblChequeStatus.setSclass(
	                    "status-badge");
	        }

	        if (lblVerificationStatus != null) {

	            lblVerificationStatus.setValue("VERIFIED");

	            lblVerificationStatus.setStyle(
	                    "background:#E8F5E9;"
	                    + "color:#198754;"
	                    + "border:1px solid #198754;"
	                    + "border-radius:12px;"
	                    + "padding:3px 10px;"
	                    + "font-size:11px;"
	                    + "font-weight:600;"
	            );
	        }

	        updateVerificationCount();

	        Messagebox.show(
	                "Cheque accepted successfully.",
	                "Verification",
	                Messagebox.OK,
	                Messagebox.INFORMATION,
	                event -> moveToNextCheque());

	    } catch (Exception e) {

	        e.printStackTrace();

	        Messagebox.show(
	                "Unable to complete verification.\n"
	                + e.getMessage(),
	                "Verification Error",
	                Messagebox.OK,
	                Messagebox.ERROR);
	    }
	}	
	private void highlightCbsField(Label field) {

	    if (field == null) {
	        return;
	    }

	    field.setStyle(
	        "background:#FFE5E5;"
	        + "border:1px solid #DC3545;"
	        + "color:#B02A37;"
	        + "padding:2px 5px;"
	        + "border-radius:4px;"
	        + "font-weight:700;"
	    );
	}
	
	private String getCbsRejectedReasonId() {

	    List<RejectedReason> reasons =
	            rejectedReasonService.getAllRejectedReasons();

	    if (reasons == null) {
	        return null;
	    }

	    for (RejectedReason reason : reasons) {

	        if (reason == null) {
	            continue;
	        }

	        if ("CBS_VALIDATION_FAILED".equalsIgnoreCase(
	                reason.getRejectedReasonCode())) {

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

	    } else if (r.contains("account holder")
	            || r.contains("name mismatch")) {

	        highlightCbsField(lblDraweeName);

	    } else if (r.contains("account not found")
	            || r.contains("account is")) {

	        highlightCbsField(lblDraweeAccountNumber);

	    } else if (r.contains("cheque number")) {

	        highlightCbsField(lblVerificationChequeNumber);

	    } else if (r.contains("cheque date")
	            || r.contains("postdated")) {

	        highlightCbsField(lblChequeDate);

	    } else if (r.contains("balance")
	            || r.contains("insufficient")) {

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

	    List<InwardCheque> allCheques =
	            inwardChequeService.getChequesByBatchAndStatus(
	                    currentBatchId,
	                    null);

	    if (allCheques == null || allCheques.isEmpty()) {
	        return;
	    }

	    int processedIndex = -1;

	    for (int i = 0; i < allCheques.size(); i++) {

	        InwardCheque cheque = allCheques.get(i);

	        if (cheque != null
	                && cheque.getInwardChequeId() != null
	                && cheque.getInwardChequeId()
	                        .equalsIgnoreCase(currentChequeId)) {

	            processedIndex = i;
	            break;
	        }
	    }

	    if (processedIndex >= 0
	            && processedIndex < allCheques.size() - 1) {

	        currentBatchCheques = allCheques;

	        currentChequeIndex = processedIndex + 1;

	        InwardCheque nextCheque =
	                currentBatchCheques.get(currentChequeIndex);

	        currentChequeId =
	                nextCheque.getInwardChequeId();

	        loadChequeDetails(currentChequeId);

	        updateChequePosition();

	        return;
	    }

	    currentBatchCheques = allCheques;

	    currentChequeIndex =
	            allCheques.size() - 1;

	    updateChequePosition();

	    if (areAllChequesVerified()) {

	        Messagebox.show(
	                "All cheques in this batch have been verified.",
	                "Verification Completed",
	                Messagebox.OK,
	                Messagebox.INFORMATION);
	    }
	}	
	private boolean areAllChequesVerified() {

	    if (currentBatchId == null ||
	            currentBatchId.trim().isEmpty()) {

	        return false;
	    }

	    List<InwardCheque> batchCheques =
	            inwardChequeService.getChequesByBatchAndStatus(
	                    currentBatchId,
	                    null);

	    if (batchCheques == null ||
	            batchCheques.isEmpty()) {

	        return false;
	    }

	    for (InwardCheque cheque : batchCheques) {

	        if (cheque == null) {
	            continue;
	        }

	        String status = cheque.getChequeStatus();

	        // A cheque is verified only when it is
	        // ACCEPTED or REJECTED.
	        if (!"ACCEPTED".equalsIgnoreCase(status)
	                && !"REJECTED".equalsIgnoreCase(status)) {

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

			// Already verified cheques cannot be sent back
			String status = cheque.getChequeStatus();

			if ("ACCEPTED".equalsIgnoreCase(status) || "REJECTED".equalsIgnoreCase(status)) {

				Messagebox.show(
						"This cheque is already verified.\n"
								+ "Accepted or rejected cheques cannot be sent back to Maker.",
						"Send Back", Messagebox.OK, Messagebox.EXCLAMATION);

				return;
			}

			// Existing code continues here
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

		// Minimum zoom
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

				// Disable mouse dragging
				+ "img.style.cursor='default';"

				+ "})();";

		org.zkoss.zk.ui.util.Clients.evalJavaScript(script);

		enableKeyboardImageNavigation();
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

			// Set summary values
			if (lblSummaryTotal != null) {
				lblSummaryTotal.setValue(String.valueOf(total));
			}

			if (lblSummaryAccepted != null) {
				lblSummaryAccepted.setValue(String.valueOf(accepted));
			}

			if (lblSummaryRejected != null) {
				lblSummaryRejected.setValue(String.valueOf(rejected));
			}

			// Open summary popup
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

			// Close summary
			if (verificationSummaryWindow != null) {
				verificationSummaryWindow.setVisible(false);
			}
			currentBatchCheques.clear();
			currentChequeId = null;
			currentBatchId = null;
			currentChequeIndex = 0;

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
	

}