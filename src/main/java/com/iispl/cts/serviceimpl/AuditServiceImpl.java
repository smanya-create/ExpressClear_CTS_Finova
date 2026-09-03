package com.iispl.cts.serviceimpl;


import java.sql.*;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;

import com.iispl.cts.entity.AuditLog;
import com.iispl.cts.service.AuditService;
import com.iispl.cts.common.config.DBConnection;

public class AuditServiceImpl implements AuditService {

    private static AuditServiceImpl instance;
    private static final Logger auditLogger = LogManager.getLogger("CTS_AUDIT_LOGGER");

    public static synchronized AuditServiceImpl getInstance() {
        if (instance == null) instance = new AuditServiceImpl();
        return instance;
    }

    @Override
    public void log(String module, String action, String details, String status) {
        // Extract session details
        String userId = "SYSTEM";
        String username = "Anonymous";
        String roleName = "N/A";
        String ipAddress = "127.0.0.1";

        try {
            if (Sessions.getCurrent() != null) {
                Object uId = Sessions.getCurrent().getAttribute("USER_ID");
                if (uId == null) uId = Sessions.getCurrent().getAttribute("CTS_USER_ID");

                Object uName = Sessions.getCurrent().getAttribute("USERNAME");
                if (uName == null) uName = Sessions.getCurrent().getAttribute("CTS_USERNAME");
                if (uName == null) uName = Sessions.getCurrent().getAttribute("LOGGED_USER");

                Object uRole = Sessions.getCurrent().getAttribute("ROLE_NAME");
                if (uRole == null) uRole = Sessions.getCurrent().getAttribute("CTS_USER_ROLE");

                if (uId != null) userId = uId.toString();
                if (uName != null) username = uName.toString();
                if (uRole != null) roleName = uRole.toString();
            }

            if (Executions.getCurrent() != null) {
                ipAddress = Executions.getCurrent().getRemoteAddr();
            }
        } catch (Exception ignored) {}

        // 1. Write to Log4j File Appender
        auditLogger.info("USER:[{}] | ROLE:[{}] | MODULE:[{}] | ACTION:[{}] | STATUS:[{}] | IP:[{}] | DETAILS:[{}]",
                userId, roleName, module, action, status, ipAddress, details);

        // 2. Persist to Database for UI display
        String sql = "INSERT INTO audit_logs (timestamp, user_id, username, role_name, module, action, details, ip_address, status) " +
                     "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setTimestamp(1, new Timestamp(System.currentTimeMillis()));
            ps.setString(2, userId);
            ps.setString(3, username);
            ps.setString(4, roleName);
            ps.setString(5, module);
            ps.setString(6, action);
            ps.setString(7, details);
            ps.setString(8, ipAddress);
            ps.setString(9, status != null ? status : "SUCCESS");
            ps.executeUpdate();
        } catch (SQLException e) {
            auditLogger.error("Failed to insert audit log into database", e);
        }
    }

   

	@Override
	public List<AuditLog> searchAuditLogs(Date fromDate, Date toDate, String module, String action, String query,
			int offset, int limit) {
		// TODO Auto-generated method stub
		List<AuditLog> list = new ArrayList<>();
        List<Object> params = new ArrayList<>();
        String whereClause = buildWhereClause(fromDate, toDate, module, action, query, params);

        String sql = "SELECT * FROM audit_logs " + whereClause + " ORDER BY timestamp DESC LIMIT ? OFFSET ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }
            ps.setInt(params.size() + 1, limit);
            ps.setInt(params.size() + 2, offset);

            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(new AuditLog(
                            rs.getLong("audit_id"),
                            rs.getTimestamp("timestamp"),
                            rs.getString("user_id"),
                            rs.getString("username"),
                            rs.getString("role_name"),
                            rs.getString("module"),
                            rs.getString("action"),
                            rs.getString("details"),
                            rs.getString("ip_address"),
                            rs.getString("status")
                    ));
                }
            }
        } catch (SQLException e) {
            auditLogger.error("Failed to query audit logs", e);
        }
        return list;
	}

	private String buildWhereClause(Date fromDate, Date toDate, String module, String action, String query,
			List<Object> params) {
		// TODO Auto-generated method stub
		StringBuilder where = new StringBuilder(" WHERE 1=1 ");

        if (fromDate != null) {
            where.append("AND timestamp >= ? ");
            params.add(new Timestamp(fromDate.getTime()));
        }
        if (toDate != null) {
            where.append("AND timestamp <= ? ");
            params.add(new Timestamp(toDate.getTime() + 86399000L));
        }
        if (module != null && !"ALL".equalsIgnoreCase(module) && !module.trim().isEmpty()) {
            where.append("AND LOWER(module) = ? ");
            params.add(module.trim().toLowerCase());
        }
        if (action != null && !"ALL".equalsIgnoreCase(action) && !action.trim().isEmpty()) {
            where.append("AND LOWER(action) = ? ");
            params.add(action.trim().toLowerCase());
        }
        if (query != null && !query.trim().isEmpty()) {
            where.append("AND (LOWER(user_id) LIKE ? OR LOWER(username) LIKE ? OR LOWER(details) LIKE ?) ");
            String q = "%" + query.trim().toLowerCase() + "%";
            params.add(q);
            params.add(q);
            params.add(q);
        }
        return where.toString();
	}

	@Override
	public int countAuditLogs(Date fromDate, Date toDate, String module, String action, String query) {
		// TODO Auto-generated method stub
		List<Object> params = new ArrayList<>();
        String whereClause = buildWhereClause(fromDate, toDate, module, action, query, params);
        String sql = "SELECT COUNT(*) FROM audit_logs " + whereClause;

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            for (int i = 0; i < params.size(); i++) {
                ps.setObject(i + 1, params.get(i));
            }

            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            auditLogger.error("Failed to count audit logs", e);
        }
		return 0;
	}
}
