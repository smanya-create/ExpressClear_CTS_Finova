package com.iispl.cts.controller.inward.maker;

import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.List;

import org.zkoss.image.AImage;
import org.zkoss.zk.ui.Component;
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

import com.iispl.cts.entity.RejectedReason;
import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.enums.inward.InwardChequeStatus;
import com.iispl.cts.service.RejectedReasonService;
import com.iispl.cts.service.inward.InwardBatchService;
import com.iispl.cts.service.inward.InwardChequeService;
import com.iispl.cts.serviceimpl.RejectedReasonServiceImpl;
import com.iispl.cts.serviceimpl.inward.InwardBatchServiceImpl;
import com.iispl.cts.serviceimpl.inward.InwardChequeServiceImpl;

public class InwardDataEntryController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // Database Services
    private final InwardBatchService batchService = new InwardBatchServiceImpl();
    private final InwardChequeService chequeService = new InwardChequeServiceImpl();
    private final RejectedReasonService rejectedReasonService = RejectedReasonServiceImpl.getInstance();

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

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        // Bind modal confirm & cancel buttons directly to ensure execution across ZK child scopes
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

        // 1. Session lookup
        String sessionBatch = (String) Sessions.getCurrent().getAttribute("ACTIVE_INWARD_BATCH_ID");
        if (sessionBatch != null && !sessionBatch.trim().isEmpty()) {
            currentBatchId = sessionBatch.trim();
        } else {
            // 2. URL parameter fallback
            String paramBatch = execution.getParameter("batchId");
            if (paramBatch != null && !paramBatch.trim().isEmpty()) {
                currentBatchId = paramBatch.trim();
            }
        }

        System.out.println("DEBUG: InwardDataEntryController loaded with Batch ID -> " + currentBatchId);

        if (currentBatchId != null) {
            loadBatch(currentBatchId);
        } else {
            System.out.println("DEBUG: InwardDataEntryController loaded without an active batch ID.");
        }
    }

    private void populateRejectedReasons() {
        if (cmbModalRejectionReason == null) return;
        cmbModalRejectionReason.getChildren().clear();

        List<RejectedReason> reasons = rejectedReasonService.getAllRejectedReasons();
        if (reasons != null) {
            for (RejectedReason r : reasons) {
                String label = "[" + r.getRejectedReasonCode() + "] " + r.getRejectedReasonName();
                Comboitem item = new Comboitem(label);
                item.setValue(r.getRejectedReasonCode());
                item.setTooltiptext(r.getRejectedReasonDescription());
                cmbModalRejectionReason.appendChild(item);
            }
        }
    }

    public void loadBatch(String batchId) {
        this.currentBatchId = batchId;

        // Populate Batch Header Data
        InwardBatch batch = batchService.getBatchById(batchId);
        if (batch != null) {
            if (lblBatchId != null) lblBatchId.setValue(batch.getInwardBatchId());
            if (lblTotalCheques != null) lblTotalCheques.setValue(String.valueOf(batch.getActualChequeCount()));
            if (lblReceivedDate != null && batch.getUploadedAt() != null) {
                lblReceivedDate.setValue(new SimpleDateFormat("dd-MM-yyyy").format(batch.getUploadedAt()));
            }
        }

        // Fetch cheques for this batch
        this.activeQueue = chequeService.getChequesByBatchAndStatus(batchId, null);
        this.currentIndex = 0;

        displayCurrentCheque();
        updateProgressBar();
    }

    private void displayCurrentCheque() {
        if (activeQueue == null || activeQueue.isEmpty()) {
            clearForm();
            if (lblChequeNo != null) lblChequeNo.setValue("-");
            if (lblDataStatus != null) {
                lblDataStatus.setValue("NO CHEQUES");
                lblDataStatus.setStyle(null);
            }
            if (lblChequePosition != null) lblChequePosition.setValue("0 of 0");
            updateNavigationState();
            updateProgressBar();
            return;
        }

        if (currentIndex < 0) currentIndex = 0;
        if (currentIndex >= activeQueue.size()) currentIndex = activeQueue.size() - 1;

        InwardCheque item = activeQueue.get(currentIndex);

        if (lblChequeNo != null) lblChequeNo.setValue(item.getChequeNumber() != null ? item.getChequeNumber() : "-");
        if (lblChequePosition != null) lblChequePosition.setValue((currentIndex + 1) + " of " + activeQueue.size());

        if (lblDataStatus != null) {
            String status = item.getChequeStatus();
            if (InwardChequeStatus.DATA_ENTRY_PENDING.name().equalsIgnoreCase(status)) {
                lblDataStatus.setValue("PENDING");
                lblDataStatus.setStyle("background-color: #fef3c7; color: #d97706; border: 1px solid #fde68a;");
            } else if (InwardChequeStatus.DATA_ENTRY_IN_PROGRESS.name().equalsIgnoreCase(status)) {
                lblDataStatus.setValue("IN PROGRESS");
                lblDataStatus.setStyle("background-color: #e0f2fe; color: #0284c7; border: 1px solid #bae6fd;");
            } else if (InwardChequeStatus.SEND_BACK_TO_MAKER.name().equalsIgnoreCase(status)) {
                lblDataStatus.setValue("SENT BACK");
                lblDataStatus.setStyle("background-color: #ffedd5; color: #ea580c; border: 1px solid #fed7aa;");
            } else if (InwardChequeStatus.REJECTION_REQUESTED.name().equalsIgnoreCase(status)) {
                lblDataStatus.setValue("REJECT REQ");
                lblDataStatus.setStyle("background-color: #fee2e2; color: #dc2626; border: 1px solid #fca5a5;");
            } else if (InwardChequeStatus.CHECKER_PROCESSING_PENDING.name().equalsIgnoreCase(status) 
                    || "ACCEPTED".equalsIgnoreCase(status)
                    || "DATA_ENTRY_COMPLETED".equalsIgnoreCase(status)) {
                lblDataStatus.setValue("COMPLETED");
                lblDataStatus.setStyle("background-color: #dcfce7; color: #16a34a; border: 1px solid #bbf7d0;");
            } else {
                lblDataStatus.setValue(status != null ? status : "PENDING");
                lblDataStatus.setStyle(null);
            }
        }

        // Reset viewer to front view
        isViewingFront = true;
        resetImageTransformations();
        updateDisplayedImage(item);

        // Populate Transaction Fields
        if (txtChequeNumber != null) txtChequeNumber.setValue(item.getChequeNumber() != null ? item.getChequeNumber() : "");
        if (txtChequeDate != null) txtChequeDate.setValue(item.getChequeDate() != null ? item.getChequeDate().toString() : "");
        if (txtAmount != null) txtAmount.setValue(item.getChequeAmount() != null ? "₹ " + item.getChequeAmount().toPlainString() : "");
        
        if (txtAmountInWords != null) {
            txtAmountInWords.setValue(convertToIndianCurrencyWords(item.getChequeAmount()));
        }
        
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
            if (btnSubmitToChecker != null) {
                btnSubmitToChecker.setDisabled(true);
            }
            return;
        }

        int total = activeQueue.size();
        long resolvedCount = activeQueue.stream()
                .filter(c -> InwardChequeStatus.CHECKER_PROCESSING_PENDING.name().equalsIgnoreCase(c.getChequeStatus())
                          || InwardChequeStatus.REJECTION_REQUESTED.name().equalsIgnoreCase(c.getChequeStatus())
                          || "ACCEPTED".equalsIgnoreCase(c.getChequeStatus())
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

        // Enable button strictly when 100% of items are resolved
        boolean allResolved = (resolvedCount == total);
        if (btnSubmitToChecker != null) {
            btnSubmitToChecker.setDisabled(!allResolved);
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
        if (imgCheque == null || item == null) return;

        String frontImg = item.getChequeImageFront();
        String backImg = item.getChequeImageBack();
        String rawPath = isViewingFront ? frontImg : backImg;

        if (rawPath != null && !rawPath.trim().isEmpty()) {
            rawPath = rawPath.trim();
            if (rawPath.startsWith("/")) {
                rawPath = rawPath.substring(1);
            }

            // 1. Prefix with Inward-data/ if needed for resources
            String resourcePath = rawPath;
            if (!resourcePath.startsWith("Inward-data/")) {
                resourcePath = "Inward-data/" + resourcePath;
            }

            // 2. Load directly from classpath (src/main/resources)
            InputStream is = Thread.currentThread().getContextClassLoader().getResourceAsStream(resourcePath);
            if (is == null) {
                is = getClass().getClassLoader().getResourceAsStream(resourcePath);
            }

            if (is != null) {
                try {
                    AImage aImage = new AImage(rawPath, is);
                    imgCheque.setContent(aImage);
                } catch (Exception e) {
                    System.err.println("ERROR: Failed to construct AImage from stream: " + e.getMessage());
                    imgCheque.setSrc(null);
                } finally {
                    try { is.close(); } catch (Exception ignored) {}
                }
            } else {
                // 3. Fallback: Direct Linux filesystem read
                String diskPath = "/home/iispl/snap/eclipse/common/git/ExpressClear_CTS_Finova/src/main/resources/" + resourcePath;
                File file = new File(diskPath);

                if (file.exists()) {
                    try {
                        imgCheque.setContent(new AImage(file));
                    } catch (Exception e) {
                        e.printStackTrace();
                        imgCheque.setSrc(null);
                    }
                } else {
                    System.err.println("ERROR: Image could not be located anywhere for cheque: " + item.getChequeNumber());
                    imgCheque.setSrc(null);
                }
            }
        } else {
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
        current.setChequeStatus(InwardChequeStatus.CHECKER_PROCESSING_PENDING.name());

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
        if (activeQueue == null || activeQueue.isEmpty()) return;

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
            Messagebox.show("Please select a valid rejection reason / return code.", "Reason Required", Messagebox.OK, Messagebox.EXCLAMATION);
            if (cmbModalRejectionReason != null) cmbModalRejectionReason.focus();
            return;
        }

        InwardCheque current = activeQueue.get(currentIndex);
        current.setChequeStatus(InwardChequeStatus.REJECTION_REQUESTED.name());

        chequeService.updateChequeDetails(current);

        if (winRejectionModal != null) {
            winRejectionModal.setVisible(false);
        }

        if (currentIndex < activeQueue.size() - 1) {
            currentIndex++;
        }
        displayCurrentCheque();
    }

    public void onClick$btnSubmitToChecker() {
        if (activeQueue == null || activeQueue.isEmpty()) return;

        long accepted = activeQueue.stream().filter(c -> InwardChequeStatus.CHECKER_PROCESSING_PENDING.name().equalsIgnoreCase(c.getChequeStatus()) 
                || "ACCEPTED".equalsIgnoreCase(c.getChequeStatus())
                || "DATA_ENTRY_COMPLETED".equalsIgnoreCase(c.getChequeStatus())).count();
        long rejected = activeQueue.stream().filter(c -> InwardChequeStatus.REJECTION_REQUESTED.name().equalsIgnoreCase(c.getChequeStatus())
                || "REJECTED".equalsIgnoreCase(c.getChequeStatus())).count();

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
        executeSubmitToChecker();
    }

    private void executeSubmitToChecker() {
        if (winCompletionConfirmModal != null) {
            winCompletionConfirmModal.setVisible(false);
        }

        if (currentBatchId != null && !currentBatchId.trim().isEmpty()) {
            batchService.updateBatchStatus(currentBatchId, "CHECKER_PROCESSING_PENDING");
            System.out.println("DEBUG: Submitted batch " + currentBatchId + " to CHECKER_PROCESSING_PENDING");
        }

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
            System.err.println("DEBUG: mainContentArea Include container could not be found!");
        }
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
        if (txtAmountInWords != null) txtAmountInWords.setValue("");
        if (txtDraweeAccount != null) txtDraweeAccount.setValue("");
        if (txtDraweeBankName != null) txtDraweeBankName.setValue("");
        if (txtPayeeName != null) txtPayeeName.setValue("");
        if (txtEntryRemark != null) txtEntryRemark.setValue("");
    }
    
    private static final String[] UNITS = {
        "", "One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine", "Ten",
        "Eleven", "Twelve", "Thirteen", "Fourteen", "Fifteen", "Sixteen", "Seventeen", "Eighteen", "Nineteen"
    };

    private static final String[] TENS = {
        "", "", "Twenty", "Thirty", "Forty", "Fifty", "Sixty", "Seventy", "Eighty", "Ninety"
    };

    public static String convertToIndianCurrencyWords(BigDecimal amount) {
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return "";
        }

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
        if (n == 0) return "Zero";
        if (n < 0) return "Minus " + convertNumberToWords(Math.abs(n));

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
                if ((n % 10) > 0) {
                    sb.append(UNITS[(int) (n % 10)]).append(" ");
                }
            }
        }
        return sb.toString().trim();
    }
    
    public void onChange$txtAmount() {
        if (txtAmount == null || txtAmountInWords == null) return;
        String raw = txtAmount.getValue().replace("₹", "").replace(",", "").trim();
        try {
            BigDecimal val = new BigDecimal(raw);
            txtAmountInWords.setValue(convertToIndianCurrencyWords(val));
        } catch (Exception e) {
            txtAmountInWords.setValue("Invalid Amount");
        }
    }
}