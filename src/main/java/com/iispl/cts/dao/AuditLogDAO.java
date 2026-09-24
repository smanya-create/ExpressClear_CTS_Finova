package com.iispl.cts.dao;

import java.util.Date;
import java.util.List;
import com.iispl.cts.entity.AuditLog;

public interface AuditLogDAO {
    boolean insertAuditLog(AuditLog auditLog);
    List<AuditLog> searchAuditLogs(Date fromDate, Date toDate, String module, String action, String query, int offset, int limit);
    int countAuditLogs(Date fromDate, Date toDate, String module, String action, String query);
}
