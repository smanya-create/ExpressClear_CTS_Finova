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
	public int getTotalChequeCountByBatchId(String outwardBatchId) {

		if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {
			throw new IllegalArgumentException("Outward batch ID cannot be null or empty");
		}

		String sql = "SELECT COUNT(outward_cheque_id) " + "FROM outward_cheque " + "WHERE outward_batch_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, outwardBatchId.trim());

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (resultSet.next()) {
					return resultSet.getInt(1);
				}
			}

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to fetch total cheque count for outward batch: " + outwardBatchId,
					exception);
		}

		return 0;
	}

	@Override
	public BigDecimal getTotalChequeAmountByBatchId(String outwardBatchId) {

		if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {
			throw new IllegalArgumentException("Outward batch ID cannot be null or empty");
		}

		String sql = "SELECT COALESCE(SUM(cheque_amount), 0) " + "FROM outward_cheque " + "WHERE outward_batch_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, outwardBatchId.trim());

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (resultSet.next()) {
					return resultSet.getBigDecimal(1);
				}
			}

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to fetch total cheque amount for outward batch: " + outwardBatchId,
					exception);
		}

		return BigDecimal.ZERO;
	}

	@Override
	public List<OutwardCheque> getChequesByBatchId(String outwardBatchId) {

		if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {
			throw new IllegalArgumentException("Outward batch ID cannot be null or empty");
		}

		List<OutwardCheque> chequeList = new ArrayList<>();

		String sql = "SELECT " + "oc.outward_cheque_id, " + "oc.outward_batch_id, " + "oc.cheque_number, "
				+ "oc.micr_code, " + "oc.drawee_name, " + "oc.drawee_account_number, " + "oc.payee_name, "
				+ "oc.payee_account_number, " + "oc.cheque_amount, " + "oc.cheque_date, " + "oc.cheque_status, "
				+ "oc.account_id, " + "oc.created_at, " + "oc.city_code, " + "oc.bank_code, " + "oc.branch_code, "
				+ "oc.cheque_image_front, " + "oc.cheque_image_back " + "FROM outward_cheque oc "
				+ "WHERE oc.outward_batch_id = ? " + "ORDER BY oc.outward_cheque_id";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, outwardBatchId.trim());

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {
					chequeList.add(mapOutwardCheque(resultSet));
				}
			}

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to fetch outward cheques for batch: " + outwardBatchId, exception);
		}

		return chequeList;
	}

	@Override
	public OutwardCheque getOutwardChequeById(String outwardChequeId) {

		if (outwardChequeId == null || outwardChequeId.trim().isEmpty()) {
			throw new IllegalArgumentException("Outward cheque ID cannot be null or empty");
		}

		String sql = "SELECT " + "outward_cheque_id, " + "outward_batch_id, " + "cheque_number, " + "micr_code, "
				+ "drawee_name, " + "drawee_account_number, " + "payee_name, " + "payee_account_number, "
				+ "cheque_amount, " + "cheque_date, " + "cheque_status, " + "account_id, " + "created_at, "
				+ "city_code, " + "bank_code, " + "branch_code, " + "cheque_image_front, " + "cheque_image_back "
				+ "FROM outward_cheque " + "WHERE outward_cheque_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, outwardChequeId.trim());

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (resultSet.next()) {
					return mapOutwardCheque(resultSet);
				}
			}

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to fetch outward cheque: " + outwardChequeId, exception);
		}

		return null;
	}

	@Override
	public OutwardCheque getOutwardChequeByScanCheque(String outwardBatchId, String chequeNumber,
			String chequeImageFront, String chequeImageBack) {

		if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {
			throw new IllegalArgumentException("Outward batch ID cannot be null or empty");
		}

		String sql = "SELECT " + "outward_cheque_id, " + "outward_batch_id, " + "cheque_number, " + "micr_code, "
				+ "drawee_name, " + "drawee_account_number, " + "payee_name, " + "payee_account_number, "
				+ "cheque_amount, " + "cheque_date, " + "cheque_status, " + "account_id, " + "created_at, "
				+ "city_code, " + "bank_code, " + "branch_code, " + "cheque_image_front, " + "cheque_image_back "
				+ "FROM outward_cheque " + "WHERE outward_batch_id = ? " + "AND (" + "(cheque_number = ? "
				+ "AND COALESCE(cheque_number, '') <> '') " + "OR (COALESCE(cheque_image_front, '') = ? "
				+ "AND COALESCE(cheque_image_front, '') <> '') " + "OR (COALESCE(cheque_image_back, '') = ? "
				+ "AND COALESCE(cheque_image_back, '') <> '')" + ") " + "ORDER BY CASE "
				+ "WHEN cheque_number = ? THEN 1 " + "WHEN COALESCE(cheque_image_front, '') = ? THEN 2 "
				+ "WHEN COALESCE(cheque_image_back, '') = ? THEN 3 " + "ELSE 4 " + "END, " + "outward_cheque_id "
				+ "LIMIT 1";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, outwardBatchId.trim());
			preparedStatement.setString(2, chequeNumber);
			preparedStatement.setString(3, chequeImageFront);
			preparedStatement.setString(4, chequeImageBack);
			preparedStatement.setString(5, chequeNumber);
			preparedStatement.setString(6, chequeImageFront);
			preparedStatement.setString(7, chequeImageBack);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (resultSet.next()) {
					return mapOutwardCheque(resultSet);
				}
			}

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to find outward cheque for scanned cheque in batch: " + outwardBatchId,
					exception);
		}

		return null;
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

		String idSql = "SELECT 'CH' || " + "(COALESCE(MAX(" + "CASE " + "WHEN outward_cheque_id ~ '^CH[0-9]+$' "
				+ "THEN CAST(SUBSTRING(outward_cheque_id FROM 3) AS BIGINT) " + "ELSE 0 " + "END" + "), 0) + 1) "
				+ "AS next_outward_cheque_id " + "FROM outward_cheque";

		String insertSql = "INSERT INTO outward_cheque (" + "outward_cheque_id, " + "outward_batch_id, "
				+ "cheque_number, " + "micr_code, " + "drawee_name, " + "drawee_account_number, " + "payee_name, "
				+ "payee_account_number, " + "cheque_amount, " + "cheque_date, " + "cheque_status, " + "account_id, "
				+ "created_at, " + "city_code, " + "bank_code, " + "branch_code, " + "cheque_image_front, "
				+ "cheque_image_back" + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

			try (PreparedStatement preparedStatement = connection.prepareStatement(insertSql)) {

				preparedStatement.setString(1, outwardChequeId);
				preparedStatement.setString(2, outwardBatchId.trim());
				preparedStatement.setString(3, cheque.getChequeNumber());
				preparedStatement.setString(4, cheque.getMicrCode());
				preparedStatement.setString(5, cheque.getDraweeName());
				preparedStatement.setString(6, cheque.getDraweeAccountNumber());
				preparedStatement.setString(7, cheque.getPayeeName());
				preparedStatement.setString(8, cheque.getPayeeAccountNumber());
				preparedStatement.setBigDecimal(9, cheque.getChequeAmount());

				if (cheque.getChequeDate() != null) {
					preparedStatement.setDate(10, cheque.getChequeDate());
				} else {
					preparedStatement.setNull(10, java.sql.Types.DATE);
				}

				preparedStatement.setString(11, "PENDING_VERIFICATION");

				preparedStatement.setString(12, cheque.getAccountId());

				if (cheque.getCreatedAt() != null) {
					preparedStatement.setTimestamp(13, cheque.getCreatedAt());
				} else {
					preparedStatement.setTimestamp(13, new java.sql.Timestamp(System.currentTimeMillis()));
				}

				preparedStatement.setString(14, cheque.getCityCode());

				preparedStatement.setString(15, cheque.getBankCode());

				preparedStatement.setString(16, cheque.getBranchCode());

				preparedStatement.setString(17, cheque.getChequeImageFront());

				preparedStatement.setString(18, cheque.getChequeImageBack());

				int rowsInserted = preparedStatement.executeUpdate();

				if (rowsInserted != 1) {
					throw new IllegalStateException(
							"Unable to create outward cheque for outward batch: " + outwardBatchId);
				}
			}

			return outwardChequeId;

		} catch (SQLException exception) {

			String message = exception.getMessage();

			throw new RuntimeException("Unable to create outward cheque for outward batch: " + outwardBatchId
					+ ". Cause: " + (message == null ? "Unknown database error" : message), exception);
		}
	}

	@Override
	public int getDataEnteredCountByBatchId(String outwardBatchId) {

		if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {

			throw new IllegalArgumentException("Outward batch ID cannot be null or empty");
		}

		String sql = "SELECT COUNT(outward_cheque_id) " + "FROM outward_cheque " + "WHERE outward_batch_id = ? "
				+ "AND UPPER(TRIM(cheque_status)) = " + "'PENDING_VERIFICATION'";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, outwardBatchId.trim());

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (resultSet.next()) {
					return resultSet.getInt(1);
				}
			}

		} catch (SQLException exception) {
			throw new RuntimeException(
					"Unable to fetch completed data entry count " + "for outward batch: " + outwardBatchId, exception);
		}

		return 0;
	}

	@Override
	public boolean updateChequeStatus(String outwardChequeId, String chequeStatus) {

		if (outwardChequeId == null || outwardChequeId.trim().isEmpty()) {

			throw new IllegalArgumentException("Outward cheque ID cannot be null or empty");
		}

		if (chequeStatus == null || chequeStatus.trim().isEmpty()) {

			throw new IllegalArgumentException("Cheque status cannot be null or empty");
		}

		String sql = "UPDATE outward_cheque " + "SET cheque_status = ? " + "WHERE outward_cheque_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, chequeStatus.trim());

			preparedStatement.setString(2, outwardChequeId.trim());

			return preparedStatement.executeUpdate() > 0;

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to update cheque status for outward cheque ID: " + outwardChequeId,
					exception);
		}
	}

	@Override
	public List<OutwardCheque> getOutwardMicrRepairCheques(String outwardBatchId) {

		if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {

			throw new IllegalArgumentException("Outward batch ID cannot be null or empty");
		}

		List<OutwardCheque> chequeList = new ArrayList<>();

		String sql = "SELECT " + "outward_cheque_id, " + "outward_batch_id, " + "cheque_number, " + "micr_code, "
				+ "drawee_name, " + "drawee_account_number, " + "payee_name, " + "payee_account_number, "
				+ "cheque_amount, " + "cheque_date, " + "cheque_status, " + "account_id, " + "created_at, "
				+ "city_code, " + "bank_code, " + "branch_code, " + "cheque_image_front, " + "cheque_image_back "
				+ "FROM outward_cheque " + "WHERE outward_batch_id = ? " + "AND UPPER(TRIM(cheque_status)) = "
				+ "'PENDING_MICR_REPAIR' " + "ORDER BY outward_cheque_id";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, outwardBatchId.trim());

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {
					chequeList.add(mapOutwardCheque(resultSet));
				}
			}

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to fetch MICR repair cheques for batch: " + outwardBatchId, exception);
		}

		return chequeList;
	}

	@Override
	public void saveOutwardMicrRepair(OutwardCheque cheque) {

		if (cheque == null) {
			throw new IllegalArgumentException("Outward cheque cannot be null");
		}

		if (cheque.getOutwardChequeId() == null || cheque.getOutwardChequeId().trim().isEmpty()) {

			throw new IllegalArgumentException("Outward cheque ID cannot be null or empty");
		}

		String sql = "UPDATE outward_cheque SET " + "micr_code = ?, " + "city_code = ?, " + "bank_code = ?, "
				+ "branch_code = ?, " + "cheque_status = ? " + "WHERE outward_cheque_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, cheque.getMicrCode());

			preparedStatement.setString(2, cheque.getCityCode());

			preparedStatement.setString(3, cheque.getBankCode());

			preparedStatement.setString(4, cheque.getBranchCode());

			preparedStatement.setString(5, cheque.getChequeStatus());

			preparedStatement.setString(6, cheque.getOutwardChequeId());

			int rowsUpdated = preparedStatement.executeUpdate();

			if (rowsUpdated == 0) {
				throw new IllegalStateException("Outward cheque not found for ID: " + cheque.getOutwardChequeId());
			}

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to save MICR repair for outward cheque: " + cheque.getOutwardChequeId(),
					exception);
		}
	}

	@Override
	public boolean saveDataEntry(OutwardCheque cheque) {

		if (cheque == null) {
			throw new IllegalArgumentException("Outward cheque cannot be null");
		}

		if (cheque.getOutwardChequeId() == null || cheque.getOutwardChequeId().trim().isEmpty()) {

			throw new IllegalArgumentException("Outward cheque ID cannot be null or empty");
		}

		try (Connection connection = DBConnection.getConnection()) {

			return saveDataEntry(connection, cheque);

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to save data entry for outward cheque: " + cheque.getOutwardChequeId(),
					exception);
		}
	}

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

		String sql = "UPDATE outward_cheque SET " + "cheque_number = ?, " + "micr_code = ?, " + "drawee_name = ?, "
				+ "drawee_account_number = ?, " + "payee_name = ?, " + "payee_account_number = ?, "
				+ "cheque_amount = ?, " + "cheque_date = ?, " + "cheque_status = ?, " + "account_id = ?, "
				+ "city_code = ?, " + "bank_code = ?, " + "branch_code = ?, " + "cheque_image_front = ?, "
				+ "cheque_image_back = ? " + "WHERE outward_cheque_id = ?";

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

			if (status == null || status.trim().isEmpty() || status.equalsIgnoreCase("PENDING_DATA_ENTRY")) {
				status = "PENDING_VERIFICATION";
			}

			preparedStatement.setString(9, status.trim());
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

			String message = exception.getMessage();

			throw new RuntimeException("Unable to save data entry for outward cheque: " + cheque.getOutwardChequeId()
					+ ". Cause: " + (message == null ? "Unknown database error" : message), exception);
		}
	}

	@Override
	public OutwardCheque getOutwardChequeByScanCheque(String scannedBatchId, String scannedChequeId) {

		if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {

			throw new IllegalArgumentException("Scanned batch ID cannot be null or empty");
		}

		if (scannedChequeId == null || scannedChequeId.trim().isEmpty()) {

			throw new IllegalArgumentException("Scanned cheque ID cannot be null or empty");
		}

		String sql = "SELECT " + "oc.outward_cheque_id, " + "oc.outward_batch_id, " + "oc.cheque_number, "
				+ "oc.micr_code, " + "oc.drawee_name, " + "oc.drawee_account_number, " + "oc.payee_name, "
				+ "oc.payee_account_number, " + "oc.cheque_amount, " + "oc.cheque_date, " + "oc.cheque_status, "
				+ "oc.account_id, " + "oc.created_at, " + "oc.city_code, " + "oc.bank_code, " + "oc.branch_code, "
				+ "oc.cheque_image_front, " + "oc.cheque_image_back " + "FROM outward_cheque oc "
				+ "INNER JOIN scan_cheque sc " + "ON sc.scanned_batch_id = oc.outward_batch_id "
				+ "AND sc.cheque_number = oc.cheque_number " + "WHERE sc.scanned_batch_id = ? "
				+ "AND sc.scanned_cheque_id = ? " + "ORDER BY oc.outward_cheque_id " + "LIMIT 1";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, scannedBatchId.trim());

			preparedStatement.setString(2, scannedChequeId.trim());

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (resultSet.next()) {
					return mapOutwardCheque(resultSet);
				}
			}

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to find outward cheque for scanned cheque: " + scannedChequeId,
					exception);
		}

		return null;
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

	@Override
	public List<OutwardCheque> getOnHoldCheques(String outwardBatchId) {

		if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {
			throw new IllegalArgumentException("Outward batch ID cannot be null or empty");
		}

		List<OutwardCheque> chequeList = new ArrayList<>();

		String sql = "SELECT " + "outward_cheque_id, " + "outward_batch_id, " + "cheque_number, " + "micr_code, "
				+ "drawee_name, " + "drawee_account_number, " + "payee_name, " + "payee_account_number, "
				+ "cheque_amount, " + "cheque_date, " + "cheque_status, " + "account_id, " + "created_at, "
				+ "city_code, " + "bank_code, " + "branch_code, " + "cheque_image_front, " + "cheque_image_back "
				+ "FROM outward_cheque " + "WHERE outward_batch_id = ? " + "AND UPPER(TRIM(cheque_status)) IN (?, ?) "
				+ "ORDER BY outward_cheque_id";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, outwardBatchId.trim());
			preparedStatement.setString(2, "PENDING_DATA_ENTRY");
			preparedStatement.setString(3, "PENDING_MICR_REPAIR");

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {
					chequeList.add(mapOutwardCheque(resultSet));
				}
			}

		} catch (SQLException exception) {
			throw new RuntimeException("Unable to fetch returned cheques for outward batch: " + outwardBatchId,
					exception);
		}

		return chequeList;
	}
}