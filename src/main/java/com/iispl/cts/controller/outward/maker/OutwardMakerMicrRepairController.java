
package com.iispl.cts.controller.outward.maker;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.service.outward.OutwardMakerService;
import com.iispl.cts.serviceimpl.outward.OutwardMakerServiceImpl;

public class OutwardMakerMicrRepairController extends GenericForwardComposer<Window> {

    private static final long serialVersionUID = 1L;

    // Service
    private final OutwardMakerService outwardMakerService;

    // ZUL components
    @Wire
    private Window micrEntryWindow;

    @Wire
    private Button btnBackToList;

    @Wire
    private Label lblBatchNumber;

    @Wire
    private Div divCompletionMessage;

    @Wire
    private Label lblCompletionTitle;

    @Wire
    private Label lblCompletionText;

    @Wire
    private Div divFormContainer;

    @Wire
    private Image imgCheque;

    @Wire
    private Label lblChequeImageTitle;

    @Wire
    private Label lblBankName;

    @Wire
    private Label lblBranchName;

    @Wire
    private Label lblPayLabel;

    @Wire
    private Label lblPayTo;

    @Wire
    private Label lblAmountWords;

    @Wire
    private Label lblAmountNumeric;

    @Wire
    private Label lblOcrReference;

    @Wire
    private Label lblChequeNote;

    @Wire
    private Label lblProgressText;

    @Wire
    private Div divProgressFill;

    @Wire
    private Label lblMicrError;

    @Wire
    private Label lblChequeNumber;

    @Wire
    private Textbox txtChequeNumber;

    @Wire
    private Label lblCityCode;

    @Wire
    private Textbox txtCityCode;

    @Wire
    private Label lblBankCode;

    @Wire
    private Textbox txtBankCode;

    @Wire
    private Label lblBranchCode;

    @Wire
    private Textbox txtBranchCode;

    @Wire
    private Label lblCurrentMicr;

    @Wire
    private Textbox txtCurrentMicr;

    @Wire
    private Div divRejectRemarks;

    @Wire
    private Label lblRejectRemarks;

    @Wire
    private Textbox txtRemarks;

    @Wire
    private Button btnSaveNext;

    @Wire
    private Button btnRejectRequest;

    // Cheque lists
    private List<ScanCheque> scanChequeList = new ArrayList<>();

    private List<OutwardCheque> outwardChequeList = new ArrayList<>();

    // Current source
    private String source;

    // Current batch
    private String batchId;

    // Current cheque index
    private int currentIndex = 0;

    // Constructor
    public OutwardMakerMicrRepairController() {
        outwardMakerService = new OutwardMakerServiceImpl();
    }

    // After compose
    @Override
    public void doAfterCompose(Window window) throws Exception {

        super.doAfterCompose(window);

        // Get URL parameters
        source = Executions.getCurrent().getParameter("source");
        batchId = Executions.getCurrent().getParameter("amp;batchId");

        System.out.println("MICR REPAIR source = [" + source + "]");
        System.out.println("MICR REPAIR batchId = [" + batchId + "]");

        // Validate source
        if (source == null || source.trim().isEmpty()) {
            showError("MICR repair source is missing.");
            return;
        }

        source = source.trim().toUpperCase();

        if (!source.equals("SCAN") && !source.equals("OUTWARD")) {
            showError("Invalid MICR repair source.");
            return;
        }

        // Validate batch ID
        if (batchId == null || batchId.trim().isEmpty()) {
            showError("Batch ID is missing.");
            return;
        }

        batchId = batchId.trim();

        // Display batch ID
        lblBatchNumber.setValue(batchId);

        // Load MICR repair cheques
        loadMicrRepairCheques();

        // Check whether cheques exist
        if (getTotalCheques() == 0) {
            showError("No MICR repair cheques found for batch " + batchId);
            btnSaveNext.setDisabled(true);
            return;
        }

        // Start with first cheque
        currentIndex = 0;
        loadCurrentCheque();
    }

    // Load MICR repair cheques
    private void loadMicrRepairCheques() {

        if ("SCAN".equals(source)) {

            scanChequeList =
                    outwardMakerService.getScanMicrRepairCheques(batchId);

            if (scanChequeList == null) {
                scanChequeList = new ArrayList<>();
            }

        } else {

            outwardChequeList =
                    outwardMakerService.getOutwardMicrRepairCheques(batchId);

            if (outwardChequeList == null) {
                outwardChequeList = new ArrayList<>();
            }
        }
    }

    // Get total cheques
    private int getTotalCheques() {

        if ("SCAN".equals(source)) {
            return scanChequeList.size();
        }

        return outwardChequeList.size();
    }

