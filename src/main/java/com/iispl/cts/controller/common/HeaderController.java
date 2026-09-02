package com.iispl.cts.controller.common;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Popup;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Vlayout;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.entity.Notification;
import com.iispl.cts.service.NotificationService;
import com.iispl.cts.serviceimpl.NotificationServiceImpl;

public class HeaderController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // Auto-wired by component ID match
    private Timer headerTimer;
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

    private final DateTimeFormatter timeFormatter = DateTimeFormatter.ofPattern("HH:mm:ss");
    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd-MMM-yyyy");

    private final List<NotificationItem> notificationQueue = new ArrayList<>();
    private final NotificationService notificationService = NotificationServiceImpl.getInstance();

    // Timer tick accumulator to poll the DB every 30 seconds instead of every second
    private int pollTicks = 0;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initUserProfile();
        loadSessionState();
        loadDatabaseNotifications();

        // Attach listener to first root element of the page
        if (getPage() != null && getPage().getFirstRoot() != null) {
            getPage().getFirstRoot().addEventListener("onSessionStatusChanged", new EventListener<Event>() {
                @Override
                public void onEvent(Event event) {
                    Boolean isOpen = (Boolean) event.getData();
                    updateSessionBadge(isOpen != null && isOpen);

                    LocalDate clearingDate = (LocalDate) Sessions.getCurrent().getAttribute("CTS_CLEARING_DATE");
                    if (clearingDate != null && lblHeaderSessionDate != null) {
                        lblHeaderSessionDate.setValue(clearingDate.format(dateFormatter));
                    }
                }
            });
        }
    }

    private void initUserProfile() {
        // Read authenticated user details from session
        String username = (String) Sessions.getCurrent().getAttribute("CTS_USERNAME");
        String role = (String) Sessions.getCurrent().getAttribute("CTS_USER_ROLE");

        if (username == null || username.trim().isEmpty()) {
            username = "Admin";
        }
        if (role == null || role.trim().isEmpty()) {
            role = "ADMIN";
        }

        if (lblHeaderUsername != null) lblHeaderUsername.setValue(username);
        if (lblHeaderRole != null) lblHeaderRole.setValue(role);
        if (lblUserInitial != null) lblUserInitial.setValue(username.substring(0, 1).toUpperCase());

        // Notifications are visible for operational roles and administrators
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

        // 1. Fetch from Database
        String sql = "SELECT clearing_date, session_status FROM clearing_session ORDER BY session_id DESC LIMIT 1";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                Date dbDate = rs.getDate("clearing_date");
                if (dbDate != null) {
                    clearingDate = dbDate.toLocalDate();
                }
                isSessionOpen = "OPEN".equalsIgnoreCase(rs.getString("session_status"));
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 2. Fallback to today's date if table is empty or date is null
        if (clearingDate == null) {
            clearingDate = LocalDate.now();
        }

        // 3. Save into Session Attributes
        Sessions.getCurrent().setAttribute("CTS_SESSION_OPEN", isSessionOpen);
        Sessions.getCurrent().setAttribute("CTS_CLEARING_DATE", clearingDate);

        // 4. Force update the Label
        if (lblHeaderSessionDate != null) {
            lblHeaderSessionDate.setValue(clearingDate.format(dateFormatter));
            lblHeaderSessionDate.setVisible(true);
        }

        // 5. Update Status Badge
        updateSessionBadge(isSessionOpen);
    }

    private void updateSessionBadge(boolean isOpen) {
        if (lblHeaderSessionStatus == null) {
            System.out.println("DEBUG: lblHeaderSessionStatus is NULL in HeaderController!");
            return;
        }

        if (isOpen) {
            lblHeaderSessionStatus.setValue("● OPEN");
            lblHeaderSessionStatus.setStyle("font-size: 11px; font-weight: bold; color: #276749; background: #c6f6d5; padding: 3px 10px; border-radius: 12px;");
        } else {
            lblHeaderSessionStatus.setValue("● CLOSED");
            lblHeaderSessionStatus.setStyle("font-size: 11px; font-weight: bold; color: #9b2c2c; background: #fed7d7; padding: 3px 10px; border-radius: 12px;");
        }
    }

    // Handles onTimer event from <timer id="headerTimer" .../>
    public void onTimer$headerTimer(Event event) {
        updateClockAndPoll();
    }

    public void onTickClock() {
        updateClockAndPoll();
    }

    private void updateClockAndPoll() {
        if (lblHeaderClock != null) {
            lblHeaderClock.setValue(LocalTime.now().format(timeFormatter));
        }

        // Background polling: check database for notifications every 30 seconds
        pollTicks++;
        if (pollTicks >= 30) {
            pollTicks = 0;
            loadDatabaseNotifications();
        }
    }

    /**
     * Reads unread notifications from DB for the active session user/role
     */
    private void loadDatabaseNotifications() {
        String role = (lblHeaderRole != null) ? lblHeaderRole.getValue() : "ADMIN";
        String userId = (String) Sessions.getCurrent().getAttribute("USER_ID");
        if (userId == null) {
            userId = (String) Sessions.getCurrent().getAttribute("CTS_USER_ID");
        }

        List<Notification> dbList = notificationService.getUnreadNotifications(role, userId);

        notificationQueue.clear();
        for (Notification item : dbList) {
            notificationQueue.add(new NotificationItem(
                item.getNotificationId(),
                item.getMessage(),
                calculateRelativeTime(item.getCreatedAt())
            ));
        }

        renderNotifications();
    }

    private String calculateRelativeTime(java.sql.Timestamp ts) {
    	if (ts == null) return "Just now";

        // Compare raw millisecond differences directly
        long diffMillis = System.currentTimeMillis() - ts.getTime();
        long seconds = diffMillis / 1000;

        if (seconds < 60) return "Just now";
        long minutes = seconds / 60;
        if (minutes < 60) return minutes + " mins ago";
        long hours = minutes / 60;
        if (hours < 24) return hours + " hrs ago";
        long days = hours / 24;
        return days + " days ago";
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

        for (NotificationItem item : notificationQueue) {
            Div notifRow = new Div();
            notifRow.setStyle("padding: 10px 14px; border-bottom: 1px solid #edf2f7; cursor: pointer; transition: background 0.2s;");
            notifRow.setWidgetListener("onMouseOver", "this.style.background='#f7fafc'");
            notifRow.setWidgetListener("onMouseOut", "this.style.background='white'");

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
        if (userId == null) {
            userId = (String) Sessions.getCurrent().getAttribute("CTS_USER_ID");
        }

        notificationService.markAllNotificationsAsRead(role, userId);
        notificationQueue.clear();
        renderNotifications();
    }

    // Helper Model
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