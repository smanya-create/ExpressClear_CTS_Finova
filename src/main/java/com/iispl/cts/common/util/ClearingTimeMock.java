package com.iispl.cts.common.util;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;

import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.WebApp;

import com.iispl.cts.common.config.DBConnection;

public class ClearingTimeMock {

    public static final String APP_KEY_PHASE = "CTS_GLOBAL_CYCLE_PHASE";
    public static final String APP_KEY_OFFSET_START = "CTS_GLOBAL_MOCK_START_INSTANT";
    public static final LocalTime CUTOFF_TIME = LocalTime.of(14, 0); // 02:00 PM Cutoff

    public static String getActivePhase() {
        Session session = Sessions.getCurrent();
        WebApp app = (session != null) ? session.getWebApp() : null;

        // If in-memory cache is present and start instant exists, return it
        if (app != null) {
            Object cached = app.getAttribute(APP_KEY_PHASE);
            Object startObj = app.getAttribute(APP_KEY_OFFSET_START);
            if (cached instanceof String && startObj instanceof Instant) {
                return (String) cached;
            }
        }

        // If cache is empty or start instant was not set, reload from DB
        String dbPhase = loadCycleFromDatabase();
        if (dbPhase == null) {
            dbPhase = "LIVE";
        }

        if (app != null) {
            app.setAttribute(APP_KEY_PHASE, dbPhase);
            app.setAttribute(APP_KEY_OFFSET_START, Instant.now());
        }

        return dbPhase;
    }

    public static LocalTime getCurrentTime() {
        String phase = getActivePhase();

        if ("LIVE".equalsIgnoreCase(phase)) {
            return LocalTime.now();
        }

        LocalTime baseTime = "MORNING".equalsIgnoreCase(phase) 
                ? LocalTime.of(10, 30, 0) 
                : LocalTime.of(15, 30, 0);

        Session session = Sessions.getCurrent();
        if (session != null && session.getWebApp() != null) {
            WebApp app = session.getWebApp();
            Object startObj = app.getAttribute(APP_KEY_OFFSET_START);
            if (startObj instanceof Instant) {
                long elapsedSeconds = Duration.between((Instant) startObj, Instant.now()).getSeconds();
                return baseTime.plusSeconds(elapsedSeconds);
            }
        }

        return baseTime;
    }

    public static boolean isOutwardWindow() {
        String phase = getActivePhase();
        return "LIVE".equalsIgnoreCase(phase) || "MORNING".equalsIgnoreCase(phase);
    }

    public static boolean isInwardWindow() {
        String phase = getActivePhase();
        return "LIVE".equalsIgnoreCase(phase) || "AFTERNOON".equalsIgnoreCase(phase);
    }

    public static void setPreset(String preset) {
        String phase = "LIVE";
        LocalTime baseTime = LocalTime.now();

        if ("MORNING".equalsIgnoreCase(preset)) {
            phase = "MORNING";
            baseTime = LocalTime.of(10, 30, 0);
        } else if ("AFTERNOON".equalsIgnoreCase(preset)) {
            phase = "AFTERNOON";
            baseTime = LocalTime.of(15, 30, 0);
        }

        Session session = Sessions.getCurrent();
        if (session != null && session.getWebApp() != null) {
            WebApp app = session.getWebApp();
            app.setAttribute(APP_KEY_PHASE, phase);
            app.setAttribute(APP_KEY_OFFSET_START, Instant.now());
        }

        persistCyclePhaseToDatabase(phase, java.sql.Time.valueOf(baseTime));
    }

    private static String loadCycleFromDatabase() {
        // Query the latest active open session first, or the most recent session
        String sql = "SELECT cycle_phase FROM clearing_session "
                   + "ORDER BY clearing_date DESC, opened_at DESC "
                   + "LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            if (rs.next()) {
                String phase = rs.getString("cycle_phase");
                if (phase != null && !phase.trim().isEmpty()) {
                    return phase.trim().toUpperCase();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "LIVE";
    }

    private static void persistCyclePhaseToDatabase(String phase, java.sql.Time sessionTime) {
        String sql = "UPDATE clearing_session "
                   + "SET cycle_phase = ?, session_time = ? "
                   + "WHERE clearing_date = (SELECT MAX(clearing_date) FROM clearing_session)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setString(1, phase);
            ps.setTime(2, sessionTime);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static java.sql.Timestamp getProcessingTimestamp() {
        LocalDate clearingDate = null;
        
        if (Sessions.getCurrent() != null) {
            clearingDate = (LocalDate) Sessions.getCurrent().getAttribute("CTS_CLEARING_DATE");
        }
        
        if (clearingDate == null) {
            clearingDate = LocalDate.now();
        }
        
        java.time.LocalDateTime ldt = java.time.LocalDateTime.of(clearingDate, getCurrentTime());
        return java.sql.Timestamp.valueOf(ldt);
    }
}