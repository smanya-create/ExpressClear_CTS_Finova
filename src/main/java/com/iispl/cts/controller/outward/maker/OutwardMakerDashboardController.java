package com.iispl.cts.controller.outward.maker;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.OutwardChequeRequest;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.service.outward.OutwardBatchService;
import com.iispl.cts.service.outward.OutwardChequeRequestService;
import com.iispl.cts.service.outward.OutwardChequeService;
import com.iispl.cts.service.outward.ScanService;
import com.iispl.cts.serviceimpl.outward.OutwardBatchServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardChequeRequestServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardChequeServiceImpl;
import com.iispl.cts.serviceimpl.outward.ScanServiceImpl;

public class OutwardMakerDashboardController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;

	private static final int PAGE_SIZE = 5;

	private static final String STATUS_PENDING_MAKER_PROCESS = "PENDING_MAKER_PROCESS";
	private static final String STATUS_PENDING_DATA_ENTRY = "PENDING_DATA_ENTRY";
	private static final String STATUS_PENDING_MICR_REPAIR = "PENDING_MICR_REPAIR";
	private static final String STATUS_MICR_REJECTED = "MICR_REJECTED";
	private static final String STATUS_PENDING_CHECKER_PROCESS = "PENDING_CHECKER_PROCESS";
	private static final String STATUS_PENDING_VERIFICATION = "PENDING_VERIFICATION";
	private static final String STATUS_ON_HOLD = "ON_HOLD";
	private static final String STATUS_REJECTION_REQUEST = "REJECTION_REQUEST";

	private static final String MODULE_ALL = "ALL";
	private static final String MODULE_RETURN_FROM_CHECKER = "RETURN_FROM_CHECKER";
	private static final String MODULE_BATCH_PROCESSING = "BATCH_PROCESSING";

	private static final String MODE_DATA_ENTRY = "DATA_ENTRY";
	private static final String RETURN_FROM_CHECKER = "RETURN_FROM_CHECKER";

	private Textbox outwardMakerTxtSearch;
	private Combobox outwardMakerCmbModule;
	private Combobox outwardMakerCmbStatus;
	private Button outwardMakerBtnSearch;
	private Button outwardMakerBtnClear;

	private Rows outwardMakerRowsBatchDetails;
	private Vlayout outwardMakerVlayoutEmptyState;
	private Label outwardMakerLblCurrentPage;

	private Button outwardMakerBtnFirst;
	private Button outwardMakerBtnPrevious;
	private Button outwardMakerBtnNext;
	private Button outwardMakerBtnLast;

	private Rows outwardMakerRowsReturnedBatches;
	private Vlayout outwardMakerVlayoutReturnedEmptyState;
	private Label outwardMakerLblReturnCurrentPage;

	private Button outwardMakerBtnReturnFirst;
	private Button outwardMakerBtnReturnPrevious;
	private Button outwardMakerBtnReturnNext;
	private Button outwardMakerBtnReturnLast;

	private ScanService scanService;
	private OutwardBatchService outwardBatchService;
	private OutwardChequeService outwardChequeService;
	private OutwardChequeRequestService outwardChequeRequestService;

	private List<ScanBatch> batchList = new ArrayList<>();
	private List<ReturnedChequeDisplayItem> returnedChequeList = new ArrayList<>();

	private int currentPage = 1;
	private int currentReturnPage = 1;

	private String currentSearchKeyword = "";
	private String currentModule = MODULE_ALL;
	private String currentStatus = MODULE_ALL;

	@Override
	public void doAfterCompose(Component component) throws Exception {

		super.doAfterCompose(component);

		scanService = new ScanServiceImpl();
		outwardBatchService = new OutwardBatchServiceImpl();
		outwardChequeService = new OutwardChequeServiceImpl();
		outwardChequeRequestService = new OutwardChequeRequestServiceImpl();

		outwardMakerTxtSearch = getFellow(component, "outwardMakerTxtSearch", Textbox.class);

		outwardMakerCmbModule = getFellow(component, "outwardMakerCmbModule", Combobox.class);

		outwardMakerCmbStatus = getFellow(component, "outwardMakerCmbStatus", Combobox.class);

		outwardMakerBtnSearch = getFellow(component, "outwardMakerBtnSearch", Button.class);

		outwardMakerBtnClear = getFellow(component, "outwardMakerBtnClear", Button.class);

		outwardMakerRowsBatchDetails = getFellow(component, "outwardMakerRowsBatchDetails", Rows.class);

		outwardMakerVlayoutEmptyState = getFellow(component, "outwardMakerVlayoutEmptyState", Vlayout.class);

		outwardMakerLblCurrentPage = getFellow(component, "outwardMakerLblCurrentPage", Label.class);

		outwardMakerBtnFirst = getFellow(component, "outwardMakerBtnFirst", Button.class);

		outwardMakerBtnPrevious = getFellow(component, "outwardMakerBtnPrevious", Button.class);

		outwardMakerBtnNext = getFellow(component, "outwardMakerBtnNext", Button.class);

		outwardMakerBtnLast = getFellow(component, "outwardMakerBtnLast", Button.class);

		outwardMakerRowsReturnedBatches = getFellow(component, "outwardMakerRowsReturnedBatches", Rows.class);

		outwardMakerVlayoutReturnedEmptyState = getFellow(component, "outwardMakerVlayoutReturnedEmptyState",
				Vlayout.class);

		outwardMakerLblReturnCurrentPage = getFellow(component, "outwardMakerLblReturnCurrentPage", Label.class);

		outwardMakerBtnReturnFirst = getFellow(component, "outwardMakerBtnReturnFirst", Button.class);

		outwardMakerBtnReturnPrevious = getFellow(component, "outwardMakerBtnReturnPrevious", Button.class);

		outwardMakerBtnReturnNext = getFellow(component, "outwardMakerBtnReturnNext", Button.class);

		outwardMakerBtnReturnLast = getFellow(component, "outwardMakerBtnReturnLast", Button.class);

		initializeFilters();
		bindFilterEvents();
		bindProcessingPagination();
		bindReturnPagination();

		refreshAllDashboardData();
	}

	private <T extends Component> T getFellow(Component component, String id, Class<T> type) {

		if (component == null || id == null || id.trim().isEmpty() || type == null) {
			return null;
		}

		Component fellow = component.getFellowIfAny(id);

		if (fellow == null) {
			return null;
		}

		if (!type.isInstance(fellow)) {
			throw new IllegalStateException("Component '" + id + "' is not of type " + type.getName());
		}

		return type.cast(fellow);
	}

	private void initializeFilters() {

		if (outwardMakerCmbModule != null && outwardMakerCmbModule.getItemCount() > 0) {

			outwardMakerCmbModule.setSelectedIndex(0);
		}

		if (outwardMakerCmbStatus != null && outwardMakerCmbStatus.getItemCount() > 0) {

			outwardMakerCmbStatus.setSelectedIndex(0);
		}
	}

	private void bindFilterEvents() {

		if (outwardMakerBtnSearch != null) {

			outwardMakerBtnSearch.addEventListener("onClick", event -> onApplyFilter());
		}

		if (outwardMakerBtnClear != null) {

			outwardMakerBtnClear.addEventListener("onClick", event -> onClearFilter());
		}

		if (outwardMakerTxtSearch != null) {

			outwardMakerTxtSearch.addEventListener("onOK", event -> onApplyFilter());
		}

		if (outwardMakerCmbModule != null) {

			outwardMakerCmbModule.addEventListener("onChange", event -> onApplyFilter());
		}

		if (outwardMakerCmbStatus != null) {

			outwardMakerCmbStatus.addEventListener("onChange", event -> onApplyFilter());
		}
	}

	private void bindProcessingPagination() {

		if (outwardMakerBtnFirst != null) {

			outwardMakerBtnFirst.addEventListener("onClick", event -> goToFirstPage());
		}

		if (outwardMakerBtnPrevious != null) {

			outwardMakerBtnPrevious.addEventListener("onClick", event -> goToPreviousPage());
		}

		if (outwardMakerBtnNext != null) {

			outwardMakerBtnNext.addEventListener("onClick", event -> goToNextPage());
		}

		if (outwardMakerBtnLast != null) {

			outwardMakerBtnLast.addEventListener("onClick", event -> goToLastPage());
		}
	}

	private void bindReturnPagination() {

		if (outwardMakerBtnReturnFirst != null) {

			outwardMakerBtnReturnFirst.addEventListener("onClick", event -> goToFirstReturnPage());
		}

		if (outwardMakerBtnReturnPrevious != null) {

			outwardMakerBtnReturnPrevious.addEventListener("onClick", event -> goToPreviousReturnPage());
		}

		if (outwardMakerBtnReturnNext != null) {

			outwardMakerBtnReturnNext.addEventListener("onClick", event -> goToNextReturnPage());
		}

		if (outwardMakerBtnReturnLast != null) {

			outwardMakerBtnReturnLast.addEventListener("onClick", event -> goToLastReturnPage());
		}
	}

	private void onApplyFilter() {

		currentSearchKeyword = outwardMakerTxtSearch == null || outwardMakerTxtSearch.getValue() == null ? ""
				: outwardMakerTxtSearch.getValue().trim();

		currentModule = getSelectedValue(outwardMakerCmbModule, MODULE_ALL);

		currentStatus = getSelectedValue(outwardMakerCmbStatus, MODULE_ALL);

		if (currentModule == null || currentModule.trim().isEmpty()) {

			currentModule = MODULE_ALL;
		}

		if (currentStatus == null || currentStatus.trim().isEmpty()) {

			currentStatus = MODULE_ALL;
		}

		refreshAllDashboardData();
	}

	private void onClearFilter() {

		currentSearchKeyword = "";
		currentModule = MODULE_ALL;
		currentStatus = MODULE_ALL;

		if (outwardMakerTxtSearch != null) {
			outwardMakerTxtSearch.setValue("");
		}

		if (outwardMakerCmbModule != null && outwardMakerCmbModule.getItemCount() > 0) {

			outwardMakerCmbModule.setSelectedIndex(0);
		}

		if (outwardMakerCmbStatus != null && outwardMakerCmbStatus.getItemCount() > 0) {

			outwardMakerCmbStatus.setSelectedIndex(0);
		}

		refreshAllDashboardData();
	}

	private String getSelectedValue(Combobox combobox, String defaultValue) {

		if (combobox == null) {
			return defaultValue;
		}

		Comboitem selectedItem = combobox.getSelectedItem();

		if (selectedItem == null) {
			return defaultValue;
		}

		Object value = selectedItem.getValue();

		String result = toSafeString(value);

		if (result.isEmpty()) {
			return defaultValue;
		}

		return result.trim().toUpperCase(Locale.ENGLISH);
	}

	private void refreshAllDashboardData() {

		boolean showReturned = MODULE_ALL.equalsIgnoreCase(currentModule)
				|| MODULE_RETURN_FROM_CHECKER.equalsIgnoreCase(currentModule);

		boolean showProcessing = MODULE_ALL.equalsIgnoreCase(currentModule)
				|| MODULE_BATCH_PROCESSING.equalsIgnoreCase(currentModule);

		if (showReturned) {

			loadReturnedCheques();

		} else {

			returnedChequeList = new ArrayList<>();

			currentReturnPage = 1;

			renderCurrentReturnPage();
		}

		if (showProcessing) {

			loadBatches();

		} else {

			batchList = new ArrayList<>();

			currentPage = 1;

			renderCurrentPage();
		}
	}

	private void loadBatches() {
		batchList = new ArrayList<>();

		if (scanService == null) {
			renderCurrentPage();
			return;
		}

		try {
			List<ScanBatch> scanBatches = scanService.getMakerDashboardBatches();

			if (scanBatches == null || scanBatches.isEmpty()) {
				currentPage = 1;
				renderCurrentPage();
				return;
			}

			Map<String, ScanBatch> uniqueBatchMap = new LinkedHashMap<>();

			for (ScanBatch batch : scanBatches) {

				if (batch == null) {
					continue;
				}

				String batchId = toSafeString(batch.getScannedBatchId()).trim();

				if (batchId.isEmpty()) {
					continue;
				}

				String batchStatus = normalizeStatus(batch.getBatchStatus());

				if (!STATUS_PENDING_MAKER_PROCESS.equals(batchStatus)) {
					continue;
				}

				if (!matchesBatchSearch(batchId)) {
					continue;
				}

				if (!matchesNewBatchStatus(batchId, batchStatus)) {
					continue;
				}

				uniqueBatchMap.put(batchId, batch);
			}

			batchList = new ArrayList<>(uniqueBatchMap.values());

		} catch (Exception exception) {
			batchList = new ArrayList<>();
			exception.printStackTrace();
		}

		currentPage = 1;
		renderCurrentPage();
	}

	private boolean matchesNewBatchStatus(String batchId, String batchStatus) {

		if (currentStatus == null || currentStatus.trim().isEmpty()
				|| MODULE_ALL.equalsIgnoreCase(currentStatus.trim())) {
			return true;
		}

		String selectedStatus = normalizeStatus(currentStatus);

		if (STATUS_PENDING_MAKER_PROCESS.equals(selectedStatus)) {
			return STATUS_PENDING_MAKER_PROCESS.equals(batchStatus);
		}

		if (outwardChequeService == null) {
			return false;
		}

		try {
			List<OutwardCheque> cheques = outwardChequeService.getChequesByBatchId(batchId);

			if (cheques == null || cheques.isEmpty()) {
				return false;
			}

			for (OutwardCheque cheque : cheques) {

				if (cheque == null) {
					continue;
				}

				String chequeStatus = normalizeStatus(cheque.getChequeStatus());

				if (selectedStatus.equals(chequeStatus)) {
					return true;
				}
			}

		} catch (Exception exception) {
			exception.printStackTrace();
		}

		return false;
	}

	private boolean matchesProcessingStatus(String status) {

		if (currentStatus == null || currentStatus.isEmpty() || MODULE_ALL.equalsIgnoreCase(currentStatus)) {

			return true;
		}

		return currentStatus.equals(normalizeStatus(status));
	}

	private boolean matchesBatchSearch(String actualBatchId) {

		if (currentSearchKeyword == null || currentSearchKeyword.isEmpty()) {

			return true;
		}

		if (actualBatchId == null || actualBatchId.trim().isEmpty()) {

			return false;
		}

		String actual = actualBatchId.trim().toUpperCase(Locale.ENGLISH);

		String search = currentSearchKeyword.trim().toUpperCase(Locale.ENGLISH);

		if (search.isEmpty()) {
			return true;
		}

		if (isThreeDigitSearch(search)) {

			if (!actual.startsWith("BAT")) {
				return false;
			}

			if (actual.length() != 6) {
				return false;
			}

			String suffix = actual.substring(3);

			return suffix.equals(search);
		}

		return actual.contains(search);
	}

	private boolean isThreeDigitSearch(String search) {

		if (search == null || search.length() != 3) {

			return false;
		}

		for (int index = 0; index < search.length(); index++) {

			if (!Character.isDigit(search.charAt(index))) {

				return false;
			}
		}

		return true;
	}

	private void loadReturnedCheques() {

		returnedChequeList = new ArrayList<>();

		if (outwardBatchService == null || outwardChequeService == null) {

			renderCurrentReturnPage();

			return;
		}

		try {

			List<OutwardBatch> onHoldBatches = outwardBatchService.getOnHoldBatches();

			if (onHoldBatches == null || onHoldBatches.isEmpty()) {

				renderCurrentReturnPage();

				return;
			}

			Map<String, ReturnedChequeDisplayItem> uniqueReturnedCheques = new LinkedHashMap<>();

			for (OutwardBatch batch : onHoldBatches) {

				if (batch == null) {
					continue;
				}

				String batchId = toSafeString(batch.getOutwardBatchId()).trim();

				if (batchId.isEmpty()) {
					continue;
				}

				if (!matchesBatchSearch(batchId)) {
					continue;
				}

				List<OutwardCheque> cheques;

				try {

					cheques = outwardChequeService.getOnHoldCheques(batchId);

				} catch (Exception exception) {

					exception.printStackTrace();

					continue;
				}

				if (cheques == null || cheques.isEmpty()) {

					continue;
				}

				for (OutwardCheque cheque : cheques) {

					if (cheque == null) {
						continue;
					}

					String chequeId = toSafeString(cheque.getOutwardChequeId()).trim();

					String chequeNumber = toSafeString(cheque.getChequeNumber()).trim();

					String chequeStatus = normalizeStatus(cheque.getChequeStatus());

					if (!isMakerReturnStatus(chequeStatus)) {

						continue;
					}

					if (!matchesReturnStatus(chequeStatus)) {

						continue;
					}

					if (chequeId.isEmpty()) {
						continue;
					}

					String uniqueKey = batchId + "|" + chequeId;

					String reasonRemarks = getReturnReasonRemarks(chequeId);

					uniqueReturnedCheques.put(uniqueKey,
							new ReturnedChequeDisplayItem(batchId, cheque, chequeStatus, reasonRemarks));
				}
			}

			returnedChequeList = new ArrayList<>(uniqueReturnedCheques.values());

		} catch (Exception exception) {

			returnedChequeList = new ArrayList<>();

			exception.printStackTrace();
		}

		currentReturnPage = 1;

		renderCurrentReturnPage();
	}

	private boolean isMakerReturnStatus(String chequeStatus) {

		return STATUS_PENDING_DATA_ENTRY.equals(chequeStatus) || STATUS_PENDING_MICR_REPAIR.equals(chequeStatus);
	}

	private boolean matchesReturnStatus(String chequeStatus) {

		if (currentStatus == null || currentStatus.isEmpty() || MODULE_ALL.equalsIgnoreCase(currentStatus)) {

			return true;
		}

		return currentStatus.equals(normalizeStatus(chequeStatus));
	}

	private String getReturnReasonRemarks(String chequeId) {

		if (chequeId == null || chequeId.trim().isEmpty() || outwardChequeRequestService == null) {

			return "-";
		}

		try {

			OutwardChequeRequest request = outwardChequeRequestService.getRequestByChequeId(chequeId.trim());

			if (request == null) {
				return "-";
			}

			String reason = toSafeString(request.getReason());

			String remarks = toSafeString(request.getRemarks());

			if (!reason.isEmpty() && !remarks.isEmpty()) {

				return reason + " - " + remarks;
			}

			if (!reason.isEmpty()) {
				return reason;
			}

			if (!remarks.isEmpty()) {
				return remarks;
			}

		} catch (Exception exception) {

			exception.printStackTrace();
		}

		return "-";
	}

	private void renderCurrentPage() {

		if (outwardMakerRowsBatchDetails == null) {
			return;
		}

		outwardMakerRowsBatchDetails.getChildren().clear();

		boolean hasData = batchList != null && !batchList.isEmpty();

		if (!hasData) {

			if (outwardMakerVlayoutEmptyState != null) {

				outwardMakerVlayoutEmptyState.setVisible(true);
			}

			setProcessingGridVisible(false);

			updatePagination();

			return;
		}

		if (outwardMakerVlayoutEmptyState != null) {

			outwardMakerVlayoutEmptyState.setVisible(false);
		}

		setProcessingGridVisible(true);

		int totalPages = getTotalPages();

		currentPage = normalizePage(currentPage, totalPages);

		int startIndex = (currentPage - 1) * PAGE_SIZE;

		int endIndex = Math.min(startIndex + PAGE_SIZE, batchList.size());

		for (int index = startIndex; index < endIndex; index++) {

			ScanBatch batch = batchList.get(index);

			if (batch != null) {

				createBatchRow(batch);
			}
		}

		updatePagination();
	}

	private void createBatchRow(ScanBatch batch) {

		if (batch == null || outwardMakerRowsBatchDetails == null) {

			return;
		}

		Row row = new Row();

		String batchId = toSafeString(batch.getScannedBatchId()).trim();

		Label batchIdLabel = new Label(safeDisplayValue(batchId));

		batchIdLabel.setSclass("outward-maker-batch-id");

		batchIdLabel.setStyle("cursor:pointer;");

		batchIdLabel.addEventListener("onClick", event -> openBatchDetails(batchId, STATUS_PENDING_MAKER_PROCESS));

		int chequeCount = Math.max(0, batch.getActualChequeCount());

		Label chequeCountLabel = new Label(Integer.toString(chequeCount));

		chequeCountLabel.setSclass("outward-maker-cheque-count");

		Label totalAmountLabel = new Label(formatIndianAmount(batch.getActualTotalAmount()));

		totalAmountLabel.setSclass("outward-maker-total-amount");

		String status = normalizeStatus(batch.getBatchStatus());

		Label statusLabel = new Label(getDisplayStatus(status));

		statusLabel.setSclass("outward-maker-status " + getStatusClass(status));

		Button viewButton = new Button("VIEW DETAILS");

		viewButton.setSclass("outward-maker-view-button");

		viewButton.addEventListener("onClick", event -> openBatchDetails(batchId, status));

		row.appendChild(batchIdLabel);
		row.appendChild(chequeCountLabel);
		row.appendChild(totalAmountLabel);
		row.appendChild(statusLabel);
		row.appendChild(viewButton);

		outwardMakerRowsBatchDetails.appendChild(row);
	}

	private void renderCurrentReturnPage() {

		if (outwardMakerRowsReturnedBatches == null) {
			return;
		}

		outwardMakerRowsReturnedBatches.getChildren().clear();

		boolean hasData = returnedChequeList != null && !returnedChequeList.isEmpty();

		if (!hasData) {

			if (outwardMakerVlayoutReturnedEmptyState != null) {

				outwardMakerVlayoutReturnedEmptyState.setVisible(true);
			}

			setReturnedGridVisible(false);

			updateReturnPagination();

			return;
		}

		if (outwardMakerVlayoutReturnedEmptyState != null) {

			outwardMakerVlayoutReturnedEmptyState.setVisible(false);
		}

		setReturnedGridVisible(true);

		int totalPages = getTotalReturnPages();

		currentReturnPage = normalizePage(currentReturnPage, totalPages);

		int startIndex = (currentReturnPage - 1) * PAGE_SIZE;

		int endIndex = Math.min(startIndex + PAGE_SIZE, returnedChequeList.size());

		for (int index = startIndex; index < endIndex; index++) {

			ReturnedChequeDisplayItem item = returnedChequeList.get(index);

			if (item != null) {

				createReturnedChequeRow(item);
			}
		}

		updateReturnPagination();
	}

	private void createReturnedChequeRow(ReturnedChequeDisplayItem item) {

		if (item == null || item.cheque == null || outwardMakerRowsReturnedBatches == null) {

			return;
		}

		Row row = new Row();

		Label batchIdLabel = new Label(safeDisplayValue(item.batchId));

		batchIdLabel.setSclass("outward-maker-return-batch-id");

		Label chequeNumberLabel = new Label(safeDisplayValue(item.cheque.getChequeNumber()));

		chequeNumberLabel.setSclass("outward-maker-cheque-number");

		String status = normalizeStatus(item.displayStatus);

		Label statusLabel = new Label(getDisplayStatus(status));

		statusLabel.setSclass("outward-maker-status " + getStatusClass(status));

		Label reasonLabel = new Label(safeDisplayValue(item.reasonRemarks));

		reasonLabel.setSclass("outward-maker-return-reason");

		Button modifyButton = new Button("MODIFY");

		modifyButton.setIconSclass("z-icon-edit");

		modifyButton.setSclass("outward-maker-return-action");

		modifyButton.addEventListener("onClick", event -> openReturnedChequeAction(item.cheque, status));

		row.appendChild(batchIdLabel);
		row.appendChild(chequeNumberLabel);
		row.appendChild(statusLabel);
		row.appendChild(reasonLabel);
		row.appendChild(modifyButton);

		outwardMakerRowsReturnedBatches.appendChild(row);
	}

	private void openReturnedChequeAction(OutwardCheque cheque, String status) {

		if (cheque == null) {
			return;
		}

		String chequeId = toSafeString(cheque.getOutwardChequeId()).trim();

		String batchId = toSafeString(cheque.getOutwardBatchId()).trim();

		if (chequeId.isEmpty() || batchId.isEmpty()) {

			return;
		}

		Include mainContentArea = findMainContentArea();

		if (mainContentArea == null) {
			return;
		}

		try {

			mainContentArea.clearDynamicProperties();

			mainContentArea.setDynamicProperty("batchId", batchId);

			mainContentArea.setDynamicProperty("chequeId", chequeId);

			mainContentArea.setDynamicProperty("outwardChequeId", chequeId);

			mainContentArea.setDynamicProperty("mode", MODE_DATA_ENTRY);

			mainContentArea.setDynamicProperty(RETURN_FROM_CHECKER, true);

			mainContentArea.setAttribute("batchId", batchId);

			mainContentArea.setAttribute("chequeId", chequeId);

			mainContentArea.setAttribute("outwardChequeId", chequeId);

			mainContentArea.setAttribute("mode", MODE_DATA_ENTRY);

			mainContentArea.setAttribute(RETURN_FROM_CHECKER, true);

			if (STATUS_PENDING_MICR_REPAIR.equals(status)) {

				mainContentArea.setSrc("/outward/maker/micr-repair/micr-repair-view.zul");

				return;
			}

			if (STATUS_PENDING_DATA_ENTRY.equals(status)) {

				mainContentArea.setSrc("/outward/maker/cheque-data-entry.zul");

				return;
			}

		} catch (Exception exception) {

			exception.printStackTrace();
		}
	}

	private void setProcessingGridVisible(boolean visible) {

		if (outwardMakerRowsBatchDetails == null) {
			return;
		}

		Component parent = outwardMakerRowsBatchDetails.getParent();

		if (parent != null) {
			parent.setVisible(visible);
		}
	}

	private void setReturnedGridVisible(boolean visible) {

		if (outwardMakerRowsReturnedBatches == null) {
			return;
		}

		Component parent = outwardMakerRowsReturnedBatches.getParent();

		if (parent != null) {
			parent.setVisible(visible);
		}
	}

	private void openBatchDetails(String batchId, String status) {

		String trimmedBatchId = toSafeString(batchId).trim();

		if (trimmedBatchId.isEmpty()) {
			return;
		}

		Include mainContentArea = findMainContentArea();

		if (mainContentArea == null) {
			return;
		}

		boolean returnedFromChecker = STATUS_ON_HOLD.equals(normalizeStatus(status));

		try {

			mainContentArea.clearDynamicProperties();

			mainContentArea.setDynamicProperty("batchId", trimmedBatchId);

			mainContentArea.setDynamicProperty("mode", MODE_DATA_ENTRY);

			mainContentArea.setDynamicProperty(RETURN_FROM_CHECKER, returnedFromChecker);

			mainContentArea.setAttribute("batchId", trimmedBatchId);

			mainContentArea.setAttribute("mode", MODE_DATA_ENTRY);

			mainContentArea.setAttribute(RETURN_FROM_CHECKER, returnedFromChecker);

			mainContentArea.setSrc("/outward/maker/batch-details.zul");

		} catch (Exception exception) {

			exception.printStackTrace();
		}
	}

	private Include findMainContentArea() {

		Component current = outwardMakerRowsBatchDetails;

		while (current != null) {

			if (current instanceof Include) {

				Include include = (Include) current;

				if ("mainContentArea".equals(include.getId())) {

					return include;
				}
			}

			current = current.getParent();
		}

		return null;
	}

	private void goToFirstPage() {

		if (currentPage <= 1) {
			return;
		}

		currentPage = 1;

		renderCurrentPage();
	}

	private void goToPreviousPage() {

		if (currentPage <= 1) {
			return;
		}

		currentPage--;

		renderCurrentPage();
	}

	private void goToNextPage() {

		int totalPages = getTotalPages();

		if (currentPage >= totalPages) {
			return;
		}

		currentPage++;

		renderCurrentPage();
	}

	private void goToLastPage() {

		int totalPages = getTotalPages();

		if (currentPage >= totalPages) {
			return;
		}

		currentPage = totalPages;

		renderCurrentPage();
	}

	private void goToFirstReturnPage() {

		if (currentReturnPage <= 1) {
			return;
		}

		currentReturnPage = 1;

		renderCurrentReturnPage();
	}

	private void goToPreviousReturnPage() {

		if (currentReturnPage <= 1) {
			return;
		}

		currentReturnPage--;

		renderCurrentReturnPage();
	}

	private void goToNextReturnPage() {

		int totalPages = getTotalReturnPages();

		if (currentReturnPage >= totalPages) {
			return;
		}

		currentReturnPage++;

		renderCurrentReturnPage();
	}

	private void goToLastReturnPage() {

		int totalPages = getTotalReturnPages();

		if (currentReturnPage >= totalPages) {
			return;
		}

		currentReturnPage = totalPages;

		renderCurrentReturnPage();
	}

	private int getTotalPages() {

		if (batchList == null || batchList.isEmpty()) {

			return 1;
		}

		return (int) Math.ceil((double) batchList.size() / PAGE_SIZE);
	}

	private int getTotalReturnPages() {

		if (returnedChequeList == null || returnedChequeList.isEmpty()) {

			return 1;
		}

		return (int) Math.ceil((double) returnedChequeList.size() / PAGE_SIZE);
	}

	private int normalizePage(int page, int totalPages) {

		int safeTotalPages = Math.max(1, totalPages);

		if (page < 1) {
			return 1;
		}

		if (page > safeTotalPages) {
			return safeTotalPages;
		}

		return page;
	}

	private void updatePagination() {

		int totalPages = getTotalPages();

		if (outwardMakerLblCurrentPage != null) {

			outwardMakerLblCurrentPage.setValue(currentPage + " / " + totalPages);
		}

		boolean firstPage = currentPage <= 1;

		boolean lastPage = currentPage >= totalPages;

		if (outwardMakerBtnFirst != null) {

			outwardMakerBtnFirst.setDisabled(firstPage);
		}

		if (outwardMakerBtnPrevious != null) {

			outwardMakerBtnPrevious.setDisabled(firstPage);
		}

		if (outwardMakerBtnNext != null) {

			outwardMakerBtnNext.setDisabled(lastPage);
		}

		if (outwardMakerBtnLast != null) {

			outwardMakerBtnLast.setDisabled(lastPage);
		}
	}

	private void updateReturnPagination() {

		int totalPages = getTotalReturnPages();

		if (outwardMakerLblReturnCurrentPage != null) {

			outwardMakerLblReturnCurrentPage.setValue(currentReturnPage + " / " + totalPages);
		}

		boolean firstPage = currentReturnPage <= 1;

		boolean lastPage = currentReturnPage >= totalPages;

		if (outwardMakerBtnReturnFirst != null) {

			outwardMakerBtnReturnFirst.setDisabled(firstPage);
		}

		if (outwardMakerBtnReturnPrevious != null) {

			outwardMakerBtnReturnPrevious.setDisabled(firstPage);
		}

		if (outwardMakerBtnReturnNext != null) {

			outwardMakerBtnReturnNext.setDisabled(lastPage);
		}

		if (outwardMakerBtnReturnLast != null) {

			outwardMakerBtnReturnLast.setDisabled(lastPage);
		}
	}

	private String formatIndianAmount(BigDecimal amount) {

		BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount;

		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH);

		symbols.setGroupingSeparator(',');

		DecimalFormat formatter = new DecimalFormat("##,##,##0.00", symbols);

		return "₹" + formatter.format(safeAmount);
	}

	private String getDisplayStatus(String status) {

		String normalizedStatus = normalizeStatus(status);

		if (STATUS_PENDING_MAKER_PROCESS.equals(normalizedStatus)) {

			return "Maker Process Pending";
		}

		if (STATUS_PENDING_DATA_ENTRY.equals(normalizedStatus)) {

			return "Data Entry Pending";
		}

		if (STATUS_PENDING_MICR_REPAIR.equals(normalizedStatus)) {

			return "MICR Pending";
		}

		if (STATUS_MICR_REJECTED.equals(normalizedStatus)) {

			return "MICR Rejected";
		}

		if (STATUS_PENDING_CHECKER_PROCESS.equals(normalizedStatus)) {

			return "Pending Checker Process";
		}

		if (STATUS_PENDING_VERIFICATION.equals(normalizedStatus)) {

			return "Pending Verification";
		}

		if (STATUS_ON_HOLD.equals(normalizedStatus)) {

			return "Returned by Checker";
		}

		if (STATUS_REJECTION_REQUEST.equals(normalizedStatus)) {

			return "Rejection Requested";
		}

		return safeDisplayValue(status);
	}

	private String getStatusClass(String status) {

		String normalizedStatus = normalizeStatus(status);

		if (STATUS_PENDING_MAKER_PROCESS.equals(normalizedStatus)) {

			return "pending-maker-process";
		}

		if (STATUS_PENDING_DATA_ENTRY.equals(normalizedStatus)) {

			return "pending-data-entry";
		}

		if (STATUS_PENDING_MICR_REPAIR.equals(normalizedStatus)) {

			return "pending-micr-repair";
		}

		if (STATUS_MICR_REJECTED.equals(normalizedStatus)) {

			return "micr-rejected";
		}

		if (STATUS_PENDING_CHECKER_PROCESS.equals(normalizedStatus)) {

			return "pending-checker-process";
		}

		if (STATUS_PENDING_VERIFICATION.equals(normalizedStatus)) {

			return "pending-verification";
		}

		if (STATUS_ON_HOLD.equals(normalizedStatus)) {

			return "on-hold";
		}

		if (STATUS_REJECTION_REQUEST.equals(normalizedStatus)) {

			return "rejection-request";
		}

		return "pending-maker-process";
	}

	private String normalizeStatus(String status) {

		if (status == null || status.trim().isEmpty()) {

			return "";
		}

		return status.trim().toUpperCase(Locale.ENGLISH).replace('-', '_').replace(' ', '_');
	}

	private String safeDisplayValue(Object value) {

		String converted = toSafeString(value);

		return converted.isEmpty() ? "-" : converted;
	}

	private String toSafeString(Object value) {

		if (value == null) {
			return "";
		}

		if (value instanceof String) {

			return ((String) value).trim();
		}

		return String.valueOf(value).trim();
	}

	private static class ReturnedChequeDisplayItem {

		private final String batchId;
		private final OutwardCheque cheque;
		private final String displayStatus;
		private final String reasonRemarks;

		private ReturnedChequeDisplayItem(String batchId, OutwardCheque cheque, String displayStatus,
				String reasonRemarks) {

			this.batchId = batchId;
			this.cheque = cheque;
			this.displayStatus = displayStatus;
			this.reasonRemarks = reasonRemarks;
		}
	}
}