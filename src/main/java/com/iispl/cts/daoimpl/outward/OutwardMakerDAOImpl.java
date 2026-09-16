package com.iispl.cts.daoimpl.outward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.OutwardMakerDAO;
import com.iispl.cts.dto.MicrRepairBatch;
import com.iispl.cts.dto.MicrRepairChequeDTO;
import com.iispl.cts.entity.outward.RejectedReason;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;

public class OutwardMakerDAOImpl implements OutwardMakerDAO {

    // =========================================================
    // SCAN MICR REPAIR - BATCHES
    // =========================================================

    @Override
    public List<MicrRepairBatch> getScanMicrRepairBatches() {

        List<MicrRepairBatch> batchList =
                new ArrayList<MicrRepairBatch>();

        String sql =
                "SELECT "
              + "sb.scanned_batch_id, "
              + "sb.uploaded_at, "
              + "sb.actual_cheque_count, "
              + "sb.batch_status, "
              + "COUNT(sc.scanned_cheque_id) AS micr_errors "
              + "FROM scan_batch sb "
              + "JOIN scan_cheque sc "
              + "ON sc.scanned_batch_id = sb.scanned_batch_id "
              + "WHERE UPPER(TRIM(sc.cheque_status)) IN ("
              + "'PENDING_MICR_REPAIR', "
                        + "'MICR_REJECTION_PENDING', "
              + "'MICR_REPAIRED' "
        
              + ") "
              + "GROUP BY "
              + "sb.scanned_batch_id, "
              + "sb.uploaded_at, "
              + "sb.actual_cheque_count, "
              + "sb.batch_status "
              + "ORDER BY sb.uploaded_at DESC";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement preparedStatement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        preparedStatement.executeQuery()
        ) {

            while (resultSet.next()) {

                MicrRepairBatch batch =
                        new MicrRepairBatch();

                batch.setBatchId(
                        resultSet.getString(
                                "scanned_batch_id"));

                batch.setScanDate(
                        resultSet.getTimestamp(
                                "uploaded_at"));

                batch.setTotalCheques(
                        resultSet.getInt(
                                "actual_cheque_count"));

                batch.setMicrErrors(
                        resultSet.getInt(
                                "micr_errors"));

                batch.setStatus(
                        resultSet.getString(
                                "batch_status"));

                batchList.add(batch);
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Unable to fetch scan MICR repair batches",
                    e);
        }

