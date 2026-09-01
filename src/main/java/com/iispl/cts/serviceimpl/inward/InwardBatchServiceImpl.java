package com.iispl.cts.serviceimpl.inward;

import java.util.List;
import com.iispl.cts.dao.inward.InwardBatchDAO;
import com.iispl.cts.daoimpl.inward.InwardBatchDAOImpl;
import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.service.inward.InwardBatchService;

public class InwardBatchServiceImpl implements InwardBatchService {

    private final InwardBatchDAO batchDao = new InwardBatchDAOImpl();

    @Override
    public List<InwardBatch> getAllActiveBatches() {
        return batchDao.findAllActiveBatches();
    }

    @Override
    public InwardBatch getBatchById(String batchId) {
        return batchDao.findById(batchId);
    }

    @Override
    public boolean updateBatchStatus(String batchId, String status) {
        return batchDao.updateStatus(batchId, status);
    }
}