    // Load current cheque
    private void loadCurrentCheque() {

        int totalCheques = getTotalCheques();

        if (currentIndex < 0 || currentIndex >= totalCheques) {
            return;
        }

        // Reset reject remarks
        divRejectRemarks.setVisible(false);
        txtRemarks.setValue("");

        // Load SCAN cheque
        if ("SCAN".equals(source)) {

            ScanCheque cheque = scanChequeList.get(currentIndex);
            populateScanCheque(cheque);

        } else {

            // Load OUTWARD cheque
            OutwardCheque cheque = outwardChequeList.get(currentIndex);
            populateOutwardCheque(cheque);
        }

        // Update progress
        updateProgress();
    }

    // Populate SCAN cheque
    private void populateScanCheque(ScanCheque cheque) {

        if (cheque == null) {
            return;
        }

        // Cheque number
        txtChequeNumber.setValue(
                safe(cheque.getChequeNumber()));

        // City / Bank / Branch
        txtCityCode.setValue(
                safe(cheque.getCityCode()));

        txtBankCode.setValue(
                safe(cheque.getBankCode()));

        txtBranchCode.setValue(
                safe(cheque.getBranchCode()));

        // Current MICR
        txtCurrentMicr.setValue(
                safe(cheque.getMicrCode()));

        // Bank / Branch display
        lblBankName.setValue(
                "Bank Code: " + safe(cheque.getBankCode()));

        lblBranchName.setValue(
                "Branch Code: " + safe(cheque.getBranchCode()));

        // Payee
        lblPayTo.setValue(
                safe(cheque.getPayeeName()));

        // Amount
        lblAmountNumeric.setValue(
                formatAmount(cheque.getChequeAmount()));

        lblAmountWords.setValue("");

        // OCR reference
        lblOcrReference.setValue(
                safe(cheque.getMicrCode()));

        // Front image
        loadChequeImage(cheque.getChequeImageFront());
    }

    // Populate OUTWARD cheque
    private void populateOutwardCheque(OutwardCheque cheque) {

        if (cheque == null) {
            return;
        }

        // Cheque number
        txtChequeNumber.setValue(
                safe(cheque.getChequeNumber()));

        // City / Bank / Branch
        txtCityCode.setValue(
                safe(cheque.getCityCode()));

        txtBankCode.setValue(
                safe(cheque.getBankCode()));

        txtBranchCode.setValue(
                safe(cheque.getBranchCode()));

        // Current MICR
        txtCurrentMicr.setValue(
                safe(cheque.getMicrCode()));

        // Bank / Branch display
        lblBankName.setValue(
                "Bank Code: " + safe(cheque.getBankCode()));

        lblBranchName.setValue(
                "Branch Code: " + safe(cheque.getBranchCode()));

        // Payee
        lblPayTo.setValue(
                safe(cheque.getPayeeName()));

        // Amount
        lblAmountNumeric.setValue(
                formatAmount(cheque.getChequeAmount()));

        lblAmountWords.setValue("");

        // OCR reference
        lblOcrReference.setValue(
                safe(cheque.getMicrCode()));
    }

    // Load cheque image
    private void loadChequeImage(String imagePath) {

        if (imagePath == null || imagePath.trim().isEmpty()) {
            imgCheque.setSrc("");
            return;
        }

        imagePath = imagePath.trim();

        if (!imagePath.startsWith("/")) {
            imagePath = "/" + imagePath;
        }

        imgCheque.setSrc(imagePath);
    }

    // Update progress
    private void updateProgress() {

        int totalCheques = getTotalCheques();

        if (totalCheques <= 0) {

            lblProgressText.setValue("Record 0 of 0");
            divProgressFill.setStyle("width: 0%;");

            return;
        }

        int recordNumber = currentIndex + 1;

        // Record number
        lblProgressText.setValue(
                "Record " + recordNumber + " of " + totalCheques);

        // Progress percentage
        double percentage =
                ((double) recordNumber / totalCheques) * 100.0;

        divProgressFill.setStyle(
                "width: " + percentage + "%;");
    }

