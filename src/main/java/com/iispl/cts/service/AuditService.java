package com.iispl.cts.service;

import java.util.Date;
import java.util.List;
import com.iispl.cts.dto.AuditSearchResult;
import com.iispl.cts.entity.AuditLog;

public interface AuditService {
	void log(String username, String module, String action, String details, String status);
    // Non-blocking fire-and-forget logging
    void log(String module, String action, String details, String status);
    void log(String userId, String username, String roleName, String module, String action, String details, String status);

    // Standard sequential search
    List<AuditLog> searchAuditLogs(Date fromDate, Date toDate, String module, String action, String query, int offset, int limit);
    int countAuditLogs(Date fromDate, Date toDate, String module, String action, String query);

    // High-performance concurrent search running data fetch & count in parallel worker threads
    AuditSearchResult searchAuditLogsConcurrently(Date fromDate, Date toDate, String module, String action, String query, int offset, int limit);
}