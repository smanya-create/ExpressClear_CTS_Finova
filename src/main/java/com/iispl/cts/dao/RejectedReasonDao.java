package com.iispl.cts.dao;

import java.util.List;

import com.iispl.cts.entity.RejectedResaon;

public  interface RejectedReasonDao {
	
	
   List<RejectedResaon> getAllRejectedReasons();

   
   RejectedResaon findById(String rejectedReasonId);

}
