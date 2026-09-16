package com.iispl.cts.serviceimpl.outward;

import java.util.List;

import com.iispl.cts.dao.outward.OutwardMakerDAO;
import com.iispl.cts.daoimpl.outward.OutwardMakerDAOImpl;

import com.iispl.cts.dto.MicrRepairBatch;
import com.iispl.cts.dto.MicrRepairChequeDTO;

import com.iispl.cts.entity.outward.RejectedReason;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.service.outward.OutwardMakerService;

public class OutwardMakerServiceImpl implements OutwardMakerService {

	// =========================================================
	// DAO
	// =========================================================

	private final OutwardMakerDAO outwardMakerDAO;

	// =========================================================
	// CONSTRUCTOR
	// =========================================================

	public OutwardMakerServiceImpl() {

		outwardMakerDAO = new OutwardMakerDAOImpl();
	}

	// =========================================================
	// SCAN MICR REPAIR BATCHES
	// =========================================================

	@Override
	public List<MicrRepairBatch> getScanMicrRepairBatches() {

		System.out.println("========== SCAN MICR REPAIR SERVICE CALLED ==========");

		List<MicrRepairBatch> batches = outwardMakerDAO.getScanMicrRepairBatches();

		System.out.println("========== SCAN MICR REPAIR DAO RETURNED ==========");

		System.out.println("Batch count = " + (batches == null ? "NULL" : batches.size()));

		return batches;
	}

	// =========================================================
	// SCAN MICR REPAIR CHEQUES
	// =========================================================

	@Override
	public List<MicrRepairChequeDTO> getScanMicrRepairCheques(String scannedBatchId) {

		return outwardMakerDAO.getScanMicrRepairCheques(scannedBatchId);
	}

	// =========================================================
	// SAVE SCAN MICR REPAIR
	// =========================================================

	@Override
	public void saveScanMicrRepair(MicrRepairChequeDTO cheque) {

		if (cheque == null) {

			throw new IllegalArgumentException("Scan MICR repair cheque cannot be null");
		}

		outwardMakerDAO.saveScanMicrRepair(cheque);
	}

	// =========================================================
	// SUBMIT SCAN MICR REPAIR
	// =========================================================

	@Override
	public void submitScanMicrRepair(List<MicrRepairChequeDTO> cheques) {

		if (cheques == null || cheques.isEmpty()) {

			throw new IllegalArgumentException("Scan MICR repair cheque list " + "cannot be null or empty");
		}

		outwardMakerDAO.submitScanMicrRepair(cheques);
	}

	// =========================================================
	// OUTWARD MICR REPAIR BATCHES
	// =========================================================

	@Override
	public List<MicrRepairBatch> getOutwardMicrRepairBatches() {

		return outwardMakerDAO.getOutwardMicrRepairBatches();
	}

	// =========================================================
	// OUTWARD MICR REPAIR CHEQUES
	// =========================================================

	@Override
	public List<MicrRepairChequeDTO> getOutwardMicrRepairCheques(String outwardBatchId) {

		return outwardMakerDAO.getOutwardMicrRepairCheques(outwardBatchId);
	}

	// =========================================================
	// SAVE OUTWARD MICR REPAIR
	// =========================================================

	@Override
	public void saveOutwardMicrRepair(MicrRepairChequeDTO cheque) {

		if (cheque == null) {

			throw new IllegalArgumentException("Outward MICR repair cheque cannot be null");
		}

		outwardMakerDAO.saveOutwardMicrRepair(cheque);
	}

	// =========================================================
	// SUBMIT OUTWARD MICR REPAIR
	// =========================================================

	@Override
	public void submitOutwardMicrRepair(List<MicrRepairChequeDTO> cheques) {

		if (cheques == null || cheques.isEmpty()) {

			throw new IllegalArgumentException("Outward MICR repair cheque list " + "cannot be null or empty");
		}

		outwardMakerDAO.submitOutwardMicrRepair(cheques);
	}

	// =========================================================
	// GET REJECTED REASONS
	// =========================================================

	@Override
	public List<RejectedReason> getRejectedReasons() {

		return outwardMakerDAO.getRejectedReasons();
	}

	@Override
	public boolean existsChequeNumberAndAccount(String chequeNumber, String accountNumber) {

		return outwardMakerDAO.existsChequeNumberAndAccount(chequeNumber, accountNumber);
	}

	@Override
	public ScanBatch getMakerBatch(String batchId) {
		return outwardMakerDAO.getMakerBatch(batchId);
	}

	@Override
	public List<ScanCheque> getMakerBatchCheques(String batchId) {
		return outwardMakerDAO.getMakerBatchCheques(batchId);
	}
}