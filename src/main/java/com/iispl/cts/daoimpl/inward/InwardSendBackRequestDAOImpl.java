package com.iispl.cts.daoimpl.inward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.HashMap;
import java.util.Map;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.inward.InwardSendBackRequestDAO;
import com.iispl.cts.dto.InwardSendBackRequestDTO;

public class InwardSendBackRequestDAOImpl implements InwardSendBackRequestDAO {

    @Override
    public Map<String, InwardSendBackRequestDTO> getPendingRequestsByBatchId(String batchId) {
        Map<String, InwardSendBackRequestDTO> map = new HashMap<>();
        String sql = "SELECT s.*, r.reason_name, r.reason_code "
                   + "FROM inward_cheque_send_back_request s "
                   + "LEFT JOIN send_back_reason r ON s.reason_id = r.reason_id "
                   + "WHERE s.inward_batch_id = ? AND s.request_status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, batchId);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    InwardSendBackRequestDTO dto = mapResultSet(rs);
                    map.put(dto.getInwardChequeId(), dto);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return map;
    }

    @Override
    public InwardSendBackRequestDTO getLatestPendingByChequeId(String inwardChequeId) {
        String sql = "SELECT s.*, r.reason_name, r.reason_code "
                   + "FROM inward_cheque_send_back_request s "
                   + "LEFT JOIN send_back_reason r ON s.reason_id = r.reason_id "
                   + "WHERE s.inward_cheque_id = ? AND s.request_status = 'PENDING' "
                   + "ORDER BY s.requested_at DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, inwardChequeId);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    return mapResultSet(rs);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public boolean markRequestResolved(String inwardChequeId, String resolvedBy) {
        String sql = "UPDATE inward_cheque_send_back_request "
                   + "SET request_status = 'RESOLVED', resolved_by = ?, resolved_at = CURRENT_TIMESTAMP "
                   + "WHERE inward_cheque_id = ? AND request_status = 'PENDING'";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, resolvedBy);
            ps.setString(2, inwardChequeId);
            return ps.executeUpdate() > 0;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    private InwardSendBackRequestDTO mapResultSet(ResultSet rs) throws Exception {
        InwardSendBackRequestDTO dto = new InwardSendBackRequestDTO();
        dto.setSendBackRequestId(rs.getString("send_back_request_id"));
        dto.setInwardChequeId(rs.getString("inward_cheque_id"));
        dto.setInwardBatchId(rs.getString("inward_batch_id"));
        dto.setReasonId(rs.getInt("reason_id"));
        dto.setReasonName(rs.getString("reason_name"));
        dto.setReasonCode(rs.getString("reason_code"));
        dto.setRemarks(rs.getString("remarks"));
        dto.setSentBackBy(rs.getString("sent_back_by"));
        dto.setRequestStatus(rs.getString("request_status"));
        dto.setRequestedAt(rs.getTimestamp("requested_at"));
        dto.setResolvedBy(rs.getString("resolved_by"));
        dto.setResolvedAt(rs.getTimestamp("resolved_at"));
        return dto;
    }
}