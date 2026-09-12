package com.iispl.cts.controller.outward.maker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;

import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zul.Button;
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
import com.iispl.cts.parser.BatchXmlParser;
import com.iispl.cts.service.outward.BatchValidationService;
import com.iispl.cts.service.outward.ScanService;
import com.iispl.cts.serviceimpl.outward.BatchValidationServiceImpl;
import com.iispl.cts.serviceimpl.outward.ScanServiceImpl;

public class OutwardMakerBatchUploadController implements Composer<Component> {

    private static final long serialVersionUID = 1L;

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

    // =========================================================
    // ROOT COMPONENT
    // =========================================================

    private Component pageRoot;

    // =========================================================
    // SERVICES
    // =========================================================

    private ScanService scanService;

    private BatchValidationService batchValidationService;

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
    public void doAfterCompose(Component component) throws Exception {

        // =====================================================
        // STORE PAGE ROOT
        // =====================================================

        pageRoot = component.getPage().getFirstRoot();

        // =====================================================
        // GET ZUL COMPONENTS
        // =====================================================

        txtExpectedTotalCheques =
                (Intbox) component.getFellow("txtExpectedTotalCheques");

        txtExpectedTotalChequeAmount =
                (Decimalbox) component.getFellow(
                        "txtExpectedTotalChequeAmount");

        txtChequeFolder =
                (Textbox) component.getFellow("txtChequeFolder");

        btnBrowse =
                (Button) component.getFellow("btnBrowse");

        btnValidateBatch =
                (Button) component.getFellow("btnValidateBatch");

        divSuccessMessage =
                (Div) component.getFellow("divSuccessMessage");

        lblSuccessText =
                (Label) component.getFellow("lblSuccessText");

        batchDetailsGroup =
                (Groupbox) component.getFellow("batchDetailsGroup");

        lstBatchDetails =
                (Listbox) component.getFellow("lstBatchDetails");

        // =====================================================
        // CREATE SERVICES
        // =====================================================

        scanService = new ScanServiceImpl();

        batchValidationService =
                new BatchValidationServiceImpl();

        // =====================================================
        // INITIAL PAGE STATE
        // =====================================================

        divSuccessMessage.setVisible(false);

        batchDetailsGroup.setVisible(false);

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
    // HANDLE ZIP UPLOAD
    // =========================================================

    private void handleZipUpload(UploadEvent uploadEvent) {

        Media media = uploadEvent.getMedia();

        if (media == null) {
            return;
        }

        String fileName = media.getName();

        // =====================================================
        // CHECK ZIP
        // =====================================================

        if (fileName == null
                || !fileName.toLowerCase().endsWith(".zip")) {

            showErrorMessage("Please upload a ZIP file.");

            return;
        }

        // =====================================================
        // GET TEMPDATA PATH
        // =====================================================

        String tempDataPath =
                Executions.getCurrent()
                        .getDesktop()
                        .getWebApp()
                        .getRealPath("/TempData");

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

            byte[] buffer = new byte[8192];

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

        txtChequeFolder.setValue(fileName);

        // =====================================================
        // RESET PREVIOUS RESULT
        // =====================================================

        batchId = null;

        divSuccessMessage.setVisible(false);

        batchDetailsGroup.setVisible(false);

        lstBatchDetails
                .getItems()
                .clear();

        // =====================================================
        // ENABLE VALIDATE
        // =====================================================

        btnValidateBatch.setDisabled(false);
    }

    // =========================================================
    // VALIDATE BATCH
    // =========================================================
    //
    // FLOW:
    //
    // Controller
    //     |
    //     | expectedTotalCheques
    //     | expectedTotalAmount
    //     | zipPath
    //     ↓
    // BatchXmlParser
    //     |
    //     | ScanBatch
    //     | List<ScanCheque>
    //     ↓
    // BatchValidationData
    //     ↓
    // BatchValidationService
    //     ↓
    // BatchValidationServiceImpl
    //     ↓
    // Validators
    //     ↓
    // ValidationResult
    //     |
    //     +---- FAIL → showErrorMessage()
    //     |
    //     +---- PASS
    //              ↓
    //           SAVE
    //              ↓
    //        SUCCESS MESSAGE
    //              ↓
    //        BATCH DETAILS
    //
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

                batchDetailsGroup.setVisible(false);

                showErrorMessage(
                        validationResult.getMessage());

                return;
            }

            // =================================================
            // STEP 8
            // VALIDATION PASSED → SAVE
            // =================================================

            String savedBatchId =
                    scanService.saveScanBatch(
                            scanBatch,
                            chequeList);

