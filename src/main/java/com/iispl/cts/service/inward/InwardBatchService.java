package com.iispl.cts.service.inward;

import java.util.List;
import com.iispl.cts.entity.inward.InwardBatch;

public interface InwardBatchService {
    List<InwardBatch> getAllActiveBatches();
    InwardBatch getBatchById(String batchId);
    boolean updateBatchStatus(String batchId, String status);
}