package com.iispl.cts.service.inward;

import java.util.List;
import com.iispl.cts.dto.InwardDashboardBatchDTO;
import com.iispl.cts.dto.InwardDashboardKpiDTO;

public interface InwardDashboardService {
    InwardDashboardKpiDTO getDashboardSummary();
    List<InwardDashboardBatchDTO> getRecentBatches(String filterBatchId);
    String resolveWorkspaceTarget(String batchId);
}