package com.iispl.cts.entity.outward;

import java.io.Serializable;
import java.sql.Timestamp;

public class OutwardChequeImage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String outwardImageId;
    private String outwardChequeId;
    private String imageType;
    private String imagePath;
    private Timestamp createdAt;

    public OutwardChequeImage() {
    }

    public OutwardChequeImage(String outwardImageId, String outwardChequeId, String imageType, 
                              String imagePath, Timestamp createdAt) {
        this.outwardImageId = outwardImageId;
        this.outwardChequeId = outwardChequeId;
        this.imageType = imageType;
        this.imagePath = imagePath;
        this.createdAt = createdAt;
    }

    public String getOutwardImageId() { return outwardImageId; }
    public void setOutwardImageId(String outwardImageId) { this.outwardImageId = outwardImageId; }

    public String getOutwardChequeId() { return outwardChequeId; }
    public void setOutwardChequeId(String outwardChequeId) { this.outwardChequeId = outwardChequeId; }

    public String getImageType() { return imageType; }
    public void setImageType(String imageType) { this.imageType = imageType; }

    public String getImagePath() { return imagePath; }
    public void setImagePath(String imagePath) { this.imagePath = imagePath; }

    public Timestamp getCreatedAt() { return createdAt; }
    public void setCreatedAt(Timestamp createdAt) { this.createdAt = createdAt; }
}