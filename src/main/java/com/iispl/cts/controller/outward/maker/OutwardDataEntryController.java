package com.iispl.cts.controller.outward.maker;

import java.text.SimpleDateFormat;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;

import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.service.outward.OutwardBatchService;
import com.iispl.cts.service.outward.OutwardChequeService;
import com.iispl.cts.serviceimpl.outward.OutwardBatchServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardChequeServiceImpl;

public class OutwardDataEntryController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 1L;

	private Rows outwardDataEntryRowsBatch;
	private Label outwardDataEntryLblEmpty;
	private Label outwardDataEntryLblSessionDate;
	private Label outwardDataEntryLblSessionStatus;
	private Label outwardDataEntryLblSessionDateTime;

	private final OutwardBatchService outwardBatchService;
	private final OutwardChequeService outwardChequeService;

	public OutwardDataEntryController() {

		outwardBatchService = new OutwardBatchServiceImpl();
		outwardChequeService = new OutwardChequeServiceImpl();
	}

	@Override
	public void doAfterCompose(Component component) throws Exception {

		super.doAfterCompose(component);

		outwardDataEntryRowsBatch = (Rows) component.getFellow("outwardDataEntryRowsBatch");

		outwardDataEntryLblEmpty = (Label) component.getFellow("outwardDataEntryLblEmpty");

		outwardDataEntryLblSessionDate = (Label) component.getFellow("outwardDataEntryLblSessionDate");

		outwardDataEntryLblSessionStatus = (Label) component.getFellow("outwardDataEntryLblSessionStatus");

		outwardDataEntryLblSessionDateTime = (Label) component.getFellow("outwardDataEntryLblSessionDateTime");

		loadSessionInformation();
		loadReadyBatches();
	}

	private void loadSessionInformation() {

		SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MMM-yyyy");

		SimpleDateFormat dateTimeFormat = new SimpleDateFormat("dd-MMM-yyyy hh:mm a");

		java.util.Date currentDate = new java.util.Date();

		outwardDataEntryLblSessionDate.setValue(dateFormat.format(currentDate));

		outwardDataEntryLblSessionStatus.setValue("OPEN");

		outwardDataEntryLblSessionDateTime.setValue(dateTimeFormat.format(currentDate));
	}

	private void loadReadyBatches() {

		System.out.println("======================================");
		System.out.println("DATA ENTRY BATCH LOAD STARTED");

		try {

			List<OutwardBatch> batches = outwardBatchService.getBatchesReadyForDataEntry();

			System.out.println("Batches returned: " + (batches == null ? "NULL" : batches.size()));

			if (batches != null) {

				for (OutwardBatch batch : batches) {

					System.out.println("Batch ID: " + batch.getOutwardBatchId() + " | Reference: "
							+ batch.getBatchReferenceId() + " | Status: " + batch.getBatchStatus() + " | Cheques: "
							+ batch.getActualChequeCount());
				}
			}

			renderBatches(batches);

			System.out.println("DATA ENTRY BATCH LOAD COMPLETED");

		} catch (Exception exception) {

			System.out.println("DATA ENTRY BATCH LOAD ERROR");

			exception.printStackTrace();

			outwardDataEntryRowsBatch.getChildren().clear();

			outwardDataEntryLblEmpty.setVisible(true);
		}

		System.out.println("======================================");
	}

	private void renderBatches(List<OutwardBatch> batches) {

		outwardDataEntryRowsBatch.getChildren().clear();

		if (batches == null || batches.isEmpty()) {

			outwardDataEntryLblEmpty.setVisible(true);

			return;
		}

		outwardDataEntryLblEmpty.setVisible(false);

		for (OutwardBatch batch : batches) {

			createBatchRow(batch);
		}
	}

	private void createBatchRow(final OutwardBatch batch) {

		int totalCheques = batch.getActualChequeCount();

		int dataEntered = 0;

		try {

			dataEntered = outwardChequeService.getDataEnteredCountByBatchId(batch.getOutwardBatchId());

		} catch (Exception exception) {

			System.out.println("Unable to get data entered count for batch " + batch.getOutwardBatchId());

			exception.printStackTrace();

			dataEntered = 0;
		}

		if (dataEntered < 0) {
			dataEntered = 0;
		}

		if (dataEntered > totalCheques) {
			dataEntered = totalCheques;
		}

		Row row = new Row();

		Label batchLabel = new Label(getValue(batch.getOutwardBatchId()));

		batchLabel.setSclass("outward-data-entry-batch-id");

		row.appendChild(batchLabel);

		Label totalLabel = new Label(String.valueOf(totalCheques));

		row.appendChild(totalLabel);

		Label enteredLabel = new Label(dataEntered + " / " + totalCheques);

		enteredLabel.setSclass("outward-data-entry-entered");

		row.appendChild(enteredLabel);

		Label statusLabel = new Label("READY");

		statusLabel.setSclass("outward-data-entry-status");

		row.appendChild(statusLabel);

		Button proceedButton = new Button("PROCEED");

		proceedButton.setSclass("outward-data-entry-proceed");

		proceedButton.addEventListener("onClick", event -> openChequeDataEntry(batch));

		row.appendChild(proceedButton);

		outwardDataEntryRowsBatch.appendChild(row);
	}

	private void openChequeDataEntry(OutwardBatch batch) {

		if (batch == null) {
			return;
		}

		String batchId = batch.getOutwardBatchId();

		if (batchId == null || batchId.trim().isEmpty()) {
			return;
		}

		Executions.sendRedirect("/outward/maker/cheque-data-entry.zul?batchId=" + batchId);
	}

	private String getValue(String value) {

		if (value == null || value.trim().isEmpty()) {

			return "-";
		}

		return value;
	}
}
