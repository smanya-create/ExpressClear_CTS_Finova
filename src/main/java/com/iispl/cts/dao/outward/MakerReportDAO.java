package com.iispl.cts.dao.outward;

import java.sql.Date;
import java.util.List;
import java.util.Map;

public interface MakerReportDAO {
	Map<String, List<Map<String, Object>>> getMakerReportData(String makerId, Date fromDate, Date toDate) throws Exception;

}
