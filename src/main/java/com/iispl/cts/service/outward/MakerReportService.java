package com.iispl.cts.service.outward;

public interface MakerReportService {
    byte[] generateMakerReportHtml(String makerId, java.util.Date fromDate, java.util.Date toDate) throws Exception;
}