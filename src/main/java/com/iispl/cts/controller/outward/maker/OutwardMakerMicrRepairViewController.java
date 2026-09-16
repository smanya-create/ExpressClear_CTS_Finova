package com.iispl.cts.controller.outward.maker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;

import com.iispl.cts.dto.MicrRepairBatch;
import com.iispl.cts.service.outward.OutwardMakerService;
import com.iispl.cts.serviceimpl.outward.OutwardMakerServiceImpl;


public class OutwardMakerMicrRepairViewController
        extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;


    // =========================================================
    // ZUL COMPONENTS
    // =========================================================

    private Component sidebarComponent;

    private Div divMicrRepairScanSection;
    private Div divMicrRepairCheckerSection;
    private Div divMicrRepairEmpty;
    private Grid grdMicrRepairBatches;

    private Label lblMicrRepairEmptyTitle;
    private Label lblMicrRepairEmptyMessage;

    /*
     * Main visible table
     */
    private Rows rowsMicrRepairScanBatches;

    /*
     * Kept because the current ZUL/controller structure expects it.
     * It will no longer be displayed as a second table.
     */
    private Rows rowsMicrRepairCheckerBatches;

    private Paging pagingMicrRepairScan;
    private Paging pagingMicrRepairChecker;


    // =========================================================
    // NEW FILTER COMPONENTS
    // =========================================================

    private Textbox txtMicrRepairSearch;

    private Button btnMicrRepairSearch;

    private Button btnMicrRepairClear;

    private Combobox cmbMicrRepairBatchType;

    private Combobox cmbMicrRepairRowsPerPage;

    private Label lblMicrRepairShowing;


    // =========================================================
    // SERVICE
    // =========================================================

    private OutwardMakerService outwardMakerService;


    // =========================================================
    // ORIGINAL DATA
    // =========================================================

    private List<MicrRepairBatch> scanBatchRows =
            new ArrayList<MicrRepairBatch>();

    private List<MicrRepairBatch> checkerBatchRows =
            new ArrayList<MicrRepairBatch>();


    // =========================================================
    // UNIFIED DISPLAY DATA
    // =========================================================

    private List<MicrRepairBatchDisplay> allBatchRows =
            new ArrayList<MicrRepairBatchDisplay>();

    private List<MicrRepairBatchDisplay> filteredBatchRows =
            new ArrayList<MicrRepairBatchDisplay>();


    // =========================================================
    // INTERNAL DISPLAY OBJECT
    // =========================================================

    private static class MicrRepairBatchDisplay {

        private MicrRepairBatch batch;

        /*
         * SCAN     -> batch came from scanning
         * OUTWARD  -> batch was returned by checker
         */
        private String source;


        MicrRepairBatchDisplay(
                MicrRepairBatch batch,
                String source) {

            this.batch = batch;
            this.source = source;
        }


        public MicrRepairBatch getBatch() {
            return batch;
        }


        public String getSource() {
            return source;
        }
    }


    // =========================================================
    // COMPOSE
    // =========================================================

    @Override
    public void doAfterCompose(Component component) throws Exception {

        super.doAfterCompose(component);


        // =====================================================
        // EXISTING COMPONENTS
        // =====================================================

        divMicrRepairScanSection =
                (Div) component.getFellow("divMicrRepairScanSection");

        divMicrRepairCheckerSection =
                (Div) component.getFellow("divMicrRepairCheckerSection");

        divMicrRepairEmpty =
                (Div) component.getFellow("divMicrRepairEmpty");


        lblMicrRepairEmptyTitle =
                (Label) component.getFellow("lblMicrRepairEmptyTitle");

        lblMicrRepairEmptyMessage =
                (Label) component.getFellow("lblMicrRepairEmptyMessage");

        grdMicrRepairBatches =
                (Grid) component.getFellow("grdMicrRepairBatches");


        rowsMicrRepairScanBatches =
                (Rows) component.getFellow("rowsMicrRepairScanBatches");

        rowsMicrRepairCheckerBatches =
                (Rows) component.getFellow("rowsMicrRepairCheckerBatches");


        pagingMicrRepairScan =
                (Paging) component.getFellow("pagingMicrRepairScan");

        pagingMicrRepairChecker =
                (Paging) component.getFellow("pagingMicrRepairChecker");


        // =====================================================
        // NEW FILTER COMPONENTS
        // =====================================================

        txtMicrRepairSearch =
                (Textbox) component.getFellow("txtMicrRepairSearch");

        btnMicrRepairSearch =
                (Button) component.getFellow("btnMicrRepairSearch");

        btnMicrRepairClear =
                (Button) component.getFellow("btnMicrRepairClear");

        cmbMicrRepairBatchType =
                (Combobox) component.getFellow("cmbMicrRepairBatchType");

        cmbMicrRepairRowsPerPage =
                (Combobox) component.getFellow("cmbMicrRepairRowsPerPage");

        lblMicrRepairShowing =
                (Label) component.getFellow("lblMicrRepairShowing");


        // =====================================================
        // CREATE SERVICE
        // =====================================================

        outwardMakerService =
                new OutwardMakerServiceImpl();


        // =====================================================
        // INITIAL STATE
        // =====================================================

        divMicrRepairScanSection.setVisible(false);

        divMicrRepairCheckerSection.setVisible(false);

        divMicrRepairEmpty.setVisible(false);

        pagingMicrRepairScan.setVisible(false);

        pagingMicrRepairChecker.setVisible(false);


        // =====================================================
        // DEFAULT FILTER
        // =====================================================

        selectDefaultBatchType();

        selectDefaultRowsPerPage();


        // =====================================================
        // FILTER EVENTS
        // =====================================================

        btnMicrRepairSearch.addEventListener(
                "onClick",
                new EventListener<Event>() {

                    @Override
                    public void onEvent(Event event)
                            throws Exception {

                        applyFilters();
                    }
                });


        btnMicrRepairClear.addEventListener(
                "onClick",
                new EventListener<Event>() {

                    @Override
                    public void onEvent(Event event)
                            throws Exception {

                        clearFilters();
                    }
                });


        cmbMicrRepairBatchType.addEventListener(
                "onSelect",
                new EventListener<Event>() {

                    @Override
                    public void onEvent(Event event)
                            throws Exception {

                        applyFilters();
                    }
                });


        cmbMicrRepairRowsPerPage.addEventListener(
                "onSelect",
                new EventListener<Event>() {

                    @Override
                    public void onEvent(Event event)
                            throws Exception {

                        refreshPagination();
                    }
                });


        // =====================================================
        // LOAD DATA
        // =====================================================

        loadMicrRepairBatches();
    }


    // =========================================================
    // DEFAULT BATCH TYPE
    // =========================================================

    private void selectDefaultBatchType() {

        if (cmbMicrRepairBatchType == null) {
            return;
        }

        List<Comboitem> items =
                cmbMicrRepairBatchType.getItems();

        if (items == null || items.isEmpty()) {
            return;
        }

        cmbMicrRepairBatchType.setSelectedItem(items.get(0));
    }


    // =========================================================
    // DEFAULT PAGE SIZE
    // =========================================================

    private void selectDefaultRowsPerPage() {

        if (cmbMicrRepairRowsPerPage == null) {
            return;
        }

        List<Comboitem> items =
                cmbMicrRepairRowsPerPage.getItems();

        if (items == null || items.isEmpty()) {
            return;
        }

        for (Comboitem item : items) {

            if ("10".equals(item.getValue())) {

                cmbMicrRepairRowsPerPage
                        .setSelectedItem(item);

                return;
            }
        }

        cmbMicrRepairRowsPerPage
                .setSelectedItem(items.get(0));
    }


    // =========================================================
    // LOAD ALL MICR REPAIR BATCHES
    // =========================================================

    private void loadMicrRepairBatches() {

        try {

            scanBatchRows.clear();

            checkerBatchRows.clear();

            allBatchRows.clear();

            filteredBatchRows.clear();


            // =================================================
            // SCAN SOURCE
            // =================================================

            loadScanMicrRepairBatches();


            // =================================================
            // OUTWARD / CHECKER SOURCE
            // =================================================

            loadCheckerReturnedMicrRepairBatches();


            // =================================================
            // COMBINE INTO ONE LIST
            // =================================================

            buildUnifiedBatchList();


            // =================================================
            // APPLY DEFAULT FILTER
            // =================================================

            applyFilters();


        } catch (Exception e) {

            e.printStackTrace();


            divMicrRepairScanSection
                    .setVisible(true);

            grdMicrRepairBatches
                    .setVisible(false);

            divMicrRepairCheckerSection
                    .setVisible(false);

            pagingMicrRepairScan
                    .setVisible(false);

            pagingMicrRepairChecker
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
         * Controller calls ONLY OutwardMakerService.
         *
         * OutwardMakerServiceImpl internally calls:
         *
         * ScanBatchDAO
         * ScanChequeDAO
         */

        List<MicrRepairBatch> scanBatches =
                outwardMakerService
                        .getScanMicrRepairBatches();


        if (scanBatches == null) {
            return;
        }


        for (MicrRepairBatch batch : scanBatches) {

            if (batch == null) {
                continue;
            }


            if (batch.getBatchId() == null
                    || batch.getBatchId()
                            .trim()
                            .isEmpty()) {

                continue;
            }


            /*
             * DAO has already calculated:
             *
             * totalCheques
             * micrErrors
             *
             * Therefore controller does NOT
             * calculate them.
             */

            scanBatchRows.add(batch);
        }
    }


    // =========================================================
    // OUTWARD / CHECKER RETURNED BATCHES
    // =========================================================

    private void loadCheckerReturnedMicrRepairBatches()
            throws Exception {

        /*
         * Controller calls ONLY OutwardMakerService.
         *
         * OutwardMakerServiceImpl internally calls:
         *
         * OutwardBatchDAO
         * OutwardChequeDAO
         */

        List<MicrRepairBatch> outwardBatches =
                outwardMakerService
                        .getOutwardMicrRepairBatches();


        if (outwardBatches == null) {
            return;
        }


        for (MicrRepairBatch batch : outwardBatches) {

            if (batch == null) {
                continue;
            }


            if (batch.getBatchId() == null
                    || batch.getBatchId()
                            .trim()
                            .isEmpty()) {

                continue;
            }


            checkerBatchRows.add(batch);
        }
    }


    // =========================================================
    // BUILD ONE UNIFIED LIST
    // =========================================================

    private void buildUnifiedBatchList() {

        allBatchRows.clear();


        // =====================================================
        // SCAN BATCHES
        // =====================================================

        for (MicrRepairBatch batch : scanBatchRows) {

            allBatchRows.add(
                    new MicrRepairBatchDisplay(
                            batch,
                            "SCAN"));
        }


        // =====================================================
        // CHECKER RETURNED BATCHES
        // =====================================================

        for (MicrRepairBatch batch : checkerBatchRows) {

            allBatchRows.add(
                    new MicrRepairBatchDisplay(
                            batch,
                            "OUTWARD"));
        }
    }


    // =========================================================
    // APPLY SEARCH + BATCH TYPE FILTER
    // =========================================================

    private void applyFilters() {

        filteredBatchRows.clear();


        String searchText = "";

        if (txtMicrRepairSearch != null
                && txtMicrRepairSearch.getValue() != null) {

            searchText =
                    txtMicrRepairSearch
                            .getValue()
                            .trim()
                            .toLowerCase();
        }


        String batchType = "ALL";


        if (cmbMicrRepairBatchType != null
                && cmbMicrRepairBatchType
                        .getSelectedItem() != null) {

            Object value =
                    cmbMicrRepairBatchType
                            .getSelectedItem()
                            .getValue();

            if (value != null) {

                batchType =
                        value.toString();
            }
        }


        for (MicrRepairBatchDisplay displayBatch
                : allBatchRows) {

            MicrRepairBatch batch =
                    displayBatch.getBatch();


            // =================================================
            // BATCH TYPE FILTER
            // =================================================

            if (!matchesBatchType(
                    displayBatch,
                    batchType)) {

                continue;
            }


            // =================================================
            // SEARCH FILTER
            // =================================================

            if (!matchesSearch(
                    batch,
                    searchText)) {

                continue;
            }


            filteredBatchRows.add(displayBatch);
        }


        refreshPagination();
    }


    // =========================================================
    // BATCH TYPE MATCH
    // =========================================================

    private boolean matchesBatchType(
            MicrRepairBatchDisplay displayBatch,
            String batchType) {

        if (batchType == null
                || "ALL".equals(batchType)) {

            return true;
        }


        if ("RETURNED_BY_CHECKER"
                .equals(batchType)) {

            return "OUTWARD".equals(
                    displayBatch.getSource());
        }


        if ("PENDING_MAKER_PROCESS"
                .equals(batchType)) {

            return "SCAN".equals(
                    displayBatch.getSource());
        }


        return true;
    }


    // =========================================================
    // SEARCH MATCH
    // =========================================================

    private boolean matchesSearch(
            MicrRepairBatch batch,
            String searchText) {

        if (searchText == null
                || searchText.isEmpty()) {

            return true;
        }


        // =====================================================
        // BATCH ID
        // =====================================================

        if (batch.getBatchId() != null
                && batch.getBatchId()
                        .toLowerCase()
                        .contains(searchText)) {

            return true;
        }


        // =====================================================
        // SCAN DATE
        // =====================================================

        String scanDate =
                formatDate(batch.getScanDate());


        if (scanDate != null
                && scanDate
                        .toLowerCase()
                        .contains(searchText)) {

            return true;
        }


        // =====================================================
        // STATUS
        // =====================================================

        if (batch.getStatus() != null
                && batch.getStatus()
                        .toLowerCase()
                        .contains(searchText)) {

            return true;
        }


        // =====================================================
        // TOTAL CHEQUES
        // =====================================================

        String totalCheques =
                String.valueOf(
                        batch.getTotalCheques());


        if (totalCheques.contains(searchText)) {

            return true;
        }


        // =====================================================
        // MICR ERRORS
        // =====================================================

        String micrErrors =
                String.valueOf(
                        batch.getMicrErrors());


        if (micrErrors.contains(searchText)) {

            return true;
        }


        return false;
    }


    // =========================================================
    // CLEAR FILTERS
    // =========================================================

    private void clearFilters() {

        if (txtMicrRepairSearch != null) {

            txtMicrRepairSearch
                    .setValue("");
        }


        selectDefaultBatchType();


        applyFilters();
    }


    // =========================================================
    // GET PAGE SIZE
    // =========================================================

    private int getPageSize() {

        int pageSize = 10;


        if (cmbMicrRepairRowsPerPage != null
                && cmbMicrRepairRowsPerPage
                        .getSelectedItem() != null) {

            Object value =
                    cmbMicrRepairRowsPerPage
                            .getSelectedItem()
                            .getValue();

            if (value != null) {

                try {

                    pageSize =
                            Integer.parseInt(
                                    value.toString());

                } catch (NumberFormatException e) {

                    pageSize = 10;
                }
            }
        }


        return pageSize;
    }


    // =========================================================
    // REFRESH PAGINATION
    // =========================================================

    private void refreshPagination() {

        rowsMicrRepairScanBatches
                .getChildren()
                .clear();


        if (filteredBatchRows.isEmpty()) {

            // Keep filter bar visible; hide only the table.
            divMicrRepairScanSection
                    .setVisible(true);

            grdMicrRepairBatches
                    .setVisible(false);

            pagingMicrRepairScan
                    .setVisible(false);


            divMicrRepairEmpty
                    .setVisible(true);


            lblMicrRepairEmptyTitle
                    .setValue(
                            "No MICR repair batches found");


            lblMicrRepairEmptyMessage
                    .setValue(
                            "No batches match the selected filter or search criteria.");


            updateShowingText();

            return;
        }


        divMicrRepairEmpty
                .setVisible(false);


        divMicrRepairScanSection
                .setVisible(true);

        grdMicrRepairBatches
                .setVisible(true);


        int pageSize =
                getPageSize();


        pagingMicrRepairScan
                .setPageSize(pageSize);


        pagingMicrRepairScan
                .setTotalSize(
                        filteredBatchRows.size());


        pagingMicrRepairScan
                .setVisible(
                        filteredBatchRows.size()
                                > pageSize);


        /*
         * Always start from first page after
         * changing search/filter/page size.
         */

        pagingMicrRepairScan
                .setActivePage(0);


        populateUnifiedPage(0);


        updateShowingText();


        /*
         * Prevent duplicate listeners by using
         * the existing component listener only once
         * through the normal ZK event mechanism.
         */

        addPagingListenerIfRequired();
    }


    // =========================================================
    // PAGING LISTENER
    // =========================================================

    private boolean pagingListenerAdded = false;


    private void addPagingListenerIfRequired() {

        if (pagingListenerAdded) {
            return;
        }


        pagingListenerAdded = true;


        pagingMicrRepairScan
                .addEventListener(
                        "onPaging",
                        new EventListener<Event>() {

                            @Override
                            public void onEvent(
                                    Event event)
                                    throws Exception {

                                int page =
                                        pagingMicrRepairScan
                                                .getActivePage();

                                populateUnifiedPage(page);

                                updateShowingText();
                            }
                        });
    }


    // =========================================================
    // POPULATE UNIFIED PAGE
    // =========================================================

    private void populateUnifiedPage(
            int page) {

        rowsMicrRepairScanBatches
                .getChildren()
                .clear();


        int pageSize =
                getPageSize();


        int start =
                page * pageSize;


        int end =
                Math.min(
                        start + pageSize,
                        filteredBatchRows.size());


        for (int i = start;
                i < end;
                i++) {

            MicrRepairBatchDisplay displayBatch =
                    filteredBatchRows.get(i);


            MicrRepairBatch batch =
                    displayBatch.getBatch();


            Row row =
                    new Row();


            // =================================================
            // BATCH ID
            // =================================================

            Label batchLabel =
                    new Label(
                            safe(
                                    batch.getBatchId()));


            batchLabel.setSclass(
                    "micr-repair-batch-id");


            row.appendChild(batchLabel);


            // =================================================
            // SCAN DATE
            // =================================================

            row.appendChild(
                    new Label(
                            formatDate(
                                    batch.getScanDate())));


            // =================================================
            // TOTAL CHEQUES
            // =================================================

            row.appendChild(
                    new Label(
                            String.valueOf(
                                    batch.getTotalCheques())));


            // =================================================
            // MICR ERRORS
            // =================================================

            row.appendChild(
                    new Label(
                            String.valueOf(
                                    batch.getMicrErrors())));


            // =================================================
            // STATUS
            // =================================================

            Label statusLabel =
                    new Label(
                            safe(
                                    batch.getStatus()));


            statusLabel.setSclass(
                    "micr-repair-status");


            row.appendChild(statusLabel);


            // =================================================
            // ACTION
            // =================================================

            Cell actionCell =
                    new Cell();


            Button openButton =
                    new Button("OPEN");


            openButton.setSclass(
                    "btn-action-repair");


            final String selectedBatchId =
                    batch.getBatchId();


            final String selectedSource =
                    displayBatch.getSource();


            openButton.addEventListener(
                    "onClick",
                    new EventListener<Event>() {

                        @Override
                        public void onEvent(
                                Event event)
                                throws Exception {

                            openMicrRepair(
                                    selectedSource,
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
    // UPDATE SHOWING TEXT
    // =========================================================

    private void updateShowingText() {

        if (lblMicrRepairShowing == null) {
            return;
        }


        int total =
                filteredBatchRows.size();


        if (total == 0) {

            lblMicrRepairShowing
                    .setValue(
                            "Showing 0 of 0 batches");

            return;
        }


        int pageSize =
                getPageSize();


        int activePage =
                pagingMicrRepairScan
                        .getActivePage();


        int start =
                (activePage * pageSize) + 1;


        int end =
                Math.min(
                        (activePage + 1)
                                * pageSize,
                        total);


        lblMicrRepairShowing
                .setValue(
                        "Showing "
                        + start
                        + " - "
                        + end
                        + " of "
                        + total
                        + " batches");
    }


    // =========================================================
    // OLD CHECKER DISPLAY
    // =========================================================

    /*
     * The old second table is no longer used.
     *
     * We intentionally keep this method so the controller
     * structure remains easy to understand and so existing
     * references do not need to be removed unnecessarily.
     */

    private void displayCheckerBatches() {

        rowsMicrRepairCheckerBatches
                .getChildren()
                .clear();

        pagingMicrRepairChecker
                .setVisible(false);

        divMicrRepairCheckerSection
                .setVisible(false);
    }


    // =========================================================
    // OPEN MICR REPAIR PAGE
    // =========================================================

    private void openMicrRepair(
            String source,
            String batchId) {

        if (source == null
                || source.trim().isEmpty()) {

            return;
        }


        if (batchId == null
                || batchId.trim().isEmpty()) {

            return;
        }


        Component root =
                Executions.getCurrent()
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

    private String formatDate(
            java.sql.Timestamp timestamp) {

        if (timestamp == null) {

            return "-";
        }


        SimpleDateFormat formatter =
                new SimpleDateFormat(
                        "dd-MM-yyyy");


        return formatter.format(timestamp);
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
}