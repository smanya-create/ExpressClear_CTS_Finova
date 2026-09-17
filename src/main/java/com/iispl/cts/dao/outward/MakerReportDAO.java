package com.iispl.cts.dao.outward;

import java.sql.Date;
import java.util.List;
import java.util.Map;

public interface MakerReportDAO {
    List<Map<String, Object>> getMicrRepairsReport(String makerId, Date fromDate, Date toDate) throws Exception;
    List<Map<String, Object>> getDataEntryReport(String makerId, Date fromDate, Date toDate) throws Exception;
    List<Map<String, Object>> getRequestRejectedChequesReport(String makerId, Date fromDate, Date toDate) throws Exception;
    List<Map<String, Object>> getSubmittedToCheckerReport(String makerId, Date fromDate, Date toDate) throws Exception;
}