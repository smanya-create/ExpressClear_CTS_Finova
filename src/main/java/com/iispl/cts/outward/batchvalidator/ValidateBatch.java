package com.iispl.cts.outward.batchvalidator;

import com.iispl.cts.dto.BatchValidationData;
import com.iispl.cts.dto.ValidationResult;

public interface ValidateBatch {

    ValidationResult validate(BatchValidationData data);
}

