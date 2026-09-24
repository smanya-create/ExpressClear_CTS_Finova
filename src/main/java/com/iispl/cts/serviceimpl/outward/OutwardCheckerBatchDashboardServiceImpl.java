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
	public List<OutwardBatch> searchPendingBatches(int pageNumber, int pageSize, String batchId, String status) {
		return outwardBatchDashboardDAO.searchPendingBatches(pageNumber, pageSize, batchId,status);
	}

	@Override
	public int getSearchPendingBatchCount(String batchId, String status){
		return outwardBatchDashboardDAO.getSearchPendingBatchCount(batchId,status);
	}

}
