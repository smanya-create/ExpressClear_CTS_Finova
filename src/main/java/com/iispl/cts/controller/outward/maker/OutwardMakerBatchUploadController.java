package com.iispl.cts.controller.outward.maker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
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

import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.parser.BatchXmlParser;
import com.iispl.cts.service.outward.OutwardChequeService;
import com.iispl.cts.service.outward.OutwardMakerService;
import com.iispl.cts.service.outward.ScanService;
import com.iispl.cts.serviceimpl.outward.OutwardChequeServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardMakerServiceImpl;
import com.iispl.cts.serviceimpl.outward.ScanServiceImpl;

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
    private Listbox chequeList;

    private Label scannedChequeTitle;
    private Label normalCount;
    private Label micrRepairCount;

    // =========================================================
    // SERVICES
    // =========================================================

    private ScanService scanService;
    private OutwardMakerService outwardMakerService;
    private OutwardChequeService outwardChequeService;

    // =========================================================
    // UPLOADED ZIP
    // =========================================================

    private File uploadedZipFile;

    // =========================================================
    // BATCH IDs
    // =========================================================

    private String batchId;

    private String outwardBatchId;

    // =========================================================
    // COMPOSE
    // =========================================================

    @Override
    public void doAfterCompose(
            Component component) throws Exception {

        // =====================================================
        // Get ZUL components
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

        chequeList =
                (Listbox) component.getFellow(
                        "chequeList");

        scannedChequeTitle =
                (Label) component.getFellow(
                        "scannedChequeTitle");

        normalCount =
                (Label) component.getFellow(
                        "normalCount");

        micrRepairCount =
                (Label) component.getFellow(
                        "micrRepairCount");

        // =====================================================
        // Create services
        // =====================================================

        scanService =
                new ScanServiceImpl();

        outwardMakerService =
                new OutwardMakerServiceImpl();

        outwardChequeService =
                new OutwardChequeServiceImpl();

        // =====================================================
        // Initial page state
        // =====================================================

        batchNumber.setValue("");

        successMessage.setVisible(false);

        scannedChequesWindow.setVisible(false);

        validateBatchButton.setDisabled(true);

        normalCount.setValue(
                "0 NORMAL");

        micrRepairCount.setValue(
                "0 MICR REPAIR");

        scannedChequeTitle.setValue(
                "Scanned Cheques");

        // =====================================================
        // Browse / ZIP upload
        // =====================================================

        browseButton.addEventListener(
                "onUpload",
                new EventListener<Event>() {

                    @Override
                    public void onEvent(
                            Event event) {

                        handleZipUpload(
                                (UploadEvent) event);
                    }
                });

        // =====================================================
        // Validate Batch button
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
        // Check ZIP extension
        // =====================================================

        if (fileName == null
                || !fileName
                        .toLowerCase()
                        .endsWith(".zip")) {

            return;
        }

        // =====================================================
        // Get webapp/TempData path
        // =====================================================

        String tempDataPath =
                Executions.getCurrent()
                        .getDesktop()
                        .getWebApp()
                        .getRealPath(
                                "/TempData");

        if (tempDataPath == null) {
            return;
        }

        File tempDataDirectory =
                new File(tempDataPath);

        // =====================================================
        // Create TempData folder if required
        // =====================================================

        if (!tempDataDirectory.exists()) {

            if (!tempDataDirectory.mkdirs()) {
                return;
            }
        }

        // =====================================================
        // Destination ZIP
        // =====================================================

        File destinationFile =
                new File(
                        tempDataDirectory,
                        fileName);

        // =====================================================
        // Save ZIP ONLY
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
            return;
        }

        // =====================================================
        // Store uploaded ZIP
        // =====================================================

        uploadedZipFile =
                destinationFile;

        // =====================================================
        // Display selected file
        // =====================================================

        chequeFolder.setValue(
                fileName);

        // =====================================================
        // Reset previous validation result
        // =====================================================

        batchId = null;

        outwardBatchId = null;

        batchNumber.setValue("");

        successMessage.setVisible(false);

        scannedChequesWindow.setVisible(false);

        chequeList.getItems().clear();

        normalCount.setValue(
                "0 NORMAL");

        micrRepairCount.setValue(
                "0 MICR REPAIR");

        scannedChequeTitle.setValue(
                "Scanned Cheques");

        // =====================================================
        // Enable Validate Batch
        // =====================================================

        validateBatchButton.setDisabled(false);

        /*
         * IMPORTANT:
         *
         * Uploading the ZIP does NOT:
         *
         * - parse XML
         * - save to database
         * - validate batch
         * - transfer to outward
         *
         * Everything happens only after
         * Validate Batch is clicked.
         */
    }

    // =========================================================
    // VALIDATE BATCH
    // =========================================================

    private void validateBatch() {

        // =====================================================
        // Check ZIP
        // =====================================================

        if (uploadedZipFile == null
                || !uploadedZipFile.exists()) {

            return;
        }

        try {

            // =================================================
            // STEP 1
            // Parse XML
            // =================================================

            BatchXmlParser parser =
                    new BatchXmlParser(
                            scanService);

            /*
             * Parser:
             *
             * ZIP
             *  ↓
             * XML
             *  ↓
             * ScanBatch
             *  ↓
             * ScanCheque
             *  ↓
             * ScanService
             *  ↓
             * Database
             *
             * Parser returns ONLY batchId.
             */

            batchId =
                    parser.parse(
                            uploadedZipFile
                                    .getAbsolutePath());

            // =================================================
            // Check returned batch ID
            // =================================================

            if (batchId == null
                    || batchId.trim().isEmpty()) {

                throw new RuntimeException(
                        "Batch ID was not returned.");
            }

            // =================================================
            // STEP 2
            // Retrieve ScanBatch from database
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
            // Get ACTUAL values from scan_batch
            // =================================================

            int actualChequeCount =
                    scanBatch.getActualChequeCount();

            BigDecimal actualTotalAmount =
                    scanBatch.getActualTotalAmount();

            // =================================================
            // STEP 4
            // Get EXPECTED values entered by user
            // =================================================

            Integer expectedChequeCount =
                    expectedTotalCheques.getValue();

            BigDecimal expectedTotalAmount =
                    expectedTotalChequeAmount.getValue();

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
            // Compare expected vs actual
            // =================================================

            boolean chequeCountValid =
                    expectedChequeCount
                            .intValue()
                    == actualChequeCount;

            boolean amountValid =
                    actualTotalAmount != null
                    && expectedTotalAmount.compareTo(
                            actualTotalAmount) == 0;

            // =================================================
            // STEP 6
            // Validation FAILED
            // =================================================

            if (!chequeCountValid
                    || !amountValid) {

                successMessage.setVisible(false);

                scannedChequesWindow.setVisible(false);

                /*
                 * Redirect to validation page.
                 *
                 * Pass the scanned batch ID so that
                 * batch-validation.zul can retrieve
                 * the required batch information.
                 */

                Executions.sendRedirect(
                        "batch-validation.zul?batchId="
                                + batchId);

                return;
            }

            // =================================================
            // STEP 7
            // Validation PASSED
            // =================================================

            /*
             * At this point:
             *
             * expected count  == scan_batch actual count
             *
             * expected amount == scan_batch actual amount
             *
             * Now transfer the batch to outward.
             */

            outwardBatchId =
                    outwardMakerService.getBatchFromScan(
                            batchId);
            System.out.println(
                    "SCANNED BATCH ID = " + batchId);

            System.out.println(
                    "GENERATED OUTWARD BATCH ID = " + outwardBatchId);

            // =================================================
            // Check returned outward batch ID
            // =================================================

            if (outwardBatchId == null
                    || outwardBatchId
                            .trim()
                            .isEmpty()) {

                throw new RuntimeException(
                        "Outward batch ID was not returned.");
            }

            // =================================================
            // STEP 8
            // Retrieve cheques from outward_cheque
            // =================================================

            List<OutwardCheque> outwardCheques =
            		outwardChequeService
                    .getChequesByBatchId(
                    		outwardBatchId);
            		

            if (outwardCheques == null) {

                throw new RuntimeException(
                        "Unable to retrieve outward cheques.");
            }

            // =================================================
            // STEP 9
            // Display outward cheques
            // =================================================

            scannedChequesWindow.setVisible(true);

            displayOutwardCheques(
                    outwardCheques);

            // =================================================
            // STEP 10
            // SUCCESS
            // =================================================

            successMessage.setVisible(true);

            successText.setValue(
                    "Batch "
                    + batchId
                    + " has been validated and transferred successfully.");

        } catch (Exception e) {

            e.printStackTrace();

            successMessage.setVisible(false);

            scannedChequesWindow.setVisible(false);

            batchId = null;

            outwardBatchId = null;

            batchNumber.setValue("");
        }
    }

    // =========================================================
    // DISPLAY OUTWARD CHEQUES
    // =========================================================

    private void displayOutwardCheques(
            List<OutwardCheque> outwardCheques) {
    	System.out.println(
				"Displaying outward cheques for batch: "
				+ outwardBatchId);
        // =====================================================
        // Clear previous rows
        // =====================================================

        chequeList.getItems().clear();

        int normal = 0;
        int micrRepair = 0;
        int itemNumber = 1;

        // =====================================================
        // Create rows
        // =====================================================

        for (OutwardCheque cheque :
                outwardCheques) {

            if (cheque == null) {
                continue;
            }

            String status =
                    cheque.getChequeStatus();

            // =================================================
            // Count statuses
            // =================================================

            if ("PENDING_MICR_REPAIR"
                    .equalsIgnoreCase(status)) {

                micrRepair++;

            } else {

                normal++;
            }

            // =================================================
            // Create row
            // =================================================

            Listitem item =
                    new Listitem();

            // =================================================
            // ITEM NO.
            // =================================================

            item.appendChild(
                    new Listcell(
                            String.valueOf(
                                    itemNumber++)));

            // =================================================
            // PAYEE NAME
            // =================================================

            item.appendChild(
                    new Listcell(
                            cheque.getPayeeName()));

            // =================================================
            // MICR CODE
            // =================================================

            item.appendChild(
                    new Listcell(
                            cheque.getMicrCode()));

            // =================================================
            // STATUS
            // =================================================

            item.appendChild(
                    new Listcell(
                            status));

            // =================================================
            // ACTION
            // =================================================

            Listcell actionCell =
                    new Listcell();

            // =================================================
            // MICR REPAIR
            // =================================================

            if ("PENDING_MICR_REPAIR"
                    .equalsIgnoreCase(status)) {

                Button micrRepairButton =
                        new Button(
                                "MICR Repair");

                micrRepairButton.setSclass(
                        "btn-action-repair");

                micrRepairButton.setAttribute(
                        "outwardCheque",
                        cheque);

                micrRepairButton.addEventListener(
                        "onClick",
                        new EventListener<Event>() {

                            @Override
                            public void onEvent(
                                    Event event) {

                                /*
                                 * MICR repair functionality
                                 * will be implemented later.
                                 */

                                System.out.println(
                                        "MICR Repair clicked for cheque: "
                                        + cheque
                                                .getOutwardChequeId());
                            }
                        });

                actionCell.appendChild(
                        micrRepairButton);

            }

            // =================================================
            // NORMAL CHEQUE
            // =================================================

            else {

                Button viewButton =
                        new Button("View");

                viewButton.setSclass(
                        "btn-action");

                viewButton.setAttribute(
                        "outwardCheque",
                        cheque);

                viewButton.addEventListener(
                        "onClick",
                        new EventListener<Event>() {

                            @Override
                            public void onEvent(
                                    Event event) {

                                /*
                                 * View functionality
                                 * will be implemented later.
                                 */

                                System.out.println(
                                        "View clicked for cheque: "
                                        + cheque
                                                .getOutwardChequeId());
                            }
                        });

                actionCell.appendChild(
                        viewButton);
            }

            // =================================================
            // Add ACTION cell
            // =================================================

            item.appendChild(
                    actionCell);

            // =================================================
            // Add row
            // =================================================

            chequeList.appendChild(
                    item);
        }

        // =====================================================
        // Update title
        // =====================================================

        scannedChequeTitle.setValue(
                "Outward Cheques ("
                + outwardCheques.size()
                + ")");

        // =====================================================
        // Update counters
        // =====================================================

        normalCount.setValue(
                normal + " NORMAL");

        micrRepairCount.setValue(
                micrRepair
                + " MICR REPAIR");
    }
}