package com.iispl.cts.dao.outward;

import java.sql.Date;

/*
 * @author Anandhu Jayakumar
 */

public interface OutwardChequeReportDAO {
	byte[] generateReport(Date fromDate, Date toDate, String reportType) throws Exception;
}