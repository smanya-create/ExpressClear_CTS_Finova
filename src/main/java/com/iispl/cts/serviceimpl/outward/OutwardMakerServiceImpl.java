package com.iispl.cts.serviceimpl.outward;

import java.sql.Connection;

import com.iispl.cts.common.config.DBConnection;
import com.iispl.cts.dao.outward.OutwardBatchDAO;
import com.iispl.cts.dao.outward.OutwardChequeDAO;
import com.iispl.cts.dao.outward.ScanBatchDAO;
import com.iispl.cts.daoimpl.outward.OutwardBatchDAOImpl;
import com.iispl.cts.daoimpl.outward.OutwardChequeDAOImpl;
import com.iispl.cts.daoimpl.outward.ScanBatchDAOImpl;
import com.iispl.cts.service.outward.OutwardMakerService;

public class OutwardMakerServiceImpl
        implements OutwardMakerService {

    private ScanBatchDAO scanBatchDAO;
    private OutwardBatchDAO outwardBatchDAO;
    private OutwardChequeDAO outwardChequeDAO;

    public OutwardMakerServiceImpl() {

        scanBatchDAO =
                new ScanBatchDAOImpl();

        outwardBatchDAO =
                new OutwardBatchDAOImpl();

        outwardChequeDAO =
                new OutwardChequeDAOImpl();
    }

    @Override
    public String getBatchFromScan(
            String scannedBatchId) {

        Connection connection = null;

        try {

            // =============================================
            // COMMON CONNECTION
            // =============================================

            connection =
                    DBConnection.getConnection();

            connection.setAutoCommit(false);

            // =============================================
            // 1. UPDATE SCAN BATCH STATUS
            // =============================================

            scanBatchDAO.updateBatchStatus(
                    connection,
                    scannedBatchId,
                    "VALIDATED");

            // =============================================
            // 2. TRANSFER BATCH
            // scan_batch → outward_batch
            // =============================================

            String outwardBatchId = outwardBatchDAO.transferBatchFromScanToOutward(
            		connection,
                    scannedBatchId);

            // =============================================
            // 3. TRANSFER CHEQUES
            // scan_cheque → outward_cheque
            // =============================================

            outwardChequeDAO.transferChequeFromScanToOutwrd(
                    connection,
                    scannedBatchId);

            // =============================================
            // EVERYTHING SUCCESSFUL
            // =============================================

            connection.commit();
            return outwardBatchId;

        } catch (Exception e) {

            // =============================================
            // ANY FAILURE → ROLLBACK EVERYTHING
            // =============================================

            if (connection != null) {

                try {
                    connection.rollback();
                } catch (Exception rollbackException) {
                    rollbackException.printStackTrace();
                }
            }

            throw new RuntimeException(
                    "Failed to process batch: "
                    + scannedBatchId,
                    e);

        } finally {

            if (connection != null) {

                try {
                    connection.setAutoCommit(true);
                } catch (Exception e) {
                    e.printStackTrace();
                }

                try {
                    connection.close();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }
}