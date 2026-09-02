package com.iispl.cts.dao.inward;

import java.util.List;

import com.iispl.cts.entity.inward.InwardChequeImage;

public interface InwardChequeImageDAO {

    List<InwardChequeImage> getAllImages();

    InwardChequeImage getImageById(String inwardImageId);

    List<InwardChequeImage> getImagesByChequeId(String inwardChequeId);

    boolean saveImage(InwardChequeImage inwardChequeImage);

    boolean updateImage(InwardChequeImage inwardChequeImage);

    boolean deleteImage(String inwardImageId);
    InwardChequeImage findFrontImageByChequeId(String inwardChequeId);
}