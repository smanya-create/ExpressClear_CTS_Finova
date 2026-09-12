package com.iispl.cts.listener;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.EventInterceptor;
import org.zkoss.zul.Button;

public class GlobalSessionLockInterceptor implements EventInterceptor {

    @Override
    public Event beforeProcessEvent(Event event) {
        if ("onClick".equals(event.getName())) {
            Component target = event.getTarget();

            if (target instanceof Button) {
                // 1. If user is ADMIN, allow full access regardless of session state
                if (isAdminUser()) {
                    return event;
                }

                // 2. Check outward session status
                Object sessionOpen = (Sessions.getCurrent() != null)
                        ? Sessions.getCurrent().getAttribute("CTS_SESSION_OPEN")
                        : null;

                // 3. If closed (false), enforce button lock for non-admin roles
                if (Boolean.FALSE.equals(sessionOpen)) {
                    Button btn = (Button) target;
                    String label = btn.getLabel() != null ? btn.getLabel().trim().toLowerCase() : "";
                    String id = btn.getId() != null ? btn.getId().toLowerCase() : "";

                    // Always allow search, reset, refresh, pagination, and logout
                    boolean isSystemControl = label.contains("search") || label.contains("reset")
                            || label.contains("refresh") || label.contains("logout") || label.contains("log out")
                            || label.equals("«") || label.equals("‹") || label.equals("›") || label.equals("»")
                            || id.contains("search") || id.contains("reset") || id.contains("refresh")
                            || id.contains("page") || id.contains("logout");

                    if (!isSystemControl) {
                        event.stopPropagation();
                        Clients.showNotification("Session is CLOSED by Admin. Actions are locked.",
                                "warning", null, "top_center", 2000);
                        return null;
                    }
                }
            }
        }
        return event;
    }

    /**
     * Inspects active session attributes to determine if current user has ADMIN role.
     */
    private boolean isAdminUser() {
        if (Sessions.getCurrent() == null) {
            return false;
        }

        // Check common CTS session role keys
        Object role = Sessions.getCurrent().getAttribute("USER_ROLE");
        if (role == null) role = Sessions.getCurrent().getAttribute("ROLE");
        if (role == null) role = Sessions.getCurrent().getAttribute("USER_TYPE");
        if (role == null) role = Sessions.getCurrent().getAttribute("ROLE_NAME");

        if (role != null) {
            String roleStr = role.toString().toUpperCase();
            if (roleStr.contains("ADMIN")) {
                return true;
            }
        }

        // Check user object or username fallback
        Object user = Sessions.getCurrent().getAttribute("LOGGED_IN_USER");
        if (user == null) user = Sessions.getCurrent().getAttribute("USER");
        if (user == null) user = Sessions.getCurrent().getAttribute("USERNAME");

        if (user != null && user.toString().toUpperCase().contains("ADMIN")) {
            return true;
        }

        return false;
    }

    @Override
    public void afterProcessEvent(Event event) {}

    @Override
    public Event beforeSendEvent(Event event) {
        return event;
    }

    @Override
    public Event beforePostEvent(Event event) {
        return event;
    }
}