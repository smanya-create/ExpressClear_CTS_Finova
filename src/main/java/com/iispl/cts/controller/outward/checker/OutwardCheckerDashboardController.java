package com.iispl.cts.controller.outward.checker;

import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.iispl.cts.dao.outward.OutwardBatchDashboardDAO;
import com.iispl.cts.daoimpl.outward.OutwardBatchDashboardDAOImpl;
import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.enums.OutwardBatchStatus;
import com.iispl.cts.enums.OutwardChequeStatus;
import com.iispl.cts.service.outward.OutwardBatchService;
import com.iispl.cts.service.outward.OutwardCheckerQueueService;
import com.iispl.cts.service.outward.OutwardChequeService;
import com.iispl.cts.serviceimpl.outward.OutwardBatchServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardCheckerQueueServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardChequeServiceImpl;

public class OutwardCheckerDashboardController extends GenericForwardComposer<Component> {

	private Textbox txtBatchId;

	private Button btnSearch;
	private Button btnClear;
	private Combobox cmbStatus;

	private Listbox lstBatches;

	private Button btnPrevious;
	private Button btnNext;
	private Button btnFirst;
	private Button btnLast;

	private Label lblPage;

	private final OutwardBatchService outwardBatchService = new OutwardBatchServiceImpl();
	private final OutwardChequeService outwardChequeService = new OutwardChequeServiceImpl();

	private final OutwardCheckerQueueService outwardCheckerQueueService = new OutwardCheckerQueueServiceImpl();

	private final OutwardBatchDashboardDAO outwardBatchDashboardDAO = new OutwardBatchDashboardDAOImpl();

	int pageNumber = 1;
	int pageSize = 5;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		cmbStatus.setSelectedIndex(0);
		setupPagination();
		setupSearch();
		setupClear();

