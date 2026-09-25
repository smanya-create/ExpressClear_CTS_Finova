package com.iispl.cts.controller.inward.maker;
import java.io.File;
import java.math.BigDecimal;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.Comparator;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import javax.xml.parsers.DocumentBuilderFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
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
import com.iispl.cts.common.util.SecurityUtil;
import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.parser.InwardBatchXmlParser.ParsedBatchData;
import com.iispl.cts.service.inward.InwardBatchService;
import com.iispl.cts.serviceimpl.inward.InwardBatchServiceImpl;

public class InwardBatchController extends SelectorComposer<Window> {
	
	private static final String PARSED_BATCH_SESSION_PREFIX = "CTS_PARSED_BATCH_";
	private static final DecimalFormat CURRENCY_FORMAT = new DecimalFormat("##,##,##0.00", DecimalFormatSymbols.getInstance(Locale.ENGLISH));
	private InwardBatchService inwardBatchService;
	private Window currentWindow;
	private Listbox batchListbox;
	private Textbox batchSearchTextbox;
	private Combobox statusCombobox;
	private Datebox receivedDatebox;
	private Button clearButton;
	private List<InwardBatch> allBatches = new ArrayList<InwardBatch>();
	
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
			if (window.getFellowIfAny("dashboardButton") != null) {
				Button dashboardButton = (Button) window.getFellow("dashboardButton");
				dashboardButton.addEventListener("onClick", event -> openDashboard());
			}
			if (window.getFellowIfAny("clearButton") != null) {
				clearButton = (Button) window.getFellow("clearButton");
				clearButton.addEventListener("onClick", event -> clearSearch());
			}
			loadBatches();
		}
	}
	
	private void openDashboard() {
		try {
			Executions.sendRedirect("/inward/maker/index.zul?page=dashboard");
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("Unable to open dashboard.", "Dashboard", Messagebox.OK, Messagebox.ERROR);
		}
	}
	
	private void clearSearch() {
		if (batchSearchTextbox != null)
			batchSearchTextbox.setValue("");
		if (statusCombobox != null)
			statusCombobox.setSelectedIndex(0);
		if (receivedDatebox != null)
			receivedDatebox.setValue(null);
		displayBatches(allBatches);
	}
	
	private void loadBatches() {
		allBatches.clear();
		try {
			String inwardDataPath = currentWindow.getDesktop().getWebApp().getRealPath("/Inward-data");
			if (inwardDataPath == null) {
				Messagebox.show("Unable to locate Inward-data folder.", "Batch Intake", Messagebox.OK, Messagebox.ERROR);
				displayBatches(allBatches);
				return;
			}
			File inwardDataDirectory = new File(inwardDataPath);
			if (!inwardDataDirectory.isDirectory()) {
				Messagebox.show("Inward-data folder not found.", "Batch Intake", Messagebox.OK, Messagebox.ERROR);
				displayBatches(allBatches);
				return;
			}
			File[] batchFolders = inwardDataDirectory.listFiles(File::isDirectory);
			if (batchFolders == null) {
				displayBatches(allBatches);
				return;
			}
			for (File batchFolder : batchFolders) {
				File npciXmlFile = new File(batchFolder, "NPCI_Inward.xml");
				if (!npciXmlFile.isFile())
					continue;
				BatchHeaderInfo header = readBatchHeader(npciXmlFile);
				if (header == null || header.batchId == null || header.batchId.trim().isEmpty())
					continue;
				InwardBatch dbBatch = inwardBatchService.getBatchById(header.batchId);
				ParsedBatchData sessionParsedData = getParsedBatchData(header.batchId);
				if (sessionParsedData != null && sessionParsedData.getInwardBatch() != null) {
					dbBatch = sessionParsedData.getInwardBatch();
					dbBatch.setBatchStatus("PENDING_VALIDATION");
				}
				if (dbBatch != null) {
					if (dbBatch.getUploadedAt() == null) {
						dbBatch.setUploadedAt(header.uploadedAt != null ? header.uploadedAt : new Timestamp(npciXmlFile.lastModified()));
					}
					allBatches.add(dbBatch);
				} else {
					InwardBatch batch = new InwardBatch();
					batch.setInwardBatchId(header.batchId);
					batch.setBatchReferenceId(header.batchReferenceId);
					batch.setBatchStatus("Received");
					batch.setActualChequeCount(0);
					batch.setActualTotalAmount(BigDecimal.ZERO);
					batch.setUploadedAt(header.uploadedAt != null ? header.uploadedAt : new Timestamp(npciXmlFile.lastModified()));
					allBatches.add(batch);
				}
			}
			allBatches.sort(Comparator.comparing(InwardBatch::getUploadedAt, Comparator.nullsLast(Comparator.reverseOrder())));
			displayBatches(allBatches);
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("Unable to load inward batches: " + e.getMessage(), "Batch Intake", Messagebox.OK, Messagebox.ERROR);
		}
	}
	
	private void displayBatches(List<InwardBatch> batches) {
		batchListbox.getItems().clear();
		if (batches == null)
			batches = new ArrayList<InwardBatch>();
		List<InwardBatch> displayList = new ArrayList<InwardBatch>(batches);
		displayList.sort(Comparator.comparing(InwardBatch::getUploadedAt, Comparator.nullsLast(Comparator.reverseOrder())));
		for (InwardBatch batch : displayList) {
			Listitem item = new Listitem();
			item.setValue(batch);
			Listcell batchCell = new Listcell();
			Label lblBatch = new Label(valueOrEmpty(batch.getInwardBatchId()));
			lblBatch.setSclass("batch-id-link");
			batchCell.appendChild(lblBatch);
			item.appendChild(batchCell);
			Listcell dateCell = new Listcell();
			Label lblDate = new Label(batch.getUploadedAt() != null
					? new java.text.SimpleDateFormat("dd-MM-yyyy").format(batch.getUploadedAt()) : "-");
			lblDate.setSclass("batch-cell-text");
			dateCell.appendChild(lblDate);
			item.appendChild(dateCell);
			Listcell amountCell = new Listcell();
			String amtFormatted = "₹ 0.00";
			if (batch.getActualTotalAmount() != null)
				amtFormatted = "₹ " + CURRENCY_FORMAT.format(batch.getActualTotalAmount());
			Label lblAmt = new Label(amtFormatted);
			lblAmt.setSclass("batch-cell-amount");
			amountCell.appendChild(lblAmt);
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
		updateBatchCount(displayList.size());
	}
	
	private void updateBatchCount(int count) {
		Label batchCountLabel = (Label) currentWindow.getFellowIfAny("batchCountLabel");
		if (batchCountLabel == null)
			return;
		batchCountLabel.setValue(count + (count == 1 ? " Batch" : " Batches"));
	}
	
	private void createActionButton(Listitem item, Listcell actionCell, InwardBatch batch) {
		String status = getDisplayStatus(batch);
		if ("Received".equalsIgnoreCase(status)) {
			Button parseButton = new Button("Parse");
			parseButton.setSclass("parse-button");
			parseButton.addEventListener("onClick", event -> parseBatch(event));
			actionCell.appendChild(parseButton);
			return;
		}
		if ("Parsing".equalsIgnoreCase(status)) {
			Button parsingButton = new Button("Parsing...");
			parsingButton.setDisabled(true);
			parsingButton.setSclass("parsing-button");
			actionCell.appendChild(parsingButton);
			return;
		}
		Label dashLabel = new Label("-");
		dashLabel.setSclass("batch-action-dash");
		actionCell.appendChild(dashLabel);
	}
	
	private String getDisplayStatus(InwardBatch batch) {
		if (batch == null)
			return "Received";
		String status = batch.getBatchStatus();
		if (status == null || status.trim().isEmpty())
			return "Received";
		status = status.trim();
		if ("VALIDATED".equalsIgnoreCase(status))
			return "Validated";
		if ("PENDING_VALIDATION".equalsIgnoreCase(status))
			return "Pending Validation";
		if ("PROCESSING".equalsIgnoreCase(status))
			return "Processing";
		if ("RECEIVED".equalsIgnoreCase(status))
			return "Received";
		if ("PARSING".equalsIgnoreCase(status))
			return "Parsing";
		if ("COMPLETED".equalsIgnoreCase(status))
			return "Completed";
		if ("VALIDATION_FAILED".equalsIgnoreCase(status))
			return "Validation Failed";
		if ("CHECKER_PROCESSING_PENDING".equalsIgnoreCase(status))
			return "Checker Pending";
		if ("CHECKER_PROCESSING".equalsIgnoreCase(status))
			return "Checker Processing";
		if ("IN_VERIFICATION".equalsIgnoreCase(status))
			return "In Verification";
		if ("HOLD".equalsIgnoreCase(status))
			return "Hold";
		if ("REJECTED".equalsIgnoreCase(status))
			return "Rejected";
		if ("FAILED".equalsIgnoreCase(status))
			return "Failed";
		return formatStatus(status);
	}
	private String formatStatus(String status) {
		String[] words = status.replace('_', ' ').trim().toLowerCase().split("\\s+");
		StringBuilder result = new StringBuilder();
		for (String word : words) {
			if (word.isEmpty())
				continue;
			if (result.length() > 0)
				result.append(" ");
			result.append(Character.toUpperCase(word.charAt(0)));
			if (word.length() > 1)
				result.append(word.substring(1));
		}
		return result.toString();
	}
	
	private void setStatusStyle(Label label, String status) {
		if (label == null) {
			return;
		}
		if (status == null || status.trim().isEmpty()) {
			label.setSclass("status-pill-gold");
			return;
		}
		String normalizedStatus = status.trim().toUpperCase();
		if ("VALIDATED".equals(normalizedStatus) || "COMPLETED".equals(normalizedStatus)
				|| "CHECKER PROCESSING".equals(normalizedStatus)) {
			label.setSclass("status-pill-completed");
		} else if ("VALIDATION FAILED".equals(normalizedStatus) || "REJECTED".equals(normalizedStatus)
				|| "FAILED".equals(normalizedStatus) || "HOLD".equals(normalizedStatus)) {
			label.setSclass("status-pill-failed");
		} else if ("PARSING".equals(normalizedStatus) || "CHECKER PROCESSING PENDING".equals(normalizedStatus)
				|| "IN VERIFICATION".equals(normalizedStatus)) {
			label.setSclass("status-pill-blue");
		} else if (normalizedStatus.contains("SENT BACK") || normalizedStatus.contains("REWORK")) {
			label.setSclass("status-pill-sentback");
		} else {
			label.setSclass("status-pill-gold");
		}
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
				Messagebox.show("Batch folder not found for: " + batchId, "Parse Failed", Messagebox.OK, Messagebox.ERROR);
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
						result = new ParseResult(finalBatchId, null, "Validation Failed", "Unable to parse NPCI and OCR XML.");
					} else {
						InwardBatch parsedBatch = parsedBatchData.getInwardBatch();
						String xmlBatchId = parsedBatch.getInwardBatchId();
						if (xmlBatchId == null || xmlBatchId.trim().isEmpty()) {
							parsedBatch.setInwardBatchId(finalBatchId);
						} else if (!finalBatchId.equalsIgnoreCase(xmlBatchId)) {
							result = new ParseResult(finalBatchId, null, "Validation Failed", "Batch number mismatch. Queue Batch: " + finalBatchId + " XML Batch: "+ xmlBatchId);
							scheduleParseResult(result);
							return;
						}
						if (parsedBatch.getBatchReferenceId() == null
								|| parsedBatch.getBatchReferenceId().trim().isEmpty()) {
							parsedBatch.setBatchReferenceId(finalFolderName);
						}
						if (parsedBatch.getActualChequeCount() <= 0) {
							int chequeCount = parsedBatchData.getInwardCheques() == null ? 0 : parsedBatchData.getInwardCheques().size();
							parsedBatch.setActualChequeCount(chequeCount);
						}
						parsedBatch.setBatchStatus("PENDING_VALIDATION");
						result = new ParseResult(finalBatchId, parsedBatchData, "Pending Validation", null);
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
			Messagebox.show("Unable to start parsing: " + e.getMessage(), "Parse Failed", Messagebox.OK, Messagebox.ERROR);
		}
	}
	
	private String getParsedBatchSessionKey(String batchId) {
		return PARSED_BATCH_SESSION_PREFIX + batchId;
	}
	private void storeParsedBatchData(String batchId, ParsedBatchData parsedBatchData) {
		if (batchId == null || batchId.trim().isEmpty() || parsedBatchData == null)
			return;
		Sessions.getCurrent().setAttribute(getParsedBatchSessionKey(batchId), parsedBatchData);
	}
	private ParsedBatchData getParsedBatchData(String batchId) {
		if (batchId == null || batchId.trim().isEmpty())
			return null;
		Object value = Sessions.getCurrent().getAttribute(getParsedBatchSessionKey(batchId));
		return value instanceof ParsedBatchData ? (ParsedBatchData) value : null;
	}
	private void removeParsedBatchData(String batchId) {
		if (batchId == null || batchId.trim().isEmpty())
			return;
		Sessions.getCurrent().removeAttribute(getParsedBatchSessionKey(batchId));
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
		if (result == null)
			return;
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
			if ("Pending Validation".equalsIgnoreCase(result.getStatus())) {
				ParsedBatchData data = result.getParsedBatchData();
				if (data == null || data.getInwardBatch() == null) {
					if (targetItem != null) {
						InwardBatch failedBatch = (InwardBatch) targetItem.getValue();
						if (failedBatch != null)
							failedBatch.setBatchStatus("VALIDATION_FAILED");
						updateFailedRow(targetItem);
					}
					Messagebox.show("Parsed batch data is not available.", "Validation Failed", Messagebox.OK, Messagebox.ERROR);
					return;
				}
				InwardBatch parsedBatch = data.getInwardBatch();
				parsedBatch.setBatchStatus("PENDING_VALIDATION");
				storeParsedBatchData(batchId, data);
				String validationError = validateParsedBatchAutomatically(data, batchId);
				if (validationError != null) {
					parsedBatch.setBatchStatus("VALIDATION_FAILED");
					if (targetItem != null) {
						targetItem.setValue(parsedBatch);
						updateFailedRow(targetItem);
					}
					updateAllBatchReference(parsedBatch);
					removeParsedBatchData(batchId);
					showValidationFailedPopup(parsedBatch, validationError);
					return;
				}
				try {
					parsedBatch.setBatchStatus("PROCESSING");
					boolean saved = inwardBatchService.saveParsedBatch(data);
					if (!saved) {
						parsedBatch.setBatchStatus("VALIDATION_FAILED");
						if (targetItem != null) {
							targetItem.setValue(parsedBatch);
							updateFailedRow(targetItem);
						}
						updateAllBatchReference(parsedBatch);
						removeParsedBatchData(batchId);
						Messagebox.show("Validation succeeded, but the parsed batch could not be saved.", "Save Failed", Messagebox.OK, Messagebox.ERROR);
						return;
					}
					removeParsedBatchData(batchId);
					if (targetItem != null) {
						targetItem.setValue(parsedBatch);
						updateProcessingAfterValidationRow(targetItem, parsedBatch);
					}
					updateAllBatchReference(parsedBatch);
					Messagebox.show("Batch " + batchId + " parsed, validated and saved successfully. Status changed to Processing.", "Parsing Successful", Messagebox.OK, Messagebox.INFORMATION);
				} catch (Exception e) {
					e.printStackTrace();
					parsedBatch.setBatchStatus("VALIDATION_FAILED");
					if (targetItem != null) {
						targetItem.setValue(parsedBatch);
						updateFailedRow(targetItem);
					}
					updateAllBatchReference(parsedBatch);
					removeParsedBatchData(batchId);
					Messagebox.show("Unable to save parsed batch: " + e.getMessage(), "Save Failed", Messagebox.OK, Messagebox.ERROR);
				}
			} else {
				if (targetItem != null) {
					InwardBatch failedBatch = (InwardBatch) targetItem.getValue();
					if (failedBatch != null) {
						failedBatch.setBatchStatus("VALIDATION_FAILED");
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
			Messagebox.show("Unable to update batch status: " + e.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
		}
	}
	
	private void showValidationFailedPopup(InwardBatch batch, String validationError) {
		String batchId = batch != null ? valueOrEmpty(batch.getInwardBatchId()) : "";
		int actualCount = batch != null ? batch.getActualChequeCount() : 0;
		BigDecimal actualAmount = batch != null ? batch.getActualTotalAmount() : BigDecimal.ZERO;
		if (actualAmount == null) {
			actualAmount = BigDecimal.ZERO;
		}
		String message = "Batch " + batchId + "\n\n" + "Actual Count: " + actualCount + "\n" + "Actual Amount: ₹ "
				+ CURRENCY_FORMAT.format(actualAmount) + "\n\n"
				+ "VALIDATION FAILED. Expected count and expected amount do not match the actual batch values.";
		Messagebox.show(message, "Validation Failed", Messagebox.OK, Messagebox.ERROR);
	}
	
	private String validateParsedBatchAutomatically(ParsedBatchData data, String batchId) {
		if (data == null || data.getInwardBatch() == null)
			return "Parsed batch data is not available.";
		InwardBatch batch = data.getInwardBatch();
		String parsedBatchId = batch.getInwardBatchId();
		if (parsedBatchId == null || parsedBatchId.trim().isEmpty())
			return "Validation failed: batch number is missing in parsed data.";
		if (batchId == null || !batchId.equalsIgnoreCase(parsedBatchId.trim()))
			return "Validation failed: batch number mismatch. Queue Batch: " + batchId + " XML Batch: " + parsedBatchId;
		List<InwardCheque> cheques = data.getInwardCheques();
		int chequeCount = cheques == null ? 0 : cheques.size();
		if (chequeCount <= 0)
			return "Validation failed: no cheque records were found in the parsed XML data.";
		if (batch.getActualChequeCount() <= 0)
			batch.setActualChequeCount(chequeCount);
		if (batch.getActualChequeCount() != chequeCount)
			return "Validation failed: batch cheque count does not match the parsed cheque records. Expected: "
					+ batch.getActualChequeCount() + ", Parsed: " + chequeCount;
		if (batch.getActualTotalAmount() == null)
			batch.setActualTotalAmount(BigDecimal.ZERO);
		BigDecimal calculatedAmount = BigDecimal.ZERO;
		for (InwardCheque cheque : cheques) {
			if (cheque != null && cheque.getChequeAmount() != null)
				calculatedAmount = calculatedAmount.add(cheque.getChequeAmount());
		}
		if (batch.getActualTotalAmount().compareTo(calculatedAmount) != 0)
			return "Validation failed: batch total amount does not match the parsed cheque amounts. Batch Amount: ₹ "
					+ CURRENCY_FORMAT.format(batch.getActualTotalAmount()) + " | Parsed Amount: ₹ "
					+ CURRENCY_FORMAT.format(calculatedAmount);
		return null;
	}
	
	private void updateAllBatchReference(InwardBatch updatedBatch) {
		if (updatedBatch == null || updatedBatch.getInwardBatchId() == null)
			return;
		for (int i = 0; i < allBatches.size(); i++) {
			InwardBatch current = allBatches.get(i);
			if (current != null && current.getInwardBatchId() != null
					&& updatedBatch.getInwardBatchId().equalsIgnoreCase(current.getInwardBatchId())) {
				allBatches.set(i, updatedBatch);
				return;
			}
		}
		allBatches.add(updatedBatch);
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
	private void updateProcessingAfterValidationRow(Listitem item, InwardBatch batch) {
		Listcell statusCell = (Listcell) item.getChildren().get(3);
		statusCell.getChildren().clear();
		Label statusLabel = new Label("Processing");
		setStatusStyle(statusLabel, "Processing");
		statusCell.appendChild(statusLabel);
		Listcell actionCell = (Listcell) item.getChildren().get(4);
		actionCell.getChildren().clear();
		Label dashLabel = new Label("-");
		dashLabel.setSclass("batch-action-dash");
		actionCell.appendChild(dashLabel);
	}
	private void updateProcessingRow(Listitem item) {
		Listcell statusCell = (Listcell) item.getChildren().get(3);
		statusCell.getChildren().clear();
		Label statusLabel = new Label("Processing");
		setStatusStyle(statusLabel, "Processing");
		statusCell.appendChild(statusLabel);
		Listcell actionCell = (Listcell) item.getChildren().get(4);
		actionCell.getChildren().clear();
		Label dashLabel = new Label("-");
		dashLabel.setSclass("batch-action-dash");
		actionCell.appendChild(dashLabel);
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
	private Timestamp parseUploadedAt(String value) {
		if (value == null || value.trim().isEmpty())
			return null;
		String text = value.trim();
		try {
			return Timestamp.valueOf(text.replace("T", " ").replace("Z", ""));
		} catch (Exception e) {
			try {
				return Timestamp.from(Instant.parse(text));
			} catch (Exception ignored) {
				return null;
			}
		}
	}
	
	private void searchBatches() {
		String batchNo = batchSearchTextbox.getValue();
		if (batchNo == null)
			batchNo = "";
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
	private static class BatchHeaderInfo {
		private String batchId;
		private String batchReferenceId;
		private Timestamp uploadedAt;
		private BatchHeaderInfo(String batchId, String batchReferenceId, Timestamp uploadedAt) {
			this.batchId = batchId;
			this.batchReferenceId = batchReferenceId;
			this.uploadedAt = uploadedAt;
		}
	}
	
	private BatchHeaderInfo readBatchHeader(File npciXmlFile) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setNamespaceAware(true);
			Document document = factory.newDocumentBuilder().parse(npciXmlFile);
			Element root = document.getDocumentElement();
			if (root == null)
				return null;
			NodeList batchHeaders = root.getElementsByTagNameNS("*", "BatchHeader");
			if (batchHeaders.getLength() == 0)
				return null;
			Element batchHeader = (Element) batchHeaders.item(0);
			String batchId = getXmlChildValue(batchHeader, "ScannedBatchId");
			String batchReferenceId = getXmlChildValue(batchHeader, "BatchReferenceId");
			String uploadedAtText = getXmlChildValue(batchHeader, "UploadedAt");
			if (batchId == null || batchId.trim().isEmpty())
				return null;
			if (batchReferenceId == null || batchReferenceId.trim().isEmpty())
				batchReferenceId = npciXmlFile.getParentFile().getName();
			Timestamp uploadedAt = parseUploadedAt(uploadedAtText);
			if (uploadedAt == null && npciXmlFile.lastModified() > 0)
				uploadedAt = new Timestamp(npciXmlFile.lastModified());
			return new BatchHeaderInfo(batchId.trim(), batchReferenceId.trim(), uploadedAt);
		} catch (Exception e) {
			System.err.println("Unable to read batch header from: " + npciXmlFile.getAbsolutePath());
			e.printStackTrace();
			return null;
		}
	}
	
	private String getXmlChildValue(Element parent, String childName) {
		NodeList nodes = parent.getElementsByTagNameNS("*", childName);
		if (nodes.getLength() == 0)
			return null;
		String value = nodes.item(0).getTextContent();
		return value == null ? null : value.trim();
	}
	
	private String getBatchFolderName(String batchId) {
		if (batchId == null || batchId.trim().isEmpty())
			return null;
		try {
			String inwardDataPath = currentWindow.getDesktop().getWebApp().getRealPath("/Inward-data");
			if (inwardDataPath == null)
				return null;
			File inwardDataDirectory = new File(inwardDataPath);
			if (!inwardDataDirectory.isDirectory())
				return null;
			File[] batchFolders = inwardDataDirectory.listFiles(File::isDirectory);
			if (batchFolders == null)
				return null;
			for (File batchFolder : batchFolders) {
				File npciXmlFile = new File(batchFolder, "NPCI_Inward.xml");
				if (!npciXmlFile.isFile())
					continue;
				BatchHeaderInfo header = readBatchHeader(npciXmlFile);
				if (header == null || header.batchId == null)
					continue;
				if (batchId.equalsIgnoreCase(header.batchId)) {
					return batchFolder.getName();
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return null;
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