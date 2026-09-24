package com.iispl.cts.validatorimpl;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.validator.OutwardChequeDataEntryValidator;

public class OutwardChequeDataEntryValidatorImpl implements OutwardChequeDataEntryValidator {

	private static final String STATUS_PENDING_DATA_ENTRY = "PENDING_DATA_ENTRY";
	private static final String STATUS_PENDING_VERIFICATION = "PENDING_VERIFICATION";
	private static final String STATUS_MAKER_RETURNED = "MAKER_RETURNED";
	private static final String STATUS_ON_HOLD = "ON_HOLD";
	private static final String STATUS_REJECT_REQUEST = "REJECT_REQUEST";
	private static final String STATUS_REJECTION_REQUEST = "REJECTION_REQUEST";
	private static final String STATUS_REJECTION_REJECT = "REJECTION_REJECT";
	private static final String STATUS_MICR_REJECTED = "MICR_REJECTED";
	private static final String STATUS_PENDING_MICR_REPAIR = "PENDING_MICR_REPAIR";

	@Override
	public ValidationResult validateChequeFields(String chequeNumber, String amountText, BigDecimal amount,
			Date chequeDate, String payeeAccount, String payeeName, String draweeName) {

		if (chequeNumber == null || chequeNumber.trim().isEmpty()) {
			return ValidationResult.failure("outwardChequeDataEntryTxtChequeNumber", "Cheque number is required.");
		}
		if (amountText == null || amountText.trim().isEmpty()) {
			return ValidationResult.failure("outwardChequeDataEntryTxtAmount", "Cheque amount is required.");
		}
		if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
			return ValidationResult.failure("outwardChequeDataEntryTxtAmount", "Enter a valid positive cheque amount.");
		}
		if (chequeDate == null) {
			return ValidationResult.failure("outwardChequeDataEntryDtChequeDate", "Cheque date is required.");
		}
		if (payeeAccount == null || payeeAccount.trim().isEmpty()) {
			return ValidationResult.failure("outwardChequeDataEntryTxtPayeeAccount",
					"Payee account number is required.");
		}
		if (payeeName == null || payeeName.trim().isEmpty()) {
			return ValidationResult.failure("outwardChequeDataEntryTxtPayeeName", "Payee name is required.");
		}
		if (draweeName == null || draweeName.trim().isEmpty()) {
			return ValidationResult.failure("outwardChequeDataEntryTxtDraweeName", "Drawee bank name is required.");
		}

