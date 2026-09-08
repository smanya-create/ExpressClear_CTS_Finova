package com.iispl.cts.controller.outward.maker;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.iispl.cts.entity.RejectedReason;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.service.RejectedReasonService;
import com.iispl.cts.service.outward.OutwardBatchService;
import com.iispl.cts.service.outward.OutwardChequeService;
import com.iispl.cts.service.outward.ScanService;
import com.iispl.cts.serviceimpl.RejectedReasonServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardBatchServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardChequeServiceImpl;
import com.iispl.cts.serviceimpl.outward.ScanServiceImpl;

public class OutwardChequeDataEntryController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 1L;

	private Label outwardChequeDataEntryLblBatchId;
	private Label outwardChequeDataEntryLblTotal;
	private Label outwardChequeDataEntryLblEntered;
	private Label outwardChequeDataEntryLblRemaining;
	private Label outwardChequeDataEntryLblRecord;
	private Label outwardChequeDataEntryLblValidationStatus;
	private Label outwardChequeDataEntryLblNoImage;
	private Label outwardChequeDataEntryLblAccountValidation;

	private Textbox outwardChequeDataEntryTxtChequeNumber;
	private Textbox outwardChequeDataEntryTxtPayeeAccount;
	private Textbox outwardChequeDataEntryTxtAmount;
	private Textbox outwardChequeDataEntryTxtChequeDate;
	private Textbox outwardChequeDataEntryTxtMicrCode;
	private Textbox outwardChequeDataEntryTxtDraweeName;
	private Textbox outwardChequeDataEntryTxtPayeeName;

	private Image outwardChequeDataEntryImage;

	private Button outwardChequeDataEntryBtnBack;
	private Button outwardChequeDataEntryBtnZoomOut;
	private Button outwardChequeDataEntryBtnZoomIn;
	private Button outwardChequeDataEntryBtnRotate;
	private Button outwardChequeDataEntryBtnFit;
	private Button outwardChequeDataEntryBtnFront;
	private Button outwardChequeDataEntryBtnBackImage;
	private Button outwardChequeDataEntryBtnValidateAccount;
	private Button outwardChequeDataEntryBtnSaveNext;
	private Button outwardChequeDataEntryBtnReject;
	private Button outwardChequeDataEntryBtnSubmit;

	private Window outwardChequeDataEntryRejectWindow;
	private Combobox outwardChequeDataEntryCmbRejectReason;
	private Textbox outwardChequeDataEntryTxtRejectRemarks;
	private Button outwardChequeDataEntryBtnCancelReject;
	private Button outwardChequeDataEntryBtnConfirmReject;

	private final OutwardBatchService outwardBatchService;
	private final OutwardChequeService outwardChequeService;
	private final ScanService scanService;
	private final RejectedReasonService rejectedReasonService;

	private String outwardBatchId;
	private String scannedBatchId;

	private List<ScanCheque> scanChequeList;
	private List<OutwardCheque> outwardChequeList;

	private int currentChequeIndex;
	private boolean accountValidated;
	private boolean showingFront = true;
	private double imageZoom = 1.0;
	private int imageRotation = 0;

	public OutwardChequeDataEntryController() {
		outwardBatchService = new OutwardBatchServiceImpl();
		outwardChequeService = new OutwardChequeServiceImpl();
		scanService = new ScanServiceImpl();
		rejectedReasonService = RejectedReasonServiceImpl.getInstance();
	}

	@Override
	public void doAfterCompose(Component component) throws Exception {

		super.doAfterCompose(component);

		outwardChequeDataEntryLblBatchId = (Label) component.getFellow("outwardChequeDataEntryLblBatchId");

		outwardChequeDataEntryLblTotal = (Label) component.getFellow("outwardChequeDataEntryLblTotal");

		outwardChequeDataEntryLblEntered = (Label) component.getFellow("outwardChequeDataEntryLblEntered");

		outwardChequeDataEntryLblRemaining = (Label) component.getFellow("outwardChequeDataEntryLblRemaining");

		outwardChequeDataEntryLblRecord = (Label) component.getFellow("outwardChequeDataEntryLblRecord");

		outwardChequeDataEntryLblValidationStatus = (Label) component
				.getFellow("outwardChequeDataEntryLblValidationStatus");

		outwardChequeDataEntryLblNoImage = (Label) component.getFellow("outwardChequeDataEntryLblNoImage");

		outwardChequeDataEntryLblAccountValidation = (Label) component
				.getFellow("outwardChequeDataEntryLblAccountValidation");

		outwardChequeDataEntryTxtChequeNumber = (Textbox) component.getFellow("outwardChequeDataEntryTxtChequeNumber");

		outwardChequeDataEntryTxtPayeeAccount = (Textbox) component.getFellow("outwardChequeDataEntryTxtPayeeAccount");

		outwardChequeDataEntryTxtAmount = (Textbox) component.getFellow("outwardChequeDataEntryTxtAmount");

		outwardChequeDataEntryTxtChequeDate = (Textbox) component.getFellow("outwardChequeDataEntryTxtChequeDate");

		outwardChequeDataEntryTxtMicrCode = (Textbox) component.getFellow("outwardChequeDataEntryTxtMicrCode");

		outwardChequeDataEntryTxtDraweeName = (Textbox) component.getFellow("outwardChequeDataEntryTxtDraweeName");

		outwardChequeDataEntryTxtPayeeName = (Textbox) component.getFellow("outwardChequeDataEntryTxtPayeeName");

		outwardChequeDataEntryImage = (Image) component.getFellow("outwardChequeDataEntryImage");

		outwardChequeDataEntryBtnBack = (Button) component.getFellow("outwardChequeDataEntryBtnBack");

		outwardChequeDataEntryBtnZoomOut = (Button) component.getFellow("outwardChequeDataEntryBtnZoomOut");

		outwardChequeDataEntryBtnZoomIn = (Button) component.getFellow("outwardChequeDataEntryBtnZoomIn");

		outwardChequeDataEntryBtnRotate = (Button) component.getFellow("outwardChequeDataEntryBtnRotate");

		outwardChequeDataEntryBtnFit = (Button) component.getFellow("outwardChequeDataEntryBtnFit");

		outwardChequeDataEntryBtnFront = (Button) component.getFellow("outwardChequeDataEntryBtnFront");

		outwardChequeDataEntryBtnBackImage = (Button) component.getFellow("outwardChequeDataEntryBtnBackImage");

		outwardChequeDataEntryBtnValidateAccount = (Button) component
				.getFellow("outwardChequeDataEntryBtnValidateAccount");

		outwardChequeDataEntryBtnSaveNext = (Button) component.getFellow("outwardChequeDataEntryBtnSaveNext");

		outwardChequeDataEntryBtnReject = (Button) component.getFellow("outwardChequeDataEntryBtnReject");

		outwardChequeDataEntryBtnSubmit = (Button) component.getFellow("outwardChequeDataEntryBtnSubmit");

		outwardChequeDataEntryRejectWindow = (Window) component.getFellow("outwardChequeDataEntryRejectWindow");

		outwardChequeDataEntryCmbRejectReason = (Combobox) outwardChequeDataEntryRejectWindow
				.getFellow("outwardChequeDataEntryCmbRejectReason");

		outwardChequeDataEntryTxtRejectRemarks = (Textbox) outwardChequeDataEntryRejectWindow
				.getFellow("outwardChequeDataEntryTxtRejectRemarks");

		outwardChequeDataEntryBtnCancelReject = (Button) outwardChequeDataEntryRejectWindow
				.getFellow("outwardChequeDataEntryBtnCancelReject");

		outwardChequeDataEntryBtnConfirmReject = (Button) outwardChequeDataEntryRejectWindow
				.getFellow("outwardChequeDataEntryBtnConfirmReject");

		outwardChequeDataEntryRejectWindow.setVisible(false);
		outwardChequeDataEntryBtnSubmit.setVisible(false);

		outwardBatchId = resolveBatchId();

		if (outwardBatchId.isEmpty()) {
			clearPage();
			return;
		}

		outwardChequeDataEntryLblBatchId.setValue(outwardBatchId);

		registerEvents();
		loadRejectedReasons();
		loadDataEntryData();
	}

	private void registerEvents() {

		outwardChequeDataEntryBtnBack.addEventListener("onClick", event -> backToDataEntryList());

		outwardChequeDataEntryBtnZoomOut.addEventListener("onClick", event -> zoomOut());

		outwardChequeDataEntryBtnZoomIn.addEventListener("onClick", event -> zoomIn());

		outwardChequeDataEntryBtnRotate.addEventListener("onClick", event -> rotateImage());

		outwardChequeDataEntryBtnFit.addEventListener("onClick", event -> fitImage());

		outwardChequeDataEntryBtnFront.addEventListener("onClick", event -> showFrontImage());

		outwardChequeDataEntryBtnBackImage.addEventListener("onClick", event -> showBackImage());

		outwardChequeDataEntryBtnValidateAccount.addEventListener("onClick", event -> validateAccount());

		outwardChequeDataEntryBtnSaveNext.addEventListener("onClick", event -> saveAndNext());

		outwardChequeDataEntryBtnReject.addEventListener("onClick", event -> openRejectWindow());

		outwardChequeDataEntryBtnCancelReject.addEventListener("onClick", event -> closeRejectWindow());

		outwardChequeDataEntryBtnConfirmReject.addEventListener("onClick", event -> requestReject());

		outwardChequeDataEntryTxtPayeeAccount.addEventListener("onChange", event -> resetAccountValidation());
	}

	private String resolveBatchId() {

		String batchId = Executions.getCurrent().getParameter("batchId");

		if (batchId != null && !batchId.trim().isEmpty()) {

			batchId = batchId.trim();

			Sessions.getCurrent().setAttribute("OUTWARD_DATA_ENTRY_BATCH_ID", batchId);

			return batchId;
		}

		Object sessionBatchId = Sessions.getCurrent().getAttribute("OUTWARD_DATA_ENTRY_BATCH_ID");

		if (sessionBatchId != null && !sessionBatchId.toString().trim().isEmpty()) {

			return sessionBatchId.toString().trim();
		}

		Object argumentBatchId = Executions.getCurrent().getArg().get("batchId");

		if (argumentBatchId != null && !argumentBatchId.toString().trim().isEmpty()) {

			return argumentBatchId.toString().trim();
		}

		return "";
	}

	private void loadDataEntryData() {

		try {

			scannedBatchId = outwardBatchService.getScannedBatchIdByOutwardBatchId(outwardBatchId);

			if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {

				clearPage();
				outwardChequeDataEntryLblValidationStatus.setValue("Scanned batch not found");
				outwardChequeDataEntryLblValidationStatus.setVisible(true);
				return;
			}

			scanChequeList = scanService.getChequesByBatchId(scannedBatchId);

			outwardChequeList = outwardChequeService.getChequesByBatchId(outwardBatchId);

			if (scanChequeList == null) {
				scanChequeList = new ArrayList<>();
			}

			if (outwardChequeList == null) {
				outwardChequeList = new ArrayList<>();
			}

			if (scanChequeList.size() != outwardChequeList.size()) {

				throw new IllegalStateException("Scanned cheque count and outward cheque count do not match");
			}

			currentChequeIndex = findFirstPendingCheque();

			updateSummary();

			if (currentChequeIndex >= 0 && currentChequeIndex < scanChequeList.size()) {

				loadCurrentCheque();

			} else {

				clearChequeFields();

				outwardChequeDataEntryBtnSaveNext.setVisible(false);

				outwardChequeDataEntryBtnSubmit.setVisible(true);

				outwardChequeDataEntryLblRecord.setValue("All records completed");
			}

		} catch (Exception exception) {

			exception.printStackTrace();

			clearPage();

			outwardChequeDataEntryLblValidationStatus.setValue("Unable to load cheque data: " + exception.getMessage());

			outwardChequeDataEntryLblValidationStatus.setVisible(true);
		}
	}

	private int findFirstPendingCheque() {

		if (outwardChequeList == null || outwardChequeList.isEmpty()) {

			return -1;
		}

		for (int index = 0; index < outwardChequeList.size(); index++) {

			OutwardCheque cheque = outwardChequeList.get(index);

			if (cheque == null || cheque.getChequeStatus() == null
					|| !"DATA_ENTRY_COMPLETED".equalsIgnoreCase(cheque.getChequeStatus().trim())) {

				return index;
			}
		}

		return -1;
	}

	private void updateSummary() {

		int total = scanChequeList == null ? 0 : scanChequeList.size();

		int entered = 0;

		if (outwardChequeList != null) {

			for (OutwardCheque cheque : outwardChequeList) {

				if (cheque != null && cheque.getChequeStatus() != null
						&& "DATA_ENTRY_COMPLETED".equalsIgnoreCase(cheque.getChequeStatus().trim())) {

					entered++;
				}
			}
		}

		int remaining = total - entered;

		if (remaining < 0) {
			remaining = 0;
		}

		outwardChequeDataEntryLblTotal.setValue(String.valueOf(total));

		outwardChequeDataEntryLblEntered.setValue(String.valueOf(entered));

		outwardChequeDataEntryLblRemaining.setValue(String.valueOf(remaining));

		if (total > 0 && currentChequeIndex >= 0 && currentChequeIndex < total) {

			outwardChequeDataEntryLblRecord.setValue("Record " + (currentChequeIndex + 1) + " of " + total);

		} else if (total == 0) {

			outwardChequeDataEntryLblRecord.setValue("Record 0 of 0");
		}
	}

	private void loadCurrentCheque() {

		if (scanChequeList == null || scanChequeList.isEmpty() || outwardChequeList == null
				|| outwardChequeList.isEmpty() || currentChequeIndex < 0 || currentChequeIndex >= scanChequeList.size()
				|| currentChequeIndex >= outwardChequeList.size()) {

			clearChequeFields();
			return;
		}

		clearChequeFields();

		showingFront = true;
		imageZoom = 1.0;
		imageRotation = 0;

		outwardChequeDataEntryBtnFront.setSclass("outward-cheque-data-entry-image-side-button active");

		outwardChequeDataEntryBtnBackImage.setSclass("outward-cheque-data-entry-image-side-button");

		loadFrontImage();

		resetAccountValidation();

		outwardChequeDataEntryLblValidationStatus.setValue("");

		outwardChequeDataEntryLblValidationStatus.setVisible(false);

		outwardChequeDataEntryBtnSaveNext.setVisible(true);

		outwardChequeDataEntryBtnSubmit.setVisible(false);

		updateSummary();
	}

	private void loadFrontImage() {

		ScanCheque scanCheque = scanChequeList.get(currentChequeIndex);

		String imagePath = scanCheque.getChequeImageFront();

		if (imagePath == null || imagePath.trim().isEmpty()) {

			outwardChequeDataEntryImage.setVisible(false);

			outwardChequeDataEntryLblNoImage.setVisible(true);

			return;
		}

		outwardChequeDataEntryImage.setSrc(imagePath.trim());

		outwardChequeDataEntryImage.setVisible(true);

		outwardChequeDataEntryLblNoImage.setVisible(false);

		applyImageTransform();
	}

	private void loadBackImage() {

		ScanCheque scanCheque = scanChequeList.get(currentChequeIndex);

		String imagePath = scanCheque.getChequeImageBack();

		if (imagePath == null || imagePath.trim().isEmpty()) {

			outwardChequeDataEntryImage.setVisible(false);

			outwardChequeDataEntryLblNoImage.setVisible(true);

			return;
		}

		outwardChequeDataEntryImage.setSrc(imagePath.trim());

		outwardChequeDataEntryImage.setVisible(true);

		outwardChequeDataEntryLblNoImage.setVisible(false);

		applyImageTransform();
	}

	private void showFrontImage() {

		showingFront = true;
		imageZoom = 1.0;
		imageRotation = 0;

		outwardChequeDataEntryBtnFront.setSclass("outward-cheque-data-entry-image-side-button active");

		outwardChequeDataEntryBtnBackImage.setSclass("outward-cheque-data-entry-image-side-button");

		loadFrontImage();
	}

	private void showBackImage() {

		showingFront = false;
		imageZoom = 1.0;
		imageRotation = 0;

		outwardChequeDataEntryBtnFront.setSclass("outward-cheque-data-entry-image-side-button");

		outwardChequeDataEntryBtnBackImage.setSclass("outward-cheque-data-entry-image-side-button active");

		loadBackImage();
	}

	private void zoomIn() {

		imageZoom += 0.1;

		if (imageZoom > 3.0) {
			imageZoom = 3.0;
		}

		applyImageTransform();
	}

	private void zoomOut() {

		imageZoom -= 0.1;

		if (imageZoom < 0.5) {
			imageZoom = 0.5;
		}

		applyImageTransform();
	}

	private void rotateImage() {

		imageRotation += 90;

		if (imageRotation >= 360) {
			imageRotation = 0;
		}

		applyImageTransform();
	}

	private void fitImage() {

		imageZoom = 1.0;
		imageRotation = 0;

		applyImageTransform();
	}

	private void applyImageTransform() {

		String style = "transform: scale(" + imageZoom + ") rotate(" + imageRotation + "deg);";

		outwardChequeDataEntryImage.setStyle(style);
	}

	private void validateAccount() {

		String accountNumber = outwardChequeDataEntryTxtPayeeAccount.getValue();

		if (accountNumber == null) {
			accountNumber = "";
		}

		accountNumber = accountNumber.trim();

		if (accountNumber.isEmpty()) {

			showAccountValidation(false, "Account is Invalid");

			return;
		}

		boolean numeric = Pattern.matches("^[0-9]+$", accountNumber);

		boolean validLength = accountNumber.length() >= 9 && accountNumber.length() <= 18;

		boolean valid = numeric && validLength;

		if (valid) {

			showAccountValidation(true, "Account is Verified");

		} else {

			showAccountValidation(false, "Account is Invalid");
		}
	}

	private void showAccountValidation(boolean valid, String message) {

		outwardChequeDataEntryLblAccountValidation.setValue(message);

		outwardChequeDataEntryLblAccountValidation.setSclass(
				valid ? "outward-cheque-account-validation valid" : "outward-cheque-account-validation invalid");

		outwardChequeDataEntryLblAccountValidation.setVisible(true);

		accountValidated = valid;
	}

	private void resetAccountValidation() {

		accountValidated = false;

		outwardChequeDataEntryLblAccountValidation.setValue("");

		outwardChequeDataEntryLblAccountValidation.setVisible(false);
	}

	private void saveAndNext() {

		if (scanChequeList == null || outwardChequeList == null || scanChequeList.isEmpty()
				|| outwardChequeList.isEmpty()) {

			return;
		}

		if (currentChequeIndex < 0 || currentChequeIndex >= scanChequeList.size()
				|| currentChequeIndex >= outwardChequeList.size()) {

			return;
		}

		if (!validateFields()) {
			return;
		}

		OutwardCheque outwardCheque = outwardChequeList.get(currentChequeIndex);

		String chequeNumber = outwardChequeDataEntryTxtChequeNumber.getValue().trim();

		String payeeAccount = outwardChequeDataEntryTxtPayeeAccount.getValue().trim();

		String amount = outwardChequeDataEntryTxtAmount.getValue().trim();

		String chequeDate = outwardChequeDataEntryTxtChequeDate.getValue().trim();

		String micrCode = outwardChequeDataEntryTxtMicrCode.getValue().trim();

		String draweeName = outwardChequeDataEntryTxtDraweeName.getValue().trim();

		String payeeName = outwardChequeDataEntryTxtPayeeName.getValue().trim();

		BigDecimal chequeAmount;

		try {

			chequeAmount = new BigDecimal(amount);

			if (chequeAmount.signum() < 0) {

				showValidation("Cheque amount cannot be negative");

				return;
			}

		} catch (NumberFormatException exception) {

			showValidation("Enter a valid cheque amount");

			return;
		}

		java.sql.Date parsedChequeDate;

		try {

			SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

			dateFormat.setLenient(false);

			parsedChequeDate = new java.sql.Date(dateFormat.parse(chequeDate).getTime());

		} catch (Exception exception) {

			showValidation("Enter cheque date in DD-MM-YYYY format");

			return;
		}

		outwardCheque.setChequeNumber(chequeNumber);

		outwardCheque.setPayeeAccountNumber(payeeAccount);

		outwardCheque.setChequeAmount(chequeAmount);

		outwardCheque.setChequeDate(parsedChequeDate);

		outwardCheque.setMicrCode(micrCode);

		outwardCheque.setDraweeName(draweeName);

		outwardCheque.setPayeeName(payeeName);

		outwardCheque.setChequeStatus("DATA_ENTRY_COMPLETED");

		try {

			boolean saved = outwardChequeService.saveDataEntry(outwardCheque);

			if (!saved) {

				showValidation("Unable to save cheque data");

				return;
			}

			outwardChequeList.set(currentChequeIndex, outwardCheque);

			updateSummary();

			if (currentChequeIndex < scanChequeList.size() - 1) {

				currentChequeIndex++;

				loadCurrentCheque();

			} else {

				currentChequeIndex = scanChequeList.size();

				updateSummary();

				clearChequeFields();

				outwardChequeDataEntryBtnSaveNext.setVisible(false);

				outwardChequeDataEntryBtnSubmit.setVisible(true);

				outwardChequeDataEntryLblRecord.setValue("All records completed");

				outwardChequeDataEntryLblValidationStatus.setValue("ALL CHEQUES DATA ENTERED");

				outwardChequeDataEntryLblValidationStatus.setVisible(true);
			}

		} catch (Exception exception) {

			exception.printStackTrace();

			showValidation("Failed to save cheque data: " + exception.getMessage());
		}
	}

	private boolean validateFields() {

		String chequeNumber = outwardChequeDataEntryTxtChequeNumber.getValue();

		if (chequeNumber == null || chequeNumber.trim().isEmpty()) {

			showValidation("Cheque Number is required");

			return false;
		}

		String amount = outwardChequeDataEntryTxtAmount.getValue();

		if (amount == null || amount.trim().isEmpty()) {

			showValidation("Cheque Amount is required");

			return false;
		}

		String chequeDate = outwardChequeDataEntryTxtChequeDate.getValue();

		if (chequeDate == null || chequeDate.trim().isEmpty()) {

			showValidation("Cheque Date is required");

			return false;
		}

		String micrCode = outwardChequeDataEntryTxtMicrCode.getValue();

		if (micrCode == null || micrCode.trim().isEmpty()) {

			showValidation("MICR Code is required");

			return false;
		}

		String payeeAccount = outwardChequeDataEntryTxtPayeeAccount.getValue();

		if (payeeAccount == null || payeeAccount.trim().isEmpty()) {

			showValidation("Payee Account Number is required");

			return false;
		}

		String payeeName = outwardChequeDataEntryTxtPayeeName.getValue();

		if (payeeName == null || payeeName.trim().isEmpty()) {

			showValidation("Payee Name is required");

			return false;
		}

		String draweeName = outwardChequeDataEntryTxtDraweeName.getValue();

		if (draweeName == null || draweeName.trim().isEmpty()) {

			showValidation("Drawee Name is required");

			return false;
		}

		if (!accountValidated) {

			validateAccount();

			if (!accountValidated) {

				showValidation("Please validate Payee Account Number");

				return false;
			}
		}

		return true;
	}

	private void showValidation(String message) {

		outwardChequeDataEntryLblValidationStatus.setValue(message);

		outwardChequeDataEntryLblValidationStatus.setVisible(true);
	}

	private void loadRejectedReasons() {

		try {

			List<RejectedReason> rejectedReasons = rejectedReasonService.getAllRejectedReasons();

			outwardChequeDataEntryCmbRejectReason.getItems().clear();

			if (rejectedReasons == null) {
				return;
			}

			for (RejectedReason rejectedReason : rejectedReasons) {

				Comboitem comboitem = new Comboitem();

				comboitem.setLabel(rejectedReason.toString());

				comboitem.setValue(rejectedReason);

				outwardChequeDataEntryCmbRejectReason.appendChild(comboitem);
			}

		} catch (Exception exception) {

			exception.printStackTrace();
		}
	}

	private void openRejectWindow() {

		if (scanChequeList == null || scanChequeList.isEmpty() || currentChequeIndex < 0
				|| currentChequeIndex >= scanChequeList.size()) {

			return;
		}

		outwardChequeDataEntryCmbRejectReason.setSelectedItem(null);

		outwardChequeDataEntryTxtRejectRemarks.setValue("");

		outwardChequeDataEntryRejectWindow.setVisible(true);

		outwardChequeDataEntryRejectWindow.doModal();
	}

	private void closeRejectWindow() {

		outwardChequeDataEntryRejectWindow.setVisible(false);
	}

	private void requestReject() {

		if (outwardChequeDataEntryCmbRejectReason.getSelectedItem() == null) {

			showValidation("Please select a rejection reason");

			return;
		}

		String remarks = outwardChequeDataEntryTxtRejectRemarks.getValue();

		if (remarks == null || remarks.trim().isEmpty()) {

			showValidation("Please enter rejection remarks");

			return;
		}

		closeRejectWindow();
	}

	private void backToDataEntryList() {

		Executions.sendRedirect("/outward/maker/data-entry.zul");
	}

	private void clearChequeFields() {

		outwardChequeDataEntryTxtChequeNumber.setValue("");

		outwardChequeDataEntryTxtPayeeAccount.setValue("");

		outwardChequeDataEntryTxtAmount.setValue("");

		outwardChequeDataEntryTxtChequeDate.setValue("");

		outwardChequeDataEntryTxtMicrCode.setValue("");

		outwardChequeDataEntryTxtDraweeName.setValue("");

		outwardChequeDataEntryTxtPayeeName.setValue("");

		resetAccountValidation();

		outwardChequeDataEntryLblValidationStatus.setValue("");

		outwardChequeDataEntryLblValidationStatus.setVisible(false);
	}

	private void clearPage() {

		scanChequeList = new ArrayList<>();

		outwardChequeList = new ArrayList<>();

		currentChequeIndex = -1;

		outwardChequeDataEntryLblBatchId.setValue("-");

		outwardChequeDataEntryLblTotal.setValue("0");

		outwardChequeDataEntryLblEntered.setValue("0");

		outwardChequeDataEntryLblRemaining.setValue("0");

		outwardChequeDataEntryLblRecord.setValue("Record 0 of 0");

		clearChequeFields();

		outwardChequeDataEntryImage.setVisible(false);

		outwardChequeDataEntryLblNoImage.setVisible(true);

		outwardChequeDataEntryBtnSaveNext.setVisible(false);

		outwardChequeDataEntryBtnSubmit.setVisible(false);
	}
}