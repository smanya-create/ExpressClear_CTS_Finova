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
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Window;

import com.iispl.cts.entity.RejectedReason;
import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.OutwardChequeRequest;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.service.NotificationService;
import com.iispl.cts.service.RejectedReasonService;
import com.iispl.cts.service.outward.OutwardBatchService;
import com.iispl.cts.service.outward.OutwardChequeService;
import com.iispl.cts.service.outward.ScanService;
import com.iispl.cts.serviceimpl.NotificationServiceImpl;
import com.iispl.cts.serviceimpl.RejectedReasonServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardBatchServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardChequeServiceImpl;
import com.iispl.cts.serviceimpl.outward.ScanServiceImpl;

public class OutwardChequeDataEntryController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 1L;

	private static final String STATUS_PENDING_DATA_ENTRY = "PENDING_DATA_ENTRY";

	private static final String STATUS_PENDING_MICR_REPAIR = "PENDING_MICR_REPAIR";

	private static final String STATUS_MICR_REJECTED = "MICR_REJECTED";

	private static final String STATUS_PENDING_VERIFICATION = "PENDING_VERIFICATION";

	private static final String STATUS_REJECTION_REQUEST = "REJECTION_REQUEST";

	private static final String STATUS_PENDING_CHECKER_PROCESS = "PENDING_CHECKER_PROCESS";

	private static final String STATUS_ON_HOLD = "ON_HOLD";

	private static final String NOTIFICATION_ROLE_OUTWARD_CHECKER = "OUTWARD_CHECKER";

	private Window outwardChequeDataEntryWin;

	private Button outwardChequeDataEntryBtnBack;

	private Button outwardChequeDataEntryBtnZoomOut;

	private Button outwardChequeDataEntryBtnZoomIn;

	private Button outwardChequeDataEntryBtnRotate;

	private Button outwardChequeDataEntryBtnReset;

	private Button outwardChequeDataEntryBtnPrevious;

	private Button outwardChequeDataEntryBtnNext;

	private Button outwardChequeDataEntryBtnResetItem;

	private Button outwardChequeDataEntryBtnReject;

	private Button outwardChequeDataEntryBtnApprove;

	private Button outwardChequeDataEntryBtnSubmit;

	private Button outwardChequeDataEntryBtnRejectModalConfirm;

	private Button outwardChequeDataEntryBtnRejectModalClose;

	private Label outwardChequeDataEntryLblTitle;

	private Label outwardChequeDataEntryLblSubtitle;

	private Label outwardChequeDataEntryLblChequePosition;

	private Label outwardChequeDataEntryLblBatchId;

	private Label outwardChequeDataEntryLblTotalCheques;

	private Label outwardChequeDataEntryLblStatusBadge;

	private Progressmeter outwardChequeDataEntryProgress;

	private Label outwardChequeDataEntryLblProgress;

	private Label outwardChequeDataEntryLblProgressCount;

	private Label outwardChequeDataEntryLblFooterStatus;

	private Label outwardChequeDataEntryLblImageEmpty;

	private Label outwardChequeDataEntryLblPostDated;

	private Label outwardChequeDataEntryLblMicrReason;

	private Label outwardChequeDataEntryLblRejectionReason;

	private Label outwardChequeDataEntryLblRejectionRemarks;

	private Label outwardChequeDataEntryLblCheckerSendBackMessage;

	private Label outwardChequeDataEntryLblMicrRejectedMessage;

	private Label outwardChequeDataEntryLblMicrPendingMessage;

	private Image outwardChequeDataEntryChequeImage;

	private Textbox outwardChequeDataEntryTxtChequeNumber;

	private Decimalbox outwardChequeDataEntryTxtAmount;

	private Datebox outwardChequeDataEntryDtChequeDate;

	private Textbox outwardChequeDataEntryTxtMicrCode;

	private Textbox outwardChequeDataEntryTxtPayeeAccount;

	private Textbox outwardChequeDataEntryTxtPayeeName;

	private Textbox outwardChequeDataEntryTxtDraweeName;

	private Textbox outwardChequeDataEntryTxtRejectRemarks;

	private Combobox outwardChequeDataEntryCmbRejectReason;

	private Window outwardChequeDataEntryRejectModal;

	private Component outwardChequeDataEntryMakerRejectionPanel;

	private Component outwardChequeDataEntryCheckerSendBackPanel;

	private Component outwardChequeDataEntryMicrRejectedPanel;

	private Component outwardChequeDataEntryMicrPendingPanel;

	private Window outwardChequeDataEntrySubmitConfirmModal;

	private Label outwardChequeDataEntryLblConfirmBatchId;

	private Label outwardChequeDataEntryLblConfirmTotal;

	private Label outwardChequeDataEntryLblConfirmCompleted;

	private Button outwardChequeDataEntryBtnSubmitConfirmCancel;

	private Button outwardChequeDataEntryBtnSubmitConfirm;

	private Window outwardChequeDataEntrySubmitSuccessModal;

	private Label outwardChequeDataEntryLblSubmitSuccess;

	private Label outwardChequeDataEntryLblSubmitSuccessDetails;

	private Button outwardChequeDataEntryBtnSubmitSuccessGo;

	private OutwardBatchService outwardBatchService;

	private OutwardChequeService outwardChequeService;

	private ScanService scanService;

	private NotificationService notificationService;

	private RejectedReasonService rejectedReasonService;

	private String outwardBatchId;

	private String scannedBatchId;

	private OutwardBatch outwardBatch;

	private List<OutwardCheque> outwardChequeList = new ArrayList<>();

	private List<OutwardCheque> activeQueue = new ArrayList<>();

	private List<ScanCheque> scanChequeList = new ArrayList<>();

	private int currentChequeIndex = -1;

	private boolean showingFrontImage = true;

	private double imageZoom = 1.0;

	private int imageRotation = 0;

	private boolean returnedFromChecker;

	private boolean hasUnsavedChanges;

	private boolean batchSubmitted;

	private Timer micrRepairRefreshTimer;

	@Override

	public void doAfterCompose(Component component) throws Exception {

		super.doAfterCompose(component);

		outwardChequeDataEntryWin = (Window) component;

		outwardChequeDataEntryBtnBack = (Button) component.getFellow("outwardChequeDataEntryBtnBack");

		outwardChequeDataEntryBtnZoomOut = (Button) component.getFellow("outwardChequeDataEntryBtnZoomOut");

		outwardChequeDataEntryBtnZoomIn = (Button) component.getFellow("outwardChequeDataEntryBtnZoomIn");

		outwardChequeDataEntryBtnRotate = (Button) component.getFellow("outwardChequeDataEntryBtnRotate");

		outwardChequeDataEntryBtnReset = (Button) component.getFellow("outwardChequeDataEntryBtnReset");

		outwardChequeDataEntryBtnPrevious = (Button) component.getFellow("outwardChequeDataEntryBtnPrevious");

		outwardChequeDataEntryBtnNext = (Button) component.getFellow("outwardChequeDataEntryBtnNext");

		outwardChequeDataEntryBtnResetItem = (Button) component.getFellow("outwardChequeDataEntryBtnResetItem");

		outwardChequeDataEntryBtnReject = (Button) component.getFellow("outwardChequeDataEntryBtnReject");

		outwardChequeDataEntryBtnApprove = (Button) component.getFellow("outwardChequeDataEntryBtnApprove");

		outwardChequeDataEntryBtnSubmit = (Button) component.getFellow("outwardChequeDataEntryBtnSubmit");

		outwardChequeDataEntryLblTitle = (Label) component.getFellow("outwardChequeDataEntryLblTitle");

		outwardChequeDataEntryLblSubtitle = (Label) component.getFellow("outwardChequeDataEntryLblSubtitle");

		outwardChequeDataEntryLblImageEmpty = (Label) component.getFellow("outwardChequeDataEntryLblImageEmpty");

		outwardChequeDataEntryLblBatchId = (Label) component.getFellow("outwardChequeDataEntryLblBatchId");

		outwardChequeDataEntryLblTotalCheques = (Label) component.getFellow("outwardChequeDataEntryLblTotalCheques");

		outwardChequeDataEntryLblStatusBadge = (Label) component.getFellow("outwardChequeDataEntryLblStatusBadge");

		outwardChequeDataEntryProgress = (Progressmeter) component.getFellow("outwardChequeDataEntryProgress");

		outwardChequeDataEntryLblProgress = (Label) component.getFellow("outwardChequeDataEntryLblProgress");

		outwardChequeDataEntryLblProgressCount = (Label) component.getFellow("outwardChequeDataEntryLblProgressCount");

		outwardChequeDataEntryLblChequePosition = (Label) component
				.getFellow("outwardChequeDataEntryLblChequePosition");

		outwardChequeDataEntryLblFooterStatus = (Label) component.getFellow("outwardChequeDataEntryLblFooterStatus");

		outwardChequeDataEntryLblPostDated = (Label) component.getFellow("outwardChequeDataEntryLblPostDated");

		outwardChequeDataEntryLblMicrReason = (Label) component.getFellow("outwardChequeDataEntryLblMicrReason");

		outwardChequeDataEntryLblRejectionReason = (Label) component

				.getFellow("outwardChequeDataEntryLblRejectionReason");

		outwardChequeDataEntryLblRejectionRemarks = (Label) component

				.getFellow("outwardChequeDataEntryLblRejectionRemarks");

		outwardChequeDataEntryLblCheckerSendBackMessage = (Label) component

				.getFellow("outwardChequeDataEntryLblCheckerSendBackMessage");

		outwardChequeDataEntryLblMicrRejectedMessage = (Label) component

				.getFellow("outwardChequeDataEntryLblMicrRejectedMessage");

		outwardChequeDataEntryLblMicrPendingMessage = (Label) component

				.getFellow("outwardChequeDataEntryLblMicrPendingMessage");

		outwardChequeDataEntryChequeImage = (Image) component.getFellow("outwardChequeDataEntryChequeImage");

		outwardChequeDataEntryTxtChequeNumber = (Textbox) component.getFellow("outwardChequeDataEntryTxtChequeNumber");

		outwardChequeDataEntryTxtAmount = (Decimalbox) component.getFellow("outwardChequeDataEntryTxtAmount");

		outwardChequeDataEntryDtChequeDate = (Datebox) component.getFellow("outwardChequeDataEntryDtChequeDate");

		outwardChequeDataEntryTxtMicrCode = (Textbox) component.getFellow("outwardChequeDataEntryTxtMicrCode");

		outwardChequeDataEntryTxtPayeeAccount = (Textbox) component.getFellow("outwardChequeDataEntryTxtPayeeAccount");

		outwardChequeDataEntryTxtPayeeName = (Textbox) component.getFellow("outwardChequeDataEntryTxtPayeeName");

		outwardChequeDataEntryTxtDraweeName = (Textbox) component.getFellow("outwardChequeDataEntryTxtDraweeName");

		outwardChequeDataEntryMakerRejectionPanel = component.getFellow("outwardChequeDataEntryMakerRejectionPanel");

		outwardChequeDataEntryCheckerSendBackPanel = component.getFellow("outwardChequeDataEntryCheckerSendBackPanel");

		outwardChequeDataEntryMicrRejectedPanel = component.getFellow("outwardChequeDataEntryMicrRejectedPanel");

		outwardChequeDataEntryMicrPendingPanel = component.getFellow("outwardChequeDataEntryMicrPendingPanel");

		outwardChequeDataEntryRejectModal = (Window) component.getFellow("outwardChequeDataEntryRejectModal");

		outwardChequeDataEntryCmbRejectReason = (Combobox) outwardChequeDataEntryRejectModal

				.getFellow("outwardChequeDataEntryCmbRejectReason");

		outwardChequeDataEntryTxtRejectRemarks = (Textbox) outwardChequeDataEntryRejectModal

				.getFellow("outwardChequeDataEntryTxtRejectRemarks");

		outwardChequeDataEntryBtnRejectModalConfirm = (Button) outwardChequeDataEntryRejectModal

				.getFellow("outwardChequeDataEntryBtnRejectModalConfirm");

		outwardChequeDataEntryBtnRejectModalClose = (Button) outwardChequeDataEntryRejectModal

				.getFellow("outwardChequeDataEntryBtnRejectModalClose");

		outwardChequeDataEntrySubmitConfirmModal = (Window) component

				.getFellow("outwardChequeDataEntrySubmitConfirmModal");

		outwardChequeDataEntryLblConfirmBatchId = (Label) outwardChequeDataEntrySubmitConfirmModal

				.getFellow("outwardChequeDataEntryLblConfirmBatchId");

		outwardChequeDataEntryLblConfirmTotal = (Label) outwardChequeDataEntrySubmitConfirmModal

				.getFellow("outwardChequeDataEntryLblConfirmTotal");

		outwardChequeDataEntryBtnSubmitConfirmCancel = (Button) outwardChequeDataEntrySubmitConfirmModal

				.getFellow("outwardChequeDataEntryBtnSubmitConfirmCancel");

		outwardChequeDataEntryBtnSubmitConfirm = (Button) outwardChequeDataEntrySubmitConfirmModal

				.getFellow("outwardChequeDataEntryBtnSubmitConfirm");

		outwardChequeDataEntrySubmitSuccessModal = (Window) component

				.getFellow("outwardChequeDataEntrySubmitSuccessModal");

		outwardChequeDataEntryLblSubmitSuccess = (Label) outwardChequeDataEntrySubmitSuccessModal

				.getFellow("outwardChequeDataEntryLblSubmitSuccess");

		outwardChequeDataEntryLblSubmitSuccessDetails = (Label) outwardChequeDataEntrySubmitSuccessModal

				.getFellow("outwardChequeDataEntryLblSubmitSuccessDetails");

		outwardChequeDataEntryBtnSubmitSuccessGo = (Button) outwardChequeDataEntrySubmitSuccessModal

				.getFellow("outwardChequeDataEntryBtnSubmitSuccessGo");

		outwardChequeDataEntrySubmitConfirmModal.setVisible(false);

		outwardChequeDataEntrySubmitSuccessModal.setVisible(false);

		outwardBatchService = new OutwardBatchServiceImpl();

		outwardChequeService = new OutwardChequeServiceImpl();

		scanService = new ScanServiceImpl();

		notificationService = new NotificationServiceImpl();

		rejectedReasonService = RejectedReasonServiceImpl.getInstance();

		outwardChequeDataEntryRejectModal.setVisible(false);

		outwardChequeDataEntrySubmitConfirmModal.setVisible(false);

		outwardChequeDataEntrySubmitSuccessModal.setVisible(false);

		hideAllPanels();

		hideStatus();

		resolveBatchContext();

		registerEvents();

		loadRejectedReasons();

		startMicrRepairRefreshTimer();

		loadDataEntryData();

	}

	private void resolveBatchContext() {

		outwardBatchId = null;

		scannedBatchId = null;

		outwardBatch = null;

		Object batchId = Executions.getCurrent().getAttribute("batchId");

		if (isBlank(batchId)) {

			Object argumentBatchId = Executions.getCurrent().getArg().get("batchId");

			if (!isBlank(argumentBatchId)) {

				batchId = argumentBatchId;

			}

		}

		if (isBlank(batchId)) {

			Object sessionBatchId = Executions.getCurrent().getSession().getAttribute("OUTWARD_DATA_ENTRY_BATCH_ID");

			if (!isBlank(sessionBatchId)) {

				batchId = sessionBatchId;

			}

		}

		if (isBlank(batchId)) {

			Object sessionBatchId = Executions.getCurrent().getSession().getAttribute("batchId");

			if (!isBlank(sessionBatchId)) {

				batchId = sessionBatchId;

			}

		}

		if (isBlank(batchId)) {

			String requestBatchId = Executions.getCurrent().getParameter("batchId");

			if (!isBlank(requestBatchId)) {

				batchId = requestBatchId;

			}

		}

		if (isBlank(batchId)) {

			throw new IllegalArgumentException("Batch ID is required");

		}

		String suppliedBatchId = batchId.toString().trim();

		try {

			OutwardBatch existingBatch = outwardBatchService.getBatchById(suppliedBatchId);

			if (existingBatch != null) {

				outwardBatchId = suppliedBatchId;

				outwardBatch = existingBatch;

				try {

					scannedBatchId = outwardBatchService.getScannedBatchIdByOutwardBatchId(outwardBatchId);

				} catch (Exception exception) {

					scannedBatchId = suppliedBatchId;

				}

				if (isBlank(scannedBatchId)) {

					scannedBatchId = suppliedBatchId;

				}

			} else {

				scannedBatchId = suppliedBatchId;

				try {

					String resolvedOutwardBatch = outwardBatchService.getOutwardBatchIdByScannedBatchId(scannedBatchId);

					if (!isBlank(resolvedOutwardBatch)) {

						outwardBatchId = resolvedOutwardBatch.trim();

						outwardBatch = outwardBatchService.getBatchById(outwardBatchId);

					}

				} catch (Exception exception) {

					outwardBatchId = suppliedBatchId;

				}

			}

		} catch (Exception exception) {

			outwardBatchId = suppliedBatchId;

			scannedBatchId = suppliedBatchId;

		}

		if (isBlank(outwardBatchId)) {

			outwardBatchId = suppliedBatchId;

		}

		if (isBlank(scannedBatchId)) {

			scannedBatchId = outwardBatchId;

		}

		Object returnFlag = Executions.getCurrent().getAttribute("RETURN_FROM_CHECKER");

		if (returnFlag == null) {

			returnFlag = Executions.getCurrent().getSession().getAttribute("RETURN_FROM_CHECKER");

		}

		returnedFromChecker = returnFlag != null && "true".equalsIgnoreCase(returnFlag.toString());

		Executions.getCurrent().getSession().setAttribute("OUTWARD_DATA_ENTRY_BATCH_ID", outwardBatchId);

		Executions.getCurrent().getSession().setAttribute("OUTWARD_DATA_ENTRY_SCANNED_BATCH_ID", scannedBatchId);

	}

	private void registerEvents() {

		outwardChequeDataEntryBtnBack.addEventListener("onClick", event -> backToDataEntryList());

		outwardChequeDataEntryBtnZoomOut.addEventListener("onClick", event -> zoomOut());

		outwardChequeDataEntryBtnZoomIn.addEventListener("onClick", event -> zoomIn());

		outwardChequeDataEntryBtnRotate.addEventListener("onClick", event -> rotateImage());

		outwardChequeDataEntryBtnReset.addEventListener("onClick", event -> resetImage());

		outwardChequeDataEntryBtnPrevious.addEventListener("onClick", event -> previousCheque());

		outwardChequeDataEntryBtnNext.addEventListener("onClick", event -> nextCheque());

		outwardChequeDataEntryBtnResetItem.addEventListener("onClick", event -> resetCurrentItem());

		outwardChequeDataEntryBtnReject.addEventListener("onClick", event -> openRejectWindow());

		outwardChequeDataEntryBtnApprove.addEventListener("onClick", event -> approveCheque());

		outwardChequeDataEntryBtnSubmit.addEventListener("onClick", event -> submitToChecker());

		outwardChequeDataEntryBtnSubmitConfirmCancel.addEventListener("onClick",
				event -> outwardChequeDataEntrySubmitConfirmModal.setVisible(false));

		outwardChequeDataEntryBtnSubmitConfirm.addEventListener("onClick", event -> {
			outwardChequeDataEntrySubmitConfirmModal.setVisible(false);
			completeBatchSubmission();
		});

		outwardChequeDataEntryBtnSubmitSuccessGo.addEventListener("onClick", event -> {
			outwardChequeDataEntrySubmitSuccessModal.setVisible(false);
			backToDataEntryList();
		});

		outwardChequeDataEntryBtnRejectModalConfirm.addEventListener("onClick", event -> requestReject());

		outwardChequeDataEntryBtnRejectModalClose.addEventListener("onClick", event -> closeRejectWindow());

		registerDirtyTracking(outwardChequeDataEntryTxtChequeNumber);

		registerDirtyTracking(outwardChequeDataEntryTxtAmount);

		registerDirtyTracking(outwardChequeDataEntryDtChequeDate);

		registerDirtyTracking(outwardChequeDataEntryTxtPayeeAccount);

		registerDirtyTracking(outwardChequeDataEntryTxtPayeeName);

		registerDirtyTracking(outwardChequeDataEntryTxtDraweeName);

	}

	private void registerDirtyTracking(Component component) {

		component.addEventListener("onChange", event -> {

			OutwardCheque cheque = getCurrentCheque();

			if (cheque != null && isEditableCheque(cheque)) {

				hasUnsavedChanges = true;

				updateActionButtons();

				updateNavigationButtons();

				updateSubmitButton();

			}

		});

	}

	private void loadDataEntryData() {

		try {

			batchSubmitted = false;

			loadScanCheques();

			if (!isBlank(outwardBatchId)) {

				loadExistingCheques();

			}

			mergeScanAndOutwardCheques();

			updateBatchSummary();

			rebuildActiveQueue();

			int firstPending = findFirstPendingDataEntry();

			if (firstPending >= 0) {

				currentChequeIndex = firstPending;

				loadCurrentCheque();

			} else {

				int first = findFirstNavigableCheque();

				if (first >= 0) {

					currentChequeIndex = first;

					loadCurrentCheque();

				} else if (hasPendingMicrRepair()) {

					showWaitingForMicrRepair();

				} else {

					showAllCompleted();

				}

			}

			updateSubmitButton();

		} catch (Exception exception) {

			clearPage();

			showStatus("Unable to load cheque data: " + safeExceptionMessage(exception));

		}

	}

	private void loadScanCheques() {

		scanChequeList = new ArrayList<>();

		if (isBlank(scannedBatchId)) {

			return;

		}

		try {

			List<ScanCheque> scans = scanService.getChequesByBatchId(scannedBatchId.trim());

			if (scans != null) {

				scanChequeList.addAll(scans);

			}

		} catch (Exception exception) {

			scanChequeList = new ArrayList<>();

		}

	}

	private void loadExistingCheques() {

		List<OutwardCheque> existing = outwardChequeService.getChequesByBatchId(outwardBatchId.trim());

		outwardChequeList = existing == null ? new ArrayList<>() : new ArrayList<>(existing);

	}

	private void mergeScanAndOutwardCheques() {

		List<OutwardCheque> ordered = new ArrayList<>();

		for (ScanCheque scanCheque : scanChequeList) {

			if (scanCheque == null) {

				continue;

			}

			OutwardCheque existing = findExistingOutwardCheque(scanCheque);

			if (existing != null) {

				if (!containsChequeById(ordered, existing)) {

					ordered.add(existing);

				}

			} else {

				ordered.add(buildOutwardChequeFromScan(scanCheque));

			}

		}

		for (OutwardCheque cheque : outwardChequeList) {

			if (cheque == null) {

				continue;

			}

			if (!containsChequeById(ordered, cheque)) {

				ordered.add(cheque);

			}

		}

		outwardChequeList = ordered;

	}

	private OutwardCheque findExistingOutwardCheque(ScanCheque scanCheque) {

		if (scanCheque == null) {

			return null;

		}

		String number = safeValue(scanCheque.getChequeNumber()).trim();

		String front = safeValue(scanCheque.getChequeImageFront()).trim();

		String back = safeValue(scanCheque.getChequeImageBack()).trim();

		for (OutwardCheque cheque : outwardChequeList) {

			if (cheque == null) {

				continue;

			}

			String chequeFront = safeValue(cheque.getChequeImageFront()).trim();

			String chequeBack = safeValue(cheque.getChequeImageBack()).trim();

			String chequeNumber = safeValue(cheque.getChequeNumber()).trim();

			if (!front.isEmpty() && front.equalsIgnoreCase(chequeFront)) {

				return cheque;

			}

			if (!back.isEmpty() && back.equalsIgnoreCase(chequeBack)) {

				return cheque;

			}

			if (!number.isEmpty() && number.equalsIgnoreCase(chequeNumber)) {

				return cheque;

			}

		}

		return null;

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

	private void rebuildActiveQueue() {

		activeQueue = new ArrayList<>();

		if (outwardChequeList == null) {

			return;

		}

		for (OutwardCheque cheque : outwardChequeList) {

			if (cheque != null) {

				activeQueue.add(cheque);

			}

		}

	}

	private int findFirstPendingDataEntry() {

		if (activeQueue == null) {

			return -1;

		}

		for (int i = 0; i < activeQueue.size(); i++) {

			OutwardCheque cheque = activeQueue.get(i);

			if (cheque == null) {

				continue;

			}

			if (STATUS_PENDING_DATA_ENTRY.equals(normalizeChequeStatus(cheque.getChequeStatus()))) {

				return i;

			}

		}

		return -1;

	}

	private int findFirstNavigableCheque() {

		if (activeQueue == null || activeQueue.isEmpty()) {

			return -1;

		}

		return 0;

	}

	private int findNextPendingDataEntry(int start) {

		if (activeQueue == null) {

			return -1;

		}

		for (int i = Math.max(0, start); i < activeQueue.size(); i++) {

			OutwardCheque cheque = activeQueue.get(i);

			if (cheque != null && STATUS_PENDING_DATA_ENTRY.equals(normalizeChequeStatus(cheque.getChequeStatus()))) {

				return i;

			}

		}

		return -1;

	}

	private OutwardCheque getCurrentCheque() {

		if (activeQueue == null || currentChequeIndex < 0 || currentChequeIndex >= activeQueue.size()) {

			return null;

		}

		return activeQueue.get(currentChequeIndex);

	}

	private void loadCurrentCheque() {

		OutwardCheque cheque = getCurrentCheque();

		if (cheque == null) {

			return;

		}

		ScanCheque scanCheque = findMatchingScanCheque(cheque);

		if (cheque.getOutwardChequeId() != null && !cheque.getOutwardChequeId().trim().isEmpty()) {

			populateChequeFields(cheque);

		} else if (scanCheque != null) {

			populateChequeFieldsFromScan(scanCheque);

		} else {

			populateChequeFields(cheque);

		}

		resetImageState();

		String imagePath;

		if (scanCheque != null) {

			imagePath = scanCheque.getChequeImageFront();

		} else {

			imagePath = cheque.getChequeImageFront();

		}

		loadImage(imagePath);

		hasUnsavedChanges = false;

		setChequeFieldsEditable(isEditableCheque(cheque));

		updatePostDatedIndicator(cheque);

		updateCurrentChequeStatus(cheque);

		updateSummary();

		updateActionButtons();

		updateNavigationButtons();

		updateSubmitButton();

	}

	private void populateChequeFields(OutwardCheque cheque) {

		outwardChequeDataEntryTxtChequeNumber.setValue(safeValue(cheque.getChequeNumber()));

		outwardChequeDataEntryTxtAmount

				.setValue(cheque.getChequeAmount());

		outwardChequeDataEntryDtChequeDate.setValue(cheque.getChequeDate());

		outwardChequeDataEntryTxtMicrCode.setValue(safeValue(cheque.getMicrCode()));

		outwardChequeDataEntryTxtPayeeAccount.setValue(safeValue(cheque.getPayeeAccountNumber()));

		outwardChequeDataEntryTxtPayeeName.setValue(safeValue(cheque.getPayeeName()));

		outwardChequeDataEntryTxtDraweeName.setValue(safeValue(cheque.getDraweeName()));

	}

	private void populateChequeFieldsFromScan(ScanCheque scanCheque) {

		outwardChequeDataEntryTxtChequeNumber.setValue(safeValue(scanCheque.getChequeNumber()));

		outwardChequeDataEntryTxtAmount

				.setValue(scanCheque.getChequeAmount());

		outwardChequeDataEntryDtChequeDate.setValue(scanCheque.getChequeDate());

		outwardChequeDataEntryTxtMicrCode.setValue(safeValue(scanCheque.getMicrCode()));

		outwardChequeDataEntryTxtPayeeAccount.setValue(safeValue(scanCheque.getPayeeAccountNumber()));

		outwardChequeDataEntryTxtPayeeName.setValue(safeValue(scanCheque.getPayeeName()));

		outwardChequeDataEntryTxtDraweeName.setValue(safeValue(scanCheque.getDraweeName()));

	}

	private void populateOutwardChequeFromFields(OutwardCheque cheque) {

		cheque.setChequeNumber(outwardChequeDataEntryTxtChequeNumber.getValue().trim());

		cheque.setPayeeAccountNumber(outwardChequeDataEntryTxtPayeeAccount.getValue().trim());

		cheque.setPayeeName(outwardChequeDataEntryTxtPayeeName.getValue().trim());

		cheque.setDraweeName(outwardChequeDataEntryTxtDraweeName.getValue().trim());

		cheque.setMicrCode(outwardChequeDataEntryTxtMicrCode.getValue().trim());

		cheque.setChequeAmount(outwardChequeDataEntryTxtAmount.getValue());

		if (outwardChequeDataEntryDtChequeDate.getValue() != null) {

			cheque.setChequeDate(new Date(outwardChequeDataEntryDtChequeDate.getValue().getTime()));

		}

	}

	private boolean validateFields() {

		String chequeNumber = outwardChequeDataEntryTxtChequeNumber.getValue().trim();

		String amount = safeDecimalValue(outwardChequeDataEntryTxtAmount.getValue());

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

			BigDecimal value = new BigDecimal(amount);

			if (value.compareTo(BigDecimal.ZERO) < 0) {

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

	private boolean isEditableCheque(OutwardCheque cheque) {

		if (cheque == null) {
			return false;
		}

		String status = normalizeChequeStatus(cheque.getChequeStatus());

		return STATUS_PENDING_DATA_ENTRY.equals(status) || STATUS_ON_HOLD.equals(status);
	}

	private void setChequeFieldsEditable(boolean editable) {

		outwardChequeDataEntryTxtChequeNumber.setReadonly(!editable);

		outwardChequeDataEntryTxtAmount.setReadonly(!editable);

		outwardChequeDataEntryDtChequeDate.setReadonly(!editable);

		outwardChequeDataEntryTxtPayeeAccount.setReadonly(!editable);

		outwardChequeDataEntryTxtPayeeName.setReadonly(!editable);

		outwardChequeDataEntryTxtDraweeName.setReadonly(!editable);

		outwardChequeDataEntryTxtMicrCode.setReadonly(true);

		outwardChequeDataEntryTxtChequeNumber.setDisabled(!editable);

		outwardChequeDataEntryTxtAmount.setDisabled(!editable);

		outwardChequeDataEntryDtChequeDate.setDisabled(!editable);

		outwardChequeDataEntryTxtPayeeAccount.setDisabled(!editable);

		outwardChequeDataEntryTxtPayeeName.setDisabled(!editable);

		outwardChequeDataEntryTxtDraweeName.setDisabled(!editable);

		outwardChequeDataEntryTxtMicrCode.setDisabled(true);

	}

	private void updateActionButtons() {

		OutwardCheque cheque = getCurrentCheque();

		if (cheque == null) {
			outwardChequeDataEntryBtnResetItem.setDisabled(true);
			outwardChequeDataEntryBtnReject.setDisabled(true);
			outwardChequeDataEntryBtnApprove.setDisabled(true);
			return;
		}

		String status = normalizeChequeStatus(cheque.getChequeStatus());
		boolean editable = isEditableCheque(cheque);

		boolean approveEnabled = editable
				&& (STATUS_PENDING_DATA_ENTRY.equals(status) || STATUS_ON_HOLD.equals(status));

		boolean rejectEnabled = editable && (STATUS_PENDING_DATA_ENTRY.equals(status) || STATUS_ON_HOLD.equals(status));

		outwardChequeDataEntryBtnResetItem.setDisabled(!editable || !hasUnsavedChanges);
		outwardChequeDataEntryBtnReject.setDisabled(!rejectEnabled);
		outwardChequeDataEntryBtnApprove.setDisabled(!approveEnabled);
	}

	private void approveCheque() {

		OutwardCheque cheque = getCurrentCheque();

		if (cheque == null) {

			return;

		}

		if (!isEditableCheque(cheque)) {

			showStatus("This cheque cannot be edited in Data Entry.");

			return;

		}

		if (!validateFields()) {

			return;

		}

		try {

			populateOutwardChequeFromFields(cheque);

			cheque.setOutwardBatchId(outwardBatchId);

			cheque.setChequeStatus(STATUS_PENDING_VERIFICATION);

			String batchForSave = !isBlank(scannedBatchId) ? scannedBatchId : outwardBatchId;

			OutwardCheque saved = outwardChequeService.saveMakerCheque(batchForSave, cheque);

			if (saved == null) {

				throw new IllegalStateException("Unable to save cheque data.");

			}

			syncChequeToFullList(saved);

			if (!isBlank(saved.getOutwardBatchId())) {

				outwardBatchId = saved.getOutwardBatchId().trim();

			}

			outwardBatch = outwardBatchService.getBatchById(outwardBatchId);

			hasUnsavedChanges = false;

			updateBatchSummary();

			showStatus("Cheque approved successfully.");

			moveToNextPendingCheque();

			updateSubmitButton();

		} catch (Exception exception) {

			showStatus("Unable to approve cheque: " + safeExceptionMessage(exception));

		}

	}

	private void moveToNextPendingCheque() {

		rebuildActiveQueue();

		int next = findNextPendingDataEntry(currentChequeIndex + 1);

		if (next < 0) {

			next = findFirstPendingDataEntry();

		}

		if (next >= 0) {

			currentChequeIndex = next;

			loadCurrentCheque();

			return;

		}

		if (hasPendingMicrRepair()) {

			showWaitingForMicrRepair();

			return;

		}

		int completed = countCompletedMakerCheques();

		if (completed == getBatchTotal() && getBatchTotal() > 0) {

			showAllCompleted();

		}

	}

	private void resetCurrentItem() {

		OutwardCheque cheque = getCurrentCheque();

		if (cheque == null) {

			return;

		}

		if (!isEditableCheque(cheque)) {

			return;

		}

		populateChequeFields(cheque);

		hasUnsavedChanges = false;

		setChequeFieldsEditable(true);

		updatePostDatedIndicator(cheque);

		updateCurrentChequeStatus(cheque);

		updateActionButtons();

		updateNavigationButtons();

		updateSubmitButton();

		showStatus("Current item has been reset.");

	}

	private void previousCheque() {

		if (hasUnsavedChanges) {

			showStatus("Save or reset the current cheque before moving.");

			return;

		}

		if (currentChequeIndex <= 0) {

			return;

		}

		currentChequeIndex--;

		loadCurrentCheque();

	}

	private void nextCheque() {

		if (hasUnsavedChanges) {

			showStatus("Save or reset the current cheque before moving.");

			return;

		}

		if (currentChequeIndex >= activeQueue.size() - 1) {

			return;

		}

		currentChequeIndex++;

		loadCurrentCheque();

	}

	private void updateNavigationButtons() {

		if (activeQueue == null || activeQueue.isEmpty() || currentChequeIndex < 0) {

			outwardChequeDataEntryBtnPrevious.setDisabled(true);

			outwardChequeDataEntryBtnNext.setDisabled(true);

			return;

		}

		outwardChequeDataEntryBtnPrevious.setDisabled(currentChequeIndex <= 0 || hasUnsavedChanges);

		outwardChequeDataEntryBtnNext.setDisabled(currentChequeIndex >= activeQueue.size() - 1 || hasUnsavedChanges);

	}

	private void updateSummary() {

		OutwardCheque cheque = getCurrentCheque();

		int total = getBatchTotal();

		int position = currentChequeIndex + 1;

		outwardChequeDataEntryLblChequePosition.setValue(position > 0 ? position + " of " + total : "Data Entry");

		if (cheque == null) {
			outwardChequeDataEntryTxtChequeNumber.setValue("");
			outwardChequeDataEntryLblStatusBadge.setValue("-");
			outwardChequeDataEntryLblFooterStatus.setValue("-");
			return;
		}

		outwardChequeDataEntryTxtChequeNumber.setValue(safeValue(cheque.getChequeNumber()));
		String currentStatus = normalizeChequeStatus(cheque.getChequeStatus());
		updateStatusBadge(currentStatus);
		outwardChequeDataEntryLblFooterStatus.setValue(displayStatus(currentStatus));
	}

	private void updateBatchSummary() {

		String batch = !isBlank(outwardBatchId) ? outwardBatchId : scannedBatchId;
		outwardChequeDataEntryLblBatchId.setValue(isBlank(batch) ? "-" : batch);

		int total = getBatchTotal();
		outwardChequeDataEntryLblTotalCheques.setValue(String.valueOf(total));

		updateProgress(total);
	}

	private void updateProgress(int total) {

		int completed = countCompletedMakerCheques();

		int percentage = total <= 0 ? 0 : (completed * 100) / total;

		if (percentage > 100) {
			percentage = 100;
		}

		outwardChequeDataEntryProgress.setValue(percentage);
		outwardChequeDataEntryLblProgress.setValue(percentage + "%");
		outwardChequeDataEntryLblProgressCount.setValue(completed + " of " + total + " completed");
	}

	private int getBatchTotal() {

		if (outwardBatch != null && outwardBatch.getActualChequeCount() > 0) {

			return outwardBatch.getActualChequeCount();

		}

		if (outwardChequeList != null && !outwardChequeList.isEmpty()) {

			return outwardChequeList.size();

		}

		return scanChequeList == null ? 0 : scanChequeList.size();

	}

	private int countCompletedMakerCheques() {

		int count = 0;

		if (outwardChequeList == null) {

			return 0;

		}

		for (OutwardCheque cheque : outwardChequeList) {

			if (cheque == null) {

				continue;

			}

			String status = normalizeChequeStatus(cheque.getChequeStatus());

			if (STATUS_PENDING_VERIFICATION.equals(status) || STATUS_REJECTION_REQUEST.equals(status)) {

				count++;

			}

		}

		return count;

	}

	private boolean hasDataEntryPendingCheque() {

		if (outwardChequeList == null) {

			return false;

		}

		for (OutwardCheque cheque : outwardChequeList) {

			if (cheque != null && STATUS_PENDING_DATA_ENTRY.equals(normalizeChequeStatus(cheque.getChequeStatus()))) {

				return true;

			}

		}

		return false;

	}

	private boolean hasPendingMicrRepair() {

		if (outwardChequeList == null) {

			return false;

		}

		for (OutwardCheque cheque : outwardChequeList) {

			if (cheque != null && STATUS_PENDING_MICR_REPAIR.equals(normalizeChequeStatus(cheque.getChequeStatus()))) {

				return true;

			}

		}

		return false;

	}

	private int countMicrPending() {

		int count = 0;

		if (outwardChequeList == null) {

			return 0;

		}

		for (OutwardCheque cheque : outwardChequeList) {

			if (cheque != null && STATUS_PENDING_MICR_REPAIR.equals(normalizeChequeStatus(cheque.getChequeStatus()))) {

				count++;

			}

		}

		return count;

	}

	private void updateSubmitButton() {

		if (outwardChequeDataEntryBtnSubmit == null) {

			return;

		}

		if (outwardChequeList == null || outwardChequeList.isEmpty()) {

			outwardChequeDataEntryBtnSubmit.setDisabled(true);

			return;

		}

		if (isBlank(outwardBatchId)) {

			outwardChequeDataEntryBtnSubmit.setDisabled(true);

			return;

		}

		if (hasUnsavedChanges) {

			outwardChequeDataEntryBtnSubmit.setDisabled(true);

			outwardChequeDataEntryLblFooterStatus.setValue("Save or reset the current cheque before submission.");

			return;

		}

		if (hasDataEntryPendingCheque()) {

			outwardChequeDataEntryBtnSubmit.setDisabled(true);

			outwardChequeDataEntryLblFooterStatus.setValue("Complete all Data Entry cheques before submitting.");

			return;

		}

		if (hasPendingMicrRepair()) {

			outwardChequeDataEntryBtnSubmit.setDisabled(true);

			int count = countMicrPending();

			outwardChequeDataEntryLblFooterStatus

					.setValue(count + " cheque" + (count == 1 ? "" : "s") + " required MICR processing.");

			return;

		}

		int total = getBatchTotal();

		int completed = countCompletedMakerCheques();

		boolean ready = total > 0 && completed == total;

		outwardChequeDataEntryBtnSubmit.setDisabled(!ready);

		if (ready) {

			outwardChequeDataEntryLblFooterStatus

					.setValue("All maker activities are completed. Batch is ready for Checker.");

		} else {

			outwardChequeDataEntryLblFooterStatus

					.setValue("Complete all maker activities before submitting the batch.");

		}

	}

	private void updateStatusBadge(String status) {

		if (outwardChequeDataEntryLblStatusBadge == null) {

			return;

		}

		String normalized = normalizeChequeStatus(status);

		String text = displayStatus(normalized);

		outwardChequeDataEntryLblStatusBadge.setValue(text);

		String sclass = "outward-cheque-data-entry-status-badge";

		if (STATUS_PENDING_DATA_ENTRY.equals(normalized)) {

			sclass += " outward-cheque-data-entry-status-pending";

		} else if (STATUS_PENDING_MICR_REPAIR.equals(normalized)) {

			sclass += " outward-cheque-data-entry-status-micr";

		} else if (STATUS_MICR_REJECTED.equals(normalized)) {

			sclass += " outward-cheque-data-entry-status-rejected";

		} else if (STATUS_ON_HOLD.equals(normalized)) {

			sclass += " outward-cheque-data-entry-status-onhold";

		} else if (STATUS_REJECTION_REQUEST.equals(normalized)) {

			sclass += " outward-cheque-data-entry-status-rejection";

		} else if (STATUS_PENDING_VERIFICATION.equals(normalized)) {

			sclass += " outward-cheque-data-entry-status-verification";

		} else if (STATUS_PENDING_CHECKER_PROCESS.equals(normalized)) {

			sclass += " outward-cheque-data-entry-status-checker";

		} else {

			sclass += " outward-cheque-data-entry-status-default";

		}

		outwardChequeDataEntryLblStatusBadge.setSclass(sclass);

	}

	private void updateCurrentChequeStatus(OutwardCheque cheque) {

		hideAllPanels();

		hideStatus();

		if (cheque == null) {

			return;

		}

		String status = normalizeChequeStatus(cheque.getChequeStatus());

		updateStatusBadge(status);

		outwardChequeDataEntryLblFooterStatus.setValue(displayStatus(status));

		if (STATUS_PENDING_MICR_REPAIR.equals(status)) {

			outwardChequeDataEntryMicrPendingPanel.setVisible(true);

			outwardChequeDataEntryLblMicrPendingMessage.setValue(

					"This cheque is currently awaiting MICR processing and cannot be edited from Data Entry.");

			showStatus("This cheque is awaiting MICR processing and cannot be edited.");

			return;

		}

		if (STATUS_MICR_REJECTED.equals(status)) {

			outwardChequeDataEntryMicrRejectedPanel.setVisible(true);

			outwardChequeDataEntryLblMicrRejectedMessage

					.setValue("This cheque was rejected during MICR processing and cannot be edited from Data Entry.");

			outwardChequeDataEntryLblMicrReason.setVisible(true);

			outwardChequeDataEntryLblMicrReason.setValue(
					"MICR Rejected: This cheque was rejected during MICR processing and cannot be edited from Data Entry.");

			showStatus("This cheque was rejected during MICR processing and cannot be edited.");

			return;

		}

		if (STATUS_REJECTION_REQUEST.equals(status)) {

			outwardChequeDataEntryMakerRejectionPanel.setVisible(true);

			loadRejectionStatus(cheque);

			return;

		}

		if (STATUS_ON_HOLD.equals(status)) {

			outwardChequeDataEntryCheckerSendBackPanel.setVisible(true);

			outwardChequeDataEntryLblCheckerSendBackMessage.setValue(

					"This cheque was returned by Checker for maker review. Correct the required details and approve the cheque again.");

			showStatus("This cheque was returned by Checker and is available for maker correction.");

			return;

		}

		if (STATUS_PENDING_VERIFICATION.equals(status)) {

			showStatus("Cheque data completed and ready for Checker verification.");

			return;

		}

		if (STATUS_PENDING_DATA_ENTRY.equals(status)) {

			hideStatus();

		}

	}

	private void loadRejectionStatus(OutwardCheque cheque) {

		if (cheque == null || isBlank(cheque.getOutwardChequeId())) {

			outwardChequeDataEntryLblRejectionReason.setValue("Rejection request created.");

			outwardChequeDataEntryLblRejectionRemarks.setValue("");

			return;

		}

		try {

			OutwardChequeRequest request = outwardChequeService

					.getRejectionRequestByChequeId(cheque.getOutwardChequeId());

			if (request == null) {

				outwardChequeDataEntryLblRejectionReason.setValue("Rejection request created.");

				outwardChequeDataEntryLblRejectionRemarks.setValue("");

				return;

			}

			String reason = safeValue(request.getReason()).trim();

			String remarks = safeValue(request.getRemarks()).trim();

			outwardChequeDataEntryLblRejectionReason.setValue(reason.isEmpty() ? "Reason: -" : "Reason: " + reason);

			outwardChequeDataEntryLblRejectionRemarks

					.setValue(remarks.isEmpty() ? "Remarks: -" : "Remarks: " + remarks);

		} catch (Exception exception) {

			outwardChequeDataEntryLblRejectionReason.setValue("Rejection request");

			outwardChequeDataEntryLblRejectionRemarks.setValue("");

		}

	}

	private void updatePostDatedIndicator(OutwardCheque cheque) {

		outwardChequeDataEntryLblPostDated.setVisible(false);

		if (cheque == null || cheque.getChequeDate() == null) {

			return;

		}

		long today = System.currentTimeMillis();

		if (cheque.getChequeDate().getTime() > today) {

			outwardChequeDataEntryLblPostDated.setVisible(true);

		}

	}

	private void hideAllPanels() {

		outwardChequeDataEntryMakerRejectionPanel.setVisible(false);

		outwardChequeDataEntryCheckerSendBackPanel.setVisible(false);

		outwardChequeDataEntryMicrRejectedPanel.setVisible(false);

		outwardChequeDataEntryMicrPendingPanel.setVisible(false);

		outwardChequeDataEntryLblPostDated.setVisible(false);

		outwardChequeDataEntryLblMicrReason.setVisible(false);

	}

	private void openRejectWindow() {

		OutwardCheque cheque = getCurrentCheque();

		if (cheque == null) {

			return;

		}

		String status = normalizeChequeStatus(cheque.getChequeStatus());

		if (!STATUS_PENDING_DATA_ENTRY.equals(status) && !STATUS_ON_HOLD.equals(status)

				&& !STATUS_PENDING_VERIFICATION.equals(status)) {

			showStatus("This cheque cannot be submitted for rejection.");

			return;

		}

		outwardChequeDataEntryCmbRejectReason.setSelectedItem(null);

		outwardChequeDataEntryTxtRejectRemarks.setValue("");

		outwardChequeDataEntryRejectModal.setVisible(true);

		outwardChequeDataEntryRejectModal.doModal();

	}

	private void closeRejectWindow() {

		outwardChequeDataEntryRejectModal.setVisible(false);

	}

	private void requestReject() {

		OutwardCheque cheque = getCurrentCheque();

		if (cheque == null) {

			return;

		}

		String status = normalizeChequeStatus(cheque.getChequeStatus());

		if (!STATUS_PENDING_DATA_ENTRY.equals(status) && !STATUS_PENDING_VERIFICATION.equals(status)

				&& !STATUS_ON_HOLD.equals(status)) {

			closeRejectWindow();

			showStatus("This cheque cannot be submitted for rejection.");

			return;

		}

		Comboitem selectedReason = outwardChequeDataEntryCmbRejectReason.getSelectedItem();

		if (selectedReason == null) {

			showStatus("Please select a rejection reason.");

			return;

		}

		Object selectedValue = selectedReason.getValue();

		if (!(selectedValue instanceof RejectedReason)) {

			showStatus("Please select a valid rejection reason.");

			return;

		}

		RejectedReason rejectedReason = (RejectedReason) selectedValue;

		String reasonId = safeValue(rejectedReason.getRejectedReasonId()).trim();

		String reason = safeValue(rejectedReason.getRejectedReasonName()).trim();

		if (reasonId.isEmpty()) {

			reasonId = safeValue(rejectedReason.getRejectedReasonCode()).trim();

		}

		if (reason.isEmpty()) {

			reason = safeValue(rejectedReason.getRejectedReasonCode()).trim();

		}

		String remarks = safeValue(outwardChequeDataEntryTxtRejectRemarks.getValue()).trim();

		if (reasonId.isEmpty() || reason.isEmpty()) {

			showStatus("Please select a valid rejection reason.");

			return;

		}

		if (remarks.isEmpty()) {

			showStatus("Please enter rejection remarks.");

			return;

		}

		try {

			if (STATUS_PENDING_DATA_ENTRY.equals(status) || STATUS_PENDING_VERIFICATION.equals(status)

					|| STATUS_ON_HOLD.equals(status)) {

				if (!validateFields()) {

					return;

				}

				populateOutwardChequeFromFields(cheque);

			}

			cheque.setOutwardBatchId(outwardBatchId);

			String batchForSave = !isBlank(scannedBatchId) ? scannedBatchId : outwardBatchId;

			boolean savedRequest = outwardChequeService.saveMakerRejectionRequest(batchForSave, cheque, reasonId,

					reason, remarks);

			if (!savedRequest) {

				closeRejectWindow();

				showStatus(

						"Unable to create rejection request. A rejection request may already exist for this cheque.");

				return;

			}

			cheque.setChequeStatus(STATUS_REJECTION_REQUEST);

			syncChequeToFullList(cheque);

			hasUnsavedChanges = false;

			closeRejectWindow();

			updateBatchSummary();

			rebuildActiveQueue();

			int next = findNextPendingDataEntry(0);

			if (next >= 0) {

				currentChequeIndex = next;

				loadCurrentCheque();

			} else if (hasPendingMicrRepair()) {

				showWaitingForMicrRepair();

			} else {

				int completed = countCompletedMakerCheques();

				if (completed == getBatchTotal()) {

					showAllCompleted();

				}

			}

			updateSubmitButton();

			showStatus("Rejection request submitted successfully.");

		} catch (Exception exception) {

			closeRejectWindow();

			showStatus("Unable to create rejection request: " + safeExceptionMessage(exception));

		}

	}

	private void loadRejectedReasons() {

		outwardChequeDataEntryCmbRejectReason.getItems().clear();

		try {

			List<RejectedReason> reasons = rejectedReasonService.getAllRejectedReasons();

			if (reasons == null) {

				return;

			}

			for (RejectedReason reason : reasons) {

				if (reason == null) {

					continue;

				}

				String code = safeValue(reason.getRejectedReasonCode()).trim();

				String name = safeValue(reason.getRejectedReasonName()).trim();

				String description = safeValue(reason.getRejectedReasonDescription()).trim();

				String label;

				if (!code.isEmpty() && !name.isEmpty()) {

					label = code + " - " + name;

				} else if (!name.isEmpty()) {

					label = name;

				} else {

					label = code;

				}

				Comboitem item = new Comboitem();

				item.setLabel(label);

				item.setValue(reason);

				if (!description.isEmpty()) {

					item.setTooltiptext(description);

				}

				outwardChequeDataEntryCmbRejectReason.appendChild(item);

			}

		} catch (Exception exception) {

			showStatus("Unable to load rejection reasons: " + safeExceptionMessage(exception));

		}

	}

	private void submitToChecker() {

		if (outwardChequeList == null || outwardChequeList.isEmpty()) {

			showStatus("There are no cheques to submit.");

			return;

		}

		if (isBlank(outwardBatchId)) {

			showStatus("Batch ID is missing.");

			return;

		}

		if (hasUnsavedChanges) {

			showStatus("Save or reset the current cheque before submitting.");

			return;

		}

		if (hasDataEntryPendingCheque()) {

			showStatus("All Data Entry cheques must be completed before submission.");

			updateSubmitButton();

			return;

		}

		if (hasPendingMicrRepair()) {

			int count = countMicrPending();

			showStatus(count + " cheque" + (count == 1 ? "" : "s") + " required MICR processing.");

			updateSubmitButton();

			return;

		}

		int total = getBatchTotal();

		int completed = countCompletedMakerCheques();

		if (completed != total) {

			showStatus("All cheques must be completed before submission.");

			updateSubmitButton();

			return;

		}

		showSubmitConfirmation(total, completed);

	}

	private void showSubmitConfirmation(int total, int completed) {

		outwardChequeDataEntryLblConfirmBatchId.setValue(isBlank(outwardBatchId) ? "-" : outwardBatchId);

		outwardChequeDataEntryLblConfirmTotal.setValue(String.valueOf(total));

		outwardChequeDataEntrySubmitConfirmModal.setVisible(true);

		outwardChequeDataEntrySubmitConfirmModal.doModal();

	}

	private void completeBatchSubmission() {

		try {

			int total = getBatchTotal();

			int completed = countCompletedMakerCheques();

			if (completed != total) {

				showStatus("All cheques must be completed before submission.");

				updateSubmitButton();

				return;

			}

			String batchForSubmit = !isBlank(scannedBatchId) ? scannedBatchId : outwardBatchId;

			boolean submitted = outwardChequeService.submitMakerBatchToChecker(batchForSubmit);

			if (!submitted) {

				showStatus("Unable to submit batch to Checker.");

				return;

			}

			boolean notificationSent = false;

			try {

				notificationSent = notificationService.sendNotification(NOTIFICATION_ROLE_OUTWARD_CHECKER, null,

						"Batch " + outwardBatchId + " is ready for Checker processing.");

			} catch (Exception exception) {

				notificationSent = false;

			}

			batchSubmitted = true;

			if (micrRepairRefreshTimer != null) {

				micrRepairRefreshTimer.stop();

			}

			showSubmissionSuccess(notificationSent, total);

		} catch (Exception exception) {

			showStatus("Unable to submit batch to Checker: " + safeExceptionMessage(exception));

		}

	}

	private void showSubmissionSuccess(boolean notificationSent, int total) {

		outwardChequeDataEntryLblSubmitSuccess.setValue("Batch " + outwardBatchId + " submitted successfully.");

		String details = "Total Cheques: " + total + " / " + total + " | Status: "

				+ displayStatus(STATUS_PENDING_CHECKER_PROCESS);

		if (!notificationSent) {

			details += " | Checker notification could not be sent.";

		}

		outwardChequeDataEntryLblSubmitSuccessDetails.setValue(details);

		outwardChequeDataEntrySubmitSuccessModal.setVisible(true);

		outwardChequeDataEntrySubmitSuccessModal.doModal();

	}

	private void showWaitingForMicrRepair() {

		currentChequeIndex = -1;

		clearChequeFields();

		setChequeFieldsEditable(false);

		outwardChequeDataEntryBtnResetItem.setDisabled(true);

		outwardChequeDataEntryBtnReject.setDisabled(true);

		outwardChequeDataEntryBtnApprove.setDisabled(true);

		outwardChequeDataEntryBtnPrevious.setDisabled(true);

		outwardChequeDataEntryBtnNext.setDisabled(true);

		outwardChequeDataEntryLblChequePosition.setValue("Waiting for MICR Repair");

		outwardChequeDataEntryTxtChequeNumber.setValue("");

		outwardChequeDataEntryLblFooterStatus.setValue("Pending MICR Repair");

		outwardChequeDataEntryLblImageEmpty.setValue("Remaining cheques are awaiting MICR processing.");

		outwardChequeDataEntryChequeImage.setVisible(false);

		outwardChequeDataEntryLblImageEmpty.setVisible(true);

		hideAllPanels();

		outwardChequeDataEntryMicrPendingPanel.setVisible(true);

		outwardChequeDataEntryLblMicrPendingMessage.setValue(countMicrPending() + " cheque"

				+ (countMicrPending() == 1 ? "" : "s") + " are awaiting MICR processing.");

		updateSubmitButton();

	}

	private void showAllCompleted() {

		clearChequeFields();

		setChequeFieldsEditable(false);

		outwardChequeDataEntryBtnResetItem.setDisabled(true);

		outwardChequeDataEntryBtnReject.setDisabled(true);

		outwardChequeDataEntryBtnApprove.setDisabled(true);

		outwardChequeDataEntryBtnPrevious.setDisabled(true);

		outwardChequeDataEntryBtnNext.setDisabled(true);

		outwardChequeDataEntryLblChequePosition.setValue("All Cheques Completed");

		outwardChequeDataEntryTxtChequeNumber.setValue("");

		outwardChequeDataEntryLblFooterStatus.setValue("Completed");

		outwardChequeDataEntryLblFooterStatus

				.setValue("All maker activities are completed. Batch is ready for Checker.");

		updateBatchSummary();

		updateSubmitButton();

	}

	private void startMicrRepairRefreshTimer() {

		if (micrRepairRefreshTimer != null) {

			return;

		}

		micrRepairRefreshTimer = new Timer();

		micrRepairRefreshTimer.setDelay(3000);

		micrRepairRefreshTimer.setRepeats(true);

		micrRepairRefreshTimer.addEventListener("onTimer", event -> refreshAfterMicrRepair());

		outwardChequeDataEntryWin.appendChild(micrRepairRefreshTimer);

	}

	private void refreshAfterMicrRepair() {

		if (batchSubmitted || isBlank(outwardBatchId) || hasUnsavedChanges) {

			return;

		}

		if (!hasPendingMicrRepair()) {

			return;

		}

		try {

			List<OutwardCheque> latest = outwardChequeService.getChequesByBatchId(outwardBatchId.trim());

			if (latest == null) {

				return;

			}

			int oldPending = countStatus(outwardChequeList, STATUS_PENDING_DATA_ENTRY);

			int oldMicr = countStatus(outwardChequeList, STATUS_PENDING_MICR_REPAIR);

			int newPending = countStatus(latest, STATUS_PENDING_DATA_ENTRY);

			int newMicr = countStatus(latest, STATUS_PENDING_MICR_REPAIR);

			if (oldPending == newPending && oldMicr == newMicr) {

				return;

			}

			OutwardCheque current = getCurrentCheque();

			String currentId = current == null ? "" : safeValue(current.getOutwardChequeId()).trim();

			String currentNumber = current == null ? "" : safeValue(current.getChequeNumber()).trim();

			outwardChequeList = new ArrayList<>(latest);

			rebuildActiveQueue();

			updateBatchSummary();

			int keep = findQueueIndex(currentId, currentNumber);

			if (keep >= 0) {

				currentChequeIndex = keep;

				loadCurrentCheque();

			} else {

				int first = findFirstPendingDataEntry();

				if (first >= 0) {

					currentChequeIndex = first;

					loadCurrentCheque();

				} else if (hasPendingMicrRepair()) {

					showWaitingForMicrRepair();

				} else {

					showAllCompleted();

				}

			}

			if (newPending > oldPending) {

				showStatus("MICR repair completed. The repaired cheque is now available for Data Entry.");

			}

			updateSubmitButton();

		} catch (Exception exception) {

			showStatus("Unable to refresh MICR status: " + safeExceptionMessage(exception));

		}

	}

	private int countStatus(List<OutwardCheque> list, String status) {

		if (list == null) {

			return 0;

		}

		int count = 0;

		for (OutwardCheque cheque : list) {

			if (cheque != null && status.equals(normalizeChequeStatus(cheque.getChequeStatus()))) {

				count++;

			}

		}

		return count;

	}

	private int findQueueIndex(String id, String number) {

		if (activeQueue == null) {

			return -1;

		}

		for (int i = 0; i < activeQueue.size(); i++) {

			OutwardCheque cheque = activeQueue.get(i);

			if (cheque == null) {

				continue;

			}

			if (!id.isEmpty() && id.equalsIgnoreCase(safeValue(cheque.getOutwardChequeId()).trim())) {

				return i;

			}

			if (!number.isEmpty() && number.equalsIgnoreCase(safeValue(cheque.getChequeNumber()).trim())) {

				return i;

			}

		}

		return -1;

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

		return null;

	}

	private boolean containsChequeById(List<OutwardCheque> list, OutwardCheque cheque) {

		if (list == null || cheque == null) {

			return false;

		}

		String id = safeValue(cheque.getOutwardChequeId()).trim();

		if (id.isEmpty()) {

			return false;

		}

		for (OutwardCheque item : list) {

			if (item != null && id.equalsIgnoreCase(safeValue(item.getOutwardChequeId()).trim())) {

				return true;

			}

		}

		return false;

	}

	private void syncChequeToFullList(OutwardCheque cheque) {

		if (cheque == null || outwardChequeList == null) {

			return;

		}

		String id = safeValue(cheque.getOutwardChequeId()).trim();

		String number = safeValue(cheque.getChequeNumber()).trim();

		for (int i = 0; i < outwardChequeList.size(); i++) {

			OutwardCheque item = outwardChequeList.get(i);

			if (item == null) {

				continue;

			}

			String itemId = safeValue(item.getOutwardChequeId()).trim();

			String itemNumber = safeValue(item.getChequeNumber()).trim();

			if ((!id.isEmpty() && id.equalsIgnoreCase(itemId))

					|| (!number.isEmpty() && number.equalsIgnoreCase(itemNumber))) {

				outwardChequeList.set(i, cheque);

				return;

			}

		}

		outwardChequeList.add(cheque);

	}

	private void loadImage(String imagePath) {

		if (isBlank(imagePath)) {

			outwardChequeDataEntryChequeImage.setVisible(false);

			outwardChequeDataEntryLblImageEmpty.setVisible(true);

			outwardChequeDataEntryLblImageEmpty.setValue("Cheque image unavailable.");

			return;

		}

		try {

			String path = imagePath.trim();

			if (!path.startsWith("/") && !path.startsWith("http://") && !path.startsWith("https://")) {

				path = "/" + path;

			}

			outwardChequeDataEntryChequeImage.setSrc(path);

			outwardChequeDataEntryChequeImage.setVisible(true);

			outwardChequeDataEntryLblImageEmpty.setVisible(false);

			outwardChequeDataEntryLblImageEmpty.setVisible(false);

			resetImage();

		} catch (Exception exception) {

			outwardChequeDataEntryChequeImage.setVisible(false);

			outwardChequeDataEntryLblImageEmpty.setVisible(true);

			outwardChequeDataEntryLblImageEmpty.setValue("Cheque image unavailable.");

		}

	}

	private void resetImageState() {

		showingFrontImage = true;

		imageZoom = 1.0;

		imageRotation = 0;

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

	private void clearChequeFields() {

		outwardChequeDataEntryTxtChequeNumber.setValue("");

		outwardChequeDataEntryTxtAmount.setValue((BigDecimal) null);

		outwardChequeDataEntryDtChequeDate.setValue(null);

		outwardChequeDataEntryTxtMicrCode.setValue("");

		outwardChequeDataEntryTxtPayeeAccount.setValue("");

		outwardChequeDataEntryTxtPayeeName.setValue("");

		outwardChequeDataEntryTxtDraweeName.setValue("");

		outwardChequeDataEntryLblPostDated.setVisible(false);

	}

	private void clearPage() {

		clearChequeFields();

		setChequeFieldsEditable(false);

		outwardChequeDataEntryLblBatchId.setValue(isBlank(outwardBatchId) ? "-" : outwardBatchId);

		outwardChequeDataEntryLblTotalCheques.setValue("0");
		outwardChequeDataEntryProgress.setValue(0);
		outwardChequeDataEntryLblProgress.setValue("0%");
		outwardChequeDataEntryLblProgressCount.setValue("0 of 0 completed");

		outwardChequeDataEntryLblFooterStatus.setValue("-");

		outwardChequeDataEntryLblChequePosition.setValue("Cheque Data Entry");

		outwardChequeDataEntryTxtChequeNumber.setValue("");

		outwardChequeDataEntryBtnResetItem.setDisabled(true);

		outwardChequeDataEntryBtnReject.setDisabled(true);

		outwardChequeDataEntryBtnApprove.setDisabled(true);

		outwardChequeDataEntryBtnSubmit.setDisabled(true);

		outwardChequeDataEntryBtnPrevious.setDisabled(true);

		outwardChequeDataEntryBtnNext.setDisabled(true);

		outwardChequeDataEntryChequeImage.setVisible(false);

		outwardChequeDataEntryLblImageEmpty.setVisible(true);

	}

	private void showStatus(String message) {

		if (message == null || message.trim().isEmpty()) {

			return;

		}

		outwardChequeDataEntryLblFooterStatus.setValue(message);

	}

	private void hideStatus() {

	}

	private void backToDataEntryList() {

		if (hasUnsavedChanges) {

			showStatus("Save or reset the current cheque before leaving.");

			return;

		}

		if (micrRepairRefreshTimer != null) {

			micrRepairRefreshTimer.stop();

		}

		Executions.getCurrent().getSession().removeAttribute("RETURN_FROM_CHECKER");

		Executions.getCurrent().getSession().removeAttribute("OUTWARD_DATA_ENTRY_BATCH_ID");

		Executions.getCurrent().getSession().removeAttribute("OUTWARD_DATA_ENTRY_SCANNED_BATCH_ID");

		Component current = outwardChequeDataEntryWin;

		org.zkoss.zul.Include mainContentArea = null;

		Window rootWindow = null;

		while (current != null) {

			if (current instanceof org.zkoss.zul.Include && "mainContentArea".equals(current.getId())) {

				mainContentArea = (org.zkoss.zul.Include) current;

			}

			if (current instanceof Window) {

				rootWindow = (Window) current;

				break;

			}

			current = current.getParent();

		}

		if (rootWindow != null) {

			Component headerComponent = rootWindow.getFellowIfAny("headerInclude");

			Component sidebarComponent = rootWindow.getFellowIfAny("sidebarInclude");

			if (headerComponent instanceof org.zkoss.zul.Include) {

				org.zkoss.zul.Include headerInclude = (org.zkoss.zul.Include) headerComponent;

				headerInclude.clearDynamicProperties();

				headerInclude.setDynamicProperty("pageTitle", "Data Entry");

				headerInclude.setDynamicProperty("pageSubtitle", "Outward Processing");

				headerInclude.setSrc("/components/header.zul");

			}

			if (sidebarComponent instanceof org.zkoss.zul.Include) {

				org.zkoss.zul.Include sidebarInclude = (org.zkoss.zul.Include) sidebarComponent;

				sidebarInclude.clearDynamicProperties();

				sidebarInclude.setDynamicProperty("role", "OUTWARD_MAKER");

				sidebarInclude.setDynamicProperty("activeTab", "outward-data-entry");

				sidebarInclude.setSrc("/components/sidebar.zul");

			}

		}

		if (mainContentArea == null) {

			Executions.sendRedirect("/outward/maker/data-entry.zul");

			return;

		}

		mainContentArea.clearDynamicProperties();

		mainContentArea.setDynamicProperty("mode", "DATA_ENTRY");

		mainContentArea.setSrc("/outward/maker/data-entry.zul");

	}

	private String normalizeChequeStatus(String status) {

		if (status == null || status.trim().isEmpty()) {

			return "";

		}

		return status.trim().toUpperCase().replace("-", "_").replace(" ", "_");

	}

	private String displayStatus(String status) {

		String normalized = normalizeChequeStatus(status);

		if (normalized.isEmpty()) {

			return "-";

		}

		String[] words = normalized.toLowerCase().split("_");

		StringBuilder result = new StringBuilder();

		for (String word : words) {

			if (word == null || word.isEmpty()) {

				continue;

			}

			if (result.length() > 0) {

				result.append(" ");

			}

			result.append(Character.toUpperCase(word.charAt(0)));

			if (word.length() > 1) {

				result.append(word.substring(1));

			}

		}

		return result.toString();

	}

	private String safeDecimalValue(BigDecimal value) {

		return value == null ? "" : value.toPlainString();

	}

	private String safeValue(String value) {

		return value == null ? "" : value;

	}

	private boolean isBlank(Object value) {

		return value == null || value.toString().trim().isEmpty();

	}

	private String safeExceptionMessage(Exception exception) {

		if (exception == null || exception.getMessage() == null || exception.getMessage().trim().isEmpty()) {

			return "Unexpected error";

		}

		return exception.getMessage();

	}

}
