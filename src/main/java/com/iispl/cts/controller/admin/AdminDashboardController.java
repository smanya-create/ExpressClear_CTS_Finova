package com.iispl.cts.controller.admin;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueues;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.common.util.ActiveUserManager;
import com.iispl.cts.common.util.ClearingTimeMock;
import com.iispl.cts.common.util.SecurityUtil;
import com.iispl.cts.serviceimpl.AuditServiceImpl;

public class AdminDashboardController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // Header & Metric Component Wires
    private Label lblHeaderDate;
    private Label lblClearingDate;
    private Label lblSessionStatus;
    private Label lblLoggedInUsers;
    private Div dotSessionStatus;
    private Combobox cmbCyclePhase;
    

    // Selection Controls
    private Div cardEOD;
    private Div cardBOD;
    private Div ringEOD;
    private Div ringBOD;
    private Radio radEOD;
    private Radio radBOD;
    private Button btnStartSession;

    // State Variables
    private LocalDate currentClearingDate;
    private boolean isSessionOpen;
    private int loggedInUsersCount;
    private String selectedAction = "EOD";

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        if (!SecurityUtil.checkAccess("ADMIN_DASHBOARD")) {
            return;
        }

        super.doAfterCompose(comp);
        loadSessionData();
        refreshUI();
        refreshActiveUsers();
        initCycleDropdown();
    }
    private void initCycleDropdown() {
        if (cmbCyclePhase != null) {
            String activePhase = ClearingTimeMock.getActivePhase();
            for (Comboitem item : cmbCyclePhase.getItems()) {
                if (item.getValue().toString().equalsIgnoreCase(activePhase)) {
                    cmbCyclePhase.setSelectedItem(item);
                    return;
                }
            }
            // Fallback default if not matched
            if (!cmbCyclePhase.getItems().isEmpty()) {
                cmbCyclePhase.setSelectedIndex(0);
            }
        }
    }

    private void loadSessionData() {
        fetchActiveClearingSession();
        fetchActiveUsersCount();
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
        String currentAdminId = resolveLoggedInUserId();
        this.loggedInUsersCount = ActiveUserManager.getActiveUserCount(currentAdminId);
    }
    
    public void refreshActiveUsers() {
        String currentAdminId = resolveLoggedInUserId();
        this.loggedInUsersCount = ActiveUserManager.getActiveUserCount(currentAdminId);
        if (lblLoggedInUsers != null) {
            lblLoggedInUsers.setValue(String.valueOf(this.loggedInUsersCount));
        }
    }
  //UI Synchronization & Card Selection
    private void refreshUI() {
        String formattedDate = currentClearingDate.format(dateFormatter);
        if (lblHeaderDate != null) lblHeaderDate.setValue(formattedDate);
        if (lblClearingDate != null) lblClearingDate.setValue(formattedDate);
        if (lblLoggedInUsers != null) lblLoggedInUsers.setValue(String.valueOf(loggedInUsersCount));

        refreshStatusBadge();
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

            if (btnStartSession != null) {
                btnStartSession.setLabel("CLOSE SESSION");
            }
        } else {
            if (cardEOD != null) cardEOD.setSclass("cts-choice-box");
            if (cardBOD != null) cardBOD.setSclass("cts-choice-box cts-choice-box-active");
            if (ringEOD != null) ringEOD.setSclass("cts-custom-radio-ring");
            if (ringBOD != null) ringBOD.setSclass("cts-custom-radio-ring cts-radio-checked");

            if (btnStartSession != null) {
                btnStartSession.setLabel("START SESSION");
            }
        }
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
        openConfirmEODModal();
    }

    private void openConfirmEODModal() {
        final Window modal = new Window();
        modal.setWidth("440px");
        modal.setBorder("none");
        modal.setClosable(false);
        modal.setSclass("cts-custom-modal");

        Vlayout container = new Vlayout();
        container.setSpacing("16px");
        container.setStyle("padding: 24px 24px 20px 24px; background: #ffffff; border-radius: 12px;");

        // Header icon + title row
        Hlayout headerLayout = new Hlayout();
        headerLayout.setSpacing("12px");
        headerLayout.setValign("middle");

        Div iconBadge = new Div();
        iconBadge.setSclass("cts-modal-icon-badge cts-badge-warning");
        Label iconSymbol = new Label("!");
        iconSymbol.setStyle("font-weight: 800; font-size: 15px; color: #b45309; line-height: 1;");
        iconBadge.appendChild(iconSymbol);

        Vlayout headerText = new Vlayout();
        headerText.setSpacing("2px");
        Label modalTitle = new Label("Confirm End of Day (EOD)");
        modalTitle.setStyle("font-weight: 700; font-size: 16px; color: #0f172a; line-height: 1.3;");

        Label modalSub = new Label("Clearing session closure");
        modalSub.setStyle("font-size: 12px; color: #64748b;");
        headerText.appendChild(modalTitle);
        headerText.appendChild(modalSub);

        headerLayout.appendChild(iconBadge);
        headerLayout.appendChild(headerText);
        container.appendChild(headerLayout);

        // Description box
        Vlayout bodyBox = new Vlayout();
        bodyBox.setSpacing("8px");
        bodyBox.setStyle("background: #f8fafc; border: 1px solid #e2e8f0; padding: 12px 14px; border-radius: 8px;");

        Label descLine1 = new Label("You are about to close the clearing day for:");
        descLine1.setStyle("font-size: 13px; color: #334155;");

        Label dateHighlight = new Label(currentClearingDate.format(dateFormatter));
        dateHighlight.setStyle("font-size: 14px; font-weight: 700; color: #0f172a;");

        Label descLine2 = new Label("Once closed, Makers and Checkers cannot post new batches until BOD is initiated for the next business day.");
        descLine2.setStyle("font-size: 12px; color: #64748b; line-height: 1.4;");

        bodyBox.appendChild(descLine1);
        bodyBox.appendChild(dateHighlight);
        bodyBox.appendChild(descLine2);
        container.appendChild(bodyBox);

        // Footer buttons
        Hlayout buttonBar = new Hlayout();
        buttonBar.setWidth("100%");
        buttonBar.setStyle("justify-content: flex-end; gap: 10px; margin-top: 4px;");

        Button btnCancel = new Button("Cancel");
        btnCancel.setSclass("cts-modal-btn-cancel");
        btnCancel.addEventListener(Events.ON_CLICK, (EventListener<Event>) e -> modal.detach());

        Button btnConfirm = new Button("Close Session");
        btnConfirm.setSclass("cts-modal-btn-danger");
        btnConfirm.addEventListener(Events.ON_CLICK, (EventListener<Event>) e -> {
            modal.detach();
            executeEOD();
        });

        buttonBar.appendChild(btnCancel);
        buttonBar.appendChild(btnConfirm);
        container.appendChild(buttonBar);

        modal.appendChild(container);
        modal.setPage(page);
        modal.doModal();
    }

    private void executeEOD() {
        String adminUserId = resolveLoggedInUserId();

        String updateSessionSql = "UPDATE clearing_session " +
                "SET session_status = 'CLOSED', " +
                "    closed_at = ?, " +
                "    session_time = ?, " +
                "    closed_by = ? " +
                "WHERE clearing_date = ? AND session_status = 'OPEN'";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);
            conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

            try (PreparedStatement ps = conn.prepareStatement(updateSessionSql)) {
                Timestamp simulatedClosedAt = ClearingTimeMock.getProcessingTimestamp();
                Time finalSessionTime = Time.valueOf(ClearingTimeMock.getCurrentTime());

                ps.setTimestamp(1, simulatedClosedAt);
                ps.setTime(2, finalSessionTime);
                ps.setString(3, adminUserId);
                ps.setDate(4, Date.valueOf(currentClearingDate));

                ps.executeUpdate();
            }
            //Begins an explicit atomic transaction.
            conn.commit();

            AuditServiceImpl.getInstance().log("EOD_BOD", "EOD_COMPLETED", 
                    "Normal EOD closed successfully for date " + currentClearingDate, "SUCCESS");

            this.isSessionOpen = false;
            this.selectedAction = "BOD";

            // Updating Session Scopes
            Sessions.getCurrent().setAttribute("CTS_SESSION_OPEN", false);
            Sessions.getCurrent().setAttribute("CTS_CLEARING_DATE", this.currentClearingDate);

            // Broadcast status to other active desktop components
            EventQueues.lookup("SESSION_UPDATE_QUEUE", EventQueues.DESKTOP, true)
                .publish(new Event("onSessionClosed", null, "CLOSED"));

            // Update Application-Wide Scope so Makers & Checkers immediately reflect session lock
            if (getPage() != null && getPage().getDesktop() != null && getPage().getDesktop().getWebApp() != null) {
                getPage().getDesktop().getWebApp().setAttribute("GLOBAL_CTS_SESSION_OPEN", false);
            }

            Events.postEvent(new Event("onSessionStatusChanged", getPage().getFirstRoot(), false));
            refreshUI();

            Clients.showNotification("EOD completed successfully.", "info", null, "top_center", 3000);

        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
                    rbEx.printStackTrace();
                }
            }
            ex.printStackTrace();
            AuditServiceImpl.getInstance().log("EOD_BOD", "EOD_FAILED", "EOD execution failed: " + ex.getMessage(), "FAILED");
            Clients.showNotification("Database error closing EOD: " + ex.getMessage(), "error", null, "top_center", 3000);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
        }
    }

    private void handleBODFlow() {
        fetchActiveClearingSession();
        if (isSessionOpen) {
            Clients.showNotification("Active clearing session is still OPEN. EOD must be completed first.", "error", null, "top_center", 2500);
            return;
        }

        openConfirmBODModal();
    }

    private void openConfirmBODModal() {
        final LocalDate nextDate = resolveTargetBODDate();

        final Window modal = new Window();
        modal.setWidth("440px");
        modal.setBorder("none");
        modal.setClosable(false);
        modal.setSclass("cts-custom-modal");

        Vlayout container = new Vlayout();
        container.setSpacing("16px");
        container.setStyle("padding: 24px 24px 20px 24px; background: #ffffff; border-radius: 12px;");

        // Header icon + title row
        Hlayout headerLayout = new Hlayout();
        headerLayout.setSpacing("12px");
        headerLayout.setValign("middle");

        Div iconBadge = new Div();
        iconBadge.setSclass("cts-modal-icon-badge cts-badge-success");
        Label iconSymbol = new Label("▶");
        iconSymbol.setStyle("font-weight: 800; font-size: 13px; color: #15803d; line-height: 1; margin-left: 2px;");
        iconBadge.appendChild(iconSymbol);

        Vlayout headerText = new Vlayout();
        headerText.setSpacing("2px");
        Label modalTitle = new Label("Confirm Begin of Day (BOD)");
        modalTitle.setStyle("font-weight: 700; font-size: 16px; color: #0f172a; line-height: 1.3;");

        Label modalSub = new Label("Clearing session initiation");
        modalSub.setStyle("font-size: 12px; color: #64748b;");
        headerText.appendChild(modalTitle);
        headerText.appendChild(modalSub);

        headerLayout.appendChild(iconBadge);
        headerLayout.appendChild(headerText);
        container.appendChild(headerLayout);

        // Description box
        Vlayout bodyBox = new Vlayout();
        bodyBox.setSpacing("8px");
        bodyBox.setStyle("background: #f8fafc; border: 1px solid #e2e8f0; padding: 12px 14px; border-radius: 8px;");

        Label descLine1 = new Label("You are about to open the clearing session for:");
        descLine1.setStyle("font-size: 13px; color: #334155;");

        Label dateHighlight = new Label(nextDate.format(dateFormatter));
        dateHighlight.setStyle("font-size: 14px; font-weight: 700; color: #0f172a;");

        Label descLine2 = new Label("Opening the session will unlock the clearing queues, allowing Makers and Checkers to process cheques and upload batches.");
        descLine2.setStyle("font-size: 12px; color: #64748b; line-height: 1.4;");

        bodyBox.appendChild(descLine1);
        bodyBox.appendChild(dateHighlight);
        bodyBox.appendChild(descLine2);
        container.appendChild(bodyBox);

        // Footer buttons
        Hlayout buttonBar = new Hlayout();
        buttonBar.setWidth("100%");
        buttonBar.setStyle("justify-content: flex-end; gap: 10px; margin-top: 4px;");

        Button btnCancel = new Button("Cancel");
        btnCancel.setSclass("cts-modal-btn-cancel");
        btnCancel.addEventListener(Events.ON_CLICK, (EventListener<Event>) e -> modal.detach());

        Button btnConfirm = new Button("Start Session");
        btnConfirm.setSclass("cts-modal-btn-primary");
        btnConfirm.addEventListener(Events.ON_CLICK, (EventListener<Event>) e -> {
            modal.detach();
            executeBOD(nextDate);
        });

        buttonBar.appendChild(btnCancel);
        buttonBar.appendChild(btnConfirm);
        container.appendChild(buttonBar);

        modal.appendChild(container);
        modal.setPage(page);
        modal.doModal();
    }

    private LocalDate resolveTargetBODDate() {
        LocalDate today = LocalDate.now();

        if (this.currentClearingDate == null || this.currentClearingDate.isBefore(today)) {
            return today;
        }

        if (this.currentClearingDate.equals(today)) {
            String checkStatusSql = "SELECT session_status FROM clearing_session " +
                                    "WHERE clearing_date = ? ORDER BY session_id DESC LIMIT 1";
            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(checkStatusSql)) {
                ps.setDate(1, Date.valueOf(today));
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        String status = rs.getString("session_status");
                        if ("CLOSED".equalsIgnoreCase(status)) {
                            return today.plusDays(1);
                        }
                    }
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return today;
        }

        return this.currentClearingDate;
    }

    private void executeBOD(LocalDate nextDate) {
        String adminUserId = resolveLoggedInUserId();

        LocalTime morningTime = LocalTime.of(9, 30, 0);
        Time sessionTime = Time.valueOf(morningTime);
        Timestamp simulatedOpenedAt = Timestamp.valueOf(nextDate.atTime(morningTime));

        String insertBodSql = "INSERT INTO clearing_session " +
                "(clearing_date, session_status, opened_by, opened_at, cycle_phase, session_time) " +
                "VALUES (?, 'OPEN', ?, ?, 'MORNING', ?)";

        Connection conn = null;
        try {
            conn = DBConnection.getConnection();
            conn.setAutoCommit(false);

            try (PreparedStatement psSession = conn.prepareStatement(insertBodSql)) {
                psSession.setDate(1, Date.valueOf(nextDate));
                psSession.setString(2, adminUserId);
                psSession.setTimestamp(3, simulatedOpenedAt);
                psSession.setTime(4, sessionTime);
                psSession.executeUpdate();
            }

            conn.commit();

            String clearingDateStr = nextDate.format(dateFormatter);

            AuditServiceImpl.getInstance().log("EOD_BOD", "BOD_STARTED", 
                    "BOD initialized for date: " + nextDate, "SUCCESS");

            this.currentClearingDate = nextDate;
            this.isSessionOpen = true;
            this.selectedAction = "EOD";

            ClearingTimeMock.setPreset("MORNING");

            // Update Session Scopes
            Sessions.getCurrent().setAttribute("CTS_SESSION_OPEN", true);
            Sessions.getCurrent().setAttribute("CTS_CLEARING_DATE", this.currentClearingDate);

            // Update Application-Wide Scope
            if (getPage() != null && getPage().getDesktop() != null && getPage().getDesktop().getWebApp() != null) {
                getPage().getDesktop().getWebApp().setAttribute("GLOBAL_CTS_SESSION_OPEN", true);
                getPage().getDesktop().getWebApp().setAttribute("GLOBAL_CLEARING_DATE", this.currentClearingDate);
            }

            Events.postEvent(new Event("onSessionStatusChanged", getPage().getFirstRoot(), true));
            refreshUI();

            Clients.showNotification("BOD successfully initiated for " + clearingDateStr, "info", null, "top_center", 3000);

            // Refresh the page to synchronize dependent components
            Executions.sendRedirect(null);

        } catch (SQLException ex) {
            if (conn != null) {
                try {
                    conn.rollback();
                } catch (SQLException rbEx) {
                    rbEx.printStackTrace();
                }
            }
            ex.printStackTrace();
            AuditServiceImpl.getInstance().log("EOD_BOD", "BOD_FAILED", "Failed to start BOD: " + ex.getMessage(), "FAILED");
            Clients.showNotification("Database error starting BOD: " + ex.getMessage(), "error", null, "top_center", 3000);
        } finally {
            if (conn != null) {
                try {
                    conn.setAutoCommit(true);
                    conn.close();
                } catch (SQLException closeEx) {
                    closeEx.printStackTrace();
                }
            }
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