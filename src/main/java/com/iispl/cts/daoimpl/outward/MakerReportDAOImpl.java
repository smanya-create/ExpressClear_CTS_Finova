package com.iispl.cts.daoimpl.outward;

import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.MakerReportDAO;

public class MakerReportDAOImpl implements MakerReportDAO {

    @Override
    public List<Map<String, Object>> getMicrRepairsReport(String makerId, Date fromDate, Date toDate) throws Exception {
        String sql = "SELECT sb.scanned_batch_id, sb.batch_reference_id, " +
                     "       COUNT(sc.scanned_cheque_id) AS total_cheques, " +
                     "       COUNT(CASE WHEN UPPER(sc.cheque_status) IN ('PENDING_DATA_ENTRY', 'REJECTED_MICR') THEN 1 END) AS repaired_count, " +
                     "       COALESCE(SUM(sc.cheque_amount), 0.00) AS total_amount, " +
                     "       sb.uploaded_at " +
                     "FROM scan_batch sb " +
                     "JOIN scan_cheque sc ON sb.scanned_batch_id = sc.scanned_batch_id " +
                     "WHERE sb.uploaded_by = ? " +
                     "  AND CAST(sb.uploaded_at AS DATE) BETWEEN ? AND ? " +
                     "GROUP BY sb.scanned_batch_id, sb.batch_reference_id, sb.uploaded_at " +
                     "HAVING COUNT(CASE WHEN UPPER(sc.cheque_status) IN ('PENDING_DATA_ENTRY', 'REJECTED_MICR') THEN 1 END) > 0 " +
                     "ORDER BY sb.uploaded_at DESC";
        try (Connection conn = DBConnection.getConnection()) {
            return executeQuery(conn, sql, makerId, fromDate, toDate);
        }
    }

    @Override
    public List<Map<String, Object>> getDataEntryReport(String makerId, Date fromDate, Date toDate) throws Exception {
        String sql = "SELECT sb.scanned_batch_id, sb.batch_reference_id, " +
                     "       COUNT(sc.scanned_cheque_id) AS pending_items, " +
                     "       sb.batch_status, sb.uploaded_at " +
                     "FROM scan_batch sb " +
                     "JOIN scan_cheque sc ON sb.scanned_batch_id = sc.scanned_batch_id " +
                     "WHERE sb.uploaded_by = ? " +
                     "  AND UPPER(sb.batch_status) = 'PENDING_MAKER_PROCESS' " +
                     "  AND UPPER(sc.cheque_status) IN ('DATA_ENTRY', 'PENDING_DATA_ENTRY', 'REJECTED_MICR') " +
                     "  AND CAST(sb.uploaded_at AS DATE) BETWEEN ? AND ? " +
                     "GROUP BY sb.scanned_batch_id, sb.batch_reference_id, sb.batch_status, sb.uploaded_at " +
                     "ORDER BY sb.uploaded_at DESC";
        try (Connection conn = DBConnection.getConnection()) {
            return executeQuery(conn, sql, makerId, fromDate, toDate);
        }
    }

    @Override
    public List<Map<String, Object>> getRequestRejectedChequesReport(String makerId, Date fromDate, Date toDate) throws Exception {
        String sql = "SELECT r.request_id, r.cheque_id, r.batch_id, r.remarks, r.reason, r.time_stamp, " +
                     "       COALESCE(sc.cheque_number, oc.cheque_number, r.cheque_id) AS cheque_number " +
                     "FROM outward_cheque_request r " +
                     "LEFT JOIN scan_cheque sc ON (r.cheque_id = sc.scanned_cheque_id) " +
                     "LEFT JOIN outward_cheque oc ON (r.cheque_id = oc.outward_cheque_id) " +
                     "WHERE CAST(r.time_stamp AS DATE) BETWEEN ? AND ? " +
                     "ORDER BY r.time_stamp DESC";

        List<Map<String, Object>> list = new ArrayList<>();
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {

            ps.setDate(1, fromDate);
            ps.setDate(2, toDate);

            try (ResultSet rs = ps.executeQuery()) {
                int cols = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= cols; i++) {
                        row.put(rs.getMetaData().getColumnLabel(i).toLowerCase(), rs.getObject(i));
                    }
                    list.add(row);
                }
            }
        }
        return list;
    }
    @Override
    public List<Map<String, Object>> getSubmittedToCheckerReport(String makerId, Date fromDate, Date toDate) throws Exception {
        String sql = "SELECT outward_batch_id, batch_reference_id, actual_cheque_count, " +
                     "       actual_total_amount, uploaded_at, batch_status " +
                     "FROM outward_batch " +
                     "WHERE uploaded_by = ? " +
                     "  AND UPPER(batch_status) = 'PENDING_CHECKER_PROCESS' " +
                     "  AND CAST(uploaded_at AS DATE) BETWEEN ? AND ? " +
                     "ORDER BY uploaded_at DESC";
        try (Connection conn = DBConnection.getConnection()) {
            return executeQuery(conn, sql, makerId, fromDate, toDate);
        }
    }

    private List<Map<String, Object>> executeQuery(Connection conn, String sql, String makerId, Date fromDate, Date toDate) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, makerId);
            ps.setDate(2, fromDate);
            ps.setDate(3, toDate);
            try (ResultSet rs = ps.executeQuery()) {
                int cols = rs.getMetaData().getColumnCount();
                while (rs.next()) {
                    Map<String, Object> row = new HashMap<>();
                    for (int i = 1; i <= cols; i++) {
                        row.put(rs.getMetaData().getColumnLabel(i).toLowerCase(), rs.getObject(i));
                    }
                    list.add(row);
                }
            }
        }
        return list;
    }
}