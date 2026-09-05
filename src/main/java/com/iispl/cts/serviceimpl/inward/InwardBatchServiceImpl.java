package com.iispl.cts.serviceimpl.inward;

import java.util.List;

import com.iispl.cts.dao.inward.InwardBatchDAO;
import com.iispl.cts.dao.inward.InwardChequeDAO;
import com.iispl.cts.dao.inward.InwardChequeImageDAO;
import com.iispl.cts.daoimpl.inward.InwardBatchDAOImpl;
import com.iispl.cts.daoimpl.inward.InwardChequeDAOImpl;
import com.iispl.cts.daoimpl.inward.InwardChequeImageDAOImpl;
import com.iispl.cts.dto.DashboardSummaryDTO;
import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.entity.inward.InwardChequeImage;
import com.iispl.cts.parser.InwardBatchXmlParser;
import com.iispl.cts.parser.InwardBatchXmlParser.ParsedBatchData;
import com.iispl.cts.service.inward.InwardBatchService;

public class InwardBatchServiceImpl implements InwardBatchService {

	private final InwardBatchDAO inwardBatchDAO;
	private final InwardChequeDAO inwardChequeDAO;
	private final InwardChequeImageDAO inwardChequeImageDAO;
	private final InwardBatchXmlParser xmlParser;

	public InwardBatchServiceImpl() {
		this.inwardBatchDAO = new InwardBatchDAOImpl();
		this.inwardChequeDAO = InwardChequeDAOImpl.getInstance();
		this.inwardChequeImageDAO = InwardChequeImageDAOImpl.getInstance();
		this.xmlParser = new InwardBatchXmlParser();
	}

	@Override
	public List<InwardBatch> getAllActiveBatches() {
		return inwardBatchDAO.findAllActiveBatches();
	}

	@Override
	public InwardBatch getBatchById(String batchId) {
		return inwardBatchDAO.findById(batchId);
	}

	@Override
	public boolean updateBatchStatus(String batchId, String status) {
		return inwardBatchDAO.updateStatus(batchId, status);
	}

	@Override
	public ParsedBatchData parseBatchXml(
	        String npciXmlPath,
	        String ocrXmlPath) throws Exception {

	    return xmlParser.parse(
	            npciXmlPath,
	            ocrXmlPath);
	}

	@Override
	public List<InwardBatch> getAllBatches() {
		return inwardBatchDAO.getAllBatches();
	}

	@Override
	public boolean saveBatch(InwardBatch inwardBatch) {
		return inwardBatchDAO.saveBatch(inwardBatch);
	}

	@Override
	public boolean updateBatch(InwardBatch inwardBatch) {
		return inwardBatchDAO.updateBatch(inwardBatch);
	}

	@Override
	public boolean deleteBatch(String inwardBatchId) {
		return inwardBatchDAO.deleteBatch(inwardBatchId);
	}

	@Override
	public boolean saveParsedBatch(ParsedBatchData parsedBatchData) {

		if (parsedBatchData == null) {
			throw new RuntimeException("Parsed batch data is null.");
		}

		InwardBatch inwardBatch = parsedBatchData.getInwardBatch();
		List<InwardCheque> inwardCheques = parsedBatchData.getInwardCheques();
		List<InwardChequeImage> inwardChequeImages = parsedBatchData.getInwardChequeImages();

		if (inwardBatch == null) {
			throw new RuntimeException("Inward batch data is missing.");
		}

		String batchId = inwardBatch.getInwardBatchId();

		if (batchId == null || batchId.trim().isEmpty()) {
			throw new RuntimeException("Batch ID is missing.");
		}

		InwardBatch existingBatch = inwardBatchDAO.getBatchById(batchId);

		if (existingBatch != null) {
			throw new RuntimeException("Batch " + batchId + " already exists in the database.");
		}

		inwardBatch.setBatchStatus("Validated");

		if (!inwardBatchDAO.saveBatch(inwardBatch)) {
			throw new RuntimeException("Failed to save batch " + batchId);
		}

		for (InwardCheque inwardCheque : inwardCheques) {

			if (!inwardChequeDAO.saveCheque(inwardCheque)) {
				throw new RuntimeException(
						"Failed to save cheque " + inwardCheque.getInwardChequeId() + " for batch " + batchId);
			}
		}

		for (InwardChequeImage inwardChequeImage : inwardChequeImages) {

			if (inwardChequeImage.getInwardImageId() == null || inwardChequeImage.getInwardImageId().trim().isEmpty()) {

				throw new RuntimeException("Image ID is missing for cheque " + inwardChequeImage.getInwardChequeId());
			}

			if (inwardChequeImage.getInwardChequeId() == null
					|| inwardChequeImage.getInwardChequeId().trim().isEmpty()) {

				throw new RuntimeException("Cheque ID is missing for image " + inwardChequeImage.getInwardImageId());
			}

			if (!inwardChequeImageDAO.saveImage(inwardChequeImage)) {
				throw new RuntimeException("Failed to save image " + inwardChequeImage.getInwardImageId()
						+ " for cheque " + inwardChequeImage.getInwardChequeId());
			}
		}

		return true;
	}

	@Override
	public List<DashboardSummaryDTO> getDashboardBatches() {
		return inwardBatchDAO.getDashboardBatches();
	}
}