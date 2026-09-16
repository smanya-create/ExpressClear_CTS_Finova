package com.iispl.cts.controller.outward.maker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
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

    /* =========================================================
       ZUL COMPONENTS
       ========================================================= */

    private Grid grdMicrRepairBatches;
    private Rows rowsMicrRepairBatches;
    private Paging pagingMicrRepair;
    private Div divMicrRepairEmpty;

    private Textbox batchIdFilter;
    private Combobox cmbStatusFilter;
    private Label batchResultCount;


    /* =========================================================
       SERVICE
       ========================================================= */

    private OutwardMakerService outwardMakerService;


    /* =========================================================
       DATA
       ========================================================= */

    private List<MicrRepairBatchDisplay> allBatches =
            new ArrayList<>();

    private List<MicrRepairBatchDisplay> filteredBatches =
            new ArrayList<>();


    /* =========================================================
       INITIALIZATION
       ========================================================= */

    @Override
    public void doAfterCompose(Component comp) throws Exception {

        super.doAfterCompose(comp);

        outwardMakerService =
                new OutwardMakerServiceImpl();


        /*
         * Default status filter
         * All Statuses
         */
        if (cmbStatusFilter != null
                && cmbStatusFilter.getItemCount() > 0) {

            cmbStatusFilter.setSelectedIndex(0);
        }


        /*
         * Pagination listener
         */
        if (pagingMicrRepair != null) {

            pagingMicrRepair.addEventListener(
                    "onPaging",
                    new EventListener<Event>() {

                        @Override
                        public void onEvent(Event event)
                                throws Exception {

                            renderPage();
                        }
                    });
        }


        /*
         * Load Outward MICR repair batches
         */
        loadBatchQueue();
    }


    /* =========================================================
       LOAD BATCH QUEUE
       ========================================================= */

    private void loadBatchQueue() {

        List<MicrRepairBatch> scanBatches =
                new ArrayList<>();

        List<MicrRepairBatch> checkerReturnedBatches =
                new ArrayList<>();


        /*
         * Scan batches
         */
        try {

            List<MicrRepairBatch> result =
                    outwardMakerService
                            .getScanMicrRepairBatches();

            if (result != null) {
                scanBatches = result;
            }

        } catch (Exception e) {

            e.printStackTrace();
        }


        /*
         * Checker returned batches
         */
        try {

            List<MicrRepairBatch> result =
                    outwardMakerService
                            .getOutwardMicrRepairBatches();

            if (result != null) {
                checkerReturnedBatches = result;
            }

        } catch (Exception e) {

            e.printStackTrace();
        }


        /*
         * Combine both sources
         */
        buildUnifiedBatchList(
                scanBatches,
                checkerReturnedBatches
        );


        /*
         * Apply filters
         */
        applyCombinedFilter(
                null,
                null
        );
    }


    /* =========================================================
       BUILD UNIFIED BATCH LIST
       ========================================================= */

    private void buildUnifiedBatchList(
            List<MicrRepairBatch> scanBatches,
            List<MicrRepairBatch> checkerReturnedBatches) {

        allBatches = new ArrayList<>();


        /*
         * Scan source
         */
        if (scanBatches != null) {

            for (MicrRepairBatch batch : scanBatches) {

                if (batch == null) {
                    continue;
                }

                allBatches.add(
                        new MicrRepairBatchDisplay(
                                batch,
                                BatchSource.SCAN
                        )
                );
            }
        }


        /*
         * Checker returned source
         */
        if (checkerReturnedBatches != null) {

            for (MicrRepairBatch batch :
                    checkerReturnedBatches) {

                if (batch == null) {
                    continue;
                }

                allBatches.add(
                        new MicrRepairBatchDisplay(
                                batch,
                                BatchSource.OUTWARD
                        )
                );
            }
        }
    }


    /* =========================================================
       APPLY SEARCH + STATUS FILTER
       ========================================================= */

    private void applyCombinedFilter(
            String searchText,
            String statusFilter) {


        /*
         * Search text
         */
        if (searchText == null) {

            searchText =
                    batchIdFilter != null
                            && batchIdFilter.getValue() != null
                            ? batchIdFilter
                                    .getValue()
                                    .trim()
                                    .toLowerCase()
                            : "";

        } else {

            searchText =
                    searchText.trim().toLowerCase();
        }


        /*
         * Status
         */
        if (statusFilter == null) {

            if (cmbStatusFilter != null
                    && cmbStatusFilter.getSelectedItem() != null
                    && cmbStatusFilter
                            .getSelectedItem()
                            .getValue() != null) {

                statusFilter =
                        cmbStatusFilter
                                .getSelectedItem()
                                .getValue()
                                .toString();

            } else {

                statusFilter = "ALL";
            }
        }


        filteredBatches =
                new ArrayList<>();


        /*
         * Apply filters to unified list
         */
        for (MicrRepairBatchDisplay displayBatch :
                allBatches) {

            if (displayBatch == null
                    || displayBatch.getBatch() == null) {

                continue;
            }


            MicrRepairBatch batch =
                    displayBatch.getBatch();


            /* ---------------------------------------------
               Search by Batch ID
               --------------------------------------------- */

            boolean matchesSearch = true;

            if (!searchText.isEmpty()) {

                String batchId =
                        batch.getBatchId();

                matchesSearch =
                        batchId != null
                                && batchId
                                        .toLowerCase()
                                        .contains(searchText);
            }


            /* ---------------------------------------------
               Status filter
               --------------------------------------------- */

            boolean matchesStatus = true;

            if (!"ALL".equalsIgnoreCase(statusFilter)) {

                if ("PENDING_MAKER"
                        .equalsIgnoreCase(statusFilter)) {

                    matchesStatus =
                            displayBatch.getSource()
                                    == BatchSource.SCAN;

                } else if ("CHECKER_RETURNED"
                        .equalsIgnoreCase(statusFilter)) {

                    matchesStatus =
                            displayBatch.getSource()
                                    == BatchSource.OUTWARD;
                }
            }


            if (matchesSearch
                    && matchesStatus) {

                filteredBatches.add(
                        displayBatch
                );
            }
        }


        setupPagination();
    }


    /* =========================================================
       PAGINATION
       ========================================================= */

    private void setupPagination() {

        int totalSize =
                filteredBatches.size();


        /*
         * Result count
         */
        if (batchResultCount != null) {

            batchResultCount.setValue(
                    totalSize
                            + (totalSize == 1
                                    ? " Batch"
                                    : " Batches")
            );
        }


        /*
         * Empty state
         */
        if (totalSize == 0) {

            if (grdMicrRepairBatches != null) {

                grdMicrRepairBatches
                        .setVisible(false);
            }

            if (pagingMicrRepair != null) {

                pagingMicrRepair
                        .setVisible(false);
            }

            if (divMicrRepairEmpty != null) {

                divMicrRepairEmpty
                        .setVisible(true);
            }

            return;
        }


        /*
         * Show grid
         */
        if (grdMicrRepairBatches != null) {

            grdMicrRepairBatches
                    .setVisible(true);
        }


        if (divMicrRepairEmpty != null) {

            divMicrRepairEmpty
                    .setVisible(false);
        }


        /*
         * Configure pagination
         */
        if (pagingMicrRepair != null) {

            pagingMicrRepair
                    .setTotalSize(totalSize);

            pagingMicrRepair
                    .setActivePage(0);

            pagingMicrRepair
                    .setVisible(
                            totalSize
                                    > pagingMicrRepair
                                            .getPageSize()
                    );
        }


        renderPage();
    }


    /* =========================================================
       RENDER PAGE
       ========================================================= */

    private void renderPage() {

        if (rowsMicrRepairBatches == null) {
            return;
        }


        rowsMicrRepairBatches
                .getChildren()
                .clear();


        int pageSize =
                pagingMicrRepair != null
                        ? pagingMicrRepair
                                .getPageSize()
                        : 10;


        int activePage =
                pagingMicrRepair != null
                        ? pagingMicrRepair
                                .getActivePage()
                        : 0;


        int startIndex =
                activePage * pageSize;


        int endIndex =
                Math.min(
                        startIndex + pageSize,
                        filteredBatches.size()
                );


        for (int i = startIndex;
                i < endIndex;
                i++) {

            MicrRepairBatchDisplay displayBatch =
                    filteredBatches.get(i);

            if (displayBatch != null
                    && displayBatch.getBatch() != null) {

                createBatchRow(displayBatch);
            }
        }
    }


    /* =========================================================
       CREATE BATCH ROW
       ========================================================= */

    private void createBatchRow(
            MicrRepairBatchDisplay displayBatch) {

        MicrRepairBatch batch =
                displayBatch.getBatch();


        Row row = new Row();


        /* =====================================================
           BATCH ID
           ===================================================== */

        Label batchIdLabel =
                new Label(
                        getValue(
                                batch.getBatchId()
                        )
                );

        batchIdLabel.setSclass(
                "outward-micr-repair-batch-id"
        );


        batchIdLabel.addEventListener(
                "onClick",
                event ->
                        openBatch(
                                batch.getBatchId(),
                                displayBatch.getSource()
                        )
        );


        /* =====================================================
           RECEIVED DATE
           ===================================================== */

        Label dateLabel =
                new Label(
                        formatDate(
                                batch.getScanDate()
                        )
                );

        dateLabel.setSclass(
                "outward-micr-repair-cell-text"
        );


        /* =====================================================
           TOTAL CHEQUES
           ===================================================== */

        Label totalChequesLabel =
                new Label(
                        String.valueOf(
                                batch.getTotalCheques()
                        )
                );

        totalChequesLabel.setSclass(
                "outward-micr-repair-count-text"
        );


        /* =====================================================
           MICR ERRORS
           ===================================================== */

        Label micrErrorsLabel =
                new Label(
                        String.valueOf(
                                batch.getMicrErrors()
                        )
                );

        micrErrorsLabel.setSclass(
                "outward-micr-repair-pending-count"
        );


        /* =====================================================
           STATUS
           ===================================================== */

        Label statusLabel =
                new Label();


        if (displayBatch.getSource()
                == BatchSource.OUTWARD) {

            statusLabel.setValue(
                    "Checker Returned"
            );

            statusLabel.setSclass(
                    "outward-micr-repair-status-returned"
            );

        } else {

            statusLabel.setValue(
                    "Pending Maker"
            );

            statusLabel.setSclass(
                    "outward-micr-repair-status-pending"
            );
        }


        /* =====================================================
           OPEN BUTTON
           ===================================================== */

        Button actionButton =
                new Button("OPEN");


        actionButton.setSclass(
                "outward-micr-repair-action-button"
        );


        actionButton.addEventListener(
                "onClick",
                event ->
                        openBatch(
                                batch.getBatchId(),
                                displayBatch.getSource()
                        )
        );


        /* =====================================================
           APPEND COMPONENTS
           ===================================================== */

        row.appendChild(
                batchIdLabel
        );

        row.appendChild(
                dateLabel
        );

        row.appendChild(
                totalChequesLabel
        );

        row.appendChild(
                micrErrorsLabel
        );

        row.appendChild(
                statusLabel
        );

        row.appendChild(
                actionComponentWrapper(
                        actionButton
                )
        );


        rowsMicrRepairBatches
                .appendChild(row);
    }


    /* =========================================================
       ACTION BUTTON
       ========================================================= */

    private Component actionComponentWrapper(
            Button button) {

        return button;
    }


    /* =========================================================
       SEARCH EVENTS
       ========================================================= */

    public void onChanging$batchIdFilter(
            InputEvent event) {

        applyCombinedFilter(
                event != null
                        ? event.getValue()
                        : "",
                null
        );
    }


    public void onChange$batchIdFilter(
            Event event) {

        applyCombinedFilter(
                null,
                null
        );
    }


    /* =========================================================
       STATUS EVENTS
       ========================================================= */

    public void onSelect$cmbStatusFilter(
            SelectEvent<?, ?> event) {

        applyCombinedFilter(
                null,
                null
        );
    }


    public void onSelect$cmbStatusFilter(
            Event event) {

        applyCombinedFilter(
                null,
                null
        );
    }


    public void onSelect$cmbStatusFilter() {

        applyCombinedFilter(
                null,
                null
        );
    }


    /* =========================================================
       CLEAR FILTER
       ========================================================= */

    public void clearBatchFilter() {

        if (batchIdFilter != null) {

            batchIdFilter.setValue("");
        }


        if (cmbStatusFilter != null
                && cmbStatusFilter.getItemCount() > 0) {

            cmbStatusFilter.setSelectedIndex(0);
        }


        applyCombinedFilter(
                "",
                "ALL"
        );
    }


    /* =========================================================
       OPEN BATCH
       ========================================================= */

    public void openBatch(
            Object batchId,
            BatchSource source) {

        if (batchId == null) {
            return;
        }


        String batchIdValue =
                String.valueOf(batchId)
                        .trim();


        if (batchIdValue.isEmpty()) {
            return;
        }


        /*
         * Existing session attributes
         */
        Sessions.getCurrent()
                .setAttribute(
                        "MICR_REPAIR_BATCH_ID",
                        batchIdValue
                );


        Sessions.getCurrent()
                .setAttribute(
                        "batchId",
                        batchIdValue
                );


        /*
         * Existing Outward source information
         */
        Sessions.getCurrent()
                .setAttribute(
                        "MICR_REPAIR_SOURCE",
                        source == BatchSource.SCAN
                                ? "SCAN"
                                : "OUTWARD"
                );


        Include mainInclude = null;


        /*
         * Find Outward main content area
         */
        try {

            mainInclude =
                    (Include) Path.getComponent(
                            "/outwardMakerRootWin/mainContentArea"
                    );

        } catch (Exception ignored) {
        }


        /*
         * Fallback lookup
         */
        if (mainInclude == null
                && self != null
                && self.getDesktop() != null) {

            for (org.zkoss.zk.ui.Page page :
                    self.getDesktop().getPages()) {

                Component component =
                        page.getFellowIfAny(
                                "mainContentArea",
                                true
                        );

                if (component instanceof Include) {

                    mainInclude =
                            (Include) component;

                    break;
                }
            }
        }


        /*
         * Navigate to MICR Repair page
         */
        if (mainInclude != null) {

            mainInclude.setSrc(null);

            mainInclude.setSrc(
                    "/outward/maker/micr-repair/micr-repair.zul"
                            + "?batchId="
                            + batchIdValue
            );

        } else {

            Executions.sendRedirect(
                    "/outward/maker/index.zul"
                            + "?page=micr-repair"
                            + "&batchId="
                            + batchIdValue
            );
        }
    }


    /* =========================================================
       DATE FORMAT
       ========================================================= */

    private String formatDate(
            Object date) {

        if (date == null) {
            return "-";
        }


        try {

            if (date instanceof java.util.Date) {

                return new SimpleDateFormat(
                        "dd-MM-yyyy"
                ).format(
                        (java.util.Date) date
                );
            }


            return date.toString().trim();

        } catch (Exception e) {

            return "-";
        }
    }


    /* =========================================================
       SAFE VALUE
       ========================================================= */

    private String getValue(
            Object value) {

        if (value == null
                || String.valueOf(value)
                        .trim()
                        .isEmpty()) {

            return "-";
        }


        return String.valueOf(value)
                .trim();
    }


    /* =========================================================
       BATCH SOURCE
       ========================================================= */

    private enum BatchSource {

        SCAN,

        OUTWARD
    }


    /* =========================================================
       DISPLAY OBJECT
       ========================================================= */

    private static class MicrRepairBatchDisplay {

        private final MicrRepairBatch batch;

        private final BatchSource source;


        private MicrRepairBatchDisplay(
                MicrRepairBatch batch,
                BatchSource source) {

            this.batch = batch;
            this.source = source;
        }


        private MicrRepairBatch getBatch() {

            return batch;
        }


        private BatchSource getSource() {

            return source;
        }
    }
}