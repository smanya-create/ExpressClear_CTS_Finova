package com.iispl.cts.daoimpl.inward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.inward.InwardChequeDAO;
import com.iispl.cts.entity.inward.InwardCheque;

public class InwardChequeDAOImpl implements InwardChequeDAO {

	@Override
	public List<InwardCheque> getAllCheques() {
		List<InwardCheque> cheques = new ArrayList<>();

		String sql = "SELECT inward_cheque_id, inward_batch_id, " + "cheque_number, micr_code, drawee_name, "
				+ "drawee_account_number, payee_name, " + "payee_account_number, cheque_amount, "
				+ "cheque_date, cheque_status, account_id, created_at " + "FROM inward_cheque "
				+ "ORDER BY created_at DESC";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {

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

				cheques.add(cheque);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return cheques;
	}

	@Override
	public InwardCheque getChequeById(String inwardChequeId) {

		String sql = "SELECT inward_cheque_id, inward_batch_id, " + "cheque_number, micr_code, drawee_name, "
				+ "drawee_account_number, payee_name, " + "payee_account_number, cheque_amount, "
				+ "cheque_date, cheque_status, account_id, created_at " + "FROM inward_cheque "
				+ "WHERE inward_cheque_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, inwardChequeId);

			try (ResultSet resultSet = statement.executeQuery()) {

				if (resultSet.next()) {

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

					return cheque;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}

	@Override
	public List<InwardCheque> getChequesByBatchId(String inwardBatchId) {

		List<InwardCheque> cheques = new ArrayList<>();

		String sql = "SELECT inward_cheque_id, inward_batch_id, " + "cheque_number, micr_code, drawee_name, "
				+ "drawee_account_number, payee_name, " + "payee_account_number, cheque_amount, "
				+ "cheque_date, cheque_status, account_id, created_at " + "FROM inward_cheque "
				+ "WHERE inward_batch_id = ? " + "ORDER BY created_at";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, inwardBatchId);

			try (ResultSet resultSet = statement.executeQuery()) {

				while (resultSet.next()) {

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

					cheques.add(cheque);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return cheques;
	}

	@Override
	public boolean saveCheque(InwardCheque inwardCheque) {

		String sql = "INSERT INTO inward_cheque " + "(inward_cheque_id, inward_batch_id, "
				+ "cheque_number, micr_code, drawee_name, " + "drawee_account_number, payee_name, "
				+ "payee_account_number, cheque_amount, " + "cheque_date, cheque_status, account_id, created_at) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

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

			return statement.executeUpdate() > 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean updateCheque(InwardCheque inwardCheque) {

		String sql = "UPDATE inward_cheque SET " + "inward_batch_id = ?, " + "cheque_number = ?, " + "micr_code = ?, "
				+ "drawee_name = ?, " + "drawee_account_number = ?, " + "payee_name = ?, "
				+ "payee_account_number = ?, " + "cheque_amount = ?, " + "cheque_date = ?, " + "cheque_status = ?, "
				+ "account_id = ?, " + "created_at = ? " + "WHERE inward_cheque_id = ?";

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
			statement.setString(13, inwardCheque.getInwardChequeId());

			return statement.executeUpdate() > 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean deleteCheque(String inwardChequeId) {

		String sql = "DELETE FROM inward_cheque " + "WHERE inward_cheque_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, inwardChequeId);

			return statement.executeUpdate() > 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}