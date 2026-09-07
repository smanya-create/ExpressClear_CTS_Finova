package com.iispl.cts.controller.outward.checker;

import java.math.BigDecimal;

import java.text.SimpleDateFormat;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.iispl.cts.entity.outward.OutwardRejectedCheques;
import com.iispl.cts.service.outward.OutwardCheckerRejectionService;
import com.iispl.cts.serviceimpl.outward.OutwardCheckerRejectionServiceImpl;

public class OutwardCheckerRejectionController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;

	// =========================================================
	// COMPONENTS
	// =========================================================

	private Listbox lstRejectedCheques;

	private Textbox txtSearch;

	private Datebox dateRejected;

	private Button btnSearch;

	private Button btnClear;

	private Button btnFirst;

	private Button btnPrevious;

	private Button btnNext;

	private Button btnLast;

	private Label lblTotalRejected;

	private Label lblPageInfo;

	private Div windowHost;

	// =========================================================
	// SERVICE
	// =========================================================

	private final OutwardCheckerRejectionService rejectionService = new OutwardCheckerRejectionServiceImpl();

	// =========================================================
	// PAGINATION VARIABLES
	// =========================================================

	private static final int PAGE_SIZE = 10;

	private int currentPage = 1;

	private int totalRecords = 0;

	private int totalPages = 1;

	// =========================================================
	// INITIAL LOAD
	// =========================================================

	@Override
	public void doAfterCompose(Component comp) throws Exception {

		super.doAfterCompose(comp);

		loadRejectedCheques();
	}

	// =========================================================
	// LOAD REJECTED CHEQUES
	// =========================================================

	private void loadRejectedCheques() {

		try {

			String searchValue = getSearchValue();
			
			java.sql.Date rejectedDate = getRejectedDate();

			// -------------------------------------------------
			// GET TOTAL
			// -------------------------------------------------

			totalRecords = rejectionService.getTotalRejectedCheques(searchValue, rejectedDate);

			calculateTotalPages();

			// -------------------------------------------------
			// FIX CURRENT PAGE
			// -------------------------------------------------

			if (currentPage > totalPages) {
				currentPage = totalPages;
			}

			if (currentPage < 1) {
				currentPage = 1;
			}

			// -------------------------------------------------
			// OFFSET
			// -------------------------------------------------

			int offset = (currentPage - 1) * PAGE_SIZE;

			// -------------------------------------------------
			// LOAD PAGE
			// -------------------------------------------------

			List<OutwardRejectedCheques> rejectedCheques = rejectionService.searchRejectedCheques(searchValue,
					rejectedDate, PAGE_SIZE, offset);

			displayRejectedCheques(rejectedCheques);

			updatePagination();

			if (lblTotalRejected != null) {

				lblTotalRejected.setValue(String.valueOf(totalRecords));
			}

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to load rejected cheques.\n" + e.getMessage(), "Error", Messagebox.OK,
					Messagebox.ERROR);
		}
	}

	// =========================================================
	// DISPLAY REJECTED CHEQUES
	// =========================================================

	private void displayRejectedCheques(List<OutwardRejectedCheques> rejectedCheques) {

		ListModelList<OutwardRejectedCheques> model = new ListModelList<>(rejectedCheques);

		lstRejectedCheques.setModel(model);

		lstRejectedCheques.setItemRenderer(new ListitemRenderer<OutwardRejectedCheques>() {

			@Override
			public void render(Listitem item, OutwardRejectedCheques cheque, int index) throws Exception {

				// =============================================
				// BATCH ID
				// =============================================

				item.appendChild(new Listcell(safe(cheque.getOutwardBatchId())));

				item.appendChild(new Listcell(safe(cheque.getOutwardChequeId())));

				BigDecimal amount = cheque.getChequeAmount();

				String amountText = amount != null ? "₹ " + amount.toPlainString() : "-";

				item.appendChild(new Listcell(amountText));

				// =============================================
				// REASON
				// =============================================

				item.appendChild(new Listcell(safe(cheque.getRemarks())));

				// =============================================
				// DATE
				// =============================================

				String rejectedDate = "-";

				if (cheque.getRejectedDate() != null) {

					SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");

					rejectedDate = sdf.format(cheque.getRejectedDate());
				}

				item.appendChild(new Listcell(rejectedDate));

				// =============================================
				// ACTION
				// =============================================

				Listcell actionCell = new Listcell();

				Button viewButton = new Button("VIEW");

				viewButton.setSclass("view-button");

				viewButton.setTooltiptext("View rejected cheque details");

				viewButton.addEventListener("onClick", event -> showRejectedCheque(cheque));

				actionCell.appendChild(viewButton);

				item.appendChild(actionCell);
			}
		});
	}

	// =========================================================
	// SEARCH
	// =========================================================

	public void onClick$btnSearch(Event event) {

		currentPage = 1;

		loadRejectedCheques();
	}

	// =========================================================
	// CLEAR SEARCH
	// =========================================================

	public void onClick$btnClear(Event event) {

		if (txtSearch != null) {
			txtSearch.setValue("");
		}

		if (dateRejected != null) {
			dateRejected.setValue(null);
		}

		currentPage = 1;

		loadRejectedCheques();
	}

	// =========================================================
	// FIRST PAGE
	// =========================================================

	public void onClick$btnFirst(Event event) {

		if (currentPage > 1) {

			currentPage = 1;

			loadRejectedCheques();
		}
	}

	// =========================================================
	// PREVIOUS PAGE
	// =========================================================

	public void onClick$btnPrevious(Event event) {

		if (currentPage > 1) {

			currentPage--;

			loadRejectedCheques();
		}
	}

	// =========================================================
	// NEXT PAGE
	// =========================================================

	public void onClick$btnNext(Event event) {

		if (currentPage < totalPages) {

			currentPage++;

			loadRejectedCheques();
		}
	}

	// =========================================================
	// LAST PAGE
	// =========================================================

	public void onClick$btnLast(Event event) {

		if (currentPage < totalPages) {

			currentPage = totalPages;

			loadRejectedCheques();
		}
	}

	// =========================================================
	// CALCULATE TOTAL PAGES
	// =========================================================

	private void calculateTotalPages() {

		if (totalRecords == 0) {

			totalPages = 1;

		} else {

			totalPages = (int) Math.ceil((double) totalRecords / PAGE_SIZE);
		}
	}

	// =========================================================
	// UPDATE PAGINATION UI
	// =========================================================

	private void updatePagination() {

		if (lblPageInfo != null) {

			lblPageInfo.setValue("Page " + currentPage + " of " + totalPages);
		}

		if (btnFirst != null) {

			btnFirst.setDisabled(currentPage <= 1);
		}

		if (btnPrevious != null) {

			btnPrevious.setDisabled(currentPage <= 1);
		}

		if (btnNext != null) {

			btnNext.setDisabled(currentPage >= totalPages);
		}

		if (btnLast != null) {

			btnLast.setDisabled(currentPage >= totalPages);
		}
	}

	// =========================================================
	// GET SEARCH VALUE
	// =========================================================

	private String getSearchValue() {

		if (txtSearch == null) {
			return null;
		}

		String value = txtSearch.getValue();

		if (value == null || value.trim().isEmpty()) {

			return null;
		}

		return value.trim();
	}

	// =========================================================
	// GET DATE
	// =========================================================

	private java.sql.Date getRejectedDate() {

		if (dateRejected == null || dateRejected.getValue() == null) {

			return null;
		}

		return new java.sql.Date(dateRejected.getValue().getTime());
	}

	// =========================================================
	// SHOW DETAILS
	// =========================================================

	private void showRejectedCheque(OutwardRejectedCheques cheque) {

		try {

			Window window = (Window) Executions.createComponents("/outward/checker/rejected-cheque-view.zul",
					windowHost, null);

			// =============================================
			// BATCH ID
			// =============================================

			Label lblBatchId = (Label) window.getFellow("lblBatchId");

			lblBatchId.setValue(safe(cheque.getOutwardBatchId()));

			// =============================================
			// CHEQUE NUMBER
			// =============================================

			Label lblChequeNumber = (Label) window.getFellow("lblChequeNumber");

			lblChequeNumber.setValue(safe(cheque.getOutwardChequeId()));

			// =============================================
			// AMOUNT
			// =============================================

			Label lblAmount = (Label) window.getFellow("lblAmount");

			String amount = "-";

			if (cheque.getChequeAmount() != null) {

				amount = "₹ " + cheque.getChequeAmount().toPlainString();
			}

			lblAmount.setValue(amount);

			// =============================================
			// REJECTED BY
			// =============================================

			Label lblRejectedBy = (Label) window.getFellow("lblRejectedBy");

			lblRejectedBy.setValue(safe(cheque.getRejectedBy()));

			// =============================================
			// REJECTED DATE
			// =============================================

			Label lblRejectedDate = (Label) window.getFellow("lblRejectedDate");

			String rejectedDate = "-";

			if (cheque.getRejectedDate() != null) {

				SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy HH:mm");

				rejectedDate = sdf.format(cheque.getRejectedDate());
			}

			lblRejectedDate.setValue(rejectedDate);

			Label lblReason = (Label) window.getFellow("lblReason");

			lblReason.setValue(safe(cheque.getRemarks()));


			window.doModal();

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to open rejected cheque details.\n" + e.getMessage(), "Error", Messagebox.OK,
					Messagebox.ERROR);
		}
	}

	// =========================================================
	// SAFE
	// =========================================================

	private String safe(String value) {

		return value != null && !value.trim().isEmpty() ? value : "-";
	}
}