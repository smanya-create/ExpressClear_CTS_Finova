package com.iispl.cts.common.util;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;

public class SecurityUtil {

    public static boolean hasPermission(String screenKey) {
        Session session = Sessions.getCurrent();
        if (session == null) {
            return false;
        }

        // Fetch granular permissions assigned to this user/role during login
        Object permsObj = session.getAttribute("USER_PERMISSIONS");
        if (permsObj == null || permsObj.toString().trim().isEmpty()) {
            return false;
        }

        String[] permissions = permsObj.toString().split(",");
        String targetKey = screenKey.trim().toUpperCase();

        for (String perm : permissions) {
            if (perm.trim().equalsIgnoreCase(targetKey)) {
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
}