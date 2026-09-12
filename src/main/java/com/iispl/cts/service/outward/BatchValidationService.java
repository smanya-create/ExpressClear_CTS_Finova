package com.iispl.cts.service.outward;

import java.math.BigDecimal;
import java.util.List;

import com.iispl.cts.dto.BatchValidationData;
import com.iispl.cts.dto.ValidationResult;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;

public interface BatchValidationService {

    ValidationResult validateBatch(
            BatchValidationData data);

    ValidationResult validateDuplicateCheques(
            List<ScanCheque> chequeList);

    ValidationResult validateDuplicateBatch(
            String batchId);

    ValidationResult validateChequeCount(
            ScanBatch scanBatch,
            List<ScanCheque> chequeList,
            Integer expectedTotalCheques);

    ValidationResult validateChequeAmount(
            ScanBatch scanBatch,
            List<ScanCheque> chequeList,
            BigDecimal expectedTotalAmount);
}
