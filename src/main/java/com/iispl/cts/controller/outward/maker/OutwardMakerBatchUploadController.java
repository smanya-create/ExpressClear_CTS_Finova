package com.iispl.cts.controller.outward.maker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.iispl.cts.dto.BatchValidationData;
import com.iispl.cts.dto.ValidationResult;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.outward.batchvalidator.MicrCodeHelper;
import com.iispl.cts.parser.BatchXmlParser;
import com.iispl.cts.service.outward.BatchValidationService;
import com.iispl.cts.service.outward.OutwardMakerService;
import com.iispl.cts.service.outward.ScanService;
import com.iispl.cts.serviceimpl.outward.BatchValidationServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardMakerServiceImpl;
import com.iispl.cts.serviceimpl.outward.ScanServiceImpl;

public class OutwardMakerBatchUploadController implements Composer<Component> {

	private static final long serialVersionUID = 1L;
	private static final String SESSION_CURRENT_BATCH_ID = "OUTWARD_MAKER_CURRENT_BATCH_ID";

	private static final String STATUS_PENDING_DATA_ENTRY = "PENDING_DATA_ENTRY";
	private static final String MODE_DATA_ENTRY = "DATA_ENTRY";
	private static final String RETURN_FROM_CHECKER = "RETURN_FROM_CHECKER";

	private static final int CHEQUES_PER_PAGE = 5;

	private Intbox txtExpectedTotalCheques;
	private Decimalbox txtExpectedTotalChequeAmount;
	private Textbox txtChequeFolder;

	private Button btnBrowse;
	private Button btnValidateBatch;

	private Vlayout vltBatchResult;

	private Label lblBatchIdValue;
	private Label lblTotalChequesValue;
	private Label lblTotalAmountValue;
	private Label lblChequeListCount;

	private Textbox txtChequeSearch;
	private Combobox cmbChequeFilter;
	private Button btnClearChequeFilter;

	private Listbox lstCheques;
	private Button btnChequePrevious;
	private Label lblChequePage;
	private Button btnChequeNext;

	private List<ScanCheque> currentChequeList = new ArrayList<ScanCheque>();
	private int currentChequePage = 0;
	private Component pageRoot;

	private ScanService scanService;
	private BatchValidationService batchValidationService;
	private OutwardMakerService outwardMakerService;

	private File uploadedZipFile;
	private String batchId;

