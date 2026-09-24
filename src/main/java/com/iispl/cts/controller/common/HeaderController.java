package com.iispl.cts.controller.common;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Vlayout;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.common.util.ClearingTimeMock;
import com.iispl.cts.entity.Notification;
import com.iispl.cts.service.NotificationService;
import com.iispl.cts.serviceimpl.NotificationServiceImpl;

public class HeaderController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    private Timer headerTimer;
    private Combobox cmbSimulateCycle;
    private Label lblHeaderSessionStatus;
    private Label lblHeaderSessionDate;
    private Label lblHeaderClock;
    private Div divNotificationBell;
    private Label lblUnreadBadge;
    private Label lblUserInitial;
    private Label lblHeaderUsername;
    private Label lblHeaderRole;
    private Popup popupNotifications;
    private Vlayout containerNotificationList;
    private Label lblOperatorCycleBadge;

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private final List<NotificationItem> notificationQueue = new ArrayList<>();
    private final NotificationService notificationService = NotificationServiceImpl.getInstance();

    private int pollTicks = 0;
    
    
    

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        // 1. Profile details first
        initUserProfile();

        // 2. Load DB Session State
        loadSessionState();

        // 3. Configure Cycle Display strictly by session role
        configureCycleDisplay();

        // 4. Hide notification bell for Admin if applicable
        String currentRole = getResolvedRole();
        Object showNotifArg = Executions.getCurrent().getArg().get("showNotifications");
        boolean suppressNotif = showNotifArg != null && "false".equalsIgnoreCase(String.valueOf(showNotifArg));

        if (isAdminRole(currentRole) || suppressNotif) {
            if (divNotificationBell != null) {
                divNotificationBell.setVisible(false);
            }
        }

        // 5. Security & Notifications...
        if (divNotificationBell != null && divNotificationBell.isVisible()) {
            try {
                loadDatabaseNotifications();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        if (comp != null && comp.getPage() != null) {
            try {
                com.iispl.cts.common.util.SecurityUtil.applySessionLockdown(comp.getPage());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Initial badge update
        syncSessionBadge();

        // Start timer/polling
        updateClockAndPoll();
    }
    private void syncSessionBadge() {
		// TODO Auto-generated method stub
    	boolean isOpen = false;

        // Check application-wide status first (reflects Admin EOD across all tabs)
        if (Sessions.getCurrent() != null && Sessions.getCurrent().getWebApp() != null) {
            Object globalOpen = Sessions.getCurrent().getWebApp().getAttribute("GLOBAL_CTS_SESSION_OPEN");
            if (globalOpen != null) {
                isOpen = Boolean.TRUE.equals(globalOpen);
            } else {
                isOpen = Boolean.TRUE.equals(Sessions.getCurrent().getAttribute("CTS_SESSION_OPEN"));
            }
        }

        updateHeaderBadge(isOpen);
		
	}
    private void updateHeaderBadge(boolean isOpen) {
        if (lblHeaderSessionStatus == null) return;

        if (isOpen) {
            lblHeaderSessionStatus.setValue("● OPEN");
            lblHeaderSessionStatus.setStyle(
                "display:inline-flex; align-items:center; justify-content:center; " +
                "font-size:11px; font-weight:700; line-height:1; padding:5px 12px; border-radius:9999px; " +
                "box-sizing:border-box; color:#15803d; background-color:#dcfce7; border:1px solid #bbf7d0;"
            );
        } else {
            lblHeaderSessionStatus.setValue("● CLOSED");
            lblHeaderSessionStatus.setStyle(
                "display:inline-flex; align-items:center; justify-content:center; " +
                "font-size:11px; font-weight:700; line-height:1; padding:5px 12px; border-radius:9999px; " +
                "box-sizing:border-box; color:#b91c1c; background-color:#fee2e2; border:1px solid #fecaca;"
            );
        }
    }
    
    private boolean isAdminRole(String role) {
        return role != null && role.contains("ADMIN");
    }
    private void configureCycleDisplay() {
        String currentRole = getResolvedRole();
        boolean isAdmin = isAdminRole(currentRole);

        if (cmbSimulateCycle != null) {
            cmbSimulateCycle.setVisible(isAdmin);
        }
        if (lblOperatorCycleBadge != null) {
            lblOperatorCycleBadge.setVisible(!isAdmin);
        }

        String activePhase = ClearingTimeMock.getActivePhase(); // "MORNING", "AFTERNOON", "LIVE"

        if (isAdmin && cmbSimulateCycle != null) {
            if ("MORNING".equalsIgnoreCase(activePhase)) {
                cmbSimulateCycle.setSelectedIndex(1);
            } else if ("AFTERNOON".equalsIgnoreCase(activePhase)) {
                cmbSimulateCycle.setSelectedIndex(2);
            } else {
                cmbSimulateCycle.setSelectedIndex(0);
            }
        } else if (lblOperatorCycleBadge != null) {
            if ("MORNING".equalsIgnoreCase(activePhase)) {
                lblOperatorCycleBadge.setValue("● OUTWARD (AM)");
                lblOperatorCycleBadge.setStyle("display:inline-flex; align-items:center; justify-content:center; font-size:11px; font-weight:700; line-height:1; padding:5px 12px; border-radius:9999px; box-sizing:border-box; color:#0369a1; background-color:#e0f2fe; border:1px solid #bae6fd;");
            } else if ("AFTERNOON".equalsIgnoreCase(activePhase)) {
                lblOperatorCycleBadge.setValue("● INWARD (PM)");
                lblOperatorCycleBadge.setStyle("display:inline-flex; align-items:center; justify-content:center; font-size:11px; font-weight:700; line-height:1; padding:5px 12px; border-radius:9999px; box-sizing:border-box; color:#7c2d12; background-color:#ffedd5; border:1px solid #fed7aa;");
            } else {
                lblOperatorCycleBadge.setValue("● ACTIVE (ALL)");
                lblOperatorCycleBadge.setStyle("display:inline-flex; align-items:center; justify-content:center; font-size:11px; font-weight:700; line-height:1; padding:5px 12px; border-radius:9999px; box-sizing:border-box; color:#15803d; background-color:#dcfce7; border:1px solid #bbf7d0;");
            }
        }
    }
    private String getResolvedRole() {
		// TODO Auto-generated method stub
    	if (Sessions.getCurrent() == null) return "GUEST";

        String role = (String) Sessions.getCurrent().getAttribute("CTS_USER_ROLE");
        if (role == null) role = (String) Sessions.getCurrent().getAttribute("USER_ROLE");
        if (role == null) role = (String) Sessions.getCurrent().getAttribute("ROLE_NAME");
        if (role == null) role = (String) Sessions.getCurrent().getAttribute("ROLE");

        return (role != null) ? role.trim().toUpperCase() : "MAKER";
	}
    private void updateOperatorCycleBadge() {
        if (lblOperatorCycleBadge == null) return;

        String phase = ClearingTimeMock.getActivePhase();

        if ("LIVE".equalsIgnoreCase(phase)) {
            lblOperatorCycleBadge.setValue("● ACTIVE (ALL)");
            lblOperatorCycleBadge.setStyle(
                "font-size:10.5px; font-weight:700; padding:6px 12px; border-radius:9999px; " +
                "color:#15803d; background:#ecfdf5; border:1px solid #a7f3d0; display:inline-block;"
            );
        } else if (ClearingTimeMock.isOutwardWindow()) {
            lblOperatorCycleBadge.setValue("● OUTWARD (AM)");
            lblOperatorCycleBadge.setStyle(
                "font-size:10.5px; font-weight:700; padding:6px 12px; border-radius:9999px; " +
                "color:#0369a1; background:#f0f9ff; border:1px solid #bae6fd; display:inline-block;"
            );
        } else {
            lblOperatorCycleBadge.setValue("● INWARD (PM)");
            lblOperatorCycleBadge.setStyle(
                "font-size:10.5px; font-weight:700; padding:6px 12px; border-radius:9999px; " +
                "color:#b45309; background:#fffbeb; border:1px solid #fde68a; display:inline-block;"
            );
        }
    }
    
    public void syncSimulationDropdown() {
        if (cmbSimulateCycle == null) return;

        String currentPhase = ClearingTimeMock.getActivePhase(); // Will now return "MORNING"

        for (Comboitem item : cmbSimulateCycle.getItems()) {
            if (currentPhase.equalsIgnoreCase(item.getValue())) {
                if (cmbSimulateCycle.getSelectedItem() != item) {
                    cmbSimulateCycle.setSelectedItem(item);
                }
                break;
            }
        }
    }
    public void onSelectSimulationCycle() {
    	if (cmbSimulateCycle == null || cmbSimulateCycle.getSelectedItem() == null) {
            return;
        }

        String selectedPhase = cmbSimulateCycle.getSelectedItem().getValue();
        ClearingTimeMock.setPreset(selectedPhase);

        // Immediately update the clock on change
        updateClockAndPoll();
    }

    private void initUserProfile() {
        String username = (String) Sessions.getCurrent().getAttribute("CTS_USERNAME");
        if (username == null) username = (String) Sessions.getCurrent().getAttribute("USERNAME");
        if (username == null || username.trim().isEmpty()) username = "User";

        String role = (String) Sessions.getCurrent().getAttribute("CTS_USER_ROLE");
        if (role == null) role = (String) Sessions.getCurrent().getAttribute("ROLE_NAME");
        if (role == null || role.trim().isEmpty()) role = "ADMIN";

        if (lblHeaderUsername != null) lblHeaderUsername.setValue(username);
        if (lblHeaderRole != null) lblHeaderRole.setValue(role);
        if (lblUserInitial != null && !username.isEmpty()) {
            lblUserInitial.setValue(username.substring(0, 1).toUpperCase());
        }

        boolean isOperationalRole = "OUTWARD_MAKER".equalsIgnoreCase(role)
                || "OUTWARD_CHECKER".equalsIgnoreCase(role)
                || "INWARD_MAKER".equalsIgnoreCase(role)
                || "INWARD_CHECKER".equalsIgnoreCase(role)
                || "ADMIN".equalsIgnoreCase(role);

        if (divNotificationBell != null) {
            divNotificationBell.setVisible(isOperationalRole);
        }
    }

    private void loadSessionState() {
        boolean isSessionOpen = false;
        LocalDate clearingDate = null;

        String sql = "SELECT clearing_date, session_status "
                + "FROM clearing_session "
                + "ORDER BY clearing_date DESC, opened_at DESC "
                + "LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                Date dbDate = rs.getDate("clearing_date");
                if (dbDate != null) clearingDate = dbDate.toLocalDate();
                isSessionOpen = "OPEN".equalsIgnoreCase(rs.getString("session_status"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        if (clearingDate == null) clearingDate = LocalDate.now();

        Sessions.getCurrent().setAttribute("CTS_SESSION_OPEN", isSessionOpen);
        Sessions.getCurrent().setAttribute("CTS_CLEARING_DATE", clearingDate);

        if (lblHeaderSessionDate != null) {
            lblHeaderSessionDate.setValue(clearingDate.format(dateFormatter));
            lblHeaderSessionDate.setVisible(true);
        }

        updateSessionBadge(isSessionOpen);
    }

    private void updateSessionBadge(boolean isOpen) {
        if (lblHeaderSessionStatus == null) return;

        String basePill = "display:inline-flex; align-items:center; justify-content:center; font-size:11px; font-weight:700; line-height:1; padding:5px 12px; border-radius:9999px; box-sizing:border-box; ";

        if (isOpen) {
            lblHeaderSessionStatus.setValue("● OPEN");
            lblHeaderSessionStatus.setStyle(basePill + "color:#276749; background:#c6f6d5;");
        } else {
            lblHeaderSessionStatus.setValue("● CLOSED");
            lblHeaderSessionStatus.setStyle(basePill + "color:#9b2c2c; background:#fed7d7;");
        }
    }

    public void onTimer$headerTimer(Event event) {
        updateClockAndPoll();
    }

    public void onTickClock() {
        updateClockAndPoll();
    }

    private void updateClockAndPoll() {
    	if (lblHeaderClock != null) {
            java.time.LocalTime mockTime = ClearingTimeMock.getCurrentTime();
            String formattedTime = mockTime.format(java.time.format.DateTimeFormatter.ofPattern("hh:mm:ss a"));
            lblHeaderClock.setValue(formattedTime);
        }
    	syncSessionBadge();
    }

    private void loadDatabaseNotifications() {
        String role = (lblHeaderRole != null) ? lblHeaderRole.getValue() : "ADMIN";
        String userId = (String) Sessions.getCurrent().getAttribute("USER_ID");
        if (userId == null) userId = (String) Sessions.getCurrent().getAttribute("CTS_USER_ID");

        List<Notification> dbList = notificationService.getUnreadNotifications(role, userId);

        notificationQueue.clear();
        if (dbList != null) {
            for (Notification item : dbList) {
                notificationQueue.add(new NotificationItem(
                    item.getNotificationId(),
                    item.getMessage(),
                    calculateRelativeTime(item.getCreatedAt())
                ));
            }
        }

        renderNotifications();
    }

    private String calculateRelativeTime(java.sql.Timestamp ts) {
        if (ts == null) return "Just now";
        long seconds = (System.currentTimeMillis() - ts.getTime()) / 1000;
        if (seconds < 60) return "Just now";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + " mins ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + " hrs ago";
        return (hours / 24) + " days ago";
    }

    private void renderNotifications() {
        if (containerNotificationList == null) return;
        containerNotificationList.getChildren().clear();

        if (notificationQueue.isEmpty()) {
            Div emptyDiv = new Div();
            emptyDiv.setStyle("padding: 20px; text-align: center;");
            Label emptyLbl = new Label("No new notifications");
            emptyLbl.setStyle("color: #a0aec0; font-size: 12px;");
            emptyDiv.appendChild(emptyLbl);
            containerNotificationList.appendChild(emptyDiv);

            if (lblUnreadBadge != null) {
                lblUnreadBadge.setVisible(false);
                lblUnreadBadge.setValue("0");
            }
            return;
        }

        if (lblUnreadBadge != null) {
            int count = notificationQueue.size();
            lblUnreadBadge.setValue(count > 99 ? "99+" : String.valueOf(count));
            lblUnreadBadge.setVisible(true);
        }

        final String activeRole = (lblHeaderRole != null) ? lblHeaderRole.getValue() : "";

        for (NotificationItem item : notificationQueue) {
            Div notifRow = new Div();
            notifRow.setStyle("padding: 10px 14px; border-bottom: 1px solid #edf2f7; cursor: pointer; transition: background 0.2s;");
            
            Vlayout itemLayout = new Vlayout();
            itemLayout.setSpacing("2px");

            Label msgLabel = new Label(item.message);
            msgLabel.setStyle("font-size: 12px; color: #2d3748; font-weight: 500; display: block;");

            Label timeLabel = new Label(item.timeAgo);
            timeLabel.setStyle("font-size: 10px; color: #a0aec0; display: block;");

            itemLayout.appendChild(msgLabel);
            itemLayout.appendChild(timeLabel);
            notifRow.appendChild(itemLayout);
            containerNotificationList.appendChild(notifRow);
        }
    }

   
    public void onClickMarkAllRead() {
        String role = (lblHeaderRole != null) ? lblHeaderRole.getValue() : "ADMIN";
        String userId = (String) Sessions.getCurrent().getAttribute("USER_ID");
        if (userId == null) userId = (String) Sessions.getCurrent().getAttribute("CTS_USER_ID");

        notificationService.markAllNotificationsAsRead(role, userId);
        notificationQueue.clear();
        renderNotifications();
    }

    private static class NotificationItem {
        final Long id;
        final String message;
        final String timeAgo;

        NotificationItem(Long id, String message, String timeAgo) {
            this.id = id;
            this.message = message;
            this.timeAgo = timeAgo;
        }
    }
    
}