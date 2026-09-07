package com.iispl.cts.serviceimpl.inward;

import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.dao.inward.InwardDashboardDAO;
import com.iispl.cts.daoimpl.inward.InwardDashboardDAOImpl;
import com.iispl.cts.dto.InwardDashboardBatchDTO;
import com.iispl.cts.dto.InwardDashboardKpiDTO;
import com.iispl.cts.service.inward.InwardDashboardService;

public class InwardDashboardServiceImpl implements InwardDashboardService {

    private final InwardDashboardDAO dashboardDAO;

    public InwardDashboardServiceImpl() {
        this.dashboardDAO = new InwardDashboardDAOImpl();
    }

    @Override
    public InwardDashboardKpiDTO getDashboardSummary() {
        return dashboardDAO.getKpiMetrics();
    }

    @Override
    public List<InwardDashboardBatchDTO> getRecentBatches(String filterBatchId) {
        List<InwardDashboardBatchDTO> list = dashboardDAO.getRecentBatches();
        if (filterBatchId == null || filterBatchId.trim().isEmpty()) {
            return list;
        }

        String searchLower = filterBatchId.trim().toLowerCase();
        List<InwardDashboardBatchDTO> filtered = new ArrayList<>();
        for (InwardDashboardBatchDTO item : list) {
            if (item.getBatchId() != null && item.getBatchId().toLowerCase().contains(searchLower)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    @Override
    public String resolveWorkspaceTarget(String batchId) {
        return dashboardDAO.determineNextWorkspace(batchId);
    }
}
