package com.iispl.cts.controller.inward.maker;

import java.util.ArrayList;

import java.io.InputStream;

import org.zkoss.image.AImage;

import java.util.HashMap;

import java.util.List;
import java.util.Map;

import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.service.inward.InwardChequeService;
import com.iispl.cts.serviceimpl.inward.InwardChequeServiceImpl;
import com.iispl.cts.entity.inward.InwardChequeImage;
import com.iispl.cts.validator.MICRValidator;
import com.iispl.cts.validatorimpl.MICRValidatorImpl;
import com.iispl.cts.entity.RejectedReason;
import com.iispl.cts.service.RejectedReasonService;
import com.iispl.cts.serviceimpl.RejectedReasonServiceImpl;
import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.service.inward.InwardBatchService;
import com.iispl.cts.serviceimpl.inward.InwardBatchServiceImpl;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Combobox;

public class InwardMicrRepairControllerr extends GenericForwardComposer<Component> {

	private static final long serialVersionUID = 1L;

	private Label lblBatchId;
	private Label lblRecordPosition;
	private Label lblProgress;
	private Label lblRepairStatus;

	private Window rejectRequestWindow;
	private Image chequeImage;
	private Groupbox emptyImageState;

	private Textbox txtChequeNumber;
	private Textbox txtCityCode;
	private Textbox txtBankCode;
	private Textbox txtBranchCode;
	private Textbox txtCurrentMicr;
	private Textbox txtTransactionCode;
	private Textbox txtCorrectedMicr;
	private Textbox txtRemarks;

	private Progressmeter progressMeter;

	private int currentRecord = 0;
	private int totalRecords = 0;

	private InwardChequeService inwardChequeService;

	private InwardBatchService inwardBatchService;

	private Label lblBatchSource;
	private Label lblTotalCheques;
	private Label lblHeaderChequeNo;
	private Label lblHeaderItemStatus;
	private Label lblReceivedDate;

	private MICRValidator micrValidator;

	private List<InwardCheque> repairCheques;

	private RejectedReasonService rejectedReasonService;

	private List<RejectedReason> rejectedReasons;

	private Combobox cmbRejectReason;

	private InwardCheque currentCheque;

	@Override
	public void doAfterCompose(Component comp) throws Exception {

		super.doAfterCompose(comp);

		inwardChequeService = new InwardChequeServiceImpl();

		inwardBatchService = new InwardBatchServiceImpl();

		rejectedReasonService = RejectedReasonServiceImpl.getInstance();

		micrValidator = new MICRValidatorImpl();

		loadRejectedReasons();
		loadRepairRecord();
	}

	private void loadRejectedReasons() {
		rejectedReasons = rejectedReasonService.getAllRejectedReasons();

		if (cmbRejectReason == null) {
			return;
		}

		cmbRejectReason.getItems().clear();

		if (rejectedReasons == null) {
			return;
		}

		for (RejectedReason reason : rejectedReasons) {
			Comboitem item = new Comboitem();

			item.setLabel(reason.getRejectedReasonCode() + " - " + reason.getRejectedReasonName());

			item.setValue(reason.getRejectedReasonId());

			cmbRejectReason.appendChild(item);
		}
	}

