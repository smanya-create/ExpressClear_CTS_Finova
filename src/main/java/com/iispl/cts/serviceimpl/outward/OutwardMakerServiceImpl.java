package com.iispl.cts.serviceimpl.outward;

import java.util.List;

import com.iispl.cts.dao.outward.OutwardBatchDAO;
import com.iispl.cts.dao.outward.OutwardChequeDAO;
import com.iispl.cts.dao.outward.ScanBatchDAO;
import com.iispl.cts.dao.outward.ScanChequeDAO;
import com.iispl.cts.daoimpl.outward.OutwardBatchDAOImpl;
import com.iispl.cts.daoimpl.outward.OutwardChequeDAOImpl;
import com.iispl.cts.daoimpl.outward.ScanBatchDAOImpl;
import com.iispl.cts.daoimpl.outward.ScanChequeDAOImpl;
import com.iispl.cts.dto.MicrRepairBatch;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.service.outward.OutwardMakerService;

public class OutwardMakerServiceImpl
        implements OutwardMakerService {

    // =========================================================
    // DAOs
    // =========================================================

    private final ScanBatchDAO scanBatchDAO;
    private final ScanChequeDAO scanChequeDAO;

    private final OutwardBatchDAO outwardBatchDAO;
    private final OutwardChequeDAO outwardChequeDAO;


    // =========================================================
    // CONSTRUCTOR
    // =========================================================

    public OutwardMakerServiceImpl() {

        scanBatchDAO = new ScanBatchDAOImpl();
        scanChequeDAO = new ScanChequeDAOImpl();

        outwardBatchDAO = new OutwardBatchDAOImpl();
        outwardChequeDAO = new OutwardChequeDAOImpl();
    }


    // =========================================================
    // SCAN MICR REPAIR
    // =========================================================

    @Override
    public List<MicrRepairBatch> getScanMicrRepairBatches() {

        return scanBatchDAO.getScanMicrRepairBatches();
    }


    @Override
    public List<ScanCheque> getScanMicrRepairCheques(
            String scannedBatchId) {

        return scanChequeDAO.getScanMicrRepairCheques(
                scannedBatchId);
    }


    @Override
    public void saveScanMicrRepair(
            ScanCheque cheque) {

        scanChequeDAO.saveScanMicrRepair(cheque);
    }


    // =========================================================
    // OUTWARD MICR REPAIR
    // =========================================================

    @Override
    public List<MicrRepairBatch> getOutwardMicrRepairBatches() {

        return outwardBatchDAO.getOutwardMicrRepairBatches();
    }


    @Override
    public List<OutwardCheque> getOutwardMicrRepairCheques(
            String outwardBatchId) {

        return outwardChequeDAO.getOutwardMicrRepairCheques(
                outwardBatchId);
    }


    @Override
    public void saveOutwardMicrRepair(
            OutwardCheque cheque) {

        outwardChequeDAO.saveOutwardMicrRepair(cheque);
    }
}