    // Save and Next
    @Listen("onClick = #btnSaveNext")
    public void saveAndNext() {

        int totalCheques = getTotalCheques();

        // Validate current cheque
        if (currentIndex < 0 || currentIndex >= totalCheques) {
            showError("Invalid cheque selection.");
            return;
        }

        // Validate MICR fields
        if (!validateMicrFields()) {
            return;
        }

        try {

            // Save SCAN cheque
            if ("SCAN".equals(source)) {

                ScanCheque cheque =
                        scanChequeList.get(currentIndex);

                updateScanChequeFromScreen(cheque);

                // Set repaired status
                cheque.setChequeStatus("PENDING_DATA_ENTRY");

                outwardMakerService.saveScanMicrRepair(cheque);

            } else {

                // Save OUTWARD cheque
                OutwardCheque cheque =
                        outwardChequeList.get(currentIndex);

                updateOutwardChequeFromScreen(cheque);

                // Set repaired status
                cheque.setChequeStatus("PENDING_DATA_ENTRY");

                outwardMakerService.saveOutwardMicrRepair(cheque);
            }

            // Check last cheque
            if (currentIndex == totalCheques - 1) {

                Clients.showNotification(
                        "MICR repair completed successfully.",
                        Clients.NOTIFICATION_TYPE_INFO,
                        null,
                        "top_center",
                        2000);

                redirectToMicrRepairView();

                return;
            }

            // Move to next cheque
            currentIndex++;

            loadCurrentCheque();

            Clients.showNotification(
                    "MICR repair saved successfully.",
                    Clients.NOTIFICATION_TYPE_INFO,
                    null,
                    "top_center",
                    1500);

        } catch (Exception e) {

            e.printStackTrace();

            Clients.showNotification(
                    "Failed to save MICR repair: "
                            + safe(e.getMessage()),
                    Clients.NOTIFICATION_TYPE_ERROR,
                    null,
                    "top_center",
                    4000);
        }
    }

    // Update SCAN cheque
    private void updateScanChequeFromScreen(ScanCheque cheque) {

        cheque.setMicrCode(
                txtCurrentMicr.getValue().trim());

        cheque.setCityCode(
                txtCityCode.getValue().trim());

        cheque.setBankCode(
                txtBankCode.getValue().trim());

        cheque.setBranchCode(
                txtBranchCode.getValue().trim());
    }

    // Update OUTWARD cheque
    private void updateOutwardChequeFromScreen(OutwardCheque cheque) {

        cheque.setMicrCode(
                txtCurrentMicr.getValue().trim());

        cheque.setCityCode(
                txtCityCode.getValue().trim());

        cheque.setBankCode(
                txtBankCode.getValue().trim());

        cheque.setBranchCode(
                txtBranchCode.getValue().trim());
    }

    // Validate MICR fields
    private boolean validateMicrFields() {

        String micr = txtCurrentMicr.getValue();
        String city = txtCityCode.getValue();
        String bank = txtBankCode.getValue();
        String branch = txtBranchCode.getValue();

        // MICR
        if (micr == null || micr.trim().isEmpty()) {

            showWarning("MICR Code is required.");
            txtCurrentMicr.setFocus(true);

            return false;
        }

        // City
        if (city == null || city.trim().isEmpty()) {

            showWarning("City Code is required.");
            txtCityCode.setFocus(true);

            return false;
        }

        // Bank
        if (bank == null || bank.trim().isEmpty()) {

            showWarning("Bank Code is required.");
            txtBankCode.setFocus(true);

            return false;
        }

        // Branch
        if (branch == null || branch.trim().isEmpty()) {

            showWarning("Branch Code is required.");
            txtBranchCode.setFocus(true);

            return false;
        }

        return true;
    }

    // Redirect to MICR repair view
    private void redirectToMicrRepairView() {

        Executions.sendRedirect(
                "micr-repair-view.zul");
    }

    // Back to list
    @Listen("onClick = #btnBackToList")
    public void backToList() {

        redirectToMicrRepairView();
    }

    // Reject request
    @Listen("onClick = #btnRejectRequest")
    public void rejectRequest() {

        // Reject functionality will be implemented later
    }

    // Show completion message
    private void showCompletionMessage() {

        divCompletionMessage.setVisible(true);

        lblCompletionText.setValue(
                "All MICR-error cheques in batch "
                        + batchId
                        + " have been repaired.");
    }

    // Format amount
    private String formatAmount(BigDecimal amount) {

        if (amount == null) {
            return "";
        }

        DecimalFormat decimalFormat =
                new DecimalFormat("#,##0.00");

        return "₹ " + decimalFormat.format(amount);
    }

    // Safe string
    private String safe(String value) {

        if (value == null) {
            return "";
        }

        return value;
    }

    // Show error
    private void showError(String message) {

        Clients.showNotification(
                message,
                Clients.NOTIFICATION_TYPE_ERROR,
                null,
                "top_center",
                4000);
    }

    // Show warning
    private void showWarning(String message) {

        Clients.showNotification(
                message,
                Clients.NOTIFICATION_TYPE_WARNING,
                null,
                "top_center",
                3000);
    }
}
