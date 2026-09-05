package com.iispl.cts.controller.inward.maker;

import java.io.File;
import java.net.URL;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.parser.InwardBatchXmlParser.ParsedBatchData;
import com.iispl.cts.service.inward.InwardBatchService;
import com.iispl.cts.serviceimpl.inward.InwardBatchServiceImpl;

public class InwardBatchController extends SelectorComposer<Window> {

	private static final long serialVersionUID = 1L;

	private InwardBatchService inwardBatchService;

	private Window currentWindow;

	private Listbox batchListbox;

	private Textbox batchSearchTextbox;

	private Combobox statusCombobox;

	private Datebox receivedDatebox;

	private List<InwardBatch> allBatches = new ArrayList<InwardBatch>();

	@Override
	public void doAfterCompose(Window window) throws Exception {

		super.doAfterCompose(window);

		this.currentWindow = window;

		window.getDesktop().enableServerPush(true);

		inwardBatchService = new InwardBatchServiceImpl();

		batchListbox = (Listbox) window.getFellow("batchListbox");

		batchSearchTextbox = (Textbox) window.getFellow("batchSearchTextbox");

		statusCombobox = (Combobox) window.getFellow("statusCombobox");

		receivedDatebox = (Datebox) window.getFellow("receivedDatebox");

		Button searchButton = (Button) window.getFellow("searchButton");

		searchButton.addEventListener("onClick", event -> searchBatches());

		loadBatches();
	}

	private void loadBatches() {

		allBatches.clear();

		addBatchToQueue("INW260904001", "BATCH-2026-09-04-001");

		addBatchToQueue("INW260904002", "BATCH-2026-09-04-002");

		addBatchToQueue("INW260904003", "BATCH-2026-09-04-003");

		displayBatches(allBatches);
	}

	private void addBatchToQueue(String batchId, String folderName) {

		try {

			InwardBatch dbBatch = inwardBatchService.getBatchById(batchId);

			if (dbBatch != null) {

				allBatches.add(dbBatch);

				return;
			}

		} catch (Exception e) {

			e.printStackTrace();
		}

		InwardBatch batch = new InwardBatch();

		batch.setInwardBatchId(batchId);

		batch.setBatchReferenceId(folderName);

		batch.setBatchStatus("Pending Validation");

		batch.setActualChequeCount(0);

		batch.setActualTotalAmount(java.math.BigDecimal.ZERO);

		allBatches.add(batch);
	}

	private void displayBatches(List<InwardBatch> batches) {

		batchListbox.getItems().clear();

		for (InwardBatch batch : batches) {

			Listitem item = new Listitem();

			item.setValue(batch);

			Listcell batchCell = new Listcell();

			String batchId = batch.getInwardBatchId();

			batchCell.appendChild(new Label(batchId == null ? "" : batchId));

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

			String status = getDisplayStatus(batch);

			Label statusLabel = new Label(status);

			setStatusStyle(statusLabel, status);

			statusCell.appendChild(statusLabel);

			item.appendChild(statusCell);

			Listcell actionCell = new Listcell();

			createActionButton(item, actionCell, batch);

			item.appendChild(actionCell);

			batchListbox.appendChild(item);
		}
	}

	private void createActionButton(Listitem item, Listcell actionCell, InwardBatch batch) {

		String status = getDisplayStatus(batch);

		if ("Validated".equalsIgnoreCase(status)) {

			Button repairButton = new Button("Open MICR Repair");

			repairButton.setSclass("repair-button");

			repairButton.addEventListener("onClick", event -> openMicrRepair(item));

			actionCell.appendChild(repairButton);

			Button viewButton = new Button("View");

			viewButton.setSclass("view-button");

			viewButton.addEventListener("onClick", event -> openBatchView(item));

			actionCell.appendChild(viewButton);

			return;
		}

		if ("Parsing".equalsIgnoreCase(status)) {

			Button parsingButton = new Button("Parsing...");

			parsingButton.setDisabled(true);

			parsingButton.setSclass("parsing-button");

			actionCell.appendChild(parsingButton);

			return;
		}

		Button parseButton = new Button("Parse");

		parseButton.setSclass("parse-button");

		parseButton.addEventListener("onClick", event -> parseBatch(event));

		actionCell.appendChild(parseButton);
	}

