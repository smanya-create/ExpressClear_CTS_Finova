package com.iispl.cts.controller.outward.maker;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Rows;

import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.service.outward.*;
import com.iispl.cts.serviceimpl.outward.OutwardMakerServiceImpl;


public class OutwardMakerMicrRepairViewController
        extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // =========================================================
    // ZUL COMPONENTS
    // =========================================================

    private Div divMicrRepairScanSection;
    private Div divMicrRepairCheckerSection;
    private Div divMicrRepairEmpty;

    private Label lblMicrRepairEmptyTitle;
    private Label lblMicrRepairEmptyMessage;

    private Rows rowsMicrRepairScanBatches;
    private Rows rowsMicrRepairCheckerBatches;

    private Paging pagingMicrRepairScan;
    private Paging pagingMicrRepairChecker;


    // =========================================================
    // SERVICE
    // =========================================================

    private OutwardMakerService outwardMakerService;


    // =========================================================
    // DATA
    // =========================================================

    private List<ScanBatchRow> scanBatchRows =
            new ArrayList<>();

    private List<OutwardBatchRow> checkerBatchRows =
            new ArrayList<>();


    // =========================================================
    // COMPOSE
    // =========================================================

    @Override
    public void doAfterCompose(Component component)
            throws Exception {

        super.doAfterCompose(component);


        // =====================================================
        // Get ZUL components
        // =====================================================

        divMicrRepairScanSection =
                (Div) component.getFellow(
                        "divMicrRepairScanSection");

        divMicrRepairCheckerSection =
                (Div) component.getFellow(
                        "divMicrRepairCheckerSection");

        divMicrRepairEmpty =
                (Div) component.getFellow(
                        "divMicrRepairEmpty");

        lblMicrRepairEmptyTitle =
                (Label) component.getFellow(
                        "lblMicrRepairEmptyTitle");

        lblMicrRepairEmptyMessage =
                (Label) component.getFellow(
                        "lblMicrRepairEmptyMessage");

        rowsMicrRepairScanBatches =
                (Rows) component.getFellow(
                        "rowsMicrRepairScanBatches");

        rowsMicrRepairCheckerBatches =
                (Rows) component.getFellow(
                        "rowsMicrRepairCheckerBatches");

        pagingMicrRepairScan =
                (Paging) component.getFellow(
                        "pagingMicrRepairScan");

        pagingMicrRepairChecker =
                (Paging) component.getFellow(
                        "pagingMicrRepairChecker");


        // =====================================================
        // Create service
        // =====================================================

        outwardMakerService =
                new OutwardMakerServiceImpl();


        // =====================================================
        // Initial state
        // =====================================================

        divMicrRepairScanSection
                .setVisible(false);

        divMicrRepairCheckerSection
                .setVisible(false);

        divMicrRepairEmpty
                .setVisible(false);

        pagingMicrRepairScan
                .setVisible(false);

        pagingMicrRepairChecker
                .setVisible(false);


        // =====================================================
        // Load MICR repair batches
        // =====================================================

        loadMicrRepairBatches();
    }


    // =========================================================
    // LOAD ALL MICR REPAIR BATCHES
    // =========================================================

    private void loadMicrRepairBatches() {

        try {

            scanBatchRows.clear();
            checkerBatchRows.clear();


            // =================================================
            // SCAN SOURCE
            // =================================================

            loadScanMicrRepairBatches();


            // =================================================
            // OUTWARD / CHECKER SOURCE
            // =================================================

            loadCheckerReturnedMicrRepairBatches();


            // =================================================
            // DISPLAY
            // =================================================

            displayScanBatches();

            displayCheckerBatches();


            // =================================================
            // EMPTY MESSAGE
            // =================================================

            boolean noScanBatches =
                    scanBatchRows.isEmpty();

            boolean noCheckerBatches =
                    checkerBatchRows.isEmpty();


            if (noScanBatches
                    && noCheckerBatches) {

                divMicrRepairEmpty
                        .setVisible(true);

                lblMicrRepairEmptyTitle
                        .setValue(
                                "No MICR repair required");

                lblMicrRepairEmptyMessage
                        .setValue(
                                "There are currently no batches requiring MICR repair.");

            } else {

                divMicrRepairEmpty
                        .setVisible(false);
            }


        } catch (Exception e) {

            e.printStackTrace();

            divMicrRepairScanSection
                    .setVisible(false);

            divMicrRepairCheckerSection
                    .setVisible(false);

            divMicrRepairEmpty
                    .setVisible(true);

            lblMicrRepairEmptyTitle
                    .setValue(
                            "Unable to load MICR repair batches");

            lblMicrRepairEmptyMessage
                    .setValue(
                            "Something went wrong while loading MICR repair batches.");
        }
    }


    // =========================================================
    // SCAN MICR REPAIR BATCHES
    // =========================================================

    private void loadScanMicrRepairBatches()
            throws Exception {

        /*
         * IMPORTANT:
         *
         * Controller does NOT call ScanBatchDAO
         * or ScanChequeDAO.
         *
         * Controller calls OutwardMakerService only.
         *
         * OutwardMakerServiceImpl will internally
         * call ScanBatchDAO / ScanChequeDAO.
         */

        List<ScanBatch> scanBatches =
                outwardMakerService
                        .getScanMicrRepairBatches();


        if (scanBatches == null) {
            return;
        }


        for (ScanBatch scanBatch :
                scanBatches) {

            if (scanBatch == null) {
                continue;
            }


            String scannedBatchId =
                    scanBatch.getScannedBatchId();


            if (scannedBatchId == null
                    || scannedBatchId.trim().isEmpty()) {

                continue;
            }


            /*
             * The service handles the ScanChequeDAO
             * internally.
             */
            int micrRepairCount =
                    outwardMakerService
                            .getScanMicrRepairChequeCount(
                                    scannedBatchId);


            if (micrRepairCount > 0) {

                ScanBatchRow row =
                        new ScanBatchRow();

                row.setBatchId(
                        scannedBatchId);

                row.setTotalCheques(
                        scanBatch.getActualChequeCount());

                row.setMicrRepairCount(
                        micrRepairCount);

                row.setStatus(
                        "NEEDS REPAIR");

                scanBatchRows.add(row);
            }
        }
    }


    // =========================================================
    // OUTWARD / CHECKER RETURNED MICR REPAIR BATCHES
    // =========================================================

    private void loadCheckerReturnedMicrRepairBatches()
            throws Exception {

        /*
         * IMPORTANT:
         *
         * Controller does NOT call
         * OutwardBatchDAO or OutwardChequeDAO.
         *
         * OutwardMakerServiceImpl will internally
         * call the required outward DAOs.
         */

        List<OutwardBatch> outwardBatches =
                outwardMakerService
                        .getMakerMicrRepairBatches();


        if (outwardBatches == null) {
            return;
        }


        for (OutwardBatch outwardBatch :
                outwardBatches) {

            if (outwardBatch == null) {
                continue;
            }


            String outwardBatchId =
                    outwardBatch.getOutwardBatchId();


            if (outwardBatchId == null
                    || outwardBatchId.trim().isEmpty()) {

                continue;
            }


            int micrRepairCount =
                    outwardMakerService
                            .getMakerMicrRepairChequeCount(
                                    outwardBatchId);


            if (micrRepairCount > 0) {

                OutwardBatchRow row =
                        new OutwardBatchRow();

                row.setBatchId(
                        outwardBatchId);

                row.setTotalCheques(
                        outwardBatch.getActualChequeCount());

                row.setMicrRepairCount(
                        micrRepairCount);

                row.setStatus(
                        "RETURNED");

                checkerBatchRows.add(row);
            }
        }
    }


    // =========================================================
    // DISPLAY SCAN BATCHES
    // =========================================================

    private void displayScanBatches() {

        rowsMicrRepairScanBatches
                .getChildren()
                .clear();


        if (scanBatchRows.isEmpty()) {

            divMicrRepairScanSection
                    .setVisible(false);

            pagingMicrRepairScan
                    .setVisible(false);

            return;
        }


        divMicrRepairScanSection
                .setVisible(true);

        pagingMicrRepairScan
                .setVisible(
                        scanBatchRows.size() > 10);

        pagingMicrRepairScan
                .setTotalSize(
                        scanBatchRows.size());

        pagingMicrRepairScan
                .setPageSize(10);


        populateScanPage(0);


        pagingMicrRepairScan
                .addEventListener(
                        "onPaging",
                        new EventListener<Event>() {

                            @Override
                            public void onEvent(
                                    Event event) {

                                populateScanPage(
                                        pagingMicrRepairScan
                                                .getActivePage());
                            }
                        });
    }


    // =========================================================
    // DISPLAY CHECKER BATCHES
    // =========================================================

    private void displayCheckerBatches() {

        rowsMicrRepairCheckerBatches
                .getChildren()
                .clear();


        if (checkerBatchRows.isEmpty()) {

            divMicrRepairCheckerSection
                    .setVisible(false);

            pagingMicrRepairChecker
                    .setVisible(false);

            return;
        }


        divMicrRepairCheckerSection
                .setVisible(true);

        pagingMicrRepairChecker
                .setVisible(
                        checkerBatchRows.size() > 10);

        pagingMicrRepairChecker
                .setTotalSize(
                        checkerBatchRows.size());

        pagingMicrRepairChecker
                .setPageSize(10);


        populateCheckerPage(0);


        pagingMicrRepairChecker
                .addEventListener(
                        "onPaging",
                        new EventListener<Event>() {

                            @Override
                            public void onEvent(
                                    Event event) {

                                populateCheckerPage(
                                        pagingMicrRepairChecker
                                                .getActivePage());
                            }
                        });
    }


    // =========================================================
    // POPULATE SCAN PAGE
    // =========================================================

    private void populateScanPage(
            int page) {

        rowsMicrRepairScanBatches
                .getChildren()
                .clear();


        int pageSize = 10;

        int start =
                page * pageSize;

        int end =
                Math.min(
                        start + pageSize,
                        scanBatchRows.size());


        for (int i = start; i < end; i++) {

            ScanBatchRow batch =
                    scanBatchRows.get(i);


            org.zkoss.zul.Row row =
                    new org.zkoss.zul.Row();


            row.appendChild(
                    new Label(
                            safe(batch.getBatchId())));


            row.appendChild(
                    new Label(
                            String.valueOf(
                                    batch.getTotalCheques())));


            row.appendChild(
                    new Label(
                            String.valueOf(
                                    batch.getMicrRepairCount())));


            row.appendChild(
                    new Label(
                            safe(batch.getStatus())));


            Listcell actionCell =
                    new Listcell();


            Button openButton =
                    new Button("OPEN");

            openButton.setSclass(
                    "btn-action-repair");


            final String selectedBatchId =
                    batch.getBatchId();


            openButton.addEventListener(
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
                    openButton);

            row.appendChild(
                    actionCell);


            rowsMicrRepairScanBatches
                    .appendChild(row);
        }
    }


    // =========================================================
    // POPULATE CHECKER PAGE
    // =========================================================

    private void populateCheckerPage(
            int page) {

        rowsMicrRepairCheckerBatches
                .getChildren()
                .clear();


        int pageSize = 10;

        int start =
                page * pageSize;

        int end =
                Math.min(
                        start + pageSize,
                        checkerBatchRows.size());


        for (int i = start; i < end; i++) {

            OutwardBatchRow batch =
                    checkerBatchRows.get(i);


            org.zkoss.zul.Row row =
                    new org.zkoss.zul.Row();


            row.appendChild(
                    new Label(
                            safe(batch.getBatchId())));


            row.appendChild(
                    new Label(
                            String.valueOf(
                                    batch.getTotalCheques())));


            row.appendChild(
                    new Label(
                            String.valueOf(
                                    batch.getMicrRepairCount())));


            row.appendChild(
                    new Label(
                            safe(batch.getStatus())));


            Listcell actionCell =
                    new Listcell();


            Button openButton =
                    new Button("OPEN");

            openButton.setSclass(
                    "btn-action-repair");


            final String selectedBatchId =
                    batch.getBatchId();


            openButton.addEventListener(
                    "onClick",
                    new EventListener<Event>() {

                        @Override
                        public void onEvent(
                                Event event) {

                            openMicrRepair(
                                    "OUTWARD",
                                    selectedBatchId);
                        }
                    });


            actionCell.appendChild(
                    openButton);

            row.appendChild(
                    actionCell);


            rowsMicrRepairCheckerBatches
                    .appendChild(row);
        }
    }


    // =========================================================
    // OPEN MICR REPAIR PAGE
    // =========================================================

    private void openMicrRepair(
            String source,
            String batchId) {

        String url =
                "micr-repair.zul"
                + "?source="
                + source
                + "&batchId="
                + batchId;


        Executions.sendRedirect(url);
    }


    // =========================================================
    // SAFE VALUE
    // =========================================================

    private String safe(String value) {

        if (value == null) {
            return "-";
        }

        return value;
    }


    // =========================================================
    // SCAN BATCH ROW
    // =========================================================

    private static class ScanBatchRow {

        private String batchId;
        private int totalCheques;
        private int micrRepairCount;
        private String status;


        public String getBatchId() {
            return batchId;
        }

        public void setBatchId(
                String batchId) {

            this.batchId = batchId;
        }


        public int getTotalCheques() {
            return totalCheques;
        }

        public void setTotalCheques(
                int totalCheques) {

            this.totalCheques =
                    totalCheques;
        }


        public int getMicrRepairCount() {
            return micrRepairCount;
        }

        public void setMicrRepairCount(
                int micrRepairCount) {

            this.micrRepairCount =
                    micrRepairCount;
        }


        public String getStatus() {
            return status;
        }

        public void setStatus(
                String status) {

            this.status = status;
        }
    }


    // =========================================================
    // OUTWARD BATCH ROW
    // =========================================================

    private static class OutwardBatchRow {

        private String batchId;
        private int totalCheques;
        private int micrRepairCount;
        private String status;


        public String getBatchId() {
            return batchId;
        }

        public void setBatchId(
                String batchId) {

            this.batchId = batchId;
        }


        public int getTotalCheques() {
            return totalCheques;
        }

        public void setTotalCheques(
                int totalCheques) {

            this.totalCheques =
                    totalCheques;
        }


        public int getMicrRepairCount() {
            return micrRepairCount;
        }

        public void setMicrRepairCount(
                int micrRepairCount) {

            this.micrRepairCount =
                    micrRepairCount;
        }


        public String getStatus() {
            return status;
        }

        public void setStatus(
                String status) {

            this.status = status;
        }
    }
}