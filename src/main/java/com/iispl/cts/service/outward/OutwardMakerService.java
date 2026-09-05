package com.iispl.cts.service.outward;

import java.util.List;

import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;

public interface OutwardMakerService {

    // Scan MICR Repair
    List<ScanBatch> getScanMicrRepairBatches();

    List<ScanCheque> getScanMicrRepairCheques(String scannedBatchId);

    void saveScanMicrRepair(ScanCheque cheque);


    // Outward Maker MICR Repair
    List<OutwardBatch> getMakerMicrRepairBatches();

    List<OutwardCheque> getMakerMicrRepairCheques(String outwardBatchId);

    void saveMakerMicrRepair(OutwardCheque cheque);

	int getMakerMicrRepairChequeCount(String outwardBatchId);

	int getScanMicrRepairChequeCount(String scannedBatchId);
}