package com.iispl.cts.outward.batchvalidator;

import com.iispl.cts.dto.BatchValidationData;
import com.iispl.cts.dto.ValidationResult;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.service.outward.ScanService;
import com.iispl.cts.serviceimpl.outward.ScanServiceImpl;

public class DuplicateBatchValidation implements ValidateBatch {

    private ScanService scanService;

    public DuplicateBatchValidation() {
        this.scanService = new ScanServiceImpl();
    }

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

        String batchId = batch.getScannedBatchId();

        if (batchId == null || batchId.trim().isEmpty()) {
            return new ValidationResult(
                    false,
                    "Batch number is missing. Please check the uploaded file.");
        }

        batchId = batchId.trim();

        try {

            ScanBatch existingBatch =
                    scanService.getBatchById(batchId);

            if (existingBatch != null) {

                return new ValidationResult(
                        false,
                        "Batch " + batchId
                                + " has already been uploaded. Please check the batch details.");
            }

            return new ValidationResult(true, null);

        } catch (Exception e) {

            e.printStackTrace();

            return new ValidationResult(
                    false,
                    "Unable to check the batch details at the moment. Please try again.");
        }
    }
}

