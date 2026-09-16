
package com.iispl.cts.controller.outward.maker;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Vlayout;

import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.service.outward.OutwardBatchService;
import com.iispl.cts.service.outward.OutwardChequeService;
import com.iispl.cts.service.outward.ScanService;
import com.iispl.cts.serviceimpl.outward.OutwardBatchServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardChequeServiceImpl;
import com.iispl.cts.serviceimpl.outward.ScanServiceImpl;

public class OutwardMakerBatchDetailsController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;

	private static final int PAGE_SIZE = 10;

	private static final String STATUS_PENDING_MAKER_PROCESS = "PENDING_MAKER_PROCESS";
	private static final String STATUS_MICR_REJECTED = "MICR_REJECTED";
	private static final String STATUS_PENDING_DATA_ENTRY = "PENDING_DATA_ENTRY";
	private static final String STATUS_PENDING_MICR_REPAIR = "PENDING_MICR_REPAIR";
	private static final String STATUS_PENDING_CHECKER_PROCESS = "PENDING_CHECKER_PROCESS";
	private static final String STATUS_PENDING_VERIFICATION = "PENDING_VERIFICATION";
	private static final String STATUS_ON_HOLD = "ON_HOLD";
	private static final String STATUS_REJECTION_REQUEST = "REJECTION_REQUEST";

	private static final String MODE_DATA_ENTRY = "DATA_ENTRY";
	private static final String RETURN_FROM_CHECKER = "RETURN_FROM_CHECKER";

	private Grid outwardMakerGridChequeDetails;
	private Rows outwardMakerRowsChequeDetails;
	private Vlayout outwardMakerVlayoutEmptyState;

	private Label outwardMakerLblBatchId;
	private Label outwardMakerLblChequeCount;
	private Label outwardMakerLblTotalAmount;
	private Label outwardMakerLblCurrentPage;

	private Button outwardMakerBtnFirst;
	private Button outwardMakerBtnPrevious;
	private Button outwardMakerBtnNext;
	private Button outwardMakerBtnLast;
	private Button outwardMakerBtnBack;

	private ScanService scanService;
	private OutwardBatchService outwardBatchService;
	private OutwardChequeService outwardChequeService;

	private ScanBatch scanBatch;
	private OutwardBatch outwardBatch;

	private List<ScanCheque> scanChequeList = new ArrayList<>();
	private List<OutwardCheque> outwardChequeList = new ArrayList<>();
	private List<ChequeDisplayItem> displayChequeList = new ArrayList<>();

	private boolean returnedBatch;
	private String currentBatchId;
	private int currentPage = 1;

	@Override
	public void doAfterCompose(Component component) throws Exception {

		super.doAfterCompose(component);

		scanService = new ScanServiceImpl();
		outwardBatchService = new OutwardBatchServiceImpl();
		outwardChequeService = new OutwardChequeServiceImpl();

		outwardMakerGridChequeDetails = getFellow(component, "outwardMakerGridChequeDetails", Grid.class);

		outwardMakerRowsChequeDetails = getFellow(component, "outwardMakerRowsChequeDetails", Rows.class);

		outwardMakerVlayoutEmptyState = getFellow(component, "outwardMakerVlayoutEmptyState", Vlayout.class);

		outwardMakerLblBatchId = getFellow(component, "outwardMakerLblBatchId", Label.class);

		outwardMakerLblChequeCount = getFellow(component, "outwardMakerLblChequeCount", Label.class);

		outwardMakerLblTotalAmount = getFellow(component, "outwardMakerLblTotalAmount", Label.class);

		outwardMakerLblCurrentPage = getFellow(component, "outwardMakerLblCurrentPage", Label.class);

		outwardMakerBtnFirst = getFellow(component, "outwardMakerBtnFirst", Button.class);

		outwardMakerBtnPrevious = getFellow(component, "outwardMakerBtnPrevious", Button.class);

		outwardMakerBtnNext = getFellow(component, "outwardMakerBtnNext", Button.class);

		outwardMakerBtnLast = getFellow(component, "outwardMakerBtnLast", Button.class);

		outwardMakerBtnBack = getFellow(component, "outwardMakerBtnBack", Button.class);

		bindEvents();

		loadBatchDetails();
	}

	private <T extends Component> T getFellow(Component component, String id, Class<T> type) {

		if (component == null || id == null || type == null) {
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

	private void bindEvents() {

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

		if (outwardMakerBtnBack != null) {
			outwardMakerBtnBack.addEventListener("onClick", event -> goBackToDashboard());
		}
	}

	private void loadBatchDetails() {

		currentBatchId = resolveBatchId();

		if (currentBatchId == null || currentBatchId.isEmpty()) {
			showEmptyState();
			return;
		}

		try {

			if (loadReturnedBatch(currentBatchId)) {
				return;
			}

			loadScanBatch(currentBatchId);

		} catch (Exception exception) {

			exception.printStackTrace();

			clearData();
			showEmptyState();
		}
	}

	private String resolveBatchId() {

		Component root = outwardMakerGridChequeDetails;

		Object value = null;

		if (root != null) {
			value = root.getAttribute("batchId");
		}

		String batchId = toSafeString(value);

		if (!batchId.isEmpty()) {
			return batchId;
		}

		value = outwardMakerGridChequeDetails == null ? null
				: outwardMakerGridChequeDetails.getPage().getAttribute("batchId");

		batchId = toSafeString(value);

		if (!batchId.isEmpty()) {
			return batchId;
		}

		if (org.zkoss.zk.ui.Executions.getCurrent() != null) {

			Object executionAttribute = org.zkoss.zk.ui.Executions.getCurrent().getAttribute("batchId");

			batchId = toSafeString(executionAttribute);

			if (!batchId.isEmpty()) {
				return batchId;
			}

			String parameter = org.zkoss.zk.ui.Executions.getCurrent().getParameter("batchId");

			batchId = toSafeString(parameter);

			if (!batchId.isEmpty()) {
				return batchId;
			}
		}

		if (org.zkoss.zk.ui.Sessions.getCurrent() != null) {

			Object sessionValue = org.zkoss.zk.ui.Sessions.getCurrent().getAttribute("OUTWARD_MAKER_SELECTED_BATCH_ID");

			batchId = toSafeString(sessionValue);

			if (!batchId.isEmpty()) {
				return batchId;
			}

			sessionValue = org.zkoss.zk.ui.Sessions.getCurrent().getAttribute("ACTIVE_OUTWARD_BATCH_ID");

			batchId = toSafeString(sessionValue);

			if (!batchId.isEmpty()) {
				return batchId;
			}

			sessionValue = org.zkoss.zk.ui.Sessions.getCurrent().getAttribute("batchId");

			batchId = toSafeString(sessionValue);

			if (!batchId.isEmpty()) {
				return batchId;
			}
		}

		return "";
	}

	private boolean loadReturnedBatch(String batchId) {

		returnedBatch = false;
		outwardBatch = null;
		outwardChequeList = new ArrayList<>();

		if (outwardBatchService == null || outwardChequeService == null) {
			return false;
		}

		OutwardBatch batch = outwardBatchService.getBatchById(batchId);

		if (batch == null) {
			return false;
		}

		String batchStatus = normalizeStatus(batch.getBatchStatus());

		if (!STATUS_ON_HOLD.equals(batchStatus)) {
			return false;
		}

		returnedBatch = true;
		outwardBatch = batch;

		List<OutwardCheque> loadedCheques = outwardChequeService.getOnHoldCheques(batchId);

		if (loadedCheques != null) {
			outwardChequeList = loadedCheques;
		}

		displayChequeList = new ArrayList<>();

		for (OutwardCheque cheque : outwardChequeList) {

			if (cheque == null) {
				continue;
			}

			displayChequeList.add(ChequeDisplayItem.fromOutwardCheque(cheque));
		}

		populateReturnedBatchSummary();

		currentPage = 1;

		renderCurrentPage();

		return true;
	}

	private void loadScanBatch(String batchId) {

		returnedBatch = false;
		scanBatch = null;

		scanChequeList = new ArrayList<>();
		displayChequeList = new ArrayList<>();

		ScanBatch loadedBatch = scanService.getBatchById(batchId);

		if (loadedBatch == null) {

			clearData();
			showEmptyState();

			return;
		}

		scanBatch = loadedBatch;

		List<ScanCheque> loadedCheques = scanService.getChequesByBatchId(batchId);

		if (loadedCheques != null) {
			scanChequeList = loadedCheques;
		}

		for (ScanCheque cheque : scanChequeList) {

			if (cheque == null) {
				continue;
			}

			displayChequeList.add(ChequeDisplayItem.fromScanCheque(cheque));
		}

		populateBatchSummary();

		currentPage = 1;

		renderCurrentPage();
	}

	private void populateBatchSummary() {

		if (scanBatch == null) {
			return;
		}

		setLabelValue(outwardMakerLblBatchId, safeDisplayValue(scanBatch.getScannedBatchId()));

		int count = Math.max(0, scanBatch.getActualChequeCount());

		setLabelValue(outwardMakerLblChequeCount, Integer.toString(count));

		setLabelValue(outwardMakerLblTotalAmount, formatIndianAmount(scanBatch.getActualTotalAmount()));
	}

	private void populateReturnedBatchSummary() {

		if (outwardBatch == null) {
			return;
		}

		setLabelValue(outwardMakerLblBatchId, safeDisplayValue(outwardBatch.getOutwardBatchId()));

		int count = displayChequeList == null ? 0 : displayChequeList.size();

		setLabelValue(outwardMakerLblChequeCount, Integer.toString(count));

		BigDecimal totalAmount = BigDecimal.ZERO;

		if (outwardChequeList != null) {

			for (OutwardCheque cheque : outwardChequeList) {

				if (cheque == null) {
					continue;
				}

				BigDecimal amount = cheque.getChequeAmount();

				if (amount != null) {
					totalAmount = totalAmount.add(amount);
				}
			}
		}

		setLabelValue(outwardMakerLblTotalAmount, formatIndianAmount(totalAmount));
	}

	private void renderCurrentPage() {

		if (outwardMakerRowsChequeDetails == null) {
			return;
		}

		outwardMakerRowsChequeDetails.getChildren().clear();

		if (displayChequeList == null || displayChequeList.isEmpty()) {

			showEmptyState();

			updatePagination();

			return;
		}

		if (outwardMakerGridChequeDetails != null) {
			outwardMakerGridChequeDetails.setVisible(true);
		}

		if (outwardMakerVlayoutEmptyState != null) {
			outwardMakerVlayoutEmptyState.setVisible(false);
		}

		int totalPages = getTotalPages();

		currentPage = normalizePage(currentPage, totalPages);

		int startIndex = (currentPage - 1) * PAGE_SIZE;

		int endIndex = Math.min(startIndex + PAGE_SIZE, displayChequeList.size());

		for (int index = startIndex; index < endIndex; index++) {

			ChequeDisplayItem item = displayChequeList.get(index);

			if (item == null) {
				continue;
			}

			if (item.scanCheque != null) {

				createScanChequeRow(item.scanCheque);

			} else if (item.outwardCheque != null) {

				createReturnedChequeRow(item.outwardCheque);
			}
		}

		updatePagination();
	}

	private void createScanChequeRow(ScanCheque cheque) {

		if (cheque == null || outwardMakerRowsChequeDetails == null) {
			return;
		}

		Row row = new Row();

		Label chequeNumberLabel = createLabel(safeDisplayValue(cheque.getChequeNumber()),
				"outward-maker-cheque-number");

		Label accountLabel = createLabel(safeDisplayValue(cheque.getDraweeAccountNumber()),
				"outward-maker-drawee-account");

		Label dateLabel = createLabel(formatIndianDate(cheque.getChequeDate()), "outward-maker-cheque-date");

		Label micrLabel = createLabel(safeDisplayValue(cheque.getMicrCode()), "outward-maker-micr-code");

		String status = normalizeStatus(cheque.getChequeStatus());

		Label statusLabel = createLabel(getDisplayStatus(status),
				"outward-maker-cheque-status " + getStatusClass(status));

		Label amountLabel = createLabel(formatIndianAmount(cheque.getChequeAmount()), "outward-maker-cheque-amount");

		Component actionComponent = createScanActionComponent(cheque);

		row.appendChild(chequeNumberLabel);
		row.appendChild(accountLabel);
		row.appendChild(dateLabel);
		row.appendChild(micrLabel);
		row.appendChild(statusLabel);
		row.appendChild(amountLabel);

		if (actionComponent != null) {
			row.appendChild(actionComponent);
		}

		outwardMakerRowsChequeDetails.appendChild(row);
	}

	private void createReturnedChequeRow(OutwardCheque cheque) {

		if (cheque == null || outwardMakerRowsChequeDetails == null) {
			return;
		}

		Row row = new Row();

		Label chequeNumberLabel = createLabel(safeDisplayValue(cheque.getChequeNumber()),
				"outward-maker-cheque-number");

		Label accountLabel = createLabel(safeDisplayValue(cheque.getDraweeAccountNumber()),
				"outward-maker-drawee-account");

		Label dateLabel = createLabel(formatIndianDate(cheque.getChequeDate()), "outward-maker-cheque-date");

		Label micrLabel = createLabel(safeDisplayValue(cheque.getMicrCode()), "outward-maker-micr-code");

		String status = normalizeStatus(cheque.getChequeStatus());

		Label statusLabel = createLabel(getDisplayStatus(status),
				"outward-maker-cheque-status " + getStatusClass(status));

		Label amountLabel = createLabel(formatIndianAmount(cheque.getChequeAmount()), "outward-maker-cheque-amount");

		Component actionComponent = createReturnedActionComponent(cheque);

		row.appendChild(chequeNumberLabel);
		row.appendChild(accountLabel);
		row.appendChild(dateLabel);
		row.appendChild(micrLabel);
		row.appendChild(statusLabel);
		row.appendChild(amountLabel);

		if (actionComponent != null) {
			row.appendChild(actionComponent);
		}

		outwardMakerRowsChequeDetails.appendChild(row);
	}

	private Label createLabel(String value, String sclass) {

		Label label = new Label(safeDisplayValue(value));

		label.setSclass(sclass);

		return label;
	}

	private Component createScanActionComponent(ScanCheque cheque) {

		if (cheque == null) {
			return createEmptyAction();
		}

		String status = normalizeStatus(cheque.getChequeStatus());

		if (isMicrRepairStatus(status)) {

			Button button = createActionButton("Modify");

			button.addEventListener("onClick", event -> openMicrRepair(cheque));

			return button;
		}

		if (isDataEntryStatus(status)) {

			Button button = createActionButton("Modify");

			button.addEventListener("onClick", event -> openDataEntry(cheque));

			return button;
		}

		return createEmptyAction();
	}

	private Component createReturnedActionComponent(OutwardCheque cheque) {

		if (cheque == null) {
			return createEmptyAction();
		}

		String status = normalizeStatus(cheque.getChequeStatus());

		if (isMicrRepairStatus(status)) {

			Button button = createActionButton("Modify");

			button.addEventListener("onClick", event -> openReturnedMicrRepair(cheque));

			return button;
		}

		if (isDataEntryStatus(status)) {

			Button button = createActionButton("Modify");

			button.addEventListener("onClick", event -> openReturnedDataEntry(cheque));

			return button;
		}

		return createEmptyAction();
	}

	private Label createEmptyAction() {

		return createLabel("-", "outward-maker-action-empty");
	}

	private Button createActionButton(String label) {

		Button button = new Button(label);

		button.setIconSclass("z-icon-edit");

		button.setSclass("outward-maker-action-button");

		return button;
	}

	private void openDataEntry(ScanCheque cheque) {

		if (cheque == null) {
			return;
		}

		String chequeId = toSafeString(cheque.getScannedChequeId());

		String batchId = toSafeString(cheque.getScannedBatchId());

		if (chequeId.isEmpty() || batchId.isEmpty()) {
			return;
		}

		Include mainContentArea = findMainContentArea();

		if (mainContentArea == null) {
			return;
		}

		setCommonNavigationProperties(mainContentArea, batchId, chequeId, false);

		mainContentArea.setSrc("/outward/maker/cheque-data-entry.zul");
	}

	private void openMicrRepair(ScanCheque cheque) {

		if (cheque == null) {
			return;
		}

		String chequeId = toSafeString(cheque.getScannedChequeId());

		String batchId = toSafeString(cheque.getScannedBatchId());

		if (chequeId.isEmpty() || batchId.isEmpty()) {
			return;
		}

		Include mainContentArea = findMainContentArea();

		if (mainContentArea == null) {
			return;
		}

		setCommonNavigationProperties(mainContentArea, batchId, chequeId, false);

		mainContentArea.setSrc("/outward/maker/micr-repair/micr-repair-view.zul");
	}

	private void openReturnedDataEntry(OutwardCheque cheque) {

		if (cheque == null) {
			return;
		}

		String chequeId = toSafeString(cheque.getOutwardChequeId());

		String batchId = toSafeString(cheque.getOutwardBatchId());

		if (chequeId.isEmpty() || batchId.isEmpty()) {
			return;
		}

		Include mainContentArea = findMainContentArea();

		if (mainContentArea == null) {
			return;
		}

		setCommonNavigationProperties(mainContentArea, batchId, chequeId, true);

		mainContentArea.setDynamicProperty("outwardChequeId", chequeId);

		mainContentArea.setAttribute("outwardChequeId", chequeId);

		mainContentArea.setSrc("/outward/maker/cheque-data-entry.zul");
	}

	private void openReturnedMicrRepair(OutwardCheque cheque) {

		if (cheque == null) {
			return;
		}

		String chequeId = toSafeString(cheque.getOutwardChequeId());

		String batchId = toSafeString(cheque.getOutwardBatchId());

		if (chequeId.isEmpty() || batchId.isEmpty()) {
			return;
		}

		Include mainContentArea = findMainContentArea();

		if (mainContentArea == null) {
			return;
		}

		setCommonNavigationProperties(mainContentArea, batchId, chequeId, true);

		mainContentArea.setDynamicProperty("outwardChequeId", chequeId);

		mainContentArea.setAttribute("outwardChequeId", chequeId);

		mainContentArea.setSrc("/outward/maker/micr-repair/micr-repair-view.zul");
	}

	private void setCommonNavigationProperties(Include mainContentArea, String batchId, String chequeId,
			boolean returnFromChecker) {

		mainContentArea.clearDynamicProperties();

		mainContentArea.setDynamicProperty("batchId", batchId);

		mainContentArea.setDynamicProperty("chequeId", chequeId);

		mainContentArea.setDynamicProperty("mode", MODE_DATA_ENTRY);

		mainContentArea.setDynamicProperty(RETURN_FROM_CHECKER, returnFromChecker);

		mainContentArea.setAttribute("batchId", batchId);

		mainContentArea.setAttribute("chequeId", chequeId);

		mainContentArea.setAttribute("mode", MODE_DATA_ENTRY);

		mainContentArea.setAttribute(RETURN_FROM_CHECKER, returnFromChecker);
	}

	private Include findMainContentArea() {

		Component current = outwardMakerRowsChequeDetails;

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

	private void goBackToDashboard() {

		Include mainContentArea = findMainContentArea();

		if (mainContentArea == null) {
			return;
		}

		mainContentArea.clearDynamicProperties();

		mainContentArea.setSrc("/outward/maker/dashboard.zul");
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

	private int getTotalPages() {

		if (displayChequeList == null || displayChequeList.isEmpty()) {
			return 1;
		}

		return (int) Math.ceil((double) displayChequeList.size() / PAGE_SIZE);
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

	private void showEmptyState() {

		if (outwardMakerGridChequeDetails != null) {
			outwardMakerGridChequeDetails.setVisible(false);
		}

		if (outwardMakerVlayoutEmptyState != null) {
			outwardMakerVlayoutEmptyState.setVisible(true);
		}

		setLabelValue(outwardMakerLblBatchId, "-");

		setLabelValue(outwardMakerLblChequeCount, "0");

		setLabelValue(outwardMakerLblTotalAmount, "₹0.00");
	}

	private void clearData() {

		scanBatch = null;
		outwardBatch = null;
		returnedBatch = false;

		scanChequeList = new ArrayList<>();
		outwardChequeList = new ArrayList<>();
		displayChequeList = new ArrayList<>();

		currentPage = 1;
	}

	private void setLabelValue(Label label, String value) {

		if (label != null) {
			label.setValue(safeDisplayValue(value));
		}
	}

	private String getDisplayStatus(String status) {

		String normalized = normalizeStatus(status);

		if (STATUS_PENDING_MAKER_PROCESS.equals(normalized)) {
			return "Pending Maker Process";
		}

		if (STATUS_PENDING_DATA_ENTRY.equals(normalized)) {
			return "Pending Data Entry";
		}

		if (STATUS_PENDING_MICR_REPAIR.equals(normalized)) {
			return "Pending MICR Repair";
		}

		if (STATUS_MICR_REJECTED.equals(normalized)) {
			return "MICR Rejected";
		}

		if (STATUS_PENDING_CHECKER_PROCESS.equals(normalized)) {
			return "Pending Checker Process";
		}

		if (STATUS_PENDING_VERIFICATION.equals(normalized)) {
			return "Pending Verification";
		}

		if (STATUS_ON_HOLD.equals(normalized)) {
			return "On Hold";
		}

		if (STATUS_REJECTION_REQUEST.equals(normalized)) {
			return "Rejection Request";
		}

		return safeDisplayValue(status);
	}

	private String getStatusClass(String status) {

		String normalized = normalizeStatus(status);

		if (STATUS_PENDING_MAKER_PROCESS.equals(normalized)) {
			return "pending-maker-process";
		}

		if (STATUS_PENDING_DATA_ENTRY.equals(normalized)) {
			return "pending-data-entry";
		}

		if (STATUS_PENDING_MICR_REPAIR.equals(normalized)) {
			return "pending-micr-repair";
		}

		if (STATUS_MICR_REJECTED.equals(normalized)) {
			return "micr-rejected";
		}

		if (STATUS_PENDING_CHECKER_PROCESS.equals(normalized)) {
			return "pending-checker-process";
		}

		if (STATUS_PENDING_VERIFICATION.equals(normalized)) {
			return "pending-verification";
		}

		if (STATUS_ON_HOLD.equals(normalized)) {
			return "on-hold";
		}

		if (STATUS_REJECTION_REQUEST.equals(normalized)) {
			return "rejection-request";
		}

		return "pending-maker-process";
	}

	private boolean isDataEntryStatus(String status) {

		return STATUS_PENDING_DATA_ENTRY.equals(normalizeStatus(status));
	}

	private boolean isMicrRepairStatus(String status) {

		return STATUS_PENDING_MICR_REPAIR.equals(normalizeStatus(status));
	}

	private String normalizeStatus(String status) {

		if (status == null || status.trim().isEmpty()) {
			return "";
		}

		return status.trim().toUpperCase(Locale.ENGLISH).replace('-', '_').replace(' ', '_');
	}

	private String formatIndianAmount(BigDecimal amount) {

		BigDecimal safeAmount = amount == null ? BigDecimal.ZERO : amount;

		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH);

		symbols.setGroupingSeparator(',');

		DecimalFormat formatter = new DecimalFormat("##,##,##0.00", symbols);

		return "₹" + formatter.format(safeAmount);
	}

	private String formatIndianDate(Object date) {

		if (date == null) {
			return "-";
		}

		try {

			if (date instanceof java.util.Date) {

				return new SimpleDateFormat("dd-MM-yyyy").format((java.util.Date) date);
			}

			String value = toSafeString(date);

			if (value.isEmpty()) {
				return "-";
			}

			if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {

				java.util.Date parsed = new SimpleDateFormat("yyyy-MM-dd").parse(value);

				return new SimpleDateFormat("dd-MM-yyyy").format(parsed);
			}

			return value;

		} catch (Exception exception) {

			return safeDisplayValue(date);
		}
	}

	private String safeDisplayValue(Object value) {

		String result = toSafeString(value);

		return result.isEmpty() ? "-" : result;
	}

	private String toSafeString(Object value) {

		if (value == null) {
			return "";
		}

		if (value instanceof String) {
			return ((String) value).trim();
		}

		return value.toString().trim();
	}

	private int normalizePage(int page, int totalPages) {

		int safeTotal = Math.max(1, totalPages);

		if (page < 1) {
			return 1;
		}

		if (page > safeTotal) {
			return safeTotal;
		}

		return page;
	}

	private static class ChequeDisplayItem {

		private ScanCheque scanCheque;
		private OutwardCheque outwardCheque;

		private static ChequeDisplayItem fromScanCheque(ScanCheque cheque) {

			ChequeDisplayItem item = new ChequeDisplayItem();

			item.scanCheque = cheque;

			return item;
		}

		private static ChequeDisplayItem fromOutwardCheque(OutwardCheque cheque) {

			ChequeDisplayItem item = new ChequeDisplayItem();

			item.outwardCheque = cheque;

			return item;
		}
	}
}
