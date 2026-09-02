package com.iispl.cts.service.inward;

import java.util.List;

import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.serviceimpl.inward.InwardBatchXmlParser.ParsedBatchData;

public interface InwardBatchService {

    List<InwardBatch> getAllBatches();

    InwardBatch getBatchById(String inwardBatchId);

    boolean saveBatch(InwardBatch inwardBatch);

    boolean updateBatch(InwardBatch inwardBatch);

    boolean deleteBatch(String inwardBatchId);

    ParsedBatchData parseBatchXml(String xmlFilePath) throws Exception;
    
    boolean saveParsedBatch(ParsedBatchData parsedBatchData);
import com.iispl.cts.entity.inward.InwardBatch;

public interface InwardBatchService {
    List<InwardBatch> getAllActiveBatches();
    InwardBatch getBatchById(String batchId);
    boolean updateBatchStatus(String batchId, String status);
}