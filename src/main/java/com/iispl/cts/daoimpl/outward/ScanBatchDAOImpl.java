package com.iispl.cts.daoimpl.outward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.iispl.cts.dao.outward.ScanBatchDAO;
import com.iispl.cts.entity.outward.ScanBatch;

public class ScanBatchDAOImpl implements ScanBatchDAO {

    @Override
    public String saveBatch(
            Connection connection,
            ScanBatch scanBatch) {

        if (scanBatch == null) {
            throw new IllegalArgumentException(
                    "Scan batch cannot be null");
        }

        if (connection == null) {
            throw new IllegalArgumentException(
                    "Database connection cannot be null");
        }

        if (scanBatch.getScannedBatchId() == null
                || scanBatch.getScannedBatchId().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Scanned batch ID cannot be null or empty");
        }

        String selectSql =
                "SELECT staging_status "
              + "FROM scan_batch "
              + "WHERE scanned_batch_id = ?";

        String insertSql =
                "INSERT INTO scan_batch "
              + "(scanned_batch_id, batch_reference_id, "
              + "actual_cheque_count, actual_total_amount, "
              + "staging_status, batch_status, uploaded_by, uploaded_at) "
              + "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

        String updateSql =
                "UPDATE scan_batch SET "
              + "batch_reference_id = ?, "
              + "actual_cheque_count = ?, "
              + "actual_total_amount = ?, "
              + "staging_status = ?, "
              + "batch_status = ?, "
              + "uploaded_by = ?, "
              + "uploaded_at = ? "
              + "WHERE scanned_batch_id = ?";

        try {

            /*
             * =====================================================
             * Check whether batch already exists
             * =====================================================
             */

            try (PreparedStatement selectStatement =
                    connection.prepareStatement(selectSql)) {

                selectStatement.setString(
                        1,
                        scanBatch.getScannedBatchId());

                try (ResultSet resultSet =
                        selectStatement.executeQuery()) {

                    /*
                     * =================================================
                     * CASE 1:
                     * Batch does not exist
                     * =================================================
                     */

                    if (!resultSet.next()) {

                        try (PreparedStatement insertStatement =
                                connection.prepareStatement(insertSql)) {

                            insertStatement.setString(
                                    1,
                                    scanBatch.getScannedBatchId());

                            insertStatement.setString(
                                    2,
                                    scanBatch.getBatchReferenceId());

                            insertStatement.setInt(
                                    3,
                                    scanBatch.getActualChequeCount());

                            insertStatement.setBigDecimal(
                                    4,
                                    scanBatch.getActualTotalAmount());

                            insertStatement.setString(
                                    5,
                                    scanBatch.getStagingStatus());

                            insertStatement.setString(
                                    6,
                                    scanBatch.getBatchStatus());

                            insertStatement.setString(
                                    7,
                                    scanBatch.getUploadedBy());

                            if (scanBatch.getUploadedAt() != null) {

                                insertStatement.setTimestamp(
                                        8,
                                        scanBatch.getUploadedAt());

                            } else {

                                insertStatement.setTimestamp(
                                        8,
                                        new java.sql.Timestamp(
                                                System.currentTimeMillis()));
                            }

                            insertStatement.executeUpdate();
                        }

                        return scanBatch.getScannedBatchId();
                    }

                    /*
                     * =================================================
                     * CASE 2:
                     * Batch already exists
                     * =================================================
                     */

                    String existingStagingStatus =
                            resultSet.getString(
                                    "staging_status");

                    /*
                     * Existing batch can only be updated
                     * when staging status is RAW.
                     */

                    if (!"RAW".equalsIgnoreCase(
                            existingStagingStatus)) {

                        throw new IllegalStateException(
                                "Batch "
                                + scanBatch.getScannedBatchId()
                                + " is already validated or processed "
                                + "and cannot be uploaded again.");
                    }
                }
            }

            /*
             * =====================================================
             * CASE 3:
             * Existing batch with RAW status
             *
             * Update current batch information.
             * =====================================================
             */

            try (PreparedStatement updateStatement =
                    connection.prepareStatement(updateSql)) {

                updateStatement.setString(
                        1,
                        scanBatch.getBatchReferenceId());

                updateStatement.setInt(
                        2,
                        scanBatch.getActualChequeCount());

                updateStatement.setBigDecimal(
                        3,
                        scanBatch.getActualTotalAmount());

                updateStatement.setString(
                        4,
                        scanBatch.getStagingStatus());

                updateStatement.setString(
                        5,
                        scanBatch.getBatchStatus());

                updateStatement.setString(
                        6,
                        scanBatch.getUploadedBy());

                if (scanBatch.getUploadedAt() != null) {

                    updateStatement.setTimestamp(
                            7,
                            scanBatch.getUploadedAt());

                } else {

                    updateStatement.setTimestamp(
                            7,
                            new java.sql.Timestamp(
                                    System.currentTimeMillis()));
                }

                updateStatement.setString(
                        8,
                        scanBatch.getScannedBatchId());

                updateStatement.executeUpdate();
            }

            return scanBatch.getScannedBatchId();

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error while saving scan batch: "
                    + scanBatch.getScannedBatchId(),
                    e);
        }
    }

    @Override
    public ScanBatch getBatchById(
            String scannedBatchId) {

        if (scannedBatchId == null
                || scannedBatchId.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Scanned batch ID cannot be null or empty");
        }

        String sql =
                "SELECT "
              + "scanned_batch_id, "
              + "batch_reference_id, "
              + "actual_cheque_count, "
              + "actual_total_amount, "
              + "staging_status, "
              + "batch_status, "
              + "uploaded_by, "
              + "uploaded_at "
              + "FROM scan_batch "
              + "WHERE scanned_batch_id = ?";

        try (Connection connection =
                    com.iispl.cts.common.config.DBConnection
                            .getConnection();

             PreparedStatement statement =
                    connection.prepareStatement(sql)) {

            statement.setString(
                    1,
                    scannedBatchId);

            try (ResultSet resultSet =
                    statement.executeQuery()) {

                if (!resultSet.next()) {
                    return null;
                }

                ScanBatch scanBatch =
                        new ScanBatch();

                scanBatch.setScannedBatchId(
                        resultSet.getString(
                                "scanned_batch_id"));

                scanBatch.setBatchReferenceId(
                        resultSet.getString(
                                "batch_reference_id"));

                scanBatch.setActualChequeCount(
                        resultSet.getInt(
                                "actual_cheque_count"));

                scanBatch.setActualTotalAmount(
                        resultSet.getBigDecimal(
                                "actual_total_amount"));

                scanBatch.setStagingStatus(
                        resultSet.getString(
                                "staging_status"));

                scanBatch.setBatchStatus(
                        resultSet.getString(
                                "batch_status"));

                scanBatch.setUploadedBy(
                        resultSet.getString(
                                "uploaded_by"));

                scanBatch.setUploadedAt(
                        resultSet.getTimestamp(
                                "uploaded_at"));

                return scanBatch;
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error while retrieving scan batch: "
                    + scannedBatchId,
                    e);
        }
    }
        @Override
        public void updateBatchStatus(
                Connection connection,
                String batchId,
                String status) {

            if (connection == null) {
                throw new IllegalArgumentException(
                        "Connection cannot be null");
            }

            if (batchId == null || batchId.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Batch ID cannot be null or empty");
            }

            if (status == null || status.trim().isEmpty()) {
                throw new IllegalArgumentException(
                        "Batch status cannot be null or empty");
            }

            String sql =
                    "UPDATE scan_batch " +
                    "SET staging_status = ? " +
                    "WHERE scanned_batch_id = ?";

            try (PreparedStatement ps =
                         connection.prepareStatement(sql)) {

                ps.setString(1, status);
                ps.setString(2, batchId);

                int rowsUpdated = ps.executeUpdate();

                if (rowsUpdated == 0) {
                    throw new IllegalStateException(
                            "Scan batch not found for batch ID: "
                            + batchId);
                }

            } catch (SQLException e) {
                throw new RuntimeException(
                        "Failed to update scan batch status for batch ID: "
                        + batchId, e);
            }
        
    }
}