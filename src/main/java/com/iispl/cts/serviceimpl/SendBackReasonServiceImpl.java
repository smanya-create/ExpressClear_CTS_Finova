package com.iispl.cts.serviceimpl;

import java.util.ArrayList;
import java.util.List;

import com.iispl.cts.dao.SendBackReasonDAO;
import com.iispl.cts.daoimpl.SendBackReasonDAOImpl;
import com.iispl.cts.entity.SendBackReason;
import com.iispl.cts.service.SendBackReasonService;

public class SendBackReasonServiceImpl implements SendBackReasonService {

    private final SendBackReasonDAO reasonDao = new SendBackReasonDAOImpl();

 // In-memory cache: queries the database only on the first access
    private static List<SendBackReason> cachedReasons = null;

    @Override
    public List<SendBackReason> getAllSendBackReasons() {
        if (cachedReasons == null) {
            cachedReasons = reasonDao.getAllSendBackReasons();
            if (cachedReasons == null) {
                cachedReasons = new ArrayList<>();
            }
        }
        return cachedReasons;
    }
}