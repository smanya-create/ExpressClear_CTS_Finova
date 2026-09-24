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

    // --- Window Constants ---
    // Outward Morning: 09:30 AM to 03:30 PM
    public static final LocalTime OUTWARD_START   = LocalTime.of(9, 30, 0);
    public static final LocalTime OUTWARD_CUTOFF  = LocalTime.of(15, 30, 0);

    // Inward Afternoon: 03:30 PM to 06:00 PM
    public static final LocalTime INWARD_START    = LocalTime.of(15, 30, 0);
    public static final LocalTime INWARD_CUTOFF   = LocalTime.of(18, 0, 0);

    // Backwards-compatibility alias for legacy calls
    public static final LocalTime CUTOFF_TIME     = OUTWARD_CUTOFF;

    public static String getActivePhase() {
        Session session = Sessions.getCurrent();
        WebApp app = (session != null) ? session.getWebApp() : null;

        // 1. If in-memory cache is present and start instant exists, return cached phase
        if (app != null) {
            Object cached = app.getAttribute(APP_KEY_PHASE);
            Object startObj = app.getAttribute(APP_KEY_OFFSET_START);
            if (cached instanceof String && startObj instanceof Instant) {
                return (String) cached;
            }
        }

        // 2. If cache is empty or start instant was not set, reload from DB
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

        // Base simulated start times: 09:30 for Morning, 15:30 for Afternoon
        LocalTime baseTime = "MORNING".equalsIgnoreCase(phase) 
                ? OUTWARD_START 
                : INWARD_START;

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

    /**
     * Checks if current time is within Outward clearing window (09:30 to 15:30).
     */
    public static boolean isOutwardWindow() {
        String phase = getActivePhase();
        if ("LIVE".equalsIgnoreCase(phase)) {
            LocalTime now = LocalTime.now();
            return !now.isBefore(OUTWARD_START) && now.isBefore(OUTWARD_CUTOFF);
        }
        
        if ("MORNING".equalsIgnoreCase(phase)) {
            LocalTime simTime = getCurrentTime();
            return !simTime.isBefore(OUTWARD_START) && simTime.isBefore(OUTWARD_CUTOFF);
        }
        return false;
    }

    /**
     * Checks if current time is within Inward clearing window (15:30 to 18:00).
     */
    public static boolean isInwardWindow() {
        String phase = getActivePhase();
        if ("LIVE".equalsIgnoreCase(phase)) {
            LocalTime now = LocalTime.now();
            return !now.isBefore(INWARD_START) && !now.isAfter(INWARD_CUTOFF);
        }
        
        if ("AFTERNOON".equalsIgnoreCase(phase)) {
            LocalTime simTime = getCurrentTime();
            return !simTime.isBefore(INWARD_START) && !simTime.isAfter(INWARD_CUTOFF);
        }
        return false;
    }

    public static void setPreset(String preset) {
        String phase = "LIVE";
        LocalTime baseTime = LocalTime.now();

        if ("MORNING".equalsIgnoreCase(preset)) {
            phase = "MORNING";
            baseTime = OUTWARD_START;  // 09:30:00
        } else if ("AFTERNOON".equalsIgnoreCase(preset)) {
            phase = "AFTERNOON";
            baseTime = INWARD_START;   // 15:30:00
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