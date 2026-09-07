package com.iispl.cts.dao;

import java.util.List;

import com.iispl.cts.entity.RejectedReason;

public  interface RejectedReasonDao {
	
	
   List<RejectedReason> getAllRejectedReasons();

   
   RejectedReason findById(String rejectedReasonId);

}
