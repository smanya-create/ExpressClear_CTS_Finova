package com.iispl.cts.controller.outward.maker;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Vlayout;

import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.service.outward.ScanService;
import com.iispl.cts.serviceimpl.outward.ScanServiceImpl;

public class OutwardMakerDashboardController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 1L;

	private static final int PAGE_SIZE = 5;

	private Rows outwardMakerRowsBatchDetails;

	private Vlayout outwardMakerVlayoutEmptyState;

	private Label outwardMakerLblCurrentPage;

	private Button outwardMakerBtnFirst;

	private Button outwardMakerBtnPrevious;

	private Button outwardMakerBtnNext;

	private Button outwardMakerBtnLast;

	private Grid outwardMakerGridBatchDetails;

	private ScanService scanService;

	private List<ScanBatch> batchList = new ArrayList<>();

	private int currentPage = 1;

	@Override
	public void doAfterCompose(Component component) throws Exception {

		super.doAfterCompose(component);

		scanService = new ScanServiceImpl();

		outwardMakerGridBatchDetails = (Grid) component.getFellow("outwardMakerGridBatchDetails");

		outwardMakerRowsBatchDetails = (Rows) component.getFellow("outwardMakerRowsBatchDetails");

		outwardMakerVlayoutEmptyState = (Vlayout) component.getFellow("outwardMakerVlayoutEmptyState");

		outwardMakerLblCurrentPage = (Label) component.getFellow("outwardMakerLblCurrentPage");

		outwardMakerBtnFirst = (Button) component.getFellow("outwardMakerBtnFirst");

		outwardMakerBtnPrevious = (Button) component.getFellow("outwardMakerBtnPrevious");

		outwardMakerBtnNext = (Button) component.getFellow("outwardMakerBtnNext");

		outwardMakerBtnLast = (Button) component.getFellow("outwardMakerBtnLast");

		outwardMakerBtnFirst.addEventListener("onClick", event -> goToFirstPage());

		outwardMakerBtnPrevious.addEventListener("onClick", event -> goToPreviousPage());

		outwardMakerBtnNext.addEventListener("onClick", event -> goToNextPage());

		outwardMakerBtnLast.addEventListener("onClick", event -> goToLastPage());

		loadBatches();
	}

	private void loadBatches() {

		try {

			batchList = scanService.getMakerDashboardBatches();

			if (batchList == null) {

				batchList = new ArrayList<>();
			}

			currentPage = 1;

			renderCurrentPage();

		} catch (Exception e) {

			e.printStackTrace();

			batchList = new ArrayList<>();

			currentPage = 1;

			renderCurrentPage();
		}
	}

	private void renderCurrentPage() {

		outwardMakerRowsBatchDetails.getChildren().clear();

		if (batchList == null || batchList.isEmpty()) {

			outwardMakerVlayoutEmptyState.setVisible(true);

			outwardMakerGridBatchDetails.setVisible(false);

			updatePagination();

			return;
		}

		outwardMakerVlayoutEmptyState.setVisible(false);

		outwardMakerGridBatchDetails.setVisible(true);

		int totalPages = getTotalPages();

		if (currentPage > totalPages) {

			currentPage = totalPages;
		}

		int startIndex = (currentPage - 1) * PAGE_SIZE;

		int endIndex = Math.min(startIndex + PAGE_SIZE, batchList.size());

		for (int index = startIndex; index < endIndex; index++) {

			ScanBatch batch = batchList.get(index);

			if (batch != null) {

				createBatchRow(batch);
			}
		}

		updatePagination();
	}

	private void createBatchRow(ScanBatch batch) {

		Row row = new Row();

		Label batchIdLabel = new Label(getValue(batch.getScannedBatchId()));

		batchIdLabel.setSclass("outward-maker-batch-id");

		Label chequeCountLabel = new Label(String.valueOf(batch.getActualChequeCount()));

		chequeCountLabel.setSclass("outward-maker-cheque-count");

		Label totalAmountLabel = new Label(formatAmount(batch.getActualTotalAmount()));

		totalAmountLabel.setSclass("outward-maker-total-amount");

		Label statusLabel = new Label(getValue(batch.getBatchStatus()));

		statusLabel.setSclass("outward-maker-status " + getStatusClass(batch.getBatchStatus()));

		Button viewButton = new Button("VIEW DETAILS");

		viewButton.setSclass("outward-maker-view-button");

		viewButton.addEventListener("onClick", event -> openBatchDetails(batch.getScannedBatchId()));

		row.appendChild(batchIdLabel);

		row.appendChild(chequeCountLabel);

		row.appendChild(totalAmountLabel);

		row.appendChild(statusLabel);

		row.appendChild(viewButton);

		outwardMakerRowsBatchDetails.appendChild(row);
	}

	private void openBatchDetails(String scannedBatchId) {

		if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {

			return;
		}

		String batchId = scannedBatchId.trim();

		Component root = outwardMakerRowsBatchDetails.getPage().getFirstRoot();

		Component mainContentArea = root.getFellowIfAny("mainContentArea", true);

		if (!(mainContentArea instanceof Include)) {

			return;
		}

		Include include = (Include) mainContentArea;

		include.setAttribute("OUTWARD_MAKER_SELECTED_BATCH_ID", batchId);

		Executions.getCurrent().getSession().setAttribute("OUTWARD_MAKER_SELECTED_BATCH_ID", batchId);

		include.clearDynamicProperties();

		include.setDynamicProperty("batchId", batchId);

		include.setSrc("/outward/maker/batch-details.zul");
	}

	private void goToFirstPage() {

		if (currentPage > 1) {

			currentPage = 1;

			renderCurrentPage();
		}
	}

	private void goToPreviousPage() {

		if (currentPage > 1) {

			currentPage--;

			renderCurrentPage();
		}
	}

	private void goToNextPage() {

		if (currentPage < getTotalPages()) {

			currentPage++;

			renderCurrentPage();
		}
	}

	private void goToLastPage() {

		int totalPages = getTotalPages();

		if (currentPage < totalPages) {

			currentPage = totalPages;

			renderCurrentPage();
		}
	}

	private int getTotalPages() {

		if (batchList == null || batchList.isEmpty()) {

			return 1;
		}

		return (int) Math.ceil((double) batchList.size() / PAGE_SIZE);
	}

	private void updatePagination() {

		int totalPages = getTotalPages();

		outwardMakerLblCurrentPage.setValue(currentPage + " / " + totalPages);

		outwardMakerBtnFirst.setDisabled(currentPage <= 1);

		outwardMakerBtnPrevious.setDisabled(currentPage <= 1);

		outwardMakerBtnNext.setDisabled(currentPage >= totalPages);

		outwardMakerBtnLast.setDisabled(currentPage >= totalPages);
	}

	private String formatAmount(BigDecimal amount) {

		if (amount == null) {

			return "₹0.00";
		}

		return "₹" + new DecimalFormat("#,##0.00").format(amount);
	}

	private String getValue(Object value) {

		if (value == null) {

			return "-";
		}

		String text = String.valueOf(value);

		if (text.trim().isEmpty()) {

			return "-";
		}

		return text;
	}

	private String getStatusClass(String status) {

		if (status == null) {

			return "processing";
		}

		String normalizedStatus = status.trim().toLowerCase();

		if (normalizedStatus.contains("completed") || normalizedStatus.contains("success")
				|| normalizedStatus.contains("validated")) {

			return "completed";
		}

		if (normalizedStatus.contains("rejected") || normalizedStatus.contains("failed")) {

			return "rejected";
		}

		if (normalizedStatus.contains("processing") || normalizedStatus.contains("validating")) {

			return "processing";
		}

		return "pending";
	}
}