package com.iispl.cts.common.util;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveUserManager {
	private static final Set<String> activeUsers = Collections.newSetFromMap(new ConcurrentHashMap<String, Boolean>());

    public static void userLoggedIn(String userId) {
        if (userId != null && !userId.trim().isEmpty()) {
            activeUsers.add(userId.trim());
        }
    }

    public static void userLoggedOut(String userId) {
        if (userId != null) {
            activeUsers.remove(userId.trim());
        }
    }

    public static int getActiveUserCount() {
        return Math.max(activeUsers.size(), 1);
    }

}
