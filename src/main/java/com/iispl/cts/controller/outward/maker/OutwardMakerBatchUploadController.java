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
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Include;

import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.parser.BatchXmlParser;
import com.iispl.cts.service.outward.ScanService;
import com.iispl.cts.serviceimpl.outward.ScanServiceImpl;

public class OutwardMakerBatchUploadController implements Composer<Component> {

    private static final long serialVersionUID = 1L;

    // =========================================================
    // ZUL COMPONENTS
    // =========================================================

    private Intbox txtExpectedTotalCheques;
    private Decimalbox txtExpectedTotalChequeAmount;

    private Textbox txtChequeFolder;
    private Textbox txtBatchNumber;

    private Button btnBrowse;
    private Button btnValidateBatch;

    private Div divSuccessMessage;
    private Label lblSuccessText;

    private Groupbox batchDetailsGroup;
    private Listbox lstBatchDetails;

    // =========================================================
    // SERVICE
    // =========================================================

    private ScanService scanService;

    // =========================================================
    // UPLOADED ZIP
    // =========================================================

    private File uploadedZipFile;

    // =========================================================
    // BATCH ID
    // =========================================================

    private String batchId;

    // =========================================================
    // COMPOSE
    // =========================================================

    @Override
    public void doAfterCompose(Component component) throws Exception {

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

        txtBatchNumber =
                (Textbox) component.getFellow(
                        "txtBatchNumber");

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

        // =====================================================
        // CREATE SERVICE
        // =====================================================

        scanService = new ScanServiceImpl();

        // =====================================================
        // INITIAL PAGE STATE
        // =====================================================

        txtBatchNumber.setValue("");

        divSuccessMessage.setVisible(false);

        batchDetailsGroup.setVisible(false);

        btnValidateBatch.setDisabled(true);

        // =====================================================
        // BROWSE / ZIP UPLOAD
        // =====================================================

        btnBrowse.addEventListener(
                "onUpload",
                new EventListener<Event>() {

                    @Override
                    public void onEvent(Event event) {

                        handleZipUpload(
                                (UploadEvent) event);
                    }
                });

        // =====================================================
        // VALIDATE BATCH BUTTON
        // =====================================================

        btnValidateBatch.addEventListener(
                "onClick",
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

    private void handleZipUpload(
            UploadEvent uploadEvent) {

        Media media = uploadEvent.getMedia();

        if (media == null) {
            return;
        }

        String fileName = media.getName();

        // =====================================================
        // CHECK ZIP EXTENSION
        // =====================================================

        if (fileName == null
                || !fileName.toLowerCase().endsWith(".zip")) {

            return;
        }

        // =====================================================
        // GET WEBAPP / TEMPDATA PATH
        // =====================================================

        String tempDataPath =
                Executions.getCurrent()
                        .getDesktop()
                        .getWebApp()
                        .getRealPath("/TempData");

        if (tempDataPath == null) {
            return;
        }

        File tempDataDirectory =
                new File(tempDataPath);

        // =====================================================
        // CREATE TEMPDATA FOLDER
        // =====================================================

        if (!tempDataDirectory.exists()) {

            if (!tempDataDirectory.mkdirs()) {
                return;
            }
        }

        // =====================================================
        // DESTINATION ZIP
        // =====================================================

        File destinationFile =
                new File(
                        tempDataDirectory,
                        fileName);

        // =====================================================
        // SAVE ZIP ONLY
        // =====================================================

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

            while (
                    (bytesRead =
                            inputStream.read(buffer))
                            != -1
            ) {

                outputStream.write(
                        buffer,
                        0,
                        bytesRead);
            }

            outputStream.flush();

        } catch (Exception e) {

            e.printStackTrace();
            return;
        }

        // =====================================================
        // STORE UPLOADED ZIP
        // =====================================================

        uploadedZipFile =
                destinationFile;

        // =====================================================
        // DISPLAY SELECTED FILE
        // =====================================================

        txtChequeFolder.setValue(
                fileName);

        // =====================================================
        // RESET PREVIOUS RESULT
        // =====================================================

        batchId = null;

        txtBatchNumber.setValue("");

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

    private void validateBatch() {

        // =====================================================
        // CHECK ZIP
        // =====================================================

        if (uploadedZipFile == null
                || !uploadedZipFile.exists()) {

            return;
        }

        try {

            // =================================================
            // STEP 1
            // PARSE XML
            // =================================================

            BatchXmlParser parser =
                    new BatchXmlParser(
                            scanService);

            /*
             * ZIP
             *   ↓
             * XML
             *   ↓
             * ScanBatch
             *   ↓
             * ScanCheque
             *   ↓
             * ScanService
             *   ↓
             * scan_batch / scan_cheque
             *
             * Parser returns batchId.
             */

            batchId =
                    parser.parse(
                            uploadedZipFile
                                    .getAbsolutePath());

            // =================================================
            // CHECK BATCH ID
            // =================================================

            if (batchId == null
                    || batchId.trim().isEmpty()) {

                throw new RuntimeException(
                        "Batch ID was not returned.");
            }

            batchId = batchId.trim();

            // =================================================
            // DISPLAY BATCH NUMBER
            // =================================================

            txtBatchNumber.setValue(
                    batchId);

            // =================================================
            // STEP 2
            // GET SCAN BATCH
            // =================================================

            ScanBatch scanBatch =
                    scanService.getBatchById(
                            batchId);

            if (scanBatch == null) {

                throw new RuntimeException(
                        "Batch not found in database: "
                                + batchId);
            }

            // =================================================
            // STEP 3
            // GET ACTUAL VALUES
            // =================================================

            int actualChequeCount =
                    scanBatch
                            .getActualChequeCount();

            BigDecimal actualTotalAmount =
                    scanBatch
                            .getActualTotalAmount();

            // =================================================
            // STEP 4
            // GET EXPECTED VALUES
            // =================================================

            Integer expectedChequeCount =
                    txtExpectedTotalCheques
                            .getValue();

            BigDecimal expectedTotalAmount =
                    txtExpectedTotalChequeAmount
                            .getValue();

            if (expectedChequeCount == null) {

                throw new RuntimeException(
                        "Expected cheque count is required.");
            }

            if (expectedTotalAmount == null) {

                throw new RuntimeException(
                        "Expected total amount is required.");
            }

            // =================================================
            // STEP 5
            // VALIDATE COUNT
            // =================================================

            boolean chequeCountValid =
                    expectedChequeCount.intValue()
                            == actualChequeCount;

            // =================================================
            // STEP 6
            // VALIDATE AMOUNT
            // =================================================

            boolean amountValid =
                    actualTotalAmount != null
                            && expectedTotalAmount
                                    .compareTo(
                                            actualTotalAmount)
                                    == 0;

            // =================================================
            // STEP 7
            // VALIDATION FAILED
            // =================================================

            if (!chequeCountValid
                    || !amountValid) {

                divSuccessMessage
                        .setVisible(false);

                batchDetailsGroup
                        .setVisible(false);

                Executions.sendRedirect(
                        "batch-validation.zul?batchId="
                                + batchId);

                return;
            }

            // =================================================
            // STEP 8
            // GET ALL CHEQUES
            // =================================================

            List<ScanCheque> scanCheques =
                    scanService
                            .getChequesByBatchId(
                                    batchId);

            if (scanCheques == null) {

                throw new RuntimeException(
                        "Unable to retrieve scan cheques.");
            }

            // =================================================
            // STEP 9
            // COUNT MICR REPAIR CHEQUES
            // =================================================

            int micrRepairCount = 0;

            for (ScanCheque cheque :
                    scanCheques) {

                if (cheque == null) {
                    continue;
                }

                String status =
                        cheque.getChequeStatus();

                if ("PENDING_MICR_REPAIR"
                        .equalsIgnoreCase(status)) {

                    micrRepairCount++;
                }
            }

            // =================================================
            // STEP 10
            // DISPLAY ONLY BATCH INFORMATION
            // =================================================

            displayBatchDetails(
                    scanBatch,
                    actualChequeCount,
                    micrRepairCount);

            // =================================================
            // STEP 11
            // SUCCESS MESSAGE
            // =================================================

            divSuccessMessage
                    .setVisible(true);

            lblSuccessText.setValue(
                    "Batch "
                            + batchId
                            + " has been validated successfully.");

            // =================================================
            // STEP 12
            // CLEAR INPUT FIELDS
            //
            // IMPORTANT:
            //
            // Batch Number is NOT cleared.
            // =================================================

            txtExpectedTotalCheques
                    .setValue(null);

            txtExpectedTotalChequeAmount
                    .setValue(BigDecimal.ZERO);

            txtChequeFolder
                    .setValue("");

            uploadedZipFile = null;

            btnValidateBatch
                    .setDisabled(true);

        } catch (Exception e) {

            e.printStackTrace();

            divSuccessMessage
                    .setVisible(false);

            batchDetailsGroup
                    .setVisible(false);

            /*
             * Do NOT clear Batch Number here if the
             * batch ID was successfully returned.
             */

            lblSuccessText.setValue(
                    "Something went wrong while processing the batch. Please try again.");
        }
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
        // BATCH
        // =====================================================

        String displayBatchId =
                scanBatch.getScannedBatchId();

        if (displayBatchId == null
                || displayBatchId.trim().isEmpty()) {

            displayBatchId = batchId;
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
                new Listcell(
                        status));

        // =====================================================
        // ACTION
        // =====================================================

        Listcell actionCell =
                new Listcell();

        // =====================================================
        // MICR REPAIR REQUIRED
        // =====================================================

        if (micrRepairCount > 0) {

            Button micrRepairButton =
                    new Button(
                            "MICR Repair");

            micrRepairButton.setSclass(
                    "btn-batch-action");

            final String selectedBatchId =
                    displayBatchId;

            micrRepairButton
                    .addEventListener(
                            "onClick",
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
            // NO MICR REPAIR
            // =================================================

            Button dataEntryButton =
                    new Button(
                            "Data Entry");

            dataEntryButton.setSclass(
                    "btn-batch-action");

            final String selectedBatchId =
                    displayBatchId;

            dataEntryButton
                    .addEventListener(
                            "onClick",
                            new EventListener<Event>() {

                                @Override
                                public void onEvent(
                                        Event event) {

                                    /*
                                     * Data Entry navigation
                                     * can be connected here once
                                     * the exact Data Entry include
                                     * path/attributes are finalized.
                                     */

                                    System.out.println(
                                            "Data Entry clicked for batch: "
                                                    + selectedBatchId);
                                }
                            });

            actionCell.appendChild(
                    dataEntryButton);
        }

        // =====================================================
        // ADD ACTION CELL
        // =====================================================

        item.appendChild(
                actionCell);

        // =====================================================
        // ADD ROW
        // =====================================================

        lstBatchDetails
                .appendChild(item);
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
        // LOAD MICR REPAIR THROUGH INCLUDE
        // =====================================================

        if (mainContentArea
                instanceof Include) {

            Include include =
                    (Include) mainContentArea;

            // =================================================
            // PASS SOURCE
            // =================================================

            include.setAttribute(
                    "MICR_REPAIR_SOURCE",
                    source.trim());

            // =================================================
            // PASS BATCH ID
            // =================================================

            include.setAttribute(
                    "MICR_REPAIR_BATCH_ID",
                    batchId.trim());

            // =================================================
            // LOAD MICR REPAIR ZUL
            // =================================================

            include.setSrc(
                    "/outward/maker/micr-repair/micr-repair.zul"
            );

            // =================================================
            // DEBUG
            // =================================================

            System.out.println(
                    "MICR REPAIR SOURCE = "
                            + source);

            System.out.println(
                    "MICR REPAIR BATCH ID = "
                            + batchId);

            System.out.println(
                    "MICR REPAIR ZUL = "
                            + "/outward/maker/micr-repair/micr-repair.zul");
        }
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