package com.iispl.cts.service.outward;

import java.util.Date;

public interface MakerReportService {
    byte[] generateMakerReportPdf(String makerId, String reportType, Date fromDate, Date toDate) throws Exception;
    byte[] generateMakerReportCsv(String makerId, String reportType, Date fromDate, Date toDate) throws Exception;
}