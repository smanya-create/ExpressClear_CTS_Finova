package com.iispl.cts.outward.batchvalidator;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.iispl.cts.dto.BatchValidationData;
import com.iispl.cts.dto.ValidationResult;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.service.outward.OutwardMakerService;

public class DuplicateChequeValidation implements ValidateBatch {

    private OutwardMakerService outwardMakerService;

    public DuplicateChequeValidation(
            OutwardMakerService outwardMakerService) {

        this.outwardMakerService = outwardMakerService;
    }

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

        Set<String> scannedChequeIds = new HashSet<>();
        Set<String> chequeNumberAccountCombinations = new HashSet<>();

        for (ScanCheque cheque : chequeList) {

            if (cheque == null) {
                return new ValidationResult(
                        false,
                        "Invalid cheque details found in batch.");
            }

            String scannedChequeId = cheque.getScannedChequeId();
            String chequeNumber = cheque.getChequeNumber();
            String accountNumber = cheque.getDraweeAccountNumber();

            /*
             * Duplicate Scanned Cheque ID inside current batch
             */
            if (scannedChequeId != null
                    && !scannedChequeId.trim().isEmpty()) {

                scannedChequeId = scannedChequeId.trim();

                if (!scannedChequeIds.add(scannedChequeId)) {
                    return new ValidationResult(
                            false,
                            "Duplicate cheque found in batch.");
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

                String combination = chequeNumber + "|" + accountNumber;

                /*
                 * 1. Duplicate inside current batch
                 */
                if (!chequeNumberAccountCombinations.add(combination)) {
                    return new ValidationResult(
                            false,
                            "Duplicate cheque found in batch.");
                }

                /*
                 * 2. Duplicate in database (system-wide)
                 */
                if (outwardMakerService.existsChequeNumberAndAccount(
                        chequeNumber, accountNumber)) {
                    return new ValidationResult(
                            false,
                            "Cheque already exists in the system.");
                }
            }
        }

        return new ValidationResult(true, null);
    }
}