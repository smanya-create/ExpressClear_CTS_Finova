package com.iispl.cts.service.outward;

import com.iispl.cts.entity.outward.ScanBatch;

public interface OutwardMakerService {

    String getBatchFromScan(
            String scannedBatchId);

}