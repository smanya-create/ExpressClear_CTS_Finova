package com.iispl.cts.dao.inward;

import java.util.Map;
import com.iispl.cts.dto.InwardSendBackRequestDTO;

public interface InwardSendBackRequestDAO {
    Map<String, InwardSendBackRequestDTO> getPendingRequestsByBatchId(String batchId);
    InwardSendBackRequestDTO getLatestPendingByChequeId(String inwardChequeId);
    boolean markRequestResolved(String inwardChequeId, String resolvedBy);
}