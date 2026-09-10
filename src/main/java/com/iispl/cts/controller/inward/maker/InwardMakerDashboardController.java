package com.iispl.cts.controller.inward.maker;

import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.*;

import com.iispl.cts.dto.InwardDashboardBatchDTO;
import com.iispl.cts.dto.InwardDashboardKpiDTO;
import com.iispl.cts.service.inward.InwardDashboardService;
import com.iispl.cts.serviceimpl.inward.InwardDashboardServiceImpl;

public class InwardMakerDashboardController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    private InwardDashboardService dashboardService;

    private Label lblPartiallyProcessedCount;
    private Label lblSentBackCount;
    private Label lblSentToRrfCount;

    private Textbox txtSearchBatchId;
    private Button btnSearchBatch;
    private Button btnClearSearch;
    private Button btnRefreshDashboard;

    private Listbox lbxBatchStatus;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        this.dashboardService = new InwardDashboardServiceImpl();
        initListRenderer();
        refreshAllDashboardData();
    }

    public void onClick$btnRefreshDashboard() {
        refreshAllDashboardData();
    }

    // Instant keystroke search
    public void onChanging$txtSearchBatchId(InputEvent event) {
        loadBatchData(event.getValue());
    }

    public void onChange$txtSearchBatchId() {
        loadBatchData(txtSearchBatchId != null ? txtSearchBatchId.getValue() : "");
    }

    public void onClick$btnSearchBatch() {
        loadBatchData(txtSearchBatchId != null ? txtSearchBatchId.getValue() : "");
    }

    public void onOK$txtSearchBatchId() {
        loadBatchData(txtSearchBatchId != null ? txtSearchBatchId.getValue() : "");
    }

    public void onClick$btnClearSearch() {
        if (txtSearchBatchId != null) {
            txtSearchBatchId.setValue("");
        }
        loadBatchData("");
    }

    private void refreshAllDashboardData() {
        loadKpiCounters();
        loadBatchData(txtSearchBatchId != null ? txtSearchBatchId.getValue() : "");
    }

    private void loadKpiCounters() {
        InwardDashboardKpiDTO kpi = dashboardService.getDashboardSummary();
        if (lblPartiallyProcessedCount != null) {
            lblPartiallyProcessedCount.setValue(String.valueOf(kpi.getPartiallyProcessedBatches()));
        }
        if (lblSentBackCount != null) {
            lblSentBackCount.setValue(String.valueOf(kpi.getSentBackBatches()));
        }
        if (lblSentToRrfCount != null) {
            lblSentToRrfCount.setValue(String.valueOf(kpi.getReturnRequestCheques()));
        }
    }

    private void loadBatchData(String query) {
        if (lbxBatchStatus == null) return;
        List<InwardDashboardBatchDTO> batches = dashboardService.getRecentBatches(query);
        lbxBatchStatus.setModel(new ListModelList<>(batches));
    }

    private void initListRenderer() {
        if (lbxBatchStatus == null) return;

        lbxBatchStatus.setItemRenderer((Listitem item, InwardDashboardBatchDTO batch, int index) -> {
            item.setValue(batch);
            item.setStyle("cursor: pointer; height: 50px;");

            // Row click redirects directly to the appropriate workspace
            item.addEventListener("onClick", e -> {
                Sessions.getCurrent().setAttribute("ACTIVE_INWARD_BATCH_ID", batch.getBatchId());
                String targetZul = dashboardService.resolveWorkspaceTarget(batch.getBatchId());
                navigateToSpaPage(targetZul);
            });

            // 1. Batch ID
            Listcell cellId = new Listcell(batch.getBatchId());
            cellId.setStyle("color: #1d4ed8; font-weight: 700; font-size: 12px; white-space: nowrap;");
            cellId.setParent(item);

            // 2. Date
            Listcell cellDate = new Listcell(batch.getBatchDate());
            cellDate.setStyle("color: #475569; font-weight: 500; font-size: 12px; white-space: nowrap;");
            cellDate.setParent(item);

            // 3. Source
            Listcell cellSource = new Listcell(batch.getSource());
            cellSource.setStyle("color: #475569; font-weight: 600; font-size: 12px;");
            cellSource.setParent(item);

            // 4. Cheques Total
            Listcell cellCheques = new Listcell(String.valueOf(batch.getTotalCheques()));
            cellCheques.setStyle("font-weight: 700; font-size: 12px; color: #0f172a;");
            cellCheques.setParent(item);

            // 5. Accepted (Green)
            Listcell cellAccepted = new Listcell(String.valueOf(batch.getAcceptedCheques()));
            cellAccepted.setStyle("color: #16a34a; font-weight: 700; font-size: 12px;");
            cellAccepted.setParent(item);

            // 6. Back to Maker (Amber)
            Listcell cellBack = new Listcell(String.valueOf(batch.getBackToMakerCheques()));
            cellBack.setStyle(batch.getBackToMakerCheques() > 0 
                ? "color: #ea580c; font-weight: 800; font-size: 12px;" 
                : "color: #94a3b8; font-size: 12px;");
            cellBack.setParent(item);

            // 7. Returns (Red)
            Listcell cellRrf = new Listcell(String.valueOf(batch.getReturnRequestCheques()));
            cellRrf.setStyle(batch.getReturnRequestCheques() > 0 
                ? "color: #dc2626; font-weight: 800; font-size: 12px;" 
                : "color: #94a3b8; font-size: 12px;");
            cellRrf.setParent(item);

            // 8. Status Badge
            Listcell cellStatus = new Listcell();
            Label lblBadge = new Label(batch.getDisplayStatus());
            lblBadge.setStyle(batch.getStatusBadgeStyle());
            lblBadge.setParent(cellStatus);
            cellStatus.setParent(item);
        });
    }

    private void navigateToSpaPage(String zulPath) {
        Include mainInclude = null;
        try {
            mainInclude = (Include) Path.getComponent("/inwardMakerRootWin/mainContentArea");
        } catch (Exception ignored) {}

        if (mainInclude == null && self != null && self.getDesktop() != null) {
            for (org.zkoss.zk.ui.Page p : self.getDesktop().getPages()) {
                Component comp = p.getFellowIfAny("mainContentArea", true);
                if (comp instanceof Include) {
                    mainInclude = (Include) comp;
                    break;
                }
            }
        }

        if (mainInclude != null) {
            mainInclude.setSrc(null);
            mainInclude.setSrc(zulPath);
        } else {
            Messagebox.show("Navigation container (mainContentArea) not found.", "Error", Messagebox.OK, Messagebox.ERROR);
        }
    }
}