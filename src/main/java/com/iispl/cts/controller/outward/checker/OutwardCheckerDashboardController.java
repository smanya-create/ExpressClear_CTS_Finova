package com.iispl.cts.controller.outward.checker;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;

public class OutwardCheckerDashboardController extends GenericForwardComposer<Component>{
	 private static final long serialVersionUID = 1L;

	    private Label lblTotalBatches;
	    private Label lblTotalCheques;
	    private Listbox lstBatches;

	    @Override
	    public void doAfterCompose(Component comp) throws Exception {
	        super.doAfterCompose(comp);
	        String role = (String) Sessions.getCurrent().getAttribute("USER_ROLE");

	        Sessions.getCurrent().setAttribute(
	                "USER_ROLE",
	                "OUTWARD_CHECKER"
	            );
	        loadDashboard();
	    }

	    private void loadDashboard() {

	        // For now UI testing
	        int totalBatches = 4;
	        int totalCheques = 120;

	        lblTotalBatches.setValue(String.valueOf(totalBatches));
	        lblTotalCheques.setValue(String.valueOf(totalCheques));
	    }

}
