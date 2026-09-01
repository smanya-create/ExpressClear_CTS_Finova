package com.iispl.cts.dao.outward;

import java.util.List;

import com.iispl.cts.dto.UnprocessedChequeDTO;

public interface CheckerUnprocessedChequeDAO {
	/**
     * Retrieves all Forced EOD rollover cheques awaiting Checker verification.
     */
    List<UnprocessedChequeDTO> getCheckerUnprocessedCheques();

    /**
     * Approves/Verifies an unprocessed cheque and marks it ready for CXF file generation.
     */
    boolean verifyCheque(Long chequeId, String checkerUserId);

    /**
     * Sends the cheque back to Maker with an actionable return reason.
     */
    boolean sendBackToMaker(Long chequeId, Long sendBackReasonId, String checkerRemarks, String checkerUserId);

    /**
     * Rejects the cheque permanently with an NPCI standard rejection code.
     */
    boolean rejectCheque(Long chequeId, Long rejectReasonId, String rejectRemarks, String checkerUserId);

}
