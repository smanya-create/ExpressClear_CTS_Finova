package com.iispl.cts.controller.inward.maker;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Include;

public class InwardMicrRepairQueueController extends GenericForwardComposer<Component>{

	
	  private static final long serialVersionUID = 1L;

	    public void openBatch(Object batchId) {

	        String batchIdValue = String.valueOf(batchId);

	        Executions.getCurrent().getDesktop().getSession()
	                .setAttribute("MICR_REPAIR_BATCH_ID", batchIdValue);

	        Component root = self.getPage().getFirstRoot();

	        Component mainContentArea = root.getFellowIfAny("mainContentArea", true);

	        if (mainContentArea instanceof Include) {
	            Include include = (Include) mainContentArea;
	            include.setSrc("/inward/maker/micr-repair/micr-cheque-list.zul");
	        }
	    }
}
