package com.iispl.cts.dto;
import java.util.List;
import com.iispl.cts.entity.AuditLog;

public class AuditSearchResult {
	private final List<AuditLog> logs;
    private final int totalCount;

    public AuditSearchResult(List<AuditLog> logs, int totalCount) {
        this.logs = logs;
        this.totalCount = totalCount;
    }

    public List<AuditLog> getLogs() { return logs; }
    public int getTotalCount() { return totalCount; }

}
