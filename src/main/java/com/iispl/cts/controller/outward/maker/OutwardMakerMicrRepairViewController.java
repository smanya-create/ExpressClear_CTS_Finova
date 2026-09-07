package com.iispl.cts.controller.outward.maker;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Paging;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;

import com.iispl.cts.dto.MicrRepairBatch;
import com.iispl.cts.service.outward.OutwardMakerService;
import com.iispl.cts.serviceimpl.outward.OutwardMakerServiceImpl;

public class OutwardMakerMicrRepairViewController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;

	// =========================================================
	// ZUL COMPONENTS
	// =========================================================

	private Div divMicrRepairScanSection;
	private Div divMicrRepairCheckerSection;
	private Div divMicrRepairEmpty;

	private Label lblMicrRepairEmptyTitle;
	private Label lblMicrRepairEmptyMessage;

	private Rows rowsMicrRepairScanBatches;
	private Rows rowsMicrRepairCheckerBatches;

	private Paging pagingMicrRepairScan;
	private Paging pagingMicrRepairChecker;

	// =========================================================
	// SERVICE
	// =========================================================

	private OutwardMakerService outwardMakerService;

	// =========================================================
	// DATA
	// =========================================================

	private List<MicrRepairBatch> scanBatchRows = new ArrayList<>();

	private List<MicrRepairBatch> checkerBatchRows = new ArrayList<>();

	// =========================================================
	// COMPOSE
	// =========================================================

	@Override
	public void doAfterCompose(Component component) throws Exception {

		super.doAfterCompose(component);

		// =====================================================
		// Get ZUL components
		// =====================================================

		divMicrRepairScanSection = (Div) component.getFellow("divMicrRepairScanSection");

		divMicrRepairCheckerSection = (Div) component.getFellow("divMicrRepairCheckerSection");

		divMicrRepairEmpty = (Div) component.getFellow("divMicrRepairEmpty");

		lblMicrRepairEmptyTitle = (Label) component.getFellow("lblMicrRepairEmptyTitle");

		lblMicrRepairEmptyMessage = (Label) component.getFellow("lblMicrRepairEmptyMessage");

		rowsMicrRepairScanBatches = (Rows) component.getFellow("rowsMicrRepairScanBatches");

		rowsMicrRepairCheckerBatches = (Rows) component.getFellow("rowsMicrRepairCheckerBatches");

		pagingMicrRepairScan = (Paging) component.getFellow("pagingMicrRepairScan");

		pagingMicrRepairChecker = (Paging) component.getFellow("pagingMicrRepairChecker");

		// =====================================================
		// Create service
		// =====================================================

		outwardMakerService = new OutwardMakerServiceImpl();

		// =====================================================
		// Initial state
		// =====================================================

		divMicrRepairScanSection.setVisible(false);

		divMicrRepairCheckerSection.setVisible(false);

		divMicrRepairEmpty.setVisible(false);

		pagingMicrRepairScan.setVisible(false);

		pagingMicrRepairChecker.setVisible(false);

		// =====================================================
		// Load MICR repair batches
		// =====================================================

		loadMicrRepairBatches();
	}

	// =========================================================
	// LOAD ALL MICR REPAIR BATCHES
	// =========================================================

	private void loadMicrRepairBatches() {

		try {

			scanBatchRows.clear();
			checkerBatchRows.clear();

			// =================================================
			// SCAN SOURCE
			// =================================================

			loadScanMicrRepairBatches();

			// =================================================
			// OUTWARD / CHECKER SOURCE
			// =================================================

			loadCheckerReturnedMicrRepairBatches();

			// =================================================
			// DISPLAY
			// =================================================

			displayScanBatches();

			displayCheckerBatches();

			// =================================================
			// EMPTY MESSAGE
			// =================================================

			boolean noScanBatches = scanBatchRows.isEmpty();

			boolean noCheckerBatches = checkerBatchRows.isEmpty();

			if (noScanBatches && noCheckerBatches) {

				divMicrRepairEmpty.setVisible(true);

				lblMicrRepairEmptyTitle.setValue("No MICR repair required");

				lblMicrRepairEmptyMessage.setValue("There are currently no batches requiring MICR repair.");

			} else {

				divMicrRepairEmpty.setVisible(false);
			}

		} catch (Exception e) {

			e.printStackTrace();

			divMicrRepairScanSection.setVisible(false);

			divMicrRepairCheckerSection.setVisible(false);

			divMicrRepairEmpty.setVisible(true);

			lblMicrRepairEmptyTitle.setValue("Unable to load MICR repair batches");

			lblMicrRepairEmptyMessage.setValue("Something went wrong while loading MICR repair batches.");
		}
	}

	// =========================================================
	// SCAN MICR REPAIR BATCHES
	// =========================================================

	private void loadScanMicrRepairBatches() throws Exception {

		/*
		 * Controller calls ONLY OutwardMakerService.
		 *
		 * OutwardMakerServiceImpl internally calls:
		 *
		 * ScanBatchDAO ScanChequeDAO
		 */

		List<MicrRepairBatch> scanBatches = outwardMakerService.getScanMicrRepairBatches();

		if (scanBatches == null) {
			return;
		}

		for (MicrRepairBatch batch : scanBatches) {

			if (batch == null) {
				continue;
			}

			if (batch.getBatchId() == null || batch.getBatchId().trim().isEmpty()) {

				continue;
			}

			/*
			 * DAO has already calculated:
			 *
			 * totalCheques micrErrors
			 *
			 * Therefore controller does NOT calculate them.
			 */

			scanBatchRows.add(batch);
		}
	}

	// =========================================================
	// OUTWARD / CHECKER RETURNED MICR REPAIR BATCHES
	// =========================================================

	private void loadCheckerReturnedMicrRepairBatches() throws Exception {

		/*
		 * Controller calls ONLY OutwardMakerService.
		 *
		 * OutwardMakerServiceImpl internally calls:
		 *
		 * OutwardBatchDAO OutwardChequeDAO
		 */

		List<MicrRepairBatch> outwardBatches = outwardMakerService.getOutwardMicrRepairBatches();

		if (outwardBatches == null) {
			return;
		}

		for (MicrRepairBatch batch : outwardBatches) {

			if (batch == null) {
				continue;
			}

			if (batch.getBatchId() == null || batch.getBatchId().trim().isEmpty()) {

				continue;
			}

			checkerBatchRows.add(batch);
		}
	}

	// =========================================================
	// DISPLAY SCAN BATCHES
	// =========================================================

	private void displayScanBatches() {

		rowsMicrRepairScanBatches.getChildren().clear();

		if (scanBatchRows.isEmpty()) {

			divMicrRepairScanSection.setVisible(false);

			pagingMicrRepairScan.setVisible(false);

			return;
		}

		divMicrRepairScanSection.setVisible(true);

		pagingMicrRepairScan.setVisible(scanBatchRows.size() > 10);

		pagingMicrRepairScan.setTotalSize(scanBatchRows.size());

		pagingMicrRepairScan.setPageSize(10);

		populateScanPage(0);

		pagingMicrRepairScan.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				populateScanPage(pagingMicrRepairScan.getActivePage());
			}
		});
	}

	// =========================================================
	// DISPLAY CHECKER BATCHES
	// =========================================================

	private void displayCheckerBatches() {

		rowsMicrRepairCheckerBatches.getChildren().clear();

		if (checkerBatchRows.isEmpty()) {

			divMicrRepairCheckerSection.setVisible(false);

			pagingMicrRepairChecker.setVisible(false);

			return;
		}

		divMicrRepairCheckerSection.setVisible(true);

		pagingMicrRepairChecker.setVisible(checkerBatchRows.size() > 10);

		pagingMicrRepairChecker.setTotalSize(checkerBatchRows.size());

		pagingMicrRepairChecker.setPageSize(10);

		populateCheckerPage(0);

		pagingMicrRepairChecker.addEventListener("onPaging", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				populateCheckerPage(pagingMicrRepairChecker.getActivePage());
			}
		});
	}

	// =========================================================
	// POPULATE SCAN PAGE
	// =========================================================

	private void populateScanPage(int page) {

		rowsMicrRepairScanBatches.getChildren().clear();

		int pageSize = 10;

		int start = page * pageSize;

		int end = Math.min(start + pageSize, scanBatchRows.size());

		for (int i = start; i < end; i++) {

			MicrRepairBatch batch = scanBatchRows.get(i);

			Row row = new Row();

			// =================================================
			// BATCH ID
			// =================================================

			row.appendChild(new Label(safe(batch.getBatchId())));

			// =================================================
			// SCAN DATE
			// =================================================

			row.appendChild(new Label(formatDate(batch.getScanDate())));

			// =================================================
			// TOTAL CHEQUES
			// =================================================

			row.appendChild(new Label(String.valueOf(batch.getTotalCheques())));

			// =================================================
			// MICR ERRORS
			// =================================================

			row.appendChild(new Label(String.valueOf(batch.getMicrErrors())));

			// =================================================
			// STATUS
			// =================================================

			row.appendChild(new Label(safe(batch.getStatus())));

			// =================================================
			// ACTION
			// =================================================

			Cell actionCell = new Cell();

			Button openButton = new Button("OPEN");

			openButton.setSclass("btn-action-repair");

			final String selectedBatchId = batch.getBatchId();

			openButton.addEventListener(
			        "onClick",
			        new EventListener<Event>() {

			            @Override
			            public void onEvent(Event event) {

			                openMicrRepair(
			                        "SCAN",
			                        selectedBatchId);
			            }
			        });
			actionCell.appendChild(openButton);

			row.appendChild(actionCell);

			rowsMicrRepairScanBatches.appendChild(row);
		}
	}

	// =========================================================
	// POPULATE CHECKER PAGE
	// =========================================================

	private void populateCheckerPage(int page) {

		rowsMicrRepairCheckerBatches.getChildren().clear();

		int pageSize = 10;

		int start = page * pageSize;

		int end = Math.min(start + pageSize, checkerBatchRows.size());

		for (int i = start; i < end; i++) {

			MicrRepairBatch batch = checkerBatchRows.get(i);

			Row row = new Row();

			// =================================================
			// BATCH ID
			// =================================================

			row.appendChild(new Label(safe(batch.getBatchId())));

			// =================================================
			// SCAN DATE
			// =================================================

			row.appendChild(new Label(formatDate(batch.getScanDate())));

			// =================================================
			// TOTAL CHEQUES
			// =================================================

			row.appendChild(new Label(String.valueOf(batch.getTotalCheques())));

			// =================================================
			// MICR ERRORS
			// =================================================

			row.appendChild(new Label(String.valueOf(batch.getMicrErrors())));

			// =================================================
			// STATUS
			// =================================================

			row.appendChild(new Label(safe(batch.getStatus())));

			// =================================================
			// ACTION
			// =================================================

			Cell actionCell = new Cell();

			Button openButton = new Button("OPEN");

			openButton.setSclass("btn-action-repair");

			final String selectedBatchId = batch.getBatchId();

			openButton.addEventListener(
			        "onClick",
			        new EventListener<Event>() {

			            @Override
			            public void onEvent(Event event) {

			                openMicrRepair(
			                        "OUTWARD",
			                        selectedBatchId);
			            }
			        });

			actionCell.appendChild(openButton);

			row.appendChild(actionCell);

			rowsMicrRepairCheckerBatches.appendChild(row);
		}
	}

	// =========================================================
	// OPEN MICR REPAIR PAGE
	// =========================================================

	private void openMicrRepair(String source, String batchId) {

	    if (source == null || source.trim().isEmpty()) {
	        return;
	    }

	    if (batchId == null || batchId.trim().isEmpty()) {
	        return;
	    }

	    String url =
	            "micr-repair.zul"
	            + "?source=" + source.trim()
	            + "&batchId=" + batchId.trim();

	    System.out.println("MICR REPAIR OPEN URL = " + url);

	    Executions.getCurrent().sendRedirect(url);
	}
	// =========================================================
	// FORMAT DATE
	// =========================================================

	private String formatDate(java.sql.Timestamp timestamp) {

		if (timestamp == null) {
			return "-";
		}

		SimpleDateFormat formatter = new SimpleDateFormat("dd-MM-yyyy");

		return formatter.format(timestamp);
	}

	// =========================================================
	// SAFE VALUE
	// =========================================================

	private String safe(String value) {

		if (value == null) {
			return "-";
		}

		return value;
	}
}