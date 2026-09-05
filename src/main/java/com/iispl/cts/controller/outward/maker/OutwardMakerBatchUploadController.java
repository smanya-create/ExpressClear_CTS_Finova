package com.iispl.cts.controller.outward.maker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.List;

import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Div;
import org.zkoss.zul.Groupbox;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.parser.BatchXmlParser;
import com.iispl.cts.service.outward.ScanService;
import com.iispl.cts.serviceimpl.outward.ScanServiceImpl;

public class OutwardMakerBatchUploadController implements Composer<Component> {

	private static final long serialVersionUID = 1L;

	// =========================================================
	// ZUL COMPONENTS
	// =========================================================

	private Intbox txtExpectedTotalCheques;
	private Decimalbox txtExpectedTotalChequeAmount;

	private Textbox txtChequeFolder;
	private Textbox txtBatchNumber;

	private Button btnBrowse;
	private Button btnValidateBatch;

	private Div divSuccessMessage;
	private Label lblSuccessText;

	private Groupbox grpScannedChequesWindow;
	private Listbox lstChequeList;

	private Label lblScannedChequeTitle;
	private Label lblNormalCount;
	private Label lblMicrRepairCount;

	// =========================================================
	// SERVICES
	// =========================================================

	private ScanService scanService;

	// =========================================================
	// UPLOADED ZIP
	// =========================================================

	private File uploadedZipFile;

	// =========================================================
	// BATCH ID
	// =========================================================

	private String batchId;

	// =========================================================
	// COMPOSE
	// =========================================================

