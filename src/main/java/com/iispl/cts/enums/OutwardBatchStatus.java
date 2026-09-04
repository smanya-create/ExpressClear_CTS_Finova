package com.iispl.cts.enums;

public enum OutwardBatchStatus {
	/**
     * Batch XML ingested; instruments are undergoing mandatory
     * Maker processing (Stage 1 MICR Repair or Stage 2 Data Entry).
     */
    MAKER_PROCESSING,

    /**
     * All instruments have been completed by Maker and handed
     * over to the Checker desk for review.
     */
    PENDING_CHECKER_REVIEW,

    /**
     * Checker returned >= 1 instrument to Maker for correction.
     * The remaining batch items stay active at the Checker desk.
     */
    PARTIALLY_REWORKED,

    /**
     * All instruments in the batch have reached a terminal state
     * (READY_FOR_CLEARANCE or REJECTED); ready for clearing file generation.
     */
    COMPLETED,

    /**
     * Entire batch cancelled or discarded (e.g., duplicate upload).
     */
    CANCELLED


}
