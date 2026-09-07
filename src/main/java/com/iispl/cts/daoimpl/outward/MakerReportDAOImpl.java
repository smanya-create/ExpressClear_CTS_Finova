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
		// TODO Auto-generated method stub
		Map<String, List<Map<String, Object>>> reportData = new HashMap<>();

        try (Connection conn = DBConnection.getConnection()) {

            // 1. Batches with MICR repairs
            String sqlMicr = "SELECT ob.outward_batch_id, ob.batch_reference_id, " +
                             "       COUNT(oc.outward_cheque_id) AS total_cheques, " +
                             "       COUNT(CASE WHEN UPPER(oc.cheque_status) LIKE '%REPAIR%' " +
                             "                    OR UPPER(oc.cheque_status) LIKE '%MICR%' THEN 1 END) AS repaired_count, " +
                             "       COALESCE(SUM(oc.cheque_amount), 0.00) AS total_amount, " +
                             "       ob.uploaded_at " +
                             "FROM outward_batch ob " +
                             "JOIN outward_cheque oc ON ob.outward_batch_id = oc.outward_batch_id " +
                             "WHERE ob.uploaded_by = ? " +
                             "  AND CAST(ob.uploaded_at AS DATE) BETWEEN ? AND ? " +
                             "GROUP BY ob.outward_batch_id, ob.batch_reference_id, ob.uploaded_at " +
                             "HAVING COUNT(CASE WHEN UPPER(oc.cheque_status) LIKE '%REPAIR%' " +
                             "                    OR UPPER(oc.cheque_status) LIKE '%MICR%' THEN 1 END) > 0 " +
                             "ORDER BY ob.uploaded_at DESC";
            reportData.put("micrRepairs", executeQuery(conn, sqlMicr, makerId, fromDate, toDate));

            // 2. Batches pending data entry
            String sqlDataEntry = "SELECT ob.outward_batch_id, ob.batch_reference_id, " +
                                  "       COUNT(oc.outward_cheque_id) AS pending_items, " +
                                  "       ob.batch_status, ob.uploaded_at " +
                                  "FROM outward_batch ob " +
                                  "JOIN outward_cheque oc ON ob.outward_batch_id = oc.outward_batch_id " +
                                  "WHERE ob.uploaded_by = ? " +
                                  "  AND UPPER(oc.cheque_status) IN ('DATA_ENTRY', 'PENDING_DATA_ENTRY', 'KEYING_PENDING') " +
                                  "  AND CAST(ob.uploaded_at AS DATE) BETWEEN ? AND ? " +
                                  "GROUP BY ob.outward_batch_id, ob.batch_reference_id, ob.batch_status, ob.uploaded_at " +
                                  "ORDER BY ob.uploaded_at DESC";
            reportData.put("dataEntry", executeQuery(conn, sqlDataEntry, makerId, fromDate, toDate));

            // 3. Unprocessed cheques
            String sqlUnprocessed = "SELECT oc.outward_batch_id, oc.outward_cheque_id, " +
                                    "       COALESCE(oc.cheque_number, 'UNREADABLE') AS cheque_number, " +
                                    "       COALESCE(oc.micr_code, 'UNREADABLE') AS micr_code, " +
                                    "       COALESCE(oc.drawee_account_number, 'UNREADABLE') AS drawee_account_number, " +
                                    "       oc.cheque_amount, oc.cheque_status, oc.created_at " +
                                    "FROM outward_cheque oc " +
                                    "JOIN outward_batch ob ON oc.outward_batch_id = oc.outward_batch_id " +
                                    "WHERE ob.uploaded_by = ? " +
                                    "  AND UPPER(oc.cheque_status) IN ('UNPROCESSED', 'PENDING', 'OCR_FAILED', 'IMAGE_REJECTED', 'FAILED') " +
                                    "  AND CAST(ob.uploaded_at AS DATE) BETWEEN ? AND ? " +
                                    "ORDER BY oc.outward_batch_id, oc.outward_cheque_id";
            reportData.put("unprocessed", executeQuery(conn, sqlUnprocessed, makerId, fromDate, toDate));

            // 4. Batches submitted to checker
            String sqlChecker = "SELECT outward_batch_id, batch_reference_id, actual_cheque_count, " +
                                "       actual_total_amount, uploaded_at, batch_status " +
                                "FROM outward_batch " +
                                "WHERE uploaded_by = ? " +
                                "  AND UPPER(batch_status) IN ('SUBMITTED_TO_CHECKER', 'PENDING_VERIFICATION', 'SUBMITTED', 'PENDING') " +
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
