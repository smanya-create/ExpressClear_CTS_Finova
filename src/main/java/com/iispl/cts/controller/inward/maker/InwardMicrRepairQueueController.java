package com.iispl.cts.controller.inward.maker;

import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Include;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;

import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.service.inward.InwardBatchService;
import com.iispl.cts.serviceimpl.inward.InwardBatchServiceImpl;

public class InwardMicrRepairQueueController extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;

	private Listbox batchQueueList;

	private InwardBatchService inwardBatchService;

	@Override
	public void doAfterCompose(Component comp) throws Exception {
		super.doAfterCompose(comp);

		inwardBatchService = new InwardBatchServiceImpl();

		loadBatchQueue();
	}

	private void loadBatchQueue() {

		List<InwardBatch> batches = inwardBatchService.getBatchesForMicrRepair();

		batchQueueList.setModel(new ListModelList<>(batches));
	}

	public void openBatch(Object batchId) {

		String batchIdValue = String.valueOf(batchId);

		Executions.getCurrent().getDesktop().getSession().setAttribute("MICR_REPAIR_BATCH_ID", batchIdValue);

		Component root = self.getPage().getFirstRoot();

		Component mainContentArea = root.getFellowIfAny("mainContentArea", true);

		if (mainContentArea instanceof Include) {

			Include include = (Include) mainContentArea;

			include.setSrc("/inward/maker/micr-repair/micr-repair.zul");
		}
	}
}