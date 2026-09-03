package com.iispl.cts.serviceimpl.inward;

import java.util.List;
import com.iispl.cts.dao.inward.InwardChequeDAO;
import com.iispl.cts.daoimpl.inward.InwardChequeDAOImpl;
import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.service.inward.InwardChequeService;

public class InwardChequeServiceImpl implements InwardChequeService {

    private final InwardChequeDAO chequeDao = new InwardChequeDAOImpl();

    @Override
    public List<InwardCheque> getChequesByBatchAndStatus(String batchId, String status) {
        return chequeDao.findByBatchAndStatus(batchId, status);
    }

    @Override
    public boolean updateChequeDetails(InwardCheque cheque) {
        return chequeDao.updateCheque(cheque);
    }
    @Override
    public InwardCheque getChequeById(String inwardChequeId) {

        return chequeDao.findById(inwardChequeId);
    }
}