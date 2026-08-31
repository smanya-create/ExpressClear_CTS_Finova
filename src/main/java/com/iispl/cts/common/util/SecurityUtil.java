package com.iispl.cts.common.util;



import java.util.Arrays;
import java.util.List;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;

public class SecurityUtil {

    public static boolean hasPermission(String screenKey) {
        Object roleObj = Sessions.getCurrent().getAttribute("CTS_USER_ROLE");
        String role = roleObj != null ? roleObj.toString().toUpperCase() : "";

        // Admin has full bypass access
        if (role.contains("ADMIN")) {
            return true;
        }

        Object permsObj = Sessions.getCurrent().getAttribute("USER_PERMISSIONS");
        if (permsObj == null || permsObj.toString().trim().isEmpty()) {
            return false;
        }

        List<String> permissions = Arrays.asList(permsObj.toString().split(","));
        return permissions.contains(screenKey.trim().toUpperCase());
    }

    public static void checkAccess(String screenKey) {
        Object user = Sessions.getCurrent().getAttribute("LOGGED_USER");
        if (user == null) {
            Executions.sendRedirect("/login.zul");
            return;
        }

        if (!hasPermission(screenKey)) {
            Executions.sendRedirect("/access-denied.zul");
        }
    }
}
