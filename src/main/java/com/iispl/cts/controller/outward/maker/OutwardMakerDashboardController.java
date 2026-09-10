package com.iispl.cts.controller.outward.maker;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Vlayout;

import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.service.outward.OutwardBatchService;
import com.iispl.cts.service.outward.OutwardChequeService;
import com.iispl.cts.service.outward.ScanService;
import com.iispl.cts.serviceimpl.outward.OutwardBatchServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardChequeServiceImpl;
import com.iispl.cts.serviceimpl.outward.ScanServiceImpl;

public class OutwardMakerDashboardController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 1L;

	private static final int PAGE_SIZE = 5;

	private static final String STATUS_PENDING_MAKER_PROCESS = "PENDING_MAKER_PROCESS";

	private static final String STATUS_PENDING_DATA_ENTRY = "PENDING_DATA_ENTRY";

	private static final String STATUS_PENDING_MICR_REPAIR = "PENDING_MICR_REPAIR";

	private static final String STATUS_PENDING_CHECKER_PROCESS = "PENDING_CHECKER_PROCESS";

	private static final String STATUS_PENDING_VERIFICATION = "PENDING_VERIFICATION";

	private static final String STATUS_ON_HOLD = "ON_HOLD";

	private static final String STATUS_REJECTION_REQUEST = "REJECTION_REQUEST";

	private Rows outwardMakerRowsBatchDetails;
	private Vlayout outwardMakerVlayoutEmptyState;
	private Label outwardMakerLblCurrentPage;

	private Button outwardMakerBtnFirst;
	private Button outwardMakerBtnPrevious;
	private Button outwardMakerBtnNext;
	private Button outwardMakerBtnLast;

	private Grid outwardMakerGridBatchDetails;

	private Rows outwardMakerRowsReturnedBatches;
	private Vlayout outwardMakerVlayoutReturnedEmptyState;
	private Grid outwardMakerGridReturnedBatches;

	private ScanService scanService;
	private OutwardBatchService outwardBatchService;
	private OutwardChequeService outwardChequeService;

	private List<ScanBatch> batchList = new ArrayList<>();

	private List<OutwardBatch> onHoldBatchList = new ArrayList<>();

	private int currentPage = 1;

	@Override
	public void doAfterCompose(Component component) throws Exception {

		super.doAfterCompose(component);

		scanService = new ScanServiceImpl();

		outwardBatchService = new OutwardBatchServiceImpl();

		outwardChequeService = new OutwardChequeServiceImpl();

		outwardMakerGridBatchDetails = (Grid) component.getFellow("outwardMakerGridBatchDetails");

		outwardMakerRowsBatchDetails = (Rows) component.getFellow("outwardMakerRowsBatchDetails");

		outwardMakerVlayoutEmptyState = (Vlayout) component.getFellow("outwardMakerVlayoutEmptyState");

		outwardMakerLblCurrentPage = (Label) component.getFellow("outwardMakerLblCurrentPage");

		outwardMakerBtnFirst = (Button) component.getFellow("outwardMakerBtnFirst");

		outwardMakerBtnPrevious = (Button) component.getFellow("outwardMakerBtnPrevious");

		outwardMakerBtnNext = (Button) component.getFellow("outwardMakerBtnNext");

		outwardMakerBtnLast = (Button) component.getFellow("outwardMakerBtnLast");

		outwardMakerGridReturnedBatches = (Grid) component.getFellow("outwardMakerGridReturnedBatches");

		outwardMakerRowsReturnedBatches = (Rows) component.getFellow("outwardMakerRowsReturnedBatches");

		outwardMakerVlayoutReturnedEmptyState = (Vlayout) component.getFellow("outwardMakerVlayoutReturnedEmptyState");

		outwardMakerBtnFirst.addEventListener("onClick", event -> goToFirstPage());

		outwardMakerBtnPrevious.addEventListener("onClick", event -> goToPreviousPage());

		outwardMakerBtnNext.addEventListener("onClick", event -> goToNextPage());

		outwardMakerBtnLast.addEventListener("onClick", event -> goToLastPage());

		loadOnHoldBatches();

		loadBatches();
	}

	private void loadOnHoldBatches() {

		try {

			onHoldBatchList = outwardBatchService.getOnHoldBatches();

			if (onHoldBatchList == null) {

				onHoldBatchList = new ArrayList<>();
			}

			renderOnHoldBatches();

		} catch (Exception exception) {

			exception.printStackTrace();

			onHoldBatchList = new ArrayList<>();

			renderOnHoldBatches();
		}
	}

	private void renderOnHoldBatches() {

		outwardMakerRowsReturnedBatches.getChildren().clear();

		if (onHoldBatchList == null || onHoldBatchList.isEmpty()) {

			outwardMakerVlayoutReturnedEmptyState.setVisible(true);

			outwardMakerGridReturnedBatches.setVisible(false);

			return;
		}

		boolean hasReturnedCheques = false;

		for (OutwardBatch batch : onHoldBatchList) {

			if (batch == null || batch.getOutwardBatchId() == null || batch.getOutwardBatchId().trim().isEmpty()) {

				continue;
			}

			try {

				List<OutwardCheque> returnedCheques = outwardChequeService.getOnHoldCheques(batch.getOutwardBatchId());

				if (returnedCheques == null || returnedCheques.isEmpty()) {

					continue;
				}

				for (OutwardCheque cheque : returnedCheques) {

					if (cheque != null) {

						createReturnedChequeRow(batch, cheque);

						hasReturnedCheques = true;
					}
				}

			} catch (Exception exception) {

				exception.printStackTrace();
			}
		}

		if (!hasReturnedCheques) {

			outwardMakerVlayoutReturnedEmptyState.setVisible(true);

			outwardMakerGridReturnedBatches.setVisible(false);

		} else {

			outwardMakerVlayoutReturnedEmptyState.setVisible(false);

			outwardMakerGridReturnedBatches.setVisible(true);
		}
	}

	private void createReturnedChequeRow(OutwardBatch batch, OutwardCheque cheque) {

		Row row = new Row();

		Label batchIdLabel = new Label(getValue(batch.getOutwardBatchId()));

		batchIdLabel.setSclass("outward-maker-batch-id");

		Label chequeNumberLabel = new Label(getValue(cheque.getChequeNumber()));

		chequeNumberLabel.setSclass("outward-maker-cheque-number");

		String chequeStatus = normalizeStatus(cheque.getChequeStatus());

		Label statusLabel = new Label(getDisplayStatus(chequeStatus));

		statusLabel.setSclass("outward-maker-status " + getStatusClass(chequeStatus));

		Label reasonLabel = new Label("-");

		reasonLabel.setSclass("outward-maker-return-reason");

		Button viewButton = new Button("VIEW DETAILS");

		viewButton.setSclass("outward-maker-view-button");

		String outwardBatchId = batch.getOutwardBatchId();

		viewButton.addEventListener("onClick", event -> openBatchDetails(outwardBatchId));

		row.appendChild(batchIdLabel);

		row.appendChild(chequeNumberLabel);

		row.appendChild(statusLabel);

		row.appendChild(reasonLabel);

		row.appendChild(viewButton);

		outwardMakerRowsReturnedBatches.appendChild(row);
	}

	private void loadBatches() {

		try {

			batchList = scanService.getMakerDashboardBatches();

			if (batchList == null) {

				batchList = new ArrayList<>();
			}

			currentPage = 1;

			renderCurrentPage();

		} catch (Exception exception) {

			exception.printStackTrace();

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

		if (currentPage < 1) {

			currentPage = 1;
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

		Label totalAmountLabel = new Label(formatIndianAmount(batch.getActualTotalAmount()));

		totalAmountLabel.setSclass("outward-maker-total-amount");

		String batchStatus = normalizeStatus(batch.getBatchStatus());

		Label statusLabel = new Label(getDisplayStatus(batchStatus));

		statusLabel.setSclass("outward-maker-status " + getStatusClass(batchStatus));

		Button viewButton = new Button("VIEW DETAILS");

		viewButton.setSclass("outward-maker-view-button");

		String scannedBatchId = batch.getScannedBatchId();

		viewButton.addEventListener("onClick", event -> openBatchDetails(scannedBatchId));

		row.appendChild(batchIdLabel);

		row.appendChild(chequeCountLabel);

		row.appendChild(totalAmountLabel);

		row.appendChild(statusLabel);

		row.appendChild(viewButton);

		outwardMakerRowsBatchDetails.appendChild(row);
	}

	private void openBatchDetails(String batchId) {

		if (batchId == null || batchId.trim().isEmpty()) {

			return;
		}

		String trimmedBatchId = batchId.trim();

		Include mainContentArea = findMainContentArea();

		if (mainContentArea == null) {

			System.out.println("mainContentArea Include not found");

			return;
		}

		mainContentArea.clearDynamicProperties();

		mainContentArea.setDynamicProperty("batchId", trimmedBatchId);

		mainContentArea.setAttribute("batchId", trimmedBatchId);

		mainContentArea.setSrc("/outward/maker/batch-details.zul");
	}

	private Include findMainContentArea() {

		Component current = outwardMakerGridBatchDetails;

		while (current != null) {

			if (current instanceof Include) {

				Include include = (Include) current;

				if ("mainContentArea".equals(include.getId())) {

					return include;
				}
			}

			current = current.getParent();
		}

		return null;
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

	private String formatIndianAmount(BigDecimal amount) {

		if (amount == null) {

			return "₹0.00";
		}

		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH);

		symbols.setGroupingSeparator(',');

		DecimalFormat formatter = new DecimalFormat("##,##,##0.00", symbols);

		return "₹" + formatter.format(amount);
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

	private String normalizeStatus(String status) {

		if (status == null) {

			return "";
		}

		return status.trim().toUpperCase();
	}

	private String getDisplayStatus(String status) {

		if (STATUS_PENDING_MAKER_PROCESS.equals(status)) {

			return STATUS_PENDING_MAKER_PROCESS;
		}

		if (STATUS_PENDING_DATA_ENTRY.equals(status)) {

			return STATUS_PENDING_DATA_ENTRY;
		}

		if (STATUS_PENDING_MICR_REPAIR.equals(status)) {

			return STATUS_PENDING_MICR_REPAIR;
		}

		if (STATUS_PENDING_CHECKER_PROCESS.equals(status)) {

			return STATUS_PENDING_CHECKER_PROCESS;
		}

		if (STATUS_PENDING_VERIFICATION.equals(status)) {

			return STATUS_PENDING_VERIFICATION;
		}

		if (STATUS_ON_HOLD.equals(status)) {

			return STATUS_ON_HOLD;
		}

		if (STATUS_REJECTION_REQUEST.equals(status)) {

			return STATUS_REJECTION_REQUEST;
		}

		return getValue(status);
	}

	private String getStatusClass(String status) {

		if (status == null || status.trim().isEmpty()) {

			return "pending-maker-process";
		}

		String normalizedStatus = status.trim().toUpperCase();

		switch (normalizedStatus) {

		case STATUS_PENDING_MAKER_PROCESS:
			return "pending-maker-process";

		case STATUS_PENDING_DATA_ENTRY:
			return "pending-data-entry";

		case STATUS_PENDING_MICR_REPAIR:
			return "pending-micr-repair";

		case STATUS_PENDING_CHECKER_PROCESS:
			return "pending-checker-process";

		case STATUS_PENDING_VERIFICATION:
			return "pending-verification";

		case STATUS_ON_HOLD:
			return "on-hold";

		case STATUS_REJECTION_REQUEST:
			return "rejection-request";

		default:
			return "pending-maker-process";
		}
	}
}