package com.iispl.cts.service;


import java.util.Date;
import java.util.List;
import com.iispl.cts.entity.AuditLog;

public interface AuditService {
    void log(String module, String action, String details, String status);
    List<AuditLog> searchAuditLogs(Date fromDate, Date toDate, String module, String action, String query,int offset, int limit);
 // Total count for the pagination bar
    int countAuditLogs(Date fromDate, Date toDate, String module, String action, String query);
}
