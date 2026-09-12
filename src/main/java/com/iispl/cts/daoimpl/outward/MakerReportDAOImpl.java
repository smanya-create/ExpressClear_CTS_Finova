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
    public Map<String, List<Map<String, Object>>> getMakerReportData(String makerId, Date fromDate, Date toDate)
            throws Exception {
        Map<String, List<Map<String, Object>>> reportData = new HashMap<>();

        try (Connection conn = DBConnection.getConnection()) {

            // 1. Batches with MICR repairs (Added outward_batch_id alias)
            String sqlMicr = "SELECT sb.scanned_batch_id, sb.scanned_batch_id AS outward_batch_id, sb.batch_reference_id, " +
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
            reportData.put("micrRepairs", executeQuery(conn, sqlMicr, makerId, fromDate, toDate));

            // 2. Batches pending data entry (Added outward_batch_id alias)
            String sqlDataEntry = "SELECT sb.scanned_batch_id, sb.scanned_batch_id AS outward_batch_id, sb.batch_reference_id, " +
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
            reportData.put("dataEntry", executeQuery(conn, sqlDataEntry, makerId, fromDate, toDate));

            // 3. Unprocessed cheques audit (Added outward_batch_id & outward_cheque_id aliases)
            String sqlUnprocessed = "SELECT sc.scanned_batch_id, sc.scanned_batch_id AS outward_batch_id, " +
                                    "       sc.scanned_cheque_id, sc.scanned_cheque_id AS outward_cheque_id, " +
                                    "       COALESCE(sc.cheque_number, 'UNREADABLE') AS cheque_number, " +
                                    "       COALESCE(sc.micr_code, 'UNREADABLE') AS micr_code, " +
                                    "       COALESCE(sc.drawee_account_number, 'UNREADABLE') AS drawee_account_number, " +
                                    "       sc.cheque_amount, sc.cheque_status, sc.created_at " +
                                    "FROM scan_cheque sc " +
                                    "JOIN scan_batch sb ON sc.scanned_batch_id = sb.scanned_batch_id " +
                                    "WHERE sb.uploaded_by = ? " +
                                    "  AND UPPER(sc.cheque_status) IN ('PENDING_MICR_REPAIR', 'UNPROCESSED', 'RAW', 'OCR_FAILED', 'IMAGE_REJECTED') " +
                                    "  AND CAST(sb.uploaded_at AS DATE) BETWEEN ? AND ? " +
                                    "ORDER BY sc.scanned_batch_id, sc.scanned_cheque_id";
            reportData.put("unprocessed", executeQuery(conn, sqlUnprocessed, makerId, fromDate, toDate));

            // 4. Batches submitted to checker
            String sqlChecker = "SELECT outward_batch_id, batch_reference_id, actual_cheque_count, " +
                                "       actual_total_amount, uploaded_at, batch_status " +
                                "FROM outward_batch " +
                                "WHERE uploaded_by = ? " +
                                "  AND UPPER(batch_status) = 'PENDING_CHECKER_PROCESS' " +
                                "  AND CAST(uploaded_at AS DATE) BETWEEN ? AND ? " +
                                "ORDER BY uploaded_at DESC";
            reportData.put("submittedToChecker", executeQuery(conn, sqlChecker, makerId, fromDate, toDate));
        }

        return reportData;
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