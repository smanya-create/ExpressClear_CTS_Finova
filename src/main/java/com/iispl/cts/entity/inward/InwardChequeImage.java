package com.iispl.cts.entity.inward;

import java.io.Serializable;
import java.sql.Timestamp;

public class InwardChequeImage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String inwardImageId;
    private String inwardChequeId;
    private String imageType;
    private String imagePath;
    private Timestamp createdAt;

    public InwardChequeImage() {
    }

    public InwardChequeImage(String inwardImageId, String inwardChequeId, String imageType, 
                             String imagePath, Timestamp createdAt) {
        this.inwardImageId = inwardImageId;
        this.inwardChequeId = inwardChequeId;
        this.imageType = imageType;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
    }

    public String getInwardImageId() { return inwardImageId; }
    public void setInwardImageId(String inwardImageId) { this.inwardImageId = inwardImageId; }

    public String getInwardChequeId() { return inwardChequeId; }
    public void setInwardChequeId(String inwardChequeId) { this.inwardChequeId = inwardChequeId; }

    public String getImageType() { return imageType; }
    public void setImageType(String imageType) { this.imageType = imageType; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}