        return batchList;
    }

    // =========================================================
    // SCAN MICR REPAIR - CHEQUES
    // =========================================================

    @Override
    public List<MicrRepairChequeDTO> getScanMicrRepairCheques(
            String scannedBatchId) {

        if (scannedBatchId == null
                || scannedBatchId.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Scanned batch ID cannot be null or empty");
        }

        List<MicrRepairChequeDTO> chequeList =
                new ArrayList<MicrRepairChequeDTO>();

        /*
         * Retrieve only the latest rejection request
         * for each cheque based on time_stamp.
         */
        String sql =
                "SELECT "
              + "sc.scanned_cheque_id, "
              + "sc.scanned_batch_id, "
              + "sc.cheque_number, "
              + "sc.micr_code, "
              + "sc.cheque_status, "
              + "sc.city_code, "
              + "sc.bank_code, "
              + "sc.branch_code, "
              + "ocr.remarks, "
              + "ocr.reason_id, "
              + "ocr.reason, "
              + "sc.cheque_image_front, "
              + "sc.cheque_image_back "
              + "FROM scan_cheque sc "
              + "LEFT JOIN LATERAL ("
              + "    SELECT "
              + "        r.remarks, "
              + "        r.reason_id, "
              + "        r.reason "
              + "    FROM outward_cheque_request r "
              + "    WHERE r.cheque_id = sc.scanned_cheque_id "
              + "    ORDER BY r.time_stamp DESC, r.request_id DESC "
              + "    LIMIT 1 "
              + ") ocr ON TRUE "
              + "WHERE sc.scanned_batch_id = ? "
              + "AND UPPER(TRIM(sc.cheque_status)) IN ("
              + "'PENDING_MICR_REPAIR', "
                        + "'MICR_REJECTION_PENDING', "
              + "'MICR_REPAIRED'"
            
              + ") "
              + "ORDER BY sc.scanned_cheque_id";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement preparedStatement =
                        connection.prepareStatement(sql)
        ) {

            preparedStatement.setString(
                    1,
                    scannedBatchId.trim());

            try (ResultSet resultSet =
                    preparedStatement.executeQuery()) {

                while (resultSet.next()) {

                    MicrRepairChequeDTO cheque =
                            new MicrRepairChequeDTO();

                    cheque.setChequeId(
                            resultSet.getString(
                                    "scanned_cheque_id"));

                    cheque.setBatchId(
                            resultSet.getString(
                                    "scanned_batch_id"));

                    cheque.setChequeNumber(
                            resultSet.getString(
                                    "cheque_number"));

                    cheque.setFullMicr(
                            resultSet.getString(
                                    "micr_code"));

                    cheque.setChequeStatus(
                            resultSet.getString(
                                    "cheque_status"));

                    cheque.setCityCode(
                            resultSet.getString(
                                    "city_code"));

                    cheque.setBankCode(
                            resultSet.getString(
                                    "bank_code"));

                    cheque.setBranchCode(
                            resultSet.getString(
                                    "branch_code"));

                    cheque.setRemarks(
                            resultSet.getString(
                                    "remarks"));

                    cheque.setReasonId(
                            resultSet.getString(
                                    "reason_id"));

                    cheque.setReason(
                            resultSet.getString(
                                    "reason"));

                    cheque.setChequeImageFront(
                            resultSet.getString(
                                    "cheque_image_front"));

                    cheque.setChequeImageBack(
                            resultSet.getString(
                                    "cheque_image_back"));

                    chequeList.add(cheque);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Unable to fetch scan MICR repair cheques "
                  + "for batch: " + scannedBatchId,
                    e);
        }

        return chequeList;
    }

    // =========================================================
    // SCAN MICR REPAIR - SAVE
    // =========================================================

    @Override
    public void saveScanMicrRepair(
            MicrRepairChequeDTO cheque) {

        if (cheque == null) {

            throw new IllegalArgumentException(
                    "Scan MICR repair cheque cannot be null");
        }

        if (cheque.getChequeId() == null
                || cheque.getChequeId().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Scanned cheque ID cannot be null or empty");
        }

        String updateChequeSql =
                "UPDATE scan_cheque SET "
              + "micr_code = ?, "
              + "city_code = ?, "
              + "bank_code = ?, "
              + "branch_code = ?, "
              + "cheque_status = ? "
              + "WHERE scanned_cheque_id = ?";

        /*
         * Rejection requests are INSERT only.
         *
         * request_id and time_stamp are generated by DB.
         */
        String insertRequestSql =
                "INSERT INTO outward_cheque_request "
              + "(cheque_id, batch_id, remarks, reason_id, reason) "
              + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection =
                DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                // =================================================
                // Update scan cheque
                // =================================================

                try (PreparedStatement statement =
                        connection.prepareStatement(
                                updateChequeSql)) {

                    statement.setString(
                            1,
                            cheque.getFullMicr());

                    statement.setString(
                            2,
                            cheque.getCityCode());

                    statement.setString(
                            3,
                            cheque.getBankCode());

                    statement.setString(
                            4,
                            cheque.getBranchCode());

                    statement.setString(
                            5,
                            cheque.getChequeStatus());

                    statement.setString(
                            6,
                            cheque.getChequeId());

                    int rowsUpdated =
                            statement.executeUpdate();

                    if (rowsUpdated == 0) {

                        throw new IllegalStateException(
                                "Scan cheque not found for ID: "
                              + cheque.getChequeId());
                    }
                }

                // =================================================
                // INSERT REJECTION REQUEST
                // =================================================

                if ("MICR_REJECTION_PENDING".equalsIgnoreCase(
                        cheque.getChequeStatus())) {

                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    insertRequestSql)) {

                        statement.setString(
                                1,
                                cheque.getChequeId());

                        statement.setString(
                                2,
                                cheque.getBatchId());

                        statement.setString(
                                3,
                                cheque.getRemarks());

                        statement.setString(
                                4,
                                cheque.getReasonId());

                        statement.setString(
                                5,
                                cheque.getReason());

                        statement.executeUpdate();
                    }
                }

                connection.commit();

            } catch (Exception e) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }

                throw e;
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to save MICR repair for scan cheque: "
                  + cheque.getChequeId(),
                    e);
        }
    }

    // =========================================================
    // SCAN MICR REPAIR - SUBMIT
    // =========================================================

    @Override
    public void submitScanMicrRepair(
            List<MicrRepairChequeDTO> cheques) {

        if (cheques == null
                || cheques.isEmpty()) {

            throw new IllegalArgumentException(
                    "Scan MICR repair cheque list "
                  + "cannot be null or empty");
        }

        String sql =
                "UPDATE scan_cheque SET "
              + "micr_code = ?, "
              + "city_code = ?, "
              + "bank_code = ?, "
              + "branch_code = ?, "
              + "cheque_status = ? "
              + "WHERE scanned_cheque_id = ?";

        try (Connection connection =
                DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement statement =
                    connection.prepareStatement(sql)) {

                for (MicrRepairChequeDTO cheque : cheques) {

                    if (cheque == null) {

                        throw new IllegalArgumentException(
                                "Scan MICR repair cheque cannot be null");
                    }

                    if (cheque.getChequeId() == null
                            || cheque.getChequeId()
                                    .trim().isEmpty()) {

                        throw new IllegalArgumentException(
                                "Scanned cheque ID cannot be null or empty");
                    }

                    statement.setString(
                            1,
                            cheque.getFullMicr());

                    statement.setString(
                            2,
                            cheque.getCityCode());

                    statement.setString(
                            3,
                            cheque.getBankCode());

                    statement.setString(
                            4,
                            cheque.getBranchCode());

                    statement.setString(
                            5,
                            cheque.getChequeStatus());

                    statement.setString(
                            6,
                            cheque.getChequeId());

                    int rowsUpdated =
                            statement.executeUpdate();

                    if (rowsUpdated == 0) {

                        throw new IllegalStateException(
                                "Scan cheque not found for ID: "
                              + cheque.getChequeId());
                    }
                }

                connection.commit();

            } catch (Exception e) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }

                throw e;
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to submit SCAN MICR repair",
                    e);
        }
    }

    // =========================================================
    // OUTWARD MICR REPAIR - BATCHES
    // =========================================================

    @Override
    public List<MicrRepairBatch> getOutwardMicrRepairBatches() {

        List<MicrRepairBatch> batchList =
                new ArrayList<MicrRepairBatch>();

        String sql =
                "SELECT "
              + "ob.outward_batch_id, "
              + "ob.uploaded_at, "
              + "ob.actual_cheque_count, "
              + "ob.batch_status, "
              + "COUNT(oc.outward_cheque_id) AS micr_errors "
              + "FROM outward_batch ob "
              + "JOIN outward_cheque oc "
              + "ON oc.outward_batch_id = ob.outward_batch_id "
              + "WHERE UPPER(TRIM(oc.cheque_status)) IN ("
              + "'PENDING_MICR_REPAIR', "
                        + "'MICR_REJECTION_PENDING', "
              + "'MICR_REPAIRED', "
              + "'MICR_REJECTED'"
              + ") "
              + "GROUP BY "
              + "ob.outward_batch_id, "
              + "ob.uploaded_at, "
              + "ob.actual_cheque_count, "
              + "ob.batch_status "
              + "ORDER BY ob.uploaded_at DESC";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement preparedStatement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        preparedStatement.executeQuery()
        ) {

            while (resultSet.next()) {

                MicrRepairBatch batch =
                        new MicrRepairBatch();

                batch.setBatchId(
                        resultSet.getString(
                                "outward_batch_id"));

                batch.setScanDate(
                        resultSet.getTimestamp(
                                "uploaded_at"));

                batch.setTotalCheques(
                        resultSet.getInt(
                                "actual_cheque_count"));

                batch.setMicrErrors(
                        resultSet.getInt(
                                "micr_errors"));

                batch.setStatus(
                        resultSet.getString(
                                "batch_status"));

                batchList.add(batch);
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Unable to fetch outward MICR repair batches",
                    e);
        }

        return batchList;
    }

    // =========================================================
    // OUTWARD MICR REPAIR - CHEQUES
    // =========================================================

    @Override
    public List<MicrRepairChequeDTO>
            getOutwardMicrRepairCheques(
                    String outwardBatchId) {

        if (outwardBatchId == null
                || outwardBatchId.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Outward batch ID cannot be null or empty");
        }

        List<MicrRepairChequeDTO> chequeList =
                new ArrayList<MicrRepairChequeDTO>();

        /*
         * Retrieve only the latest request for the cheque.
         *
         * The latest record is determined by:
         *
         *     time_stamp DESC
         *
         * request_id DESC is used as a tie-breaker.
         */
        String sql =
                "SELECT "
              + "oc.outward_cheque_id, "
              + "oc.outward_batch_id, "
              + "oc.cheque_number, "
              + "oc.micr_code, "
              + "oc.cheque_status, "
              + "oc.city_code, "
              + "oc.bank_code, "
              + "oc.branch_code, "
              + "oc.cheque_image_front, "
              + "oc.cheque_image_back, "
              + "ocr.remarks, "
              + "ocr.reason_id, "
              + "ocr.reason "
              + "FROM outward_cheque oc "
              + "LEFT JOIN LATERAL ("
              + "    SELECT "
              + "        r.remarks, "
              + "        r.reason_id, "
              + "        r.reason "
              + "    FROM outward_cheque_request r "
              + "    WHERE r.cheque_id = oc.outward_cheque_id "
              + "    ORDER BY r.time_stamp DESC, r.request_id DESC "
              + "    LIMIT 1 "
              + ") ocr ON TRUE "
              + "WHERE oc.outward_batch_id = ? "
              + "AND UPPER(TRIM(oc.cheque_status)) IN ("
              + "'PENDING_MICR_REPAIR', "
                        + "'MICR_REJECTION_PENDING', "
              + "'MICR_REPAIRED', "
              + "'MICR_REJECTED'"
              + ") "
              + "ORDER BY oc.outward_cheque_id";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement preparedStatement =
                        connection.prepareStatement(sql)
        ) {

            preparedStatement.setString(
                    1,
                    outwardBatchId.trim());

            try (ResultSet resultSet =
                    preparedStatement.executeQuery()) {

                while (resultSet.next()) {

                    MicrRepairChequeDTO cheque =
                            new MicrRepairChequeDTO();

                    cheque.setChequeId(
                            resultSet.getString(
                                    "outward_cheque_id"));

                    cheque.setBatchId(
                            resultSet.getString(
                                    "outward_batch_id"));

                    cheque.setChequeNumber(
                            resultSet.getString(
                                    "cheque_number"));

                    cheque.setFullMicr(
                            resultSet.getString(
                                    "micr_code"));

                    cheque.setChequeStatus(
                            resultSet.getString(
                                    "cheque_status"));

                    cheque.setCityCode(
                            resultSet.getString(
                                    "city_code"));

                    cheque.setBankCode(
                            resultSet.getString(
                                    "bank_code"));

                    cheque.setBranchCode(
                            resultSet.getString(
                                    "branch_code"));

                    cheque.setChequeImageFront(
                            resultSet.getString(
                                    "cheque_image_front"));

                    cheque.setChequeImageBack(
                            resultSet.getString(
                                    "cheque_image_back"));

                    cheque.setRemarks(
                            resultSet.getString(
                                    "remarks"));

                    cheque.setReasonId(
                            resultSet.getString(
                                    "reason_id"));

                    cheque.setReason(
                            resultSet.getString(
                                    "reason"));

                    chequeList.add(cheque);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Unable to fetch outward MICR repair cheques "
                  + "for batch: " + outwardBatchId,
                    e);
        }

        return chequeList;
    }

    // =========================================================
    // OUTWARD MICR REPAIR - SAVE
    // =========================================================

    @Override
    public void saveOutwardMicrRepair(
            MicrRepairChequeDTO cheque) {

        if (cheque == null) {

            throw new IllegalArgumentException(
                    "Outward MICR repair cheque cannot be null");
        }

        if (cheque.getChequeId() == null
                || cheque.getChequeId().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Outward cheque ID cannot be null or empty");
        }

        String updateChequeSql =
                "UPDATE outward_cheque SET "
              + "micr_code = ?, "
              + "city_code = ?, "
              + "bank_code = ?, "
              + "branch_code = ?, "
              + "cheque_status = ? "
              + "WHERE outward_cheque_id = ?";

        /*
         * Rejection requests are INSERT only.
         *
         * request_id and time_stamp are generated by DB.
         */
        String insertRequestSql =
                "INSERT INTO outward_cheque_request "
              + "(cheque_id, batch_id, remarks, reason_id, reason) "
              + "VALUES (?, ?, ?, ?, ?)";

        try (Connection connection =
                DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try {

                // =================================================
                // Update outward cheque
                // =================================================

                try (PreparedStatement statement =
                        connection.prepareStatement(
                                updateChequeSql)) {

                    statement.setString(
                            1,
                            cheque.getFullMicr());

                    statement.setString(
                            2,
                            cheque.getCityCode());

                    statement.setString(
                            3,
                            cheque.getBankCode());

                    statement.setString(
                            4,
                            cheque.getBranchCode());

                    statement.setString(
                            5,
                            cheque.getChequeStatus());

                    statement.setString(
                            6,
                            cheque.getChequeId());

                    int rowsUpdated =
                            statement.executeUpdate();

                    if (rowsUpdated == 0) {

                        throw new IllegalStateException(
                                "Outward cheque not found for ID: "
                              + cheque.getChequeId());
                    }
                }

                // =================================================
                // INSERT REJECTION REQUEST
                // =================================================

                if ("MICR_REJECTION_PENDING".equalsIgnoreCase(
                        cheque.getChequeStatus())) {

                    try (PreparedStatement statement =
                            connection.prepareStatement(
                                    insertRequestSql)) {

                        statement.setString(
                                1,
                                cheque.getChequeId());

                        statement.setString(
                                2,
                                cheque.getBatchId());

                        statement.setString(
                                3,
                                cheque.getRemarks());

                        statement.setString(
                                4,
                                cheque.getReasonId());

                        statement.setString(
                                5,
                                cheque.getReason());

                        statement.executeUpdate();
                    }
                }

                connection.commit();

            } catch (Exception e) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }

                throw e;
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to save MICR repair for outward cheque: "
                  + cheque.getChequeId(),
                    e);
        }
    }

    // =========================================================
    // OUTWARD MICR REPAIR - SUBMIT
    // =========================================================

    @Override
    public void submitOutwardMicrRepair(
            List<MicrRepairChequeDTO> cheques) {

        if (cheques == null
                || cheques.isEmpty()) {

            throw new IllegalArgumentException(
                    "Outward MICR repair cheque list "
                  + "cannot be null or empty");
        }

        String sql =
                "UPDATE outward_cheque SET "
              + "micr_code = ?, "
              + "city_code = ?, "
              + "bank_code = ?, "
              + "branch_code = ?, "
              + "cheque_status = ? "
              + "WHERE outward_cheque_id = ?";

        try (Connection connection =
                DBConnection.getConnection()) {

            connection.setAutoCommit(false);

            try (PreparedStatement statement =
                    connection.prepareStatement(sql)) {

                for (MicrRepairChequeDTO cheque : cheques) {

                    if (cheque == null) {

                        throw new IllegalArgumentException(
                                "Outward MICR repair cheque cannot be null");
                    }

                    if (cheque.getChequeId() == null
                            || cheque.getChequeId()
                                    .trim().isEmpty()) {

                        throw new IllegalArgumentException(
                                "Outward cheque ID cannot be null or empty");
                    }

                    statement.setString(
                            1,
                            cheque.getFullMicr());

                    statement.setString(
                            2,
                            cheque.getCityCode());

                    statement.setString(
                            3,
                            cheque.getBankCode());

                    statement.setString(
                            4,
                            cheque.getBranchCode());

                    statement.setString(
                            5,
                            cheque.getChequeStatus());

                    statement.setString(
                            6,
                            cheque.getChequeId());

                    int rowsUpdated =
                            statement.executeUpdate();

                    if (rowsUpdated == 0) {

                        throw new IllegalStateException(
                                "Outward cheque not found for ID: "
                              + cheque.getChequeId());
                    }
                }

                connection.commit();

            } catch (Exception e) {

                try {
                    connection.rollback();
                } catch (SQLException rollbackException) {
                    e.addSuppressed(rollbackException);
                }

                throw e;
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to submit OUTWARD MICR repair",
                    e);
        }
    }

    // =========================================================
    // COMMON - REJECTED REASONS
    // =========================================================

    @Override
    public List<RejectedReason> getRejectedReasons() {

        List<RejectedReason> reasons =
                new ArrayList<RejectedReason>();

        String sql =
                "SELECT rejected_reason_id, "
              + "       rejected_reason_code, "
              + "       rejected_reason_name, "
              + "       rejected_reason_description "
              + "FROM rejected_reasons "
              + "ORDER BY rejected_reason_id";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement preparedStatement =
                        connection.prepareStatement(sql);

                ResultSet resultSet =
                        preparedStatement.executeQuery()
        ) {

            while (resultSet.next()) {

                RejectedReason reason =
                        new RejectedReason();

                reason.setRejectedReasonId(
                        resultSet.getString(
                                "rejected_reason_id"));

                reason.setRejectedReasonCode(
                        resultSet.getString(
                                "rejected_reason_code"));

                reason.setRejectedReasonName(
                        resultSet.getString(
                                "rejected_reason_name"));

                reason.setRejectedReasonDescription(
                        resultSet.getString(
                                "rejected_reason_description"));

                reasons.add(reason);
            }

            System.out.println(
                    "Rejected Reasons Loaded = "
                  + reasons.size());

        } catch (Exception e) {

            e.printStackTrace();

            throw new RuntimeException(
                    "Unable to load rejected reasons.",
                    e);
        }

        return reasons;
    }

    // =========================================================
    // DUPLICATE CHEQUE VALIDATION
    // =========================================================

    @Override
    public boolean existsChequeNumberAndAccount(
            String chequeNumber,
            String accountNumber) {

        if (chequeNumber == null
                || chequeNumber.trim().isEmpty()) {

            return false;
        }

        if (accountNumber == null
                || accountNumber.trim().isEmpty()) {

            return false;
        }

        String sql =
                "SELECT COUNT(*) "
              + "FROM scan_cheque "
              + "WHERE TRIM(cheque_number) = ? "
              + "AND TRIM(drawee_account_number) = ?";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement preparedStatement =
                        connection.prepareStatement(sql)
        ) {

            preparedStatement.setString(
                    1,
                    chequeNumber.trim());

            preparedStatement.setString(
                    2,
                    accountNumber.trim());

            try (ResultSet resultSet =
                    preparedStatement.executeQuery()) {

                if (resultSet.next()) {

                    return resultSet.getInt(1) > 0;
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Unable to check duplicate cheque. "
                  + "Cheque number: " + chequeNumber
                  + ", Account number: " + accountNumber,
                    e);
        }

        return false;
    }

    @Override
    public ScanBatch getMakerBatch(String batchId) {

        if (batchId == null
                || batchId.trim().isEmpty()) {

            return null;
        }

        String sql =
                "SELECT "
              + "sb.scanned_batch_id, "
              + "sb.batch_reference_id, "
              + "sb.actual_cheque_count, "
              + "sb.actual_total_amount, "
              + "sb.staging_status, "
              + "sb.batch_status, "
              + "sb.uploaded_by, "
              + "sb.uploaded_at "
              + "FROM scan_batch sb "
              + "WHERE sb.scanned_batch_id = ? "
              + "AND NOT EXISTS ("
              + "    SELECT 1 "
              + "    FROM outward_batch ob "
              + "    WHERE ob.outward_batch_id = sb.scanned_batch_id"
              + ")";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement preparedStatement =
                        connection.prepareStatement(sql)
        ) {

            preparedStatement.setString(
                    1,
                    batchId.trim());

            try (ResultSet resultSet =
                    preparedStatement.executeQuery()) {

                if (resultSet.next()) {

                    ScanBatch batch =
                            new ScanBatch();

                    batch.setScannedBatchId(
                            resultSet.getString(
                                    "scanned_batch_id"));

                    batch.setBatchReferenceId(
                            resultSet.getString(
                                    "batch_reference_id"));

                    batch.setActualChequeCount(
                            resultSet.getInt(
                                    "actual_cheque_count"));

                    batch.setActualTotalAmount(
                            resultSet.getBigDecimal(
                                    "actual_total_amount"));

                    batch.setStagingStatus(
                            resultSet.getString(
                                    "staging_status"));

                    batch.setBatchStatus(
                            resultSet.getString(
                                    "batch_status"));

                    batch.setUploadedBy(
                            resultSet.getString(
                                    "uploaded_by"));

                    batch.setUploadedAt(
                            resultSet.getTimestamp(
                                    "uploaded_at"));

                    return batch;
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Unable to fetch maker batch: "
                  + batchId,
                    e);
        }

        return null;
    }

    @Override
    public List<ScanCheque> getMakerBatchCheques(String batchId) {

        if (batchId == null
                || batchId.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Batch ID cannot be null or empty");
        }

        List<ScanCheque> chequeList =
                new ArrayList<ScanCheque>();

        String sql =
                "SELECT "
              + "sc.scanned_cheque_id, "
              + "sc.scanned_batch_id, "
              + "sc.cheque_number, "
              + "sc.micr_code, "
              + "sc.drawee_name, "
              + "sc.drawee_account_number, "
              + "sc.payee_name, "
              + "sc.payee_account_number, "
              + "sc.cheque_amount, "
              + "sc.cheque_date, "
              + "sc.cheque_status, "
              + "sc.account_id, "
              + "sc.created_at, "
              + "sc.city_code, "
              + "sc.bank_code, "
              + "sc.branch_code, "
              + "sc.cheque_image_front, "
              + "sc.cheque_image_back "
              + "FROM scan_cheque sc "
              + "WHERE sc.scanned_batch_id = ? "
              + "ORDER BY sc.scanned_cheque_id";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement preparedStatement =
                        connection.prepareStatement(sql)
        ) {

            preparedStatement.setString(
                    1,
                    batchId.trim());

            try (ResultSet resultSet =
                    preparedStatement.executeQuery()) {

                while (resultSet.next()) {

                    ScanCheque cheque =
                            new ScanCheque();

                    cheque.setScannedChequeId(
                            resultSet.getString(
                                    "scanned_cheque_id"));

                    cheque.setScannedBatchId(
                            resultSet.getString(
                                    "scanned_batch_id"));

                    cheque.setChequeNumber(
                            resultSet.getString(
                                    "cheque_number"));

                    cheque.setMicrCode(
                            resultSet.getString(
                                    "micr_code"));

                    cheque.setDraweeName(
                            resultSet.getString(
                                    "drawee_name"));

                    cheque.setDraweeAccountNumber(
                            resultSet.getString(
                                    "drawee_account_number"));

                    cheque.setPayeeName(
                            resultSet.getString(
                                    "payee_name"));

                    cheque.setPayeeAccountNumber(
                            resultSet.getString(
                                    "payee_account_number"));

                    cheque.setChequeAmount(
                            resultSet.getBigDecimal(
                                    "cheque_amount"));

                    cheque.setChequeDate(
                            resultSet.getDate(
                                    "cheque_date"));

                    cheque.setChequeStatus(
                            resultSet.getString(
                                    "cheque_status"));

                    cheque.setAccountId(
                            resultSet.getString(
                                    "account_id"));

                    cheque.setCreatedAt(
                            resultSet.getTimestamp(
                                    "created_at"));

                    cheque.setCityCode(
                            resultSet.getString(
                                    "city_code"));

                    cheque.setBankCode(
                            resultSet.getString(
                                    "bank_code"));

                    cheque.setBranchCode(
                            resultSet.getString(
                                    "branch_code"));

                    cheque.setChequeImageFront(
                            resultSet.getString(
                                    "cheque_image_front"));

                    cheque.setChequeImageBack(
                            resultSet.getString(
                                    "cheque_image_back"));

                    chequeList.add(cheque);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Unable to fetch maker batch cheques "
                  + "for batch: " + batchId,
                    e);
        }

        return chequeList;
    }
}