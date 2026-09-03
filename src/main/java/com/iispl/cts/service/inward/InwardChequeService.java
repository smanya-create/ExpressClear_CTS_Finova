package com.iispl.cts.service.inward;

import java.util.List;
import com.iispl.cts.entity.inward.InwardCheque;

public interface InwardChequeService {
    List<InwardCheque> getChequesByBatchAndStatus(String batchId, String status);
    boolean updateChequeDetails(InwardCheque cheque);
    InwardCheque getChequeById(String inwardChequeId);
}
