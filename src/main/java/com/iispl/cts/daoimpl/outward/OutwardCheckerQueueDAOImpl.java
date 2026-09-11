package com.iispl.cts.daoimpl.outward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.OutwardCheckerQueueDAO;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.OutwardChequeImage;
import com.iispl.cts.entity.outward.OutwardRejectedCheques;
import com.iispl.cts.entity.outward.RejectedReason;
import com.iispl.cts.entity.outward.SendBackReason;

public class OutwardCheckerQueueDAOImpl
        implements OutwardCheckerQueueDAO {


    // ============================================================
    // GET CHEQUES BY SELECTED BATCH
    // ============================================================

    @Override
    public List<OutwardCheque> getChequesByBatchId(
            String batchId) throws SQLException {

        List<OutwardCheque> cheques = new ArrayList<>();

        String sql =
                "SELECT outward_cheque_id, "
              + "       outward_batch_id, "
              + "       cheque_number, "
              + "       micr_code, "
              + "       drawee_name, "
              + "       drawee_account_number, "
              + "       payee_name, "
              + "       payee_account_number, "
              + "       cheque_amount, "
              + "       cheque_date, "
              + "       cheque_status, "
              + "       account_id, "
              + "       created_at, "
              + "       city_code, "
              + "       bank_code, "
              + "       branch_code, "
              + "       cheque_image_front, "
              + "       cheque_image_back "
              + "FROM outward_cheque "
              + "WHERE outward_batch_id = ? "
              + "AND cheque_status = 'PENDING_VERIFICATION' "
              + "ORDER BY outward_cheque_id";

        try (Connection con = DBConnection.getConnection();
             PreparedStatement ps = con.prepareStatement(sql)) {

            ps.setString(1, batchId);

            System.out.println("=================================");
            System.out.println("GET CHEQUES BY BATCH");
            System.out.println("Batch ID = [" + batchId + "]");

            try (ResultSet rs = ps.executeQuery()) {

                while (rs.next()) {

                    OutwardCheque cheque =
                            new OutwardCheque();

                    cheque.setOutwardChequeId(
                            rs.getString(
                                    "outward_cheque_id"));

                    cheque.setOutwardBatchId(
                            rs.getString(
                                    "outward_batch_id"));

                    cheque.setChequeNumber(
                            rs.getString(
                                    "cheque_number"));

                    cheque.setMicrCode(
                            rs.getString(
                                    "micr_code"));

                    cheque.setDraweeName(
                            rs.getString(
                                    "drawee_name"));

                    cheque.setDraweeAccountNumber(
                            rs.getString(
                                    "drawee_account_number"));

                    cheque.setPayeeName(
                            rs.getString(
                                    "payee_name"));

                    cheque.setPayeeAccountNumber(
                            rs.getString(
                                    "payee_account_number"));

                    cheque.setChequeAmount(
                            rs.getBigDecimal(
                                    "cheque_amount"));

                    cheque.setChequeDate(
                            rs.getDate(
                                    "cheque_date"));

                    cheque.setChequeStatus(
                            rs.getString(
                                    "cheque_status"));

                    cheque.setAccountId(
                            rs.getString(
                                    "account_id"));

                    cheque.setCreatedAt(
                            rs.getTimestamp(
                                    "created_at"));

                    // FRONT IMAGE
                    cheque.setChequeImageFront(
                            rs.getString(
                                    "cheque_image_front"));

                    // BACK IMAGE
                    cheque.setChequeImageBack(
                            rs.getString(
                                    "cheque_image_back"));

                    cheques.add(cheque);
                }
            }
        }

        System.out.println(
                "Cheque Count for selected batch = "
                + cheques.size());

        System.out.println("=================================");

        return cheques;
    }


    // ============================================================
    // GET BATCH STATUS
    // ============================================================

    @Override
    public String getBatchStatus(
            String batchId) throws SQLException {

        String sql =
                "SELECT batch_status "
              + "FROM outward_batch "
              + "WHERE outward_batch_id = ?";

        try (Connection con =
                     DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, batchId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                if (rs.next()) {

                    String status =
                            rs.getString("batch_status");

                    System.out.println(
                            "=================================");

                    System.out.println(
                            "BATCH STATUS");

                    System.out.println(
                            "Batch ID = " + batchId);

                    System.out.println(
                            "Batch Status = [" + status + "]");

                    System.out.println(
                            "=================================");

                    return status;
                }
            }
        }

        System.out.println(
                "No batch found for Batch ID = "
                + batchId);

        return null;
    }


    // ============================================================
    // UPDATE CHEQUE STATUS
    // ============================================================

    @Override
    public void updateChequeStatus(
            String chequeNo,
            String status) throws SQLException {

        String sql =
                "UPDATE outward_cheque "
              + "SET cheque_status = ? "
              + "WHERE cheque_number = ?";

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, chequeNo);

            int rowsUpdated =
                    ps.executeUpdate();

            System.out.println(
                    "=================================");

            System.out.println(
                    "CHEQUE STATUS UPDATE");

            System.out.println(
                    "Cheque No = " + chequeNo);

            System.out.println(
                    "New Status = " + status);

            System.out.println(
                    "Rows Updated = " + rowsUpdated);

            System.out.println(
                    "=================================");
        }
    }


    // ============================================================
    // REJECT CHEQUE
    //
    // IMPORTANT:
    //
    // username = ochecker
    //
    // users table:
    //
    // USR1003 | ochecker
    //
    // Therefore rejected_by must be:
    //
    // USR1003
    //
    // NOT:
    //
    // ochecker
    //
    // ============================================================

    @Override
    public void rejectCheque(
            String chequeNo,
            String username,
            String remarks)
            throws SQLException {

        Connection con = null;

        try {

            con = DBConnection.getConnection();

            // ====================================================
            // START TRANSACTION
            // ====================================================

            con.setAutoCommit(false);


            // ====================================================
            // STEP 1
            // GET USER ID FROM USERNAME
            // ====================================================

            String userSql =
                    "SELECT user_id "
                  + "FROM users "
                  + "WHERE username = ? "
                  + "AND status = 'ACTIVE'";

            String userId = null;

            try (PreparedStatement ps =
                         con.prepareStatement(userSql)) {

                ps.setString(1, username);

                try (ResultSet rs =
                             ps.executeQuery()) {

                    if (rs.next()) {

                        userId =
                                rs.getString("user_id");
                    }
                }
            }


            // ====================================================
            // USER NOT FOUND
            // ====================================================

            if (userId == null ||
                userId.trim().isEmpty()) {

                throw new SQLException(
                        "Active user not found for username: "
                        + username);
            }


            System.out.println(
                    "=================================");

            System.out.println(
                    "REJECT CHEQUE - USER");

            System.out.println(
                    "Username = " + username);

            System.out.println(
                    "User ID = " + userId);

            System.out.println(
                    "=================================");


            // ====================================================
            // STEP 2
            // GET CHEQUE DETAILS
            // ====================================================

            String chequeSql =
                    "SELECT outward_cheque_id, "
                  + "       outward_batch_id, "
                  + "       cheque_amount "
                  + "FROM outward_cheque "
                  + "WHERE cheque_number = ?";

            String outwardChequeId = null;
            String outwardBatchId = null;
            java.math.BigDecimal chequeAmount = null;

            try (PreparedStatement ps =
                         con.prepareStatement(chequeSql)) {

                ps.setString(1, chequeNo);

                try (ResultSet rs =
                             ps.executeQuery()) {

                    if (rs.next()) {

                        outwardChequeId =
                                rs.getString(
                                        "outward_cheque_id");

                        outwardBatchId =
                                rs.getString(
                                        "outward_batch_id");

                        chequeAmount =
                                rs.getBigDecimal(
                                        "cheque_amount");
                    }
                }
            }


            // ====================================================
            // CHEQUE NOT FOUND
            // ====================================================

            if (outwardChequeId == null ||
                outwardChequeId.trim().isEmpty()) {

                throw new SQLException(
                        "Cheque not found: "
                        + chequeNo);
            }


            System.out.println(
                    "=================================");

            System.out.println(
                    "REJECT CHEQUE - CHEQUE DATA");

            System.out.println(
                    "Cheque Number = "
                    + chequeNo);

            System.out.println(
                    "Outward Cheque ID = "
                    + outwardChequeId);

            System.out.println(
                    "Outward Batch ID = "
                    + outwardBatchId);

            System.out.println(
                    "Cheque Amount = "
                    + chequeAmount);

            System.out.println(
                    "=================================");


            // ====================================================
            // STEP 3
            // INSERT INTO OUTWARD_REJECTED_CHEQUES
            //
            // rejected_date is NOT included because PostgreSQL
            // automatically uses CURRENT_TIMESTAMP.
            // ====================================================

            String insertSql =
                    "INSERT INTO outward_rejected_cheques "
                  + "(outward_cheque_id, "
                  + " rejected_by, "
                  + " remarks, "
                  + " outward_batch_id, "
                  + " cheque_amount) "
                  + "VALUES (?, ?, ?, ?, ?)";

            try (PreparedStatement ps =
                         con.prepareStatement(insertSql)) {

                ps.setString(
                        1,
                        outwardChequeId);

                // IMPORTANT:
                // This is USR1003, not ochecker.
                ps.setString(
                        2,
                        userId);

                ps.setString(
                        3,
                        remarks);

                ps.setString(
                        4,
                        outwardBatchId);

                ps.setBigDecimal(
                        5,
                        chequeAmount);

                int rowsInserted =
                        ps.executeUpdate();

                System.out.println(
                        "=================================");

                System.out.println(
                        "REJECTED CHEQUE INSERT");

                System.out.println(
                        "Rows Inserted = "
                        + rowsInserted);

                System.out.println(
                        "Rejected By User ID = "
                        + userId);

                System.out.println(
                        "=================================");
            }


            // ====================================================
            // STEP 4
            // CHANGE OUTWARD CHEQUE STATUS
            // ====================================================

            String updateSql =
                    "UPDATE outward_cheque "
                  + "SET cheque_status = 'REJECTED' "
                  + "WHERE cheque_number = ?";

            try (PreparedStatement ps =
                         con.prepareStatement(updateSql)) {

                ps.setString(1, chequeNo);

                int rowsUpdated =
                        ps.executeUpdate();

                if (rowsUpdated == 0) {

                    throw new SQLException(
                            "Unable to update cheque status "
                            + "for cheque: "
                            + chequeNo);
                }

                System.out.println(
                        "=================================");

                System.out.println(
                        "CHEQUE STATUS CHANGED");

                System.out.println(
                        "Cheque Number = "
                        + chequeNo);

                System.out.println(
                        "Status = REJECTED");

                System.out.println(
                        "Rows Updated = "
                        + rowsUpdated);

                System.out.println(
                        "=================================");
            }


            // ====================================================
            // STEP 5
            // COMMIT
            // ====================================================

            con.commit();

            System.out.println(
                    "=================================");

            System.out.println(
                    "REJECT TRANSACTION SUCCESS");

            System.out.println(
                    "Cheque = " + chequeNo);

            System.out.println(
                    "User = " + username);

            System.out.println(
                    "User ID = " + userId);

            System.out.println(
                    "Status = REJECTED");

            System.out.println(
                    "=================================");


        } catch (SQLException e) {

            // ====================================================
            // ROLLBACK
            // ====================================================

            if (con != null) {

                try {

                    con.rollback();

                    System.out.println(
                            "Reject transaction rolled back.");

                } catch (SQLException rollbackException) {

                    rollbackException.printStackTrace();
                }
            }

            throw e;

        } finally {

            // ====================================================
            // CLOSE CONNECTION
            // ====================================================

            if (con != null) {

                try {

                    con.setAutoCommit(true);

                    con.close();

                } catch (SQLException closeException) {

                    closeException.printStackTrace();
                }
            }
        }
    }


    // ============================================================
    // GET FRONT / BACK IMAGES
    // ============================================================

    @Override
    public List<OutwardChequeImage> getImagesByChequeId(
            String outwardChequeId)
            throws SQLException {

        List<OutwardChequeImage> images =
                new ArrayList<>();

        String sql =
                "SELECT outward_image_id, "
              + "       outward_cheque_id, "
              + "       image_type, "
              + "       image_path, "
              + "       created_at "
              + "FROM outward_cheque_image "
              + "WHERE outward_cheque_id = ? "
              + "ORDER BY image_type";

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setString(1, outwardChequeId);

            try (ResultSet rs =
                         ps.executeQuery()) {

                while (rs.next()) {

                    OutwardChequeImage image =
                            new OutwardChequeImage();

                    image.setOutwardImageId(
                            rs.getString(
                                    "outward_image_id"));

                    image.setOutwardChequeId(
                            rs.getString(
                                    "outward_cheque_id"));

                    image.setImageType(
                            rs.getString(
                                    "image_type"));

                    image.setImagePath(
                            rs.getString(
                                    "image_path"));

                    image.setCreatedAt(
                            rs.getTimestamp(
                                    "created_at"));

                    images.add(image);
                }
            }
        }

        System.out.println(
                "=================================");

        System.out.println(
                "DATABASE IMAGE DATA");

        System.out.println(
                "Cheque ID = "
                + outwardChequeId);

        System.out.println(
                "Image Count = "
                + images.size());

        System.out.println(
                "=================================");

        return images;
    }


    // ============================================================
    // GET SEND BACK REASONS
    // ============================================================

    @Override
    public List<SendBackReason> getSendBackReasons()
            throws SQLException {

        List<SendBackReason> reasons =
                new ArrayList<>();

        String sql =
                "SELECT reason_id, "
              + "       reason_code, "
              + "       reason_name, "
              + "       reason_description "
              + "FROM send_back_reason "
              + "ORDER BY reason_id";

        try (Connection con =
                     DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql);
             ResultSet rs =
                     ps.executeQuery()) {

            while (rs.next()) {

                SendBackReason reason =
                        new SendBackReason();

                reason.setReasonId(
                        rs.getString("reason_id"));

                reason.setReasonCode(
                        rs.getString("reason_code"));

                reason.setReasonName(
                        rs.getString("reason_name"));

                reason.setReasonDescription(
                        rs.getString(
                                "reason_description"));

                reasons.add(reason);
            }
        }

        System.out.println(
                "Send Back Reasons Loaded = "
                + reasons.size());

        return reasons;
    }


    // ============================================================
    // CHECK PAYEE ACCOUNT
    // ============================================================

    @Override
    public boolean isPayeeAccountExists(
            String accountNumber)
            throws SQLException {

        String sql =
                "SELECT 1 "
              + "FROM master_account "
              + "WHERE account_number = ? "
              + "LIMIT 1";

        try (Connection con =
                     DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql)) {

            ps.setString(1, accountNumber);

            try (ResultSet rs =
                         ps.executeQuery()) {

                return rs.next();
            }
        }
    }


    // ============================================================
    // UPDATE BATCH STATUS
    // ============================================================

    @Override
    public void updateBatchStatus(
            String batchId,
            String status)
            throws SQLException {

        String sql =
                "UPDATE outward_batch "
              + "SET batch_status = ? "
              + "WHERE outward_batch_id = ?";

        try (Connection connection =
                     DBConnection.getConnection();
             PreparedStatement ps =
                     connection.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, batchId);

            int rowsUpdated =
                    ps.executeUpdate();

            System.out.println(
                    "=================================");

            System.out.println(
                    "BATCH STATUS UPDATE");

            System.out.println(
                    "Batch ID = " + batchId);

            System.out.println(
                    "New Status = " + status);

            System.out.println(
                    "Rows Updated = " + rowsUpdated);

            System.out.println(
                    "=================================");
        }
    }


    // ============================================================
    // GET REJECTED REASONS
    // ============================================================

    @Override
    public List<RejectedReason> getRejectedReasons()
            throws SQLException {

        List<RejectedReason> reasons =
                new ArrayList<>();

        String sql =
                "SELECT rejected_reason_id, "
              + "       rejected_reason_code, "
              + "       rejected_reason_name, "
              + "       rejected_reason_description "
              + "FROM rejected_reasons "
              + "ORDER BY rejected_reason_id";

        try (Connection con =
                     DBConnection.getConnection();
             PreparedStatement ps =
                     con.prepareStatement(sql);
             ResultSet rs =
                     ps.executeQuery()) {

            while (rs.next()) {

                RejectedReason reason =
                        new RejectedReason();

                reason.setRejectedReasonId(
                        rs.getString(
                                "rejected_reason_id"));

                reason.setRejectedReasonCode(
                        rs.getString(
                                "rejected_reason_code"));

                reason.setRejectedReasonName(
                        rs.getString(
                                "rejected_reason_name"));

                reason.setRejectedReasonDescription(
                        rs.getString(
                                "rejected_reason_description"));

                reasons.add(reason);
            }
        }

        System.out.println(
                "Rejected Reasons Loaded = "
                + reasons.size());

        return reasons;
    }


    // ============================================================
    // SAVE REJECTED CHEQUE
    //
    // This method is kept for your existing code.
    //
    // IMPORTANT:
    // rejectedCheque.getRejectedBy() should contain USER_ID
    // such as USR1003, not username ochecker.
    //
    // ============================================================
 // ============================================================
 // SAVE REJECTED CHEQUE
 // ============================================================

 @Override
 public void saveRejectedCheque(
         OutwardRejectedCheques rejectedCheque)
         throws SQLException {

     /*
      * rejectedCheque.getRejectedBy() contains USERNAME
      *
      * Example:
      *     ochecker
      *
      * But outward_rejected_cheques.rejected_by
      * references users.user_id.
      *
      * Therefore we get user_id from users using username.
      */

     String sql =
             "INSERT INTO outward_rejected_cheques "
           + "(outward_cheque_id, "
           + " rejected_by, "
           + " rejected_date, "
           + " remarks, "
           + " outward_batch_id, "
           + " cheque_amount) "
           + "SELECT ?, "
           + "       user_id, "
           + "       ?, "
           + "       ?, "
           + "       ?, "
           + "       ? "
           + "FROM users "
           + "WHERE username = ? "
           + "AND UPPER(status) = 'ACTIVE'";

     try (Connection con = DBConnection.getConnection();
          PreparedStatement ps = con.prepareStatement(sql)) {

         // --------------------------------------------------------
         // 1. OUTWARD CHEQUE ID
         // --------------------------------------------------------

         ps.setString(
                 1,
                 rejectedCheque.getOutwardChequeId()
         );

         // --------------------------------------------------------
         // 2. REJECTED DATE
         // --------------------------------------------------------

         ps.setTimestamp(
                 2,
                 rejectedCheque.getRejectedDate()
         );

         // --------------------------------------------------------
         // 3. REMARKS
         // --------------------------------------------------------

         ps.setString(
                 3,
                 rejectedCheque.getRemarks()
         );

         // --------------------------------------------------------
         // 4. OUTWARD BATCH ID
         // --------------------------------------------------------

         ps.setString(
                 4,
                 rejectedCheque.getOutwardBatchId()
         );

         // --------------------------------------------------------
         // 5. CHEQUE AMOUNT
         // --------------------------------------------------------

         ps.setBigDecimal(
                 5,
                 rejectedCheque.getChequeAmount()
         );

         // --------------------------------------------------------
         // 6. USERNAME
         // --------------------------------------------------------

         ps.setString(
                 6,
                 rejectedCheque.getRejectedBy()
         );

         System.out.println("=================================");
         System.out.println("SAVE REJECTED CHEQUE");
         System.out.println("Cheque ID = "
                 + rejectedCheque.getOutwardChequeId());
         System.out.println("Username = "
                 + rejectedCheque.getRejectedBy());
         System.out.println("Batch ID = "
                 + rejectedCheque.getOutwardBatchId());
         System.out.println("Amount = "
                 + rejectedCheque.getChequeAmount());
         System.out.println("=================================");

         int rows = ps.executeUpdate();

         // --------------------------------------------------------
         // CHECK WHETHER USER WAS FOUND
         // --------------------------------------------------------

         if (rows == 0) {

             throw new SQLException(
                     "Unable to save rejected cheque. "
                   + "No ACTIVE user found for username: "
                   + rejectedCheque.getRejectedBy()
             );
         }

         System.out.println(
                 "Rejected cheque saved successfully. Rows = "
                 + rows
         );
     }
 }
}