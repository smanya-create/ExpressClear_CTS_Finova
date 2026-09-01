package com.iispl.cts.service.inward;

import java.util.List;

import com.iispl.cts.entity.inward.InwardCheque;

public interface InwardChequeService {

    List<InwardCheque> getMicrRepairRequiredCheques();

    InwardCheque findById(String inwardChequeId);

    boolean updateMicrRepair(String inwardChequeId,String correctedMicrCode,String chequeStatus);

    List<InwardCheque> getChequesByBatchAndStatus(String batchId, String status);
  
    boolean updateChequeDetails(InwardCheque cheque);
}
