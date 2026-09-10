
package com.iispl.cts.daoimpl.outward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.ScanChequeDAO;
import com.iispl.cts.entity.outward.ScanCheque;

public class ScanChequeDAOImpl implements ScanChequeDAO {

    @Override
    public String saveBatch(Connection connection, List<ScanCheque> chequeList) {

        if (connection == null) {
            throw new IllegalArgumentException("Database connection cannot be null");
        }

        if (chequeList == null || chequeList.isEmpty()) {
            throw new IllegalArgumentException("Cheque list cannot be null or empty");
        }

        String checkSql =
                "SELECT scanned_cheque_id " +
                "FROM scan_cheque " +
                "WHERE scanned_cheque_id = ?";

        String insertSql =
                "INSERT INTO scan_cheque (" +
                "scanned_cheque_id, " +
                "scanned_batch_id, " +
                "cheque_number, " +
                "micr_code, " +
                "drawee_name, " +
                "drawee_account_number, " +
                "payee_name, " +
                "payee_account_number, " +
                "cheque_amount, " +
                "cheque_date, " +
                "cheque_status, " +
                "account_id, " +
                "created_at, " +
                "city_code, " +
                "bank_code, " +
                "branch_code, " +
                "cheque_image_front, " +
                "cheque_image_back" +
                ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String updateSql =
                "UPDATE scan_cheque SET " +
                "scanned_batch_id = ?, " +
                "cheque_number = ?, " +
                "micr_code = ?, " +
                "drawee_name = ?, " +
                "drawee_account_number = ?, " +
                "payee_name = ?, " +
                "payee_account_number = ?, " +
                "cheque_amount = ?, " +
                "cheque_date = ?, " +
                "cheque_status = ?, " +
                "account_id = ?, " +
                "created_at = ?, " +
                "city_code = ?, " +
                "bank_code = ?, " +
                "branch_code = ?, " +
                "cheque_image_front = ?, " +
                "cheque_image_back = ? " +
                "WHERE scanned_cheque_id = ?";

        String scannedBatchId = null;

        try (
                PreparedStatement checkStatement =
                        connection.prepareStatement(checkSql);

                PreparedStatement insertStatement =
                        connection.prepareStatement(insertSql);

                PreparedStatement updateStatement =
                        connection.prepareStatement(updateSql)
        ) {

            for (ScanCheque cheque : chequeList) {

                if (cheque == null) {
                    throw new IllegalArgumentException(
                            "Cheque object cannot be null");
                }

                if (cheque.getScannedChequeId() == null ||
                        cheque.getScannedChequeId().trim().isEmpty()) {

                    throw new IllegalArgumentException(
                            "Scanned cheque ID cannot be null or empty");
                }

                /*
                 * Get batch ID
                 */
                if (scannedBatchId == null) {

                    scannedBatchId = cheque.getScannedBatchId();

                } else if (!scannedBatchId.equals(
                        cheque.getScannedBatchId())) {

                    throw new IllegalArgumentException(
                            "Cheque list contains multiple batch IDs");
                }

                /*
                 * Check whether cheque already exists
                 */
                boolean chequeExists = false;

                checkStatement.clearParameters();
                checkStatement.setString(
                        1,
                        cheque.getScannedChequeId());

                try (ResultSet resultSet =
                        checkStatement.executeQuery()) {

                    if (resultSet.next()) {
                        chequeExists = true;
                    }
                }

                /*
                 * Existing cheque -> UPDATE
                 */
                if (chequeExists) {

                    updateStatement.clearParameters();

                    updateStatement.setString(
                            1, cheque.getScannedBatchId());

                    updateStatement.setString(
                            2, cheque.getChequeNumber());

                    updateStatement.setString(
                            3, cheque.getMicrCode());

                    updateStatement.setString(
                            4, cheque.getDraweeName());

                    updateStatement.setString(
                            5, cheque.getDraweeAccountNumber());

                    updateStatement.setString(
                            6, cheque.getPayeeName());

                    updateStatement.setString(
                            7, cheque.getPayeeAccountNumber());

                    updateStatement.setBigDecimal(
                            8, cheque.getChequeAmount());

                    updateStatement.setDate(
                            9, cheque.getChequeDate());

                    updateStatement.setString(
                            10, cheque.getChequeStatus());

                    updateStatement.setString(
                            11, cheque.getAccountId());

                    if (cheque.getCreatedAt() != null) {

                        updateStatement.setTimestamp(
                                12, cheque.getCreatedAt());

                    } else {

                        updateStatement.setTimestamp(
                                12,
                                new java.sql.Timestamp(
                                        System.currentTimeMillis()));
                    }

                    updateStatement.setString(
                            13, cheque.getCityCode());

                    updateStatement.setString(
                            14, cheque.getBankCode());

                    updateStatement.setString(
                            15, cheque.getBranchCode());

                    updateStatement.setString(
                            16, cheque.getChequeImageFront());

                    updateStatement.setString(
                            17, cheque.getChequeImageBack());

                    updateStatement.setString(
                            18, cheque.getScannedChequeId());

                    updateStatement.executeUpdate();

                } else {

                    /*
                     * New cheque -> INSERT
                     */

                    insertStatement.clearParameters();

                    insertStatement.setString(
                            1, cheque.getScannedChequeId());

                    insertStatement.setString(
                            2, cheque.getScannedBatchId());

                    insertStatement.setString(
                            3, cheque.getChequeNumber());

                    insertStatement.setString(
                            4, cheque.getMicrCode());

                    insertStatement.setString(
                            5, cheque.getDraweeName());

                    insertStatement.setString(
                            6, cheque.getDraweeAccountNumber());

                    insertStatement.setString(
                            7, cheque.getPayeeName());

                    insertStatement.setString(
                            8, cheque.getPayeeAccountNumber());

                    insertStatement.setBigDecimal(
                            9, cheque.getChequeAmount());

                    insertStatement.setDate(
                            10, cheque.getChequeDate());

                    insertStatement.setString(
                            11, cheque.getChequeStatus());

                    insertStatement.setString(
                            12, cheque.getAccountId());

                    if (cheque.getCreatedAt() != null) {

                        insertStatement.setTimestamp(
                                13, cheque.getCreatedAt());

                    } else {

                        insertStatement.setTimestamp(
                                13,
                                new java.sql.Timestamp(
                                        System.currentTimeMillis()));
                    }

                    insertStatement.setString(
                            14, cheque.getCityCode());

                    insertStatement.setString(
                            15, cheque.getBankCode());

                    insertStatement.setString(
                            16, cheque.getBranchCode());

                    insertStatement.setString(
                            17, cheque.getChequeImageFront());

                    insertStatement.setString(
                            18, cheque.getChequeImageBack());

                    insertStatement.executeUpdate();
                }
            }

        } catch (SQLException e) {

            /*
             * Do NOT rollback here.
             *
             * ScanServiceImpl owns the transaction.
             */
            throw new RuntimeException(
                    "Error while saving scanned cheques", e);
        }

        System.out.println(
                "Scanned cheque data processed successfully.");

        return scannedBatchId;
    }


