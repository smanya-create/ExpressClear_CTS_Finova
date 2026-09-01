package com.iispl.cts.dao.outward;

import java.util.List;

import com.iispl.cts.entity.outward.OutwardCheque;

public interface OutwardChequeDAO{
	List<OutwardCheque> getChequesByBatchId(String outwardBatchId);	
}
