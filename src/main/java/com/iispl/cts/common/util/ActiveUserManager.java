package com.iispl.cts.common.util;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class ActiveUserManager {

    // Maps userId -> Set of active desktop/tab IDs
    private static final ConcurrentHashMap<String, Set<String>> userDesktopMap = new ConcurrentHashMap<>();

    public static void registerDesktop(String userId, String desktopId) {
        if (userId != null && !userId.trim().isEmpty() && desktopId != null) {
            String cleanUserId = userId.trim();
            userDesktopMap.computeIfAbsent(cleanUserId, k -> Collections.newSetFromMap(new ConcurrentHashMap<>()))
                          .add(desktopId);
            System.out.println(">>> [ActiveUserManager] Registered Desktop: " + desktopId + " for User: " + cleanUserId 
                               + " | Total Active Users: " + userDesktopMap.size());
        }
    }

    public static void unregisterDesktop(String userId, String desktopId) {
        if (userId != null && desktopId != null) {
            String cleanUserId = userId.trim();
            Set<String> desktops = userDesktopMap.get(cleanUserId);
            if (desktops != null) {
                desktops.remove(desktopId);
                // If user has no more open tabs/windows, remove them completely
                if (desktops.isEmpty()) {
                    userDesktopMap.remove(cleanUserId);
                    System.out.println(">>> [ActiveUserManager] User Closed Browser: " + cleanUserId 
                                       + " | Remaining Active Users: " + userDesktopMap.size());
                }
            }
        }
    }

    // Called on explicit button Logout
    public static void userLoggedOut(String userId) {
        if (userId != null) {
            userDesktopMap.remove(userId.trim());
            System.out.println(">>> [ActiveUserManager] Explicit Logout: " + userId 
                               + " | Remaining Active Users: " + userDesktopMap.size());
        }
    }

    public static int getActiveUserCount() {
        return userDesktopMap.size();
    }
}