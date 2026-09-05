package com.iispl.cts.serviceimpl;

import java.util.List;

import com.iispl.cts.dao.RejectedReasonDao;
import com.iispl.cts.daoimpl.RejectedReasonDaoImpl;
import com.iispl.cts.entity.RejectedReason;
import com.iispl.cts.service.RejectedReasonService;

public class RejectedReasonServiceImpl implements RejectedReasonService {

	  private static RejectedReasonServiceImpl instance;

	    private final RejectedReasonDao rejectedReasonDao;

	    private RejectedReasonServiceImpl() {
	        rejectedReasonDao = RejectedReasonDaoImpl.getInstance();
	    }

	    public static synchronized RejectedReasonServiceImpl getInstance() {
	        if (instance == null) {
	            instance = new RejectedReasonServiceImpl();
	        }
	        return instance;
	    }

	    @Override
	    public List<RejectedReason> getAllRejectedReasons() {
	        return rejectedReasonDao.getAllRejectedReasons();
	    }

}
