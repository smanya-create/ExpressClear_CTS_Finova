package com.iispl.cts.controller.outward.maker;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Hlayout;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.service.outward.OutwardBatchService;
import com.iispl.cts.service.outward.OutwardChequeService;
import com.iispl.cts.service.outward.ScanService;
import com.iispl.cts.serviceimpl.outward.OutwardBatchServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardChequeServiceImpl;
import com.iispl.cts.serviceimpl.outward.ScanServiceImpl;

public class OutwardChequeDataEntryController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 1L;

	private static final String STATUS_PENDING_DATA_ENTRY = "PENDING_DATA_ENTRY";
	private static final String STATUS_PENDING_VERIFICATION = "PENDING_VERIFICATION";
	private static final String STATUS_REJECTION_REQUEST = "REJECTION_REQUEST";
	private static final String STATUS_PENDING_CHECKER_PROCESS = "PENDING_CHECKER_PROCESS";

	private Window outwardChequeDataEntryWin;

	private Button outwardChequeDataEntryBtnBack;
	private Button outwardChequeDataEntryBtnImageToggle;
	private Button outwardChequeDataEntryBtnZoomOut;
	private Button outwardChequeDataEntryBtnZoomIn;
	private Button outwardChequeDataEntryBtnRotate;
	private Button outwardChequeDataEntryBtnReset;
	private Button outwardChequeDataEntryBtnPrevious;
	private Button outwardChequeDataEntryBtnNext;
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
	private Datebox outwardChequeDataEntryDtChequeDate;
	private Textbox outwardChequeDataEntryTxtMicrCode;
	private Textbox outwardChequeDataEntryTxtPayeeAccount;
	private Textbox outwardChequeDataEntryTxtPayeeName;
	private Textbox outwardChequeDataEntryTxtDraweeName;
	private Textbox outwardChequeDataEntryTxtRejectRemarks;

	private Window outwardChequeDataEntryRejectWindow;

	private OutwardBatchService outwardBatchService;
	private OutwardChequeService outwardChequeService;
	private ScanService scanService;

	private String outwardBatchId;
	private String scannedBatchId;

	private OutwardBatch outwardBatch;
	private List<OutwardCheque> outwardChequeList = new ArrayList<>();
	private List<ScanCheque> scanChequeList = new ArrayList<>();

	private int currentChequeIndex = -1;
	private boolean showingFrontImage = true;
	private double imageZoom = 1.0;
	private int imageRotation = 0;

	@Override
	public void doAfterCompose(Component component) throws Exception {
		super.doAfterCompose(component);

		outwardChequeDataEntryWin = (Window) component.getFellow("outwardChequeDataEntryWin");

		outwardChequeDataEntryBtnBack = (Button) component.getFellow("outwardChequeDataEntryBtnBack");

		outwardChequeDataEntryBtnImageToggle = (Button) component.getFellow("outwardChequeDataEntryBtnImageToggle");

		outwardChequeDataEntryBtnZoomOut = (Button) component.getFellow("outwardChequeDataEntryBtnZoomOut");

		outwardChequeDataEntryBtnZoomIn = (Button) component.getFellow("outwardChequeDataEntryBtnZoomIn");

		outwardChequeDataEntryBtnRotate = (Button) component.getFellow("outwardChequeDataEntryBtnRotate");

		outwardChequeDataEntryBtnReset = (Button) component.getFellow("outwardChequeDataEntryBtnReset");

		outwardChequeDataEntryBtnPrevious = (Button) component.getFellow("outwardChequeDataEntryBtnPrevious");

		outwardChequeDataEntryBtnNext = (Button) component.getFellow("outwardChequeDataEntryBtnNext");

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

		outwardChequeDataEntryDtChequeDate = (Datebox) component.getFellow("outwardChequeDataEntryDtChequeDate");

		outwardChequeDataEntryTxtMicrCode = (Textbox) component.getFellow("outwardChequeDataEntryTxtMicrCode");

		outwardChequeDataEntryTxtPayeeAccount = (Textbox) component.getFellow("outwardChequeDataEntryTxtPayeeAccount");

		outwardChequeDataEntryTxtPayeeName = (Textbox) component.getFellow("outwardChequeDataEntryTxtPayeeName");

		outwardChequeDataEntryTxtDraweeName = (Textbox) component.getFellow("outwardChequeDataEntryTxtDraweeName");

		outwardChequeDataEntryTxtRejectRemarks = (Textbox) outwardChequeDataEntryRejectWindow
				.getFellow("outwardChequeDataEntryTxtRejectRemarks");

		outwardBatchService = new OutwardBatchServiceImpl();
		outwardChequeService = new OutwardChequeServiceImpl();
		scanService = new ScanServiceImpl();

		outwardChequeDataEntryRejectWindow.setVisible(false);
		outwardChequeDataEntryBtnSubmit.setDisabled(true);

		resolveBatchContext();
		registerEvents();
		loadDataEntryData();
	}

	private void resolveBatchContext() {
		Object dynamicBatchId = Executions.getCurrent().getAttribute("batchId");

		if (dynamicBatchId != null && !dynamicBatchId.toString().trim().isEmpty()) {
			scannedBatchId = dynamicBatchId.toString().trim();
		}

		if (scannedBatchId == null || scannedBatchId.isEmpty()) {
			String requestBatchId = Executions.getCurrent().getParameter("batchId");

			if (requestBatchId != null && !requestBatchId.trim().isEmpty()) {
				scannedBatchId = requestBatchId.trim();
			}
		}

		if (scannedBatchId == null || scannedBatchId.isEmpty()) {
			Object argumentBatchId = Executions.getCurrent().getArg().get("batchId");

			if (argumentBatchId != null && !argumentBatchId.toString().trim().isEmpty()) {
				scannedBatchId = argumentBatchId.toString().trim();
			}
		}

		if (scannedBatchId == null || scannedBatchId.isEmpty()) {
			Object sessionBatchId = Executions.getCurrent().getSession().getAttribute("OUTWARD_DATA_ENTRY_BATCH_ID");

			if (sessionBatchId != null && !sessionBatchId.toString().trim().isEmpty()) {
				scannedBatchId = sessionBatchId.toString().trim();
			}
		}

		if (scannedBatchId == null || scannedBatchId.isEmpty()) {
			throw new IllegalArgumentException("Scanned batch ID is required");
		}

		scannedBatchId = scannedBatchId.trim();
	}

	private void registerEvents() {
		outwardChequeDataEntryBtnBack.addEventListener("onClick", event -> backToDataEntryList());

		outwardChequeDataEntryBtnImageToggle.addEventListener("onClick", event -> toggleChequeImage());

		outwardChequeDataEntryBtnZoomOut.addEventListener("onClick", event -> zoomOut());

		outwardChequeDataEntryBtnZoomIn.addEventListener("onClick", event -> zoomIn());

		outwardChequeDataEntryBtnRotate.addEventListener("onClick", event -> rotateImage());

		outwardChequeDataEntryBtnReset.addEventListener("onClick", event -> resetImage());

		outwardChequeDataEntryBtnPrevious.addEventListener("onClick", event -> previousCheque());

		outwardChequeDataEntryBtnNext.addEventListener("onClick", event -> nextCheque());

		outwardChequeDataEntryBtnSaveNext.addEventListener("onClick", event -> saveAndNext());

		outwardChequeDataEntryBtnReject.addEventListener("onClick", event -> openRejectWindow());

		outwardChequeDataEntryBtnConfirmReject.addEventListener("onClick", event -> requestReject());

		outwardChequeDataEntryBtnCancelReject.addEventListener("onClick", event -> closeRejectWindow());

		outwardChequeDataEntryBtnSubmit.addEventListener("onClick", event -> submitToChecker());
	}

	private void loadDataEntryData() {
		try {
			ScanBatch scanBatch = scanService.getBatchById(scannedBatchId);

			if (scanBatch == null) {
				throw new IllegalStateException("Scan batch not found: " + scannedBatchId);
			}

			scanChequeList = scanService.getChequesByBatchId(scannedBatchId);

			if (scanChequeList == null) {
				scanChequeList = new ArrayList<>();
			}

			outwardBatchId = outwardBatchService.getOutwardBatchIdByScannedBatchId(scannedBatchId);

			outwardChequeList = new ArrayList<>();

			if (outwardBatchId != null && !outwardBatchId.trim().isEmpty()) {

				outwardBatchId = outwardBatchId.trim();

				outwardBatch = outwardBatchService.getBatchById(outwardBatchId);

				List<OutwardCheque> existing = outwardChequeService.getChequesByBatchId(outwardBatchId);

				if (existing != null) {
					outwardChequeList.addAll(existing);
				}
			}

			List<OutwardCheque> ordered = new ArrayList<>();

			for (ScanCheque scanCheque : scanChequeList) {
				OutwardCheque existing = findExistingOutwardCheque(scanCheque, outwardChequeList);

				if (existing != null) {
					ordered.add(existing);
				} else {
					ordered.add(buildOutwardChequeFromScan(scanCheque));
				}
			}

			outwardChequeList = ordered;

			if (outwardBatch == null) {
				outwardBatch = createDisplayBatch(scanBatch);
			}

			updateBatchSummary();

			currentChequeIndex = findFirstPendingCheque();

			if (currentChequeIndex >= 0) {
				loadCurrentCheque();

				outwardChequeDataEntryBtnSaveNext.setDisabled(false);

				outwardChequeDataEntryBtnReject.setDisabled(false);
			} else {
				showAllCompleted();
			}

			updateSubmitButton();

		} catch (Exception exception) {
			clearPage();

			showStatus("Unable to load cheque data: " + safeExceptionMessage(exception));
		}
	}

	private OutwardBatch createDisplayBatch(ScanBatch scanBatch) {
		if (scanBatch == null) {
			return null;
		}

		OutwardBatch batch = new OutwardBatch();

		String batchId = outwardBatchId != null && !outwardBatchId.trim().isEmpty() ? outwardBatchId.trim()
				: scannedBatchId.trim();

		batch.setOutwardBatchId(batchId);
		batch.setBatchReferenceId(scanBatch.getBatchReferenceId());
		batch.setActualChequeCount(scanBatch.getActualChequeCount());
		batch.setActualTotalAmount(scanBatch.getActualTotalAmount());
		batch.setBatchStatus(scanBatch.getBatchStatus());
		batch.setUploadedBy(scanBatch.getUploadedBy());
		batch.setUploadedAt(scanBatch.getUploadedAt());

		return batch;
	}

	private OutwardCheque buildOutwardChequeFromScan(ScanCheque scanCheque) {

		OutwardCheque cheque = new OutwardCheque();

		cheque.setOutwardBatchId(outwardBatchId);
		cheque.setChequeNumber(scanCheque.getChequeNumber());
		cheque.setMicrCode(scanCheque.getMicrCode());
		cheque.setDraweeName(scanCheque.getDraweeName());
		cheque.setDraweeAccountNumber(scanCheque.getDraweeAccountNumber());
		cheque.setPayeeName(scanCheque.getPayeeName());
		cheque.setPayeeAccountNumber(scanCheque.getPayeeAccountNumber());
		cheque.setChequeAmount(scanCheque.getChequeAmount());
		cheque.setChequeDate(scanCheque.getChequeDate());
		cheque.setAccountId(scanCheque.getAccountId());
		cheque.setCreatedAt(scanCheque.getCreatedAt());
		cheque.setCityCode(scanCheque.getCityCode());
		cheque.setBankCode(scanCheque.getBankCode());
		cheque.setBranchCode(scanCheque.getBranchCode());
		cheque.setChequeImageFront(scanCheque.getChequeImageFront());
		cheque.setChequeImageBack(scanCheque.getChequeImageBack());
		cheque.setChequeStatus(STATUS_PENDING_DATA_ENTRY);

		return cheque;
	}

	private OutwardCheque findExistingOutwardCheque(ScanCheque scanCheque, List<OutwardCheque> existingCheques) {

		if (scanCheque == null || existingCheques == null) {
			return null;
		}

		String number = safeValue(scanCheque.getChequeNumber()).trim();

		String front = safeValue(scanCheque.getChequeImageFront()).trim();

		String back = safeValue(scanCheque.getChequeImageBack()).trim();

		for (OutwardCheque cheque : existingCheques) {
			if (cheque == null) {
				continue;
			}

			if (!front.isEmpty() && front.equalsIgnoreCase(safeValue(cheque.getChequeImageFront()).trim())) {
				return cheque;
			}

			if (!back.isEmpty() && back.equalsIgnoreCase(safeValue(cheque.getChequeImageBack()).trim())) {
				return cheque;
			}

			if (!number.isEmpty() && number.equalsIgnoreCase(safeValue(cheque.getChequeNumber()).trim())) {
				return cheque;
			}
		}

		return null;
	}

	private ScanCheque findMatchingScanCheque(OutwardCheque cheque) {

		if (cheque == null || scanChequeList == null) {
			return null;
		}

		String number = safeValue(cheque.getChequeNumber()).trim();

		String front = safeValue(cheque.getChequeImageFront()).trim();

		String back = safeValue(cheque.getChequeImageBack()).trim();

		for (ScanCheque scanCheque : scanChequeList) {
			if (scanCheque == null) {
				continue;
			}

			if (!front.isEmpty() && front.equalsIgnoreCase(safeValue(scanCheque.getChequeImageFront()).trim())) {
				return scanCheque;
			}

			if (!back.isEmpty() && back.equalsIgnoreCase(safeValue(scanCheque.getChequeImageBack()).trim())) {
				return scanCheque;
			}

			if (!number.isEmpty() && number.equalsIgnoreCase(safeValue(scanCheque.getChequeNumber()).trim())) {
				return scanCheque;
			}
		}

		if (currentChequeIndex >= 0 && currentChequeIndex < scanChequeList.size()) {
			return scanChequeList.get(currentChequeIndex);
		}

		return null;
	}

	private boolean isEditableCheque(OutwardCheque cheque) {
		return cheque != null && STATUS_PENDING_DATA_ENTRY.equalsIgnoreCase(safeValue(cheque.getChequeStatus()).trim());
	}

	private int findFirstPendingCheque() {
		return findNextPendingCheque(0);
	}

	private int findNextPendingCheque(int startIndex) {
		if (outwardChequeList == null) {
			return -1;
		}

		for (int index = Math.max(0, startIndex); index < outwardChequeList.size(); index++) {

			if (isEditableCheque(outwardChequeList.get(index))) {
				return index;
			}
		}

		return -1;
	}

	private int findPreviousNavigableCheque(int startIndex) {
		if (outwardChequeList == null) {
			return -1;
		}

		for (int index = startIndex; index >= 0; index--) {

			if (outwardChequeList.get(index) != null) {
				return index;
			}
		}

		return -1;
	}

	private int findNextNavigableCheque(int startIndex) {
		if (outwardChequeList == null) {
			return -1;
		}

		for (int index = Math.max(0, startIndex); index < outwardChequeList.size(); index++) {

			if (outwardChequeList.get(index) != null) {
				return index;
			}
		}

		return -1;
	}

	private void loadCurrentCheque() {
		if (currentChequeIndex < 0 || currentChequeIndex >= outwardChequeList.size()) {
			return;
		}

		OutwardCheque cheque = outwardChequeList.get(currentChequeIndex);

		ScanCheque scanCheque = findMatchingScanCheque(cheque);

		if (cheque.getOutwardChequeId() != null && !cheque.getOutwardChequeId().trim().isEmpty()) {

			populateChequeFields(cheque);

		} else if (scanCheque != null) {

			populateChequeFieldsFromScan(scanCheque);

		} else {

			populateChequeFields(cheque);
		}

		resetImageState();

		if (scanCheque != null) {
			loadFrontImage(scanCheque);
		} else {
			loadImageFromOutwardCheque(cheque);
		}

		updateSummary();
		updateSaveButton();
		updateNavigationButtons();
	}

	private void populateChequeFields(OutwardCheque cheque) {

		outwardChequeDataEntryTxtChequeNumber.setValue(safeValue(cheque.getChequeNumber()));

		outwardChequeDataEntryTxtAmount
				.setValue(cheque.getChequeAmount() == null ? "" : cheque.getChequeAmount().toPlainString());

		outwardChequeDataEntryDtChequeDate.setValue(cheque.getChequeDate());

		outwardChequeDataEntryTxtMicrCode.setValue(safeValue(cheque.getMicrCode()));

		outwardChequeDataEntryTxtPayeeAccount.setValue(safeValue(cheque.getPayeeAccountNumber()));

		outwardChequeDataEntryTxtPayeeName.setValue(safeValue(cheque.getPayeeName()));

		outwardChequeDataEntryTxtDraweeName.setValue(safeValue(cheque.getDraweeName()));
	}

	private void populateChequeFieldsFromScan(ScanCheque scanCheque) {

		outwardChequeDataEntryTxtChequeNumber.setValue(safeValue(scanCheque.getChequeNumber()));

		outwardChequeDataEntryTxtAmount
				.setValue(scanCheque.getChequeAmount() == null ? "" : scanCheque.getChequeAmount().toPlainString());

		outwardChequeDataEntryDtChequeDate.setValue(scanCheque.getChequeDate());

		outwardChequeDataEntryTxtMicrCode.setValue(safeValue(scanCheque.getMicrCode()));

		outwardChequeDataEntryTxtPayeeAccount.setValue(safeValue(scanCheque.getPayeeAccountNumber()));

		outwardChequeDataEntryTxtPayeeName.setValue(safeValue(scanCheque.getPayeeName()));

		outwardChequeDataEntryTxtDraweeName.setValue(safeValue(scanCheque.getDraweeName()));
	}

	private void updateSummary() {
		if (currentChequeIndex < 0 || currentChequeIndex >= outwardChequeList.size()) {

			showAllCompleted();
			return;
		}

		OutwardCheque cheque = outwardChequeList.get(currentChequeIndex);

		String number = safeValue(cheque.getChequeNumber());

		if (number.isEmpty()) {
			ScanCheque scanCheque = findMatchingScanCheque(cheque);

			if (scanCheque != null) {
				number = safeValue(scanCheque.getChequeNumber());
			}
		}

		int total = scanChequeList == null ? 0 : scanChequeList.size();

		outwardChequeDataEntryLblRecord.setValue("CHEQUE " + (currentChequeIndex + 1) + " OF " + total);

		outwardChequeDataEntryLblSummaryChequeNumber.setValue(number);
	}

	private void updateBatchSummary() {
		int total = scanChequeList == null ? 0 : scanChequeList.size();

		int completed = 0;

		if (outwardBatchId != null && !outwardBatchId.trim().isEmpty()) {

			completed = outwardChequeService.getDataEnteredCountByBatchId(outwardBatchId);
		}

		int remaining = Math.max(total - completed, 0);

		outwardChequeDataEntryLblBatchId
				.setValue(outwardBatchId != null && !outwardBatchId.trim().isEmpty() ? outwardBatchId : scannedBatchId);

		outwardChequeDataEntryLblTotal.setValue(String.valueOf(total));

		outwardChequeDataEntryLblRemaining.setValue(String.valueOf(remaining));
	}

	private void updateSaveButton() {
		if (currentChequeIndex < 0 || currentChequeIndex >= outwardChequeList.size()) {

			outwardChequeDataEntryBtnSaveNext.setDisabled(true);

			return;
		}

		int next = findNextPendingCheque(currentChequeIndex + 1);

		outwardChequeDataEntryBtnSaveNext.setLabel(next >= 0 ? "SAVE & NEXT" : "SAVE");

		outwardChequeDataEntryBtnSaveNext.setDisabled(false);
	}

	private void updateSubmitButton() {
		if (scanChequeList == null || scanChequeList.isEmpty() || outwardBatchId == null
				|| outwardBatchId.trim().isEmpty()) {

			outwardChequeDataEntryBtnSubmit.setDisabled(true);

			return;
		}

		int completed = outwardChequeService.getDataEnteredCountByBatchId(outwardBatchId);

		outwardChequeDataEntryBtnSubmit.setDisabled(completed != scanChequeList.size());
	}

	private void updateNavigationButtons() {
		if (currentChequeIndex < 0 || outwardChequeList == null || outwardChequeList.isEmpty()) {

			outwardChequeDataEntryBtnPrevious.setDisabled(true);

			outwardChequeDataEntryBtnNext.setDisabled(true);

			return;
		}

		outwardChequeDataEntryBtnPrevious.setDisabled(findPreviousNavigableCheque(currentChequeIndex - 1) < 0);

		outwardChequeDataEntryBtnNext.setDisabled(findNextNavigableCheque(currentChequeIndex + 1) < 0);
	}

	private void previousCheque() {
		int index = findPreviousNavigableCheque(currentChequeIndex - 1);

		if (index < 0) {
			return;
		}

		currentChequeIndex = index;
		loadCurrentCheque();
		hideStatus();
	}

	private void nextCheque() {
		int index = findNextNavigableCheque(currentChequeIndex + 1);

		if (index < 0) {
			return;
		}

		currentChequeIndex = index;
		loadCurrentCheque();
		hideStatus();
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

			if (cheque == null) {
				cheque = new OutwardCheque();
				outwardChequeList.set(currentChequeIndex, cheque);
			}

			populateOutwardChequeFromFields(cheque);

			cheque.setOutwardBatchId(outwardBatchId);
			cheque.setChequeStatus(STATUS_PENDING_VERIFICATION);

			OutwardCheque saved = outwardChequeService.saveMakerCheque(scannedBatchId, cheque);

			if (saved == null) {
				throw new IllegalStateException("Unable to save cheque data");
			}

			outwardChequeList.set(currentChequeIndex, saved);

			outwardBatchId = saved.getOutwardBatchId();

			if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {

				throw new IllegalStateException("Outward batch ID was not returned after save");
			}

			outwardBatchId = outwardBatchId.trim();

			outwardBatch = outwardBatchService.getBatchById(outwardBatchId);

			updateBatchSummary();

			int next = findNextPendingCheque(currentChequeIndex + 1);

			if (next >= 0) {

				currentChequeIndex = next;
				loadCurrentCheque();

				showStatus("Cheque saved successfully.");

			} else {

				showAllCompleted();

				showStatus("All cheque data has been completed.");
			}

			updateSubmitButton();

		} catch (Exception exception) {

			showStatus("Unable to save cheque data: " + safeExceptionMessage(exception));
		}
	}

	private void populateOutwardChequeFromFields(OutwardCheque cheque) {

		cheque.setChequeNumber(outwardChequeDataEntryTxtChequeNumber.getValue().trim());

		cheque.setMicrCode(outwardChequeDataEntryTxtMicrCode.getValue().trim());

		cheque.setPayeeAccountNumber(outwardChequeDataEntryTxtPayeeAccount.getValue().trim());

		cheque.setPayeeName(outwardChequeDataEntryTxtPayeeName.getValue().trim());

		cheque.setDraweeName(outwardChequeDataEntryTxtDraweeName.getValue().trim());

		cheque.setChequeAmount(new BigDecimal(outwardChequeDataEntryTxtAmount.getValue().trim()));

		if (outwardChequeDataEntryDtChequeDate.getValue() != null) {

			cheque.setChequeDate(new Date(outwardChequeDataEntryDtChequeDate.getValue().getTime()));
		}
	}

	private boolean validateFields() {
		String chequeNumber = outwardChequeDataEntryTxtChequeNumber.getValue().trim();

		String amount = outwardChequeDataEntryTxtAmount.getValue().trim();

		String micr = outwardChequeDataEntryTxtMicrCode.getValue().trim();

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
			if (new BigDecimal(amount).compareTo(BigDecimal.ZERO) < 0) {

				showStatus("Cheque amount cannot be negative.");

				return false;
			}

		} catch (NumberFormatException exception) {

			showStatus("Enter a valid cheque amount.");

			return false;
		}

		if (outwardChequeDataEntryDtChequeDate.getValue() == null) {

			showStatus("Cheque date is required.");

			return false;
		}

		if (micr.isEmpty()) {
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

		OutwardCheque cheque = outwardChequeList.get(currentChequeIndex);

		ScanCheque scanCheque = findMatchingScanCheque(cheque);

		if (showingFrontImage) {

			showingFrontImage = false;

			outwardChequeDataEntryBtnImageToggle.setLabel("FRONT VIEW");

			loadImage(scanCheque != null ? scanCheque.getChequeImageBack() : cheque.getChequeImageBack());

		} else {

			showingFrontImage = true;

			outwardChequeDataEntryBtnImageToggle.setLabel("BACK VIEW");

			loadImage(scanCheque != null ? scanCheque.getChequeImageFront() : cheque.getChequeImageFront());
		}
	}

	private void loadFrontImage(ScanCheque scanCheque) {

		showingFrontImage = true;

		outwardChequeDataEntryBtnImageToggle.setLabel("BACK VIEW");

		loadImage(scanCheque == null ? null : scanCheque.getChequeImageFront());
	}

	private void loadImageFromOutwardCheque(OutwardCheque cheque) {

		loadImage(showingFrontImage ? cheque.getChequeImageFront() : cheque.getChequeImageBack());
	}

	private void loadImage(String imagePath) {
		if (imagePath == null || imagePath.trim().isEmpty()) {

			showImageUnavailable();
			return;
		}

		try {
			String path = imagePath.trim();

			if (!path.startsWith("/") && !path.startsWith("http://") && !path.startsWith("https://")) {

				path = "/" + path;
			}

			outwardChequeDataEntryChequeImage.setSrc(path);

			outwardChequeDataEntryChequeImage.setVisible(true);

			outwardChequeDataEntryLblImageMessage.setVisible(false);

			resetImage();

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

		outwardChequeDataEntryChequeImage.setStyle("transform:scale(1) rotate(0deg);");
	}

	private void zoomIn() {
		imageZoom = Math.min(imageZoom + 0.1, 3.0);

		applyImageTransform();
	}

	private void zoomOut() {
		imageZoom = Math.max(imageZoom - 0.1, 0.5);

		applyImageTransform();
	}

	private void rotateImage() {
		imageRotation += 90;

		if (imageRotation >= 360) {
			imageRotation = 0;
		}

		applyImageTransform();
	}

	private void resetImage() {
		imageZoom = 1.0;
		imageRotation = 0;

		applyImageTransform();
	}

	private void applyImageTransform() {
		outwardChequeDataEntryChequeImage
				.setStyle("transform:scale(" + imageZoom + ") rotate(" + imageRotation + "deg);");
	}

	private void openRejectWindow() {
		if (currentChequeIndex < 0 || currentChequeIndex >= outwardChequeList.size()) {
			return;
		}

		OutwardCheque cheque = outwardChequeList.get(currentChequeIndex);

		if (cheque == null || cheque.getOutwardChequeId() == null || cheque.getOutwardChequeId().trim().isEmpty()) {

			showStatus("Save the cheque before creating a rejection request.");

			return;
		}

		outwardChequeDataEntryTxtRejectRemarks.setValue("");

		outwardChequeDataEntryRejectWindow.setVisible(true);

		outwardChequeDataEntryTxtRejectRemarks.setFocus(true);
	}

	private void closeRejectWindow() {
		outwardChequeDataEntryRejectWindow.setVisible(false);
	}

	private void requestReject() {
		if (currentChequeIndex < 0 || currentChequeIndex >= outwardChequeList.size()) {
			return;
		}

		String remarks = outwardChequeDataEntryTxtRejectRemarks.getValue().trim();

		if (remarks.isEmpty()) {
			showStatus("Please enter rejection remarks.");

			return;
		}

		OutwardCheque cheque = outwardChequeList.get(currentChequeIndex);

		if (cheque == null || cheque.getOutwardChequeId() == null || cheque.getOutwardChequeId().trim().isEmpty()) {

			closeRejectWindow();

			showStatus("Save the cheque before creating a rejection request.");

			return;
		}

		boolean updated = outwardChequeService.updateChequeStatus(cheque.getOutwardChequeId(),
				STATUS_REJECTION_REQUEST);

		if (!updated) {
			showStatus("Unable to create rejection request.");

			return;
		}

		cheque.setChequeStatus(STATUS_REJECTION_REQUEST);

		closeRejectWindow();

		int next = findNextPendingCheque(currentChequeIndex + 1);

		if (next >= 0) {

			currentChequeIndex = next;
			loadCurrentCheque();

		} else {

			showAllCompleted();
		}

		updateBatchSummary();
		updateSubmitButton();

		showStatus("Rejection request created for cheque " + safeValue(cheque.getChequeNumber()) + ".");
	}

	private void submitToChecker() {
		if (scanChequeList == null || scanChequeList.isEmpty()) {

			showStatus("There are no cheques to submit.");

			return;
		}

		if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {

			showStatus("Save the cheques before submission.");

			return;
		}

		int completed = outwardChequeService.getDataEnteredCountByBatchId(outwardBatchId);

		if (completed != scanChequeList.size()) {

			showStatus("All cheques must be completed before submission.");

			updateSubmitButton();

			return;
		}

		showSubmitConfirmation(scanChequeList.size(), completed);
	}

	private void showSubmitConfirmation(int total, int completed) {

		Window confirmation = new Window();

		confirmation.setTitle("SUBMIT TO CHECKER");

		confirmation.setBorder("normal");
		confirmation.setClosable(false);
		confirmation.setMode("modal");
		confirmation.setWidth("460px");
		confirmation.setPosition("center");

		confirmation.setStyle("border-radius:8px;");

		Vlayout content = new Vlayout();

		content.setWidth("100%");
		content.setSpacing("14px");

		content.setStyle("padding:24px;box-sizing:border-box;");

		Label title = new Label("READY FOR CHECKER VERIFICATION");

		title.setStyle("font-size:17px;" + "font-weight:700;" + "color:#17375e;");

		content.appendChild(title);

		Label message = new Label("Batch " + outwardBatchId + " has completed all cheque data entry.");

		message.setStyle("font-size:13px;" + "color:#52657d;");

		content.appendChild(message);

		Label completedLabel = new Label("Completed Cheques: " + completed + " / " + total);

		completedLabel.setStyle("font-size:13px;" + "font-weight:600;" + "color:#34445c;");

		content.appendChild(completedLabel);

		Label status = new Label("After submission: " + STATUS_PENDING_CHECKER_PROCESS);

		status.setStyle("font-size:12px;" + "font-weight:700;" + "color:#52657d;" + "background:#eef3f8;"
				+ "padding:9px 12px;" + "border-radius:5px;");

		content.appendChild(status);

		Hlayout actions = new Hlayout();

		actions.setWidth("100%");
		actions.setSpacing("10px");

		Button cancel = new Button("CANCEL");

		cancel.setHflex("1");
		cancel.setHeight("36px");

		cancel.setStyle("background:#fff;" + "border:1px solid #cbd5e1;" + "border-radius:5px;" + "color:#52657d;"
				+ "font-size:11px;" + "font-weight:700;");

		Button submit = new Button("SUBMIT TO CHECKER");

		submit.setHflex("1");
		submit.setHeight("36px");

		submit.setStyle("background:#0b1d3a;" + "border:1px solid #0b1d3a;" + "border-radius:5px;" + "color:#fff;"
				+ "font-size:11px;" + "font-weight:700;");

		cancel.addEventListener("onClick", event -> confirmation.detach());

		submit.addEventListener("onClick", event -> {
			confirmation.detach();
			completeBatchSubmission();
		});

		actions.appendChild(cancel);
		actions.appendChild(submit);

		content.appendChild(actions);

		confirmation.appendChild(content);

		outwardChequeDataEntryWin.appendChild(confirmation);

		confirmation.doModal();
	}

	private void completeBatchSubmission() {
		try {

			boolean updated = outwardBatchService.updateOutWardBatchStatus(outwardBatchId,
					STATUS_PENDING_CHECKER_PROCESS);

			if (!updated) {
				showStatus("Unable to submit batch to Checker.");

				return;
			}

			showSubmissionSuccess();

		} catch (Exception exception) {

			showStatus("Unable to submit batch to Checker: " + safeExceptionMessage(exception));
		}
	}

	private void showSubmissionSuccess() {
		Window success = new Window();

		success.setTitle("BATCH SUBMITTED");

		success.setBorder("normal");
		success.setClosable(false);
		success.setMode("modal");
		success.setWidth("480px");
		success.setPosition("center");

		success.setStyle("border-radius:8px;");

		Vlayout content = new Vlayout();

		content.setWidth("100%");
		content.setSpacing("14px");

		content.setStyle("padding:26px;box-sizing:border-box;");

		Label title = new Label("BATCH SUBMITTED SUCCESSFULLY");

		title.setStyle("font-size:18px;" + "font-weight:700;" + "color:#17375e;");

		content.appendChild(title);

		Label batch = new Label("Batch: " + outwardBatchId);

		batch.setStyle("font-size:14px;" + "font-weight:700;" + "color:#34445c;");

		content.appendChild(batch);

		int total = scanChequeList == null ? 0 : scanChequeList.size();

		Label completed = new Label("Completed: " + total + " / " + total + " cheques");

		completed.setStyle("font-size:13px;" + "color:#52657d;");

		content.appendChild(completed);

		Label status = new Label("Status: " + STATUS_PENDING_CHECKER_PROCESS);

		status.setStyle("font-size:12px;" + "font-weight:700;" + "color:#52657d;" + "background:#eef3f8;"
				+ "padding:10px 12px;" + "border-radius:5px;");

		content.appendChild(status);

		Hlayout actions = new Hlayout();

		actions.setWidth("100%");
		actions.setSpacing("10px");

		Button nextBatch = new Button("NEXT BATCH");

		nextBatch.setHflex("1");
		nextBatch.setHeight("38px");

		nextBatch.setStyle("background:#2f6da5;" + "border:1px solid #2f6da5;" + "border-radius:5px;" + "color:#fff;"
				+ "font-size:11px;" + "font-weight:700;");

		Button dashboard = new Button("GO TO DASHBOARD");

		dashboard.setHflex("1");
		dashboard.setHeight("38px");

		dashboard.setStyle("background:#0b1d3a;" + "border:1px solid #0b1d3a;" + "border-radius:5px;" + "color:#fff;"
				+ "font-size:11px;" + "font-weight:700;");

		nextBatch.addEventListener("onClick", event -> {
			success.detach();
			backToDataEntryList();
		});

		dashboard.addEventListener("onClick", event -> {
			success.detach();
			goToDashboard();
		});

		actions.appendChild(nextBatch);
		actions.appendChild(dashboard);

		content.appendChild(actions);

		success.appendChild(content);

		outwardChequeDataEntryWin.appendChild(success);

		success.doModal();
	}

	private void showAllCompleted() {
		currentChequeIndex = -1;

		clearChequeFields();

		outwardChequeDataEntryBtnSaveNext.setDisabled(true);

		outwardChequeDataEntryBtnReject.setDisabled(true);

		outwardChequeDataEntryBtnPrevious.setDisabled(true);

		outwardChequeDataEntryBtnNext.setDisabled(true);

		outwardChequeDataEntryLblRecord.setValue("ALL CHEQUES COMPLETED");

		outwardChequeDataEntryLblSummaryChequeNumber.setValue("-");

		outwardChequeDataEntryLblRemaining.setValue("0");
	}

	private void backToDataEntryList() {
		Component component = outwardChequeDataEntryWin;

		while (component != null) {

			if ("mainContentArea".equals(component.getId()) && component instanceof org.zkoss.zul.Include) {

				org.zkoss.zul.Include include = (org.zkoss.zul.Include) component;

				include.setDynamicProperty("batchId", null);

				include.setDynamicProperty("mode", null);

				include.setSrc("/outward/maker/data-entry.zul");

				return;
			}

			component = component.getParent();
		}

		Executions.sendRedirect("/outward/maker/data-entry.zul");
	}

	private void goToDashboard() {
		Component component = outwardChequeDataEntryWin;

		while (component != null) {

			if ("mainContentArea".equals(component.getId()) && component instanceof org.zkoss.zul.Include) {

				org.zkoss.zul.Include include = (org.zkoss.zul.Include) component;

				include.setDynamicProperty("batchId", null);

				include.setDynamicProperty("mode", null);

				include.setSrc("/outward/maker/dashboard.zul");

				return;
			}

			component = component.getParent();
		}

		Executions.sendRedirect("/outward/maker/dashboard.zul");
	}

	private void clearChequeFields() {
		outwardChequeDataEntryTxtChequeNumber.setValue("");

		outwardChequeDataEntryTxtAmount.setValue("");

		outwardChequeDataEntryDtChequeDate.setValue(null);

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

		outwardChequeDataEntryBtnPrevious.setDisabled(true);

		outwardChequeDataEntryBtnNext.setDisabled(true);

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

	private String safeValue(String value) {
		return value == null ? "" : value;
	}

	private String safeExceptionMessage(Exception exception) {

		if (exception == null || exception.getMessage() == null || exception.getMessage().trim().isEmpty()) {

			return "Unexpected error";
		}

		return exception.getMessage();
	}
}