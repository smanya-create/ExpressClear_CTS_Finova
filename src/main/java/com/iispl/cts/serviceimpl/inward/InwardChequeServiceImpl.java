package com.iispl.cts.serviceimpl.inward;

import java.util.List;
import com.iispl.cts.dao.inward.InwardChequeDAO;
import com.iispl.cts.daoimpl.inward.InwardChequeDAOImpl;
import com.iispl.cts.daoimpl.inward.InwardChequeImageDAOImpl;
import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.service.inward.InwardChequeService;
import com.iispl.cts.entity.inward.InwardChequeImage;


public class InwardChequeServiceImpl implements InwardChequeService {

	private final InwardChequeDAO inwardChequeDAO;

	public InwardChequeServiceImpl() {
		this.inwardChequeDAO = InwardChequeDAOImpl.getInstance();
	}

	@Override
	public List<InwardCheque> getMicrRepairRequiredCheques() {
		return inwardChequeDAO.getMicrRepairRequiredCheques();
	}

	@Override
	public InwardCheque findById(String inwardChequeId) {
		return inwardChequeDAO.findById(inwardChequeId);
	}

	@Override
	public boolean updateMicrRepair(String inwardChequeId, String correctedMicrCode, String chequeStatus) {

		return inwardChequeDAO.updateMicrRepair(inwardChequeId, correctedMicrCode, chequeStatus);
	}

	@Override
	public List<InwardCheque> getChequesByBatchAndStatus(String batchId, String status) {
		return inwardChequeDAO.findByBatchAndStatus(batchId, status);
	}

	@Override
	public boolean updateChequeDetails(InwardCheque cheque) {
		return inwardChequeDAO.updateCheque(cheque);
	}

	@Override
	public InwardChequeImage getFrontImage(String inwardChequeId) {
		return InwardChequeImageDAOImpl.getInstance().findFrontImageByChequeId(inwardChequeId);
	}

	
}