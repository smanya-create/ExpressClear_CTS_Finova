package com.iispl.cts.service.outward;

import java.sql.Connection;
import java.util.List;

import com.iispl.cts.entity.outward.OutwardChequeRequest;

public interface OutwardChequeRequestService {

	boolean saveRequest(Connection connection, OutwardChequeRequest request);

	boolean existsByChequeId(Connection connection, String chequeId);

	boolean existsByChequeId(String chequeId);

	OutwardChequeRequest getRequestByChequeId(String chequeId);

	List<OutwardChequeRequest> getRequestsByBatchId(String batchId);

	OutwardChequeRequest createRequest(String chequeId, String batchId, String reasonId, String reason, String remarks);
}