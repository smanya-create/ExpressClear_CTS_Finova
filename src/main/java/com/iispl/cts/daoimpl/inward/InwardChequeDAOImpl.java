package com.iispl.cts.daoimpl.inward;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.iispl.cts.dao.inward.InwardChequeDAO;
import com.iispl.cts.entity.inward.InwardCheque;

public class InwardChequeDAOImpl implements InwardChequeDAO {

    // Static in-memory storage (replace with JDBC PreparedStatement later)
    private final List<InwardCheque> chequeTable = new ArrayList<>();

    public InwardChequeDAOImpl() {
        initStaticCheques();
    }

    private void initStaticCheques() {
        Timestamp now = new Timestamp(System.currentTimeMillis());

        // --- BAT1001 Cheques ---
        chequeTable.add(new InwardCheque("CH1005", "BAT1001", "100456", "110532119", 
            "AGARWAL TEXTILES HUF", "07890200001888", "PAWAN KUMAR BANDARU", "5104873962", 
            new BigDecimal("75000.00"), Date.valueOf("2026-04-05"), "PENDING_DATA_ENTRY", null, now));

        chequeTable.add(new InwardCheque("CH1006", "BAT1001", "119876", "110532119", 
            "AGARWAL TEXTILES HUF", "07890200001888", "SARITA & ANAND TEXTILES PRIVATE LIMITED", "4256718903", 
            new BigDecimal("3500000.00"), Date.valueOf("2026-03-25"), "PENDING_DATA_ENTRY", null, now));

        // --- BAT1002 Cheques ---
        chequeTable.add(new InwardCheque("CH1007", "BAT1002", "204512", "560220001", 
            "Rajesh Sharma", "000201589632", "Vikram Solar Systems", "995498236410235", 
            new BigDecimal("185000.00"), Date.valueOf("2026-06-30"), "PENDING_DATA_ENTRY", null, now));

        chequeTable.add(new InwardCheque("CH1008", "BAT1002", "310890", "400240015", 
            "Meera Nair", "50100234987110", "AGARWAL TEXTILES HUF", "07890200001888", 
            new BigDecimal("450000.00"), Date.valueOf("2026-06-30"), "PENDING_DATA_ENTRY", null, now));

        chequeTable.add(new InwardCheque("CH1009", "BAT1002", "552109", "600211008", 
            "K. V. Raman", "918020054321789", "PAWAN KUMAR BANDARU", "5104873962", 
            new BigDecimal("62500.00"), Date.valueOf("2026-06-30"), "PENDING_DATA_ENTRY", null, now));
    }

    @Override
    public List<InwardCheque> findByBatchAndStatus(String batchId, String status) {
        return chequeTable.stream()
                .filter(c -> c.getInwardBatchId().equalsIgnoreCase(batchId) 
                          && (status == null || c.getChequeStatus().equalsIgnoreCase(status)))
                .collect(Collectors.toList());
    }

    @Override
    public boolean updateCheque(InwardCheque cheque) {
        for (int i = 0; i < chequeTable.size(); i++) {
            if (chequeTable.get(i).getInwardChequeId().equals(cheque.getInwardChequeId())) {
                chequeTable.set(i, cheque);
                return true;
            }
        }
        return false;
    }
}