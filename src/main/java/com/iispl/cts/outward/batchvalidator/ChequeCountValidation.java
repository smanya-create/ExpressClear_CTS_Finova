
package com.iispl.cts.outward.batchvalidator;

import java.util.List;

import com.iispl.cts.dto.BatchValidationData;
import com.iispl.cts.dto.ValidationResult;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;

public class ChequeCountValidation implements ValidateBatch {

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

        Integer expectedTotalCheques =
                data.getExpectedTotalCheques();

        if (expectedTotalCheques == null) {
            return new ValidationResult(
                    false,
                    "Expected total cheque count is missing. Please enter the expected cheque count.");
        }

        int expectedCount = expectedTotalCheques.intValue();

        int batchCount = batch.getActualChequeCount();

        int actualChequeCount = chequeList.size();

        /*
         * Expected count vs Batch Info count
         */
        if (expectedCount != batchCount) {

            return new ValidationResult(
                    false,
                    "Cheque count mismatch. Expected cheque count is "
                            + expectedCount
                            + ", but batch information contains "
                            + batchCount
                            + " cheques.");
        }

        /*
         * Expected count vs actual cheque list count
         */
        if (expectedCount != actualChequeCount) {

            return new ValidationResult(
                    false,
                    "Cheque count mismatch. Expected cheque count is "
                            + expectedCount
                            + ", but "
                            + actualChequeCount
                            + " cheque records were found in the uploaded file.");
        }

        /*
         * Batch Info count vs actual cheque list count
         */
        if (batchCount != actualChequeCount) {

            return new ValidationResult(
                    false,
                    "Cheque count mismatch. Batch information contains "
                            + batchCount
                            + " cheques, but "
                            + actualChequeCount
                            + " cheque records were found in the uploaded file.");
        }

        return new ValidationResult(true, null);
    }
}
