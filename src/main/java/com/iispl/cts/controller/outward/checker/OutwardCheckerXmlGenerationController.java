package com.iispl.cts.controller.outward.checker;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;

import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.enums.OutwardBatchStatus;
import com.iispl.cts.service.outward.OutwardBatchService;
import com.iispl.cts.service.outward.OutwardChequeService;
import com.iispl.cts.serviceimpl.outward.OutwardBatchServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardChequeServiceImpl;

public class OutwardCheckerXmlGenerationController extends GenericForwardComposer<Component> {

	private Listbox lstVerifiedBatches;
	private Label lblVerifiedBatches;

	private OutwardBatchService outwardBatchService = new OutwardBatchServiceImpl();
	private OutwardChequeService outwardChequeService = new OutwardChequeServiceImpl();

	@Override
	public void doAfterCompose(Component component) throws Exception {

		super.doAfterCompose(component);

		loadVerifiedBatches();
	}

	//Loading verified batches
	public void loadVerifiedBatches() {

		List<OutwardBatch> verifiedBatches = outwardBatchService.getVerifiedBatches();

		verifiedBatches.removeIf(batch -> OutwardBatchStatus.COMPLETED.toString().equalsIgnoreCase(batch.getBatchStatus()));
		lblVerifiedBatches.setValue(String.valueOf(verifiedBatches.size()));
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

				Button generateXmlButton = new Button("Generate bxf");

				generateXmlButton.setClass("generate-button");
				generateXmlButton.setAttribute("batch", verifiedBatch);
				generateXmlButton.addEventListener(Events.ON_CLICK, event -> generateXml(verifiedBatch));

				actionCell.appendChild(generateXmlButton);

				item.appendChild(actionCell);
			}
		});
	}

	// Generates the XML file for the selected batch, updates its status to COMPLETED -> refreshes the list -> and downloads the generated file.
	private void generateXml(OutwardBatch batch) {
		try {
			String batchId = batch.getOutwardBatchId();
			List<OutwardCheque> cheques = outwardChequeService.getChequesByBatchId(batchId);
			if (cheques == null || cheques.isEmpty()) {
				Messagebox.show("No cheques found for batch " + batchId, "XML Generation", Messagebox.OK,Messagebox.EXCLAMATION);
				return;
			}

			String outputDirectory = Paths.get(System.getProperty("user.home"), "Downloads/xml-output").toString();
			Path xmlFile = OutwardXmlGenerator.generateXml(batch, cheques, outputDirectory);
			if (xmlFile == null) {
				Messagebox.show("XML file was not generated.", "XML Generation", Messagebox.OK, Messagebox.ERROR);
				return;
			}
			outwardBatchService.updateBatchStatus(batchId, OutwardBatchStatus.COMPLETED.toString());
			loadVerifiedBatches();
			Filedownload.save(xmlFile.toFile(), "application/xml");
		} catch (Exception e) {
			e.printStackTrace();
			Messagebox.show("XML generation failed.\n\n" + e.getMessage(), "XML Generation", Messagebox.OK,
					Messagebox.ERROR);
		}
	}
}