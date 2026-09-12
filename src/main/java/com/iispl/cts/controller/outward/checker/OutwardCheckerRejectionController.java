package com.iispl.cts.controller.outward.checker;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.iispl.cts.entity.outward.OutwardRejectedCheques;
import com.iispl.cts.service.outward.OutwardCheckerRejectionService;
import com.iispl.cts.serviceimpl.outward.OutwardCheckerRejectionServiceImpl;

public class OutwardCheckerRejectionController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // =========================================================
    // COMPONENTS
    // =========================================================
    private Listbox lstRejectedCheques;
    private Textbox txtSearch;
    private Datebox dateRejected;

    // Pagination Controls (« ‹ [1] / 1 › »)
    private Button btnFirst;
    private Button btnPrevious;
    private Intbox ibCurrentPage;
    private Label lblTotalPages;
    private Button btnNextPage;
    private Button btnLastPage;

    private Label lblTotalRejected;
    private Component windowHost;

    // =========================================================
    // SERVICE & FORMATTERS
    // =========================================================
    private final OutwardCheckerRejectionService rejectionService = new OutwardCheckerRejectionServiceImpl();
    private final DecimalFormat df = new DecimalFormat("##,##,##0.00");
    private final SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");

    // =========================================================
    // PAGINATION VARIABLES
    // =========================================================
    private static final int PAGE_SIZE = 10;
    private int currentPage = 1;
    private int totalRecords = 0;
    private int totalPages = 1;

    // =========================================================
    // INITIAL LOAD
    // =========================================================
    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        loadRejectedCheques();
    }

    // =========================================================
    // LOAD REJECTED CHEQUES
    // =========================================================
    private void loadRejectedCheques() {
        try {
            String searchValue = getSearchValue();
            java.sql.Date rejectedDate = getRejectedDate();

            totalRecords = rejectionService.getTotalRejectedCheques(searchValue, rejectedDate);
            calculateTotalPages();

            if (currentPage > totalPages) {
                currentPage = totalPages;
            }
            if (currentPage < 1) {
                currentPage = 1;
            }

            int offset = (currentPage - 1) * PAGE_SIZE;

            List<OutwardRejectedCheques> rejectedCheques = rejectionService.searchRejectedCheques(searchValue,
                    rejectedDate, PAGE_SIZE, offset);

            displayRejectedCheques(rejectedCheques);
            updatePagination();

            if (lblTotalRejected != null) {
                lblTotalRejected.setValue(String.valueOf(totalRecords));
            }

        } catch (Exception e) {
            e.printStackTrace();
            Messagebox.show("Unable to load rejected cheques.\n" + e.getMessage(), "Error", Messagebox.OK,
                    Messagebox.ERROR);
        }
    }

    // =========================================================
    // DISPLAY REJECTED CHEQUES (Strict Column Alignment)
    // =========================================================
    private void displayRejectedCheques(List<OutwardRejectedCheques> rejectedCheques) {
        ListModelList<OutwardRejectedCheques> model = new ListModelList<>(rejectedCheques);
        lstRejectedCheques.setModel(model);

        lstRejectedCheques.setItemRenderer(new ListitemRenderer<OutwardRejectedCheques>() {
            @Override
            public void render(Listitem item, OutwardRejectedCheques cheque, int index) throws Exception {
                item.setValue(cheque);

                // 1. Batch ID (Left)
                Listcell cellBatch = new Listcell();
                cellBatch.setStyle("text-align: left; vertical-align: middle; padding-left: 14px;");
                Label lblBatch = new Label(safe(cheque.getOutwardBatchId()));
                lblBatch.setStyle("font-size: 13px; font-weight: 600; color: #1e293b; display: block;");
                cellBatch.appendChild(lblBatch);

                // 2. Cheque Number (Center, Monospace)
                Listcell cellCheque = new Listcell();
                cellCheque.setStyle("text-align: center; vertical-align: middle;");
                Label lblChq = new Label(safe(cheque.getOutwardChequeId()));
                lblChq.setStyle("font-family: monospace; font-size: 13px; font-weight: 600; color: #334155; display: block; text-align: center;");
                cellCheque.appendChild(lblChq);

                // 3. Amount (Center, Currency)
                Listcell cellAmt = new Listcell();
                cellAmt.setStyle("text-align: center; vertical-align: middle;");
                BigDecimal amount = cheque.getChequeAmount();
                String amountText = (amount != null) ? "₹ " + df.format(amount) : "₹ 0.00";
                Label lblAmt = new Label(amountText);
                lblAmt.setStyle("font-size: 13px; font-weight: 700; color: #0f172a; display: block; text-align: center;");
                cellAmt.appendChild(lblAmt);

                // 4. Rejection Reason (Left, Italicized Warning)
                Listcell cellReason = new Listcell();
                cellReason.setStyle("text-align: left; vertical-align: middle; padding-left: 14px;");
                Label lblReason = new Label(safe(cheque.getRemarks()));
                lblReason.setStyle("font-size: 12px; font-style: italic; color: #b91c1c; display: block; word-break: break-word;");
                cellReason.appendChild(lblReason);

                // 5. Rejected On (Center)
                Listcell cellDate = new Listcell();
                cellDate.setStyle("text-align: center; vertical-align: middle;");
                String rejectedDate = (cheque.getRejectedDate() != null) ? sdf.format(cheque.getRejectedDate()) : "------";
                Label lblDate = new Label(rejectedDate);
                lblDate.setStyle("font-size: 12px; color: #64748b; display: block; text-align: center;");
                cellDate.appendChild(lblDate);

                // 6. Action Button ("VIEW" - Solid Dark Navy)
                Listcell cellAction = new Listcell();
                cellAction.setStyle("text-align: center; vertical-align: middle; padding: 4px;");
                Button viewButton = new Button("VIEW");
                viewButton.setStyle("background: #173B61 !important; color: #ffffff !important; border: 1px solid #173B61 !important; "
                        + "font-size: 11px !important; font-weight: 600 !important; padding: 4px 14px !important; "
                        + "border-radius: 4px !important; cursor: pointer !important; white-space: nowrap; box-shadow: 0 1px 2px rgba(0,0,0,0.08);");
                viewButton.setTooltiptext("View rejected cheque details");
                viewButton.addEventListener("onClick", event -> showRejectedCheque(cheque));
                cellAction.appendChild(viewButton);

                // Append cells in exact header order
                item.appendChild(cellBatch);
                item.appendChild(cellCheque);
                item.appendChild(cellAmt);
                item.appendChild(cellReason);
                item.appendChild(cellDate);
                item.appendChild(cellAction);
            }
        });
    }

    // =========================================================
    // SEARCH & FILTER EVENTS
    // =========================================================
    public void onClick$btnSearch(Event event) {
        currentPage = 1;
        loadRejectedCheques();
    }

    public void onOK$txtSearch(Event event) {
        onClick$btnSearch(event);
    }

    public void onClick$btnClear(Event event) {
        if (txtSearch != null) txtSearch.setValue("");
        if (dateRejected != null) dateRejected.setValue(null);
        currentPage = 1;
        loadRejectedCheques();
    }

    // =========================================================
    // PAGINATION EVENTS (« ‹ [1] / 1 › »)
    // =========================================================
    public void onClick$btnFirst(Event event) {
        if (currentPage > 1) {
            currentPage = 1;
            loadRejectedCheques();
        }
    }

    public void onClick$btnPrevious(Event event) {
        if (currentPage > 1) {
            currentPage--;
            loadRejectedCheques();
        }
    }

    public void onClick$btnNext(Event event) {
        if (currentPage < totalPages) {
            currentPage++;
            loadRejectedCheques();
        }
    }

    public void onClick$btnLast(Event event) {
        if (currentPage < totalPages) {
            currentPage = totalPages;
            loadRejectedCheques();
        }
    }

    public void onChange$ibCurrentPage(Event event) {
        if (ibCurrentPage != null && ibCurrentPage.getValue() != null) {
            int target = ibCurrentPage.getValue();
            if (target < 1) target = 1;
            else if (target > totalPages) target = totalPages;
            this.currentPage = target;
            loadRejectedCheques();
        }
    }

    public void onOK$ibCurrentPage(Event event) {
        onChange$ibCurrentPage(event);
    }

    private void calculateTotalPages() {
        if (totalRecords <= 0) {
            totalPages = 1;
        } else {
            totalPages = (int) Math.ceil((double) totalRecords / PAGE_SIZE);
        }
    }

    private void updatePagination() {
        if (ibCurrentPage != null) {
            ibCurrentPage.setValue(currentPage);
        }
        if (lblTotalPages != null) {
            lblTotalPages.setValue("/ " + totalPages);
        }

        boolean isFirst = (currentPage <= 1);
        boolean isLast = (currentPage >= totalPages);

        if (btnFirst != null) btnFirst.setDisabled(isFirst);
        if (btnPrevious != null) btnPrevious.setDisabled(isFirst);
        if (btnNextPage != null) btnNextPage.setDisabled(isLast);
        if (btnLastPage != null) btnLastPage.setDisabled(isLast);
    }

    // =========================================================
    // PARAMETER RETRIEVAL
    // =========================================================
    private String getSearchValue() {
        if (txtSearch == null) return null;
        String value = txtSearch.getValue();
        return (value == null || value.trim().isEmpty()) ? null : value.trim();
    }

    private java.sql.Date getRejectedDate() {
        if (dateRejected == null || dateRejected.getValue() == null) return null;
        return new java.sql.Date(dateRejected.getValue().getTime());
    }

    // =========================================================
    // SHOW DETAILS MODAL
    // =========================================================
    private void showRejectedCheque(OutwardRejectedCheques cheque) {
        try {
            Window window = (Window) Executions.createComponents("/outward/checker/rejected-cheque-view.zul",
                    windowHost, null);

            Label lblBatchId = (Label) window.getFellowIfAny("lblBatchId");
            if (lblBatchId != null) lblBatchId.setValue(safe(cheque.getOutwardBatchId()));

            Label lblChequeNumber = (Label) window.getFellowIfAny("lblChequeNumber");
            if (lblChequeNumber != null) lblChequeNumber.setValue(safe(cheque.getOutwardChequeId()));

            Label lblAmount = (Label) window.getFellowIfAny("lblAmount");
            if (lblAmount != null) {
                String amount = (cheque.getChequeAmount() != null) ? "₹ " + df.format(cheque.getChequeAmount()) : "-";
                lblAmount.setValue(amount);
            }

            Label lblRejectedBy = (Label) window.getFellowIfAny("lblRejectedBy");
            if (lblRejectedBy != null) lblRejectedBy.setValue(safe(cheque.getRejectedBy()));

            Label lblRejectedDate = (Label) window.getFellowIfAny("lblRejectedDate");
            if (lblRejectedDate != null) {
                String rejectedDate = (cheque.getRejectedDate() != null) ? sdf.format(cheque.getRejectedDate()) : "-";
                lblRejectedDate.setValue(rejectedDate);
            }

            Label lblReason = (Label) window.getFellowIfAny("lblReason");
            if (lblReason != null) lblReason.setValue(safe(cheque.getRemarks()));

            window.doModal();

        } catch (Exception e) {
            e.printStackTrace();
            Messagebox.show("Unable to open rejected cheque details.\n" + e.getMessage(), "Error", Messagebox.OK,
                    Messagebox.ERROR);
        }
    }

    private String safe(String value) {
        return (value != null && !value.trim().isEmpty()) ? value : "-";
    }
}