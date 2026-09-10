package com.iispl.cts.dao.outward;

import java.sql.SQLException;
import java.util.List;

import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.OutwardChequeImage;
import com.iispl.cts.entity.outward.SendBackReason;

public interface OutwardCheckerQueueDAO {

    // ============================================================
    // GET CHEQUES BY SELECTED BATCH
    // ============================================================

    List<OutwardCheque> getChequesByBatchId(String batchId)
            throws SQLException;

    // ============================================================
    // GET BATCH STATUS
    // ============================================================

    String getBatchStatus(String batchId)
            throws SQLException;

    // ============================================================
    // UPDATE CHEQUE STATUS
    // ============================================================

    void updateChequeStatus(
            String chequeNo,
            String status)
            throws SQLException;

    // ============================================================
    // GET CHEQUE IMAGES
    // ============================================================

    List<OutwardChequeImage> getImagesByChequeId(
            String outwardChequeId)
            throws SQLException;

    // ============================================================
    // GET SEND BACK REASONS
    // ============================================================

    List<SendBackReason> getSendBackReasons()
            throws SQLException;

    // ============================================================
    // CHECK PAYEE ACCOUNT
    // ============================================================

    boolean isPayeeAccountExists(
            String accountNumber)
            throws SQLException;

    // ============================================================
    // UPDATE BATCH STATUS
    // ============================================================

    void updateBatchStatus(
            String batchId,
            String status)
            throws SQLException;
}