package com.iispl.cts.service.outward;

import java.util.List;

import com.iispl.cts.dto.MicrRepairBatch;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.ScanCheque;

public interface OutwardMakerService {

    // =========================================================
    // Scan MICR Repair
    // =========================================================

    List<MicrRepairBatch> getScanMicrRepairBatches();

    List<ScanCheque> getScanMicrRepairCheques(
            String scannedBatchId);

    void saveScanMicrRepair(
            ScanCheque cheque);


    // =========================================================
    // Outward MICR Repair
    // =========================================================

    List<MicrRepairBatch> getOutwardMicrRepairBatches();

    List<OutwardCheque> getOutwardMicrRepairCheques(
            String outwardBatchId);

    void saveOutwardMicrRepair(
            OutwardCheque cheque);
}