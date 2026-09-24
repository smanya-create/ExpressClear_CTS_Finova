package com.iispl.cts.daoimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.AuditLogDAO;
import com.iispl.cts.entity.AuditLog;

public class AuditLogDAOImpl implements AuditLogDAO {

    private static final Logger LOGGER = LogManager.getLogger(AuditLogDAOImpl.class);
    private static AuditLogDAOImpl instance;

    private AuditLogDAOImpl() {}

    public static synchronized AuditLogDAOImpl getInstance() {
        if (instance == null) {
            instance = new AuditLogDAOImpl();
        }
        return instance;
    }

    @Override
    public boolean insertAuditLog(AuditLog log) {
        String sql = "INSERT INTO audit_logs (timestamp, user_id, username, role_name, module, action, details, ip_address, status) "
                   + "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setTimestamp(1, log.getTimestamp() != null ? log.getTimestamp() : new Timestamp(System.currentTimeMillis()));
            ps.setString(2, log.getUserId());
            ps.setString(3, log.getUsername());
            ps.setString(4, log.getRoleName());
            ps.setString(5, log.getModule());
            ps.setString(6, log.getAction());
            ps.setString(7, log.getDetails());
            ps.setString(8, log.getIpAddress());
            ps.setString(9, log.getStatus());

            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            LOGGER.error("Failed to insert audit log into database", e);
            return false;
        }
    }

    @Override
    public List<AuditLog> searchAuditLogs(Date fromDate, Date toDate, String module, String action, String query, int offset, int limit) {
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
                    list.add(mapResultSet(rs));
                }
            }
        } catch (SQLException e) {
            LOGGER.error("Failed to query audit logs", e);
        }
        return list;
    }

    @Override
    public int countAuditLogs(Date fromDate, Date toDate, String module, String action, String query) {
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
            LOGGER.error("Failed to count audit logs", e);
        }
        return 0;
    }

    private String buildWhereClause(Date fromDate, Date toDate, String module, String action, String query, List<Object> params) {
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

    private AuditLog mapResultSet(ResultSet rs) throws SQLException {
        return new AuditLog(
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
        );
    }
}