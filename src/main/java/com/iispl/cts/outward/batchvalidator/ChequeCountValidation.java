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

        Integer expectedTotalCheques = data.getExpectedTotalCheques();

        if (expectedTotalCheques == null) {
            return new ValidationResult(
                    false,
                    "Cheque count mismatch.");
        }

        int expectedCount = expectedTotalCheques.intValue();
        int batchCount = batch.getActualChequeCount();
        int actualChequeCount = chequeList.size();

        /*
         * Check for any count mismatch across expected, header, or actual list count
         */
        if (expectedCount != batchCount 
                || expectedCount != actualChequeCount 
                || batchCount != actualChequeCount) {

            return new ValidationResult(
                    false,
                    "Cheque count mismatch.");
        }

        return new ValidationResult(true, null);
    }
}