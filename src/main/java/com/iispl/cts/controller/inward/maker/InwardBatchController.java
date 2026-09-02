package com.iispl.cts.controller.inward.maker;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.service.inward.InwardBatchService;
import com.iispl.cts.serviceimpl.inward.InwardBatchServiceImpl;
import com.iispl.cts.serviceimpl.inward.InwardBatchXmlParser.ParsedBatchData;

public class InwardBatchController extends SelectorComposer<Window> {

	private static final long serialVersionUID = 1L;

	private InwardBatchService inwardBatchService;

	private Listbox batchListbox;

	@Override
	public void doAfterCompose(Window window) throws Exception {

		super.doAfterCompose(window);

		inwardBatchService = new InwardBatchServiceImpl();

		batchListbox = (Listbox) window.getFellow("batchListbox");

		loadBatches();
	}

	private void loadBatches() {

		batchListbox.getItems().clear();

		List<InwardBatch> batches = new ArrayList<>();

		loadXmlBatch("Batch1001new.xml", batches);

		loadXmlBatch("Batch1002_ChequeTransmission.xml", batches);

		for (InwardBatch batch : batches) {

			Listitem item = new Listitem();

			item.setValue(batch);

			Listcell batchCell = new Listcell();

			batchCell.appendChild(new Label(batch.getInwardBatchId()));

			item.appendChild(batchCell);

			Listcell dateCell = new Listcell();

			if (batch.getUploadedAt() != null) {

				dateCell.appendChild(new Label(batch.getUploadedAt().toString()));

			} else {

				dateCell.appendChild(new Label(""));
			}

			item.appendChild(dateCell);

			Listcell amountCell = new Listcell();

			if (batch.getActualTotalAmount() != null) {

				amountCell.appendChild(new Label("₹ " + batch.getActualTotalAmount().toPlainString()));

			} else {

				amountCell.appendChild(new Label("₹ 0"));
			}

			item.appendChild(amountCell);

			Listcell statusCell = new Listcell();

			String status = batch.getBatchStatus();

			if (status == null || status.trim().isEmpty() || "Processing".equalsIgnoreCase(status)
					|| "Pending".equalsIgnoreCase(status)) {

				status = "Pending Validation";
			}

			Label statusLabel = new Label(status);

			setStatusStyle(statusLabel, status);

			statusCell.appendChild(statusLabel);

			item.appendChild(statusCell);

			Listcell actionCell = new Listcell();

			if ("Validated".equalsIgnoreCase(batch.getBatchStatus())) {

				Button repairButton = new Button("Open MICR Repair");

				repairButton.setSclass("repair-button");

				repairButton.addEventListener("onClick", event -> openMicrRepair(item));

				actionCell.appendChild(repairButton);

			} else {

				Button validateButton = new Button("Validate");

				validateButton.setSclass("validate-button");

				validateButton.addEventListener("onClick", event -> validateBatch(event));

				actionCell.appendChild(validateButton);
			}

			item.appendChild(actionCell);

			batchListbox.appendChild(item);
		}
	}

	private void loadXmlBatch(String xmlFileName, List<InwardBatch> batches) {

		try {

			URL resource = Thread.currentThread().getContextClassLoader().getResource(xmlFileName);

			if (resource == null) {
				return;
			}

			ParsedBatchData parsedBatchData = inwardBatchService.parseBatchXml(resource.toURI().getPath());

			if (parsedBatchData == null || parsedBatchData.getInwardBatch() == null) {

				return;
			}

			InwardBatch batch = parsedBatchData.getInwardBatch();

			InwardBatch dbBatch = inwardBatchService.getBatchById(batch.getInwardBatchId());

			if (dbBatch != null) {

				batches.add(dbBatch);

			} else {

				batch.setBatchStatus("Pending Validation");

				batches.add(batch);
			}

		} catch (Exception e) {

			e.printStackTrace();
		}
	}

