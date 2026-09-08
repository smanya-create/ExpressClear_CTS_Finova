	package com.iispl.cts.dao.outward;
	
	import java.sql.Date;
	
	public interface OutwardChequeReportDAO {
	
		byte[] generateReport(Date fromDate, Date toDate) throws Exception;
	}