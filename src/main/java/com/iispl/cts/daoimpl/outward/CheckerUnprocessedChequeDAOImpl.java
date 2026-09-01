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
		// TODO Auto-generated method stub
		List<UnprocessedChequeDTO> list = new ArrayList<>();

		String sql = "SELECT c.cheque_id, c.outward_batch_id, b.batch_reference_id, cs.clearing_date, " +
				"c.cheque_number, (c.city_code || c.bank_code || c.branch_code) AS sort_code, " +
				"c.cheque_amount, c.cheque_status, c.is_eod_rollover, sbr.reason_name, c.checker_remarks, c.created_at " +
				"FROM outward_cheque c " +
				"JOIN outward_batch b ON c.outward_batch_id = b.outward_batch_id " +
				"LEFT JOIN clearing_session cs ON b.clearing_date = cs.clearing_date " +
				"LEFT JOIN send_back_reasons sbr ON c.send_back_reason_id = sbr.reason_id " +
				"WHERE c.is_eod_rollover = TRUE " +
				"AND c.cheque_status = 'PENDING_VERIFICATION' " +
				"ORDER BY c.created_at ASC, c.cheque_id ASC";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql);
				ResultSet rs = ps.executeQuery()) {

			while (rs.next()) {
				UnprocessedChequeDTO dto = new UnprocessedChequeDTO();
				dto.setChequeId(rs.getLong("cheque_id"));
				dto.setBatchId(rs.getLong("outward_batch_id"));
				dto.setBatchNo(rs.getString("batch_reference_id"));
				dto.setOriginalSessionName(rs.getString("clearing_date"));
				dto.setChequeNo(rs.getString("cheque_number"));
				dto.setSortCode(rs.getString("sort_code"));
				dto.setAmount(rs.getBigDecimal("cheque_amount"));
				dto.setStatus(rs.getString("cheque_status"));
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
	public boolean verifyCheque(Long chequeId, String checkerUserId) {
		// TODO Auto-generated method stub
		String sql = "UPDATE outward_cheque " +
				"SET cheque_status = 'VERIFIED', verified_by = ?, verified_at = CURRENT_TIMESTAMP " +
				"WHERE cheque_id = ? AND cheque_status = 'PENDING_VERIFICATION'";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setString(1, checkerUserId);
			ps.setLong(2, chequeId);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean sendBackToMaker(Long chequeId, Long sendBackReasonId, String checkerRemarks, String checkerUserId) {
		// TODO Auto-generated method stub
		String sql = "UPDATE outward_cheque " +
				"SET cheque_status = 'PENDING_DATA_ENTRY', send_back_reason_id = ?, checker_remarks = ?, " +
				"verified_by = ?, verified_at = CURRENT_TIMESTAMP " +
				"WHERE cheque_id = ? AND cheque_status = 'PENDING_VERIFICATION'";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, sendBackReasonId);
			ps.setString(2, checkerRemarks);
			ps.setString(3, checkerUserId);
			ps.setLong(4, chequeId);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}	}

	@Override
	public boolean rejectCheque(Long chequeId, Long rejectReasonId, String rejectRemarks, String checkerUserId) {
		// TODO Auto-generated method stub
		String sql = "UPDATE outward_cheque " +
				"SET cheque_status = 'REJECTED', reject_reason_id = ?, checker_remarks = ?, " +
				"verified_by = ?, verified_at = CURRENT_TIMESTAMP " +
				"WHERE cheque_id = ? AND cheque_status = 'PENDING_VERIFICATION'";

		try (Connection conn = DBConnection.getConnection();
				PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setLong(1, rejectReasonId);
			ps.setString(2, rejectRemarks);
			ps.setString(3, checkerUserId);
			ps.setLong(4, chequeId);
			return ps.executeUpdate() > 0;
		} catch (SQLException e) {
			e.printStackTrace();
			return false;
		}
	}
}


