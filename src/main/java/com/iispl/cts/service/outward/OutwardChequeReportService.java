package com.iispl.cts.service.outward;

import java.sql.Date;

public interface OutwardChequeReportService {

   
    byte[] generateReport(Date fromDate, Date toDate) throws Exception;
}