	private void validateBatch(Event event) {

		try {

			Button clickedButton = (Button) event.getTarget();

			Listcell actionCell = (Listcell) clickedButton.getParent();

			Listitem item = (Listitem) actionCell.getParent();

			InwardBatch queueBatch = (InwardBatch) item.getValue();

			if (queueBatch == null) {

				Messagebox.show("Batch information not found.", "Validation Failed", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			String batchId = queueBatch.getInwardBatchId();

			if (batchId == null || batchId.trim().isEmpty()) {

				Messagebox.show("Batch number is missing.", "Validation Failed", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			String xmlFileName = getXmlFileName(batchId);

			if (xmlFileName == null) {

				Messagebox.show("XML file mapping not found for batch " + batchId, "Validation Failed", Messagebox.OK,
						Messagebox.ERROR);

				return;
			}

			URL resource = Thread.currentThread().getContextClassLoader().getResource(xmlFileName);

			if (resource == null) {

				Messagebox.show(xmlFileName + " not found in src/main/resources.", "Validation Failed", Messagebox.OK,
						Messagebox.ERROR);

				return;
			}

			ParsedBatchData parsedBatchData = inwardBatchService.parseBatchXml(resource.toURI().getPath());

			if (parsedBatchData == null || parsedBatchData.getInwardBatch() == null) {

				Messagebox.show("Unable to read batch XML.", "Validation Failed", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			InwardBatch parsedBatch = parsedBatchData.getInwardBatch();

			String xmlBatchId = parsedBatch.getInwardBatchId();

			if (xmlBatchId == null || xmlBatchId.trim().isEmpty()) {

				Messagebox.show("Batch ID is missing in XML.", "Validation Failed", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			if (!batchId.equalsIgnoreCase(xmlBatchId)) {

				Messagebox.show(
						"Batch number mismatch.\n\n" + "Queue Batch: " + batchId + "\n" + "XML Batch: " + xmlBatchId,
						"Validation Failed", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			parsedBatch.setBatchStatus("Validated");

			boolean saved = inwardBatchService.saveParsedBatch(parsedBatchData);

			if (!saved) {

				Messagebox.show(
						"Validation was successful, " + "but the batch could not be saved " + "in the database.",
						"Database Error", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			item.setValue(parsedBatch);

			updateValidatedRow(item);

			Messagebox.show("Batch " + batchId + " validated and saved successfully.", "Validation Successful",
					Messagebox.OK, Messagebox.INFORMATION);

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Validation failed: " + e.getMessage(), "Validation Failed", Messagebox.OK,
					Messagebox.ERROR);
		}
	}

	private String getXmlFileName(String batchId) {

		if ("BAT1001".equalsIgnoreCase(batchId)) {

			return "Batch1001new.xml";
		}

		if ("BAT1002".equalsIgnoreCase(batchId)) {

			return "Batch1002_ChequeTransmission.xml";
		}

		return null;
	}

	private void updateValidatedRow(Listitem item) {

		Listcell statusCell = (Listcell) item.getChildren().get(3);

		statusCell.getChildren().clear();

		Label statusLabel = new Label("Validated");

		setStatusStyle(statusLabel, "Validated");

		statusCell.appendChild(statusLabel);

		Listcell actionCell = (Listcell) item.getChildren().get(4);

		actionCell.getChildren().clear();

		Button repairButton = new Button("Open MICR Repair");

		repairButton.setSclass("repair-button");

		repairButton.addEventListener("onClick", event -> openMicrRepair(item));

		actionCell.appendChild(repairButton);
	}

	private void openMicrRepair(Listitem item) {

		InwardBatch batch = (InwardBatch) item.getValue();

		Messagebox.show("Opening MICR Repair for batch " + batch.getInwardBatchId(), "MICR Repair", Messagebox.OK,
				Messagebox.INFORMATION);
	}

	private void setStatusStyle(Label label, String status) {

		if ("Validated".equalsIgnoreCase(status)) {

			label.setSclass("status-validated");

		} else if ("Validation Failed".equalsIgnoreCase(status)) {

			label.setSclass("status-failed");

		} else {

			label.setSclass("status-pending");
		}
	}
}