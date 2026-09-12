
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
                    "Batch information is not available. Please check the uploaded file.");
        }

        ScanBatch batch = data.getBatch();

        if (batch == null) {
            return new ValidationResult(
                    false,
                    "Batch information is not available. Please check the uploaded file.");
        }

        List<ScanCheque> chequeList = data.getChequeList();

        if (chequeList == null) {
            return new ValidationResult(
                    false,
                    "Cheque information is not available. Please check the uploaded file.");
        }

        BigDecimal expectedTotalAmount =
                data.getExpectedTotalAmount();

        if (expectedTotalAmount == null) {
            return new ValidationResult(
                    false,
                    "Expected total cheque amount is missing. Please enter the expected amount.");
        }

        BigDecimal batchTotalAmount =
                batch.getActualTotalAmount();

        if (batchTotalAmount == null) {
            return new ValidationResult(
                    false,
                    "Total cheque amount is missing from the batch information.");
        }

        /*
         * Calculate total amount from cheque list
         */
        BigDecimal actualTotalAmount = BigDecimal.ZERO;

        for (ScanCheque cheque : chequeList) {

            if (cheque == null) {
                return new ValidationResult(
                        false,
                        "Invalid cheque information found. Please check the uploaded file.");
            }

            BigDecimal chequeAmount =
                    cheque.getChequeAmount();

            if (chequeAmount == null) {
                return new ValidationResult(
                        false,
                        "Cheque amount is missing for cheque "
                                + cheque.getScannedChequeId()
                                + ". Please check the cheque details.");
            }

            actualTotalAmount =
                    actualTotalAmount.add(chequeAmount);
        }

        /*
         * Expected amount vs Batch Info amount
         */
        if (expectedTotalAmount.compareTo(batchTotalAmount) != 0) {

            return new ValidationResult(
                    false,
                    "Total amount mismatch. Expected total amount is "
                            + expectedTotalAmount
                            + ", but batch information contains "
                            + batchTotalAmount
                            + ".");
        }

        /*
         * Expected amount vs actual cheque list total
         */
        if (expectedTotalAmount.compareTo(actualTotalAmount) != 0) {

            return new ValidationResult(
                    false,
                    "Total amount mismatch. Expected total amount is "
                            + expectedTotalAmount
                            + ", but the total of cheque amounts is "
                            + actualTotalAmount
                            + ".");
        }

        /*
         * Batch Info amount vs actual cheque list total
         */
        if (batchTotalAmount.compareTo(actualTotalAmount) != 0) {

            return new ValidationResult(
                    false,
                    "Total amount mismatch. Batch information contains "
                            + batchTotalAmount
                            + ", but the total of cheque amounts is "
                            + actualTotalAmount
                            + ".");
        }

        return new ValidationResult(true, null);
    }
}
