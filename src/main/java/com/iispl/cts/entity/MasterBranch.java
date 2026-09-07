package com.iispl.cts.entity;

import java.io.Serializable;

public class MasterBranch implements Serializable {

    private static final long serialVersionUID = 1L;

    private String branchId;
    private String bankCode;
    private String branchName;
    private String cityName;
    private String micrCityCode;
    private String micrBranchCode;
    private String fullMicrCode;
    private String ifscCode;

    public MasterBranch() {
    }

    public MasterBranch(String branchId,
                        String bankCode,
                        String branchName,
                        String cityName,
                        String micrCityCode,
                        String micrBranchCode,
                        String fullMicrCode,
                        String ifscCode) {

        this.branchId = branchId;
        this.bankCode = bankCode;
        this.branchName = branchName;
        this.cityName = cityName;
        this.micrCityCode = micrCityCode;
        this.micrBranchCode = micrBranchCode;
        this.fullMicrCode = fullMicrCode;
        this.ifscCode = ifscCode;
    }

    public String getBranchId() {
        return branchId;
    }

    public void setBranchId(String branchId) {
        this.branchId = branchId;
    }

    public String getBankCode() {
        return bankCode;
    }

    public void setBankCode(String bankCode) {
        this.bankCode = bankCode;
    }

    public String getBranchName() {
        return branchName;
    }

    public void setBranchName(String branchName) {
        this.branchName = branchName;
    }

    public String getCityName() {
        return cityName;
    }

    public void setCityName(String cityName) {
        this.cityName = cityName;
    }

    public String getMicrCityCode() {
        return micrCityCode;
    }

    public void setMicrCityCode(String micrCityCode) {
        this.micrCityCode = micrCityCode;
    }

    public String getMicrBranchCode() {
        return micrBranchCode;
    }

    public void setMicrBranchCode(String micrBranchCode) {
        this.micrBranchCode = micrBranchCode;
    }

    public String getFullMicrCode() {
        return fullMicrCode;
    }

    public void setFullMicrCode(String fullMicrCode) {
        this.fullMicrCode = fullMicrCode;
    }

    public String getIfscCode() {
        return ifscCode;
    }

    public void setIfscCode(String ifscCode) {
        this.ifscCode = ifscCode;
    }
}