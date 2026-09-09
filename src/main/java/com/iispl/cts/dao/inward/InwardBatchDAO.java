package com.iispl.cts.dao.inward;

import java.util.List;

import com.iispl.cts.dto.DashboardSummaryDTO;
import com.iispl.cts.dto.InwardReportChequeDTO;
import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.enums.inward.InwardBatchStatus;

public interface InwardBatchDAO {

    List<InwardBatch> getAllBatches();
    
    List<InwardBatch> getBatchesForMicrRepair();

    InwardBatch getBatchById(String inwardBatchId);

    boolean saveBatch(InwardBatch inwardBatch);

    boolean updateBatch(InwardBatch inwardBatch);

    boolean deleteBatch(String inwardBatchId);
    
    
//    List<InwardBatch> findAllActiveBatches();
//    InwardBatch findById(String batchId);
    boolean updateStatus(String batchId, String status);
    
    List<DashboardSummaryDTO> getDashboardBatches();
    List<InwardReportChequeDTO> getChequesByBatch();
    

	boolean updateProcessingBatchStatus(String batchId, InwardBatchStatus status);
}