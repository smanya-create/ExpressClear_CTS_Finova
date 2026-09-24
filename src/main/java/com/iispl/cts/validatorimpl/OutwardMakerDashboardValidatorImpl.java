package com.iispl.cts.validatorimpl;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

import com.iispl.cts.validator.OutwardMakerDashboardValidator;

public class OutwardMakerDashboardValidatorImpl implements OutwardMakerDashboardValidator {

	private static final Set<String> ALLOWED_MODULES = new HashSet<>(
			Arrays.asList("ALL", "RETURN_FROM_CHECKER", "BATCH_PROCESSING"));

	private static final Set<String> ALLOWED_STATUSES = new HashSet<>(
			Arrays.asList("ALL", "PENDING_MAKER_PROCESS", "PENDING_DATA_ENTRY", "PENDING_MICR_REPAIR", "MICR_REJECTED",
					"ON_HOLD", "PENDING_CHECKER_PROCESS", "PENDING_VERIFICATION", "MAKER_RETURNED", "REJECT_REQUEST"));

	@Override
	public String sanitizeSearchKeyword(String keyword) {
		if (keyword == null) {
			return "";
		}
		return keyword.trim();
	}

	@Override
	public boolean isValidSearchKeyword(String keyword) {
		if (keyword == null || keyword.trim().isEmpty()) {
			return true;
		}
		String clean = keyword.trim();
		return clean.matches("^[a-zA-Z0-9_-]+$");
	}

	@Override
	public boolean isThreeDigitSearch(String search) {
		if (search == null || search.length() != 3) {
			return false;
		}
		for (int index = 0; index < search.length(); index++) {
			if (!Character.isDigit(search.charAt(index))) {
				return false;
			}
		}
		return true;
	}

	@Override
	public boolean matchesBatchSearch(String actualBatchId, String searchKeyword) {
		if (searchKeyword == null || searchKeyword.trim().isEmpty()) {
			return true;
		}
		if (actualBatchId == null || actualBatchId.trim().isEmpty()) {
			return false;
		}

		String actual = actualBatchId.trim().toUpperCase(Locale.ENGLISH);
		String search = searchKeyword.trim().toUpperCase(Locale.ENGLISH);

		if (isThreeDigitSearch(search)) {
			if (!actual.startsWith("BAT") || actual.length() != 6) {
				return false;
			}
			return actual.substring(3).equals(search);
		}

		return actual.contains(search);
	}

	@Override
	public String validateModule(String module) {
		if (module == null || module.trim().isEmpty()) {
			return "ALL";
		}
		String normalized = module.trim().toUpperCase(Locale.ENGLISH);
		return ALLOWED_MODULES.contains(normalized) ? normalized : "ALL";
	}

	@Override
	public String validateStatus(String status) {
		if (status == null || status.trim().isEmpty()) {
			return "ALL";
		}
		String normalized = status.trim().toUpperCase(Locale.ENGLISH);
		return ALLOWED_STATUSES.contains(normalized) ? normalized : "ALL";
	}
}