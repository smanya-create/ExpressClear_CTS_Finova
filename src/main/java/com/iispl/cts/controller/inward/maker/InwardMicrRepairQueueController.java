package com.iispl.cts.controller.inward.maker;

import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Include;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zk.ui.Sessions;

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

		if (batchId == null) {
			return;
		}

		String batchIdValue = String.valueOf(batchId).trim();

		if (batchIdValue.isEmpty()) {
			return;
		}

		
		Sessions.getCurrent().setAttribute("MICR_REPAIR_BATCH_ID", batchIdValue);
		
		Include mainInclude = null;

		try {
			mainInclude = (Include) Path.getComponent("/inwardMakerRootWin/mainContentArea");
		} catch (Exception ignored) {
		}

		
		if (mainInclude == null && self != null && self.getDesktop() != null) {

			for (org.zkoss.zk.ui.Page page : self.getDesktop().getPages()) {

				Component component = page.getFellowIfAny("mainContentArea", true);

				if (component instanceof Include) {
					mainInclude = (Include) component;
					break;
				}
			}
		}

		
		if (mainInclude != null) {

			mainInclude.setSrc(null);

			mainInclude.setSrc("/inward/maker/micr-repair/micr-repair.zul");

		} else {

			System.err.println("DEBUG: Failed to locate mainContentArea " + "for MICR Repair navigation!");
		}
	}
}