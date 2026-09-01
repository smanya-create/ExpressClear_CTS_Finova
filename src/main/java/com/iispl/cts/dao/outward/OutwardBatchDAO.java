package com.iispl.cts.dao.outward;

import java.util.List;

import com.iispl.cts.entity.outward.OutwardBatch;

public interface OutwardBatchDAO {
	List<OutwardBatch> getVerifiedBatches();
}