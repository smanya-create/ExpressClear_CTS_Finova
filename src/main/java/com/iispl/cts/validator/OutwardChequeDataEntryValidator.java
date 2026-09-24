package com.iispl.cts.validator;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import com.iispl.cts.entity.outward.OutwardCheque;

public interface OutwardChequeDataEntryValidator {

	ValidationResult validateChequeFields(String chequeNumber, String amountText, BigDecimal amount, Date chequeDate,
			String payeeAccount, String payeeName, String draweeName);

	String normalizeStatus(String status);

	String formatDisplayStatus(String status);

	boolean isMicrPending(String status);

	boolean isMicrRejected(String status);

	boolean isRejectRequest(String status);

	boolean isOnHold(String status);

	boolean isPostDated(Date chequeDate);

	boolean hasReturnedCheques(List<OutwardCheque> chequeList);

	boolean isEditable(OutwardCheque cheque, boolean batchHasReturned);

	boolean isBatchReadyForChecker(List<OutwardCheque> chequeList, int totalCount, boolean batchSubmitted,
			boolean hasUnsavedChanges);

	String buildCheckerNotificationMessage(String batchId, List<OutwardCheque> chequeList);

	public static class ValidationResult {
		private final boolean valid;
		private final String fieldId;
		private final String errorMessage;

		public ValidationResult(boolean valid, String fieldId, String errorMessage) {
			this.valid = valid;
			this.fieldId = fieldId;
			this.errorMessage = errorMessage;
		}

		public static ValidationResult success() {
			return new ValidationResult(true, null, null);
		}

		public static ValidationResult failure(String fieldId, String errorMessage) {
			return new ValidationResult(false, fieldId, errorMessage);
		}

		public boolean isValid() {
			return valid;
		}

		public String getFieldId() {
			return fieldId;
		}

		public String getErrorMessage() {
			return errorMessage;
		}
	}
}