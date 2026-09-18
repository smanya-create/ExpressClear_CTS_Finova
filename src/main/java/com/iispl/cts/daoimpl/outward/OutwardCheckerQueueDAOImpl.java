package com.iispl.cts.daoimpl.outward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.OutwardCheckerQueueDAO;
import com.iispl.cts.dto.RejectRequestDTO;
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
              + "AND cheque_status IN ('PENDING_VERIFICATION', 'REJECT_REQUEST', 'VERIFIED_BY_CHECKER') "
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
            String reasonId,
            String remarks)
            throws SQLException {

        Connection con = null;

        try {

            // ====================================================
            // 1. GET CONNECTION
            // ====================================================

            con = DBConnection.getConnection();

            if (con == null) {
                throw new SQLException(
                        "Database connection is null.");
            }

            con.setAutoCommit(false);

            System.out.println("=================================");
            System.out.println("REJECT CHEQUE");
            System.out.println("Cheque Number = " + chequeNo);
            System.out.println("Username      = " + username);
            System.out.println("Reason ID     = " + reasonId);
            System.out.println("Remarks       = " + remarks);
            System.out.println("=================================");


            // ====================================================
            // 2. GET USER ID FROM USERNAME
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

            if (userId == null ||
                userId.trim().isEmpty()) {

                throw new SQLException(
                        "Active user not found for username: "
                        + username);
            }

            System.out.println("Rejected By User ID = "
                    + userId);


            // ====================================================
            // 3. GET CHEQUE DETAILS
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

            if (outwardChequeId == null ||
                outwardChequeId.trim().isEmpty()) {

                throw new SQLException(
                        "Cheque not found: "
                        + chequeNo);
            }


            // ====================================================
            // 4. GET REJECTION REASON
            //
            // reason_id in outward_rejected_cheques
            // references rejected_reasons.rejected_reason_code
            // ====================================================

            String reasonSql =
                    "SELECT rejected_reason_code, "
                  + "       rejected_reason_name "
                  + "FROM rejected_reasons "
                  + "WHERE rejected_reason_code = ?";

            String rejectedReasonCode = null;
            String rejectedReasonName = null;

            try (PreparedStatement ps =
                         con.prepareStatement(reasonSql)) {

                ps.setString(1, reasonId);

                try (ResultSet rs =
                             ps.executeQuery()) {

                    if (rs.next()) {

                        rejectedReasonCode =
                                rs.getString(
                                        "rejected_reason_code");

                        rejectedReasonName =
                                rs.getString(
                                        "rejected_reason_name");
                    }
                }
            }

            // ====================================================
            // VALIDATE REASON
            // ====================================================

            if (rejectedReasonCode == null ||
                rejectedReasonCode.trim().isEmpty()) {

                throw new SQLException(
                        "Invalid rejection reason code: "
                        + reasonId);
            }

            if (rejectedReasonName == null ||
                rejectedReasonName.trim().isEmpty()) {

                throw new SQLException(
                        "Rejection reason name not found for code: "
                        + reasonId);
            }

            System.out.println("Reason Code = "
                    + rejectedReasonCode);

            System.out.println("Reason Name = "
                    + rejectedReasonName);


            // ====================================================
            // 5. INSERT INTO outward_rejected_cheques
            // ====================================================

            String insertSql =
                    "INSERT INTO public.outward_rejected_cheques "
                  + "(outward_cheque_id, "
                  + " rejected_by, "
                  + " remarks, "
                  + " outward_batch_id, "
                  + " cheque_amount, "
                  + " reason_id, "
                  + " reason) "
                  + "VALUES (?, ?, ?, ?, ?, ?, ?)";

            try (PreparedStatement ps =
                         con.prepareStatement(insertSql)) {

                ps.setString(
                        1,
                        outwardChequeId);

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

                // IMPORTANT
                // reason_id is rejected_reason_code
                ps.setString(
                        6,
                        rejectedReasonCode);

                // reason is rejected_reason_name
                ps.setString(
                        7,
                        rejectedReasonName);

                int rowsInserted =
                        ps.executeUpdate();

                if (rowsInserted != 1) {

                    throw new SQLException(
                            "Failed to insert rejected cheque: "
                            + chequeNo);
                }

                System.out.println(
                        "=================================");

                System.out.println(
                        "REJECTED CHEQUE INSERTED");

                System.out.println(
                        "Cheque ID = "
                        + outwardChequeId);

                System.out.println(
                        "User ID = "
                        + userId);

                System.out.println(
                        "Reason ID = "
                        + rejectedReasonCode);

                System.out.println(
                        "Reason = "
                        + rejectedReasonName);

                System.out.println(
                        "Remarks = "
                        + remarks);

                System.out.println(
                        "Rows Inserted = "
                        + rowsInserted);

                System.out.println(
                        "=================================");
            }


            // ====================================================
            // 6. UPDATE OUTWARD CHEQUE STATUS
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
            // 7. COMMIT
            // ====================================================

            con.commit();

            System.out.println(
                    "=================================");

            System.out.println(
                    "REJECT TRANSACTION SUCCESS");

            System.out.println(
                    "Cheque = "
                    + chequeNo);

            System.out.println(
                    "User = "
                    + username);

            System.out.println(
                    "User ID = "
                    + userId);

            System.out.println(
                    "Reason ID = "
                    + rejectedReasonCode);

            System.out.println(
                    "Reason = "
                    + rejectedReasonName);

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
 // RETURN CHEQUE TO MAKER
 //
 // 1. Get cheque ID and batch ID
 // 2. Get reason name
 // 3. Insert request into outward_cheque_request
 // 4. Change cheque status to RETURN_TO_MAKER
 // 5. Commit everything
 //
 // ============================================================

 // ============================================================
 // RETURN CHEQUE TO MAKER
 //
 // 1. Get cheque ID and batch ID
 // 2. Get reason name
 // 3. Insert request into outward_cheque_request
 // 4. Change cheque status
 // 5. Commit everything
 //
 // ============================================================

 @Override
 public void returnChequeToMaker(
         String chequeNo,
         String reasonId,
         String remarks)
         throws SQLException {

     Connection con = null;

     try {

         // ========================================================
         // 1. GET CONNECTION
         // ========================================================

         con = DBConnection.getConnection();

         // Start transaction
         con.setAutoCommit(false);

         System.out.println("=================================");
         System.out.println("RETURN CHEQUE TO MAKER");
         System.out.println("Cheque Number = " + chequeNo);
         System.out.println("Reason ID = " + reasonId);
         System.out.println("Remarks = " + remarks);
         System.out.println("=================================");


         // ========================================================
         // 2. GET CHEQUE ID AND BATCH ID
         // ========================================================

         String chequeSql =
                 "SELECT outward_cheque_id, "
               + "       outward_batch_id "
               + "FROM outward_cheque "
               + "WHERE cheque_number = ?";

         String outwardChequeId = null;
         String outwardBatchId = null;

         try (PreparedStatement ps =
                      con.prepareStatement(chequeSql)) {

             ps.setString(1, chequeNo);

             try (ResultSet rs =
                          ps.executeQuery()) {

                 if (rs.next()) {

                     outwardChequeId =
                             rs.getString("outward_cheque_id");

                     outwardBatchId =
                             rs.getString("outward_batch_id");
                 }
             }
         }


         // ========================================================
         // 3. VALIDATE CHEQUE
         // ========================================================

         if (outwardChequeId == null ||
             outwardChequeId.trim().isEmpty()) {

             throw new SQLException(
                     "Cheque not found: " + chequeNo);
         }

         if (outwardBatchId == null ||
             outwardBatchId.trim().isEmpty()) {

             throw new SQLException(
                     "Batch ID not found for cheque: "
                     + chequeNo);
         }


         System.out.println(
                 "Outward Cheque ID = "
                 + outwardChequeId);

         System.out.println(
                 "Outward Batch ID = "
                 + outwardBatchId);


         // ========================================================
         // 4. GET REASON NAME
         // ========================================================

         String reasonSql =
                 "SELECT reason_name "
               + "FROM send_back_reason "
               + "WHERE reason_id = ?";

         String reasonName = null;

         try (PreparedStatement ps =
                      con.prepareStatement(reasonSql)) {

             ps.setString(1, reasonId);

             try (ResultSet rs =
                          ps.executeQuery()) {

                 if (rs.next()) {

                     reasonName =
                             rs.getString("reason_name");
                 }
             }
         }


         // ========================================================
         // 5. VALIDATE REASON
         // ========================================================

         if (reasonName == null ||
             reasonName.trim().isEmpty()) {

             throw new SQLException(
                     "Send back reason not found for reason ID: "
                     + reasonId);
         }


         System.out.println(
                 "Reason Name = " + reasonName);


         // ========================================================
         // 6. INSERT INTO outward_cheque_request
         //
         // Table columns:
         //
         // request_id  -> generated automatically by PostgreSQL
         // cheque_id   -> outward_cheque.outward_cheque_id
         // batch_id    -> outward_cheque.outward_batch_id
         // remarks     -> checker remarks
         // reason_id   -> selected reason ID
         // reason      -> reason_name
         //
         // ========================================================

         String insertSql =
                 "INSERT INTO outward_cheque_request "
               + "(cheque_id, "
               + " batch_id, "
               + " remarks, "
               + " reason_id, "
               + " reason) "
               + "VALUES (?, ?, ?, ?, ?)";

         try (PreparedStatement ps =
                      con.prepareStatement(insertSql)) {

             ps.setString(1, outwardChequeId);
             ps.setString(2, outwardBatchId);
             ps.setString(3, remarks);
             ps.setString(4, reasonId);
             ps.setString(5, reasonName);

             int rowsInserted =
                     ps.executeUpdate();

             if (rowsInserted != 1) {

                 throw new SQLException(
                         "Failed to insert outward cheque request "
                         + "for cheque: " + chequeNo);
             }

             System.out.println(
                     "=================================");

             System.out.println(
                     "OUTWARD CHEQUE REQUEST INSERTED");

             System.out.println(
                     "Cheque ID = " + outwardChequeId);

             System.out.println(
                     "Batch ID = " + outwardBatchId);

             System.out.println(
                     "Reason ID = " + reasonId);

             System.out.println(
                     "Reason = " + reasonName);

             System.out.println(
                     "Remarks = " + remarks);

             System.out.println(
                     "Rows Inserted = " + rowsInserted);

             System.out.println(
                     "=================================");
         }

      // ========================================================
      // 7. UPDATE CHEQUE STATUS
      // ========================================================

      String chequeStatus;

      if ("11".equals(reasonId) || "12".equals(reasonId)) {
          chequeStatus = "PENDING_MICR_REPAIR";
      } else {
          chequeStatus = "PENDING_DATA_ENTRY";
      }

      System.out.println("Cheque Status = " + chequeStatus);
      System.out.println("Updating using Outward Cheque ID = " + outwardChequeId);

      String updateSql =
              "UPDATE outward_cheque "
            + "SET cheque_status = ? "
            + "WHERE outward_cheque_id = ?";

      try (PreparedStatement ps =
                   con.prepareStatement(updateSql)) {

          ps.setString(1, chequeStatus);
          ps.setString(2, outwardChequeId);

          int rowsUpdated = ps.executeUpdate();

          System.out.println("Rows Updated = " + rowsUpdated);

          if (rowsUpdated != 1) {
              throw new SQLException(
                      "Failed to update cheque status "
                      + "for cheque: " + chequeNo
                      + ". Outward cheque ID: "
                      + outwardChequeId
              );
          }

          System.out.println(
                  "================================="
          );

          System.out.println(
                  "CHEQUE STATUS UPDATED"
          );

          System.out.println(
                  "Cheque Number = " + chequeNo
          );

          System.out.println(
                  "Outward Cheque ID = " + outwardChequeId
          );

          System.out.println(
                  "New Status = " + chequeStatus
          );

          System.out.println(
                  "Rows Updated = " + rowsUpdated
          );

          System.out.println(
                  "================================="
          );
      }

         // ========================================================
         // 8. COMMIT TRANSACTION
         // ========================================================

         con.commit();

         System.out.println(
                 "=================================");

         System.out.println(
                 "RETURN TO MAKER SUCCESS");

         System.out.println(
                 "Cheque = " + chequeNo);

         System.out.println(
                 "Status = RETURN_TO_MAKER");

         System.out.println(
                 "=================================");


     } catch (SQLException e) {

         // ========================================================
         // 9. ROLLBACK
         // ========================================================

         if (con != null) {

             try {

                 con.rollback();

                 System.out.println(
                         "Return-to-maker transaction rolled back.");

             } catch (SQLException rollbackException) {

                 rollbackException.printStackTrace();
             }
         }

         throw e;


     } finally {

         // ========================================================
         // 10. CLOSE CONNECTION
         // ========================================================

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
    public void saveRejectedCheque(OutwardRejectedCheques rejectedCheque)
            throws SQLException {

    	String sql =
    	        "INSERT INTO public.outward_rejected_cheques "
    	      + "(outward_cheque_id, "
    	      + " rejected_by, "
    	      + " rejected_date, "
    	      + " remarks, "
    	      + " outward_batch_id, "
    	      + " cheque_amount, "
    	      + " reason_id, "
    	      + " reason) "
    	      + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        Connection connection = null;
        PreparedStatement ps = null;

        try {

            connection = DBConnection.getConnection();

            if (connection == null) {
                throw new SQLException("Database connection is null.");
            }

            ps = connection.prepareStatement(sql);

            // Existing fields
            ps.setString(1, rejectedCheque.getOutwardChequeId());
            ps.setString(2, rejectedCheque.getRejectedBy());
            ps.setTimestamp(3, rejectedCheque.getRejectedDate());
            ps.setString(4, rejectedCheque.getRemarks());
            ps.setString(5, rejectedCheque.getOutwardBatchId());
            ps.setBigDecimal(6, rejectedCheque.getChequeAmount());
            ps.setString(7, rejectedCheque.getRejectedReasonId());
            ps.setString(8, rejectedCheque.getRejectedReasonName());

            ps.executeUpdate();

            System.out.println(
                    "Rejected cheque saved successfully."
            );

            System.out.println(
                    "Reason ID   : "
                    + rejectedCheque.getRejectedReasonId()
            );

            System.out.println(
                    "Reason Name : "
                    + rejectedCheque.getRejectedReasonName()
            );

        } finally {

            if (ps != null) {
                ps.close();
            }

            if (connection != null) {
                connection.close();
            }
        }
    }
    
    
 @Override
 public RejectRequestDTO getRejectRequestByChequeId(
         String chequeId) throws SQLException {

	 String sql =
		        "SELECT request_id, "
		      + "       cheque_id, "
		      + "       batch_id, "
		      + "       remarks, "
		      + "       reason_id, "
		      + "       reason "
		      + "FROM outward_cheque_request "
		      + "WHERE cheque_id = ? "
		      + "ORDER BY time_stamp DESC, request_id DESC "
		      + "LIMIT 1";
     try (
         Connection connection =
                 DBConnection.getConnection();

         PreparedStatement ps =
                 connection.prepareStatement(sql)
     ) {

         // ----------------------------------------------------
         // SET CHEQUE ID
         // ----------------------------------------------------

         ps.setString(1, chequeId);

         System.out.println("=================================");
         System.out.println("GET REJECT REQUEST");
         System.out.println("Cheque ID = " + chequeId);
         System.out.println("=================================");

         try (ResultSet rs = ps.executeQuery()) {

             // ------------------------------------------------
             // CHECK WHETHER REQUEST EXISTS
             // ------------------------------------------------

             if (rs.next()) {

                 RejectRequestDTO dto =
                         new RejectRequestDTO();

                 // --------------------------------------------
                 // REQUEST ID
                 // --------------------------------------------

                 dto.setRequestId(
                         rs.getString("request_id")
                 );

                 // --------------------------------------------
                 // CHEQUE ID
                 // --------------------------------------------

                 dto.setChequeId(
                         rs.getString("cheque_id")
                 );

                 // --------------------------------------------
                 // BATCH ID
                 // --------------------------------------------

                 dto.setBatchId(
                         rs.getString("batch_id")
                 );

                 // --------------------------------------------
                 // REASON ID
                 // --------------------------------------------

                 dto.setRejectedReasonId(
                         rs.getString("reason_id")
                 );

                 // --------------------------------------------
                 // REASON
                 // --------------------------------------------

                 dto.setRejectedReasonName(
                         rs.getString("reason")
                 );

                 // --------------------------------------------
                 // REMARKS
                 // --------------------------------------------

                 dto.setRemarks(
                         rs.getString("remarks")
                 );

                 System.out.println(
                         "Reject Request Found");

                 System.out.println(
                         "Request ID = "
                         + dto.getRequestId());

                 System.out.println(
                         "Cheque ID = "
                         + dto.getChequeId());

                 System.out.println(
                         "Batch ID = "
                         + dto.getBatchId());

                 System.out.println(
                         "Reason ID = "
                         + dto.getRejectedReasonId());

                 System.out.println(
                         "Reason = "
                         + dto.getRejectedReasonName());

                 System.out.println(
                         "Remarks = "
                         + dto.getRemarks());

                 System.out.println(
                         "=================================");

                 return dto;
             }
         }
     }

     // --------------------------------------------------------
     // NO REQUEST FOUND
     // --------------------------------------------------------

     System.out.println(
             "No reject request found for Cheque ID = "
             + chequeId);

     return null;
 }
}