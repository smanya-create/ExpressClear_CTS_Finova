package com.iispl.cts.daoimpl.outward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.OutwardChequeRequestDAO;
import com.iispl.cts.entity.outward.OutwardChequeRequest;

public class OutwardChequeRequestDAOImpl implements OutwardChequeRequestDAO {

	@Override
	public boolean saveRequest(Connection connection, OutwardChequeRequest request) {

		if (connection == null) {
			throw new IllegalArgumentException("Connection cannot be null");
		}

		if (request == null) {
			throw new IllegalArgumentException("Outward cheque request cannot be null");
		}

		if (request.getChequeId() == null || request.getChequeId().trim().isEmpty()) {
			throw new IllegalArgumentException("Cheque ID cannot be null or empty");
		}

		if (request.getBatchId() == null || request.getBatchId().trim().isEmpty()) {
			throw new IllegalArgumentException("Batch ID cannot be null or empty");
		}

		String sql = "INSERT INTO outward_cheque_request " + "(cheque_id, batch_id, remarks, reason_id, reason) "
				+ "VALUES (?, ?, ?, ?, ?) " + "RETURNING request_id";

		try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, request.getChequeId().trim());
			preparedStatement.setString(2, request.getBatchId().trim());

			if (request.getRemarks() == null || request.getRemarks().trim().isEmpty()) {
				preparedStatement.setNull(3, java.sql.Types.LONGVARCHAR);
			} else {
				preparedStatement.setString(3, request.getRemarks().trim());
			}

			if (request.getReasonId() == null || request.getReasonId().trim().isEmpty()) {
				preparedStatement.setNull(4, java.sql.Types.VARCHAR);
			} else {
				preparedStatement.setString(4, request.getReasonId().trim());
			}

			if (request.getReason() == null || request.getReason().trim().isEmpty()) {
				preparedStatement.setNull(5, java.sql.Types.VARCHAR);
			} else {
				preparedStatement.setString(5, request.getReason().trim());
			}

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (resultSet.next()) {
					request.setRequestId(resultSet.getString("request_id"));
					return true;
				}
			}

			return false;

		} catch (SQLException exception) {

			String message = exception.getMessage();

			throw new RuntimeException("Unable to save outward cheque rejection request. Cheque ID: "
					+ request.getChequeId() + ". Cause: " + (message == null ? "Unknown database error" : message),
					exception);
		}
	}

	@Override
	public boolean existsByChequeId(Connection connection, String chequeId) {

		if (connection == null) {
			throw new IllegalArgumentException("Connection cannot be null");
		}

		if (chequeId == null || chequeId.trim().isEmpty()) {
			return false;
		}

		String sql = "SELECT 1 " + "FROM outward_cheque_request " + "WHERE cheque_id = ? " + "LIMIT 1";

		try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, chequeId.trim());

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				return resultSet.next();
			}

		} catch (SQLException exception) {

			throw new RuntimeException("Unable to check outward cheque rejection request for cheque ID: " + chequeId,
					exception);
		}
	}

	@Override
	public boolean existsByChequeId(String chequeId) {

		if (chequeId == null || chequeId.trim().isEmpty()) {
			return false;
		}

		String sql = "SELECT 1 " + "FROM outward_cheque_request " + "WHERE cheque_id = ? " + "LIMIT 1";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, chequeId.trim());

			try (ResultSet resultSet = preparedStatement.executeQuery()) {
				return resultSet.next();
			}

		} catch (SQLException exception) {

			throw new RuntimeException("Unable to check outward cheque rejection request for cheque ID: " + chequeId,
					exception);
		}
	}

	@Override
	public OutwardChequeRequest getRequestByChequeId(String chequeId) {

		if (chequeId == null || chequeId.trim().isEmpty()) {
			return null;
		}

		String sql = "SELECT " + "request_id, " + "cheque_id, " + "batch_id, " + "remarks, " + "reason_id, "
				+ "reason, " + "time_stamp " + "FROM outward_cheque_request " + "WHERE cheque_id = ? "
				+ "ORDER BY time_stamp DESC " + "LIMIT 1";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, chequeId.trim());

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (resultSet.next()) {
					return mapRequest(resultSet);
				}
			}

		} catch (SQLException exception) {

			throw new RuntimeException("Unable to fetch outward cheque rejection request for cheque ID: " + chequeId,
					exception);
		}

		return null;
	}

	@Override
	public List<OutwardChequeRequest> getRequestsByBatchId(String batchId) {

		if (batchId == null || batchId.trim().isEmpty()) {
			return Collections.emptyList();
		}

		List<OutwardChequeRequest> requests = new ArrayList<>();

		String sql = "SELECT " + "request_id, " + "cheque_id, " + "batch_id, " + "remarks, " + "reason_id, "
				+ "reason, " + "time_stamp " + "FROM outward_cheque_request " + "WHERE batch_id = ? "
				+ "ORDER BY time_stamp DESC, request_id";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, batchId.trim());

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				while (resultSet.next()) {
					requests.add(mapRequest(resultSet));
				}
			}

		} catch (SQLException exception) {

			throw new RuntimeException("Unable to fetch outward cheque rejection requests for batch ID: " + batchId,
					exception);
		}

		return requests;
	}

	private OutwardChequeRequest mapRequest(ResultSet resultSet) throws SQLException {

		OutwardChequeRequest request = new OutwardChequeRequest();

		request.setRequestId(resultSet.getString("request_id"));
		request.setChequeId(resultSet.getString("cheque_id"));
		request.setBatchId(resultSet.getString("batch_id"));
		request.setRemarks(resultSet.getString("remarks"));
		request.setReasonId(resultSet.getString("reason_id"));
		request.setReason(resultSet.getString("reason"));

		return request;
	}

	@Override
	public boolean saveRejectionRequest(Connection connection, String chequeId, String batchId, String remarks,
			String reasonId, String reason) {

		if (connection == null) {
			throw new IllegalArgumentException("Connection cannot be null");
		}

		if (chequeId == null || chequeId.trim().isEmpty()) {
			throw new IllegalArgumentException("Cheque ID cannot be null or empty");
		}

		if (batchId == null || batchId.trim().isEmpty()) {
			throw new IllegalArgumentException("Batch ID cannot be null or empty");
		}

		String sql = "INSERT INTO outward_cheque_request " + "(cheque_id, batch_id, remarks, reason_id, reason) "
				+ "VALUES (?, ?, ?, ?, ?)";

		try (PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, chequeId.trim());
			preparedStatement.setString(2, batchId.trim());
			preparedStatement.setString(3, remarks);

			if (reasonId != null && !reasonId.trim().isEmpty()) {
				preparedStatement.setString(4, reasonId.trim());
			} else {
				preparedStatement.setNull(4, java.sql.Types.VARCHAR);
			}

			preparedStatement.setString(5, reason);

			return preparedStatement.executeUpdate() == 1;

		} catch (SQLException exception) {

			String message = exception.getMessage();

			throw new RuntimeException("Unable to save rejection request for cheque: " + chequeId + ". Cause: "
					+ (message == null ? "Unknown database error" : message), exception);
		}
	}
}