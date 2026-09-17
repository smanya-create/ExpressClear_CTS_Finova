package com.iispl.cts.outward.batchvalidator;

import java.math.BigDecimal;
import java.util.List;

import com.iispl.cts.dto.BatchValidationData;
import com.iispl.cts.dto.ValidationResult;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;

public class TotalAmountValidation implements ValidateBatch {

    @Override
    public ValidationResult validate(BatchValidationData data) {

        if (data == null) {
            return new ValidationResult(
                    false,
                    "Invalid batch details found.");
        }

        ScanBatch batch = data.getBatch();

        if (batch == null) {
            return new ValidationResult(
                    false,
                    "Invalid batch details found.");
        }

        List<ScanCheque> chequeList = data.getChequeList();

        if (chequeList == null) {
            return new ValidationResult(
                    false,
                    "Invalid cheque details found in batch.");
        }

        BigDecimal expectedTotalAmount = data.getExpectedTotalAmount();

        if (expectedTotalAmount == null) {
            return new ValidationResult(
                    false,
                    "Total batch amount mismatch.");
        }

        BigDecimal batchTotalAmount = batch.getActualTotalAmount();

        if (batchTotalAmount == null) {
            return new ValidationResult(
                    false,
                    "Total batch amount mismatch.");
        }

        /*
         * Calculate total amount from cheque list
         */
        BigDecimal actualTotalAmount = BigDecimal.ZERO;

        for (ScanCheque cheque : chequeList) {

            if (cheque == null) {
                return new ValidationResult(
                        false,
                        "Invalid cheque details found in batch.");
            }

            BigDecimal chequeAmount = cheque.getChequeAmount();

            if (chequeAmount == null) {
                return new ValidationResult(
                        false,
                        "Invalid cheque details found in batch.");
            }

            actualTotalAmount = actualTotalAmount.add(chequeAmount);
        }

        /*
         * Check for any amount mismatch across expected, header, or actual cheque total
         */
        if (expectedTotalAmount.compareTo(batchTotalAmount) != 0
                || expectedTotalAmount.compareTo(actualTotalAmount) != 0
                || batchTotalAmount.compareTo(actualTotalAmount) != 0) {

            return new ValidationResult(
                    false,
                    "Total batch amount mismatch.");
        }

        return new ValidationResult(true, null);
    }
}