            if (savedBatchId == null
                    || savedBatchId.trim().isEmpty()) {

                throw new RuntimeException(
                        "Batch could not be saved.");
            }

            batchId =
                    savedBatchId.trim();

            // =================================================
            // STEP 9
            // COUNT MICR REPAIR
            // =================================================

            int micrRepairCount =
                    countMicrRepairCheques(
                            chequeList);

            // =================================================
            // STEP 10
            // SUCCESS MESSAGE
            // =================================================

            divSuccessMessage.setVisible(true);

            lblSuccessText.setValue(
                    "Batch " + batchId
                            + " has been uploaded successfully.");

            // =================================================
            // STEP 11
            // DISPLAY BATCH DETAILS
            // =================================================

            displayBatchDetails(
                    scanBatch,
                    chequeList.size(),
                    micrRepairCount);

            // =================================================
            // STEP 12
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

            // =================================================
            // HIDE DETAILS
            // =================================================

            batchDetailsGroup.setVisible(false);

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

        for (ScanCheque cheque : chequeList) {

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

        lstBatchDetails
                .getItems()
                .clear();

        // =====================================================
        // SHOW SECTION
        // =====================================================

        batchDetailsGroup
                .setVisible(true);

        // =====================================================
        // CREATE ROW
        // =====================================================

        Listitem item =
                new Listitem();

        // =====================================================
        // BATCH ID
        // =====================================================

        String displayBatchId =
                scanBatch.getScannedBatchId();

        if (displayBatchId == null
                || displayBatchId.trim().isEmpty()) {

            displayBatchId =
                    batchId;
        }

        item.appendChild(
                new Listcell(
                        safe(displayBatchId)));

        // =====================================================
        // SCAN DATE
        // =====================================================

        item.appendChild(
                new Listcell(
                        formatDate(
                                scanBatch.getUploadedAt())));

        // =====================================================
        // TOTAL CHEQUES
        // =====================================================

        item.appendChild(
                new Listcell(
                        String.valueOf(
                                actualChequeCount)));

        // =====================================================
        // MICR ERRORS
        // =====================================================

        item.appendChild(
                new Listcell(
                        String.valueOf(
                                micrRepairCount)));

        // =====================================================
        // STATUS
        // =====================================================

        String status =
                scanBatch.getBatchStatus();

        if (status == null
                || status.trim().isEmpty()) {

            status = "PROCESSING";
        }

        item.appendChild(
                new Listcell(status));

        // =====================================================
        // ACTION
        // =====================================================

        Listcell actionCell =
                new Listcell();

        // =====================================================
        // MICR REPAIR
        // =====================================================

        if (micrRepairCount > 0) {

            Button micrRepairButton =
                    new Button("MICR Repair");

            micrRepairButton.setSclass(
                    "btn-batch-action");

            final String selectedBatchId =
                    displayBatchId;

            micrRepairButton.addEventListener(
                    Events.ON_CLICK,
                    new EventListener<Event>() {

                        @Override
                        public void onEvent(
                                Event event) {

                            openMicrRepair(
                                    "SCAN",
                                    selectedBatchId);
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

            final String selectedBatchId =
                    displayBatchId;

            dataEntryButton.addEventListener(
                    Events.ON_CLICK,
                    new EventListener<Event>() {

                        @Override
                        public void onEvent(
                                Event event) {

                            System.out.println(
                                    "Data Entry clicked for batch: "
                                            + selectedBatchId);
                        }
                    });

            actionCell.appendChild(
                    dataEntryButton);
        }

        // =====================================================
        // ADD ACTION
        // =====================================================

        item.appendChild(
                actionCell);

        // =====================================================
        // ADD ROW
        // =====================================================

        lstBatchDetails.appendChild(
                item);
    }

    // =========================================================
    // OPEN MICR REPAIR
    // =========================================================

    private void openMicrRepair(
            String source,
            String batchId) {

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

            include.setAttribute(
                    "MICR_REPAIR_SOURCE",
                    source.trim());

            include.setAttribute(
                    "MICR_REPAIR_BATCH_ID",
                    batchId.trim());

            include.setSrc(
                    "/outward/maker/micr-repair/micr-repair.zul");

            System.out.println(
                    "MICR REPAIR SOURCE = "
                            + source);

            System.out.println(
                    "MICR REPAIR BATCH ID = "
                            + batchId);
        }
    }

    // =========================================================
    // FORMAT DATE
    // =========================================================

    private String formatDate(Date date) {

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

    private String safe(String value) {

        if (value == null
                || value.trim().isEmpty()) {

            return "-";
        }

        return value.trim();
    }
}