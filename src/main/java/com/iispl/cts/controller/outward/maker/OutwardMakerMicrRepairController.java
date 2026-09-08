package com.iispl.cts.controller.outward.maker;

import java.lang.reflect.Method;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.service.outward.OutwardMakerService;
import com.iispl.cts.serviceimpl.outward.OutwardMakerServiceImpl;

public class OutwardMakerMicrRepairController extends GenericForwardComposer<Window> {

    private static final long serialVersionUID = 1L;

    private final OutwardMakerService outwardMakerService;

    // Main components

    @Wire
    private Window micrEntryWindow;

    @Wire
    private Button btnBackToList;

    @Wire
    private Label lblBatchNumber;

    @Wire
    private Label lblTotalCheques;

    @Wire
    private Label lblCompletedCheques;

    @Wire
    private Label lblRemainingCheques;

    @Wire
    private Div divCompletionMessage;

    @Wire
    private Label lblCompletionTitle;

    @Wire
    private Label lblCompletionText;

    @Wire
    private Div divFormContainer;

    // Image components

    @Wire
    private Image imgCheque;

    @Wire
    private Label lblChequeImageTitle;

    @Wire
    private Label lblRecordPosition;

    @Wire
    private Label lblChequeNavigation;

    @Wire
    private Button btnZoom;

    @Wire
    private Button btnRotate;

    @Wire
    private Button btnFront;

    @Wire
    private Button btnBack;

    @Wire
    private Button btnPrevious;

    @Wire
    private Button btnNext;

    // Cheque fields

    @Wire
    private Textbox txtChequeNumber;

    @Wire
    private Textbox txtCityCode;

    @Wire
    private Textbox txtBankCode;

    @Wire
    private Textbox txtBranchCode;

    @Wire
    private Textbox txtCurrentMicr;

    @Wire
    private Textbox txtCorrectedMicr;

    @Wire
    private Textbox txtBankName;

    @Wire
    private Textbox txtBranchName;

    @Wire
    private Textbox txtPayTo;

    @Wire
    private Textbox txtAmount;

    @Wire
    private Textbox txtRemarks;

    @Wire
    private Div divRejectRemarks;

    @Wire
    private Label lblRejectRemarks;

    @Wire
    private Label lblMicrError;

    // Progress

    @Wire
    private Label lblProgressText;

    @Wire
    private Div divProgressFill;

    // Actions

    @Wire
    private Button btnSaveNext;

    @Wire
    private Button btnRejectRequest;

    // Data

    private List<ScanCheque> scanChequeList = new ArrayList<>();

    private List<OutwardCheque> outwardChequeList = new ArrayList<>();

    private String source;

    private String batchId;

    private int currentIndex = 0;

    // Image state

    private boolean showingBackImage = false;

    private double zoomLevel = 1.0;

    private int rotation = 0;

    private String frontImagePath;

    private String backImagePath;

    public OutwardMakerMicrRepairController() {
        outwardMakerService = new OutwardMakerServiceImpl();
    }

    @Override
    public void doAfterCompose(Window window) throws Exception {

        super.doAfterCompose(window);

        loadParameters(window);

        if (source == null || source.trim().isEmpty()) {
            showError("MICR repair source is missing.");
            return;
        }

        source = source.trim().toUpperCase();

        if (!"SCAN".equals(source) && !"OUTWARD".equals(source)) {
            showError("Invalid MICR repair source.");
            return;
        }

        if (batchId == null || batchId.trim().isEmpty()) {
            showError("Batch ID is missing.");
            return;
        }

        batchId = batchId.trim();

        lblBatchNumber.setValue(batchId);

        loadMicrRepairCheques();

        int totalCheques = getTotalCheques();

        if (totalCheques == 0) {

            showError(
                    "No MICR repair cheques found for batch "
                            + batchId);

            btnSaveNext.setDisabled(true);
            btnPrevious.setDisabled(true);
            btnNext.setDisabled(true);

            return;
        }

        currentIndex = 0;

        loadCurrentCheque();
    }

    private void loadParameters(Window window) {

        Component parent = window.getParent();

        while (parent != null && !(parent instanceof Include)) {
            parent = parent.getParent();
        }

        if (parent instanceof Include) {

            Include include = (Include) parent;

            Object sourceAttribute =
                    include.getAttribute("MICR_REPAIR_SOURCE");

            Object batchAttribute =
                    include.getAttribute("MICR_REPAIR_BATCH_ID");

            if (sourceAttribute != null) {
                source = sourceAttribute.toString();
            }

            if (batchAttribute != null) {
                batchId = batchAttribute.toString();
            }
        }

        // Fallback for direct URL loading

        if (source == null || source.trim().isEmpty()) {
            source = Executions.getCurrent().getParameter("source");
        }

        if (batchId == null || batchId.trim().isEmpty()) {
            batchId = Executions.getCurrent().getParameter("batchId");
        }

        System.out.println(
                "MICR REPAIR source = [" + source + "]");

        System.out.println(
                "MICR REPAIR batchId = [" + batchId + "]");
    }

