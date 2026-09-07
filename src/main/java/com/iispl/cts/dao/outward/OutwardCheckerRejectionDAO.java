package com.iispl.cts.dao.outward;

import java.sql.Date;
import java.util.List;

import com.iispl.cts.entity.outward.OutwardRejectedCheques;

public interface OutwardCheckerRejectionDAO {

    List<OutwardRejectedCheques> getRejectedCheques(
            int limit,
            int offset) throws Exception;

    List<OutwardRejectedCheques> searchRejectedCheques(
            String searchValue,
            Date rejectedDate,
            int limit,
            int offset) throws Exception;

    int getTotalRejectedCheques() throws Exception;

    int getTotalRejectedCheques(
            String searchValue,
            Date rejectedDate) throws Exception;

    boolean saveRejectedCheque(
            OutwardRejectedCheques rejectedCheque) throws Exception;
}