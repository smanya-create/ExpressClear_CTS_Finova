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
                    "Cheque information is not available. Please check the uploaded file.");
        }

        List<ScanCheque> chequeList = data.getChequeList();

        if (chequeList == null || chequeList.isEmpty()) {
            return new ValidationResult(
                    false,
                    "No cheque information is available. Please check the uploaded file.");
        }

        for (ScanCheque cheque : chequeList) {

            if (cheque == null) {
                return new ValidationResult(
                        false,
                        "Invalid cheque information found. Please check the uploaded file.");
            }

            String chequeId = cheque.getScannedChequeId();

            // Scanned Cheque ID
            if (chequeId == null || chequeId.trim().isEmpty()) {
                return new ValidationResult(
                        false,
                        "Scanned cheque ID is missing. Please check the cheque details.");
            }

            chequeId = chequeId.trim();

            // Scanned Batch ID
            if (cheque.getScannedBatchId() == null
                    || cheque.getScannedBatchId().trim().isEmpty()) {

                return new ValidationResult(
                        false,
                        "Scanned batch ID is missing for cheque "
                                + chequeId + ". Please check the cheque details.");
            }

            // Cheque Number
            if (cheque.getChequeNumber() == null
                    || cheque.getChequeNumber().trim().isEmpty()) {

                return new ValidationResult(
                        false,
                        "Cheque number is missing for cheque "
                                + chequeId + ". Please check the cheque details.");
            }

            // Account Number
            if (cheque.getDraweeAccountNumber() == null
                    || cheque.getDraweeAccountNumber().trim().isEmpty()) {

                return new ValidationResult(
                        false,
                        "Account number is missing for cheque "
                                + chequeId + ". Please check the cheque details.");
            }
        }

        return new ValidationResult(true, null);
    }
}