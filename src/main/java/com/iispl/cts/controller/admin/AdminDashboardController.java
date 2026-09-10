package com.iispl.cts.controller.admin;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Column;
import org.zkoss.zul.Columns;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Hbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.common.util.ActiveUserManager;
import com.iispl.cts.common.util.SecurityUtil;
import com.iispl.cts.dto.PendingChequeDTO;
import com.iispl.cts.serviceimpl.AuditServiceImpl;

public class AdminDashboardController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // Header & Metric Labels
    private Label lblAdminName;
    private Label lblHeaderDate;
    private Label lblClearingDate;
    private Label lblSessionStatus;
    private Label lblLoggedInUsers;
    private Label lblPendingCount;
    private Label lblPendingTag;

    // Selection Controls
    private Div cardEOD;
    private Div cardBOD;
    private Radio radEOD;
    private Radio radBOD;
    private Label lblEODWarnText;
    private Button btnStartSession;

    // Warning Panel
    private Div boxWarningBanner;
    private Label lblWarningMsg;
    private Button btnViewPending;

    // State Variables
    private LocalDate currentClearingDate;
    private boolean isSessionOpen;
    private int loggedInUsersCount;
    private int pendingChequesCount;
    private String selectedAction = "EOD";

    private Div dotSessionStatus;
    private Div ringEOD;
    private Div ringBOD;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");
    private final List<PendingChequeDTO> pendingTransactionsList = new ArrayList<>();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
    	if (!SecurityUtil.checkAccess("ADMIN_DASHBOARD")) {
            return; // Stops component initialization and redirects unauthenticated users
        }
    	
        super.doAfterCompose(comp);
        loadSessionData();
        refreshUI();
        refreshActiveUsers();
    }
    

    private void loadSessionData() {
        fetchActiveClearingSession();
        fetchActiveUsersCount();
        loadPendingCheques();
        this.selectedAction = this.isSessionOpen ? "EOD" : "BOD";
    }

    private void fetchActiveClearingSession() {
        String sql = "SELECT clearing_date, session_status FROM clearing_session ORDER BY clearing_date DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                Date dbDate = rs.getDate("clearing_date");
                this.currentClearingDate = dbDate != null ? dbDate.toLocalDate() : LocalDate.now();
                this.isSessionOpen = "OPEN".equalsIgnoreCase(rs.getString("session_status"));
            } else {
                this.currentClearingDate = LocalDate.now();
                this.isSessionOpen = false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            this.currentClearingDate = LocalDate.now();
            this.isSessionOpen = false;
        }
    }

   
    private void fetchActiveUsersCount() {
    	this.loggedInUsersCount = ActiveUserManager.getActiveUserCount();
    }
    public void refreshActiveUsers() {
        this.loggedInUsersCount = ActiveUserManager.getActiveUserCount();
        if (lblLoggedInUsers != null) {
            lblLoggedInUsers.setValue(String.valueOf(this.loggedInUsersCount));
        }
    }
    	
    

    
    	private void loadPendingCheques() {
            pendingTransactionsList.clear();

            // Combined query: checks pending scan items (Maker) and pending outward items (Checker)
            String sql = 
                "SELECT sc.scanned_batch_id AS batch_id, " +
                "       sb.batch_reference_id, " +
                "       sc.cheque_number, " +
                "       sc.cheque_status, " +
                "       sc.cheque_amount, " +
                "       sc.created_at, " +
                "       'SCAN_STAGE' AS pipeline_source " +
                "FROM scan_cheque sc " +
                "LEFT JOIN scan_batch sb ON sc.scanned_batch_id = sb.scanned_batch_id " +
                "WHERE UPPER(sc.cheque_status) IN ('PENDING_DATA_ENTRY', 'PENDING_REPAIR', 'RAW', 'PENDING', 'Pending') " +
                
                "UNION ALL " +
                
                "SELECT oc.outward_batch_id AS batch_id, " +
                "       ob.batch_reference_id, " +
                "       oc.cheque_number, " +
                "       oc.cheque_status, " +
                "       oc.cheque_amount, " +
                "       oc.created_at, " +
                "       'OUTWARD_STAGE' AS pipeline_source " +
                "FROM outward_cheque oc " +
                "LEFT JOIN outward_batch ob ON oc.outward_batch_id = ob.outward_batch_id " +
                "WHERE UPPER(oc.cheque_status) IN ('PENDING_VERIFICATION', 'PENDING_DATA_ENTRY', 'PENDING_REPAIR', 'PENDING', 'Pending') " +
                
                "ORDER BY created_at ASC";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    String batchId = rs.getString("batch_id");
                    String batchRef = rs.getString("batch_reference_id");
                    String displayBatch = (batchRef != null && !batchRef.isEmpty()) ? batchId + " (" + batchRef + ")" : batchId;
                    
                    String chqNo = rs.getString("cheque_number");
                    String status = rs.getString("cheque_status");
                    double amount = rs.getDouble("cheque_amount");
                    String source = rs.getString("pipeline_source");

                    String assignedQueue;
                    if ("PENDING_DATA_ENTRY".equalsIgnoreCase(status)) {
                        assignedQueue = "Outward Maker (Data Entry)";
                    } else if ("PENDING_REPAIR".equalsIgnoreCase(status) || "RAW".equalsIgnoreCase(status)) {
                        assignedQueue = "Outward Maker (MICR Repair)";
                    } else if ("PENDING_VERIFICATION".equalsIgnoreCase(status)) {
                        assignedQueue = "Outward Checker Queue";
                    } else {
                        assignedQueue = "SCAN_STAGE".equals(source) ? "Maker Staging Queue" : "Outward Processing Queue";
                    }

                    pendingTransactionsList.add(new PendingChequeDTO(
                        displayBatch,
                        chqNo != null ? chqNo : "------",
                        "OUTWARD",
                        status,
                        assignedQueue,
                        "Amount: " + String.format("%.2f", amount)
                    ));
                }
            } catch (SQLException e) {
                e.printStackTrace();
                pendingTransactionsList.clear();
            }
            this.pendingChequesCount = pendingTransactionsList.size();
        
    }

    private void refreshUI() {
        String formattedDate = currentClearingDate.format(dateFormatter);
        if (lblHeaderDate != null) lblHeaderDate.setValue(formattedDate);
        if (lblClearingDate != null) lblClearingDate.setValue(formattedDate);
        if (lblLoggedInUsers != null) lblLoggedInUsers.setValue(String.valueOf(loggedInUsersCount));
        if (lblPendingCount != null) lblPendingCount.setValue(String.valueOf(pendingChequesCount));

        refreshStatusBadge();

        if (pendingChequesCount > 0 && isSessionOpen) {
            if (lblPendingTag != null) lblPendingTag.setValue("Requires attention before EOD");
            if (lblEODWarnText != null) lblEODWarnText.setValue(pendingChequesCount + " pending cheques detected");
            if (lblWarningMsg != null) lblWarningMsg.setValue(pendingChequesCount + " cheques are currently pending. Review their processing stage before performing EOD.");
            if (boxWarningBanner != null) boxWarningBanner.setVisible(true);
        } else {
            if (lblPendingTag != null) lblPendingTag.setValue("");
            if (lblEODWarnText != null) lblEODWarnText.setValue("");
            if (boxWarningBanner != null) boxWarningBanner.setVisible(false);
        }

        updateCardStyles();
    }

    private void refreshStatusBadge() {
        if (lblSessionStatus != null) {
            if (isSessionOpen) {
                lblSessionStatus.setValue("OPEN");
                lblSessionStatus.setStyle("font-weight: 700; color: #276749; font-size: 14px;");
            } else {
                lblSessionStatus.setValue("CLOSED");
                lblSessionStatus.setStyle("font-weight: 700; color: #c53030; font-size: 14px;");
            }
        }
        if (dotSessionStatus != null) {
            dotSessionStatus.setSclass(isSessionOpen ? "cts-status-dot-open" : "cts-status-dot-closed");
        }
    }

    private void updateCardStyles() {
        if ("EOD".equals(selectedAction)) {
            if (cardEOD != null) cardEOD.setSclass("cts-choice-box cts-choice-box-active");
            if (cardBOD != null) cardBOD.setSclass("cts-choice-box");
            if (ringEOD != null) ringEOD.setSclass("cts-custom-radio-ring cts-radio-checked");
            if (ringBOD != null) ringBOD.setSclass("cts-custom-radio-ring");
        } else {
            if (cardEOD != null) cardEOD.setSclass("cts-choice-box");
            if (cardBOD != null) cardBOD.setSclass("cts-choice-box cts-choice-box-active");
            if (ringEOD != null) ringEOD.setSclass("cts-custom-radio-ring");
            if (ringBOD != null) ringBOD.setSclass("cts-custom-radio-ring cts-radio-checked");
        }
    }

    public void onClick$btnViewPending(Event event) {
        openPendingTransactionsModal();
    }

    private void openPendingTransactionsModal() {
        final Window win = new Window();
        win.setTitle("Pending Cheques & Batches (" + pendingTransactionsList.size() + " items)");
        win.setWidth("780px");
        win.setBorder("normal");
        win.setClosable(true);

        Vlayout rootLayout = new Vlayout();
        rootLayout.setSpacing("12px");
        rootLayout.setStyle("padding: 16px; background: #ffffff;");

        Label subLabel = new Label("Review items paused in the clearing pipeline before triggering EOD closure.");
        subLabel.setStyle("font-size: 12px; color: #718096; display: block; margin-bottom: 4px;");
        rootLayout.appendChild(subLabel);

        Grid grid = new Grid();
        grid.setStyle("border: 1px solid #e2e8f0; border-radius: 6px;");

        Columns cols = new Columns();
        cols.setSizable(false);

        Column cBatch = new Column("Batch ID / Ref");
        cBatch.setWidth("180px");
        cBatch.setStyle("font-weight: bold; font-size: 11px; background: #f7fafc; color: #4a5568;");

        Column cChq = new Column("Cheque No.");
        cChq.setWidth("100px");
        cChq.setStyle("font-weight: bold; font-size: 11px; background: #f7fafc; color: #4a5568;");

        Column cDir = new Column("Direction");
        cDir.setWidth("80px");
        cDir.setStyle("font-weight: bold; font-size: 11px; background: #f7fafc; color: #4a5568;");

        Column cStage = new Column("Paused Stage");
        cStage.setWidth("160px");
        cStage.setStyle("font-weight: bold; font-size: 11px; background: #f7fafc; color: #4a5568;");

        Column cQueue = new Column("Assigned Queue");
        cQueue.setStyle("font-weight: bold; font-size: 11px; background: #f7fafc; color: #4a5568;");

        cols.appendChild(cBatch);
        cols.appendChild(cChq);
        cols.appendChild(cDir);
        cols.appendChild(cStage);
        cols.appendChild(cQueue);
        grid.appendChild(cols);

        Rows rows = new Rows();
        for (PendingChequeDTO item : pendingTransactionsList) {
            Row row = new Row();
            row.setStyle("font-size: 12px; height: 38px;");

            Label lBatch = new Label(item.getBatchId());
            lBatch.setStyle("font-weight: 600; color: #2d3748;");

            Label lChq = new Label(item.getChequeNo());
            lChq.setStyle("color: #4a5568;");

            Label lDir = new Label(item.getDirection());
            lDir.setStyle("font-size: 10px; font-weight: bold; color: #2b6cb0; background: #ebf8ff; padding: 2px 6px; border-radius: 4px;");

            Label lStage = new Label(item.getPausedStage());
            lStage.setStyle("font-size: 10px; font-weight: bold; color: #c05621; background: #feebc8; padding: 2px 8px; border-radius: 10px;");

            Label lQueue = new Label(item.getAssignedQueue());
            lQueue.setStyle("color: #718096; font-size: 11px;");

            row.appendChild(lBatch);
            row.appendChild(lChq);
            row.appendChild(lDir);
            row.appendChild(lStage);
            row.appendChild(lQueue);

            rows.appendChild(row);
        }

        grid.appendChild(rows);
        rootLayout.appendChild(grid);

        Hbox footerBox = new Hbox();
        footerBox.setWidth("100%");
        footerBox.setPack("end");
        footerBox.setStyle("margin-top: 10px;");

        Button btnClose = new Button("Close");
        btnClose.setStyle("background: #edf2f7; color: #2d3748; font-weight: 600; font-size: 12px; padding: 6px 16px; border: 1px solid #cbd5e0; border-radius: 4px; cursor: pointer;");
        btnClose.addEventListener(Events.ON_CLICK, (EventListener<Event>) e -> win.detach());

        footerBox.appendChild(btnClose);
        rootLayout.appendChild(footerBox);

        win.appendChild(rootLayout);
        win.setPage(page);
        win.doModal();
    }

    public void onClick$cardEOD(Event event) { selectEOD(); }
    public void onCheck$radEOD(Event event) { selectEOD(); }
    public void onClick$cardBOD(Event event) { selectBOD(); }
    public void onCheck$radBOD(Event event) { selectBOD(); }

    private void selectEOD() {
        if (!isSessionOpen) {
            Clients.showNotification("Clearing session is CLOSED. Select Begin of Day (BOD).", "warning", null, "top_center", 2500);
            return;
        }
        this.selectedAction = "EOD";
        updateCardStyles();
    }

    private void selectBOD() {
        if (isSessionOpen) {
            Clients.showNotification("Session is OPEN. End of Day (EOD) must be completed first.", "warning", null, "top_center", 2500);
            return;
        }
        this.selectedAction = "BOD";
        updateCardStyles();
    }

    public void onClick$btnStartSession(Event event) {
        if ("EOD".equals(selectedAction)) {
            handleEODFlow();
        } else if ("BOD".equals(selectedAction)) {
            handleBODFlow();
        }
    }

    private void handleEODFlow() {
        if (!isSessionOpen) {
            Clients.showNotification("Session is already closed.", "error", null, "top_center", 2000);
            return;
        }

        if (pendingChequesCount > 0) {
            openForcedEODModal();
        } else {
            Messagebox.show("Are you sure you want to close the clearing day (" + currentClearingDate.format(dateFormatter) + ")?",
                "Confirm EOD", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, evt -> {
                    if (Messagebox.ON_YES.equals(evt.getName())) {
                        executeEOD(false, null);
                    }
                });
        }
    }

    private void openForcedEODModal() {
        final Window win = new Window();
        win.setTitle("Pending Cheques Detected - End of Day (EOD)");
        win.setWidth("500px");
        win.setBorder("normal");
        win.setClosable(true);

        Vlayout container = new Vlayout();
        container.setSpacing("14px");
        container.setStyle("padding: 16px; background: #ffffff;");

        Div alertBox = new Div();
        alertBox.setStyle("background:#fff5f5; border:1px solid #feb2b2; padding:12px; border-radius:6px; font-size:12px; color:#9b2c2c;");
        alertBox.appendChild(new Label("Warning: " + pendingChequesCount + " pending cheques detected. Proceeding will move them to the UNPROCESSED queue of their respective roles."));
        container.appendChild(alertBox);

        Label lblRemark = new Label("Admin Remarks (Mandatory):");
        lblRemark.setStyle("font-weight:600; font-size:12px; color:#2d3748;");
        container.appendChild(lblRemark);

        final Textbox txtRemarks = new Textbox();
        txtRemarks.setRows(3);
        txtRemarks.setWidth("100%");
        txtRemarks.setPlaceholder("Enter mandatory reason for closing EOD with pending cheques...");
        container.appendChild(txtRemarks);

        Hbox btnBox = new Hbox();
        btnBox.setWidth("100%");
        btnBox.setPack("end");
        btnBox.setSpacing("8px");

        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle("padding: 5px 15px; font-size:12px; cursor: pointer;");
        btnCancel.addEventListener(Events.ON_CLICK, (EventListener<Event>) e -> win.detach());

        Button btnConfirm = new Button("Proceed with EOD");
        btnConfirm.setStyle("background:#c53030; color:white; font-weight:bold; padding: 5px 15px; font-size:12px; border:none; border-radius:4px; cursor: pointer;");
        btnConfirm.addEventListener(Events.ON_CLICK, (EventListener<Event>) e -> {
            String remarks = txtRemarks.getValue();
            if (remarks == null || remarks.trim().isEmpty()) {
                Clients.showNotification("Remarks are mandatory when pending cheques exist.", "error", txtRemarks, "top_center", 2000);
                return;
            }
            win.detach();
            executeEOD(true, remarks.trim());
        });

        btnBox.appendChild(btnCancel);
        btnBox.appendChild(btnConfirm);
        container.appendChild(btnBox);

        win.appendChild(container);
        win.setPage(page);
        win.doModal();
    }

    private void executeEOD(boolean isForced, String remarks) {
        String adminUserId = resolveLoggedInUserId();
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String updateSessionSql = "UPDATE clearing_session " +
                                  "SET session_status = 'CLOSED', closed_at = ?, closed_by = ?, remarks = ? " +
                                  "WHERE clearing_date = ? AND session_status = 'OPEN'";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Close the clearing session
            try (PreparedStatement ps = conn.prepareStatement(updateSessionSql)) {
                ps.setTimestamp(1, now);
                ps.setString(2, adminUserId);
                ps.setString(3, remarks != null ? remarks : "Normal EOD closed");
                ps.setDate(4, Date.valueOf(this.currentClearingDate));
                int rowsUpdated = ps.executeUpdate();

                if (rowsUpdated == 0) {
                    conn.rollback();
                    Clients.showNotification("No OPEN session found for current date.", "error", null, "top_center", 3000);
                    return;
                }
            }

            conn.commit();

            String auditDetail = isForced
                ? "Forced EOD completed with " + pendingChequesCount + " pending cheques paused. Reason: " + remarks
                : "Normal EOD closed successfully for date " + currentClearingDate;
            AuditServiceImpl.getInstance().log("EOD_BOD", "EOD_COMPLETED", auditDetail, "SUCCESS");

            this.isSessionOpen = false;
            this.selectedAction = "BOD";
            this.pendingTransactionsList.clear();
            this.pendingChequesCount = 0;

            Sessions.getCurrent().setAttribute("CTS_SESSION_OPEN", false);
            Sessions.getCurrent().setAttribute("CTS_CLEARING_DATE", this.currentClearingDate);

            Events.postEvent(new Event("onSessionStatusChanged", getPage().getFirstRoot(), false));
            refreshUI();

            String msg = isForced ? "Forced EOD Completed. Pending items will move to Unprocessed on next BOD." : "EOD completed successfully.";
            Clients.showNotification(msg, "info", null, "top_center", 3000);

        } catch (SQLException ex) {
            ex.printStackTrace();
            AuditServiceImpl.getInstance().log("EOD_BOD", "EOD_FAILED", "EOD execution failed: " + ex.getMessage(), "FAILED");
            Clients.showNotification("Database error closing EOD: " + ex.getMessage(), "error", null, "top_center", 3000);
        }
    }

    private void handleBODFlow() {
        if (isSessionOpen) {
            Clients.showNotification("Previous session EOD is not completed. BOD cannot proceed.", "error", null, "top_center", 2500);
            return;
        }

        String adminUserId = resolveLoggedInUserId();
        LocalDate nextDate = this.currentClearingDate.plusDays(1);
        Timestamp now = new Timestamp(System.currentTimeMillis());

        String insertBodSql = "INSERT INTO clearing_session (clearing_date, session_status, opened_by, opened_at) VALUES (?, 'OPEN', ?, ?)";
        
        // 1. Rollover scan_cheque pending items to UNPROCESSED
        String updateScanChequesToUnprocessedSql = 
                "UPDATE scan_cheque " +
                "SET cheque_status = 'UNPROCESSED' " +
                "WHERE UPPER(cheque_status) IN ('RAW', 'PENDING_REPAIR', 'PENDING_DATA_ENTRY', 'PENDING', 'Pending')";

        // 2. Rollover scan_batch pending items to UNPROCESSED
        String updateScanBatchesToUnprocessedSql = 
                "UPDATE scan_batch " +
                "SET batch_status = 'UNPROCESSED' " +
                "WHERE UPPER(batch_status) IN ('PENDING', 'Pending', 'RAW')";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            // Step 1: Open new clearing session
            try (PreparedStatement psSession = conn.prepareStatement(insertBodSql)) {
                psSession.setDate(1, Date.valueOf(nextDate));
                psSession.setString(2, adminUserId);
                psSession.setTimestamp(3, now);
                psSession.executeUpdate();
            }

            // Step 2: Transition pending scan_cheques to UNPROCESSED
            int rolledOverCheques = 0;
            try (PreparedStatement psCheques = conn.prepareStatement(updateScanChequesToUnprocessedSql)) {
                rolledOverCheques = psCheques.executeUpdate();
            }

            // Step 3: Transition pending scan_batches to UNPROCESSED
            int rolledOverBatches = 0;
            try (PreparedStatement psBatches = conn.prepareStatement(updateScanBatchesToUnprocessedSql)) {
                rolledOverBatches = psBatches.executeUpdate();
            }

            conn.commit();

            // Step 4: Audit Trail
            AuditServiceImpl.getInstance().log("EOD_BOD", "BOD_STARTED", 
                    "BOD initialized for date: " + nextDate + " | Rolled over " + rolledOverCheques 
                    + " scan cheques and " + rolledOverBatches + " scan batches to UNPROCESSED", "SUCCESS");

            // Step 5: Update UI State
            this.currentClearingDate = nextDate;
            this.isSessionOpen = true;
            this.selectedAction = "EOD";
            this.pendingChequesCount = 0;

            Sessions.getCurrent().setAttribute("CTS_SESSION_OPEN", true);
            Sessions.getCurrent().setAttribute("CTS_CLEARING_DATE", this.currentClearingDate);

            Events.postEvent(new Event("onSessionStatusChanged", getPage().getFirstRoot(), true));
            refreshUI();

            String successMsg = "BOD Completed. Clearing date: " + currentClearingDate.format(dateFormatter);
            if (rolledOverCheques > 0) {
                successMsg += " (" + rolledOverCheques + " scan cheques moved to Maker Unprocessed Queue)";
            }
            Clients.showNotification(successMsg, "info", null, "top_center", 3500);

        } catch (SQLException ex) {
            ex.printStackTrace();
            AuditServiceImpl.getInstance().log("EOD_BOD", "BOD_FAILED", "Failed to start BOD: " + ex.getMessage(), "FAILED");
            Clients.showNotification("Database error starting BOD: " + ex.getMessage(), "error", null, "top_center", 3000);
        }
    }
    private String resolveLoggedInUserId() {
        String adminUserId = (String) Sessions.getCurrent().getAttribute("USER_ID");
        if (adminUserId == null) adminUserId = (String) Sessions.getCurrent().getAttribute("CTS_USER_ID");
        if (adminUserId == null) {
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement("SELECT user_id FROM users LIMIT 1");
                 ResultSet rs = ps.executeQuery()) {
                if (rs.next()) return rs.getString("user_id");
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return "USR1001";
        }
        return adminUserId;
    }
    
}