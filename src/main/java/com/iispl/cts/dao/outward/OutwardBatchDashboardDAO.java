package com.iispl.cts.dao.outward;

import java.util.Date;
import java.util.List;

import com.iispl.cts.daoimpl.outward.OutwardBatchDashboardDAOImpl;
import com.iispl.cts.entity.outward.OutwardBatch;

public interface OutwardBatchDashboardDAO {
	 List<OutwardBatch> searchPendingBatches(int pageNumber, int pageSize, String batchId, Date fromDate, Date toDate);
	 int getSearchPendingBatchCount(String batchId, Date fromDate, Date toDate);

}
