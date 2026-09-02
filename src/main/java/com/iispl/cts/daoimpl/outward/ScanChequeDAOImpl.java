package com.iispl.cts.daoimpl.outward;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.iispl.cts.dao.outward.ScanChequeDAO;
import com.iispl.cts.entity.outward.ScanCheque;

public class ScanChequeDAOImpl implements ScanChequeDAO {

    @Override
    public String saveBatch(
            Connection connection,
            List<ScanCheque> chequeList) {

        if (connection == null) {
            throw new IllegalArgumentException(
                    "Database connection cannot be null");
        }

        if (chequeList == null
                || chequeList.isEmpty()) {

            throw new IllegalArgumentException(
                    "Cheque list cannot be null or empty");
        }

        String checkSql =
                "SELECT scanned_cheque_id "
              + "FROM scan_cheque "
              + "WHERE scanned_cheque_id = ?";

        String insertSql =
                "INSERT INTO scan_cheque ("
              + "scanned_cheque_id, "
              + "scanned_batch_id, "
              + "cheque_number, "
              + "micr_code, "
              + "drawee_name, "
              + "drawee_account_number, "
              + "payee_name, "
              + "payee_account_number, "
              + "cheque_amount, "
              + "cheque_date, "
              + "cheque_status, "
              + "account_id, "
              + "created_at, "
              + "city_code, "
              + "bank_code, "
              + "branch_code"
              + ") VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

        String updateSql =
                "UPDATE scan_cheque SET "
              + "scanned_batch_id = ?, "
              + "cheque_number = ?, "
              + "micr_code = ?, "
              + "drawee_name = ?, "
              + "drawee_account_number = ?, "
              + "payee_name = ?, "
              + "payee_account_number = ?, "
              + "cheque_amount = ?, "
              + "cheque_date = ?, "
              + "cheque_status = ?, "
              + "account_id = ?, "
              + "created_at = ?, "
              + "city_code = ?, "
              + "bank_code = ?, "
              + "branch_code = ? "
              + "WHERE scanned_cheque_id = ?";

        String scannedBatchId = null;

        try {

            /*
             * =====================================================
             * Prepare statements using the SAME connection
             * received from ScanServiceImpl.
             * =====================================================
             */

            try (
                    PreparedStatement checkStatement =
                            connection.prepareStatement(
                                    checkSql);

                    PreparedStatement insertStatement =
                            connection.prepareStatement(
                                    insertSql);

                    PreparedStatement updateStatement =
                            connection.prepareStatement(
                                    updateSql)) {

                /*
                 * =================================================
                 * Process every cheque
                 * =================================================
                 */

                for (ScanCheque cheque : chequeList) {

                    if (cheque == null) {

                        throw new IllegalArgumentException(
                                "Cheque object cannot be null");
                    }

                    if (cheque.getScannedChequeId() == null
                            || cheque.getScannedChequeId()
                                    .trim()
                                    .isEmpty()) {

                        throw new IllegalArgumentException(
                                "Scanned cheque ID cannot be null "
                                + "or empty");
                    }

                    /*
                     * -------------------------------------------------
                     * Get batch ID
                     * -------------------------------------------------
                     */

                    if (scannedBatchId == null) {

                        scannedBatchId =
                                cheque.getScannedBatchId();

                    } else if (!scannedBatchId.equals(
                            cheque.getScannedBatchId())) {

                        throw new IllegalArgumentException(
                                "Cheque list contains multiple "
                                + "batch IDs");
                    }

                    /*
                     * =================================================
                     * Check whether cheque already exists
                     * =================================================
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
                     * =================================================
                     * CASE 1:
                     * Existing cheque → UPDATE
                     * =================================================
                     */

                    if (chequeExists) {

                        updateStatement.clearParameters();

                        /*
                         * 1. scanned_batch_id
                         */
                        updateStatement.setString(
                                1,
                                cheque.getScannedBatchId());

                        /*
                         * 2. cheque_number
                         */
                        updateStatement.setString(
                                2,
                                cheque.getChequeNumber());

                        /*
                         * 3. micr_code
                         */
                        updateStatement.setString(
                                3,
                                cheque.getMicrCode());

                        /*
                         * 4. drawee_name
                         */
                        updateStatement.setString(
                                4,
                                cheque.getDraweeName());

                        /*
                         * 5. drawee_account_number
                         */
                        updateStatement.setString(
                                5,
                                cheque.getDraweeAccountNumber());

                        /*
                         * 6. payee_name
                         */
                        updateStatement.setString(
                                6,
                                cheque.getPayeeName());

                        /*
                         * 7. payee_account_number
                         */
                        updateStatement.setString(
                                7,
                                cheque.getPayeeAccountNumber());

                        /*
                         * 8. cheque_amount
                         */
                        updateStatement.setBigDecimal(
                                8,
                                cheque.getChequeAmount());

                        /*
                         * 9. cheque_date
                         */
                        updateStatement.setDate(
                                9,
                                cheque.getChequeDate());

                        /*
                         * 10. cheque_status
                         */
                        updateStatement.setString(
                                10,
                                cheque.getChequeStatus());

                        /*
                         * 11. account_id
                         */
                        updateStatement.setString(
                                11,
                                cheque.getAccountId());

                        /*
                         * 12. created_at
                         */
                        if (cheque.getCreatedAt() != null) {

                            updateStatement.setTimestamp(
                                    12,
                                    cheque.getCreatedAt());

                        } else {

                            updateStatement.setTimestamp(
                                    12,
                                    new java.sql.Timestamp(
                                            System.currentTimeMillis()));
                        }

                        /*
                         * 13. city_code
                         */
                        updateStatement.setString(
                                13,
                                cheque.getCityCode());

                        /*
                         * 14. bank_code
                         */
                        updateStatement.setString(
                                14,
                                cheque.getBankCode());

                        /*
                         * 15. branch_code
                         */
                        updateStatement.setString(
                                15,
                                cheque.getBranchCode());

                        /*
                         * 16. scanned_cheque_id
                         */
                        updateStatement.setString(
                                16,
                                cheque.getScannedChequeId());

                        updateStatement.executeUpdate();

                    } else {

                        /*
                         * =================================================
                         * CASE 2:
                         * New cheque → INSERT
                         * =================================================
                         */

                        insertStatement.clearParameters();

                        /*
                         * 1. scanned_cheque_id
                         */
                        insertStatement.setString(
                                1,
                                cheque.getScannedChequeId());

                        /*
                         * 2. scanned_batch_id
                         */
                        insertStatement.setString(
                                2,
                                cheque.getScannedBatchId());

                        /*
                         * 3. cheque_number
                         */
                        insertStatement.setString(
                                3,
                                cheque.getChequeNumber());

                        /*
                         * 4. micr_code
                         */
                        insertStatement.setString(
                                4,
                                cheque.getMicrCode());

                        /*
                         * 5. drawee_name
                         */
                        insertStatement.setString(
                                5,
                                cheque.getDraweeName());

                        /*
                         * 6. drawee_account_number
                         */
                        insertStatement.setString(
                                6,
                                cheque.getDraweeAccountNumber());

                        /*
                         * 7. payee_name
                         */
                        insertStatement.setString(
                                7,
                                cheque.getPayeeName());

                        /*
                         * 8. payee_account_number
                         */
                        insertStatement.setString(
                                8,
                                cheque.getPayeeAccountNumber());

                        /*
                         * 9. cheque_amount
                         */
                        insertStatement.setBigDecimal(
                                9,
                                cheque.getChequeAmount());

                        /*
                         * 10. cheque_date
                         */
                        insertStatement.setDate(
                                10,
                                cheque.getChequeDate());

                        /*
                         * 11. cheque_status
                         */
                        insertStatement.setString(
                                11,
                                cheque.getChequeStatus());

                        /*
                         * 12. account_id
                         *
                         * Nullable in database.
                         */
                        insertStatement.setString(
                                12,
                                cheque.getAccountId());

                        /*
                         * 13. created_at
                         */
                        if (cheque.getCreatedAt() != null) {

                            insertStatement.setTimestamp(
                                    13,
                                    cheque.getCreatedAt());

                        } else {

                            insertStatement.setTimestamp(
                                    13,
                                    new java.sql.Timestamp(
                                            System.currentTimeMillis()));
                        }

                        /*
                         * 14. city_code
                         */
                        insertStatement.setString(
                                14,
                                cheque.getCityCode());

                        /*
                         * 15. bank_code
                         */
                        insertStatement.setString(
                                15,
                                cheque.getBankCode());

                        /*
                         * 16. branch_code
                         */
                        insertStatement.setString(
                                16,
                                cheque.getBranchCode());

                        insertStatement.executeUpdate();
                    }
                }
            }

            System.out.println(
                    "Scanned cheque data processed successfully.");

            return scannedBatchId;

        } catch (SQLException e) {

            /*
             * IMPORTANT:
             *
             * Do NOT rollback here.
             *
             * ScanServiceImpl owns the transaction and
             * will rollback the batch + cheque operations.
             */

            throw new RuntimeException(
                    "Error while saving scanned cheques",
                    e);
        }
    }

    @Override
    public List<ScanCheque> getChequesByBatchId(
            String scannedBatchId) {

        if (scannedBatchId == null
                || scannedBatchId.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "Scanned batch ID cannot be null or empty");
        }

        String sql =
                "SELECT "
              + "scanned_cheque_id, "
              + "scanned_batch_id, "
              + "cheque_number, "
              + "micr_code, "
              + "drawee_name, "
              + "drawee_account_number, "
              + "payee_name, "
              + "payee_account_number, "
              + "cheque_amount, "
              + "cheque_date, "
              + "cheque_status, "
              + "account_id, "
              + "created_at, "
              + "city_code, "
              + "bank_code, "
              + "branch_code "
              + "FROM scan_cheque "
              + "WHERE scanned_batch_id = ? "
              + "ORDER BY scanned_cheque_id";

        List<ScanCheque> chequeList =
                new java.util.ArrayList<>();

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
}