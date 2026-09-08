package com.iispl.cts.controller.inward.checker;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import org.zkoss.zk.ui.select.Selectors;
import org.zkoss.zk.ui.event.Events;

import com.iispl.cts.daoimpl.inward.InwardChequeImageDAOImpl;
import com.iispl.cts.entity.RejectedReason;
import com.iispl.cts.entity.SendBackReason;
import com.iispl.cts.entity.inward.CbsValidationResult;
import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.entity.inward.InwardChequeImage;
import com.iispl.cts.enums.inward.InwardChequeStatus;
import com.iispl.cts.service.RejectedReasonService;
import com.iispl.cts.service.SendBackReasonService;
import com.iispl.cts.service.inward.InwardChequeService;
import com.iispl.cts.serviceimpl.RejectedReasonServiceImpl;
import com.iispl.cts.serviceimpl.SendBackReasonServiceImpl;
import com.iispl.cts.serviceimpl.inward.InwardChequeServiceImpl;
import org.zkoss.zk.ui.select.annotation.Wire;

public class InwardCheckerVerificationController
        extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;
    private Label lblBatchId;
    private Label lblTotalCheques;
    private Label lblChequeNumber;
    private Label lblChequeStatus;
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
    private RejectedReasonService rejectedReasonService =
            RejectedReasonServiceImpl.getInstance();
    private List<InwardCheque> currentBatchCheques = new ArrayList<>();
    private String currentChequeId;
    private String currentBatchId;
    private int currentChequeIndex = 0;
    private Button btnProceedRrf;
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
    private Textbox txtSendBackRemarks;
    
    private InwardChequeImageDAOImpl inwardChequeImageDAO;

    @Override
    public void doAfterCompose(Component comp) throws Exception {

        super.doAfterCompose(comp);

        pageRoot = comp.getPage().getFirstRoot();

        inwardChequeService = new InwardChequeServiceImpl();
        inwardChequeImageDAO =
                InwardChequeImageDAOImpl.getInstance();
        sendBackReasonService =
                new SendBackReasonServiceImpl();

        // Get reject popup
        Window window =
                (Window) pageRoot.getFellow("rejectReasonWindow");

        // Get Proceed button from popup ID space
        Button proceedButton =
                (Button) window.getFellow("btnProceedReject");

        // Get Cancel button from popup ID space
        Button cancelButton =
                (Button) window.getFellow("btnCancelReject");

        // Manually register events because popup has its own ID space
        proceedButton.addEventListener(
                Events.ON_CLICK,
                event -> onClick$btnProceedReject());

        cancelButton.addEventListener(
                Events.ON_CLICK,
                event -> onClick$btnCancelReject());

        System.out.println(
                "INWARD CHECKER VERIFICATION CONTROLLER LOADED");
        List<InwardCheque> cheques =
                inwardChequeService.getChequesByBatchAndStatus(
                        "BAT1001",
                        null);

        if (cheques != null && !cheques.isEmpty()) {

            loadChequeDetails(
                    cheques.get(0).getInwardChequeId());
        }
    }
    private void loadChequeDetails(String inwardChequeId) {

        try {

            InwardCheque cheque =
                    inwardChequeService.findById(inwardChequeId);
          

            if (cheque == null) {

                Messagebox.show(
                        "Cheque not found: " + inwardChequeId,
                        "Verification",
                        Messagebox.OK,
                        Messagebox.EXCLAMATION);

                return;
            }
            
            loadChequeImage(inwardChequeId,"FRONT");
            currentChequeId = cheque.getInwardChequeId();
            currentBatchId = cheque.getInwardBatchId();
            currentBatchCheques =
                    inwardChequeService.getChequesByBatchAndStatus(
                            currentBatchId,
                            null);

            int foundIndex = -1;

            for (int i = 0; i < currentBatchCheques.size(); i++) {

                InwardCheque batchCheque =
                        currentBatchCheques.get(i);

                if (batchCheque.getInwardChequeId()
                        .equalsIgnoreCase(currentChequeId)) {

                    foundIndex = i;
                    break;
                }
            }

            if (foundIndex >= 0) {
                currentChequeIndex = foundIndex;
            }

            if (lblBatchId != null) {
                lblBatchId.setValue(
                        safeValue(cheque.getInwardBatchId()));
            }

            if (lblTotalCheques != null) {

                lblTotalCheques.setValue(
                        String.valueOf(currentBatchCheques.size()));
            }


            if (lblChequeNumber != null) {

                lblChequeNumber.setValue(
                        safeValue(cheque.getChequeNumber()));
            }

            if (lblVerificationChequeNumber != null) {

                lblVerificationChequeNumber.setValue(
                        safeValue(cheque.getChequeNumber()));
            }

            if (lblChequeStatus != null) {

                lblChequeStatus.setValue(
                        safeValue(cheque.getChequeStatus()));
            }

            if (lblReceivedDate != null
                    && cheque.getCreatedAt() != null) {

                lblReceivedDate.setValue(
                        cheque.getCreatedAt().toString());
            }

            updateVerificationCount();

            if (lblMicrCode != null) {

                lblMicrCode.setValue(
                        safeValue(cheque.getMicrCode()));
            }


            lblBankCode.setValue(cheque.getBankCode());
            lblBranchCode.setValue(cheque.getBranchCode());
            lblTransactionCode.setValue(cheque.getTransactionCode());

            if (lblPresentingBank != null) {
                lblPresentingBank.setValue("NPCI");
            }

            if (lblChequeDate != null
                    && cheque.getChequeDate() != null) {

                lblChequeDate.setValue(
                        cheque.getChequeDate().toString());
            }

            if (lblAmount != null
                    && cheque.getChequeAmount() != null) {

                lblAmount.setValue(
                        "₹" + cheque.getChequeAmount().toString());
            }

            if (lblDraweeName != null) {

                lblDraweeName.setValue(
                        safeValue(cheque.getDraweeName()));
            }

            if (lblDraweeAccountNumber != null) {

                lblDraweeAccountNumber.setValue(
                        safeValue(cheque.getDraweeAccountNumber()));
            }

            // Temporary
            if (lblAccountBalance != null) {
                lblAccountBalance.setValue("₹50,000.00");
            }
         // Verification status
         // Verification status
            if (lblVerificationStatus != null) {

                String status = cheque.getChequeStatus();

                if ("ACCEPTED".equalsIgnoreCase(status)
                        || "REJECTED".equalsIgnoreCase(status)) {

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

                } else {

                    lblVerificationStatus.setValue("PENDING");
                    lblVerificationStatus.setSclass("verification-status-badge");
                    lblVerificationStatus.setStyle("");
                }
            }
            updateChequePosition();


        } catch (Exception e) {

            e.printStackTrace();

            Messagebox.show(
                    "Unable to load cheque details.\n"
                            + e.getMessage(),
                    "Verification Error",
                    Messagebox.OK,
                    Messagebox.ERROR);
        }
    }  
    private void loadChequeImage(
            String inwardChequeId,
            String imageType) {

        if (chequeImage != null) {
            chequeImage.setVisible(false);
            chequeImage.setSrc(null);
        }

        if (emptyImageState != null) {
            emptyImageState.setVisible(true);
        }

        if (inwardChequeId == null
                || inwardChequeId.trim().isEmpty()) {
            return;
        }

        InwardChequeImage image = null;

        if ("FRONT".equalsIgnoreCase(imageType)) {

            image = inwardChequeService
                    .getFrontImage(inwardChequeId);

        } else if ("BACK".equalsIgnoreCase(imageType)) {

            image = inwardChequeService
                    .getBackImage(inwardChequeId);
        }

        if (image != null
                && image.getImagePath() != null
                && !image.getImagePath().trim().isEmpty()) {

            String imageSrc =
                    "/" + image.getImagePath();

            chequeImage.setSrc(imageSrc);
            chequeImage.setVisible(true);

            if (emptyImageState != null) {
                emptyImageState.setVisible(false);
            }
        }
    }
    public void onClick$btnFront() {

        if (currentChequeId != null) {

            loadChequeImage(
                    currentChequeId,"FRONT"
                   );
        }
    }
    public void onClick$btnBack() {

        if (currentChequeId != null) {

            loadChequeImage(
                    currentChequeId,"BACK"
                   );
        }
    }

    public void onClick$btnAccept() {

        try {

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

            // -------------------------------------------------
            // CHECK WHETHER CHEQUE IS ALREADY VERIFIED
            // -------------------------------------------------

            String currentStatus = cheque.getChequeStatus();

            if ("ACCEPTED".equalsIgnoreCase(currentStatus)
                    || "REJECTED".equalsIgnoreCase(currentStatus)) {

                Messagebox.show(
                        "This cheque is already verified.",
                        "Verification",
                        Messagebox.OK,
                        Messagebox.EXCLAMATION);

                return;
            }

            // -------------------------------------------------
            // RUN CBS VALIDATION AUTOMATICALLY
            // -------------------------------------------------

            CbsValidationResult cbsResult =
                    runCbsValidation(cheque);

            if (!cbsResult.isPassed()) {
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

                // Cheque Status = REJECTED
                if (lblChequeStatus != null) {

                    lblChequeStatus.setValue("REJECTED");

                    lblChequeStatus.setSclass(
                            "status-badge mismatch");
                }

                // Verification = VERIFIED
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
                        "CBS validation failed.\n"
                        + "Cheque has been rejected.",
                        "CBS Validation Failed",
                        Messagebox.OK,
                        Messagebox.ERROR,
                        event -> moveToNextCheque());

                return;
            }

            // -------------------------------------------------
            // CBS VALIDATION PASSED
            // -------------------------------------------------

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

            // Cheque Status = ACCEPTED
            if (lblChequeStatus != null) {

                lblChequeStatus.setValue("ACCEPTED");

                lblChequeStatus.setSclass(
                        "status-badge");
            }

            // Verification = VERIFIED
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

            // Update 1/4, 2/4, 3/4, 4/4
            updateVerificationCount();

            // -------------------------------------------------
            // SHOW SUCCESS MESSAGE THEN MOVE NEXT
            // -------------------------------------------------

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
    public void onClick$btnReturn() {

        try {

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

            // -------------------------------------------------
            // CHECK WHETHER CHEQUE IS ALREADY VERIFIED
            // -------------------------------------------------

            String currentStatus = cheque.getChequeStatus();

            if ("ACCEPTED".equalsIgnoreCase(currentStatus)
                    || "REJECTED".equalsIgnoreCase(currentStatus)) {

                Messagebox.show(
                        "This cheque is already verified.",
                        "Verification",
                        Messagebox.OK,
                        Messagebox.EXCLAMATION);

                return;
            }

            // -------------------------------------------------
            // OPEN REJECT POPUP
            // -------------------------------------------------

            Window window =
                    (Window) pageRoot.getFellow(
                            "rejectReasonWindow");

            Combobox comboBox =
                    (Combobox) window.getFellow(
                            "cmbRejectedReason");

            Textbox remarks =
                    (Textbox) window.getFellow(
                            "txtRejectRemarks");

            loadRejectedReasons(comboBox);

            comboBox.setSelectedItem(null);

            remarks.setValue("");

            window.doModal();

        } catch (Exception e) {

            e.printStackTrace();

            Messagebox.show(
                    "Unable to open reject window.\n"
                    + e.getMessage(),
                    "Verification Error",
                    Messagebox.OK,
                    Messagebox.ERROR);
        }
    }
    
    
    private void loadRejectedReasons(Combobox comboBox) {

        comboBox.getItems().clear();

        List<RejectedReason> reasons =
                rejectedReasonService.getAllRejectedReasons();

        for (RejectedReason reason : reasons) {

            Comboitem item = new Comboitem();

            item.setLabel(
                    reason.getRejectedReasonCode()
                    + " - "
                    + reason.getRejectedReasonName());

            item.setValue(reason);

            comboBox.appendChild(item);
        }
    }

    
    private void moveToNextCheque() {

        // Refresh the batch data from database
        currentBatchCheques =
                inwardChequeService.getChequesByBatchAndStatus(
                        currentBatchId,
                        null);

        // Move to next cheque
        if (currentChequeIndex < currentBatchCheques.size() - 1) {

            currentChequeIndex++;

            InwardCheque nextCheque =
                    currentBatchCheques.get(currentChequeIndex);

            currentChequeId =
                    nextCheque.getInwardChequeId();

            loadChequeDetails(currentChequeId);

            updateChequePosition();

        } else {

            // Last cheque
            updateChequePosition();

            Messagebox.show(
                    "All cheques in this batch have been verified.",
                    "Verification Completed",
                    Messagebox.OK,
                    Messagebox.INFORMATION);
        }
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

        lblChequePosition.setValue(
                (currentChequeIndex + 1) + "/" + total);


        if (btnPrevious != null) {

            btnPrevious.setDisabled(
                    currentChequeIndex <= 0);
        }

        if (btnNext != null) {

            btnNext.setDisabled(
                    currentChequeIndex >= total - 1);
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

            List<InwardCheque> batchCheques =
                    inwardChequeService.getChequesByBatchAndStatus(
                            currentBatchId,
                            null);

            int total = batchCheques.size();
            int verified = 0;

            for (InwardCheque cheque : batchCheques) {

                String status = cheque.getChequeStatus();

                if (status != null
                        && ("ACCEPTED".equalsIgnoreCase(status)
                        || "REJECTED".equalsIgnoreCase(status))) {

                    verified++;
                }
            }

            lblVerification.setValue(
                    verified + "/" + total);


            // Show Proceed to RRF only when ALL cheques are verified
            if (btnProceedRrf != null) {

                btnProceedRrf.setVisible(
                        total > 0 && verified == total);
            }

            // Show Proceed to RRF only when ALL cheques are verified
            if (btnProceedRrf != null) {

                btnProceedRrf.setVisible(
                        total > 0 && verified == total);
            }

        } catch (Exception e) {

            e.printStackTrace();

            lblVerification.setValue("0/0");

            if (btnProceedRrf != null) {
                btnProceedRrf.setVisible(false);
            }
        }
    }
    public void onClick$btnNext() {

        if (currentBatchCheques == null
                || currentBatchCheques.isEmpty()) {
            return;
        }

        if (currentChequeIndex
                >= currentBatchCheques.size() - 1) {
            return;
        }

        currentChequeIndex++;

        InwardCheque nextCheque =
                currentBatchCheques.get(currentChequeIndex);

        currentChequeId =
                nextCheque.getInwardChequeId();

        loadChequeDetails(currentChequeId);

        updateChequePosition();
    }
    public void onClick$btnPrevious() {

        if (currentBatchCheques == null
                || currentBatchCheques.isEmpty()) {
            return;
        }

        if (currentChequeIndex <= 0) {
            return;
        }

        currentChequeIndex--;

        InwardCheque previousCheque =
                currentBatchCheques.get(currentChequeIndex);

        currentChequeId =
                previousCheque.getInwardChequeId();

        loadChequeDetails(currentChequeId);

        updateChequePosition();
    }
    public void onClick$btnProceedReject() {

        try {

            Window window =
                    (Window) pageRoot.getFellow(
                            "rejectReasonWindow");

            Combobox comboBox =
                    (Combobox) window.getFellow(
                            "cmbRejectedReason");

            Textbox remarksBox =
                    (Textbox) window.getFellow(
                            "txtRejectRemarks");

            // -------------------------------------------------
            // GET SELECTED REASON
            // -------------------------------------------------

            Comboitem selectedItem =
                    comboBox.getSelectedItem();

            if (selectedItem == null) {

                Messagebox.show(
                        "Please select a rejected reason.",
                        "Validation",
                        Messagebox.OK,
                        Messagebox.EXCLAMATION);

                return;
            }

            RejectedReason reason =
                    (RejectedReason) selectedItem.getValue();

            if (reason == null) {

                Messagebox.show(
                        "Invalid rejected reason selected.",
                        "Validation",
                        Messagebox.OK,
                        Messagebox.ERROR);

                return;
            }

            String remarks =
                    remarksBox.getValue();

            if (remarks != null) {
                remarks = remarks.trim();
            }

            // -------------------------------------------------
            // GET CURRENT CHEQUE
            // -------------------------------------------------

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

            // -------------------------------------------------
            // CHECK WHETHER ALREADY VERIFIED
            // -------------------------------------------------

            String currentStatus =
                    cheque.getChequeStatus();

            if ("ACCEPTED".equalsIgnoreCase(currentStatus)
                    || "REJECTED".equalsIgnoreCase(currentStatus)) {

                window.setVisible(false);

                Messagebox.show(
                        "This cheque is already verified.",
                        "Verification",
                        Messagebox.OK,
                        Messagebox.EXCLAMATION);

                return;
            }

            // -------------------------------------------------
            // GET LOGGED-IN USER
            // -------------------------------------------------

            Object userObject =
                    Sessions.getCurrent()
                            .getAttribute("CTS_USERNAME");

            String rejectedBy;

            if (userObject != null) {

                rejectedBy =
                        userObject.toString();

            } else {

                rejectedBy = "Alex";
            }

            // -------------------------------------------------
            // SAVE REJECTION DETAILS
            // -------------------------------------------------

            boolean rejectionSaved =
                    inwardChequeService.saveRejection(
                            cheque.getInwardChequeId(),
                            reason.getRejectedReasonId(),
                            remarks,
                            rejectedBy);

            if (!rejectionSaved) {

                Messagebox.show(
                        "Unable to save rejection details.",
                        "Verification",
                        Messagebox.OK,
                        Messagebox.ERROR);

                return;
            }

            // -------------------------------------------------
            // UPDATE CHEQUE STATUS
            // -------------------------------------------------

            cheque.setChequeStatus("REJECTED");

            boolean chequeUpdated =
                    inwardChequeService.updateChequeDetails(
                            cheque);

            if (!chequeUpdated) {

                Messagebox.show(
                        "Rejection details were saved, "
                        + "but cheque status could not be updated.",
                        "Verification",
                        Messagebox.OK,
                        Messagebox.ERROR);

                return;
            }

            // -------------------------------------------------
            // UPDATE SCREEN
            // -------------------------------------------------

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

            // -------------------------------------------------
            // CLOSE POPUP
            // -------------------------------------------------

            window.setVisible(false);

            comboBox.setSelectedItem(null);

            remarksBox.setValue("");

            // -------------------------------------------------
            // UPDATE VERIFICATION COUNT
            // -------------------------------------------------

            updateVerificationCount();

            // -------------------------------------------------
            // SHOW MESSAGE THEN MOVE NEXT
            // -------------------------------------------------

            Messagebox.show(
                    "Cheque rejected successfully.",
                    "Verification",
                    Messagebox.OK,
                    Messagebox.INFORMATION,
                    event -> moveToNextCheque());

        } catch (Exception e) {

            e.printStackTrace();

            Messagebox.show(
                    "Unable to process rejection.\n"
                    + e.getMessage(),
                    "Verification Error",
                    Messagebox.OK,
                    Messagebox.ERROR);
        }
    }
    
    public void onClick$btnCancelReject() {

        try {

            Window window =
                    (Window) pageRoot.getFellow("rejectReasonWindow");

            Combobox comboBox =
                    (Combobox) window.getFellow("cmbRejectedReason");

            Textbox remarksBox =
                    (Textbox) window.getFellow("txtRejectRemarks");

            comboBox.setSelectedItem(null);
            remarksBox.setValue("");

            window.setVisible(false);

        } catch (Exception e) {

            e.printStackTrace();
        }
    }
    
    private CbsValidationResult runCbsValidation(InwardCheque cheque) {

        if (cheque == null) {
            return new CbsValidationResult(
                    false,
                    "Cheque information is not available.");
        }

        return inwardChequeService.validateCbs(cheque);
    }
    public void onClick$btnSendBack() {

        try {

            Window window =
                    (Window) pageRoot.getFellow(
                            "sendBackReasonWindow");

            Combobox comboBox =
                    (Combobox) window.getFellow(
                            "cmbSendBackReason");

            Textbox remarks =
                    (Textbox) window.getFellow(
                            "txtSendBackRemarks");

            loadSendBackReasons(comboBox);

            comboBox.setSelectedItem(null);
            remarks.setValue("");

            window.doModal();

        } catch (Exception e) {

            e.printStackTrace();

            Messagebox.show(
                    "Unable to open Send Back window.\n"
                    + e.getMessage(),
                    "Send Back Error",
                    Messagebox.OK,
                    Messagebox.ERROR);
        }
    }
    public void onClick$btnProceedSendBack() {

        try {

            Comboitem selectedItem =
                    cmbSendBackReason.getSelectedItem();

            if (selectedItem == null) {

                Messagebox.show(
                        "Please select a send back reason.",
                        "Validation",
                        Messagebox.OK,
                        Messagebox.EXCLAMATION);

                return;
            }

            InwardCheque cheque =
                    inwardChequeService.findById(currentChequeId);

            if (cheque == null) {

                Messagebox.show(
                        "Cheque not found.",
                        "Send Back",
                        Messagebox.OK,
                        Messagebox.ERROR);

                return;
            }

            cheque.setChequeStatus(
                    InwardChequeStatus.SEND_BACK_TO_MAKER.name());

            boolean updated =
                    inwardChequeService.updateChequeDetails(cheque);

            if (!updated) {

                Messagebox.show(
                        "Unable to send the cheque back to Maker.",
                        "Send Back",
                        Messagebox.OK,
                        Messagebox.ERROR);

                return;
            }

            sendBackReasonWindow.setVisible(false);

            Messagebox.show(
                    "Cheque has been sent back to Maker successfully.",
                    "Send Back",
                    Messagebox.OK,
                    Messagebox.INFORMATION,
                    event -> moveToNextCheque());

        } catch (Exception e) {

            e.printStackTrace();

            Messagebox.show(
                    "Unable to process Send Back.\n"
                    + e.getMessage(),
                    "Send Back Error",
                    Messagebox.OK,
                    Messagebox.ERROR);
        }
    }
    public void onClick$btnCancelSendBack() {

        sendBackReasonWindow.setVisible(false);
    }
    private void loadSendBackReasons(Combobox comboBox) {

        comboBox.getItems().clear();

        List<SendBackReason> reasons =
                sendBackReasonService.getAllSendBackReasons();

        for (SendBackReason reason : reasons) {

            Comboitem item = new Comboitem();

            item.setLabel(
                    reason.getReasonCode()
                    + " - "
                    + reason.getReasonName());

            item.setValue(reason);

            comboBox.appendChild(item);
        }
    }
    
}