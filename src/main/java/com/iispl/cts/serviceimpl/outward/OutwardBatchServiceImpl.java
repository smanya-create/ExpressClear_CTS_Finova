package com.iispl.cts.serviceimpl.outward;
import java.util.List;

import com.iispl.cts.dao.outward.OutwardBatchDAO;
import com.iispl.cts.daoimpl.outward.OutwardBatchDAOImpl;
import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.service.outward.OutwardBatchService;

public class OutwardBatchServiceImpl implements OutwardBatchService{
	private OutwardBatchDAO outwardBatchDAO = new OutwardBatchDAOImpl();

	@Override
	public List<OutwardBatch> getVerifiedBatches() {
		return outwardBatchDAO.getVerifiedBatches();
	}
	
}
