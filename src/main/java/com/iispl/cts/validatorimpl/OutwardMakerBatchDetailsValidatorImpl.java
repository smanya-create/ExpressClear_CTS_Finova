package com.iispl.cts.validatorimpl;

import java.util.Locale;

import com.iispl.cts.validator.OutwardMakerBatchDetailsValidator;

public class OutwardMakerBatchDetailsValidatorImpl implements OutwardMakerBatchDetailsValidator {

	private static final String STATUS_PENDING_MICR_REPAIR = "PENDING_MICR_REPAIR";
	private static final String STATUS_PENDING_DATA_ENTRY = "PENDING_DATA_ENTRY";

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
	public boolean canModifyCheque(String chequeStatus) {
		if (chequeStatus == null || chequeStatus.trim().isEmpty()) {
			return false;
		}
		String normalized = chequeStatus.trim().toUpperCase(Locale.ENGLISH).replace('-', '_').replace(' ', '_');
		return STATUS_PENDING_MICR_REPAIR.equals(normalized) || STATUS_PENDING_DATA_ENTRY.equals(normalized);
	}

	@Override
	public String determineTargetView(String chequeStatus) {
		if (chequeStatus == null || chequeStatus.trim().isEmpty()) {
			return "/outward/maker/cheque-data-entry.zul";
		}
		String normalized = chequeStatus.trim().toUpperCase(Locale.ENGLISH).replace('-', '_').replace(' ', '_');
		if (STATUS_PENDING_MICR_REPAIR.equals(normalized)) {
			return "/outward/maker/micr-repair/micr-repair-view.zul";
		}
		return "/outward/maker/cheque-data-entry.zul";
	}
}