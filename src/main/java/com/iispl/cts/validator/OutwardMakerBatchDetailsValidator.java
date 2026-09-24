package com.iispl.cts.validator;

public interface OutwardMakerBatchDetailsValidator {

	String sanitizeBatchId(String batchId);

	boolean isValidBatchId(String batchId);

	boolean canModifyCheque(String chequeStatus);

	String determineTargetView(String chequeStatus);
}