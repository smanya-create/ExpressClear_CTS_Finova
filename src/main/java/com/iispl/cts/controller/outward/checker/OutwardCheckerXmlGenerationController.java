package com.iispl.cts.controller.outward.checker;

import java.io.File;
import java.nio.file.Path;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;

import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.service.outward.OutwardBatchService;
import com.iispl.cts.service.outward.OutwardChequeService;
import com.iispl.cts.serviceimpl.outward.OutwardBatchServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardChequeServiceImpl;

public class OutwardCheckerXmlGenerationController extends GenericForwardComposer<Component> {
	private Listbox lstVerifiedBatches;
	private OutwardBatchService outwardBatchService = new OutwardBatchServiceImpl();
	private OutwardChequeService outwardChequeService = new OutwardChequeServiceImpl();
	@Override
	public void doAfterCompose(Component component) throws Exception {
		super.doAfterCompose(component);
		loadVerifiedBatches();
	}
	
	public void loadVerifiedBatches() {
		List<OutwardBatch> verifiedBatches = outwardBatchService.getVerifiedBatches();
		System.out.println("Verified batches: " + verifiedBatches);
	    System.out.println("Verified batch count: " + verifiedBatches.size());
		ListModelList<OutwardBatch> model = new ListModelList<>(verifiedBatches);
		lstVerifiedBatches.setModel(model);
		lstVerifiedBatches.setItemRenderer(new ListitemRenderer<OutwardBatch>() {

			@Override
			public void render(Listitem item, OutwardBatch verifiedBatch, int index) throws Exception {
				item.appendChild(new Listcell(verifiedBatch.getOutwardBatchId()));
				item.appendChild(new Listcell(String.valueOf(verifiedBatch.getActualChequeCount())));
				item.appendChild(new Listcell(String.valueOf(verifiedBatch.getActualTotalAmount())));
				item.appendChild(new Listcell(verifiedBatch.getBatchStatus()));
				Listcell actionCell = new Listcell();
				Button generateXmlButton = new Button("Generate Xml");
				generateXmlButton.setClass("generate-button");
				
				generateXmlButton.setAttribute("batch", verifiedBatch);
				generateXmlButton.addEventListener(
                        Events.ON_CLICK,
                        event -> {

                            OutwardBatch selectedBatch =
                                (OutwardBatch) generateXmlButton
                                    .getAttribute("batch");

//                            generateXml(
//                                selectedBatch
//                            );
                        }
                    );
				actionCell.appendChild(generateXmlButton);
				item.appendChild(actionCell);
				
				
			}
		});
	}
	

}
