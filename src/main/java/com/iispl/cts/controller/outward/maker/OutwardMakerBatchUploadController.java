package com.iispl.cts.controller.outward.maker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.iispl.cts.dto.BatchValidationData;
import com.iispl.cts.dto.ValidationResult;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.outward.batchvalidator.MicrCodeHelper;
import com.iispl.cts.parser.BatchXmlParser;
import com.iispl.cts.service.outward.BatchValidationService;
import com.iispl.cts.service.outward.OutwardMakerService;
import com.iispl.cts.service.outward.ScanService;
import com.iispl.cts.serviceimpl.outward.BatchValidationServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardMakerServiceImpl;
import com.iispl.cts.serviceimpl.outward.ScanServiceImpl;

public class OutwardMakerBatchUploadController
        implements Composer<Component> {

    private static final long serialVersionUID = 1L;

    // =========================================================
    // SESSION
    // =========================================================

    private static final String SESSION_CURRENT_BATCH_ID =
            "OUTWARD_MAKER_CURRENT_BATCH_ID";

    // =========================================================
    // ZUL COMPONENTS
    // =========================================================

    private Intbox txtExpectedTotalCheques;

    private Decimalbox txtExpectedTotalChequeAmount;

    private Textbox txtChequeFolder;

    private Button btnBrowse;

    private Button btnValidateBatch;

    private Div divSuccessMessage;

    private Label lblSuccessText;

    private Groupbox batchDetailsGroup;

    private Listbox lstBatchDetails;

    private Groupbox chequeDetailsGroup;

    private Listbox lstCheques;

    private Combobox cmbChequeFilter;

    private Label lblTotalCheques;

    private Button btnChequePrevious;

    private Label lblChequePage;

    private Button btnChequeNext;

    // =========================================================
    // CURRENT CHEQUE LIST / PAGINATION
    // =========================================================

    private List<ScanCheque> currentChequeList =
            new ArrayList<ScanCheque>();

    private int currentChequePage = 0;

    private static final int CHEQUES_PER_PAGE = 10;

    // =========================================================
    // ROOT COMPONENT
    // =========================================================

    private Component pageRoot;

    // =========================================================
    // SERVICES
    // =========================================================

    private ScanService scanService;

    private BatchValidationService batchValidationService;

    private OutwardMakerService outwardMakerService;

    // =========================================================
    // UPLOADED ZIP
    // =========================================================

    private File uploadedZipFile;

    // =========================================================
    // CURRENT BATCH
    // =========================================================

    private String batchId;

    // =========================================================
    // COMPOSE
    // =========================================================

    @Override
    public void doAfterCompose(
            Component component) throws Exception {

        // =====================================================
        // STORE PAGE ROOT
        // =====================================================

        pageRoot =
                component.getPage().getFirstRoot();

        // =====================================================
        // GET ZUL COMPONENTS
        // =====================================================

        txtExpectedTotalCheques =
                (Intbox) component.getFellow(
                        "txtExpectedTotalCheques");

        txtExpectedTotalChequeAmount =
                (Decimalbox) component.getFellow(
                        "txtExpectedTotalChequeAmount");

        txtChequeFolder =
                (Textbox) component.getFellow(
                        "txtChequeFolder");

        btnBrowse =
                (Button) component.getFellow(
                        "btnBrowse");

        btnValidateBatch =
                (Button) component.getFellow(
                        "btnValidateBatch");

        divSuccessMessage =
                (Div) component.getFellow(
                        "divSuccessMessage");

        lblSuccessText =
                (Label) component.getFellow(
                        "lblSuccessText");

        batchDetailsGroup =
                (Groupbox) component.getFellow(
                        "batchDetailsGroup");

        lstBatchDetails =
                (Listbox) component.getFellow(
                        "lstBatchDetails");

        chequeDetailsGroup =
                (Groupbox) component.getFellow(
                        "grpChequeDetails");

        lstCheques =
                (Listbox) component.getFellow(
                        "lstCheques");

        cmbChequeFilter =
                (Combobox) component.getFellow(
                        "cmbChequeFilter");

        lblTotalCheques =
                (Label) component.getFellow(
                        "lblTotalCheques");

        btnChequePrevious =
                (Button) component.getFellow(
                        "btnChequePrevious");

        lblChequePage =
                (Label) component.getFellow(
                        "lblChequePage");

        btnChequeNext =
                (Button) component.getFellow(
                        "btnChequeNext");

        // =====================================================
        // CREATE SERVICES
        // =====================================================

        scanService =
                new ScanServiceImpl();

        batchValidationService =
                new BatchValidationServiceImpl();

        outwardMakerService =
                new OutwardMakerServiceImpl();

        // =====================================================
        // INITIAL PAGE STATE
        // =====================================================

        divSuccessMessage.setVisible(false);

        batchDetailsGroup.setVisible(false);

        chequeDetailsGroup.setVisible(false);

        lstCheques.getItems().clear();

        currentChequeList.clear();

        currentChequePage = 0;

        lblTotalCheques.setValue(
                "Total Cheques: 0");

        lblChequePage.setValue(
                "Page 1");

        btnChequePrevious.setDisabled(true);

        btnChequeNext.setDisabled(true);

        // =====================================================
        // LOAD CURRENT SESSION BATCH
        // =====================================================

        loadCurrentSessionBatch();

        // =====================================================
        // CHEQUE FILTER
        // =====================================================

        cmbChequeFilter.addEventListener(
                Events.ON_CHANGE,
                new EventListener<Event>() {

                    @Override
                    public void onEvent(Event event) {

                        currentChequePage = 0;

                        displayChequeDetails();
                    }
                });

        // =====================================================
        // PREVIOUS BUTTON
        // =====================================================

        btnChequePrevious.addEventListener(
                Events.ON_CLICK,
                new EventListener<Event>() {

                    @Override
                    public void onEvent(Event event) {

                        if (currentChequePage > 0) {

                            currentChequePage--;

                            displayChequeDetails();
                        }
                    }
                });

        // =====================================================
        // NEXT BUTTON
        // =====================================================

        btnChequeNext.addEventListener(
                Events.ON_CLICK,
                new EventListener<Event>() {

                    @Override
                    public void onEvent(Event event) {

                        if (currentChequePage
                                < getTotalChequePages() - 1) {

                            currentChequePage++;

                            displayChequeDetails();
                        }
                    }
                });

        btnValidateBatch.setDisabled(true);

        // =====================================================
        // BROWSE / ZIP UPLOAD
        // =====================================================

        btnBrowse.addEventListener(
                Events.ON_UPLOAD,
                new EventListener<Event>() {

                    @Override
                    public void onEvent(Event event) {

                        handleZipUpload(
                                (UploadEvent) event);
                    }
                });

        // =====================================================
        // VALIDATE BUTTON
        // =====================================================

        btnValidateBatch.addEventListener(
                Events.ON_CLICK,
                new EventListener<Event>() {

                    @Override
                    public void onEvent(Event event) {

                        validateBatch();
                    }
                });
    }

    // =========================================================
    // LOAD CURRENT SESSION BATCH
    // =========================================================

    private void loadCurrentSessionBatch() {

        Session session =
                Sessions.getCurrent();

        if (session == null) {
            return;
        }

        Object sessionBatchId =
                session.getAttribute(
                        SESSION_CURRENT_BATCH_ID);

        if (sessionBatchId == null) {
            return;
        }

        String currentSessionBatchId =
                sessionBatchId.toString();

        if (currentSessionBatchId == null
                || currentSessionBatchId.trim().isEmpty()) {

            return;
        }

        currentSessionBatchId =
                currentSessionBatchId.trim();

        ScanBatch makerBatch =
                outwardMakerService.getMakerBatch(
                        currentSessionBatchId);

        // =====================================================
        // BATCH ALREADY MOVED TO OUTWARD
        // =====================================================

        if (makerBatch == null) {

            batchId = null;

            batchDetailsGroup.setVisible(false);

            chequeDetailsGroup.setVisible(false);

            lstBatchDetails
                    .getItems()
                    .clear();

            return;
        }

        // =====================================================
        // CURRENT MAKER BATCH FOUND
        // =====================================================

        batchId =
                currentSessionBatchId;

        int actualChequeCount =
                makerBatch.getActualChequeCount();

        /*
         * Fetch cheques only to calculate the MICR repair
         * count for the batch row.
         *
         * The cheque list is NOT stored in the session.
         */
        List<ScanCheque> batchCheques =
                outwardMakerService
                        .getMakerBatchCheques(
                                batchId);

        int micrRepairCount =
                countMicrRepairCheques(
                        batchCheques);

        displayBatchDetails(
                makerBatch,
                actualChequeCount,
                micrRepairCount);
    }

    // =========================================================
    // HANDLE ZIP UPLOAD
    // =========================================================

    private void handleZipUpload(
            UploadEvent uploadEvent) {

        Media media =
                uploadEvent.getMedia();

        if (media == null) {
            return;
        }

        String fileName =
                media.getName();

        // =====================================================
        // CHECK ZIP
        // =====================================================

        if (fileName == null
                || !fileName.toLowerCase()
                        .endsWith(".zip")) {

            showErrorMessage(
                    "Please upload a ZIP file.");

            return;
        }

        // =====================================================
        // GET TEMPDATA PATH
        // =====================================================

        String tempDataPath =
                Executions.getCurrent()
                        .getDesktop()
                        .getWebApp()
                        .getRealPath(
                                "/TempData");

        if (tempDataPath == null) {

            showErrorMessage(
                    "Unable to access TempData directory.");

            return;
        }

        File tempDataDirectory =
                new File(tempDataPath);

        // =====================================================
        // CREATE TEMPDATA DIRECTORY
        // =====================================================

        if (!tempDataDirectory.exists()) {

            if (!tempDataDirectory.mkdirs()) {

                showErrorMessage(
                        "Unable to create TempData directory.");

                return;
            }
        }

        // =====================================================
        // DESTINATION FILE
        // =====================================================

        File destinationFile =
                new File(
                        tempDataDirectory,
                        fileName);

        // =====================================================
        // SAVE ZIP
        // =====================================================

        try (
                InputStream inputStream =
                        media.getStreamData();

                FileOutputStream outputStream =
                        new FileOutputStream(
                                destinationFile)) {

            byte[] buffer =
                    new byte[8192];

            int bytesRead;

            while ((bytesRead =
                    inputStream.read(buffer)) != -1) {

                outputStream.write(
                        buffer,
                        0,
                        bytesRead);
            }

            outputStream.flush();

        } catch (Exception e) {

            e.printStackTrace();

            showErrorMessage(
                    "Unable to save uploaded ZIP file.");

            return;
        }

        // =====================================================
        // STORE UPLOADED FILE
        // =====================================================

        uploadedZipFile =
                destinationFile;

        // =====================================================
        // DISPLAY FILE NAME
        // =====================================================

        txtChequeFolder.setValue(
                fileName);

        // =====================================================
        // RESET ONLY CURRENT UPLOAD INPUT
        // =====================================================

        /*
         * Do NOT remove the current batch from session here.
         *
         * The session batch is replaced only after the new
         * batch is successfully validated and saved.
         */

        divSuccessMessage.setVisible(false);

        chequeDetailsGroup.setVisible(false);

        currentChequeList.clear();

        currentChequePage = 0;

        lstCheques.getItems().clear();

        lblTotalCheques.setValue(
                "Total Cheques: 0");

        lblChequePage.setValue(
                "Page 1");

        btnChequePrevious.setDisabled(true);

        btnChequeNext.setDisabled(true);

        // =====================================================
        // ENABLE VALIDATE
        // =====================================================

        btnValidateBatch.setDisabled(false);
    }

    // =========================================================
    // VALIDATE BATCH
    // =========================================================

    private void validateBatch() {

        // =====================================================
        // CHECK ZIP
        // =====================================================

        if (uploadedZipFile == null
                || !uploadedZipFile.exists()) {

            showErrorMessage(
                    "Please upload a ZIP file before validating the batch.");

            return;
        }

        // =====================================================
        // GET EXPECTED COUNT
        // =====================================================

        Integer expectedTotalCheques =
                txtExpectedTotalCheques.getValue();

        if (expectedTotalCheques == null
                || expectedTotalCheques <= 0) {

            showErrorMessage(
                    "Please enter a valid expected cheque count.");

            return;
        }

        // =====================================================
        // GET EXPECTED AMOUNT
        // =====================================================

        BigDecimal expectedTotalAmount =
                txtExpectedTotalChequeAmount.getValue();

        if (expectedTotalAmount == null
                || expectedTotalAmount.signum() <= 0) {

            showErrorMessage(
                    "Please enter a valid expected total amount.");

            return;
        }

        try {

            // =================================================
            // STEP 1
            // PARSE
            // =================================================

            BatchXmlParser parser =
                    new BatchXmlParser();

            BatchXmlParser.ParsedBatchData parsedData =
                    parser.parse(
                            expectedTotalCheques,
                            expectedTotalAmount,
                            uploadedZipFile.getAbsolutePath());

            // =================================================
            // STEP 2
            // GET PARSED BATCH
            // =================================================

            ScanBatch scanBatch =
                    parsedData.getScanBatch();

            if (scanBatch == null) {

                throw new RuntimeException(
                        "Batch information could not be parsed.");
            }

            // =================================================
            // STEP 3
            // GET PARSED CHEQUES
            // =================================================

            List<ScanCheque> chequeList =
                    parsedData.getChequeList();

            if (chequeList == null
                    || chequeList.isEmpty()) {

                throw new RuntimeException(
                        "No cheques were found in the uploaded batch.");
            }

            // =================================================
            // STEP 4
            // GET BATCH ID
            // =================================================

            batchId =
                    scanBatch.getScannedBatchId();

            if (batchId == null
                    || batchId.trim().isEmpty()) {

                throw new RuntimeException(
                        "Batch ID was not found in the XML.");
            }

            batchId =
                    batchId.trim();

            // =================================================
            // STEP 5
            // CREATE VALIDATION DATA
            // =================================================

            BatchValidationData validationData =
                    new BatchValidationData();

            validationData.setBatch(
                    scanBatch);

            validationData.setChequeList(
                    chequeList);

            validationData.setExpectedTotalCheques(
                    expectedTotalCheques);

            validationData.setExpectedTotalAmount(
                    expectedTotalAmount);

            // =================================================
            // STEP 6
            // VALIDATE
            // =================================================

            ValidationResult validationResult =
                    batchValidationService.validateBatch(
                            validationData);

            // =================================================
            // STEP 7
            // VALIDATION FAILED
            // =================================================

            if (!validationResult.isValid()) {

                /*
                 * Do not remove the previous session batch.
                 *
                 * The new batch has not been saved yet.
                 */
                showErrorMessage(
                        validationResult.getMessage());

                return;
            }

            // =================================================
            // STEP 8
            // VALIDATION PASSED → SAVE
            // =================================================

            MicrCodeHelper micrCodeHelper =
                    new MicrCodeHelper();

            List<ScanCheque> micrChequeList =
                    micrCodeHelper.checkMicrCode(
                            chequeList);

            String savedBatchId =
                    scanService.saveScanBatch(
                            scanBatch,
                            micrChequeList);

            if (savedBatchId == null
                    || savedBatchId.trim().isEmpty()) {

                throw new RuntimeException(
                        "Batch could not be saved.");
            }

            batchId =
                    savedBatchId.trim();

            // =================================================
            // STEP 9
            // STORE ONLY CURRENT BATCH ID IN SESSION
            // =================================================

            Session session =
                    Sessions.getCurrent();

            if (session != null) {

                session.setAttribute(
                        SESSION_CURRENT_BATCH_ID,
                        batchId);
            }

            // =================================================
            // STEP 10
            // COUNT MICR REPAIR
            // =================================================

            int micrRepairCount =
                    countMicrRepairCheques(
                            chequeList);

            /*
             * This list is kept only for the current controller
             * until the user opens another batch/page.
             *
             * It is NOT stored in the session.
             */
            currentChequeList =
                    new ArrayList<ScanCheque>(
                            chequeList);

            currentChequePage = 0;

            chequeDetailsGroup.setVisible(false);

            lstCheques.getItems().clear();

            // =================================================
            // STEP 11
            // SUCCESS MESSAGE
            // =================================================

            divSuccessMessage.setVisible(true);

            lblSuccessText.setValue(
                    "Batch " + batchId
                            + " has been uploaded successfully.");

            // =================================================
            // STEP 12
            // DISPLAY ONLY NEW CURRENT BATCH
            // =================================================

            displayBatchDetails(
                    scanBatch,
                    chequeList.size(),
                    micrRepairCount);

            // =================================================
            // STEP 13
            // CLEAR INPUTS
            // =================================================

            txtExpectedTotalCheques.setValue(null);

            txtExpectedTotalChequeAmount
                    .setValue(BigDecimal.ZERO);

            txtChequeFolder.setValue("");

            uploadedZipFile = null;

            btnValidateBatch.setDisabled(true);

        } catch (Exception e) {

            // =================================================
            // LOG ERROR
            // =================================================

            e.printStackTrace();

            /*
             * Do not remove the previous batch from the screen
             * or session when the new batch fails.
             */

            // =================================================
            // ERROR MESSAGE
            // =================================================

            String errorMessage =
                    e.getMessage();

            if (errorMessage == null
                    || errorMessage.trim().isEmpty()) {

                errorMessage =
                        "Something went wrong while processing the batch.";
            }

            showErrorMessage(
                    errorMessage);
        }
    }

    // =========================================================
    // COUNT MICR REPAIR CHEQUES
    // =========================================================

    private int countMicrRepairCheques(
            List<ScanCheque> chequeList) {

        int count = 0;

        if (chequeList == null) {
            return count;
        }

        for (ScanCheque cheque :
                chequeList) {

            if (cheque == null) {
                continue;
            }

            String status =
                    cheque.getChequeStatus();

            if ("PENDING_MICR_REPAIR"
                    .equalsIgnoreCase(status)) {

                count++;
            }
        }

        return count;
    }

    // =========================================================
    // ERROR MESSAGE POPUP
    // =========================================================

    private void showErrorMessage(
            String message) {

        final Window errorWindow =
                new Window();

        errorWindow.setId(
                "batchValidationErrorPopup");

        errorWindow.setSclass(
                "batch-validation-error-popup");

        errorWindow.setTitle(
                "Batch Validation Failed");

        errorWindow.setBorder(
                "normal");

        errorWindow.setClosable(false);

        errorWindow.setWidth(
                "450px");

        errorWindow.setHeight(
                "auto");

        // =====================================================
        // CONTENT
        // =====================================================

        Vlayout content =
                new Vlayout();

        content.setSpacing(
                "15px");

        content.setSclass(
                "batch-validation-error-content");

        content.setStyle(
                "padding:20px;");

        // =====================================================
        // MESSAGE
        // =====================================================

        Label messageLabel =
                new Label();

        messageLabel.setValue(
                message);

        messageLabel.setMultiline(
                true);

        messageLabel.setSclass(
                "batch-validation-error-message");

        content.appendChild(
                messageLabel);

        // =====================================================
        // OK BUTTON
        // =====================================================

        Button okButton =
                new Button("OK");

        okButton.setId(
                "batchValidationErrorOk");

        okButton.setSclass(
                "batch-validation-error-ok");

        okButton.addEventListener(
                Events.ON_CLICK,
                new EventListener<Event>() {

                    @Override
                    public void onEvent(
                            Event event) {

                        errorWindow.detach();
                    }
                });

        content.appendChild(
                okButton);

        // =====================================================
        // ADD CONTENT
        // =====================================================

        errorWindow.appendChild(
                content);

        // =====================================================
        // ATTACH TO PAGE
        // =====================================================

        pageRoot.appendChild(
                errorWindow);

        // =====================================================
        // SHOW MODAL
        // =====================================================

        errorWindow.doModal();
    }

    // =========================================================
    // DISPLAY BATCH DETAILS
    // =========================================================

    private void displayBatchDetails(
            ScanBatch scanBatch,
            int actualChequeCount,
            int micrRepairCount) {

        // =====================================================
        // CLEAR OLD DATA
        // =====================================================

        lstBatchDetails.getItems().clear();

        // =====================================================
        // SHOW BATCH SECTION
        // =====================================================

        batchDetailsGroup.setVisible(true);

        // Cheque details remain hidden until the user clicks
        // View Cheques.
        chequeDetailsGroup.setVisible(false);

        // =====================================================
        // CREATE BATCH ROW
        // =====================================================

        Listitem item =
                new Listitem();

        String displayBatchId =
                scanBatch.getScannedBatchId();

        if (displayBatchId == null
                || displayBatchId.trim().isEmpty()) {

            displayBatchId = batchId;
        }

        item.appendChild(
                new Listcell(
                        safe(displayBatchId)));

        item.appendChild(
                new Listcell(
                        formatDate(
                                scanBatch.getUploadedAt())));

        item.appendChild(
                new Listcell(
                        String.valueOf(
                                actualChequeCount)));

        item.appendChild(
                new Listcell(
                        String.valueOf(
                                micrRepairCount)));

        String status =
                scanBatch.getBatchStatus();

        if (status == null
                || status.trim().isEmpty()) {

            status = "PROCESSING";
        }

        item.appendChild(
                new Listcell(status));

        // =====================================================
        // BATCH ACTION = VIEW CHEQUES
        // =====================================================

        Listcell actionCell =
                new Listcell();

        final String selectedBatchId =
                displayBatchId;

        Button viewChequesButton =
                new Button("View Cheques");

        viewChequesButton.setSclass(
                "btn-batch-action");

        viewChequesButton.addEventListener(
                Events.ON_CLICK,
                new EventListener<Event>() {

                    @Override
                    public void onEvent(Event event) {

                        // =================================================
                        // HIDE CHEQUES
                        // =================================================

                        if (chequeDetailsGroup.isVisible()) {

                            chequeDetailsGroup
                                    .setVisible(false);

                            viewChequesButton
                                    .setLabel(
                                            "View Cheques");

                            return;
                        }

                        // =================================================
                        // GET CHEQUES FROM DB
                        // =================================================

                        List<ScanCheque> chequeList =
                                outwardMakerService
                                        .getMakerBatchCheques(
                                                selectedBatchId);

                        if (chequeList == null
                                || chequeList.isEmpty()) {

                            showErrorMessage(
                                    "No cheque information is available for batch "
                                            + selectedBatchId
                                            + ".");

                            return;
                        }

                        /*
                         * Cheques are retrieved from DB.
                         * They are not stored in session.
                         */
                        currentChequeList =
                                new ArrayList<ScanCheque>(
                                        chequeList);

                        batchId =
                                selectedBatchId;

                        currentChequePage = 0;

                        displayChequeDetails();

                        chequeDetailsGroup
                                .setVisible(true);

                        viewChequesButton
                                .setLabel(
                                        "Hide Cheques");
                    }
                });

        actionCell.appendChild(
                viewChequesButton);

        item.appendChild(
                actionCell);

        lstBatchDetails.appendChild(
                item);
    }

    // =========================================================
    // DISPLAY CHEQUE DETAILS
    // =========================================================

    private void displayChequeDetails() {

        if (currentChequeList == null) {

            currentChequeList =
                    new ArrayList<ScanCheque>();
        }

        lstCheques.getItems().clear();

        List<ScanCheque> filteredList =
                getFilteredChequeList();

        int totalCheques =
                filteredList.size();

        int totalPages =
                getTotalPages(totalCheques);

        if (totalPages == 0) {

            currentChequePage = 0;

            lblTotalCheques.setValue(
                    "Total Cheques: 0");

            lblChequePage.setValue(
                    "Page 1");

            btnChequePrevious.setDisabled(true);

            btnChequeNext.setDisabled(true);

            chequeDetailsGroup.setVisible(true);

            return;
        }

        if (currentChequePage >= totalPages) {

            currentChequePage =
                    totalPages - 1;
        }

        int startIndex =
                currentChequePage
                        * CHEQUES_PER_PAGE;

        int endIndex =
                Math.min(
                        startIndex + CHEQUES_PER_PAGE,
                        totalCheques);

        for (int index = startIndex;
                index < endIndex;
                index++) {

            ScanCheque cheque =
                    filteredList.get(index);

            if (cheque == null) {
                continue;
            }

            Listitem item =
                    new Listitem();

            // =================================================
            // CHEQUE ID
            // =================================================

            item.appendChild(
                    new Listcell(
                            safe(
                                    cheque.getScannedChequeId())));

            // =================================================
            // CHEQUE NUMBER
            // =================================================

            item.appendChild(
                    new Listcell(
                            safe(
                                    cheque.getChequeNumber())));

            // =================================================
            // AMOUNT
            // =================================================

            item.appendChild(
                    new Listcell(
                            formatAmount(
                                    cheque.getChequeAmount())));

            // =================================================
            // STATUS
            // =================================================

            String status =
                    cheque.getChequeStatus();

            if (status == null
                    || status.trim().isEmpty()) {

                status = "-";
            }

            item.appendChild(
                    new Listcell(status));

            // =================================================
            // MICR CODE
            // =================================================

            item.appendChild(
                    new Listcell(
                            safe(
                                    cheque.getMicrCode())));

            // =================================================
            // CHEQUE-LEVEL ACTION
            // =================================================

            Listcell actionCell =
                    new Listcell();

            final ScanCheque selectedCheque =
                    cheque;

            final String selectedBatchId =
                    batchId;

            boolean micrRepairRequired =
                    "PENDING_MICR_REPAIR"
                            .equalsIgnoreCase(status);

            // =================================================
            // MICR REPAIR
            // =================================================

            if (micrRepairRequired) {

                Button micrRepairButton =
                        new Button("MICR Repair");

                micrRepairButton.setSclass(
                        "btn-batch-action");

                micrRepairButton.addEventListener(
                        Events.ON_CLICK,
                        new EventListener<Event>() {

                            @Override
                            public void onEvent(
                                    Event event) {

                                openMicrRepair(
                                        "SCAN",
                                        selectedBatchId,
                                        selectedCheque
                                                .getScannedChequeId());
                            }
                        });

                actionCell.appendChild(
                        micrRepairButton);

            } else {

                // =================================================
                // DATA ENTRY
                // =================================================

                Button dataEntryButton =
                        new Button("Data Entry");

                dataEntryButton.setSclass(
                        "btn-batch-action");

                // Data Entry is enabled for every cheque
                // that does not require MICR repair.
                dataEntryButton.setDisabled(false);

                dataEntryButton.addEventListener(
                        Events.ON_CLICK,
                        new EventListener<Event>() {

                            @Override
                            public void onEvent(
                                    Event event) {

                                if (!dataEntryButton
                                        .isDisabled()) {

                                    openDataEntry(
                                            selectedCheque,
                                            selectedBatchId);
                                }
                            }
                        });

                actionCell.appendChild(
                        dataEntryButton);
            }

            item.appendChild(
                    actionCell);

            lstCheques.appendChild(
                    item);
        }

        // =====================================================
        // PAGINATION DISPLAY
        // =====================================================

        lblTotalCheques.setValue(
                "Total Cheques: "
                        + totalCheques);

        lblChequePage.setValue(
                "Page "
                        + (currentChequePage + 1)
                        + " of "
                        + totalPages);

        btnChequePrevious.setDisabled(
                currentChequePage <= 0);

        btnChequeNext.setDisabled(
                currentChequePage >= totalPages - 1);

        chequeDetailsGroup.setVisible(true);
    }

    // =========================================================
    // FILTER CHEQUES
    // =========================================================

    private List<ScanCheque> getFilteredChequeList() {

        List<ScanCheque> filteredList =
                new ArrayList<ScanCheque>();

        String filter = "ALL";

        if (cmbChequeFilter != null
                && cmbChequeFilter.getSelectedItem() != null) {

            Object filterValue =
                    cmbChequeFilter
                            .getSelectedItem()
                            .getValue();

            if (filterValue != null) {

                filter =
                        filterValue.toString();
            }
        }

        for (ScanCheque cheque :
                currentChequeList) {

            if (cheque == null) {
                continue;
            }

            String status =
                    cheque.getChequeStatus();

            if ("MICR_REPAIR"
                    .equalsIgnoreCase(filter)) {

                if ("PENDING_MICR_REPAIR"
                        .equalsIgnoreCase(status)) {

                    filteredList.add(cheque);
                }

            } else if ("NORMAL"
                    .equalsIgnoreCase(filter)) {

                if (!"PENDING_MICR_REPAIR"
                        .equalsIgnoreCase(status)) {

                    filteredList.add(cheque);
                }

            } else {

                // ALL

                filteredList.add(cheque);
            }
        }

        return filteredList;
    }

    // =========================================================
    // PAGINATION HELPERS
    // =========================================================

    private int getTotalPages(
            int totalCheques) {

        if (totalCheques <= 0) {
            return 0;
        }

        return (totalCheques
                + CHEQUES_PER_PAGE
                - 1)
                / CHEQUES_PER_PAGE;
    }

    private int getTotalChequePages() {

        return getTotalPages(
                getFilteredChequeList()
                        .size());
    }

    // =========================================================
    // DATA ENTRY
    // =========================================================

    private void openDataEntry(
            ScanCheque cheque,
            String selectedBatchId) {

        // The current project does not define a
        // Data Entry route in this controller.
        // Keep the cheque-level action here without
        // inventing a route that may not exist.

        System.out.println(
                "Data Entry clicked for cheque: "
                        + (cheque == null
                                ? "null"
                                : cheque.getScannedChequeId())
                        + ", batch: "
                        + selectedBatchId);
    }

    // =========================================================
    // MICR REPAIR
    // =========================================================

    private void openMicrRepair(
            String source,
            String batchId,
            String chequeId) {

        // =====================================================
        // CHECK SOURCE
        // =====================================================

        if (source == null
                || source.trim().isEmpty()) {

            return;
        }

        // =====================================================
        // CHECK BATCH ID
        // =====================================================

        if (batchId == null
                || batchId.trim().isEmpty()) {

            return;
        }

        // =====================================================
        // CHECK CHEQUE ID
        // =====================================================

        if (chequeId == null
                || chequeId.trim().isEmpty()) {

            return;
        }

        // =====================================================
        // GET ROOT
        // =====================================================

        Component root =
                Executions.getCurrent()
                        .getDesktop()
                        .getFirstPage()
                        .getFirstRoot();

        // =====================================================
        // GET MAIN CONTENT AREA
        // =====================================================

        Component mainContentArea =
                root.getFellowIfAny(
                        "mainContentArea",
                        true);

        // =====================================================
        // LOAD MICR REPAIR
        // =====================================================

        if (mainContentArea instanceof Include) {

            Include include =
                    (Include) mainContentArea;

            // =================================================
            // SEND SOURCE
            // =================================================

            include.setAttribute(
                    "MICR_REPAIR_SOURCE",
                    source.trim());

            // =================================================
            // SEND BATCH ID
            // =================================================

            include.setAttribute(
                    "MICR_REPAIR_BATCH_ID",
                    batchId.trim());

            // =================================================
            // SEND CHEQUE ID
            // =================================================

            include.setAttribute(
                    "MICR_REPAIR_CHEQUE_ID",
                    chequeId.trim());

            // =================================================
            // LOAD MICR REPAIR PAGE
            // =================================================

            include.setSrc(
                    "/outward/maker/micr-repair/micr-repair.zul");

            // =================================================
            // LOG DETAILS
            // =================================================

            System.out.println(
                    "MICR REPAIR SOURCE = "
                            + source);

            System.out.println(
                    "MICR REPAIR BATCH ID = "
                            + batchId);

            System.out.println(
                    "MICR REPAIR CHEQUE ID = "
                            + chequeId);
        }
    }

    // =========================================================
    // FORMAT AMOUNT
    // =========================================================

    private String formatAmount(
            BigDecimal amount) {

        if (amount == null) {
            return "-";
        }

        return amount.toPlainString();
    }

    // =========================================================
    // FORMAT DATE
    // =========================================================

    private String formatDate(
            Date date) {

        if (date == null) {
            return "-";
        }

        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "dd-MM-yyyy");

        return formatter.format(date);
    }

    // =========================================================
    // SAFE VALUE
    // =========================================================

    private String safe(
            String value) {

        if (value == null
                || value.trim().isEmpty()) {

            return "-";
        }

        return value.trim();
    }
}