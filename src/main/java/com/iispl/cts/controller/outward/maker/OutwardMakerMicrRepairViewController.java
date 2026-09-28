package com.iispl.cts.controller.outward.maker;

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
import org.zkoss.zk.ui.event.SelectEvent;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;

import com.iispl.cts.dto.MicrRepairBatch;
import com.iispl.cts.dto.MicrRepairChequeDTO;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.service.outward.OutwardMakerService;
import com.iispl.cts.serviceimpl.outward.OutwardMakerServiceImpl;

public class OutwardMakerMicrRepairViewController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;

	private Grid grdMicrRepairBatches;
	private Rows rowsMicrRepairBatches;
	private Paging pagingMicrRepair;
	private Div divMicrRepairEmpty;

	private Textbox batchIdFilter;
	private Combobox cmbStatusFilter;
	private Label batchResultCount;

	private OutwardMakerService outwardMakerService;

	private List<MicrRepairBatchDisplay> allBatches = new ArrayList<>();
	private List<MicrRepairBatchDisplay> filteredBatches = new ArrayList<>();

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		outwardMakerService = new OutwardMakerServiceImpl();

		if (cmbStatusFilter != null && cmbStatusFilter.getItemCount() > 0) {
			cmbStatusFilter.setSelectedIndex(0);
		}

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
		List<MicrRepairBatch> scanBatches = new ArrayList<>();
		List<MicrRepairBatch> checkerReturnedBatches = new ArrayList<>();

		try {
			List<MicrRepairBatch> result = outwardMakerService.getScanMicrRepairBatches();
			if (result != null) {
				scanBatches = result;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		try {
			List<MicrRepairBatch> result = outwardMakerService.getOutwardMicrRepairBatches();
			if (result != null) {
				checkerReturnedBatches = result;
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		buildUnifiedBatchList(scanBatches, checkerReturnedBatches);
		applyCombinedFilter(null, null);
	}

	private void buildUnifiedBatchList(List<MicrRepairBatch> scanBatches,
			List<MicrRepairBatch> checkerReturnedBatches) {

		allBatches = new ArrayList<>();

		if (scanBatches != null) {
			for (MicrRepairBatch batch : scanBatches) {
				if (batch == null) {
					continue;
				}
				allBatches.add(new MicrRepairBatchDisplay(batch, BatchSource.SCAN));
			}
		}

		if (checkerReturnedBatches != null) {
			for (MicrRepairBatch batch : checkerReturnedBatches) {
				if (batch == null) {
					continue;
				}
				allBatches.add(new MicrRepairBatchDisplay(batch, BatchSource.OUTWARD));
			}
		}
	}

	private void applyCombinedFilter(String searchText, String statusFilter) {
		if (searchText == null) {
			searchText = batchIdFilter != null && batchIdFilter.getValue() != null
					? batchIdFilter.getValue().trim().toLowerCase()
					: "";
		} else {
			searchText = searchText.trim().toLowerCase();
		}

		if (statusFilter == null) {
			if (cmbStatusFilter != null && cmbStatusFilter.getSelectedItem() != null
					&& cmbStatusFilter.getSelectedItem().getValue() != null) {
				statusFilter = cmbStatusFilter.getSelectedItem().getValue().toString();
			} else {
				statusFilter = "ALL";
			}
		}

		filteredBatches = new ArrayList<>();

		for (MicrRepairBatchDisplay displayBatch : allBatches) {
			if (displayBatch == null || displayBatch.getBatch() == null) {
				continue;
			}

			MicrRepairBatch batch = displayBatch.getBatch();

			boolean matchesSearch = true;
			if (!searchText.isEmpty()) {
				String batchId = batch.getBatchId();
				matchesSearch = batchId != null && batchId.toLowerCase().contains(searchText);
			}

			boolean matchesStatus = true;
			if (!"ALL".equalsIgnoreCase(statusFilter)) {
				if ("PENDING_MAKER".equalsIgnoreCase(statusFilter)) {
					matchesStatus = displayBatch.getSource() == BatchSource.SCAN;
				} else if ("CHECKER_RETURNED".equalsIgnoreCase(statusFilter)) {
					matchesStatus = displayBatch.getSource() == BatchSource.OUTWARD;
				}
			}

			if (matchesSearch && matchesStatus) {
				filteredBatches.add(displayBatch);
			}
		}

		setupPagination();
	}

	private void setupPagination() {
		int totalSize = filteredBatches.size();

		if (batchResultCount != null) {
			batchResultCount.setValue(totalSize + (totalSize == 1 ? " Batch" : " Batches"));
		}

		if (totalSize == 0) {
			if (grdMicrRepairBatches != null) {
				grdMicrRepairBatches.setVisible(false);
			}
			if (pagingMicrRepair != null) {
				pagingMicrRepair.setVisible(false);
			}
			if (divMicrRepairEmpty != null) {
				divMicrRepairEmpty.setVisible(true);
			}
			return;
		}

		if (grdMicrRepairBatches != null) {
			grdMicrRepairBatches.setVisible(true);
		}
		if (divMicrRepairEmpty != null) {
			divMicrRepairEmpty.setVisible(false);
		}
		if (pagingMicrRepair != null) {
			pagingMicrRepair.setTotalSize(totalSize);
			pagingMicrRepair.setActivePage(0);
			pagingMicrRepair.setVisible(totalSize > pagingMicrRepair.getPageSize());
		}

		renderPage();
	}

	private void renderPage() {
		if (rowsMicrRepairBatches == null) {
			return;
		}

		rowsMicrRepairBatches.getChildren().clear();
		int pageSize = pagingMicrRepair != null ? pagingMicrRepair.getPageSize() : 10;
		int activePage = pagingMicrRepair != null ? pagingMicrRepair.getActivePage() : 0;
		int startIndex = activePage * pageSize;
		int endIndex = Math.min(startIndex + pageSize, filteredBatches.size());

		for (int i = startIndex; i < endIndex; i++) {
			MicrRepairBatchDisplay displayBatch = filteredBatches.get(i);
			if (displayBatch != null && displayBatch.getBatch() != null) {
				createBatchRow(displayBatch);
			}
		}
	}

	private void createBatchRow(MicrRepairBatchDisplay displayBatch) {
		MicrRepairBatch batch = displayBatch.getBatch();
		Row row = new Row();

		Label batchIdLabel = new Label(getValue(batch.getBatchId()));
		batchIdLabel.setSclass("outward-micr-repair-batch-id");
		batchIdLabel.addEventListener("onClick", event -> openBatch(batch.getBatchId(), displayBatch.getSource()));

		Label dateLabel = new Label(formatDate(batch.getScanDate()));
		dateLabel.setSclass("outward-micr-repair-cell-text");

		Label totalChequesLabel = new Label(String.valueOf(batch.getTotalCheques()));
		totalChequesLabel.setSclass("outward-micr-repair-count-text");

		// Fetch and display remaining pending MICR errors dynamically
		int pendingMicrCount = calculatePendingMicrErrors(batch.getBatchId(), displayBatch.getSource());
		Label micrErrorsLabel = new Label(String.valueOf(pendingMicrCount));
		micrErrorsLabel.setSclass("outward-micr-repair-pending-count");

		Label statusLabel = new Label();
		if (displayBatch.getSource() == BatchSource.OUTWARD) {
			statusLabel.setValue("Checker Returned");
			statusLabel.setSclass("outward-micr-repair-status-returned");
		} else {
			statusLabel.setValue("Pending Maker");
			statusLabel.setSclass("outward-micr-repair-status-pending");
		}
		statusLabel.setStyle("text-transform: none;");

		Button actionButton = new Button("Open");
		actionButton.setSclass("outward-micr-repair-action-button");
		actionButton.addEventListener("onClick", event -> openBatch(batch.getBatchId(), displayBatch.getSource()));

		row.appendChild(batchIdLabel);
		row.appendChild(dateLabel);
		row.appendChild(totalChequesLabel);
		row.appendChild(micrErrorsLabel);
		row.appendChild(statusLabel);
		row.appendChild(actionComponentWrapper(actionButton));
		rowsMicrRepairBatches.appendChild(row);
	}

	// Calculate unresolved pending MICR errors for the given batch
	private int calculatePendingMicrErrors(String batchId, BatchSource source) {
		if (batchId == null || batchId.trim().isEmpty() || outwardMakerService == null) {
			return 0;
		}

		try {
			List<MicrRepairChequeDTO> cheques = null;

			if (source == BatchSource.SCAN) {
				cheques = outwardMakerService.getScanMicrRepairCheques(batchId.trim());
			} else if (source == BatchSource.OUTWARD) {
				cheques = outwardMakerService.getOutwardMicrRepairCheques(batchId.trim());
			}

			if (cheques == null || cheques.isEmpty()) {
				return 0;
			}

			int pendingCount = 0;
			for (MicrRepairChequeDTO cheque : cheques) {
				if (cheque == null || cheque.getChequeStatus() == null) {
					continue;
				}

				String status = cheque.getChequeStatus().trim().toUpperCase();

				// Count only active pending MICR repair statuses
				if ("PENDING_MICR_REPAIR".equals(status)
						) {
					pendingCount++;
				}
			}

			return pendingCount;

		} catch (Exception e) {
			e.printStackTrace();
			return 0;
		}
	}

	private Component actionComponentWrapper(Button button) {
		return button;
	}

	public void onChanging$batchIdFilter(InputEvent event) {
		applyCombinedFilter(event != null ? event.getValue() : "", null);
	}

	public void onChange$batchIdFilter(Event event) {
		applyCombinedFilter(null, null);
	}

	public void onSelect$cmbStatusFilter(SelectEvent<?, ?> event) {
		applyCombinedFilter(null, null);
	}

	public void onSelect$cmbStatusFilter(Event event) {
		applyCombinedFilter(null, null);
	}

	public void onSelect$cmbStatusFilter() {
		applyCombinedFilter(null, null);
	}

	public void clearBatchFilter() {
		if (batchIdFilter != null) {
			batchIdFilter.setValue("");
		}
		if (cmbStatusFilter != null && cmbStatusFilter.getItemCount() > 0) {
			cmbStatusFilter.setSelectedIndex(0);
		}
		applyCombinedFilter("", "ALL");
	}

	public void openBatch(Object batchId, BatchSource source) {
		if (batchId == null) {
			return;
		}

		String batchIdValue = String.valueOf(batchId).trim();
		if (batchIdValue.isEmpty()) {
			return;
		}

		Sessions.getCurrent().setAttribute("MICR_REPAIR_BATCH_ID", batchIdValue);
		Sessions.getCurrent().setAttribute("batchId", batchIdValue);
		Sessions.getCurrent().setAttribute("MICR_REPAIR_SOURCE", source == BatchSource.SCAN ? "SCAN" : "OUTWARD");

		Include mainInclude = null;
		try {
			mainInclude = (Include) Path.getComponent("/outwardMakerRootWin/mainContentArea");
		} catch (Exception ignored) {
		}

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
			String sourceValue = source == BatchSource.SCAN ? "SCAN" : "OUTWARD";
			mainInclude.setAttribute("MICR_REPAIR_SOURCE", sourceValue);
			mainInclude.setAttribute("MICR_REPAIR_BATCH_ID", batchIdValue);
			mainInclude.setSrc(null);
			mainInclude.setSrc("/outward/maker/micr-repair/micr-repair.zul" + "?batchId=" + batchIdValue + "&source=" + sourceValue);
		} else {
			Executions.sendRedirect("/outward/maker/index.zul" + "?page=micr-repair" + "&batchId=" + batchIdValue + "&source=" + (source == BatchSource.SCAN ? "SCAN" : "OUTWARD"));
		}
	}

	private String formatDate(Object date) {
		if (date == null) {
			return "-";
		}
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
		if (value == null || String.valueOf(value).trim().isEmpty()) {
			return "-";
		}
		return String.valueOf(value).trim();
	}

	private enum BatchSource {
		SCAN,
		OUTWARD
	}

	private static class MicrRepairBatchDisplay {
		private final MicrRepairBatch batch;
		private final BatchSource source;

		private MicrRepairBatchDisplay(MicrRepairBatch batch, BatchSource source) {
			this.batch = batch;
			this.source = source;
		}

		private MicrRepairBatch getBatch() {
			return batch;
		}

		private BatchSource getSource() {
			return source;
		}
	}
}