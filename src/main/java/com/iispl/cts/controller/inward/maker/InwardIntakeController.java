package com.iispl.cts.controller.inward.maker;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.util.GenericComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

public class InwardIntakeController extends GenericComposer {

	private Window validationSuccessWindow;
	private Window validationFailedWindow;

	private Label statusLabel1;
	private Label statusLabel2;

	private Button validateButton1;
	private Button validateButton2;

	private String selectedBatch;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		validationSuccessWindow = (Window) comp.getFellow("validationSuccessWindow");

		validationFailedWindow = (Window) comp.getFellow("validationFailedWindow");

		validateButton1 = (Button) comp.getFellow("validateButton1");

		validateButton2 = (Button) comp.getFellow("validateButton2");

		Button viewErrorButton = (Button) comp.getFellow("viewErrorButton");

		Button successOkButton = (Button) validationSuccessWindow.getFellow("successOkButton");

		Button backToUploadButton = (Button) validationFailedWindow.getFellow("backToUploadButton");

		statusLabel1 = (Label) comp.getFellow("statusLabel1");

		statusLabel2 = (Label) comp.getFellow("statusLabel2");

		validateButton1.addEventListener("onClick", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				selectedBatch = "1";

				Label batchNumber = (Label) validationSuccessWindow.getFellow("successBatchNumber");

				batchNumber.setValue("IW-20260827-001");

				validationSuccessWindow.doModal();
			}
		});

		validateButton2.addEventListener("onClick", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				selectedBatch = "2";

				Label batchNumber = (Label) validationSuccessWindow.getFellow("successBatchNumber");

				batchNumber.setValue("IW-20260827-002");

				validationSuccessWindow.doModal();
			}
		});

		successOkButton.addEventListener("onClick", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				if ("1".equals(selectedBatch)) {

					statusLabel1.setValue("Validated");
					statusLabel1.setSclass("status-validated");

					validateButton1.setLabel("Open MICR Repair");

					validateButton1.setSclass("repair-button");

					validateButton1.setWidth("135px");

				} else if ("2".equals(selectedBatch)) {

					statusLabel2.setValue("Validated");
					statusLabel2.setSclass("status-validated");

					validateButton2.setLabel("Open MICR Repair");

					validateButton2.setSclass("repair-button");

					validateButton2.setWidth("135px");
				}

				validationSuccessWindow.setVisible(false);
			}
		});

		viewErrorButton.addEventListener("onClick", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				validationFailedWindow.doModal();
			}
		});

		backToUploadButton.addEventListener("onClick", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				validationFailedWindow.setVisible(false);
			}
		});
	}
}