	private void loadRepairRecord() {

		try {

			String batchId = Executions.getCurrent().getParameter("batchId");

			if (batchId == null || batchId.trim().isEmpty()) {

				Object sessionBatchId = Executions.getCurrent().getSession().getAttribute("MICR_REPAIR_BATCH_ID");

				if (sessionBatchId != null) {
					batchId = String.valueOf(sessionBatchId);
				}
			}

			if (batchId == null || batchId.trim().isEmpty()) {

				Messagebox.show("No MICR repair batch was selected.", "MICR Repair", Messagebox.OK,
						Messagebox.EXCLAMATION);

				return;
			}

			batchId = batchId.trim();

			List<InwardCheque> batchCheques = inwardChequeService.getChequesByBatchAndStatus(batchId, null);

			repairCheques = new java.util.ArrayList<>();

			if (batchCheques != null) {

				for (InwardCheque cheque : batchCheques) {

					if (cheque == null) {
						continue;
					}

					String status = cheque.getChequeStatus();

					if ("MICR_REPAIR_PENDING".equalsIgnoreCase(status)
							|| "MICR_REPAIR_IN_PROGRESS".equalsIgnoreCase(status)
							|| "MICR_REPAIR_REQUIRED".equalsIgnoreCase(status)) {

						repairCheques.add(cheque);
					}
				}
			}

			if (repairCheques.isEmpty()) {

				totalRecords = 0;
				currentRecord = 0;
				currentCheque = null;

				clearRecordFields();
				updateNavigation();
				loadChequeImage(null);

				return;
			}

			totalRecords = repairCheques.size();

			if (currentRecord < 0) {
				currentRecord = 0;
			}

			if (currentRecord >= totalRecords) {
				currentRecord = totalRecords - 1;
			}

			currentCheque = repairCheques.get(currentRecord);

			loadBatchSummary(currentCheque);

			populateChequeFields(currentCheque);

			updateNavigation();

			loadChequeImage(currentCheque.getInwardChequeId());

		} catch (Exception e) {

			e.printStackTrace();

			totalRecords = 0;
			currentRecord = 0;
			currentCheque = null;

			clearRecordFields();
			updateNavigation();
			loadChequeImage(null);

			Messagebox.show("Unable to load MICR repair records.", "Error", Messagebox.OK, Messagebox.ERROR);
		}
	}

	private void loadBatchSummary(InwardCheque cheque) {

		if (cheque == null) {
			return;
		}

		InwardBatch batch = inwardBatchService.getBatchById(cheque.getInwardBatchId());

		if (batch == null) {
			return;
		}

		if (lblBatchId != null) {
			lblBatchId.setValue(String.valueOf(batch.getInwardBatchId()));
		}

		if (lblBatchSource != null) {
			lblBatchSource.setValue("CHI");
		}

		if (lblTotalCheques != null) {
			lblTotalCheques.setValue(String.valueOf(batch.getActualChequeCount()));
		}

		if (lblHeaderChequeNo != null) {
			lblHeaderChequeNo.setValue(cheque.getChequeNumber());
		}

		if (lblHeaderItemStatus != null) {
			lblHeaderItemStatus.setValue(cheque.getChequeStatus());
		}

		if (lblReceivedDate != null && batch.getUploadedAt() != null) {
			lblReceivedDate.setValue(new java.text.SimpleDateFormat("dd-MM-yyyy").format(batch.getUploadedAt()));
		}
	}

	private void clearRecordFields() {

		if (lblBatchId != null) {
			lblBatchId.setValue("BATCH: —");
		}

		if (txtChequeNumber != null) {
			txtChequeNumber.setValue("");
		}

		if (txtCityCode != null) {
			txtCityCode.setValue("");
		}

		if (txtBankCode != null) {
			txtBankCode.setValue("");
		}

		if (txtBranchCode != null) {
			txtBranchCode.setValue("");
		}

		if (txtCurrentMicr != null) {
			txtCurrentMicr.setValue("");
		}

		if (txtTransactionCode != null) {
			txtTransactionCode.setValue("");
		}

		if (txtCorrectedMicr != null) {
			txtCorrectedMicr.setValue("");
		}

		if (txtRemarks != null) {
			txtRemarks.setValue("");
		}

		if (lblRepairStatus != null) {
			lblRepairStatus.setValue("MICR ERROR");
		}
	}

