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
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.service.outward.ScanService;
import com.iispl.cts.serviceimpl.outward.ScanServiceImpl;

public class OutwardMakerBatchDetailsController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 1L;

	private static final int PAGE_SIZE = 10;

	private Grid outwardMakerGridChequeDetails;

	private Rows outwardMakerRowsChequeDetails;

	private Vlayout outwardMakerVlayoutEmptyState;

	private Label outwardMakerLblBatchId;
	private Label outwardMakerLblChequeCount;
	private Label outwardMakerLblTotalAmount;
	private Label outwardMakerLblCurrentPage;

	private Button outwardMakerBtnFirst;
	private Button outwardMakerBtnPrevious;
	private Button outwardMakerBtnNext;
	private Button outwardMakerBtnLast;
	private Button outwardMakerBtnBack;

	private ScanService scanService;

	private ScanBatch scanBatch;

	private List<ScanCheque> chequeList = new ArrayList<>();

	private int currentPage = 1;

	@Override
	public void doAfterCompose(Component component) throws Exception {

		super.doAfterCompose(component);

		scanService = new ScanServiceImpl();

		outwardMakerGridChequeDetails = (Grid) component.getFellow("outwardMakerGridChequeDetails");

		outwardMakerRowsChequeDetails = (Rows) component.getFellow("outwardMakerRowsChequeDetails");

		outwardMakerVlayoutEmptyState = (Vlayout) component.getFellow("outwardMakerVlayoutEmptyState");

		outwardMakerLblBatchId = (Label) component.getFellow("outwardMakerLblBatchId");

		outwardMakerLblChequeCount = (Label) component.getFellow("outwardMakerLblChequeCount");

		outwardMakerLblTotalAmount = (Label) component.getFellow("outwardMakerLblTotalAmount");

		outwardMakerLblCurrentPage = (Label) component.getFellow("outwardMakerLblCurrentPage");

		outwardMakerBtnFirst = (Button) component.getFellow("outwardMakerBtnFirst");

		outwardMakerBtnPrevious = (Button) component.getFellow("outwardMakerBtnPrevious");

		outwardMakerBtnNext = (Button) component.getFellow("outwardMakerBtnNext");

		outwardMakerBtnLast = (Button) component.getFellow("outwardMakerBtnLast");

		outwardMakerBtnBack = (Button) component.getFellow("outwardMakerBtnBack");

		outwardMakerBtnFirst.addEventListener("onClick", event -> goToFirstPage());

		outwardMakerBtnPrevious.addEventListener("onClick", event -> goToPreviousPage());

		outwardMakerBtnNext.addEventListener("onClick", event -> goToNextPage());

		outwardMakerBtnLast.addEventListener("onClick", event -> goToLastPage());

		outwardMakerBtnBack.addEventListener("onClick", event -> goBackToDashboard());

		loadBatchDetails();
	}

	private void loadBatchDetails() {

		try {

			Object batchIdObject = null;

			Component component = outwardMakerRowsChequeDetails;

			Component parent = component.getParent();

			while (parent != null) {

				Object attribute = parent.getAttribute("OUTWARD_MAKER_SELECTED_BATCH_ID");

				if (attribute != null) {

					batchIdObject = attribute;

					break;
				}

				parent = parent.getParent();
			}

			if (batchIdObject == null) {

				batchIdObject = Executions.getCurrent().getAttribute("batchId");
			}

			if (batchIdObject == null) {

				batchIdObject = Executions.getCurrent().getSession().getAttribute("OUTWARD_MAKER_SELECTED_BATCH_ID");
			}

			if (batchIdObject == null) {

				showEmptyState();

				return;
			}

			String batchId = batchIdObject.toString().trim();

			if (batchId.isEmpty()) {

				showEmptyState();

				return;
			}

			scanBatch = scanService.getBatchById(batchId);

			if (scanBatch == null) {

				showEmptyState();

				return;
			}

			chequeList = scanService.getChequesByBatchId(batchId);

			if (chequeList == null) {

				chequeList = new ArrayList<>();
			}

			populateBatchSummary();

			currentPage = 1;

			renderCurrentPage();

		} catch (Exception e) {

			e.printStackTrace();

			scanBatch = null;

			chequeList = new ArrayList<>();

			showEmptyState();
		}
	}

	private void populateBatchSummary() {

		outwardMakerLblBatchId.setValue(getValue(scanBatch.getScannedBatchId()));

		outwardMakerLblChequeCount.setValue(String.valueOf(scanBatch.getActualChequeCount()));

		outwardMakerLblTotalAmount.setValue(formatAmount(scanBatch.getActualTotalAmount()));
	}

	private void renderCurrentPage() {

		outwardMakerRowsChequeDetails.getChildren().clear();

		if (chequeList == null || chequeList.isEmpty()) {

			showEmptyState();

			updatePagination();

			return;
		}

		outwardMakerVlayoutEmptyState.setVisible(false);

		outwardMakerGridChequeDetails.setVisible(true);

		int totalPages = getTotalPages();

		if (currentPage > totalPages) {

			currentPage = totalPages;
		}

		int startIndex = (currentPage - 1) * PAGE_SIZE;

		int endIndex = Math.min(startIndex + PAGE_SIZE, chequeList.size());

		for (int index = startIndex; index < endIndex; index++) {

			ScanCheque cheque = chequeList.get(index);

			if (cheque != null) {

				createChequeRow(cheque);
			}
		}

		updatePagination();
	}

	private void createChequeRow(ScanCheque cheque) {

		Row row = new Row();

		Label chequeNumberLabel = new Label(getValue(cheque.getChequeNumber()));

		chequeNumberLabel.setSclass("outward-maker-cheque-number");

		Label payeeAccountLabel = new Label(getValue(cheque.getPayeeAccountNumber()));

		payeeAccountLabel.setSclass("outward-maker-payee-account");

		Label chequeDateLabel = new Label(getValue(cheque.getChequeDate()));

		chequeDateLabel.setSclass("outward-maker-cheque-date");

		Label micrCodeLabel = new Label(getValue(cheque.getMicrCode()));

		micrCodeLabel.setSclass("outward-maker-micr-code");

		Label chequeStatusLabel = new Label(getValue(cheque.getChequeStatus()));

		chequeStatusLabel.setSclass("outward-maker-cheque-status " + getStatusClass(cheque.getChequeStatus()));

		Label chequeAmountLabel = new Label(formatAmount(cheque.getChequeAmount()));

		chequeAmountLabel.setSclass("outward-maker-cheque-amount");

		Component actionComponent = createActionComponent(cheque);

		row.appendChild(chequeNumberLabel);

		row.appendChild(payeeAccountLabel);

		row.appendChild(chequeDateLabel);

		row.appendChild(micrCodeLabel);

		row.appendChild(chequeStatusLabel);

		row.appendChild(chequeAmountLabel);

		row.appendChild(actionComponent);

		outwardMakerRowsChequeDetails.appendChild(row);
	}

	private Component createActionComponent(ScanCheque cheque) {

		String status = cheque.getChequeStatus();

		if (status == null || status.trim().isEmpty()) {

			return new Label("-");
		}

		String normalizedStatus = status.trim().toUpperCase();

		if (normalizedStatus.equals("MICR_REPAIR") || normalizedStatus.equals("MICR_REPAIR_REQUIRED")
				|| normalizedStatus.equals("PENDING_MICR_REPAIR")) {

			Button button = new Button("MICR REPAIR REQUIRED");

			button.setSclass("outward-maker-action-button");

			button.addEventListener("onClick", event -> openMicrRepair(cheque));

			return button;
		}

		if (normalizedStatus.equals("DATA_ENTRY")) {

			Button button = new Button("DATA ENTRY REQUIRED");

			button.setSclass("outward-maker-action-button");

			button.addEventListener("onClick", event -> openDataEntry(cheque));

			return button;
		}

		return new Label("-");
	}

	private void openMicrRepair(ScanCheque cheque) {

		if (cheque == null || cheque.getScannedChequeId() == null) {

			return;
		}

		Component root = outwardMakerRowsChequeDetails.getPage().getFirstRoot();

		Component mainContentArea = root.getFellowIfAny("mainContentArea", true);

		if (!(mainContentArea instanceof Include)) {

			return;
		}

		Include include = (Include) mainContentArea;

		include.clearDynamicProperties();

		include.setDynamicProperty("chequeId", cheque.getScannedChequeId());

		include.setSrc("/outward/maker/micr-repair/micr-repair-view.zul");
	}

	private void openDataEntry(ScanCheque cheque) {

		if (cheque == null || cheque.getScannedChequeId() == null) {

			return;
		}

		Component root = outwardMakerRowsChequeDetails.getPage().getFirstRoot();

		Component mainContentArea = root.getFellowIfAny("mainContentArea", true);

		if (!(mainContentArea instanceof Include)) {

			return;
		}

		Include include = (Include) mainContentArea;

		include.clearDynamicProperties();

		include.setDynamicProperty("chequeId", cheque.getScannedChequeId());

		include.setSrc("/maker/data-entry.zul");
	}

	private void goBackToDashboard() {

		Component root = outwardMakerRowsChequeDetails.getPage().getFirstRoot();

		Component mainContentArea = root.getFellowIfAny("mainContentArea", true);

		if (!(mainContentArea instanceof Include)) {

			return;
		}

		Include include = (Include) mainContentArea;

		include.clearDynamicProperties();

		include.setSrc("/outward/maker/dashboard.zul");
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

		if (chequeList == null || chequeList.isEmpty()) {

			return 1;
		}

		return (int) Math.ceil((double) chequeList.size() / PAGE_SIZE);
	}

	private void updatePagination() {

		int totalPages = getTotalPages();

		outwardMakerLblCurrentPage.setValue(currentPage + " / " + totalPages);

		outwardMakerBtnFirst.setDisabled(currentPage <= 1);

		outwardMakerBtnPrevious.setDisabled(currentPage <= 1);

		outwardMakerBtnNext.setDisabled(currentPage >= totalPages);

		outwardMakerBtnLast.setDisabled(currentPage >= totalPages);
	}

	private void showEmptyState() {

		outwardMakerGridChequeDetails.setVisible(false);

		outwardMakerVlayoutEmptyState.setVisible(true);

		outwardMakerLblBatchId.setValue("-");

		outwardMakerLblChequeCount.setValue("0");

		outwardMakerLblTotalAmount.setValue("₹0.00");
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

			return "pending";
		}

		String normalizedStatus = status.trim().toLowerCase();

		if (normalizedStatus.contains("micr")) {

			return "micr-repair";
		}

		if (normalizedStatus.contains("data_entry")) {

			return "data-entry";
		}

		if (normalizedStatus.contains("completed") || normalizedStatus.contains("success")
				|| normalizedStatus.contains("verified")) {

			return "completed";
		}

		if (normalizedStatus.contains("rejected") || normalizedStatus.contains("failed")) {

			return "rejected";
		}

		return "pending";
	}
}