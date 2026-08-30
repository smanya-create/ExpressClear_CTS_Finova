package com.iispl.cts.controller.inward.maker;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;

public class InwardMicrRepairControllerr extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    private Label lblBatchId;
    private Label lblRecordPosition;
    private Label lblProgress;
    private Label lblRepairStatus;

    private Image chequeImage;
    private Groupbox emptyImageState;

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

    @Override
    public void doAfterCompose(Component comp) throws Exception {

        super.doAfterCompose(comp);

        loadRepairRecord();
    }

    private void loadRepairRecord() {

        /*
         * Database/service integration will be added later.
         * Currently there are no MICR repair records available.
         */

        totalRecords = 0;
        currentRecord = 0;

        clearRecordFields();
        updateNavigation();
        showImagePlaceholder();
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
                lblRecordPosition.setValue(
                    "Record " + currentRecord + " of " + totalRecords
                );
            }
        }

        if (lblProgress != null) {
            lblProgress.setValue(
                currentRecord + " / " + totalRecords
            );
        }

        if (progressMeter != null) {

            int progress = totalRecords == 0
                    ? 0
                    : (currentRecord * 100) / totalRecords;

            progressMeter.setValue(progress);
        }
    }

    private void showImagePlaceholder() {

        if (chequeImage != null) {
            chequeImage.setVisible(false);
            chequeImage.setSrc(null);
        }

        if (emptyImageState != null) {
            emptyImageState.setVisible(true);
        }
    }

    public void onClick$btnPrevious() {

        if (totalRecords == 0 || currentRecord <= 1) {
            return;
        }

        currentRecord--;
        loadRepairRecord();
    }

    public void onClick$btnNext() {

        if (totalRecords == 0 || currentRecord >= totalRecords) {
            return;
        }

        currentRecord++;
        loadRepairRecord();
    }

    public void onClick$btnSaveAndNext() {

        if (totalRecords == 0) {

            Messagebox.show(
                "No MICR repair records are available.",
                "MICR Repair",
                Messagebox.OK,
                Messagebox.INFORMATION
            );

            return;
        }

        String correctedMicr = txtCorrectedMicr != null
                ? txtCorrectedMicr.getValue()
                : "";

        if (correctedMicr == null || correctedMicr.trim().isEmpty()) {

            if (txtCorrectedMicr != null) {
                txtCorrectedMicr.setErrorMessage(
                    "Corrected MICR code is required."
                );
            }

            return;
        }

        /*
         * Database update will be added after
         * the inward MICR repair service is implemented.
         */

        Messagebox.show(
            "MICR correction saved successfully.",
            "MICR Repair",
            Messagebox.OK,
            Messagebox.INFORMATION
        );

        if (currentRecord < totalRecords) {
            currentRecord++;
            loadRepairRecord();
        }
    }

    public void onClick$btnRejectRequest() {

        if (totalRecords == 0) {

            Messagebox.show(
                "No MICR repair record is available to reject.",
                "MICR Repair",
                Messagebox.OK,
                Messagebox.INFORMATION
            );

            return;
        }

        /*
         * Rejection processing will be connected
         * to the inward workflow later.
         */

        Messagebox.show(
            "Reject request action will be connected to the inward workflow.",
            "MICR Repair",
            Messagebox.OK,
            Messagebox.INFORMATION
        );
    }

    public void onClick$btnBackToList() {

        Executions.sendRedirect("/inward/maker/index.zul");
    }
}