package com.iispl.cts.controller.outward.checker;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Window;

import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.service.outward.OutwardBatchService;
import com.iispl.cts.serviceimpl.outward.OutwardBatchServiceImpl;

public class OutwardCheckerDashboardController extends GenericForwardComposer<Component> {

	private Label lblTotalBatches;
	private Label lblTotalCheques;
	private Listbox lstBatches;
	private Button btnPrevious;
	private Button btnNext;
	private Label lblPage;

	private OutwardBatchService outwardBatchService = new OutwardBatchServiceImpl();
	int pageNumber = 1;
	int pageSize = 5;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		String role = (String) Sessions.getCurrent().getAttribute("CTS_USER_ROLE");
		btnPrevious.addEventListener(Events.ON_CLICK, event -> {
			if (pageNumber > 1) {
				pageNumber--;
				loadDashboard();
			}
		});

		btnNext.addEventListener(Events.ON_CLICK, event -> {
			int totalBatches = outwardBatchService.getPendingBatchCount();
			int totalPages = (int) Math.ceil((double) totalBatches / pageSize);

			if (pageNumber < totalPages) {
				pageNumber++;
				loadDashboard();
			}
		});

		loadDashboard();
	}

	private void loadDashboard() {

		try {
			List<OutwardBatch> pendingBatches = outwardBatchService.getPendingBatches(pageNumber, pageSize);

			int totalBatches = outwardBatchService.getPendingBatchCount();
			int totalPages = (int) Math.ceil((double) totalBatches / pageSize);

			lblPage.setValue(pageNumber + "/" + totalPages);

			btnPrevious.setDisabled(pageNumber == 1);
			btnNext.setDisabled(pageNumber >= totalPages);

			int totalCheques = 0;

			for (OutwardBatch batch : pendingBatches) {
				totalCheques += batch.getActualChequeCount();
			}

			lblTotalBatches.setValue(String.valueOf(totalBatches));

			lblTotalCheques.setValue(String.valueOf(totalCheques));

			ListModelList<OutwardBatch> model = new ListModelList<>(pendingBatches);

			lstBatches.setModel(model);

			lstBatches.setItemRenderer(new ListitemRenderer<OutwardBatch>() {

				@Override
				public void render(Listitem item, OutwardBatch batch, int index) throws Exception {
					item.setValue(batch);
					item.appendChild(new Listcell(batch.getOutwardBatchId()));
					item.appendChild(new Listcell(String.valueOf(batch.getActualChequeCount())));
					BigDecimal amount = batch.getActualTotalAmount();
					String amountText = amount == null ? "₹0.00" : "₹" + amount.toPlainString();
					item.appendChild(new Listcell(amountText));
					
					String submittedBy = batch.getUploadedBy();
					item.appendChild(new Listcell(submittedBy == null ? "-" : submittedBy));

					String submittedAt = "-";

					if (batch.getUploadedAt() != null) {

						SimpleDateFormat formatter = new SimpleDateFormat("dd-MMM-yyyy HH:mm a");

						submittedAt = formatter.format(batch.getUploadedAt());
					}

					item.appendChild(new Listcell(submittedAt));

					Listcell statusCell = new Listcell();

					Label statusLabel = new Label(batch.getBatchStatus());

					statusLabel.setSclass("status-pending");

					statusCell.appendChild(statusLabel);

					item.appendChild(statusCell);

					Listcell actionCell = new Listcell();

					Button queueButton = new Button("QUEUE");

					queueButton.setSclass("queue-button");

					queueButton.addEventListener(Events.ON_CLICK, event -> {

					    OutwardBatch selectedBatch = item.getValue();

					    Map<String, Object> args = new HashMap<>();

					    args.put("batchId", selectedBatch.getOutwardBatchId());

					    Window popup = (Window) Executions.createComponents(
					            "/outward/checker/batch-proceed-popup.zul",
					            null,
					            args
					    );

					    popup.doModal();
					});
					actionCell.appendChild(queueButton);

					item.appendChild(actionCell);
				}
			});

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("Unable to load Checker Dashboard.\n\n" + e.getMessage(), "Dashboard Error", Messagebox.OK,
					Messagebox.ERROR);
		}
	}
}