package com.iispl.cts.enums;

public enum OutwardChequeStatus {
	/**
	 * Granular lifecycle statuses for individual cheque instruments.
	 */
	
	    /**
	     * XML ingested; instrument requires mandatory Maker MICR verification/repair.
	     */
	    PENDING_MICR,

	    /**
	     * MICR confirmed; instrument requires mandatory Maker amount entry.
	     */
	    PENDING_DATA_ENTRY,

	    /**
	     * Both MICR and Amount completed by Maker; awaiting Checker review.
	     */
	    REPAIRED_BY_MAKER,

	    /**
	     * Checker verified instrument AND real-time inline CBS validation succeeded.
	     * (Terminal clearing state).
	     */
	    READY_FOR_CLEARANCE,

	    /**
	     * Checker clicked Verify, but inline CBS validation failed 
	     * (e.g., account dormant, closed, invalid).
	     */
	    CBS_FAILED,

	    /**
	     * Instrument formally rejected (e.g., signature mismatch, technical return).
	     * Routed to Outward Return memo file (Terminal rejection state).
	     */
	    REJECTED
	}

