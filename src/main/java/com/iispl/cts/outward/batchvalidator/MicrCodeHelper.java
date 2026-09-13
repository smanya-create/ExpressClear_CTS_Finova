package com.iispl.cts.outward.batchvalidator;

import java.util.List;

import com.iispl.cts.entity.outward.ScanCheque;

public class MicrCodeHelper {

    public List<ScanCheque> checkMicrCode(List<ScanCheque> chequeList) {

        if (chequeList == null || chequeList.isEmpty()) {
            return chequeList;
        }

        for (ScanCheque cheque : chequeList) {

            if (cheque == null) {
                continue;
            }

            String cityCode = cheque.getCityCode();
            String bankCode = cheque.getBankCode();
            String branchCode = cheque.getBranchCode();
            String fullMicr = cheque.getMicrCode();

            boolean micrError = false;

            if (cityCode == null || cityCode.trim().isEmpty()
                    || bankCode == null || bankCode.trim().isEmpty()
                    || branchCode == null || branchCode.trim().isEmpty()
                    || fullMicr == null || fullMicr.trim().isEmpty()) {

                micrError = true;
            }

            if (!micrError) {

                cityCode = cityCode.trim();
                bankCode = bankCode.trim();
                branchCode = branchCode.trim();
                fullMicr = fullMicr.trim();

                // Alphanumeric characters are allowed.
                // Special characters are not allowed.
                if (!cityCode.matches("[a-zA-Z0-9]+")
                        || !bankCode.matches("[a-zA-Z0-9]+")
                        || !branchCode.matches("[a-zA-Z0-9]+")
                        || !fullMicr.matches("[a-zA-Z0-9]+")) {

                    micrError = true;
                }

                // All-zero MICR codes are not allowed.
                if ("000".equals(cityCode)
                        || "000".equals(bankCode)
                        || "000".equals(branchCode)
                        || "000000000".equals(fullMicr)) {

                    micrError = true;
                }

                // Full MICR must be CityCode + BankCode + BranchCode.
                String expectedFullMicr =
                        cityCode + bankCode + branchCode;

                if (!fullMicr.equals(expectedFullMicr)) {
                    micrError = true;
                }
            }

            if (micrError) {
                cheque.setChequeStatus("PENDING_MICR_REPAIR");
            }
        }

        return chequeList;
    }
}