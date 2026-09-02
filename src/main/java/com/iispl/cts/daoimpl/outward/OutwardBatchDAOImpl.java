package com.iispl.cts.daoimpl.outward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.OutwardBatchDAO;
import com.iispl.cts.entity.outward.OutwardBatch;

public class OutwardBatchDAOImpl implements OutwardBatchDAO {

	@Override
	public List<OutwardBatch> getRecentBatches() {

		List<OutwardBatch> batchList = new ArrayList<>();

		String sql = "SELECT outward_batch_id, " + "batch_reference_id, " + "actual_cheque_count, "
				+ "actual_total_amount, " + "batch_status, " + "uploaded_by, " + "uploaded_at " + "FROM outward_batch "
				+ "ORDER BY uploaded_at DESC " + "LIMIT 20";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql);
				ResultSet resultSet = preparedStatement.executeQuery()) {

			while (resultSet.next()) {
				batchList.add(mapOutwardBatch(resultSet));
			}

		} catch (Exception exception) {
			throw new RuntimeException("Unable to fetch recent outward batches", exception);
		}

		return batchList;
	}

	@Override
	public List<OutwardBatch> searchBatches(String batchId, String status) {

		List<OutwardBatch> batchList = new ArrayList<>();

		StringBuilder sql = new StringBuilder();

		sql.append(
				"SELECT outward_batch_id, " + "batch_reference_id, " + "actual_cheque_count, " + "actual_total_amount, "
						+ "batch_status, " + "uploaded_by, " + "uploaded_at " + "FROM outward_batch " + "WHERE 1 = 1 ");

		List<String> parameters = new ArrayList<>();

		if (batchId != null && !batchId.trim().isEmpty()) {

			sql.append("AND (" + "LOWER(outward_batch_id) LIKE LOWER(?) " + "OR LOWER(batch_reference_id) LIKE LOWER(?)"
					+ ") ");

			String searchBatchId = "%" + batchId.trim() + "%";

			parameters.add(searchBatchId);
			parameters.add(searchBatchId);
		}

		if (status != null && !status.trim().isEmpty() && !status.equalsIgnoreCase("ALL")) {

			sql.append("AND LOWER(batch_status) = LOWER(?) ");

			parameters.add(status.trim());
		}

		sql.append("ORDER BY uploaded_at DESC LIMIT 20");

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql.toString())) {

			for (int index = 0; index < parameters.size(); index++) {

				preparedStatement.setString(index + 1, parameters.get(index));
			}

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {
					batchList.add(mapOutwardBatch(resultSet));
				}
			}

		} catch (Exception exception) {
			throw new RuntimeException("Unable to search outward batches", exception);
		}

		return batchList;
	}

	@Override
	public OutwardBatch getBatchById(String outwardBatchId) {

		String sql = "SELECT outward_batch_id, " + "batch_reference_id, " + "actual_cheque_count, "
				+ "actual_total_amount, " + "batch_status, " + "uploaded_by, " + "uploaded_at " + "FROM outward_batch "
				+ "WHERE outward_batch_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, outwardBatchId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (resultSet.next()) {
					return mapOutwardBatch(resultSet);
				}
			}

		} catch (Exception exception) {
			throw new RuntimeException("Unable to fetch outward batch", exception);
		}

		return null;
	}

	private OutwardBatch mapOutwardBatch(ResultSet resultSet) throws Exception {

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