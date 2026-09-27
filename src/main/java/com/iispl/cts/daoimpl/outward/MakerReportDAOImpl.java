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
                     "    sc.scanned_cheque_id, " +
                     "    sc.cheque_number, " +
                     "    sc.micr_code, " +
                     "    sc.drawee_name, " +
                     "    sc.drawee_account_number, " +
                     "    sc.payee_name, " +
                     "    sc.payee_account_number, " +
                     "    sc.cheque_amount, " +
                     "    sc.cheque_date, " +
                     "    sc.cheque_status, " +
                     "    sc.created_at, " +
                     "    sb.uploaded_at " +
                     "FROM scan_batch sb " +
                     "JOIN scan_cheque sc ON sb.scanned_batch_id = sc.scanned_batch_id " +
                     "WHERE (UPPER(sc.cheque_status) LIKE '%MICR%' " +
                     "    OR UPPER(sc.cheque_status) IN ('PENDING_MICR_REPAIR', 'MICR_REPAIRED', 'MICR_REJECTION_PENDING', 'REJECTED_MICR')) " +
                     "  AND CAST(GREATEST(sb.uploaded_at, sc.created_at) AS DATE) BETWEEN ? AND ? " +
                     "ORDER BY sb.scanned_batch_id ASC, sc.created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, fromDate);
            ps.setDate(2, toDate);
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
                     "  AND UPPER(sc.cheque_status) IN ('DATA_ENTRY', 'PENDING_DATA_ENTRY') " +
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
        String sql = "SELECT " +
                     "    rc.outward_rejected_cheque_id AS request_id, " +
                     "    rc.outward_cheque_id AS cheque_id, " +
                     "    rc.outward_batch_id AS batch_id, " +
                     "    rc.remarks, " +
                     "    rc.reason, " +
                     "    rc.rejected_date AS time_stamp, " +
                     "    rc.cheque_amount, " +
                     "    rc.rejected_by, " +
                     "    COALESCE(oc.cheque_number, sc.cheque_number, rc.outward_cheque_id) AS cheque_number " +
                     "FROM outward_rejected_cheques rc " +
                     "LEFT JOIN outward_cheque oc ON rc.outward_cheque_id = oc.outward_cheque_id " +
                     "LEFT JOIN scan_cheque sc ON rc.outward_cheque_id = sc.scanned_cheque_id " +
                     "WHERE CAST(rc.rejected_date AS DATE) BETWEEN ? AND ? " +
                     "ORDER BY rc.rejected_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, fromDate);
            ps.setDate(2, toDate);
            return extractResultSet(ps);
        }
    }
    @Override
    public List<Map<String, Object>> getSubmittedToCheckerReport(String makerId, Date fromDate, Date toDate) throws Exception {
        String sql = "SELECT " +
                     "    sb.scanned_batch_id, " +
                     "    sb.batch_reference_id, " +
                     "    sb.actual_cheque_count, " +
                     "    sb.actual_total_amount, " +
                     "    sb.batch_status, " +
                     "    sb.uploaded_at, " +
                     "    u.full_name AS uploaded_by_name " +
                     "FROM scan_batch sb " +
                     "LEFT JOIN users u ON sb.uploaded_by = u.user_id " +
                     "LEFT JOIN scan_cheque sc ON sb.scanned_batch_id = sc.scanned_batch_id " +
                     "WHERE (UPPER(sb.batch_status) LIKE '%CHECKER%' " +
                     "    OR UPPER(sb.batch_status) LIKE '%SUBMIT%') " +
                     "  AND CAST(GREATEST(sb.uploaded_at, COALESCE(sc.created_at, sb.uploaded_at)) AS DATE) BETWEEN ? AND ? " +
                     "GROUP BY sb.scanned_batch_id, sb.batch_reference_id, sb.actual_cheque_count, " +
                     "         sb.actual_total_amount, sb.batch_status, sb.uploaded_at, u.full_name " +
                     "ORDER BY sb.uploaded_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, fromDate);
            ps.setDate(2, toDate);
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