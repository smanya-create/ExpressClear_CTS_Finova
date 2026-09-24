package com.iispl.cts.validator;

public interface OutwardDataEntryValidator {

	String normalizeStatus(String status);

	String formatStatus(String status);

	String getStatusClass(String status);

	String sanitizeBatchId(String batchId);

	boolean isValidBatchId(String batchId);

	boolean isDataEntryEligible(String status);

	boolean isReturnedFromChecker(String status);
}