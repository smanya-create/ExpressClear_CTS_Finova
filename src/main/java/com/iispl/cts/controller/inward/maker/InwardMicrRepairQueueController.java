package com.iispl.cts.controller.inward.maker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;

import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.service.inward.InwardBatchService;
import com.iispl.cts.serviceimpl.inward.InwardBatchServiceImpl;

public class InwardMicrRepairQueueController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;

	private Grid grdMicrRepairBatches;
	private Rows rowsMicrRepairBatches;
	private Paging pagingMicrRepair;
	private Div divMicrRepairEmpty;

	private Textbox batchIdFilter;
	private Label batchResultCount;

	private InwardBatchService inwardBatchService;
	private List<InwardBatch> allBatches = new ArrayList<>();
	private List<InwardBatch> filteredBatches = new ArrayList<>();

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		this.inwardBatchService = new InwardBatchServiceImpl();

		if (pagingMicrRepair != null) {
			pagingMicrRepair.addEventListener("onPaging", new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					renderPage();
				}
			});
		}

		loadBatchQueue();
	}

	private void loadBatchQueue() {
		List<InwardBatch> batches = inwardBatchService.getBatchesForMicrRepair();

		if (batches == null) {
			this.allBatches = new ArrayList<>();
		} else {
			this.allBatches = new ArrayList<>(batches);
		}

		this.filteredBatches = new ArrayList<>(this.allBatches);
		setupPagination();
	}

	private void setupPagination() {
		int totalSize = filteredBatches.size();

		if (batchResultCount != null) {
			batchResultCount.setValue(totalSize + (totalSize == 1 ? " Batch" : " Batches"));
		}

		if (totalSize == 0) {
			if (grdMicrRepairBatches != null) grdMicrRepairBatches.setVisible(false);
			if (pagingMicrRepair != null) pagingMicrRepair.setVisible(false);
			if (divMicrRepairEmpty != null) divMicrRepairEmpty.setVisible(true);
			return;
		}

		if (grdMicrRepairBatches != null) grdMicrRepairBatches.setVisible(true);
		if (divMicrRepairEmpty != null) divMicrRepairEmpty.setVisible(false);

		if (pagingMicrRepair != null) {
			pagingMicrRepair.setTotalSize(totalSize);
			pagingMicrRepair.setActivePage(0);
			pagingMicrRepair.setVisible(totalSize > pagingMicrRepair.getPageSize());
		}

		renderPage();
	}

	private void renderPage() {
		if (rowsMicrRepairBatches == null) return;
		rowsMicrRepairBatches.getChildren().clear();

		int pageSize = (pagingMicrRepair != null) ? pagingMicrRepair.getPageSize() : 10;
		int activePage = (pagingMicrRepair != null) ? pagingMicrRepair.getActivePage() : 0;

		int startIndex = activePage * pageSize;
		int endIndex = Math.min(startIndex + pageSize, filteredBatches.size());

		for (int i = startIndex; i < endIndex; i++) {
			InwardBatch batch = filteredBatches.get(i);
			if (batch != null) {
				createBatchRow(batch);
			}
		}
	}

	private void createBatchRow(InwardBatch batch) {
		Row row = new Row();

		// Batch ID link
		Label batchIdLabel = new Label(getValue(batch.getInwardBatchId()));
		batchIdLabel.setSclass("micr-repair-batch-id");
		batchIdLabel.addEventListener("onClick", event -> openBatch(batch.getInwardBatchId()));

		// Received Date
		Label dateLabel = new Label(formatDate(batch.getUploadedAt()));
		dateLabel.setSclass("micr-repair-cell-text");

		// Total Cheques
		Label totalChequesLabel = new Label(String.valueOf(batch.getActualChequeCount()));
		totalChequesLabel.setSclass("micr-repair-count-text");

		// MICR Errors / Pending
		Label micrErrorsLabel = new Label(String.valueOf(batch.getMicrRepairPendingCount()));
		micrErrorsLabel.setSclass("micr-repair-pending-count");

		// Status Pill
		Label statusLabel = new Label("PENDING_MAKER_PROCESS");
		statusLabel.setSclass("micr-repair-status");

		// Action Button (Outward Style: OPEN)
		Button actionButton = new Button("OPEN");
		actionButton.setSclass("btn-action-repair");
		actionButton.addEventListener("onClick", event -> openBatch(batch.getInwardBatchId()));

		row.appendChild(batchIdLabel);
		row.appendChild(dateLabel);
		row.appendChild(totalChequesLabel);
		row.appendChild(micrErrorsLabel);
		row.appendChild(statusLabel);
		row.appendChild(actionComponentWrapper(actionButton));

		rowsMicrRepairBatches.appendChild(row);
	}

	private Component actionComponentWrapper(Button button) {
		return button;
	}

	public void onChanging$batchIdFilter(InputEvent event) {
		String searchText = event.getValue();
		if (searchText == null) searchText = "";
		searchText = searchText.trim().toLowerCase();

		if (searchText.isEmpty()) {
			this.filteredBatches = new ArrayList<>(this.allBatches);
		} else {
			this.filteredBatches = new ArrayList<>();
			for (InwardBatch batch : allBatches) {
				if (batch != null && batch.getInwardBatchId() != null) {
					if (batch.getInwardBatchId().toLowerCase().contains(searchText)) {
						this.filteredBatches.add(batch);
					}
				}
			}
		}

		setupPagination();
	}

	public void clearBatchFilter() {
		if (batchIdFilter != null) {
			batchIdFilter.setValue("");
		}
		this.filteredBatches = new ArrayList<>(this.allBatches);
		setupPagination();
	}

	public void openBatch(Object batchId) {
		if (batchId == null) return;
		String batchIdValue = String.valueOf(batchId).trim();
		if (batchIdValue.isEmpty()) return;

		Sessions.getCurrent().setAttribute("MICR_REPAIR_BATCH_ID", batchIdValue);
		Sessions.getCurrent().setAttribute("batchId", batchIdValue);

		Include mainInclude = null;
		try {
			mainInclude = (Include) Path.getComponent("/inwardMakerRootWin/mainContentArea");
		} catch (Exception ignored) {}

		if (mainInclude == null && self != null && self.getDesktop() != null) {
			for (org.zkoss.zk.ui.Page page : self.getDesktop().getPages()) {
				Component component = page.getFellowIfAny("mainContentArea", true);
				if (component instanceof Include) {
					mainInclude = (Include) component;
					break;
				}
			}
		}

		if (mainInclude != null) {
			mainInclude.setSrc(null);
			mainInclude.setSrc("/inward/maker/micr-repair/micr-repair.zul?batchId=" + batchIdValue);
		} else {
			Executions.sendRedirect("/inward/maker/index.zul?page=micr-repair&batchId=" + batchIdValue);
		}
	}

	private String formatDate(Object date) {
		if (date == null) return "-";
		try {
			if (date instanceof java.util.Date) {
				return new SimpleDateFormat("dd-MM-yyyy").format((java.util.Date) date);
			}
			return date.toString().trim();
		} catch (Exception e) {
			return "-";
		}
	}

	private String getValue(Object value) {
		return (value == null || String.valueOf(value).trim().isEmpty()) ? "-" : String.valueOf(value).trim();
	}
}