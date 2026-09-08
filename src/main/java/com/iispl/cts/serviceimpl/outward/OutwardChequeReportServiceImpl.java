package com.iispl.cts.serviceimpl.outward;

import java.sql.Date;

import com.iispl.cts.dao.outward.OutwardChequeReportDAO;
import com.iispl.cts.daoimpl.outward.OutwardChequeReportDAOImpl;
import com.iispl.cts.service.outward.OutwardChequeReportService;

public class OutwardChequeReportServiceImpl
        implements OutwardChequeReportService {

    private final OutwardChequeReportDAO reportDAO;

    public OutwardChequeReportServiceImpl() {

        reportDAO = new OutwardChequeReportDAOImpl();
    }

    @Override
    public byte[] generateReport(
            Date fromDate,
            Date toDate) throws Exception {

        return reportDAO.generateReport(
                fromDate,
                toDate
        );
    }
}