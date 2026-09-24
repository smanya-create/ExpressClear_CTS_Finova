package com.iispl.cts.daoimpl.outward;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.OutwardChequeDAO;
import com.iispl.cts.entity.outward.OutwardCheque;

public class OutwardChequeDAOImpl implements OutwardChequeDAO {

	@Override
	public boolean saveDataEntry(Connection connection, OutwardCheque cheque) {
		if (connection == null) {
			throw new IllegalArgumentException("Connection cannot be null");
		}
		if (cheque == null) {
			throw new IllegalArgumentException("Outward cheque cannot be null");
		}
		if (cheque.getOutwardChequeId() == null || cheque.getOutwardChequeId().trim().isEmpty()) {
			throw new IllegalArgumentException("Outward cheque ID cannot be null or empty");
		}

		String sql = "UPDATE outward_cheque SET "
				+ "cheque_number = ?, micr_code = ?, drawee_name = ?, drawee_account_number = ?, "
				+ "payee_name = ?, payee_account_number = ?, cheque_amount = ?, cheque_date = ?, "
				+ "cheque_status = ?, account_id = ?, city_code = ?, bank_code = ?, branch_code = ?, "
				+ "cheque_image_front = ?, cheque_image_back = ? WHERE outward_cheque_id = ?";

		try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {
			preparedStatement.setString(1, cheque.getChequeNumber());
			preparedStatement.setString(2, cheque.getMicrCode());
			preparedStatement.setString(3, cheque.getDraweeName());
			preparedStatement.setString(4, cheque.getDraweeAccountNumber());
			preparedStatement.setString(5, cheque.getPayeeName());
			preparedStatement.setString(6, cheque.getPayeeAccountNumber());
			preparedStatement.setBigDecimal(7, cheque.getChequeAmount());

			if (cheque.getChequeDate() != null) {
				preparedStatement.setDate(8, cheque.getChequeDate());
			} else {
				preparedStatement.setNull(8, java.sql.Types.DATE);
			}

			String status = cheque.getChequeStatus();
			if (status == null || status.trim().isEmpty()) {
				status = "PENDING_VERIFICATION";
			} else {
				status = status.trim().toUpperCase();
			}

			preparedStatement.setString(9, status);
			preparedStatement.setString(10, cheque.getAccountId());
			preparedStatement.setString(11, cheque.getCityCode());
			preparedStatement.setString(12, cheque.getBankCode());
			preparedStatement.setString(13, cheque.getBranchCode());
			preparedStatement.setString(14, cheque.getChequeImageFront());
			preparedStatement.setString(15, cheque.getChequeImageBack());
			preparedStatement.setString(16, cheque.getOutwardChequeId().trim());

			int rowsUpdated = preparedStatement.executeUpdate();
			if (rowsUpdated == 0) {
				throw new IllegalStateException("Outward cheque not found for ID: " + cheque.getOutwardChequeId());
			}
			return true;

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to save data entry for outward cheque: " + cheque.getOutwardChequeId()
					+ ". Cause: " + exception.getMessage(), exception);
		}
	}

	@Override
	public boolean saveDataEntry(OutwardCheque cheque) {
		try (Connection connection = DBConnection.getConnection()) {
			return saveDataEntry(connection, cheque);
		} catch (SQLException exception) {
			throw new RuntimeException("Unable to save data entry for outward cheque: " + cheque.getOutwardChequeId(),
					exception);
		}
	}

	@Override
	public String createOutwardChequeFromScan(Connection connection, String outwardBatchId, OutwardCheque cheque) {
		if (connection == null) {
			throw new IllegalArgumentException("Connection cannot be null");
		}
		if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {
			throw new IllegalArgumentException("Outward batch ID cannot be null or empty");
		}
		if (cheque == null) {
			throw new IllegalArgumentException("Outward cheque cannot be null");
		}

		String idSql = "SELECT 'CH' || (COALESCE(MAX(CASE WHEN outward_cheque_id ~ '^CH[0-9]+$' "
				+ "THEN CAST(SUBSTRING(outward_cheque_id FROM 3) AS BIGINT) ELSE 0 END), 0) + 1) "
				+ "AS next_outward_cheque_id FROM outward_cheque";

		String insertSql = "INSERT INTO outward_cheque ("
				+ "outward_cheque_id, outward_batch_id, cheque_number, micr_code, drawee_name, drawee_account_number, "
				+ "payee_name, payee_account_number, cheque_amount, cheque_date, cheque_status, account_id, "
				+ "created_at, city_code, bank_code, branch_code, cheque_image_front, cheque_image_back"
				+ ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		String outwardChequeId = null;

		try (PreparedStatement idStatement = connection.prepareStatement(idSql)) {
			try (ResultSet resultSet = idStatement.executeQuery()) {
				if (resultSet.next()) {
					outwardChequeId = resultSet.getString("next_outward_cheque_id");
				}
			}

			if (outwardChequeId == null || outwardChequeId.trim().isEmpty()) {
				throw new IllegalStateException("Unable to generate outward cheque ID");
			}

			outwardChequeId = outwardChequeId.trim();

			try (PreparedStatement ps = connection.prepareStatement(insertSql)) {
				ps.setString(1, outwardChequeId);
				ps.setString(2, outwardBatchId.trim());
				ps.setString(3, cheque.getChequeNumber());
				ps.setString(4, cheque.getMicrCode());
				ps.setString(5, cheque.getDraweeName());
				ps.setString(6, cheque.getDraweeAccountNumber());
				ps.setString(7, cheque.getPayeeName());
				ps.setString(8, cheque.getPayeeAccountNumber());
				ps.setBigDecimal(9, cheque.getChequeAmount());

				if (cheque.getChequeDate() != null) {
					ps.setDate(10, cheque.getChequeDate());
				} else {
					ps.setNull(10, java.sql.Types.DATE);
				}

				String status = cheque.getChequeStatus();
				if (status == null || status.trim().isEmpty()) {
					status = "PENDING_VERIFICATION";
				}
				ps.setString(11, status.trim().toUpperCase());
				ps.setString(12, cheque.getAccountId());

				if (cheque.getCreatedAt() != null) {
					ps.setTimestamp(13, cheque.getCreatedAt());
				} else {
					ps.setTimestamp(13, new java.sql.Timestamp(System.currentTimeMillis()));
				}

				ps.setString(14, cheque.getCityCode());
				ps.setString(15, cheque.getBankCode());
				ps.setString(16, cheque.getBranchCode());
				ps.setString(17, cheque.getChequeImageFront());
				ps.setString(18, cheque.getChequeImageBack());

				int rowsInserted = ps.executeUpdate();
				if (rowsInserted != 1) {
					throw new IllegalStateException(
							"Unable to create outward cheque for outward batch: " + outwardBatchId);
				}
			}

			return outwardChequeId;

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to create outward cheque for outward batch: " + outwardBatchId,
					exception);
		}
	}

	@Override
	public int getTotalChequeCountByBatchId(String outwardBatchId) {
		String sql = "SELECT COUNT(outward_cheque_id) FROM outward_cheque WHERE outward_batch_id = ?";
		try (Connection connection = DBConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, outwardBatchId.trim());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt(1);
			}
		} catch (SQLException exception) {
			throw new RuntimeException("Unable to fetch total cheque count", exception);
		}
		return 0;
	}

	@Override
	public BigDecimal getTotalChequeAmountByBatchId(String outwardBatchId) {
		String sql = "SELECT COALESCE(SUM(cheque_amount), 0) FROM outward_cheque WHERE outward_batch_id = ?";
		try (Connection connection = DBConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, outwardBatchId.trim());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getBigDecimal(1);
			}
		} catch (SQLException exception) {
			throw new RuntimeException("Unable to fetch total cheque amount", exception);
		}
		return BigDecimal.ZERO;
	}

	@Override
	public List<OutwardCheque> getChequesByBatchId(String outwardBatchId) {
		List<OutwardCheque> chequeList = new ArrayList<>();
		String sql = "SELECT outward_cheque_id, outward_batch_id, cheque_number, micr_code, drawee_name, "
				+ "drawee_account_number, payee_name, payee_account_number, cheque_amount, cheque_date, "
				+ "cheque_status, account_id, created_at, city_code, bank_code, branch_code, "
				+ "cheque_image_front, cheque_image_back FROM outward_cheque "
				+ "WHERE outward_batch_id = ? ORDER BY outward_cheque_id";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, outwardBatchId.trim());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					chequeList.add(mapOutwardCheque(rs));
				}
			}
		} catch (SQLException exception) {
			throw new RuntimeException("Unable to fetch outward cheques for batch: " + outwardBatchId, exception);
		}
		return chequeList;
	}

	@Override
	public OutwardCheque getOutwardChequeById(String outwardChequeId) {
		String sql = "SELECT outward_cheque_id, outward_batch_id, cheque_number, micr_code, drawee_name, "
				+ "drawee_account_number, payee_name, payee_account_number, cheque_amount, cheque_date, "
				+ "cheque_status, account_id, created_at, city_code, bank_code, branch_code, "
				+ "cheque_image_front, cheque_image_back FROM outward_cheque WHERE outward_cheque_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, outwardChequeId.trim());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return mapOutwardCheque(rs);
			}
		} catch (SQLException exception) {
			throw new RuntimeException("Unable to fetch outward cheque: " + outwardChequeId, exception);
		}
		return null;
	}

	@Override
	public OutwardCheque getOutwardChequeByScanCheque(String outwardBatchId, String chequeNumber,
			String chequeImageFront, String chequeImageBack) {
		String sql = "SELECT outward_cheque_id, outward_batch_id, cheque_number, micr_code, drawee_name, "
				+ "drawee_account_number, payee_name, payee_account_number, cheque_amount, cheque_date, "
				+ "cheque_status, account_id, created_at, city_code, bank_code, branch_code, "
				+ "cheque_image_front, cheque_image_back FROM outward_cheque WHERE outward_batch_id = ? AND ("
				+ "(cheque_number = ? AND COALESCE(cheque_number, '') <> '') "
				+ "OR (COALESCE(cheque_image_front, '') = ? AND COALESCE(cheque_image_front, '') <> '') "
				+ "OR (COALESCE(cheque_image_back, '') = ? AND COALESCE(cheque_image_back, '') <> '')) "
				+ "ORDER BY outward_cheque_id LIMIT 1";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, outwardBatchId.trim());
			ps.setString(2, chequeNumber);
			ps.setString(3, chequeImageFront);
			ps.setString(4, chequeImageBack);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return mapOutwardCheque(rs);
			}
		} catch (SQLException exception) {
			throw new RuntimeException("Unable to find outward cheque", exception);
		}
		return null;
	}

	@Override
	public OutwardCheque getOutwardChequeByScanCheque(String scannedBatchId, String scannedChequeId) {
		String sql = "SELECT oc.outward_cheque_id, oc.outward_batch_id, oc.cheque_number, oc.micr_code, "
				+ "oc.drawee_name, oc.drawee_account_number, oc.payee_name, oc.payee_account_number, "
				+ "oc.cheque_amount, oc.cheque_date, oc.cheque_status, oc.account_id, oc.created_at, "
				+ "oc.city_code, oc.bank_code, oc.branch_code, oc.cheque_image_front, oc.cheque_image_back "
				+ "FROM outward_cheque oc INNER JOIN scan_cheque sc ON sc.scanned_batch_id = oc.outward_batch_id "
				+ "AND sc.cheque_number = oc.cheque_number WHERE sc.scanned_batch_id = ? AND sc.scanned_cheque_id = ? "
				+ "ORDER BY oc.outward_cheque_id LIMIT 1";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, scannedBatchId.trim());
			ps.setString(2, scannedChequeId.trim());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return mapOutwardCheque(rs);
			}
		} catch (SQLException exception) {
			throw new RuntimeException("Unable to find outward cheque for scanned cheque: " + scannedChequeId,
					exception);
		}
		return null;
	}

	@Override
	public int getDataEnteredCountByBatchId(String outwardBatchId) {
		String sql = "SELECT COUNT(outward_cheque_id) FROM outward_cheque WHERE outward_batch_id = ? "
				+ "AND UPPER(TRIM(cheque_status)) IN ('PENDING_VERIFICATION', 'MAKER_RETURNED')";
		try (Connection connection = DBConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, outwardBatchId.trim());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt(1);
			}
		} catch (SQLException exception) {
			throw new RuntimeException("Unable to fetch completed data entry count", exception);
		}
		return 0;
	}

	@Override
	public boolean updateChequeStatus(String outwardChequeId, String chequeStatus) {
		try (Connection connection = DBConnection.getConnection()) {
			return updateChequeStatus(connection, outwardChequeId, chequeStatus);
		} catch (SQLException exception) {
			throw new RuntimeException("Unable to update cheque status: " + outwardChequeId, exception);
		}
	}

	@Override
	public boolean updateChequeStatus(Connection connection, String outwardChequeId, String chequeStatus) {
		String sql = "UPDATE outward_cheque SET cheque_status = ? WHERE outward_cheque_id = ?";
		try (PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, chequeStatus.trim().toUpperCase());
			ps.setString(2, outwardChequeId.trim());
			return ps.executeUpdate() > 0;
		} catch (SQLException exception) {
			throw new RuntimeException("Unable to update cheque status: " + outwardChequeId, exception);
		}
	}

	@Override
	public List<OutwardCheque> getOnHoldCheques(String outwardBatchId) {
		List<OutwardCheque> chequeList = new ArrayList<>();
		String sql = "SELECT oc.outward_cheque_id, oc.outward_batch_id, oc.cheque_number, oc.micr_code, oc.drawee_name, "
				+ "oc.drawee_account_number, oc.payee_name, oc.payee_account_number, oc.cheque_amount, oc.cheque_date, "
				+ "oc.cheque_status, oc.account_id, oc.created_at, oc.city_code, oc.bank_code, oc.branch_code, "
				+ "oc.cheque_image_front, oc.cheque_image_back " + "FROM outward_cheque oc "
				+ "LEFT JOIN outward_batch ob ON ob.outward_batch_id = oc.outward_batch_id "
				+ "WHERE oc.outward_batch_id = ? " + "AND ( "
				+ "    UPPER(TRIM(oc.cheque_status)) IN ('ON_HOLD', 'MAKER_RETURNED', 'SEND_BACK') "
				+ "    OR (UPPER(TRIM(COALESCE(ob.batch_status, ''))) = 'ON_HOLD' AND UPPER(TRIM(oc.cheque_status)) NOT IN ('PENDING_VERIFICATION')) "
				+ ") " + "ORDER BY oc.outward_cheque_id";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, outwardBatchId.trim());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					chequeList.add(mapOutwardCheque(rs));
				}
			}
		} catch (SQLException exception) {
			throw new RuntimeException("Unable to fetch returned cheques for batch: " + outwardBatchId, exception);
		}
		return chequeList;
	}

	@Override
	public List<OutwardCheque> getMakerDataEntryCheques(String outwardBatchId) {
		List<OutwardCheque> chequeList = new ArrayList<>();
		String sql = "SELECT oc.outward_cheque_id, oc.outward_batch_id, oc.cheque_number, oc.micr_code, oc.drawee_name, "
				+ "oc.drawee_account_number, oc.payee_name, oc.payee_account_number, oc.cheque_amount, oc.cheque_date, "
				+ "oc.cheque_status, oc.account_id, oc.created_at, oc.city_code, oc.bank_code, oc.branch_code, "
				+ "oc.cheque_image_front, oc.cheque_image_back " + "FROM outward_cheque oc "
				+ "LEFT JOIN outward_batch ob ON ob.outward_batch_id = oc.outward_batch_id "
				+ "WHERE oc.outward_batch_id = ? " + "AND ( "
				+ "    (UPPER(TRIM(COALESCE(ob.batch_status, ''))) = 'ON_HOLD' AND UPPER(TRIM(oc.cheque_status)) IN ('ON_HOLD', 'MAKER_RETURNED', 'PENDING_DATA_ENTRY')) "
				+ "    OR (UPPER(TRIM(COALESCE(ob.batch_status, ''))) <> 'ON_HOLD' AND UPPER(TRIM(oc.cheque_status)) IN ('PENDING_DATA_ENTRY', 'ON_HOLD', 'MAKER_RETURNED', 'MICR_REJECTED')) "
				+ ") " + "ORDER BY oc.outward_cheque_id";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, outwardBatchId.trim());
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					chequeList.add(mapOutwardCheque(rs));
				}
			}
		} catch (SQLException exception) {
			throw new RuntimeException("Unable to fetch Maker data entry cheques for batch: " + outwardBatchId,
					exception);
		}
		return chequeList;
	}

	@Override
	public int getCompletedMakerChequeCountByBatchId(String outwardBatchId) {
		String sql = "SELECT COUNT(outward_cheque_id) FROM outward_cheque WHERE outward_batch_id = ? "
				+ "AND UPPER(TRIM(cheque_status)) IN ('PENDING_VERIFICATION', 'MAKER_RETURNED', 'REJECT_REQUEST', 'REJECTION_REQUEST', 'REJECTION_REJECT')";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement ps = connection.prepareStatement(sql)) {
			ps.setString(1, outwardBatchId.trim());
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next())
					return rs.getInt(1);
			}
		} catch (SQLException exception) {
			throw new RuntimeException("Unable to fetch completed Maker cheque count", exception);
		}
		return 0;
	}

	@Override
	public boolean saveRejectionRequest(String outwardChequeId, String rejectionReason, String rejectionRemarks) {
		if (outwardChequeId == null || outwardChequeId.trim().isEmpty()) {
			throw new IllegalArgumentException("Outward cheque ID cannot be null or empty");
		}
		if (rejectionReason == null || rejectionReason.trim().isEmpty()) {
			throw new IllegalArgumentException("Rejection reason cannot be null or empty");
		}

		OutwardCheque cheque = getOutwardChequeById(outwardChequeId);
		if (cheque == null) {
			throw new IllegalArgumentException("Outward cheque not found: " + outwardChequeId);
		}

		String reasonId = null;
		String reason = rejectionReason.trim();
		int separatorIndex = reason.indexOf("|");
		if (separatorIndex > 0) {
			reasonId = reason.substring(0, separatorIndex).trim();
			reason = reason.substring(separatorIndex + 1).trim();
		}

		String updateChequeSql = "UPDATE outward_cheque SET cheque_status = 'REJECT_REQUEST' WHERE outward_cheque_id = ?";
		String insertRequestSql = "INSERT INTO outward_cheque_request (cheque_id, batch_id, remarks, reason_id, reason) VALUES (?, ?, ?, ?, ?)";

		try (Connection connection = DBConnection.getConnection()) {
			boolean previousAutoCommit = connection.getAutoCommit();
			try {
				connection.setAutoCommit(false);
				try (PreparedStatement updateStatement = connection.prepareStatement(updateChequeSql)) {
					updateStatement.setString(1, outwardChequeId.trim());
					updateStatement.executeUpdate();
				}

				try (PreparedStatement insertStatement = connection.prepareStatement(insertRequestSql)) {
					insertStatement.setString(1, cheque.getOutwardChequeId());
					insertStatement.setString(2, cheque.getOutwardBatchId());
					insertStatement.setString(3, rejectionRemarks);
					insertStatement.setString(4, reasonId);
					insertStatement.setString(5, reason);
					insertStatement.executeUpdate();
				}

				connection.commit();
				connection.setAutoCommit(previousAutoCommit);
				return true;
			} catch (Exception exception) {
				connection.rollback();
				connection.setAutoCommit(previousAutoCommit);
				throw new RuntimeException("Unable to save rejection request: " + outwardChequeId, exception);
			}
		} catch (SQLException exception) {
			throw new RuntimeException("Database error saving rejection request: " + outwardChequeId, exception);
		}
	}

	private OutwardCheque mapOutwardCheque(ResultSet resultSet) throws SQLException {
		OutwardCheque outwardCheque = new OutwardCheque();
		outwardCheque.setOutwardChequeId(resultSet.getString("outward_cheque_id"));
		outwardCheque.setOutwardBatchId(resultSet.getString("outward_batch_id"));
		outwardCheque.setChequeNumber(resultSet.getString("cheque_number"));
		outwardCheque.setMicrCode(resultSet.getString("micr_code"));
		outwardCheque.setDraweeName(resultSet.getString("drawee_name"));
		outwardCheque.setDraweeAccountNumber(resultSet.getString("drawee_account_number"));
		outwardCheque.setPayeeName(resultSet.getString("payee_name"));
		outwardCheque.setPayeeAccountNumber(resultSet.getString("payee_account_number"));
		outwardCheque.setChequeAmount(resultSet.getBigDecimal("cheque_amount"));
		outwardCheque.setChequeDate(resultSet.getDate("cheque_date"));
		outwardCheque.setChequeStatus(resultSet.getString("cheque_status"));
		outwardCheque.setAccountId(resultSet.getString("account_id"));
		outwardCheque.setCreatedAt(resultSet.getTimestamp("created_at"));
		outwardCheque.setCityCode(resultSet.getString("city_code"));
		outwardCheque.setBankCode(resultSet.getString("bank_code"));
		outwardCheque.setBranchCode(resultSet.getString("branch_code"));
		outwardCheque.setChequeImageFront(resultSet.getString("cheque_image_front"));
		outwardCheque.setChequeImageBack(resultSet.getString("cheque_image_back"));
		return outwardCheque;
	}
}