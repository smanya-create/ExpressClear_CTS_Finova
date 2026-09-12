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
import org.zkoss.zul.Intbox;
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
    
    private Button btnFirstPage;
    private Button btnPrevPage;
    private Intbox ibCurrentPage;
    private Label lblTotalPages;
    private Button btnNextPage;
    private Button btnLastPage;
    
    private static final int PAGE_SIZE = 12;
    private int activePageIndex = 0;
    private int totalPages = 1;
    private List<UnprocessedChequeDTO> currentFilteredList = new ArrayList<>();

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
 // =========================================================
    // LISTBOX RENDERER (Strict CTS Alignment System)
    // =========================================================
    private void initListboxRenderer() {
        lstUnprocessed.setItemRenderer(new ListitemRenderer<UnprocessedChequeDTO>() {
            @Override
            public void render(Listitem item, UnprocessedChequeDTO dto, int index) {
                item.setValue(dto);

                // 1. Original Batch / Session (Text -> LEFT)
                Listcell cellBatch = new Listcell();
                cellBatch.setStyle("text-align: left; vertical-align: middle; padding-left: 14px;");
                Vlayout vBatch = new Vlayout();
                vBatch.setSpacing("2px");

                Label lblBNo = new Label(dto.getBatchNo());
                lblBNo.setStyle("font-size: 13px; font-weight: 600; color: #1e293b; display: block;");
                
                Label lblSName = new Label(dto.getOriginalSessionName() != null ? dto.getOriginalSessionName() : "Scan Staging");
                lblSName.setStyle("font-size: 11px; color: #64748b; display: block;");
                
                vBatch.appendChild(lblBNo);
                vBatch.appendChild(lblSName);
                cellBatch.appendChild(vBatch);

                // 2. Cheque No (Number -> CENTER)
                Listcell cellChq = new Listcell();
                cellChq.setStyle("text-align: center; vertical-align: middle;");
                Label lblChq = new Label(dto.getChequeNo() != null ? dto.getChequeNo() : "------");
                lblChq.setStyle("font-family: monospace; font-size: 13px; font-weight: 600; color: #334155; display: block; text-align: center;");
                cellChq.appendChild(lblChq);

                // 3. MICR Sort Code (Number -> CENTER)
                Listcell cellSort = new Listcell();
                cellSort.setStyle("text-align: center; vertical-align: middle;");
                Label lblSort = new Label(dto.getSortCode() != null ? dto.getSortCode() : "------");
                lblSort.setStyle("font-family: monospace; font-size: 13px; font-weight: 500; color: #334155; display: block; text-align: center;");
                cellSort.appendChild(lblSort);

                // 4. Amount (Number / Currency -> CENTER)
                Listcell cellAmt = new Listcell();
                cellAmt.setStyle("text-align: center; vertical-align: middle;");
                Label lblAmt = new Label(dto.getAmount() != null ? "₹ " + df.format(dto.getAmount()) : "₹ 0.00");
                lblAmt.setStyle("font-size: 13px; font-weight: 700; color: #0f172a; display: block; text-align: center;");
                cellAmt.appendChild(lblAmt);

             // 5. Required Task Badge (Same Orange for Both)
                Listcell cellStage = new Listcell();
                cellStage.setStyle("text-align: center; vertical-align: middle;");
                Label lblStage = new Label();
                boolean wasDataEntry = isItemDataEntry(dto);

                String orangeBadgeStyle = "display: table; margin: 0 auto; padding: 4px 12px; border-radius: 12px; "
                        + "font-size: 11px; font-weight: 700; white-space: nowrap; "
                        + "background: #ffedd5; color: #c2410c; border: 1px solid #fed7aa;";

                if (wasDataEntry) {
                    lblStage.setValue("Data Entry Pending");
                } else {
                    lblStage.setValue("MICR Repair Pending");
                }
                lblStage.setStyle(orangeBadgeStyle);
                cellStage.appendChild(lblStage);

                // 6. Reason / Remarks (Left-aligned)
                Listcell cellRemarks = new Listcell();
                cellRemarks.setStyle("text-align: left; vertical-align: middle; padding-left: 14px;");
                String reasonText = dto.getSendBackReason() != null ? dto.getSendBackReason() : "Scan Review";
                if (dto.getRemarks() != null && !dto.getRemarks().isEmpty()) {
                    reasonText += " (" + dto.getRemarks() + ")";
                }
                Label lblReason = new Label(reasonText);
                lblReason.setStyle("font-size: 12px; font-style: italic; color: #c2410c; display: block; word-break: break-word;");
                cellRemarks.appendChild(lblReason);

                // 7. Action Button (Plain text, no icons, no symbols)
                Listcell cellAction = new Listcell();
                cellAction.setStyle("text-align: center; vertical-align: middle; padding: 0 8px;");
                Button btnAction = new Button();
                String darkNavyBtnStyle = "background: #173B61; color: #ffffff; border: 1px solid #173B61; "
                        + "font-size: 11px; font-weight: 600; padding: 6px 16px; border-radius: 4px; "
                        + "cursor: pointer; white-space: nowrap; box-shadow: 0 1px 2px rgba(0,0,0,0.1);";
                btnAction.setStyle(darkNavyBtnStyle);

                if (wasDataEntry) {
                    btnAction.setLabel("Data Entry");
                } else {
                    btnAction.setLabel("MICR Repair");
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

        this.currentFilteredList = masterList.stream().filter(item -> {
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

        // Calculate pages
        this.totalPages = (int) Math.ceil((double) currentFilteredList.size() / PAGE_SIZE);
        if (this.totalPages < 1) this.totalPages = 1;
        
        loadPage(0);
    }
    private void loadPage(int pageIndex) {
        if (pageIndex >= this.totalPages) pageIndex = this.totalPages - 1;
        if (pageIndex < 0) pageIndex = 0;
        this.activePageIndex = pageIndex;

        // Update toolbar controls
        if (ibCurrentPage != null) ibCurrentPage.setValue(this.activePageIndex + 1);
        if (lblTotalPages != null) lblTotalPages.setValue("/ " + this.totalPages);

        boolean isFirst = (this.activePageIndex <= 0);
        boolean isLast = (this.activePageIndex >= this.totalPages - 1);

        if (btnFirstPage != null) btnFirstPage.setDisabled(isFirst);
        if (btnPrevPage != null) btnPrevPage.setDisabled(isFirst);
        if (btnNextPage != null) btnNextPage.setDisabled(isLast);
        if (btnLastPage != null) btnLastPage.setDisabled(isLast);

        // Slice list for active page
        int fromIndex = this.activePageIndex * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, currentFilteredList.size());
        
        List<UnprocessedChequeDTO> pageSubList = (fromIndex < currentFilteredList.size())
                ? currentFilteredList.subList(fromIndex, toIndex)
                : new ArrayList<>();

        lstUnprocessed.setModel(new ListModelList<>(pageSubList));
    }
    public void onClick$btnFirstPage(Event event) {
        if (activePageIndex > 0) loadPage(0);
    }

    public void onClick$btnPrevPage(Event event) {
        if (activePageIndex > 0) loadPage(activePageIndex - 1);
    }

    public void onClick$btnNextPage(Event event) {
        if (activePageIndex < totalPages - 1) loadPage(activePageIndex + 1);
    }

    public void onClick$btnLastPage(Event event) {
        if (activePageIndex < totalPages - 1) loadPage(totalPages - 1);
    }

    public void onChange$ibCurrentPage(Event event) {
        Integer target = ibCurrentPage.getValue();
        if (target == null || target < 1) target = 1;
        else if (target > totalPages) target = totalPages;
        loadPage(target - 1);
    }

    public void onOK$ibCurrentPage(Event event) {
        onChange$ibCurrentPage(event);
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