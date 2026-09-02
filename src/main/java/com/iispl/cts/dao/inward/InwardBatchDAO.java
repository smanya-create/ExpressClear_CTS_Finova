package com.iispl.cts.dao.inward;

import java.util.List;

import com.iispl.cts.entity.inward.InwardBatch;

public interface InwardBatchDAO {

    List<InwardBatch> getAllBatches();

    InwardBatch getBatchById(String inwardBatchId);

    boolean saveBatch(InwardBatch inwardBatch);

    boolean updateBatch(InwardBatch inwardBatch);

    boolean deleteBatch(String inwardBatchId);
    
    List<InwardBatch> findAllActiveBatches();
    InwardBatch findById(String batchId);
    boolean updateStatus(String batchId, String status);
}