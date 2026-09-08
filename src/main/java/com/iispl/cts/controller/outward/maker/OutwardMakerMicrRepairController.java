package com.iispl.cts.controller.outward.maker;

import java.lang.reflect.Method;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.service.outward.OutwardMakerService;
import com.iispl.cts.serviceimpl.outward.OutwardMakerServiceImpl;

public class OutwardMakerMicrRepairController
        extends SelectorComposer<Window> {

    private static final long serialVersionUID = 1L;

    private final OutwardMakerService outwardMakerService =
            new OutwardMakerServiceImpl();

    @Wire
    private Label lblBatchNumber;

    @Wire
    private Label lblMicrRepairRequired;

    @Wire
    private Label lblCompletedCheques;

    @Wire
    private Label lblRemainingCheques;

    @Wire
    private Label lblRecordPosition;

    @Wire
    private Label lblChequeNavigation;

    @Wire
    private Label lblProgressText;

    @Wire
    private Label lblChequeImageTitle;

    @Wire
    private Label lblMicrError;

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
    private Button btnPrevious;

    @Wire
    private Button btnNext;

    @Wire
    private Button btnZoom;

    @Wire
    private Button btnZoomOut;

    @Wire
    private Button btnZoomReset;

    @Wire
    private Button btnRotate;

    @Wire
    private Button btnImageToggle;

    @Wire
    private Button btnRejectRequest;

    @Wire
    private Button btnSaveNext;

    @Wire
    private Div divProgressFill;

    @Wire
    private org.zkoss.zul.Image imgCheque;

    private List<?> micrRepairCheques;

    private int currentIndex = 0;
    private int completedCount = 0;

    private String source;
    private String batchId;

    /*
     * Image viewing state
     */
    private double zoomLevel = 1.0;

    private int rotation = 0;

    private int imageX = 0;
    private int imageY = 0;

    private boolean showingBackImage = false;
    private boolean updatingMicrFields = false;

    @Override
    public void doAfterCompose(Window window) throws Exception {

        super.doAfterCompose(window);

        loadParameters();

        if (batchId == null || batchId.trim().isEmpty()) {
            showError("Batch ID is missing.");
            return;
        }

        if (source == null || source.trim().isEmpty()) {
            source = "SCAN";
        }

        loadMicrRepairCheques();

        if (micrRepairCheques == null
                || micrRepairCheques.isEmpty()) {

            showWarning(
                    "No MICR repair cheques found for batch "
                    + batchId);

            updateSummary();
            updateProgress();

            return;
        }

        currentIndex = 0;

        loadCurrentCheque();

        initializeImageViewer();
    }

    private void loadParameters() {

        Component parent = getSelf().getParent();

        while (parent != null && !(parent instanceof Include)) {
            parent = parent.getParent();
        }

        if (parent instanceof Include) {

            Include include = (Include) parent;

            Object sourceAttr =
                    include.getAttribute("MICR_REPAIR_SOURCE");

            Object batchAttr =
                    include.getAttribute("MICR_REPAIR_BATCH_ID");

            if (sourceAttr != null) {
                source = String.valueOf(sourceAttr);
            }

            if (batchAttr != null) {
                batchId = String.valueOf(batchAttr);
            }
        }

        if (source == null || source.trim().isEmpty()) {

            source =
                    Executions.getCurrent()
                            .getParameter("source");
        }

        if (batchId == null || batchId.trim().isEmpty()) {

            batchId =
                    Executions.getCurrent()
                            .getParameter("batchId");
        }

        if (source != null) {
            source = source.trim();
        }

        if (batchId != null) {
            batchId = batchId.trim();
        }
    }

    private void loadMicrRepairCheques() {

        try {

            if ("SCAN".equalsIgnoreCase(source)) {

                List<ScanCheque> cheques =
                        outwardMakerService
                                .getScanMicrRepairCheques(batchId);

                micrRepairCheques = cheques;

            } else {

                List<OutwardCheque> cheques =
                        outwardMakerService
                                .getOutwardMicrRepairCheques(batchId);

                micrRepairCheques = cheques;
            }

        } catch (Exception e) {

            showError(
                    "Unable to load MICR repair cheques.");

            e.printStackTrace();
        }
    }

    private void loadCurrentCheque() {

        if (micrRepairCheques == null
                || micrRepairCheques.isEmpty()
                || currentIndex < 0
                || currentIndex >= micrRepairCheques.size()) {

            return;
        }

        Object cheque =
                micrRepairCheques.get(currentIndex);

        resetImageViewState();

        if (cheque instanceof ScanCheque) {

            populateScanCheque(
                    (ScanCheque) cheque);

        } else if (cheque instanceof OutwardCheque) {

            populateOutwardCheque(
                    (OutwardCheque) cheque);
        }

        showFrontImage();

        updateNavigationButtons();
        updateSummary();
        updateProgress();
        updateSaveButton();
    }

    private void populateScanCheque(
            ScanCheque cheque) {

        lblBatchNumber.setValue(batchId);

        txtChequeNumber.setValue(
                safe(cheque.getChequeNumber()));

        txtCityCode.setValue(
                safe(cheque.getCityCode()));

        txtBankCode.setValue(
                safe(cheque.getBankCode()));

        txtBranchCode.setValue(
                safe(cheque.getBranchCode()));

        txtCurrentMicr.setValue(
                safe(cheque.getMicrCode()));

        setCorrectedMicrFromCodes();

        updateImage(cheque, false);
    }

    private void populateOutwardCheque(
            OutwardCheque cheque) {

        lblBatchNumber.setValue(batchId);

        txtChequeNumber.setValue(
                safe(cheque.getChequeNumber()));

        txtCityCode.setValue(
                safe(cheque.getCityCode()));

        txtBankCode.setValue(
                safe(cheque.getBankCode()));

        txtBranchCode.setValue(
                safe(cheque.getBranchCode()));

        txtCurrentMicr.setValue(
                safe(cheque.getMicrCode()));

        setCorrectedMicrFromCodes();

        updateImage(cheque, false);
    }

    private void updateImage(
            Object cheque,
            boolean back) {

        String imagePath = null;

        if (cheque == null) {

            imgCheque.setVisible(false);

            lblChequeImageTitle
                    .setValue("Cheque Image");

            return;
        }

        String methodName =
                back
                        ? "getChequeImageBack"
                        : "getChequeImageFront";

        try {

            Method method =
                    cheque.getClass()
                            .getMethod(methodName);

            Object value =
                    method.invoke(cheque);

            if (value != null) {
                imagePath =
                        String.valueOf(value);
            }

        } catch (Exception e) {

            e.printStackTrace();
        }

        if (imagePath == null
                || imagePath.trim().isEmpty()) {

            imgCheque.setVisible(false);

            lblChequeImageTitle.setValue(
                    back
                            ? "Cheque Back Image"
                            : "Cheque Front Image");

            return;
        }

        try {

            if (!imagePath.startsWith("/")) {
                imagePath = "/" + imagePath;
            }

            imgCheque.setSrc(imagePath);
            imgCheque.setVisible(true);

            lblChequeImageTitle.setValue(
                    back
                            ? "Cheque Back Image"
                            : "Cheque Front Image");

            applyImageTransform();

        } catch (Exception e) {

            imgCheque.setVisible(false);

            e.printStackTrace();
        }
    }

    /*
     * Initialize browser-side image viewer.
     *
     * The viewer remains fixed.
     * The image can be zoomed and dragged inside it.
     */
    private void initializeImageViewer() {

        Clients.evalJavaScript(
                "window.setTimeout(function(){"
                + "var img=document.getElementById('"
                + imgCheque.getUuid()
                + "');"
                + "if(!img)return;"

                + "img.style.cursor='grab';"
                + "img.style.userSelect='none';"
                + "img.style.webkitUserSelect='none';"

                + "if(img.dataset.micrViewerReady==='true')return;"
                + "img.dataset.micrViewerReady='true';"

                + "var dragging=false;"
                + "var startX=0;"
                + "var startY=0;"
                + "var startLeft=0;"
                + "var startTop=0;"

                + "img.addEventListener('mousedown',function(e){"
                + "if(e.button!==0)return;"
                + "dragging=true;"
                + "startX=e.clientX;"
                + "startY=e.clientY;"
                + "startLeft=parseFloat(img.dataset.posX||'0');"
                + "startTop=parseFloat(img.dataset.posY||'0');"
                + "img.style.cursor='grabbing';"
                + "e.preventDefault();"
                + "});"

                + "document.addEventListener('mousemove',function(e){"
                + "if(!dragging)return;"
                + "var x=startLeft+(e.clientX-startX);"
                + "var y=startTop+(e.clientY-startY);"
                + "img.dataset.posX=x;"
                + "img.dataset.posY=y;"
                + "img.style.transform="
                + "'translate('+x+'px,'+y+'px) scale('+"
                + "(img.dataset.zoom||'1')"
                + " + ') rotate('+"
                + "(img.dataset.rotation||'0')"
                + " + 'deg)';"
                + "});"

                + "document.addEventListener('mouseup',function(){"
                + "if(!dragging)return;"
                + "dragging=false;"
                + "img.style.cursor='grab';"
                + "});"

                + "img.addEventListener('wheel',function(e){"
                + "e.preventDefault();"
                + "var oldZoom=parseFloat(img.dataset.zoom||'1');"
                + "var newZoom=e.deltaY<0"
                + "?Math.min(oldZoom+0.1,3)"
                + ":Math.max(oldZoom-0.1,1);"
                + "if(newZoom===oldZoom)return;"

                + "var rect=img.getBoundingClientRect();"
                + "var mouseX=e.clientX-(rect.left+rect.width/2);"
                + "var mouseY=e.clientY-(rect.top+rect.height/2);"

                + "var currentX=parseFloat(img.dataset.posX||'0');"
                + "var currentY=parseFloat(img.dataset.posY||'0');"

                + "var ratio=newZoom/oldZoom;"

                + "currentX=currentX-(mouseX*(ratio-1));"
                + "currentY=currentY-(mouseY*(ratio-1));"

                + "img.dataset.zoom=newZoom;"
                + "img.dataset.posX=currentX;"
                + "img.dataset.posY=currentY;"

                + "img.style.transform="
                + "'translate('+currentX+'px,'+currentY+'px) scale('+"
                + "newZoom"
                + " + ') rotate('+"
                + "(img.dataset.rotation||'0')"
                + " + 'deg)';"
                + "},{passive:false});"

                + "},100);");
    }

    private void resetImageViewState() {

        zoomLevel = 1.0;
        rotation = 0;

        imageX = 0;
        imageY = 0;

        applyImageTransform();
    }

    @Listen("onClick = #btnImageToggle")
    public void toggleImage() {

        resetImageViewState();

        if (showingBackImage) {

            showFrontImage();

        } else {

            showBackImage();
        }
    }

    private void showFrontImage() {

        showingBackImage = false;

        btnImageToggle.setLabel("View Back");

        if (micrRepairCheques == null
                || micrRepairCheques.isEmpty()) {

            return;
        }

        Object cheque =
                micrRepairCheques.get(currentIndex);

        updateImage(cheque, false);
    }

    private void showBackImage() {

        showingBackImage = true;

        btnImageToggle.setLabel("View Front");

        if (micrRepairCheques == null
                || micrRepairCheques.isEmpty()) {

            return;
        }

        Object cheque =
                micrRepairCheques.get(currentIndex);

        updateImage(cheque, true);
    }

    @Listen("onClick = #btnZoom")
    public void zoomIn() {

        zoomLevel += 0.25;

        if (zoomLevel > 3.0) {
            zoomLevel = 3.0;
        }

        applyImageTransform();
    }

    @Listen("onClick = #btnZoomOut")
    public void zoomOut() {

        zoomLevel -= 0.25;

        if (zoomLevel < 1.0) {
            zoomLevel = 1.0;
        }

        if (zoomLevel == 1.0) {
            imageX = 0;
            imageY = 0;
        }

        applyImageTransform();
    }

    @Listen("onClick = #btnZoomReset")
    public void resetZoom() {

        zoomLevel = 1.0;
        imageX = 0;
        imageY = 0;

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

        if (imgCheque == null) {
            return;
        }

        String transform =
                "translate("
                + imageX
                + "px,"
                + imageY
                + "px) "
                + "scale("
                + zoomLevel
                + ") "
                + "rotate("
                + rotation
                + "deg);";

        imgCheque.setStyle(
                "transform:"
                + transform
                + "transform-origin:center center;"
                + "cursor:"
                + (zoomLevel > 1.0
                        ? "grab;"
                        : "default;")
                + "user-select:none;"
                + "-webkit-user-select:none;");

        String script =
                "var img=document.getElementById('"
                + imgCheque.getUuid()
                + "');"
                + "if(img){"
                + "img.dataset.zoom='"
                + zoomLevel
                + "';"
                + "img.dataset.rotation='"
                + rotation
                + "';"
                + "img.dataset.posX='"
                + imageX
                + "';"
                + "img.dataset.posY='"
                + imageY
                + "';"
                + "}";

        Clients.evalJavaScript(script);
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

        if (micrRepairCheques == null
                || currentIndex
                    >= micrRepairCheques.size() - 1) {

            return;
        }

        currentIndex++;

        loadCurrentCheque();
    }

    private void updateNavigationButtons() {

        if (micrRepairCheques == null
                || micrRepairCheques.isEmpty()) {

            btnPrevious.setDisabled(true);
            btnNext.setDisabled(true);

            return;
        }

        btnPrevious.setDisabled(
                currentIndex <= 0);

        btnNext.setDisabled(
                currentIndex
                    >= micrRepairCheques.size() - 1);
    }

    private void updateSummary() {

        int total =
                getTotalCheques();

        lblMicrRepairRequired.setValue(
                String.valueOf(total));

        int completed = completedCount;

        if (completed < 0) {
            completed = 0;
        }

        if (completed > total) {
            completed = total;
        }

        int remaining =
                total - completed;

        lblCompletedCheques.setValue(
                String.valueOf(completed));

        lblRemainingCheques.setValue(
                String.valueOf(remaining));

        if (total == 0) {

            lblRecordPosition.setValue(
                    "Record 0 of 0");

            lblChequeNavigation.setValue("");

        } else {

            lblRecordPosition.setValue(
                    "Record "
                    + (currentIndex + 1)
                    + " of "
                    + total);

            lblChequeNavigation.setValue(
                    (currentIndex + 1)
                    + " / "
                    + total);
        }
    }

    private void updateProgress() {

        int total =
                getTotalCheques();

        if (total <= 0) {

            lblProgressText.setValue(
                    "Record 0 of 0");

            divProgressFill.setWidth("0%");

            return;
        }

        int completed =
                completedCount;

        if (completed < 0) {
            completed = 0;
        }

        if (completed > total) {
            completed = total;
        }

        int percentage =
                (int) Math.round(
                        ((double) completed
                                / total) * 100);

        lblProgressText.setValue(
                "Record "
                + (currentIndex + 1)
                + " of "
                + total);

        divProgressFill.setWidth(
                percentage + "%");
    }

    private int getTotalCheques() {

        if (micrRepairCheques == null) {
            return 0;
        }

        return micrRepairCheques.size();
    }

    @Listen("onChanging = #txtCityCode")
    public void cityCodeChanging(
            InputEvent event) {

        if (updatingMicrFields) {
            return;
        }

        String value =
                event.getValue();

        txtCityCode.setValue(value);

        updateCorrectedMicrFromCodes(
                value,
                txtBankCode.getValue(),
                txtBranchCode.getValue());

        updateSaveButton();
    }

    @Listen("onChanging = #txtBankCode")
    public void bankCodeChanging(
            InputEvent event) {

        if (updatingMicrFields) {
            return;
        }

        String value =
                event.getValue();

        txtBankCode.setValue(value);

        updateCorrectedMicrFromCodes(
                txtCityCode.getValue(),
                value,
                txtBranchCode.getValue());

        updateSaveButton();
    }

    @Listen("onChanging = #txtBranchCode")
    public void branchCodeChanging(
            InputEvent event) {

        if (updatingMicrFields) {
            return;
        }

        String value =
                event.getValue();

        txtBranchCode.setValue(value);

        updateCorrectedMicrFromCodes(
                txtCityCode.getValue(),
                txtBankCode.getValue(),
                value);

        updateSaveButton();
    }

    @Listen("onChanging = #txtCorrectedMicr")
    public void correctedMicrChanging(
            InputEvent event) {

        if (updatingMicrFields) {
            return;
        }

        String micr =
                event.getValue();

        txtCorrectedMicr.setValue(micr);

        updateCodesFromCorrectedMicr(
                micr);

        updateSaveButton();
    }

    private void setCorrectedMicrFromCodes() {

        updatingMicrFields = true;

        try {

            String city =
                    safe(txtCityCode.getValue());

            String bank =
                    safe(txtBankCode.getValue());

            String branch =
                    safe(txtBranchCode.getValue());

            if (city.length() == 3
                    && bank.length() == 3
                    && branch.length() == 3) {

                txtCorrectedMicr.setValue(
                        city + bank + branch);

            } else {

                txtCorrectedMicr.setValue("");
            }

        } finally {

            updatingMicrFields = false;
        }
    }

    private void updateCorrectedMicrFromCodes(
            String city,
            String bank,
            String branch) {

        if (updatingMicrFields) {
            return;
        }

        updatingMicrFields = true;

        try {

            city = safe(city);
            bank = safe(bank);
            branch = safe(branch);

            txtCorrectedMicr.setValue(
                    city + bank + branch);

        } finally {

            updatingMicrFields = false;
        }
    }

    private void updateCodesFromCorrectedMicr(
            String micr) {

        if (updatingMicrFields) {
            return;
        }

        micr = safe(micr);

        updatingMicrFields = true;

        try {

            if (micr.length() >= 3) {

                txtCityCode.setValue(
                        micr.substring(0, 3));

            } else {

                txtCityCode.setValue(micr);
            }

            if (micr.length() >= 6) {

                txtBankCode.setValue(
                        micr.substring(3, 6));

            } else if (micr.length() > 3) {

                txtBankCode.setValue(
                        micr.substring(3));

            } else {

                txtBankCode.setValue("");
            }

            if (micr.length() >= 9) {

                txtBranchCode.setValue(
                        micr.substring(6, 9));

            } else if (micr.length() > 6) {

                txtBranchCode.setValue(
                        micr.substring(6));

            } else {

                txtBranchCode.setValue("");
            }

        } finally {

            updatingMicrFields = false;
        }
    }

    private boolean isValidMicrCodes() {

        String city =
                safe(txtCityCode.getValue());

        String bank =
                safe(txtBankCode.getValue());

        String branch =
                safe(txtBranchCode.getValue());

        String micr =
                safe(txtCorrectedMicr.getValue());

        if (city.length() != 3
                || bank.length() != 3
                || branch.length() != 3) {

            return false;
        }

        if (micr.length() != 9) {
            return false;
        }

        return micr.equals(
                city + bank + branch);
    }

    private void updateSaveButton() {

        boolean valid =
                isValidMicrCodes();

        btnSaveNext.setVisible(valid);

        if (!valid) {

            lblMicrError.setVisible(true);

            lblMicrError.setValue(
                    "City Code, Bank Code and Branch Code must each contain exactly 3 characters.");

            return;
        }

        lblMicrError.setVisible(false);

        int totalCheques =
                getTotalCheques();

        if (currentIndex
                == totalCheques - 1) {

            btnSaveNext.setLabel("Save");

        } else {

            btnSaveNext.setLabel(
                    "Save & Next");
        }
    }

    @Listen("onClick = #btnSaveNext")
    public void saveMicrRepair() {

        if (!isValidMicrCodes()) {

            showWarning(
                    "City Code, Bank Code and Branch Code must each contain exactly 3 characters.");

            updateSaveButton();

            return;
        }

        if (micrRepairCheques == null
                || micrRepairCheques.isEmpty()
                || currentIndex < 0
                || currentIndex
                    >= micrRepairCheques.size()) {

            return;
        }

        String cityCode =
                safe(txtCityCode.getValue());

        String bankCode =
                safe(txtBankCode.getValue());

        String branchCode =
                safe(txtBranchCode.getValue());

        String correctedMicr =
                safe(txtCorrectedMicr.getValue());

        Object currentCheque =
                micrRepairCheques
                        .get(currentIndex);

        try {

            if (currentCheque
                    instanceof ScanCheque) {

                ScanCheque cheque =
                        (ScanCheque) currentCheque;

                cheque.setCityCode(cityCode);
                cheque.setBankCode(bankCode);
                cheque.setBranchCode(branchCode);
                cheque.setMicrCode(
                        correctedMicr);

                cheque.setChequeStatus(
                        "PENDING_DATA_ENTRY");

                outwardMakerService
                        .saveScanMicrRepair(
                                cheque);

                completedCount++;

            } else if (currentCheque
                    instanceof OutwardCheque) {

                OutwardCheque cheque =
                        (OutwardCheque) currentCheque;

                cheque.setCityCode(cityCode);
                cheque.setBankCode(bankCode);
                cheque.setBranchCode(branchCode);
                cheque.setMicrCode(
                        correctedMicr);

                cheque.setChequeStatus(
                        "PENDING_DATA_ENTRY");

                outwardMakerService
                        .saveOutwardMicrRepair(
                                cheque);

                completedCount++;
            }

            if (currentIndex
                    >= micrRepairCheques.size() - 1) {

                goBackToList();

            } else {

                currentIndex++;

                loadCurrentCheque();
            }

        } catch (Exception e) {

            showError(
                    "Unable to save MICR repair.");

            e.printStackTrace();
        }
    }

    @Listen("onClick = #btnRejectRequest")
    public void requestReject() {

        if (micrRepairCheques == null
                || micrRepairCheques.isEmpty()
                || currentIndex < 0
                || currentIndex
                    >= micrRepairCheques.size()) {

            return;
        }

        Object currentCheque =
                micrRepairCheques
                        .get(currentIndex);

        try {

            if (currentCheque
                    instanceof ScanCheque) {

                ScanCheque cheque =
                        (ScanCheque) currentCheque;

                cheque.setChequeStatus(
                        "REJECT_REQUEST");

                outwardMakerService
                        .saveScanMicrRepair(
                                cheque);

                completedCount++;

            } else if (currentCheque
                    instanceof OutwardCheque) {

                OutwardCheque cheque =
                        (OutwardCheque) currentCheque;

                cheque.setChequeStatus(
                        "REJECT_REQUEST");

                outwardMakerService
                        .saveOutwardMicrRepair(
                                cheque);

                completedCount++;
            }

            if (currentIndex
                    >= micrRepairCheques.size() - 1) {

                goBackToList();

            } else {

                currentIndex++;

                loadCurrentCheque();
            }

        } catch (Exception e) {

            showError(
                    "Unable to submit reject request.");

            e.printStackTrace();
        }
    }

    @Listen("onClick = #btnBackToList")
    public void backToList() {

        goBackToList();
    }

    private void goBackToList() {

        Component root =
                getSelf()
                        .getPage()
                        .getFirstRoot();

        Component mainContentArea =
                root.getFellowIfAny(
                        "mainContentArea",
                        true);

        if (mainContentArea
                instanceof Include) {

            Include include =
                    (Include) mainContentArea;

            include.setAttribute(
                    "MICR_REPAIR_SOURCE",
                    source);

            include.setAttribute(
                    "MICR_REPAIR_BATCH_ID",
                    batchId);

            include.setSrc(
                    "/outward/maker/micr-repair/micr-repair-view.zul");

        } else {

            Executions.sendRedirect(
                    "/outward/maker/micr-repair/micr-repair-view.zul"
                    + "?source=" + source
                    + "&batchId=" + batchId);
        }
    }

    private String safe(String value) {

        return value == null
                ? ""
                : value.trim();
    }

    private void showError(String message) {

        org.zkoss.zul.Messagebox.show(
                message,
                "Error",
                org.zkoss.zul.Messagebox.OK,
                org.zkoss.zul.Messagebox.ERROR);
    }

    private void showWarning(String message) {

        org.zkoss.zul.Messagebox.show(
                message,
                "Warning",
                org.zkoss.zul.Messagebox.OK,
                org.zkoss.zul.Messagebox.EXCLAMATION);
    }
}