    private void loadMicrRepairCheques() {

        if ("SCAN".equals(source)) {

            scanChequeList =
                    outwardMakerService
                            .getScanMicrRepairCheques(batchId);

            if (scanChequeList == null) {
                scanChequeList = new ArrayList<>();
            }

        } else {

            outwardChequeList =
                    outwardMakerService
                            .getOutwardMicrRepairCheques(batchId);

            if (outwardChequeList == null) {
                outwardChequeList = new ArrayList<>();
            }
        }
    }

    private int getTotalCheques() {

        if ("SCAN".equals(source)) {
            return scanChequeList.size();
        }

        return outwardChequeList.size();
    }

    private void loadCurrentCheque() {

        int totalCheques = getTotalCheques();

        if (currentIndex < 0 || currentIndex >= totalCheques) {
            return;
        }

        divRejectRemarks.setVisible(false);

        txtRemarks.setValue("");

        lblMicrError.setVisible(false);

        txtCorrectedMicr.setValue("");

        showingBackImage = false;

        zoomLevel = 1.0;

        rotation = 0;

        if ("SCAN".equals(source)) {

            ScanCheque cheque =
                    scanChequeList.get(currentIndex);

            populateScanCheque(cheque);

        } else {

            OutwardCheque cheque =
                    outwardChequeList.get(currentIndex);

            populateOutwardCheque(cheque);
        }

        updateProgress();

        updateNavigationButtons();
    }

    private void populateScanCheque(ScanCheque cheque) {

        if (cheque == null) {
            return;
        }

        txtChequeNumber.setValue(
                safe(cheque.getChequeNumber()));

        txtCityCode.setValue(
                safe(cheque.getCityCode()));

        txtBankCode.setValue(
                safe(cheque.getBankCode()));

        txtBranchCode.setValue(
                safe(cheque.getBranchCode()));

        String currentMicr =
                safe(cheque.getMicrCode());

        txtCurrentMicr.setValue(currentMicr);

        txtCorrectedMicr.setValue(currentMicr);

        txtBankName.setValue(
                "Bank Code: " + safe(cheque.getBankCode()));

        txtBranchName.setValue(
                "Branch Code: " + safe(cheque.getBranchCode()));

        txtPayTo.setValue(
                safe(cheque.getPayeeName()));

        txtAmount.setValue(
                formatAmount(cheque.getChequeAmount()));

        frontImagePath =
                getImagePath(
                        cheque,
                        "getChequeImageFront");

        backImagePath =
                getImagePath(
                        cheque,
                        "getChequeImageBack");

        showFrontImage();
    }

    private void populateOutwardCheque(OutwardCheque cheque) {

        if (cheque == null) {
            return;
        }

        txtChequeNumber.setValue(
                safe(cheque.getChequeNumber()));

        txtCityCode.setValue(
                safe(cheque.getCityCode()));

        txtBankCode.setValue(
                safe(cheque.getBankCode()));

        txtBranchCode.setValue(
                safe(cheque.getBranchCode()));

        String currentMicr =
                safe(cheque.getMicrCode());

        txtCurrentMicr.setValue(currentMicr);

        txtCorrectedMicr.setValue(currentMicr);

        txtBankName.setValue(
                "Bank Code: " + safe(cheque.getBankCode()));

        txtBranchName.setValue(
                "Branch Code: " + safe(cheque.getBranchCode()));

        txtPayTo.setValue(
                safe(cheque.getPayeeName()));

        txtAmount.setValue(
                formatAmount(cheque.getChequeAmount()));

        frontImagePath =
                getImagePath(
                        cheque,
                        "getChequeImageFront");

        backImagePath =
                getImagePath(
                        cheque,
                        "getChequeImageBack");

        showFrontImage();
    }

    private String getImagePath(
            Object cheque,
            String methodName) {

        if (cheque == null) {
            return "";
        }

        try {

            Method method =
                    cheque.getClass()
                            .getMethod(methodName);

            Object value = method.invoke(cheque);

            return value == null
                    ? ""
                    : value.toString();

        } catch (Exception e) {

            return "";
        }
    }

    private void showFrontImage() {

        showingBackImage = false;

        lblChequeImageTitle.setValue(
                "Cheque Front Image");

        loadChequeImage(frontImagePath);

        applyImageTransform();
    }

