package com.iispl.cts.daoimpl.inward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.inward.InwardChequeDAO;
import com.iispl.cts.entity.inward.InwardCheque;

public class InwardChequeDAOImpl implements InwardChequeDAO {

	private static InwardChequeDAOImpl instance;

	private InwardChequeDAOImpl() {
	}

	public static synchronized InwardChequeDAOImpl getInstance() {
		if (instance == null) {
			instance = new InwardChequeDAOImpl();
		}
		return instance;
	}

	// Fetch all inward cheques that require MICR repair.

	@Override
	public List<InwardCheque> getMicrRepairRequiredCheques() {

		List<InwardCheque> list = new ArrayList<>();

		String sql = "SELECT inward_cheque_id, inward_batch_id, cheque_number, "+ "micr_code, drawee_name, drawee_account_number, "+ "payee_name, payee_account_number, cheque_amount, "
				+ "cheque_date, cheque_status, account_id, created_at " + "FROM inward_cheque "+ "WHERE cheque_status = ? " + "ORDER BY created_at ASC";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, "MICR_REPAIR_REQUIRED");

			try (ResultSet rs = ps.executeQuery()) {

				while (rs.next()) {
					list.add(mapResultSet(rs));
				}
			}

		} catch (SQLException e) {

			System.err.println("Failed to load MICR repair records.");

			e.printStackTrace();
		}

		System.out.println("Records loaded: " + list.size());

		return list;
	}

	
	 // Fetch one inward cheque by its ID.
	
	@Override
	public InwardCheque findById(String inwardChequeId) {

		if (inwardChequeId == null || inwardChequeId.trim().isEmpty()) {
			return null;
		}

		String sql = "SELECT inward_cheque_id, inward_batch_id, cheque_number, "+ "micr_code, drawee_name, drawee_account_number, "+ "payee_name, payee_account_number, cheque_amount, "+ "cheque_date, cheque_status, account_id, created_at " + "FROM inward_cheque "
				     + "WHERE inward_cheque_id = ?";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, inwardChequeId.trim());

			try (ResultSet rs = ps.executeQuery()) {

				if (rs.next()) {
					return mapResultSet(rs);
				}
			}

		} catch (SQLException e) {

			System.err.println("Failed to find cheque: " + inwardChequeId);

			e.printStackTrace();
		}

		return null;
	}

	
	 // Update MICR code and cheque status after repair.
	 
	@Override
	public boolean updateMicrRepair(String inwardChequeId, String correctedMicrCode, String chequeStatus) {

		if (inwardChequeId == null || inwardChequeId.trim().isEmpty() || correctedMicrCode == null
				    || correctedMicrCode.trim().isEmpty() || chequeStatus == null || chequeStatus.trim().isEmpty())
		
		           {

			          return false;
		           }

		String sql = "UPDATE inward_cheque " + "SET micr_code = ?, cheque_status = ? " + "WHERE inward_cheque_id = ?";

		try (Connection conn = DBConnection.getConnection(); PreparedStatement ps = conn.prepareStatement(sql)) {

			ps.setString(1, correctedMicrCode.trim());
			ps.setString(2, chequeStatus.trim());
			ps.setString(3, inwardChequeId.trim());

			int rowsUpdated = ps.executeUpdate();

			System.out.println("Updated rows: " + rowsUpdated);

			return rowsUpdated > 0;

		} catch (SQLException e) {

			System.err.println("Failed to update MICR repair " + "for cheque: " + inwardChequeId);

			e.printStackTrace();

			return false;
		}
	}

	
	private InwardCheque mapResultSet(ResultSet rs) throws SQLException {

		InwardCheque cheque = new InwardCheque();

		cheque.setInwardChequeId(rs.getString("inward_cheque_id"));

		cheque.setInwardBatchId(rs.getString("inward_batch_id"));

		cheque.setChequeNumber(rs.getString("cheque_number"));

		cheque.setMicrCode(rs.getString("micr_code"));

		cheque.setDraweeName(rs.getString("drawee_name"));

		cheque.setDraweeAccountNumber(rs.getString("drawee_account_number"));

		cheque.setPayeeName(rs.getString("payee_name"));

		cheque.setPayeeAccountNumber(rs.getString("payee_account_number"));

		cheque.setChequeAmount(rs.getBigDecimal("cheque_amount"));

		cheque.setChequeDate(rs.getDate("cheque_date"));

		cheque.setChequeStatus(rs.getString("cheque_status"));

		cheque.setAccountId(rs.getString("account_id"));

		cheque.setCreatedAt(rs.getTimestamp("created_at"));

		return cheque;
	}
}