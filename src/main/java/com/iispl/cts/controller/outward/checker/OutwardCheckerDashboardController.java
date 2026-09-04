package com.iispl.cts.controller.outward.checker;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.List;

import org.zkoss.zk.ui.Component;
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

import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.service.outward.OutwardBatchService;
import com.iispl.cts.serviceimpl.outward.OutwardBatchServiceImpl;

public class OutwardCheckerDashboardController extends GenericForwardComposer<Component> {

	private Label lblTotalBatches;
	private Label lblTotalCheques;
	private Listbox lstBatches;

	private OutwardBatchService outwardBatchService = new OutwardBatchServiceImpl();

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);
		String role = (String) Sessions.getCurrent().getAttribute("USER_ROLE");
		loadDashboard();
	}

	private void loadDashboard() {
		try {
			List<OutwardBatch> pendingBatches = outwardBatchService.getPendingBatches();

			int totalBatches = pendingBatches.size();

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

					queueButton.setAttribute("batch", batch);

					queueButton.addEventListener(Events.ON_CLICK, event -> {

						OutwardBatch selectedBatch = (OutwardBatch) queueButton.getAttribute("batch");

						Messagebox.show(
								"Batch " + selectedBatch.getOutwardBatchId() + " selected for Checker verification.",
								"Checker Queue", Messagebox.OK, Messagebox.INFORMATION);
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