package com.iispl.cts.dao.outward;

import java.util.Date;
import java.util.List;

import com.iispl.cts.daoimpl.outward.OutwardBatchDashboardDAOImpl;
import com.iispl.cts.entity.outward.OutwardBatch;

public interface OutwardBatchDashboardDAO {
	List<OutwardBatch> searchPendingBatches(int pageNumber, int pageSize, String batchId, String status);
	int getSearchPendingBatchCount(String batchId, String status);

}
