package com.iispl.cts.daoimpl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.SendBackReasonDAO;
import com.iispl.cts.entity.SendBackReason;

public class SendBackReasonDAOImpl implements SendBackReasonDAO {

	private static final String SQL_SELECT_ALL = "SELECT reason_id, reason_code, reason_name, reason_description "
			+ "FROM send_back_reason " + "ORDER BY reason_id ASC";

	@Override
	public List<SendBackReason> getAllSendBackReasons() {

		List<SendBackReason> reasons = new ArrayList<>();

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(SQL_SELECT_ALL);
				ResultSet resultSet = preparedStatement.executeQuery()) {

			while (resultSet.next()) {

				SendBackReason reason = new SendBackReason();

				reason.setReasonId(resultSet.getInt("reason_id"));
				reason.setReasonCode(resultSet.getString("reason_code"));
				reason.setReasonName(resultSet.getString("reason_name"));
				reason.setReasonDescription(resultSet.getString("reason_description"));

				reasons.add(reason);
			}

			return reasons;

		} catch (Exception exception) {

			String message = exception.getMessage();

			throw new RuntimeException("Unable to fetch send back reasons. Cause: "
					+ (message == null ? "Unknown database error" : message), exception);
		}
	}
}