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
import org.zkoss.zul.Textbox;

import com.iispl.cts.dto.DashboardSummaryDTO;
import com.iispl.cts.service.inward.InwardBatchService;
import com.iispl.cts.serviceimpl.inward.InwardBatchServiceImpl;

public class InwardCheckerDashboardController extends GenericForwardComposer<Component> {

    // Auto-wired components matching ZUL component IDs
    private Listbox batchListbox;
    private Textbox txtSearchBatchId;
    private Button btnSearch;
    private Button btnClear;

    // Service dependency
    private final InwardBatchService batchService = new InwardBatchServiceImpl();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        // Initial table load
        loadSubmittedBatches();

        // Attach event listeners explicitly
        if (btnSearch != null) {
            btnSearch.addEventListener(Events.ON_CLICK, e -> performSearch());
        }

        if (btnClear != null) {
            btnClear.addEventListener(Events.ON_CLICK, e -> performClear());
        }
    }

    /**
     * Loads all batches submitted to Inward Checker.
     * Dashboard data is retrieved from the service layer.
     */
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

    /**
     * Handles search by Batch ID.
     */
    private void performSearch() {

        String query = (txtSearchBatchId != null && txtSearchBatchId.getValue() != null)
                ? txtSearchBatchId.getValue().trim()
                : "";

        if (query.isEmpty()) {
            loadSubmittedBatches();
            return;
        }

        List<DashboardSummaryDTO> batches = batchService.getDashboardBatches();

        if (batches == null || batches.isEmpty()) {
            batchListbox.getItems().clear();
            return;
        }

        List<DashboardSummaryDTO> filteredBatches = batches.stream()
                .filter(b -> b.getBatchId() != null
                        && b.getBatchId().equalsIgnoreCase(query))
                .collect(Collectors.toList());

        batchListbox.getItems().clear();

        for (DashboardSummaryDTO batch : filteredBatches) {
            renderBatchRow(batch);
        }
    }

    /**
     * Clears the search field and reloads all batches.
     */
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

        Listitem item = new Listitem();

        String batchId = batch.getBatchId();
        int totalCheques = batch.getTotalCheques();
        int normalCount = batch.getMakerApprovedCheques();
        int rejectionCount = batch.getRejectionRequestCheques();

        // 1. Batch ID
        Listcell cellBatchId = new Listcell(batchId);
        cellBatchId.setStyle("font-weight:bold; color:#1D2D46;");
        item.appendChild(cellBatchId);

        // 2. Total Cheques
        Listcell cellTotal = new Listcell(String.valueOf(totalCheques));
        item.appendChild(cellTotal);

        // 3. Normal Cheques
        Listcell cellNormal = new Listcell(String.valueOf(normalCount));
        item.appendChild(cellNormal);

        // 4. Rejection Requests
        Listcell cellRejections = new Listcell();

        Label lblRejections = new Label(String.valueOf(rejectionCount));
        lblRejections.setStyle(
                "color:#E32C10; font-weight:bold; font-size:12px"
        );

        cellRejections.appendChild(lblRejections);
        item.appendChild(cellRejections);

        // 5. Action Button
        Listcell actionCell = new Listcell();

        Button btnVerify = new Button("Proceed Verification");

        btnVerify.setStyle(
                "background:#242F82; " +
                "color:white; " +
                "border:0; " +
                "border-radius:4px; " +
                "cursor:pointer; " +
                "font-size:10px; " +
                "padding:3px 6px;"
        );

        btnVerify.addEventListener(Events.ON_CLICK, e -> {

            Executions.getCurrent().sendRedirect(
                    "/inward/checker/verification.zul?batchId=" + batchId
            );
        });

        actionCell.appendChild(btnVerify);
        item.appendChild(actionCell);

        batchListbox.appendChild(item);
    }
}