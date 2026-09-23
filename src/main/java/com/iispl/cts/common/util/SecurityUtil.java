package com.iispl.cts.common.util;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Page;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zul.Button;
import org.zkoss.zul.impl.InputElement;

public class SecurityUtil {

    public static boolean hasPermission(String screenKey) {
        Session session = Sessions.getCurrent();
        if (session == null) return false;

        String role = (String) session.getAttribute("USER_ROLE");
        if (role != null && role.toUpperCase().contains("ADMIN")) {
            return true;
        }

        Object permsObj = session.getAttribute("USER_PERMISSIONS");
        if (permsObj == null || permsObj.toString().trim().isEmpty()) {
            return false;
        }

        String[] permissions = permsObj.toString().split(",");
        String targetKey = screenKey.trim().toUpperCase();

        for (String perm : permissions) {
            String p = perm.trim().toUpperCase();
            if (p.equals(targetKey) || p.equals("ALL") || p.equals("*")) {
                return true;
            }
        }
        return false;
    }

    public static boolean checkAccess(String screenKey) {
        Session session = Sessions.getCurrent();
        if (session == null) {
            Executions.sendRedirect("/common/login.zul");
            return false;
        }

        Object user = session.getAttribute("LOGGED_USER");
        if (user == null) user = session.getAttribute("CTS_USER_ID");
        if (user == null) user = session.getAttribute("USER_ID");

        if (user == null) {
            Executions.sendRedirect("/common/login.zul");
            return false;
        }

        if (!hasPermission(screenKey)) {
            Executions.sendRedirect("/common/access-denied.zul");
            return false;
        }

        return true;
    }

    public static void applySessionLockdown(Page page) {
        Session session = Sessions.getCurrent();
        if (session == null || page == null) return;

        // 1. Admin users bypass lockdown
        String role = (String) session.getAttribute("USER_ROLE");
        if (role == null) role = (String) session.getAttribute("CTS_USER_ROLE");
        if (role != null && role.toUpperCase().contains("ADMIN")) {
            return;
        }

        Boolean isOpen = (Boolean) session.getAttribute("CTS_SESSION_OPEN");
        String path = (page.getRequestPath() != null) ? page.getRequestPath().toLowerCase() : "";

        // Condition A: If DB session is CLOSED, lock everything
        boolean lockAll = !Boolean.TRUE.equals(isOpen);

        // Condition B: If Outward screen and not morning window, lock
        boolean lockOutward = path.contains("/outward/") && !ClearingTimeMock.isOutwardWindow();

        // Condition C: If Inward screen and not afternoon window, lock
        boolean lockInward = path.contains("/inward/") && !ClearingTimeMock.isInwardWindow();

        if (lockAll || lockOutward || lockInward) {
            String reason = lockAll ? "Session is CLOSED by Admin." :
                           (lockOutward ? "Outward Cutoff reached (02:00 PM). Presentation window closed." :
                                          "Inward opens after Outward cutoff at 02:00 PM.");

            for (Component root : page.getRoots()) {
                disableActionControls(root, reason);
            }
        }
    }

    private static void disableActionControls(Component comp, String tooltip) {
        if (comp instanceof Button) {
            Button btn = (Button) comp;
            String id = (btn.getId() != null) ? btn.getId().toLowerCase() : "";
            String label = (btn.getLabel() != null) ? btn.getLabel().trim().toLowerCase() : "";

            boolean isSystemControl = id.contains("logout") || id.contains("search") 
                    || id.contains("view") || id.contains("refresh") || id.contains("page")
                    || label.equals("«") || label.equals("‹") || label.equals("›") || label.equals("»");

            if (!isSystemControl) {
                btn.setDisabled(true);
                btn.setTooltiptext(tooltip);
                btn.setSclass("btn-grid-action btn-grid-action-disabled");
            }
        } else if (comp instanceof InputElement) {
            ((InputElement) comp).setReadonly(true);
        }

        for (Component child : comp.getChildren()) {
            disableActionControls(child, tooltip);
        }
    }
}