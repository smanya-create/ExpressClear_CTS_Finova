package com.iispl.cts.daoimpl.outward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.OutwardBatchDashboardDAO;
import com.iispl.cts.entity.outward.OutwardBatch;

public class OutwardBatchDashboardDAOImpl implements OutwardBatchDashboardDAO {

	@Override
	public List<OutwardBatch> searchPendingBatches(int pageNumber, int pageSize, String batchId, String status) {

		List<OutwardBatch> batches = new ArrayList<>();

		StringBuilder sql = new StringBuilder("SELECT outward_batch_id, batch_reference_id, actual_cheque_count, "
				+ "actual_total_amount, batch_status, uploaded_by, uploaded_at " + "FROM outward_batch "
				+ "WHERE 1 = 1 ");

		List<String> parameters = new ArrayList<>();

		if (batchId != null && !batchId.trim().isEmpty()) {
			sql.append("AND UPPER(outward_batch_id) LIKE UPPER(?) ");
			parameters.add("%" + batchId.trim() + "%");
		}

		if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {

			sql.append("AND UPPER(TRIM(batch_status)) = UPPER(TRIM(?)) ");
			parameters.add(status.trim());

		} else {

			sql.append("AND UPPER(TRIM(batch_status)) IN (?, ?) ");

			parameters.add("PENDING_CHECKER_PROCESS");
			parameters.add("ON_HOLD");
		}

		sql.append("ORDER BY uploaded_at DESC " + "LIMIT ? OFFSET ?");

		int offset = (pageNumber - 1) * pageSize;

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

			int parameterIndex = 1;

			for (String parameter : parameters) {
				preparedStatement.setString(parameterIndex++, parameter);
			}

			preparedStatement.setInt(parameterIndex++, pageSize);

			preparedStatement.setInt(parameterIndex, offset);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {
					batches.add(mapOutwardBatch(resultSet));
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return batches;
	}

	@Override
	public int getSearchPendingBatchCount(String batchId, String status) {

		StringBuilder sql = new StringBuilder("SELECT COUNT(*) " + "FROM outward_batch " + "WHERE 1 = 1 ");

		List<String> parameters = new ArrayList<>();

		if (batchId != null && !batchId.trim().isEmpty()) {
			sql.append("AND UPPER(outward_batch_id) LIKE UPPER(?) ");
			parameters.add("%" + batchId.trim() + "%");
		}

		if (status != null && !status.trim().isEmpty() && !"ALL".equalsIgnoreCase(status)) {

			sql.append("AND UPPER(TRIM(batch_status)) = UPPER(TRIM(?)) ");
			parameters.add(status.trim());

		} else {

			sql.append("AND UPPER(TRIM(batch_status)) IN (?, ?) ");

			parameters.add("PENDING_CHECKER_PROCESS");
			parameters.add("ON_HOLD");
		}

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

			int parameterIndex = 1;

			for (String parameter : parameters) {
				preparedStatement.setString(parameterIndex++, parameter);
			}

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (resultSet.next()) {
					return resultSet.getInt(1);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return 0;
	}

	private OutwardBatch mapOutwardBatch(ResultSet resultSet) throws SQLException {

		OutwardBatch outwardBatch = new OutwardBatch();

		outwardBatch.setOutwardBatchId(resultSet.getString("outward_batch_id"));

		outwardBatch.setBatchReferenceId(resultSet.getString("batch_reference_id"));

		outwardBatch.setActualChequeCount(resultSet.getInt("actual_cheque_count"));

		outwardBatch.setActualTotalAmount(resultSet.getBigDecimal("actual_total_amount"));

		outwardBatch.setBatchStatus(resultSet.getString("batch_status"));

		outwardBatch.setUploadedBy(resultSet.getString("uploaded_by"));

		outwardBatch.setUploadedAt(resultSet.getTimestamp("uploaded_at"));

		return outwardBatch;
	}

}