		loadDashboard();
	}

	// handling pagenation
	private void setupPagination() {
		btnPrevious.addEventListener(Events.ON_CLICK, event -> {
			if (pageNumber > 1) {
				pageNumber--;
				loadDashboard();
			}
		});

		btnNext.addEventListener(Events.ON_CLICK, event -> {
			int totalBatches = getTotalBatchCount();
			int totalPages = calculateTotalPages(totalBatches);
			if (pageNumber < totalPages) {
				pageNumber++;
				loadDashboard();
			}
		});
		btnFirst.addEventListener(Events.ON_CLICK, event -> {
			if (pageNumber > 1) {
				pageNumber = 1;
				loadDashboard();
			}
		});
		btnLast.addEventListener(Events.ON_CLICK, event -> {
			int totalBatches = getTotalBatchCount();
			int totalPages = calculateTotalPages(totalBatches);

			if (totalPages > 0 && pageNumber < totalPages) {
				pageNumber = totalPages;
				loadDashboard();
			}
		});
	}

	private void setupSearch() {
		btnSearch.addEventListener(Events.ON_CLICK, event -> {
			pageNumber = 1;
			loadDashboard();
		});
	}

	private void setupClear() {
		btnClear.addEventListener(Events.ON_CLICK, event -> {
			txtBatchId.setValue("");
			cmbStatus.setSelectedIndex(0);
			pageNumber = 1;
			loadDashboard();
		});
	}

	// checking search is in active or not
	private boolean isSearchActive() {
		String batchId = txtBatchId.getValue();

		boolean batchIdEntered = batchId != null && !batchId.trim().isEmpty();

		boolean statusSelected = !"ALL".equalsIgnoreCase(getSelectedStatus());

		return batchIdEntered || statusSelected;
	}

	// Getting total batch count
	private int getTotalBatchCount() {
		if (isSearchActive()) {
			return outwardBatchDashboardDAO.getSearchPendingBatchCount(txtBatchId.getValue(), getSelectedStatus());
		}

		return outwardBatchService.getPendingBatchCount();
	}

	private String getSelectedStatus() {
		Comboitem selectedItem = cmbStatus.getSelectedItem();

		if (selectedItem == null) {
			return "ALL";
		}

		String status = selectedItem.getValue();

		return status == null ? "ALL" : status;
	}

	private int calculateTotalPages(int totalBatches) {

		return (int) Math.ceil((double) totalBatches / pageSize);
	}

	// loading dashboard
	private void loadDashboard() {

		try {

			List<OutwardBatch> pendingBatches;
			int totalBatches;

			if (isSearchActive()) {

				String batchId = txtBatchId.getValue();
				String status = getSelectedStatus();

				pendingBatches = outwardBatchDashboardDAO.searchPendingBatches(pageNumber, pageSize, batchId, status);

				totalBatches = outwardBatchDashboardDAO.getSearchPendingBatchCount(batchId, status);

				if (totalBatches == 0) {
					Messagebox.show("No batches found for the given search criteria.", "No Results", Messagebox.OK,
							Messagebox.INFORMATION);

					if (batchId != null && !batchId.trim().isEmpty()) {
						txtBatchId.focus();
					}
				}

			} else {

				pendingBatches = outwardBatchService.getPendingBatches(pageNumber, pageSize);

				totalBatches = outwardBatchService.getPendingBatchCount();
			}

			// Calculating total pages based on total batch count
			int totalPages = calculateTotalPages(totalBatches);

			updatePagination(totalPages);

			setBatchList(pendingBatches);

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to load Checker Dashboard.\n\n" + e.getMessage(), "Dashboard Error", Messagebox.OK,
					Messagebox.ERROR);
		}
	}

	private void updatePagination(int totalPages) {

		if (totalPages == 0) {

			pageNumber = 0;

			lblPage.setValue("0/0");

			btnFirst.setDisabled(true);
			btnPrevious.setDisabled(true);
			btnNext.setDisabled(true);
			btnLast.setDisabled(true);

			return;
		}
		if (pageNumber < 1) {
			pageNumber = 1;
		}
		if (pageNumber > totalPages) {
			pageNumber = totalPages;
		}

		lblPage.setValue(pageNumber + "/" + totalPages);

		btnFirst.setDisabled(pageNumber == 1);

		btnPrevious.setDisabled(pageNumber == 1);

		btnNext.setDisabled(pageNumber == totalPages);

		btnLast.setDisabled(pageNumber == totalPages);
	}

	private void setBatchList(List<OutwardBatch> pendingBatches) {

		ListModelList<OutwardBatch> model = new ListModelList<>(pendingBatches);

		lstBatches.setModel(model);
		lstBatches.setItemRenderer(new ListitemRenderer<OutwardBatch>() {

			@Override
			public void render(Listitem item, OutwardBatch batch, int index) throws Exception {
				addBatchRow(item, batch);
			}
		});
	}

	private void addBatchRow(Listitem item, OutwardBatch batch) throws SQLException {

		item.setValue(batch);
		item.appendChild(new Listcell(batch.getOutwardBatchId()));

		Map<String, Integer> chequeCounts = getChequeCounts(batch.getOutwardBatchId());

		int totalCheques = chequeCounts.get("total");
		int normalCheques = chequeCounts.get("normal");
		int makerReturned = chequeCounts.get("makerReturned");
		int rejectionRequests = chequeCounts.get("rejection");

		item.appendChild(new Listcell(String.valueOf(totalCheques)));

		item.appendChild(new Listcell(String.valueOf(normalCheques)));

		Listcell makerReturnedCell = new Listcell(String.valueOf(makerReturned));
		makerReturnedCell.setSclass("maker-returned-count");
		item.appendChild(makerReturnedCell);

		Listcell rejectionCell = new Listcell(String.valueOf(rejectionRequests));
		rejectionCell.setSclass("rejection-request-count");

		item.appendChild(rejectionCell);

		addStatusCell(item, batch);
		addActionCell(item, batch);
	}

	private void addStatusCell(Listitem item, OutwardBatch batch) {
		Listcell statusCell = new Listcell();
		statusCell.setStyle("text-align:center;" + "vertical-align:middle;");

		String batchStatus = batch.getBatchStatus();
		String displayStatus = getDisplayStatus(batchStatus);

		Label statusLabel = new Label(displayStatus);
		statusLabel.setSclass("status-pending");

		statusLabel.setStyle("display:inline-block;" + "background:#fff7ed;" + "color:#b45309;"
				+ "border:1px solid #f59e0b;" + "border-radius:16px;" + "font-size:8px;" + "font-weight:800;"
				+ "text-align:center;" + "white-space:nowrap;");

		statusCell.appendChild(statusLabel);
		item.appendChild(statusCell);
	}

	private String getDisplayStatus(String batchStatus) {
		if (OutwardBatchStatus.PENDING_CHECKER_PROCESS.toString().equalsIgnoreCase(batchStatus)) {
			return "Pending";
		}
		if (OutwardBatchStatus.ON_HOLD.toString().equalsIgnoreCase(batchStatus)) {
			return "On Hold";
		}
		return batchStatus;
	}

	private void addActionCell(Listitem item, OutwardBatch batch) {
		Listcell actionCell = new Listcell();

		String batchStatus = batch.getBatchStatus();
		String buttonLabel = getButtonLabel(batchStatus);

		Button queueButton = new Button(buttonLabel);

		queueButton.setSclass("queue-button");
		queueButton.addEventListener(Events.ON_CLICK, event -> openBatchPopup(item));

		actionCell.appendChild(queueButton);

		item.appendChild(actionCell);
	}

	private String getButtonLabel(String batchStatus) {
		if (OutwardBatchStatus.PENDING_CHECKER_PROCESS.toString().equalsIgnoreCase(batchStatus)) {
			return "Proceed";
		}
		if (OutwardBatchStatus.ON_HOLD.toString().equalsIgnoreCase(batchStatus)) {
			return "Proceed";
		}
		return "Proceed";
	}

	// batch proceed popup is displaying when we click on proceed button
	private void openBatchPopup(Listitem item) {

		OutwardBatch selectedBatch = item.getValue();
		Map<String, Object> args = new HashMap<>();
		args.put("batchId", selectedBatch.getOutwardBatchId());
		Window popup = (Window) Executions.createComponents("/outward/checker/batch-proceed-popup.zul", null, args);
		popup.doModal();
	}

	// counting normal cheques,Total cheques,Rejection request cheques,maker
	private Map<String, Integer> getChequeCounts(String batchId) throws SQLException {

		Map<String, Integer> allChequesCount = new HashMap<>();
		int normalCheques = 0;
		int rejectionRequestsCheques = 0;
		int makerReturnedCheques = 0;

		List<OutwardCheque> cheques = outwardChequeService.getChequesByBatchId(batchId);

		if (cheques != null) {
			for (OutwardCheque cheque : cheques) {
				if (cheque == null) {
					continue;
				}
				String chequeStatus = cheque.getChequeStatus();
				if (OutwardChequeStatus.MAKER_RETURNED.toString().equalsIgnoreCase(chequeStatus)) {
					makerReturnedCheques++;

				} else if (OutwardChequeStatus.REJECT_REQUEST.toString().equalsIgnoreCase(chequeStatus)) {
					rejectionRequestsCheques++;

				} else if (OutwardChequeStatus.PENDING_VERIFICATION.toString().equalsIgnoreCase(chequeStatus) || OutwardChequeStatus.VERIFIED_BY_CHECKER.toString().equalsIgnoreCase(chequeStatus)) {
					normalCheques++;
				}
			}
		}
		int totalCheques = normalCheques + makerReturnedCheques + rejectionRequestsCheques;

		allChequesCount.put("total", totalCheques);
		allChequesCount.put("normal", normalCheques);
		allChequesCount.put("rejection", rejectionRequestsCheques);
		allChequesCount.put("makerReturned", makerReturnedCheques);

		return allChequesCount;
	}
}