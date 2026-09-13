package com.iispl.cts.serviceimpl.inward;

import java.util.Collections;
import java.util.Map;

import com.iispl.cts.dao.inward.InwardSendBackRequestDAO;
import com.iispl.cts.daoimpl.inward.InwardSendBackRequestDAOImpl;
import com.iispl.cts.dto.InwardSendBackRequestDTO;
import com.iispl.cts.service.inward.InwardSendBackRequestService;

public class InwardSendBackRequestServiceImpl implements InwardSendBackRequestService {

    private final InwardSendBackRequestDAO sendBackRequestDao;

    public InwardSendBackRequestServiceImpl() {
        this.sendBackRequestDao = new InwardSendBackRequestDAOImpl();
    }

    @Override
    public Map<String, InwardSendBackRequestDTO> getPendingRequestsByBatchId(String batchId) {
        if (batchId == null || batchId.trim().isEmpty()) {
            return Collections.emptyMap();
        }
        return sendBackRequestDao.getPendingRequestsByBatchId(batchId.trim());
    }

    @Override
    public InwardSendBackRequestDTO getLatestPendingByChequeId(String inwardChequeId) {
        if (inwardChequeId == null || inwardChequeId.trim().isEmpty()) {
            return null;
        }
        return sendBackRequestDao.getLatestPendingByChequeId(inwardChequeId.trim());
    }

    @Override
    public boolean markRequestResolved(String inwardChequeId, String resolvedBy) {
        if (inwardChequeId == null || inwardChequeId.trim().isEmpty()) {
            return false;
        }
        return sendBackRequestDao.markRequestResolved(inwardChequeId.trim(), resolvedBy);
    }
}