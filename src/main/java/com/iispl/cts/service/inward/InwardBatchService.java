package com.iispl.cts.service.inward;

import java.util.List;

import com.iispl.cts.dto.DashboardSummaryDTO;
import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.parser.InwardBatchXmlParser;
import com.iispl.cts.parser.InwardBatchXmlParser.ParsedBatchData;

public interface InwardBatchService {

	List<InwardBatch> getAllBatches();

	boolean saveBatch(InwardBatch inwardBatch);

	boolean updateBatch(InwardBatch inwardBatch);

	boolean deleteBatch(String inwardBatchId);

	ParsedBatchData parseBatchXml(String npciXmlPath, String ocrXmlPath) throws Exception;

	boolean saveParsedBatch(ParsedBatchData parsedBatchData);

	List<InwardBatch> getAllActiveBatches();

	InwardBatch getBatchById(String batchId);

	boolean updateBatchStatus(String batchId, String status);

	List<DashboardSummaryDTO> getDashboardBatches();
}