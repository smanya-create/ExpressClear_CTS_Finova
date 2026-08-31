package com.iispl.cts.controller.inward.maker;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

public class InwardIntakeController extends GenericComposer {

	private Window validationFailedWindow;
	private Window validationSuccessWindow;
	private Window micrRepairWindow;

	private Textbox batchNoTextbox;
	private Combobox statusCombobox;
	private Datebox receivedDate;

	private Label statusLabel1;
	private Label statusLabel2;
	private Label successBatchNo;
	private Label batchCountLabel;

	private Button validateButton1;
	private Button validateButton2;

	private Button micrRepairButton1;
	private Button micrRepairButton2;
	private Button micrRepairButton;

	private Listitem batchRow1;
	private Listitem batchRow2;
	private Listitem batchRow3;
	private Listitem batchRow4;

	@Override
	public void doAfterCompose(Component comp) throws Exception {

		super.doAfterCompose(comp);

		Window mainWindow = (Window) comp;

		validationFailedWindow = (Window) mainWindow.getFellow("validationFailedWindow");

		validationSuccessWindow = (Window) mainWindow.getFellow("validationSuccessWindow");

		micrRepairWindow = (Window) mainWindow.getFellow("micrRepairWindow");

		batchNoTextbox = (Textbox) mainWindow.getFellow("batchNoTextbox");

		statusCombobox = (Combobox) mainWindow.getFellow("statusCombobox");

		receivedDate = (Datebox) mainWindow.getFellow("receivedDate");

		batchCountLabel = (Label) mainWindow.getFellow("batchCountLabel");

		statusLabel1 = (Label) mainWindow.getFellow("statusLabel1");

		statusLabel2 = (Label) mainWindow.getFellow("statusLabel2");

		validateButton1 = (Button) mainWindow.getFellow("validateButton1");

		validateButton2 = (Button) mainWindow.getFellow("validateButton2");

		micrRepairButton1 = (Button) mainWindow.getFellow("micrRepairButton1");

		micrRepairButton2 = (Button) mainWindow.getFellow("micrRepairButton2");

		micrRepairButton = (Button) mainWindow.getFellow("micrRepairButton");

		successBatchNo = (Label) validationSuccessWindow.getFellow("successBatchNo");

		Button backToUploadButton = (Button) validationFailedWindow.getFellow("backToUploadButton");

		Button successOkButton = (Button) validationSuccessWindow.getFellow("successOkButton");

		Button micrOkButton = (Button) micrRepairWindow.getFellow("micrOkButton");

		batchRow1 = (Listitem) mainWindow.getFellow("batchRow1");

		batchRow2 = (Listitem) mainWindow.getFellow("batchRow2");

		batchRow3 = (Listitem) mainWindow.getFellow("batchRow3");

		batchRow4 = (Listitem) mainWindow.getFellow("batchRow4");

		Button searchButton = (Button) mainWindow.getFellow("searchButton");

		Button viewErrorButton = (Button) mainWindow.getFellow("viewErrorButton");

		searchButton.addEventListener("onClick", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {
				searchBatches();
			}
		});

		validateButton1.addEventListener("onClick", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				statusLabel1.setValue("Validated");
				statusLabel1.setSclass("status-validated");

				validateButton1.setVisible(false);
				micrRepairButton1.setVisible(true);

				successBatchNo.setValue("IW-20260827-001");

				validationSuccessWindow.setVisible(true);
			}
		});

		validateButton2.addEventListener("onClick", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				statusLabel2.setValue("Validated");
				statusLabel2.setSclass("status-validated");

				validateButton2.setVisible(false);
				micrRepairButton2.setVisible(true);

				successBatchNo.setValue("IW-20260827-002");

				validationSuccessWindow.setVisible(true);
			}
		});

		viewErrorButton.addEventListener("onClick", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				validationFailedWindow.setVisible(true);
			}
		});

		backToUploadButton.addEventListener("onClick", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				validationFailedWindow.setVisible(false);
			}
		});

		successOkButton.addEventListener("onClick", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				validationSuccessWindow.setVisible(false);
			}
		});

		micrRepairButton1.addEventListener("onClick", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				micrRepairWindow.setVisible(true);
			}
		});

		micrRepairButton2.addEventListener("onClick", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				micrRepairWindow.setVisible(true);
			}
		});

		micrRepairButton.addEventListener("onClick", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				micrRepairWindow.setVisible(true);
			}
		});

		micrOkButton.addEventListener("onClick", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				micrRepairWindow.setVisible(false);
			}
		});
	}

	private void searchBatches() {

		String batchNo = "";

		if (batchNoTextbox.getValue() != null) {
			batchNo = batchNoTextbox.getValue().trim().toLowerCase();
		}

		String status = "ALL";

		if (statusCombobox.getSelectedItem() != null) {
			status = statusCombobox.getSelectedItem().getValue();
		}

		Date selectedDate = receivedDate.getValue();

		boolean row1 = matches("IW-20260827-001", "PENDING", "27-Aug-2026", batchNo, status, selectedDate);

		boolean row2 = matches("IW-20260827-002", "PENDING", "27-Aug-2026", batchNo, status, selectedDate);

		boolean row3 = matches("IW-20260827-003", "FAILED", "27-Aug-2026", batchNo, status, selectedDate);

		boolean row4 = matches("IW-20260827-004", "VALIDATED", "27-Aug-2026", batchNo, status, selectedDate);

		batchRow1.setVisible(row1);
		batchRow2.setVisible(row2);
		batchRow3.setVisible(row3);
		batchRow4.setVisible(row4);

		int count = 0;

		if (row1) {
			count++;
		}

		if (row2) {
			count++;
		}

		if (row3) {
			count++;
		}

		if (row4) {
			count++;
		}

		batchCountLabel.setValue("Showing " + count + " of 12 received batches");
	}

	private boolean matches(String batchNumber, String rowStatus, String rowDate, String searchBatch,
			String selectedStatus, Date selectedDate) {

		boolean batchMatches = searchBatch.isEmpty() || batchNumber.toLowerCase().contains(searchBatch);

		boolean statusMatches = selectedStatus.equals("ALL") || selectedStatus.equals(rowStatus);

		boolean dateMatches = true;

		if (selectedDate != null) {

			SimpleDateFormat format = new SimpleDateFormat("dd-MMM-yyyy");

			String selectedDateString = format.format(selectedDate);

			dateMatches = selectedDateString.equals(rowDate);
		}

		return batchMatches && statusMatches && dateMatches;
	}
}