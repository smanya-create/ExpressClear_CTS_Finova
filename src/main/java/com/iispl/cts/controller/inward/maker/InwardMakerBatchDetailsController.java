package com.iispl.cts.controller.inward.maker;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Vlayout;

import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.service.inward.InwardBatchService;
import com.iispl.cts.service.inward.InwardChequeService;
import com.iispl.cts.serviceimpl.inward.InwardBatchServiceImpl;
import com.iispl.cts.serviceimpl.inward.InwardChequeServiceImpl;

public class InwardMakerBatchDetailsController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;
	private static final int PAGE_SIZE = 10;

	private Grid inwardMakerGridChequeDetails;
	private Rows inwardMakerRowsChequeDetails;
	private Vlayout inwardMakerVlayoutEmptyState;

	private Label inwardMakerLblBatchId;
	private Label inwardMakerLblChequeCount;
	private Label inwardMakerLblTotalAmount;
	private Label inwardMakerLblCurrentPage;

	private Button inwardMakerBtnFirst;
	private Button inwardMakerBtnPrevious;
	private Button inwardMakerBtnNext;
	private Button inwardMakerBtnLast;
	private Button inwardMakerBtnBack;

	private InwardBatchService inwardBatchService;
	private InwardChequeService inwardChequeService;

	private InwardBatch inwardBatch;
	private List<InwardCheque> inwardChequeList = new ArrayList<>();

	private int currentPage = 1;
	private String currentBatchId;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		this.inwardBatchService = new InwardBatchServiceImpl();
		this.inwardChequeService = new InwardChequeServiceImpl();

		if (inwardMakerBtnFirst != null) inwardMakerBtnFirst.addEventListener("onClick", event -> goToFirstPage());
		if (inwardMakerBtnPrevious != null) inwardMakerBtnPrevious.addEventListener("onClick", event -> goToPreviousPage());
		if (inwardMakerBtnNext != null) inwardMakerBtnNext.addEventListener("onClick", event -> goToNextPage());
		if (inwardMakerBtnLast != null) inwardMakerBtnLast.addEventListener("onClick", event -> goToLastPage());
		if (inwardMakerBtnBack != null) inwardMakerBtnBack.addEventListener("onClick", event -> goBackToDashboard());

		loadBatchDetails();
	}

	private void loadBatchDetails() {
		try {
			this.currentBatchId = Executions.getCurrent().getParameter("batchId");

			if (this.currentBatchId == null || this.currentBatchId.trim().isEmpty()) {
				Object attr = Executions.getCurrent().getAttribute("batchId");
				if (attr != null) this.currentBatchId = attr.toString();
			}

			if (this.currentBatchId == null || this.currentBatchId.trim().isEmpty()) {
				Object sess = Sessions.getCurrent().getAttribute("INWARD_MAKER_SELECTED_BATCH_ID");
				if (sess == null) sess = Sessions.getCurrent().getAttribute("ACTIVE_INWARD_BATCH_ID");
				if (sess == null) sess = Sessions.getCurrent().getAttribute("batchId");
				if (sess != null) this.currentBatchId = sess.toString();
			}

			if (this.currentBatchId == null || this.currentBatchId.trim().isEmpty()) {
				showEmptyState();
				return;
			}

			this.currentBatchId = this.currentBatchId.trim();
			this.inwardBatch = inwardBatchService.getBatchById(this.currentBatchId);

			if (this.inwardBatch == null) {
				showEmptyState();
				return;
			}

			this.inwardChequeList = inwardChequeService.getChequesByBatchAndStatus(this.currentBatchId, null);
			if (this.inwardChequeList == null) {
				this.inwardChequeList = new ArrayList<>();
			}

			populateBatchSummary();
			this.currentPage = 1;
			renderCurrentPage();

		} catch (Exception e) {
			e.printStackTrace();
			showEmptyState();
		}
	}

	private void populateBatchSummary() {
		if (inwardMakerLblBatchId != null) inwardMakerLblBatchId.setValue(getValue(inwardBatch.getInwardBatchId()));
		if (inwardMakerLblChequeCount != null) inwardMakerLblChequeCount.setValue(String.valueOf(inwardBatch.getActualChequeCount()));
		if (inwardMakerLblTotalAmount != null) inwardMakerLblTotalAmount.setValue(formatIndianAmount(inwardBatch.getActualTotalAmount()));
	}

	private void renderCurrentPage() {
		if (inwardMakerRowsChequeDetails == null) return;
		inwardMakerRowsChequeDetails.getChildren().clear();

		if (inwardChequeList == null || inwardChequeList.isEmpty()) {
			showEmptyState();
			updatePagination();
			return;
		}

		if (inwardMakerGridChequeDetails != null) inwardMakerGridChequeDetails.setVisible(true);
		if (inwardMakerVlayoutEmptyState != null) inwardMakerVlayoutEmptyState.setVisible(false);

		int totalPages = getTotalPages();
		if (currentPage > totalPages) currentPage = totalPages;
		if (currentPage < 1) currentPage = 1;

		int startIndex = (currentPage - 1) * PAGE_SIZE;
		int endIndex = Math.min(startIndex + PAGE_SIZE, inwardChequeList.size());

		for (int i = startIndex; i < endIndex; i++) {
			InwardCheque cheque = inwardChequeList.get(i);
			if (cheque != null) {
				createChequeRow(cheque);
			}
		}

		updatePagination();
	}

	private void createChequeRow(InwardCheque cheque) {
		Row row = new Row();

		Label chequeNumberLabel = new Label(getValue(cheque.getChequeNumber()));
		chequeNumberLabel.setSclass("inward-maker-cheque-number");

		Label accountLabel = new Label(getValue(cheque.getDraweeAccountNumber()));
		accountLabel.setSclass("inward-maker-drawee-account");

		Label chequeDateLabel = new Label(formatIndianDate(cheque.getChequeDate()));
		chequeDateLabel.setSclass("inward-maker-cheque-date");

		Label micrCodeLabel = new Label(getValue(cheque.getMicrCode()));
		micrCodeLabel.setSclass("inward-maker-micr-code");

		String actualStatus = normalizeStatus(cheque.getChequeStatus());
		Label chequeStatusLabel = new Label(getDisplayStatus(actualStatus));
		chequeStatusLabel.setSclass("inward-maker-cheque-status " + getStatusClass(actualStatus));

		Label chequeAmountLabel = new Label(formatIndianAmount(cheque.getChequeAmount()));
		chequeAmountLabel.setSclass("inward-maker-cheque-amount");

		Component actionComponent = createActionComponent(cheque);

		row.appendChild(chequeNumberLabel);
		row.appendChild(accountLabel);
		row.appendChild(chequeDateLabel);
		row.appendChild(micrCodeLabel);
		row.appendChild(chequeStatusLabel);
		row.appendChild(chequeAmountLabel);
		row.appendChild(actionComponent);

		inwardMakerRowsChequeDetails.appendChild(row);
	}

	private Component createActionComponent(InwardCheque cheque) {
		String status = normalizeStatus(cheque.getChequeStatus());

		if (isMicrRepairStatus(status)) {
			Button button = new Button("MICR REPAIR REQUIRED");
			button.setSclass("inward-maker-action-button");
			button.addEventListener("onClick", event -> openMicrRepair(cheque));
			return button;
		}

		if (isDataEntryStatus(status)) {
			Button button = new Button("DATA ENTRY REQUIRED");
			button.setSclass("inward-maker-action-button");
			button.addEventListener("onClick", event -> openDataEntry(cheque));
			return button;
		}

		Label label = new Label("-");
		label.setSclass("inward-maker-action-empty");
		return label;
	}

	private void openMicrRepair(InwardCheque cheque) {
		if (cheque == null) return;
		String chqId = cheque.getInwardChequeId();

		Sessions.getCurrent().setAttribute("MICR_REPAIR_BATCH_ID", this.currentBatchId);
		Sessions.getCurrent().setAttribute("MICR_REPAIR_CHEQUE_ID", chqId);
		Sessions.getCurrent().setAttribute("batchId", this.currentBatchId);

		Executions.sendRedirect("/inward/maker/index.zul?page=micr-repair&batchId=" + this.currentBatchId + "&chequeId=" + chqId);
	}

	private void openDataEntry(InwardCheque cheque) {
		if (cheque == null) return;
		String chqId = cheque.getInwardChequeId();

		Sessions.getCurrent().setAttribute("DATA_ENTRY_BATCH_ID", this.currentBatchId);
		Sessions.getCurrent().setAttribute("DATA_ENTRY_CHEQUE_ID", chqId);
		Sessions.getCurrent().setAttribute("batchId", this.currentBatchId);

		Executions.sendRedirect("/inward/maker/index.zul?page=data-entry&batchId=" + this.currentBatchId + "&chequeId=" + chqId);
	}

	private void goBackToDashboard() {
		Sessions.getCurrent().removeAttribute("INWARD_MAKER_SELECTED_BATCH_ID");
		Sessions.getCurrent().removeAttribute("ACTIVE_INWARD_BATCH_ID");
		Sessions.getCurrent().removeAttribute("MICR_REPAIR_BATCH_ID");
		Sessions.getCurrent().removeAttribute("DATA_ENTRY_BATCH_ID");
		Sessions.getCurrent().removeAttribute("batchId");
		Executions.sendRedirect("/inward/maker/index.zul");
	}

	private boolean isMicrRepairStatus(String status) {
		return "PENDING_MICR_REPAIR".equals(status) || "MICR_REPAIR_PENDING".equals(status)
				|| "MICR_REPAIR_IN_PROGRESS".equals(status) || "MICR_REPAIR_REQUIRED".equals(status)
				|| "SEND_BACK_TO_MAKER_MICR".equals(status);
	}

	private boolean isDataEntryStatus(String status) {
		return "PENDING_DATA_ENTRY".equals(status) || "DATA_ENTRY_PENDING".equals(status)
				|| "DATA_ENTRY_IN_PROGRESS".equals(status) || "SEND_BACK_TO_MAKER_DATA_ENTRY".equals(status);
	}

	private String getDisplayStatus(String status) {
		if (isMicrRepairStatus(status)) return "PENDING_MICR_REPAIR";
		if (isDataEntryStatus(status)) return "PENDING_DATA_ENTRY";
		if ("SEND_BACK_TO_MAKER".equals(status) || "ON_HOLD".equals(status)) return "ON_HOLD";
		return getValue(status);
	}

	private String getStatusClass(String status) {
		if (isMicrRepairStatus(status)) return "pending-micr-repair";
		if (isDataEntryStatus(status)) return "pending-data-entry";
		if ("SEND_BACK_TO_MAKER".equals(status) || "ON_HOLD".equals(status)) return "on-hold";
		return "pending-data-entry";
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
		if (inwardChequeList == null || inwardChequeList.isEmpty()) return 1;
		return (int) Math.ceil((double) inwardChequeList.size() / PAGE_SIZE);
	}

	private void updatePagination() {
		int totalPages = getTotalPages();
		if (inwardMakerLblCurrentPage != null) {
			inwardMakerLblCurrentPage.setValue(currentPage + " / " + totalPages);
		}
		if (inwardMakerBtnFirst != null) inwardMakerBtnFirst.setDisabled(currentPage <= 1);
		if (inwardMakerBtnPrevious != null) inwardMakerBtnPrevious.setDisabled(currentPage <= 1);
		if (inwardMakerBtnNext != null) inwardMakerBtnNext.setDisabled(currentPage >= totalPages);
		if (inwardMakerBtnLast != null) inwardMakerBtnLast.setDisabled(currentPage >= totalPages);
	}

	private void showEmptyState() {
		if (inwardMakerGridChequeDetails != null) inwardMakerGridChequeDetails.setVisible(false);
		if (inwardMakerVlayoutEmptyState != null) inwardMakerVlayoutEmptyState.setVisible(true);
		if (inwardMakerLblBatchId != null) inwardMakerLblBatchId.setValue("-");
		if (inwardMakerLblChequeCount != null) inwardMakerLblChequeCount.setValue("0");
		if (inwardMakerLblTotalAmount != null) inwardMakerLblTotalAmount.setValue("₹0.00");
	}

	private String formatIndianAmount(BigDecimal amount) {
		if (amount == null) return "₹0.00";
		DecimalFormatSymbols symbols = DecimalFormatSymbols.getInstance(Locale.ENGLISH);
		symbols.setGroupingSeparator(',');
		return "₹" + new DecimalFormat("##,##,##0.00", symbols).format(amount);
	}

	private String formatIndianDate(Object date) {
		if (date == null) return "-";
		try {
			if (date instanceof java.sql.Date || date instanceof java.util.Date) {
				return new SimpleDateFormat("dd-MM-yyyy").format((java.util.Date) date);
			}
			String val = date.toString().trim();
			if (val.matches("\\d{4}-\\d{2}-\\d{2}")) {
				return new SimpleDateFormat("dd-MM-yyyy").format(new SimpleDateFormat("yyyy-MM-dd").parse(val));
			}
			return val;
		} catch (Exception e) {
			return getValue(date);
		}
	}

	private String getValue(Object value) {
		return (value == null || String.valueOf(value).trim().isEmpty()) ? "-" : String.valueOf(value).trim();
	}

	private String normalizeStatus(String status) {
		return (status != null) ? status.trim().toUpperCase() : "";
	}
}