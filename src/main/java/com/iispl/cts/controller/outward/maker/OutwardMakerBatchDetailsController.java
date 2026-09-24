package com.iispl.cts.controller.outward.maker;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;

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
import com.iispl.cts.validator.OutwardMakerBatchDetailsValidator;
import com.iispl.cts.validatorimpl.OutwardMakerBatchDetailsValidatorImpl;

public class OutwardMakerBatchDetailsController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;
	private static final int PAGE_SIZE = 5;

	private static final String STATUS_PENDING_MAKER_PROCESS = "PENDING_MAKER_PROCESS";
	private static final String STATUS_MICR_REJECTED = "MICR_REJECTED";
	private static final String STATUS_PENDING_DATA_ENTRY = "PENDING_DATA_ENTRY";
	private static final String STATUS_PENDING_MICR_REPAIR = "PENDING_MICR_REPAIR";
	private static final String STATUS_PENDING_CHECKER_PROCESS = "PENDING_CHECKER_PROCESS";
	private static final String STATUS_PENDING_VERIFICATION = "PENDING_VERIFICATION";
	private static final String STATUS_MAKER_RETURNED = "MAKER_RETURNED";
	private static final String STATUS_ON_HOLD = "ON_HOLD";
	private static final String STATUS_REJECT_REQUEST = "REJECT_REQUEST";

	private static final String MODE_DATA_ENTRY = "DATA_ENTRY";
	private static final String RETURN_FROM_CHECKER = "RETURN_FROM_CHECKER";

	private Div outwardMakerDivRowsContainer;
	private Component outwardMakerVlayoutEmptyState;

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
	private OutwardMakerBatchDetailsValidator validator;

	private ScanBatch scanBatch;
	private OutwardBatch outwardBatch;

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
		validator = new OutwardMakerBatchDetailsValidatorImpl();

		outwardMakerDivRowsContainer = getFellow(component, "outwardMakerDivRowsContainer", Div.class);
		outwardMakerVlayoutEmptyState = component.getFellowIfAny("outwardMakerVlayoutEmptyState");

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
		if (component == null || id == null || type == null)
			return null;
		Component fellow = component.getFellowIfAny(id);
		if (fellow == null)
			return null;
		if (!type.isInstance(fellow)) {
			throw new IllegalStateException("Component '" + id + "' is not of type " + type.getName());
		}
		return type.cast(fellow);
	}

	private void bindEvents() {
		if (outwardMakerBtnFirst != null)
			outwardMakerBtnFirst.addEventListener("onClick", event -> goToFirstPage());
		if (outwardMakerBtnPrevious != null)
			outwardMakerBtnPrevious.addEventListener("onClick", event -> goToPreviousPage());
		if (outwardMakerBtnNext != null)
			outwardMakerBtnNext.addEventListener("onClick", event -> goToNextPage());
		if (outwardMakerBtnLast != null)
			outwardMakerBtnLast.addEventListener("onClick", event -> goToLastPage());
		if (outwardMakerBtnBack != null)
			outwardMakerBtnBack.addEventListener("onClick", event -> goBackToDashboard());
	}

	private void loadBatchDetails() {
		currentBatchId = resolveBatchId();

		if (!validator.isValidBatchId(currentBatchId)) {
			showEmptyState();
			return;
		}

		try {
			if (loadReturnedBatch(currentBatchId)) {
				return;
			}
			loadBatchWithLiveCheques(currentBatchId);
		} catch (Exception exception) {
			exception.printStackTrace();
			clearData();
			showEmptyState();
		}
	}

	private String resolveBatchId() {
		Component root = outwardMakerDivRowsContainer;
		Object value = null;

		if (root != null) {
			value = root.getAttribute("batchId");
			if (isEmpty(value))
				value = root.getAttribute("selectedBatchId");
		}
		String batchId = toSafeString(value);
		if (!batchId.isEmpty())
			return validator.sanitizeBatchId(batchId);

		if (root != null && root.getPage() != null) {
			value = root.getPage().getAttribute("batchId");
			if (isEmpty(value))
				value = root.getPage().getAttribute("selectedBatchId");
		}
		batchId = toSafeString(value);
		if (!batchId.isEmpty())
			return validator.sanitizeBatchId(batchId);

		if (Executions.getCurrent() != null) {
			value = Executions.getCurrent().getAttribute("batchId");
			if (isEmpty(value))
				value = Executions.getCurrent().getAttribute("selectedBatchId");
			batchId = toSafeString(value);
			if (!batchId.isEmpty())
				return validator.sanitizeBatchId(batchId);

			String parameter = Executions.getCurrent().getParameter("batchId");
			if (parameter == null || parameter.trim().isEmpty()) {
				parameter = Executions.getCurrent().getParameter("selectedBatchId");
			}
			batchId = toSafeString(parameter);
			if (!batchId.isEmpty())
				return validator.sanitizeBatchId(batchId);
		}

		if (Sessions.getCurrent() != null) {
			value = Sessions.getCurrent().getAttribute("OUTWARD_MAKER_SELECTED_BATCH_ID");
			if (isEmpty(value))
				value = Sessions.getCurrent().getAttribute("ACTIVE_OUTWARD_BATCH_ID");
			if (isEmpty(value))
				value = Sessions.getCurrent().getAttribute("batchId");
			if (isEmpty(value))
				value = Sessions.getCurrent().getAttribute("selectedBatchId");

			batchId = toSafeString(value);
			if (!batchId.isEmpty())
				return validator.sanitizeBatchId(batchId);
		}

		return "";
	}

	private boolean isEmpty(Object obj) {
		return obj == null || obj.toString().trim().isEmpty();
	}

	private boolean loadReturnedBatch(String batchId) {
		returnedBatch = false;
		outwardBatch = null;

		if (outwardBatchService == null || outwardChequeService == null) {
			return false;
		}

		OutwardBatch batch = outwardBatchService.getBatchById(batchId);
		if (batch == null)
			return false;

		String batchStatus = normalizeStatus(batch.getBatchStatus());
		if (!STATUS_ON_HOLD.equals(batchStatus)) {
			return false;
		}

		returnedBatch = true;
		outwardBatch = batch;

		List<OutwardCheque> loadedCheques = outwardChequeService.getOnHoldCheques(batchId);
		displayChequeList = new ArrayList<>();

		if (loadedCheques != null) {
			for (OutwardCheque cheque : loadedCheques) {
				if (cheque == null)
					continue;
				displayChequeList.add(ChequeDisplayItem.fromOutwardCheque(cheque));
			}
		}

		populateReturnedBatchSummary();
		currentPage = 1;
		renderCurrentPage();
		return true;
	}

	private void loadBatchWithLiveCheques(String batchId) {
		returnedBatch = false;
		scanBatch = null;
		outwardBatch = null;
		displayChequeList = new ArrayList<>();
		Set<String> seenChequeNumbers = new HashSet<>();

		if (outwardBatchService != null) {
			try {
				outwardBatch = outwardBatchService.getBatchById(batchId);
			} catch (Exception ignored) {
			}
		}
		if (scanService != null) {
			try {
				scanBatch = scanService.getBatchById(batchId);
			} catch (Exception ignored) {
			}
		}

		if (outwardChequeService != null) {
			try {
				List<OutwardCheque> outwardCheques = outwardChequeService.getChequesByBatchId(batchId);
				if (outwardCheques != null) {
					for (OutwardCheque cheque : outwardCheques) {
						if (cheque == null)
							continue;
						displayChequeList.add(ChequeDisplayItem.fromOutwardCheque(cheque));
						String chqNo = toSafeString(cheque.getChequeNumber());
						if (!chqNo.isEmpty())
							seenChequeNumbers.add(chqNo);
					}
				}
			} catch (Exception ignored) {
			}
		}

		if (scanService != null) {
			try {
				List<ScanCheque> scanCheques = scanService.getChequesByBatchId(batchId);
				if (scanCheques != null) {
					for (ScanCheque cheque : scanCheques) {
						if (cheque == null)
							continue;
						String chqNo = toSafeString(cheque.getChequeNumber());
						if (!chqNo.isEmpty() && seenChequeNumbers.contains(chqNo))
							continue;
						displayChequeList.add(ChequeDisplayItem.fromScanCheque(cheque));
					}
				}
			} catch (Exception ignored) {
			}
		}

		if (displayChequeList.isEmpty() && outwardChequeService != null) {
			try {
				List<OutwardCheque> onHoldCheques = outwardChequeService.getOnHoldCheques(batchId);
				if (onHoldCheques != null) {
					for (OutwardCheque cheque : onHoldCheques) {
						if (cheque == null)
							continue;
						displayChequeList.add(ChequeDisplayItem.fromOutwardCheque(cheque));
					}
				}
			} catch (Exception ignored) {
			}
		}

		populateLiveBatchSummary(batchId);
		currentPage = 1;
		renderCurrentPage();
	}

	private void populateLiveBatchSummary(String batchId) {
		setLabelValue(outwardMakerLblBatchId, safeDisplayValue(batchId));

		int count = 0;
		BigDecimal totalAmount = BigDecimal.ZERO;

		if (scanBatch != null && scanBatch.getActualChequeCount() > 0) {
			count = scanBatch.getActualChequeCount();
			totalAmount = scanBatch.getActualTotalAmount() != null ? scanBatch.getActualTotalAmount() : BigDecimal.ZERO;
		} else if (outwardBatch != null && outwardBatch.getActualChequeCount() > 0) {
			count = outwardBatch.getActualChequeCount();
			totalAmount = outwardBatch.getActualTotalAmount() != null ? outwardBatch.getActualTotalAmount()
					: BigDecimal.ZERO;
		} else {
			count = displayChequeList.size();
			for (ChequeDisplayItem item : displayChequeList) {
				if (item.chequeAmount != null) {
					totalAmount = totalAmount.add(item.chequeAmount);
				}
			}
		}

		setLabelValue(outwardMakerLblChequeCount, Integer.toString(Math.max(0, count)));
		setLabelValue(outwardMakerLblTotalAmount, formatIndianAmount(totalAmount));
	}

	private void populateReturnedBatchSummary() {
		if (outwardBatch == null)
			return;
		setLabelValue(outwardMakerLblBatchId, safeDisplayValue(outwardBatch.getOutwardBatchId()));
		int count = displayChequeList.size();
		setLabelValue(outwardMakerLblChequeCount, Integer.toString(count));

		BigDecimal totalAmount = BigDecimal.ZERO;
		for (ChequeDisplayItem item : displayChequeList) {
			if (item.chequeAmount != null) {
				totalAmount = totalAmount.add(item.chequeAmount);
			}
		}
		setLabelValue(outwardMakerLblTotalAmount, formatIndianAmount(totalAmount));
	}

	private void renderCurrentPage() {
		if (outwardMakerDivRowsContainer == null)
			return;
		outwardMakerDivRowsContainer.getChildren().clear();

		boolean hasData = displayChequeList != null && !displayChequeList.isEmpty();

		if (!hasData) {
			if (outwardMakerVlayoutEmptyState != null)
				outwardMakerVlayoutEmptyState.setVisible(true);
			if (outwardMakerDivRowsContainer != null)
				outwardMakerDivRowsContainer.setVisible(false);
			updatePagination();
			return;
		}

		if (outwardMakerDivRowsContainer != null)
			outwardMakerDivRowsContainer.setVisible(true);
		if (outwardMakerVlayoutEmptyState != null)
			outwardMakerVlayoutEmptyState.setVisible(false);

		int totalPages = getTotalPages();
		currentPage = normalizePage(currentPage, totalPages);

		int startIndex = (currentPage - 1) * PAGE_SIZE;
		int endIndex = Math.min(startIndex + PAGE_SIZE, displayChequeList.size());

		for (int index = startIndex; index < endIndex; index++) {
			ChequeDisplayItem item = displayChequeList.get(index);
			if (item != null) {
				createUnifiedChequeDivRow(item);
			}
		}

		updatePagination();
	}

	private void createUnifiedChequeDivRow(ChequeDisplayItem item) {
		Div row = new Div();
		row.setSclass("div-table-row");

		// 1. Cheque No.
		Div tdChqNo = new Div();
		tdChqNo.setSclass("div-td col-chq-no");
		tdChqNo.appendChild(createLabel(safeDisplayValue(item.chequeNumber), "outward-maker-batch-cheque-number"));

		// 2. Payee Account No. (Prioritized)
		Div tdAccNo = new Div();
		tdAccNo.setSclass("div-td col-acc-no");
		tdAccNo.appendChild(createLabel(safeDisplayValue(item.payeeAccountNumber), "outward-maker-batch-account-no"));

		// 3. Date
		Div tdDate = new Div();
		tdDate.setSclass("div-td col-date");
		tdDate.appendChild(createLabel(formatIndianDate(item.chequeDate), "outward-maker-batch-cheque-date"));

		// 4. MICR
		Div tdMicr = new Div();
		tdMicr.setSclass("div-td col-micr");
		tdMicr.appendChild(createLabel(safeDisplayValue(item.micrCode), "outward-maker-batch-micr-code"));

		// 5. Status
		String status = normalizeStatus(item.chequeStatus);
		Div tdStatus = new Div();
		tdStatus.setSclass("div-td col-status");
		tdStatus.appendChild(createLabel(getDisplayStatus(status), "outward-maker-status " + getStatusClass(status)));

		// 6. Amount
		Div tdAmount = new Div();
		tdAmount.setSclass("div-td col-amount");
		tdAmount.appendChild(createLabel(formatIndianAmount(item.chequeAmount), "outward-maker-batch-cheque-amount"));

		// 7. Action
		Div tdAction = new Div();
		tdAction.setSclass("div-td col-action");
		tdAction.appendChild(createChequeActionComponent(item, status));

		row.appendChild(tdChqNo);
		row.appendChild(tdAccNo);
		row.appendChild(tdDate);
		row.appendChild(tdMicr);
		row.appendChild(tdStatus);
		row.appendChild(tdAmount);
		row.appendChild(tdAction);

		outwardMakerDivRowsContainer.appendChild(row);
	}

	private Label createLabel(String value, String sclass) {
		Label label = new Label(safeDisplayValue(value));
		label.setSclass(sclass);
		return label;
	}

	private Component createChequeActionComponent(ChequeDisplayItem item, String status) {
		if (item == null)
			return createEmptyAction();

		if (validator.canModifyCheque(status) || STATUS_PENDING_VERIFICATION.equals(status)
				|| STATUS_MAKER_RETURNED.equals(status)) {
			Button button = new Button("Modify");
			button.setIconSclass("z-icon-edit");
			button.setSclass("outward-maker-btn-action-modify");
			button.addEventListener("onClick", event -> openChequeTargetAction(item, status));
			return button;
		}

		return createEmptyAction();
	}

	private Label createEmptyAction() {
		return createLabel("-", "outward-maker-action-empty");
	}

	private void openChequeTargetAction(ChequeDisplayItem item, String status) {
		if (item == null)
			return;

		String chequeId = toSafeString(item.chequeId);
		String batchId = toSafeString(currentBatchId);

		if (chequeId.isEmpty() || batchId.isEmpty())
			return;

		Include mainContentArea = findMainContentArea();
		if (mainContentArea == null)
			return;

		mainContentArea.clearDynamicProperties();
		mainContentArea.setDynamicProperty("batchId", batchId);
		mainContentArea.setDynamicProperty("chequeId", chequeId);
		mainContentArea.setDynamicProperty("outwardChequeId", chequeId);
		mainContentArea.setDynamicProperty("mode", MODE_DATA_ENTRY);
		mainContentArea.setDynamicProperty(RETURN_FROM_CHECKER, returnedBatch);

		mainContentArea.setAttribute("batchId", batchId);
		mainContentArea.setAttribute("chequeId", chequeId);
		mainContentArea.setAttribute("outwardChequeId", chequeId);
		mainContentArea.setAttribute("mode", MODE_DATA_ENTRY);
		mainContentArea.setAttribute(RETURN_FROM_CHECKER, returnedBatch);

		String targetZul = validator.determineTargetView(status);
		mainContentArea.setSrc(targetZul);
	}

	private Include findMainContentArea() {
		Component current = outwardMakerDivRowsContainer;
		while (current != null) {
			if (current instanceof Include && "mainContentArea".equals(current.getId())) {
				return (Include) current;
			}
			current = current.getParent();
		}
		return null;
	}

	private void goBackToDashboard() {
		Include mainContentArea = findMainContentArea();
		if (mainContentArea == null)
			return;

		mainContentArea.clearDynamicProperties();
		mainContentArea.setSrc("/outward/maker/dashboard.zul");
	}

	private void goToFirstPage() {
		if (currentPage <= 1)
			return;
		currentPage = 1;
		renderCurrentPage();
	}

	private void goToPreviousPage() {
		if (currentPage <= 1)
			return;
		currentPage--;
		renderCurrentPage();
	}

	private void goToNextPage() {
		if (currentPage >= getTotalPages())
			return;
		currentPage++;
		renderCurrentPage();
	}

	private void goToLastPage() {
		if (currentPage >= getTotalPages())
			return;
		currentPage = getTotalPages();
		renderCurrentPage();
	}

	private int getTotalPages() {
		if (displayChequeList == null || displayChequeList.isEmpty())
			return 1;
		return (int) Math.ceil((double) displayChequeList.size() / PAGE_SIZE);
	}

	private void updatePagination() {
		int totalPages = getTotalPages();
		if (outwardMakerLblCurrentPage != null) {
			outwardMakerLblCurrentPage.setValue(currentPage + " / " + totalPages);
		}
		if (outwardMakerBtnFirst != null)
			outwardMakerBtnFirst.setDisabled(currentPage <= 1);
		if (outwardMakerBtnPrevious != null)
			outwardMakerBtnPrevious.setDisabled(currentPage <= 1);
		if (outwardMakerBtnNext != null)
			outwardMakerBtnNext.setDisabled(currentPage >= totalPages);
		if (outwardMakerBtnLast != null)
			outwardMakerBtnLast.setDisabled(currentPage >= totalPages);
	}

	private void showEmptyState() {
		if (outwardMakerDivRowsContainer != null)
			outwardMakerDivRowsContainer.setVisible(false);
		if (outwardMakerVlayoutEmptyState != null)
			outwardMakerVlayoutEmptyState.setVisible(true);
		setLabelValue(outwardMakerLblBatchId, "-");
		setLabelValue(outwardMakerLblChequeCount, "0");
		setLabelValue(outwardMakerLblTotalAmount, "₹0.00");
	}

	private void clearData() {
		scanBatch = null;
		outwardBatch = null;
		returnedBatch = false;
		displayChequeList = new ArrayList<>();
		currentPage = 1;
	}

	private void setLabelValue(Label label, String value) {
		if (label != null)
			label.setValue(safeDisplayValue(value));
	}

	private String getDisplayStatus(String status) {
		String normalized = normalizeStatus(status);

		if (STATUS_PENDING_MAKER_PROCESS.equals(normalized))
			return "Pending Maker Process";
		if (STATUS_PENDING_DATA_ENTRY.equals(normalized))
			return "Pending Data Entry";
		if (STATUS_PENDING_MICR_REPAIR.equals(normalized))
			return "Pending Micr Repair";
		if (STATUS_MICR_REJECTED.equals(normalized))
			return "Micr Rejected";
		if (STATUS_PENDING_CHECKER_PROCESS.equals(normalized))
			return "Pending Checker Process";
		if (STATUS_PENDING_VERIFICATION.equals(normalized))
			return "Pending Verification";
		if (STATUS_MAKER_RETURNED.equals(normalized))
			return "Maker Returned";
		if (STATUS_ON_HOLD.equals(normalized))
			return "On Hold";
		if (STATUS_REJECT_REQUEST.equals(normalized))
			return "Reject Request";

		return toCamelCaseWords(status);
	}

	private String toCamelCaseWords(String text) {
		if (text == null || text.trim().isEmpty())
			return "-";
		String[] words = text.replace('_', ' ').replace('-', ' ').split("\\s+");
		StringBuilder sb = new StringBuilder();
		for (String word : words) {
			if (!word.isEmpty()) {
				sb.append(Character.toUpperCase(word.charAt(0)));
				if (word.length() > 1) {
					sb.append(word.substring(1).toLowerCase(Locale.ENGLISH));
				}
				sb.append(" ");
			}
		}
		return sb.toString().trim();
	}

	private String getStatusClass(String status) {
		String normalized = normalizeStatus(status);

		if (STATUS_PENDING_MAKER_PROCESS.equals(normalized))
			return "pending-maker-process";
		if (STATUS_PENDING_DATA_ENTRY.equals(normalized))
			return "pending-data-entry";
		if (STATUS_PENDING_MICR_REPAIR.equals(normalized))
			return "pending-micr-repair";
		if (STATUS_MICR_REJECTED.equals(normalized))
			return "micr-rejected";
		if (STATUS_PENDING_CHECKER_PROCESS.equals(normalized))
			return "pending-checker-process";
		if (STATUS_PENDING_VERIFICATION.equals(normalized))
			return "pending-verification";
		if (STATUS_MAKER_RETURNED.equals(normalized))
			return "maker-returned";
		if (STATUS_ON_HOLD.equals(normalized))
			return "on-hold";
		if (STATUS_REJECT_REQUEST.equals(normalized))
			return "reject-request";

		return "pending-maker-process";
	}

	private String normalizeStatus(String status) {
		if (status == null || status.trim().isEmpty())
			return "";
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
		if (date == null)
			return "-";
		try {
			if (date instanceof java.util.Date) {
				return new SimpleDateFormat("dd-MM-yyyy").format((java.util.Date) date);
			}
			String value = toSafeString(date);
			if (value.isEmpty())
				return "-";

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
		if (value == null)
			return "";
		if (value instanceof String)
			return ((String) value).trim();
		return value.toString().trim();
	}

	private int normalizePage(int page, int totalPages) {
		int safeTotal = Math.max(1, totalPages);
		if (page < 1)
			return 1;
		if (page > safeTotal)
			return safeTotal;
		return page;
	}

	private static class ChequeDisplayItem {
		private String chequeId;
		private String chequeNumber;
		private String payeeAccountNumber;
		private Object chequeDate;
		private String micrCode;
		private String chequeStatus;
		private BigDecimal chequeAmount;

		private static ChequeDisplayItem fromScanCheque(ScanCheque cheque) {
			ChequeDisplayItem item = new ChequeDisplayItem();
			item.chequeId = cheque.getScannedChequeId();
			item.chequeNumber = cheque.getChequeNumber();

			// Prioritizes payee account number with fallback to drawee account number
			String account = cheque.getPayeeAccountNumber();
			if (account == null || account.trim().isEmpty()) {
				account = cheque.getDraweeAccountNumber();
			}
			item.payeeAccountNumber = account;
			item.chequeDate = cheque.getChequeDate();
			item.micrCode = cheque.getMicrCode();
			item.chequeStatus = cheque.getChequeStatus();
			item.chequeAmount = cheque.getChequeAmount();
			return item;
		}

		private static ChequeDisplayItem fromOutwardCheque(OutwardCheque cheque) {
			ChequeDisplayItem item = new ChequeDisplayItem();
			item.chequeId = cheque.getOutwardChequeId();
			item.chequeNumber = cheque.getChequeNumber();

			// Prioritizes payee account number with fallback to drawee account number
			String account = cheque.getPayeeAccountNumber();
			if (account == null || account.trim().isEmpty()) {
				account = cheque.getDraweeAccountNumber();
			}
			item.payeeAccountNumber = account;
			item.chequeDate = cheque.getChequeDate();
			item.micrCode = cheque.getMicrCode();
			item.chequeStatus = cheque.getChequeStatus();
			item.chequeAmount = cheque.getChequeAmount();
			return item;
		}
	}
}