package com.iispl.cts.service.outward;

import java.util.List;

import com.iispl.cts.entity.outward.OutwardCheque;

public interface OutwardChequeService{
	List<OutwardCheque> getChequesByBatchId(String outwardBatchId);
	
}