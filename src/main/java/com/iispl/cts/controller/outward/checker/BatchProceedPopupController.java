package com.iispl.cts.controller.outward.checker;

import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

public class BatchProceedPopupController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;

	private Label lblPopupBatchId;
	private Button btnProceed;
	private Button btnCancel;
	private Window batchProceedPopup;

	private String batchId;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		Map<?, ?> args = Executions.getCurrent().getArg();
		Object batchIdObj = args.get("batchId");

		if (batchIdObj != null) {
			batchId = batchIdObj.toString();
		}

		System.out.println("Popup received batchId = " + batchId);

		if (lblPopupBatchId != null) {
			lblPopupBatchId.setValue(batchId != null ? batchId : "-");
		}
		System.out.println("Queue controller created");
	}

	public void onClick$btnProceed() {

		if (batchId == null || batchId.trim().isEmpty()) {
			return;
		}
		Sessions.getCurrent().setAttribute("SELECTED_OUTWARD_BATCH_ID", batchId.trim());
		Executions.sendRedirect("/outward/checker/checker-queue.zul");
	}

	public void onClick$btnCancel() {
		batchProceedPopup.detach();
	}
}