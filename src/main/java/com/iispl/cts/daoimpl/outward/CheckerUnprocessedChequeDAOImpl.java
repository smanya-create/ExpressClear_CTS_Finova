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

public class CheckerUnprocessedChequeDAOImpl implements CheckerUnprocessedChequeDAO {

	@Override
    public List<UnprocessedChequeDTO> getCheckerUnprocessedCheques() {
        List<UnprocessedChequeDTO> list = new ArrayList<>();

        String sql = "SELECT c.outward_cheque_id, c.outward_batch_id, b.batch_reference_id, " +
                     "       c.cheque_number, c.micr_code, c.cheque_amount, c.cheque_status, c.created_at " +
                     "FROM outward_cheque c " +
                     "INNER JOIN outward_batch b ON TRIM(c.outward_batch_id) = TRIM(b.outward_batch_id) " +
                     "WHERE ( " +
                     // Condition A: Cheque itself explicitly flagged as UNPROCESSED / ROLLED_OVER
                     "       UPPER(c.cheque_status) IN ('UNPROCESSED', 'UNPROCESSED_VERIFICATION','UNPROCESSED_VERIFY') " +
                     "       OR UPPER(c.cheque_status) LIKE 'UNPROCESSED%' " +
                     "      ) " +
                     "   OR ( " +
                     // Condition B: The parent batch was closed under Forced EOD / Rolled over
                     "       UPPER(b.batch_status) IN ('UNPROCESSED', 'ROLLED_OVER', 'FORCED_EOD') " +
                     "       AND UPPER(c.cheque_status) IN ('UNPROCESSED', 'UNPROCESSED_VERIFY', 'PENDING_VERIFICATION', 'PENDING_CHECKER_VERIFICATION') " +
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

                dto.setBatchNo(rs.getString("batch_reference_id") != null ? rs.getString("batch_reference_id") : batchIdStr);
                dto.setOriginalSessionName("Prior EOD Rollover");
                dto.setChequeNo(rs.getString("cheque_number"));
                dto.setSortCode(rs.getString("micr_code"));
                dto.setAmount(rs.getBigDecimal("cheque_amount"));
                dto.setStatus(rs.getString("cheque_status"));
                dto.setRemarks(chqIdStr + " (" + batchIdStr + ")");
                dto.setForcedEodRollover(true);
                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public boolean verifyCheque(Long chequeId, String checkerUserId) {
        String chqIdFormatted = "CH" + chequeId;
        String sql = "UPDATE outward_cheque " +
                     "SET cheque_status = 'VERIFIED' " +
                     "WHERE outward_cheque_id = ? " +
                     "  AND (UPPER(cheque_status) IN ('PENDING_VERIFICATION', 'PENDING_CHECKER_VERIFICATION', 'UNPROCESSED', 'UNPROCESSED_VERIFY') " +
                     "       OR UPPER(cheque_status) LIKE 'UNPROCESSED%')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, chqIdFormatted);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean sendBackToMaker(Long chequeId, Long sendBackReasonId, String checkerRemarks, String checkerUserId) {
        String chqIdFormatted = "CH" + chequeId;
        String sql = "UPDATE outward_cheque " +
                     "SET cheque_status = 'PENDING_DATA_ENTRY' " +
                     "WHERE outward_cheque_id = ? " +
                     "  AND (UPPER(cheque_status) IN ('PENDING_VERIFICATION', 'PENDING_CHECKER_VERIFICATION', 'UNPROCESSED', 'UNPROCESSED_VERIFY') " +
                     "       OR UPPER(cheque_status) LIKE 'UNPROCESSED%')";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, chqIdFormatted);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean rejectCheque(Long chequeId, Long rejectReasonId, String rejectRemarks, String checkerUserId) {
        String chqIdFormatted = "CH" + chequeId;
        
        String insertRejectionSql = "INSERT INTO outward_rejected_cheques (outward_cheque_id, rejected_by, remarks) VALUES (?, ?, ?)";
        String updateChequeSql = "UPDATE outward_cheque SET cheque_status = 'REJECTED' WHERE outward_cheque_id = ?";

        try (Connection conn = DBConnection.getConnection()) {
            conn.setAutoCommit(false);

            try (PreparedStatement psReject = conn.prepareStatement(insertRejectionSql);
                 PreparedStatement psUpdate = conn.prepareStatement(updateChequeSql)) {

                psReject.setString(1, chqIdFormatted);
                psReject.setString(2, checkerUserId != null ? checkerUserId : "USR1001");
                psReject.setString(3, rejectRemarks != null ? rejectRemarks : "Rejected during rollover review");
                psReject.executeUpdate();

                psUpdate.setString(1, chqIdFormatted);
                psUpdate.executeUpdate();

                conn.commit();
                return true;
            } catch (SQLException e) {
                conn.rollback();
                throw e;
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return false;
        }
    }
}