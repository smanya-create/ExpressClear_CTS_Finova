package com.iispl.cts.dao.inward;

import java.util.List;
import com.iispl.cts.dto.InwardDashboardBatchDTO;
import com.iispl.cts.dto.InwardDashboardKpiDTO;

public interface InwardDashboardDAO {
    InwardDashboardKpiDTO getKpiMetrics();
    List<InwardDashboardBatchDTO> getRecentBatches();
    String determineNextWorkspace(String batchId);
}