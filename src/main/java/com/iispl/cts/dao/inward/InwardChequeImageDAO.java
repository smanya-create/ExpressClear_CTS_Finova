package com.iispl.cts.dao.inward;

import com.iispl.cts.entity.inward.InwardChequeImage;

public interface InwardChequeImageDAO {

    InwardChequeImage findFrontImageByChequeId(String inwardChequeId);
}