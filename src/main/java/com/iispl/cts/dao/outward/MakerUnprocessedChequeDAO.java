package com.iispl.cts.dao.outward;

import java.util.List;

import com.iispl.cts.dto.UnprocessedChequeDTO;

public interface MakerUnprocessedChequeDAO {
	/**
     * Retrieves all pending/unprocessed cheques filtered by operator role
     * and prioritized by Forced EOD rollover status.
     *
     * @param userRole "MAKER", "CHECKER", or "ADMIN"
     * @return List of pending cheques
     */
    List<UnprocessedChequeDTO> getUnprocessedCheques(String userRole);

    /**
     * Checks if there are active in-flight rollover cheques from previous clearing sessions.
     *
     * @return count of pending rollover items
     */
    long countPendingRolloverItems();

	
}
