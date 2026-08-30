package com.iispl.cts.controller.admin;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.EventQueues;
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
    private final List<PendingChequeItem> pendingTransactionsList = new ArrayList<>();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        loadSessionData();
        refreshUI();
    }

    private void loadSessionData() {
        this.currentClearingDate = LocalDate.now();
        this.isSessionOpen = true;
        this.loggedInUsersCount = 0;
        this.selectedAction = this.isSessionOpen ? "EOD" : "BOD";

        // Mock pending cheques data corresponding to Workflow Steps 3 & 4
        pendingTransactionsList.clear();
        pendingTransactionsList.add(new PendingChequeItem("BATCH-OUT-101", "CHQ-882910", "OUTWARD", "MAKER PENDING", "Outward Maker Queue", "Awaiting Data Entry / MICR"));
        pendingTransactionsList.add(new PendingChequeItem("BATCH-OUT-102", "CHQ-993012", "OUTWARD", "CHECKER PENDING", "Outward Checker Queue", "Awaiting Verification / Approval"));
        pendingTransactionsList.add(new PendingChequeItem("BATCH-INW-504", "CHQ-110293", "INWARD", "CHECKER PENDING", "Inward Checker Queue", "Awaiting Return Reason / Pass"));

        this.pendingChequesCount = pendingTransactionsList.size();
    }

    private void refreshUI() {
        String formattedDate = currentClearingDate.format(dateFormatter);
        if (lblHeaderDate != null) lblHeaderDate.setValue(formattedDate);
        if (lblClearingDate != null) lblClearingDate.setValue(formattedDate);
        if (lblLoggedInUsers != null) lblLoggedInUsers.setValue(String.valueOf(loggedInUsersCount));
        if (lblPendingCount != null) lblPendingCount.setValue(String.valueOf(pendingChequesCount));

        if (lblSessionStatus != null) {
            if (isSessionOpen) {
                lblSessionStatus.setValue("OPEN");
                lblSessionStatus.setStyle("font-weight: 700; color: #276749; font-size: 14px;");
            } else {
                lblSessionStatus.setValue("CLOSED");
                lblSessionStatus.setStyle("font-weight: 700; color: #c53030; font-size: 14px;");
            }
        }

        if (pendingChequesCount > 0 && isSessionOpen) {
            if (lblPendingTag != null) lblPendingTag.setValue("Requires attention before EOD");
            if (lblEODWarnText != null) lblEODWarnText.setValue(pendingChequesCount + " pending cheques detected");
            if (lblWarningMsg != null) lblWarningMsg.setValue(pendingChequesCount + " cheques are currently pending. Review their current processing stage before performing EOD.");
            if (boxWarningBanner != null) boxWarningBanner.setVisible(true);
        } else {
            if (lblPendingTag != null) lblPendingTag.setValue("");
            if (lblEODWarnText != null) lblEODWarnText.setValue("");
            if (boxWarningBanner != null) boxWarningBanner.setVisible(false);
        }

        updateCardStyles();
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
    // View Pending Cheques Modal Trigger
    public void onClick$btnViewPending(Event event) {
        openPendingTransactionsModal();
    }

    private void openPendingTransactionsModal() {
        final Window win = new Window();
        win.setTitle("Pending Cheques & Batches");
        win.setWidth("760px");
        win.setBorder("normal");
        win.setClosable(true);

        Vlayout rootLayout = new Vlayout();
        rootLayout.setSpacing("12px");
        rootLayout.setStyle("padding: 16px; background: #ffffff;");

        // Subtitle
        Label subLabel = new Label("Review items paused in the clearing pipeline before triggering EOD closure.");
        subLabel.setStyle("font-size: 12px; color: #718096; display: block; margin-bottom: 4px;");
        rootLayout.appendChild(subLabel);

        // Grid Table
        Grid grid = new Grid();
        grid.setStyle("border: 1px solid #e2e8f0; border-radius: 6px;");

        Columns cols = new Columns();
        cols.setSizable(false);

        Column cBatch = new Column("Batch ID");
        cBatch.setWidth("140px");
        cBatch.setStyle("font-weight: bold; font-size: 11px; background: #f7fafc; color: #4a5568;");

        Column cChq = new Column("Cheque No.");
        cChq.setWidth("110px");
        cChq.setStyle("font-weight: bold; font-size: 11px; background: #f7fafc; color: #4a5568;");

        Column cDir = new Column("Direction");
        cDir.setWidth("90px");
        cDir.setStyle("font-weight: bold; font-size: 11px; background: #f7fafc; color: #4a5568;");

        Column cStage = new Column("Paused Stage / Status");
        cStage.setWidth("170px");
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
        for (PendingChequeItem item : pendingTransactionsList) {
            Row row = new Row();
            row.setStyle("font-size: 12px; height: 38px;");

            // Batch ID
            Label lBatch = new Label(item.batchId);
            lBatch.setStyle("font-weight: 600; color: #2d3748;");

            // Cheque ID
            Label lChq = new Label(item.chequeNo);
            lChq.setStyle("color: #4a5568;");

            // Direction Pill
            Label lDir = new Label(item.direction);
            if ("OUTWARD".equalsIgnoreCase(item.direction)) {
                lDir.setStyle("font-size: 10px; font-weight: bold; color: #2b6cb0; background: #ebf8ff; padding: 2px 6px; border-radius: 4px;");
            } else {
                lDir.setStyle("font-size: 10px; font-weight: bold; color: #805ad5; background: #faf5ff; padding: 2px 6px; border-radius: 4px;");
            }

            // Current Paused Stage Badge
            Label lStage = new Label(item.pausedStage);
            lStage.setStyle("font-size: 10px; font-weight: bold; color: #c05621; background: #feebc8; padding: 2px 8px; border-radius: 10px;");

            // Assigned Queue
            Label lQueue = new Label(item.assignedQueue);
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

        // Modal Bottom Actions
        Hbox footerBox = new Hbox();
        footerBox.setWidth("100%");
        footerBox.setPack("end");
        footerBox.setStyle("margin-top: 10px;");

        Button btnClose = new Button("Close");
        btnClose.setStyle("background: #edf2f7; color: #2d3748; font-weight: 600; font-size: 12px; padding: 6px 16px; border: 1px solid #cbd5e0; border-radius: 4px; cursor: pointer;");
        btnClose.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            @Override
            public void onEvent(Event e) {
                win.detach();
            }
        });

        footerBox.appendChild(btnClose);
        rootLayout.appendChild(footerBox);

        win.appendChild(rootLayout);
        win.setPage(page);
        win.doModal();
    }

    // Card & Session Selectors
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
                    "Confirm EOD", Messagebox.YES | Messagebox.NO, Messagebox.QUESTION, new EventListener<Event>() {
                        @Override
                        public void onEvent(Event evt) {
                            if (Messagebox.ON_YES.equals(evt.getName())) {
                                executeEOD(false, null);
                            }
                        }
                    });
        }
    }

    private void openForcedEODModal() {
        final Window win = new Window();
        win.setTitle("Forced End of Day (EOD) Confirmation");
        win.setWidth("500px");
        win.setBorder("normal");
        win.setClosable(true);

        Vlayout container = new Vlayout();
        container.setSpacing("14px");
        container.setStyle("padding: 16px;");

        Div alertBox = new Div();
        alertBox.setStyle("background:#fff5f5; border:1px solid #feb2b2; padding:12px; border-radius:6px; font-size:12px; color:#9b2c2c;");
        alertBox.appendChild(new Label("Warning: " + pendingChequesCount + " pending cheques detected. Proceeding will convert them to UNPROCESSED and roll them over to the next clearing session."));
        container.appendChild(alertBox);

        Label lblRemark = new Label("Admin Remarks (Mandatory):");
        lblRemark.setStyle("font-weight:600; font-size:12px; color:#2d3748;");
        container.appendChild(lblRemark);

        final Textbox txtRemarks = new Textbox();
        txtRemarks.setRows(3);
        txtRemarks.setWidth("100%");
        txtRemarks.setPlaceholder("Enter mandatory reason for forcing EOD...");
        container.appendChild(txtRemarks);

        Hbox btnBox = new Hbox();
        btnBox.setWidth("100%");
        btnBox.setPack("end");
        btnBox.setSpacing("8px");

        Button btnCancel = new Button("Cancel");
        btnCancel.setStyle("padding: 5px 15px; font-size:12px;");
        btnCancel.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            @Override
            public void onEvent(Event e) { win.detach(); }
        });

        Button btnConfirm = new Button("Proceed with Forced EOD");
        btnConfirm.setStyle("background:#c53030; color:white; font-weight:bold; padding: 5px 15px; font-size:12px; border:none; border-radius:4px;");
        btnConfirm.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            @Override
            public void onEvent(Event e) {
                String remarks = txtRemarks.getValue();
                if (remarks == null || remarks.trim().isEmpty()) {
                    Clients.showNotification("Remarks are mandatory for Forced EOD.", "error", txtRemarks, "top_center", 2000);
                    return;
                }
                win.detach();
                executeEOD(true, remarks.trim());
            }
        });

        btnBox.appendChild(btnCancel);
        btnBox.appendChild(btnConfirm);
        container.appendChild(btnBox);

        win.appendChild(container);
        win.setPage(page);
        win.doModal();
    }

    private void executeEOD(boolean isForced, String remarks) {
        this.isSessionOpen = false;
        this.selectedAction = "BOD";

        // 1. Persist to standard HTTP session
        Sessions.getCurrent().setAttribute("CTS_SESSION_OPEN", false);
        Sessions.getCurrent().setAttribute("CTS_CLEARING_DATE", this.currentClearingDate);

        // 2. Post native ZK event to Desktop root (targets all included controllers on the page)
        Events.postEvent(new Event("onSessionStatusChanged", getPage().getFirstRoot(), false));

        refreshUI();

        String msg = isForced ? "Forced EOD Completed. Pending transactions marked as UNPROCESSED." : "Day closed successfully.";
        Clients.showNotification(msg, "info", null, "top_center", 3000);
    }
    private void handleBODFlow() {
    	if (isSessionOpen) {
            Clients.showNotification("Previous day EOD is not completed. BOD cannot proceed.", "error", null, "top_center", 2500);
            return;
        }

        this.currentClearingDate = this.currentClearingDate.plusDays(1);
        this.isSessionOpen = true;
        this.selectedAction = "EOD";

        // 1. Persist to standard HTTP session
        Sessions.getCurrent().setAttribute("CTS_SESSION_OPEN", true);
        Sessions.getCurrent().setAttribute("CTS_CLEARING_DATE", this.currentClearingDate);

        // 2. Post native ZK event to Desktop root
        Events.postEvent(new Event("onSessionStatusChanged", getPage().getFirstRoot(), true));

        refreshUI();
        Clients.showNotification("BOD Completed. New clearing date started: " + currentClearingDate.format(dateFormatter), "info", null, "top_center", 3000);
    }

    // Model DTO
    public static class PendingChequeItem {
        private final String batchId;
        private final String chequeNo;
        private final String direction;
        private final String pausedStage;
        private final String assignedQueue;
        private final String notes;

        public PendingChequeItem(String batchId, String chequeNo, String direction, String pausedStage, String assignedQueue, String notes) {
            this.batchId = batchId;
            this.chequeNo = chequeNo;
            this.direction = direction;
            this.pausedStage = pausedStage;
            this.assignedQueue = assignedQueue;
            this.notes = notes;
        }

        public String getBatchId() { return batchId; }
        public String getChequeNo() { return chequeNo; }
        public String getDirection() { return direction; }
        public String getPausedStage() { return pausedStage; }
        public String getAssignedQueue() { return assignedQueue; }
        public String getNotes() { return notes; }
    }
   

    private void refreshStatusBadge() {
        if (lblSessionStatus != null && dotSessionStatus != null) {
            if (isSessionOpen) {
                lblSessionStatus.setValue("OPEN");
                lblSessionStatus.setSclass("cts-status-text-open");
                dotSessionStatus.setSclass("cts-status-dot-open");
            } else {
                lblSessionStatus.setValue("CLOSED");
                lblSessionStatus.setSclass("cts-status-text-closed");
                dotSessionStatus.setSclass("cts-status-dot-closed");
            }
        }
    }
}