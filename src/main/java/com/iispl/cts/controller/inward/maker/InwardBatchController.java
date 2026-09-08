package com.iispl.cts.controller.inward.maker;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.zkoss.image.AImage;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.iispl.cts.daoimpl.inward.InwardChequeDAOImpl;
import com.iispl.cts.daoimpl.inward.InwardChequeImageDAOImpl;
import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.entity.inward.InwardChequeImage;
import com.iispl.cts.parser.InwardBatchXmlParser.ParsedBatchData;
import com.iispl.cts.service.inward.InwardBatchService;
import com.iispl.cts.serviceimpl.inward.InwardBatchServiceImpl;

public class InwardBatchController extends SelectorComposer<Window> {

	private static final long serialVersionUID = 1L;

	private InwardBatchService inwardBatchService;
	private InwardChequeDAOImpl inwardChequeDAO;
	private InwardChequeImageDAOImpl inwardChequeImageDAO;

	private Window currentWindow;

	private Listbox batchListbox;
	private Textbox batchSearchTextbox;
	private Combobox statusCombobox;
	private Datebox receivedDatebox;

	private Listbox chequeListbox;
	private Textbox chequeSearchTextbox;
	private Label chequeCountLabel;

	private List<InwardBatch> allBatches = new ArrayList<InwardBatch>();
	private List<InwardCheque> allCheques = new ArrayList<InwardCheque>();

	private String currentBatchFolderName;

