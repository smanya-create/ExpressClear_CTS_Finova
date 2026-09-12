package com.iispl.cts.dao.outward;

import java.sql.SQLException;
import java.util.List;

import com.iispl.cts.dto.RejectRequestDTO;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.OutwardChequeImage;
import com.iispl.cts.entity.outward.OutwardRejectedCheques;
import com.iispl.cts.entity.outward.RejectedReason;
import com.iispl.cts.entity.outward.SendBackReason;

public interface OutwardCheckerQueueDAO {

    // ============================================================
    // GET CHEQUES BY SELECTED BATCH
    // ============================================================

    List<OutwardCheque> getChequesByBatchId(
            String batchId) throws SQLException;


    // ============================================================
    // GET BATCH STATUS
    // ============================================================

    String getBatchStatus(
            String batchId) throws SQLException;


    // ============================================================
    // UPDATE CHEQUE STATUS
    // ============================================================

    void updateChequeStatus(
            String chequeNo,
            String status) throws SQLException;


    // ============================================================
    // REJECT CHEQUE
    //
    // This method:
    //
    // 1. Gets user_id from username
    // 2. Gets cheque information
    // 3. Inserts rejected cheque
    // 4. Changes cheque status to REJECTED
    // 5. Commits both operations
    //
    // ============================================================

    void rejectCheque(
            String chequeNo,
            String username,
            String remarks) throws SQLException;


    // ============================================================
    // GET CHEQUE IMAGES
    // ============================================================

    List<OutwardChequeImage> getImagesByChequeId(
            String outwardChequeId) throws SQLException;


    // ============================================================
    // GET SEND BACK REASONS
    // ============================================================

    List<SendBackReason> getSendBackReasons()
            throws SQLException;


    // ============================================================
    // CHECK PAYEE ACCOUNT
    // ============================================================

    boolean isPayeeAccountExists(
            String accountNumber) throws SQLException;


    // ============================================================
    // UPDATE BATCH STATUS
    // ============================================================

    void updateBatchStatus(
            String batchId,
            String status) throws SQLException;


    // ============================================================
    // GET REJECTED REASONS
    // ============================================================

    List<RejectedReason> getRejectedReasons()
            throws SQLException;


    // ============================================================
    // SAVE REJECTED CHEQUE
    // ============================================================

    void saveRejectedCheque(
            OutwardRejectedCheques rejectedCheque)
            throws SQLException;
    
    RejectRequestDTO getRejectRequestByChequeId(
            String chequeId) throws SQLException;
}