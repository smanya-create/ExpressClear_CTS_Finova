package com.iispl.cts.controller.outward.maker;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Vlayout;

import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.service.outward.OutwardBatchService;
import com.iispl.cts.service.outward.OutwardChequeService;
import com.iispl.cts.service.outward.ScanService;
import com.iispl.cts.serviceimpl.outward.OutwardBatchServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardChequeServiceImpl;
import com.iispl.cts.serviceimpl.outward.ScanServiceImpl;

public class OutwardDataEntryController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;
	private static final int PAGE_SIZE = 5;

	private static final String STATUS_PENDING_MAKER_PROCESS = "PENDING_MAKER_PROCESS";
	private static final String STATUS_PENDING_DATA_ENTRY = "PENDING_DATA_ENTRY";
	private static final String STATUS_PENDING_MICR_REPAIR = "PENDING_MICR_REPAIR";
	private static final String STATUS_MICR_REJECTED = "MICR_REJECTED";
	private static final String STATUS_PENDING_VERIFICATION = "PENDING_VERIFICATION";
	private static final String STATUS_PENDING_CHECKER_PROCESS = "PENDING_CHECKER_PROCESS";
	private static final String STATUS_REJECTION_REQUEST = "REJECTION_REQUEST";
	private static final String STATUS_REJECTION_REJECT = "REJECTION_REJECT";
	private static final String STATUS_ON_HOLD = "ON_HOLD";

	private static final String MODE_DATA_ENTRY = "DATA_ENTRY";
	private static final String RETURN_FROM_CHECKER = "RETURN_FROM_CHECKER";
	private static final String CHEQUE_DATA_ENTRY_ZUL = "/outward/maker/cheque-data-entry.zul";

	private Rows outwardDataEntryRowsBatch;
	private Vlayout outwardDataEntryEmptyState;
	private Label outwardDataEntryLblCurrentPage;
	private Label outwardDataEntryLblBatchCount;

	private Button outwardDataEntryBtnFirst;
	private Button outwardDataEntryBtnPrevious;
	private Button outwardDataEntryBtnNext;
	private Button outwardDataEntryBtnLast;

	private final ScanService scanService;
	private final OutwardBatchService outwardBatchService;
	private final OutwardChequeService outwardChequeService;

	private List<OutwardBatch> outwardDataEntryBatchList;
	private int outwardDataEntryCurrentPage = 1;

	public OutwardDataEntryController() {
		scanService = new ScanServiceImpl();
		outwardBatchService = new OutwardBatchServiceImpl();
		outwardChequeService = new OutwardChequeServiceImpl();
		outwardDataEntryBatchList = new ArrayList<>();
	}

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		outwardDataEntryRowsBatch = (Rows) comp.getFellow("outwardDataEntryRowsBatch");
		outwardDataEntryEmptyState = (Vlayout) comp.getFellow("outwardDataEntryEmptyState");

		outwardDataEntryLblCurrentPage = (Label) comp.getFellow("outwardDataEntryLblCurrentPage");
		outwardDataEntryLblBatchCount = (Label) comp.getFellow("outwardDataEntryLblBatchCount");

		outwardDataEntryBtnFirst = (Button) comp.getFellow("outwardDataEntryBtnFirst");
		outwardDataEntryBtnPrevious = (Button) comp.getFellow("outwardDataEntryBtnPrevious");
		outwardDataEntryBtnNext = (Button) comp.getFellow("outwardDataEntryBtnNext");
		outwardDataEntryBtnLast = (Button) comp.getFellow("outwardDataEntryBtnLast");

		bindPaginationEvents();
		loadBatches();
	}

	private void loadBatches() {
		outwardDataEntryBatchList = new ArrayList<>();
		loadInitialDataEntryBatches();
		loadExistingDataEntryBatches();

		outwardDataEntryCurrentPage = 1;
		updateBatchCount();
		renderCurrentPage();
	}

	private void loadInitialDataEntryBatches() {
		List<ScanBatch> scanBatches = scanService.getMakerDashboardBatches();
		if (scanBatches == null || scanBatches.isEmpty())
			return;

		for (ScanBatch scanBatch : scanBatches) {
			if (scanBatch == null)
				continue;

			String scannedBatchId = safeValue(scanBatch.getScannedBatchId());
			if (scannedBatchId.isEmpty())
				continue;

			List<ScanCheque> scanCheques = scanService.getChequesByBatchId(scannedBatchId);
			if (scanCheques == null)
				scanCheques = new ArrayList<>();

			String outwardBatchId = getOutwardBatchId(scannedBatchId);
			if (outwardBatchId.isEmpty())
				outwardBatchId = scannedBatchId;

			int totalCheques = scanBatch.getActualChequeCount();
			if (totalCheques <= 0)
				totalCheques = scanCheques.size();
			if (totalCheques <= 0)
				continue;

			OutwardBatch batch = new OutwardBatch();
			batch.setOutwardBatchId(outwardBatchId);
			batch.setBatchReferenceId(scanBatch.getBatchReferenceId());
			batch.setActualChequeCount(totalCheques);
			batch.setActualTotalAmount(scanBatch.getActualTotalAmount());
			batch.setBatchStatus(normalizeStatus(scanBatch.getBatchStatus()));
			batch.setUploadedBy(scanBatch.getUploadedBy());
			batch.setUploadedAt(scanBatch.getUploadedAt());

			if (!containsBatch(outwardBatchId)) {
				outwardDataEntryBatchList.add(batch);
			}
		}
	}

	private void loadExistingDataEntryBatches() {
		List<OutwardBatch> batches = outwardBatchService.getBatchesReadyForDataEntry();
		if (batches == null || batches.isEmpty())
			return;

		for (OutwardBatch batch : batches) {
			if (batch == null)
				continue;

			String batchId = safeValue(batch.getOutwardBatchId());
			if (batchId.isEmpty())
				continue;

			String status = normalizeStatus(batch.getBatchStatus());
			if (!STATUS_PENDING_DATA_ENTRY.equals(status) && !STATUS_ON_HOLD.equals(status)
					&& !STATUS_PENDING_MICR_REPAIR.equals(status) && !STATUS_PENDING_MAKER_PROCESS.equals(status)) {
				continue;
			}

			batch.setBatchStatus(status);
			if (!containsBatch(batchId)) {
				outwardDataEntryBatchList.add(batch);
			}
		}
	}

	private String getOutwardBatchId(String scannedBatchId) {
		if (isBlank(scannedBatchId))
			return "";
		try {
			String outwardBatchId = outwardBatchService.getOutwardBatchIdByScannedBatchId(scannedBatchId);
			return safeValue(outwardBatchId);
		} catch (Exception exception) {
			return "";
		}
	}

	private boolean containsBatch(String batchId) {
		if (isBlank(batchId))
			return false;
		for (OutwardBatch batch : outwardDataEntryBatchList) {
			if (batch == null)
				continue;
			if (safeValue(batch.getOutwardBatchId()).equalsIgnoreCase(batchId.trim())) {
				return true;
			}
		}
		return false;
	}

	private void renderCurrentPage() {
		outwardDataEntryRowsBatch.getChildren().clear();

		if (outwardDataEntryBatchList == null || outwardDataEntryBatchList.isEmpty()) {
			outwardDataEntryEmptyState.setVisible(true);
			updatePagination();
			return;
		}

		outwardDataEntryEmptyState.setVisible(false);
		int totalPages = getTotalPages();
		if (outwardDataEntryCurrentPage > totalPages) {
			outwardDataEntryCurrentPage = totalPages;
		}

		int startIndex = (outwardDataEntryCurrentPage - 1) * PAGE_SIZE;
		int endIndex = Math.min(startIndex + PAGE_SIZE, outwardDataEntryBatchList.size());

		for (int index = startIndex; index < endIndex; index++) {
			OutwardBatch batch = outwardDataEntryBatchList.get(index);
			if (batch != null) {
				createBatchRow(batch);
			}
		}

		updatePagination();
	}

	private void createBatchRow(final OutwardBatch batch) {
		Row row = new Row();
		row.setSclass("outward-data-entry-row");

		String batchId = safeValue(batch.getOutwardBatchId());
		Label batchLabel = new Label(batchId);
		batchLabel.setSclass("outward-data-entry-cell outward-data-entry-batch-id");

		int totalCheques = getTotalChequeCount(batch);
		Label totalLabel = new Label(String.valueOf(totalCheques));
		totalLabel.setSclass("outward-data-entry-cell outward-data-entry-number");

		int completedCheques = getCompletedMakerChequeCount(batch);
		Label progressLabel = new Label(completedCheques + " / " + totalCheques);
		progressLabel.setSclass("outward-data-entry-cell outward-data-entry-progress");

		int micrRejected = getMicrRejectedCount(batch);
		Label micrRejectedLabel = new Label(String.valueOf(micrRejected));
		if (micrRejected > 0) {
			micrRejectedLabel.setSclass(
					"outward-data-entry-cell outward-data-entry-micr-rejected outward-data-entry-micr-rejected-active");
		} else {
			micrRejectedLabel.setSclass("outward-data-entry-cell outward-data-entry-micr-rejected");
		}

		String batchStatus = normalizeStatus(batch.getBatchStatus());
		Label statusLabel = new Label(formatStatus(batchStatus));
		statusLabel.setSclass("outward-data-entry-cell outward-data-entry-status " + getStatusClass(batchStatus));

		Button proceedButton = new Button("Proceed →");
		proceedButton.setSclass("outward-data-entry-proceed-button");
		proceedButton.addEventListener(Events.ON_CLICK, (EventListener<Event>) event -> openChequeDataEntry(batch));

		row.appendChild(batchLabel);
		row.appendChild(totalLabel);
		row.appendChild(progressLabel);
		row.appendChild(micrRejectedLabel);
		row.appendChild(statusLabel);
		row.appendChild(proceedButton);

		outwardDataEntryRowsBatch.appendChild(row);
	}

	private int getTotalChequeCount(OutwardBatch batch) {
		if (batch == null)
			return 0;
		if (batch.getActualChequeCount() > 0)
			return batch.getActualChequeCount();

		String batchId = safeValue(batch.getOutwardBatchId());
		if (batchId.isEmpty())
			return 0;

		List<OutwardCheque> outwardCheques = getOutwardCheques(batchId);
		if (!outwardCheques.isEmpty())
			return outwardCheques.size();

		List<ScanCheque> scanCheques = scanService.getChequesByBatchId(batchId);
		return scanCheques != null ? scanCheques.size() : 0;
	}

	private int getCompletedMakerChequeCount(OutwardBatch batch) {
		if (batch == null)
			return 0;
		String batchId = safeValue(batch.getOutwardBatchId());
		if (batchId.isEmpty())
			return 0;

		try {
			return outwardChequeService.getCompletedMakerChequeCountByBatchId(batchId);
		} catch (Exception ignored) {
			return 0;
		}
	}

	private int getMicrRejectedCount(OutwardBatch batch) {
		if (batch == null)
			return 0;
		String batchId = safeValue(batch.getOutwardBatchId());
		if (batchId.isEmpty())
			return 0;

		List<OutwardCheque> outwardCheques = getOutwardCheques(batchId);
		int rejectedCount = 0;
		for (OutwardCheque cheque : outwardCheques) {
			if (cheque != null && STATUS_MICR_REJECTED.equals(normalizeStatus(cheque.getChequeStatus()))) {
				rejectedCount++;
			}
		}
		if (rejectedCount > 0)
			return rejectedCount;

		List<ScanCheque> scanCheques = scanService.getChequesByBatchId(batchId);
		return countScanChequesByStatus(scanCheques, STATUS_MICR_REJECTED);
	}

	private int countScanChequesByStatus(List<ScanCheque> cheques, String requiredStatus) {
		if (cheques == null || cheques.isEmpty())
			return 0;
		int count = 0;
		for (ScanCheque cheque : cheques) {
			if (cheque != null && requiredStatus.equals(normalizeStatus(cheque.getChequeStatus()))) {
				count++;
			}
		}
		return count;
	}

	private List<OutwardCheque> getOutwardCheques(String batchId) {
		try {
			List<OutwardCheque> cheques = outwardChequeService.getChequesByBatchId(batchId);
			return cheques != null ? cheques : new ArrayList<>();
		} catch (Exception exception) {
			return new ArrayList<>();
		}
	}

	private void openChequeDataEntry(OutwardBatch batch) {
		if (batch == null)
			return;
		String batchId = safeValue(batch.getOutwardBatchId());
		if (batchId.isEmpty())
			return;

		Include mainContentArea = findMainContentArea(outwardDataEntryRowsBatch);
		if (mainContentArea == null)
			return;

		String batchStatus = normalizeStatus(batch.getBatchStatus());
		boolean returnedFromChecker = STATUS_ON_HOLD.equals(batchStatus);
		String scannedBatchId = safeValue(outwardBatchService.getScannedBatchIdByOutwardBatchId(batchId));

		mainContentArea.clearDynamicProperties();
		mainContentArea.setDynamicProperty("batchId", batchId);
		mainContentArea.setDynamicProperty("mode", MODE_DATA_ENTRY);
		mainContentArea.setDynamicProperty(RETURN_FROM_CHECKER, returnedFromChecker);

		if (!scannedBatchId.isEmpty()) {
			mainContentArea.setDynamicProperty("scannedBatchId", scannedBatchId);
		}

		mainContentArea.setAttribute("batchId", batchId);
		mainContentArea.setAttribute("mode", MODE_DATA_ENTRY);
		mainContentArea.setAttribute(RETURN_FROM_CHECKER, returnedFromChecker);

		if (!scannedBatchId.isEmpty()) {
			mainContentArea.setAttribute("scannedBatchId", scannedBatchId);
		}

		mainContentArea.setSrc(CHEQUE_DATA_ENTRY_ZUL);
	}

	private Include findMainContentArea(Component component) {
		Component current = component;
		while (current != null) {
			if (current instanceof Include && "mainContentArea".equals(current.getId())) {
				return (Include) current;
			}
			current = current.getParent();
		}
		return null;
	}

	private void bindPaginationEvents() {
		outwardDataEntryBtnFirst.addEventListener(Events.ON_CLICK, (EventListener<Event>) event -> goToFirstPage());
		outwardDataEntryBtnPrevious.addEventListener(Events.ON_CLICK,
				(EventListener<Event>) event -> goToPreviousPage());
		outwardDataEntryBtnNext.addEventListener(Events.ON_CLICK, (EventListener<Event>) event -> goToNextPage());
		outwardDataEntryBtnLast.addEventListener(Events.ON_CLICK, (EventListener<Event>) event -> goToLastPage());
	}

	private void goToFirstPage() {
		if (outwardDataEntryCurrentPage <= 1)
			return;
		outwardDataEntryCurrentPage = 1;
		renderCurrentPage();
	}

	private void goToPreviousPage() {
		if (outwardDataEntryCurrentPage <= 1)
			return;
		outwardDataEntryCurrentPage--;
		renderCurrentPage();
	}

	private void goToNextPage() {
		if (outwardDataEntryCurrentPage >= getTotalPages())
			return;
		outwardDataEntryCurrentPage++;
		renderCurrentPage();
	}

	private void goToLastPage() {
		if (outwardDataEntryCurrentPage >= getTotalPages())
			return;
		outwardDataEntryCurrentPage = getTotalPages();
		renderCurrentPage();
	}

	private void updateBatchCount() {
		int count = outwardDataEntryBatchList == null ? 0 : outwardDataEntryBatchList.size();
		outwardDataEntryLblBatchCount.setValue(count == 1 ? "1 Batch" : count + " Batches");
	}

	private void updatePagination() {
		int totalPages = getTotalPages();
		outwardDataEntryLblCurrentPage.setValue(outwardDataEntryCurrentPage + " / " + totalPages);
		outwardDataEntryBtnFirst.setDisabled(outwardDataEntryCurrentPage <= 1);
		outwardDataEntryBtnPrevious.setDisabled(outwardDataEntryCurrentPage <= 1);
		outwardDataEntryBtnNext.setDisabled(outwardDataEntryCurrentPage >= totalPages);
		outwardDataEntryBtnLast.setDisabled(outwardDataEntryCurrentPage >= totalPages);
	}

	private int getTotalPages() {
		if (outwardDataEntryBatchList == null || outwardDataEntryBatchList.isEmpty())
			return 1;
		return (int) Math.ceil((double) outwardDataEntryBatchList.size() / PAGE_SIZE);
	}

	private String getStatusClass(String status) {
		String normalized = normalizeStatus(status);
		if (STATUS_ON_HOLD.equals(normalized))
			return "send-back";
		if (STATUS_MICR_REJECTED.equals(normalized))
			return "rejected";
		if (STATUS_PENDING_VERIFICATION.equals(normalized))
			return "completed";
		if (STATUS_PENDING_CHECKER_PROCESS.equals(normalized))
			return "processing";
		if (STATUS_REJECTION_REQUEST.equals(normalized) || STATUS_REJECTION_REJECT.equals(normalized))
			return "rejection-request";
		return "pending";
	}

	private String formatStatus(String status) {
		String normalized = normalizeStatus(status);
		if (STATUS_PENDING_MAKER_PROCESS.equals(normalized) || STATUS_PENDING_DATA_ENTRY.equals(normalized)) {
			return "Pending Data Entry";
		}
		if (STATUS_ON_HOLD.equals(normalized)) {
			return "On Hold (Returned)";
		}
		if (normalized.isEmpty())
			return "-";

		String[] parts = normalized.split("_");
		StringBuilder result = new StringBuilder();
		for (String part : parts) {
			if (part == null || part.isEmpty())
				continue;
			if (result.length() > 0)
				result.append(" ");
			String lower = part.toLowerCase(Locale.ENGLISH);
			result.append(Character.toUpperCase(lower.charAt(0)));
			if (lower.length() > 1)
				result.append(lower.substring(1));
		}
		return result.toString();
	}

	private String normalizeStatus(String status) {
		if (status == null || status.trim().isEmpty())
			return "";
		return status.trim().replace("-", "_").replace(" ", "_").toUpperCase(Locale.ENGLISH);
	}

	private String safeValue(String value) {
		return value == null ? "" : value.trim();
	}

	private boolean isBlank(String value) {
		return value == null || value.trim().isEmpty();
	}
}