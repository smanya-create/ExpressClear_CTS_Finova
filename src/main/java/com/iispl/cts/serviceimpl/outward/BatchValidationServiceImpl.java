package com.iispl.cts.serviceimpl.outward;

import java.math.BigDecimal;
import java.util.List;

import com.iispl.cts.dto.BatchValidationData;
import com.iispl.cts.dto.ValidationResult;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.outward.batchvalidator.ChequeCountValidation;
import com.iispl.cts.outward.batchvalidator.ChequeDataValidation;
import com.iispl.cts.outward.batchvalidator.DuplicateBatchValidation;
import com.iispl.cts.outward.batchvalidator.DuplicateChequeValidation;
import com.iispl.cts.outward.batchvalidator.TotalAmountValidation;
import com.iispl.cts.outward.batchvalidator.ValidateBatch;
import com.iispl.cts.service.outward.BatchValidationService;
import com.iispl.cts.service.outward.OutwardMakerService;

public class BatchValidationServiceImpl implements BatchValidationService {

    private OutwardMakerService outwardMakerService;

    private ValidateBatch duplicateBatchValidation;
    private ValidateBatch chequeDataValidation;
    private ValidateBatch duplicateChequeValidation;
    private ValidateBatch chequeCountValidation;
    private ValidateBatch totalAmountValidation;

    public BatchValidationServiceImpl() {

        outwardMakerService = new OutwardMakerServiceImpl();

        duplicateBatchValidation = new DuplicateBatchValidation();
        chequeDataValidation = new ChequeDataValidation();
        duplicateChequeValidation = new DuplicateChequeValidation(outwardMakerService);
        chequeCountValidation = new ChequeCountValidation();
        totalAmountValidation = new TotalAmountValidation();
    }

    // =========================================================
    // MAIN BATCH VALIDATION
    // =========================================================
    @Override
    public ValidationResult validateBatch(BatchValidationData data) {

        ValidationResult result;

        // 1. DUPLICATE BATCH (Fail-Fast)
        if (duplicateBatchValidation != null) {
            result = duplicateBatchValidation.validate(data);
            if (result != null && !result.isValid()) {
                return result;
            }
        }

        // 2. CHEQUE DATA STRUCTURE (Fail-Fast)
        if (chequeDataValidation != null) {
            result = chequeDataValidation.validate(data);
            if (result != null && !result.isValid()) {
                return result;
            }
        }

        // 3. DUPLICATE CHEQUE (Fail-Fast)
        if (duplicateChequeValidation != null) {
            result = duplicateChequeValidation.validate(data);
            if (result != null && !result.isValid()) {
                return result;
            }
        }

        // 4 & 5. CHEQUE COUNT & TOTAL AMOUNT (Show both if both fail)
        StringBuilder errorMessages = new StringBuilder();

        if (chequeCountValidation != null) {
            ValidationResult countResult = chequeCountValidation.validate(data);
            if (countResult != null && !countResult.isValid()) {
                errorMessages.append(countResult.getMessage());
            }
        }

        if (totalAmountValidation != null) {
            ValidationResult amountResult = totalAmountValidation.validate(data);
            if (amountResult != null && !amountResult.isValid()) {
                if (errorMessages.length() > 0) {
                    errorMessages.append("\n");
                }
                errorMessages.append(amountResult.getMessage());
            }
        }

        if (errorMessages.length() > 0) {
            return new ValidationResult(false, errorMessages.toString());
        }

        return new ValidationResult(true, null);
    }

    // =========================================================
    // INDIVIDUAL HELPER METHODS
    // =========================================================

    @Override
    public ValidationResult validateDuplicateCheques(List<ScanCheque> chequeList) {
        BatchValidationData data = new BatchValidationData();
        data.setChequeList(chequeList);
        return duplicateChequeValidation.validate(data);
    }

    @Override
    public ValidationResult validateDuplicateBatch(String batchId) {
        BatchValidationData data = new BatchValidationData();
        ScanBatch scanBatch = new ScanBatch();
        scanBatch.setScannedBatchId(batchId);
        data.setBatch(scanBatch);
        return duplicateBatchValidation.validate(data);
    }

    @Override
    public ValidationResult validateChequeCount(ScanBatch scanBatch, List<ScanCheque> chequeList, Integer expectedTotalCheques) {
        BatchValidationData data = new BatchValidationData();
        data.setBatch(scanBatch);
        data.setChequeList(chequeList);
        data.setExpectedTotalCheques(expectedTotalCheques);
        return chequeCountValidation.validate(data);
    }

    @Override
    public ValidationResult validateChequeAmount(ScanBatch scanBatch, List<ScanCheque> chequeList, BigDecimal expectedTotalAmount) {
        BatchValidationData data = new BatchValidationData();
        data.setBatch(scanBatch);
        data.setChequeList(chequeList);
        data.setExpectedTotalAmount(expectedTotalAmount);
        return totalAmountValidation.validate(data);
    }
}