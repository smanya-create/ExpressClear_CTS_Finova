package com.iispl.cts.outward.batchvalidator;

import java.util.List;

import com.iispl.cts.dto.BatchValidationData;
import com.iispl.cts.dto.ValidationResult;
import com.iispl.cts.entity.outward.ScanCheque;

public class ChequeDataValidation implements ValidateBatch {

    @Override
    public ValidationResult validate(BatchValidationData data) {

        if (data == null) {
            return new ValidationResult(
                    false,
                    "Invalid cheque details found in batch.");
        }

        List<ScanCheque> chequeList = data.getChequeList();

        if (chequeList == null || chequeList.isEmpty()) {
            return new ValidationResult(
                    false,
                    "Invalid cheque details found in batch.");
        }

        for (ScanCheque cheque : chequeList) {

            if (cheque == null) {
                return new ValidationResult(
                        false,
                        "Invalid cheque details found in batch.");
            }

            // Scanned Cheque ID
            if (cheque.getScannedChequeId() == null
                    || cheque.getScannedChequeId().trim().isEmpty()) {
                return new ValidationResult(
                        false,
                        "Invalid cheque details found in batch.");
            }

            // Scanned Batch ID
            if (cheque.getScannedBatchId() == null
                    || cheque.getScannedBatchId().trim().isEmpty()) {
                return new ValidationResult(
                        false,
                        "Invalid cheque details found in batch.");
            }

            // Cheque Number
            if (cheque.getChequeNumber() == null
                    || cheque.getChequeNumber().trim().isEmpty()) {
                return new ValidationResult(
                        false,
                        "Invalid cheque details found in batch.");
            }

            // Account Number
            if (cheque.getDraweeAccountNumber() == null
                    || cheque.getDraweeAccountNumber().trim().isEmpty()) {
                return new ValidationResult(
                        false,
                        "Invalid cheque details found in batch.");
            }
        }

        return new ValidationResult(true, null);
    }
}