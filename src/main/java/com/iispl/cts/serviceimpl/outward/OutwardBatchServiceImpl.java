package com.iispl.cts.serviceimpl.outward;
import java.util.List;

import com.iispl.cts.dao.outward.OutwardBatchDAO;
import com.iispl.cts.daoimpl.outward.OutwardBatchDAOImpl;
import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.service.outward.OutwardBatchService;


public class OutwardBatchServiceImpl implements OutwardBatchService{

	private final OutwardBatchDAO outwardBatchDAO;

	public OutwardBatchServiceImpl() {
		outwardBatchDAO = new OutwardBatchDAOImpl();
	}
  
	@Override
	public List<OutwardBatch> getVerifiedBatches() {
		return outwardBatchDAO.getVerifiedBatches();
	}

	@Override
	public List<OutwardBatch> getRecentBatches() {
		return outwardBatchDAO.getRecentBatches();
	}

	@Override
	public List<OutwardBatch> searchBatches(String batchId, String status) {

		return outwardBatchDAO.searchBatches(batchId, status);
	}

	@Override
	public OutwardBatch getBatchById(String outwardBatchId) {

		return outwardBatchDAO.getBatchById(outwardBatchId);
	}
}

