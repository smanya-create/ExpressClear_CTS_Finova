package com.iispl.cts.dao.outward;

import java.sql.SQLException;
import java.util.List;

import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.OutwardChequeImage;



public interface OutwardCheckerQueueDAO {
	
	  List<OutwardCheque> getChequesByBatchId(String batchId)
	            throws SQLException;

	    void updateChequeStatus(String chequeNo, String status)
	            throws SQLException;
	    
	    List<OutwardChequeImage> getImagesByChequeId(String outwardChequeId)
	            throws SQLException;

}
