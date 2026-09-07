package com.iispl.cts.controller.outward.maker;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.Clients;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.service.outward.OutwardMakerService;
import com.iispl.cts.serviceimpl.outward.OutwardMakerServiceImpl;

public class OutwardMakerMicrRepairController extends GenericForwardComposer<Window> {

	private static final long serialVersionUID = 1L;

	// ============================================================
	// SERVICE
	// ============================================================

	private final OutwardMakerService outwardMakerService;

	// ============================================================
	// ZUL COMPONENTS
	// ============================================================

	@Wire
	private Window micrEntryWindow;

	@Wire
	private Button btnBackToList;

	@Wire
	private Label lblBatchNumber;

	@Wire
	private Div divCompletionMessage;

	@Wire
	private Label lblCompletionTitle;

	@Wire
	private Label lblCompletionText;

	@Wire
	private Div divFormContainer;

	@Wire
	private Image imgCheque;

	@Wire
	private Label lblChequeImageTitle;

	@Wire
	private Label lblBankName;

	@Wire
	private Label lblBranchName;

	@Wire
	private Label lblPayLabel;

	@Wire
	private Label lblPayTo;

	@Wire
	private Label lblAmountWords;

	@Wire
	private Label lblAmountNumeric;

	@Wire
	private Label lblOcrReference;

	@Wire
	private Label lblChequeNote;

	@Wire
	private Label lblProgressText;

	@Wire
	private Div divProgressFill;

	@Wire
	private Label lblMicrError;

	@Wire
	private Label lblChequeNumber;

	@Wire
	private Textbox txtChequeNumber;

	@Wire
	private Label lblCityCode;

	@Wire
	private Textbox txtCityCode;

	@Wire
	private Label lblBankCode;

	@Wire
	private Textbox txtBankCode;

	@Wire
	private Label lblBranchCode;

	@Wire
	private Textbox txtBranchCode;

	@Wire
	private Label lblCurrentMicr;

	@Wire
	private Textbox txtCurrentMicr;

	@Wire
	private Div divRejectRemarks;

	@Wire
	private Label lblRejectRemarks;

	@Wire
	private Textbox txtRemarks;

	@Wire
	private Button btnSaveNext;

	@Wire
	private Button btnRejectRequest;

	// ============================================================
	// CHEQUE LISTS
	// ============================================================

	private List<ScanCheque> scanChequeList = new ArrayList<>();

	private List<OutwardCheque> outwardChequeList = new ArrayList<>();

	// ============================================================
	// CURRENT SOURCE
	// ============================================================

	/*
	 * SCAN OUTWARD
	 */
	private String source;

	// ============================================================
	// CURRENT BATCH
	// ============================================================

	private String batchId;

	// ============================================================
	// CURRENT CHEQUE INDEX
	// ============================================================

	/*
	 * Java List starts from 0.
	 *
	 * Example:
	 *
	 * currentIndex = 0 -> Record 1 currentIndex = 1 -> Record 2
	 */
	private int currentIndex = 0;

	// ============================================================
	// CONSTRUCTOR
	// ============================================================

	public OutwardMakerMicrRepairController() {

		outwardMakerService = new OutwardMakerServiceImpl();
	}

	// ============================================================
	// AFTER COMPOSE
	// ============================================================

	@Override
	public void doAfterCompose(Window window) throws Exception {

		super.doAfterCompose(window);

		// --------------------------------------------------------
		// Get URL parameters
		// --------------------------------------------------------

		source = Executions.getCurrent().getParameter("source");

		batchId = Executions.getCurrent().getParameter("batchId");
		
		
		System.out.println("======================================");
		System.out.println("MICR REPAIR PARAMETER DEBUG");
		System.out.println("source   = [" + source + "]");
		System.out.println("batchId  = [" + batchId + "]");

		String ampBatchId =
		        Executions.getCurrent().getParameter("amp;batchId");

		System.out.println("amp;batchId = [" + ampBatchId + "]");
		System.out.println("======================================");

		// --------------------------------------------------------
		// Validate source
		// --------------------------------------------------------

		if (source == null || source.trim().isEmpty()) {

			showError("MICR repair source is missing.");

			return;
		}

		source = source.trim().toUpperCase();

		if (!source.equals("SCAN") && !source.equals("OUTWARD")) {

			showError("Invalid MICR repair source.");

			return;
		}

		// --------------------------------------------------------
		// Validate batch ID
		// --------------------------------------------------------

		if (batchId == null || batchId.trim().isEmpty()) {

			showError("Batch ID is missing.");

			return;
		}

		batchId = batchId.trim();

		// --------------------------------------------------------
		// Display batch ID
		// --------------------------------------------------------

		lblBatchNumber.setValue(batchId);

		// --------------------------------------------------------
		// Load MICR repair cheques
		// --------------------------------------------------------

		loadMicrRepairCheques();

		// --------------------------------------------------------
		// Check whether cheques exist
		// --------------------------------------------------------

		if (getTotalCheques() == 0) {

			showError("No MICR repair cheques found for batch " + batchId);

			btnSaveNext.setDisabled(true);

			return;
		}

		// --------------------------------------------------------
		// Start with first cheque
		// --------------------------------------------------------

		currentIndex = 0;

		loadCurrentCheque();
	}

