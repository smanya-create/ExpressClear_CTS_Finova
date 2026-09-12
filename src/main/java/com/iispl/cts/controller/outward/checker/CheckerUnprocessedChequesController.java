package com.iispl.cts.controller.outward.checker;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Intbox;
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

    // Summary Metric Labels
    private Label lblTotalVerifyCount;
    private Label lblTotalVerifyAmount;

    // Search and Table Components
    private Textbox txtSearchBatch;
    private Button btnSearch;
    private Button btnReset;
    private Button btnRefreshQueue;
    private Listbox lstCheckerUnprocessed;

    // Centered Pagination Controls
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

    // Pure ZK session closed state indicator
    private boolean isSessionClosed = false;

    private final CheckerUnprocessedChequeDAO checkerDAO = new CheckerUnprocessedChequeDAOImpl();
    private List<UnprocessedChequeDTO> masterList = new ArrayList<>();
    private final DecimalFormat df = new DecimalFormat("##,##,##0.00");

    @Override
    public void doAfterCompose(Component comp) throws Exception {
    	super.doAfterCompose(comp);

        // Print all active session keys to Eclipse console
        System.out.println("========== ALL ACTIVE SESSION ATTRIBUTES ==========");
        for (String attrName : Sessions.getCurrent().getAttributes().keySet()) {
            System.out.println(attrName + " = " + Sessions.getCurrent().getAttribute(attrName));
        }
        System.out.println("===================================================");

        resolveSessionStatus();
        initListboxRenderer();
        loadCheckerUnprocessedCheques();
    }

    /**
     * Inspects existing session variables created during login or header loading.
     */
    private void resolveSessionStatus() {
        Object sessionOpenAttr = Sessions.getCurrent().getAttribute("CTS_SESSION_OPEN");
        
        if (sessionOpenAttr != null) {
            // If CTS_SESSION_OPEN is false or "false", the session is closed
            if (sessionOpenAttr instanceof Boolean) {
                this.isSessionClosed = !((Boolean) sessionOpenAttr);
            } else {
                this.isSessionClosed = "false".equalsIgnoreCase(sessionOpenAttr.toString().trim());
            }
        } else {
            // Default to closed if not found
            this.isSessionClosed = true;
        }

        System.out.println("[CTS SECURITY] CTS_SESSION_OPEN: " + sessionOpenAttr + " -> isSessionClosed: " + this.isSessionClosed);
    }

    private void initListboxRenderer() {
        lstCheckerUnprocessed.setItemRenderer(new ListitemRenderer<UnprocessedChequeDTO>() {
            @Override
            public void render(Listitem item, UnprocessedChequeDTO dto, int index) {
                item.setValue(dto);

                // 1. Batch ID & Session Info (Left-aligned)
                Listcell cellBatch = new Listcell();
                cellBatch.setStyle("text-align: left; vertical-align: middle; padding-left: 14px;");
                Vlayout vBatch = new Vlayout();
                vBatch.setSpacing("2px");
                Label lblBNo = new Label(dto.getBatchNo());
                lblBNo.setStyle("font-size: 13px; font-weight: 600; color: #1e293b; display: block;");
                Label lblSName = new Label("Rollover: " + (dto.getOriginalSessionName() != null ? dto.getOriginalSessionName() : "Prior EOD"));
                lblSName.setStyle("font-size: 11px; color: #64748b; display: block;");
                vBatch.appendChild(lblBNo);
                vBatch.appendChild(lblSName);
                cellBatch.appendChild(vBatch);

                // 2. Cheque No (Center)
                Listcell cellChq = new Listcell();
                cellChq.setStyle("text-align: center; vertical-align: middle;");
                Label lblChq = new Label(dto.getChequeNo() != null ? dto.getChequeNo() : "------");
                lblChq.setStyle("font-family: monospace; font-size: 13px; font-weight: 600; color: #334155; display: block; text-align: center;");
                cellChq.appendChild(lblChq);

                // 3. MICR Code (Center)
             // 3. MICR Code (Center)
                Listcell cellMicr = new Listcell();
                cellMicr.setStyle("text-align: center; vertical-align: middle;");
                Label lblSort = new Label(dto.getSortCode() != null ? dto.getSortCode() : "------");
                lblSort.setStyle("font-family: monospace; font-size: 13px; font-weight: 500; color: #334155; display: block; text-align: center;");
                cellMicr.appendChild(lblSort);

                // 4. Amount (Center)
                Listcell cellAmt = new Listcell();
                cellAmt.setStyle("text-align: center; vertical-align: middle;");
                Label lblAmt = new Label(dto.getAmount() != null ? "₹ " + df.format(dto.getAmount()) : "₹ 0.00");
                lblAmt.setStyle("font-size: 13px; font-weight: 700; color: #0f172a; display: block; text-align: center;");
                cellAmt.appendChild(lblAmt);

             // 5. Exact Cheque Status from DB
                Listcell cellTask = new Listcell();
                cellTask.setStyle("text-align: center; vertical-align: middle;");

                String rawDbStatus = (dto.getStatus() != null && !dto.getStatus().trim().isEmpty()) 
                        ? dto.getStatus().trim() 
                        : "------";

                Label lblTask = new Label(rawDbStatus);
                lblTask.setStyle("display: table; margin: 0 auto; padding: 4px 12px; border-radius: 12px; "
                        + "font-size: 11px; font-weight: 700; white-space: nowrap; "
                        + "background: #ffedd5 !important; color: #c2410c !important; border: 1px solid #fed7aa !important;");
                cellTask.appendChild(lblTask);

                // 6. Reason / Remarks (Left-aligned)
                Listcell cellRemarks = new Listcell();
                cellRemarks.setStyle("text-align: left; vertical-align: middle; padding-left: 14px;");
                String reason = (dto.getSendBackReason() != null)
                        ? dto.getSendBackReason() + (dto.getRemarks() != null ? " (" + dto.getRemarks() + ")" : "")
                        : "Rolled over awaiting Checker verification";
                Label lblReason = new Label(reason);
                lblReason.setStyle("font-size: 12px; font-style: italic; color: #c2410c; display: block; word-break: break-word;");
                cellRemarks.appendChild(lblReason);

                // 7. Action Button ('Queue', Dark Navy, Disabled when Session is CLOSED)
                Listcell cellAction = new Listcell();
                cellAction.setStyle("text-align: center; vertical-align: middle; padding: 0 8px;");

                Button btnAction = new Button("Queue");
                btnAction.setImage(null);
                btnAction.setIconSclass(null);

                if (isSessionClosed) {
                    btnAction.setDisabled(true);
                    btnAction.setTooltiptext("Outward Clearing session is closed by Admin. Processing is disabled.");
                    btnAction.setStyle("background: #cbd5e1 !important; color: #94a3b8 !important; "
                            + "border: 1px solid #cbd5e1 !important; font-size: 11px !important; "
                            + "font-weight: 600 !important; padding: 6px 18px !important; border-radius: 4px !important; "
                            + "cursor: not-allowed !important; white-space: nowrap !important; box-shadow: none !important;");
                } else {
                    btnAction.setDisabled(false);
                    btnAction.setStyle("background: #173B61 !important; color: #ffffff !important; "
                            + "border: 1px solid #173B61 !important; font-size: 11px !important; "
                            + "font-weight: 600 !important; padding: 6px 18px !important; border-radius: 4px !important; "
                            + "cursor: pointer !important; white-space: nowrap !important; box-shadow: 0 1px 2px rgba(0,0,0,0.1) !important;");
                    btnAction.addEventListener("onClick", event -> routeToCheckerVerification(dto));
                }

                cellAction.appendChild(btnAction);

                // Append cells
                item.appendChild(cellBatch);
                item.appendChild(cellChq);
                item.appendChild(cellMicr);
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
       FILTERING & PAGINATION SLICING
       ========================================================= */

    public void onClick$btnSearch(Event event) {
        applyFilters();
    }

    public void onOK$txtSearchBatch(Event event) {
        applyFilters();
    }

    public void onClick$btnReset(Event event) {
        if (txtSearchBatch != null) txtSearchBatch.setValue("");
        applyFilters();
    }

    public void onClick$btnRefreshQueue(Event event) {
        if (txtSearchBatch != null) txtSearchBatch.setValue("");
        resolveSessionStatus();
        loadCheckerUnprocessedCheques();
        Clients.showNotification("Verification queue refreshed", "info", null, "top_center", 1500);
    }

    private void applyFilters() {
        String keyword = (txtSearchBatch != null && txtSearchBatch.getValue() != null)
                ? txtSearchBatch.getValue().trim().toLowerCase()
                : "";

        this.currentFilteredList = masterList.stream().filter(item -> {
            if (keyword.isEmpty()) return true;
            boolean bMatch = item.getBatchNo() != null && item.getBatchNo().toLowerCase().contains(keyword);
            boolean cMatch = item.getChequeNo() != null && item.getChequeNo().toLowerCase().contains(keyword);
            boolean micrMatch = item.getSortCode() != null && item.getSortCode().toLowerCase().contains(keyword);
            return bMatch || cMatch || micrMatch;
        }).collect(Collectors.toList());

        this.totalPages = (int) Math.ceil((double) currentFilteredList.size() / PAGE_SIZE);
        if (this.totalPages < 1) this.totalPages = 1;

        loadPage(0);
    }

    private void loadPage(int pageIndex) {
        if (pageIndex >= this.totalPages) pageIndex = this.totalPages - 1;
        if (pageIndex < 0) pageIndex = 0;
        this.activePageIndex = pageIndex;

        if (ibCurrentPage != null) ibCurrentPage.setValue(this.activePageIndex + 1);
        if (lblTotalPages != null) lblTotalPages.setValue("/ " + this.totalPages);

        boolean isFirst = (this.activePageIndex <= 0);
        boolean isLast = (this.activePageIndex >= this.totalPages - 1);

        if (btnFirstPage != null) btnFirstPage.setDisabled(isFirst);
        if (btnPrevPage != null) btnPrevPage.setDisabled(isFirst);
        if (btnNextPage != null) btnNextPage.setDisabled(isLast);
        if (btnLastPage != null) btnLastPage.setDisabled(isLast);

        int fromIndex = this.activePageIndex * PAGE_SIZE;
        int toIndex = Math.min(fromIndex + PAGE_SIZE, currentFilteredList.size());

        List<UnprocessedChequeDTO> pageSubList = (fromIndex < currentFilteredList.size())
                ? currentFilteredList.subList(fromIndex, toIndex)
                : new ArrayList<>();

        lstCheckerUnprocessed.setModel(new ListModelList<>(pageSubList));
    }

    /* =========================================================
       PAGINATION BUTTON LISTENERS
       ========================================================= */

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

    /* =========================================================
       ROUTING
       ========================================================= */

    private void routeToCheckerVerification(UnprocessedChequeDTO dto) {
        if (dto == null) return;

        // Server-side lock check
        if (isSessionClosed) {
            Clients.showNotification("Session is CLOSED by Admin. Actions are locked.", "error", null, "top_center", 2500);
            return;
        }

        String batchIdStr = (dto.getBatchId() != null && dto.getBatchId() > 0)
                ? "BAT" + dto.getBatchId()
                : dto.getBatchNo();

        Sessions.getCurrent().setAttribute("SELECTED_OUTWARD_BATCH_ID", batchIdStr);
        Sessions.getCurrent().setAttribute("SELECTED_VERIFY_CHEQUE_NO", dto.getChequeNo());
        Sessions.getCurrent().setAttribute("SELECTED_VERIFY_CHEQUE_ID", "CH" + dto.getChequeId());

        Executions.sendRedirect("/outward/checker/checker-queue.zul");
    }
}