package com.iispl.cts.daoimpl.outward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.OutwardChequeImageDAO;
import com.iispl.cts.entity.outward.OutwardChequeImage;

public class OutwardChequeImageDAOImpl implements OutwardChequeImageDAO {

	@Override
	public List<OutwardChequeImage> getImagesByChequeId(String outwardChequeId) {
		String selectSql =
		        "SELECT "
		        + "oc.outward_cheque_id, "
		        + "oc.outward_batch_id, "
		        + "oc.cheque_number, "
		        + "oc.micr_code, "
		        + "oc.drawee_name, "
		        + "oc.drawee_account_number, "
		        + "oc.payee_name, "
		        + "oc.payee_account_number, "
		        + "oc.cheque_amount, "
		        + "oc.cheque_date, "
		        + "oc.cheque_status, "
		        + "front.image_path AS cheque_image_front, "
		        + "back.image_path AS cheque_image_back "
		        + "FROM outward_cheque oc "
		        + "LEFT JOIN outward_cheque_image front "
		        + "ON oc.outward_cheque_id = front.outward_cheque_id "
		        + "AND front.image_type = 'FRONT' "
		        + "LEFT JOIN outward_cheque_image back "
		        + "ON oc.outward_cheque_id = back.outward_cheque_id "
		        + "AND back.image_type = 'BACK' "
		        + "WHERE oc.outward_batch_id = ?";

		List<OutwardChequeImage> images = new ArrayList<>();

		try (Connection connection = DBConnection.getConnection();
				PreparedStatement prepStmt = connection.prepareStatement(selectSql)) {

			prepStmt.setString(1, outwardChequeId);
			ResultSet resultSet = prepStmt.executeQuery();

			while (resultSet.next()) {

				OutwardChequeImage image = new OutwardChequeImage();
				image.setOutwardImageId(resultSet.getString("outward_image_id"));
				image.setOutwardChequeId(resultSet.getString("outward_cheque_id"));
				image.setImageType(resultSet.getString("image_type"));
				image.setImagePath(resultSet.getString("image_path"));
				image.setCreatedAt(resultSet.getTimestamp("created_at"));
				
				images.add(image);
			}

		} catch (Exception exception) {
			throw new RuntimeException("Failed to fetch images for outward cheque: " + outwardChequeId, exception);
		}
		return images;
	}

}