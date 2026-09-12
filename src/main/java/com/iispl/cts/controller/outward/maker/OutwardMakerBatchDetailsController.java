package com.iispl.cts.controller.outward.maker;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
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

public class OutwardMakerBatchDetailsController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 1L;

	private static final int PAGE_SIZE = 10;

	private static final String STATUS_PENDING_DATA_ENTRY = "PENDING_DATA_ENTRY";

	private static final String STATUS_PENDING_MICR_REPAIR = "PENDING_MICR_REPAIR";

	private static final String STATUS_PENDING_VERIFICATION = "PENDING_VERIFICATION";

	private static final String STATUS_PENDING_CHECKER_PROCESS = "PENDING_CHECKER_PROCESS";

	private static final String STATUS_ON_HOLD = "ON_HOLD";

	private static final String STATUS_REJECTION_REQUEST = "REJECTION_REQUEST";

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

	private List<Object> displayChequeList = new ArrayList<>();

	private boolean returnedBatch;

	private int currentPage = 1;

	@Override
	public void doAfterCompose(Component component) throws Exception {

		super.doAfterCompose(component);

		scanService = new ScanServiceImpl();

		outwardBatchService = new OutwardBatchServiceImpl();

		outwardChequeService = new OutwardChequeServiceImpl();

		outwardMakerGridChequeDetails = (Grid) component.getFellow("outwardMakerGridChequeDetails");

		outwardMakerRowsChequeDetails = (Rows) component.getFellow("outwardMakerRowsChequeDetails");

		outwardMakerVlayoutEmptyState = (Vlayout) component.getFellow("outwardMakerVlayoutEmptyState");

		outwardMakerLblBatchId = (Label) component.getFellow("outwardMakerLblBatchId");

		outwardMakerLblChequeCount = (Label) component.getFellow("outwardMakerLblChequeCount");

		outwardMakerLblTotalAmount = (Label) component.getFellow("outwardMakerLblTotalAmount");

		outwardMakerLblCurrentPage = (Label) component.getFellow("outwardMakerLblCurrentPage");

		outwardMakerBtnFirst = (Button) component.getFellow("outwardMakerBtnFirst");

		outwardMakerBtnPrevious = (Button) component.getFellow("outwardMakerBtnPrevious");

		outwardMakerBtnNext = (Button) component.getFellow("outwardMakerBtnNext");

		outwardMakerBtnLast = (Button) component.getFellow("outwardMakerBtnLast");

		outwardMakerBtnBack = (Button) component.getFellow("outwardMakerBtnBack");

		outwardMakerBtnFirst.addEventListener("onClick", event -> goToFirstPage());

		outwardMakerBtnPrevious.addEventListener("onClick", event -> goToPreviousPage());

		outwardMakerBtnNext.addEventListener("onClick", event -> goToNextPage());

		outwardMakerBtnLast.addEventListener("onClick", event -> goToLastPage());

		outwardMakerBtnBack.addEventListener("onClick", event -> goBackToDashboard());

		loadBatchDetails();
	}

	private void loadBatchDetails() {

		try {

			Object batchIdObject = Executions.getCurrent().getAttribute("batchId");

			if (batchIdObject == null) {

				batchIdObject = Executions.getCurrent().getSession().getAttribute("OUTWARD_MAKER_SELECTED_BATCH_ID");
			}

			if (batchIdObject == null) {

				showEmptyState();

				return;
			}

			String batchId = batchIdObject.toString().trim();

			if (batchId.isEmpty()) {

				showEmptyState();

				return;
			}

			loadReturnedBatch(batchId);

			if (returnedBatch) {

				return;
			}

			loadScanBatch(batchId);

		} catch (Exception exception) {

			exception.printStackTrace();

			throw new RuntimeException("Unable to load batch details", exception);
		}
	}

	private void loadReturnedBatch(String batchId) {

		returnedBatch = false;

		outwardBatch = outwardBatchService.getBatchById(batchId);

		if (outwardBatch == null) {

			return;
		}

		String batchStatus = normalizeStatus(outwardBatch.getBatchStatus());

		if (!STATUS_ON_HOLD.equals(batchStatus)) {

			return;
		}

		returnedBatch = true;

		outwardChequeList = outwardChequeService.getOnHoldCheques(batchId);

		if (outwardChequeList == null) {

			outwardChequeList = new ArrayList<>();
		}

		displayChequeList.clear();

		displayChequeList.addAll(outwardChequeList);

		populateReturnedBatchSummary();

		currentPage = 1;

		renderCurrentPage();
	}

	private void loadScanBatch(String batchId) {

		returnedBatch = false;

		scanBatch = scanService.getBatchById(batchId);

		if (scanBatch == null) {

			showEmptyState();

			return;
		}

		scanChequeList = scanService.getChequesByBatchId(batchId);

		if (scanChequeList == null) {

			scanChequeList = new ArrayList<>();
		}

		displayChequeList.clear();

		displayChequeList.addAll(scanChequeList);

		populateBatchSummary();

		currentPage = 1;

		renderCurrentPage();
	}

	private void populateBatchSummary() {

		outwardMakerLblBatchId.setValue(getValue(scanBatch.getScannedBatchId()));

		outwardMakerLblChequeCount.setValue(String.valueOf(scanBatch.getActualChequeCount()));

		outwardMakerLblTotalAmount.setValue(formatIndianAmount(scanBatch.getActualTotalAmount()));
	}

	private void populateReturnedBatchSummary() {

		outwardMakerLblBatchId.setValue(getValue(outwardBatch.getOutwardBatchId()));

		outwardMakerLblChequeCount.setValue(String.valueOf(outwardChequeList.size()));

		BigDecimal totalAmount = BigDecimal.ZERO;

		for (OutwardCheque cheque : outwardChequeList) {

			if (cheque != null && cheque.getChequeAmount() != null) {

				totalAmount = totalAmount.add(cheque.getChequeAmount());
			}
		}

		outwardMakerLblTotalAmount.setValue(formatIndianAmount(totalAmount));
	}

	private void renderCurrentPage() {

		outwardMakerRowsChequeDetails.getChildren().clear();

		if (displayChequeList == null || displayChequeList.isEmpty()) {

			showEmptyState();

			updatePagination();

			return;
		}

		outwardMakerGridChequeDetails.setVisible(true);

		outwardMakerVlayoutEmptyState.setVisible(false);

		int totalPages = getTotalPages();

		if (currentPage > totalPages) {

			currentPage = totalPages;
		}

		if (currentPage < 1) {

			currentPage = 1;
		}

		int startIndex = (currentPage - 1) * PAGE_SIZE;

		int endIndex = Math.min(startIndex + PAGE_SIZE, displayChequeList.size());

		for (int index = startIndex; index < endIndex; index++) {

			Object cheque = displayChequeList.get(index);

			if (cheque instanceof ScanCheque) {

				createScanChequeRow((ScanCheque) cheque);

			} else if (cheque instanceof OutwardCheque) {

				createReturnedChequeRow((OutwardCheque) cheque);
			}
		}

		updatePagination();
	}

	private void createScanChequeRow(ScanCheque cheque) {

		Row row = new Row();

		Label chequeNumberLabel = new Label(getValue(cheque.getChequeNumber()));

		chequeNumberLabel.setStyle("font-size:10px;" + "font-weight:800;" + "color:#1d4ed8;" + "text-align:center;"
				+ "white-space:nowrap;");

		Label payeeAccountLabel = new Label(getValue(cheque.getPayeeAccountNumber()));

		payeeAccountLabel.setStyle("font-size:10px;" + "font-weight:600;" + "color:#334155;" + "text-align:center;"
				+ "white-space:nowrap;");

		Label chequeDateLabel = new Label(formatIndianDate(cheque.getChequeDate()));

		chequeDateLabel.setStyle("font-size:10px;" + "font-weight:600;" + "color:#334155;" + "text-align:center;"
				+ "white-space:nowrap;");

		Label micrCodeLabel = new Label(getValue(cheque.getMicrCode()));

		micrCodeLabel.setStyle("font-size:10px;" + "font-weight:600;" + "color:#334155;" + "text-align:center;"
				+ "white-space:nowrap;");

		String actualStatus = normalizeStatus(cheque.getChequeStatus());

		Label chequeStatusLabel = new Label(getDisplayStatus(actualStatus));

		chequeStatusLabel.setStyle(getStatusStyle(actualStatus));

		Label chequeAmountLabel = new Label(formatIndianAmount(cheque.getChequeAmount()));

		chequeAmountLabel.setStyle("font-size:10px;" + "font-weight:700;" + "color:#0f172a;" + "text-align:center;"
				+ "white-space:nowrap;");

		Component actionComponent = createScanActionComponent(cheque);

		row.appendChild(chequeNumberLabel);

		row.appendChild(payeeAccountLabel);

		row.appendChild(chequeDateLabel);

		row.appendChild(micrCodeLabel);

		row.appendChild(chequeStatusLabel);

		row.appendChild(chequeAmountLabel);

		row.appendChild(actionComponent);

		outwardMakerRowsChequeDetails.appendChild(row);
	}

	private void createReturnedChequeRow(OutwardCheque cheque) {

		Row row = new Row();

		Label chequeNumberLabel = new Label(getValue(cheque.getChequeNumber()));

		chequeNumberLabel.setStyle("font-size:10px;" + "font-weight:800;" + "color:#1d4ed8;" + "text-align:center;"
				+ "white-space:nowrap;");

		Label payeeAccountLabel = new Label(getValue(cheque.getPayeeAccountNumber()));

		payeeAccountLabel.setStyle("font-size:10px;" + "font-weight:600;" + "color:#334155;" + "text-align:center;"
				+ "white-space:nowrap;");

		Label chequeDateLabel = new Label(formatIndianDate(cheque.getChequeDate()));

		chequeDateLabel.setStyle("font-size:10px;" + "font-weight:600;" + "color:#334155;" + "text-align:center;"
				+ "white-space:nowrap;");

		Label micrCodeLabel = new Label(getValue(cheque.getMicrCode()));

		micrCodeLabel.setStyle("font-size:10px;" + "font-weight:600;" + "color:#334155;" + "text-align:center;"
				+ "white-space:nowrap;");

		String actualStatus = normalizeStatus(cheque.getChequeStatus());

		Label chequeStatusLabel = new Label(getDisplayStatus(actualStatus));

		chequeStatusLabel.setStyle(getStatusStyle(actualStatus));

		Label chequeAmountLabel = new Label(formatIndianAmount(cheque.getChequeAmount()));

		chequeAmountLabel.setStyle("font-size:10px;" + "font-weight:700;" + "color:#0f172a;" + "text-align:center;"
				+ "white-space:nowrap;");

		Component actionComponent = createReturnedActionComponent(cheque);

		row.appendChild(chequeNumberLabel);

		row.appendChild(payeeAccountLabel);

		row.appendChild(chequeDateLabel);

		row.appendChild(micrCodeLabel);

		row.appendChild(chequeStatusLabel);

		row.appendChild(chequeAmountLabel);

		row.appendChild(actionComponent);

		outwardMakerRowsChequeDetails.appendChild(row);
	}

	private Component createScanActionComponent(ScanCheque cheque) {

		String status = normalizeStatus(cheque.getChequeStatus());

		if (isDataEntryStatus(status)) {

			Button button = createActionButton("DATA ENTRY REQUIRED");

			button.addEventListener("onClick", event -> openDataEntry(cheque));

			return button;
		}

		if (isMicrRepairStatus(status)) {

			Button button = createActionButton("MICR REPAIR REQUIRED");

			button.addEventListener("onClick", event -> openMicrRepair(cheque));

			return button;
		}

		Label label = new Label("-");

		label.setStyle("font-size:10px;" + "font-weight:600;" + "color:#64748b;" + "text-align:center;");

		return label;
	}

	private Component createReturnedActionComponent(OutwardCheque cheque) {

		String status = normalizeStatus(cheque.getChequeStatus());

		if (isDataEntryStatus(status)) {

			Button button = createActionButton("DATA ENTRY REQUIRED");

			button.addEventListener("onClick", event -> openReturnedDataEntry(cheque));

			return button;
		}

		if (isMicrRepairStatus(status)) {

			Button button = createActionButton("MICR REPAIR REQUIRED");

			button.addEventListener("onClick", event -> openReturnedMicrRepair(cheque));

			return button;
		}

		Label label = new Label("-");

		label.setStyle("font-size:10px;" + "font-weight:600;" + "color:#64748b;" + "text-align:center;");

		return label;
	}

	private Button createActionButton(String label) {

		Button button = new Button(label);

		button.setStyle("display:block;" + "width:calc(100% - 16px);" + "height:34px;" + "margin:0 8px;"
				+ "padding:0 8px;" + "box-sizing:border-box;" + "background:#172554;" + "background-color:#172554;"
				+ "border:1px solid #172554;" + "border-radius:6px;" + "color:#ffffff;" + "font-size:9px;"
				+ "font-weight:800;" + "line-height:34px;" + "text-align:center;" + "white-space:nowrap;"
				+ "cursor:pointer;");

		return button;
	}

	private String getStatusStyle(String status) {

		String commonStyle = "display:block;" + "width:100%;" + "height:36px;" + "box-sizing:border-box;" + "margin:0;"
				+ "padding:9px 12px;" + "border-radius:18px;" + "font-family:Arial,sans-serif;" + "font-size:10px;"
				+ "font-weight:800;" + "line-height:18px;" + "text-align:center;" + "white-space:nowrap;"
				+ "overflow:visible;" + "text-overflow:clip;" + "color:#334155;" + "box-shadow:"
				+ "inset 0 1px 2px rgba(255,255,255,0.95)," + "0 2px 5px rgba(15,23,42,0.04);";

		if (isDataEntryStatus(status)) {

			return commonStyle + "background:rgba(255,237,213,0.82);" + "background-color:rgba(255,237,213,0.82);"
					+ "border:1px solid rgba(249,115,22,0.32);";
		}

		if (isMicrRepairStatus(status)) {

			return commonStyle + "background:rgba(254,226,226,0.82);" + "background-color:rgba(254,226,226,0.82);"
					+ "border:1px solid rgba(220,38,38,0.32);";
		}

		if (STATUS_PENDING_VERIFICATION.equals(status)) {

			return commonStyle + "background:rgba(219,234,254,0.78);" + "background-color:rgba(219,234,254,0.78);"
					+ "border:1px solid rgba(59,130,246,0.25);";
		}

		if (STATUS_PENDING_CHECKER_PROCESS.equals(status)) {

			return commonStyle + "background:rgba(224,231,255,0.78);" + "background-color:rgba(224,231,255,0.78);"
					+ "border:1px solid rgba(79,70,229,0.25);";
		}

		if (STATUS_ON_HOLD.equals(status)) {

			return commonStyle + "background:rgba(254,243,199,0.80);" + "background-color:rgba(254,243,199,0.80);"
					+ "border:1px solid rgba(245,158,11,0.28);";
		}

		if (STATUS_REJECTION_REQUEST.equals(status)) {

			return commonStyle + "background:rgba(254,226,226,0.78);" + "background-color:rgba(254,226,226,0.78);"
					+ "border:1px solid rgba(220,38,38,0.25);";
		}

		return commonStyle + "background:rgba(241,245,249,0.78);" + "background-color:rgba(241,245,249,0.78);"
				+ "border:1px solid rgba(148,163,184,0.25);";
	}

	private boolean isDataEntryStatus(String status) {

		return STATUS_PENDING_DATA_ENTRY.equals(status);
	}

	private boolean isMicrRepairStatus(String status) {

		return STATUS_PENDING_MICR_REPAIR.equals(status);
	}

	private String getDisplayStatus(String status) {

		if (isDataEntryStatus(status)) {

			return STATUS_PENDING_DATA_ENTRY;
		}

		if (isMicrRepairStatus(status)) {

			return STATUS_PENDING_MICR_REPAIR;
		}

		if (STATUS_PENDING_VERIFICATION.equals(status)) {

			return STATUS_PENDING_VERIFICATION;
		}

		if (STATUS_PENDING_CHECKER_PROCESS.equals(status)) {

			return STATUS_PENDING_CHECKER_PROCESS;
		}

		if (STATUS_ON_HOLD.equals(status)) {

			return STATUS_ON_HOLD;
		}

		if (STATUS_REJECTION_REQUEST.equals(status)) {

			return STATUS_REJECTION_REQUEST;
		}

		return getValue(status);
	}

	private String normalizeStatus(String status) {

		if (status == null) {

			return "";
		}

		return status.trim().toUpperCase();
	}

	private String formatIndianAmount(BigDecimal amount) {

		if (amount == null) {

			return "₹0.00";
		}

		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH);

		symbols.setGroupingSeparator(',');

		DecimalFormat formatter = new DecimalFormat("##,##,##0.00", symbols);

		return "₹" + formatter.format(amount);
	}

	private String formatIndianDate(Object date) {

		if (date == null) {

			return "-";
		}

		try {

			if (date instanceof java.sql.Date) {

				return new SimpleDateFormat("dd-MM-yyyy").format((java.sql.Date) date);
			}

			if (date instanceof java.util.Date) {

				return new SimpleDateFormat("dd-MM-yyyy").format((java.util.Date) date);
			}

			String value = date.toString().trim();

			if (value.isEmpty()) {

				return "-";
			}

			if (value.matches("\\d{4}-\\d{2}-\\d{2}")) {

				java.util.Date parsedDate = new SimpleDateFormat("yyyy-MM-dd").parse(value);

				return new SimpleDateFormat("dd-MM-yyyy").format(parsedDate);
			}

			return value;

		} catch (Exception exception) {

			return getValue(date);
		}
	}

	private void openDataEntry(ScanCheque cheque) {

		if (cheque == null || cheque.getScannedChequeId() == null || cheque.getScannedChequeId().trim().isEmpty()) {

			return;
		}

		Include mainContentArea = findMainContentArea(outwardMakerRowsChequeDetails);

		if (mainContentArea == null) {

			throw new IllegalStateException("mainContentArea Include not found");
		}

		mainContentArea.clearDynamicProperties();

		mainContentArea.setDynamicProperty("chequeId", cheque.getScannedChequeId());

		mainContentArea.setDynamicProperty("batchId", cheque.getScannedBatchId());

		mainContentArea.setDynamicProperty("returnFromChecker", false);

		mainContentArea.setSrc("/outward/maker/cheque-data-entry.zul");
	}

	private void openMicrRepair(ScanCheque cheque) {

		if (cheque == null || cheque.getScannedChequeId() == null || cheque.getScannedChequeId().trim().isEmpty()) {

			return;
		}

		Include mainContentArea = findMainContentArea(outwardMakerRowsChequeDetails);

		if (mainContentArea == null) {

			throw new IllegalStateException("mainContentArea Include not found");
		}

		mainContentArea.clearDynamicProperties();

		mainContentArea.setDynamicProperty("chequeId", cheque.getScannedChequeId());

		mainContentArea.setDynamicProperty("batchId", cheque.getScannedBatchId());

		mainContentArea.setDynamicProperty("returnFromChecker", false);

		mainContentArea.setSrc("/outward/maker/micr-repair/" + "micr-repair-view.zul");
	}

	private void openReturnedDataEntry(OutwardCheque cheque) {

		if (cheque == null || cheque.getOutwardChequeId() == null || cheque.getOutwardChequeId().trim().isEmpty()) {

			return;
		}

		Include mainContentArea = findMainContentArea(outwardMakerRowsChequeDetails);

		if (mainContentArea == null) {

			throw new IllegalStateException("mainContentArea Include not found");
		}

		mainContentArea.clearDynamicProperties();

		mainContentArea.setDynamicProperty("chequeId", cheque.getOutwardChequeId());

		mainContentArea.setDynamicProperty("outwardChequeId", cheque.getOutwardChequeId());

		mainContentArea.setDynamicProperty("batchId", cheque.getOutwardBatchId());

		mainContentArea.setDynamicProperty("returnFromChecker", true);

		mainContentArea.setSrc("/outward/maker/data-entry.zul");
	}

	private void openReturnedMicrRepair(OutwardCheque cheque) {

		if (cheque == null || cheque.getOutwardChequeId() == null || cheque.getOutwardChequeId().trim().isEmpty()) {

			return;
		}

		Include mainContentArea = findMainContentArea(outwardMakerRowsChequeDetails);

		if (mainContentArea == null) {

			throw new IllegalStateException("mainContentArea Include not found");
		}

		mainContentArea.clearDynamicProperties();

		mainContentArea.setDynamicProperty("chequeId", cheque.getOutwardChequeId());

		mainContentArea.setDynamicProperty("outwardChequeId", cheque.getOutwardChequeId());

		mainContentArea.setDynamicProperty("batchId", cheque.getOutwardBatchId());

		mainContentArea.setDynamicProperty("returnFromChecker", true);

		mainContentArea.setSrc("/outward/maker/micr-repair/" + "micr-repair-view.zul");
	}

	private Include findMainContentArea(Component component) {

		Component current = component;

		while (current != null) {

			if (current instanceof Include && "mainContentArea".equals(current.getId())) {

				return (Include) current;
			}

			current = current.getParent();
		}

		return null;
	}

	private void goBackToDashboard() {

		Include mainContentArea = findMainContentArea(outwardMakerRowsChequeDetails);

		if (mainContentArea == null) {

			throw new IllegalStateException("mainContentArea Include not found");
		}

		mainContentArea.clearDynamicProperties();

		mainContentArea.setSrc("/outward/maker/dashboard.zul");
	}

	private void goToFirstPage() {

		if (currentPage > 1) {

			currentPage = 1;

			renderCurrentPage();
		}
	}

	private void goToPreviousPage() {

		if (currentPage > 1) {

			currentPage--;

			renderCurrentPage();
		}
	}

	private void goToNextPage() {

		if (currentPage < getTotalPages()) {

			currentPage++;

			renderCurrentPage();
		}
	}

	private void goToLastPage() {

		int totalPages = getTotalPages();

		if (currentPage < totalPages) {

			currentPage = totalPages;

			renderCurrentPage();
		}
	}

	private int getTotalPages() {

		if (displayChequeList == null || displayChequeList.isEmpty()) {

			return 1;
		}

		return (int) Math.ceil((double) displayChequeList.size() / PAGE_SIZE);
	}

	private void updatePagination() {

		int totalPages = getTotalPages();

		outwardMakerLblCurrentPage.setValue(currentPage + " / " + totalPages);

		outwardMakerBtnFirst.setDisabled(currentPage <= 1);

		outwardMakerBtnPrevious.setDisabled(currentPage <= 1);

		outwardMakerBtnNext.setDisabled(currentPage >= totalPages);

		outwardMakerBtnLast.setDisabled(currentPage >= totalPages);
	}

	private void showEmptyState() {

		outwardMakerGridChequeDetails.setVisible(false);

		outwardMakerVlayoutEmptyState.setVisible(true);

		outwardMakerLblBatchId.setValue("-");

		outwardMakerLblChequeCount.setValue("0");

		outwardMakerLblTotalAmount.setValue("₹0.00");
	}

	private String getValue(Object value) {

		if (value == null) {

			return "-";
		}

		String text = String.valueOf(value);

		if (text.trim().isEmpty()) {

			return "-";
		}

		return text;
	}
}