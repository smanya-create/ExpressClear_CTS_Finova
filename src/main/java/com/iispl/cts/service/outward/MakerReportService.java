package com.iispl.cts.service.outward;

import java.util.Date;

public interface MakerReportService {
	byte[] generateMakerReportPdf(String makerId, Date fromDate, Date toDate) throws Exception;

    byte[] generateMakerReportCsv(String makerId, Date fromDate, Date toDate) throws Exception;

}