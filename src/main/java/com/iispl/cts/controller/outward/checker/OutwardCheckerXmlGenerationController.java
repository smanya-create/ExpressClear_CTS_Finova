package com.iispl.cts.controller.outward.checker;

import java.nio.file.Path;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Label;
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
	private Hlayout generatedXmlRow;
	private Hlayout xmlInfoMessage;
	private Label lblXmlFileName;
	private Label lblXmlFileDescription;
	private Button btnSendToNPCI;
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
				generateXmlButton.addEventListener(Events.ON_CLICK, event -> {

					OutwardBatch selectedBatch = (OutwardBatch) generateXmlButton.getAttribute("batch");

					generateXml(selectedBatch);
				});
				actionCell.appendChild(generateXmlButton);
				item.appendChild(actionCell);

			}
		});
	}

	private void generateXml(OutwardBatch batch) {

		try {

			String batchId = batch.getOutwardBatchId();

			List<OutwardCheque> cheques = outwardChequeService.getChequesByBatchId(batchId);

			if (cheques == null || cheques.isEmpty()) {

				Messagebox.show("No cheques found for batch " + batchId, "XML Generation", Messagebox.OK,
						Messagebox.EXCLAMATION);

				return;
			}

			String outputDirectory = "xml-output";

			Path xmlFile = OutwardXmlGenerator.generateXml(batch, cheques, outputDirectory);
			lblXmlFileName.setValue(
			        xmlFile.getFileName().toString());

			lblXmlFileDescription.setValue(
			        "XML generated successfully - Ready to send to NPCI.");

			generatedXmlRow.setVisible(true);

			btnSendToNPCI.setVisible(true);

			xmlInfoMessage.setVisible(false);

			btnSendToNPCI.setAttribute(
			        "xmlFile",
			        xmlFile);
			System.out.println("XML FILE LOCATION: " + xmlFile.toAbsolutePath());

			Messagebox.show(
					"XML generated successfully.\n\n" + "Batch: " + batchId + "\n" + "File: " + xmlFile.getFileName(),
					"XML Generation", Messagebox.OK, Messagebox.INFORMATION);

		} catch (Exception e) {

			e.printStackTrace();

			Messagebox.show("XML generation failed.\n\n" + e.getMessage(), "XML Generation", Messagebox.OK,
					Messagebox.ERROR);
		}
	}
	
	public void onClickSendToNPCI() {

		 Path xmlFile = (Path) btnSendToNPCI.getAttribute("xmlFile");

		    if (xmlFile == null) {
		        Messagebox.show(
		                "Please generate XML first.",
		                "NPCI",
		                Messagebox.OK,
		                Messagebox.EXCLAMATION);
		        return;
		    }

		    Messagebox.show(
		            "XML ready to send to NPCI:\n"
		            + xmlFile.getFileName(),
		            "NPCI",
		            Messagebox.OK,
		            Messagebox.INFORMATION);
	}

}
