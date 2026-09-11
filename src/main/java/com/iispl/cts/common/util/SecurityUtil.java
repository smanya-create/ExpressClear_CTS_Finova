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
        if (session == null) {
            return false;
        }

        // 1. Admins bypass granular screen permission checks
        String role = (String) session.getAttribute("USER_ROLE");
        if (role != null && role.toUpperCase().contains("ADMIN")) {
            return true;
        }

        // 2. Fetch granular permissions
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
        if (session == null || page == null) {
            return;
        }

        // 1. Admin users bypass lockdown
        String role = (String) session.getAttribute("USER_ROLE");
        if (role == null) role = (String) session.getAttribute("CTS_USER_ROLE");
        if (role != null && role.toUpperCase().contains("ADMIN")) {
            return;
        }

        // 2. If clearing session is OPEN, operations remain enabled
        Boolean isOpen = (Boolean) session.getAttribute("CTS_SESSION_OPEN");
        if (Boolean.TRUE.equals(isOpen)) {
            return;
        }

        // 3. Session is CLOSED: walk entire page tree and disable mutative elements
        for (Component root : page.getRoots()) {
            disableActionControls(root);
        }
    }

    private static void disableActionControls(Component comp) {
        if (comp instanceof Button) {
            Button btn = (Button) comp;
            String id = (btn.getId() != null) ? btn.getId().toLowerCase() : "";
            // Keep navigational and view actions accessible
            if (!id.contains("logout") && !id.contains("search") && !id.contains("view") && !id.contains("refresh")) {
                btn.setDisabled(true);
            }
        } else if (comp instanceof InputElement) {
            ((InputElement) comp).setReadonly(true);
        }

        for (Component child : comp.getChildren()) {
            disableActionControls(child);
        }
    }
}