	// ============================================================
	// LOAD MICR REPAIR CHEQUES
	// ============================================================

	private void loadMicrRepairCheques() {

		if ("SCAN".equals(source)) {

			scanChequeList = outwardMakerService.getScanMicrRepairCheques(batchId);

			if (scanChequeList == null) {

				scanChequeList = new ArrayList<>();
			}

		} else {

			outwardChequeList = outwardMakerService.getOutwardMicrRepairCheques(batchId);

			if (outwardChequeList == null) {

				outwardChequeList = new ArrayList<>();
			}
		}
	}

	// ============================================================
	// GET TOTAL CHEQUES
	// ============================================================

	private int getTotalCheques() {

		if ("SCAN".equals(source)) {

			return scanChequeList.size();

		}

		return outwardChequeList.size();
	}

	// ============================================================
	// LOAD CURRENT CHEQUE
	// ============================================================

	private void loadCurrentCheque() {

		int totalCheques = getTotalCheques();

		if (currentIndex < 0 || currentIndex >= totalCheques) {

			return;
		}

		// --------------------------------------------------------
		// Hide reject remarks for every new cheque
		// --------------------------------------------------------

		divRejectRemarks.setVisible(false);

		txtRemarks.setValue("");

		// --------------------------------------------------------
		// Load SCAN cheque
		// --------------------------------------------------------

		if ("SCAN".equals(source)) {

			ScanCheque cheque = scanChequeList.get(currentIndex);

			populateScanCheque(cheque);

		}

		// --------------------------------------------------------
		// Load OUTWARD cheque
		// --------------------------------------------------------

		else {

			OutwardCheque cheque = outwardChequeList.get(currentIndex);

			populateOutwardCheque(cheque);
		}

		// --------------------------------------------------------
		// Update progress
		// --------------------------------------------------------

		updateProgress();
	}

	// ============================================================
	// POPULATE SCAN CHEQUE
	// ============================================================

	private void populateScanCheque(ScanCheque cheque) {

		if (cheque == null) {
			return;
		}

		// --------------------------------------------------------
		// Cheque number
		// --------------------------------------------------------

		txtChequeNumber.setValue(safe(cheque.getChequeNumber()));

		// --------------------------------------------------------
		// City / Bank / Branch
		// --------------------------------------------------------

		txtCityCode.setValue(safe(cheque.getCityCode()));

		txtBankCode.setValue(safe(cheque.getBankCode()));

		txtBranchCode.setValue(safe(cheque.getBranchCode()));

		// --------------------------------------------------------
		// Current MICR
		// --------------------------------------------------------

		txtCurrentMicr.setValue(safe(cheque.getMicrCode()));

		// --------------------------------------------------------
		// Bank / Branch display
		// --------------------------------------------------------

		lblBankName.setValue("Bank Code: " + safe(cheque.getBankCode()));

		lblBranchName.setValue("Branch Code: " + safe(cheque.getBranchCode()));

		// --------------------------------------------------------
		// Payee
		// --------------------------------------------------------

		lblPayTo.setValue(safe(cheque.getPayeeName()));

		// --------------------------------------------------------
		// Amount
		// --------------------------------------------------------

		lblAmountNumeric.setValue(formatAmount(cheque.getChequeAmount()));

		// ScanCheque does not contain amount in words.
		lblAmountWords.setValue("");

		// --------------------------------------------------------
		// OCR reference
		// --------------------------------------------------------

		lblOcrReference.setValue(safe(cheque.getMicrCode()));

		// --------------------------------------------------------
		// FRONT IMAGE
		// --------------------------------------------------------

		loadChequeImage(cheque.getChequeImageFront());
	}

