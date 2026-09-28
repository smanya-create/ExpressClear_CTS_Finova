package com.iispl.cts.serviceimpl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;

import com.iispl.cts.dao.AuditLogDAO;
import com.iispl.cts.daoimpl.AuditLogDAOImpl;
import com.iispl.cts.dto.AuditSearchResult;
import com.iispl.cts.entity.AuditLog;
import com.iispl.cts.service.AuditService;

public class AuditServiceImpl implements AuditService {

    private static final Logger auditLogger = LogManager.getLogger("CTS_AUDIT_LOGGER");
    private static AuditServiceImpl instance;
    private final AuditLogDAO auditDAO;

    // Worker pool for background audit logging and parallel search queries
    private final ExecutorService auditExecutor = new ThreadPoolExecutor(
            4, 8, 60L, TimeUnit.SECONDS,
            new LinkedBlockingQueue<>(1000),
            new ThreadFactory() {
                private final AtomicInteger count = new AtomicInteger(1);
                @Override
                public Thread newThread(Runnable r) {
                    Thread t = new Thread(r, "CTS-AuditPool-Worker-" + count.getAndIncrement());
                    t.setDaemon(true);
                    return t;
                }
            },
            new ThreadPoolExecutor.CallerRunsPolicy()
    );

    private AuditServiceImpl() {
        this.auditDAO = AuditLogDAOImpl.getInstance();
    }

    // Singleton instance access
    public static synchronized AuditServiceImpl getInstance() {
        if (instance == null) {
            instance = new AuditServiceImpl();
        }
        return instance;
    }

    // Async log submission pulling missing user attributes from active session
    @Override
    public void log(String username, String module, String action, String details, String status) {
        String userId = "SYSTEM";
        String roleName = "N/A";
        String ipAddress = "127.0.0.1";

        try {
            if (Sessions.getCurrent() != null) {
                Object uId = Sessions.getCurrent().getAttribute("USER_ID");
                Object uRole = Sessions.getCurrent().getAttribute("ROLE_NAME");
                if (uId != null) userId = uId.toString();
                if (uRole != null) roleName = uRole.toString();
            }
            if (Executions.getCurrent() != null) {
                ipAddress = Executions.getCurrent().getRemoteAddr();
            }
        } catch (Exception ignored) {}

        if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
        }

        final String finalUser = (username != null && !username.trim().isEmpty()) ? username.trim() : "Anonymous";
        final String fUserId = userId;
        final String fRoleName = roleName;
        final String fIpAddress = ipAddress;
        final String fStatus = (status != null ? status : "SUCCESS");

        AuditLog entry = new AuditLog(
                null,
                new Timestamp(System.currentTimeMillis()),
                fUserId,
                finalUser,
                fRoleName,
                module,
                action,
                details,
                fIpAddress,
                fStatus
        );

        CompletableFuture.runAsync(() -> auditDAO.insertAuditLog(entry), auditExecutor);
    }

    // Primary log method writing to file and queuing asynchronous database insertion
    @Override
    public void log(String userId, String username, String roleName, String module, String action, String details, String status) {
        String ipAddress = "127.0.0.1";
        try {
            if (Executions.getCurrent() != null) {
                ipAddress = Executions.getCurrent().getRemoteAddr();
            }
        } catch (Exception ignored) {}

        if ("0:0:0:0:0:0:0:1".equals(ipAddress) || "::1".equals(ipAddress)) {
            ipAddress = "127.0.0.1";
        }

        final String fUserId = (userId != null && !userId.trim().isEmpty()) ? userId.trim() : "SYSTEM";
        final String fUsername = (username != null && !username.trim().isEmpty()) ? username.trim() : "Anonymous";
        final String fRoleName = (roleName != null && !roleName.trim().isEmpty()) ? roleName.trim() : "N/A";
        final String fIpAddress = ipAddress;
        final String fStatus = (status != null ? status : "SUCCESS");

        auditLogger.info("USER:[{}] | ROLE:[{}] | MODULE:[{}] | ACTION:[{}] | STATUS:[{}] | IP:[{}] | DETAILS:[{}]",
                fUserId, fRoleName, module, action, fStatus, fIpAddress, details);

        AuditLog entry = new AuditLog(
                null,
                new Timestamp(System.currentTimeMillis()),
                fUserId,
                fUsername,
                fRoleName,
                module,
                action,
                details,
                fIpAddress,
                fStatus
        );

        CompletableFuture.runAsync(() -> auditDAO.insertAuditLog(entry), auditExecutor);
    }

    // Convenience overload extracting user credentials from current session
    @Override
    public void log(String module, String action, String details, String status) {
        String userId = "SYSTEM";
        String username = "Anonymous";
        String roleName = "N/A";

        try {
            if (Sessions.getCurrent() != null) {
                Object uId = Sessions.getCurrent().getAttribute("USER_ID");
                Object uName = Sessions.getCurrent().getAttribute("USERNAME");
                Object uRole = Sessions.getCurrent().getAttribute("ROLE_NAME");

                if (uId != null) userId = uId.toString();
                if (uName != null) username = uName.toString();
                if (uRole != null) roleName = uRole.toString();
            }
        } catch (Exception ignored) {}

        log(userId, username, roleName, module, action, details, status);
    }

    // Executes count and search queries concurrently on the executor pool
    @Override
    public AuditSearchResult searchAuditLogsConcurrently(Date fromDate, Date toDate, String module,
                                                         String action, String query, int offset, int limit) {
        CompletableFuture<List<AuditLog>> logsFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("[THREAD DEBUG - FETCH] Running on: " + Thread.currentThread().getName());
            return auditDAO.searchAuditLogs(fromDate, toDate, module, action, query, offset, limit);
        }, auditExecutor);

        CompletableFuture<Integer> countFuture = CompletableFuture.supplyAsync(() -> {
            System.out.println("[THREAD DEBUG - COUNT] Running on: " + Thread.currentThread().getName());
            return auditDAO.countAuditLogs(fromDate, toDate, module, action, query);
        }, auditExecutor);

        // Await both tasks before constructing combined result
        CompletableFuture.allOf(logsFuture, countFuture).join();

        try {
            return new AuditSearchResult(logsFuture.get(), countFuture.get());
        } catch (Exception e) {
            auditLogger.error("Failed executing parallel audit queries", e);
            return new AuditSearchResult(new ArrayList<>(), 0);
        }
    }

    // Direct synchronous search query delegation
    @Override
    public List<AuditLog> searchAuditLogs(Date fromDate, Date toDate, String module, String action, String query, int offset, int limit) {
        return auditDAO.searchAuditLogs(fromDate, toDate, module, action, query, offset, limit);
    }

    // Direct synchronous count query delegation
    @Override
    public int countAuditLogs(Date fromDate, Date toDate, String module, String action, String query) {
        return auditDAO.countAuditLogs(fromDate, toDate, module, action, query);
    }
}