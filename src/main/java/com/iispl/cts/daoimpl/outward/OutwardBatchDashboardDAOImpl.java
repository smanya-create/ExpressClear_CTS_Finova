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
	public List<OutwardBatch> searchPendingBatches(
			int pageNumber,
			int pageSize,
			String batchId) {

		List<OutwardBatch> batches = new ArrayList<>();

		if (pageNumber < 1) {
			pageNumber = 1;
		}

		if (pageSize < 1) {
			pageSize = 5;
		}

		StringBuilder sql = new StringBuilder();

		sql.append("SELECT outward_batch_id, batch_reference_id, ");
		sql.append("actual_cheque_count, actual_total_amount, ");
		sql.append("batch_status, uploaded_by, uploaded_at ");
		sql.append("FROM outward_batch ");
		sql.append("WHERE UPPER(TRIM(batch_status)) IN (?, ?) ");

		if (batchId != null && !batchId.trim().isEmpty()) {

			sql.append("AND UPPER(outward_batch_id) LIKE UPPER(?) ");
		}

		sql.append("ORDER BY uploaded_at DESC ");
		sql.append("LIMIT ? OFFSET ?");

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement =
						connection.prepareStatement(sql.toString())) {

			int parameterIndex = 1;

			preparedStatement.setString(
					parameterIndex++,
					"PENDING_CHECKER_PROCESS");

			preparedStatement.setString(
					parameterIndex++,
					"ON_HOLD");

			if (batchId != null && !batchId.trim().isEmpty()) {

				preparedStatement.setString(
						parameterIndex++,
						"%" + batchId.trim() + "%");
			}

			int offset =
					(pageNumber - 1) * pageSize;

			preparedStatement.setInt(
					parameterIndex++,
					pageSize);

			preparedStatement.setInt(
					parameterIndex++,
					offset);

			try (ResultSet resultSet =
					preparedStatement.executeQuery()) {

				while (resultSet.next()) {

					batches.add(
							mapOutwardBatch(resultSet));
				}
			}

		} catch (SQLException exception) {

			throw new RuntimeException(
					"Unable to search pending checker batches",
					exception);
		}

		return batches;
	}

	private OutwardBatch mapOutwardBatch(
			ResultSet resultSet) throws SQLException {

		OutwardBatch outwardBatch =
				new OutwardBatch();

		outwardBatch.setOutwardBatchId(
				resultSet.getString(
						"outward_batch_id"));

		outwardBatch.setBatchReferenceId(
				resultSet.getString(
						"batch_reference_id"));

		outwardBatch.setActualChequeCount(
				resultSet.getInt(
						"actual_cheque_count"));

		outwardBatch.setActualTotalAmount(
				resultSet.getBigDecimal(
						"actual_total_amount"));

		outwardBatch.setBatchStatus(
				resultSet.getString(
						"batch_status"));

		outwardBatch.setUploadedBy(
				resultSet.getString(
						"uploaded_by"));

		outwardBatch.setUploadedAt(
				resultSet.getTimestamp(
						"uploaded_at"));

		return outwardBatch;
	}

	@Override
	public int getSearchPendingBatchCount(
			String batchId) {

		StringBuilder sql =
				new StringBuilder(
						"SELECT COUNT(*) "
						+ "FROM outward_batch "
						+ "WHERE UPPER(TRIM(batch_status)) IN (?, ?) ");

		if (batchId != null && !batchId.trim().isEmpty()) {

			sql.append(
					"AND UPPER(outward_batch_id) LIKE UPPER(?) ");
		}

		try (Connection connection =
				DBConnection.getConnection();
				PreparedStatement preparedStatement =
						connection.prepareStatement(
								sql.toString())) {

			int parameterIndex = 1;

			preparedStatement.setString(
					parameterIndex++,
					"PENDING_CHECKER_PROCESS");

			preparedStatement.setString(
					parameterIndex++,
					"ON_HOLD");

			if (batchId != null && !batchId.trim().isEmpty()) {

				preparedStatement.setString(
						parameterIndex++,
						"%" + batchId.trim() + "%");
			}

			try (ResultSet resultSet =
					preparedStatement.executeQuery()) {

				if (resultSet.next()) {

					return resultSet.getInt(1);
				}
			}

		} catch (SQLException exception) {

			throw new RuntimeException(
					"Unable to count searched checker batches",
					exception);
		}

		return 0;
	}
}