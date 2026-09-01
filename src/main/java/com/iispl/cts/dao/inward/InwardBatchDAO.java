package com.iispl.cts.dao.inward;

import java.util.List;
import com.iispl.cts.entity.inward.InwardBatch;

public interface InwardBatchDAO {
    List<InwardBatch> findAllActiveBatches();
    InwardBatch findById(String batchId);
    boolean updateStatus(String batchId, String status);
}