    private void showBackImage() {

        showingBackImage = true;

        lblChequeImageTitle.setValue(
                "Cheque Back Image");

        if (backImagePath == null
                || backImagePath.trim().isEmpty()) {

            imgCheque.setVisible(false);

            imgCheque.setSrc("");

            lblChequeImageTitle.setValue(
                    "Cheque Back Image Not Available");

            return;
        }

        loadChequeImage(backImagePath);

        applyImageTransform();
    }

    private void loadChequeImage(String imagePath) {

        if (imagePath == null
                || imagePath.trim().isEmpty()) {

            imgCheque.setSrc("");

            imgCheque.setVisible(false);

            return;
        }

        imagePath = imagePath.trim();

        if (!imagePath.startsWith("/")) {
            imagePath = "/" + imagePath;
        }

        imgCheque.setSrc(imagePath);

        imgCheque.setVisible(true);
    }

    @Listen("onClick = #btnFront")
    public void viewFront() {

        zoomLevel = 1.0;

        rotation = 0;

        showFrontImage();
    }

    @Listen("onClick = #btnBack")
    public void viewBack() {

        zoomLevel = 1.0;

        rotation = 0;

        showBackImage();
    }

    @Listen("onClick = #btnZoom")
    public void zoomImage() {

        zoomLevel += 0.25;

        if (zoomLevel > 3.0) {
            zoomLevel = 1.0;
        }

        applyImageTransform();
    }

    @Listen("onClick = #btnRotate")
    public void rotateImage() {

        rotation += 90;

        if (rotation >= 360) {
            rotation = 0;
        }

        applyImageTransform();
    }

    private void applyImageTransform() {

        String transform =
                "transform: scale("
                        + zoomLevel
                        + ") rotate("
                        + rotation
                        + "deg);"
                        + "transform-origin:center center;";

        imgCheque.setStyle(transform);
    }

    @Listen("onClick = #btnPrevious")
    public void previousCheque() {

        if (currentIndex <= 0) {
            return;
        }

        currentIndex--;

        loadCurrentCheque();
    }

    @Listen("onClick = #btnNext")
    public void nextCheque() {

        int totalCheques = getTotalCheques();

        if (currentIndex >= totalCheques - 1) {
            return;
        }

        currentIndex++;

        loadCurrentCheque();
    }

    private void updateNavigationButtons() {

        int totalCheques = getTotalCheques();

        btnPrevious.setDisabled(
                currentIndex <= 0);

        btnNext.setDisabled(
                currentIndex >= totalCheques - 1);

        lblRecordPosition.setValue(
                "Record "
                        + (currentIndex + 1)
                        + " of "
                        + totalCheques);

        lblChequeNavigation.setValue(
                (currentIndex + 1)
                        + " / "
                        + totalCheques);
    }

    private void updateProgress() {

        int totalCheques = getTotalCheques();

        if (totalCheques <= 0) {

            lblProgressText.setValue(
                    "Record 0 of 0");

            divProgressFill.setStyle(
                    "width:0%;");

            lblTotalCheques.setValue("0");

            lblCompletedCheques.setValue("0");

            lblRemainingCheques.setValue("0");

            return;
        }

        int recordNumber =
                currentIndex + 1;

        double percentage =
                ((double) recordNumber
                        / totalCheques)
                        * 100.0;

        lblProgressText.setValue(
                "Record "
                        + recordNumber
                        + " of "
                        + totalCheques);

        divProgressFill.setStyle(
                "width:"
                        + percentage
                        + "%;");

        lblTotalCheques.setValue(
                String.valueOf(totalCheques));

        lblCompletedCheques.setValue(
                String.valueOf(currentIndex));

        lblRemainingCheques.setValue(
                String.valueOf(
                        totalCheques - recordNumber));
    }