		return ValidationResult.success();
	}

	@Override
	public String normalizeStatus(String status) {
		if (status == null || status.trim().isEmpty()) {
			return "";
		}
		return status.trim().replace("-", "_").replace(" ", "_").toUpperCase(Locale.ENGLISH);
	}

	@Override
	public String formatDisplayStatus(String status) {
		String normalized = normalizeStatus(status);
		if (normalized.isEmpty())
			return "-";

		if (STATUS_MAKER_RETURNED.equals(normalized))
			return "Maker Returned";
		if (STATUS_PENDING_VERIFICATION.equals(normalized))
			return "Pending Verification";
		if (STATUS_PENDING_DATA_ENTRY.equals(normalized))
			return "Pending Data Entry";
		if (STATUS_REJECT_REQUEST.equals(normalized) || STATUS_REJECTION_REQUEST.equals(normalized)
				|| STATUS_REJECTION_REJECT.equals(normalized))
			return "Reject Request";
		if (isOnHold(normalized))
			return "On Hold";
		if (isMicrPending(normalized))
			return "Pending MICR Repair";
		if (isMicrRejected(normalized))
			return "MICR Rejected";

		String[] words = normalized.toLowerCase(Locale.ENGLISH).split("_");
		StringBuilder result = new StringBuilder();
		for (String word : words) {
			if (word == null || word.isEmpty())
				continue;
			if (result.length() > 0)
				result.append(" ");
			result.append(Character.toUpperCase(word.charAt(0)));
			if (word.length() > 1)
				result.append(word.substring(1));
		}
		return result.toString();
	}

	@Override
	public boolean isMicrPending(String status) {
		String s = normalizeStatus(status);
		return s.equals(STATUS_PENDING_MICR_REPAIR) || s.equals("MICR_PENDING") || s.equals("PENDING_MICR");
	}

	@Override
	public boolean isMicrRejected(String status) {
		String s = normalizeStatus(status);
		return s.equals(STATUS_MICR_REJECTED) || s.equals("MICR_REJECT");
	}

	@Override
	public boolean isRejectRequest(String status) {
		String s = normalizeStatus(status);
		return s.equals(STATUS_REJECT_REQUEST) || s.equals(STATUS_REJECTION_REQUEST)
				|| s.equals(STATUS_REJECTION_REJECT);
	}

	@Override
	public boolean isOnHold(String status) {
		String s = normalizeStatus(status);
		return s.equals(STATUS_ON_HOLD) || s.equals("SEND_BACK") || s.equals("CHECKER_SEND_BACK");
	}

	@Override
	public boolean isPostDated(Date chequeDate) {
		if (chequeDate == null)
			return false;
		return chequeDate.getTime() > System.currentTimeMillis();
	}

	@Override
	public boolean hasReturnedCheques(List<OutwardCheque> chequeList) {
		if (chequeList == null)
			return false;
		for (OutwardCheque cheque : chequeList) {
			if (cheque != null) {
				String status = normalizeStatus(cheque.getChequeStatus());
				if (isOnHold(status) || STATUS_MAKER_RETURNED.equals(status)) {
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean isEditable(OutwardCheque cheque, boolean batchHasReturned) {
		if (cheque == null)
			return false;
		String status = normalizeStatus(cheque.getChequeStatus());

		if (isRejectRequest(status) || isMicrRejected(status) || isMicrPending(status)) {
			return false;
		}

		if (batchHasReturned) {
			return isOnHold(status) || STATUS_MAKER_RETURNED.equals(status);
		}

		return true;
	}

	@Override
	public boolean isBatchReadyForChecker(List<OutwardCheque> chequeList, int totalCount, boolean batchSubmitted,
			boolean hasUnsavedChanges) {
		if (batchSubmitted || hasUnsavedChanges)
			return false;
		if (chequeList == null || chequeList.isEmpty() || totalCount <= 0)
			return false;
		if (chequeList.size() != totalCount)
			return false;

		int completed = 0;
		for (OutwardCheque cheque : chequeList) {
			if (cheque == null)
				return false;
			String status = normalizeStatus(cheque.getChequeStatus());

			boolean isCompleted = STATUS_PENDING_VERIFICATION.equals(status) || STATUS_MAKER_RETURNED.equals(status)
					|| isRejectRequest(status);

			if (!isCompleted) {
				return false;
			}
			completed++;
		}
		return completed == totalCount;
	}

	@Override
	public String buildCheckerNotificationMessage(String batchId, List<OutwardCheque> chequeList) {
		List<String> modifiedChequeNumbers = new ArrayList<>();
		if (chequeList != null) {
			for (OutwardCheque oc : chequeList) {
				if (oc != null && STATUS_MAKER_RETURNED.equals(normalizeStatus(oc.getChequeStatus()))) {
					String num = oc.getChequeNumber();
					if (num != null && !num.trim().isEmpty()) {
						modifiedChequeNumbers.add(num.trim());
					}
				}
			}
		}

		if (!modifiedChequeNumbers.isEmpty()) {
			if (modifiedChequeNumbers.size() == 1) {
				return "Batch " + batchId + ": Cheque No. " + modifiedChequeNumbers.get(0)
						+ " has been modified by Maker and is ready for re-approval.";
			}
			return "Batch " + batchId + ": Returned cheques (" + String.join(", ", modifiedChequeNumbers)
					+ ") have been modified by Maker and are ready for re-approval.";
		}

		return "Batch " + batchId + " is ready for Checker processing.";
	}
}