	private void updateNavigation() {

		if (lblRecordPosition != null) {

			if (totalRecords == 0) {
				lblRecordPosition.setValue("No records");
			} else {
				lblRecordPosition.setValue("Record " + (currentRecord + 1) + " of " + totalRecords);
			}
		}

		if (lblProgress != null) {
			lblProgress.setValue((totalRecords == 0 ? 0 : currentRecord + 1) + " / " + totalRecords);
		}

		if (progressMeter != null) {

			int progress = totalRecords == 0 ? 0 : ((currentRecord + 1) * 100) / totalRecords;

			progressMeter.setValue(progress);
		}
	}

	private void loadChequeImage(String inwardChequeId) {

		if (chequeImage != null) {
			chequeImage.setVisible(false);
			chequeImage.setSrc(null);
		}

		if (emptyImageState != null) {
			emptyImageState.setVisible(true);
		}

		if (inwardChequeId == null || inwardChequeId.trim().isEmpty()) {
			return;
		}

		try {

			InwardChequeImage image = inwardChequeService.getFrontImage(inwardChequeId);

			if (image == null || image.getImagePath() == null || image.getImagePath().trim().isEmpty()) {

				System.out.println("MICR Repair: No front image found for cheque -> " + inwardChequeId);

				return;
			}

			String imagePath = image.getImagePath().trim();

			/*
			 * Images are stored under:
			 *
			 * src/main/resources/Inward-data/
			 *
			 * Eclipse deploys src/main/resources to the web application root.
			 *
			 * Therefore the browser URL is:
			 *
			 * /Inward-data/<stored image path>
			 *
			 * Example: /Inward-data/BATCH-2026-09-04-003/images/CHQ001_front.jpg
			 */
			String imageSrc = "/Inward-data/" + imagePath;

			System.out.println("MICR Repair: Loading image URL -> " + imageSrc);

			chequeImage.setSrc(imageSrc);
			chequeImage.setVisible(true);

			if (emptyImageState != null) {
				emptyImageState.setVisible(false);
			}

		} catch (Exception e) {

			System.err.println("MICR Repair: Failed to load image for cheque -> " + inwardChequeId);

			e.printStackTrace();

			if (chequeImage != null) {
				chequeImage.setVisible(false);
			}

			if (emptyImageState != null) {
				emptyImageState.setVisible(true);
			}
		}
	}

	public void onClick$btnPrevious() {

		if (totalRecords == 0 || currentRecord <= 0) {
			return;
		}

		currentRecord--;
		loadRepairRecord();
	}

	public void onClick$btnNext() {

		if (totalRecords == 0 || currentRecord >= totalRecords - 1) {
			return;
		}

		currentRecord++;
		loadRepairRecord();
	}

	public void onClick$btnSaveAndNext() {

		if (totalRecords == 0 || currentCheque == null) {

			Messagebox.show("No MICR repair record is available.", "MICR Repair", Messagebox.OK,
					Messagebox.INFORMATION);

			return;
		}

		String correctedMicr = txtCorrectedMicr != null ? txtCorrectedMicr.getValue() : "";

		if (correctedMicr == null || correctedMicr.trim().isEmpty()) {

			if (txtCorrectedMicr != null) {
				txtCorrectedMicr.setErrorMessage("Corrected MICR code is required.");
			}

			return;
		}

		correctedMicr = correctedMicr.trim();

		if (!micrValidator.isValid(correctedMicr)) {

			if (txtCorrectedMicr != null) {
				txtCorrectedMicr.setErrorMessage("MICR code must contain exactly 9 digits.");
			}

			return;
		}

		boolean updated = inwardChequeService.updateMicrRepair(currentCheque.getInwardChequeId(), correctedMicr,
				"PENDING_DATA_ENTRY");

		if (!updated) {

			Messagebox.show("Unable to save MICR correction.", "MICR Repair", Messagebox.OK, Messagebox.ERROR);

			return;
		}

		Messagebox.show("MICR correction saved successfully.", "MICR Repair", Messagebox.OK, Messagebox.INFORMATION);

		String batchId = (String) Executions.getCurrent().getSession().getAttribute("MICR_REPAIR_BATCH_ID");

		List<InwardCheque> batchCheques = inwardChequeService.getChequesByBatchAndStatus(batchId, null);

		repairCheques = new java.util.ArrayList<>();

		if (batchCheques != null) {

			for (InwardCheque cheque : batchCheques) {

				if (cheque == null) {
					continue;
				}

				String status = cheque.getChequeStatus();

				if ("MICR_REPAIR_PENDING".equalsIgnoreCase(status) || "MICR_REPAIR_IN_PROGRESS".equalsIgnoreCase(status)
						|| "MICR_REPAIR_REQUIRED".equalsIgnoreCase(status)) {

					repairCheques.add(cheque);
				}
			}
		}

		totalRecords = repairCheques.size();

		if (totalRecords == 0) {

			currentRecord = 0;
			currentCheque = null;

			clearRecordFields();
			updateNavigation();
			loadChequeImage(null);

			return;
		}

		if (currentRecord >= totalRecords) {
			currentRecord = totalRecords - 1;
		}

		loadRepairRecord();
	}

