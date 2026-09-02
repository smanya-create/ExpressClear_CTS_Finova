package com.iispl.cts.daoimpl.inward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.inward.InwardChequeImageDAO;
import com.iispl.cts.entity.inward.InwardChequeImage;

public class InwardChequeImageDAOImpl implements InwardChequeImageDAO {

	@Override
	public List<InwardChequeImage> getAllImages() {

		List<InwardChequeImage> images = new ArrayList<>();

		String sql = "SELECT inward_image_id, inward_cheque_id, " + "image_type, image_path, created_at "
				+ "FROM inward_cheque_image " + "ORDER BY created_at DESC";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql);
				ResultSet resultSet = statement.executeQuery()) {

			while (resultSet.next()) {

				InwardChequeImage image = new InwardChequeImage();

				image.setInwardImageId(resultSet.getString("inward_image_id"));

				image.setInwardChequeId(resultSet.getString("inward_cheque_id"));

				image.setImageType(resultSet.getString("image_type"));

				image.setImagePath(resultSet.getString("image_path"));

				image.setCreatedAt(resultSet.getTimestamp("created_at"));

				images.add(image);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return images;
	}

	@Override
	public InwardChequeImage getImageById(String inwardImageId) {

		String sql = "SELECT inward_image_id, inward_cheque_id, " + "image_type, image_path, created_at "
				+ "FROM inward_cheque_image " + "WHERE inward_image_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, inwardImageId);

			try (ResultSet resultSet = statement.executeQuery()) {

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

	@Override
	public List<InwardChequeImage> getImagesByChequeId(String inwardChequeId) {

		List<InwardChequeImage> images = new ArrayList<>();

		String sql = "SELECT inward_image_id, inward_cheque_id, " + "image_type, image_path, created_at "
				+ "FROM inward_cheque_image " + "WHERE inward_cheque_id = ? " + "ORDER BY image_type";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, inwardChequeId);

			try (ResultSet resultSet = statement.executeQuery()) {

				while (resultSet.next()) {

					InwardChequeImage image = new InwardChequeImage();

					image.setInwardImageId(resultSet.getString("inward_image_id"));

					image.setInwardChequeId(resultSet.getString("inward_cheque_id"));

					image.setImageType(resultSet.getString("image_type"));

					image.setImagePath(resultSet.getString("image_path"));

					image.setCreatedAt(resultSet.getTimestamp("created_at"));

					images.add(image);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
		}

		return images;
	}

	@Override
	public boolean saveImage(InwardChequeImage inwardChequeImage) {

		String sql = "INSERT INTO inward_cheque_image "
				+ "(inward_image_id, inward_cheque_id, image_type, image_path, created_at) " + "VALUES (?, ?, ?, ?, ?)";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, inwardChequeImage.getInwardImageId());
			statement.setString(2, inwardChequeImage.getInwardChequeId());
			statement.setString(3, inwardChequeImage.getImageType());
			statement.setString(4, inwardChequeImage.getImagePath());
			statement.setTimestamp(5, inwardChequeImage.getCreatedAt());

			return statement.executeUpdate() > 0;

		} catch (Exception e) {

			throw new RuntimeException("Failed to save image for cheque " + inwardChequeImage.getInwardChequeId()
					+ ". Image ID: " + inwardChequeImage.getInwardImageId() + ". Image Type: "
					+ inwardChequeImage.getImageType() + ". Image Path: " + inwardChequeImage.getImagePath()
					+ ". Database Error: " + e.getMessage(), e);
		}
	}

	@Override
	public boolean updateImage(InwardChequeImage inwardChequeImage) {

		String sql = "UPDATE inward_cheque_image SET " + "inward_cheque_id = ?, " + "image_type = ?, "
				+ "image_path = ?, " + "created_at = ? " + "WHERE inward_image_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {

			statement.setString(1, inwardChequeImage.getInwardChequeId());

			statement.setString(2, inwardChequeImage.getImageType());

			statement.setString(3, inwardChequeImage.getImagePath());

			statement.setTimestamp(4, inwardChequeImage.getCreatedAt());

			statement.setString(5, inwardChequeImage.getInwardImageId());

			return statement.executeUpdate() > 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}

	@Override
	public boolean deleteImage(String inwardImageId) {

		String sql = "DELETE FROM inward_cheque_image " + "WHERE inward_image_id = ?";

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement statement = connection.prepareStatement(sql)) {
			statement.setString(1, inwardImageId);
			return statement.executeUpdate() > 0;

		} catch (Exception e) {
			e.printStackTrace();
			return false;
		}
	}
}