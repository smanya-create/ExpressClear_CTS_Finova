package com.iispl.cts.controller.outward.checker;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

import com.iispl.cts.dao.outward.CheckerUnprocessedChequeDAO;
import com.iispl.cts.daoimpl.outward.CheckerUnprocessedChequeDAOImpl;
import com.iispl.cts.dto.UnprocessedChequeDTO;

public class CheckerUnprocessedChequesController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // Metric Summary Labels
    private Label lblTotalVerifyCount;
    private Label lblTotalVerifyAmount;

    // Search and Table Controls
    private Textbox txtSearchBatch;
    private Button btnSearch;
    private Button btnReset;
    private Button btnRefreshQueue;
    private Listbox lstCheckerUnprocessed;

    private final CheckerUnprocessedChequeDAO checkerDAO = new CheckerUnprocessedChequeDAOImpl();
    private List<UnprocessedChequeDTO> masterList = new ArrayList<>();
    private final DecimalFormat df = new DecimalFormat("##,##,##0.00");

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initListboxRenderer();
        loadCheckerUnprocessedCheques();
    }

    private void initListboxRenderer() {
        lstCheckerUnprocessed.setItemRenderer(new ListitemRenderer<UnprocessedChequeDTO>() {
            @Override
            public void render(Listitem item, UnprocessedChequeDTO dto, int index) {
                item.setValue(dto);

                // 1. Batch & Session Info
                Listcell cellBatch = new Listcell();
                Vlayout vBatch = new Vlayout();
                vBatch.setSpacing("2px");
                Label lblBNo = new Label(dto.getBatchNo());
                lblBNo.setSclass("list-batch-title");
                Label lblSName = new Label("Rollover: " + (dto.getOriginalSessionName() != null ? dto.getOriginalSessionName() : "Prior EOD"));
                lblSName.setSclass("list-batch-sub");
                vBatch.appendChild(lblBNo);
                vBatch.appendChild(lblSName);
                cellBatch.appendChild(vBatch);

                // 2. Cheque No
                Listcell cellChq = new Listcell(dto.getChequeNo() != null ? dto.getChequeNo() : "------");
                cellChq.setSclass("list-monospace");

                // 3. Sort Code
                Listcell cellSort = new Listcell(dto.getSortCode() != null ? dto.getSortCode() : "------");
                cellSort.setSclass("list-monospace");

                // 4. Amount
                Listcell cellAmt = new Listcell(dto.getAmount() != null ? "₹ " + df.format(dto.getAmount()) : "₹ 0.00");
                cellAmt.setSclass("list-amount");

                // 5. Status Badge (Pending Verification)
                Listcell cellTask = new Listcell();
                cellTask.setStyle("text-align: center; vertical-align: middle;");

                Label lblTask = new Label("Pending Verification");
                lblTask.setStyle("display: table; margin: 0 auto; padding: 4px 12px; border-radius: 12px; "
                        + "font-size: 11px; font-weight: 700; white-space: nowrap; "
                        + "background: #ffedd5 !important; color: #c2410c !important; border: 1px solid #fed7aa !important;");
                cellTask.appendChild(lblTask);

                // 6. Reason / Remarks
                Listcell cellRemarks = new Listcell();
                if (dto.getSendBackReason() != null) {
                    Label lblReason = new Label(dto.getSendBackReason() + (dto.getRemarks() != null ? " (" + dto.getRemarks() + ")" : ""));
                    lblReason.setSclass("list-remarks");
                    cellRemarks.appendChild(lblReason);
                } else {
                    Label lblAuto = new Label("Rolled over awaiting Checker verification");
                    lblAuto.setStyle("font-size: 11px; color: #718096; font-style: italic;");
                    cellRemarks.appendChild(lblAuto);
                }

                // 7. Action Button
             // 7. Action Button (Plain text 'Queue', Dark Navy, No Icons/Symbols)
                Listcell cellAction = new Listcell();
                cellAction.setStyle("text-align: center; vertical-align: middle; padding: 0 8px;");

                Button btnAction = new Button("Queue");
                btnAction.setImage(null);
                btnAction.setIconSclass(null);
                btnAction.setStyle("background: #173B61 !important; background-color: #173B61 !important; "
                        + "color: #ffffff !important; border: 1px solid #173B61 !important; "
                        + "font-size: 11px !important; font-weight: 600 !important; "
                        + "padding: 6px 18px !important; border-radius: 4px !important; "
                        + "cursor: pointer !important; white-space: nowrap !important; "
                        + "box-shadow: 0 1px 2px rgba(0,0,0,0.1) !important;");

                btnAction.addEventListener("onClick", event -> routeToCheckerVerification(dto));
                cellAction.appendChild(btnAction);

                item.appendChild(cellBatch);
                item.appendChild(cellChq);
                item.appendChild(cellSort);
                item.appendChild(cellAmt);
                item.appendChild(cellTask);
                item.appendChild(cellRemarks);
                item.appendChild(cellAction);
            }
        });
    }

    public void loadCheckerUnprocessedCheques() {
        this.masterList = checkerDAO.getCheckerUnprocessedCheques();
        updateCounters();
        applyFilters();
    }

    private void updateCounters() {
        long count = masterList.size();
        double sum = masterList.stream()
                               .filter(c -> c.getAmount() != null)
                               .mapToDouble(c -> c.getAmount().doubleValue())
                               .sum();

        if (lblTotalVerifyCount != null) lblTotalVerifyCount.setValue(String.valueOf(count));
        if (lblTotalVerifyAmount != null) lblTotalVerifyAmount.setValue("₹ " + df.format(sum));
    }

    /* =========================================================
       SEARCH & RESET EVENT HANDLERS
       ========================================================= */

    // 1. Triggered on Search Button Click (ZK ID auto-wiring)
    public void onClick$btnSearch(Event event) {
        applyFilters();
    }

    // 2. Triggered on pressing ENTER in the search box
    public void onOK$txtSearchBatch(Event event) {
        applyFilters();
    }

    // 3. Triggered on Reset Button Click (ZK ID auto-wiring)
    public void onClick$btnReset(Event event) {
        onResetFilter();
    }

    // 4. Triggered on Refresh Verification Queue Button Click
    public void onClick$btnRefreshQueue(Event event) {
        if (txtSearchBatch != null) {
            txtSearchBatch.setValue("");
        }
        loadCheckerUnprocessedCheques();
    }

    // Explicit helper methods (also support forward="..." or EL invocations)
    public void onFilterChanged() {
        applyFilters();
    }

    public void onResetFilter() {
        if (txtSearchBatch != null) {
            txtSearchBatch.setValue("");
        }
        applyFilters();
    }

    public void onSearchClick(Event event) {
        applyFilters();
    }

    public void onResetClick(Event event) {
        onResetFilter();
    }

    private void applyFilters() {
        String searchKeyword = (txtSearchBatch != null && txtSearchBatch.getValue() != null)
                ? txtSearchBatch.getValue().trim().toLowerCase()
                : "";

        List<UnprocessedChequeDTO> filtered = masterList.stream().filter(item -> {
            if (searchKeyword.isEmpty()) return true;
            boolean bMatch = item.getBatchNo() != null && item.getBatchNo().toLowerCase().contains(searchKeyword);
            boolean cMatch = item.getChequeNo() != null && item.getChequeNo().toLowerCase().contains(searchKeyword);
            boolean micrMatch = item.getSortCode() != null && item.getSortCode().toLowerCase().contains(searchKeyword);
            return bMatch || cMatch || micrMatch;
        }).collect(Collectors.toList());

        lstCheckerUnprocessed.setModel(new ListModelList<>(filtered));
    }

    private void routeToCheckerVerification(UnprocessedChequeDTO dto) {
        if (dto == null) {
            return;
        }

        String batchIdStr = (dto.getBatchId() != null && dto.getBatchId() > 0)
                ? "BAT" + dto.getBatchId()
                : dto.getBatchNo();

        // 1. Set the batch ID required by OutwardCheckerQueueController
        Sessions.getCurrent().setAttribute("SELECTED_OUTWARD_BATCH_ID", batchIdStr);

        // 2. Set the target cheque identifiers
        Sessions.getCurrent().setAttribute("SELECTED_VERIFY_CHEQUE_NO", dto.getChequeNo());
        Sessions.getCurrent().setAttribute("SELECTED_VERIFY_CHEQUE_ID", "CH" + dto.getChequeId());

        // 3. Redirect to the Checker Queue view
        Executions.sendRedirect("/outward/checker/checker-queue.zul");
    }
}