	public void onClick$btnRejectRequest() {
		if (currentCheque == null) {
			Messagebox.show("No cheque is selected.", "Reject Request", Messagebox.OK, Messagebox.INFORMATION);
			return;
		}

		if (rejectRequestWindow != null) {
			rejectRequestWindow.doModal();
		}
	}

	public void onClick$btnCancelReject() {
		if (rejectRequestWindow != null) {
			rejectRequestWindow.setVisible(false);
		}
	}

	public void onClick$btnConfirmReject() {

		if (currentCheque == null) {
			return;
		}

		if (cmbRejectReason == null || cmbRejectReason.getSelectedItem() == null) {
			Messagebox.show("Please select a rejection reason.", "Reject Request", Messagebox.OK,
					Messagebox.EXCLAMATION);
			return;
		}

		Comboitem selectedItem = cmbRejectReason.getSelectedItem();

		String rejectedReasonId = String.valueOf(selectedItem.getValue());

		if (rejectedReasonId == null || rejectedReasonId.trim().isEmpty()) {
			Messagebox.show("Invalid rejection reason.", "Reject Request", Messagebox.OK, Messagebox.EXCLAMATION);
			return;
		}

		Messagebox.show("Reject request submitted successfully.", "Reject Request", Messagebox.OK,
				Messagebox.INFORMATION);

		if (rejectRequestWindow != null) {
			rejectRequestWindow.setVisible(false);
		}
	}

	public void onClick$btnBackToList() {

		Executions.sendRedirect("/inward/maker/index.zul");
	}

	private void populateChequeFields(InwardCheque cheque) {

		if (cheque == null) {
			clearRecordFields();
			return;
		}

		if (txtChequeNumber != null) {
			txtChequeNumber.setValue(cheque.getChequeNumber() != null ? cheque.getChequeNumber() : "");
		}

		if (txtCityCode != null) {
			txtCityCode.setValue(cheque.getCityCode() != null ? cheque.getCityCode() : "");
		}

		if (txtBankCode != null) {
			txtBankCode.setValue(cheque.getBankCode() != null ? cheque.getBankCode() : "");
		}

		if (txtBranchCode != null) {
			txtBranchCode.setValue(cheque.getBranchCode() != null ? cheque.getBranchCode() : "");
		}

		if (txtCurrentMicr != null) {
			txtCurrentMicr.setValue(cheque.getMicrCode() != null ? cheque.getMicrCode() : "");
		}

		if (txtTransactionCode != null) {
			txtTransactionCode.setValue(cheque.getTransactionCode() != null ? cheque.getTransactionCode() : "");
		}

		if (txtCorrectedMicr != null) {
			txtCorrectedMicr.setValue("");
			txtCorrectedMicr.clearErrorMessage();
		}

		if (txtRemarks != null) {
			txtRemarks.setValue("");
		}
	}
}