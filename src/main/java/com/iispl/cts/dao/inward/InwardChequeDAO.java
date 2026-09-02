package com.iispl.cts.dao.inward;

import java.util.List;

import com.iispl.cts.entity.inward.InwardCheque;

public interface InwardChequeDAO {

    List<InwardCheque> getAllCheques();

    InwardCheque getChequeById(String inwardChequeId);

    List<InwardCheque> getChequesByBatchId(String inwardBatchId);

    boolean saveCheque(InwardCheque inwardCheque);

    boolean updateCheque(InwardCheque inwardCheque);

    boolean deleteCheque(String inwardChequeId);
}