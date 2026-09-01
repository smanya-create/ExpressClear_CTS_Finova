package com.iispl.cts.daoimpl.outward;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.OutwardCheckerQueueDAO;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.OutwardChequeImage;

public class OutwardCheckerQueueDAOImpl implements OutwardCheckerQueueDAO {

    // ============================================================
    // GET CHEQUES BY BATCH ID
    // ============================================================
	@Override
	public List<OutwardCheque> getChequesByBatchId(String batchId)
	        throws SQLException {

	    List<OutwardCheque> cheques = new ArrayList<>();

	    String sql =
	        "SELECT outward_cheque_id, " +
	        "       outward_batch_id, " +
	        "       cheque_number, " +
	        "       micr_code, " +
	        "       drawee_name, " +
	        "       drawee_account_number, " +
	        "       payee_name, " +
	        "       payee_account_number, " +
	        "       cheque_amount, " +
	        "       cheque_date, " +
	        "       cheque_status, " +
	        "       account_id, " +
	        "       created_at " +
	        "FROM outward_cheque " +
	        "WHERE outward_batch_id = ? " +
	        "ORDER BY outward_cheque_id";

	    try (Connection con = DBConnection.getConnection();
	         PreparedStatement ps = con.prepareStatement(sql)) {

	        ps.setString(1, batchId);

	        System.out.println("Searching cheque for batch = [" + batchId + "]");

	        try (ResultSet rs = ps.executeQuery()) {

	            while (rs.next()) {

	                OutwardCheque cheque = new OutwardCheque();

	                cheque.setOutwardChequeId(
	                    rs.getString("outward_cheque_id")
	                );

	                cheque.setOutwardBatchId(
	                    rs.getString("outward_batch_id")
	                );

	                cheque.setChequeNumber(
	                    rs.getString("cheque_number")
	                );

	                cheque.setMicrCode(
	                    rs.getString("micr_code")
	                );

	                cheque.setDraweeName(
	                    rs.getString("drawee_name")
	                );

	                cheque.setDraweeAccountNumber(
	                    rs.getString("drawee_account_number")
	                );

	                cheque.setPayeeName(
	                    rs.getString("payee_name")
	                );

	                cheque.setPayeeAccountNumber(
	                    rs.getString("payee_account_number")
	                );

	                cheque.setChequeAmount(
	                    rs.getBigDecimal("cheque_amount")
	                );

	                cheque.setChequeDate(
	                    rs.getDate("cheque_date")
	                );

	                cheque.setChequeStatus(
	                    rs.getString("cheque_status")
	                );

	                cheque.setAccountId(
	                    rs.getString("account_id")
	                );

	                cheque.setCreatedAt(
	                    rs.getTimestamp("created_at")
	                );

	                cheques.add(cheque);
	            }
	        }
	    }

	    System.out.println("=================================");
	    System.out.println("DATABASE CHEQUE DATA");
	    System.out.println("Batch ID    = " + batchId);
	    System.out.println("Cheque Count = " + cheques.size());
	    System.out.println("=================================");

	    return cheques;
	}
    // ============================================================
    // UPDATE CHEQUE STATUS
    // ============================================================

    @Override
    public void updateChequeStatus(String chequeNo, String status)
            throws SQLException {

        String sql =
                "UPDATE outward_cheque " +
                "SET cheque_status = ? " +
                "WHERE cheque_number = ?";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, chequeNo);

            int rowsUpdated = ps.executeUpdate();

            System.out.println("=================================");
            System.out.println("CHEQUE STATUS UPDATE");
            System.out.println("Cheque No = " + chequeNo);
            System.out.println("New Status = " + status);
            System.out.println("Rows Updated = " + rowsUpdated);
            System.out.println("=================================");
        }
    }


    // ============================================================
    // GET FRONT / BACK IMAGES
    // ============================================================

    @Override
    public List<OutwardChequeImage> getImagesByChequeId(
            String outwardChequeId) throws SQLException {

        List<OutwardChequeImage> images = new ArrayList<>();

        String sql =
                "SELECT " +
                "outward_image_id, " +
                "outward_cheque_id, " +
                "image_type, " +
                "image_path, " +
                "created_at " +
                "FROM outward_cheque_image " +
                "WHERE outward_cheque_id = ? " +
                "ORDER BY image_type";

        try (Connection connection = DBConnection.getConnection();
             PreparedStatement ps = connection.prepareStatement(sql)) {

            ps.setString(1, outwardChequeId);

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    OutwardChequeImage image =
                            new OutwardChequeImage();

                    image.setOutwardImageId(
                            rs.getString("outward_image_id"));

                    image.setOutwardChequeId(
                            rs.getString("outward_cheque_id"));

                    image.setImageType(
                            rs.getString("image_type"));

                    image.setImagePath(
                            rs.getString("image_path"));

                    image.setCreatedAt(
                            rs.getTimestamp("created_at"));

                    images.add(image);
                }
            }
        }

        System.out.println("=================================");
        System.out.println("DATABASE IMAGE DATA");
        System.out.println("Cheque ID = " + outwardChequeId);
        System.out.println("Image Count = " + images.size());
        System.out.println("=================================");

        return images;
    }
}