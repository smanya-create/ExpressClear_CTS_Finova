package com.iispl.cts.serviceimpl.outward;

import java.sql.SQLException;
import java.util.List;

import com.iispl.cts.dao.outward.OutwardCheckerQueueDAO;
import com.iispl.cts.daoimpl.outward.OutwardCheckerQueueDAOImpl;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.OutwardChequeImage;
import com.iispl.cts.entity.outward.SendBackReason;
import com.iispl.cts.service.outward.OutwardCheckerQueueService;

public class OutwardCheckerQueueServiceImpl implements OutwardCheckerQueueService {
	
	private OutwardCheckerQueueDAO outwardCheckerQueueDAO;

    public OutwardCheckerQueueServiceImpl() {
    	outwardCheckerQueueDAO = new OutwardCheckerQueueDAOImpl();
    }


	@Override
	public List<OutwardCheque> getChequesByBatchId(String batchId) throws SQLException {
		// TODO Auto-generated method stub
		return outwardCheckerQueueDAO.getChequesByBatchId(batchId);
	}

	@Override
	public void verifyCheque(String chequeNo) throws SQLException {
		// TODO Auto-generated method stub
		outwardCheckerQueueDAO.updateChequeStatus(chequeNo, "VERIFIED");

	}

	@Override
	public void returnChequeToMaker(String chequeNo) throws SQLException {
		// TODO Auto-generated method stub
		outwardCheckerQueueDAO.updateChequeStatus(chequeNo,"RETURN_TO_MAKER");

	}

	@Override
	public void rejectCheque(String chequeNo) throws SQLException {
		// TODO Auto-generated method stub
		outwardCheckerQueueDAO.updateChequeStatus(chequeNo,"REJECTED");
		
	}


	@Override
	public List<OutwardChequeImage> getImagesByChequeId(String outwardChequeId) throws Exception {
		// TODO Auto-generated method stub

        return outwardCheckerQueueDAO.getImagesByChequeId(outwardChequeId);
	}
	
	@Override
	public List<SendBackReason> getSendBackReasons() throws SQLException {

	    return outwardCheckerQueueDAO.getSendBackReasons();
	}

}
