package com.iispl.cts.serviceimpl.outward;

import java.util.List;

import com.iispl.cts.dao.outward.OutwardBatchDAO;
import com.iispl.cts.dao.outward.OutwardChequeDAO;
import com.iispl.cts.dao.outward.ScanBatchDAO;
import com.iispl.cts.dao.outward.ScanChequeDAO;
import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.service.outward.OutwardMakerService;

public class OutwardMakerServiceImpl
        implements OutwardMakerService {
//
//    private final ScanBatchDAO scanBatchDAO;
//    private final ScanChequeDAO scanChequeDAO;
//
//    private final OutwardBatchDAO outwardBatchDAO;
//    private final OutwardChequeDAO outwardChequeDAO;


//    public OutwardMakerServiceImpl() {
//
//        scanBatchDAO = new ScanBatchDAO();
//        scanChequeDAO = new ScanChequeDAO();
//
//        outwardBatchDAO = new OutwardBatchDAO();
//        outwardChequeDAO = new OutwardChequeDAO();
//    }


    // =========================================================
    // SCAN MICR REPAIR
    // =========================================================

    @Override
    public List<ScanBatch> getScanMicrRepairBatches() {

        // We will add the actual DAO call here
        // after checking ScanBatchDAO.

        return null;
    }


    @Override
    public List<ScanCheque> getScanMicrRepairCheques(
            String scannedBatchId) {

        // We will add the actual DAO call here
        // after checking ScanChequeDAO.

        return null;
    }


    public void updateScanMicrRepair(
            ScanCheque cheque) {

        // We will add the actual DAO update here
        // after checking ScanChequeDAO.
    }


    // =========================================================
    // OUTWARD MAKER MICR REPAIR
    // =========================================================

    @Override
    public List<OutwardBatch> getMakerMicrRepairBatches() {

        // We will add the actual DAO call here
        // after checking OutwardBatchDAO.

        return null;
    }


    @Override
    public List<OutwardCheque> getMakerMicrRepairCheques(
            String outwardBatchId) {

        // We will add the actual DAO call here
        // after checking OutwardChequeDAO.

        return null;
    }


    
    public void updateMakerMicrRepair(
            OutwardCheque cheque) {

        // We will add the actual DAO update here
        // after checking OutwardChequeDAO.
    }


	@Override
	public void saveScanMicrRepair(ScanCheque cheque) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public void saveMakerMicrRepair(OutwardCheque cheque) {
		// TODO Auto-generated method stub
		
	}


	@Override
	public int getMakerMicrRepairChequeCount(String outwardBatchId) {
		// TODO Auto-generated method stub
		return 0;
	}


	@Override
	public int getScanMicrRepairChequeCount(String scannedBatchId) {
		// TODO Auto-generated method stub
		return 0;
	}
}