	@Override
	public void doAfterCompose(Window window) throws Exception {
		super.doAfterCompose(window);

		currentWindow = window;
		inwardBatchService = new InwardBatchServiceImpl();

		if (window.getFellowIfAny("batchListbox") != null) {
			window.getDesktop().enableServerPush(true);

			batchListbox = (Listbox) window.getFellow("batchListbox");
			batchSearchTextbox = (Textbox) window.getFellow("batchSearchTextbox");
			statusCombobox = (Combobox) window.getFellow("statusCombobox");
			receivedDatebox = (Datebox) window.getFellow("receivedDatebox");

			Button searchButton = (Button) window.getFellow("searchButton");
			searchButton.addEventListener("onClick", event -> searchBatches());

			loadBatches();
		}

		if (window.getFellowIfAny("chequeListbox") != null) {
			initializeBatchView(window);
		}
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

		if (batches == null) {
			batches = new ArrayList<InwardBatch>();
		}

		for (InwardBatch batch : batches) {
			Listitem item = new Listitem();
			item.setValue(batch);

			Listcell batchCell = new Listcell();
			batchCell.appendChild(new Label(valueOrEmpty(batch.getInwardBatchId())));
			item.appendChild(batchCell);

			Listcell dateCell = new Listcell();

			if (batch.getUploadedAt() != null) {
				dateCell.appendChild(new Label(batch.getUploadedAt().toString()));
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

		updateBatchCount(batches.size());
	}

	private void updateBatchCount(int count) {
		Label batchCountLabel = (Label) currentWindow.getFellowIfAny("batchCountLabel");

		if (batchCountLabel == null) {
			return;
		}

		if (count == 0) {
			batchCountLabel.setValue("Showing 0 to 0 of 0 batches");
			return;
		}

		batchCountLabel.setValue("Showing 1 to " + count + " of " + count + " batches");
	}

	private void createActionButton(Listitem item, Listcell actionCell, InwardBatch batch) {

		String status = getDisplayStatus(batch);

		if ("Validated".equalsIgnoreCase(status)) {
			Button viewButton = new Button("View");

			viewButton.setSclass("view-button");
			viewButton.setWidth("90px");

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

			String npciPath = currentWindow.getDesktop().getWebApp().getRealPath("/" + npciXml);

			String ocrPath = currentWindow.getDesktop().getWebApp().getRealPath("/" + ocrXml);

			if (npciPath == null || !new File(npciPath).isFile()) {
				Messagebox.show("NPCI XML not found:\n" + npciXml, "Parse Failed", Messagebox.OK, Messagebox.ERROR);
				return;
			}

			if (ocrPath == null || !new File(ocrPath).isFile()) {
				Messagebox.show("OCR XML not found:\n" + ocrXml, "Parse Failed", Messagebox.OK, Messagebox.ERROR);
				return;
			}

			batch.setBatchStatus("Parsing");
			updateParsingRow(item);

			final String finalNpciPath = npciPath;
			final String finalOcrPath = ocrPath;
			final String finalBatchId = batchId;
			final String finalFolderName = folderName;

			Thread parsingThread = new Thread(() -> {

				ParseResult result;

				try {
					ParsedBatchData parsedBatchData = inwardBatchService.parseBatchXml(finalNpciPath, finalOcrPath);

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

							scheduleParseResult(result);
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

				scheduleParseResult(result);

			});

			parsingThread.setName("InwardBatchParser-" + batchId);

			parsingThread.start();

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to start parsing: " + e.getMessage(), "Parse Failed", Messagebox.OK,
					Messagebox.ERROR);
		}
	}

	private void scheduleParseResult(ParseResult result) {
		Executions.schedule(currentWindow.getDesktop(), new EventListener<Event>() {
			@Override
			public void onEvent(Event event) throws Exception {
				handleParseComplete((ParseResult) event.getData());
			}
		}, new Event("onParseComplete", currentWindow, result));
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

		Button viewButton = new Button("View");

		viewButton.setSclass("view-button");
		viewButton.setWidth("90px");

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

	private void openBatchView(Listitem item) {
		try {

			InwardBatch batch = (InwardBatch) item.getValue();

			if (batch == null) {
				return;
			}

			String batchId = batch.getInwardBatchId();

			if (batchId == null || batchId.trim().isEmpty()) {

				Messagebox.show("Batch number is missing.", "Batch View", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			String encodedBatchId = URLEncoder.encode(batchId, "UTF-8");

			Executions.sendRedirect("/inward/maker/batch/batch-view.zul?batchId=" + encodedBatchId);

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to open batch details.", "Batch View", Messagebox.OK, Messagebox.ERROR);
		}
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
				&& selectedCalendar.get(Calendar.MONTH) == uploadedCalendar.get(Calendar.MONTH)
				&& selectedCalendar.get(Calendar.DAY_OF_MONTH) == uploadedCalendar.get(Calendar.DAY_OF_MONTH);
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

	private void initializeBatchView(Window window) {

		try {

			chequeListbox = (Listbox) window.getFellow("chequeListbox");

			chequeSearchTextbox = (Textbox) window.getFellow("chequeSearchTextbox");

			chequeCountLabel = (Label) window.getFellow("chequeCountLabel");

			inwardChequeDAO = InwardChequeDAOImpl.getInstance();

			inwardChequeImageDAO = InwardChequeImageDAOImpl.getInstance();

			Button filterButton = (Button) window.getFellow("filterButton");

			Button backButton = (Button) window.getFellow("backButton");

			filterButton.addEventListener("onClick", event -> filterCheques());

			backButton.addEventListener("onClick",
					event -> Executions.sendRedirect("/inward/maker/index.zul?page=batch-intake"));
			String batchId = Executions.getCurrent().getParameter("batchId");

			if (batchId == null || batchId.trim().isEmpty()) {

				Messagebox.show("Batch number is missing.", "Batch View", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			loadBatchDetails(batchId.trim());

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to initialize batch view.", "Batch View", Messagebox.OK, Messagebox.ERROR);
		}
	}

	private void loadBatchDetails(String batchId) {

		try {

			InwardBatch batch = inwardBatchService.getBatchById(batchId);

			if (batch == null) {

				Messagebox.show("Batch not found: " + batchId, "Batch View", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			Label batchIdLabel = (Label) currentWindow.getFellow("batchIdLabel");

			Label batchReferenceLabel = (Label) currentWindow.getFellow("batchReferenceLabel");

			Label receivedDateTimeLabel = (Label) currentWindow.getFellow("receivedDateTimeLabel");

			Label fileNameLabel = (Label) currentWindow.getFellow("fileNameLabel");

			Label uploadedByLabel = (Label) currentWindow.getFellow("uploadedByLabel");

			Label totalChequesLabel = (Label) currentWindow.getFellow("totalChequesLabel");

			Label totalAmountLabel = (Label) currentWindow.getFellow("totalAmountLabel");

			Label parsingStatusLabel = (Label) currentWindow.getFellow("parsingStatusLabel");

			Label batchStatusLabel = (Label) currentWindow.getFellow("batchStatusLabel");

			batchIdLabel.setValue(valueOrEmpty(batch.getInwardBatchId()));

			batchReferenceLabel.setValue(valueOrEmpty(batch.getBatchReferenceId()));

			if (batch.getUploadedAt() != null) {

				receivedDateTimeLabel.setValue(batch.getUploadedAt().toString());

			} else {

				receivedDateTimeLabel.setValue("");
			}

			fileNameLabel.setValue("NPCI_Inward.xml");

			uploadedByLabel.setValue(valueOrEmpty(batch.getUploadedBy()));

			totalChequesLabel.setValue(String.valueOf(batch.getActualChequeCount()));

			if (batch.getActualTotalAmount() != null) {

				totalAmountLabel.setValue("₹ " + batch.getActualTotalAmount().toPlainString());

			} else {

				totalAmountLabel.setValue("₹ 0");
			}

			String status = getDisplayStatus(batch);

			if ("Validated".equalsIgnoreCase(status)) {

				parsingStatusLabel.setValue("SUCCESS");

				parsingStatusLabel.setSclass("status-badge status-success");

			} else if ("Parsing".equalsIgnoreCase(status)) {

				parsingStatusLabel.setValue("PARSING");

				parsingStatusLabel.setSclass("status-badge status-hold");

			} else if ("Validation Failed".equalsIgnoreCase(status)) {

				parsingStatusLabel.setValue("FAILED");

				parsingStatusLabel.setSclass("status-badge status-hold");

			} else {

				parsingStatusLabel.setValue("PENDING");

				parsingStatusLabel.setSclass("status-badge status-hold");
			}

			batchStatusLabel.setValue(status);

			batchStatusLabel.setSclass("status-badge status-hold");

			currentBatchFolderName = getBatchFolderName(batchId);

			loadBatchCheques(batchId);

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to load batch details.", "Batch View", Messagebox.OK, Messagebox.ERROR);
		}
	}

	private void loadBatchCheques(String batchId) {

		try {

			allCheques = inwardChequeDAO.getChequesByBatchId(batchId);

			if (allCheques == null) {

				allCheques = new ArrayList<InwardCheque>();
			}

			displayCheques(allCheques);

		} catch (Exception e) {

			e.printStackTrace();

			allCheques = new ArrayList<InwardCheque>();

			displayCheques(allCheques);
		}
	}

	private void displayCheques(List<InwardCheque> cheques) {

		chequeListbox.getItems().clear();

		if (cheques == null) {

			cheques = new ArrayList<InwardCheque>();
		}

		for (int i = 0; i < cheques.size(); i++) {

			InwardCheque cheque = cheques.get(i);

			Listitem item = new Listitem();

			item.setValue(cheque);

			item.appendChild(new Listcell(String.valueOf(i + 1)));

			item.appendChild(new Listcell(valueOrEmpty(cheque.getChequeNumber())));

			item.appendChild(new Listcell(valueOrEmpty(cheque.getMicrCode())));

			item.appendChild(new Listcell(valueOrEmpty(cheque.getDraweeAccountNumber())));

			item.appendChild(new Listcell(valueOrEmpty(cheque.getPayeeName())));

			String amount = "₹ 0";
			if (cheque.getChequeAmount() != null) {

				amount = "₹ " + cheque.getChequeAmount().toPlainString();
			}

			item.appendChild(new Listcell(amount));

			Listcell statusCell = new Listcell();

			String chequeStatus = valueOrEmpty(cheque.getChequeStatus());

			Label statusLabel = new Label(chequeStatus);

			if ("MICR REPAIR".equalsIgnoreCase(chequeStatus) || "MICR_REPAIR_REQUIRED".equalsIgnoreCase(chequeStatus)
					|| "MICR_REPAIR_PENDING".equalsIgnoreCase(chequeStatus)) {

				statusLabel.setSclass("cheque-status-repair");

			} else {

				statusLabel.setSclass("cheque-status-normal");
			}

			statusCell.appendChild(statusLabel);

			item.appendChild(statusCell);

			Listcell actionCell = new Listcell();

			Button viewButton = new Button("View");

			viewButton.setSclass("view-button");

			viewButton.setWidth("70px");

			final InwardCheque selectedCheque = cheque;

			viewButton.addEventListener("onClick", event -> showChequeImages(selectedCheque));

			actionCell.appendChild(viewButton);

			item.appendChild(actionCell);

			chequeListbox.appendChild(item);
		}

		updateChequeCount(cheques.size());
	}

	private void showChequeImages(InwardCheque cheque) {

		try {

			if (cheque == null) {

				Messagebox.show("Cheque information not found.", "Cheque Images", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			String chequeId = valueOrEmpty(cheque.getInwardChequeId()).trim();

			if (chequeId.isEmpty()) {

				Messagebox.show("Cheque ID not found.", "Cheque Images", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			if (inwardChequeImageDAO == null) {
				inwardChequeImageDAO = InwardChequeImageDAOImpl.getInstance();
			}

			List<InwardChequeImage> imageRecords = inwardChequeImageDAO.getImagesByChequeId(chequeId);

			InwardChequeImage frontRecord = null;

			InwardChequeImage backRecord = null;

			if (imageRecords != null) {

				for (InwardChequeImage imageRecord : imageRecords) {

					if (imageRecord == null) {
						continue;
					}

					String imageType = valueOrEmpty(imageRecord.getImageType()).trim();

					if ("FRONT".equalsIgnoreCase(imageType)) {

						frontRecord = imageRecord;

					} else if ("BACK".equalsIgnoreCase(imageType)) {

						backRecord = imageRecord;
					}
				}
			}

			byte[] frontBytes = null;

			byte[] backBytes = null;

			if (frontRecord != null) {

				String frontPath = valueOrEmpty(frontRecord.getImagePath()).trim();

				if (!frontPath.isEmpty()) {

					frontBytes = readResourceBytesWithFallback(frontPath);
				}
			}

			if (backRecord != null) {

				String backPath = valueOrEmpty(backRecord.getImagePath()).trim();

				if (!backPath.isEmpty()) {

					backBytes = readResourceBytesWithFallback(backPath);
				}
			}

			if (frontBytes == null && backBytes == null) {

				Messagebox.show("Images not found for Cheque No.: " + valueOrEmpty(cheque.getChequeNumber()),
						"Cheque Images", Messagebox.OK, Messagebox.ERROR);

				return;
			}

			Window imageWindow = new Window("Cheque Images", "normal", true);

			imageWindow.setWidth("1100px");

			imageWindow.setHeight("700px");

			imageWindow.setClosable(true);

			imageWindow.setSizable(true);

			Vlayout mainLayout = new Vlayout();

			mainLayout.setWidth("100%");

			mainLayout.setSpacing("15px");

			mainLayout.setStyle("padding:20px;");

			Label title = new Label("Cheque No.: " + valueOrEmpty(cheque.getChequeNumber()));

			title.setStyle("font-size:18px;font-weight:600;");

			mainLayout.appendChild(title);

			Hlayout imagesLayout = new Hlayout();

			imagesLayout.setWidth("100%");

			imagesLayout.setSpacing("20px");

			imagesLayout.setStyle("align-items:flex-start;");

			Vlayout frontLayout = new Vlayout();

			frontLayout.setHflex("1");

			frontLayout.setSpacing("8px");

			Label frontLabel = new Label("Front Image");

			frontLabel.setStyle("font-size:15px;font-weight:600;text-align:center;");

			frontLayout.appendChild(frontLabel);

			if (frontBytes != null && frontBytes.length > 0) {

				Image frontImage = new Image();

				frontImage.setWidth("100%");

				frontImage.setHeight("520px");

				frontImage.setStyle(
						"object-fit:contain;" + "border:1px solid #d9e2ec;" + "padding:8px;" + "background:#ffffff;");

				frontImage.setContent(new AImage("front.jpg", frontBytes));

				frontLayout.appendChild(frontImage);

			} else {

				Label frontMissing = new Label("Front image not found");

				frontMissing.setStyle("font-size:14px;color:#999;text-align:center;");

				frontLayout.appendChild(frontMissing);
			}

			imagesLayout.appendChild(frontLayout);

			Vlayout backLayout = new Vlayout();

			backLayout.setHflex("1");

			backLayout.setSpacing("8px");

			Label backLabel = new Label("Back Image");

			backLabel.setStyle("font-size:15px;font-weight:600;text-align:center;");

			backLayout.appendChild(backLabel);

			if (backBytes != null && backBytes.length > 0) {

				Image backImage = new Image();

				backImage.setWidth("100%");

				backImage.setHeight("520px");

				backImage.setStyle(
						"object-fit:contain;" + "border:1px solid #d9e2ec;" + "padding:8px;" + "background:#ffffff;");

				backImage.setContent(new AImage("back.png", backBytes));

				backLayout.appendChild(backImage);

			} else {

				Label backMissing = new Label("Back image not found");

				backMissing.setStyle("font-size:14px;color:#999;text-align:center;");

				backLayout.appendChild(backMissing);
			}

			imagesLayout.appendChild(backLayout);

			mainLayout.appendChild(imagesLayout);

			imageWindow.appendChild(mainLayout);

			currentWindow.appendChild(imageWindow);

			imageWindow.doModal();

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to open cheque images.\n" + e.getMessage(), "Cheque Images", Messagebox.OK,
					Messagebox.ERROR);
		}
	}

	private byte[] readResourceBytesWithFallback(String imagePath) {

		String normalizedPath = imagePath.trim();

		while (normalizedPath.startsWith("/")) {
			normalizedPath = normalizedPath.substring(1);
		}

		String[] possiblePaths = new String[] { normalizedPath, "Inward-data/" + normalizedPath };

		for (String path : possiblePaths) {

			byte[] bytes = readResourceBytes(path);

			if (bytes != null && bytes.length > 0) {

				return bytes;
			}
		}

		return null;
	}

	private byte[] readResourceBytes(String resourcePath) {

		InputStream inputStream = null;

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

		try {

			inputStream = getResourceStream(resourcePath);

			if (inputStream == null) {
				return null;
			}

			byte[] buffer = new byte[8192];

			int length;

			while ((length = inputStream.read(buffer)) != -1) {

				outputStream.write(buffer, 0, length);
			}

			return outputStream.toByteArray();

		} catch (Exception e) {

			e.printStackTrace();

			return null;

		} finally {

			try {

				if (inputStream != null) {
					inputStream.close();
				}

			} catch (Exception e) {
			}

			try {
				outputStream.close();
			} catch (Exception e) {
			}
		}
	}

	private InputStream getResourceStream(String resourcePath) {

		String normalizedPath = resourcePath;

		if (normalizedPath == null) {
			return null;
		}

		normalizedPath = normalizedPath.trim();

		while (normalizedPath.startsWith("/")) {
			normalizedPath = normalizedPath.substring(1);
		}

		try {

			ClassLoader contextClassLoader = Thread.currentThread().getContextClassLoader();

			if (contextClassLoader != null) {

				InputStream stream = contextClassLoader.getResourceAsStream(normalizedPath);

				if (stream != null) {
					return stream;
				}
			}

		} catch (Exception e) {

			e.printStackTrace();
		}

		try {

			ClassLoader classLoader = InwardBatchController.class.getClassLoader();

			if (classLoader != null) {

				InputStream stream = classLoader.getResourceAsStream(normalizedPath);

				if (stream != null) {
					return stream;
				}
			}

		} catch (Exception e) {

			e.printStackTrace();
		}

		try {

			String realPath = currentWindow.getDesktop().getWebApp().getRealPath("/" + normalizedPath);

			if (realPath != null) {

				File file = new File(realPath);

				if (file.isFile()) {

					return java.nio.file.Files.newInputStream(file.toPath());
				}
			}

		} catch (Exception e) {

			e.printStackTrace();
		}

		try {

			String realPath = currentWindow.getDesktop().getWebApp().getRealPath("/WEB-INF/classes/" + normalizedPath);

			if (realPath != null) {

				File file = new File(realPath);

				if (file.isFile()) {

					return java.nio.file.Files.newInputStream(file.toPath());
				}
			}

		} catch (Exception e) {

			e.printStackTrace();
		}

		return null;
	}

	private String extractChequeImageNumber(String inwardChequeId) {

		if (inwardChequeId == null || inwardChequeId.trim().isEmpty()) {

			return null;
		}

		String value = inwardChequeId.trim();

		int chqIndex = value.lastIndexOf("CHQ");

		if (chqIndex < 0) {
			return null;
		}

		String number = value.substring(chqIndex + 3);

		if (number.isEmpty()) {
			return null;
		}

		for (int i = 0; i < number.length(); i++) {

			if (!Character.isDigit(number.charAt(i))) {

				return null;
			}
		}

		return String.format("%03d", Integer.parseInt(number));
	}

	private void filterCheques() {

		String searchValue = chequeSearchTextbox.getValue();

		if (searchValue == null) {
			searchValue = "";
		}

		searchValue = searchValue.trim().toLowerCase();

		if (searchValue.isEmpty()) {

			displayCheques(allCheques);

			return;
		}

		List<InwardCheque> filteredCheques = new ArrayList<InwardCheque>();

		for (InwardCheque cheque : allCheques) {

			String chequeNumber = valueOrEmpty(cheque.getChequeNumber()).toLowerCase();

			String accountNumber = valueOrEmpty(cheque.getDraweeAccountNumber()).toLowerCase();

			if (chequeNumber.contains(searchValue) || accountNumber.contains(searchValue)) {

				filteredCheques.add(cheque);
			}
		}

		displayCheques(filteredCheques);
	}

	private void updateChequeCount(int count) {

		if (count == 0) {

			chequeCountLabel.setValue("No cheque records found");

			return;
		}

		chequeCountLabel.setValue("Showing 1 to " + count + " of " + count + " cheques");
	}

	private String valueOrEmpty(String value) {

		return value == null ? "" : value;
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