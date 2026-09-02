package com.iispl.cts.serviceimpl.outward;

import java.math.BigDecimal;


import java.util.List;

import com.iispl.cts.dao.outward.OutwardChequeDAO;
import com.iispl.cts.daoimpl.outward.OutwardChequeDAOImpl;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.service.outward.OutwardChequeService;

public class OutwardChequeServiceImpl implements OutwardChequeService {

	private final OutwardChequeDAO outwardChequeDAO;

	public OutwardChequeServiceImpl() {
		outwardChequeDAO = new OutwardChequeDAOImpl();
	}

	@Override
	public List<OutwardCheque> getChequesByBatchId(String outwardBatchId) {

		return outwardChequeDAO.getChequesByBatchId(outwardBatchId);
	}

	@Override
	public int getTotalChequeCountByBatchId(String outwardBatchId) {

		return outwardChequeDAO.getTotalChequeCountByBatchId(outwardBatchId);
	}

	@Override
	public BigDecimal getTotalChequeAmountByBatchId(String outwardBatchId) {

		return outwardChequeDAO.getTotalChequeAmountByBatchId(outwardBatchId);
	}
}

