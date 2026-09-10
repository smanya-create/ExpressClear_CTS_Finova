package com.iispl.cts.controller.outward.maker;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.iispl.cts.entity.RejectedReason;
import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.ScanBatch;
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

	private static final String MODE_NEW = "PENDING_DATA_ENTRY";
	private static final String MODE_SEND_BACK = "SEND_BACK_MAKER";

	private static final String STATUS_PENDING_DATA_ENTRY = "PENDING_DATA_ENTRY";
	private static final String STATUS_PENDING_VERIFICATION = "PENDING_VERIFICATION";
	private static final String STATUS_SEND_BACK_MAKER = "SEND_BACK_MAKER";
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
	private Combobox outwardChequeDataEntryCmbRejectReason;

	private Window outwardChequeDataEntryRejectWindow;

	private OutwardBatchService outwardBatchService;
	private OutwardChequeService outwardChequeService;
	private ScanService scanService;
	private RejectedReasonService rejectedReasonService;

	private String outwardBatchId;
	private String scannedBatchId;
	private String mode;

	private OutwardBatch outwardBatch;
	private List<OutwardCheque> outwardChequeList;
	private List<ScanCheque> scanChequeList;

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

		outwardChequeDataEntryCmbRejectReason = (Combobox) outwardChequeDataEntryRejectWindow
				.getFellow("outwardChequeDataEntryCmbRejectReason");

		outwardChequeDataEntryTxtRejectRemarks = (Textbox) outwardChequeDataEntryRejectWindow
				.getFellow("outwardChequeDataEntryTxtRejectRemarks");

		outwardBatchService = new OutwardBatchServiceImpl();
		outwardChequeService = new OutwardChequeServiceImpl();
		scanService = new ScanServiceImpl();
		rejectedReasonService = RejectedReasonServiceImpl.getInstance();

		outwardChequeDataEntryRejectWindow.setVisible(false);
		outwardChequeDataEntryBtnSubmit.setDisabled(true);

		resolveBatchContext();
		registerEvents();
		loadRejectedReasons();
		loadDataEntryData();
	}

	private void resolveBatchContext() {

		Object dynamicMode = Executions.getCurrent().getAttribute("mode");

		if (dynamicMode != null && !dynamicMode.toString().trim().isEmpty()) {

			mode = dynamicMode.toString().trim();
		}

		Object dynamicBatchId = Executions.getCurrent().getAttribute("batchId");

		if (dynamicBatchId != null && !dynamicBatchId.toString().trim().isEmpty()) {

			if (MODE_SEND_BACK.equalsIgnoreCase(mode)) {

				outwardBatchId = dynamicBatchId.toString().trim();

			} else {

				scannedBatchId = dynamicBatchId.toString().trim();
			}
		}

		if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {

			String requestBatchId = Executions.getCurrent().getParameter("batchId");

			if (requestBatchId != null && !requestBatchId.trim().isEmpty()) {

				if (MODE_SEND_BACK.equalsIgnoreCase(mode)) {

					outwardBatchId = requestBatchId.trim();

				} else {

					scannedBatchId = requestBatchId.trim();
				}
			}
		}

		if (mode == null || mode.trim().isEmpty()) {

			String requestMode = Executions.getCurrent().getParameter("mode");

			if (requestMode != null && !requestMode.trim().isEmpty()) {

				mode = requestMode.trim();
			}
		}

		if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {

			Object argumentBatchId = Executions.getCurrent().getArg().get("batchId");

			if (argumentBatchId != null && !argumentBatchId.toString().trim().isEmpty()) {

				if (MODE_SEND_BACK.equalsIgnoreCase(mode)) {

					outwardBatchId = argumentBatchId.toString().trim();

				} else {

					scannedBatchId = argumentBatchId.toString().trim();
				}
			}
		}

		if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {

			Object sessionBatchId = Executions.getCurrent().getSession().getAttribute("OUTWARD_DATA_ENTRY_BATCH_ID");

			if (sessionBatchId != null && !sessionBatchId.toString().trim().isEmpty()) {

				scannedBatchId = sessionBatchId.toString().trim();
			}
		}

		if (MODE_SEND_BACK.equalsIgnoreCase(mode)) {

			if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {

				throw new IllegalArgumentException("Outward batch ID is required");
			}

		} else {

			if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {

				throw new IllegalArgumentException("Scanned batch ID is required");
			}
		}
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

			if (MODE_SEND_BACK.equalsIgnoreCase(mode)) {

				loadSendBackData();

			} else {

				loadFreshScanData();
			}

			updateBatchSummary();

			currentChequeIndex = findFirstPendingCheque();

			if (currentChequeIndex >= 0) {

				loadCurrentCheque();

				outwardChequeDataEntryBtnSaveNext.setDisabled(false);

				outwardChequeDataEntryBtnReject.setDisabled(MODE_SEND_BACK.equalsIgnoreCase(mode));

			} else {

				clearChequeFields();

				outwardChequeDataEntryBtnSaveNext.setDisabled(true);

				outwardChequeDataEntryBtnReject.setDisabled(true);

				outwardChequeDataEntryLblRecord.setValue("ALL CHEQUES COMPLETED");

				outwardChequeDataEntryLblSummaryChequeNumber.setValue("-");

				updateNavigationButtons();
			}

			updateSubmitButton();

		} catch (Exception exception) {

			clearPage();

			showStatus("Unable to load cheque data: " + safeExceptionMessage(exception));
		}
	}

	private void loadFreshScanData() {

		if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {

			throw new IllegalStateException("Scanned batch ID is required");
		}

		scannedBatchId = scannedBatchId.trim();

		ScanBatch scanBatch = scanService.getBatchById(scannedBatchId);

		if (scanBatch == null) {

			throw new IllegalStateException("Scan batch not found: " + scannedBatchId);
		}

		scanChequeList = scanService.getChequesByBatchId(scannedBatchId);

		if (scanChequeList == null) {

			scanChequeList = new ArrayList<>();
		}

		outwardBatchId = null;
		outwardBatch = null;

		String existingOutwardBatchId = outwardBatchService.getOutwardBatchIdByScannedBatchId(scannedBatchId);

		if (existingOutwardBatchId != null && !existingOutwardBatchId.trim().isEmpty()) {

			outwardBatchId = existingOutwardBatchId.trim();

			outwardBatch = outwardBatchService.getBatchById(outwardBatchId);
		}

		List<OutwardCheque> existingCheques = new ArrayList<>();

		if (outwardBatchId != null && !outwardBatchId.trim().isEmpty()) {

			List<OutwardCheque> loadedCheques = outwardChequeService.getChequesByBatchId(outwardBatchId);

			if (loadedCheques != null) {

				existingCheques.addAll(loadedCheques);
			}
		}

		outwardChequeList = new ArrayList<>();

		for (ScanCheque scanCheque : scanChequeList) {

			OutwardCheque existing = findExistingOutwardCheque(scanCheque, existingCheques);

			if (existing != null) {

				outwardChequeList.add(existing);

			} else {

				outwardChequeList.add(buildOutwardChequeFromScan(scanCheque));
			}
		}

		outwardBatch = createDisplayBatch(scanBatch);
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

	private void loadSendBackData() {

		if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {

			throw new IllegalStateException("Outward batch ID is required");
		}

		outwardBatchId = outwardBatchId.trim();

		outwardBatch = outwardBatchService.getBatchById(outwardBatchId);

		if (outwardBatch == null) {

			throw new IllegalStateException("Outward batch not found: " + outwardBatchId);
		}

		scannedBatchId = outwardBatchService.getScannedBatchIdByOutwardBatchId(outwardBatchId);

		if (scannedBatchId == null || scannedBatchId.trim().isEmpty()) {

			throw new IllegalStateException("Scanned batch not found for outward batch: " + outwardBatchId);
		}

		scannedBatchId = scannedBatchId.trim();

		outwardChequeList = outwardChequeService.getChequesByBatchId(outwardBatchId);

		if (outwardChequeList == null) {

			outwardChequeList = new ArrayList<>();
		}

		scanChequeList = scanService.getChequesByBatchId(scannedBatchId);

		if (scanChequeList == null) {

			scanChequeList = new ArrayList<>();
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

			String status = safeValue(cheque.getChequeStatus()).trim();

			if (STATUS_PENDING_VERIFICATION.equalsIgnoreCase(status)
					|| STATUS_REJECTION_REQUEST.equalsIgnoreCase(status)) {

				continue;
			}

			if (STATUS_PENDING_CHECKER_PROCESS.equalsIgnoreCase(status)) {
				continue;
			}

			return index;
		}

		return -1;
	}

	private void loadCurrentCheque() {

		if (outwardChequeList == null || currentChequeIndex < 0 || currentChequeIndex >= outwardChequeList.size()) {

			return;
		}

		OutwardCheque cheque = outwardChequeList.get(currentChequeIndex);

		if (cheque == null) {
			return;
		}

		ScanCheque scanCheque = currentChequeIndex < scanChequeList.size() ? scanChequeList.get(currentChequeIndex)
				: findMatchingScanCheque(cheque);

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

	private ScanCheque findMatchingScanCheque(OutwardCheque outwardCheque) {

		if (outwardCheque == null || scanChequeList == null) {

			return null;
		}

		String frontImage = outwardCheque.getChequeImageFront();

		String backImage = outwardCheque.getChequeImageBack();

		if (frontImage != null && !frontImage.trim().isEmpty()) {

			for (ScanCheque scanCheque : scanChequeList) {

				if (scanCheque == null) {
					continue;
				}

				String scanFrontImage = safeValue(scanCheque.getChequeImageFront()).trim();

				if (frontImage.trim().equalsIgnoreCase(scanFrontImage)) {

					return scanCheque;
				}
			}
		}

		if (backImage != null && !backImage.trim().isEmpty()) {

			for (ScanCheque scanCheque : scanChequeList) {

				if (scanCheque == null) {
					continue;
				}

				String scanBackImage = safeValue(scanCheque.getChequeImageBack()).trim();

				if (backImage.trim().equalsIgnoreCase(scanBackImage)) {

					return scanCheque;
				}
			}
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

	private OutwardCheque findExistingOutwardCheque(ScanCheque scanCheque, List<OutwardCheque> existingCheques) {

		if (scanCheque == null || existingCheques == null || existingCheques.isEmpty()) {

			return null;
		}

		String chequeNumber = safeValue(scanCheque.getChequeNumber()).trim();

		String frontImage = safeValue(scanCheque.getChequeImageFront()).trim();

		String backImage = safeValue(scanCheque.getChequeImageBack()).trim();

		for (OutwardCheque cheque : existingCheques) {

			if (cheque == null) {
				continue;
			}

			String existingChequeNumber = safeValue(cheque.getChequeNumber()).trim();

			String existingFront = safeValue(cheque.getChequeImageFront()).trim();

			String existingBack = safeValue(cheque.getChequeImageBack()).trim();

			if (!frontImage.isEmpty() && frontImage.equalsIgnoreCase(existingFront)) {

				return cheque;
			}

			if (!backImage.isEmpty() && backImage.equalsIgnoreCase(existingBack)) {

				return cheque;
			}

			if (!chequeNumber.isEmpty() && chequeNumber.equalsIgnoreCase(existingChequeNumber)) {

				return cheque;
			}
		}

		return null;
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

		if (scanCheque == null) {

			clearChequeFields();

			return;
		}

		outwardChequeDataEntryTxtChequeNumber.setValue(safeValue(scanCheque.getChequeNumber()));

		outwardChequeDataEntryTxtAmount
				.setValue(scanCheque.getChequeAmount() == null ? "" : scanCheque.getChequeAmount().toPlainString());

		outwardChequeDataEntryDtChequeDate.setValue(scanCheque.getChequeDate());

		outwardChequeDataEntryTxtMicrCode.setValue(safeValue(scanCheque.getMicrCode()));

		outwardChequeDataEntryTxtPayeeAccount.setValue(safeValue(scanCheque.getPayeeAccountNumber()));

		outwardChequeDataEntryTxtPayeeName.setValue(safeValue(scanCheque.getPayeeName()));

		outwardChequeDataEntryTxtDraweeName.setValue(safeValue(scanCheque.getDraweeName()));
	}

	private OutwardCheque buildOutwardChequeFromScan(ScanCheque scanCheque) {

		if (scanCheque == null) {

			throw new IllegalArgumentException("Scan cheque cannot be null");
		}

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

	private String safeValue(String value) {

		return value == null ? "" : value;
	}

	private String safeExceptionMessage(Exception exception) {

		if (exception == null || exception.getMessage() == null || exception.getMessage().trim().isEmpty()) {

			return "Unexpected error";
		}

		return exception.getMessage();
	}

	private void updateBatchSummary() {

		int totalCheques = scanChequeList == null ? 0 : scanChequeList.size();

		int completedCheques = 0;

		if (outwardBatchId != null && !outwardBatchId.trim().isEmpty()) {

			completedCheques = outwardChequeService.getDataEnteredCountByBatchId(outwardBatchId);
		}

		int remainingCheques = Math.max(totalCheques - completedCheques, 0);

		String displayBatchId = outwardBatchId != null && !outwardBatchId.trim().isEmpty() ? outwardBatchId
				: scannedBatchId;

		outwardChequeDataEntryLblBatchId.setValue(safeValue(displayBatchId));

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

		String chequeNumber = safeValue(cheque.getChequeNumber());

		if (chequeNumber.isEmpty() && scanChequeList != null && currentChequeIndex < scanChequeList.size()) {

			chequeNumber = safeValue(scanChequeList.get(currentChequeIndex).getChequeNumber());
		}

		outwardChequeDataEntryLblSummaryChequeNumber.setValue(chequeNumber);
	}

	private void updateSubmitButton() {

		if (scanChequeList == null || scanChequeList.isEmpty()) {

			outwardChequeDataEntryBtnSubmit.setDisabled(true);

			return;
		}

		if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {

			outwardChequeDataEntryBtnSubmit.setDisabled(true);

			return;
		}

		int totalCheques = scanChequeList.size();

		int completedCheques = outwardChequeService.getDataEnteredCountByBatchId(outwardBatchId);

		boolean allCompleted = totalCheques > 0 && completedCheques == totalCheques;

		outwardChequeDataEntryBtnSubmit.setDisabled(!allCompleted);
	}

	private void updateSaveButton() {

		if (currentChequeIndex < 0 || outwardChequeList == null || currentChequeIndex >= outwardChequeList.size()) {

			outwardChequeDataEntryBtnSaveNext.setDisabled(true);

			return;
		}

		boolean lastCheque = currentChequeIndex == outwardChequeList.size() - 1;

		if (lastCheque) {

			outwardChequeDataEntryBtnSaveNext.setLabel("SAVE");

		} else {

			outwardChequeDataEntryBtnSaveNext.setLabel("SAVE & NEXT");
		}
	}

	private void updateNavigationButtons() {

		if (outwardChequeList == null || outwardChequeList.isEmpty() || currentChequeIndex < 0) {

			outwardChequeDataEntryBtnPrevious.setDisabled(true);

			outwardChequeDataEntryBtnNext.setDisabled(true);

			return;
		}

		outwardChequeDataEntryBtnPrevious.setDisabled(currentChequeIndex <= 0);

		outwardChequeDataEntryBtnNext.setDisabled(currentChequeIndex >= outwardChequeList.size() - 1);
	}

	private void previousCheque() {

		if (currentChequeIndex <= 0 || outwardChequeList == null) {

			return;
		}

		currentChequeIndex--;

		loadCurrentCheque();

		hideStatus();
	}

	private void nextCheque() {

		if (outwardChequeList == null || currentChequeIndex < 0 || currentChequeIndex >= outwardChequeList.size() - 1) {

			return;
		}

		currentChequeIndex++;

		loadCurrentCheque();

		hideStatus();
	}

	private void saveAndNext() {

		if (currentChequeIndex < 0 || outwardChequeList == null || currentChequeIndex >= outwardChequeList.size()) {

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

			cheque.setChequeStatus(STATUS_PENDING_VERIFICATION);

			ScanCheque scanCheque = currentChequeIndex < scanChequeList.size() ? scanChequeList.get(currentChequeIndex)
					: findMatchingScanCheque(cheque);

			if (scanCheque == null) {

				throw new IllegalStateException("Scan cheque not found");
			}

			OutwardCheque savedCheque = outwardChequeService.saveMakerCheque(scannedBatchId, cheque);

			if (savedCheque == null) {

				throw new IllegalStateException("Unable to save cheque data");
			}

			outwardChequeList.set(currentChequeIndex, savedCheque);

			outwardBatchId = savedCheque.getOutwardBatchId();

			if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {

				throw new IllegalStateException("Outward batch ID was not returned after save");
			}

			outwardBatchId = outwardBatchId.trim();

			outwardBatch = outwardBatchService.getBatchById(outwardBatchId);

			updateBatchSummary();

			boolean lastCheque = currentChequeIndex == outwardChequeList.size() - 1;

			if (!lastCheque) {

				int nextIndex = findNextPendingCheque(currentChequeIndex + 1);

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

					updateNavigationButtons();

					showStatus("All cheque data has been completed.");
				}

			} else {

				clearChequeFields();

				outwardChequeDataEntryBtnSaveNext.setDisabled(true);

				outwardChequeDataEntryBtnReject.setDisabled(true);

				outwardChequeDataEntryLblRecord.setValue("ALL CHEQUES COMPLETED");

				outwardChequeDataEntryLblSummaryChequeNumber.setValue("-");

				updateNavigationButtons();

				showStatus("All cheque data has been completed.");
			}

			updateSubmitButton();

		} catch (Exception exception) {

			showStatus("Unable to save cheque data: " + safeExceptionMessage(exception));
		}
	}

	private int findNextPendingCheque(int startIndex) {

		if (outwardChequeList == null) {
			return -1;
		}

		for (int index = startIndex; index < outwardChequeList.size(); index++) {

			OutwardCheque cheque = outwardChequeList.get(index);

			if (cheque == null) {
				continue;
			}

			String status = safeValue(cheque.getChequeStatus()).trim();

			if (STATUS_PENDING_VERIFICATION.equalsIgnoreCase(status)
					|| STATUS_REJECTION_REQUEST.equalsIgnoreCase(status)
					|| STATUS_PENDING_CHECKER_PROCESS.equalsIgnoreCase(status)) {

				continue;
			}

			return index;
		}

		return -1;
	}

	private void populateOutwardChequeFromFields(OutwardCheque cheque) {

		cheque.setChequeNumber(outwardChequeDataEntryTxtChequeNumber.getValue().trim());

		cheque.setMicrCode(outwardChequeDataEntryTxtMicrCode.getValue().trim());

		cheque.setPayeeAccountNumber(outwardChequeDataEntryTxtPayeeAccount.getValue().trim());

		cheque.setPayeeName(outwardChequeDataEntryTxtPayeeName.getValue().trim());

		cheque.setDraweeName(outwardChequeDataEntryTxtDraweeName.getValue().trim());

		String amount = outwardChequeDataEntryTxtAmount.getValue().trim();

		cheque.setChequeAmount(new BigDecimal(amount));

		if (outwardChequeDataEntryDtChequeDate.getValue() != null) {

			cheque.setChequeDate(new Date(outwardChequeDataEntryDtChequeDate.getValue().getTime()));

		} else {

			cheque.setChequeDate(null);
		}
	}

	private boolean validateFields() {

		String chequeNumber = outwardChequeDataEntryTxtChequeNumber.getValue().trim();

		String amount = outwardChequeDataEntryTxtAmount.getValue().trim();

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

		if (outwardChequeDataEntryDtChequeDate.getValue() == null) {

			showStatus("Cheque date is required.");

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

		if (currentChequeIndex < 0 || scanChequeList == null || currentChequeIndex >= scanChequeList.size()) {

			return;
		}

		ScanCheque scanCheque = scanChequeList.get(currentChequeIndex);

		if (scanCheque != null) {

			if (showingFrontImage) {

				loadBackImage(scanCheque);

			} else {

				loadFrontImage(scanCheque);
			}

			return;
		}

		if (outwardChequeList == null || currentChequeIndex >= outwardChequeList.size()) {

			return;
		}

		OutwardCheque cheque = outwardChequeList.get(currentChequeIndex);

		if (showingFrontImage) {

			showingFrontImage = false;

			outwardChequeDataEntryBtnImageToggle.setLabel("FRONT VIEW");

			loadImage(cheque.getChequeImageBack());

		} else {

			showingFrontImage = true;

			outwardChequeDataEntryBtnImageToggle.setLabel("BACK VIEW");

			loadImage(cheque.getChequeImageFront());
		}
	}

	private void loadFrontImage(ScanCheque scanCheque) {

		if (scanCheque == null) {

			showImageUnavailable();

			return;
		}

		showingFrontImage = true;

		outwardChequeDataEntryBtnImageToggle.setLabel("BACK VIEW");

		loadImage(scanCheque.getChequeImageFront());
	}

	private void loadBackImage(ScanCheque scanCheque) {

		if (scanCheque == null) {

			showImageUnavailable();

			return;
		}

		showingFrontImage = false;

		outwardChequeDataEntryBtnImageToggle.setLabel("FRONT VIEW");

		loadImage(scanCheque.getChequeImageBack());
	}

	private void loadImageFromOutwardCheque(OutwardCheque cheque) {

		if (cheque == null) {

			showImageUnavailable();

			return;
		}

		if (showingFrontImage) {

			loadImage(cheque.getChequeImageFront());

		} else {

			loadImage(cheque.getChequeImageBack());
		}
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

	private void resetImage() {

		imageZoom = 1.0;

		imageRotation = 0;

		applyImageTransform();
	}

	private void applyImageTransform() {

		outwardChequeDataEntryChequeImage
				.setStyle("transform: scale(" + imageZoom + ") rotate(" + imageRotation + "deg);");
	}

	private void openRejectWindow() {

		if (currentChequeIndex < 0 || outwardChequeList == null || currentChequeIndex >= outwardChequeList.size()) {

			return;
		}

		if (MODE_SEND_BACK.equalsIgnoreCase(mode)) {
			return;
		}

		OutwardCheque cheque = outwardChequeList.get(currentChequeIndex);

		if (cheque == null || cheque.getOutwardChequeId() == null || cheque.getOutwardChequeId().trim().isEmpty()) {

			showStatus("Save the cheque before creating a rejection request.");

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

		if (currentChequeIndex < 0 || outwardChequeList == null || currentChequeIndex >= outwardChequeList.size()) {

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

		OutwardCheque cheque = outwardChequeList.get(currentChequeIndex);

		if (cheque == null || cheque.getOutwardChequeId() == null || cheque.getOutwardChequeId().trim().isEmpty()) {

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

		showStatus("Rejection request created for cheque " + safeValue(cheque.getChequeNumber()) + ".");

		int nextIndex = findNextPendingCheque(currentChequeIndex + 1);

		if (nextIndex >= 0) {

			currentChequeIndex = nextIndex;

			loadCurrentCheque();

			outwardChequeDataEntryBtnSaveNext.setDisabled(false);

			outwardChequeDataEntryBtnReject.setDisabled(false);

		} else {

			currentChequeIndex = -1;

			clearChequeFields();

			outwardChequeDataEntryBtnSaveNext.setDisabled(true);

			outwardChequeDataEntryBtnReject.setDisabled(true);

			outwardChequeDataEntryLblRecord.setValue("ALL CHEQUES COMPLETED");

			outwardChequeDataEntryLblSummaryChequeNumber.setValue("-");

			updateNavigationButtons();
		}

		updateBatchSummary();
		updateSubmitButton();
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

		if (outwardBatchId == null || outwardBatchId.trim().isEmpty()) {

			showStatus("Save at least one cheque before submission.");

			return;
		}

		int totalCheques = scanChequeList == null ? 0 : scanChequeList.size();

		int completedCheques = outwardChequeService.getDataEnteredCountByBatchId(outwardBatchId);

		if (completedCheques != totalCheques) {

			showStatus("All cheques must be completed before submission.");

			updateSubmitButton();

			return;
		}

		boolean updated = outwardBatchService.updateOutWardBatchStatus(outwardBatchId, STATUS_PENDING_CHECKER_PROCESS);

		if (!updated) {

			showStatus("Unable to submit batch to Checker.");

			return;
		}

		backToDataEntryList();
	}

	private void backToDataEntryList() {

		Component component = outwardChequeDataEntryWin;

		while (component != null) {

			if ("mainContentArea".equals(component.getId())) {

				component.setAttribute("batchId", null);

				component.setAttribute("mode", null);

				if (component instanceof org.zkoss.zul.Include) {

					org.zkoss.zul.Include include = (org.zkoss.zul.Include) component;

					include.setDynamicProperty("batchId", null);

					include.setDynamicProperty("mode", null);

					include.setSrc("/outward/maker/data-entry.zul");

					return;
				}
			}

			component = component.getParent();
		}

		Executions.sendRedirect("/outward/maker/data-entry.zul");
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
}