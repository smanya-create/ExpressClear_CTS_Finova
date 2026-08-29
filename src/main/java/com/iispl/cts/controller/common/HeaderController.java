package com.iispl.cts.controller.common;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;

public class HeaderController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    private Label lblHeaderSessionDate;
    private Label lblHeaderUsername;
    private Label lblHeaderRole;
    private Label lblUserInitial;
    private Label lblHeaderClock;
    private Label lblHeaderSessionStatus;
    private Label lblUnreadBadge;
    private Div divNotificationBell;
    private Component containerNotificationList;

    private static final SimpleDateFormat DATE_FORMATTER = new SimpleDateFormat("dd-MMM-yyyy");
    private static final SimpleDateFormat TIME_FORMATTER = new SimpleDateFormat("HH:mm:ss");

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        String username = (String) Sessions.getCurrent().getAttribute("LOGGED_USER");
        String role = (String) Sessions.getCurrent().getAttribute("USER_ROLE");

        if (username == null || username.trim().isEmpty()) username = "User";
        if (role == null || role.trim().isEmpty()) role = "OUTWARD_MAKER";

        if (lblHeaderUsername != null) lblHeaderUsername.setValue(username);
        if (lblHeaderRole != null) lblHeaderRole.setValue(formatRoleName(role));
        if (lblUserInitial != null) lblUserInitial.setValue(getRoleInitial(role));
        if (lblHeaderClock != null) lblHeaderClock.setValue(TIME_FORMATTER.format(new Date()));

        syncGlobalSessionData();
        loadRoleNotifications(role);
    }

    public void onTickClock() {
        if (lblHeaderClock != null) {
            lblHeaderClock.setValue(TIME_FORMATTER.format(new Date()));
        }
        syncGlobalSessionData();
    }

    private void loadRoleNotifications(String role) {
        // Show notification bell for all Maker and Checker operational roles
        boolean isMakerOrChecker = "OUTWARD_MAKER".equalsIgnoreCase(role) || 
                                   "OUTWARD_CHECKER".equalsIgnoreCase(role) ||
                                   "INWARD_MAKER".equalsIgnoreCase(role) || 
                                   "INWARD_CHECKER".equalsIgnoreCase(role);

        if (divNotificationBell != null) {
            divNotificationBell.setVisible(isMakerOrChecker);
        }

        if (!isMakerOrChecker || containerNotificationList == null) {
            return;
        }

        containerNotificationList.getChildren().clear();
        List<String[]> notifications = new ArrayList<>();

        if ("OUTWARD_MAKER".equalsIgnoreCase(role)) {
            notifications.add(new String[]{"Batch BAT-2026-0828-01 Uploaded", "10 mins ago", "📥"});
            notifications.add(new String[]{"3 MICR repair items remaining", "25 mins ago", "⚠️"});
        } else if ("OUTWARD_CHECKER".equalsIgnoreCase(role)) {
            notifications.add(new String[]{"Batch BAT-2026-0828-01 pending verification", "5 mins ago", "🔍"});
            notifications.add(new String[]{"Batch BAT-2026-0828-02 submitted by Maker", "15 mins ago", "📋"});
        } else if ("INWARD_MAKER".equalsIgnoreCase(role)) {
            notifications.add(new String[]{"Inward File INW-2026-0829-01 Received", "2 mins ago", "📩"});
            notifications.add(new String[]{"5 Inward cheques pending MICR Repair", "12 mins ago", "⚠️"});
        } else if ("INWARD_CHECKER".equalsIgnoreCase(role)) {
            notifications.add(new String[]{"Inward Batch INW-2026-0829-01 ready for Verification", "8 mins ago", "🔎"});
            notifications.add(new String[]{"2 Return Request Files (RRF) pending authorization", "20 mins ago", "📄"});
        }

        if (lblUnreadBadge != null) {
            lblUnreadBadge.setValue(String.valueOf(notifications.size()));
            lblUnreadBadge.setVisible(!notifications.isEmpty());
        }

        for (String[] notif : notifications) {
            Div item = new Div();
            item.setStyle("padding: 10px 14px; border-bottom: 1px solid #edf2f7; display: flex; align-items: flex-start; gap: 10px; cursor: pointer;");

            Label icon = new Label(notif[2]);
            icon.setStyle("font-size: 16px;");

            Div content = new Div();
            content.setStyle("display: flex; flex-direction: column;");

            Label msg = new Label(notif[0]);
            msg.setStyle("font-size: 12px; font-weight: 500; color: #2d3748; display: block;");

            Label time = new Label(notif[1]);
            time.setStyle("font-size: 10px; color: #a0aec0; display: block; margin-top: 2px;");

            content.appendChild(msg);
            content.appendChild(time);

            item.appendChild(icon);
            item.appendChild(content);

            containerNotificationList.appendChild(item);
        }
    }

    public void onClickMarkAllRead() {
        if (lblUnreadBadge != null) {
            lblUnreadBadge.setVisible(false);
        }
        if (containerNotificationList != null) {
            containerNotificationList.getChildren().clear();

            Div empty = new Div();
            empty.setStyle("padding: 20px; text-align: center;");
            Label lbl = new Label("No new notifications.");
            lbl.setStyle("font-size: 12px; color: #a0aec0;");
            empty.appendChild(lbl);

            containerNotificationList.appendChild(empty);
        }
    }

    private void syncGlobalSessionData() {
        String globalStatus = (String) Executions.getCurrent().getDesktop().getWebApp().getAttribute("GLOBAL_SESSION_STATUS");
        if (globalStatus == null) {
            globalStatus = (String) Sessions.getCurrent().getAttribute("SESSION_STATUS");
        }
        if (globalStatus == null) {
            globalStatus = "OPEN";
        }

        if (lblHeaderSessionStatus != null) {
            if ("CLOSED".equalsIgnoreCase(globalStatus)) {
                lblHeaderSessionStatus.setValue("● CLOSED");
                lblHeaderSessionStatus.setStyle("font-size: 11px; font-weight: bold; color: #e53e3e; background: #fed7d7; padding: 3px 10px; border-radius: 12px;");
            } else {
                lblHeaderSessionStatus.setValue("● OPEN");
                lblHeaderSessionStatus.setStyle("font-size: 11px; font-weight: bold; color: #38a169; background: #c6f6d5; padding: 3px 10px; border-radius: 12px;");
            }
        }

        String globalDate = (String) Executions.getCurrent().getDesktop().getWebApp().getAttribute("GLOBAL_CLEARING_DATE");
        if (globalDate == null) {
            globalDate = (String) Sessions.getCurrent().getAttribute("CLEARING_DATE");
        }
        if (globalDate == null) {
            globalDate = DATE_FORMATTER.format(new Date());
        }

        if (lblHeaderSessionDate != null) {
            lblHeaderSessionDate.setValue(globalDate);
        }
    }

    private String getRoleInitial(String role) {
        if (role == null || role.isEmpty()) return "U";
        if (role.toUpperCase().contains("ADMIN")) return "A";
        if (role.toUpperCase().contains("CHECKER")) return "C";
        if (role.toUpperCase().contains("MAKER")) return "M";
        return role.substring(0, 1).toUpperCase();
    }

    private String formatRoleName(String role) {
        if (role == null) return "User";
        switch (role.toUpperCase()) {
            case "ADMIN": return "Administrator";
            case "OUTWARD_MAKER": return "Outward Maker";
            case "OUTWARD_CHECKER": return "Outward Checker";
            case "INWARD_MAKER": return "Inward Maker";
            case "INWARD_CHECKER": return "Inward Checker";
            default: return role;
        }
    }
}