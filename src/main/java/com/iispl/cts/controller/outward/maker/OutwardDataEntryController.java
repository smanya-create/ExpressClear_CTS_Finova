package com.iispl.cts.controller.outward.maker;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.service.outward.OutwardBatchService;
import com.iispl.cts.serviceimpl.outward.OutwardBatchServiceImpl;

public class OutwardDataEntryController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;

	private static final int PAGE_SIZE = 5;

	private static final String STATUS_PENDING_MAKER_PROCESS = "PENDING_MAKER_PROCESS";
	private static final String STATUS_PENDING_DATA_ENTRY = "PENDING_DATA_ENTRY";
	private static final String STATUS_PENDING_MICR_REPAIR = "PENDING_MICR_REPAIR";
	private static final String STATUS_PENDING_CHECKER_PROCESS = "PENDING_CHECKER_PROCESS";
	private static final String STATUS_PENDING_VERIFICATION = "PENDING_VERIFICATION";
	private static final String STATUS_ON_HOLD = "ON_HOLD";
	private static final String STATUS_REJECTION_REQUEST = "REJECTION_REQUEST";

	private static final String MODE_DATA_ENTRY = "DATA_ENTRY";

	private static final String RETURN_FROM_CHECKER = "RETURN_FROM_CHECKER";

	private Rows outwardDataEntryRowsBatch;

	private Vlayout outwardDataEntryEmptyState;

	private Label outwardDataEntryLblCurrentPage;

	private Button outwardDataEntryBtnFirst;

	private Button outwardDataEntryBtnPrevious;

	private Button outwardDataEntryBtnNext;

	private Button outwardDataEntryBtnLast;

	private final OutwardBatchService outwardBatchService;

	private List<OutwardBatch> outwardDataEntryBatchList;

	private int outwardDataEntryCurrentPage = 1;

	public OutwardDataEntryController() {
		outwardBatchService = new OutwardBatchServiceImpl();
		outwardDataEntryBatchList = new ArrayList<>();
	}

	@Override
	public void doAfterCompose(Component comp) throws Exception {

		super.doAfterCompose(comp);

		outwardDataEntryRowsBatch = (Rows) comp.getFellow("outwardDataEntryRowsBatch");

		outwardDataEntryEmptyState = (Vlayout) comp.getFellow("outwardDataEntryEmptyState");

		outwardDataEntryLblCurrentPage = (Label) comp.getFellow("outwardDataEntryLblCurrentPage");

		outwardDataEntryBtnFirst = (Button) comp.getFellow("outwardDataEntryBtnFirst");

		outwardDataEntryBtnPrevious = (Button) comp.getFellow("outwardDataEntryBtnPrevious");

		outwardDataEntryBtnNext = (Button) comp.getFellow("outwardDataEntryBtnNext");

		outwardDataEntryBtnLast = (Button) comp.getFellow("outwardDataEntryBtnLast");

		updateDataEntryNavigation(comp);

		bindPaginationEvents();

		loadBatches();
	}

	private void updateDataEntryNavigation(Component component) {

		Component current = component;

		Include mainContentArea = null;

		while (current != null) {

			if (current instanceof Include && "mainContentArea".equals(current.getId())) {

				mainContentArea = (Include) current;

				break;
			}

			current = current.getParent();
		}

		if (mainContentArea == null) {
			return;
		}

		current = mainContentArea;

		Window rootWindow = null;

		while (current != null) {

			if (current instanceof Window) {

				rootWindow = (Window) current;

				break;
			}

			current = current.getParent();
		}

		if (rootWindow == null) {
			return;
		}

		Component headerComponent = rootWindow.getFellowIfAny("headerInclude");

		Component sidebarComponent = rootWindow.getFellowIfAny("sidebarInclude");

		if (headerComponent instanceof Include) {

			Include headerInclude = (Include) headerComponent;

			headerInclude.clearDynamicProperties();

			headerInclude.setDynamicProperty("pageTitle", "Data Entry");

			headerInclude.setDynamicProperty("pageSubtitle", "Outward Processing");

			headerInclude.setSrc("/components/header.zul");
		}

		if (sidebarComponent instanceof Include) {

			Include sidebarInclude = (Include) sidebarComponent;

			sidebarInclude.clearDynamicProperties();

			sidebarInclude.setDynamicProperty("role", "OUTWARD_MAKER");

			sidebarInclude.setDynamicProperty("activeTab", "outward-data-entry");

			sidebarInclude.setSrc("/components/sidebar.zul");
		}
	}

	private void bindPaginationEvents() {

		outwardDataEntryBtnFirst.addEventListener("onClick", event -> goToFirstPage());

		outwardDataEntryBtnPrevious.addEventListener("onClick", event -> goToPreviousPage());

		outwardDataEntryBtnNext.addEventListener("onClick", event -> goToNextPage());

		outwardDataEntryBtnLast.addEventListener("onClick", event -> goToLastPage());
	}

	private void loadBatches() {

		try {

			outwardDataEntryBatchList = new ArrayList<>();

			loadInitialDataEntryBatches();

			loadReturnedDataEntryBatches();

			outwardDataEntryCurrentPage = 1;

			renderCurrentPage();

		} catch (Exception exception) {

			outwardDataEntryBatchList = new ArrayList<>();

			outwardDataEntryCurrentPage = 1;

			renderCurrentPage();

			exception.printStackTrace();
		}
	}

	private void loadInitialDataEntryBatches() {

		List<OutwardBatch> initialBatches = outwardBatchService.getScanBatchesReadyForDataEntry();

		if (initialBatches == null || initialBatches.isEmpty()) {
			return;
		}

		for (OutwardBatch batch : initialBatches) {

			if (batch == null) {
				continue;
			}

			String batchId = getValue(batch.getOutwardBatchId());

			if ("--".equals(batchId)) {
				continue;
			}

			batch.setBatchStatus(STATUS_PENDING_MAKER_PROCESS);

			if (!containsBatch(batchId)) {

				outwardDataEntryBatchList.add(batch);
			}
		}
	}

	private void loadReturnedDataEntryBatches() {

		List<OutwardBatch> returnedBatches = outwardBatchService.getBatchesReadyForDataEntry();

		if (returnedBatches == null || returnedBatches.isEmpty()) {
			return;
		}

		for (OutwardBatch batch : returnedBatches) {

			if (batch == null) {
				continue;
			}

			String batchId = getValue(batch.getOutwardBatchId());

			if ("--".equals(batchId)) {
				continue;
			}

			String batchStatus = normalizeStatus(batch.getBatchStatus());

			if (!STATUS_ON_HOLD.equals(batchStatus)) {
				continue;
			}

			batch.setBatchStatus(STATUS_ON_HOLD);

			if (!containsBatch(batchId)) {

				outwardDataEntryBatchList.add(batch);
			}
		}
	}

	private boolean containsBatch(String batchId) {

		if (batchId == null || batchId.trim().isEmpty()) {
			return false;
		}

		for (OutwardBatch existingBatch : outwardDataEntryBatchList) {

			if (existingBatch == null) {
				continue;
			}

			String existingBatchId = existingBatch.getOutwardBatchId();

			if (existingBatchId != null && batchId.trim().equalsIgnoreCase(existingBatchId.trim())) {

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

		Row outwardDataEntryRow = new Row();

		outwardDataEntryRow.setSclass("outward-data-entry-row");

		String batchId = getValue(batch.getOutwardBatchId());

		Label outwardDataEntryLblBatch = new Label(batchId);

		outwardDataEntryLblBatch.setSclass("outward-data-entry-cell outward-data-entry-batch-id");

		int totalCheques = batch.getActualChequeCount();

		Label outwardDataEntryLblTotal = new Label(String.valueOf(totalCheques));

		outwardDataEntryLblTotal.setSclass("outward-data-entry-cell outward-data-entry-number");

		int dataEntered = getDataEnteredCount(batch);

		Label outwardDataEntryLblDataEntered = new Label(dataEntered + " / " + totalCheques);

		outwardDataEntryLblDataEntered.setSclass("outward-data-entry-cell outward-data-entry-progress");

		String batchStatus = normalizeStatus(batch.getBatchStatus());

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

	private int getDataEnteredCount(OutwardBatch batch) {

		if (batch == null) {
			return 0;
		}

		int totalCheques = batch.getActualChequeCount();

		String batchStatus = normalizeStatus(batch.getBatchStatus());

		if (STATUS_ON_HOLD.equals(batchStatus)) {

			return 0;
		}

		if (totalCheques <= 0) {
			return 0;
		}

		return 0;
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

		String batchStatus = normalizeStatus(batch.getBatchStatus());

		boolean returnedFromChecker = STATUS_ON_HOLD.equals(batchStatus);

		mainContentArea.clearDynamicProperties();

		mainContentArea.setDynamicProperty("batchId", batchId);

		mainContentArea.setDynamicProperty("mode", MODE_DATA_ENTRY);

		mainContentArea.setDynamicProperty(RETURN_FROM_CHECKER, returnedFromChecker);

		mainContentArea.setAttribute("batchId", batchId);

		mainContentArea.setAttribute("mode", MODE_DATA_ENTRY);

		mainContentArea.setAttribute(RETURN_FROM_CHECKER, returnedFromChecker);

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

		if (outwardDataEntryCurrentPage >= totalPages) {
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

		String normalizedStatus = normalizeStatus(status);

		if (STATUS_PENDING_MAKER_PROCESS.equals(normalizedStatus)) {

			return "pending-maker-process";
		}

		if (STATUS_PENDING_DATA_ENTRY.equals(normalizedStatus)) {

			return "pending-data-entry";
		}

		if (STATUS_PENDING_MICR_REPAIR.equals(normalizedStatus)) {

			return "pending-micr-repair";
		}

		if (STATUS_PENDING_CHECKER_PROCESS.equals(normalizedStatus)) {

			return "pending-checker-process";
		}

		if (STATUS_PENDING_VERIFICATION.equals(normalizedStatus)) {

			return "pending-verification";
		}

		if (STATUS_ON_HOLD.equals(normalizedStatus)) {

			return "on-hold";
		}

		if (STATUS_REJECTION_REQUEST.equals(normalizedStatus)) {

			return "rejection-request";
		}

		return "pending-maker-process";
	}

	private String normalizeStatus(String status) {

		if (status == null || status.trim().isEmpty()) {
			return STATUS_PENDING_MAKER_PROCESS;
		}

		String normalizedStatus = status.trim().toUpperCase().replace('-', '_').replace(' ', '_');

		if (STATUS_PENDING_MAKER_PROCESS.equals(normalizedStatus)) {

			return STATUS_PENDING_MAKER_PROCESS;
		}

		if (STATUS_PENDING_DATA_ENTRY.equals(normalizedStatus)) {

			return STATUS_PENDING_DATA_ENTRY;
		}

		if (STATUS_PENDING_MICR_REPAIR.equals(normalizedStatus)) {

			return STATUS_PENDING_MICR_REPAIR;
		}

		if (STATUS_PENDING_CHECKER_PROCESS.equals(normalizedStatus)) {

			return STATUS_PENDING_CHECKER_PROCESS;
		}

		if (STATUS_PENDING_VERIFICATION.equals(normalizedStatus)) {

			return STATUS_PENDING_VERIFICATION;
		}

		if (STATUS_ON_HOLD.equals(normalizedStatus)) {

			return STATUS_ON_HOLD;
		}

		if (STATUS_REJECTION_REQUEST.equals(normalizedStatus)) {

			return STATUS_REJECTION_REQUEST;
		}

		return normalizedStatus;
	}

	private String getValue(String value) {

		if (value == null || value.trim().isEmpty()) {
			return "--";
		}

		return value.trim();
	}
}