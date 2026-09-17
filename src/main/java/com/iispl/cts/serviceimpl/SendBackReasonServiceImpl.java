package com.iispl.cts.serviceimpl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.iispl.cts.dao.SendBackReasonDAO;
import com.iispl.cts.daoimpl.SendBackReasonDAOImpl;
import com.iispl.cts.entity.SendBackReason;
import com.iispl.cts.service.SendBackReasonService;

public class SendBackReasonServiceImpl implements SendBackReasonService {

	private final SendBackReasonDAO reasonDao;

	private static List<SendBackReason> cachedReasons;

	public SendBackReasonServiceImpl() {
		this.reasonDao = new SendBackReasonDAOImpl();
	}

	@Override
	public List<SendBackReason> getAllSendBackReasons() {

		if (cachedReasons == null) {

			List<SendBackReason> reasons = reasonDao.getAllSendBackReasons();

			if (reasons == null) {
				reasons = new ArrayList<>();
			}

			cachedReasons = new ArrayList<>(reasons);
		}

		return Collections.unmodifiableList(cachedReasons);
	}
}