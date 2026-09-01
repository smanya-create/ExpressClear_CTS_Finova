package com.iispl.cts.serviceimpl.outward;

import java.util.List;

import com.iispl.cts.dao.outward.OutwardChequeDAO;
import com.iispl.cts.daoimpl.outward.OutwardChequeDAOImpl;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.service.outward.OutwardChequeService;

public class OutwardChequeServiceImpl implements OutwardChequeService{

	private OutwardChequeDAO outwardChequeDAO = new OutwardChequeDAOImpl();
	@Override
	public List<OutwardCheque> getChequesByBatchId(String outwardBatchId) {
		
		return outwardChequeDAO.getChequesByBatchId(outwardBatchId);
	}
	
}
