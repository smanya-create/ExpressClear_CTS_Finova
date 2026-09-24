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
	        String requestPath = desktop.getRequestPath();
	        if (userId != null && !userId.trim().isEmpty()) {
	            if (requestPath == null || !requestPath.contains("login.zul")) {
	                // Attach directly to the desktop instance
	                desktop.setAttribute("CTS_DESKTOP_USER_ID", userId.trim());
	                ActiveUserManager.registerDesktop(userId.trim(), desktop.getId());
	            }
	        }
	    }
	}

	@Override
	public void cleanup(Desktop desktop) throws Exception {
	    if (desktop == null) return;

	    // First check the desktop's own attribute map
	    String userId = (String) desktop.getAttribute("CTS_DESKTOP_USER_ID");
	    
	    // Fallback if not found on desktop
	    if (userId == null) {
	        try {
	            Session sess = desktop.getSession();
	            if (sess != null) {
	                userId = getUserId(sess);
	            }
	        } catch (Exception ignored) {}
	    }

	    String requestPath = desktop.getRequestPath();
	    if (userId != null && (requestPath == null || !requestPath.contains("login.zul"))) {
	        ActiveUserManager.unregisterDesktop(userId.trim(), desktop.getId());
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