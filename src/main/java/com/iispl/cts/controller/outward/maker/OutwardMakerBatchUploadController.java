package com.iispl.cts.controller.outward.maker;
import com.iispl.cts.parser.BatchXmlParser;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zul.Button;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;


/**
 * Controller for Outward Maker Batch Upload.
 *
 * Flow:
 *
 * batch-upload.zul
 *        |
 *        v
 * Controller
 *        |
 *        v
 * BatchXmlParser
 *
 * Upload:
 *     Validate ZIP
 *     -> exactly one XML
 *     -> JPEG cheque images
 *     -> parse XML
 *
 * Validate:
 *     Expected Count vs XML Count
 *     Expected Amount vs XML Amount
 *
 * Match:
 *     Success
 *
 * Mismatch:
 *     Validation popup
 */
public class OutwardMakerBatchUploadController
        implements Composer<Component> {


    private static final long serialVersionUID = 1L;


    // =========================================================
    // ZUL COMPONENTS
    // =========================================================

    private Intbox expectedTotalCheques;

    private Decimalbox expectedTotalChequeAmount;

    private Textbox chequeFolder;

    private Textbox batchNumber;

    private Button browseButton;

    private Button validateBatchButton;

    private Div successMessage;

    private Label successText;

    private Groupbox scannedChequesWindow;

    private Label scannedChequeTitle;

    private Label normalCount;

    private Label micrRepairCount;

    private Listbox chequeList;


    // =========================================================
    // BATCH DATA
    // =========================================================

    private File uploadedBatchFile;

    private String xmlBatchId;

    private int xmlTotalCount;

    private BigDecimal xmlTotalAmount =
            BigDecimal.ZERO;


    private final List<BatchXmlParser.ChequeData> cheques =
            new ArrayList<>();


    // =========================================================
    // COMPOSE
    // =========================================================

    @Override
    public void doAfterCompose(
            Component component)
            throws Exception {


        // =====================================================
        // GET ZUL COMPONENTS
        // =====================================================

        expectedTotalCheques =
                (Intbox) component.getFellow(
                        "expectedTotalCheques");

        expectedTotalChequeAmount =
                (Decimalbox) component.getFellow(
                        "expectedTotalChequeAmount");

        chequeFolder =
                (Textbox) component.getFellow(
                        "chequeFolder");

        batchNumber =
                (Textbox) component.getFellow(
                        "batchNumber");

        browseButton =
                (Button) component.getFellow(
                        "browseButton");

        validateBatchButton =
                (Button) component.getFellow(
                        "validateBatchButton");

        successMessage =
                (Div) component.getFellow(
                        "successMessage");

        successText =
                (Label) component.getFellow(
                        "successText");

        scannedChequesWindow =
                (Groupbox) component.getFellow(
                        "scannedChequesWindow");

        scannedChequeTitle =
                (Label) component.getFellow(
                        "scannedChequeTitle");

        normalCount =
                (Label) component.getFellow(
                        "normalCount");

        micrRepairCount =
                (Label) component.getFellow(
                        "micrRepairCount");

        chequeList =
                (Listbox) component.getFellow(
                        "chequeList");


        // =====================================================
        // INITIAL STATE
        // =====================================================

        successMessage.setVisible(false);

        scannedChequesWindow.setVisible(false);

        validateBatchButton.setDisabled(true);

        batchNumber.setValue("");

        normalCount.setValue(
                "0 NORMAL");

        micrRepairCount.setValue(
                "0 MICR REPAIR");


        // =====================================================
        // COUNT CHANGE
        // =====================================================

        expectedTotalCheques.addEventListener(
                "onChange",
                new EventListener<Event>() {

                    @Override
                    public void onEvent(
                            Event event) {

                        setValidateButtonState();
                    }
                });


        // =====================================================
        // AMOUNT CHANGE
        // =====================================================

        expectedTotalChequeAmount.addEventListener(
                "onChange",
                new EventListener<Event>() {

                    @Override
                    public void onEvent(
                            Event event) {

                        setValidateButtonState();
                    }
                });


        // =====================================================
        // ZIP UPLOAD
        // =====================================================

        browseButton.addEventListener(
                "onUpload",
                new EventListener<Event>() {

                    @Override
                    public void onEvent(
                            Event event) {

                        handleBatchZipUpload(
                                (UploadEvent) event);
                    }
                });


        // =====================================================
        // VALIDATE BUTTON
        // =====================================================

        validateBatchButton.addEventListener(
                "onClick",
                new EventListener<Event>() {

                    @Override
                    public void onEvent(
                            Event event) {

                        validateBatch();
                    }
                });
    }


    // =========================================================
    // ZIP UPLOAD
    // =========================================================

    private void handleBatchZipUpload(
            UploadEvent uploadEvent) {


        Media media =
                uploadEvent.getMedia();


        if (media == null) {
            return;
        }


        String fileName =
                media.getName();


        // =====================================================
        // CHECK ZIP EXTENSION
        // =====================================================

        if (fileName == null
                || !fileName
                        .toLowerCase()
                        .endsWith(".zip")) {


            Messagebox.show(
                    "Only ZIP files are allowed.",
                    "Invalid Batch File",
                    Messagebox.OK,
                    Messagebox.EXCLAMATION);

            return;
        }


        // =====================================================
        // TEMP DATA DIRECTORY
        // =====================================================

        String tempDataPath =
                Executions.getCurrent()
                        .getDesktop()
                        .getWebApp()
                        .getRealPath(
                                "/TempData");


        if (tempDataPath == null) {

            Messagebox.show(
                    "Unable to locate TempData folder.",
                    "Upload Error",
                    Messagebox.OK,
                    Messagebox.ERROR);

            return;
        }


        File tempDataDirectory =
                new File(tempDataPath);


        if (!tempDataDirectory.exists()
                && !tempDataDirectory.mkdirs()) {

            Messagebox.show(
                    "Unable to create TempData folder.",
                    "Upload Error",
                    Messagebox.OK,
                    Messagebox.ERROR);

            return;
        }


        // =====================================================
        // SAVE ZIP
        // =====================================================

        File destinationFile =
                new File(
                        tempDataDirectory,
                        fileName);


        try (
                InputStream inputStream =
                        media.getStreamData();

                FileOutputStream outputStream =
                        new FileOutputStream(
                                destinationFile)
        ) {


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


        } catch (Exception exception) {

            exception.printStackTrace();


            Messagebox.show(
                    "Unable to save batch ZIP file.\n\n"
                            + exception.getMessage(),
                    "Upload Error",
                    Messagebox.OK,
                    Messagebox.ERROR);

            return;
        }


        uploadedBatchFile =
                destinationFile;


        // =====================================================
        // CLEAR PREVIOUS DATA
        // =====================================================

        clearPreviousBatchData();


        // =====================================================
        // DISPLAY ZIP NAME
        // =====================================================

        chequeFolder.setValue(
                fileName);


        // =====================================================
        // READ ZIP
        // =====================================================

        try {

            readBatchZip(
                    uploadedBatchFile);


        } catch (Exception exception) {

            exception.printStackTrace();


            uploadedBatchFile = null;

            chequeFolder.setValue("");

            batchNumber.setValue("");

            clearParsedData();


            Messagebox.show(
                    exception.getMessage(),
                    "Batch Upload Error",
                    Messagebox.OK,
                    Messagebox.ERROR);


            setValidateButtonState();

            return;
        }


        // =====================================================
        // DISPLAY BATCH NUMBER
        // =====================================================

        batchNumber.setValue(
                xmlBatchId);


        // =====================================================
        // ENABLE VALIDATE BUTTON
        // =====================================================

        setValidateButtonState();


        /*
         * IMPORTANT:
         *
         * DO NOT validate here.
         *
         * DO NOT open batch-validation.zul here.
         *
         * User must click Validate Batch.
         */
    }


    // =========================================================
    // READ ZIP
    // =========================================================

    private void readBatchZip(
            File batchFile)
            throws Exception {


        boolean xmlFound = false;

        int jpegCount = 0;


        try (
                InputStream inputStream =
                        new FileInputStream(
                                batchFile);

                ZipInputStream zipInputStream =
                        new ZipInputStream(
                                inputStream)
        ) {


            ZipEntry zipEntry;


            while ((zipEntry =
                    zipInputStream.getNextEntry())
                    != null) {


                // =================================================
                // IGNORE DIRECTORIES
                // =================================================

                if (zipEntry.isDirectory()) {

                    zipInputStream.closeEntry();

                    continue;
                }


                String fileName =
                        zipEntry.getName()
                                .toLowerCase();


                // =================================================
                // XML
                // =================================================

                if (fileName.endsWith(".xml")) {


                    if (xmlFound) {

                        throw new Exception(
                                "Batch ZIP must contain exactly one XML file.");
                    }


                    xmlFound = true;


                    /*
                     * Read XML completely while the ZIP entry
                     * is open.
                     */

                    byte[] xmlBytes =
                            zipInputStream.readAllBytes();


                    // ---------------------------------------------
                    // CONTROLLER -> PARSER
                    // ---------------------------------------------

                    BatchXmlParser parser =
                            new BatchXmlParser();


                    BatchXmlParser.ParsedBatchData
                            parsedData =
                            parser.parse(
                                    xmlBytes);


                    // ---------------------------------------------
                    // GET DATA FROM PARSER
                    // ---------------------------------------------

                    xmlBatchId =
                            parsedData.getBatchId();


                    xmlTotalCount =
                            parsedData.getTotalCount();


                    xmlTotalAmount =
                            parsedData.getTotalAmount();


                    cheques.clear();


                    cheques.addAll(
                            parsedData.getCheques());
                }


                // =================================================
                // CHEQUE IMAGES
                // =================================================

                else if (fileName.endsWith(".jpg")
                        || fileName.endsWith(".jpeg")) {


                    jpegCount++;
                }


                zipInputStream.closeEntry();
            }
        }


        // =====================================================
        // XML REQUIRED
        // =====================================================

        if (!xmlFound) {

            throw new Exception(
                    "No XML file was found in the batch ZIP.");
        }


        // =====================================================
        // CHEQUE IMAGES REQUIRED
        // =====================================================

        if (jpegCount == 0) {

            throw new Exception(
                    "No cheque JPEG images were found in the batch ZIP.");
        }


        // =====================================================
        // FRONT/BACK IMAGE CHECK
        // =====================================================

        if (jpegCount % 2 != 0) {

            throw new Exception(
                    "Cheque front/back JPEG images are incomplete.");
        }
    }


    // =========================================================
    // VALIDATE BATCH
    // =========================================================

    private void validateBatch() {


        // =====================================================
        // EXPECTED COUNT
        // =====================================================

        Integer expectedCount =
                expectedTotalCheques.getValue();


        if (expectedCount == null
                || expectedCount <= 0) {


            Messagebox.show(
                    "Please enter a valid expected cheque count.",
                    "Batch Validation",
                    Messagebox.OK,
                    Messagebox.EXCLAMATION);


            Clients.focus(
                    expectedTotalCheques);

            return;
        }


        // =====================================================
        // EXPECTED AMOUNT
        // =====================================================

        BigDecimal expectedAmount =
                expectedTotalChequeAmount.getValue();


        if (expectedAmount == null
                || expectedAmount.signum() <= 0) {


            Messagebox.show(
                    "Please enter a valid expected total amount.",
                    "Batch Validation",
                    Messagebox.OK,
                    Messagebox.EXCLAMATION);


            Clients.focus(
                    expectedTotalChequeAmount);

            return;
        }


        // =====================================================
        // ZIP CHECK
        // =====================================================

        if (uploadedBatchFile == null
                || !uploadedBatchFile.exists()
                || xmlBatchId == null) {


            Messagebox.show(
                    "Please upload a valid batch ZIP file first.",
                    "Batch Validation",
                    Messagebox.OK,
                    Messagebox.EXCLAMATION);

            return;
        }


        // =====================================================
        // ACTUAL VALUES FROM XML
        // =====================================================

        int actualCount =
                xmlTotalCount;


        BigDecimal actualAmount =
                xmlTotalAmount;


        // =====================================================
        // COMPARE COUNT
        // =====================================================

        boolean countMatched =
                expectedCount == actualCount;


        // =====================================================
        // COMPARE AMOUNT
        // =====================================================

        boolean amountMatched =
                expectedAmount.compareTo(
                        actualAmount) == 0;


        // =====================================================
        // FAILED
        // =====================================================

        if (!countMatched
                || !amountMatched) {


            showValidationPopup(
                    expectedCount,
                    actualCount,
                    expectedAmount,
                    actualAmount);


            return;
        }


        // =====================================================
        // SUCCESS
        // =====================================================

        showSuccessfulBatch();
    }


    // =========================================================
    // VALIDATION POPUP
    // =========================================================

    private void showValidationPopup(
            int expectedCount,
            int actualCount,
            BigDecimal expectedAmount,
            BigDecimal actualAmount) {


        Map<String, Object> validationData =
                new HashMap<>();


        validationData.put(
                "expectedCheques",
                expectedCount);


        validationData.put(
                "xmlTotalCheques",
                actualCount);


        validationData.put(
                "expectedAmount",
                expectedAmount);


        validationData.put(
                "xmlTotalAmount",
                actualAmount);


        validationData.put(
                "chequeCountStatus",
                expectedCount == actualCount
                        ? "Matched"
                        : "Mismatch");


        validationData.put(
                "amountStatus",
                expectedAmount.compareTo(
                        actualAmount) == 0
                        ? "Matched"
                        : "Mismatch");


        /*
         * This is the ONLY place where the validation
         * popup is opened.
         */

        Window validationWindow =
                (Window) Executions.createComponents(
                        "/outward/maker/batch/batch-validation.zul",
                        null,
                        validationData);


        validationWindow.doModal();
    }


    // =========================================================
    // SUCCESS
    // =========================================================

    private void showSuccessfulBatch() {


        int normal = 0;

        int micrRepair = 0;


        for (BatchXmlParser.ChequeData cheque
                : cheques) {


            if ("MICR REPAIR".equalsIgnoreCase(
                    cheque.getUiStatus())) {

                micrRepair++;

            } else {

                normal++;
            }
        }


        // =====================================================
        // SUCCESS TEXT
        // =====================================================

        successText.setValue(
                "Batch "
                        + xmlBatchId
                        + " uploaded successfully — "
                        + xmlTotalCount
                        + " cheques scanned. "
                        + "Normal: "
                        + normal
                        + " | MICR Repair Required: "
                        + micrRepair);


        normalCount.setValue(
                normal + " NORMAL");


        micrRepairCount.setValue(
                micrRepair + " MICR REPAIR");


        scannedChequeTitle.setValue(
                "Batch "
                        + xmlBatchId
                        + " — Scanned Cheques");


        successMessage.setVisible(true);

        scannedChequesWindow.setVisible(true);


        populateChequeList();
    }


    // =========================================================
    // POPULATE CHEQUE LIST
    // =========================================================

    private void populateChequeList() {


        chequeList.getItems().clear();


        for (BatchXmlParser.ChequeData cheque
                : cheques) {


            Listitem item =
                    new Listitem();


            // =================================================
            // ITEM NO
            // =================================================

            item.appendChild(
                    new Listcell(
                            safe(
                                    cheque.getItemNumber())));


            // =================================================
            // PAYEE NAME
            // =================================================

            item.appendChild(
                    new Listcell(
                            safe(
                                    cheque.getPayeeName())));


            // =================================================
            // MICR
            // =================================================

            Listcell micrCell =
                    new Listcell(
                            safe(
                                    cheque.getMicrCode()));


            if ("MICR REPAIR".equalsIgnoreCase(
                    cheque.getUiStatus())) {

                micrCell.setSclass(
                        "micr-error");
            }


            item.appendChild(
                    micrCell);


            // =================================================
            // STATUS
            // =================================================

            Listcell statusCell =
                    new Listcell();


            Label statusLabel =
                    new Label(
                            safe(
                                    cheque.getUiStatus()));


            if ("MICR REPAIR".equalsIgnoreCase(
                    cheque.getUiStatus())) {

                statusLabel.setSclass(
                        "status-badge micr-repair");

            } else {

                statusLabel.setSclass(
                        "status-badge normal");
            }


            statusCell.appendChild(
                    statusLabel);


            item.appendChild(
                    statusCell);


            // =================================================
            // ACTION
            // =================================================

            Listcell actionCell =
                    new Listcell();


            Button actionButton =
                    new Button();


            if ("MICR REPAIR".equalsIgnoreCase(
                    cheque.getUiStatus())) {


                actionButton.setLabel(
                        "Repair");


                actionButton.setSclass(
                        "btn-action-repair");


                final BatchXmlParser.ChequeData
                        selectedCheque =
                        cheque;


                actionButton.addEventListener(
                        "onClick",
                        new EventListener<Event>() {

                            @Override
                            public void onEvent(
                                    Event event) {

                                repairCheque(
                                        selectedCheque);
                            }
                        });


            } else {


                actionButton.setLabel(
                        "View");


                actionButton.setSclass(
                        "btn-action");


                final BatchXmlParser.ChequeData
                        selectedCheque =
                        cheque;


                actionButton.addEventListener(
                        "onClick",
                        new EventListener<Event>() {

                            @Override
                            public void onEvent(
                                    Event event) {

                                viewCheque(
                                        selectedCheque);
                            }
                        });
            }


            actionCell.appendChild(
                    actionButton);


            item.appendChild(
                    actionCell);


            chequeList.appendChild(
                    item);
        }
    }


    // =========================================================
    // VIEW CHEQUE
    // =========================================================

    private void viewCheque(
            BatchXmlParser.ChequeData cheque) {


        Messagebox.show(
                "Item Number : "
                        + safe(
                                cheque.getItemNumber())
                        + "\nPayee Name : "
                        + safe(
                                cheque.getPayeeName())
                        + "\nMICR Code : "
                        + safe(
                                cheque.getMicrCode())
                        + "\nAmount : "
                        + safe(
                                cheque.getAmount())
                        + "\nCheque Date : "
                        + safe(
                                cheque.getChequeDate())
                        + "\nFront Image : "
                        + safe(
                                cheque.getFrontImage())
                        + "\nBack Image : "
                        + safe(
                                cheque.getBackImage()),
                "Cheque Details",
                Messagebox.OK,
                Messagebox.INFORMATION);
    }


    // =========================================================
    // MICR REPAIR
    // =========================================================

    private void repairCheque(
            BatchXmlParser.ChequeData cheque) {


        Messagebox.show(
                "MICR Repair\n\n"
                        + "Item Number : "
                        + safe(
                                cheque.getItemNumber())
                        + "\nMICR Code : "
                        + safe(
                                cheque.getMicrCode())
                        + "\nPayee Name : "
                        + safe(
                                cheque.getPayeeName()),
                "MICR Repair",
                Messagebox.OK,
                Messagebox.INFORMATION);
    }


    // =========================================================
    // BUTTON STATE
    // =========================================================

    private void setValidateButtonState() {


        boolean countValid =
                expectedTotalCheques.getValue() != null
                        && expectedTotalCheques.getValue() > 0;


        boolean amountValid =
                expectedTotalChequeAmount.getValue() != null
                        && expectedTotalChequeAmount
                                .getValue()
                                .signum() > 0;


        boolean zipValid =
                uploadedBatchFile != null
                        && uploadedBatchFile.exists()
                        && xmlBatchId != null
                        && !cheques.isEmpty();


        validateBatchButton.setDisabled(
                !(countValid
                        && amountValid
                        && zipValid));
    }


    // =========================================================
    // CLEAR PREVIOUS BATCH DATA
    // =========================================================

    private void clearPreviousBatchData() {


        xmlBatchId = null;

        xmlTotalCount = 0;

        xmlTotalAmount =
                BigDecimal.ZERO;

        cheques.clear();


        successMessage.setVisible(false);

        scannedChequesWindow.setVisible(false);


        chequeList.getItems().clear();


        batchNumber.setValue("");


        normalCount.setValue(
                "0 NORMAL");


        micrRepairCount.setValue(
                "0 MICR REPAIR");
    }


    // =========================================================
    // CLEAR PARSED DATA
    // =========================================================

    private void clearParsedData() {


        xmlBatchId = null;

        xmlTotalCount = 0;

        xmlTotalAmount =
                BigDecimal.ZERO;

        cheques.clear();


        chequeList.getItems().clear();


        successMessage.setVisible(false);

        scannedChequesWindow.setVisible(false);
    }


    // =========================================================
    // SAFE VALUE
    // =========================================================

    private String safe(
            Object value) {

        return value == null
                ? ""
                : value.toString();
    }
}