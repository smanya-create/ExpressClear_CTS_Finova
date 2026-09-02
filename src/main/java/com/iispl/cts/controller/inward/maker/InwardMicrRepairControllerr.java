package com.iispl.cts.controller.inward.maker;

import java.util.List;

import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.service.inward.InwardChequeService;
import com.iispl.cts.serviceimpl.inward.InwardChequeServiceImpl;
import com.iispl.cts.entity.inward.InwardChequeImage;
import com.iispl.cts.validator.MICRValidator;
import com.iispl.cts.validatorimpl.MICRValidatorImpl;
import com.iispl.cts.entity.RejectedResaon;
import com.iispl.cts.service.RejectedReasonService;
import com.iispl.cts.serviceimpl.RejectedReasonServiceImpl;

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

	private MICRValidator micrValidator;

	private List<InwardCheque> repairCheques;

	private RejectedReasonService rejectedReasonService;

	private List<RejectedResaon> rejectedReasons;

	private Combobox cmbRejectReason;

	private InwardCheque currentCheque;

	@Override
	public void doAfterCompose(Component comp) throws Exception {

		super.doAfterCompose(comp);

		inwardChequeService = new InwardChequeServiceImpl();

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

		for (RejectedResaon reason : rejectedReasons) {
			Comboitem item = new Comboitem();

			item.setLabel(reason.getRejectedReasonCode() + " - " + reason.getRejectedReasonName());

			item.setValue(reason.getRejectedReasonId());

			cmbRejectReason.appendChild(item);
		}
	}

	private void loadRepairRecord() {

		try {

			// Get cheques waiting for MICR repair
			repairCheques = inwardChequeService.getMicrRepairRequiredCheques();

			if (repairCheques == null || repairCheques.isEmpty()) {

				totalRecords = 0;
				currentRecord = 0;
				currentCheque = null;

				clearRecordFields();
				updateNavigation();
				loadChequeImage(null);

				return;
			}

			// Total MICR repair cheques
			totalRecords = repairCheques.size();

			if (currentRecord < 0) {
				currentRecord = 0;
			}

			if (currentRecord >= totalRecords) {
				currentRecord = totalRecords - 1;
			}

			// Get the current cheque
			currentCheque = repairCheques.get(currentRecord);

			// Populate UI fields
			populateChequeFields(currentCheque);

			// Update Previous / Next buttons
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

			if (image != null && image.getImagePath() != null && !image.getImagePath().trim().isEmpty()) {

				if (chequeImage != null) {
					chequeImage.setSrc(image.getImagePath());
					chequeImage.setVisible(true);
				}

				if (emptyImageState != null) {
					emptyImageState.setVisible(false);
				}
			}

		} catch (Exception e) {
			e.printStackTrace();
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

		repairCheques = inwardChequeService.getMicrRepairRequiredCheques();

		totalRecords = repairCheques != null ? repairCheques.size() : 0;

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

		// Cheque number
		if (txtChequeNumber != null) {
			txtChequeNumber.setValue(cheque.getChequeNumber() != null ? cheque.getChequeNumber() : "");
		}

		// Current MICR
		if (txtCurrentMicr != null) {
			txtCurrentMicr.setValue(cheque.getMicrCode() != null ? cheque.getMicrCode() : "");
		}

		// Corrected MICR starts empty for the maker
		if (txtCorrectedMicr != null) {
			txtCorrectedMicr.setValue("");
		}

		// Remarks start empty
		if (txtRemarks != null) {
			txtRemarks.setValue("");
		}
	}
}