package com.iispl.cts.controller.outward.maker;

import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Window;

import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.service.outward.OutwardBatchService;
import com.iispl.cts.service.outward.OutwardChequeService;
import com.iispl.cts.serviceimpl.outward.OutwardBatchServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardChequeServiceImpl;

public class OutwardDataEntryController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;

	private static final int PAGE_SIZE = 5;

	private Rows outwardDataEntryRowsBatch;
	private Div outwardDataEntryEmptyState;
	private Label outwardDataEntryLblCurrentPage;
	private Button outwardDataEntryBtnFirst;
	private Button outwardDataEntryBtnPrevious;
	private Button outwardDataEntryBtnNext;
	private Button outwardDataEntryBtnLast;

	private final OutwardBatchService outwardBatchService;
	private final OutwardChequeService outwardChequeService;

	private List<OutwardBatch> outwardDataEntryBatchList;
	private int outwardDataEntryCurrentPage = 1;

	public OutwardDataEntryController() {

		outwardBatchService = new OutwardBatchServiceImpl();
		outwardChequeService = new OutwardChequeServiceImpl();

		outwardDataEntryBatchList = new ArrayList<>();
	}

	@Override
	public void doAfterCompose(Component comp) throws Exception {

		super.doAfterCompose(comp);

		updateDataEntryNavigation(comp);

		outwardDataEntryRowsBatch = (Rows) comp.getFellow("outwardDataEntryRowsBatch");

		outwardDataEntryEmptyState = (Div) comp.getFellow("outwardDataEntryEmptyState");

		outwardDataEntryLblCurrentPage = (Label) comp.getFellow("outwardDataEntryLblCurrentPage");

		outwardDataEntryBtnFirst = (Button) comp.getFellow("outwardDataEntryBtnFirst");

		outwardDataEntryBtnPrevious = (Button) comp.getFellow("outwardDataEntryBtnPrevious");

		outwardDataEntryBtnNext = (Button) comp.getFellow("outwardDataEntryBtnNext");

		outwardDataEntryBtnLast = (Button) comp.getFellow("outwardDataEntryBtnLast");

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

			List<OutwardBatch> batches = outwardBatchService.getBatchesReadyForDataEntry();

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

			OutwardBatch batch = outwardDataEntryBatchList.get(i);

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

		int dataEntered = getDataEnteredCount(batch.getOutwardBatchId());

		Label outwardDataEntryLblDataEntered = new Label(dataEntered + " / " + totalCheques);

		outwardDataEntryLblDataEntered.setSclass("outward-data-entry-cell outward-data-entry-progress");

		String batchStatus = getValue(batch.getBatchStatus());

		Label outwardDataEntryLblStatus = new Label(batchStatus);

		outwardDataEntryLblStatus
				.setSclass("outward-data-entry-cell outward-data-entry-status " + getStatusClass(batchStatus));

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

	private int getDataEnteredCount(String outwardBatchId) {

		if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {

			return 0;
		}

		try {

			return outwardChequeService.getDataEnteredCountByBatchId(outwardBatchId);

		} catch (Exception e) {

			e.printStackTrace();

			return 0;
		}
	}

	private void openChequeDataEntry(OutwardBatch batch) {

		if (batch == null || batch.getOutwardBatchId() == null || batch.getOutwardBatchId().trim().isEmpty()) {

			return;
		}

		String batchId = batch.getOutwardBatchId().trim();

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