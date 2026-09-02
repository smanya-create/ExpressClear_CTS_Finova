package com.iispl.cts.daoimpl.outward;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.OutwardChequeDAO;
import com.iispl.cts.entity.outward.OutwardCheque;

public class OutwardChequeDAOImpl implements OutwardChequeDAO {

	@Override
	public List<OutwardCheque> getChequesByBatchId(String outwardBatchId) {
		List<OutwardCheque> chequeList = new ArrayList<>();

		String sql = "SELECT outward_cheque_id, outward_batch_id, cheque_number, micr_code, "
				+ "drawee_name, drawee_account_number, payee_name, payee_account_number, "
				+ "cheque_amount, cheque_date, cheque_status, account_id, created_at "
				+ "FROM outward_cheque WHERE outward_batch_id = ? ORDER BY created_at ASC";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, outwardBatchId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				while (resultSet.next()) {
					chequeList.add(mapOutwardCheque(resultSet));
				}
			}

		} catch (Exception exception) {
			throw new RuntimeException("Unable to fetch outward cheques", exception);
		}

		return chequeList;
	}

	@Override
	public int getTotalChequeCountByBatchId(String outwardBatchId) {
		String sql = "SELECT COUNT(outward_cheque_id) FROM outward_cheque WHERE outward_batch_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, outwardBatchId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getInt(1);
				}
			}

		} catch (Exception exception) {
			throw new RuntimeException("Unable to fetch total cheque count", exception);
		}

		return 0;
	}

	@Override
	public BigDecimal getTotalChequeAmountByBatchId(String outwardBatchId) {
		String sql = "SELECT COALESCE(SUM(cheque_amount), 0) FROM outward_cheque WHERE outward_batch_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, outwardBatchId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				if (resultSet.next()) {
					return resultSet.getBigDecimal(1);
				}
			}

		} catch (Exception exception) {
			throw new RuntimeException("Unable to fetch total cheque amount", exception);
		}

		return BigDecimal.ZERO;
	}

	private OutwardCheque mapOutwardCheque(ResultSet resultSet) throws Exception {
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

		return outwardCheque;
	}
}