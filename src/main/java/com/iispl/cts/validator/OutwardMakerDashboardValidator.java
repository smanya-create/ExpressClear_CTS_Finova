package com.iispl.cts.validator;

public interface OutwardMakerDashboardValidator {

	String sanitizeSearchKeyword(String keyword);

	boolean isValidSearchKeyword(String keyword);

	boolean isThreeDigitSearch(String search);

	boolean matchesBatchSearch(String actualBatchId, String searchKeyword);

	String validateModule(String module);

	String validateStatus(String status);
}