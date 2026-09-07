package com.iispl.cts.serviceimpl.outward;

import java.sql.Date;
import java.util.List;

import com.iispl.cts.dao.outward.OutwardCheckerRejectionDAO;
import com.iispl.cts.daoimpl.outward.OutwardCheckerRejectionDAOImpl;
import com.iispl.cts.entity.outward.OutwardRejectedCheques;
import com.iispl.cts.service.outward.OutwardCheckerRejectionService;

public class OutwardCheckerRejectionServiceImpl
        implements OutwardCheckerRejectionService {

    private final OutwardCheckerRejectionDAO rejectionDAO;

    public OutwardCheckerRejectionServiceImpl() {
        this.rejectionDAO =
                new OutwardCheckerRejectionDAOImpl();
    }

    @Override
    public List<OutwardRejectedCheques> getRejectedCheques(
            int limit,
            int offset) throws Exception {

        return rejectionDAO.getRejectedCheques(
                limit,
                offset);
    }

    @Override
    public List<OutwardRejectedCheques> searchRejectedCheques(
            String searchValue,
            Date rejectedDate,
            int limit,
            int offset) throws Exception {

        return rejectionDAO.searchRejectedCheques(
                searchValue,
                rejectedDate,
                limit,
                offset);
    }

    @Override
    public int getTotalRejectedCheques()
            throws Exception {

        return rejectionDAO.getTotalRejectedCheques();
    }

    @Override
    public int getTotalRejectedCheques(
            String searchValue,
            Date rejectedDate)
            throws Exception {

        return rejectionDAO.getTotalRejectedCheques(
                searchValue,
                rejectedDate);
    }

    @Override
    public boolean saveRejectedCheque(
            OutwardRejectedCheques rejectedCheque)
            throws Exception {

        return rejectionDAO.saveRejectedCheque(
                rejectedCheque);
    }
}