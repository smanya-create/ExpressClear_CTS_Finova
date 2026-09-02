package com.iispl.cts.service.outward;
import java.math.BigDecimal;

import java.util.List;

import com.iispl.cts.entity.outward.OutwardCheque;
public interface OutwardChequeService {

	List<OutwardCheque> getChequesByBatchId(String outwardBatchId);

	int getTotalChequeCountByBatchId(String outwardBatchId);

	BigDecimal getTotalChequeAmountByBatchId(String outwardBatchId);

}