	// ============================================================
	// POPULATE OUTWARD CHEQUE
	// ============================================================

	private void populateOutwardCheque(OutwardCheque cheque) {

		if (cheque == null) {
			return;
		}

		// --------------------------------------------------------
		// Cheque number
		// --------------------------------------------------------

		txtChequeNumber.setValue(safe(cheque.getChequeNumber()));

		// --------------------------------------------------------
		// City / Bank / Branch
		// --------------------------------------------------------

		txtCityCode.setValue(safe(cheque.getCityCode()));

		txtBankCode.setValue(safe(cheque.getBankCode()));

		txtBranchCode.setValue(safe(cheque.getBranchCode()));

		// --------------------------------------------------------
		// Current MICR
		// --------------------------------------------------------

		txtCurrentMicr.setValue(safe(cheque.getMicrCode()));

		// --------------------------------------------------------
		// Bank / Branch display
		// --------------------------------------------------------

		lblBankName.setValue("Bank Code: " + safe(cheque.getBankCode()));

		lblBranchName.setValue("Branch Code: " + safe(cheque.getBranchCode()));

		// --------------------------------------------------------
		// Payee
		// --------------------------------------------------------

		lblPayTo.setValue(safe(cheque.getPayeeName()));

		// --------------------------------------------------------
		// Amount
		// --------------------------------------------------------

		lblAmountNumeric.setValue(formatAmount(cheque.getChequeAmount()));

		lblAmountWords.setValue("");

		// --------------------------------------------------------
		// OCR reference
		// --------------------------------------------------------

		lblOcrReference.setValue(safe(cheque.getMicrCode()));

		/*
		 * Do NOT load an outward image here.
		 *
		 * Your current confirmed OutwardCheque entity does not have a chequeImageFront
		 * field.
		 */
	}

	// ============================================================
	// LOAD CHEQUE IMAGE
	// ============================================================

	private void loadChequeImage(String imagePath) {

		if (imagePath == null || imagePath.trim().isEmpty()) {

			imgCheque.setSrc("");

			return;
		}

		imagePath = imagePath.trim();

		if (!imagePath.startsWith("/")) {

			imagePath = "/" + imagePath;
		}

		imgCheque.setSrc(imagePath);
	}

	// ============================================================
	// UPDATE PROGRESS
	// ============================================================

	private void updateProgress() {

		int totalCheques = getTotalCheques();

		if (totalCheques <= 0) {

			lblProgressText.setValue("Record 0 of 0");

			divProgressFill.setStyle("width: 0%;");

			return;
		}

		int recordNumber = currentIndex + 1;

		// --------------------------------------------------------
		// Record X of N
		// --------------------------------------------------------

		lblProgressText.setValue("Record " + recordNumber + " of " + totalCheques);

		// --------------------------------------------------------
		// Progress percentage
		// --------------------------------------------------------

		double percentage = ((double) recordNumber / totalCheques) * 100.0;

		divProgressFill.setStyle("width: " + percentage + "%;");
	}

	// ============================================================
	// SAVE AND NEXT
	// ============================================================

	@Listen("onClick = #btnSaveNext")
	public void saveAndNext() {

		int totalCheques = getTotalCheques();

		// --------------------------------------------------------
		// Validate current cheque
		// --------------------------------------------------------

		if (currentIndex < 0 || currentIndex >= totalCheques) {

			showError("Invalid cheque selection.");

			return;
		}

		// --------------------------------------------------------
		// Validate MICR fields
		// --------------------------------------------------------

		if (!validateMicrFields()) {

			return;
		}

		try {

			// ====================================================
			// SCAN
			// ====================================================

			if ("SCAN".equals(source)) {

				ScanCheque cheque = scanChequeList.get(currentIndex);

				updateScanChequeFromScreen(cheque);

				/*
				 * Existing service method.
				 *
				 * ServiceImpl changes will be done later.
				 */
				outwardMakerService.saveScanMicrRepair(cheque);
			}

			// ====================================================
			// OUTWARD
			// ====================================================

			else {

				OutwardCheque cheque = outwardChequeList.get(currentIndex);

				updateOutwardChequeFromScreen(cheque);

				/*
				 * Existing service method.
				 *
				 * ServiceImpl changes will be done later.
				 */
				outwardMakerService.saveOutwardMicrRepair(cheque);
			}

			// ====================================================
			// LAST CHEQUE
			// ====================================================

			if (currentIndex == totalCheques - 1) {

				/*
				 * Current cheque has successfully been saved.
				 *
				 * Batch status update will be added later in ServiceImpl.
				 *
				 * For now redirect to MICR Repair View.
				 */

				Clients.showNotification("MICR repair completed successfully.", Clients.NOTIFICATION_TYPE_INFO, null,
						"top_center", 2000);

				redirectToMicrRepairView();

				return;
			}

			// ====================================================
			// NEXT CHEQUE
			// ====================================================

			currentIndex++;

			loadCurrentCheque();

			Clients.showNotification("MICR repair saved successfully.", Clients.NOTIFICATION_TYPE_INFO, null,
					"top_center", 1500);

		} catch (Exception e) {

			e.printStackTrace();

			Clients.showNotification("Failed to save MICR repair: " + safe(e.getMessage()),
					Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 4000);
		}
	}

