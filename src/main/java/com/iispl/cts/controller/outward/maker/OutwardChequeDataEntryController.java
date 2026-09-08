package com.iispl.cts.controller.outward.maker;

import java.math.BigDecimal;
import java.sql.Date;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.iispl.cts.entity.RejectedReason;
import com.iispl.cts.entity.outward.OutwardBatch;
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

	private Window outwardChequeDataEntryWin;

	private Button outwardChequeDataEntryBtnBack;
	private Button outwardChequeDataEntryBtnImageToggle;
	private Button outwardChequeDataEntryBtnZoomOut;
	private Button outwardChequeDataEntryBtnZoomIn;
	private Button outwardChequeDataEntryBtnRotate;
	private Button outwardChequeDataEntryBtnFit;
	private Button outwardChequeDataEntryBtnReject;
	private Button outwardChequeDataEntryBtnSaveNext;
	private Button outwardChequeDataEntryBtnSubmit;
	private Button outwardChequeDataEntryBtnConfirmReject;
	private Button outwardChequeDataEntryBtnCancelReject;

	private Label outwardChequeDataEntryLblTitle;
	private Label outwardChequeDataEntryLblSubtitle;
	private Label outwardChequeDataEntryLblRecord;
	private Label outwardChequeDataEntryLblBatchId;
	private Label outwardChequeDataEntryLblSummaryChequeNumber;
	private Label outwardChequeDataEntryLblTotal;
	private Label outwardChequeDataEntryLblRemaining;
	private Label outwardChequeDataEntryLblImageMessage;
	private Label outwardChequeDataEntryLblValidationStatus;

	private Image outwardChequeDataEntryChequeImage;

	private Textbox outwardChequeDataEntryTxtChequeNumber;
	private Textbox outwardChequeDataEntryTxtAmount;
	private Textbox outwardChequeDataEntryTxtChequeDate;
	private Textbox outwardChequeDataEntryTxtMicrCode;
	private Textbox outwardChequeDataEntryTxtPayeeAccount;
	private Textbox outwardChequeDataEntryTxtPayeeName;
	private Textbox outwardChequeDataEntryTxtDraweeName;
	private Textbox outwardChequeDataEntryTxtRejectRemarks;

	private Combobox outwardChequeDataEntryCmbRejectReason;

	private Window outwardChequeDataEntryRejectWindow;

	private OutwardBatchService outwardBatchService;
	private OutwardChequeService outwardChequeService;
	private ScanService scanService;
	private RejectedReasonService rejectedReasonService;

	private String outwardBatchId;
	private String scannedBatchId;

	private OutwardBatch outwardBatch;
	private List<OutwardCheque> outwardChequeList;
	private List<ScanCheque> scanChequeList;

	private int currentChequeIndex = -1;

	private boolean showingFrontImage = true;

	private double imageZoom = 1.0;
	private int imageRotation = 0;

	private final SimpleDateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");

	@Override
	public void doAfterCompose(Component component) throws Exception {
		super.doAfterCompose(component);

		outwardChequeDataEntryWin = (Window) component.getFellow("outwardChequeDataEntryWin");

		outwardChequeDataEntryBtnBack = (Button) component.getFellow("outwardChequeDataEntryBtnBack");

		outwardChequeDataEntryBtnImageToggle = (Button) component.getFellow("outwardChequeDataEntryBtnImageToggle");

		outwardChequeDataEntryBtnZoomOut = (Button) component.getFellow("outwardChequeDataEntryBtnZoomOut");

		outwardChequeDataEntryBtnZoomIn = (Button) component.getFellow("outwardChequeDataEntryBtnZoomIn");

		outwardChequeDataEntryBtnRotate = (Button) component.getFellow("outwardChequeDataEntryBtnRotate");

		outwardChequeDataEntryBtnFit = (Button) component.getFellow("outwardChequeDataEntryBtnFit");

		outwardChequeDataEntryBtnReject = (Button) component.getFellow("outwardChequeDataEntryBtnReject");

		outwardChequeDataEntryBtnSaveNext = (Button) component.getFellow("outwardChequeDataEntryBtnSaveNext");

		outwardChequeDataEntryBtnSubmit = (Button) component.getFellow("outwardChequeDataEntryBtnSubmit");

		outwardChequeDataEntryRejectWindow = (Window) component.getFellow("outwardChequeDataEntryRejectWindow");

		outwardChequeDataEntryBtnConfirmReject = (Button) outwardChequeDataEntryRejectWindow
				.getFellow("outwardChequeDataEntryBtnConfirmReject");

		outwardChequeDataEntryBtnCancelReject = (Button) outwardChequeDataEntryRejectWindow
				.getFellow("outwardChequeDataEntryBtnCancelReject");

		outwardChequeDataEntryLblTitle = (Label) component.getFellow("outwardChequeDataEntryLblTitle");

		outwardChequeDataEntryLblSubtitle = (Label) component.getFellow("outwardChequeDataEntryLblSubtitle");

		outwardChequeDataEntryLblRecord = (Label) component.getFellow("outwardChequeDataEntryLblRecord");

		outwardChequeDataEntryLblBatchId = (Label) component.getFellow("outwardChequeDataEntryLblBatchId");

		outwardChequeDataEntryLblSummaryChequeNumber = (Label) component
				.getFellow("outwardChequeDataEntryLblSummaryChequeNumber");

		outwardChequeDataEntryLblTotal = (Label) component.getFellow("outwardChequeDataEntryLblTotal");

		outwardChequeDataEntryLblRemaining = (Label) component.getFellow("outwardChequeDataEntryLblRemaining");

		outwardChequeDataEntryLblImageMessage = (Label) component.getFellow("outwardChequeDataEntryLblImageMessage");

		outwardChequeDataEntryLblValidationStatus = (Label) component
				.getFellow("outwardChequeDataEntryLblValidationStatus");

		outwardChequeDataEntryChequeImage = (Image) component.getFellow("outwardChequeDataEntryChequeImage");

		outwardChequeDataEntryTxtChequeNumber = (Textbox) component.getFellow("outwardChequeDataEntryTxtChequeNumber");

		outwardChequeDataEntryTxtAmount = (Textbox) component.getFellow("outwardChequeDataEntryTxtAmount");

		outwardChequeDataEntryTxtChequeDate = (Textbox) component.getFellow("outwardChequeDataEntryTxtChequeDate");

		outwardChequeDataEntryTxtMicrCode = (Textbox) component.getFellow("outwardChequeDataEntryTxtMicrCode");

		outwardChequeDataEntryTxtPayeeAccount = (Textbox) component.getFellow("outwardChequeDataEntryTxtPayeeAccount");

		outwardChequeDataEntryTxtPayeeName = (Textbox) component.getFellow("outwardChequeDataEntryTxtPayeeName");

		outwardChequeDataEntryTxtDraweeName = (Textbox) component.getFellow("outwardChequeDataEntryTxtDraweeName");

		outwardChequeDataEntryCmbRejectReason = (Combobox) outwardChequeDataEntryRejectWindow
				.getFellow("outwardChequeDataEntryCmbRejectReason");

		outwardChequeDataEntryTxtRejectRemarks = (Textbox) outwardChequeDataEntryRejectWindow
				.getFellow("outwardChequeDataEntryTxtRejectRemarks");

		outwardBatchService = new OutwardBatchServiceImpl();
		outwardChequeService = new OutwardChequeServiceImpl();
		scanService = new ScanServiceImpl();
		rejectedReasonService = RejectedReasonServiceImpl.getInstance();

		dateFormat.setLenient(false);

		outwardChequeDataEntryRejectWindow.setVisible(false);
		outwardChequeDataEntryBtnSubmit.setDisabled(true);

		resolveBatchId();
		registerEvents();
		loadRejectedReasons();
		loadDataEntryData();
	}

	private void resolveBatchId() {

		String requestBatchId = Executions.getCurrent().getParameter("batchId");

		if (requestBatchId != null && !requestBatchId.trim().isEmpty()) {
			outwardBatchId = requestBatchId.trim();
		}

		if (outwardBatchId == null || outwardBatchId.isEmpty()) {

			Object sessionBatchId = Executions.getCurrent().getSession().getAttribute("OUTWARD_DATA_ENTRY_BATCH_ID");

			if (sessionBatchId != null) {
				outwardBatchId = sessionBatchId.toString().trim();
			}
		}

		if (outwardBatchId == null || outwardBatchId.isEmpty()) {

			Object argumentBatchId = Executions.getCurrent().getArg().get("batchId");

			if (argumentBatchId != null) {
				outwardBatchId = argumentBatchId.toString().trim();
			}
		}

		if (outwardBatchId == null || outwardBatchId.isEmpty()) {
			throw new IllegalArgumentException("Outward batch ID is required");
		}
	}

	private void registerEvents() {

		outwardChequeDataEntryBtnBack.addEventListener("onClick", event -> backToDataEntryList());

		outwardChequeDataEntryBtnImageToggle.addEventListener("onClick", event -> toggleChequeImage());

		outwardChequeDataEntryBtnZoomOut.addEventListener("onClick", event -> zoomOut());

		outwardChequeDataEntryBtnZoomIn.addEventListener("onClick", event -> zoomIn());

		outwardChequeDataEntryBtnRotate.addEventListener("onClick", event -> rotateImage());

		outwardChequeDataEntryBtnFit.addEventListener("onClick", event -> fitImage());

		outwardChequeDataEntryBtnSaveNext.addEventListener("onClick", event -> saveAndNext());

		outwardChequeDataEntryBtnReject.addEventListener("onClick", event -> openRejectWindow());

		outwardChequeDataEntryBtnConfirmReject.addEventListener("onClick", event -> requestReject());

		outwardChequeDataEntryBtnCancelReject.addEventListener("onClick", event -> closeRejectWindow());

		outwardChequeDataEntryBtnSubmit.addEventListener("onClick", event -> submitToChecker());
	}

	private void loadDataEntryData() {

		try {

			outwardBatch = outwardBatchService.getBatchById(outwardBatchId);

			if (outwardBatch == null) {
				throw new IllegalStateException("Outward batch not found: " + outwardBatchId);
			}

			scannedBatchId = outwardBatchService.getScannedBatchIdByOutwardBatchId(outwardBatchId);

			if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {
				throw new IllegalStateException("Scanned batch not found for outward batch: " + outwardBatchId);
			}

			outwardChequeList = outwardChequeService.getChequesByBatchId(outwardBatchId);

			scanChequeList = scanService.getChequesByBatchId(scannedBatchId);

			if (outwardChequeList == null) {
				outwardChequeList = new ArrayList<>();
			}

			if (scanChequeList == null) {
				scanChequeList = new ArrayList<>();
			}

			updateBatchSummary();

			currentChequeIndex = findFirstPendingCheque();

			if (currentChequeIndex >= 0) {

				loadCurrentCheque();

				outwardChequeDataEntryBtnSaveNext.setDisabled(false);
				outwardChequeDataEntryBtnReject.setDisabled(false);

			} else {

				clearChequeFields();

				outwardChequeDataEntryBtnSaveNext.setDisabled(true);
				outwardChequeDataEntryBtnReject.setDisabled(true);
			}

			updateSubmitButton();

		} catch (Exception exception) {

			clearPage();

			showStatus("Unable to load cheque data: " + exception.getMessage());
		}
	}

	private int findFirstPendingCheque() {

		if (outwardChequeList == null) {
			return -1;
		}

		for (int index = 0; index < outwardChequeList.size(); index++) {

			OutwardCheque cheque = outwardChequeList.get(index);

			if (cheque == null) {
				continue;
			}

			String status = cheque.getChequeStatus();

			if (!"DATA_ENTRY_COMPLETED".equalsIgnoreCase(status == null ? "" : status.trim())) {

				return index;
			}
		}

		return -1;
	}

	private void loadCurrentCheque() {

		if (currentChequeIndex < 0 || currentChequeIndex >= outwardChequeList.size()) {
			return;
		}

		OutwardCheque outwardCheque = outwardChequeList.get(currentChequeIndex);

		if (outwardCheque == null) {
			return;
		}

		populateChequeFields(outwardCheque);

		ScanCheque scanCheque = findMatchingScanCheque(outwardCheque);

		resetImageState();

		if (scanCheque != null) {
			loadFrontImage(scanCheque);
		} else {
			showImageUnavailable();
		}

		updateSummary();
	}

	private ScanCheque findMatchingScanCheque(OutwardCheque outwardCheque) {

		if (outwardCheque == null || scanChequeList == null) {
			return null;
		}

		String chequeNumber = outwardCheque.getChequeNumber();

		if (chequeNumber != null && !chequeNumber.trim().isEmpty()) {

			for (ScanCheque scanCheque : scanChequeList) {

				if (scanCheque == null) {
					continue;
				}

				String scanChequeNumber = scanCheque.getChequeNumber();

				if (scanChequeNumber != null && chequeNumber.trim().equalsIgnoreCase(scanChequeNumber.trim())) {

					return scanCheque;
				}
			}
		}

		if (currentChequeIndex >= 0 && currentChequeIndex < scanChequeList.size()) {

			return scanChequeList.get(currentChequeIndex);
		}

		return null;
	}

	private void populateChequeFields(OutwardCheque cheque) {

		outwardChequeDataEntryTxtChequeNumber.setValue(safeValue(cheque.getChequeNumber()));

		outwardChequeDataEntryTxtAmount
				.setValue(cheque.getChequeAmount() == null ? "" : cheque.getChequeAmount().toPlainString());

		outwardChequeDataEntryTxtChequeDate
				.setValue(cheque.getChequeDate() == null ? "" : dateFormat.format(cheque.getChequeDate()));

		outwardChequeDataEntryTxtMicrCode.setValue(safeValue(cheque.getMicrCode()));

		outwardChequeDataEntryTxtPayeeAccount.setValue(safeValue(cheque.getPayeeAccountNumber()));

		outwardChequeDataEntryTxtPayeeName.setValue(safeValue(cheque.getPayeeName()));

		outwardChequeDataEntryTxtDraweeName.setValue(safeValue(cheque.getDraweeName()));
	}

	private String safeValue(String value) {

		return value == null ? "" : value;
	}

	private void updateBatchSummary() {

		int totalCheques = outwardChequeList == null ? 0 : outwardChequeList.size();

		int completedCheques = outwardChequeService.getDataEnteredCountByBatchId(outwardBatchId);

		int remainingCheques = Math.max(totalCheques - completedCheques, 0);

		outwardChequeDataEntryLblBatchId.setValue(safeValue(outwardBatchId));

		outwardChequeDataEntryLblTotal.setValue(String.valueOf(totalCheques));

		outwardChequeDataEntryLblRemaining.setValue(String.valueOf(remainingCheques));
	}

	private void updateSummary() {

		if (outwardChequeList == null || currentChequeIndex < 0 || currentChequeIndex >= outwardChequeList.size()) {

			outwardChequeDataEntryLblRecord.setValue("CHEQUE COMPLETED");

			outwardChequeDataEntryLblSummaryChequeNumber.setValue("-");

			return;
		}

		OutwardCheque cheque = outwardChequeList.get(currentChequeIndex);

		outwardChequeDataEntryLblRecord.setValue("CHEQUE " + (currentChequeIndex + 1));

		outwardChequeDataEntryLblSummaryChequeNumber.setValue(safeValue(cheque.getChequeNumber()));
	}

	private void updateSubmitButton() {

		if (outwardChequeList == null || outwardChequeList.isEmpty()) {

			outwardChequeDataEntryBtnSubmit.setDisabled(true);

			return;
		}

		int totalCheques = outwardChequeList.size();

		int completedCheques = outwardChequeService.getDataEnteredCountByBatchId(outwardBatchId);

		boolean allCompleted = totalCheques > 0 && completedCheques == totalCheques;

		outwardChequeDataEntryBtnSubmit.setDisabled(!allCompleted);
	}

	private void saveAndNext() {

		if (currentChequeIndex < 0 || currentChequeIndex >= outwardChequeList.size()) {
			return;
		}

		if (!validateFields()) {
			return;
		}

		try {

			OutwardCheque cheque = outwardChequeList.get(currentChequeIndex);

			populateOutwardChequeFromFields(cheque);

			boolean saved = outwardChequeService.saveDataEntry(cheque);

			if (!saved) {
				throw new IllegalStateException("Unable to save cheque data");
			}

			cheque.setChequeStatus("DATA_ENTRY_COMPLETED");

			updateBatchSummary();

			int nextIndex = findFirstPendingCheque();

			if (nextIndex >= 0) {

				currentChequeIndex = nextIndex;

				loadCurrentCheque();

			} else {

				currentChequeIndex = -1;

				clearChequeFields();

				outwardChequeDataEntryBtnSaveNext.setDisabled(true);

				outwardChequeDataEntryBtnReject.setDisabled(true);

				outwardChequeDataEntryLblRecord.setValue("ALL CHEQUES COMPLETED");

				outwardChequeDataEntryLblSummaryChequeNumber.setValue("-");

				showStatus("All cheque data has been completed.");
			}

			updateSubmitButton();

		} catch (Exception exception) {

			showStatus("Unable to save cheque data: " + exception.getMessage());
		}
	}

	private void populateOutwardChequeFromFields(OutwardCheque cheque) {

		cheque.setChequeNumber(outwardChequeDataEntryTxtChequeNumber.getValue().trim());

		cheque.setMicrCode(outwardChequeDataEntryTxtMicrCode.getValue().trim());

		cheque.setPayeeAccountNumber(outwardChequeDataEntryTxtPayeeAccount.getValue().trim());

		cheque.setPayeeName(outwardChequeDataEntryTxtPayeeName.getValue().trim());

		cheque.setDraweeName(outwardChequeDataEntryTxtDraweeName.getValue().trim());

		String amount = outwardChequeDataEntryTxtAmount.getValue().trim();

		cheque.setChequeAmount(new BigDecimal(amount));

		String date = outwardChequeDataEntryTxtChequeDate.getValue().trim();

		try {

			java.util.Date parsedDate = dateFormat.parse(date);

			cheque.setChequeDate(new Date(parsedDate.getTime()));

		} catch (ParseException exception) {

			throw new IllegalArgumentException("Cheque date must be in DD-MM-YYYY format");
		}
	}

	private boolean validateFields() {

		String chequeNumber = outwardChequeDataEntryTxtChequeNumber.getValue().trim();

		String amount = outwardChequeDataEntryTxtAmount.getValue().trim();

		String chequeDate = outwardChequeDataEntryTxtChequeDate.getValue().trim();

		String micrCode = outwardChequeDataEntryTxtMicrCode.getValue().trim();

		String payeeAccount = outwardChequeDataEntryTxtPayeeAccount.getValue().trim();

		String payeeName = outwardChequeDataEntryTxtPayeeName.getValue().trim();

		String draweeName = outwardChequeDataEntryTxtDraweeName.getValue().trim();

		if (chequeNumber.isEmpty()) {

			showStatus("Cheque number is required.");

			return false;
		}

		if (amount.isEmpty()) {

			showStatus("Cheque amount is required.");

			return false;
		}

		try {

			BigDecimal parsedAmount = new BigDecimal(amount);

			if (parsedAmount.compareTo(BigDecimal.ZERO) < 0) {

				showStatus("Cheque amount cannot be negative.");

				return false;
			}

		} catch (NumberFormatException exception) {

			showStatus("Enter a valid cheque amount.");

			return false;
		}

		if (chequeDate.isEmpty()) {

			showStatus("Cheque date is required.");

			return false;
		}

		try {

			dateFormat.parse(chequeDate);

		} catch (ParseException exception) {

			showStatus("Cheque date must be in DD-MM-YYYY format.");

			return false;
		}

		if (micrCode.isEmpty()) {

			showStatus("MICR code is required.");

			return false;
		}

		if (payeeAccount.isEmpty()) {

			showStatus("Payee account number is required.");

			return false;
		}

		if (payeeName.isEmpty()) {

			showStatus("Payee name is required.");

			return false;
		}

		if (draweeName.isEmpty()) {

			showStatus("Drawee name is required.");

			return false;
		}

		hideStatus();

		return true;
	}

	private void toggleChequeImage() {

		if (currentChequeIndex < 0 || currentChequeIndex >= outwardChequeList.size()) {
			return;
		}

		OutwardCheque outwardCheque = outwardChequeList.get(currentChequeIndex);

		ScanCheque scanCheque = findMatchingScanCheque(outwardCheque);

		if (scanCheque == null) {

			showImageUnavailable();

			return;
		}

		if (showingFrontImage) {

			loadBackImage(scanCheque);

		} else {

			loadFrontImage(scanCheque);
		}
	}

	private void loadFrontImage(ScanCheque scanCheque) {

		showingFrontImage = true;

		outwardChequeDataEntryBtnImageToggle.setLabel("BACK VIEW");

		loadImage(scanCheque.getChequeImageFront());
	}

	private void loadBackImage(ScanCheque scanCheque) {

		showingFrontImage = false;

		outwardChequeDataEntryBtnImageToggle.setLabel("FRONT VIEW");

		loadImage(scanCheque.getChequeImageBack());
	}

	private void loadImage(String imagePath) {

		if (imagePath == null || imagePath.trim().isEmpty()) {

			showImageUnavailable();

			return;
		}

		try {

			outwardChequeDataEntryChequeImage.setSrc(imagePath);

			outwardChequeDataEntryChequeImage.setVisible(true);

			outwardChequeDataEntryLblImageMessage.setVisible(false);

			fitImage();

		} catch (Exception exception) {

			showImageUnavailable();
		}
	}

	private void showImageUnavailable() {

		outwardChequeDataEntryChequeImage.setVisible(false);

		outwardChequeDataEntryLblImageMessage.setVisible(true);
	}

	private void resetImageState() {

		showingFrontImage = true;

		imageZoom = 1.0;

		imageRotation = 0;

		outwardChequeDataEntryBtnImageToggle.setLabel("BACK VIEW");

		outwardChequeDataEntryChequeImage.setStyle("transform: scale(1.0) rotate(0deg);");
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

		outwardChequeDataEntryChequeImage
				.setStyle("transform: scale(" + imageZoom + ") rotate(" + imageRotation + "deg);");
	}

	private void openRejectWindow() {

		if (currentChequeIndex < 0 || currentChequeIndex >= outwardChequeList.size()) {
			return;
		}

		outwardChequeDataEntryCmbRejectReason.setSelectedItem(null);

		outwardChequeDataEntryTxtRejectRemarks.setValue("");

		outwardChequeDataEntryRejectWindow.setVisible(true);
	}

	private void closeRejectWindow() {

		outwardChequeDataEntryRejectWindow.setVisible(false);
	}

	private void requestReject() {

		if (currentChequeIndex < 0 || currentChequeIndex >= outwardChequeList.size()) {
			return;
		}

		if (outwardChequeDataEntryCmbRejectReason.getSelectedItem() == null) {

			showStatus("Please select a rejection reason.");

			return;
		}

		String remarks = outwardChequeDataEntryTxtRejectRemarks.getValue().trim();

		if (remarks.isEmpty()) {

			showStatus("Please enter remarks for rejection.");

			return;
		}

		String reason = outwardChequeDataEntryCmbRejectReason.getSelectedItem().getLabel();

		OutwardCheque cheque = outwardChequeList.get(currentChequeIndex);

		closeRejectWindow();

		showStatus("Reject request created for cheque " + safeValue(cheque.getChequeNumber()) + " with reason " + reason
				+ ".");
	}

	private void loadRejectedReasons() {

		try {

			List<RejectedReason> reasons = rejectedReasonService.getAllRejectedReasons();

			outwardChequeDataEntryCmbRejectReason.getItems().clear();

			if (reasons == null) {
				return;
			}

			for (RejectedReason reason : reasons) {

				if (reason == null) {
					continue;
				}

				Comboitem item = new Comboitem();

				item.setLabel(safeValue(reason.getRejectedReasonName()));

				item.setValue(reason.getRejectedReasonId());

				outwardChequeDataEntryCmbRejectReason.appendChild(item);
			}

		} catch (Exception exception) {

			showStatus("Unable to load rejection reasons.");
		}
	}

	private void submitToChecker() {

		if (outwardChequeList == null || outwardChequeList.isEmpty()) {

			showStatus("There are no cheques to submit.");

			return;
		}

		int totalCheques = outwardChequeList.size();

		int completedCheques = outwardChequeService.getDataEnteredCountByBatchId(outwardBatchId);

		if (completedCheques != totalCheques) {

			showStatus("All cheques must be completed before submission.");

			updateSubmitButton();

			return;
		}

		showStatus("Batch is ready to be submitted to Checker.");
	}

	private void backToDataEntryList() {

		Executions.sendRedirect("/outward/maker/data-entry.zul");
	}

	private void clearChequeFields() {

		outwardChequeDataEntryTxtChequeNumber.setValue("");

		outwardChequeDataEntryTxtAmount.setValue("");

		outwardChequeDataEntryTxtChequeDate.setValue("");

		outwardChequeDataEntryTxtMicrCode.setValue("");

		outwardChequeDataEntryTxtPayeeAccount.setValue("");

		outwardChequeDataEntryTxtPayeeName.setValue("");

		outwardChequeDataEntryTxtDraweeName.setValue("");
	}

	private void clearPage() {

		clearChequeFields();

		outwardChequeDataEntryLblBatchId.setValue("-");

		outwardChequeDataEntryLblTotal.setValue("0");

		outwardChequeDataEntryLblRemaining.setValue("0");

		outwardChequeDataEntryLblRecord.setValue("CHEQUE DATA ENTRY");

		outwardChequeDataEntryLblSummaryChequeNumber.setValue("-");

		outwardChequeDataEntryBtnSaveNext.setDisabled(true);

		outwardChequeDataEntryBtnReject.setDisabled(true);

		outwardChequeDataEntryBtnSubmit.setDisabled(true);

		outwardChequeDataEntryChequeImage.setVisible(false);

		outwardChequeDataEntryLblImageMessage.setVisible(true);
	}

	private void showStatus(String message) {

		if (message == null || message.trim().isEmpty()) {

			hideStatus();

			return;
		}

		outwardChequeDataEntryLblValidationStatus.setValue(message);

		outwardChequeDataEntryLblValidationStatus.setVisible(true);
	}

	private void hideStatus() {

		outwardChequeDataEntryLblValidationStatus.setValue("");

		outwardChequeDataEntryLblValidationStatus.setVisible(false);
	}
}