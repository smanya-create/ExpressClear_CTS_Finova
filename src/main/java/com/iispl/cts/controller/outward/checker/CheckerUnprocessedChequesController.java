//package com.iispl.cts.controller.outward.checker;
//
//import java.math.BigDecimal;
//import java.sql.Connection;
//import java.sql.PreparedStatement;
//import java.sql.SQLException;
//import java.text.DecimalFormat;
//import java.util.ArrayList;
//import java.util.List;
//
//import org.zkoss.zk.ui.Component;
//import org.zkoss.zk.ui.Executions;
//import org.zkoss.zk.ui.Sessions;
//import org.zkoss.zk.ui.event.Event;
//import org.zkoss.zk.ui.util.Clients;
//import org.zkoss.zk.ui.util.GenericForwardComposer;
//import org.zkoss.zul.Button;
//import org.zkoss.zul.Intbox;
//import org.zkoss.zul.Label;
//import org.zkoss.zul.ListModelList;
//import org.zkoss.zul.Listbox;
//import org.zkoss.zul.Listcell;
//import org.zkoss.zul.Listitem;
//import org.zkoss.zul.ListitemRenderer;
//import org.zkoss.zul.Textbox;
//import org.zkoss.zul.Vlayout;
//
//import com.iispl.cts.common.config.DBConnection;
//import com.iispl.cts.dao.outward.CheckerUnprocessedChequeDAO;
//import com.iispl.cts.daoimpl.outward.CheckerUnprocessedChequeDAOImpl;
//import com.iispl.cts.dto.UnprocessedChequeDTO;
//
//public class CheckerUnprocessedChequesController extends GenericForwardComposer<Component> {
//
//    private static final long serialVersionUID = 1L;
//
//    // Summary Metric Labels
//    private Label lblTotalVerifyCount;
//    private Label lblTotalVerifyAmount;
//
//    // Search and Table Components
//    private Textbox txtSearchBatch;
//    private Button btnSearch;
//    private Button btnReset;
//    private Button btnRefreshQueue;
//    private Listbox lstCheckerUnprocessed;
//
//    // Centered Pagination Controls
//    private Button btnFirstPage;
//    private Button btnPrevPage;
//    private Intbox ibCurrentPage;
//    private Label lblTotalPages;
//    private Button btnNextPage;
//    private Button btnLastPage;
//
//    private static final int PAGE_SIZE = 12;
//    private int activePageIndex = 0;
//    private int totalPages = 1;
//    private List<UnprocessedChequeDTO> currentFilteredList = new ArrayList<>();
//
//    // Pure ZK session closed state indicator
//    private boolean isSessionClosed = false;
//
//    private final CheckerUnprocessedChequeDAO checkerDAO = new CheckerUnprocessedChequeDAOImpl();
//    private List<UnprocessedChequeDTO> masterList = new ArrayList<>();
//    private final DecimalFormat df = new DecimalFormat("##,##,##0.00");
//
//    @Override
//    public void doAfterCompose(Component comp) throws Exception {
//        super.doAfterCompose(comp);
//        checkSessionState();
//        resolveSessionStatus();
//        initListboxRenderer();
//        loadCheckerUnprocessedCheques();
//    }
//
//    private void checkSessionState() {
//		// TODO Auto-generated method stub
//    	// Safe check for closed session
//        try (java.sql.Connection conn = com.iispl.cts.common.config.DBConnection.getConnection();
//             java.sql.PreparedStatement ps = conn.prepareStatement(
//                     "SELECT session_status FROM clearing_session ORDER BY clearing_date DESC LIMIT 1");
//             java.sql.ResultSet rs = ps.executeQuery()) {
//            if (rs.next()) {
//                this.isSessionClosed = "CLOSED".equalsIgnoreCase(rs.getString("session_status"));
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//            this.isSessionClosed = false;
//        }
//		
//	}
//
//	/**
//     * Inspects global desktop context and session attributes to determine outward session status.
//     */
//    private void resolveSessionStatus() {
//        // 1. Check Global Application Scope
//        Object globalSessionOpen = (getPage() != null && getPage().getDesktop() != null && getPage().getDesktop().getWebApp() != null)
//                ? getPage().getDesktop().getWebApp().getAttribute("GLOBAL_CTS_SESSION_OPEN")
//                : null;
//
//        if (globalSessionOpen instanceof Boolean) {
//            this.isSessionClosed = !((Boolean) globalSessionOpen);
//            return;
//        }
//
//        // 2. Check HTTP Session Attribute
//        Object sessionOpenAttr = Sessions.getCurrent() != null 
//                ? Sessions.getCurrent().getAttribute("CTS_SESSION_OPEN") 
//                : null;
//
//        if (sessionOpenAttr != null) {
//            if (sessionOpenAttr instanceof Boolean) {
//                this.isSessionClosed = !((Boolean) sessionOpenAttr);
//            } else {
//                this.isSessionClosed = "false".equalsIgnoreCase(sessionOpenAttr.toString().trim());
//            }
//        } else {
//            this.isSessionClosed = true;
//        }
//    }
//
//    private void initListboxRenderer() {
//    	if (lstCheckerUnprocessed == null) {
//            System.err.println("[CTS ERROR] lstCheckerUnprocessed is NULL. Check ZUL wiring.");
//            return;
//        }
//
//        lstCheckerUnprocessed.setItemRenderer(new ListitemRenderer<UnprocessedChequeDTO>() {
//            @Override
//            public void render(Listitem item, UnprocessedChequeDTO dto, int index) {
//                if (dto == null) return;
//
//                item.setValue(dto);
//
//                // 1. Batch ID & Session Info
//                Listcell cellBatch = new Listcell();
//                cellBatch.setStyle("text-align: center; vertical-align: middle;");
//
//                String fullBatchNo = dto.getBatchNo() != null ? dto.getBatchNo().trim() : "";
//                String displayBatchId = fullBatchNo;
//
//                if (displayBatchId.matches(".*BAT\\d+.*")) {
//                    java.util.regex.Matcher m = java.util.regex.Pattern.compile("(BAT\\d+)").matcher(displayBatchId);
//                    if (m.find()) {
//                        displayBatchId = m.group(1);
//                    }
//                } else if (dto.getBatchId() != null && dto.getBatchId() > 0) {
//                    displayBatchId = "BAT" + dto.getBatchId();
//                } else if (fullBatchNo.contains("-")) {
//                    displayBatchId = fullBatchNo.substring(fullBatchNo.lastIndexOf('-') + 1);
//                }
//
//                Label lblBNo = new Label(displayBatchId);
//                lblBNo.setStyle("font-size: 13px; font-weight: 700; color: #1e293b; text-align: center; display: block;");
//                cellBatch.appendChild(lblBNo);
//
//
//                // 2. Cheque No
//                Listcell cellChq = new Listcell();
//                cellChq.setStyle("text-align: center; vertical-align: middle;");
//                Label lblChq = new Label(dto.getChequeNo() != null ? dto.getChequeNo() : "------");
//                lblChq.setStyle("font-family: monospace; font-size: 13px; font-weight: 600; color: #334155; display: block; text-align: center;");
//                cellChq.appendChild(lblChq);
//
//                // 3. MICR Code
//                Listcell cellMicr = new Listcell();
//                cellMicr.setStyle("text-align: center; vertical-align: middle;");
//                Label lblSort = new Label(dto.getSortCode() != null ? dto.getSortCode() : "------");
//                lblSort.setStyle("font-family: monospace; font-size: 13px; font-weight: 500; color: #334155; display: block; text-align: center;");
//                cellMicr.appendChild(lblSort);
//
//                // 4. Amount
//                Listcell cellAmt = new Listcell();
//                cellAmt.setStyle("text-align: center; vertical-align: middle;");
//                String amtStr = "₹ 0.00";
//                if (dto.getAmount() != null) {
//                    amtStr = "₹ " + df.format(dto.getAmount());
//                }
//                Label lblAmt = new Label(amtStr);
//                lblAmt.setStyle("font-size: 13px; font-weight: 700; color: #0f172a; display: block; text-align: center;");
//                cellAmt.appendChild(lblAmt);
//
//                // 5. Status
//                Listcell cellTask = new Listcell();
//                cellTask.setStyle("text-align: center; vertical-align: middle;");
//                String rawStatus = dto.getStatus() != null ? dto.getStatus() : "UNPROCESSED";
//                Label lblTask = new Label(rawStatus);
//                lblTask.setStyle("display: table; margin: 0 auto; padding: 4px 12px; border-radius: 12px; "
//                        + "font-size: 11px; font-weight: 700; white-space: nowrap; "
//                        + "background: #ffedd5 !important; color: #c2410c !important; border: 1px solid #fed7aa !important;");
//                cellTask.appendChild(lblTask);
//
//                // 6. Reason / Remarks
//                Listcell cellRemarks = new Listcell();
//                cellRemarks.setStyle("text-align: left; vertical-align: middle; padding-left: 14px;");
//                String remarksText = dto.getRemarks() != null ? dto.getRemarks() : "Rolled over awaiting Checker verification";
//                Label lblReason = new Label(remarksText);
//                lblReason.setStyle("font-size: 12px; font-style: italic; color: #c2410c; display: block; word-break: break-word;");
//                cellRemarks.appendChild(lblReason);
//
//                // 7. Action Button
//                Listcell cellAction = new Listcell();
//                cellAction.setStyle("text-align: center; vertical-align: middle; padding: 0 8px;");
//
//                Button btnAction = new Button("Queue");
//                if (isSessionClosed) {
//                    btnAction.setDisabled(true);
//                    btnAction.setStyle("background: #cbd5e1 !important; color: #94a3b8 !important; "
//                            + "border: 1px solid #cbd5e1 !important; font-size: 11px !important; "
//                            + "font-weight: 600 !important; padding: 6px 18px !important; border-radius: 4px !important; "
//                            + "cursor: not-allowed !important;");
//                } else {
//                    btnAction.setDisabled(false);
//                    btnAction.setStyle("background: #173B61 !important; color: #ffffff !important; "
//                            + "border: 1px solid #173B61 !important; font-size: 11px !important; "
//                            + "font-weight: 600 !important; padding: 6px 18px !important; border-radius: 4px !important; "
//                            + "cursor: pointer !important;");
//                    btnAction.addEventListener("onClick", event -> routeToCheckerVerification(dto));
//                }
//                cellAction.appendChild(btnAction);
//
//                // Append all to item
//                item.appendChild(cellBatch);
//                item.appendChild(cellChq);
//                item.appendChild(cellMicr);
//                item.appendChild(cellAmt);
//                item.appendChild(cellTask);
//                item.appendChild(cellRemarks);
//                item.appendChild(cellAction);
//            }
//        });
//    }
//
//    public void loadCheckerUnprocessedCheques() {
//    	this.masterList = checkerDAO.getCheckerUnprocessedCheques();
//        if (this.masterList == null) {
//            this.masterList = new ArrayList<>();
//        }
//
//        updateCounters();
//        applyFilters();
//    }
//
//    private void updateCounters() {
//    	int count = this.masterList.size();
//        BigDecimal total = BigDecimal.ZERO;
//        for (UnprocessedChequeDTO dto : this.masterList) {
//            if (dto.getAmount() != null) {
//                total = total.add(dto.getAmount());
//            }
//        }
//
//        if (lblTotalVerifyCount != null) {
//            lblTotalVerifyCount.setValue(String.valueOf(count));
//        }
//        if (lblTotalVerifyAmount != null) {
//            lblTotalVerifyAmount.setValue("₹ " + df.format(total));
//        }
//    }
//
//    /* =========================================================
//       FILTERING & PAGINATION SLICING
//       ========================================================= */
//
//    public void onClick$btnSearch(Event event) {
//        applyFilters();
//    }
//
//    public void onOK$txtSearchBatch(Event event) {
//        applyFilters();
//    }
//
//    public void onClick$btnReset(Event event) {
//        if (txtSearchBatch != null) txtSearchBatch.setValue("");
//        applyFilters();
//    }
//
//    public void onClick$btnRefreshQueue(Event event) {
//        if (txtSearchBatch != null) txtSearchBatch.setValue("");
//        resolveSessionStatus();
//        loadCheckerUnprocessedCheques();
//        Clients.showNotification("Verification queue refreshed", "info", null, "top_center", 1500);
//    }
//
// 
//
//    // 2. Harden applyFilters() to accept UNPROCESSED_VERIFY and variations
//    public void applyFilters() {
//    	if (this.masterList == null) {
//            this.masterList = new ArrayList<>();
//        }
//
//        List<UnprocessedChequeDTO> filtered = new ArrayList<>();
//        String query = (txtSearchBatch != null && txtSearchBatch.getValue() != null)
//                ? txtSearchBatch.getValue().trim().toLowerCase()
//                : "";
//
//        for (UnprocessedChequeDTO dto : this.masterList) {
//            if (dto == null) continue;
//            if (query.isEmpty()) {
//                filtered.add(dto);
//            } else {
//                String bNo = dto.getBatchNo() != null ? dto.getBatchNo().toLowerCase() : "";
//                String chqNo = dto.getChequeNo() != null ? dto.getChequeNo().toLowerCase() : "";
//                String micr = dto.getSortCode() != null ? dto.getSortCode().toLowerCase() : "";
//                if (bNo.contains(query) || chqNo.contains(query) || micr.contains(query)) {
//                    filtered.add(dto);
//                }
//            }
//        }
//
//        if (lstCheckerUnprocessed != null) {
//            lstCheckerUnprocessed.setModel(new ListModelList<>(filtered));
//        }
//
//        if (lblTotalPages != null) lblTotalPages.setValue("/ 1");
//        if (ibCurrentPage != null) ibCurrentPage.setValue(1);
//    }
//    private void loadPage(int pageIndex) {
//        if (pageIndex >= this.totalPages) pageIndex = this.totalPages - 1;
//        if (pageIndex < 0) pageIndex = 0;
//        this.activePageIndex = pageIndex;
//
//        if (ibCurrentPage != null) ibCurrentPage.setValue(this.activePageIndex + 1);
//        if (lblTotalPages != null) lblTotalPages.setValue("/ " + this.totalPages);
//
//        boolean isFirst = (this.activePageIndex <= 0);
//        boolean isLast = (this.activePageIndex >= this.totalPages - 1);
//
//        if (btnFirstPage != null) btnFirstPage.setDisabled(isFirst);
//        if (btnPrevPage != null) btnPrevPage.setDisabled(isFirst);
//        if (btnNextPage != null) btnNextPage.setDisabled(isLast);
//        if (btnLastPage != null) btnLastPage.setDisabled(isLast);
//
//        int fromIndex = this.activePageIndex * PAGE_SIZE;
//        int toIndex = Math.min(fromIndex + PAGE_SIZE, currentFilteredList.size());
//
//        List<UnprocessedChequeDTO> pageSubList = (fromIndex < currentFilteredList.size())
//                ? currentFilteredList.subList(fromIndex, toIndex)
//                : new ArrayList<>();
//
//        lstCheckerUnprocessed.setModel(new ListModelList<>(pageSubList));
//    }
//
//    /* =========================================================
//       PAGINATION BUTTON LISTENERS
//       ========================================================= */
//
//    public void onClick$btnFirstPage(Event event) {
//        if (activePageIndex > 0) loadPage(0);
//    }
//
//    public void onClick$btnPrevPage(Event event) {
//        if (activePageIndex > 0) loadPage(activePageIndex - 1);
//    }
//
//    public void onClick$btnNextPage(Event event) {
//        if (activePageIndex < totalPages - 1) loadPage(activePageIndex + 1);
//    }
//
//    public void onClick$btnLastPage(Event event) {
//        if (activePageIndex < totalPages - 1) loadPage(totalPages - 1);
//    }
//
//    public void onChange$ibCurrentPage(Event event) {
//        Integer target = ibCurrentPage.getValue();
//        if (target == null || target < 1) target = 1;
//        else if (target > totalPages) target = totalPages;
//        loadPage(target - 1);
//    }
//
//    public void onOK$ibCurrentPage(Event event) {
//        onChange$ibCurrentPage(event);
//    }
//
//    /* =========================================================
//       ROUTING & BATCH REACTIVATION
//       ========================================================= */
//
//    private void routeToCheckerVerification(UnprocessedChequeDTO dto) {
//        if (dto == null) return;
//
//        String batchIdStr = (dto.getBatchId() != null && dto.getBatchId() > 0) 
//                ? "BAT" + dto.getBatchId() 
//                : (dto.getBatchNo() != null ? dto.getBatchNo() : "BAT1002");
//
//        String chqIdStr = (dto.getChequeId() != null && dto.getChequeId() > 0) 
//                ? "CH" + dto.getChequeId() 
//                : null;
//
//        // Reactivate batch and instruments in DB
//        reactivateCheckerChequeAndBatch(chqIdStr, dto.getChequeNo(), batchIdStr);
//
//        // Populate session attributes
//        org.zkoss.zk.ui.Session session = Sessions.getCurrent();
//        session.setAttribute("SELECTED_OUTWARD_BATCH_ID", batchIdStr);
//        session.setAttribute("CURRENT_BATCH_ID", batchIdStr);
//        session.setAttribute("SELECTED_OUTWARD_CHEQUE_ID", chqIdStr);
//        session.setAttribute("SELECTED_CHEQUE_NO", dto.getChequeNo());
//
//        // Redirect to verification queue
//        Executions.sendRedirect("/outward/checker/checker-queue.zul");
//    }
//
// // 1. Reactivate the entire batch and its cheques in the DB
//    private void reactivateCheckerChequeAndBatch(String chqIdStr, String chqNo, String batchIdStr) {
//        if (batchIdStr == null || batchIdStr.trim().isEmpty()) {
//            return;
//        }
//
//        // 1. Move all cheques belonging to this rollover batch into PENDING_VERIFICATION
//        String updateChequesSql = 
//                "UPDATE outward_cheque " +
//                "SET cheque_status = 'PENDING_VERIFICATION' " +
//                "WHERE outward_batch_id = ? " +
//                "  AND UPPER(cheque_status) LIKE 'UNPROCESSED%'";
//
//        // 2. Flip the batch status from UNPROCESSED to PENDING_CHECKER_PROCESS
//        // This makes it instantly discoverable by your teammate's Dashboard query!
//        String updateBatchSql = 
//                "UPDATE outward_batch " +
//                "SET batch_status = 'PENDING_CHECKER_PROCESS' " +
//                "WHERE outward_batch_id = ? " +
//                "  AND UPPER(batch_status) = 'UNPROCESSED'";
//
//        try (java.sql.Connection conn = com.iispl.cts.common.config.DBConnection.getConnection()) {
//            conn.setAutoCommit(false);
//            try (java.sql.PreparedStatement psCheques = conn.prepareStatement(updateChequesSql);
//                 java.sql.PreparedStatement psBatch = conn.prepareStatement(updateBatchSql)) {
//
//                psCheques.setString(1, batchIdStr);
//                int chequesUpdated = psCheques.executeUpdate();
//
//                psBatch.setString(1, batchIdStr);
//                int batchesUpdated = psBatch.executeUpdate();
//
//                conn.commit();
//                System.out.println("[CTS REACTIVATION] Reactivated Batch: " + batchIdStr + 
//                        " | Cheques updated: " + chequesUpdated + " | Batch updated: " + batchesUpdated);
//            } catch (Exception ex) {
//                conn.rollback();
//                ex.printStackTrace();
//            }
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
//	/**
//     * Atomically transitions the outward cheque to PENDING_CHECKER_VERIFICATION
//     * and sets outward_batch back to PENDING_CHECKER_PROCESS.
//     */
//    private void reactivateCheckerChequeAndBatch(Long chequeId, String batchIdStr) {
//        if (chequeId == null) return;
//
//        String cleanBatchId = (batchIdStr != null) ? batchIdStr.toUpperCase().replace("BAT", "").trim() : "";
//
//        String updateChequeSql = "UPDATE outward_cheque SET cheque_status = 'PENDING_CHECKER_VERIFICATION' "
//                + "WHERE outward_cheque_id = ? AND UPPER(cheque_status) LIKE 'UNPROCESSED%'";
//
//        String updateBatchSql = "UPDATE outward_batch SET batch_status = 'PENDING_CHECKER_PROCESS' "
//                + "WHERE (outward_batch_id::text = ? OR batch_reference_id = ?) "
//                + "  AND UPPER(batch_status) = 'UNPROCESSED'";
//
//        try (Connection conn = DBConnection.getConnection()) {
//            conn.setAutoCommit(false);
//
//            try (PreparedStatement psChq = conn.prepareStatement(updateChequeSql)) {
//                psChq.setLong(1, chequeId);
//                psChq.executeUpdate();
//            }
//
//            if (!cleanBatchId.isEmpty()) {
//                try (PreparedStatement psBatch = conn.prepareStatement(updateBatchSql)) {
//                    psBatch.setString(1, cleanBatchId);
//                    psBatch.setString(2, batchIdStr);
//                    psBatch.executeUpdate();
//                }
//            }
//
//            conn.commit();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
//    }
//}