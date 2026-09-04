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

	private static final String SELECT_COLUMNS = "inward_cheque_id, " + "inward_batch_id, " + "cheque_number, "
			+ "micr_code, " + "drawee_name, " + "drawee_account_number, " + "payee_name, " + "payee_account_number, "
			+ "cheque_amount, " + "cheque_date, " + "cheque_status, " + "account_id, " + "created_at, " + "city_code, "
			+ "bank_code, " + "branch_code, " + "cheque_image_front, " + "cheque_image_back, " + "transaction_code, "
			+ "item_sequence_number ";

	@Override
	public List<InwardCheque> getMicrRepairRequiredCheques() {

		List<InwardCheque> list = new ArrayList<>();

		String sql = "SELECT " + SELECT_COLUMNS + "FROM inward_cheque " + "WHERE cheque_status = ? "
				+ "ORDER BY item_sequence_number ASC, created_at ASC";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, "MICR_REPAIR_REQUIRED");

			try (ResultSet resultSet = statement.executeQuery()) {

				while (resultSet.next()) {

					list.add(mapResultSet(resultSet));
				}
			}

		} catch (SQLException e) {

			System.err.println("Failed to load MICR repair records.");

			e.printStackTrace();
		}

		System.out.println("MICR repair records loaded: " + list.size());

		return list;
	}

	@Override
	public InwardCheque findById(String inwardChequeId) {

		if (inwardChequeId == null || inwardChequeId.trim().isEmpty()) {

			return null;
		}

		String sql = "SELECT " + SELECT_COLUMNS + "FROM inward_cheque " + "WHERE inward_cheque_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, inwardChequeId.trim());

			try (ResultSet resultSet = statement.executeQuery()) {

				if (resultSet.next()) {

					return mapResultSet(resultSet);
				}
			}

		} catch (SQLException e) {

			System.err.println("Failed to find cheque: " + inwardChequeId);

			e.printStackTrace();
		}

		return null;
	}

	@Override
	public List<InwardCheque> findByBatchAndStatus(String batchId, String status) {

		List<InwardCheque> cheques = new ArrayList<>();

		if (batchId == null || batchId.trim().isEmpty()) {

			return cheques;
		}

		StringBuilder sql = new StringBuilder();

		sql.append("SELECT ").append(SELECT_COLUMNS).append("FROM inward_cheque ").append("WHERE inward_batch_id = ? ");

		if (status != null && !status.trim().isEmpty()) {

			sql.append("AND cheque_status = ? ");
		}

		sql.append("ORDER BY item_sequence_number ASC, created_at ASC");

		try (Connection connection = DBConnection.getConnection();

				PreparedStatement statement = connection.prepareStatement(sql.toString())) {

			statement.setString(1, batchId.trim());

			if (status != null && !status.trim().isEmpty()) {

				statement.setString(2, status.trim());
			}

			try (ResultSet resultSet = statement.executeQuery()) {

				while (resultSet.next()) {

					cheques.add(mapResultSet(resultSet));
				}
			}

		} catch (SQLException e) {

			System.err.println("Failed to load cheques for batch: " + batchId);

			e.printStackTrace();
		}

		return cheques;
	}

	@Override
	public List<InwardCheque> getAllCheques() {

		List<InwardCheque> cheques = new ArrayList<>();

		String sql = "SELECT " + SELECT_COLUMNS + "FROM inward_cheque " + "ORDER BY created_at DESC";

		try (Connection connection = DBConnection.getConnection();

				PreparedStatement statement = connection.prepareStatement(sql);

				ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {

				cheques.add(mapResultSet(resultSet));
			}

		} catch (SQLException e) {

			System.err.println("Failed to load all inward cheques.");

			e.printStackTrace();
		}

		return cheques;
	}

	@Override
	public InwardCheque getChequeById(String inwardChequeId) {

		return findById(inwardChequeId);
	}

	@Override
	public List<InwardCheque> getChequesByBatchId(String inwardBatchId) {

		return findByBatchAndStatus(inwardBatchId, null);
	}

	@Override
	public boolean saveCheque(InwardCheque inwardCheque) {

		if (inwardCheque == null) {

			return false;
		}

		String sql = "INSERT INTO inward_cheque (" + "inward_cheque_id, " + "inward_batch_id, " + "cheque_number, "
				+ "micr_code, " + "drawee_name, " + "drawee_account_number, " + "payee_name, "
				+ "payee_account_number, " + "cheque_amount, " + "cheque_date, " + "cheque_status, " + "account_id, "
				+ "created_at, " + "city_code, " + "bank_code, " + "branch_code, " + "cheque_image_front, "
				+ "cheque_image_back, " + "transaction_code, " + "item_sequence_number" + ") VALUES ("
				+ "?, ?, ?, ?, ?, ?, ?, ?, ?, ?, " + "?, ?, ?, ?, ?, ?, ?, ?, ?, ?" + ")";

		try (Connection connection = DBConnection.getConnection();

				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, inwardCheque.getInwardChequeId());

			statement.setString(2, inwardCheque.getInwardBatchId());

			statement.setString(3, inwardCheque.getChequeNumber());

			statement.setString(4, inwardCheque.getMicrCode());

			statement.setString(5, inwardCheque.getDraweeName());

			statement.setString(6, inwardCheque.getDraweeAccountNumber());

			statement.setString(7, inwardCheque.getPayeeName());

			statement.setString(8, inwardCheque.getPayeeAccountNumber());

			statement.setBigDecimal(9, inwardCheque.getChequeAmount());

			statement.setDate(10, inwardCheque.getChequeDate());

			statement.setString(11, inwardCheque.getChequeStatus());

			statement.setString(12, inwardCheque.getAccountId());

			statement.setTimestamp(13, inwardCheque.getCreatedAt());

			statement.setString(14, inwardCheque.getCityCode());

			statement.setString(15, inwardCheque.getBankCode());

			statement.setString(16, inwardCheque.getBranchCode());

			statement.setString(17, inwardCheque.getChequeImageFront());

			statement.setString(18, inwardCheque.getChequeImageBack());

			statement.setString(19, inwardCheque.getTransactionCode());

			if (inwardCheque.getItemSequenceNumber() != null) {

				statement.setInt(20, inwardCheque.getItemSequenceNumber());

			} else {

				statement.setNull(20, java.sql.Types.INTEGER);
			}

			return statement.executeUpdate() > 0;

		} catch (SQLException e) {

			System.err.println("Failed to save inward cheque: " + inwardCheque.getInwardChequeId());

			e.printStackTrace();

			return false;
		}
	}

	@Override
	public boolean updateCheque(InwardCheque inwardCheque) {

		if (inwardCheque == null || inwardCheque.getInwardChequeId() == null
				|| inwardCheque.getInwardChequeId().trim().isEmpty()) {

			return false;
		}

		String sql = "UPDATE inward_cheque SET " + "inward_batch_id = ?, " + "cheque_number = ?, " + "micr_code = ?, "
				+ "drawee_name = ?, " + "drawee_account_number = ?, " + "payee_name = ?, "
				+ "payee_account_number = ?, " + "cheque_amount = ?, " + "cheque_date = ?, " + "cheque_status = ?, "
				+ "account_id = ?, " + "created_at = ?, " + "city_code = ?, " + "bank_code = ?, " + "branch_code = ?, "
				+ "cheque_image_front = ?, " + "cheque_image_back = ?, " + "transaction_code = ?, "
				+ "item_sequence_number = ? " + "WHERE inward_cheque_id = ?";

		try (Connection connection = DBConnection.getConnection();

				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, inwardCheque.getInwardBatchId());

			statement.setString(2, inwardCheque.getChequeNumber());

			statement.setString(3, inwardCheque.getMicrCode());

			statement.setString(4, inwardCheque.getDraweeName());

			statement.setString(5, inwardCheque.getDraweeAccountNumber());

			statement.setString(6, inwardCheque.getPayeeName());

			statement.setString(7, inwardCheque.getPayeeAccountNumber());

			statement.setBigDecimal(8, inwardCheque.getChequeAmount());

			statement.setDate(9, inwardCheque.getChequeDate());

			statement.setString(10, inwardCheque.getChequeStatus());

			statement.setString(11, inwardCheque.getAccountId());

			statement.setTimestamp(12, inwardCheque.getCreatedAt());

			statement.setString(13, inwardCheque.getCityCode());

			statement.setString(14, inwardCheque.getBankCode());

			statement.setString(15, inwardCheque.getBranchCode());

			statement.setString(16, inwardCheque.getChequeImageFront());

			statement.setString(17, inwardCheque.getChequeImageBack());

			statement.setString(18, inwardCheque.getTransactionCode());

			if (inwardCheque.getItemSequenceNumber() != null) {

				statement.setInt(19, inwardCheque.getItemSequenceNumber());

			} else {

				statement.setNull(19, java.sql.Types.INTEGER);
			}

			statement.setString(20, inwardCheque.getInwardChequeId());

			return statement.executeUpdate() > 0;

		} catch (SQLException e) {

			System.err.println("Failed to update inward cheque: " + inwardCheque.getInwardChequeId());

			e.printStackTrace();

			return false;
		}
	}

	@Override
	public boolean updateMicrRepair(String inwardChequeId, String correctedMicrCode, String chequeStatus) {

		if (inwardChequeId == null || inwardChequeId.trim().isEmpty() || correctedMicrCode == null
				|| correctedMicrCode.trim().isEmpty() || chequeStatus == null || chequeStatus.trim().isEmpty()) {

			return false;
		}

		String sql = "UPDATE inward_cheque " + "SET micr_code = ?, " + "cheque_status = ? "
				+ "WHERE inward_cheque_id = ?";

		try (Connection connection = DBConnection.getConnection();

				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, correctedMicrCode.trim());

			statement.setString(2, chequeStatus.trim());

			statement.setString(3, inwardChequeId.trim());

			return statement.executeUpdate() > 0;

		} catch (SQLException e) {

			System.err.println("Failed to update MICR repair for cheque: " + inwardChequeId);

			e.printStackTrace();

			return false;
		}
	}

	@Override
	public boolean deleteCheque(String inwardChequeId) {

		if (inwardChequeId == null || inwardChequeId.trim().isEmpty()) {

			return false;
		}

		String sql = "DELETE FROM inward_cheque " + "WHERE inward_cheque_id = ?";

		try (Connection connection = DBConnection.getConnection();

				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, inwardChequeId.trim());

			return statement.executeUpdate() > 0;

		} catch (SQLException e) {

			System.err.println("Failed to delete inward cheque: " + inwardChequeId);

			e.printStackTrace();

			return false;
		}
	}

	private InwardCheque mapResultSet(ResultSet resultSet) throws SQLException {

		InwardCheque cheque = new InwardCheque();

		cheque.setInwardChequeId(resultSet.getString("inward_cheque_id"));

		cheque.setInwardBatchId(resultSet.getString("inward_batch_id"));

		cheque.setChequeNumber(resultSet.getString("cheque_number"));

		cheque.setMicrCode(resultSet.getString("micr_code"));

		cheque.setDraweeName(resultSet.getString("drawee_name"));

		cheque.setDraweeAccountNumber(resultSet.getString("drawee_account_number"));

		cheque.setPayeeName(resultSet.getString("payee_name"));

		cheque.setPayeeAccountNumber(resultSet.getString("payee_account_number"));

		cheque.setChequeAmount(resultSet.getBigDecimal("cheque_amount"));

		cheque.setChequeDate(resultSet.getDate("cheque_date"));

		cheque.setChequeStatus(resultSet.getString("cheque_status"));

		cheque.setAccountId(resultSet.getString("account_id"));

		cheque.setCreatedAt(resultSet.getTimestamp("created_at"));

		cheque.setCityCode(resultSet.getString("city_code"));

		cheque.setBankCode(resultSet.getString("bank_code"));

		cheque.setBranchCode(resultSet.getString("branch_code"));

		cheque.setChequeImageFront(resultSet.getString("cheque_image_front"));

		cheque.setChequeImageBack(resultSet.getString("cheque_image_back"));

		cheque.setTransactionCode(resultSet.getString("transaction_code"));

		int sequence = resultSet.getInt("item_sequence_number");

		if (!resultSet.wasNull()) {

			cheque.setItemSequenceNumber(sequence);
		}

		return cheque;
	}
}