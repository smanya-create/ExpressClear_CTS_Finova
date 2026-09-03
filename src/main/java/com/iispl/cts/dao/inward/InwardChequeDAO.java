package com.iispl.cts.dao.inward;

import java.util.List;
import com.iispl.cts.entity.inward.InwardCheque;

public interface InwardChequeDAO {
    List<InwardCheque> findByBatchAndStatus(String batchId, String status);
    boolean updateCheque(InwardCheque cheque);
    
    InwardCheque findById(String inwardChequeId);
    
}