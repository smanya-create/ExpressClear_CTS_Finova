package com.iispl.cts.daoimpl.outward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.MakerUnprocessedChequeDAO;
import com.iispl.cts.dto.UnprocessedChequeDTO;

public class MakerUnprocessedChequeDAOImpl implements MakerUnprocessedChequeDAO {

    @Override
    public List<UnprocessedChequeDTO> getUnprocessedCheques(String userRole) {
        List<UnprocessedChequeDTO> list = new ArrayList<>();

        String sql = "SELECT c.cheque_id, c.batch_id, b.batch_no, cs.session_name, " +
                     "c.cheque_no, (c.city_code || c.bank_code || c.branch_code) AS sort_code, " +
                     "c.amount, c.status, c.is_eod_rollover, sbr.reason_name, c.checker_remarks, c.created_at " +
                     "FROM outward_cheque c " +
                     "JOIN outward_batch b ON c.batch_id = b.batch_id " +
                     "LEFT JOIN clearing_session cs ON b.clearing_session_id = cs.session_id " +
                     "LEFT JOIN send_back_reasons sbr ON c.send_back_reason_id = sbr.reason_id " +
                     "WHERE c.is_eod_rollover = TRUE " +
                     "AND c.status IN ('RAW', 'PENDING_REPAIR', 'PENDING_DATA_ENTRY') " +
                     "ORDER BY c.created_at ASC, c.cheque_id ASC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {

            while (rs.next()) {
                UnprocessedChequeDTO dto = new UnprocessedChequeDTO();
                dto.setChequeId(rs.getLong("cheque_id"));
                dto.setBatchId(rs.getLong("batch_id"));
                dto.setBatchNo(rs.getString("batch_no"));
                dto.setOriginalSessionName(rs.getString("session_name"));
                dto.setChequeNo(rs.getString("cheque_no"));
                dto.setSortCode(rs.getString("sort_code"));
                dto.setAmount(rs.getBigDecimal("amount"));
                dto.setStatus(rs.getString("status"));
                dto.setForcedEodRollover(true);
                dto.setSendBackReason(rs.getString("reason_name"));
                dto.setRemarks(rs.getString("checker_remarks"));
                list.add(dto);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return list;
    }

    @Override
    public long countPendingRolloverItems() {
        String sql = "SELECT COUNT(*) FROM outward_cheque WHERE is_eod_rollover = TRUE AND status IN ('RAW', 'PENDING_REPAIR', 'PENDING_DATA_ENTRY')";
        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql);
             ResultSet rs = ps.executeQuery()) {
            if (rs.next()) {
                return rs.getLong(1);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
}