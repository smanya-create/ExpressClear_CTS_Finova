package com.iispl.cts.listener;

import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.util.SessionCleanup;
import org.zkoss.zk.ui.util.SessionInit;

import com.iispl.cts.common.util.ActiveUserManager;

public class UserSession implements SessionCleanup {

	@Override
    public void cleanup(Session sess) throws Exception {
        String userId = (String) sess.getAttribute("USER_ID");
        if (userId == null) {
            userId = (String) sess.getAttribute("CTS_USER_ID");
        }
        ActiveUserManager.userLoggedOut(userId);
    }
}