	@Override
	public void doAfterCompose(Component component) throws Exception {

		// =====================================================
		// Get ZUL components
		// =====================================================

		txtExpectedTotalCheques = (Intbox) component.getFellow("txtExpectedTotalCheques");

		txtExpectedTotalChequeAmount = (Decimalbox) component.getFellow("txtExpectedTotalChequeAmount");

		txtChequeFolder = (Textbox) component.getFellow("txtChequeFolder");

		txtBatchNumber = (Textbox) component.getFellow("txtBatchNumber");

		btnBrowse = (Button) component.getFellow("btnBrowse");

		btnValidateBatch = (Button) component.getFellow("btnValidateBatch");

		divSuccessMessage = (Div) component.getFellow("divSuccessMessage");

		lblSuccessText = (Label) component.getFellow("lblSuccessText");

		grpScannedChequesWindow = (Groupbox) component.getFellow("grpScannedChequesWindow");

		lstChequeList = (Listbox) component.getFellow("lstChequeList");

		lblScannedChequeTitle = (Label) component.getFellow("lblScannedChequeTitle");

		lblNormalCount = (Label) component.getFellow("lblNormalCount");

		lblMicrRepairCount = (Label) component.getFellow("lblMicrRepairCount");

		// =====================================================
		// Create service
		// =====================================================

		scanService = new ScanServiceImpl();

		// =====================================================
		// Initial page state
		// =====================================================

		txtBatchNumber.setValue("");

		divSuccessMessage.setVisible(false);

		grpScannedChequesWindow.setVisible(false);

		btnValidateBatch.setDisabled(true);

		lblNormalCount.setValue("0 NORMAL");

		lblMicrRepairCount.setValue("0 MICR REPAIR");

		lblScannedChequeTitle.setValue("Scanned Cheques");

		// =====================================================
		// Browse / ZIP upload
		// =====================================================

		btnBrowse.addEventListener("onUpload", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				handleZipUpload((UploadEvent) event);
			}
		});

		// =====================================================
		// Validate Batch button
		// =====================================================

		btnValidateBatch.addEventListener("onClick", new EventListener<Event>() {

			@Override
			public void onEvent(Event event) {

				validateBatch();
			}
		});
	}

	// =========================================================
	// HANDLE ZIP UPLOAD
	// =========================================================

	private void handleZipUpload(UploadEvent uploadEvent) {

		Media media = uploadEvent.getMedia();

		if (media == null) {
			return;
		}

		String fileName = media.getName();

		// =====================================================
		// Check ZIP extension
		// =====================================================

		if (fileName == null || !fileName.toLowerCase().endsWith(".zip")) {

			return;
		}

		// =====================================================
		// Get webapp/TempData path
		// =====================================================

		String tempDataPath = Executions.getCurrent().getDesktop().getWebApp().getRealPath("/TempData");

		if (tempDataPath == null) {
			return;
		}

		File tempDataDirectory = new File(tempDataPath);

		// =====================================================
		// Create TempData folder if required
		// =====================================================

		if (!tempDataDirectory.exists()) {

			if (!tempDataDirectory.mkdirs()) {
				return;
			}
		}

		// =====================================================
		// Destination ZIP
		// =====================================================

		File destinationFile = new File(tempDataDirectory, fileName);

		// =====================================================
		// Save ZIP ONLY
		// =====================================================

		try (InputStream inputStream = media.getStreamData();

				FileOutputStream outputStream = new FileOutputStream(destinationFile)) {

			byte[] buffer = new byte[8192];

			int bytesRead;

			while ((bytesRead = inputStream.read(buffer)) != -1) {

				outputStream.write(buffer, 0, bytesRead);
			}

			outputStream.flush();

		} catch (Exception e) {

			e.printStackTrace();
			return;
		}

		// =====================================================
		// Store uploaded ZIP
		// =====================================================

		uploadedZipFile = destinationFile;

		// =====================================================
		// Display selected file
		// =====================================================

		txtChequeFolder.setValue(fileName);

		// =====================================================
		// Reset previous validation result
		// =====================================================

		batchId = null;

		txtBatchNumber.setValue("");

		divSuccessMessage.setVisible(false);

		grpScannedChequesWindow.setVisible(false);

		lstChequeList.getItems().clear();

		lblNormalCount.setValue("0 NORMAL");

		lblMicrRepairCount.setValue("0 MICR REPAIR");

		lblScannedChequeTitle.setValue("Scanned Cheques");

		// =====================================================
		// Enable Validate Batch
		// =====================================================

		btnValidateBatch.setDisabled(false);

		/*
		 * IMPORTANT:
		 *
		 * Uploading the ZIP does NOT:
		 *
		 * - parse XML - save to database - validate batch
		 *
		 * Everything happens only after Validate Batch is clicked.
		 */
	}

	// =========================================================
	// VALIDATE BATCH
	// =========================================================

	private void validateBatch() {

		// =====================================================
		// Check ZIP
		// =====================================================

		if (uploadedZipFile == null || !uploadedZipFile.exists()) {

			return;
		}

		try {

			// =================================================
			// STEP 1
			// Parse XML
			// =================================================

			BatchXmlParser parser = new BatchXmlParser(scanService);

			/*
			 * Parser:
			 *
			 * ZIP ↓ XML ↓ ScanBatch ↓ ScanCheque ↓ ScanService ↓ scan_batch scan_cheque
			 *
			 * Parser returns ONLY batchId.
			 */

			batchId = parser.parse(uploadedZipFile.getAbsolutePath());

			// =================================================
			// Check returned batch ID
			// =================================================

			if (batchId == null || batchId.trim().isEmpty()) {

				throw new RuntimeException("Batch ID was not returned.");
			}

			// =================================================
			// STEP 2
			// Retrieve ScanBatch from database
			// =================================================

			ScanBatch scanBatch = scanService.getBatchById(batchId);

			if (scanBatch == null) {

				throw new RuntimeException("Batch not found in database: " + batchId);
			}

			// =================================================
			// STEP 3
			// Get ACTUAL values from scan_batch
			// =================================================

			int actualChequeCount = scanBatch.getActualChequeCount();

			BigDecimal actualTotalAmount = scanBatch.getActualTotalAmount();

			// =================================================
			// STEP 4
			// Get EXPECTED values entered by user
			// =================================================

			Integer expectedChequeCount = txtExpectedTotalCheques.getValue();

			BigDecimal expectedTotalAmount = txtExpectedTotalChequeAmount.getValue();

			if (expectedChequeCount == null) {

				throw new RuntimeException("Expected cheque count is required.");
			}

			if (expectedTotalAmount == null) {

				throw new RuntimeException("Expected total amount is required.");
			}

			// =================================================
			// STEP 5
			// Compare expected vs actual
			// =================================================

			boolean chequeCountValid = expectedChequeCount.intValue() == actualChequeCount;

			boolean amountValid = actualTotalAmount != null && expectedTotalAmount.compareTo(actualTotalAmount) == 0;

			// =================================================
			// STEP 6
			// Validation FAILED
			// =================================================

			if (!chequeCountValid || !amountValid) {

				divSuccessMessage.setVisible(false);

				grpScannedChequesWindow.setVisible(false);

				/*
				 * Redirect to validation page.
				 *
				 * Pass the scanned batch ID so that batch-validation.zul can retrieve the
				 * required batch information from scan tables.
				 */

				Executions.sendRedirect("batch-validation.zul?batchId=" + batchId);

				return;
			}

			// =================================================
			// STEP 7
			// Validation PASSED
			// =================================================

			/*
			 * At this point:
			 *
			 * expected count == scan_batch actual count
			 *
			 * expected amount == scan_batch actual amount
			 *
			 * Now retrieve the cheques directly from scan_cheque.
			 */

			List<ScanCheque> scanCheques = scanService.getChequesByBatchId(batchId);

			if (scanCheques == null) {

				throw new RuntimeException("Unable to retrieve scan cheques.");
			}

			// =================================================
			// STEP 8
			// Display scan cheques
			// =================================================

			grpScannedChequesWindow.setVisible(true);

			displayScanCheques(scanCheques);

			// =================================================
			// STEP 9
			// SUCCESS
			// =================================================

			divSuccessMessage.setVisible(true);

			lblSuccessText.setValue("Batch " + batchId + " has been validated successfully.");

		} catch (Exception e) {

			e.printStackTrace();

			divSuccessMessage.setVisible(false);

			grpScannedChequesWindow.setVisible(false);

			// Show user-friendly error message
			lblSuccessText.setValue("Something went wrong while processing the batch. Please try again.");

			batchId = null;

			txtBatchNumber.setValue("");
		}
	}

	// =========================================================
	// DISPLAY SCAN CHEQUES
	// =========================================================

	private void displayScanCheques(List<ScanCheque> scanCheques) {

		System.out.println("Displaying scan cheques for batch: " + batchId);

		// =====================================================
		// Clear previous rows
		// =====================================================

		lstChequeList.getItems().clear();

		int normal = 0;
		int micrRepair = 0;
		int itemNumber = 1;

		// =====================================================
		// Create rows
		// =====================================================

		for (ScanCheque cheque : scanCheques) {

			if (cheque == null) {
				continue;
			}

			String status = cheque.getChequeStatus();

			// =================================================
			// Count statuses
			// =================================================

			if ("MICR_REPAIR_REQUIRED".equalsIgnoreCase(status)) {

				micrRepair++;

			} else {

				normal++;
			}

			// =================================================
			// Create row
			// =================================================

			Listitem item = new Listitem();

			// =================================================
			// ITEM NO.
			// =================================================

			item.appendChild(new Listcell(String.valueOf(itemNumber++)));

			// =================================================
			// PAYEE NAME
			// =================================================

			item.appendChild(new Listcell(cheque.getPayeeName()));

			// =================================================
			// MICR CODE
			// =================================================

			item.appendChild(new Listcell(cheque.getMicrCode()));

			// =================================================
			// STATUS
			// =================================================

			item.appendChild(new Listcell(status));

			// =================================================
			// ACTION
			// =================================================

			Listcell actionCell = new Listcell();

			// =================================================
			// MICR REPAIR
			// =================================================

			if ("MICR_REPAIR_REQUIRED".equalsIgnoreCase(status)) {

				Button micrRepairButton = new Button("MICR Repair");

				micrRepairButton.setSclass("btn-action-repair");

				micrRepairButton.setAttribute("scanCheque", cheque);

				micrRepairButton.addEventListener("onClick", new EventListener<Event>() {

					@Override
					public void onEvent(Event event) {

						/*
						 * MICR repair functionality will be implemented later.
						 */

						System.out.println("MICR Repair clicked for cheque: " + cheque.getScannedChequeId());
					}
				});

				actionCell.appendChild(micrRepairButton);

			}

			// =================================================
			// NORMAL CHEQUE
			// =================================================

			else {

				Button viewButton = new Button("View");

				viewButton.setSclass("btn-action");

				viewButton.setAttribute("scanCheque", cheque);

				viewButton.addEventListener("onClick", new EventListener<Event>() {

					@Override
					public void onEvent(Event event) {

						/*
						 * View functionality will be implemented later.
						 */

						System.out.println("View clicked for cheque: " + cheque.getScannedChequeId());
					}
				});

				actionCell.appendChild(viewButton);
			}

			// =================================================
			// Add ACTION cell
			// =================================================

			item.appendChild(actionCell);

			// =================================================
			// Add row
			// =================================================

			lstChequeList.appendChild(item);
		}

		// =====================================================
		// Update title
		// =====================================================

		lblScannedChequeTitle.setValue("Scanned Cheques (" + scanCheques.size() + ")");

		// =====================================================
		// Update counters
		// =====================================================

		lblNormalCount.setValue(normal + " NORMAL");

		lblMicrRepairCount.setValue(micrRepair + " MICR REPAIR");
	}
}