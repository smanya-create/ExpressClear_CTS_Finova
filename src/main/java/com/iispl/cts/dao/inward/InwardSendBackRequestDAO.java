package com.iispl.cts.dao.inward;

import java.util.Map;
import com.iispl.cts.dto.InwardSendBackRequestDTO;

public interface InwardSendBackRequestDAO {
    Map<String, InwardSendBackRequestDTO> getPendingRequestsByBatchId(String batchId);
    InwardSendBackRequestDTO getLatestPendingByChequeId(String inwardChequeId);
    boolean markRequestResolved(String inwardChequeId, String resolvedBy);
    
    // Added for Data entry controller requirement
    boolean hasPendingSendBackRequests(String batchId);
    boolean isChequeInCurrentRework(String inwardChequeId, String inwardBatchId);
    boolean resolveAllPendingByBatchId(String batchId, String resolvedBy);
    
    
}