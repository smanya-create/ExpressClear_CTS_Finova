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
import org.zkoss.zul.Include;
import org.zkoss.zul.Label;
import org.zkoss.zul.Progressmeter;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Timer;
import org.zkoss.zul.Window;
import org.zkoss.zul.impl.XulElement;

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
	private static final String STATUS_REJECTION_REJECT = "REJECTION_REJECT";
	private static final String STATUS_PENDING_CHECKER_PROCESS = "PENDING_CHECKER_PROCESS";
	private static final String STATUS_ON_HOLD = "ON_HOLD";

	private static final String NOTIFICATION_ROLE_OUTWARD_CHECKER = "OUTWARD_CHECKER";
	private static final String DATA_ENTRY_ZUL = "/outward/maker/data-entry.zul";
	private static final String ERROR_CLASS = "outward-input-error";

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
	private Button outwardChequeDataEntryBtnCloseMicrPending;
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
	private Window outwardChequeDataEntryMicrPendingModal;
	private Window outwardChequeDataEntrySubmitSuccessModal;

	private Label outwardChequeDataEntryLblConfirmBatchId;
	private Label outwardChequeDataEntryLblConfirmTotal;
	private Label outwardChequeDataEntryLblConfirmCompleted;
	private Label outwardChequeDataEntryLblMicrPendingSubmitMessage;
	private Label outwardChequeDataEntryLblMicrPendingBatchId;
	private Label outwardChequeDataEntryLblMicrCompletedCount;
	private Label outwardChequeDataEntryLblMicrPendingCount;

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
			outwardChequeDataEntryBtnCancelReject = getButton(outwardChequeDataEntryRejectModal,
					"outwardChequeDataEntryBtnCancelReject");
			outwardChequeDataEntryBtnConfirmReject = getButton(outwardChequeDataEntryRejectModal,
					"outwardChequeDataEntryBtnConfirmReject");
		}

		if (outwardChequeDataEntrySubmitConfirmModal != null) {
			outwardChequeDataEntryLblConfirmBatchId = getLabel(outwardChequeDataEntrySubmitConfirmModal,
					"outwardChequeDataEntryLblConfirmBatchId");
			outwardChequeDataEntryLblConfirmTotal = getLabel(outwardChequeDataEntrySubmitConfirmModal,
					"outwardChequeDataEntryLblConfirmTotal");
			outwardChequeDataEntryLblConfirmCompleted = getLabel(outwardChequeDataEntrySubmitConfirmModal,
					"outwardChequeDataEntryLblConfirmCompleted");
			outwardChequeDataEntryBtnCancelSubmit = getButton(outwardChequeDataEntrySubmitConfirmModal,
					"outwardChequeDataEntryBtnCancelSubmit");
			outwardChequeDataEntryBtnConfirmSubmit = getButton(outwardChequeDataEntrySubmitConfirmModal,
					"outwardChequeDataEntryBtnConfirmSubmit");
		}

		outwardChequeDataEntryMicrPendingModal = getWindow(component, "outwardChequeDataEntryMicrPendingModal");
		if (outwardChequeDataEntryMicrPendingModal != null) {
			outwardChequeDataEntryLblMicrPendingSubmitMessage = getLabel(outwardChequeDataEntryMicrPendingModal,
					"outwardChequeDataEntryLblMicrPendingSubmitMessage");
			outwardChequeDataEntryLblMicrPendingBatchId = getLabel(outwardChequeDataEntryMicrPendingModal,
					"outwardChequeDataEntryLblMicrPendingBatchId");
			outwardChequeDataEntryLblMicrCompletedCount = getLabel(outwardChequeDataEntryMicrPendingModal,
					"outwardChequeDataEntryLblMicrCompletedCount");
			outwardChequeDataEntryLblMicrPendingCount = getLabel(outwardChequeDataEntryMicrPendingModal,
					"outwardChequeDataEntryLblMicrPendingCount");
			outwardChequeDataEntryBtnCloseMicrPending = getButton(outwardChequeDataEntryMicrPendingModal,
					"outwardChequeDataEntryBtnCloseMicrPending");
		}

		if (outwardChequeDataEntrySubmitSuccessModal != null) {
			outwardChequeDataEntryLblSubmitSuccess = getLabel(outwardChequeDataEntrySubmitSuccessModal,
					"outwardChequeDataEntryLblSubmitSuccess");
			outwardChequeDataEntryLblSubmitSuccessDetails = getLabel(outwardChequeDataEntrySubmitSuccessModal,
					"outwardChequeDataEntryLblSubmitSuccessDetails");
			outwardChequeDataEntryBtnGoToDataEntry = getButton(outwardChequeDataEntrySubmitSuccessModal,
					"outwardChequeDataEntryBtnGoToDataEntry");
		}

		hideModal(outwardChequeDataEntryRejectModal);
		hideModal(outwardChequeDataEntrySubmitConfirmModal);
		hideModal(outwardChequeDataEntryMicrPendingModal);
		hideModal(outwardChequeDataEntrySubmitSuccessModal);
	}

	private Button getButton(Component parent, String id) {
		if (parent == null || id == null)
			return null;
		Component c = parent.getFellowIfAny(id);
		return c instanceof Button ? (Button) c : null;
	}

	private Label getLabel(Component parent, String id) {
		if (parent == null || id == null)
			return null;
		Component c = parent.getFellowIfAny(id);
		return c instanceof Label ? (Label) c : null;
	}

	private Textbox getTextbox(Component parent, String id) {
		if (parent == null || id == null)
			return null;
		Component c = parent.getFellowIfAny(id);
		return c instanceof Textbox ? (Textbox) c : null;
	}

	private Datebox getDatebox(Component parent, String id) {
		if (parent == null || id == null)
			return null;
		Component c = parent.getFellowIfAny(id);
		return c instanceof Datebox ? (Datebox) c : null;
	}

	private Combobox getCombobox(Component parent, String id) {
		if (parent == null || id == null)
			return null;
		Component c = parent.getFellowIfAny(id);
		return c instanceof Combobox ? (Combobox) c : null;
	}

	private Image getImage(Component parent, String id) {
		if (parent == null || id == null)
			return null;
		Component c = parent.getFellowIfAny(id);
		return c instanceof Image ? (Image) c : null;
	}

	private Progressmeter getProgressmeter(Component parent, String id) {
		if (parent == null || id == null)
			return null;
		Component c = parent.getFellowIfAny(id);
		return c instanceof Progressmeter ? (Progressmeter) c : null;
	}

	private Window getWindow(Component parent, String id) {
		if (parent == null || id == null)
			return null;
		Component c = parent.getFellowIfAny(id);
		return c instanceof Window ? (Window) c : null;
	}

	private void configureDatebox() {
		if (outwardChequeDataEntryDtChequeDate == null)
			return;
		outwardChequeDataEntryDtChequeDate.setFormat("dd-MM-yyyy");
		outwardChequeDataEntryDtChequeDate.setButtonVisible(true);
	}

	private void resolveBatchContext() {
		outwardBatchId = null;
		scannedBatchId = null;
		outwardBatch = null;

		Object batchId = Executions.getCurrent().getAttribute("batchId");
		if (isBlank(batchId))
			batchId = Executions.getCurrent().getArg().get("batchId");
		if (isBlank(batchId))
			batchId = Executions.getCurrent().getSession().getAttribute("OUTWARD_DATA_ENTRY_BATCH_ID");
		if (isBlank(batchId))
			batchId = Executions.getCurrent().getSession().getAttribute("batchId");
		if (isBlank(batchId))
			batchId = Executions.getCurrent().getParameter("batchId");

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

		if (isBlank(outwardBatchId))
			outwardBatchId = suppliedBatchId;
		if (isBlank(scannedBatchId))
			scannedBatchId = outwardBatchId;

		Executions.getCurrent().getSession().setAttribute("OUTWARD_DATA_ENTRY_BATCH_ID", outwardBatchId);
		Executions.getCurrent().getSession().setAttribute("OUTWARD_DATA_ENTRY_SCANNED_BATCH_ID", scannedBatchId);
	}

	private void registerEvents() {
		registerSafeModalClose(outwardChequeDataEntryRejectModal);
		registerSafeModalClose(outwardChequeDataEntrySubmitConfirmModal);
		registerSafeModalClose(outwardChequeDataEntryMicrPendingModal);
		registerSafeModalClose(outwardChequeDataEntrySubmitSuccessModal);

		addClick(outwardChequeDataEntryBtnBack, event -> goBackToDashboard());
		addClick(outwardChequeDataEntryBtnGoToDataEntry, event -> {
			hideModal(outwardChequeDataEntrySubmitSuccessModal);
			goBackToDashboard();
		});

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

		addClick(outwardChequeDataEntryBtnCloseMicrPending, event -> hideModal(outwardChequeDataEntryMicrPendingModal));

		registerDirtyTracking(outwardChequeDataEntryTxtChequeNumber);
		registerDirtyTracking(outwardChequeDataEntryTxtAmount);
		registerDirtyTracking(outwardChequeDataEntryDtChequeDate);
		registerDirtyTracking(outwardChequeDataEntryTxtPayeeAccount);
		registerDirtyTracking(outwardChequeDataEntryTxtPayeeName);
		registerDirtyTracking(outwardChequeDataEntryTxtDraweeName);
	}

	private void registerSafeModalClose(Window window) {
		if (window == null)
			return;
		window.addEventListener(Events.ON_CLOSE, (Event event) -> {
			event.stopPropagation();
			window.setVisible(false);
		});
	}

	private void showModalSafely(Window window) {
		if (window == null)
			return;
		if (window.getPage() == null && outwardChequeDataEntryWin != null) {
			window.setParent(outwardChequeDataEntryWin);
		}
		window.setVisible(true);
		window.doModal();
	}

	private void hideModal(Window window) {
		if (window != null && window.getPage() != null) {
			window.setVisible(false);
		}
	}

	private interface EventAction {
		void execute(Event event) throws Exception;
	}

	private void addClick(Button button, EventAction action) {
		if (button == null || action == null)
			return;
		button.addEventListener(Events.ON_CLICK, event -> {
			try {
				action.execute(event);
			} catch (Exception exception) {
				showStatus("Operation failed: " + safeExceptionMessage(exception));
			}
		});
	}

	private void registerDirtyTracking(Component component) {
		if (component == null)
			return;
		component.addEventListener(Events.ON_CHANGE, event -> {
			clearFieldHighlight(component);
			markDirty();
		});
		component.addEventListener(Events.ON_CHANGING, event -> {
			clearFieldHighlight(component);
			markDirty();
		});
	}

	private void markDirty() {
		OutwardCheque cheque = getCurrentCheque();
		if (cheque != null && isEditableCheque(cheque)) {
			hasUnsavedChanges = true;
			updateActionButtons();
			updateNavigationButtons();
		}
	}

	private void loadDataEntryData() {
		try {
			batchSubmitted = false;

			loadScanCheques();
			loadExistingCheques();
			mergeScanAndOutwardCheques();
			rebuildActiveQueue();

			updateBatchSummary();

			int initialIndex = findFirstActionableCheque();
			if (initialIndex < 0 && !activeQueue.isEmpty()) {
				initialIndex = 0;
			}

			if (initialIndex >= 0) {
				currentChequeIndex = initialIndex;
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
		if (isBlank(scannedBatchId))
			return;

		try {
			List<ScanCheque> scans = scanService.getChequesByBatchId(scannedBatchId);
			if (scans != null)
				scanChequeList.addAll(scans);
		} catch (Exception ignored) {
			scanChequeList = new ArrayList<>();
		}
	}

	private void loadExistingCheques() {
		outwardChequeList = new ArrayList<>();
		if (isBlank(outwardBatchId))
			return;

		try {
			List<OutwardCheque> existing = outwardChequeService.getChequesByBatchId(outwardBatchId);
			if (existing != null)
				outwardChequeList.addAll(existing);
		} catch (Exception ignored) {
			outwardChequeList = new ArrayList<>();
		}
	}

	private void mergeScanAndOutwardCheques() {
		List<OutwardCheque> merged = new ArrayList<>();

		if (scanChequeList != null && !scanChequeList.isEmpty()) {
			for (int i = 0; i < scanChequeList.size(); i++) {
				ScanCheque sc = scanChequeList.get(i);
				if (sc == null)
					continue;

				OutwardCheque matched = findMatchingOutwardCheque(sc, i);
				if (matched != null) {
					merged.add(matched);
				} else {
					merged.add(buildOutwardChequeFromScan(sc));
				}
			}
		} else if (outwardChequeList != null && !outwardChequeList.isEmpty()) {
			merged.addAll(outwardChequeList);
		}

		outwardChequeList = merged;
	}

	private OutwardCheque findMatchingOutwardCheque(ScanCheque sc, int index) {
		if (outwardChequeList == null || outwardChequeList.isEmpty()) {
			return null;
		}

		String scNumber = safeValue(sc.getChequeNumber()).trim();
		String scFront = safeValue(sc.getChequeImageFront()).trim();

		if (!scNumber.isEmpty()) {
			for (OutwardCheque oc : outwardChequeList) {
				if (oc != null && scNumber.equalsIgnoreCase(safeValue(oc.getChequeNumber()).trim())) {
					return oc;
				}
			}
		}

		if (!scFront.isEmpty()) {
			for (OutwardCheque oc : outwardChequeList) {
				if (oc != null && scFront.equalsIgnoreCase(safeValue(oc.getChequeImageFront()).trim())) {
					return oc;
				}
			}
		}

		if (index < outwardChequeList.size()) {
			OutwardCheque oc = outwardChequeList.get(index);
			if (oc != null && !isBlank(oc.getOutwardChequeId())) {
				return oc;
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
		if (isMicrRejectedStatus(scanStatus)) {
			cheque.setChequeStatus(STATUS_MICR_REJECTED);
		} else if (isMicrPendingStatus(scanStatus)) {
			cheque.setChequeStatus(STATUS_PENDING_MICR_REPAIR);
		} else {
			cheque.setChequeStatus(STATUS_PENDING_DATA_ENTRY);
		}

		return cheque;
	}

	private void rebuildActiveQueue() {
		activeQueue = new ArrayList<>();
		if (outwardChequeList == null)
			return;
		for (OutwardCheque cheque : outwardChequeList) {
			if (cheque != null) {
				activeQueue.add(cheque);
			}
		}
	}

	private int findFirstActionableCheque() {
		if (activeQueue == null)
			return -1;
		for (int i = 0; i < activeQueue.size(); i++) {
			OutwardCheque c = activeQueue.get(i);
			if (c == null)
				continue;
			String status = normalizeChequeStatus(c.getChequeStatus());
			if (isOnHoldStatus(status) || STATUS_PENDING_DATA_ENTRY.equals(status) || status.isEmpty()) {
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
		if (cheque == null)
			return;

		clearAllFieldHighlights();
		hideValidation();

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
		if (scanCheque != null)
			imagePath = scanCheque.getChequeImageFront();
		if (isBlank(imagePath))
			imagePath = cheque.getChequeImageFront();

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
		if (cheque == null)
			return;
		if (outwardChequeDataEntryTxtChequeNumber != null)
			outwardChequeDataEntryTxtChequeNumber.setValue(safeValue(cheque.getChequeNumber()));
		if (outwardChequeDataEntryTxtAmount != null) {
			BigDecimal amount = cheque.getChequeAmount();
			outwardChequeDataEntryTxtAmount.setValue(amount == null ? "" : amount.toPlainString());
		}
		if (outwardChequeDataEntryDtChequeDate != null)
			outwardChequeDataEntryDtChequeDate.setValue(cheque.getChequeDate());
		if (outwardChequeDataEntryTxtMicrCode != null)
			outwardChequeDataEntryTxtMicrCode.setValue(safeValue(cheque.getMicrCode()));
		if (outwardChequeDataEntryTxtPayeeAccount != null)
			outwardChequeDataEntryTxtPayeeAccount.setValue(safeValue(cheque.getPayeeAccountNumber()));
		if (outwardChequeDataEntryTxtPayeeName != null)
			outwardChequeDataEntryTxtPayeeName.setValue(safeValue(cheque.getPayeeName()));
		if (outwardChequeDataEntryTxtDraweeName != null)
			outwardChequeDataEntryTxtDraweeName.setValue(safeValue(cheque.getDraweeName()));
	}

	private void populateChequeFieldsFromScan(ScanCheque scanCheque) {
		if (scanCheque == null)
			return;
		if (outwardChequeDataEntryTxtChequeNumber != null)
			outwardChequeDataEntryTxtChequeNumber.setValue(safeValue(scanCheque.getChequeNumber()));
		if (outwardChequeDataEntryTxtAmount != null) {
			BigDecimal amount = scanCheque.getChequeAmount();
			outwardChequeDataEntryTxtAmount.setValue(amount == null ? "" : amount.toPlainString());
		}
		if (outwardChequeDataEntryDtChequeDate != null)
			outwardChequeDataEntryDtChequeDate.setValue(scanCheque.getChequeDate());
		if (outwardChequeDataEntryTxtMicrCode != null)
			outwardChequeDataEntryTxtMicrCode.setValue(safeValue(scanCheque.getMicrCode()));
		if (outwardChequeDataEntryTxtPayeeAccount != null)
			outwardChequeDataEntryTxtPayeeAccount.setValue(safeValue(scanCheque.getPayeeAccountNumber()));
		if (outwardChequeDataEntryTxtPayeeName != null)
			outwardChequeDataEntryTxtPayeeName.setValue(safeValue(scanCheque.getPayeeName()));
		if (outwardChequeDataEntryTxtDraweeName != null)
			outwardChequeDataEntryTxtDraweeName.setValue(safeValue(scanCheque.getDraweeName()));
	}

	private BigDecimal getAmountFromField() {
		if (outwardChequeDataEntryTxtAmount == null)
			return null;
		String value = safeValue(outwardChequeDataEntryTxtAmount.getValue()).trim();
		if (value.isEmpty())
			return null;
		try {
			return new BigDecimal(value.replace(",", "").trim());
		} catch (NumberFormatException exception) {
			return null;
		}
	}

	private void populateOutwardChequeFromFields(OutwardCheque cheque) {
		if (cheque == null)
			return;
		if (outwardChequeDataEntryTxtChequeNumber != null)
			cheque.setChequeNumber(safeValue(outwardChequeDataEntryTxtChequeNumber.getValue()).trim());
		if (outwardChequeDataEntryTxtPayeeAccount != null)
			cheque.setPayeeAccountNumber(safeValue(outwardChequeDataEntryTxtPayeeAccount.getValue()).trim());
		if (outwardChequeDataEntryTxtPayeeName != null)
			cheque.setPayeeName(safeValue(outwardChequeDataEntryTxtPayeeName.getValue()).trim());
		if (outwardChequeDataEntryTxtDraweeName != null)
			cheque.setDraweeName(safeValue(outwardChequeDataEntryTxtDraweeName.getValue()).trim());
		if (outwardChequeDataEntryTxtMicrCode != null)
			cheque.setMicrCode(safeValue(outwardChequeDataEntryTxtMicrCode.getValue()).trim());

		cheque.setChequeAmount(getAmountFromField());
		if (outwardChequeDataEntryDtChequeDate != null && outwardChequeDataEntryDtChequeDate.getValue() != null) {
			cheque.setChequeDate(new Date(outwardChequeDataEntryDtChequeDate.getValue().getTime()));
		}
	}

	private boolean validateFields() {
		clearAllFieldHighlights();

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
			highlightError(outwardChequeDataEntryTxtChequeNumber, "Cheque number is required.");
			return false;
		}
		if (amountText.isEmpty()) {
			highlightError(outwardChequeDataEntryTxtAmount, "Cheque amount is required.");
			return false;
		}
		if (amount == null || amount.compareTo(BigDecimal.ZERO) < 0) {
			highlightError(outwardChequeDataEntryTxtAmount, "Enter a valid positive cheque amount.");
			return false;
		}
		if (outwardChequeDataEntryDtChequeDate == null || outwardChequeDataEntryDtChequeDate.getValue() == null) {
			highlightError(outwardChequeDataEntryDtChequeDate, "Cheque date is required.");
			return false;
		}
		if (payeeAccount.isEmpty()) {
			highlightError(outwardChequeDataEntryTxtPayeeAccount, "Payee account number is required.");
			return false;
		}
		if (payeeName.isEmpty()) {
			highlightError(outwardChequeDataEntryTxtPayeeName, "Payee name is required.");
			return false;
		}
		if (draweeName.isEmpty()) {
			highlightError(outwardChequeDataEntryTxtDraweeName, "Drawee name is required.");
			return false;
		}

		hideValidation();
		return true;
	}

	private void highlightError(Component comp, String message) {
		if (comp instanceof XulElement) {
			XulElement xulEl = (XulElement) comp;
			String s = xulEl.getSclass();
			xulEl.setSclass((s == null ? "" : s + " ") + ERROR_CLASS);
		}
		showValidation(message);
	}

	private void clearFieldHighlight(Component comp) {
		if (comp instanceof XulElement) {
			XulElement xulEl = (XulElement) comp;
			String s = xulEl.getSclass();
			if (s != null && s.contains(ERROR_CLASS)) {
				xulEl.setSclass(s.replace(ERROR_CLASS, "").trim());
			}
		}
	}

	private void clearAllFieldHighlights() {
		clearFieldHighlight(outwardChequeDataEntryTxtChequeNumber);
		clearFieldHighlight(outwardChequeDataEntryTxtAmount);
		clearFieldHighlight(outwardChequeDataEntryDtChequeDate);
		clearFieldHighlight(outwardChequeDataEntryTxtPayeeAccount);
		clearFieldHighlight(outwardChequeDataEntryTxtPayeeName);
		clearFieldHighlight(outwardChequeDataEntryTxtDraweeName);
	}

	private boolean isEditableCheque(OutwardCheque cheque) {
		if (cheque == null)
			return false;
		String status = normalizeChequeStatus(cheque.getChequeStatus());
		if (isRejectionRequestStatus(status) || isRejectionRejectStatus(status) || isMicrRejectedStatus(status)
				|| isMicrPendingStatus(status)) {
			return false;
		}
		return true;
	}

	private void setChequeFieldsEditable(boolean editable) {
		setFieldState(outwardChequeDataEntryTxtChequeNumber, editable);
		setFieldState(outwardChequeDataEntryTxtAmount, editable);
		setFieldState(outwardChequeDataEntryDtChequeDate, editable);
		setFieldState(outwardChequeDataEntryTxtPayeeAccount, editable);
		setFieldState(outwardChequeDataEntryTxtPayeeName, editable);
		setFieldState(outwardChequeDataEntryTxtDraweeName, editable);

		if (outwardChequeDataEntryTxtMicrCode != null) {
			outwardChequeDataEntryTxtMicrCode.setReadonly(true);
			outwardChequeDataEntryTxtMicrCode.setDisabled(true);
		}
	}

	private void setFieldState(Textbox textbox, boolean editable) {
		if (textbox == null)
			return;
		textbox.setReadonly(!editable);
		textbox.setDisabled(!editable);
	}

	private void setFieldState(Datebox datebox, boolean editable) {
		if (datebox == null)
			return;
		datebox.setReadonly(!editable);
		datebox.setDisabled(!editable);
		datebox.setButtonVisible(editable);
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

		// MICR REJECTED -> Click Save Cheque saves as REJECTION_REQUEST
		if (isMicrRejectedStatus(status)) {
			setChequeFieldsEditable(false);
			if (outwardChequeDataEntryBtnApprove != null) {
				outwardChequeDataEntryBtnApprove.setLabel("Save Cheque");
				outwardChequeDataEntryBtnApprove.setDisabled(false);
			}
			disableButton(outwardChequeDataEntryBtnReject);
			disableButton(outwardChequeDataEntryBtnResetItem);
			return;
		}

		if (isMicrPendingStatus(status)) {
			setChequeFieldsEditable(false);
			disableButton(outwardChequeDataEntryBtnApprove);
			disableButton(outwardChequeDataEntryBtnReject);
			disableButton(outwardChequeDataEntryBtnResetItem);
			return;
		}

		if (isRejectionRequestStatus(status) || isRejectionRejectStatus(status)) {
			setChequeFieldsEditable(false);
			disableButton(outwardChequeDataEntryBtnResetItem);
			disableButton(outwardChequeDataEntryBtnReject);
			disableButton(outwardChequeDataEntryBtnApprove);
			return;
		}

		boolean editable = isEditableCheque(cheque);

		if (outwardChequeDataEntryBtnApprove != null) {
			outwardChequeDataEntryBtnApprove.setLabel("✓ Approve Cheque");
			boolean alreadyVerified = STATUS_PENDING_VERIFICATION.equals(status);
			outwardChequeDataEntryBtnApprove.setDisabled(!editable || (alreadyVerified && !hasUnsavedChanges));
		}

		if (outwardChequeDataEntryBtnReject != null) {
			outwardChequeDataEntryBtnReject.setDisabled(!editable);
		}

		if (outwardChequeDataEntryBtnResetItem != null) {
			outwardChequeDataEntryBtnResetItem.setDisabled(!editable || !hasUnsavedChanges);
		}
	}

	private void saveCurrentCheque() {
		OutwardCheque cheque = getCurrentCheque();
		if (cheque == null)
			return;

		String status = normalizeChequeStatus(cheque.getChequeStatus());

		if (isMicrRejectedStatus(status)) {
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

			cheque.setOutwardChequeId(saved.getOutwardChequeId());
			cheque.setChequeStatus(STATUS_PENDING_VERIFICATION);
			syncChequeToFullList(saved);

			hasUnsavedChanges = false;
			refreshBatch();
			showStatus("Cheque approved and saved as PENDING_VERIFICATION.");
			rebuildActiveQueue();
			updateSubmitButton();

			int nextIndex = findNextPendingDataEntry(currentChequeIndex + 1);
			if (nextIndex >= 0) {
				currentChequeIndex = nextIndex;
				loadCurrentCheque();
			} else {
				loadCurrentCheque();
			}

		} catch (Exception exception) {
			showStatus("Unable to save cheque: " + safeExceptionMessage(exception));
		}
	}

	private void saveMicrRejectedCheque(OutwardCheque cheque) {
		try {
			cheque.setOutwardBatchId(outwardBatchId);
			cheque.setChequeStatus(STATUS_MICR_REJECTED);

			String batchForSave = !isBlank(scannedBatchId) ? scannedBatchId : outwardBatchId;
			OutwardCheque saved = outwardChequeService.saveMakerCheque(batchForSave, cheque);
			if (saved == null) {
				throw new IllegalStateException("Unable to save MICR rejected cheque.");
			}

			// Saving sets status to REJECTION_REQUEST in outward_cheque
			cheque.setOutwardChequeId(saved.getOutwardChequeId());
			cheque.setChequeStatus(STATUS_REJECTION_REQUEST);
			syncChequeToFullList(saved);

			hasUnsavedChanges = false;
			refreshBatch();
			showStatus("MICR rejected cheque saved as REJECTION_REQUEST.");
			rebuildActiveQueue();
			updateSubmitButton();

			int nextIndex = findNextPendingDataEntry(currentChequeIndex + 1);
			if (nextIndex >= 0) {
				currentChequeIndex = nextIndex;
				loadCurrentCheque();
			} else {
				loadCurrentCheque();
			}

		} catch (Exception exception) {
			showStatus("Unable to save MICR rejected cheque: " + safeExceptionMessage(exception));
		}
	}

	private void resetCurrentItem() {
		OutwardCheque cheque = getCurrentCheque();
		if (cheque == null || !isEditableCheque(cheque))
			return;

		populateChequeFields(cheque);
		clearAllFieldHighlights();
		hideValidation();
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

		if (currentChequeIndex > 0) {
			currentChequeIndex--;
			loadCurrentCheque();
		} else {
			showStatus("Already at the first cheque in this batch.");
		}
	}

	private void nextCheque() {
		if (hasUnsavedChanges) {
			showStatus("Save or reset the current cheque before moving.");
			return;
		}

		if (currentChequeIndex < activeQueue.size() - 1) {
			currentChequeIndex++;
			loadCurrentCheque();
		} else {
			showStatus("Already at the last cheque in this batch.");
		}
	}

	private void updateNavigationButtons() {
		if (outwardChequeDataEntryBtnPrevious == null || outwardChequeDataEntryBtnNext == null)
			return;

		if (activeQueue == null || activeQueue.isEmpty()) {
			outwardChequeDataEntryBtnPrevious.setDisabled(true);
			outwardChequeDataEntryBtnNext.setDisabled(true);
			return;
		}

		boolean canGoBack = (currentChequeIndex > 0) && !hasUnsavedChanges;
		outwardChequeDataEntryBtnPrevious.setDisabled(!canGoBack);

		boolean canGoForward = (currentChequeIndex < activeQueue.size() - 1) && !hasUnsavedChanges;
		outwardChequeDataEntryBtnNext.setDisabled(!canGoForward);
	}

	private int findNextPendingDataEntry(int start) {
		if (activeQueue == null)
			return -1;
		for (int i = Math.max(0, start); i < activeQueue.size(); i++) {
			OutwardCheque cheque = activeQueue.get(i);
			if (cheque == null)
				continue;
			String status = normalizeChequeStatus(cheque.getChequeStatus());
			if (STATUS_PENDING_DATA_ENTRY.equals(status) || isOnHoldStatus(status) || status.isEmpty()) {
				return i;
			}
		}
		for (int i = 0; i < start && i < activeQueue.size(); i++) {
			OutwardCheque cheque = activeQueue.get(i);
			if (cheque == null)
				continue;
			String status = normalizeChequeStatus(cheque.getChequeStatus());
			if (STATUS_PENDING_DATA_ENTRY.equals(status) || isOnHoldStatus(status) || status.isEmpty()) {
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
		if (outwardChequeList == null)
			return 0;
		for (OutwardCheque cheque : outwardChequeList) {
			if (cheque == null)
				continue;
			String status = normalizeChequeStatus(cheque.getChequeStatus());
			if (STATUS_PENDING_VERIFICATION.equals(status) || isRejectionRequestStatus(status)
					|| isRejectionRejectStatus(status)) {
				count++;
			}
		}
		return count;
	}

	private boolean hasPendingMicrRepair() {
		return countStatus(outwardChequeList, STATUS_PENDING_MICR_REPAIR) > 0;
	}

	private int countMicrPending() {
		return countStatus(outwardChequeList, STATUS_PENDING_MICR_REPAIR);
	}

	private void updateSubmitButton() {
		if (outwardChequeDataEntryBtnSubmit == null)
			return;
		outwardChequeDataEntryBtnSubmit.setDisabled(hasUnsavedChanges || batchSubmitted);
	}

	private boolean isBatchReadyForChecker() {
		if (batchSubmitted || hasUnsavedChanges)
			return false;
		if (outwardChequeList == null || outwardChequeList.isEmpty())
			return false;

		int total = getBatchTotal();
		if (total <= 0 || outwardChequeList.size() != total)
			return false;

		for (OutwardCheque cheque : outwardChequeList) {
			if (cheque == null)
				return false;
			String status = normalizeChequeStatus(cheque.getChequeStatus());
			boolean isProcessed = STATUS_PENDING_VERIFICATION.equals(status) || isRejectionRequestStatus(status)
					|| isRejectionRejectStatus(status);

			if (!isProcessed) {
				return false;
			}
		}
		return countCompletedMakerCheques() == total;
	}

	private void updateCurrentChequeStatus(OutwardCheque cheque) {
		hideAllPanels();
		if (cheque == null)
			return;

		String status = normalizeChequeStatus(cheque.getChequeStatus());

		if (outwardChequeDataEntryLblChequeStatus != null) {
			outwardChequeDataEntryLblChequeStatus.setValue(displayStatus(status));
		}
		if (outwardChequeDataEntryLblFooterStatus != null) {
			outwardChequeDataEntryLblFooterStatus.setValue(displayStatus(status));
		}

		if (isMicrPendingStatus(status)) {
			showPanel(outwardChequeDataEntryMicrPendingPanel);
			setLabel(outwardChequeDataEntryLblMicrPendingMessage,
					"This cheque is awaiting MICR repair. All fields are disabled until MICR is resolved.");
			setChequeFieldsEditable(false);
			updateActionButtons();
			updateNavigationButtons();
			updateSubmitButton();
			return;
		}

		if (isMicrRejectedStatus(status)) {
			showPanel(outwardChequeDataEntryMicrRejectedPanel);
			setLabel(outwardChequeDataEntryLblMicrRejectedMessage,
					"This cheque was rejected during MICR. Click 'Save Cheque' to record as REJECTION_REQUEST.");
			setLabel(outwardChequeDataEntryLblMicrReason, "MICR Status: MICR Rejected");
			if (outwardChequeDataEntryLblMicrReason != null)
				outwardChequeDataEntryLblMicrReason.setVisible(true);
			setChequeFieldsEditable(false);
			updateActionButtons();
			updateNavigationButtons();
			return;
		}

		if (isRejectionRejectStatus(status)) {
			showPanel(outwardChequeDataEntryMakerRejectionPanel);
			loadRejectionStatus(cheque, "Data Entry Rejected (REJECTION_REJECT)");
			setChequeFieldsEditable(false);
			updateActionButtons();
			updateNavigationButtons();
			return;
		}

		if (isRejectionRequestStatus(status)) {
			showPanel(outwardChequeDataEntryMakerRejectionPanel);
			loadRejectionStatus(cheque, "Rejection Request (REJECTION_REQUEST)");
			setChequeFieldsEditable(false);
			updateActionButtons();
			updateNavigationButtons();
			return;
		}

		if (isOnHoldStatus(status)) {
			showPanel(outwardChequeDataEntryCheckerSendBackPanel);
			setLabel(outwardChequeDataEntryLblCheckerSendBackMessage,
					"This cheque was returned by Checker for modification. Please verify and save.");
			setChequeFieldsEditable(true);
			updateActionButtons();
			updateNavigationButtons();
			return;
		}

		if (STATUS_PENDING_VERIFICATION.equals(status)) {
			setChequeFieldsEditable(true);
			updateActionButtons();
			updateNavigationButtons();
			return;
		}

		setChequeFieldsEditable(true);
		updateActionButtons();
		updateNavigationButtons();
	}

	private void loadRejectionStatus(OutwardCheque cheque, String defaultTitle) {
		if (cheque == null)
			return;

		String chequeId = safeValue(cheque.getOutwardChequeId()).trim();
		OutwardChequeRequest request = null;

		if (!chequeId.isEmpty()) {
			try {
				request = outwardChequeService.getRejectionRequestByChequeId(chequeId);
			} catch (Exception ignored) {
			}
		}

		if (request != null) {
			String reason = safeValue(request.getReason()).trim();
			String remarks = safeValue(request.getRemarks()).trim();
			setLabel(outwardChequeDataEntryLblRejectionReason, reason.isEmpty() ? defaultTitle : "Reason: " + reason);
			setLabel(outwardChequeDataEntryLblRejectionRemarks, remarks.isEmpty() ? "" : "Remarks: " + remarks);
		} else {
			setLabel(outwardChequeDataEntryLblRejectionReason, defaultTitle);
			setLabel(outwardChequeDataEntryLblRejectionRemarks, "");
		}

		showPanel(outwardChequeDataEntryMakerRejectionPanel);
	}

	private void updatePostDatedIndicator(OutwardCheque cheque) {
		if (outwardChequeDataEntryLblPostDated == null)
			return;
		outwardChequeDataEntryLblPostDated.setVisible(false);
		if (cheque == null || cheque.getChequeDate() == null)
			return;

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

		if (outwardChequeDataEntryLblPostDated != null)
			outwardChequeDataEntryLblPostDated.setVisible(false);
		if (outwardChequeDataEntryLblMicrReason != null)
			outwardChequeDataEntryLblMicrReason.setVisible(false);
	}

	private void openRejectWindow() {
		OutwardCheque cheque = getCurrentCheque();
		if (cheque == null || !isEditableCheque(cheque)) {
			showStatus("This cheque cannot be submitted for rejection.");
			return;
		}

		if (outwardChequeDataEntryRejectModal == null) {
			showStatus("Rejection window is not configured correctly.");
			return;
		}

		if (outwardChequeDataEntryCmbRejectReason != null) {
			outwardChequeDataEntryCmbRejectReason.setSelectedItem(null);
		}
		if (outwardChequeDataEntryTxtRejectRemarks != null) {
			outwardChequeDataEntryTxtRejectRemarks.setValue("");
		}

		showModalSafely(outwardChequeDataEntryRejectModal);
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

		if (outwardChequeDataEntryCmbRejectReason == null)
			return;

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
		if (reasonId.isEmpty())
			reasonId = safeValue(reasonObject.getRejectedReasonCode()).trim();

		String reason = safeValue(reasonObject.getRejectedReasonName()).trim();
		if (reason.isEmpty())
			reason = safeValue(reasonObject.getRejectedReasonCode()).trim();

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
			populateOutwardChequeFromFields(cheque);
			cheque.setOutwardBatchId(outwardBatchId);

			String batchForSave = !isBlank(scannedBatchId) ? scannedBatchId : outwardBatchId;

			// Rejection from Data Entry sets status to REJECTION_REJECT
			boolean saved = outwardChequeService.saveMakerRejectionRequest(batchForSave, cheque, reasonId, reason,
					remarks);

			if (!saved) {
				closeRejectWindow();
				showStatus("Unable to create rejection request.");
				return;
			}

			cheque.setChequeStatus(STATUS_REJECTION_REJECT);
			syncChequeToFullList(cheque);

			hasUnsavedChanges = false;
			closeRejectWindow();
			refreshBatch();
			rebuildActiveQueue();

			showStatus("Cheque rejected as REJECTION_REJECT.");
			updateSubmitButton();
			loadCurrentCheque();

		} catch (Exception exception) {
			closeRejectWindow();
			showStatus("Unable to create rejection request: " + safeExceptionMessage(exception));
		}
	}

	private void loadRejectedReasons() {
		if (outwardChequeDataEntryCmbRejectReason == null)
			return;
		outwardChequeDataEntryCmbRejectReason.getItems().clear();

		try {
			List<RejectedReason> reasons = rejectedReasonService.getAllRejectedReasons();
			if (reasons == null)
				return;

			for (RejectedReason reason : reasons) {
				if (reason == null)
					continue;
				String code = safeValue(reason.getRejectedReasonCode()).trim();
				String name = safeValue(reason.getRejectedReasonName()).trim();
				String label = (!code.isEmpty() && !name.isEmpty()) ? code + " - " + name
						: (!name.isEmpty() ? name : code);

				Comboitem item = new Comboitem();
				item.setLabel(label);
				item.setValue(reason);

				String description = safeValue(reason.getRejectedReasonDescription()).trim();
				if (!description.isEmpty())
					item.setTooltiptext(description);

				outwardChequeDataEntryCmbRejectReason.appendChild(item);
			}
		} catch (Exception exception) {
			showStatus("Unable to load rejection reasons: " + safeExceptionMessage(exception));
		}
	}

	private void submitToChecker() {
		if (hasUnsavedChanges) {
			showStatus("Save or reset the current cheque before submission.");
			return;
		}

		int micrPending = countMicrPending();
		if (micrPending > 0) {
			if (outwardChequeDataEntryMicrPendingModal != null) {
				setLabel(outwardChequeDataEntryLblMicrPendingBatchId, isBlank(outwardBatchId) ? "-" : outwardBatchId);
				setLabel(outwardChequeDataEntryLblMicrCompletedCount,
						countCompletedMakerCheques() + " / " + getBatchTotal());
				setLabel(outwardChequeDataEntryLblMicrPendingCount, String.valueOf(micrPending));
				setLabel(outwardChequeDataEntryLblMicrPendingSubmitMessage,
						"Batch cannot be submitted yet. There "
								+ (micrPending == 1 ? "is still 1 cheque" : "are still " + micrPending + " cheques")
								+ " pending in MICR Repair.");
				showModalSafely(outwardChequeDataEntryMicrPendingModal);
			} else {
				showStatus(
						"Batch cannot be submitted yet: " + micrPending + " cheque(s) still pending in MICR Repair.");
			}
			return;
		}

		if (!isBatchReadyForChecker()) {
			int pendingDataEntry = countStatus(outwardChequeList, STATUS_PENDING_DATA_ENTRY);
			if (pendingDataEntry > 0) {
				showStatus(pendingDataEntry + " cheque(s) still require Data Entry.");
				return;
			}
			showStatus("All cheques must be completed before submission.");
			return;
		}

		showSubmitConfirmation(getBatchTotal(), countCompletedMakerCheques());
	}

	private void showSubmitConfirmation(int total, int completed) {
		if (outwardChequeDataEntrySubmitConfirmModal == null) {
			completeBatchSubmission();
			return;
		}

		setLabel(outwardChequeDataEntryLblConfirmBatchId, isBlank(outwardBatchId) ? "-" : outwardBatchId);
		setLabel(outwardChequeDataEntryLblConfirmTotal, completed + " / " + total);
		setLabel(outwardChequeDataEntryLblConfirmCompleted, String.valueOf(completed));

		showModalSafely(outwardChequeDataEntrySubmitConfirmModal);
	}

	private void completeBatchSubmission() {
		try {
			int total = getBatchTotal();
			if (total <= 0) {
				showStatus("No cheques are available for submission.");
				return;
			}

			if (!isBatchReadyForChecker()) {
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
			if (micrRepairRefreshTimer != null)
				micrRepairRefreshTimer.stop();

			if (outwardBatch != null) {
				outwardBatch.setBatchStatus(STATUS_PENDING_CHECKER_PROCESS);
			}

			// Cheques maintain their exact status (PENDING_VERIFICATION, REJECTION_REQUEST,
			// REJECTION_REJECT)

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
		String details = "Total Cheques: " + total + " / " + total + " | Batch Status: "
				+ displayStatus(STATUS_PENDING_CHECKER_PROCESS);
		if (!notificationSent) {
			details += " | Checker notification could not be sent.";
		}

		setLabel(outwardChequeDataEntryLblSubmitSuccess, "Batch " + outwardBatchId + " submitted successfully.");
		setLabel(outwardChequeDataEntryLblSubmitSuccessDetails, details);

		if (outwardChequeDataEntrySubmitSuccessModal != null) {
			showModalSafely(outwardChequeDataEntrySubmitSuccessModal);
		} else {
			goBackToDashboard();
		}
	}

	private void showWaitingForMicrRepair() {
		currentChequeIndex = -1;
		clearChequeFields();
		setChequeFieldsEditable(false);

		setLabel(outwardChequeDataEntryLblRecordImage, "Waiting for MICR Repair");
		setLabel(outwardChequeDataEntryLblFooterStatus, "Pending MICR Repair");

		if (outwardChequeDataEntryLblImageEmpty != null) {
			int pending = countMicrPending();
			outwardChequeDataEntryLblImageEmpty.setValue(pending + " cheque(s) awaiting MICR processing.");
			outwardChequeDataEntryLblImageEmpty.setVisible(true);
		}

		if (outwardChequeDataEntryChequeImage != null)
			outwardChequeDataEntryChequeImage.setVisible(false);

		hideAllPanels();
		showPanel(outwardChequeDataEntryMicrPendingPanel);
		setLabel(outwardChequeDataEntryLblMicrPendingMessage,
				countMicrPending() + " cheque(s) awaiting MICR processing.");
		updateActionButtons();
		updateSubmitButton();
	}

	private void startMicrRepairRefreshTimer() {
		if (outwardChequeDataEntryWin == null || micrRepairRefreshTimer != null)
			return;

		micrRepairRefreshTimer = new Timer();
		micrRepairRefreshTimer.setDelay(3000);
		micrRepairRefreshTimer.setRepeats(true);
		micrRepairRefreshTimer.addEventListener(Events.ON_TIMER, event -> refreshAfterMicrRepair());
		outwardChequeDataEntryWin.appendChild(micrRepairRefreshTimer);
	}

	private void refreshAfterMicrRepair() {
		if (batchSubmitted || isBlank(outwardBatchId) || hasUnsavedChanges)
			return;
		if (!hasPendingMicrRepair())
			return;

		try {
			List<OutwardCheque> latest = outwardChequeService.getChequesByBatchId(outwardBatchId);
			if (latest == null)
				return;

			outwardChequeList = new ArrayList<>(latest);
			mergeScanAndOutwardCheques();
			rebuildActiveQueue();
			updateBatchSummary();

			if (currentChequeIndex < 0 && !activeQueue.isEmpty()) {
				currentChequeIndex = 0;
				loadCurrentCheque();
			}
			updateSubmitButton();
		} catch (Exception ignored) {
		}
	}

	private int countStatus(List<OutwardCheque> list, String requiredStatus) {
		if (list == null)
			return 0;
		int count = 0;
		for (OutwardCheque cheque : list) {
			if (cheque == null)
				continue;
			if (requiredStatus.equals(normalizeChequeStatus(cheque.getChequeStatus()))) {
				count++;
			}
		}
		return count;
	}

	private ScanCheque findMatchingScanCheque(OutwardCheque cheque) {
		if (cheque == null || scanChequeList == null)
			return null;
		String number = safeValue(cheque.getChequeNumber()).trim();
		String front = safeValue(cheque.getChequeImageFront()).trim();
		String back = safeValue(cheque.getChequeImageBack()).trim();

		for (ScanCheque scanCheque : scanChequeList) {
			if (scanCheque == null)
				continue;
			if (!front.isEmpty() && front.equalsIgnoreCase(safeValue(scanCheque.getChequeImageFront()).trim()))
				return scanCheque;
			if (!back.isEmpty() && back.equalsIgnoreCase(safeValue(scanCheque.getChequeImageBack()).trim()))
				return scanCheque;
			if (!number.isEmpty() && number.equalsIgnoreCase(safeValue(scanCheque.getChequeNumber()).trim()))
				return scanCheque;
		}
		return null;
	}

	private void syncChequeToFullList(OutwardCheque cheque) {
		if (cheque == null || outwardChequeList == null)
			return;

		if (currentChequeIndex >= 0 && currentChequeIndex < outwardChequeList.size()) {
			outwardChequeList.set(currentChequeIndex, cheque);
			return;
		}

		String id = safeValue(cheque.getOutwardChequeId()).trim();
		for (int i = 0; i < outwardChequeList.size(); i++) {
			OutwardCheque item = outwardChequeList.get(i);
			if (item != null && !id.isEmpty() && id.equalsIgnoreCase(safeValue(item.getOutwardChequeId()).trim())) {
				outwardChequeList.set(i, cheque);
				return;
			}
		}
	}

	private void refreshBatch() {
		if (isBlank(outwardBatchId))
			return;
		try {
			outwardBatch = outwardBatchService.getBatchById(outwardBatchId);
		} catch (Exception ignored) {
		}
		updateBatchSummary();
	}

	private void toggleImage() {
		OutwardCheque cheque = getCurrentCheque();
		if (cheque == null || outwardChequeDataEntryChequeImage == null)
			return;

		String imagePath;
		if (showingFrontImage) {
			imagePath = cheque.getChequeImageBack();
			showingFrontImage = false;
			if (outwardChequeDataEntryBtnImageToggle != null)
				outwardChequeDataEntryBtnImageToggle.setLabel("Front View");
		} else {
			imagePath = cheque.getChequeImageFront();
			showingFrontImage = true;
			if (outwardChequeDataEntryBtnImageToggle != null)
				outwardChequeDataEntryBtnImageToggle.setLabel("Back View");
		}
		loadImage(imagePath);
	}

	private void loadImage(String imagePath) {
		if (outwardChequeDataEntryChequeImage == null)
			return;

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

			if (outwardChequeDataEntryLblImageEmpty != null)
				outwardChequeDataEntryLblImageEmpty.setVisible(false);
			if (outwardChequeDataEntryLblImageMessage != null)
				outwardChequeDataEntryLblImageMessage.setVisible(false);
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
		if (outwardChequeDataEntryBtnImageToggle != null)
			outwardChequeDataEntryBtnImageToggle.setLabel("Back View");
		if (outwardChequeDataEntryChequeImage != null)
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
		imageRotation = (imageRotation + 90) % 360;
		applyImageTransform();
	}

	private void resetImage() {
		imageZoom = 1.0;
		imageRotation = 0;
		applyImageTransform();
	}

	private void applyImageTransform() {
		if (outwardChequeDataEntryChequeImage == null)
			return;
		outwardChequeDataEntryChequeImage
				.setStyle("transform:scale(" + imageZoom + ") rotate(" + imageRotation + "deg);");
	}

	private void clearChequeFields() {
		if (outwardChequeDataEntryTxtChequeNumber != null)
			outwardChequeDataEntryTxtChequeNumber.setValue("");
		if (outwardChequeDataEntryTxtAmount != null)
			outwardChequeDataEntryTxtAmount.setValue("");
		if (outwardChequeDataEntryDtChequeDate != null)
			outwardChequeDataEntryDtChequeDate.setValue(null);
		if (outwardChequeDataEntryTxtMicrCode != null)
			outwardChequeDataEntryTxtMicrCode.setValue("");
		if (outwardChequeDataEntryTxtPayeeAccount != null)
			outwardChequeDataEntryTxtPayeeAccount.setValue("");
		if (outwardChequeDataEntryTxtPayeeName != null)
			outwardChequeDataEntryTxtPayeeName.setValue("");
		if (outwardChequeDataEntryTxtDraweeName != null)
			outwardChequeDataEntryTxtDraweeName.setValue("");
		clearAllFieldHighlights();
	}

	private void clearPage() {
		clearChequeFields();
		setChequeFieldsEditable(false);
		setLabel(outwardChequeDataEntryLblBatchId, isBlank(outwardBatchId) ? "-" : outwardBatchId);
		setLabel(outwardChequeDataEntryLblTotal, "0");
		setLabel(outwardChequeDataEntryLblFooterStatus, "-");
		setLabel(outwardChequeDataEntryLblRecordImage, "Cheque 0 of 0");
		updateActionButtons();
		if (outwardChequeDataEntryChequeImage != null)
			outwardChequeDataEntryChequeImage.setVisible(false);
		if (outwardChequeDataEntryLblImageEmpty != null)
			outwardChequeDataEntryLblImageEmpty.setVisible(true);
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
			Executions.sendRedirect(DATA_ENTRY_ZUL);
			return;
		}

		mainContentArea.clearDynamicProperties();
		mainContentArea.setSrc(DATA_ENTRY_ZUL);
	}

	private Include findMainContentArea() {
		Component current = outwardChequeDataEntryWin;
		while (current != null) {
			Component fellow = current.getFellowIfAny("mainContentArea");
			if (fellow instanceof Include)
				return (Include) fellow;
			current = current.getParent();
		}

		Component root = outwardChequeDataEntryWin;
		while (root != null && root.getParent() != null) {
			root = root.getParent();
		}

		if (root != null) {
			Component fellow = root.getFellowIfAny("mainContentArea");
			if (fellow instanceof Include)
				return (Include) fellow;
		}

		return null;
	}

	private void showStatus(String message) {
		if (outwardChequeDataEntryLblFooterStatus == null || isBlank(message))
			return;
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
		if (component != null)
			component.setVisible(true);
	}

	private void hidePanel(Component component) {
		if (component != null)
			component.setVisible(false);
	}

	private void setLabel(Label label, String value) {
		if (label != null)
			label.setValue(value == null ? "" : value);
	}

	private void setButtonEnabled(Button button, boolean enabled) {
		if (button != null)
			button.setDisabled(!enabled);
	}

	private void disableButton(Button button) {
		if (button != null)
			button.setDisabled(true);
	}

	private String normalizeChequeStatus(String status) {
		if (status == null || status.trim().isEmpty())
			return "";
		return status.trim().replace("-", "_").replace(" ", "_").toUpperCase(Locale.ENGLISH);
	}

	private boolean isMicrPendingStatus(String status) {
		String s = normalizeChequeStatus(status);
		return s.equals("PENDING_MICR_REPAIR") || s.equals("MICR_PENDING") || s.equals("PENDING_MICR");
	}

	private boolean isMicrRejectedStatus(String status) {
		String s = normalizeChequeStatus(status);
		return s.equals("MICR_REJECTED") || s.equals("MICR_REJECT");
	}

	private boolean isRejectionRequestStatus(String status) {
		String s = normalizeChequeStatus(status);
		return s.equals("REJECTION_REQUEST");
	}

	private boolean isRejectionRejectStatus(String status) {
		String s = normalizeChequeStatus(status);
		return s.equals("REJECTION_REJECT") || s.equals("REJECTED") || s.equals("MAKER_REJECTED");
	}

	private boolean isOnHoldStatus(String status) {
		String s = normalizeChequeStatus(status);
		return s.equals("ON_HOLD") || s.equals("SEND_BACK") || s.equals("CHECKER_SEND_BACK");
	}

	private String displayStatus(String status) {
		String normalized = normalizeChequeStatus(status);
		if (normalized.isEmpty())
			return "-";

		String[] words = normalized.toLowerCase(Locale.ENGLISH).split("_");
		StringBuilder result = new StringBuilder();
		for (String word : words) {
			if (word == null || word.isEmpty())
				continue;
			if (result.length() > 0)
				result.append(" ");
			result.append(Character.toUpperCase(word.charAt(0)));
			if (word.length() > 1)
				result.append(word.substring(1));
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