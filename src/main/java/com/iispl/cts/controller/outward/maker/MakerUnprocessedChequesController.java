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

public class MakerUnprocessedChequesController extends GenericForwardComposer<Component> {

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
    // DAO & DATA
    // =========================================================
    private final MakerUnprocessedChequeDAO unprocessedDAO = new MakerUnprocessedChequeDAOImpl();
    private List<UnprocessedChequeDTO> masterList = new ArrayList<>();
    private final DecimalFormat df = new DecimalFormat("##,##,##0.00");

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        if (cmbStatusFilter != null && cmbStatusFilter.getItemCount() > 0) {
            cmbStatusFilter.setSelectedIndex(0);
        }

        initListboxRenderer();
        loadUnprocessedCheques();
    }

    // =========================================================
    // LISTBOX RENDERER
    // =========================================================
    private void initListboxRenderer() {
        lstUnprocessed.setItemRenderer(new ListitemRenderer<UnprocessedChequeDTO>() {
            @Override
            public void render(Listitem item, UnprocessedChequeDTO dto, int index) {
                item.setValue(dto);

                // 1. Batch & Reference
                Listcell cellBatch = new Listcell();
                Vlayout vBatch = new Vlayout();
                vBatch.setSpacing("2px");
                Label lblBNo = new Label(dto.getBatchNo());
                lblBNo.setSclass("list-batch-title");
                Label lblSName = new Label(dto.getOriginalSessionName() != null ? dto.getOriginalSessionName() : "Scan Staging");
                lblSName.setSclass("list-batch-sub");
                vBatch.appendChild(lblBNo);
                vBatch.appendChild(lblSName);
                cellBatch.appendChild(vBatch);

                // 2. Cheque No
                Listcell cellChq = new Listcell(dto.getChequeNo() != null ? dto.getChequeNo() : "------");
                cellChq.setSclass("list-monospace");

                // 3. MICR Sort Code
                Listcell cellSort = new Listcell(dto.getSortCode() != null ? dto.getSortCode() : "------");
                cellSort.setSclass("list-monospace");

                // 4. Amount
                Listcell cellAmt = new Listcell(dto.getAmount() != null ? "₹ " + df.format(dto.getAmount()) : "₹ 0.00");
                cellAmt.setSclass("list-amount");

                // 5. Action Stage Badge (Reads from preserved stage in remarks or active status)
                Listcell cellStage = new Listcell();
                Label lblStage = new Label();
                boolean wasDataEntry = isItemDataEntry(dto);

                if (wasDataEntry) {
                    lblStage.setValue("Data Entry Pending");
                    lblStage.setSclass("badge-entry");
                } else {
                    lblStage.setValue("MICR Repair Pending");
                    lblStage.setSclass("badge-repair");
                }
                cellStage.appendChild(lblStage);

                // 6. Reason / Remarks
                Listcell cellRemarks = new Listcell();
                String reasonText = dto.getSendBackReason() != null ? dto.getSendBackReason() : "Scan Review";
                if (dto.getRemarks() != null && !dto.getRemarks().isEmpty()) {
                    reasonText += " (" + dto.getRemarks() + ")";
                }
                Label lblReason = new Label(reasonText);
                lblReason.setSclass("list-remarks");
                cellRemarks.appendChild(lblReason);

                // 7. Action Button
                Listcell cellAction = new Listcell();
                Button btnAction = new Button();
                String darkBlueBtnStyle = "background: #1e3a8a; color: #ffffff; border: 1px solid #1e3a8a; "
                        + "font-size: 11px; font-weight: 600; padding: 5px 12px; border-radius: 4px; "
                        + "cursor: pointer; white-space: nowrap; box-shadow: 0 1px 2px rgba(0,0,0,0.1);";
                btnAction.setStyle(darkBlueBtnStyle);

                if (wasDataEntry) {
                    btnAction.setLabel("Data Entry →");
                    btnAction.setIconSclass("z-icon-pencil");
                } else {
                    btnAction.setLabel("MICR Repair →");
                    btnAction.setIconSclass("z-icon-wrench");
                }

                btnAction.addEventListener("onClick", event -> routeToMakerModule(dto, wasDataEntry));
                cellAction.appendChild(btnAction);

                // Add cells in order
                item.appendChild(cellBatch);
                item.appendChild(cellChq);
                item.appendChild(cellSort);
                item.appendChild(cellAmt);
                item.appendChild(cellStage);
                item.appendChild(cellRemarks);
                item.appendChild(cellAction);
            }
        });
    }

    // Helper: checks whether instrument was paused at Data Entry
    private boolean isItemDataEntry(UnprocessedChequeDTO dto) {
        if (dto == null) return false;
        String status = dto.getStatus() != null ? dto.getStatus().trim().toUpperCase() : "";
        String remarks = dto.getRemarks() != null ? dto.getRemarks().trim().toUpperCase() : "";
        return status.contains("DATA_ENTRY") || remarks.contains("DATA_ENTRY");
    }
    // =========================================================
    // DATA RETRIEVAL & COUNTERS
    // =========================================================
    public void loadUnprocessedCheques() {
        this.masterList = unprocessedDAO.getUnprocessedCheques("MAKER");
        updateCounters();
        applyFilters(txtSearchBatch != null ? txtSearchBatch.getValue() : "");
    }

    private void updateCounters() {
        long total = masterList.size();
        long dataEntry = masterList.stream().filter(this::isItemDataEntry).count();
        long repair = total - dataEntry;

        if (lblTotalCount != null) lblTotalCount.setValue(String.valueOf(total));
        if (lblRepairCount != null) lblRepairCount.setValue(String.valueOf(repair));
        if (lblDataEntryCount != null) lblDataEntryCount.setValue(String.valueOf(dataEntry));
    }

    // =========================================================
    // SEARCH & FILTER EVENTS
    // =========================================================
    public void onFilterChanged() {
        applyFilters(txtSearchBatch != null ? txtSearchBatch.getValue() : "");
    }

    public void onOK$txtSearchBatch(Event event) {
        applyFilters(txtSearchBatch != null ? txtSearchBatch.getValue() : "");
    }

    public void onChanging$txtSearchBatch(InputEvent event) {
        applyFilters(event.getValue());
    }

    public void onClick$btnRefresh(Event event) {
        handleRefresh();
    }

    public void onClick$btnRefresh() {
        handleRefresh();
    }

    private void handleRefresh() {
        if (txtSearchBatch != null) txtSearchBatch.setValue("");
        if (cmbStatusFilter != null && cmbStatusFilter.getItemCount() > 0) {
            cmbStatusFilter.setSelectedIndex(0);
        }
        loadUnprocessedCheques();
        org.zkoss.zk.ui.util.Clients.showNotification("Queue refreshed", "info", null, "top_center", 1500);
    }

    private void applyFilters(String searchKeywordInput) {
        String stageFilter = (cmbStatusFilter != null && cmbStatusFilter.getSelectedItem() != null)
                ? cmbStatusFilter.getSelectedItem().getValue().toString()
                : "ALL";

        final String searchKeyword = (searchKeywordInput != null) ? searchKeywordInput.trim().toLowerCase() : "";

        List<UnprocessedChequeDTO> filtered = masterList.stream().filter(item -> {
            boolean matchesStage = true;
            boolean isDataEntry = isItemDataEntry(item);

            if ("PENDING_REPAIR".equalsIgnoreCase(stageFilter)) {
                matchesStage = !isDataEntry;
            } else if ("PENDING_DATA_ENTRY".equalsIgnoreCase(stageFilter)) {
                matchesStage = isDataEntry;
            }

            boolean matchesSearch = true;
            if (!searchKeyword.isEmpty()) {
                boolean bMatch = item.getBatchNo() != null && item.getBatchNo().toLowerCase().contains(searchKeyword);
                boolean cMatch = item.getChequeNo() != null && item.getChequeNo().toLowerCase().contains(searchKeyword);
                boolean micrMatch = item.getSortCode() != null && item.getSortCode().toLowerCase().contains(searchKeyword);
                boolean rMatch = item.getRemarks() != null && item.getRemarks().toLowerCase().contains(searchKeyword);
                matchesSearch = bMatch || cMatch || micrMatch || rMatch;
            }

            return matchesStage && matchesSearch;
        }).collect(Collectors.toList());

        lstUnprocessed.setModel(new ListModelList<>(filtered));
    }

    // =========================================================
    // ROUTING LOGIC
    // =========================================================
    private void routeToMakerModule(UnprocessedChequeDTO dto, boolean isDataEntry) {
        if (dto == null) return;

        String batchIdStr = (dto.getBatchId() != null && dto.getBatchId() > 0) 
                ? "BAT" + dto.getBatchId() 
                : dto.getBatchNo();
        String chqIdStr = "CH" + dto.getChequeId();

        Sessions.getCurrent().setAttribute("SELECTED_SCAN_BATCH_ID", batchIdStr);
        Sessions.getCurrent().setAttribute("SELECTED_SCAN_CHEQUE_ID", chqIdStr);
        Sessions.getCurrent().setAttribute("SELECTED_CHEQUE_NO", dto.getChequeNo());
        Sessions.getCurrent().setAttribute("SELECTED_OUTWARD_BATCH_ID", batchIdStr);
        Sessions.getCurrent().setAttribute("SELECTED_CHEQUE_ID", chqIdStr);

        if (isDataEntry) {
            Executions.sendRedirect("/outward/maker/data-entry.zul");
            return;
        }

        // MICR Repair: load into mainContentArea Include
        String source = "SCAN";
        Component root = Executions.getCurrent().getDesktop().getFirstPage().getFirstRoot();
        Component mainContentArea = root.getFellowIfAny("mainContentArea", true);

        if (mainContentArea instanceof Include) {
            Include include = (Include) mainContentArea;
            include.setAttribute("MICR_REPAIR_SOURCE", source);
            include.setAttribute("MICR_REPAIR_BATCH_ID", batchIdStr);
            include.setSrc("/outward/maker/micr-repair/micr-repair.zul");
        } else {
            System.out.println("ERROR: mainContentArea Include not found. Falling back to redirect.");
            Executions.sendRedirect("/outward/maker/micr-repair/micr-repair.zul");
        }
    }
    
}