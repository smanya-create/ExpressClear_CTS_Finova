package com.iispl.cts.service.outward;

import java.util.Date;
import java.util.List;

import com.iispl.cts.entity.outward.OutwardBatch;

public interface OutwardCheckerBatchDashboardService {
	List<OutwardBatch> searchPendingBatches(int pageNumber, int pageSize, String batchId);

	int getSearchPendingBatchCount(String batchId);

}
