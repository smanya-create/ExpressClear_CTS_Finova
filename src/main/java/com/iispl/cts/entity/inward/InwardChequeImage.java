package com.iispl.cts.entity.inward;

import java.io.Serializable;
import java.sql.Timestamp;

public class InwardChequeImage implements Serializable {

    private static final long serialVersionUID = 1L;

    private String inwardImageId;        // character varying(12) - 'IMG...'
    private String inwardChequeId;       // character varying(10) -> fk to inward_cheque
    private String imageType;            // character varying(10) - 'FRONT' or 'BACK'
    private String imagePath;            // character varying(255)
    private Timestamp createdAt;         // timestamp without time zone

    public InwardChequeImage() {
    }

    public InwardChequeImage(String inwardImageId, String inwardChequeId, String imageType, String imagePath) {
        this.inwardImageId = inwardImageId;
        this.inwardChequeId = inwardChequeId;
        this.imageType = imageType;
        this.imagePath = imagePath;
    }

    // Getters and Setters
    public String getInwardImageId() {
        return inwardImageId;
    }

    public void setInwardImageId(String inwardImageId) {
        this.inwardImageId = inwardImageId;
    }

    public String getInwardChequeId() {
        return inwardChequeId;
    }

    public void setInwardChequeId(String inwardChequeId) {
        this.inwardChequeId = inwardChequeId;
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
}