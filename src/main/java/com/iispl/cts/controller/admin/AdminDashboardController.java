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
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
import org.zkoss.zul.Radio;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.common.util.ActiveUserManager;
import com.iispl.cts.common.util.ClearingTimeMock;
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
			return;
		}

		super.doAfterCompose(comp);
		loadSessionData();
		refreshUI();
		refreshActiveUsers();
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

	private void loadPendingCheques() {
		pendingTransactionsList.clear();

		String sql = 
				// 1. SCAN STAGE: Exclude cheques whose parent batch or cheque has already progressed to outward clearing
				"SELECT sc.scanned_batch_id AS batch_id, " +
				"       sb.batch_reference_id, " +
				"       sc.cheque_number, " +
				"       sc.cheque_status, " +
				"       sc.cheque_amount, " +
				"       sc.created_at, " +
				"       'SCAN_STAGE' AS pipeline_source " +
				"FROM scan_cheque sc " +
				"LEFT JOIN scan_batch sb ON sc.scanned_batch_id = sb.scanned_batch_id " +
				"WHERE UPPER(sc.cheque_status) IN ('PENDING_DATA_ENTRY', 'PENDING_REPAIR', 'PENDING_MICR_REPAIR', 'RAW', 'PENDING') " +
				"  AND UPPER(COALESCE(sb.batch_status, '')) NOT IN ('COMPLETED', 'PROMOTED', 'PENDING_CHECKER_PROCESS') " +
				"  AND NOT EXISTS ( " +
				"      SELECT 1 FROM outward_batch ob " +
				"      WHERE ob.outward_batch_id = sc.scanned_batch_id " +
				"  ) " +

				"UNION ALL " +

				// 2. OUTWARD STAGE: Items currently awaiting Checker review or verification
				"SELECT oc.outward_batch_id AS batch_id, " +
				"       ob.batch_reference_id, " +
				"       oc.cheque_number, " +
				"       oc.cheque_status, " +
				"       oc.cheque_amount, " +
				"       oc.created_at, " +
				"       'OUTWARD_STAGE' AS pipeline_source " +
				"FROM outward_cheque oc " +
				"LEFT JOIN outward_batch ob ON oc.outward_batch_id = ob.outward_batch_id " +
				"WHERE UPPER(oc.cheque_status) IN ('PENDING_VERIFICATION', 'PENDING_CHECKER_VERIFICATION', 'PENDING_DATA_ENTRY', 'PENDING_REPAIR', 'PENDING_MICR_REPAIR', 'PENDING') " +

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
				} else if ("PENDING_REPAIR".equalsIgnoreCase(status) || "PENDING_MICR_REPAIR".equalsIgnoreCase(status) || "RAW".equalsIgnoreCase(status)) {
					assignedQueue = "Outward Maker (MICR Repair)";
				} else if ("PENDING_VERIFICATION".equalsIgnoreCase(status) || "PENDING_CHECKER_VERIFICATION".equalsIgnoreCase(status)) {
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
			executeEOD(false, null);
		});

		buttonBar.appendChild(btnCancel);
		buttonBar.appendChild(btnConfirm);
		container.appendChild(buttonBar);

		modal.appendChild(container);
		modal.setPage(page);
		modal.doModal();
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
		        "SET session_status = 'CLOSED', " +
		        "    closed_at = ?, " +
		        "    session_time = ?, " +
		        "    closed_by = ?, " +
		        "    remarks = ? " +
		        "WHERE clearing_date = ? AND session_status = 'OPEN'";

		String updateScanChequesSql = 
				"UPDATE scan_cheque SET cheque_status = CASE " +
						"    WHEN UPPER(cheque_status) LIKE '%DATA_ENTRY%' THEN 'UNPROCESSED_DATA_ENTRY' " +
						"    ELSE 'UNPROCESSED_MICR' " +
						"END " +
						"WHERE UPPER(cheque_status) IN ('PENDING_MICR_REPAIR', 'PENDING_REPAIR', 'PENDING_DATA_ENTRY', 'RAW', 'PENDING')";

		String updateScanBatchesSql = 
				"UPDATE scan_batch SET batch_status = 'UNPROCESSED' " +
						"WHERE UPPER(batch_status) IN ('PENDING_MAKER_PROCESS', 'PENDING', 'RAW')";

		String updateOutwardChequesSql = 
				"UPDATE outward_cheque SET cheque_status = 'UNPROCESSED_VERIFY' " +
						"WHERE UPPER(cheque_status) IN ('PENDING_CHECKER_VERIFICATION', 'PENDING_VERIFICATION', 'PENDING')";

		String updateOutwardBatchesSql = 
				"UPDATE outward_batch SET batch_status = 'UNPROCESSED' " +
						"WHERE UPPER(batch_status) IN ('PENDING_CHECKER_PROCESS', 'PENDING')";

		Connection conn = null;
		try {
			conn = DBConnection.getConnection();
			conn.setAutoCommit(false);
			conn.setTransactionIsolation(Connection.TRANSACTION_SERIALIZABLE);

			try (PreparedStatement ps = conn.prepareStatement(updateSessionSql)) {
			    // 1. Simulated timestamp (e.g., 2026-09-22 15:30:xx)
			    java.sql.Timestamp simulatedClosedAt = ClearingTimeMock.getProcessingTimestamp();
			    
			    // 2. Simulated time (e.g., 15:30:00)
			    java.sql.Time finalSessionTime = java.sql.Time.valueOf(ClearingTimeMock.getCurrentTime());

			    ps.setTimestamp(1, simulatedClosedAt);
			    ps.setTime(2, finalSessionTime);
			    ps.setString(3, adminUserId); // or your user ID variable
			    ps.setString(4, remarks);       // or null / reason string
			    ps.setDate(5, java.sql.Date.valueOf(currentClearingDate));

			    ps.executeUpdate();
			}
			int rolledOverScan = 0;
			int rolledOverOutward = 0;
			if (isForced) {
				try (PreparedStatement ps1 = conn.prepareStatement(updateScanChequesSql)) {
					rolledOverScan = ps1.executeUpdate();
				}
				try (PreparedStatement ps2 = conn.prepareStatement(updateScanBatchesSql)) {
					ps2.executeUpdate();
				}
				try (PreparedStatement ps3 = conn.prepareStatement(updateOutwardChequesSql)) {
					rolledOverOutward = ps3.executeUpdate();
				}
				try (PreparedStatement ps4 = conn.prepareStatement(updateOutwardBatchesSql)) {
					ps4.executeUpdate();
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

			// Update User Session
			Sessions.getCurrent().setAttribute("CTS_SESSION_OPEN", false);
			// Broadcast to HeaderController across the active desktop
			org.zkoss.zk.ui.event.EventQueues.lookup("SESSION_UPDATE_QUEUE", org.zkoss.zk.ui.event.EventQueues.DESKTOP, true)
			    .publish(new org.zkoss.zk.ui.event.Event("onSessionClosed", null, "CLOSED"));
			Sessions.getCurrent().setAttribute("CTS_CLEARING_DATE", this.currentClearingDate);

			// Update Application-Wide Scope so Makers & Checkers immediately reflect lock
			if (getPage() != null && getPage().getDesktop() != null && getPage().getDesktop().getWebApp() != null) {
			    getPage().getDesktop().getWebApp().setAttribute("GLOBAL_CTS_SESSION_OPEN", false);
			}

			Events.postEvent(new Event("onSessionStatusChanged", getPage().getFirstRoot(), false));
			refreshUI();

			String msg = isForced ? "Forced EOD completed. " + (rolledOverScan + rolledOverOutward) + " items marked UNPROCESSED." : "EOD completed successfully.";
			Clients.showNotification(msg, "info", null, "top_center", 3000);

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
			Clients.showNotification("Active clearing session is still OPEN in database. EOD must be completed first.", "error", null, "top_center", 2500);
			return;
		}

		openConfirmBODModal();
	}
	private void openConfirmBODModal() {
		// DYNAMIC: Resolves to today unless today is already closed
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

		// If currentClearingDate is null or before today, use today
		if (this.currentClearingDate == null || this.currentClearingDate.isBefore(today)) {
			return today;
		}

		// If the clearing date is today, check if today's session is already CLOSED in DB
		if (this.currentClearingDate.equals(today)) {
			String checkStatusSql = "SELECT session_status FROM clearing_session " +
			                        "WHERE clearing_date = ? ORDER BY session_id DESC LIMIT 1";
			try (Connection conn = DBConnection.getConnection();
			     PreparedStatement ps = conn.prepareStatement(checkStatusSql)) {
				ps.setDate(1, java.sql.Date.valueOf(today));
				try (ResultSet rs = ps.executeQuery()) {
					if (rs.next()) {
						String status = rs.getString("session_status");
						// Only if today's session is CLOSED does BOD advance to next day
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

		// If currentClearingDate is already set to today or future, use it
		return this.currentClearingDate;
	}
	private void executeBOD(LocalDate nextDate) {
		String adminUserId = resolveLoggedInUserId();

		// 1. Initial simulated morning time
		java.time.LocalTime morningTime = java.time.LocalTime.of(10, 30, 0);
		java.sql.Time sessionTime = java.sql.Time.valueOf(morningTime);
		Timestamp simulatedOpenedAt = Timestamp.valueOf(nextDate.atTime(morningTime));

		// 2. Updated SQL: Explicitly insert cycle_phase and session_time
		String insertBodSql = "INSERT INTO clearing_session " +
				"(clearing_date, session_status, opened_by, opened_at, cycle_phase, session_time) " +
				"VALUES (?, 'OPEN', ?, ?, 'MORNING', ?)";

		String countScanUnprocessed = "SELECT COUNT(*) FROM scan_cheque WHERE UPPER(cheque_status) LIKE 'UNPROCESSED%'";
		String countOutwardUnprocessed = "SELECT COUNT(*) FROM outward_cheque WHERE UPPER(cheque_status) LIKE 'UNPROCESSED%'";

		Connection conn = null;
		try {
			conn = DBConnection.getConnection();
			conn.setAutoCommit(false);

			try (PreparedStatement psSession = conn.prepareStatement(insertBodSql)) {
				psSession.setDate(1, Date.valueOf(nextDate));
				psSession.setString(2, adminUserId);
				psSession.setTimestamp(3, simulatedOpenedAt); // 2026-09-24 10:30:00
				psSession.setTime(4, sessionTime);            // 10:30:00
				psSession.executeUpdate();
			}

			int rolledOverScanCheques = 0;
			int rolledOverCheckerCheques = 0;

			try (PreparedStatement ps = conn.prepareStatement(countScanUnprocessed);
					ResultSet rs = ps.executeQuery()) {
				if (rs.next()) rolledOverScanCheques = rs.getInt(1);
			}

			try (PreparedStatement ps = conn.prepareStatement(countOutwardUnprocessed);
					ResultSet rs = ps.executeQuery()) {
				if (rs.next()) rolledOverCheckerCheques = rs.getInt(1);
			}

			conn.commit();

			String clearingDateStr = nextDate.format(dateFormatter);

			AuditServiceImpl.getInstance().log("EOD_BOD", "BOD_STARTED", 
					"BOD initialized for date: " + nextDate + " | Unprocessed scan: " + rolledOverScanCheques 
					+ ", outward: " + rolledOverCheckerCheques, "SUCCESS");

			this.currentClearingDate = nextDate;
			this.isSessionOpen = true;
			this.selectedAction = "EOD";
			this.pendingChequesCount = 0;

			// 3. Initialize ClearingTimeMock explicitly to MORNING
			com.iispl.cts.common.util.ClearingTimeMock.setPreset("MORNING");

			// Update User Session
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

			// 4. Force a clean page refresh so HeaderController re-runs doAfterCompose with MORNING
			org.zkoss.zk.ui.Executions.sendRedirect(null);

		} catch (SQLException ex) {
			if (conn != null) {
				try {
					conn.rollback();
				} catch (SQLException rbEx) {
					rbEx.printStackTrace();
				}
			}
			ex.printStackTrace();
			AuditServiceImpl.getInstance(
					).log("EOD_BOD", "BOD_FAILED", "Failed to start BOD: " + ex.getMessage(), "FAILED");
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
	public void onTimer$userRefreshTimer(Event event) {
	    refreshActiveUsers();
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