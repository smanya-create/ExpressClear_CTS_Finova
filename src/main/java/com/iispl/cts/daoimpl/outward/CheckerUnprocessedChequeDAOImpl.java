package com.iispl.cts.daoimpl.outward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.CheckerUnprocessedChequeDAO;
import com.iispl.cts.dto.UnprocessedChequeDTO;
import com.iispl.cts.serviceimpl.AuditServiceImpl;

public class CheckerUnprocessedChequeDAOImpl implements CheckerUnprocessedChequeDAO {

    @Override
    public List<UnprocessedChequeDTO> getCheckerUnprocessedCheques() {
        List<UnprocessedChequeDTO> list = new ArrayList<>();

        String sql = 
                "SELECT c.outward_cheque_id, " +
                "       c.outward_batch_id, " +
                "       COALESCE(b.batch_reference_id, c.outward_batch_id) AS batch_reference_id, " +
                "       c.cheque_number, " +
                "       c.micr_code, " +
                "       c.cheque_amount, " +
                "       c.cheque_status, " +
                "       c.created_at " +
                "FROM outward_cheque c " +
                "LEFT JOIN outward_batch b ON c.outward_batch_id = b.outward_batch_id " +
                "WHERE ( " +
                "       UPPER(TRIM(c.cheque_status)) IN ('UNPROCESSED', 'UNPROCESSED_VERIFY', 'UNPROCESSED_VERIFICATION') " +
                "       OR UPPER(TRIM(c.cheque_status)) LIKE 'UNPROCESSED%' " +
                "      ) " +
                "   OR ( " +
                "       UPPER(TRIM(COALESCE(b.batch_status, ''))) IN ('UNPROCESSED', 'ROLLED_OVER', 'FORCED_EOD') " +
                "       AND UPPER(TRIM(c.cheque_status)) IN ('PENDING_VERIFICATION', 'PENDING_CHECKER_VERIFICATION') " +
                "      ) " +
                "ORDER BY c.created_at ASC, c.outward_cheque_id ASC";

            try (Connection conn = DBConnection.getConnection();
                 PreparedStatement ps = conn.prepareStatement(sql);
                 ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {
                    UnprocessedChequeDTO dto = new UnprocessedChequeDTO();
                    
                    String chqIdStr = rs.getString("outward_cheque_id");
                    try {
                        dto.setChequeId(Long.parseLong(chqIdStr.replaceAll("\\D+", "")));
                    } catch (Exception e) {
                        dto.setChequeId(0L);
                    }
                    
                    String batchIdStr = rs.getString("outward_batch_id");
                    try {
                        dto.setBatchId(Long.parseLong(batchIdStr.replaceAll("\\D+", "")));
                    } catch (Exception e) {
                        dto.setBatchId(0L);
                    }

                    String ref = rs.getString("batch_reference_id");
                    dto.setBatchNo(ref != null && !ref.isEmpty() ? ref : batchIdStr);
                    dto.setOriginalSessionName("Prior EOD Rollover");
                    dto.setChequeNo(rs.getString("cheque_number"));
                    dto.setSortCode(rs.getString("micr_code"));
                    dto.setAmount(rs.getBigDecimal("cheque_amount"));
                    dto.setStatus(rs.getString("cheque_status"));
                    dto.setRemarks("Rolled over awaiting Checker verification");
                    dto.setForcedEodRollover(true);
                    
                    list.add(dto);
                }
                System.out.println("[CTS DEBUG] Checker unprocessed cheques loaded successfully: " + list.size());
            } catch (SQLException e) {
                System.err.println("[CTS ERROR] Failed to fetch checker unprocessed cheques: " + e.getMessage());
                e.printStackTrace();
            }
            return list;
    }

