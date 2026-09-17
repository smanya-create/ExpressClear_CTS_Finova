package com.iispl.cts.dao.outward;

import java.sql.Connection;
import java.util.List;

import com.iispl.cts.entity.outward.OutwardChequeRequest;

public interface OutwardChequeRequestDAO {

	boolean saveRequest(Connection connection, OutwardChequeRequest request);

	boolean existsByChequeId(Connection connection, String chequeId);

	boolean existsByChequeId(String chequeId);

	OutwardChequeRequest getRequestByChequeId(String chequeId);

	List<OutwardChequeRequest> getRequestsByBatchId(String batchId);

	boolean saveRejectionRequest(Connection connection, String chequeId, String batchId, String remarks,
			String reasonId, String reason);
}