	// ============================================================
	// UPDATE SCAN CHEQUE
	// ============================================================

	private void updateScanChequeFromScreen(ScanCheque cheque) {

		cheque.setCityCode(txtCityCode.getValue().trim());

		cheque.setBankCode(txtBankCode.getValue().trim());

		cheque.setBranchCode(txtBranchCode.getValue().trim());
	}

	// ============================================================
	// UPDATE OUTWARD CHEQUE
	// ============================================================

	private void updateOutwardChequeFromScreen(OutwardCheque cheque) {

		cheque.setCityCode(txtCityCode.getValue().trim());

		cheque.setBankCode(txtBankCode.getValue().trim());

		cheque.setBranchCode(txtBranchCode.getValue().trim());
	}

	// ============================================================
	// VALIDATE MICR FIELDS
	// ============================================================

	private boolean validateMicrFields() {

		String city = txtCityCode.getValue();

		String bank = txtBankCode.getValue();

		String branch = txtBranchCode.getValue();

		// --------------------------------------------------------
		// City
		// --------------------------------------------------------

		if (city == null || city.trim().isEmpty()) {

			showWarning("City Code is required.");

			txtCityCode.setFocus(true);
			return false;
		}

		// --------------------------------------------------------
		// Bank
		// --------------------------------------------------------

		if (bank == null || bank.trim().isEmpty()) {

			showWarning("Bank Code is required.");

			txtBankCode.setFocus(true);

			return false;
		}

		// --------------------------------------------------------
		// Branch
		// --------------------------------------------------------

		if (branch == null || branch.trim().isEmpty()) {

			showWarning("Branch Code is required.");

			txtBranchCode.setFocus(true);

			return false;
		}

		return true;
	}

	// ============================================================
	// REDIRECT TO MICR REPAIR VIEW
	// ============================================================

	private void redirectToMicrRepairView() {

		Executions.sendRedirect("micr-repair-view.zul");
	}

	// ============================================================
	// BACK TO LIST
	// ============================================================

	@Listen("onClick = #btnBackToList")
	public void backToList() {

		redirectToMicrRepairView();
	}

	// ============================================================
	// REJECT REQUEST
	// ============================================================

	@Listen("onClick = #btnRejectRequest")
	public void rejectRequest() {

		/*
		 * Reject Request will be implemented later.
		 *
		 * No database operation is performed now.
		 */
	}

	// ============================================================
	// COMPLETION MESSAGE
	// ============================================================

	private void showCompletionMessage() {

		divCompletionMessage.setVisible(true);

		lblCompletionText.setValue("All MICR-error cheques in batch " + batchId + " have been repaired.");
	}

	// ============================================================
	// FORMAT AMOUNT
	// ============================================================

	private String formatAmount(BigDecimal amount) {

		if (amount == null) {

			return "";
		}

		DecimalFormat decimalFormat = new DecimalFormat("#,##0.00");

		return "₹ " + decimalFormat.format(amount);
	}

	// ============================================================
	// SAFE STRING
	// ============================================================

	private String safe(String value) {

		if (value == null) {

			return "";
		}

		return value;
	}

	// ============================================================
	// ERROR NOTIFICATION
	// ============================================================

	private void showError(String message) {

		Clients.showNotification(message, Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 4000);
	}

	// ============================================================
	// WARNING NOTIFICATION
	// ============================================================

	private void showWarning(String message) {

		Clients.showNotification(message, Clients.NOTIFICATION_TYPE_WARNING, null, "top_center", 3000);
	}
}