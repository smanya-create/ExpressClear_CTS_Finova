package com.iispl.cts.controller.inward.maker;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Space;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

import com.iispl.cts.dto.InwardDashboardBatchDTO;
import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.enums.inward.InwardChequeStatus;
import com.iispl.cts.service.inward.InwardBatchService;
import com.iispl.cts.service.inward.InwardChequeService;
import com.iispl.cts.service.inward.InwardDashboardService;
import com.iispl.cts.serviceimpl.inward.InwardBatchServiceImpl;
import com.iispl.cts.serviceimpl.inward.InwardChequeServiceImpl;
import com.iispl.cts.serviceimpl.inward.InwardDashboardServiceImpl;

public class InwardMakerDashboardController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;
    private static final int PAGE_SIZE = 5;

    // Services
    private InwardDashboardService dashboardService;
    private InwardBatchService batchService;
    private InwardChequeService chequeService;

    // Filter Toolbar Components
    private Textbox inwardMakerTxtSearch;
    private Combobox inwardMakerCmbModule;
    private Combobox inwardMakerCmbStatus;
    private Button inwardMakerBtnSearch;
    private Button inwardMakerBtnClear;

    // Section Containers
    private Div inwardMakerReturnSection;
    private Space inwardMakerSectionSpacer;
    private Div inwardMakerProcessingSection;
    private Div inwardMakerPaginationBar;
    private Div inwardMakerReturnPaginationBar;

    // Batches Processing Grid & Pagination
    private Grid inwardMakerGridBatchDetails;
    private Rows inwardMakerRowsBatchDetails;
    private Vlayout inwardMakerVlayoutEmptyState;
    private Label inwardMakerLblCurrentPage;

    private Button inwardMakerBtnFirst;
    private Button inwardMakerBtnPrevious;
    private Button inwardMakerBtnNext;
    private Button inwardMakerBtnLast;

    // Return From Checker Grid & Pagination
    private Grid inwardMakerGridReturnedBatches;
    private Rows inwardMakerRowsReturnedBatches;
    private Vlayout inwardMakerVlayoutReturnedEmptyState;
    private Label inwardMakerLblReturnCurrentPage;

    private Button inwardMakerBtnReturnFirst;
    private Button inwardMakerBtnReturnPrevious;
    private Button inwardMakerBtnReturnNext;
    private Button inwardMakerBtnReturnLast;

    // Data lists & state
    private List<InwardDashboardBatchDTO> batchList = new ArrayList<>();
    private List<ReturnedChequeDisplayItem> returnedChequeList = new ArrayList<>();
    private int currentPage = 1;
    private int currentReturnPage = 1;

    // Active Filter State
    private String currentSearchKeyword = "";
    private String currentModule = "ALL";
    private String currentStatus = "ALL";

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        this.dashboardService = new InwardDashboardServiceImpl();
        this.batchService = new InwardBatchServiceImpl();
        this.chequeService = new InwardChequeServiceImpl();

        // Default Combobox Selections
        if (inwardMakerCmbModule != null && inwardMakerCmbModule.getItemCount() > 0) {
            inwardMakerCmbModule.setSelectedIndex(0);
        }
        if (inwardMakerCmbStatus != null && inwardMakerCmbStatus.getItemCount() > 0) {
            inwardMakerCmbStatus.setSelectedIndex(0);
        }

        // Filter Actions
        if (inwardMakerBtnSearch != null) inwardMakerBtnSearch.addEventListener("onClick", event -> onApplyFilter());
        if (inwardMakerBtnClear != null) inwardMakerBtnClear.addEventListener("onClick", event -> onClearFilter());

        if (inwardMakerTxtSearch != null) {
            inwardMakerTxtSearch.addEventListener("onOK", event -> onApplyFilter());
        }

        // Section 2 (Processing Batches) Pagination Listeners
        if (inwardMakerBtnFirst != null) inwardMakerBtnFirst.addEventListener("onClick", event -> goToFirstPage());
        if (inwardMakerBtnPrevious != null) inwardMakerBtnPrevious.addEventListener("onClick", event -> goToPreviousPage());
        if (inwardMakerBtnNext != null) inwardMakerBtnNext.addEventListener("onClick", event -> goToNextPage());
        if (inwardMakerBtnLast != null) inwardMakerBtnLast.addEventListener("onClick", event -> goToLastPage());

        // Section 1 (Return From Checker) Pagination Listeners
        if (inwardMakerBtnReturnFirst != null) inwardMakerBtnReturnFirst.addEventListener("onClick", event -> goToFirstReturnPage());
        if (inwardMakerBtnReturnPrevious != null) inwardMakerBtnReturnPrevious.addEventListener("onClick", event -> goToPreviousReturnPage());
        if (inwardMakerBtnReturnNext != null) inwardMakerBtnReturnNext.addEventListener("onClick", event -> goToNextReturnPage());
        if (inwardMakerBtnReturnLast != null) inwardMakerBtnReturnLast.addEventListener("onClick", event -> goToLastReturnPage());

        refreshAllDashboardData();
    }

    private void onApplyFilter() {
        this.currentSearchKeyword = (inwardMakerTxtSearch != null && inwardMakerTxtSearch.getValue() != null)
                ? inwardMakerTxtSearch.getValue().trim().toLowerCase() : "";

        this.currentModule = (inwardMakerCmbModule != null && inwardMakerCmbModule.getSelectedItem() != null)
                ? inwardMakerCmbModule.getSelectedItem().getValue().toString() : "ALL";

        this.currentStatus = (inwardMakerCmbStatus != null && inwardMakerCmbStatus.getSelectedItem() != null)
                ? inwardMakerCmbStatus.getSelectedItem().getValue().toString() : "ALL";

        refreshAllDashboardData();
    }

    private void onClearFilter() {
        this.currentSearchKeyword = "";
        this.currentModule = "ALL";
        this.currentStatus = "ALL";

        if (inwardMakerTxtSearch != null) inwardMakerTxtSearch.setValue("");
        if (inwardMakerCmbModule != null && inwardMakerCmbModule.getItemCount() > 0) inwardMakerCmbModule.setSelectedIndex(0);
        if (inwardMakerCmbStatus != null && inwardMakerCmbStatus.getItemCount() > 0) inwardMakerCmbStatus.setSelectedIndex(0);

        refreshAllDashboardData();
    }

    public void refreshAllDashboardData() {
        boolean showReturned = "ALL".equalsIgnoreCase(currentModule) || "RETURN_FROM_CHECKER".equalsIgnoreCase(currentModule);
        boolean showProcessing = "ALL".equalsIgnoreCase(currentModule) || "BATCH_PROCESSING".equalsIgnoreCase(currentModule);

        if (inwardMakerReturnSection != null) inwardMakerReturnSection.setVisible(showReturned);
        if (inwardMakerSectionSpacer != null) inwardMakerSectionSpacer.setVisible(showReturned && showProcessing);
        if (inwardMakerProcessingSection != null) inwardMakerProcessingSection.setVisible(showProcessing);

        if (showReturned) {
            loadReturnedCheques();
        }
        if (showProcessing) {
            loadBatches();
        }
    }

    // =========================================================
    // 1. RETURN FROM CHECKER SECTION
    // =========================================================
    private void loadReturnedCheques() {
        this.returnedChequeList = new ArrayList<>();

        try {
            List<InwardDashboardBatchDTO> recentBatches = dashboardService.getRecentBatches("");
            if (recentBatches != null) {
                for (InwardDashboardBatchDTO bDto : recentBatches) {
                    if (bDto.getBackToMakerCheques() > 0) {
                        
                        // Filter by Batch ID Only
                        if (!currentSearchKeyword.isEmpty()) {
                            String bId = bDto.getBatchId() != null ? bDto.getBatchId().toLowerCase() : "";
                            if (!bId.contains(currentSearchKeyword)) {
                                continue;
                            }
                        }

                        List<InwardCheque> batchCheques = chequeService.getChequesByBatchAndStatus(bDto.getBatchId(), null);
                        if (batchCheques != null) {
                            for (InwardCheque chq : batchCheques) {
                                String status = chq.getChequeStatus();
                                if (isSentBackStatus(status)) {
                                    String normStatus = normalizeStatus(status);
                                    String displayStatus = getDisplayStatus(normStatus);

                                    // Filter by Status
                                    if (!"ALL".equalsIgnoreCase(currentStatus) && !displayStatus.equalsIgnoreCase(currentStatus)) {
                                        continue;
                                    }

                                    returnedChequeList.add(new ReturnedChequeDisplayItem(bDto.getBatchId(), chq, displayStatus));
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.currentReturnPage = 1;
        renderCurrentReturnPage();
    }

    private void renderCurrentReturnPage() {
        if (inwardMakerRowsReturnedBatches == null) return;
        inwardMakerRowsReturnedBatches.getChildren().clear();

        if (returnedChequeList == null || returnedChequeList.isEmpty()) {
            if (inwardMakerVlayoutReturnedEmptyState != null) inwardMakerVlayoutReturnedEmptyState.setVisible(true);
            if (inwardMakerGridReturnedBatches != null) inwardMakerGridReturnedBatches.setVisible(false);
            if (inwardMakerReturnPaginationBar != null) inwardMakerReturnPaginationBar.setVisible(false);
            updateReturnPagination();
            return;
        }

        if (inwardMakerVlayoutReturnedEmptyState != null) inwardMakerVlayoutReturnedEmptyState.setVisible(false);
        if (inwardMakerGridReturnedBatches != null) inwardMakerGridReturnedBatches.setVisible(true);
        if (inwardMakerReturnPaginationBar != null) inwardMakerReturnPaginationBar.setVisible(true);

        int totalPages = getTotalReturnPages();
        if (currentReturnPage > totalPages) currentReturnPage = totalPages;
        if (currentReturnPage < 1) currentReturnPage = 1;

        int startIndex = (currentReturnPage - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, returnedChequeList.size());

        for (int i = startIndex; i < endIndex; i++) {
            ReturnedChequeDisplayItem item = returnedChequeList.get(i);
            if (item != null) {
                createReturnedChequeRow(item.batchId, item.cheque, item.displayStatus);
            }
        }

        updateReturnPagination();
    }

    private void createReturnedChequeRow(String batchId, InwardCheque cheque, String displayStatus) {
        Row row = new Row();

        Label batchIdLabel = new Label(getValue(batchId));
        batchIdLabel.setSclass("inward-maker-batch-id");

        Label chequeNumberLabel = new Label(getValue(cheque.getChequeNumber()));
        chequeNumberLabel.setSclass("inward-maker-cheque-number");

        Label statusLabel = new Label(displayStatus);
        statusLabel.setSclass("inward-maker-status");

        Label reasonLabel = new Label("-");
        reasonLabel.setSclass("inward-maker-reason");

        Button viewButton = new Button("VIEW DETAILS");
        viewButton.setSclass("inward-maker-view-button");
        viewButton.addEventListener("onClick", event -> openBatchWorkflow(batchId));

        row.appendChild(batchIdLabel);
        row.appendChild(chequeNumberLabel);
        row.appendChild(statusLabel);
        row.appendChild(reasonLabel);
        row.appendChild(viewButton);

        inwardMakerRowsReturnedBatches.appendChild(row);
    }

    // Return Pagination Navigation
    private void goToFirstReturnPage() {
        if (currentReturnPage > 1) {
            currentReturnPage = 1;
            renderCurrentReturnPage();
        }
    }

    private void goToPreviousReturnPage() {
        if (currentReturnPage > 1) {
            currentReturnPage--;
            renderCurrentReturnPage();
        }
    }

    private void goToNextReturnPage() {
        if (currentReturnPage < getTotalReturnPages()) {
            currentReturnPage++;
            renderCurrentReturnPage();
        }
    }

    private void goToLastReturnPage() {
        int totalPages = getTotalReturnPages();
        if (currentReturnPage < totalPages) {
            currentReturnPage = totalPages;
            renderCurrentReturnPage();
        }
    }

    private int getTotalReturnPages() {
        if (returnedChequeList == null || returnedChequeList.isEmpty()) {
            return 1;
        }
        return (int) Math.ceil((double) returnedChequeList.size() / PAGE_SIZE);
    }

    private void updateReturnPagination() {
        int totalPages = getTotalReturnPages();
        if (inwardMakerLblReturnCurrentPage != null) {
            inwardMakerLblReturnCurrentPage.setValue(currentReturnPage + " / " + totalPages);
        }
        if (inwardMakerBtnReturnFirst != null) inwardMakerBtnReturnFirst.setDisabled(currentReturnPage <= 1);
        if (inwardMakerBtnReturnPrevious != null) inwardMakerBtnReturnPrevious.setDisabled(currentReturnPage <= 1);
        if (inwardMakerBtnReturnNext != null) inwardMakerBtnReturnNext.setDisabled(currentReturnPage >= totalPages);
        if (inwardMakerBtnReturnLast != null) inwardMakerBtnReturnLast.setDisabled(currentReturnPage >= totalPages);
    }

    // =========================================================
    // 2. BATCHES PROCESSING SECTION
    // =========================================================
    private void loadBatches() {
        try {
            List<InwardDashboardBatchDTO> allBatches = dashboardService.getRecentBatches("");
            this.batchList = new ArrayList<>();

            if (allBatches != null) {
                for (InwardDashboardBatchDTO b : allBatches) {
                    if (b.getBackToMakerCheques() == 0 && !"SENT_BACK".equalsIgnoreCase(b.getDisplayStatus())) {
                        
                        // Filter by Search Keyword (Batch ID only)
                        if (!currentSearchKeyword.isEmpty()) {
                            String bId = b.getBatchId() != null ? b.getBatchId().toLowerCase() : "";
                            if (!bId.contains(currentSearchKeyword)) {
                                continue;
                            }
                        }

                        // Determine Status
                        String targetZul = dashboardService.resolveWorkspaceTarget(b.getBatchId());
                        String trueStatus = (targetZul != null && targetZul.toLowerCase().contains("micr"))
                                ? "PENDING_MICR_REPAIR" : "PENDING_DATA_ENTRY";

                        // Filter by Status
                        if (!"ALL".equalsIgnoreCase(currentStatus) && !trueStatus.equalsIgnoreCase(currentStatus)) {
                            continue;
                        }

                        this.batchList.add(b);
                    }
                }
            }

            this.currentPage = 1;
            renderCurrentPage();
        } catch (Exception e) {
            e.printStackTrace();
            this.batchList = new ArrayList<>();
            this.currentPage = 1;
            renderCurrentPage();
        }
    }

    private void renderCurrentPage() {
        if (inwardMakerRowsBatchDetails == null) return;
        inwardMakerRowsBatchDetails.getChildren().clear();

        if (batchList == null || batchList.isEmpty()) {
            if (inwardMakerVlayoutEmptyState != null) inwardMakerVlayoutEmptyState.setVisible(true);
            if (inwardMakerGridBatchDetails != null) inwardMakerGridBatchDetails.setVisible(false);
            if (inwardMakerPaginationBar != null) inwardMakerPaginationBar.setVisible(false);
            updatePagination();
            return;
        }

        if (inwardMakerVlayoutEmptyState != null) inwardMakerVlayoutEmptyState.setVisible(false);
        if (inwardMakerGridBatchDetails != null) inwardMakerGridBatchDetails.setVisible(true);
        if (inwardMakerPaginationBar != null) inwardMakerPaginationBar.setVisible(true);

        int totalPages = getTotalPages();
        if (currentPage > totalPages) currentPage = totalPages;
        if (currentPage < 1) currentPage = 1;

        int startIndex = (currentPage - 1) * PAGE_SIZE;
        int endIndex = Math.min(startIndex + PAGE_SIZE, batchList.size());

        for (int i = startIndex; i < endIndex; i++) {
            InwardDashboardBatchDTO batch = batchList.get(i);
            if (batch != null) {
                createBatchRow(batch);
            }
        }

        updatePagination();
    }

    private void createBatchRow(InwardDashboardBatchDTO batch) {
        Row row = new Row();

        Label batchIdLabel = new Label(getValue(batch.getBatchId()));
        batchIdLabel.setSclass("inward-maker-batch-id");
        batchIdLabel.setStyle("cursor: pointer;");
        batchIdLabel.addEventListener("onClick", event -> openBatchWorkflow(batch.getBatchId()));

        Label chequeCountLabel = new Label(String.valueOf(batch.getTotalCheques()));
        chequeCountLabel.setSclass("inward-maker-cheque-count");

        InwardBatch fullBatch = batchService.getBatchById(batch.getBatchId());
        BigDecimal totalAmt = (fullBatch != null && fullBatch.getActualTotalAmount() != null) 
                ? fullBatch.getActualTotalAmount() : BigDecimal.ZERO;
        Label totalAmountLabel = new Label(formatIndianAmount(totalAmt));
        totalAmountLabel.setSclass("inward-maker-total-amount");

        String targetZul = dashboardService.resolveWorkspaceTarget(batch.getBatchId());
        String trueStatus = (targetZul != null && targetZul.toLowerCase().contains("micr")) 
                ? "PENDING_MICR_REPAIR" : "PENDING_DATA_ENTRY";

        Label statusLabel = new Label(getDisplayStatus(trueStatus));
        statusLabel.setSclass("inward-maker-status");

        Button viewButton = new Button("VIEW DETAILS");
        viewButton.setSclass("inward-maker-view-button");
        viewButton.addEventListener("onClick", event -> openBatchWorkflow(batch.getBatchId()));

        row.appendChild(batchIdLabel);
        row.appendChild(chequeCountLabel);
        row.appendChild(totalAmountLabel);
        row.appendChild(statusLabel);
        row.appendChild(viewButton);

        inwardMakerRowsBatchDetails.appendChild(row);
    }

    // =========================================================
    // WORKFLOW ROUTING & SPA NAVIGATION
    // =========================================================
    private void openBatchWorkflow(String batchId) {
        if (batchId == null || batchId.trim().isEmpty()) {
            return;
        }

        String trimmedBatchId = batchId.trim();

        Sessions.getCurrent().setAttribute("INWARD_MAKER_SELECTED_BATCH_ID", trimmedBatchId);
        Sessions.getCurrent().setAttribute("ACTIVE_INWARD_BATCH_ID", trimmedBatchId);
        Sessions.getCurrent().setAttribute("MICR_REPAIR_BATCH_ID", trimmedBatchId);
        Sessions.getCurrent().setAttribute("DATA_ENTRY_BATCH_ID", trimmedBatchId);
        Sessions.getCurrent().setAttribute("batchId", trimmedBatchId);

        Include mainContentArea = null;
        if (Executions.getCurrent() != null && Executions.getCurrent().getDesktop() != null) {
            for (org.zkoss.zk.ui.Page page : Executions.getCurrent().getDesktop().getPages()) {
                Component comp = page.getFellowIfAny("mainContentArea", true);
                if (comp instanceof Include) {
                    mainContentArea = (Include) comp;
                    break;
                }
            }
        }

        if (mainContentArea != null) {
            try {
                mainContentArea.clearDynamicProperties();
                mainContentArea.setDynamicProperty("batchId", trimmedBatchId);
                mainContentArea.setAttribute("batchId", trimmedBatchId);
                
                mainContentArea.setSrc(null);
                mainContentArea.setSrc("/inward/maker/batch-details.zul?batchId=" + trimmedBatchId);
                return;
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        Executions.sendRedirect("/inward/maker/index.zul?page=batch-details&batchId=" + trimmedBatchId);
    }

    // =========================================================
    // PROCESSING PAGINATION LOGIC
    // =========================================================
    private void goToFirstPage() {
        if (currentPage > 1) {
            currentPage = 1;
            renderCurrentPage();
        }
    }

    private void goToPreviousPage() {
        if (currentPage > 1) {
            currentPage--;
            renderCurrentPage();
        }
    }

    private void goToNextPage() {
        if (currentPage < getTotalPages()) {
            currentPage++;
            renderCurrentPage();
        }
    }

    private void goToLastPage() {
        int totalPages = getTotalPages();
        if (currentPage < totalPages) {
            currentPage = totalPages;
            renderCurrentPage();
        }
    }

    private int getTotalPages() {
        if (batchList == null || batchList.isEmpty()) {
            return 1;
        }
        return (int) Math.ceil((double) batchList.size() / PAGE_SIZE);
    }

    private void updatePagination() {
        int totalPages = getTotalPages();
        if (inwardMakerLblCurrentPage != null) {
            inwardMakerLblCurrentPage.setValue(currentPage + " / " + totalPages);
        }
        if (inwardMakerBtnFirst != null) inwardMakerBtnFirst.setDisabled(currentPage <= 1);
        if (inwardMakerBtnPrevious != null) inwardMakerBtnPrevious.setDisabled(currentPage <= 1);
        if (inwardMakerBtnNext != null) inwardMakerBtnNext.setDisabled(currentPage >= totalPages);
        if (inwardMakerBtnLast != null) inwardMakerBtnLast.setDisabled(currentPage >= totalPages);
    }

    // =========================================================
    // FORMATTING & LABEL HELPERS
    // =========================================================
    private boolean isSentBackStatus(String status) {
        if (status == null) return false;
        return InwardChequeStatus.SEND_BACK_TO_MAKER_DATA_ENTRY.name().equalsIgnoreCase(status)
                || InwardChequeStatus.SEND_BACK_TO_MAKER.name().equalsIgnoreCase(status);
    }

    private String formatIndianAmount(BigDecimal amount) {
        if (amount == null) return "₹0.00";
        DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH);
        symbols.setGroupingSeparator(',');
        DecimalFormat formatter = new DecimalFormat("##,##,##0.00", symbols);
        return "₹" + formatter.format(amount);
    }

    private String getValue(Object value) {
        if (value == null) return "-";
        String text = String.valueOf(value);
        return text.trim().isEmpty() ? "-" : text;
    }

    private String normalizeStatus(String status) {
        return (status != null) ? status.trim().toUpperCase() : "";
    }

    private String getDisplayStatus(String status) {
        if (status == null || status.trim().isEmpty()) return "PENDING_MAKER_PROCESS";

        String s = status.trim().replace(" ", "_").toUpperCase();

        if ("SEND_BACK_TO_MAKER_DATA_ENTRY".equals(s)) return "FIX DATA ENTRY";
        if ("SEND_BACK_TO_MAKER_MICR".equals(s)) return "REPAIR MICR";
        if ("SEND_BACK_TO_MAKER".equals(s) || "SENT_BACK".equals(s)) return "NEEDS REWORK";

        if ("PENDING_MICR_REPAIR".equals(s) || "MICR_REPAIR_PENDING".equals(s) || "MICR_REPAIR".equals(s)) {
            return "PENDING_MICR_REPAIR";
        }
        
        if ("PENDING_DATA_ENTRY".equals(s) || "DATA_ENTRY_PENDING".equals(s) 
                || "PARTIALLY_PROCESSED".equals(s) || "DATA_ENTRY".equals(s)) {
            return "PENDING_DATA_ENTRY";
        }

        if ("PENDING_MAKER_PROCESS".equals(s) || "PROCESSING".equals(s) || "RAW".equals(s)) {
            return "PENDING_MAKER_PROCESS";
        }

        return s;
    }

    // Helper wrapper for paginating returned cheques
    private static class ReturnedChequeDisplayItem {
        final String batchId;
        final InwardCheque cheque;
        final String displayStatus;

        ReturnedChequeDisplayItem(String batchId, InwardCheque cheque, String displayStatus) {
            this.batchId = batchId;
            this.cheque = cheque;
            this.displayStatus = displayStatus;
        }
    }
}