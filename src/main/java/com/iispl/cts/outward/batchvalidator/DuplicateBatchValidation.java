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
                    "Invalid batch details found.");
        }

        ScanBatch batch = data.getBatch();

        if (batch == null) {
            return new ValidationResult(
                    false,
                    "Invalid batch details found.");
        }

        String batchId = batch.getScannedBatchId();

        if (batchId == null || batchId.trim().isEmpty()) {
            return new ValidationResult(
                    false,
                    "Invalid batch details found.");
        }

        batchId = batchId.trim();

        try {

            ScanBatch existingBatch = scanService.getBatchById(batchId);

            if (existingBatch != null) {
                return new ValidationResult(
                        false,
                        "Batch already exists.");
            }

            return new ValidationResult(true, null);

        } catch (Exception e) {

            e.printStackTrace();

            return new ValidationResult(
                    false,
                    "Unable to check batch details. Please try again.");
        }
    }
}