package com.iispl.cts.service.outward;

import java.sql.SQLException;
import java.util.List;


import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.OutwardChequeImage;

public interface OutwardCheckerQueueService {
	
	List<OutwardCheque> getChequesByBatchId(String batchId)
            throws SQLException;
	
	List<OutwardChequeImage> getImagesByChequeId(String outwardChequeId) 
				throws Exception;

    void verifyCheque(String chequeNo)
            throws SQLException;

    void returnChequeToMaker(String chequeNo)
            throws SQLException;

    void rejectCheque(String chequeNo)
            throws SQLException;


}
