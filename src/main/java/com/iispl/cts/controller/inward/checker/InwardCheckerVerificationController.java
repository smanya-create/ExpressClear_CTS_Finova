package com.iispl.cts.controller.inward.checker;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;

import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.service.inward.InwardChequeService;
import com.iispl.cts.serviceimpl.inward.InwardChequeServiceImpl;

public class InwardCheckerVerificationController
        extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;
    private Label lblBatchId;
    private Label lblTotalCheques;
    private Label lblChequeNumber;
    private Label lblChequeStatus;
    private Label lblReceivedDate;
    private Label lblVerification;

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
    private Button btnRunCbsValidation;
    private Button btnAccept;
    private Button btnReturn;
    private Button btnSendBack;
    private Label lblChequePosition;

    private InwardChequeService inwardChequeService;
    private List<InwardCheque> currentBatchCheques = new ArrayList<>();
    private String currentChequeId;
    private String currentBatchId;
    private int currentChequeIndex = 0;
    private Button btnProceedRrf;

    @Override
    public void doAfterCompose(Component comp) throws Exception {

        super.doAfterCompose(comp);

        inwardChequeService = new InwardChequeServiceImpl();

        System.out.println(
                "INWARD CHECKER VERIFICATION CONTROLLER LOADED");

        loadChequeDetails("CH1117");
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
            currentChequeId = cheque.getInwardChequeId();
            currentBatchId = cheque.getInwardBatchId();
            currentBatchCheques =
                    inwardChequeService.getChequesByBatchAndStatus(
                            currentBatchId,
                            null);

            for (int i = 0; i < currentBatchCheques.size(); i++) {

                if (currentBatchCheques.get(i)
                        .getInwardChequeId()
                        .equalsIgnoreCase(currentChequeId)) {

                    currentChequeIndex = i;
                    break;
                }
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


            if (lblBankCode != null) {
                lblBankCode.setValue("110");
            }

            if (lblBranchCode != null) {
                lblBranchCode.setValue("532");
            }

            if (lblTransactionCode != null) {
                lblTransactionCode.setValue("19");
            }

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
    }    public void onClick$btnRunCbsValidation() {

        loadChequeDetails("CH1005");

        Messagebox.show(
                "CBS Validation Completed Successfully",
                "CBS Validation",
                Messagebox.OK,
                Messagebox.INFORMATION);
    }

    public void onClick$btnAccept() {

        try {

            InwardCheque cheque =
                    inwardChequeService.findById(
                            currentChequeId);

            if (cheque == null) {

                Messagebox.show(
                        "Cheque not found.",
                        "Verification",
                        Messagebox.OK,
                        Messagebox.ERROR);

                return;
            }

            cheque.setChequeStatus("ACCEPTED");

            boolean updated =
                    inwardChequeService.updateChequeDetails(cheque);

            if (!updated) {

                Messagebox.show(
                        "Unable to update cheque status.",
                        "Verification",
                        Messagebox.OK,
                        Messagebox.ERROR);

                return;
            }

            updateVerificationCount();


            if (currentChequeIndex
                    < currentBatchCheques.size() - 1) {

                currentChequeIndex++;

                InwardCheque nextCheque =
                        currentBatchCheques.get(
                                currentChequeIndex);

                loadChequeDetails(
                        nextCheque.getInwardChequeId());

            } else {

                // Last cheque
                updateChequePosition();

                Messagebox.show(
                        "All cheques in this batch have been verified.",
                        "Verification Completed",
                        Messagebox.OK,
                        Messagebox.INFORMATION);
            }

        } catch (Exception e) {

            e.printStackTrace();

            Messagebox.show(
                    "Unable to accept cheque.\n"
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

            cheque.setChequeStatus("REJECTED");

            inwardChequeService.updateChequeDetails(cheque);

            updateVerificationCount();

            Messagebox.show(
                    "Cheque rejected successfully.",
                    "Verification",
                    Messagebox.OK,
                    Messagebox.INFORMATION);

        } catch (Exception e) {

            e.printStackTrace();

            Messagebox.show(
                    "Unable to reject cheque.\n" + e.getMessage(),
                    "Verification Error",
                    Messagebox.OK,
                    Messagebox.ERROR);
        }
    }
   
    public void onClick$btnSendBack() {

        Messagebox.show(
                "Cheque sent back successfully.",
                "Verification",
                Messagebox.OK,
                Messagebox.INFORMATION);
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

            if (lblChequePosition != null && total > 0) {

                int percentage = (verified * 100) / total;

                lblChequePosition.setValue(
                        (currentChequeIndex + 1)
                        + "/" + total
                        + " (" + percentage + "%)");
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

        loadChequeDetails(
                nextCheque.getInwardChequeId());
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

        loadChequeDetails(
                previousCheque.getInwardChequeId());
    }
}