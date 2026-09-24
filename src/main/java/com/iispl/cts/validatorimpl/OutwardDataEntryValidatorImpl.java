package com.iispl.cts.validatorimpl;

import java.util.Locale;

import com.iispl.cts.validator.OutwardDataEntryValidator;

public class OutwardDataEntryValidatorImpl implements OutwardDataEntryValidator {

	private static final String STATUS_PENDING_MAKER_PROCESS = "PENDING_MAKER_PROCESS";
	private static final String STATUS_PENDING_DATA_ENTRY = "PENDING_DATA_ENTRY";
	private static final String STATUS_PENDING_MICR_REPAIR = "PENDING_MICR_REPAIR";
	private static final String STATUS_MICR_REJECTED = "MICR_REJECTED";
	private static final String STATUS_PENDING_VERIFICATION = "PENDING_VERIFICATION";
	private static final String STATUS_PENDING_CHECKER_PROCESS = "PENDING_CHECKER_PROCESS";
	private static final String STATUS_REJECT_REQUEST = "REJECT_REQUEST";
	private static final String STATUS_MAKER_RETURNED = "MAKER_RETURNED";
	private static final String STATUS_ON_HOLD = "ON_HOLD";

	@Override
	public String normalizeStatus(String status) {
		if (status == null || status.trim().isEmpty()) {
			return "";
		}
		return status.trim().replace('-', '_').replace(' ', '_').toUpperCase(Locale.ENGLISH);
	}

	@Override
	public String formatStatus(String status) {
		String normalized = normalizeStatus(status);
		if (STATUS_PENDING_MAKER_PROCESS.equals(normalized) || STATUS_PENDING_DATA_ENTRY.equals(normalized)) {
			return "Pending Data Entry";
		}
		if (STATUS_ON_HOLD.equals(normalized)) {
			return "On Hold (Returned)";
		}
		if (STATUS_MAKER_RETURNED.equals(normalized)) {
			return "Maker Returned";
		}
		if (STATUS_REJECT_REQUEST.equals(normalized)) {
			return "Reject Request";
		}
		if (STATUS_PENDING_MICR_REPAIR.equals(normalized)) {
			return "Pending MICR Repair";
		}
		if (STATUS_MICR_REJECTED.equals(normalized)) {
			return "MICR Rejected";
		}
		if (STATUS_PENDING_VERIFICATION.equals(normalized)) {
			return "Pending Verification";
		}
		if (STATUS_PENDING_CHECKER_PROCESS.equals(normalized)) {
			return "Pending Checker Process";
		}
		if (normalized.isEmpty()) {
			return "-";
		}

		String[] parts = normalized.split("_");
		StringBuilder result = new StringBuilder();
		for (String part : parts) {
			if (part == null || part.isEmpty()) {
				continue;
			}
			if (result.length() > 0) {
				result.append(" ");
			}
			String lower = part.toLowerCase(Locale.ENGLISH);
			result.append(Character.toUpperCase(lower.charAt(0)));
			if (lower.length() > 1) {
				result.append(lower.substring(1));
			}
		}
		return result.toString();
	}

	@Override
	public String getStatusClass(String status) {
		String normalized = normalizeStatus(status);
		if (STATUS_ON_HOLD.equals(normalized)) {
			return "send-back";
		}
		if (STATUS_MAKER_RETURNED.equals(normalized)) {
			return "maker-returned";
		}
		if (STATUS_MICR_REJECTED.equals(normalized)) {
			return "rejected";
		}
		if (STATUS_PENDING_VERIFICATION.equals(normalized)) {
			return "completed";
		}
		if (STATUS_PENDING_CHECKER_PROCESS.equals(normalized)) {
			return "processing";
		}
		if (STATUS_REJECT_REQUEST.equals(normalized)) {
			return "reject-request";
		}
		return "pending";
	}

	@Override
	public String sanitizeBatchId(String batchId) {
		if (batchId == null) {
			return "";
		}
		return batchId.trim();
	}

	@Override
	public boolean isValidBatchId(String batchId) {
		if (batchId == null || batchId.trim().isEmpty()) {
			return false;
		}
		return batchId.trim().matches("^[a-zA-Z0-9_-]+$");
	}

	@Override
	public boolean isDataEntryEligible(String status) {
		String normalized = normalizeStatus(status);
		return STATUS_PENDING_DATA_ENTRY.equals(normalized) || STATUS_ON_HOLD.equals(normalized)
				|| STATUS_MAKER_RETURNED.equals(normalized) || STATUS_REJECT_REQUEST.equals(normalized)
				|| STATUS_PENDING_MICR_REPAIR.equals(normalized) || STATUS_PENDING_MAKER_PROCESS.equals(normalized);
	}

	@Override
	public boolean isReturnedFromChecker(String status) {
		String normalized = normalizeStatus(status);
		return STATUS_ON_HOLD.equals(normalized) || STATUS_REJECT_REQUEST.equals(normalized);
	}
}