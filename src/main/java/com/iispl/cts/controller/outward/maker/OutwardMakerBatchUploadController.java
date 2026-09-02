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

import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.parser.BatchXmlParser;
import com.iispl.cts.service.outward.ScanService;
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
    // SERVICE
    // =========================================================

    private ScanService scanService;

    // =========================================================
    // UPLOADED ZIP
    // =========================================================

    private File uploadedZipFile;

    private String batchId;

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
        // Create service
        // =====================================================

        scanService =
                new ScanServiceImpl();

        // =====================================================
        // Initial page state
        // =====================================================

        batchNumber.setValue("");

        successMessage.setVisible(false);

        scannedChequesWindow.setVisible(false);

        validateBatchButton.setDisabled(true);

        normalCount.setValue("0 NORMAL");

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
         * DO NOT call parser here.
         *
         * DO NOT call ScanService here.
         *
         * DO NOT perform database operation here.
         *
         * DO NOT perform validation here.
         *
         * Everything happens only after the user
         * clicks Validate Batch.
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
             * Parser will:
             *
             * ZIP
             *  ↓
             * XML
             *  ↓
             * ScanBatch
             *  ↓
             * List<ScanCheque>
             *  ↓
             * ScanService
             *  ↓
             * Database
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
            // Get batch from DB
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
            // Get cheques from DB
            // =================================================

            List<ScanCheque> scannedCheques =
                    scanService.getChequesByBatchId(
                            batchId);

            if (scannedCheques == null) {

                throw new RuntimeException(
                        "Unable to retrieve cheques.");
            }

            // =================================================
            // STEP 4
            // Validate cheque count
            // =================================================

            int expectedChequeCount =
                    scanBatch.getActualChequeCount();

            int actualChequeCount =
                    scannedCheques.size();

            boolean chequeCountValid =
                    expectedChequeCount
                            == actualChequeCount;

            // =================================================
            // STEP 5
            // Validate total amount
            // =================================================

            BigDecimal expectedTotalAmount =
                    scanBatch.getActualTotalAmount();

            BigDecimal actualTotalAmount =
                    BigDecimal.ZERO;

            for (ScanCheque cheque :
                    scannedCheques) {

                if (cheque != null
                        && cheque.getChequeAmount()
                                != null) {

                    actualTotalAmount =
                            actualTotalAmount.add(
                                    cheque.getChequeAmount());
                }
            }

            boolean amountValid =
                    expectedTotalAmount != null
                            && expectedTotalAmount
                                    .compareTo(
                                            actualTotalAmount) == 0;

            // =================================================
            // STEP 6
            // Update expected values on page
            // =================================================

            expectedTotalCheques.setValue(
                    expectedChequeCount);

            expectedTotalChequeAmount.setValue(
                    expectedTotalAmount);

            // =================================================
            // STEP 7
            // Validation PASSED
            // =================================================

            if (chequeCountValid
                    && amountValid) {

                /*
                 * ---------------------------------------------
                 * Show success message on SAME PAGE
                 * ---------------------------------------------
                 */

                successMessage.setVisible(true);

                successText.setValue(
                        "Batch "
                        + batchId
                        + " has been validated successfully.");

                /*
                 * ---------------------------------------------
                 * Show scanned cheque list
                 * ---------------------------------------------
                 */

                scannedChequesWindow.setVisible(true);

                /*
                 * ---------------------------------------------
                 * Populate cheque list
                 * ---------------------------------------------
                 */

                displayScannedCheques(
                        scannedCheques);

                /*
                 * IMPORTANT:
                 *
                 * No Messagebox.show() here.
                 */
            }

            // =================================================
            // STEP 8
            // Validation FAILED
            // =================================================

            else {

                /*
                 * Keep success message hidden.
                 */

                successMessage.setVisible(false);

                /*
                 * Keep cheque list hidden because
                 * validation did not pass.
                 */

                scannedChequesWindow.setVisible(false);

                /*
                 * For now we are not showing a popup.
                 *
                 * A validation-error section can be added
                 * to the ZUL later.
                 */
            }

        } catch (Exception e) {

            e.printStackTrace();

            /*
             * Hide success/list when processing fails.
             */

            successMessage.setVisible(false);

            scannedChequesWindow.setVisible(false);

            batchId = null;

            batchNumber.setValue("");
        }
    }

    // =========================================================
    // DISPLAY SCANNED CHEQUES
    // =========================================================

    private void displayScannedCheques(
            List<ScanCheque> scannedCheques) {

        /*
         * Clear previous UI rows.
         */

        chequeList.getItems().clear();

        int normal = 0;

        int micrRepair = 0;

        int itemNumber = 1;

        // =====================================================
        // Create list rows
        // =====================================================

        for (ScanCheque cheque :
                scannedCheques) {

            if (cheque == null) {
                continue;
            }

            String status =
                    cheque.getChequeStatus();

            // -------------------------------------------------
            // Count status
            // -------------------------------------------------

            if ("MICR_REPAIR_REQUIRED"
                    .equalsIgnoreCase(status)) {

                micrRepair++;

            } else {

                normal++;
            }

            // -------------------------------------------------
            // Create row
            // -------------------------------------------------

            Listitem item =
                    new Listitem();

            // -------------------------------------------------
            // ITEM NO.
            // -------------------------------------------------

            item.appendChild(
                    new Listcell(
                            String.valueOf(
                                    itemNumber++)));

            // -------------------------------------------------
            // PAYEE NAME
            // -------------------------------------------------

            item.appendChild(
                    new Listcell(
                            cheque.getPayeeName()));

            // -------------------------------------------------
            // MICR CODE
            // -------------------------------------------------

            item.appendChild(
                    new Listcell(
                            cheque.getMicrCode()));

            // -------------------------------------------------
            // STATUS
            // -------------------------------------------------

            item.appendChild(
                    new Listcell(
                            status));

            // -------------------------------------------------
            // ACTION
            //
            // Not implemented yet.
            // -------------------------------------------------

            item.appendChild(
                    new Listcell(""));

            // -------------------------------------------------
            // Add row
            // -------------------------------------------------

            chequeList.appendChild(
                    item);
        }

        // =====================================================
        // Update title
        // =====================================================

        scannedChequeTitle.setValue(
                "Scanned Cheques ("
                + scannedCheques.size()
                + ")");

        // =====================================================
        // Update status counters
        // =====================================================

        normalCount.setValue(
                normal + " NORMAL");

        micrRepairCount.setValue(
                micrRepair + " MICR REPAIR");
    }
}