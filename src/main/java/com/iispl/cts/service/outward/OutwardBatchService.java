package com.iispl.cts.service.outward;

import java.util.List;

import com.iispl.cts.entity.outward.OutwardBatch;

public interface OutwardBatchService{
	 List<OutwardBatch> getVerifiedBatches();
}