    @Listen("onClick = #btnSaveNext")
    public void saveAndNext() {

        int totalCheques =
                getTotalCheques();

        if (currentIndex < 0
                || currentIndex >= totalCheques) {

            showError(
                    "Invalid cheque selection.");

            return;
        }

        if (!validateMicrFields()) {
            return;
        }

        try {

            if ("SCAN".equals(source)) {

                ScanCheque cheque =
                        scanChequeList
                                .get(currentIndex);

                updateScanChequeFromScreen(
                        cheque);

                cheque.setChequeStatus(
                        "PENDING_DATA_ENTRY");

                outwardMakerService
                        .saveScanMicrRepair(cheque);

            } else {

                OutwardCheque cheque =
                        outwardChequeList
                                .get(currentIndex);

                updateOutwardChequeFromScreen(
                        cheque);

                cheque.setChequeStatus(
                        "PENDING_DATA_ENTRY");

                outwardMakerService
                        .saveOutwardMicrRepair(cheque);
            }

            if (currentIndex
                    == totalCheques - 1) {

                showCompletionMessage();

                Clients.showNotification(
                        "MICR repair completed successfully.",
                        Clients.NOTIFICATION_TYPE_INFO,
                        null,
                        "top_center",
                        2000);

                redirectToMicrRepairView();

                return;
            }

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

    private void updateScanChequeFromScreen(
            ScanCheque cheque) {

        cheque.setMicrCode(
                txtCorrectedMicr
                        .getValue()
                        .trim());

        cheque.setCityCode(
                txtCityCode
                        .getValue()
                        .trim());

        cheque.setBankCode(
                txtBankCode
                        .getValue()
                        .trim());

        cheque.setBranchCode(
                txtBranchCode
                        .getValue()
                        .trim());
    }

    private void updateOutwardChequeFromScreen(
            OutwardCheque cheque) {

        cheque.setMicrCode(
                txtCorrectedMicr
                        .getValue()
                        .trim());

        cheque.setCityCode(
                txtCityCode
                        .getValue()
                        .trim());

        cheque.setBankCode(
                txtBankCode
                        .getValue()
                        .trim());

        cheque.setBranchCode(
                txtBranchCode
                        .getValue()
                        .trim());
    }

    private boolean validateMicrFields() {

        String micr =
                txtCorrectedMicr.getValue();

        String city =
                txtCityCode.getValue();

        String bank =
                txtBankCode.getValue();

        String branch =
                txtBranchCode.getValue();

        lblMicrError.setVisible(false);

        if (micr == null
                || micr.trim().isEmpty()) {

            lblMicrError.setValue(
                    "Corrected MICR Code is required.");

            lblMicrError.setVisible(true);

            txtCorrectedMicr.setFocus(true);

            return false;
        }

        if (city == null
                || city.trim().isEmpty()) {

            showWarning(
                    "City Code is required.");

            txtCityCode.setFocus(true);

            return false;
        }

        if (bank == null
                || bank.trim().isEmpty()) {

            showWarning(
                    "Bank Code is required.");

            txtBankCode.setFocus(true);

            return false;
        }

        if (branch == null
                || branch.trim().isEmpty()) {

            showWarning(
                    "Branch Code is required.");

            txtBranchCode.setFocus(true);

            return false;
        }

        return true;
    }

    private void redirectToMicrRepairView() {

        Component root =
                micrEntryWindow
                        .getDesktop()
                        .getFirstPage()
                        .getFirstRoot();

        Component mainContentArea =
                root.getFellowIfAny(
                        "mainContentArea",
                        true);

        if (mainContentArea
                instanceof Include) {

            Include include =
                    (Include) mainContentArea;

            include.setSrc(
                    "/outward/maker/micr-repair/micr-repair-view.zul");

            return;
        }

        // Fallback if opened directly

        Executions.sendRedirect(
                "/outward/maker/micr-repair/micr-repair-view.zul");
    }

    @Listen("onClick = #btnBackToList")
    public void backToList() {

        redirectToMicrRepairView();
    }

    @Listen("onClick = #btnRejectRequest")
    public void rejectRequest() {

        boolean visible =
                divRejectRemarks.isVisible();

        divRejectRemarks.setVisible(
                !visible);

        if (!visible) {

            lblRejectRemarks.setValue(
                    "REJECTION REMARKS");

            txtRemarks.setFocus(true);
        }
    }

    private void showCompletionMessage() {

        if (divCompletionMessage == null) {
            return;
        }

        divCompletionMessage.setVisible(true);

        if (lblCompletionTitle != null) {

            lblCompletionTitle.setValue(
                    "MICR Repair Completed");
        }

        if (lblCompletionText != null) {

            lblCompletionText.setValue(
                    "All MICR-error cheques in batch "
                            + batchId
                            + " have been repaired.");
        }

        if (divFormContainer != null) {
            divFormContainer.setVisible(false);
        }
    }

    private String formatAmount(
            BigDecimal amount) {

        if (amount == null) {
            return "";
        }

        DecimalFormat decimalFormat =
                new DecimalFormat("#,##0.00");

        return "₹ "
                + decimalFormat.format(amount);
    }

    private String safe(String value) {

        if (value == null) {
            return "";
        }

        return value;
    }

    private void showError(String message) {

        Clients.showNotification(
                message,
                Clients.NOTIFICATION_TYPE_ERROR,
                null,
                "top_center",
                4000);
    }

    private void showWarning(String message) {

        Clients.showNotification(
                message,
                Clients.NOTIFICATION_TYPE_WARNING,
                null,
                "top_center",
                3000);
    }
}