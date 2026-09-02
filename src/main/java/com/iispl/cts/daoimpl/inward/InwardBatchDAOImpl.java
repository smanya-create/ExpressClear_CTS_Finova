package com.iispl.cts.daoimpl.inward;

import java.math.BigDecimal;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.dao.inward.InwardBatchDAO;
import com.iispl.cts.entity.inward.InwardBatch;

public class InwardBatchDAOImpl implements InwardBatchDAO {

    // Static in-memory storage (replace with JDBC ResultSet later)
    private final List<InwardBatch> batchTable = new ArrayList<>();

    public InwardBatchDAOImpl() {
        initStaticBatches();
    }

    private void initStaticBatches() {
        batchTable.add(new InwardBatch(
            "BAT1001", 
            "REF-BATCH-2026-001", 
            2, 
            new BigDecimal("3775000.00"), 
            "Processing", 
            "USR1001", 
            Timestamp.valueOf("2026-08-31 15:14:01")
        ));

        batchTable.add(new InwardBatch(
            "BAT1002", 
            "REF-BATCH-2026-002", 
            3, 
            new BigDecimal("697500.00"), 
            "Processing", 
            "USR1001", 
            Timestamp.valueOf("2026-08-31 15:14:01")
        ));
    }

    @Override
    public List<InwardBatch> findAllActiveBatches() {
        return new ArrayList<>(batchTable);
    }

    @Override
    public InwardBatch findById(String batchId) {
        return batchTable.stream()
                .filter(b -> b.getInwardBatchId().equalsIgnoreCase(batchId))
                .findFirst()
                .orElse(null);
    }

    @Override
    public boolean updateStatus(String batchId, String status) {
        InwardBatch batch = findById(batchId);
        if (batch != null) {
            batch.setBatchStatus(status);
            return true;
        }
        return false;
    }
}