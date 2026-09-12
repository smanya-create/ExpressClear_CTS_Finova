package com.iispl.cts.serviceimpl.outward;

import java.util.Date;
import java.util.List;

import com.iispl.cts.dao.outward.OutwardBatchDashboardDAO;
import com.iispl.cts.daoimpl.outward.OutwardBatchDashboardDAOImpl;
import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.service.outward.OutwardCheckerBatchDashboardService;

public class OutwardCheckerBatchDashboardServiceImpl implements OutwardCheckerBatchDashboardService {
	private final OutwardBatchDashboardDAO outwardBatchDashboardDAO;
	public OutwardCheckerBatchDashboardServiceImpl() {
		outwardBatchDashboardDAO =  new  OutwardBatchDashboardDAOImpl();
	}

	@Override
	public List<OutwardBatch> searchPendingBatches(int pageNumber, int pageSize, String batchId, Date fromDate,
			Date toDate) {
		return outwardBatchDashboardDAO.searchPendingBatches(pageNumber, pageSize, batchId, fromDate, toDate);
	}

	@Override
	public int getSearchPendingBatchCount(String batchId, Date fromDate, Date toDate) {
		return outwardBatchDashboardDAO.getSearchPendingBatchCount(batchId, fromDate, toDate);
	}

}
