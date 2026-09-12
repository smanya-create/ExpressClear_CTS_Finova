package com.iispl.cts.controller.inward.checker;

import java.util.List;
import java.util.stream.Collectors;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

import com.iispl.cts.dto.DashboardSummaryDTO;
import com.iispl.cts.enums.inward.InwardBatchStatus;
import com.iispl.cts.service.inward.InwardBatchService;
import com.iispl.cts.serviceimpl.inward.InwardBatchServiceImpl;

public class InwardCheckerDashboardController extends GenericForwardComposer<Component> {

    
    private Listbox batchListbox;
    private Textbox txtSearchBatchId;
    private Button btnSearch;
    private Button btnClear;

   
    private final InwardBatchService batchService = new InwardBatchServiceImpl();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

       
        loadSubmittedBatches();

       
        if (btnSearch != null) {
            btnSearch.addEventListener(Events.ON_CLICK, e -> performSearch());
        }

        if (btnClear != null) {
            btnClear.addEventListener(Events.ON_CLICK, e -> performClear());
        }
    }

   
    private void loadSubmittedBatches() {

        batchListbox.getItems().clear();

        List<DashboardSummaryDTO> batches = batchService.getDashboardBatches();

        if (batches == null || batches.isEmpty()) {
            return;
        }

        for (DashboardSummaryDTO batch : batches) {
            renderBatchRow(batch);
        }
    }

    
    private void performSearch() {
        String query = (txtSearchBatchId != null && txtSearchBatchId.getValue() != null)
                ? txtSearchBatchId.getValue().trim() : "";

        if (query.isEmpty()) {
            loadSubmittedBatches();
            return;
        }

        DashboardSummaryDTO batch = batchService.getDashboardBatches().stream()
                .filter(b -> b.getBatchId() != null && b.getBatchId().equalsIgnoreCase(query))
                .findFirst()
                .orElse(null);

        batchListbox.getItems().clear();

        
        if(batch!=null)
        renderBatchRow(batch);
    }
    private void performClear() {

        if (txtSearchBatchId != null) {
            txtSearchBatchId.setValue("");
        }

        loadSubmittedBatches();
    }

    /**
     * Renders a single dashboard row.
     */
    private void renderBatchRow(DashboardSummaryDTO batch) {

        String batchStatus = batch.getBatchStatus();

        if (InwardBatchStatus.COMPLETED.toString().equalsIgnoreCase(batchStatus)) {
            return;
        }

        Listitem item = new Listitem();

        String batchId = batch.getBatchId();
        int totalCheques = batch.getTotalCheques();
        int normalCount = batch.getMakerApprovedCheques();
        int rejectionCount = batch.getRejectionRequestCheques();
        int returnedCount = batch.getMakerReturned();

        Listcell cellBatchId = new Listcell(batchId);
        cellBatchId.setStyle("font-weight:bold; color:#1D2D46;");
        item.appendChild(cellBatchId);

        Listcell cellTotal = new Listcell(String.valueOf(totalCheques));
        item.appendChild(cellTotal);

        Listcell cellNormal = new Listcell(String.valueOf(normalCount));
        item.appendChild(cellNormal);

        Listcell cellRejections = new Listcell();
        Label lblRejections = new Label(String.valueOf(rejectionCount));
        lblRejections.setStyle("color:#E32C10; font-weight:bold; font-size:12px");
        cellRejections.appendChild(lblRejections);
        item.appendChild(cellRejections);

        Listcell cellReturned = new Listcell();
        Label lblReturned = new Label(String.valueOf(returnedCount));
        lblReturned.setStyle("color:#A68026; font-weight:bold; font-size:12px");
        cellReturned.appendChild(lblReturned);
        item.appendChild(cellReturned);

        Listcell statusCell = new Listcell();
        Label statusLabel = new Label(batchStatus);

        if (InwardBatchStatus.COMPLETED.toString().equalsIgnoreCase(batchStatus)) {
            statusLabel.setSclass("batch-status batch-status-completed");
        } else {
            statusLabel.setSclass("batch-status batch-status-pending");
        }

        statusCell.setStyle("text-align:center;vertical-align:middle;");
        statusCell.appendChild(statusLabel);
        item.appendChild(statusCell);

        Listcell actionCell = new Listcell();

        String actionLabel;
        String buttonStyle;

        if (InwardBatchStatus.CHECKER_PROCESSING.toString().equalsIgnoreCase(batchStatus)) {

            actionLabel = "Process";
            buttonStyle = "background:#F5A900; color:#7A4500; border:1px solid #F5A900; border-radius:15px; cursor:pointer; font-size:9px; font-weight:bold; padding:2px 10px; line-height:16px;";
        } else if (InwardBatchStatus.CHECKER_PROCESSING_PENDING.toString().equalsIgnoreCase(batchStatus)) {

            actionLabel = "Proceed Verification";
            buttonStyle = "background:#242F82; color:white; border-radius:4px; cursor:pointer; font-size:10px; padding:2px 8px;";

        } else {

            return;
        }

        Button btnVerify = new Button(actionLabel);
        btnVerify.setStyle(buttonStyle);

        btnVerify.addEventListener(Events.ON_CLICK, e -> {

            boolean updated = batchService.updateProcessingBatchStatus(
                    batchId,
                    InwardBatchStatus.CHECKER_PROCESSING
            );

            if (updated) {
                Executions.getCurrent().sendRedirect(
                        "/inward/checker/verification.zul?batchId=" + batchId
                );
            } else {
                Messagebox.show("Unable to proceed");
            }
        });

        actionCell.setStyle("text-align:center;vertical-align:middle;");
        actionCell.appendChild(btnVerify);
        item.appendChild(actionCell);

        batchListbox.appendChild(item);
    }
}