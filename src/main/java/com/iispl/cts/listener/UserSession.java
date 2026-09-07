package com.iispl.cts.listener;

import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.util.DesktopCleanup;
import org.zkoss.zk.ui.util.DesktopInit;
import org.zkoss.zk.ui.util.SessionCleanup;

import com.iispl.cts.common.util.ActiveUserManager;

public class UserSession implements DesktopInit, DesktopCleanup, SessionCleanup {

    @Override
    public void init(Desktop desktop, Object request) throws Exception {
        Session sess = desktop.getSession();
        if (sess != null) {
            String userId = getUserId(sess);
            // Only register if user is authenticated and not on login page
            String requestPath = desktop.getRequestPath();
            if (userId != null && !userId.trim().isEmpty()) {
                if (requestPath == null || !requestPath.contains("login.zul")) {
                    ActiveUserManager.registerDesktop(userId, desktop.getId());
                }
            }
        }
    }

    @Override
    public void cleanup(Desktop desktop) throws Exception {
        Session sess = desktop.getSession();
        if (sess != null) {
            String userId = getUserId(sess);
            String requestPath = desktop.getRequestPath();
            // Do not unregister using the login page desktop
            if (userId != null && (requestPath == null || !requestPath.contains("login.zul"))) {
                ActiveUserManager.unregisterDesktop(userId, desktop.getId());
            }
        }
    }

    @Override
    public void cleanup(Session sess) throws Exception {
        String userId = getUserId(sess);
        if (userId != null) {
            ActiveUserManager.userLoggedOut(userId);
        }
    }

    private String getUserId(Session sess) {
        String userId = (String) sess.getAttribute("USER_ID");
        if (userId == null) {
            userId = (String) sess.getAttribute("CTS_USER_ID");
        }
        return userId;
    }
}