package com.iispl.cts.validatorimpl;



import com.iispl.cts.validator.MICRValidator;

public class MICRValidatorImpl implements MICRValidator {

    @Override
    public boolean isValid(String micrCode) {

        if (micrCode == null) {
            return false;
        }

        micrCode = micrCode.trim();

        return micrCode.matches("\\d{9}");
    }
}