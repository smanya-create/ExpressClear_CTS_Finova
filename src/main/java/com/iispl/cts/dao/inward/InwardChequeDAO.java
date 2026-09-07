package com.iispl.cts.dao.inward;

import java.util.List;

import com.iispl.cts.entity.inward.InwardCheque;

public interface InwardChequeDAO {

	List<InwardCheque> getMicrRepairRequiredCheques();

	InwardCheque findById(String inwardChequeId);

	List<InwardCheque> findByBatchAndStatus(String batchId, String status);

	List<InwardCheque> getAllCheques();

	InwardCheque getChequeById(String inwardChequeId);

	List<InwardCheque> getChequesByBatchId(String inwardBatchId);

	boolean saveCheque(InwardCheque inwardCheque);

	boolean updateCheque(InwardCheque inwardCheque);

	boolean updateMicrRepair(String inwardChequeId, String correctedMicrCode, String chequeStatus);

	boolean deleteCheque(String inwardChequeId);
}