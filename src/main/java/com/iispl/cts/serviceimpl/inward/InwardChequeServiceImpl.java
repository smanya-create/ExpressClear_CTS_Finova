package com.iispl.cts.serviceimpl.inward;

import java.util.List;
import com.iispl.cts.dao.inward.InwardChequeDAO;
import com.iispl.cts.daoimpl.inward.InwardChequeDAOImpl;
import com.iispl.cts.daoimpl.inward.InwardChequeImageDAOImpl;
import com.iispl.cts.entity.inward.CbsValidationResult;
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
	public boolean updateMicrRepair(String inwardChequeId, String inwardBatchId, String originalMicr,
			String correctedMicrCode, String chequeStatus, String repairedBy, String remarks) {

		return inwardChequeDAO.updateMicrRepair(inwardChequeId, inwardBatchId, originalMicr, correctedMicrCode,
				chequeStatus, repairedBy, remarks);
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

	@Override
	public InwardChequeImage getBackImage(String inwardChequeId) {
		return InwardChequeImageDAOImpl.getInstance().findBackImageByChequeId(inwardChequeId);
	}

	@Override
	public boolean saveRejection(String inwardChequeId, String rejectedReasonId, String remarks, String rejectedBy) {

		return inwardChequeDAO.saveRejection(inwardChequeId, rejectedReasonId, remarks, rejectedBy);
	}

	@Override
	public CbsValidationResult validateCbs(InwardCheque cheque) {
		return inwardChequeDAO.validateCbs(cheque);
	}

}