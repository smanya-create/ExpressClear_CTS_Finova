package com.iispl.cts.serviceimpl.outward;

import java.sql.Connection;
import java.util.Collections;
import java.util.List;

import com.iispl.cts.dao.outward.OutwardChequeRequestDAO;
import com.iispl.cts.daoimpl.outward.OutwardChequeRequestDAOImpl;
import com.iispl.cts.entity.outward.OutwardChequeRequest;
import com.iispl.cts.service.outward.OutwardChequeRequestService;

public class OutwardChequeRequestServiceImpl implements OutwardChequeRequestService {

	private final OutwardChequeRequestDAO outwardChequeRequestDAO;

	public OutwardChequeRequestServiceImpl() {
		this.outwardChequeRequestDAO = new OutwardChequeRequestDAOImpl();
	}

	public OutwardChequeRequestServiceImpl(OutwardChequeRequestDAO outwardChequeRequestDAO) {

		if (outwardChequeRequestDAO == null) {
			throw new IllegalArgumentException("OutwardChequeRequestDAO cannot be null");
		}

		this.outwardChequeRequestDAO = outwardChequeRequestDAO;
	}

	@Override
	public boolean saveRequest(Connection connection, OutwardChequeRequest request) {

		if (connection == null) {
			throw new IllegalArgumentException("Connection cannot be null");
		}

		validateRequest(request);

		if (outwardChequeRequestDAO.existsByChequeId(connection, request.getChequeId())) {
			return false;
		}

		return outwardChequeRequestDAO.saveRequest(connection, request);
	}

	@Override
	public boolean existsByChequeId(Connection connection, String chequeId) {

		if (connection == null) {
			throw new IllegalArgumentException("Connection cannot be null");
		}

		if (chequeId == null || chequeId.trim().isEmpty()) {
			return false;
		}

		return outwardChequeRequestDAO.existsByChequeId(connection, chequeId.trim());
	}

	@Override
	public boolean existsByChequeId(String chequeId) {

		if (chequeId == null || chequeId.trim().isEmpty()) {
			return false;
		}

		return outwardChequeRequestDAO.existsByChequeId(chequeId.trim());
	}

	@Override
	public OutwardChequeRequest getRequestByChequeId(String chequeId) {

		if (chequeId == null || chequeId.trim().isEmpty()) {
			return null;
		}

		return outwardChequeRequestDAO.getRequestByChequeId(chequeId.trim());
	}

	@Override
	public List<OutwardChequeRequest> getRequestsByBatchId(String batchId) {

		if (batchId == null || batchId.trim().isEmpty()) {
			return Collections.emptyList();
		}

		return outwardChequeRequestDAO.getRequestsByBatchId(batchId.trim());
	}

	@Override
	public OutwardChequeRequest createRequest(String chequeId, String batchId, String reasonId, String reason,
			String remarks) {

		if (chequeId == null || chequeId.trim().isEmpty()) {
			throw new IllegalArgumentException("Cheque ID cannot be null or empty");
		}

		if (batchId == null || batchId.trim().isEmpty()) {
			throw new IllegalArgumentException("Batch ID cannot be null or empty");
		}

		if (remarks == null || remarks.trim().isEmpty()) {
			throw new IllegalArgumentException("Rejection remarks are required");
		}

		OutwardChequeRequest request = new OutwardChequeRequest();

		request.setChequeId(chequeId.trim());
		request.setBatchId(batchId.trim());

		if (reasonId != null && !reasonId.trim().isEmpty()) {
			request.setReasonId(reasonId.trim());
		}

		if (reason != null && !reason.trim().isEmpty()) {
			request.setReason(reason.trim());
		}

		request.setRemarks(remarks.trim());

		return request;
	}

	private void validateRequest(OutwardChequeRequest request) {

		if (request == null) {
			throw new IllegalArgumentException("Outward cheque request cannot be null");
		}

		if (request.getChequeId() == null || request.getChequeId().trim().isEmpty()) {
			throw new IllegalArgumentException("Cheque ID cannot be null or empty");
		}

		if (request.getBatchId() == null || request.getBatchId().trim().isEmpty()) {
			throw new IllegalArgumentException("Batch ID cannot be null or empty");
		}

		if (request.getRemarks() == null || request.getRemarks().trim().isEmpty()) {
			throw new IllegalArgumentException("Rejection remarks are required");
		}
	}
}