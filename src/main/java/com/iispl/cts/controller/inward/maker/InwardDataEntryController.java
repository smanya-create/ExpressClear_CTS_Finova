package com.iispl.cts.controller.inward.maker;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;

import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.service.inward.InwardBatchService;
import com.iispl.cts.service.inward.InwardChequeService;
import com.iispl.cts.serviceimpl.inward.InwardBatchServiceImpl;
import com.iispl.cts.serviceimpl.inward.InwardChequeServiceImpl;

public class InwardDataEntryController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // Services
    private final InwardBatchService batchService = new InwardBatchServiceImpl();
    private final InwardChequeService chequeService = new InwardChequeServiceImpl();

    // Top Metadata Card Labels
    private Label lblBatchId;
    private Label lblSource;
    private Label lblTotalCheques;
    private Label lblChequeNo;
    private Label lblDataStatus;
    private Label lblReceivedDate;

    // Navigation, Progress & Dynamic Proceed Button
    private Progressmeter pmBatchProgress;
    private Label lblProgressText;
    private Label lblChequePosition;
    private Button btnPrevCheque;
    private Button btnNextCheque;
    private Button btnProceedToCompletion;

    // Viewer Controls
    private Image imgCheque;
    private Button btnViewFront;
    private Button btnViewBack;
    private Button btnZoomIn;
    private Button btnZoomOut;
    private Button btnZoomFit;
    private Button btnRotate;

    // Form Controls
    private Textbox txtChequeNumber;
    private Textbox txtChequeDate;
    private Textbox txtAmount;
    private Textbox txtDraweeAccount;
    private Textbox txtDraweeBankName;
    private Textbox txtPayeeName;
    private Textbox txtEntryRemark;

    // Action Buttons
    private Button btnCancel;
    private Button btnRequestRejection;
    private Button btnApproveCheque;

    // Rejection Modal Controls
    private Div winRejectionModal;
    private Combobox cmbModalRejectionReason;
    private Textbox txtModalRejectionRemark;
    private Button btnCancelModalReject;
    private Button btnConfirmModalReject;

    // Completion Confirmation Modal Controls
    private Div winCompletionConfirmModal;
    private Label lblModalTotal;
    private Label lblModalAccepted;
    private Label lblModalRejected;
    private Button btnCancelCompletionModal;
    private Button btnConfirmCompletionModal;

    // Image Mapping
    private static final Map<String, String[]> IMAGE_MAP = new HashMap<>();
    static {
        IMAGE_MAP.put("CH1005", new String[]{"/Batch1001-images/cheque001_front.png", "/Batch1001-images/cheque001_back.png"});
        IMAGE_MAP.put("CH1006", new String[]{"/Batch1001-images/cheque002_front.png", "/Batch1001-images/cheque002_back.png"});
        IMAGE_MAP.put("CH1007", new String[]{"/Batch1002-images/cheque004_front.png", "/Batch1002-images/cheque004_back.png"});
        IMAGE_MAP.put("CH1008", new String[]{"/Batch1002-images/cheque005_front.png", "/Batch1002-images/cheque005_back.png"});
        IMAGE_MAP.put("CH1009", new String[]{"/Batch1002-images/cheque006_front.png", "/Batch1002-images/cheque006_back.png"});
    }

    private List<InwardCheque> activeQueue;
    private int currentIndex = 0;
    private String currentBatchId = "BAT1001";

    // Viewer transformation state
    private boolean isViewingFront = true;
    private int zoomLevel = 100;
    private int rotationAngle = 0;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        String paramBatch = execution.getParameter("batchId");
        if (paramBatch != null && !paramBatch.trim().isEmpty()) {
            currentBatchId = paramBatch.trim();
        }

        loadBatch(currentBatchId);
    }

    public void loadBatch(String batchId) {
        this.currentBatchId = batchId;

        // 1. Populate Batch Header
        InwardBatch batch = batchService.getBatchById(batchId);
        if (batch != null) {
            if (lblBatchId != null) lblBatchId.setValue(batch.getInwardBatchId());
            if (lblTotalCheques != null) lblTotalCheques.setValue(String.valueOf(batch.getActualChequeCount()));
            if (lblReceivedDate != null && batch.getUploadedAt() != null) {
                lblReceivedDate.setValue(new SimpleDateFormat("dd-MM-yyyy").format(batch.getUploadedAt()));
            }
        }

        // 2. Fetch all cheques in batch
        this.activeQueue = chequeService.getChequesByBatchAndStatus(batchId, null);
        this.currentIndex = 0;

        displayCurrentCheque();
        updateProgressBar();
    }

    private void displayCurrentCheque() {
        if (activeQueue == null || activeQueue.isEmpty()) {
            clearForm();
            if (lblChequeNo != null) lblChequeNo.setValue("-");
            if (lblDataStatus != null) lblDataStatus.setValue("NO CHEQUES");
            if (lblChequePosition != null) lblChequePosition.setValue("0 of 0");
            updateNavigationState();
            updateProgressBar();
            return;
        }

        if (currentIndex < 0) currentIndex = 0;
        if (currentIndex >= activeQueue.size()) currentIndex = activeQueue.size() - 1;

        InwardCheque item = activeQueue.get(currentIndex);

        // Header and position indicators
        if (lblChequeNo != null) lblChequeNo.setValue(item.getChequeNumber());
        if (lblChequePosition != null) lblChequePosition.setValue((currentIndex + 1) + " of " + activeQueue.size());

        if (lblDataStatus != null) {
            String status = item.getChequeStatus();
            if ("PENDING_DATA_ENTRY".equalsIgnoreCase(status)) {
                lblDataStatus.setValue("PENDING");
            } else if ("DATA_ENTRY_COMPLETED".equalsIgnoreCase(status)) {
                lblDataStatus.setValue("COMPLETED");
            } else {
                lblDataStatus.setValue(status != null ? status : "PENDING");
            }
        }

        // Reset viewer to front view
        isViewingFront = true;
        resetImageTransformations();
        updateDisplayedImage(item);

        // Form Fields
        if (txtChequeNumber != null) txtChequeNumber.setValue(item.getChequeNumber() != null ? item.getChequeNumber() : "");
        if (txtChequeDate != null) txtChequeDate.setValue(item.getChequeDate() != null ? item.getChequeDate().toString() : "");
        if (txtAmount != null) txtAmount.setValue(item.getChequeAmount() != null ? "₹ " + item.getChequeAmount().toPlainString() : "");
        if (txtDraweeAccount != null) txtDraweeAccount.setValue(item.getDraweeAccountNumber() != null ? item.getDraweeAccountNumber() : "");
        if (txtDraweeBankName != null) txtDraweeBankName.setValue(item.getDraweeName() != null ? item.getDraweeName() : "");
        if (txtPayeeName != null) txtPayeeName.setValue(item.getPayeeName() != null ? item.getPayeeName() : "");
        if (txtEntryRemark != null) txtEntryRemark.setValue("");

        updateNavigationState();
        updateProgressBar();
    }

    private void updateProgressBar() {
        if (activeQueue == null || activeQueue.isEmpty()) {
            if (pmBatchProgress != null) pmBatchProgress.setValue(0);
            if (lblProgressText != null) lblProgressText.setValue("0/0 (0%)");
            if (btnProceedToCompletion != null) btnProceedToCompletion.setVisible(false);
            return;
        }

        int total = activeQueue.size();
        long resolvedCount = activeQueue.stream()
                .filter(c -> "ACCEPTED".equalsIgnoreCase(c.getChequeStatus()) 
                          || "REJECTED".equalsIgnoreCase(c.getChequeStatus())
                          || "DATA_ENTRY_COMPLETED".equalsIgnoreCase(c.getChequeStatus()))
                .count();

        int percentage = (int) Math.round(((double) resolvedCount / total) * 100);

        if (pmBatchProgress != null) {
            pmBatchProgress.setValue(percentage);
        }
        if (lblProgressText != null) {
            lblProgressText.setValue(resolvedCount + "/" + total + " (" + percentage + "%)");
        }

        // Reveal the "Proceed with Maker Completion" button once all are resolved
        boolean allResolved = (resolvedCount == total);
        if (btnProceedToCompletion != null) {
            btnProceedToCompletion.setVisible(allResolved);
        }
    }

    private void updateNavigationState() {
        if (btnPrevCheque != null) {
            btnPrevCheque.setDisabled(activeQueue == null || currentIndex <= 0);
        }
        if (btnNextCheque != null) {
            btnNextCheque.setDisabled(activeQueue == null || currentIndex >= activeQueue.size() - 1);
        }
    }

    private void saveCurrentChequeStateSilently() {
        if (activeQueue == null || activeQueue.isEmpty() || currentIndex >= activeQueue.size()) return;

        InwardCheque current = activeQueue.get(currentIndex);

        if (txtChequeNumber != null) current.setChequeNumber(txtChequeNumber.getValue().trim());
        if (txtDraweeAccount != null) current.setDraweeAccountNumber(txtDraweeAccount.getValue().trim());
        if (txtDraweeBankName != null) current.setDraweeName(txtDraweeBankName.getValue().trim());
        if (txtPayeeName != null) current.setPayeeName(txtPayeeName.getValue().trim());

        if (txtAmount != null && !txtAmount.getValue().trim().isEmpty()) {
            String rawAmount = txtAmount.getValue().replace("₹", "").replace(",", "").trim();
            try {
                current.setChequeAmount(new BigDecimal(rawAmount));
            } catch (Exception ignored) {}
        }

        if (txtChequeDate != null && !txtChequeDate.getValue().trim().isEmpty()) {
            try {
                current.setChequeDate(Date.valueOf(txtChequeDate.getValue().trim()));
            } catch (Exception ignored) {}
        }

        chequeService.updateChequeDetails(current);
    }

    // --- NAVIGATION CONTROLS ---
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

    // --- IMAGE VIEWER CONTROLS ---
    private void updateDisplayedImage(InwardCheque item) {
        if (imgCheque == null) return;
        String[] paths = IMAGE_MAP.get(item.getInwardChequeId());
        if (paths != null) {
            imgCheque.setSrc(isViewingFront ? paths[0] : paths[1]);
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
            imgCheque.setStyle("width: " + zoomLevel + "%; transform: rotate(" + rotationAngle + "deg); transition: transform 0.2s, width 0.2s; object-fit: contain;");
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

    // --- FORM ACTIONS ---
    public void onClick$btnApproveCheque() {
        if (activeQueue == null || activeQueue.isEmpty()) return;

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
        current.setChequeStatus("ACCEPTED");

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

    // --- REJECTION MODAL CONTROLS ---
    public void onClick$btnRequestRejection() {
        if (activeQueue == null || activeQueue.isEmpty()) return;

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
            Messagebox.show("Please select a valid rejection reason / return code.", "Reason Required", Messagebox.OK, Messagebox.EXCLAMATION);
            if (cmbModalRejectionReason != null) cmbModalRejectionReason.focus();
            return;
        }

        InwardCheque current = activeQueue.get(currentIndex);
        current.setChequeStatus("REJECTED");

        chequeService.updateChequeDetails(current);

        if (winRejectionModal != null) {
            winRejectionModal.setVisible(false);
        }

        if (currentIndex < activeQueue.size() - 1) {
            currentIndex++;
        }
        displayCurrentCheque();
    }

    // --- MOVE TO MAKER COMPLETION CONTROLS ---
    public void onClick$btnProceedToCompletion() {
        if (activeQueue == null || activeQueue.isEmpty()) return;

        long accepted = activeQueue.stream().filter(c -> "ACCEPTED".equalsIgnoreCase(c.getChequeStatus())).count();
        long rejected = activeQueue.stream().filter(c -> "REJECTED".equalsIgnoreCase(c.getChequeStatus())).count();

        if (lblModalTotal != null) lblModalTotal.setValue(String.valueOf(activeQueue.size()));
        if (lblModalAccepted != null) lblModalAccepted.setValue(String.valueOf(accepted));
        if (lblModalRejected != null) lblModalRejected.setValue(String.valueOf(rejected));

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
        if (winCompletionConfirmModal != null) {
            winCompletionConfirmModal.setVisible(false);
        }

        // 1. Update Batch status to READY_FOR_COMPLETION
        batchService.updateBatchStatus(currentBatchId, "READY_FOR_COMPLETION");

        // 2. Transfer operator to Maker Completion module
        Executions.sendRedirect("/inward/maker/maker-completion.zul?batchId=" + currentBatchId);
    }

    private String validateFormFields() {
        String chqNo = txtChequeNumber != null ? txtChequeNumber.getValue().trim() : "";
        if (chqNo.isEmpty()) return "Cheque Number is mandatory.";
        if (!chqNo.matches("\\d{6}")) return "Cheque Number must be exactly 6 digits.";

        String dtStr = txtChequeDate != null ? txtChequeDate.getValue().trim() : "";
        if (dtStr.isEmpty()) return "Cheque Date is mandatory.";
        try {
            LocalDate.parse(dtStr);
        } catch (Exception e) {
            return "Invalid Cheque Date format. Expected format: YYYY-MM-DD.";
        }

        String amtStr = txtAmount != null ? txtAmount.getValue().replace("₹", "").replace(",", "").trim() : "";
        if (amtStr.isEmpty()) return "Cheque Amount is mandatory.";
        try {
            BigDecimal amt = new BigDecimal(amtStr);
            if (amt.compareTo(BigDecimal.ZERO) <= 0) return "Cheque Amount must be greater than zero.";
        } catch (Exception e) {
            return "Invalid numerical amount.";
        }

        String drwAcc = txtDraweeAccount != null ? txtDraweeAccount.getValue().trim() : "";
        if (drwAcc.isEmpty()) return "Drawee Account Number is mandatory.";

        return null;
    }

    private void clearForm() {
        if (imgCheque != null) imgCheque.setSrc(null);
        if (txtChequeNumber != null) txtChequeNumber.setValue("");
        if (txtChequeDate != null) txtChequeDate.setValue("");
        if (txtAmount != null) txtAmount.setValue("");
        if (txtDraweeAccount != null) txtDraweeAccount.setValue("");
        if (txtDraweeBankName != null) txtDraweeBankName.setValue("");
        if (txtPayeeName != null) txtPayeeName.setValue("");
        if (txtEntryRemark != null) txtEntryRemark.setValue("");
    }
}