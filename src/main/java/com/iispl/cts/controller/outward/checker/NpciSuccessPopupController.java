package com.iispl.cts.controller.outward.checker;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Window;

public class NpciSuccessPopupController
        extends GenericForwardComposer<Component> {

    private Label lblBatchId;
    private Label lblFileName;
    private Button btnDone;

    @Override
    public void doAfterCompose(Component component) throws Exception {
        super.doAfterCompose(component);


        btnDone.addEventListener("onClick", event -> {
            ((Window) component).detach();
        });
    }
}
