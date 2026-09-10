package com.iispl.cts.controller.outward.maker;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

import com.iispl.cts.dao.outward.MakerUnprocessedChequeDAO;
import com.iispl.cts.daoimpl.outward.MakerUnprocessedChequeDAOImpl;
import com.iispl.cts.dto.UnprocessedChequeDTO;

public class MakerUnprocessedChequesController
        extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // =========================================================
    // ZUL COMPONENTS
    // =========================================================

    private Label lblTotalCount;
    private Label lblRepairCount;
    private Label lblDataEntryCount;

    private Combobox cmbStatusFilter;
    private Textbox txtSearchBatch;
    private Button btnRefresh;
    private Listbox lstUnprocessed;

    // =========================================================
    // DAO
    // =========================================================

    private final MakerUnprocessedChequeDAO unprocessedDAO =
            new MakerUnprocessedChequeDAOImpl();

    // =========================================================
    // DATA
    // =========================================================

    private List<UnprocessedChequeDTO> masterList =
            new ArrayList<>();

    private final DecimalFormat df =
            new DecimalFormat("##,##,##0.00");

    // =========================================================
    // COMPOSE
    // =========================================================

    @Override
    public void doAfterCompose(Component comp)
            throws Exception {

        super.doAfterCompose(comp);

        // =====================================================
        // DEFAULT FILTER
        // =====================================================

        if (cmbStatusFilter != null
                && cmbStatusFilter.getItemCount() > 0) {

            cmbStatusFilter.setSelectedIndex(0);
        }

        // =====================================================
        // INITIALIZE LISTBOX
        // =====================================================

        initListboxRenderer();

        // =====================================================
        // LOAD DATA
        // =====================================================

        loadUnprocessedCheques();
    }

    // =========================================================
    // LISTBOX RENDERER
    // =========================================================

    private void initListboxRenderer() {

        lstUnprocessed.setItemRenderer(
                new ListitemRenderer<UnprocessedChequeDTO>() {

                    @Override
                    public void render(
                            Listitem item,
                            UnprocessedChequeDTO dto,
                            int index) {

                        item.setValue(dto);

                        // =================================================
                        // 1. BATCH & REFERENCE
                        // =================================================

                        Listcell cellBatch =
                                new Listcell();

                        Vlayout vBatch =
                                new Vlayout();

                        vBatch.setSpacing("2px");

                        Label lblBNo =
                                new Label(
                                        dto.getBatchNo());

                        lblBNo.setSclass(
                                "list-batch-title");

                        Label lblSName =
                                new Label(
                                        dto.getOriginalSessionName() != null
                                                ? dto.getOriginalSessionName()
                                                : "Scan Staging");

                        lblSName.setSclass(
                                "list-batch-sub");

                        vBatch.appendChild(
                                lblBNo);

                        vBatch.appendChild(
                                lblSName);

                        cellBatch.appendChild(
                                vBatch);

                        // =================================================
                        // 2. CHEQUE NO
                        // =================================================

                        Listcell cellChq =
                                new Listcell(
                                        dto.getChequeNo() != null
                                                ? dto.getChequeNo()
                                                : "------");

                        cellChq.setSclass(
                                "list-monospace");

                        // =================================================
                        // 3. MICR SORT CODE
                        // =================================================

                        Listcell cellSort =
                                new Listcell(
                                        dto.getSortCode() != null
                                                ? dto.getSortCode()
                                                : "------");

                        cellSort.setSclass(
                                "list-monospace");

                        // =================================================
                        // 4. AMOUNT
                        // =================================================

                        Listcell cellAmt =
                                new Listcell(
                                        dto.getAmount() != null
                                                ? "₹ "
                                                        + df.format(
                                                                dto.getAmount())
                                                : "₹ 0.00");

                        cellAmt.setSclass(
                                "list-amount");

                        // =================================================
                        // 5. ACTION STAGE BADGE
                        // =================================================

                        Listcell cellStage =
                                new Listcell();

                        Label lblStage =
                                new Label();

                        if ("PENDING_DATA_ENTRY"
                                .equals(dto.getStatus())) {

                            lblStage.setValue(
                                    "Data Entry Pending");

                            lblStage.setSclass(
                                    "badge-entry");

                        } else {

                            lblStage.setValue(
                                    "MICR Repair Pending");

                            lblStage.setSclass(
                                    "badge-repair");
                        }

                        cellStage.appendChild(
                                lblStage);

                        // =================================================
                        // 6. REASON / REMARKS
                        // =================================================

                        Listcell cellRemarks =
                                new Listcell();

                        String reasonText =
                                dto.getSendBackReason() != null
                                        ? dto.getSendBackReason()
                                        : "Scan Review";

                        if (dto.getRemarks() != null
                                && !dto.getRemarks().isEmpty()) {

                            reasonText +=
                                    " (" +
                                    dto.getRemarks() +
                                    ")";
                        }

                        Label lblReason =
                                new Label(
                                        reasonText);

                        lblReason.setSclass(
                                "list-remarks");

                        cellRemarks.appendChild(
                                lblReason);

                        // =================================================
                        // 7. DYNAMIC ACTION BUTTON
                        // =================================================

                        Listcell cellAction =
                                new Listcell();

                        Button btnAction =
                                new Button();

                        // =================================================
                        // DARK BLUE BUTTON STYLE
                        // =================================================

                        String darkBlueBtnStyle =
                                "background: #1e3a8a; "
                                + "color: #ffffff; "
                                + "border: 1px solid #1e3a8a; "
                                + "font-size: 11px; "
                                + "font-weight: 600; "
                                + "padding: 5px 12px; "
                                + "border-radius: 4px; "
                                + "cursor: pointer; "
                                + "white-space: nowrap; "
                                + "box-shadow: 0 1px 2px rgba(0,0,0,0.1);";

                        // =================================================
                        // DATA ENTRY
                        // =================================================

                        if ("PENDING_DATA_ENTRY"
                                .equals(dto.getStatus())) {

                            btnAction.setLabel(
                                    "Data Entry →");

                            btnAction.setIconSclass(
                                    "z-icon-pencil");

                        }

                        // =================================================
                        // MICR REPAIR
                        // =================================================

                        else {

                            btnAction.setLabel(
                                    "MICR Repair →");

                            btnAction.setIconSclass(
                                    "z-icon-wrench");
                        }

                        btnAction.setStyle(
                                darkBlueBtnStyle);

                        // =================================================
                        // BUTTON ACTION
                        // =================================================

                        btnAction.addEventListener(
                                "onClick",
                                event ->
                                        routeToMakerModule(dto));

                        cellAction.appendChild(
                                btnAction);

                        // =================================================
                        // ADD CELLS
                        // =================================================

                        item.appendChild(
                                cellBatch);

                        item.appendChild(
                                cellChq);

                        item.appendChild(
                                cellSort);

                        item.appendChild(
                                cellAmt);

                        item.appendChild(
                                cellStage);

                        item.appendChild(
                                cellRemarks);

                        item.appendChild(
                                cellAction);
                    }
                });
    }

    // =========================================================
    // LOAD UNPROCESSED CHEQUES
    // =========================================================

    public void loadUnprocessedCheques() {

        this.masterList =
                unprocessedDAO
                        .getUnprocessedCheques("MAKER");

        updateCounters();

        applyFilters(
                txtSearchBatch != null
                        ? txtSearchBatch.getValue()
                        : "");
    }

    // =========================================================
    // UPDATE COUNTERS
    // =========================================================

    private void updateCounters() {

        long total =
                masterList.size();

        long repair =
                masterList.stream()
                        .filter(c ->
                                "PENDING_REPAIR"
                                        .equals(c.getStatus()))
                        .count();

        long dataEntry =
                masterList.stream()
                        .filter(c ->
                                "PENDING_DATA_ENTRY"
                                        .equals(c.getStatus()))
                        .count();

        if (lblTotalCount != null) {

            lblTotalCount.setValue(
                    String.valueOf(total));
        }

        if (lblRepairCount != null) {

            lblRepairCount.setValue(
                    String.valueOf(repair));
        }

        if (lblDataEntryCount != null) {

            lblDataEntryCount.setValue(
                    String.valueOf(dataEntry));
        }
    }

    // =========================================================
    // FILTER CHANGED
    // =========================================================

    public void onFilterChanged() {

        applyFilters(
                txtSearchBatch != null
                        ? txtSearchBatch.getValue()
                        : "");
    }

    // =========================================================
    // SEARCH - ENTER
    // =========================================================

    public void onOK$txtSearchBatch(
            Event event) {

        applyFilters(
                txtSearchBatch != null
                        ? txtSearchBatch.getValue()
                        : "");
    }

    // =========================================================
    // SEARCH - LIVE TYPING
    // =========================================================

    public void onChanging$txtSearchBatch(
            InputEvent event) {

        applyFilters(
                event.getValue());
    }

    // =========================================================
    // REFRESH
    // =========================================================

    public void onClick$btnRefresh(
            Event event) {

        handleRefresh();
    }

    // =========================================================
    // REFRESH OVERLOAD
    // =========================================================

    public void onClick$btnRefresh() {

        handleRefresh();
    }

    // =========================================================
    // HANDLE REFRESH
    // =========================================================

    private void handleRefresh() {

        if (txtSearchBatch != null) {

            txtSearchBatch.setValue("");
        }

        if (cmbStatusFilter != null
                && cmbStatusFilter.getItemCount() > 0) {

            cmbStatusFilter.setSelectedIndex(0);
        }

        loadUnprocessedCheques();

        org.zkoss.zk.ui.util.Clients
                .showNotification(
                        "Queue refreshed",
                        "info",
                        null,
                        "top_center",
                        1500);
    }

    // =========================================================
    // APPLY FILTERS
    // =========================================================

    private void applyFilters(
            String searchKeywordInput) {

        String stageFilter =
                (cmbStatusFilter != null
                        && cmbStatusFilter
                                .getSelectedItem() != null)
                        ? cmbStatusFilter
                                .getSelectedItem()
                                .getValue()
                                .toString()
                        : "ALL";

        final String searchKeyword =
                (searchKeywordInput != null)
                        ? searchKeywordInput
                                .trim()
                                .toLowerCase()
                        : "";

        List<UnprocessedChequeDTO> filtered =
                masterList.stream()
                        .filter(item -> {

                            // =============================================
                            // STAGE MATCH
                            // =============================================

                            boolean matchesStage =
                                    true;

                            if (!"ALL".equalsIgnoreCase(
                                    stageFilter)) {

                                if ("PENDING_REPAIR"
                                        .equalsIgnoreCase(
                                                stageFilter)) {

                                    matchesStage =
                                            "PENDING_REPAIR"
                                                    .equals(
                                                            item.getStatus());

                                } else if (
                                        "PENDING_DATA_ENTRY"
                                                .equalsIgnoreCase(
                                                        stageFilter)) {

                                    matchesStage =
                                            "PENDING_DATA_ENTRY"
                                                    .equals(
                                                            item.getStatus());
                                }
                            }

                            // =============================================
                            // KEYWORD MATCH
                            // =============================================

                            boolean matchesSearch =
                                    true;

                            if (!searchKeyword.isEmpty()) {

                                boolean bMatch =
                                        item.getBatchNo() != null
                                                && item.getBatchNo()
                                                        .toLowerCase()
                                                        .contains(
                                                                searchKeyword);

                                boolean cMatch =
                                        item.getChequeNo() != null
                                                && item.getChequeNo()
                                                        .toLowerCase()
                                                        .contains(
                                                                searchKeyword);

                                boolean micrMatch =
                                        item.getSortCode() != null
                                                && item.getSortCode()
                                                        .toLowerCase()
                                                        .contains(
                                                                searchKeyword);

                                boolean rMatch =
                                        item.getRemarks() != null
                                                && item.getRemarks()
                                                        .toLowerCase()
                                                        .contains(
                                                                searchKeyword);

                                matchesSearch =
                                        bMatch
                                                || cMatch
                                                || micrMatch
                                                || rMatch;
                            }

                            return matchesStage
                                    && matchesSearch;
                        })
                        .collect(
                                Collectors.toList());

        lstUnprocessed.setModel(
                new ListModelList<>(
                        filtered));
    }

    // =========================================================
    // ROUTE TO MAKER MODULE
    // =========================================================
    //
    // DATA ENTRY:
    //     Existing redirect behavior is preserved.
    //
    // MICR REPAIR:
    //     No full-page redirect.
    //     Uses mainContentArea Include.
    //     Sends:
    //         MICR_REPAIR_SOURCE
    //         MICR_REPAIR_BATCH_ID
    // =========================================================

    private void routeToMakerModule(
            UnprocessedChequeDTO dto) {

        if (dto == null) {
            return;
        }

        // =====================================================
        // CREATE IDS
        // =====================================================

        String batchIdStr =
                "BAT" + dto.getBatchId();

        String chqIdStr =
                "CH" + dto.getChequeId();

        // =====================================================
        // EXISTING SESSION VALUES
        // =====================================================

        Sessions.getCurrent()
                .setAttribute(
                        "SELECTED_SCAN_BATCH_ID",
                        batchIdStr);

        Sessions.getCurrent()
                .setAttribute(
                        "SELECTED_SCAN_CHEQUE_ID",
                        chqIdStr);

        Sessions.getCurrent()
                .setAttribute(
                        "SELECTED_CHEQUE_NO",
                        dto.getChequeNo());

        Sessions.getCurrent()
                .setAttribute(
                        "SELECTED_OUTWARD_BATCH_ID",
                        batchIdStr);

        Sessions.getCurrent()
                .setAttribute(
                        "SELECTED_CHEQUE_ID",
                        chqIdStr);

        // =====================================================
        // DATA ENTRY
        //
        // DO NOT CHANGE EXISTING BEHAVIOR
        // =====================================================

        if ("PENDING_DATA_ENTRY"
                .equals(dto.getStatus())) {

            Executions.sendRedirect(
                    "/outward/maker/data-entry.zul");

            return;
        }

        // =====================================================
        // MICR REPAIR
        //
        // USE EXISTING MAIN CONTENT INCLUDE
        // =====================================================

        String source =
                "SCAN";

        Component root =
                Executions.getCurrent()
                        .getDesktop()
                        .getFirstPage()
                        .getFirstRoot();

        Component mainContentArea =
                root.getFellowIfAny(
                        "mainContentArea",
                        true);

        // =====================================================
        // CHECK INCLUDE
        // =====================================================

        if (mainContentArea
                instanceof Include) {

            Include include =
                    (Include) mainContentArea;

            // =================================================
            // SEND SOURCE
            // =================================================

            include.setAttribute(
                    "MICR_REPAIR_SOURCE",
                    source);

            // =================================================
            // SEND BATCH ID
            // =================================================

            include.setAttribute(
                    "MICR_REPAIR_BATCH_ID",
                    batchIdStr);

            // =================================================
            // LOAD MICR REPAIR PAGE
            // =================================================

            include.setSrc(
                    "/outward/maker/micr-repair/micr-repair.zul");

            // =================================================
            // DEBUG
            // =================================================

            System.out.println(
                    "MICR REPAIR SOURCE = "
                            + source);

            System.out.println(
                    "MICR REPAIR BATCH ID = "
                            + batchIdStr);

            System.out.println(
                    "MICR REPAIR ZUL = "
                            + "/outward/maker/micr-repair/micr-repair.zul");

        } else {

            // =================================================
            // INCLUDE NOT FOUND
            // =================================================

            System.out.println(
                    "ERROR: mainContentArea Include not found.");
        }
    }
}