    @Override
    public List<ScanCheque> getChequesByBatchId(
            String scannedBatchId) {

        if (scannedBatchId == null ||
                scannedBatchId.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Scanned batch ID cannot be null or empty");
        }

        String sql =
                "SELECT " +
                "scanned_cheque_id, " +
                "scanned_batch_id, " +
                "cheque_number, " +
                "micr_code, " +
                "drawee_name, " +
                "drawee_account_number, " +
                "payee_name, " +
                "payee_account_number, " +
                "cheque_amount, " +
                "cheque_date, " +
                "cheque_status, " +
                "account_id, " +
                "created_at, " +
                "city_code, " +
                "bank_code, " +
                "branch_code, " +
                "cheque_image_front, " +
                "cheque_image_back " +
                "FROM scan_cheque " +
                "WHERE scanned_batch_id = ? " +
                "ORDER BY scanned_cheque_id";

        List<ScanCheque> chequeList =
                new ArrayList<>();

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, scannedBatchId);

            try (ResultSet resultSet =
                    statement.executeQuery()) {

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

            return chequeList;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error while retrieving cheques for batch: "
                            + scannedBatchId,
                    e);
        }
    }


    @Override
    public void updateChequeStatus(
            Connection connection,
            String batchId,
            String status) {

        if (connection == null) {
            throw new IllegalArgumentException(
                    "Connection cannot be null");
        }

        if (batchId == null ||
                batchId.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Batch ID cannot be null or empty");
        }

        if (status == null ||
                status.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Cheque status cannot be null or empty");
        }

        String sql =
                "UPDATE scan_cheque " +
                "SET cheque_status = ? " +
                "WHERE scanned_batch_id = ?";

        try (PreparedStatement ps =
                connection.prepareStatement(sql)) {

            ps.setString(1, status);
            ps.setString(2, batchId);

            int rowsUpdated =
                    ps.executeUpdate();

            if (rowsUpdated == 0) {

                throw new IllegalStateException(
                        "No cheques found for batch ID: "
                                + batchId);
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to update cheque status for batch ID: "
                            + batchId,
                    e);
        }
    }


    @Override
    public List<ScanCheque> getScanMicrRepairCheques(
            String scannedBatchId) {

        if (scannedBatchId == null ||
                scannedBatchId.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Scanned batch ID cannot be null or empty");
        }

        String sql =
                "SELECT " +
                "scanned_cheque_id, " +
                "scanned_batch_id, " +
                "cheque_number, " +
                "micr_code, " +
                "drawee_name, " +
                "drawee_account_number, " +
                "payee_name, " +
                "payee_account_number, " +
                "cheque_amount, " +
                "cheque_date, " +
                "cheque_status, " +
                "account_id, " +
                "created_at, " +
                "city_code, " +
                "bank_code, " +
                "branch_code, " +
                "cheque_image_front, " +
                "cheque_image_back " +
                "FROM scan_cheque " +
                "WHERE scanned_batch_id = ? " +
                "AND UPPER(TRIM(cheque_status)) = " +
                "'PENDING_MICR_REPAIR' " +
                "ORDER BY scanned_cheque_id";

        List<ScanCheque> chequeList =
                new ArrayList<>();

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, scannedBatchId);

            try (ResultSet resultSet =
                    statement.executeQuery()) {

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

            return chequeList;

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error while retrieving MICR repair cheques "
                            + "for batch: " + scannedBatchId,
                    e);
        }
    }


    @Override
    public int getDataEnteredCountByBatchId(
            String scannedBatchId) {

        if (scannedBatchId == null ||
                scannedBatchId.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Scanned batch ID cannot be null or empty");
        }

        String sql =
                "SELECT COUNT(scanned_cheque_id) " +
                "FROM scan_cheque " +
                "WHERE scanned_batch_id = ? " +
                "AND UPPER(TRIM(cheque_status)) NOT IN " +
                "('PENDING_DATA_ENTRY', " +
                "'PENDING_MICR_REPAIR', " +
                "'MICR_REPAIR', " +
                "'MICR_REPAIR_REQUIRED')";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(1, scannedBatchId);

            try (ResultSet resultSet =
                    statement.executeQuery()) {

                if (resultSet.next()) {

                    return resultSet.getInt(1);
                }
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Error while retrieving data entered count "
                            + "for batch: " + scannedBatchId,
                    e);
        }

        return 0;
    }


    @Override
    public void saveScanMicrRepair(
            ScanCheque cheque) {

        if (cheque == null) {

            throw new IllegalArgumentException(
                    "Scan cheque cannot be null");
        }

        if (cheque.getScannedChequeId() == null ||
                cheque.getScannedChequeId().trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Scanned cheque ID cannot be null or empty");
        }

        String sql =
                "UPDATE scan_cheque SET " +
                "micr_code = ?, " +
                "city_code = ?, " +
                "bank_code = ?, " +
                "branch_code = ?, " +
                "cheque_status = ? " +
                "WHERE scanned_cheque_id = ?";

        try (
                Connection connection =
                        DBConnection.getConnection();

                PreparedStatement statement =
                        connection.prepareStatement(sql)
        ) {

            statement.setString(
                    1, cheque.getMicrCode());

            statement.setString(
                    2, cheque.getCityCode());

            statement.setString(
                    3, cheque.getBankCode());

            statement.setString(
                    4, cheque.getBranchCode());

            statement.setString(
                    5, cheque.getChequeStatus());

            statement.setString(
                    6, cheque.getScannedChequeId());

            int rowsUpdated =
                    statement.executeUpdate();

            if (rowsUpdated == 0) {

                throw new IllegalStateException(
                        "Scan cheque not found for ID: "
                                + cheque.getScannedChequeId());
            }

        } catch (SQLException e) {

            throw new RuntimeException(
                    "Failed to save MICR repair for scan cheque: "
                            + cheque.getScannedChequeId(),
                    e);
        }
    }
}