	@Override
	public void doAfterCompose(Component component) throws Exception {

		pageRoot = component.getPage().getFirstRoot();

		txtExpectedTotalCheques = (Intbox) component.getFellow("txtExpectedTotalCheques");
		txtExpectedTotalChequeAmount = (Decimalbox) component.getFellow("txtExpectedTotalChequeAmount");
		txtChequeFolder = (Textbox) component.getFellow("txtChequeFolder");
		btnBrowse = (Button) component.getFellow("btnBrowse");
		btnValidateBatch = (Button) component.getFellow("btnValidateBatch");

		vltBatchResult = (Vlayout) component.getFellow("vltBatchResult");
		lblBatchIdValue = (Label) component.getFellow("lblBatchIdValue");
		lblTotalChequesValue = (Label) component.getFellow("lblTotalChequesValue");
		lblTotalAmountValue = (Label) component.getFellow("lblTotalAmountValue");
		lblChequeListCount = (Label) component.getFellow("lblChequeListCount");

		txtChequeSearch = (Textbox) component.getFellow("txtChequeSearch");
		cmbChequeFilter = (Combobox) component.getFellow("cmbChequeFilter");
		btnClearChequeFilter = (Button) component.getFellow("btnClearChequeFilter");

		lstCheques = (Listbox) component.getFellow("lstCheques");
		btnChequePrevious = (Button) component.getFellow("btnChequePrevious");
		lblChequePage = (Label) component.getFellow("lblChequePage");
		btnChequeNext = (Button) component.getFellow("btnChequeNext");

		scanService = new ScanServiceImpl();
		batchValidationService = new BatchValidationServiceImpl();
		outwardMakerService = new OutwardMakerServiceImpl();

		vltBatchResult.setVisible(false);
		lstCheques.getItems().clear();
		currentChequeList.clear();
		currentChequePage = 0;
		lblChequeListCount.setValue("0 Cheques");

		if (cmbChequeFilter.getItemCount() > 0) {
			cmbChequeFilter.setSelectedIndex(0);
		} else if (cmbChequeFilter.getValue() == null || cmbChequeFilter.getValue().trim().isEmpty()) {
			cmbChequeFilter.setValue("All Cheques");
		}

		updateChequePagination();
		loadCurrentSessionBatch();
		btnValidateBatch.setDisabled(true);

		btnBrowse.addEventListener(Events.ON_UPLOAD, new EventListener<UploadEvent>() {
			@Override
			public void onEvent(UploadEvent event) {
				handleZipUpload(event);
			}
		});

		btnValidateBatch.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) {
				validateBatch();
			}
		});

		txtChequeSearch.addEventListener(Events.ON_CHANGING, new EventListener<InputEvent>() {
			@Override
			public void onEvent(InputEvent event) {
				applyChequeFilter(event.getValue());
			}
		});

		EventListener<Event> filterEventListener = new EventListener<Event>() {
			@Override
			public void onEvent(Event event) {
				currentChequePage = 0;
				displayFilteredChequeList();
			}
		};

		cmbChequeFilter.addEventListener(Events.ON_SELECT, filterEventListener);
		cmbChequeFilter.addEventListener(Events.ON_CHANGE, filterEventListener);

		btnClearChequeFilter.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) {
				txtChequeSearch.setValue("");
				if (cmbChequeFilter.getItemCount() > 0) {
					cmbChequeFilter.setSelectedIndex(0);
				} else {
					cmbChequeFilter.setValue("All Cheques");
				}
				currentChequePage = 0;
				displayFilteredChequeList("");
			}
		});

		btnChequePrevious.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) {
				if (currentChequePage > 0) {
					currentChequePage--;
					displayFilteredChequeList();
				}
			}
		});

		btnChequeNext.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
			@Override
			public void onEvent(Event event) {
				int totalPages = getTotalChequePages();
				if (currentChequePage < totalPages - 1) {
					currentChequePage++;
					displayFilteredChequeList();
				}
			}
		});
	}

	private void loadCurrentSessionBatch() {
		Session session = Sessions.getCurrent();
		if (session == null) return;

		Object sessionBatchId = session.getAttribute(SESSION_CURRENT_BATCH_ID);
		if (sessionBatchId == null) return;

		String currentSessionBatchId = sessionBatchId.toString().trim();
		if (currentSessionBatchId.isEmpty()) return;

		batchId = currentSessionBatchId;

		// Fetch directly from DB services
		ScanBatch makerBatch = outwardMakerService.getMakerBatch(batchId);
		if (makerBatch == null) {
			batchId = null;
			session.removeAttribute(SESSION_CURRENT_BATCH_ID);
			vltBatchResult.setVisible(false);
			lstCheques.getItems().clear();
			currentChequeList.clear();
			currentChequePage = 0;
			updateChequePagination();
			return;
		}

		List<ScanCheque> batchCheques = outwardMakerService.getMakerBatchCheques(batchId);
		if (batchCheques == null) {
			batchCheques = new ArrayList<ScanCheque>();
		}

		currentChequeList = new ArrayList<ScanCheque>(batchCheques);
		displayBatchInformation(makerBatch, batchCheques);
		displayChequeList(batchCheques);
	}

	private void handleZipUpload(UploadEvent uploadEvent) {
		Media media = uploadEvent.getMedia();
		if (media == null) return;

		String fileName = media.getName();
		if (fileName == null || !fileName.toLowerCase().endsWith(".zip")) {
			showErrorMessage("Please upload a ZIP file.");
			return;
		}

		String tempDataPath = Executions.getCurrent().getDesktop().getWebApp().getRealPath("/TempData");
		if (tempDataPath == null) {
			showErrorMessage("Unable to access TempData directory.");
			return;
		}

		File tempDataDirectory = new File(tempDataPath);
		if (!tempDataDirectory.exists() && !tempDataDirectory.mkdirs()) {
			showErrorMessage("Unable to create TempData directory.");
			return;
		}

		File destinationFile = new File(tempDataDirectory, fileName);

		try (InputStream inputStream = media.getStreamData();
			 FileOutputStream outputStream = new FileOutputStream(destinationFile)) {

			byte[] buffer = new byte[8192];
			int bytesRead;
			while ((bytesRead = inputStream.read(buffer)) != -1) {
				outputStream.write(buffer, 0, bytesRead);
			}
			outputStream.flush();

		} catch (Exception e) {
			e.printStackTrace();
			showErrorMessage("Unable to save uploaded ZIP file.");
			return;
		}

		uploadedZipFile = destinationFile;
		txtChequeFolder.setValue(fileName);
		btnValidateBatch.setDisabled(false);
	}

	private void validateBatch() {

		if (uploadedZipFile == null || !uploadedZipFile.exists()) {
			showErrorMessage("Please upload a ZIP file before validating the batch.");
			return;
		}

		Integer expectedTotalCheques = txtExpectedTotalCheques.getValue();
		if (expectedTotalCheques == null || expectedTotalCheques <= 0) {
			showErrorMessage("Please enter a valid expected cheque count.");
			return;
		}

		BigDecimal expectedTotalAmount = txtExpectedTotalChequeAmount.getValue();
		if (expectedTotalAmount == null || expectedTotalAmount.signum() <= 0) {
			showErrorMessage("Please enter a valid expected total amount.");
			return;
		}

		try {
			BatchXmlParser parser = new BatchXmlParser();
			BatchXmlParser.ParsedBatchData parsedData = parser.parse(uploadedZipFile.getAbsolutePath());

			ScanBatch scanBatch = parsedData.getScanBatch();
			if (scanBatch == null) {
				throw new RuntimeException("Batch information could not be parsed.");
			}

			List<ScanCheque> chequeList = parsedData.getChequeList();
			if (chequeList == null || chequeList.isEmpty()) {
				throw new RuntimeException("No cheques were found in the uploaded batch.");
			}

			batchId = scanBatch.getScannedBatchId();
			if (batchId == null || batchId.trim().isEmpty()) {
				throw new RuntimeException("Batch ID was not found in the XML.");
			}
			batchId = batchId.trim();

			BatchValidationData validationData = new BatchValidationData();
			validationData.setBatch(scanBatch);
			validationData.setChequeList(chequeList);
			validationData.setExpectedTotalCheques(expectedTotalCheques);
			validationData.setExpectedTotalAmount(expectedTotalAmount);

			ValidationResult validationResult = batchValidationService.validateBatch(validationData);
			if (!validationResult.isValid()) {
				showErrorMessage(validationResult.getMessage());
				return;
			}

			MicrCodeHelper micrCodeHelper = new MicrCodeHelper();
			List<ScanCheque> micrChequeList = micrCodeHelper.checkMicrCode(chequeList);

			String savedBatchId = scanService.saveScanBatch(scanBatch, micrChequeList);
			if (savedBatchId == null || savedBatchId.trim().isEmpty()) {
				throw new RuntimeException("Batch could not be saved.");
			}

			batchId = savedBatchId.trim();

			// Store ONLY the batch ID in the session
			Session session = Sessions.getCurrent();
			if (session != null) {
				session.setAttribute(SESSION_CURRENT_BATCH_ID, batchId);
			}

			// Clean up UI inputs
			txtExpectedTotalCheques.setValue(null);
			txtExpectedTotalChequeAmount.setValue(BigDecimal.ZERO);
			txtChequeFolder.setValue("");
			uploadedZipFile = null;
			btnValidateBatch.setDisabled(true);

			// Fetch persisted data back from DB to ensure state consistency
			ScanBatch persistedBatch = outwardMakerService.getMakerBatch(batchId);
			List<ScanCheque> persistedCheques = outwardMakerService.getMakerBatchCheques(batchId);
			if (persistedCheques == null) {
				persistedCheques = new ArrayList<ScanCheque>();
			}

			currentChequeList = new ArrayList<ScanCheque>(persistedCheques);
			displayBatchInformation(persistedBatch != null ? persistedBatch : scanBatch, currentChequeList);
			displayChequeList(currentChequeList);

		} catch (Exception e) {
			e.printStackTrace();
			String errorMessage = e.getMessage();
			if (errorMessage == null || errorMessage.trim().isEmpty()) {
				errorMessage = "Something went wrong while processing the batch.";
			}
			showErrorMessage(errorMessage);
		}
	}

	private void displayBatchInformation(ScanBatch scanBatch, List<ScanCheque> chequeList) {
		if (scanBatch == null) return;

		String displayBatchId = scanBatch.getScannedBatchId();
		if (displayBatchId == null || displayBatchId.trim().isEmpty()) {
			displayBatchId = batchId;
		}

		lblBatchIdValue.setValue(safe(displayBatchId));
		int totalCheques = (chequeList == null) ? 0 : chequeList.size();
		lblTotalChequesValue.setValue(String.valueOf(totalCheques));

		BigDecimal totalAmount = calculateChequeTotal(chequeList);
		lblTotalAmountValue.setValue(formatAmount(totalAmount));

		vltBatchResult.setVisible(true);
	}

	private void displayChequeList(List<ScanCheque> chequeList) {
		if (chequeList == null) {
			currentChequeList = new ArrayList<ScanCheque>();
		} else {
			currentChequeList = new ArrayList<ScanCheque>(chequeList);
		}
		currentChequePage = 0;
		displayFilteredChequeList();
	}

	private void applyChequeFilter(String searchValue) {
		currentChequePage = 0;
		displayFilteredChequeList(searchValue);
	}

	private void displayFilteredChequeList() {
		displayFilteredChequeList(txtChequeSearch.getValue());
	}

	private void displayFilteredChequeList(String searchKeyword) {
		lstCheques.getItems().clear();

		String searchText = (searchKeyword == null) ? "" : searchKeyword.trim().toLowerCase();

		String filterValue = "All Cheques";
		Comboitem selectedItem = cmbChequeFilter.getSelectedItem();
		if (selectedItem != null && selectedItem.getLabel() != null && !selectedItem.getLabel().trim().isEmpty()) {
			filterValue = selectedItem.getLabel().trim();
		} else if (cmbChequeFilter.getValue() != null && !cmbChequeFilter.getValue().trim().isEmpty()) {
			filterValue = cmbChequeFilter.getValue().trim();
		}

		List<ScanCheque> filteredCheques = new ArrayList<ScanCheque>();

		for (ScanCheque cheque : currentChequeList) {
			if (cheque == null) continue;

			String chequeNumber = cheque.getChequeNumber();
			String accountNo = cheque.getDraweeAccountNumber();

			boolean matchesSearch = searchText.isEmpty() ||
					(chequeNumber != null && chequeNumber.toLowerCase().contains(searchText)) ||
					(accountNo != null && accountNo.toLowerCase().contains(searchText));

			if (!matchesSearch) continue;

			String status = cheque.getChequeStatus();
			if (status == null) status = "";
			status = status.trim().toUpperCase();

			boolean isChequeMicrRepair = isMicrRepairStatus(status);
			boolean isChequeDataEntry = isDataEntryStatus(status);

			boolean matchesType = true;
			String fUpper = filterValue.toUpperCase();

			if (fUpper.contains("MICR")) {
				matchesType = isChequeMicrRepair;
			} else if (fUpper.contains("DATA ENTRY") || fUpper.contains("ENTRY")) {
				matchesType = isChequeDataEntry;
			}

			if (matchesType) {
				filteredCheques.add(cheque);
			}
		}

		int totalPages = filteredCheques.isEmpty() ? 1 : (int) Math.ceil((double) filteredCheques.size() / CHEQUES_PER_PAGE);
		if (currentChequePage >= totalPages) {
			currentChequePage = totalPages - 1;
		}
		if (currentChequePage < 0) {
			currentChequePage = 0;
		}

		int startIndex = currentChequePage * CHEQUES_PER_PAGE;
		int endIndex = Math.min(startIndex + CHEQUES_PER_PAGE, filteredCheques.size());
		int serialNumber = startIndex + 1;

		for (int i = startIndex; i < endIndex; i++) {
			ScanCheque cheque = filteredCheques.get(i);
			if (cheque == null) continue;

			Listitem item = new Listitem();

			item.appendChild(new Listcell(String.valueOf(serialNumber++)));
			item.appendChild(new Listcell(safe(cheque.getChequeNumber())));
			item.appendChild(new Listcell(safe(cheque.getDraweeAccountNumber())));
			item.appendChild(new Listcell(formatDate(cheque.getChequeDate())));
			item.appendChild(new Listcell(formatAmount(cheque.getChequeAmount())));

			String rawStatus = cheque.getChequeStatus();
			if (rawStatus == null || rawStatus.trim().isEmpty()) {
				rawStatus = "";
			}
			rawStatus = rawStatus.trim().toUpperCase();

			String displayStatus = getDisplayStatus(rawStatus);

			Listcell statusCell = new Listcell();
			Label statusLabel = new Label(displayStatus);

			boolean isMicrRepair = isMicrRepairStatus(rawStatus);

			if (isMicrRepair) {
				statusLabel.setSclass("chequeStatusMicrRepair");
			} else {
				statusLabel.setSclass("chequeStatusDataEntry");
			}

			statusCell.appendChild(statusLabel);
			item.appendChild(statusCell);

			Listcell actionCell = new Listcell();
			Button actionButton = new Button();

			final String selectedBatchId = (batchId != null) ? batchId : "";
			final String selectedChequeId = cheque.getScannedChequeId();
			final String currentStatus = rawStatus;

			if (isMicrRepair) {
				actionButton.setLabel("MICR Repair");
				actionButton.setSclass("btnChequeAction");
				actionButton.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) {
						openMicrRepairPopup("SCAN", selectedBatchId, selectedChequeId);
					}
				});
			} else {
				actionButton.setLabel("Data Entry");
				actionButton.setSclass("btnChequeAction");
				actionButton.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
					@Override
					public void onEvent(Event event) {
						openDataEntryPopup(selectedBatchId, selectedChequeId, currentStatus);
					}
				});
			}

			actionCell.appendChild(actionButton);
			item.appendChild(actionCell);

			lstCheques.appendChild(item);
		}

		lblChequeListCount.setValue(filteredCheques.size() + " Cheques");
		updateChequePagination(filteredCheques.size());
	}

	private boolean isMicrRepairStatus(String status) {
		if (status == null) return false;
		return "PENDING_MICR_REPAIR".equals(status)
				|| "MICR_REJECTED".equals(status)
				|| "MICR_REJECTION_PENDING".equals(status)
				|| "MICR_FAILED".equals(status)
				|| "INVALID_MICR".equals(status)
				|| status.contains("MICR_REPAIR");
	}

	private boolean isDataEntryStatus(String status) {
		if (status == null) return false;
		return STATUS_PENDING_DATA_ENTRY.equals(status)
				|| "MICR_REPAIRED".equals(status)
				|| status.contains("DATA_ENTRY");
	}

	private void openDataEntryPopup(String bId, String chequeId) {
		openDataEntryPopup(bId, chequeId, STATUS_PENDING_DATA_ENTRY);
	}

	private void openDataEntryPopup(String bId, String chequeId, String status) {
		try {
			Include mainContentArea = findMainInclude();
			if (mainContentArea == null) {
				showErrorMessage("Unable to find main area.");
				return;
			}

			String targetBatchId = (bId != null) ? bId.trim() : "";
			if (targetBatchId.isEmpty() && batchId != null) {
				targetBatchId = batchId.trim();
			}

			String targetChequeId = (chequeId != null) ? chequeId.trim() : "";

			if (targetBatchId.isEmpty()) {
				showErrorMessage("Batch ID is missing.");
				return;
			}

			Session session = Sessions.getCurrent();
			if (session != null) {
				session.setAttribute("batchId", targetBatchId);
				session.setAttribute("chequeId", targetChequeId);
				session.setAttribute("outwardChequeId", targetChequeId);
				session.setAttribute("OUTWARD_MAKER_CURRENT_BATCH_ID", targetBatchId);
			}

			if (Executions.getCurrent() != null) {
				Executions.getCurrent().setAttribute("batchId", targetBatchId);
				Executions.getCurrent().setAttribute("chequeId", targetChequeId);
				Executions.getCurrent().setAttribute("outwardChequeId", targetChequeId);
				Executions.getCurrent().setAttribute("mode", MODE_DATA_ENTRY);
				Executions.getCurrent().setAttribute(RETURN_FROM_CHECKER, true);
			}

			mainContentArea.setAttribute("batchId", targetBatchId);
			mainContentArea.setAttribute("chequeId", targetChequeId);
			mainContentArea.setAttribute("outwardChequeId", targetChequeId);
			mainContentArea.setAttribute("mode", MODE_DATA_ENTRY);
			mainContentArea.setAttribute(RETURN_FROM_CHECKER, true);

			String targetSrc = "/outward/maker/cheque-data-entry.zul"
					+ "?batchId=" + targetBatchId
					+ "&chequeId=" + targetChequeId
					+ "&outwardChequeId=" + targetChequeId
					+ "&mode=" + MODE_DATA_ENTRY
					+ "&" + RETURN_FROM_CHECKER + "=true";

			mainContentArea.setSrc(null);
			mainContentArea.setSrc(targetSrc);

		} catch (Exception e) {
			e.printStackTrace();
			showErrorMessage("Unable to open Data Entry view.");
		}
	}

	private void updateChequePagination() {
		int totalCount = (currentChequeList == null) ? 0 : currentChequeList.size();
		updateChequePagination(totalCount);
	}

	private void updateChequePagination(int totalItems) {
		int totalPages = totalItems <= 0 ? 1 : (int) Math.ceil((double) totalItems / CHEQUES_PER_PAGE);
		lblChequePage.setValue("Page " + (currentChequePage + 1) + " of " + totalPages);
		btnChequePrevious.setDisabled(currentChequePage <= 0);
		btnChequeNext.setDisabled(currentChequePage >= totalPages - 1);
	}

	private int getTotalChequePages() {
		if (currentChequeList == null || currentChequeList.isEmpty()) {
			return 1;
		}
		return (int) Math.ceil((double) currentChequeList.size() / CHEQUES_PER_PAGE);
	}

	public void openMicrRepairPopup(String source, String batchId, String chequeId) {
		try {
			String src = source;
			if (src == null || src.trim().isEmpty()) {
				src = (String) Sessions.getCurrent().getAttribute("MICR_REPAIR_SOURCE");
			}
			if (src == null || src.trim().isEmpty()) {
				src = "SCAN";
			} else {
				src = src.trim();
			}

			String bId = (batchId != null) ? batchId.trim() : "";
			String cId = (chequeId != null) ? chequeId.trim() : "";

			if (bId.isEmpty()) return;

			Sessions.getCurrent().setAttribute("MICR_REPAIR_SOURCE", src);
			Sessions.getCurrent().setAttribute("MICR_REPAIR_BATCH_ID", bId);
			Sessions.getCurrent().setAttribute("MICR_REPAIR_CHEQUE_ID", cId);

			Include mainInclude = findMainInclude();

			if (mainInclude != null) {
				Executions.getCurrent().setAttribute("targetSourceCode", src);
				Executions.getCurrent().setAttribute("targetBatchId", bId);
				Executions.getCurrent().setAttribute("targetChequeId", cId);

				mainInclude.setAttribute("MICR_REPAIR_SOURCE", src);
				mainInclude.setAttribute("MICR_REPAIR_BATCH_ID", bId);
				mainInclude.setAttribute("MICR_REPAIR_CHEQUE_ID", cId);

				mainInclude.setSrc(null);
				mainInclude.setSrc(
					"/outward/maker/micr-repair/micr-repair.zul"
					+ "?source=" + src
					+ "&batchId=" + bId
					+ "&chequeId=" + cId
				);
			} else {
				Map<String, Object> args = new HashMap<String, Object>();
				args.put("source", src);
				args.put("batchId", bId);
				args.put("chequeId", cId);

				Component parent = (pageRoot != null) ? pageRoot : Executions.getCurrent().getDesktop().getFirstPage().getFirstRoot();
				Window win = (Window) Executions.createComponents("/outward/maker/micr-repair/micr-repair.zul", parent, args);
				win.doModal();
			}
		} catch (Exception e) {
			e.printStackTrace();
			showErrorMessage("Unable to open MICR Repair view.");
		}
	}

	public void openMicrRepairPopup(String batchId, String chequeId) {
		openMicrRepairPopup("SCAN", batchId, chequeId);
	}

	private Include findMainInclude() {
		try {
			Include inc = (Include) Path.getComponent("/outwardMakerRootWin/mainContentArea");
			if (inc != null) return inc;
		} catch (Exception ignored) {}

		if (Executions.getCurrent() != null) {
			for (org.zkoss.zk.ui.Page page : Executions.getCurrent().getDesktop().getPages()) {
				Component comp = page.getFellowIfAny("mainContentArea", true);
				if (comp == null) {
					comp = page.getFellowIfAny("mainInclude", true);
				}
				if (comp instanceof Include) {
					return (Include) comp;
				}
			}
		}
		return null;
	}

	private String getDisplayStatus(String backendStatus) {
		if (backendStatus == null || backendStatus.trim().isEmpty()) {
			return "-";
		}

		String status = backendStatus.trim().toUpperCase();

		if (isMicrRepairStatus(status)) {
			if ("MICR_REJECTED".equals(status)) return "MICR Rejected";
			if ("MICR_REJECTION_PENDING".equals(status)) return "MICR Rejection Pending";
			return "MICR Repair";
		}

		if (isDataEntryStatus(status)) {
			if ("MICR_REPAIRED".equals(status)) return "MICR Repaired";
			return "Data Entry";
		}

		String readable = status.replace("_", " ");
		return readable.substring(0, 1).toUpperCase() + readable.substring(1).toLowerCase();
	}

	private BigDecimal calculateChequeTotal(List<ScanCheque> chequeList) {
		BigDecimal total = BigDecimal.ZERO;
		if (chequeList == null) return total;

		for (ScanCheque cheque : chequeList) {
			if (cheque != null && cheque.getChequeAmount() != null) {
				total = total.add(cheque.getChequeAmount());
			}
		}
		return total;
	}

	private String safe(String value) {
		return (value == null || value.trim().isEmpty()) ? "-" : value.trim();
	}

	private String formatDate(Date date) {
		if (date == null) return "-";
		try {
			SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
			return sdf.format(date);
		} catch (Exception e) {
			return "-";
		}
	}

	private String formatAmount(BigDecimal amount) {
		if (amount == null) return "0.00";
		return String.format("%.2f", amount);
	}

	private void showErrorMessage(String message) {
		Messagebox.show(
				message,
				"Error",
				Messagebox.OK,
				Messagebox.ERROR);
	}
}