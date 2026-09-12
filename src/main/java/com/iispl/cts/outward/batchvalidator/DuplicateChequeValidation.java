
package com.iispl.cts.outward.batchvalidator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.iispl.cts.dto.BatchValidationData;
import com.iispl.cts.dto.ValidationResult;
import com.iispl.cts.entity.outward.ScanCheque;

public class DuplicateChequeValidation implements ValidateBatch {

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

        Set<String> scannedChequeIds = new HashSet<>();
        Set<String> chequeNumberAccountCombinations = new HashSet<>();

        for (ScanCheque cheque : chequeList) {

            if (cheque == null) {
                return new ValidationResult(
                        false,
                        "Invalid cheque information found. Please check the uploaded file.");
            }

            String scannedChequeId = cheque.getScannedChequeId();
            String chequeNumber = cheque.getChequeNumber();
            String accountNumber = cheque.getDraweeAccountNumber();

            /*
             * Duplicate Scanned Cheque ID
             */
            if (scannedChequeId != null
                    && !scannedChequeId.trim().isEmpty()) {

                scannedChequeId = scannedChequeId.trim();

                if (!scannedChequeIds.add(scannedChequeId)) {

                    return new ValidationResult(
                            false,
                            "Duplicate scanned cheque ID "
                                    + scannedChequeId
                                    + " found in the uploaded batch. "
                                    + "Please check the cheque details.");
                }
            }

            /*
             * Duplicate Cheque Number + Account Number
             */
            if (chequeNumber != null
                    && !chequeNumber.trim().isEmpty()
                    && accountNumber != null
                    && !accountNumber.trim().isEmpty()) {

                chequeNumber = chequeNumber.trim();
                accountNumber = accountNumber.trim();

                String combination =
                        chequeNumber + "|" + accountNumber;

                if (!chequeNumberAccountCombinations.add(combination)) {

                    return new ValidationResult(
                            false,
                            "Duplicate cheque found. Cheque number "
                                    + chequeNumber
                                    + " with account number "
                                    + accountNumber
                                    + " already exists in the uploaded batch. "
                                    + "Please check the cheque details.");
                }
            }
        }

        return new ValidationResult(true, null);
    }
}

