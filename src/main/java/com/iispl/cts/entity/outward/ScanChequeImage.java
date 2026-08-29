package com.iispl.cts.entity.outward;


import java.io.Serializable;
import java.sql.Timestamp;

public class ScanChequeImage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String scannedImageId;
    private String scannedChequeId;
    private String imageType; // FRONT or BACK
    private String imagePath;
    private Timestamp createdAt;

    public ScanChequeImage() {
    }

    public ScanChequeImage(String scannedImageId, String scannedChequeId, String imageType, String imagePath, Timestamp createdAt) {
        this.scannedImageId = scannedImageId;
        this.scannedChequeId = scannedChequeId;
        this.imageType = imageType;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
    }

	public String getScannedImageId() {
		return scannedImageId;
	}

	public void setScannedImageId(String scannedImageId) {
		this.scannedImageId = scannedImageId;
	}

	public String getScannedChequeId() {
		return scannedChequeId;
	}

	public void setScannedChequeId(String scannedChequeId) {
		this.scannedChequeId = scannedChequeId;
	}

	public String getImageType() {
		return imageType;
	}

	public void setImageType(String imageType) {
		this.imageType = imageType;
	}

	public String getImagePath() {
		return imagePath;
	}

	public void setImagePath(String imagePath) {
		this.imagePath = imagePath;
	}

	public Timestamp getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(Timestamp createdAt) {
		this.createdAt = createdAt;
	}

	public static long getSerialversionuid() {
		return serialVersionUID;
	}

    
}
