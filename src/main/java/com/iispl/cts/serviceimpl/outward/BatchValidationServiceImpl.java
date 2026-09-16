package com.iispl.cts.serviceimpl.outward;

import java.math.BigDecimal;

import java.util.List;

import com.iispl.cts.dto.BatchValidationData;
import com.iispl.cts.dto.ValidationResult;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.outward.batchvalidator.BatchIdentityValidation;
import com.iispl.cts.outward.batchvalidator.ChequeCountValidation;
import com.iispl.cts.outward.batchvalidator.ChequeDataValidation;
import com.iispl.cts.outward.batchvalidator.DuplicateBatchValidation;
import com.iispl.cts.outward.batchvalidator.DuplicateChequeValidation;
import com.iispl.cts.outward.batchvalidator.TotalAmountValidation;
import com.iispl.cts.outward.batchvalidator.ValidateBatch;
import com.iispl.cts.service.outward.BatchValidationService;
import com.iispl.cts.service.outward.OutwardMakerService;

public class BatchValidationServiceImpl
        implements BatchValidationService {
	private OutwardMakerService outwardMakerService;

    private ValidateBatch duplicateBatchValidation;
    private ValidateBatch chequeDataValidation;
    private ValidateBatch duplicateChequeValidation;
    private ValidateBatch chequeCountValidation;
    private ValidateBatch totalAmountValidation;
    private ValidateBatch batchIdentityValidation;

    public BatchValidationServiceImpl() {

        outwardMakerService =
                new OutwardMakerServiceImpl();

        duplicateBatchValidation =
                new DuplicateBatchValidation();

        chequeDataValidation =
                new ChequeDataValidation();

        duplicateChequeValidation =
                new DuplicateChequeValidation(
                        outwardMakerService);

        chequeCountValidation =
                new ChequeCountValidation();

        totalAmountValidation =
                new TotalAmountValidation();

        batchIdentityValidation =
                new BatchIdentityValidation();
    }

    // =========================================================
    // MAIN BATCH VALIDATION
    // =========================================================
    @Override
    public ValidationResult validateBatch(
            BatchValidationData data) {

        StringBuilder errorMessages =
                new StringBuilder();

        ValidationResult result;

        // =====================================================
        // 1. DUPLICATE BATCH
        // =====================================================

        result =
                duplicateBatchValidation.validate(data);

        if (!result.isValid()) {
            errorMessages.append(result.getMessage());
        }

        // =====================================================
        // 2. CHEQUE DATA
        // =====================================================

        result =
                chequeDataValidation.validate(data);

        if (!result.isValid()) {

            if (errorMessages.length() > 0) {
                errorMessages.append("\n\n");
            }

            errorMessages.append(result.getMessage());
        }

        // =====================================================
        // 3. DUPLICATE CHEQUE
        // =====================================================

        result =
                duplicateChequeValidation.validate(data);

        if (!result.isValid()) {

            if (errorMessages.length() > 0) {
                errorMessages.append("\n\n");
            }

            errorMessages.append(result.getMessage());
        }

        // =====================================================
        // 4. CHEQUE COUNT
        // =====================================================

        result =
                chequeCountValidation.validate(data);

        if (!result.isValid()) {

            if (errorMessages.length() > 0) {
                errorMessages.append("\n\n");
            }

            errorMessages.append(result.getMessage());
        }

        // =====================================================
        // 5. TOTAL AMOUNT
        // =====================================================

        result =
                totalAmountValidation.validate(data);

        if (!result.isValid()) {

            if (errorMessages.length() > 0) {
                errorMessages.append("\n\n");
            }

            errorMessages.append(result.getMessage());
        }

        // =====================================================
        // RETURN ALL VALIDATION ERRORS
        // =====================================================

        if (errorMessages.length() > 0) {

            return new ValidationResult(
                    false,
                    errorMessages.toString());
        }

        // =====================================================
        // ALL VALIDATIONS PASSED
        // =====================================================

        return new ValidationResult(
                true,
                null);
    }
    // =========================================================
    // DUPLICATE CHEQUE
    // =========================================================

    @Override
    public ValidationResult validateDuplicateCheques(
            List<ScanCheque> chequeList) {

        BatchValidationData data =
                new BatchValidationData();

        data.setChequeList(
                chequeList);

        return duplicateChequeValidation.validate(
                data);
    }

    // =========================================================
    // DUPLICATE BATCH
    // =========================================================

    @Override
    public ValidationResult validateDuplicateBatch(
            String batchId) {

        BatchValidationData data =
                new BatchValidationData();

        ScanBatch scanBatch =
                new ScanBatch();

        scanBatch.setScannedBatchId(
                batchId);

        data.setBatch(
                scanBatch);

        return duplicateBatchValidation.validate(
                data);
    }

    // =========================================================
    // CHEQUE COUNT
    // =========================================================

    @Override
    public ValidationResult validateChequeCount(
            ScanBatch scanBatch,
            List<ScanCheque> chequeList,
            Integer expectedTotalCheques) {

        BatchValidationData data =
                new BatchValidationData();

        data.setBatch(
                scanBatch);

        data.setChequeList(
                chequeList);

        data.setExpectedTotalCheques(
                expectedTotalCheques);

        return chequeCountValidation.validate(
                data);
    }

    // =========================================================
    // CHEQUE AMOUNT
    // =========================================================

    @Override
    public ValidationResult validateChequeAmount(
            ScanBatch scanBatch,
            List<ScanCheque> chequeList,
            BigDecimal expectedTotalAmount) {

        BatchValidationData data =
                new BatchValidationData();

        data.setBatch(
                scanBatch);

        data.setChequeList(
                chequeList);

        data.setExpectedTotalAmount(
                expectedTotalAmount);

        return totalAmountValidation.validate(
                data);
    }
}

