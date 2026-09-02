package com.iispl.cts.daoimpl.inward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

import com.iispl.cts.dao.inward.InwardChequeImageDAO;
import com.iispl.cts.entity.inward.InwardChequeImage;
import com.iispl.cts.common.config.DBConnection;

public class InwardChequeImageDAOImpl implements InwardChequeImageDAO {

	private static InwardChequeImageDAOImpl instance;

	private InwardChequeImageDAOImpl() {
	}

	public static synchronized InwardChequeImageDAOImpl getInstance() {
		if (instance == null) {
			instance = new InwardChequeImageDAOImpl();
		}
		return instance;
	}

	@Override
	public InwardChequeImage findFrontImageByChequeId(String inwardChequeId) {

		String sql = "SELECT inward_image_id, inward_cheque_id, image_type, " + "image_path, created_at "
				+ "FROM inward_cheque_image " + "WHERE inward_cheque_id = ? " + "AND LOWER(image_type) = 'front' "
				+ "LIMIT 1";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement preparedStatement = connection.prepareStatement(sql)) {

			preparedStatement.setString(1, inwardChequeId);

			try (ResultSet resultSet = preparedStatement.executeQuery()) {

				if (resultSet.next()) {

					InwardChequeImage image = new InwardChequeImage();

					image.setInwardImageId(resultSet.getString("inward_image_id"));

					image.setInwardChequeId(resultSet.getString("inward_cheque_id"));

					image.setImageType(resultSet.getString("image_type"));

					image.setImagePath(resultSet.getString("image_path"));

					image.setCreatedAt(resultSet.getTimestamp("created_at"));

					return image;
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return null;
	}
}