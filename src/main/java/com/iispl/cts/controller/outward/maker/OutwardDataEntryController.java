package com.iispl.cts.controller.outward.maker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

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
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.service.outward.OutwardBatchService;
import com.iispl.cts.service.outward.ScanService;
import com.iispl.cts.serviceimpl.outward.OutwardBatchServiceImpl;
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

	private static final String STATUS_ON_HOLD = "ON_HOLD";

	private Rows outwardDataEntryRowsBatch;

	private Vlayout outwardDataEntryEmptyState;

	private Label outwardDataEntryLblCurrentPage;

	private Label outwardDataEntryLblBatchCount;

	private Button outwardDataEntryBtnFirst;

	private Button outwardDataEntryBtnPrevious;

	private Button outwardDataEntryBtnNext;

	private Button outwardDataEntryBtnLast;

	private ScanService scanService;

	private OutwardBatchService outwardBatchService;

	private List<OutwardBatch> outwardDataEntryBatchList;

	private Map<String, Integer> pendingDataEntryCounts;

	private int outwardDataEntryCurrentPage = 1;

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

		scanService = new ScanServiceImpl();

		outwardBatchService = new OutwardBatchServiceImpl();

		outwardDataEntryBatchList = new ArrayList<>();

		pendingDataEntryCounts = new HashMap<>();

		bindPaginationEvents();

		loadBatches();
	}

	private void loadBatches() {

		outwardDataEntryBatchList.clear();

		pendingDataEntryCounts.clear();

		loadScanBatches();

		loadOnHoldBatches();

		outwardDataEntryCurrentPage = 1;

		updateBatchCount();

		renderCurrentPage();
	}

	private void loadScanBatches() {

		List<ScanBatch> scanBatches = scanService.getMakerDashboardBatches();

		if (scanBatches == null || scanBatches.isEmpty()) {
			return;
		}

		for (ScanBatch scanBatch : scanBatches) {

			if (scanBatch == null) {
				continue;
			}

			String batchId = scanBatch.getScannedBatchId();

			if (batchId == null || batchId.trim().isEmpty()) {
				continue;
			}

			batchId = batchId.trim();

			List<ScanCheque> scanCheques = scanService.getChequesByBatchId(batchId);

			if (scanCheques == null) {
				scanCheques = new ArrayList<>();
			}

			int totalCheques = scanBatch.getActualChequeCount();

			if (totalCheques <= 0) {
				totalCheques = scanCheques.size();
			}

			if (totalCheques <= 0) {
				continue;
			}

			int pendingDataEntry = getPendingDataEntryCount(scanCheques);

			OutwardBatch batch = new OutwardBatch();

			batch.setOutwardBatchId(batchId);

			batch.setBatchReferenceId(scanBatch.getBatchReferenceId());

			batch.setActualChequeCount(totalCheques);

			batch.setActualTotalAmount(scanBatch.getActualTotalAmount());

			batch.setBatchStatus(scanBatch.getBatchStatus());

			batch.setUploadedBy(scanBatch.getUploadedBy());

			batch.setUploadedAt(scanBatch.getUploadedAt());

			if (!containsBatch(batchId)) {

				outwardDataEntryBatchList.add(batch);

				pendingDataEntryCounts.put(batchId, pendingDataEntry);
			}
		}
	}

	private void loadOnHoldBatches() {

		List<OutwardBatch> batches = outwardBatchService.getBatchesReadyForDataEntry();

		if (batches == null || batches.isEmpty()) {
			return;
		}

		for (OutwardBatch batch : batches) {

			if (batch == null) {
				continue;
			}

			String batchId = batch.getOutwardBatchId();

			if (batchId == null || batchId.trim().isEmpty()) {
				continue;
			}

			batchId = batchId.trim();

			String status = normalizeStatus(batch.getBatchStatus());

			if (!STATUS_ON_HOLD.equals(status)) {
				continue;
			}

			if (!containsBatch(batchId)) {

				outwardDataEntryBatchList.add(batch);
			}
		}
	}

	private int getPendingDataEntryCount(List<ScanCheque> scanCheques) {

		int count = 0;

		for (ScanCheque cheque : scanCheques) {

			if (cheque == null) {
				continue;
			}

			String status = normalizeStatus(cheque.getChequeStatus());

			if (STATUS_PENDING_DATA_ENTRY.equals(status)) {
				count++;
			}
		}

		return count;
	}

	private int getPendingDataEntryCount(OutwardBatch batch) {

		if (batch == null) {
			return 0;
		}

		String batchId = batch.getOutwardBatchId();

		if (batchId == null) {
			return 0;
		}

		Integer count = pendingDataEntryCounts.get(batchId);

		if (count != null) {
			return count;
		}

		return 0;
	}

	private boolean containsBatch(String batchId) {

		if (batchId == null) {
			return false;
		}

		for (OutwardBatch batch : outwardDataEntryBatchList) {

			if (batch == null) {
				continue;
			}

			String existingBatchId = batch.getOutwardBatchId();

			if (existingBatchId != null && existingBatchId.equalsIgnoreCase(batchId)) {

				return true;
			}
		}

		return false;
	}

	private void renderCurrentPage() {

		outwardDataEntryRowsBatch.getChildren().clear();

		if (outwardDataEntryBatchList.isEmpty()) {

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

			createBatchRow(batch);
		}

		updatePagination();
	}

	private void createBatchRow(final OutwardBatch batch) {

		Row row = new Row();

		row.setSclass("outward-data-entry-row");

		String batchId = safeValue(batch.getOutwardBatchId());

		Label batchLabel = new Label(batchId);

		batchLabel.setSclass("outward-data-entry-cell " + "outward-data-entry-batch-id");

		Label totalLabel = new Label(String.valueOf(batch.getActualChequeCount()));

		totalLabel.setSclass("outward-data-entry-cell " + "outward-data-entry-number");

		int pendingDataEntry = getPendingDataEntryCount(batch);

		Label pendingLabel = new Label(pendingDataEntry + " / " + batch.getActualChequeCount());

		pendingLabel.setSclass("outward-data-entry-cell " + "outward-data-entry-progress");

		String databaseStatus = normalizeStatus(batch.getBatchStatus());

		Label statusLabel = new Label(formatStatus(databaseStatus));

		statusLabel
				.setSclass("outward-data-entry-cell " + "outward-data-entry-status " + getStatusClass(databaseStatus));

		Button proceedButton = new Button("Proceed →");

		proceedButton.setSclass("outward-data-entry-proceed-button");

		proceedButton.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) throws Exception {

				openChequeDataEntry(batch);
			}
		});

		row.appendChild(batchLabel);
		row.appendChild(totalLabel);
		row.appendChild(pendingLabel);
		row.appendChild(statusLabel);
		row.appendChild(proceedButton);

		outwardDataEntryRowsBatch.appendChild(row);
	}

	private void openChequeDataEntry(OutwardBatch batch) {

		if (batch == null) {
			return;
		}

		String batchId = batch.getOutwardBatchId();

		if (batchId == null || batchId.trim().isEmpty()) {
			return;
		}

		Include mainContentArea = findMainContentArea(outwardDataEntryRowsBatch);

		if (mainContentArea == null) {
			return;
		}

		batchId = batchId.trim();

		mainContentArea.setAttribute("batchId", batchId);

		mainContentArea.setAttribute("mode", "DATA_ENTRY");

		mainContentArea.setDynamicProperty("batchId", batchId);

		mainContentArea.setDynamicProperty("mode", "DATA_ENTRY");

		mainContentArea.setSrc("/outward/maker/cheque-data-entry.zul");
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

		outwardDataEntryBtnFirst.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				goToFirstPage();
			}
		});

		outwardDataEntryBtnPrevious.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				goToPreviousPage();
			}
		});

		outwardDataEntryBtnNext.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				goToNextPage();
			}
		});

		outwardDataEntryBtnLast.addEventListener(Events.ON_CLICK, new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				goToLastPage();
			}
		});
	}

	private void goToFirstPage() {

		outwardDataEntryCurrentPage = 1;

		renderCurrentPage();
	}

	private void goToPreviousPage() {

		if (outwardDataEntryCurrentPage > 1) {

			outwardDataEntryCurrentPage--;

			renderCurrentPage();
		}
	}

	private void goToNextPage() {

		if (outwardDataEntryCurrentPage < getTotalPages()) {

			outwardDataEntryCurrentPage++;

			renderCurrentPage();
		}
	}

	private void goToLastPage() {

		outwardDataEntryCurrentPage = getTotalPages();

		renderCurrentPage();
	}

	private int getTotalPages() {

		if (outwardDataEntryBatchList.isEmpty()) {
			return 1;
		}

		return (outwardDataEntryBatchList.size() + PAGE_SIZE - 1) / PAGE_SIZE;
	}

	private void updateBatchCount() {

		int count = outwardDataEntryBatchList.size();

		if (count == 1) {

			outwardDataEntryLblBatchCount.setValue("1 Batch");

		} else {

			outwardDataEntryLblBatchCount.setValue(count + " Batches");
		}
	}

	private void updatePagination() {

		int totalPages = getTotalPages();

		outwardDataEntryLblCurrentPage.setValue(outwardDataEntryCurrentPage + " / " + totalPages);

		outwardDataEntryBtnFirst.setDisabled(outwardDataEntryCurrentPage <= 1);

		outwardDataEntryBtnPrevious.setDisabled(outwardDataEntryCurrentPage <= 1);

		outwardDataEntryBtnNext.setDisabled(outwardDataEntryCurrentPage >= totalPages);

		outwardDataEntryBtnLast.setDisabled(outwardDataEntryCurrentPage >= totalPages);
	}

	private String normalizeStatus(String status) {

		if (status == null || status.trim().isEmpty()) {

			return "";
		}

		return status.trim().replace("-", "_").replace(" ", "_").toUpperCase(Locale.ENGLISH);
	}

	private String formatStatus(String status) {

		String normalized = normalizeStatus(status);

		if (normalized.isEmpty()) {
			return "-";
		}

		String[] parts = normalized.split("_");

		StringBuilder result = new StringBuilder();

		for (String part : parts) {

			if (part == null || part.isEmpty()) {
				continue;
			}

			if (result.length() > 0) {
				result.append(" ");
			}

			String lower = part.toLowerCase(Locale.ENGLISH);

			result.append(Character.toUpperCase(lower.charAt(0)));

			if (lower.length() > 1) {

				result.append(lower.substring(1));
			}
		}

		return result.toString();
	}

	private String getStatusClass(String status) {

		String normalized = normalizeStatus(status);

		if (STATUS_PENDING_MAKER_PROCESS.equals(normalized)) {

			return "pending-maker-process";
		}

		if (STATUS_PENDING_DATA_ENTRY.equals(normalized)) {

			return "pending-data-entry";
		}

		if (STATUS_PENDING_MICR_REPAIR.equals(normalized)) {

			return "pending-micr-repair";
		}

		if (STATUS_MICR_REJECTED.equals(normalized)) {

			return "rejected";
		}

		if (STATUS_PENDING_VERIFICATION.equals(normalized)) {

			return "pending-verification";
		}

		if (STATUS_PENDING_CHECKER_PROCESS.equals(normalized)) {

			return "pending-checker-process";
		}

		if (STATUS_REJECTION_REQUEST.equals(normalized)) {

			return "rejection-request";
		}

		if (STATUS_ON_HOLD.equals(normalized)) {

			return "on-hold";
		}

		return "pending-maker-process";
	}

	private String safeValue(String value) {

		if (value == null || value.trim().isEmpty()) {

			return "-";
		}

		return value.trim();
	}
}