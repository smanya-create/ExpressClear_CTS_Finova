package com.iispl.cts.controller.outward.maker;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Window;
import org.zkoss.zul.Include;

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

	private static final String DASHBOARD_ZUL = "/outward/maker/data-entry.zul";

	private Window outwardChequeDataEntryWin;

	private Button outwardChequeDataEntryBtnBack;
	private Button outwardChequeDataEntryBtnImageToggle;
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

	private Button outwardChequeDataEntryBtnCancelReject;
	private Button outwardChequeDataEntryBtnConfirmReject;

	private Button outwardChequeDataEntryBtnCancelSubmit;
	private Button outwardChequeDataEntryBtnConfirmSubmit;

	private Button outwardChequeDataEntryBtnGoToDataEntry;

	private Label outwardChequeDataEntryLblTitle;
	private Label outwardChequeDataEntryLblSubtitle;
	private Label outwardChequeDataEntryLblRecord;
	private Label outwardChequeDataEntryLblBatchId;
	private Label outwardChequeDataEntryLblTotal;
	private Label outwardChequeDataEntryLblRecordImage;
	private Label outwardChequeDataEntryLblFooterStatus;
	private Label outwardChequeDataEntryLblImageMessage;
	private Label outwardChequeDataEntryLblImageEmpty;
	private Label outwardChequeDataEntryLblPostDated;
	private Label outwardChequeDataEntryLblMicrReason;
	private Label outwardChequeDataEntryLblRejectionReason;
	private Label outwardChequeDataEntryLblRejectionRemarks;
	private Label outwardChequeDataEntryLblCheckerSendBackMessage;
	private Label outwardChequeDataEntryLblMicrRejectedMessage;
	private Label outwardChequeDataEntryLblMicrPendingMessage;
	private Label outwardChequeDataEntryLblChequeStatus;
	private Label outwardChequeDataEntryLblValidationStatus;

	private Image outwardChequeDataEntryChequeImage;

	private Textbox outwardChequeDataEntryTxtChequeNumber;
	private Textbox outwardChequeDataEntryTxtAmount;
	private Datebox outwardChequeDataEntryDtChequeDate;
	private Textbox outwardChequeDataEntryTxtMicrCode;
	private Textbox outwardChequeDataEntryTxtPayeeAccount;
	private Textbox outwardChequeDataEntryTxtPayeeName;
	private Textbox outwardChequeDataEntryTxtDraweeName;

	private Combobox outwardChequeDataEntryCmbRejectReason;
	private Textbox outwardChequeDataEntryTxtRejectRemarks;

	private Component outwardChequeDataEntryMakerRejectionPanel;
	private Component outwardChequeDataEntryCheckerSendBackPanel;
	private Component outwardChequeDataEntryMicrRejectedPanel;
	private Component outwardChequeDataEntryMicrPendingPanel;

	private Progressmeter outwardChequeDataEntryProgress;

	private Window outwardChequeDataEntryRejectModal;
	private Window outwardChequeDataEntrySubmitConfirmModal;
	private Window outwardChequeDataEntrySubmitSuccessModal;

	private Label outwardChequeDataEntryLblConfirmBatchId;
	private Label outwardChequeDataEntryLblConfirmTotal;
	private Label outwardChequeDataEntryLblConfirmCompleted;

	private Label outwardChequeDataEntryLblSubmitSuccess;
	private Label outwardChequeDataEntryLblSubmitSuccessDetails;

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

	private boolean hasUnsavedChanges;
	private boolean batchSubmitted;

	private Timer micrRepairRefreshTimer;

	@Override
	public void doAfterCompose(Component component) throws Exception {

		super.doAfterCompose(component);

		outwardChequeDataEntryWin = component instanceof Window ? (Window) component : null;

		outwardBatchService = new OutwardBatchServiceImpl();
		outwardChequeService = new OutwardChequeServiceImpl();
		scanService = new ScanServiceImpl();
		notificationService = new NotificationServiceImpl();
		rejectedReasonService = RejectedReasonServiceImpl.getInstance();

		bindComponents(component);
		configureDatebox();
		resolveBatchContext();
		registerEvents();
		loadRejectedReasons();
		loadDataEntryData();
		startMicrRepairRefreshTimer();
	}

	private void bindComponents(Component component) {

		outwardChequeDataEntryBtnBack = getButton(component, "outwardChequeDataEntryBtnBack");
		outwardChequeDataEntryBtnImageToggle = getButton(component, "outwardChequeDataEntryBtnImageToggle");
		outwardChequeDataEntryBtnZoomOut = getButton(component, "outwardChequeDataEntryBtnZoomOut");
		outwardChequeDataEntryBtnZoomIn = getButton(component, "outwardChequeDataEntryBtnZoomIn");
		outwardChequeDataEntryBtnRotate = getButton(component, "outwardChequeDataEntryBtnRotate");
		outwardChequeDataEntryBtnReset = getButton(component, "outwardChequeDataEntryBtnReset");
		outwardChequeDataEntryBtnPrevious = getButton(component, "outwardChequeDataEntryBtnPrevious");
		outwardChequeDataEntryBtnNext = getButton(component, "outwardChequeDataEntryBtnNext");
		outwardChequeDataEntryBtnResetItem = getButton(component, "outwardChequeDataEntryBtnResetItem");
		outwardChequeDataEntryBtnReject = getButton(component, "outwardChequeDataEntryBtnReject");
		outwardChequeDataEntryBtnApprove = getButton(component, "outwardChequeDataEntryBtnApprove");
		outwardChequeDataEntryBtnSubmit = getButton(component, "outwardChequeDataEntryBtnSubmit");

		outwardChequeDataEntryBtnCancelReject = getButton(component, "outwardChequeDataEntryBtnCancelReject");

		outwardChequeDataEntryBtnConfirmReject = getButton(component, "outwardChequeDataEntryBtnConfirmReject");

		outwardChequeDataEntryBtnCancelSubmit = getButton(component, "outwardChequeDataEntryBtnCancelSubmit");

		outwardChequeDataEntryBtnConfirmSubmit = getButton(component, "outwardChequeDataEntryBtnConfirmSubmit");

		outwardChequeDataEntryBtnGoToDataEntry = getButton(component, "outwardChequeDataEntryBtnGoToDataEntry");

		outwardChequeDataEntryLblTitle = getLabel(component, "outwardChequeDataEntryLblTitle");

		outwardChequeDataEntryLblSubtitle = getLabel(component, "outwardChequeDataEntryLblSubtitle");

		outwardChequeDataEntryLblRecord = getLabel(component, "outwardChequeDataEntryLblRecord");

		outwardChequeDataEntryLblBatchId = getLabel(component, "outwardChequeDataEntryLblBatchId");

		outwardChequeDataEntryLblTotal = getLabel(component, "outwardChequeDataEntryLblTotal");

		outwardChequeDataEntryLblRecordImage = getLabel(component, "outwardChequeDataEntryLblRecordImage");

		outwardChequeDataEntryLblFooterStatus = getLabel(component, "outwardChequeDataEntryLblFooterStatus");

		outwardChequeDataEntryLblImageMessage = getLabel(component, "outwardChequeDataEntryLblImageMessage");

		outwardChequeDataEntryLblImageEmpty = getLabel(component, "outwardChequeDataEntryLblImageEmpty");

		outwardChequeDataEntryLblPostDated = getLabel(component, "outwardChequeDataEntryLblPostDated");

		outwardChequeDataEntryLblMicrReason = getLabel(component, "outwardChequeDataEntryLblMicrReason");

		outwardChequeDataEntryLblRejectionReason = getLabel(component, "outwardChequeDataEntryLblRejectionReason");

		outwardChequeDataEntryLblRejectionRemarks = getLabel(component, "outwardChequeDataEntryLblRejectionRemarks");

		outwardChequeDataEntryLblCheckerSendBackMessage = getLabel(component,
				"outwardChequeDataEntryLblCheckerSendBackMessage");

		outwardChequeDataEntryLblMicrRejectedMessage = getLabel(component,
				"outwardChequeDataEntryLblMicrRejectedMessage");

		outwardChequeDataEntryLblMicrPendingMessage = getLabel(component,
				"outwardChequeDataEntryLblMicrPendingMessage");

		outwardChequeDataEntryLblChequeStatus = getLabel(component, "outwardChequeDataEntryLblChequeStatus");

		outwardChequeDataEntryLblValidationStatus = getLabel(component, "outwardChequeDataEntryLblValidationStatus");

		outwardChequeDataEntryChequeImage = getImage(component, "outwardChequeDataEntryChequeImage");

		outwardChequeDataEntryTxtChequeNumber = getTextbox(component, "outwardChequeDataEntryTxtChequeNumber");

		outwardChequeDataEntryTxtAmount = getTextbox(component, "outwardChequeDataEntryTxtAmount");

		outwardChequeDataEntryDtChequeDate = getDatebox(component, "outwardChequeDataEntryDtChequeDate");

		outwardChequeDataEntryTxtMicrCode = getTextbox(component, "outwardChequeDataEntryTxtMicrCode");

		outwardChequeDataEntryTxtPayeeAccount = getTextbox(component, "outwardChequeDataEntryTxtPayeeAccount");

		outwardChequeDataEntryTxtPayeeName = getTextbox(component, "outwardChequeDataEntryTxtPayeeName");

		outwardChequeDataEntryTxtDraweeName = getTextbox(component, "outwardChequeDataEntryTxtDraweeName");

		outwardChequeDataEntryMakerRejectionPanel = component
				.getFellowIfAny("outwardChequeDataEntryMakerRejectionPanel");

		outwardChequeDataEntryCheckerSendBackPanel = component
				.getFellowIfAny("outwardChequeDataEntryCheckerSendBackPanel");

		outwardChequeDataEntryMicrRejectedPanel = component.getFellowIfAny("outwardChequeDataEntryMicrRejectedPanel");

		outwardChequeDataEntryMicrPendingPanel = component.getFellowIfAny("outwardChequeDataEntryMicrPendingPanel");

		outwardChequeDataEntryProgress = getProgressmeter(component, "outwardChequeDataEntryProgress");

		outwardChequeDataEntryRejectModal = getWindow(component, "outwardChequeDataEntryRejectModal");

		outwardChequeDataEntrySubmitConfirmModal = getWindow(component, "outwardChequeDataEntrySubmitConfirmModal");

		outwardChequeDataEntrySubmitSuccessModal = getWindow(component, "outwardChequeDataEntrySubmitSuccessModal");

		if (outwardChequeDataEntryRejectModal != null) {

			outwardChequeDataEntryCmbRejectReason = getCombobox(outwardChequeDataEntryRejectModal,
					"outwardChequeDataEntryCmbRejectReason");

			outwardChequeDataEntryTxtRejectRemarks = getTextbox(outwardChequeDataEntryRejectModal,
					"outwardChequeDataEntryTxtRejectRemarks");
		}

		if (outwardChequeDataEntrySubmitConfirmModal != null) {

			outwardChequeDataEntryLblConfirmBatchId = getLabel(outwardChequeDataEntrySubmitConfirmModal,
					"outwardChequeDataEntryLblConfirmBatchId");

			outwardChequeDataEntryLblConfirmTotal = getLabel(outwardChequeDataEntrySubmitConfirmModal,
					"outwardChequeDataEntryLblConfirmTotal");

			outwardChequeDataEntryLblConfirmCompleted = getLabel(outwardChequeDataEntrySubmitConfirmModal,
					"outwardChequeDataEntryLblConfirmCompleted");
		}

		if (outwardChequeDataEntrySubmitSuccessModal != null) {

			outwardChequeDataEntryLblSubmitSuccess = getLabel(outwardChequeDataEntrySubmitSuccessModal,
					"outwardChequeDataEntryLblSubmitSuccess");

			outwardChequeDataEntryLblSubmitSuccessDetails = getLabel(outwardChequeDataEntrySubmitSuccessModal,
					"outwardChequeDataEntryLblSubmitSuccessDetails");
		}

		hideModal(outwardChequeDataEntryRejectModal);
		hideModal(outwardChequeDataEntrySubmitConfirmModal);
		hideModal(outwardChequeDataEntrySubmitSuccessModal);
	}

	private Button getButton(Component parent, String id) {

		if (parent == null || id == null) {
			return null;
		}

		Component component = parent.getFellowIfAny(id);

		return component instanceof Button ? (Button) component : null;
	}

	private Label getLabel(Component parent, String id) {

		if (parent == null || id == null) {
			return null;
		}

		Component component = parent.getFellowIfAny(id);

		return component instanceof Label ? (Label) component : null;
	}

	private Textbox getTextbox(Component parent, String id) {

		if (parent == null || id == null) {
			return null;
		}

		Component component = parent.getFellowIfAny(id);

		return component instanceof Textbox ? (Textbox) component : null;
	}

	private Datebox getDatebox(Component parent, String id) {

		if (parent == null || id == null) {
			return null;
		}

		Component component = parent.getFellowIfAny(id);

		return component instanceof Datebox ? (Datebox) component : null;
	}

	private Combobox getCombobox(Component parent, String id) {

		if (parent == null || id == null) {
			return null;
		}

		Component component = parent.getFellowIfAny(id);

		return component instanceof Combobox ? (Combobox) component : null;
	}

	private Image getImage(Component parent, String id) {

		if (parent == null || id == null) {
			return null;
		}

		Component component = parent.getFellowIfAny(id);

		return component instanceof Image ? (Image) component : null;
	}

	private Progressmeter getProgressmeter(Component parent, String id) {

		if (parent == null || id == null) {
			return null;
		}

		Component component = parent.getFellowIfAny(id);

		return component instanceof Progressmeter ? (Progressmeter) component : null;
	}

	private Window getWindow(Component parent, String id) {

		if (parent == null || id == null) {
			return null;
		}

		Component component = parent.getFellowIfAny(id);

		return component instanceof Window ? (Window) component : null;
	}

	private void hideModal(Window window) {

		if (window != null) {
			window.setVisible(false);
		}
	}

	private void configureDatebox() {

		if (outwardChequeDataEntryDtChequeDate == null) {
			return;
		}

		outwardChequeDataEntryDtChequeDate.setFormat("dd/MM/yyyy");
		outwardChequeDataEntryDtChequeDate.setButtonVisible(true);
	}

	private void resolveBatchContext() {

		outwardBatchId = null;
		scannedBatchId = null;
		outwardBatch = null;

		Object batchId = Executions.getCurrent().getAttribute("batchId");

		if (isBlank(batchId)) {
			batchId = Executions.getCurrent().getArg().get("batchId");
		}

		if (isBlank(batchId)) {
			batchId = Executions.getCurrent().getSession().getAttribute("OUTWARD_DATA_ENTRY_BATCH_ID");
		}

		if (isBlank(batchId)) {
			batchId = Executions.getCurrent().getSession().getAttribute("batchId");
		}

		if (isBlank(batchId)) {
			batchId = Executions.getCurrent().getParameter("batchId");
		}

		if (isBlank(batchId)) {
			throw new IllegalArgumentException("Batch ID is required.");
		}

		String suppliedBatchId = batchId.toString().trim();

		try {

			OutwardBatch existing = outwardBatchService.getBatchById(suppliedBatchId);

			if (existing != null) {

				outwardBatchId = suppliedBatchId;
				outwardBatch = existing;

				try {

					scannedBatchId = outwardBatchService.getScannedBatchIdByOutwardBatchId(outwardBatchId);

				} catch (Exception ignored) {

					scannedBatchId = suppliedBatchId;
				}

			} else {

				scannedBatchId = suppliedBatchId;

				try {

					String resolved = outwardBatchService.getOutwardBatchIdByScannedBatchId(scannedBatchId);

					if (!isBlank(resolved)) {

						outwardBatchId = resolved.trim();

						outwardBatch = outwardBatchService.getBatchById(outwardBatchId);
					}

				} catch (Exception ignored) {

					outwardBatchId = suppliedBatchId;
				}
			}

		} catch (Exception ignored) {

			outwardBatchId = suppliedBatchId;
			scannedBatchId = suppliedBatchId;
		}

		if (isBlank(outwardBatchId)) {
			outwardBatchId = suppliedBatchId;
		}

		if (isBlank(scannedBatchId)) {
			scannedBatchId = outwardBatchId;
		}

		Executions.getCurrent().getSession().setAttribute("OUTWARD_DATA_ENTRY_BATCH_ID", outwardBatchId);

		Executions.getCurrent().getSession().setAttribute("OUTWARD_DATA_ENTRY_SCANNED_BATCH_ID", scannedBatchId);
	}

	private void registerEvents() {

		addClick(outwardChequeDataEntryBtnBack, event -> goBackToDashboard());

		addClick(outwardChequeDataEntryBtnImageToggle, event -> toggleImage());

		addClick(outwardChequeDataEntryBtnZoomOut, event -> zoomOut());

		addClick(outwardChequeDataEntryBtnZoomIn, event -> zoomIn());

		addClick(outwardChequeDataEntryBtnRotate, event -> rotateImage());

		addClick(outwardChequeDataEntryBtnReset, event -> resetImage());

		addClick(outwardChequeDataEntryBtnPrevious, event -> previousCheque());

		addClick(outwardChequeDataEntryBtnNext, event -> nextCheque());

		addClick(outwardChequeDataEntryBtnResetItem, event -> resetCurrentItem());

		addClick(outwardChequeDataEntryBtnReject, event -> openRejectWindow());

		addClick(outwardChequeDataEntryBtnApprove, event -> saveCurrentCheque());

		addClick(outwardChequeDataEntryBtnSubmit, event -> submitToChecker());

		addClick(outwardChequeDataEntryBtnCancelReject, event -> closeRejectWindow());

		addClick(outwardChequeDataEntryBtnConfirmReject, event -> requestReject());

		addClick(outwardChequeDataEntryBtnCancelSubmit, event -> hideModal(outwardChequeDataEntrySubmitConfirmModal));

		addClick(outwardChequeDataEntryBtnConfirmSubmit, event -> {

			hideModal(outwardChequeDataEntrySubmitConfirmModal);

			completeBatchSubmission();
		});

		addClick(outwardChequeDataEntryBtnGoToDataEntry, event -> {

			hideModal(outwardChequeDataEntrySubmitSuccessModal);

			goBackToDashboard();
		});

		registerDirtyTracking(outwardChequeDataEntryTxtChequeNumber);
		registerDirtyTracking(outwardChequeDataEntryTxtAmount);
		registerDirtyTracking(outwardChequeDataEntryDtChequeDate);

		registerDirtyTracking(outwardChequeDataEntryTxtPayeeAccount);
		registerDirtyTracking(outwardChequeDataEntryTxtPayeeName);
		registerDirtyTracking(outwardChequeDataEntryTxtDraweeName);
	}

	private interface EventAction {

		void execute(Event event) throws Exception;
	}

	private void addClick(Button button, EventAction action) {

		if (button == null || action == null) {
			return;
		}

		button.addEventListener(Events.ON_CLICK, event -> {

			try {
				action.execute(event);
			} catch (Exception exception) {
				showStatus("Operation failed: " + safeExceptionMessage(exception));
			}
		});
	}

	private void registerDirtyTracking(Component component) {

		if (component == null) {
			return;
		}

		component.addEventListener(Events.ON_CHANGE, event -> {

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
			loadExistingCheques();
			mergeScanAndOutwardCheques();
			rebuildActiveQueue();

			updateBatchSummary();

			int firstPending = findFirstPendingDataEntry();

			if (firstPending >= 0) {

				currentChequeIndex = firstPending;

				loadCurrentCheque();

			} else if (!activeQueue.isEmpty()) {

				currentChequeIndex = 0;

				loadCurrentCheque();

			} else {

				showWaitingForMicrRepair();
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

			List<ScanCheque> scans = scanService.getChequesByBatchId(scannedBatchId);

			if (scans != null) {
				scanChequeList.addAll(scans);
			}

		} catch (Exception ignored) {

			scanChequeList = new ArrayList<>();
		}
	}

	private void loadExistingCheques() {

		outwardChequeList = new ArrayList<>();

		if (isBlank(outwardBatchId)) {
			return;
		}

		try {

			List<OutwardCheque> existing = outwardChequeService.getChequesByBatchId(outwardBatchId);

			if (existing != null) {
				outwardChequeList.addAll(existing);
			}

		} catch (Exception ignored) {

			outwardChequeList = new ArrayList<>();
		}
	}

	private void mergeScanAndOutwardCheques() {

		List<OutwardCheque> merged = new ArrayList<>();

		for (ScanCheque scanCheque : scanChequeList) {

			if (scanCheque == null) {
				continue;
			}

			OutwardCheque existing = findExistingOutwardCheque(scanCheque);

			if (existing != null) {

				addIfNotPresent(merged, existing);

			} else {

				merged.add(buildOutwardChequeFromScan(scanCheque));
			}
		}

		for (OutwardCheque cheque : outwardChequeList) {

			if (cheque == null) {
				continue;
			}

			addIfNotPresent(merged, cheque);
		}

		outwardChequeList = merged;
	}

	private void addIfNotPresent(List<OutwardCheque> list, OutwardCheque cheque) {

		if (cheque == null) {
			return;
		}

		String id = safeValue(cheque.getOutwardChequeId()).trim();

		String number = safeValue(cheque.getChequeNumber()).trim();

		String front = safeValue(cheque.getChequeImageFront()).trim();

		String back = safeValue(cheque.getChequeImageBack()).trim();

		for (OutwardCheque existing : list) {

			if (existing == null) {
				continue;
			}

			String existingId = safeValue(existing.getOutwardChequeId()).trim();

			String existingNumber = safeValue(existing.getChequeNumber()).trim();

			String existingFront = safeValue(existing.getChequeImageFront()).trim();

			String existingBack = safeValue(existing.getChequeImageBack()).trim();

			if (!id.isEmpty() && id.equalsIgnoreCase(existingId)) {
				return;
			}

			if (!number.isEmpty() && number.equalsIgnoreCase(existingNumber)) {
				return;
			}

			if (!front.isEmpty() && front.equalsIgnoreCase(existingFront)) {
				return;
			}

			if (!back.isEmpty() && back.equalsIgnoreCase(existingBack)) {
				return;
			}
		}

		list.add(cheque);
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

			String chequeNumber = safeValue(cheque.getChequeNumber()).trim();

			String chequeFront = safeValue(cheque.getChequeImageFront()).trim();

			String chequeBack = safeValue(cheque.getChequeImageBack()).trim();

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

		String scanStatus = normalizeChequeStatus(scanCheque.getChequeStatus());

		if (STATUS_MICR_REJECTED.equals(scanStatus)) {

			cheque.setChequeStatus(STATUS_MICR_REJECTED);

		} else if (STATUS_PENDING_MICR_REPAIR.equals(scanStatus)) {

			cheque.setChequeStatus(STATUS_PENDING_MICR_REPAIR);

		} else {

			cheque.setChequeStatus(STATUS_PENDING_DATA_ENTRY);
		}

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

			if (STATUS_ON_HOLD.equals(normalizeChequeStatus(cheque.getChequeStatus()))) {

				return i;
			}
		}

		return -1;
	}

	private int findNextPendingDataEntry(int start) {

		if (activeQueue == null) {
			return -1;
		}

		for (int i = Math.max(0, start); i < activeQueue.size(); i++) {

			OutwardCheque cheque = activeQueue.get(i);

			if (cheque == null) {
				continue;
			}

			String status = normalizeChequeStatus(cheque.getChequeStatus());

			if (STATUS_PENDING_DATA_ENTRY.equals(status) || STATUS_ON_HOLD.equals(status)) {

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

		if (!isBlank(cheque.getOutwardChequeId())) {

			populateChequeFields(cheque);

		} else if (scanCheque != null) {

			populateChequeFieldsFromScan(scanCheque);

		} else {

			populateChequeFields(cheque);
		}

		resetImageState();

		String imagePath = null;

		if (scanCheque != null) {
			imagePath = scanCheque.getChequeImageFront();
		}

		if (isBlank(imagePath)) {
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

		if (cheque == null) {
			return;
		}

		if (outwardChequeDataEntryTxtChequeNumber != null) {

			outwardChequeDataEntryTxtChequeNumber.setValue(safeValue(cheque.getChequeNumber()));
		}

		if (outwardChequeDataEntryTxtAmount != null) {

			BigDecimal amount = cheque.getChequeAmount();

			outwardChequeDataEntryTxtAmount.setValue(amount == null ? "" : amount.toPlainString());
		}

		if (outwardChequeDataEntryDtChequeDate != null) {

			outwardChequeDataEntryDtChequeDate.setValue(cheque.getChequeDate());
		}

		if (outwardChequeDataEntryTxtMicrCode != null) {

			outwardChequeDataEntryTxtMicrCode.setValue(safeValue(cheque.getMicrCode()));
		}

		if (outwardChequeDataEntryTxtPayeeAccount != null) {

			outwardChequeDataEntryTxtPayeeAccount.setValue(safeValue(cheque.getPayeeAccountNumber()));
		}

		if (outwardChequeDataEntryTxtPayeeName != null) {

			outwardChequeDataEntryTxtPayeeName.setValue(safeValue(cheque.getPayeeName()));
		}

		if (outwardChequeDataEntryTxtDraweeName != null) {

			outwardChequeDataEntryTxtDraweeName.setValue(safeValue(cheque.getDraweeName()));
		}
	}

	private void populateChequeFieldsFromScan(ScanCheque scanCheque) {

		if (scanCheque == null) {
			return;
		}

		if (outwardChequeDataEntryTxtChequeNumber != null) {

			outwardChequeDataEntryTxtChequeNumber.setValue(safeValue(scanCheque.getChequeNumber()));
		}

		if (outwardChequeDataEntryTxtAmount != null) {

			BigDecimal amount = scanCheque.getChequeAmount();

			outwardChequeDataEntryTxtAmount.setValue(amount == null ? "" : amount.toPlainString());
		}

		if (outwardChequeDataEntryDtChequeDate != null) {

			outwardChequeDataEntryDtChequeDate.setValue(scanCheque.getChequeDate());
		}

		if (outwardChequeDataEntryTxtMicrCode != null) {

			outwardChequeDataEntryTxtMicrCode.setValue(safeValue(scanCheque.getMicrCode()));
		}

		if (outwardChequeDataEntryTxtPayeeAccount != null) {

			outwardChequeDataEntryTxtPayeeAccount.setValue(safeValue(scanCheque.getPayeeAccountNumber()));
		}

		if (outwardChequeDataEntryTxtPayeeName != null) {

			outwardChequeDataEntryTxtPayeeName.setValue(safeValue(scanCheque.getPayeeName()));
		}

		if (outwardChequeDataEntryTxtDraweeName != null) {

			outwardChequeDataEntryTxtDraweeName.setValue(safeValue(scanCheque.getDraweeName()));
		}
	}

	private BigDecimal getAmountFromField() {

		if (outwardChequeDataEntryTxtAmount == null) {
			return null;
		}

		String value = safeValue(outwardChequeDataEntryTxtAmount.getValue()).trim();

		if (value.isEmpty()) {
			return null;
		}

		try {

			String cleaned = value.replace(",", "").trim();

			return new BigDecimal(cleaned);

		} catch (NumberFormatException exception) {

			return null;
		}
	}

	private void populateOutwardChequeFromFields(OutwardCheque cheque) {

		if (cheque == null) {
			return;
		}

		if (outwardChequeDataEntryTxtChequeNumber != null) {

			cheque.setChequeNumber(safeValue(outwardChequeDataEntryTxtChequeNumber.getValue()).trim());
		}

		if (outwardChequeDataEntryTxtPayeeAccount != null) {

			cheque.setPayeeAccountNumber(safeValue(outwardChequeDataEntryTxtPayeeAccount.getValue()).trim());
		}

		if (outwardChequeDataEntryTxtPayeeName != null) {

			cheque.setPayeeName(safeValue(outwardChequeDataEntryTxtPayeeName.getValue()).trim());
		}

		if (outwardChequeDataEntryTxtDraweeName != null) {

			cheque.setDraweeName(safeValue(outwardChequeDataEntryTxtDraweeName.getValue()).trim());
		}

		if (outwardChequeDataEntryTxtMicrCode != null) {

			cheque.setMicrCode(safeValue(outwardChequeDataEntryTxtMicrCode.getValue()).trim());
		}

		cheque.setChequeAmount(getAmountFromField());

		if (outwardChequeDataEntryDtChequeDate != null && outwardChequeDataEntryDtChequeDate.getValue() != null) {

			cheque.setChequeDate(new Date(outwardChequeDataEntryDtChequeDate.getValue().getTime()));
		}
	}

	private boolean validateFields() {

		String chequeNumber = outwardChequeDataEntryTxtChequeNumber == null ? ""
				: safeValue(outwardChequeDataEntryTxtChequeNumber.getValue()).trim();

		String amountText = outwardChequeDataEntryTxtAmount == null ? ""
				: safeValue(outwardChequeDataEntryTxtAmount.getValue()).trim();

		BigDecimal amount = getAmountFromField();

		String payeeAccount = outwardChequeDataEntryTxtPayeeAccount == null ? ""
				: safeValue(outwardChequeDataEntryTxtPayeeAccount.getValue()).trim();

		String payeeName = outwardChequeDataEntryTxtPayeeName == null ? ""
				: safeValue(outwardChequeDataEntryTxtPayeeName.getValue()).trim();

		String draweeName = outwardChequeDataEntryTxtDraweeName == null ? ""
				: safeValue(outwardChequeDataEntryTxtDraweeName.getValue()).trim();

		if (chequeNumber.isEmpty()) {

			showValidation("Cheque number is required.");

			return false;
		}

		if (amountText.isEmpty()) {

			showValidation("Cheque amount is required.");

			return false;
		}

		if (amount == null) {

			showValidation("Enter a valid cheque amount.");

			return false;
		}

		if (amount.compareTo(BigDecimal.ZERO) < 0) {

			showValidation("Cheque amount cannot be negative.");

			return false;
		}

		if (outwardChequeDataEntryDtChequeDate == null || outwardChequeDataEntryDtChequeDate.getValue() == null) {

			showValidation("Cheque date is required.");

			return false;
		}

		if (payeeAccount.isEmpty()) {

			showValidation("Payee account number is required.");

			return false;
		}

		if (payeeName.isEmpty()) {

			showValidation("Payee name is required.");

			return false;
		}

		if (draweeName.isEmpty()) {

			showValidation("Drawee name is required.");

			return false;
		}

		hideValidation();

		return true;
	}

	private boolean isEditableCheque(OutwardCheque cheque) {

		if (cheque == null) {
			return false;
		}

		String status = normalizeChequeStatus(cheque.getChequeStatus());

		return STATUS_PENDING_DATA_ENTRY.equals(status) || STATUS_ON_HOLD.equals(status);
	}

	private boolean isMicrRejectedCheque(OutwardCheque cheque) {

		return cheque != null && STATUS_MICR_REJECTED.equals(normalizeChequeStatus(cheque.getChequeStatus()));
	}

	private void setChequeFieldsEditable(boolean editable) {

		setEditable(outwardChequeDataEntryTxtChequeNumber, editable);

		setEditable(outwardChequeDataEntryTxtAmount, editable);

		setEditable(outwardChequeDataEntryDtChequeDate, editable);

		setEditable(outwardChequeDataEntryTxtPayeeAccount, editable);

		setEditable(outwardChequeDataEntryTxtPayeeName, editable);

		setEditable(outwardChequeDataEntryTxtDraweeName, editable);

		if (outwardChequeDataEntryTxtMicrCode != null) {

			outwardChequeDataEntryTxtMicrCode.setReadonly(true);
			outwardChequeDataEntryTxtMicrCode.setDisabled(true);
		}
	}

	private void setEditable(Textbox textbox, boolean editable) {

		if (textbox == null) {
			return;
		}

		textbox.setReadonly(!editable);
		textbox.setDisabled(!editable);
	}

	private void setEditable(Datebox datebox, boolean editable) {

		if (datebox == null) {
			return;
		}

		datebox.setReadonly(!editable);
		datebox.setDisabled(!editable);
	}

	private void disableChequeInteractionButtons() {

		disableButton(outwardChequeDataEntryBtnBack);
		disableButton(outwardChequeDataEntryBtnImageToggle);
		disableButton(outwardChequeDataEntryBtnZoomOut);
		disableButton(outwardChequeDataEntryBtnZoomIn);
		disableButton(outwardChequeDataEntryBtnRotate);
		disableButton(outwardChequeDataEntryBtnReset);
		disableButton(outwardChequeDataEntryBtnPrevious);
		disableButton(outwardChequeDataEntryBtnNext);
		disableButton(outwardChequeDataEntryBtnResetItem);
		disableButton(outwardChequeDataEntryBtnReject);
		disableButton(outwardChequeDataEntryBtnSubmit);
	}

	private void updateActionButtons() {

		OutwardCheque cheque = getCurrentCheque();

		if (cheque == null) {
			disableButton(outwardChequeDataEntryBtnResetItem);
			disableButton(outwardChequeDataEntryBtnReject);
			disableButton(outwardChequeDataEntryBtnApprove);
			return;
		}

		String status = normalizeChequeStatus(cheque.getChequeStatus());

		setButtonEnabled(outwardChequeDataEntryBtnBack, true);
		setButtonEnabled(outwardChequeDataEntryBtnImageToggle, true);
		setButtonEnabled(outwardChequeDataEntryBtnZoomOut, true);
		setButtonEnabled(outwardChequeDataEntryBtnZoomIn, true);
		setButtonEnabled(outwardChequeDataEntryBtnRotate, true);
		setButtonEnabled(outwardChequeDataEntryBtnReset, true);

		if (STATUS_MICR_REJECTED.equals(status)) {
			setChequeFieldsEditable(false);
			disableChequeInteractionButtons();
			if (outwardChequeDataEntryBtnApprove != null) {
				outwardChequeDataEntryBtnApprove.setLabel("✓ Approve Cheque");
				outwardChequeDataEntryBtnApprove.setDisabled(false);
			}
			return;
		}

		if (STATUS_PENDING_MICR_REPAIR.equals(status)) {
			setChequeFieldsEditable(false);
			disableChequeInteractionButtons();
			disableButton(outwardChequeDataEntryBtnApprove);
			return;
		}

		if (STATUS_REJECTION_REQUEST.equals(status) || STATUS_PENDING_VERIFICATION.equals(status)
				|| STATUS_PENDING_CHECKER_PROCESS.equals(status)) {
			setChequeFieldsEditable(false);
			disableButton(outwardChequeDataEntryBtnResetItem);
			disableButton(outwardChequeDataEntryBtnReject);
			disableButton(outwardChequeDataEntryBtnApprove);
			return;
		}

		boolean editable = isEditableCheque(cheque);

		if (outwardChequeDataEntryBtnApprove != null) {
			outwardChequeDataEntryBtnApprove.setLabel("✓ Approve Cheque");
			outwardChequeDataEntryBtnApprove.setDisabled(!editable);
		}

		if (outwardChequeDataEntryBtnResetItem != null) {
			outwardChequeDataEntryBtnResetItem.setDisabled(!editable || !hasUnsavedChanges);
		}

		if (outwardChequeDataEntryBtnReject != null) {
			outwardChequeDataEntryBtnReject.setDisabled(!editable);
		}
	}

	private void saveCurrentCheque() {

		OutwardCheque cheque = getCurrentCheque();

		if (cheque == null) {
			return;
		}

		String status = normalizeChequeStatus(cheque.getChequeStatus());

		if (STATUS_MICR_REJECTED.equals(status)) {

			saveMicrRejectedCheque(cheque);

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

			hasUnsavedChanges = false;

			refreshBatch();

			showStatus("Cheque approved successfully.");

			rebuildActiveQueue();

			moveToNextPendingCheque();

			updateSubmitButton();

		} catch (Exception exception) {

			showStatus("Unable to save cheque: " + safeExceptionMessage(exception));
		}
	}

	private void saveMicrRejectedCheque(OutwardCheque cheque) {

		try {

			cheque.setOutwardBatchId(outwardBatchId);

			String batchForSave = !isBlank(scannedBatchId) ? scannedBatchId : outwardBatchId;

			OutwardCheque saved = outwardChequeService.saveMakerCheque(batchForSave, cheque);

			if (saved == null) {

				throw new IllegalStateException("Unable to save MICR rejected cheque.");
			}

			String chequeId = safeValue(saved.getOutwardChequeId()).trim();

			if (!chequeId.isEmpty()) {

				boolean statusUpdated = outwardChequeService.updateChequeStatus(chequeId, STATUS_REJECTION_REQUEST);

				if (!statusUpdated) {

					throw new IllegalStateException("Unable to update MICR rejected cheque status.");
				}
			}

			saved.setChequeStatus(STATUS_REJECTION_REQUEST);

			syncChequeToFullList(saved);

			hasUnsavedChanges = false;

			refreshBatch();

			showStatus("MICR rejected cheque saved successfully. " + "Status: Rejection Request.");

			rebuildActiveQueue();

			moveToNextPendingCheque();

			updateSubmitButton();

		} catch (Exception exception) {

			showStatus("Unable to save MICR rejected cheque: " + safeExceptionMessage(exception));
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

		if (countCompletedMakerCheques() == getBatchTotal() && getBatchTotal() > 0) {

			showAllCompleted();
		}
	}

	private void resetCurrentItem() {

		OutwardCheque cheque = getCurrentCheque();

		if (cheque == null || !isEditableCheque(cheque)) {

			return;
		}

		populateChequeFields(cheque);

		hasUnsavedChanges = false;

		setChequeFieldsEditable(true);

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

		OutwardCheque cheque = getCurrentCheque();
		if (cheque == null || !isEditableCheque(cheque)) {
			return;
		}

		int previous = findPreviousEditableCheque(currentChequeIndex - 1);
		if (previous < 0) {
			return;
		}

		currentChequeIndex = previous;
		loadCurrentCheque();
	}

	private void nextCheque() {

		if (hasUnsavedChanges) {
			showStatus("Save or reset the current cheque before moving.");
			return;
		}

		OutwardCheque cheque = getCurrentCheque();
		if (cheque == null || !isEditableCheque(cheque)) {
			return;
		}

		int next = findNextEditableCheque(currentChequeIndex + 1);
		if (next < 0) {
			return;
		}

		currentChequeIndex = next;
		loadCurrentCheque();
	}

	private void updateNavigationButtons() {

		if (outwardChequeDataEntryBtnPrevious == null || outwardChequeDataEntryBtnNext == null) {
			return;
		}

		OutwardCheque cheque = getCurrentCheque();

		if (activeQueue == null || activeQueue.isEmpty() || currentChequeIndex < 0 || cheque == null) {
			outwardChequeDataEntryBtnPrevious.setDisabled(true);
			outwardChequeDataEntryBtnNext.setDisabled(true);
			return;
		}

		String status = normalizeChequeStatus(cheque.getChequeStatus());

		if (STATUS_MICR_REJECTED.equals(status) || STATUS_PENDING_MICR_REPAIR.equals(status)
				|| STATUS_REJECTION_REQUEST.equals(status) || STATUS_PENDING_VERIFICATION.equals(status)
				|| STATUS_PENDING_CHECKER_PROCESS.equals(status)) {
			outwardChequeDataEntryBtnPrevious.setDisabled(true);
			outwardChequeDataEntryBtnNext.setDisabled(true);
			return;
		}

		int previous = findPreviousEditableCheque(currentChequeIndex - 1);
		int next = findNextEditableCheque(currentChequeIndex + 1);

		outwardChequeDataEntryBtnPrevious.setDisabled(previous < 0 || hasUnsavedChanges);
		outwardChequeDataEntryBtnNext.setDisabled(next < 0 || hasUnsavedChanges);
	}

	private int findPreviousEditableCheque(int start) {

		if (activeQueue == null) {
			return -1;
		}

		for (int i = Math.min(start, activeQueue.size() - 1); i >= 0; i--) {
			OutwardCheque cheque = activeQueue.get(i);
			if (cheque != null && isEditableCheque(cheque)) {
				return i;
			}
		}

		return -1;
	}

	private int findNextEditableCheque(int start) {

		if (activeQueue == null) {
			return -1;
		}

		for (int i = Math.max(0, start); i < activeQueue.size(); i++) {
			OutwardCheque cheque = activeQueue.get(i);
			if (cheque != null && isEditableCheque(cheque)) {
				return i;
			}
		}

		return -1;
	}

	private void updateSummary() {

		int total = getBatchTotal();

		int position = currentChequeIndex + 1;

		if (outwardChequeDataEntryLblRecordImage != null) {

			if (position > 0 && !activeQueue.isEmpty()) {

				outwardChequeDataEntryLblRecordImage.setValue("Cheque " + position + " of " + total);

			} else {

				outwardChequeDataEntryLblRecordImage.setValue("Cheque 0 of " + total);
			}
		}

		if (outwardChequeDataEntryLblRecord != null) {

			int completed = countCompletedMakerCheques();

			outwardChequeDataEntryLblRecord.setValue(completed + " / " + total + " Completed");
		}
	}

	private void updateBatchSummary() {

		String batch = !isBlank(outwardBatchId) ? outwardBatchId : scannedBatchId;

		if (outwardChequeDataEntryLblBatchId != null) {

			outwardChequeDataEntryLblBatchId.setValue(isBlank(batch) ? "-" : batch);
		}

		int total = getBatchTotal();

		if (outwardChequeDataEntryLblTotal != null) {

			outwardChequeDataEntryLblTotal.setValue(String.valueOf(total));
		}

		int completed = countCompletedMakerCheques();

		int percent = total <= 0 ? 0 : (completed * 100) / total;

		if (outwardChequeDataEntryProgress != null) {

			outwardChequeDataEntryProgress.setValue(percent);
		}

		if (outwardChequeDataEntryLblRecord != null) {

			outwardChequeDataEntryLblRecord.setValue(completed + " / " + total + " Completed");
		}
	}

	private int getBatchTotal() {

		if (outwardBatch != null && outwardBatch.getActualChequeCount() > 0) {

			return outwardBatch.getActualChequeCount();
		}

		if (!isBlank(outwardBatchId)) {

			try {

				OutwardBatch latest = outwardBatchService.getBatchById(outwardBatchId);

				if (latest != null && latest.getActualChequeCount() > 0) {

					outwardBatch = latest;

					return latest.getActualChequeCount();
				}

			} catch (Exception ignored) {
			}
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

		return countStatus(outwardChequeList, STATUS_PENDING_DATA_ENTRY) > 0;
	}

	private boolean hasPendingMicrRepair() {

		return countStatus(outwardChequeList, STATUS_PENDING_MICR_REPAIR) > 0;
	}

	private int countMicrPending() {

		return countStatus(outwardChequeList, STATUS_PENDING_MICR_REPAIR);
	}

	private int countMicrRejected() {

		return countStatus(outwardChequeList, STATUS_MICR_REJECTED);
	}

	private void updateSubmitButton() {

		if (outwardChequeDataEntryBtnSubmit == null) {
			return;
		}

		if (outwardChequeList == null || outwardChequeList.isEmpty()) {

			outwardChequeDataEntryBtnSubmit.setDisabled(true);

			return;
		}

		if (hasUnsavedChanges) {

			outwardChequeDataEntryBtnSubmit.setDisabled(true);

			return;
		}

		int pendingDataEntry = countStatus(outwardChequeList, STATUS_PENDING_DATA_ENTRY);

		if (pendingDataEntry > 0) {

			outwardChequeDataEntryBtnSubmit.setDisabled(true);

			return;
		}

		int pendingMicr = countStatus(outwardChequeList, STATUS_PENDING_MICR_REPAIR);

		if (pendingMicr > 0) {

			outwardChequeDataEntryBtnSubmit.setDisabled(true);

			return;
		}

		int total = getBatchTotal();

		int completed = countCompletedMakerCheques();

		boolean ready = total > 0 && completed == total;

		outwardChequeDataEntryBtnSubmit.setDisabled(!ready);
	}

	private void updateCurrentChequeStatus(OutwardCheque cheque) {

		hideAllPanels();

		if (cheque == null) {
			return;
		}

		String status = normalizeChequeStatus(cheque.getChequeStatus());

		if (outwardChequeDataEntryLblChequeStatus != null) {

			outwardChequeDataEntryLblChequeStatus.setValue(displayStatus(status));
		}

		if (outwardChequeDataEntryLblFooterStatus != null) {

			outwardChequeDataEntryLblFooterStatus.setValue(displayStatus(status));
		}

		if (STATUS_PENDING_MICR_REPAIR.equals(status)) {

			showPanel(outwardChequeDataEntryMicrPendingPanel);

			setLabel(outwardChequeDataEntryLblMicrPendingMessage,
					"This cheque is awaiting MICR repair and cannot be edited.");

			setChequeFieldsEditable(false);

			disableButton(outwardChequeDataEntryBtnApprove);

			disableButton(outwardChequeDataEntryBtnReject);

			return;
		}

		if (STATUS_MICR_REJECTED.equals(status)) {

			showPanel(outwardChequeDataEntryMicrRejectedPanel);

			setLabel(outwardChequeDataEntryLblMicrRejectedMessage, "This cheque was rejected during MICR processing. "
					+ "The cheque is read-only. " + "Save it to continue with Rejection Request status.");

			setLabel(outwardChequeDataEntryLblMicrReason, "MICR Status: MICR Rejected");

			if (outwardChequeDataEntryLblMicrReason != null) {

				outwardChequeDataEntryLblMicrReason.setVisible(true);
			}

			setChequeFieldsEditable(false);

			if (outwardChequeDataEntryBtnApprove != null) {

				outwardChequeDataEntryBtnApprove.setLabel("Save Cheque");

				outwardChequeDataEntryBtnApprove.setDisabled(false);
			}

			disableButton(outwardChequeDataEntryBtnReject);

			return;
		}

		if (STATUS_REJECTION_REQUEST.equals(status)) {

			showPanel(outwardChequeDataEntryMakerRejectionPanel);

			loadRejectionStatus(cheque);

			setChequeFieldsEditable(false);

			disableButton(outwardChequeDataEntryBtnApprove);

			disableButton(outwardChequeDataEntryBtnReject);

			return;
		}

		if (STATUS_ON_HOLD.equals(status)) {

			showPanel(outwardChequeDataEntryCheckerSendBackPanel);

			setLabel(outwardChequeDataEntryLblCheckerSendBackMessage,
					"This cheque was returned by Checker for maker correction.");

			setChequeFieldsEditable(true);

			return;
		}

		if (STATUS_PENDING_VERIFICATION.equals(status) || STATUS_PENDING_CHECKER_PROCESS.equals(status)) {

			setChequeFieldsEditable(false);

			disableButton(outwardChequeDataEntryBtnApprove);

			disableButton(outwardChequeDataEntryBtnReject);

			return;
		}

		if (STATUS_PENDING_DATA_ENTRY.equals(status)) {

			setChequeFieldsEditable(true);
		}
	}

	private void loadRejectionStatus(OutwardCheque cheque) {

		if (cheque == null || isBlank(cheque.getOutwardChequeId())) {

			setLabel(outwardChequeDataEntryLblRejectionReason, "Rejection request created.");

			setLabel(outwardChequeDataEntryLblRejectionRemarks, "");

			return;
		}

		try {

			OutwardChequeRequest request = outwardChequeService
					.getRejectionRequestByChequeId(cheque.getOutwardChequeId());

			if (request == null) {

				setLabel(outwardChequeDataEntryLblRejectionReason, "Rejection request created.");

				setLabel(outwardChequeDataEntryLblRejectionRemarks, "");

				return;
			}

			String reason = safeValue(request.getReason()).trim();

			String remarks = safeValue(request.getRemarks()).trim();

			setLabel(outwardChequeDataEntryLblRejectionReason, reason.isEmpty() ? "Reason: -" : "Reason: " + reason);

			setLabel(outwardChequeDataEntryLblRejectionRemarks,
					remarks.isEmpty() ? "Remarks: -" : "Remarks: " + remarks);

		} catch (Exception exception) {

			setLabel(outwardChequeDataEntryLblRejectionReason, "Rejection request created.");

			setLabel(outwardChequeDataEntryLblRejectionRemarks, "");
		}
	}

	private void updatePostDatedIndicator(OutwardCheque cheque) {

		if (outwardChequeDataEntryLblPostDated == null) {
			return;
		}

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

		hidePanel(outwardChequeDataEntryMakerRejectionPanel);

		hidePanel(outwardChequeDataEntryCheckerSendBackPanel);

		hidePanel(outwardChequeDataEntryMicrRejectedPanel);

		hidePanel(outwardChequeDataEntryMicrPendingPanel);

		if (outwardChequeDataEntryLblPostDated != null) {

			outwardChequeDataEntryLblPostDated.setVisible(false);
		}

		if (outwardChequeDataEntryLblMicrReason != null) {

			outwardChequeDataEntryLblMicrReason.setVisible(false);
		}
	}

	private void openRejectWindow() {

		OutwardCheque cheque = getCurrentCheque();

		if (cheque == null || !isEditableCheque(cheque)) {

			showStatus("This cheque cannot be submitted for rejection.");

			return;
		}

		if (outwardChequeDataEntryRejectModal == null || outwardChequeDataEntryCmbRejectReason == null
				|| outwardChequeDataEntryTxtRejectRemarks == null) {

			showStatus("Rejection window is not configured correctly.");

			return;
		}

		outwardChequeDataEntryCmbRejectReason.setSelectedItem(null);

		outwardChequeDataEntryTxtRejectRemarks.setValue("");

		outwardChequeDataEntryRejectModal.setVisible(true);

		outwardChequeDataEntryRejectModal.doModal();
	}

	private void closeRejectWindow() {

		hideModal(outwardChequeDataEntryRejectModal);
	}

	private void requestReject() {

		OutwardCheque cheque = getCurrentCheque();

		if (cheque == null || !isEditableCheque(cheque)) {

			closeRejectWindow();

			showStatus("This cheque cannot be submitted for rejection.");

			return;
		}

		if (outwardChequeDataEntryCmbRejectReason == null) {
			return;
		}

		Comboitem selected = outwardChequeDataEntryCmbRejectReason.getSelectedItem();

		if (selected == null) {

			showStatus("Please select a rejection reason.");

			return;
		}

		Object value = selected.getValue();

		if (!(value instanceof RejectedReason)) {

			showStatus("Please select a valid rejection reason.");

			return;
		}

		RejectedReason reasonObject = (RejectedReason) value;

		String reasonId = safeValue(reasonObject.getRejectedReasonId()).trim();

		if (reasonId.isEmpty()) {

			reasonId = safeValue(reasonObject.getRejectedReasonCode()).trim();
		}

		String reason = safeValue(reasonObject.getRejectedReasonName()).trim();

		if (reason.isEmpty()) {

			reason = safeValue(reasonObject.getRejectedReasonCode()).trim();
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

		if (!validateFields()) {
			return;
		}

		try {

			populateOutwardChequeFromFields(cheque);

			cheque.setOutwardBatchId(outwardBatchId);

			String batchForSave = !isBlank(scannedBatchId) ? scannedBatchId : outwardBatchId;

			boolean saved = outwardChequeService.saveMakerRejectionRequest(batchForSave, cheque, reasonId, reason,
					remarks);

			if (!saved) {

				closeRejectWindow();

				showStatus("Unable to create rejection request.");

				return;
			}

			cheque.setChequeStatus(STATUS_REJECTION_REQUEST);

			syncChequeToFullList(cheque);

			hasUnsavedChanges = false;

			closeRejectWindow();

			refreshBatch();

			rebuildActiveQueue();

			showStatus("Rejection request submitted successfully.");

			moveToNextPendingCheque();

			updateSubmitButton();

		} catch (Exception exception) {

			closeRejectWindow();

			showStatus("Unable to create rejection request: " + safeExceptionMessage(exception));
		}
	}

	private void loadRejectedReasons() {

		if (outwardChequeDataEntryCmbRejectReason == null) {
			return;
		}

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

				String description = safeValue(reason.getRejectedReasonDescription()).trim();

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

		if (hasUnsavedChanges) {

			showStatus("Save or reset the current cheque before submission.");

			return;
		}

		int pendingDataEntry = countStatus(outwardChequeList, STATUS_PENDING_DATA_ENTRY);

		if (pendingDataEntry > 0) {

			showStatus(
					pendingDataEntry + " cheque" + (pendingDataEntry == 1 ? "" : "s") + " still require Data Entry.");

			return;
		}

		int pendingMicr = countStatus(outwardChequeList, STATUS_PENDING_MICR_REPAIR);

		if (pendingMicr > 0) {

			showStatus(pendingMicr + " cheque" + (pendingMicr == 1 ? "" : "s") + " required MICR repair.");

			return;
		}

		int total = getBatchTotal();

		int completed = countCompletedMakerCheques();

		if (completed != total) {

			showStatus("All cheques must be completed before submission.");

			return;
		}

		showSubmitConfirmation(total, completed);
	}

	private void showSubmitConfirmation(int total, int completed) {

		if (outwardChequeDataEntrySubmitConfirmModal == null) {

			completeBatchSubmission();

			return;
		}

		setLabel(outwardChequeDataEntryLblConfirmBatchId, isBlank(outwardBatchId) ? "-" : outwardBatchId);

		setLabel(outwardChequeDataEntryLblConfirmTotal, completed + " / " + total);

		setLabel(outwardChequeDataEntryLblConfirmCompleted, String.valueOf(completed));

		outwardChequeDataEntrySubmitConfirmModal.setVisible(true);

		outwardChequeDataEntrySubmitConfirmModal.doModal();
	}

	private void completeBatchSubmission() {

		try {

			int total = getBatchTotal();

			int completed = countCompletedMakerCheques();

			if (total <= 0) {

				showStatus("No cheques are available for submission.");

				return;
			}

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

			batchSubmitted = true;

			if (micrRepairRefreshTimer != null) {

				micrRepairRefreshTimer.stop();
			}

			boolean notificationSent = false;

			try {

				notificationSent = notificationService.sendNotification(NOTIFICATION_ROLE_OUTWARD_CHECKER, null,
						"Batch " + outwardBatchId + " is ready for Checker processing.");

			} catch (Exception ignored) {
			}

			showSubmissionSuccess(notificationSent, total);

		} catch (Exception exception) {

			showStatus("Unable to submit batch to Checker: " + safeExceptionMessage(exception));
		}
	}

	private void showSubmissionSuccess(boolean notificationSent, int total) {

		String details = "Total Cheques: " + total + " / " + total + " | Status: "
				+ displayStatus(STATUS_PENDING_CHECKER_PROCESS);

		if (!notificationSent) {

			details += " | Checker notification could not be sent.";
		}

		setLabel(outwardChequeDataEntryLblSubmitSuccess, "Batch " + outwardBatchId + " submitted successfully.");

		setLabel(outwardChequeDataEntryLblSubmitSuccessDetails, details);

		if (outwardChequeDataEntrySubmitSuccessModal != null) {

			outwardChequeDataEntrySubmitSuccessModal.setVisible(true);

			outwardChequeDataEntrySubmitSuccessModal.doModal();

		} else {

			goBackToDashboard();
		}
	}

	private void showWaitingForMicrRepair() {

		currentChequeIndex = -1;

		clearChequeFields();

		setChequeFieldsEditable(false);

		disableButton(outwardChequeDataEntryBtnResetItem);

		disableButton(outwardChequeDataEntryBtnReject);

		disableButton(outwardChequeDataEntryBtnApprove);

		disableButton(outwardChequeDataEntryBtnPrevious);

		disableButton(outwardChequeDataEntryBtnNext);

		if (outwardChequeDataEntryLblRecordImage != null) {

			outwardChequeDataEntryLblRecordImage.setValue("Waiting for MICR Repair");
		}

		if (outwardChequeDataEntryLblFooterStatus != null) {

			outwardChequeDataEntryLblFooterStatus.setValue("Pending MICR Repair");
		}

		if (outwardChequeDataEntryLblImageEmpty != null) {

			int pending = countMicrPending();

			outwardChequeDataEntryLblImageEmpty
					.setValue(pending + " cheque" + (pending == 1 ? "" : "s") + " awaiting MICR processing.");

			outwardChequeDataEntryLblImageEmpty.setVisible(true);
		}

		if (outwardChequeDataEntryChequeImage != null) {

			outwardChequeDataEntryChequeImage.setVisible(false);
		}

		hideAllPanels();

		showPanel(outwardChequeDataEntryMicrPendingPanel);

		setLabel(outwardChequeDataEntryLblMicrPendingMessage, countMicrPending() + " cheque"
				+ (countMicrPending() == 1 ? "" : "s") + " are awaiting MICR processing.");

		updateSubmitButton();
	}

	private void showAllCompleted() {

		clearChequeFields();

		setChequeFieldsEditable(false);

		disableButton(outwardChequeDataEntryBtnResetItem);

		disableButton(outwardChequeDataEntryBtnReject);

		disableButton(outwardChequeDataEntryBtnApprove);

		disableButton(outwardChequeDataEntryBtnPrevious);

		disableButton(outwardChequeDataEntryBtnNext);

		if (outwardChequeDataEntryLblRecordImage != null) {

			outwardChequeDataEntryLblRecordImage.setValue("All Cheques Completed");
		}

		if (outwardChequeDataEntryLblFooterStatus != null) {

			outwardChequeDataEntryLblFooterStatus
					.setValue("All maker activities are completed. " + "Batch is ready for Checker.");
		}

		updateBatchSummary();
		updateSubmitButton();
	}

	private void startMicrRepairRefreshTimer() {

		if (outwardChequeDataEntryWin == null || micrRepairRefreshTimer != null) {

			return;
		}

		micrRepairRefreshTimer = new Timer();

		micrRepairRefreshTimer.setDelay(3000);

		micrRepairRefreshTimer.setRepeats(true);

		micrRepairRefreshTimer.addEventListener(Events.ON_TIMER, event -> refreshAfterMicrRepair());

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

			List<OutwardCheque> latest = outwardChequeService.getChequesByBatchId(outwardBatchId);

			if (latest == null) {
				return;
			}

			int oldPending = countStatus(outwardChequeList, STATUS_PENDING_DATA_ENTRY);

			int oldMicr = countStatus(outwardChequeList, STATUS_PENDING_MICR_REPAIR);

			String currentId = "";
			String currentNumber = "";

			OutwardCheque current = getCurrentCheque();

			if (current != null) {

				currentId = safeValue(current.getOutwardChequeId()).trim();

				currentNumber = safeValue(current.getChequeNumber()).trim();
			}

			outwardChequeList = new ArrayList<>(latest);

			mergeScanAndOutwardCheques();

			rebuildActiveQueue();

			updateBatchSummary();

			int newPending = countStatus(outwardChequeList, STATUS_PENDING_DATA_ENTRY);

			int newMicr = countStatus(outwardChequeList, STATUS_PENDING_MICR_REPAIR);

			int keep = findQueueIndex(currentId, currentNumber);

			if (keep >= 0) {

				currentChequeIndex = keep;

				loadCurrentCheque();

			} else {

				int first = findFirstPendingDataEntry();

				if (first >= 0) {

					currentChequeIndex = first;

					loadCurrentCheque();

				} else if (newMicr > 0) {

					showWaitingForMicrRepair();

				} else {

					showAllCompleted();
				}
			}

			if (newPending > oldPending || newMicr < oldMicr) {

				showStatus("MICR repair completed. " + "The repaired cheque is now available for Data Entry.");
			}

			updateSubmitButton();

		} catch (Exception exception) {

			showStatus("Unable to refresh MICR status: " + safeExceptionMessage(exception));
		}
	}

	private int countStatus(List<OutwardCheque> list, String requiredStatus) {

		if (list == null) {
			return 0;
		}

		int count = 0;

		for (OutwardCheque cheque : list) {

			if (cheque == null) {
				continue;
			}

			if (requiredStatus.equals(normalizeChequeStatus(cheque.getChequeStatus()))) {

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

	private void refreshBatch() {

		if (isBlank(outwardBatchId)) {
			return;
		}

		try {

			outwardBatch = outwardBatchService.getBatchById(outwardBatchId);

		} catch (Exception ignored) {
		}

		updateBatchSummary();
	}

	private void toggleImage() {

		OutwardCheque cheque = getCurrentCheque();

		if (cheque == null || outwardChequeDataEntryChequeImage == null) {

			return;
		}

		String imagePath;

		if (showingFrontImage) {

			imagePath = cheque.getChequeImageBack();

			showingFrontImage = false;

			if (outwardChequeDataEntryBtnImageToggle != null) {

				outwardChequeDataEntryBtnImageToggle.setLabel("Front View");
			}

		} else {

			imagePath = cheque.getChequeImageFront();

			showingFrontImage = true;

			if (outwardChequeDataEntryBtnImageToggle != null) {

				outwardChequeDataEntryBtnImageToggle.setLabel("Back View");
			}
		}

		loadImage(imagePath);
	}

	private void loadImage(String imagePath) {

		if (outwardChequeDataEntryChequeImage == null) {
			return;
		}

		if (isBlank(imagePath)) {

			outwardChequeDataEntryChequeImage.setVisible(false);

			if (outwardChequeDataEntryLblImageEmpty != null) {

				outwardChequeDataEntryLblImageEmpty.setVisible(true);

				outwardChequeDataEntryLblImageEmpty.setValue("Cheque image unavailable");
			}

			return;
		}

		try {

			String path = imagePath.trim();

			if (!path.startsWith("/") && !path.startsWith("http://") && !path.startsWith("https://")) {

				path = "/" + path;
			}

			outwardChequeDataEntryChequeImage.setSrc(path);

			outwardChequeDataEntryChequeImage.setVisible(true);

			if (outwardChequeDataEntryLblImageEmpty != null) {

				outwardChequeDataEntryLblImageEmpty.setVisible(false);
			}

			if (outwardChequeDataEntryLblImageMessage != null) {

				outwardChequeDataEntryLblImageMessage.setVisible(false);
			}

			resetImage();

		} catch (Exception exception) {

			outwardChequeDataEntryChequeImage.setVisible(false);

			if (outwardChequeDataEntryLblImageEmpty != null) {

				outwardChequeDataEntryLblImageEmpty.setVisible(true);

				outwardChequeDataEntryLblImageEmpty.setValue("Cheque image unavailable");
			}
		}
	}

	private void resetImageState() {

		showingFrontImage = true;
		imageZoom = 1.0;
		imageRotation = 0;

		if (outwardChequeDataEntryBtnImageToggle != null) {

			outwardChequeDataEntryBtnImageToggle.setLabel("Back View");
		}

		if (outwardChequeDataEntryChequeImage != null) {

			outwardChequeDataEntryChequeImage.setStyle("transform:scale(1) rotate(0deg);");
		}
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

		if (outwardChequeDataEntryChequeImage == null) {
			return;
		}

		outwardChequeDataEntryChequeImage
				.setStyle("transform:scale(" + imageZoom + ") rotate(" + imageRotation + "deg);");
	}

	private void clearChequeFields() {

		if (outwardChequeDataEntryTxtChequeNumber != null) {

			outwardChequeDataEntryTxtChequeNumber.setValue("");
		}

		if (outwardChequeDataEntryTxtAmount != null) {

			outwardChequeDataEntryTxtAmount.setValue("");
		}

		if (outwardChequeDataEntryDtChequeDate != null) {

			outwardChequeDataEntryDtChequeDate.setValue(null);
		}

		if (outwardChequeDataEntryTxtMicrCode != null) {

			outwardChequeDataEntryTxtMicrCode.setValue("");
		}

		if (outwardChequeDataEntryTxtPayeeAccount != null) {

			outwardChequeDataEntryTxtPayeeAccount.setValue("");
		}

		if (outwardChequeDataEntryTxtPayeeName != null) {

			outwardChequeDataEntryTxtPayeeName.setValue("");
		}

		if (outwardChequeDataEntryTxtDraweeName != null) {

			outwardChequeDataEntryTxtDraweeName.setValue("");
		}
	}

	private void clearPage() {

		clearChequeFields();

		setChequeFieldsEditable(false);

		setLabel(outwardChequeDataEntryLblBatchId, isBlank(outwardBatchId) ? "-" : outwardBatchId);

		setLabel(outwardChequeDataEntryLblTotal, "0");

		setLabel(outwardChequeDataEntryLblFooterStatus, "-");

		setLabel(outwardChequeDataEntryLblRecordImage, "Cheque 0 of 0");

		disableButton(outwardChequeDataEntryBtnResetItem);

		disableButton(outwardChequeDataEntryBtnReject);

		disableButton(outwardChequeDataEntryBtnApprove);

		disableButton(outwardChequeDataEntryBtnSubmit);

		disableButton(outwardChequeDataEntryBtnPrevious);

		disableButton(outwardChequeDataEntryBtnNext);

		if (outwardChequeDataEntryChequeImage != null) {

			outwardChequeDataEntryChequeImage.setVisible(false);
		}

		if (outwardChequeDataEntryLblImageEmpty != null) {

			outwardChequeDataEntryLblImageEmpty.setVisible(true);
		}
	}

	private void goBackToDashboard() {

		if (hasUnsavedChanges) {

			showStatus("Save or reset the current cheque before leaving.");

			return;
		}

		if (micrRepairRefreshTimer != null) {

			micrRepairRefreshTimer.stop();
		}

		Executions.getCurrent().getSession().removeAttribute("OUTWARD_DATA_ENTRY_BATCH_ID");

		Executions.getCurrent().getSession().removeAttribute("OUTWARD_DATA_ENTRY_SCANNED_BATCH_ID");

		Include mainContentArea = findMainContentArea();

		if (mainContentArea == null) {

			Executions.sendRedirect(DASHBOARD_ZUL);

			return;
		}

		mainContentArea.clearDynamicProperties();

		mainContentArea.setSrc(DASHBOARD_ZUL);
	}

	private Include findMainContentArea() {

		Component current = outwardChequeDataEntryWin;

		while (current != null) {

			Component fellow = current.getFellowIfAny("mainContentArea");

			if (fellow instanceof Include) {

				return (Include) fellow;
			}

			current = current.getParent();
		}

		Component root = outwardChequeDataEntryWin;

		while (root != null && root.getParent() != null) {

			root = root.getParent();
		}

		if (root != null) {

			Component fellow = root.getFellowIfAny("mainContentArea");

			if (fellow instanceof Include) {

				return (Include) fellow;
			}
		}

		return null;
	}

	private void showStatus(String message) {

		if (outwardChequeDataEntryLblFooterStatus == null || isBlank(message)) {

			return;
		}

		outwardChequeDataEntryLblFooterStatus.setValue(message);
	}

	private void showValidation(String message) {

		if (outwardChequeDataEntryLblValidationStatus == null) {

			showStatus(message);

			return;
		}

		outwardChequeDataEntryLblValidationStatus.setValue(message);

		outwardChequeDataEntryLblValidationStatus.setVisible(true);

		showStatus(message);
	}

	private void hideValidation() {

		if (outwardChequeDataEntryLblValidationStatus != null) {

			outwardChequeDataEntryLblValidationStatus.setVisible(false);
		}
	}

	private void showPanel(Component component) {

		if (component != null) {

			component.setVisible(true);
		}
	}

	private void hidePanel(Component component) {

		if (component != null) {

			component.setVisible(false);
		}
	}

	private void setLabel(Label label, String value) {

		if (label != null) {

			label.setValue(value == null ? "" : value);
		}
	}

	private void setButtonEnabled(Button button, boolean enabled) {

		if (button != null) {

			button.setDisabled(!enabled);
		}
	}

	private void disableButton(Button button) {

		if (button != null) {

			button.setDisabled(true);
		}
	}

	private String normalizeChequeStatus(String status) {

		if (status == null || status.trim().isEmpty()) {

			return "";
		}

		return status.trim().replace("-", "_").replace(" ", "_").toUpperCase(Locale.ENGLISH);
	}

	private String displayStatus(String status) {

		String normalized = normalizeChequeStatus(status);

		if (normalized.isEmpty()) {

			return "-";
		}

		String[] words = normalized.toLowerCase(Locale.ENGLISH).split("_");

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