    @Override
    public boolean verifyCheque(Long chequeId, String checkerUserId) {
        if (chequeId == null) return false;

        String chqIdFormatted = "CH" + chequeId;
        String rawIdStr = String.valueOf(chequeId);

        String sql = "UPDATE outward_cheque " +
                     "SET cheque_status = 'VERIFIED', verified_by = ?, verified_at = CURRENT_TIMESTAMP " +
                     "WHERE (outward_cheque_id = ? OR outward_cheque_id = ?) " +
                     "  AND (UPPER(cheque_status) IN ('PENDING_VERIFICATION', 'PENDING_CHECKER_VERIFICATION', 'UNPROCESSED', 'UNPROCESSED_VERIFY') " +
                     "       OR UPPER(cheque_status) LIKE 'UNPROCESSED%')";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            int updated;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, checkerUserId != null ? checkerUserId : "USR1001");
                ps.setString(2, chqIdFormatted);
                ps.setString(3, rawIdStr);
                updated = ps.executeUpdate();
            }

            if (updated > 0) {
                checkAndUpdateBatchCompletion(conn, chqIdFormatted, rawIdStr);
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean sendBackToMaker(Long chequeId, Long sendBackReasonId, String checkerRemarks, String checkerUserId) {
        if (chequeId == null) return false;

        String chqIdFormatted = "CH" + chequeId;
        String rawIdStr = String.valueOf(chequeId);

        String sql = "UPDATE outward_cheque " +
                     "SET cheque_status = 'PENDING_DATA_ENTRY', " +
                     "    checker_remarks = ?, " +
                     "    send_back_reason_id = ?, " +
                     "    verified_by = ? " +
                     "WHERE (outward_cheque_id = ? OR outward_cheque_id = ?) " +
                     "  AND (UPPER(cheque_status) IN ('PENDING_VERIFICATION', 'PENDING_CHECKER_VERIFICATION', 'UNPROCESSED', 'UNPROCESSED_VERIFY') " +
                     "       OR UPPER(cheque_status) LIKE 'UNPROCESSED%')";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            int updated;
            try (PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, checkerRemarks != null ? checkerRemarks : "Sent back by Checker");
                if (sendBackReasonId != null) {
                    ps.setLong(2, sendBackReasonId);
                } else {
                    ps.setNull(2, java.sql.Types.BIGINT);
                }
                ps.setString(3, checkerUserId != null ? checkerUserId : "USR1001");
                ps.setString(4, chqIdFormatted);
                ps.setString(5, rawIdStr);
                updated = ps.executeUpdate();
            }

            if (updated > 0) {
                AuditServiceImpl.getInstance().log("CHECKER_VERIFY", "SEND_BACK_TO_MAKER", 
                        "Cheque ID " + chqIdFormatted + " returned to Data Entry. Reason ID: " + sendBackReasonId + " Remarks: " + checkerRemarks, "SUCCESS");
                conn.commit();
                return true;
            } else {
                conn.rollback();
                return false;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean rejectCheque(Long chequeId, Long rejectReasonId, String rejectRemarks, String checkerUserId) {
        if (chequeId == null) return false;

        String chqIdFormatted = "CH" + chequeId;
        String rawIdStr = String.valueOf(chequeId);

        String insertRejectionSql = "INSERT INTO outward_rejected_cheques (outward_cheque_id, rejected_by, remarks, reject_reason_id, rejected_at) " +
                                    "VALUES (?, ?, ?, ?, CURRENT_TIMESTAMP)";
        String updateChequeSql = "UPDATE outward_cheque SET cheque_status = 'REJECTED' " +
                                 "WHERE (outward_cheque_id = ? OR outward_cheque_id = ?)";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psReject = conn.prepareStatement(insertRejectionSql);
                 PreparedStatement psUpdate = conn.prepareStatement(updateChequeSql)) {

                psReject.setString(1, chqIdFormatted);
                psReject.setString(2, checkerUserId != null ? checkerUserId : "USR1001");
                psReject.setString(3, rejectRemarks != null ? rejectRemarks : "Rejected during rollover review");
                if (rejectReasonId != null) {
                    psReject.setLong(4, rejectReasonId);
                } else {
                    psReject.setNull(4, java.sql.Types.BIGINT);
                }
                psReject.executeUpdate();

                psUpdate.setString(1, chqIdFormatted);
                psUpdate.setString(2, rawIdStr);
                int updated = psUpdate.executeUpdate();

                if (updated > 0) {
                    checkAndUpdateBatchCompletion(conn, chqIdFormatted, rawIdStr);
                    conn.commit();
                    return true;
                } else {
                    conn.rollback();
                    return false;
                }
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * Helper to auto-complete batch if all instruments in this batch have been decided.
     */
    private void checkAndUpdateBatchCompletion(Connection conn, String chqIdFormatted, String rawIdStr) throws SQLException {
        String findBatchSql = "SELECT outward_batch_id FROM outward_cheque WHERE outward_cheque_id = ? OR outward_cheque_id = ? LIMIT 1";
        String checkPendingSql = "SELECT COUNT(*) FROM outward_cheque " +
                                 "WHERE outward_batch_id = ? " +
                                 "  AND UPPER(cheque_status) NOT IN ('VERIFIED', 'REJECTED')";
        String updateBatchSql = "UPDATE outward_batch SET batch_status = 'COMPLETED' WHERE outward_batch_id = ?";

        String batchId = null;
        try (PreparedStatement psFind = conn.prepareStatement(findBatchSql)) {
            psFind.setString(1, chqIdFormatted);
            psFind.setString(2, rawIdStr);
            try (ResultSet rs = psFind.executeQuery()) {
                if (rs.next()) {
                    batchId = rs.getString("outward_batch_id");
                }
            }
        }

        if (batchId != null) {
            try (PreparedStatement psCheck = conn.prepareStatement(checkPendingSql)) {
                psCheck.setString(1, batchId);
                try (ResultSet rs = psCheck.executeQuery()) {
                    if (rs.next() && rs.getInt(1) == 0) {
                        try (PreparedStatement psUpdBat = conn.prepareStatement(updateBatchSql)) {
                            psUpdBat.setString(1, batchId);
                            psUpdBat.executeUpdate();
                        }
                    }
                }
            }
        }
    }
}