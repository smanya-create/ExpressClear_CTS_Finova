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
    	 System.out.println("========== CHECKER QUEUE STARTED ==========");
        super.doAfterCompose(comp);

        Map<?, ?> args = Executions.getCurrent().getArg();

        Object batchIdObj = args.get("batchId");

        if (batchIdObj != null) {
            batchId = batchIdObj.toString();
        }

        System.out.println("Popup received batchId = " + batchId);

        if (lblPopupBatchId != null) {
            lblPopupBatchId.setValue(
                batchId != null ? batchId : "-"
            );
        }
        
        System.out.println("Queue controller created");
    }

    public void onClick$btnProceed() {

    	 System.out.println("========== CHECKER QUEUE STARTED ==========");
        System.out.println("PROCEED clicked");
        System.out.println("Selected batchId = " + batchId);

        if (batchId == null || batchId.trim().isEmpty()) {
            System.out.println("ERROR: batchId is null or empty");
            return;
        }

        Sessions.getCurrent().setAttribute(
            "SELECTED_OUTWARD_BATCH_ID",
            batchId.trim()
        );

        System.out.println(
            "Session batchId = "
            + Sessions.getCurrent().getAttribute(
                "SELECTED_OUTWARD_BATCH_ID"
            )
        );

        
        Executions.sendRedirect( "/outward/checker/checker-queue.zul");
        
        System.out.println("========== REDIRECT CALLED ==========");
    }
    public void onClick$btnCancel() {

        batchProceedPopup.detach();
    }
}