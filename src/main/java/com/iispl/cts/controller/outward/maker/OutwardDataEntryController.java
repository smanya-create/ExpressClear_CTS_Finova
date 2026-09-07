package com.iispl.cts.controller.outward.maker;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;

import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.service.outward.ScanService;
import com.iispl.cts.serviceimpl.outward.ScanServiceImpl;

public class OutwardDataEntryController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;

	private static final int PAGE_SIZE = 5;

	@Wire
	private Rows outwardDataEntryRowsBatch;

	@Wire
	private Div outwardDataEntryEmptyState;

	@Wire
	private Label outwardDataEntryLblCurrentPage;

	@Wire
	private Button outwardDataEntryBtnFirst;

	@Wire
	private Button outwardDataEntryBtnPrevious;

	@Wire
	private Button outwardDataEntryBtnNext;

	@Wire
	private Button outwardDataEntryBtnLast;

	private final ScanService scanService;

	private List<ScanBatch> outwardDataEntryBatchList;

	private int outwardDataEntryCurrentPage = 1;

	public OutwardDataEntryController() {
		scanService = new ScanServiceImpl();
		outwardDataEntryBatchList = new ArrayList<>();
	}

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		bindPaginationEvents();

		loadBatches();
	}

	private void bindPaginationEvents() {

		outwardDataEntryBtnFirst.addEventListener("onClick", event -> goToFirstPage());

		outwardDataEntryBtnPrevious.addEventListener("onClick", event -> goToPreviousPage());

		outwardDataEntryBtnNext.addEventListener("onClick", event -> goToNextPage());

		outwardDataEntryBtnLast.addEventListener("onClick", event -> goToLastPage());
	}

	private void loadBatches() {

		try {

			List<ScanBatch> batches = scanService.getMakerDashboardBatches();

			if (batches == null) {
				outwardDataEntryBatchList = new ArrayList<>();
			} else {
				outwardDataEntryBatchList = new ArrayList<>(batches);
			}

			outwardDataEntryCurrentPage = 1;

			renderCurrentPage();

		} catch (Exception e) {

			outwardDataEntryBatchList = new ArrayList<>();

			outwardDataEntryCurrentPage = 1;

			renderCurrentPage();

			e.printStackTrace();
		}
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

		for (int i = startIndex; i < endIndex; i++) {

			ScanBatch batch = outwardDataEntryBatchList.get(i);

			createBatchRow(batch);
		}

		updatePagination();
	}

	private void createBatchRow(final ScanBatch batch) {

		Row outwardDataEntryRow = new Row();

		outwardDataEntryRow.setSclass("outward-data-entry-row");

		String batchId = getValue(batch.getScannedBatchId());

		Label outwardDataEntryLblBatch = new Label(batchId);

		outwardDataEntryLblBatch.setSclass("outward-data-entry-cell " + "outward-data-entry-batch-id");

		int totalCheques = batch.getActualChequeCount();

		Label outwardDataEntryLblTotal = new Label(String.valueOf(totalCheques));

		outwardDataEntryLblTotal.setSclass("outward-data-entry-cell " + "outward-data-entry-number");

		int dataEntered = getDataEnteredCount(batch.getScannedBatchId());

		Label outwardDataEntryLblDataEntered = new Label(dataEntered + " / " + totalCheques);

		outwardDataEntryLblDataEntered.setSclass("outward-data-entry-cell " + "outward-data-entry-progress");

		String batchStatus = getValue(batch.getBatchStatus());

		Label outwardDataEntryLblStatus = new Label(batchStatus);

		outwardDataEntryLblStatus
				.setSclass("outward-data-entry-cell " + "outward-data-entry-status " + getStatusClass(batchStatus));

		Button outwardDataEntryBtnProceed = new Button("PROCEED");

		outwardDataEntryBtnProceed.setSclass("outward-data-entry-proceed-button");

		outwardDataEntryBtnProceed.addEventListener("onClick", event -> openChequeDataEntry(batch));

		outwardDataEntryRow.appendChild(outwardDataEntryLblBatch);

		outwardDataEntryRow.appendChild(outwardDataEntryLblTotal);

		outwardDataEntryRow.appendChild(outwardDataEntryLblDataEntered);

		outwardDataEntryRow.appendChild(outwardDataEntryLblStatus);

		outwardDataEntryRow.appendChild(outwardDataEntryBtnProceed);

		outwardDataEntryRowsBatch.appendChild(outwardDataEntryRow);
	}

	private int getDataEnteredCount(String scannedBatchId) {

		if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {

			return 0;
		}

		try {

			return scanService.getDataEnteredCountByBatchId(scannedBatchId);

		} catch (Exception e) {

			e.printStackTrace();

			return 0;
		}
	}

	private void openChequeDataEntry(ScanBatch batch) {

		if (batch == null || batch.getScannedBatchId() == null || batch.getScannedBatchId().trim().isEmpty()) {

			return;
		}

		String batchId = batch.getScannedBatchId();

		Executions.sendRedirect("/outward/maker/cheque-data-entry.zul?batchId=" + batchId);
	}

	private void goToFirstPage() {

		if (outwardDataEntryCurrentPage == 1) {
			return;
		}

		outwardDataEntryCurrentPage = 1;

		renderCurrentPage();
	}

	private void goToPreviousPage() {

		if (outwardDataEntryCurrentPage <= 1) {
			return;
		}

		outwardDataEntryCurrentPage--;

		renderCurrentPage();
	}

	private void goToNextPage() {

		int totalPages = getTotalPages();

		if (outwardDataEntryCurrentPage >= totalPages) {
			return;
		}

		outwardDataEntryCurrentPage++;

		renderCurrentPage();
	}

	private void goToLastPage() {

		int totalPages = getTotalPages();

		if (outwardDataEntryCurrentPage == totalPages) {
			return;
		}

		outwardDataEntryCurrentPage = totalPages;

		renderCurrentPage();
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

		if (outwardDataEntryBatchList == null || outwardDataEntryBatchList.isEmpty()) {

			return 1;
		}

		return (int) Math.ceil((double) outwardDataEntryBatchList.size() / PAGE_SIZE);
	}

	private String getStatusClass(String status) {

		if (status == null || status.trim().isEmpty()) {

			return "pending";
		}

		String normalizedStatus = status.trim().toLowerCase().replace(" ", "-").replace("_", "-");

		if ("processing".equals(normalizedStatus)) {
			return "processing";
		}

		if ("completed".equals(normalizedStatus)) {
			return "completed";
		}

		if ("rejected".equals(normalizedStatus)) {
			return "rejected";
		}

		if ("pending".equals(normalizedStatus)) {
			return "pending";
		}

		return "pending";
	}

	private String getValue(String value) {

		if (value == null || value.trim().isEmpty()) {

			return "--";
		}

		return value.trim();
	}
}