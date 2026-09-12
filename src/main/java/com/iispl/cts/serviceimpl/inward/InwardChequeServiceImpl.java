package com.iispl.cts.serviceimpl.inward;

import java.math.BigDecimal;
import java.util.List;
import com.iispl.cts.dao.inward.InwardChequeDAO;
import com.iispl.cts.daoimpl.inward.InwardChequeDAOImpl;
import com.iispl.cts.daoimpl.inward.InwardChequeImageDAOImpl;
import com.iispl.cts.entity.inward.CbsValidationResult;
import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.service.inward.InwardChequeService;
import com.iispl.cts.entity.inward.InwardChequeImage;
import com.iispl.cts.entity.inward.InwardChequeRejectionRequest;

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
	public boolean saveRejectionRequest(InwardChequeRejectionRequest request) {

		return inwardChequeDAO.saveRejectionRequest(request);
	}

	@Override
	public boolean updateChequeStatus(String inwardChequeId, String chequeStatus) {

		return inwardChequeDAO.updateChequeStatus(inwardChequeId, chequeStatus);
	}

	@Override
	public CbsValidationResult validateCbs(InwardCheque cheque) {
		return inwardChequeDAO.validateCbs(cheque);
	}

	@Override
	public BigDecimal getAccountBalance(String accountNumber) {
		return inwardChequeDAO.getAccountBalance(accountNumber);
	}

	@Override
	public String getBankNameByCode(String bankCode) {
		return inwardChequeDAO.getBankNameByCode(bankCode);
	}

	@Override
	public String getRejectedReasonDetails(String inwardChequeId) {
		return inwardChequeDAO.getRejectedReasonDetails(inwardChequeId);
	}

	@Override
	public String getMakerRejectionRequestDetails(String inwardChequeId) {
		return inwardChequeDAO.getMakerRejectionRequestDetails(inwardChequeId);
	}

	@Override
	public boolean saveSendBackRequest(String inwardChequeId, String inwardBatchId, String reasonId, String remarks,
			String sentBackBy) {

		return inwardChequeDAO.saveSendBackRequest(inwardChequeId, inwardBatchId, reasonId, remarks, sentBackBy);
	}

}