	private String getDisplayStatus(InwardBatch batch) {

		if (batch == null) {

			return "Pending Validation";
		}

		String status = batch.getBatchStatus();

		if (status == null || status.trim().isEmpty()) {

			return "Pending Validation";
		}

		return status;
	}

	private void parseBatch(Event event) {

		try {

			Button clickedButton = (Button) event.getTarget();

			Listcell actionCell = (Listcell) clickedButton.getParent();

			Listitem item = (Listitem) actionCell.getParent();

			InwardBatch batch = (InwardBatch) item.getValue();

			if (batch == null) {

				Messagebox.show("Batch information not found.", "Parse Failed", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			String batchId = batch.getInwardBatchId();

			if (batchId == null || batchId.trim().isEmpty()) {

				Messagebox.show("Batch number is missing.", "Parse Failed", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			String folderName = getBatchFolderName(batchId);

			if (folderName == null) {

				Messagebox.show("Batch folder not found for: " + batchId, "Parse Failed", Messagebox.OK,
						Messagebox.ERROR);

				return;
			}

			String npciXml = "Inward-data/" + folderName + "/NPCI_Inward.xml";

			String ocrXml = "Inward-data/" + folderName + "/OCR_Mock.xml";

			ClassLoader classLoader = Thread.currentThread().getContextClassLoader();

			URL npciResource = classLoader.getResource(npciXml);

			URL ocrResource = classLoader.getResource(ocrXml);

			if (npciResource == null) {

				Messagebox.show("NPCI XML not found:\n" + npciXml, "Parse Failed", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			if (ocrResource == null) {

				Messagebox.show("OCR XML not found:\n" + ocrXml, "Parse Failed", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			batch.setBatchStatus("Parsing");

			updateParsingRow(item);

			final String npciPath = new File(npciResource.toURI()).getAbsolutePath();

			final String ocrPath = new File(ocrResource.toURI()).getAbsolutePath();

			final String finalBatchId = batchId;

			final String finalFolderName = folderName;

			Thread parsingThread = new Thread(() -> {

				ParseResult result;

				try {

					ParsedBatchData parsedBatchData = inwardBatchService.parseBatchXml(npciPath, ocrPath);

					if (parsedBatchData == null || parsedBatchData.getInwardBatch() == null) {

						result = new ParseResult(finalBatchId, null, "Validation Failed",
								"Unable to parse NPCI and OCR XML.");

					} else {

						InwardBatch parsedBatch = parsedBatchData.getInwardBatch();

						String xmlBatchId = parsedBatch.getInwardBatchId();

						if (xmlBatchId == null || xmlBatchId.trim().isEmpty()) {

							parsedBatch.setInwardBatchId(finalBatchId);

						} else if (!finalBatchId.equalsIgnoreCase(xmlBatchId)) {

							result = new ParseResult(finalBatchId, null, "Validation Failed",
									"Batch number mismatch. Queue Batch: " + finalBatchId + " XML Batch: "
											+ xmlBatchId);

							Executions.schedule(currentWindow.getDesktop(), new EventListener<Event>() {

								@Override
								public void onEvent(Event event) throws Exception {

									handleParseComplete((ParseResult) event.getData());
								}
							}, new Event("onParseComplete", currentWindow, result));

							return;
						}

						if (parsedBatch.getBatchReferenceId() == null
								|| parsedBatch.getBatchReferenceId().trim().isEmpty()) {

							parsedBatch.setBatchReferenceId(finalFolderName);
						}

						if (parsedBatch.getActualChequeCount() <= 0) {

							int chequeCount = parsedBatchData.getInwardCheques() == null ? 0
									: parsedBatchData.getInwardCheques().size();

							parsedBatch.setActualChequeCount(chequeCount);
						}

						parsedBatch.setBatchStatus("Validated");

						boolean saved = inwardBatchService.saveParsedBatch(parsedBatchData);

						if (saved) {

							result = new ParseResult(finalBatchId, parsedBatchData, "Validated", null);

						} else {

							result = new ParseResult(finalBatchId, null, "Validation Failed",
									"Batch parsed successfully but database save failed.");
						}
					}

				} catch (Exception e) {

					e.printStackTrace();

					String errorMessage = e.getMessage();

					if (errorMessage == null || errorMessage.trim().isEmpty()) {

						errorMessage = e.getClass().getSimpleName();
					}

					result = new ParseResult(finalBatchId, null, "Validation Failed", errorMessage);
				}

				final ParseResult finalResult = result;

				Executions.schedule(currentWindow.getDesktop(), new EventListener<Event>() {

					@Override
					public void onEvent(Event event) throws Exception {

						handleParseComplete((ParseResult) event.getData());
					}
				}, new Event("onParseComplete", currentWindow, finalResult));
			});

			parsingThread.setName("InwardBatchParser-" + batchId);

			parsingThread.start();

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to start parsing: " + e.getMessage(), "Parse Failed", Messagebox.OK,
					Messagebox.ERROR);
		}
	}

	private void handleParseComplete(ParseResult result) {

		if (result == null) {
			return;
		}

		try {

			String batchId = result.getBatchId();

			Listitem targetItem = null;

			for (Listitem item : batchListbox.getItems()) {

				InwardBatch itemBatch = (InwardBatch) item.getValue();

				if (itemBatch != null && itemBatch.getInwardBatchId() != null
						&& batchId.equalsIgnoreCase(itemBatch.getInwardBatchId())) {

					targetItem = item;

					break;
				}
			}

			if ("Validated".equalsIgnoreCase(result.getStatus())) {

				ParsedBatchData data = result.getParsedBatchData();

				if (data == null || data.getInwardBatch() == null) {

					return;
				}

				InwardBatch parsedBatch = data.getInwardBatch();

				parsedBatch.setBatchStatus("Validated");

				if (targetItem != null) {

					targetItem.setValue(parsedBatch);

					updateValidatedRow(targetItem);
				}

				for (int i = 0; i < allBatches.size(); i++) {

					InwardBatch current = allBatches.get(i);

					if (current != null && current.getInwardBatchId() != null
							&& batchId.equalsIgnoreCase(current.getInwardBatchId())) {

						allBatches.set(i, parsedBatch);

						break;
					}
				}

				Messagebox.show("Batch " + batchId + " parsed, validated and saved successfully.", "Parsing Successful",
						Messagebox.OK, Messagebox.INFORMATION);

			} else {

				if (targetItem != null) {

					InwardBatch failedBatch = (InwardBatch) targetItem.getValue();

					if (failedBatch != null) {

						failedBatch.setBatchStatus("Validation Failed");
					}

					updateFailedRow(targetItem);
				}

				String message = result.getMessage();

				if (message == null || message.trim().isEmpty()) {

					message = "NPCI/OCR parsing or validation failed.";
				}

				Messagebox.show(message, "Validation Failed", Messagebox.OK, Messagebox.ERROR);
			}

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to update batch status: " + e.getMessage(), "Error", Messagebox.OK,
					Messagebox.ERROR);
		}
	}

	private void updateParsingRow(Listitem item) {

		Listcell statusCell = (Listcell) item.getChildren().get(3);

		statusCell.getChildren().clear();

		Label statusLabel = new Label("Parsing");

		setStatusStyle(statusLabel, "Parsing");

		statusCell.appendChild(statusLabel);

		Listcell actionCell = (Listcell) item.getChildren().get(4);

		actionCell.getChildren().clear();

		Button parsingButton = new Button("Parsing...");

		parsingButton.setDisabled(true);

		parsingButton.setSclass("parsing-button");

		actionCell.appendChild(parsingButton);
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

		Button viewButton = new Button("View");

		viewButton.setSclass("view-button");

		viewButton.addEventListener("onClick", event -> openBatchView(item));

		actionCell.appendChild(viewButton);
	}

	private void updateFailedRow(Listitem item) {

		Listcell statusCell = (Listcell) item.getChildren().get(3);

		statusCell.getChildren().clear();

		Label statusLabel = new Label("Validation Failed");

		setStatusStyle(statusLabel, "Validation Failed");

		statusCell.appendChild(statusLabel);

		Listcell actionCell = (Listcell) item.getChildren().get(4);

		actionCell.getChildren().clear();

		Button parseButton = new Button("Parse");

		parseButton.setSclass("parse-button");

		parseButton.addEventListener("onClick", event -> parseBatch(event));

		actionCell.appendChild(parseButton);
	}

	private void openMicrRepair(Listitem item) {

		InwardBatch batch = (InwardBatch) item.getValue();

		if (batch == null) {
			return;
		}

		Messagebox.show("Opening MICR Repair for batch " + batch.getInwardBatchId(), "MICR Repair", Messagebox.OK,
				Messagebox.INFORMATION);
	}

	private void openBatchView(Listitem item) {

		InwardBatch batch = (InwardBatch) item.getValue();

		if (batch == null) {
			return;
		}

		Messagebox.show("Opening batch details for " + batch.getInwardBatchId(), "Batch View", Messagebox.OK,
				Messagebox.INFORMATION);
	}

	private void searchBatches() {

		String batchNo = batchSearchTextbox.getValue();

		if (batchNo == null) {
			batchNo = "";
		}

		batchNo = batchNo.trim();

		String selectedStatus = "All";

		if (statusCombobox.getSelectedItem() != null) {

			selectedStatus = statusCombobox.getSelectedItem().getLabel();

		} else if (statusCombobox.getValue() != null && !statusCombobox.getValue().trim().isEmpty()) {

			selectedStatus = statusCombobox.getValue().trim();
		}

		Date selectedDate = receivedDatebox.getValue();

		List<InwardBatch> filteredBatches = new ArrayList<InwardBatch>();

		for (InwardBatch batch : allBatches) {

			boolean batchMatch = true;

			boolean statusMatch = true;

			boolean dateMatch = true;

			if (!batchNo.isEmpty()) {

				String currentBatchNo = batch.getInwardBatchId();

				if (currentBatchNo == null || !currentBatchNo.toLowerCase().contains(batchNo.toLowerCase())) {

					batchMatch = false;
				}
			}

			if (!"All".equalsIgnoreCase(selectedStatus)) {

				String currentStatus = getDisplayStatus(batch);

				if (!selectedStatus.equalsIgnoreCase(currentStatus)) {

					statusMatch = false;
				}
			}

			if (selectedDate != null) {

				if (batch.getUploadedAt() == null) {

					dateMatch = false;

				} else {

					dateMatch = isSameDate(selectedDate, batch.getUploadedAt());
				}
			}

			if (batchMatch && statusMatch && dateMatch) {

				filteredBatches.add(batch);
			}
		}

		displayBatches(filteredBatches);

		if (filteredBatches.isEmpty()) {

			Messagebox.show("No batches found matching the search criteria.", "Search Result", Messagebox.OK,
					Messagebox.INFORMATION);
		}
	}

	private boolean isSameDate(Date selectedDate, Date uploadedDate) {

		Calendar selectedCalendar = Calendar.getInstance();

		selectedCalendar.setTime(selectedDate);

		Calendar uploadedCalendar = Calendar.getInstance();

		uploadedCalendar.setTime(uploadedDate);

		return selectedCalendar.get(Calendar.YEAR) == uploadedCalendar.get(Calendar.YEAR)

				&&

				selectedCalendar.get(Calendar.MONTH) == uploadedCalendar.get(Calendar.MONTH)

				&&

				selectedCalendar.get(Calendar.DAY_OF_MONTH) == uploadedCalendar.get(Calendar.DAY_OF_MONTH);
	}

	private void setStatusStyle(Label label, String status) {

		if ("Validated".equalsIgnoreCase(status)) {

			label.setSclass("status-validated");

		} else if ("Validation Failed".equalsIgnoreCase(status)) {

			label.setSclass("status-failed");

		} else if ("Parsing".equalsIgnoreCase(status)) {

			label.setSclass("status-parsing");

		} else {

			label.setSclass("status-pending");
		}
	}

	private String getBatchFolderName(String batchId) {

		if ("INW260904001".equalsIgnoreCase(batchId)) {

			return "BATCH-2026-09-04-001";
		}

		if ("INW260904002".equalsIgnoreCase(batchId)) {

			return "BATCH-2026-09-04-002";
		}

		if ("INW260904003".equalsIgnoreCase(batchId)) {

			return "BATCH-2026-09-04-003";
		}

		return null;
	}

	private static class ParseResult {

		private final String batchId;

		private final ParsedBatchData parsedBatchData;

		private final String status;

		private final String message;

		ParseResult(String batchId, ParsedBatchData parsedBatchData, String status, String message) {

			this.batchId = batchId;

			this.parsedBatchData = parsedBatchData;

			this.status = status;

			this.message = message;
		}

		String getBatchId() {

			return batchId;
		}

		ParsedBatchData getParsedBatchData() {

			return parsedBatchData;
		}

		String getStatus() {

			return status;
		}

		String getMessage() {

			return message;
		}
	}
}