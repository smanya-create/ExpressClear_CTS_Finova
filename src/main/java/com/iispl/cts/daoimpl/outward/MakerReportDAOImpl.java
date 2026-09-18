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
        String sql = "SELECT " +
                     "    sb.scanned_batch_id, " +
                     "    sb.batch_reference_id, " +
                     "    COUNT(sc.scanned_cheque_id) AS total_cheques, " +
                     "    COUNT(CASE WHEN UPPER(sc.cheque_status) IN ('PENDING_MICR_REPAIR', 'PENDING_DATA_ENTRY', 'DATA_ENTRY', 'REJECTED_MICR') THEN 1 END) AS repaired_count, " +
                     "    COALESCE(SUM(CAST(sc.cheque_amount AS NUMERIC)), 0.00) AS total_amount, " +
                     "    COALESCE(sb.uploaded_at, MIN(sc.created_at)) AS uploaded_at " +
                     "FROM scan_batch sb " +
                     "JOIN scan_cheque sc ON sb.scanned_batch_id = sc.scanned_batch_id " +
                     "WHERE (sb.uploaded_by = ? OR ? = 'USR1001' OR sb.uploaded_by IS NULL) " +
                     "  AND CAST(COALESCE(sb.uploaded_at, sc.created_at) AS DATE) BETWEEN ? AND ? " +
                     "GROUP BY sb.scanned_batch_id, sb.batch_reference_id, sb.uploaded_at " +
                     "HAVING COUNT(CASE WHEN UPPER(sc.cheque_status) IN ('PENDING_MICR_REPAIR', 'PENDING_DATA_ENTRY', 'DATA_ENTRY', 'REJECTED_MICR') THEN 1 END) > 0 " +
                     "ORDER BY uploaded_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, makerId);
            ps.setString(2, makerId);
            ps.setDate(3, fromDate);
            ps.setDate(4, toDate);
            return extractResultSet(ps);
        }
    }

    @Override
    public List<Map<String, Object>> getDataEntryReport(String makerId, Date fromDate, Date toDate) throws Exception {
        String sql = "SELECT " +
                     "    sc.scanned_cheque_id, " +
                     "    sc.scanned_batch_id, " +
                     "    sb.batch_reference_id, " +
                     "    sc.cheque_number, " +
                     "    sc.micr_code, " +
                     "    sc.drawee_name, " +
                     "    sc.drawee_account_number, " +
                     "    sc.payee_name, " +
                     "    sc.payee_account_number, " +
                     "    sc.cheque_amount, " +
                     "    sc.cheque_date, " +
                     "    sc.cheque_status, " +
                     "    sc.created_at " +
                     "FROM scan_cheque sc " +
                     "JOIN scan_batch sb ON sc.scanned_batch_id = sb.scanned_batch_id " +
                     "WHERE (sb.uploaded_by = ? OR ? = 'USR1001' OR sb.uploaded_by IS NULL) " +
                     "  AND UPPER(sc.cheque_status) IN ('DATA_ENTRY', 'PENDING_DATA_ENTRY', 'PENDING_MICR_REPAIR') " +
                     "  AND CAST(COALESCE(sc.created_at, sb.uploaded_at) AS DATE) BETWEEN ? AND ? " +
                     "ORDER BY sc.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, makerId);
            ps.setString(2, makerId);
            ps.setDate(3, fromDate);
            ps.setDate(4, toDate);
            return extractResultSet(ps);
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

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, fromDate);
            ps.setDate(2, toDate);
            return extractResultSet(ps);
        }
    }

    @Override
    public List<Map<String, Object>> getSubmittedToCheckerReport(String makerId, Date fromDate, Date toDate) throws Exception {
        String sql = "SELECT outward_batch_id, batch_reference_id, actual_cheque_count, " +
                     "       actual_total_amount, uploaded_at, batch_status " +
                     "FROM outward_batch " +
                     "WHERE (uploaded_by = ? OR ? = 'USR1001' OR uploaded_by IS NULL) " +
                     "  AND UPPER(batch_status) IN ('PENDING_CHECKER_PROCESS', 'SUBMITTED', 'PENDING_CHECKER') " +
                     "  AND CAST(uploaded_at AS DATE) BETWEEN ? AND ? " +
                     "ORDER BY uploaded_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, makerId);
            ps.setString(2, makerId);
            ps.setDate(3, fromDate);
            ps.setDate(4, toDate);
            return extractResultSet(ps);
        }
    }

    private List<Map<String, Object>> extractResultSet(PreparedStatement ps) throws Exception {
        List<Map<String, Object>> list = new ArrayList<>();
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
        return list;
    }
}