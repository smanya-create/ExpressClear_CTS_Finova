package com.iispl.cts.controller.outward.maker;

import java.math.BigDecimal;
import java.sql.Date;
import java.util.ArrayList;
import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Div;
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
import com.iispl.cts.validator.OutwardChequeDataEntryValidator;
import com.iispl.cts.validator.OutwardChequeDataEntryValidator.ValidationResult;
import com.iispl.cts.validatorimpl.OutwardChequeDataEntryValidatorImpl;

public class OutwardChequeDataEntryController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 1L;

	private static final String STATUS_PENDING_DATA_ENTRY = "PENDING_DATA_ENTRY";
	private static final String STATUS_PENDING_VERIFICATION = "PENDING_VERIFICATION";
	private static final String STATUS_MAKER_RETURNED = "MAKER_RETURNED";
	private static final String STATUS_REJECT_REQUEST = "REJECT_REQUEST";
	private static final String STATUS_MICR_REJECTED = "MICR_REJECTED";
	private static final String STATUS_PENDING_MICR_REPAIR = "PENDING_MICR_REPAIR";
	private static final String STATUS_PENDING_CHECKER_PROCESS = "PENDING_CHECKER_PROCESS";
	private static final String STATUS_ON_HOLD = "ON_HOLD";

	private static final String NOTIFICATION_ROLE_OUTWARD_CHECKER = "OUTWARD_CHECKER";
	private static final String DATA_ENTRY_ZUL = "/outward/maker/data-entry.zul";
	private static final String ERROR_CLASS = "field-error-red";

	private Component outwardChequeDataEntryWin;

	private Button outwardChequeDataEntryBtnBack;
	private Button outwardChequeDataEntryBtnTopPrevious;
	private Button outwardChequeDataEntryBtnTopNext;

	private Button outwardChequeDataEntryBtnImageToggle;
	private Button outwardChequeDataEntryBtnZoomOut;
	private Button outwardChequeDataEntryBtnZoomIn;
	private Button outwardChequeDataEntryBtnRotate;
	private Button outwardChequeDataEntryBtnReset;

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

	private Label outwardChequeDataEntryLblBatchId;
	private Label outwardChequeDataEntryLblTotal;
	private Label outwardChequeDataEntryLblRecord;
	private Label outwardChequeDataEntryLblTopRecord;
	private Label outwardChequeDataEntryLblImageEmpty;
	private Label outwardChequeDataEntryLblChequeStatus;

	private Div dataEntryAlertBox;
	private Label lblDataEntryAlertTitle;
	private Label lblDataEntryAlertMessage;
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

	private Progressmeter outwardChequeDataEntryProgress;

	private Window outwardChequeDataEntryRejectModal;
	private Window outwardChequeDataEntrySubmitConfirmModal;
	private Window outwardChequeDataEntryMicrPendingModal;
	private Window outwardChequeDataEntrySubmitSuccessModal;

	private Label outwardChequeDataEntryLblConfirmBatchId;
	private Label outwardChequeDataEntryLblConfirmTotal;
	private Label outwardChequeDataEntryLblMicrPendingSubmitMessage;
	private Label outwardChequeDataEntryLblSubmitSuccess;
	private Label outwardChequeDataEntryLblSubmitSuccessDetails;

	private OutwardBatchService outwardBatchService;
	private OutwardChequeService outwardChequeService;
	private ScanService scanService;
	private NotificationService notificationService;
	private RejectedReasonService rejectedReasonService;
	private OutwardChequeDataEntryValidator validator;

	private String outwardBatchId;
	private String scannedBatchId;
	private OutwardBatch outwardBatch;

	private List<OutwardCheque> allBatchCheques = new ArrayList<>();
	private List<OutwardCheque> activeQueue = new ArrayList<>();
	private List<ScanCheque> scanChequeList = new ArrayList<>();

	private int currentChequeIndex = -1;
	private boolean showingFrontImage = true;
	private double imageZoom = 1.0;
	private int imageRotation = 0;

	private boolean hasUnsavedChanges;
	private boolean batchSubmitted;
	private boolean isReworkBatch = false;
	private Timer micrRepairRefreshTimer;

	@Override
	public void doAfterCompose(Component component) throws Exception {
		super.doAfterCompose(component);

		outwardChequeDataEntryWin = component;

		outwardBatchService = new OutwardBatchServiceImpl();
		outwardChequeService = new OutwardChequeServiceImpl();
		scanService = new ScanServiceImpl();
		notificationService = new NotificationServiceImpl();
		rejectedReasonService = RejectedReasonServiceImpl.getInstance();
		validator = new OutwardChequeDataEntryValidatorImpl();

		bindComponents(component);
		configureDatebox();
		resolveBatchContext();
		registerEvents();
		loadRejectedReasons();
		loadDataEntryData();
		startMicrRepairRefreshTimer();
	}

	private void bindComponents(Component component) {
		outwardChequeDataEntryBtnBack = getFellow(component, "outwardChequeDataEntryBtnBack", Button.class);
		outwardChequeDataEntryBtnTopPrevious = getFellow(component, "outwardChequeDataEntryBtnTopPrevious",
				Button.class);
		outwardChequeDataEntryBtnTopNext = getFellow(component, "outwardChequeDataEntryBtnTopNext", Button.class);

		outwardChequeDataEntryBtnImageToggle = getFellow(component, "outwardChequeDataEntryBtnImageToggle",
				Button.class);
		outwardChequeDataEntryBtnZoomOut = getFellow(component, "outwardChequeDataEntryBtnZoomOut", Button.class);
		outwardChequeDataEntryBtnZoomIn = getFellow(component, "outwardChequeDataEntryBtnZoomIn", Button.class);
		outwardChequeDataEntryBtnRotate = getFellow(component, "outwardChequeDataEntryBtnRotate", Button.class);
		outwardChequeDataEntryBtnReset = getFellow(component, "outwardChequeDataEntryBtnReset", Button.class);

		outwardChequeDataEntryBtnResetItem = getFellow(component, "outwardChequeDataEntryBtnResetItem", Button.class);
		outwardChequeDataEntryBtnReject = getFellow(component, "outwardChequeDataEntryBtnReject", Button.class);
		outwardChequeDataEntryBtnApprove = getFellow(component, "outwardChequeDataEntryBtnApprove", Button.class);
		outwardChequeDataEntryBtnSubmit = getFellow(component, "outwardChequeDataEntryBtnSubmit", Button.class);

		outwardChequeDataEntryLblBatchId = getFellow(component, "outwardChequeDataEntryLblBatchId", Label.class);
		outwardChequeDataEntryLblTotal = getFellow(component, "outwardChequeDataEntryLblTotal", Label.class);
		outwardChequeDataEntryLblRecord = getFellow(component, "outwardChequeDataEntryLblRecord", Label.class);
		outwardChequeDataEntryLblTopRecord = getFellow(component, "outwardChequeDataEntryLblTopRecord", Label.class);
		outwardChequeDataEntryLblImageEmpty = getFellow(component, "outwardChequeDataEntryLblImageEmpty", Label.class);
		outwardChequeDataEntryLblChequeStatus = getFellow(component, "outwardChequeDataEntryLblChequeStatus",
				Label.class);

		dataEntryAlertBox = getFellow(component, "dataEntryAlertBox", Div.class);
		lblDataEntryAlertTitle = getFellow(component, "lblDataEntryAlertTitle", Label.class);
		lblDataEntryAlertMessage = getFellow(component, "lblDataEntryAlertMessage", Label.class);
		outwardChequeDataEntryLblValidationStatus = getFellow(component, "outwardChequeDataEntryLblValidationStatus",
				Label.class);

		outwardChequeDataEntryChequeImage = getFellow(component, "outwardChequeDataEntryChequeImage", Image.class);

		outwardChequeDataEntryTxtChequeNumber = getFellow(component, "outwardChequeDataEntryTxtChequeNumber",
				Textbox.class);
		outwardChequeDataEntryTxtAmount = getFellow(component, "outwardChequeDataEntryTxtAmount", Textbox.class);
		outwardChequeDataEntryDtChequeDate = getFellow(component, "outwardChequeDataEntryDtChequeDate", Datebox.class);
		outwardChequeDataEntryTxtMicrCode = getFellow(component, "outwardChequeDataEntryTxtMicrCode", Textbox.class);
		outwardChequeDataEntryTxtPayeeAccount = getFellow(component, "outwardChequeDataEntryTxtPayeeAccount",
				Textbox.class);
		outwardChequeDataEntryTxtPayeeName = getFellow(component, "outwardChequeDataEntryTxtPayeeName", Textbox.class);
		outwardChequeDataEntryTxtDraweeName = getFellow(component, "outwardChequeDataEntryTxtDraweeName",
				Textbox.class);

		outwardChequeDataEntryProgress = getFellow(component, "outwardChequeDataEntryProgress", Progressmeter.class);

		outwardChequeDataEntryRejectModal = getFellow(component, "outwardChequeDataEntryRejectModal", Window.class);
		outwardChequeDataEntrySubmitConfirmModal = getFellow(component, "outwardChequeDataEntrySubmitConfirmModal",
				Window.class);
		outwardChequeDataEntrySubmitSuccessModal = getFellow(component, "outwardChequeDataEntrySubmitSuccessModal",
				Window.class);
		outwardChequeDataEntryMicrPendingModal = getFellow(component, "outwardChequeDataEntryMicrPendingModal",
				Window.class);

		if (outwardChequeDataEntryRejectModal != null) {
			outwardChequeDataEntryCmbRejectReason = getFellow(outwardChequeDataEntryRejectModal,
					"outwardChequeDataEntryCmbRejectReason", Combobox.class);
			outwardChequeDataEntryTxtRejectRemarks = getFellow(outwardChequeDataEntryRejectModal,
					"outwardChequeDataEntryTxtRejectRemarks", Textbox.class);
			outwardChequeDataEntryBtnCancelReject = getFellow(outwardChequeDataEntryRejectModal,
					"outwardChequeDataEntryBtnCancelReject", Button.class);
			outwardChequeDataEntryBtnConfirmReject = getFellow(outwardChequeDataEntryRejectModal,
					"outwardChequeDataEntryBtnConfirmReject", Button.class);
		}

		if (outwardChequeDataEntrySubmitConfirmModal != null) {
			outwardChequeDataEntryLblConfirmBatchId = getFellow(outwardChequeDataEntrySubmitConfirmModal,
					"outwardChequeDataEntryLblConfirmBatchId", Label.class);
			outwardChequeDataEntryLblConfirmTotal = getFellow(outwardChequeDataEntrySubmitConfirmModal,
					"outwardChequeDataEntryLblConfirmTotal", Label.class);
			outwardChequeDataEntryBtnCancelSubmit = getFellow(outwardChequeDataEntrySubmitConfirmModal,
					"outwardChequeDataEntryBtnCancelSubmit", Button.class);
			outwardChequeDataEntryBtnConfirmSubmit = getFellow(outwardChequeDataEntrySubmitConfirmModal,
					"outwardChequeDataEntryBtnConfirmSubmit", Button.class);
		}

		if (outwardChequeDataEntryMicrPendingModal != null) {
			outwardChequeDataEntryLblMicrPendingSubmitMessage = getFellow(outwardChequeDataEntryMicrPendingModal,
					"outwardChequeDataEntryLblMicrPendingSubmitMessage", Label.class);
			outwardChequeDataEntryBtnCloseMicrPending = getFellow(outwardChequeDataEntryMicrPendingModal,
					"outwardChequeDataEntryBtnCloseMicrPending", Button.class);
		}

		if (outwardChequeDataEntrySubmitSuccessModal != null) {
			outwardChequeDataEntryLblSubmitSuccess = getFellow(outwardChequeDataEntrySubmitSuccessModal,
					"outwardChequeDataEntryLblSubmitSuccess", Label.class);
			outwardChequeDataEntryLblSubmitSuccessDetails = getFellow(outwardChequeDataEntrySubmitSuccessModal,
					"outwardChequeDataEntryLblSubmitSuccessDetails", Label.class);
			outwardChequeDataEntryBtnGoToDataEntry = getFellow(outwardChequeDataEntrySubmitSuccessModal,
					"outwardChequeDataEntryBtnGoToDataEntry", Button.class);
		}

		hideModal(outwardChequeDataEntryRejectModal);
		hideModal(outwardChequeDataEntrySubmitConfirmModal);
		hideModal(outwardChequeDataEntryMicrPendingModal);
		hideModal(outwardChequeDataEntrySubmitSuccessModal);
	}

	@SuppressWarnings("unchecked")
	private <T extends Component> T getFellow(Component parent, String id, Class<T> clazz) {
		if (parent == null || id == null)
			return null;
		Component c = parent.getFellowIfAny(id);
		return clazz.isInstance(c) ? (T) c : null;
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

		addClick(outwardChequeDataEntryBtnTopPrevious, event -> previousCheque());
		addClick(outwardChequeDataEntryBtnTopNext, event -> nextCheque());

		addClick(outwardChequeDataEntryBtnImageToggle, event -> toggleImage());

		addClick(outwardChequeDataEntryBtnZoomOut, event -> zoomOut());
		addClick(outwardChequeDataEntryBtnZoomIn, event -> zoomIn());
		addClick(outwardChequeDataEntryBtnRotate, event -> rotateImage());
		addClick(outwardChequeDataEntryBtnReset, event -> resetImage());

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

	private interface EventAction {
		void execute(Event event) throws Exception;
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
		if (cheque != null && isChequeActionable(cheque)) {
			hasUnsavedChanges = true;
			updateActionButtons();
			updateNavigationButtons();
		}
	}

	private boolean isChequeActionable(OutwardCheque cheque) {
		if (cheque == null)
			return false;
		String status = validator.normalizeStatus(cheque.getChequeStatus());
		if (validator.isMicrPending(status) || validator.isMicrRejected(status) || validator.isRejectRequest(status)) {
			return false;
		}
		return true;
	}

	private void loadDataEntryData() {
		try {
			batchSubmitted = false;

			loadScanCheques();
			loadExistingCheques();
			mergeScanAndOutwardCheques();

			// Refresh batch status directly from DB to verify if it is returned
			refreshBatch();
			boolean batchStatusOnHold = outwardBatch != null
					&& STATUS_ON_HOLD.equalsIgnoreCase(outwardBatch.getBatchStatus());
			this.isReworkBatch = batchStatusOnHold || hasCheckerReturnedCheques();

			rebuildActiveQueue();
			updateBatchSummary();

			if (!activeQueue.isEmpty()) {
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

	private boolean hasCheckerReturnedCheques() {
		if (allBatchCheques == null)
			return false;
		for (OutwardCheque c : allBatchCheques) {
			if (c != null) {
				String st = validator.normalizeStatus(c.getChequeStatus());
				if (validator.isOnHold(st) || STATUS_MAKER_RETURNED.equals(st)) {
					return true;
				}
			}
		}
		return false;
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
		allBatchCheques = new ArrayList<>();
		if (isBlank(outwardBatchId))
			return;

		try {
			List<OutwardCheque> existing = outwardChequeService.getChequesByBatchId(outwardBatchId);
			if (existing != null)
				allBatchCheques.addAll(existing);
		} catch (Exception ignored) {
			allBatchCheques = new ArrayList<>();
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
		} else if (allBatchCheques != null && !allBatchCheques.isEmpty()) {
			merged.addAll(allBatchCheques);
		}

		allBatchCheques = merged;
	}

	private OutwardCheque findMatchingOutwardCheque(ScanCheque sc, int index) {
		if (allBatchCheques == null || allBatchCheques.isEmpty())
			return null;

		String scNumber = safeValue(sc.getChequeNumber()).trim();
		String scFront = safeValue(sc.getChequeImageFront()).trim();

		if (!scNumber.isEmpty()) {
			for (OutwardCheque oc : allBatchCheques) {
				if (oc != null && scNumber.equalsIgnoreCase(safeValue(oc.getChequeNumber()).trim())) {
					return oc;
				}
			}
		}

		if (!scFront.isEmpty()) {
			for (OutwardCheque oc : allBatchCheques) {
				if (oc != null && scFront.equalsIgnoreCase(safeValue(oc.getChequeImageFront()).trim())) {
					return oc;
				}
			}
		}

		if (index < allBatchCheques.size()) {
			OutwardCheque oc = allBatchCheques.get(index);
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

		String scanStatus = validator.normalizeStatus(scanCheque.getChequeStatus());
		if (validator.isMicrRejected(scanStatus)) {
			cheque.setChequeStatus(STATUS_MICR_REJECTED);
		} else if (validator.isMicrPending(scanStatus)) {
			cheque.setChequeStatus(STATUS_PENDING_MICR_REPAIR);
		} else {
			cheque.setChequeStatus(STATUS_PENDING_DATA_ENTRY);
		}

		return cheque;
	}

	/**
	 * STRICT QUEUE ISOLATION (MATCHING INWARD PATTERN): When the batch is on hold
	 * (rework mode), activeQueue ONLY loads cheques that were returned for
	 * modification (ON_HOLD / MAKER_RETURNED / PENDING_DATA_ENTRY). All
	 * already-verified cheques (PENDING_VERIFICATION) are completely excluded from
	 * the queue.
	 */
	private void rebuildActiveQueue() {
		activeQueue = new ArrayList<>();
		if (allBatchCheques == null)
			return;

		if (this.isReworkBatch) {
			for (OutwardCheque cheque : allBatchCheques) {
				if (cheque == null)
					continue;
				String status = validator.normalizeStatus(cheque.getChequeStatus());

				// Exclude already verified items when in rework mode
				if (STATUS_PENDING_VERIFICATION.equals(status)) {
					continue;
				}

				// Only load returned/rework items
				if (validator.isOnHold(status) || STATUS_MAKER_RETURNED.equals(status)
						|| STATUS_PENDING_DATA_ENTRY.equals(status) || status.isEmpty()) {
					activeQueue.add(cheque);
				}
			}
		} else {
			// Fresh batch: include all non-MICR-pending cheques
			for (OutwardCheque cheque : allBatchCheques) {
				if (cheque == null)
					continue;
				String status = validator.normalizeStatus(cheque.getChequeStatus());
				if (!validator.isMicrPending(status)) {
					activeQueue.add(cheque);
				}
			}
		}
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
		setChequeFieldsEditable(isChequeActionable(cheque));

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

	private boolean validateFormFields() {
		clearAllFieldHighlights();

		String chequeNumber = outwardChequeDataEntryTxtChequeNumber == null ? ""
				: safeValue(outwardChequeDataEntryTxtChequeNumber.getValue()).trim();
		String amountText = outwardChequeDataEntryTxtAmount == null ? ""
				: safeValue(outwardChequeDataEntryTxtAmount.getValue()).trim();
		BigDecimal amount = getAmountFromField();
		java.util.Date chequeDate = outwardChequeDataEntryDtChequeDate == null ? null
				: outwardChequeDataEntryDtChequeDate.getValue();
		String payeeAccount = outwardChequeDataEntryTxtPayeeAccount == null ? ""
				: safeValue(outwardChequeDataEntryTxtPayeeAccount.getValue()).trim();
		String payeeName = outwardChequeDataEntryTxtPayeeName == null ? ""
				: safeValue(outwardChequeDataEntryTxtPayeeName.getValue()).trim();
		String draweeName = outwardChequeDataEntryTxtDraweeName == null ? ""
				: safeValue(outwardChequeDataEntryTxtDraweeName.getValue()).trim();

		ValidationResult result = validator.validateChequeFields(chequeNumber, amountText, amount, chequeDate,
				payeeAccount, payeeName, draweeName);

		if (!result.isValid()) {
			Component component = outwardChequeDataEntryWin != null
					? outwardChequeDataEntryWin.getFellowIfAny(result.getFieldId())
					: null;
			highlightError(component, result.getErrorMessage());
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

	private void setChequeFieldsEditable(boolean editable) {
		setFieldState(outwardChequeDataEntryTxtChequeNumber, editable);
		setFieldState(outwardChequeDataEntryTxtAmount, editable);
		setFieldState(outwardChequeDataEntryDtChequeDate, editable);
		setFieldState(outwardChequeDataEntryTxtPayeeAccount, editable);
		setFieldState(outwardChequeDataEntryTxtPayeeName, editable);
		setFieldState(outwardChequeDataEntryTxtDraweeName, editable);

		if (outwardChequeDataEntryTxtMicrCode != null) {
			outwardChequeDataEntryTxtMicrCode.setReadonly(true);
		}
	}

	private void setFieldState(Textbox textbox, boolean editable) {
		if (textbox == null)
			return;
		textbox.setReadonly(!editable);
	}

	private void setFieldState(Datebox datebox, boolean editable) {
		if (datebox == null)
			return;
		datebox.setReadonly(!editable);
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

		String status = validator.normalizeStatus(cheque.getChequeStatus());
		setButtonEnabled(outwardChequeDataEntryBtnBack, true);

		if (validator.isMicrRejected(status)) {
			setChequeFieldsEditable(false);
			if (outwardChequeDataEntryBtnApprove != null) {
				outwardChequeDataEntryBtnApprove.setLabel("Save Cheque");
				outwardChequeDataEntryBtnApprove.setDisabled(false);
			}
			disableButton(outwardChequeDataEntryBtnReject);
			disableButton(outwardChequeDataEntryBtnResetItem);
			return;
		}

		if (validator.isMicrPending(status)) {
			setChequeFieldsEditable(false);
			disableButton(outwardChequeDataEntryBtnApprove);
			disableButton(outwardChequeDataEntryBtnReject);
			disableButton(outwardChequeDataEntryBtnResetItem);
			return;
		}

		if (validator.isRejectRequest(status)) {
			setChequeFieldsEditable(false);
			disableButton(outwardChequeDataEntryBtnResetItem);
			disableButton(outwardChequeDataEntryBtnReject);
			disableButton(outwardChequeDataEntryBtnApprove);
			return;
		}

		// Cheque needs action (ON_HOLD / PENDING_DATA_ENTRY) -> Enable Approve Cheque
		if (outwardChequeDataEntryBtnApprove != null) {
			outwardChequeDataEntryBtnApprove.setLabel("Approve Cheque");
			// If already saved as MAKER_RETURNED, only enable if Maker edited a field
			boolean alreadySavedInThisSession = STATUS_MAKER_RETURNED.equals(status);
			outwardChequeDataEntryBtnApprove.setDisabled(alreadySavedInThisSession && !hasUnsavedChanges);
		}

		if (outwardChequeDataEntryBtnReject != null) {
			outwardChequeDataEntryBtnReject.setDisabled(false);
		}

		if (outwardChequeDataEntryBtnResetItem != null) {
			outwardChequeDataEntryBtnResetItem.setDisabled(!hasUnsavedChanges);
		}
	}

	private void saveCurrentCheque() {
		OutwardCheque cheque = getCurrentCheque();
		if (cheque == null)
			return;

		String status = validator.normalizeStatus(cheque.getChequeStatus());

		if (validator.isMicrRejected(status)) {
			saveMicrRejectedCheque(cheque);
			return;
		}

		if (!validateFormFields()) {
			return;
		}

		try {
			populateOutwardChequeFromFields(cheque);
			cheque.setOutwardBatchId(outwardBatchId);

			// Strict Lifecycle Rule:
			// If batch is in rework (isReworkBatch == true) OR cheque was ON_HOLD, persist
			// as MAKER_RETURNED.
			// If normal fresh batch, persist as PENDING_VERIFICATION.
			boolean isReturned = this.isReworkBatch || validator.isOnHold(status)
					|| STATUS_MAKER_RETURNED.equals(status);
			String targetStatus = isReturned ? STATUS_MAKER_RETURNED : STATUS_PENDING_VERIFICATION;
			cheque.setChequeStatus(targetStatus);

			String batchForSave = !isBlank(scannedBatchId) ? scannedBatchId : outwardBatchId;
			OutwardCheque saved = outwardChequeService.saveMakerCheque(batchForSave, cheque);

			if (saved == null) {
				throw new IllegalStateException("Unable to save cheque data.");
			}

			cheque.setOutwardChequeId(saved.getOutwardChequeId());
			cheque.setChequeStatus(targetStatus);
			syncChequeToFullList(saved);

			hasUnsavedChanges = false;
			refreshBatch();

			loadCurrentCheque();
			updateSubmitButton();

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

			cheque.setOutwardChequeId(saved.getOutwardChequeId());
			cheque.setChequeStatus(STATUS_REJECT_REQUEST);
			syncChequeToFullList(saved);

			hasUnsavedChanges = false;
			refreshBatch();
			loadCurrentCheque();
			updateSubmitButton();

		} catch (Exception exception) {
			showStatus("Unable to save MICR rejected cheque: " + safeExceptionMessage(exception));
		}
	}

	private void resetCurrentItem() {
		OutwardCheque cheque = getCurrentCheque();
		if (cheque == null)
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
		}
	}

	private void updateNavigationButtons() {
		if (outwardChequeDataEntryBtnTopPrevious == null || outwardChequeDataEntryBtnTopNext == null)
			return;

		if (activeQueue == null || activeQueue.isEmpty()) {
			outwardChequeDataEntryBtnTopPrevious.setDisabled(true);
			outwardChequeDataEntryBtnTopNext.setDisabled(true);
			return;
		}

		boolean canGoBack = (currentChequeIndex > 0) && !hasUnsavedChanges;
		outwardChequeDataEntryBtnTopPrevious.setDisabled(!canGoBack);

		boolean canGoForward = (currentChequeIndex < activeQueue.size() - 1) && !hasUnsavedChanges;
		outwardChequeDataEntryBtnTopNext.setDisabled(!canGoForward);
	}

	private void updateSummary() {
		int totalInQueue = activeQueue.size();
		int position = currentChequeIndex + 1;

		if (outwardChequeDataEntryLblTopRecord != null) {
			if (position > 0 && totalInQueue > 0) {
				outwardChequeDataEntryLblTopRecord.setValue(position + " of " + totalInQueue);
			} else {
				outwardChequeDataEntryLblTopRecord.setValue("0 of 0");
			}
		}

		if (outwardChequeDataEntryLblRecord != null) {
			if (this.isReworkBatch) {
				long resolvedRework = activeQueue.stream()
						.filter(c -> STATUS_MAKER_RETURNED.equals(validator.normalizeStatus(c.getChequeStatus()))
								|| validator.isRejectRequest(c.getChequeStatus()))
						.count();
				outwardChequeDataEntryLblRecord.setValue(resolvedRework + "/" + totalInQueue);
			} else {
				int completed = countCompletedMakerCheques();
				outwardChequeDataEntryLblRecord.setValue(completed + "/" + getBatchTotal());
			}
		}
	}

	private void updateBatchSummary() {
		String batch = !isBlank(outwardBatchId) ? outwardBatchId : scannedBatchId;
		if (outwardChequeDataEntryLblBatchId != null) {
			outwardChequeDataEntryLblBatchId.setValue(isBlank(batch) ? "-" : batch);
		}

		if (outwardChequeDataEntryLblTotal != null) {
			outwardChequeDataEntryLblTotal
					.setValue(String.valueOf(this.isReworkBatch ? activeQueue.size() : getBatchTotal()));
		}

		int percent = 0;
		if (this.isReworkBatch && !activeQueue.isEmpty()) {
			long resolvedRework = activeQueue.stream()
					.filter(c -> STATUS_MAKER_RETURNED.equals(validator.normalizeStatus(c.getChequeStatus()))
							|| validator.isRejectRequest(c.getChequeStatus()))
					.count();
			percent = (int) Math.round(((double) resolvedRework / activeQueue.size()) * 100);
		} else {
			int total = getBatchTotal();
			int completed = countCompletedMakerCheques();
			percent = total <= 0 ? 0 : (completed * 100) / total;
		}

		if (outwardChequeDataEntryProgress != null) {
			outwardChequeDataEntryProgress.setValue(percent);
		}
	}

	private int getBatchTotal() {
		if (outwardBatch != null && outwardBatch.getActualChequeCount() > 0) {
			return outwardBatch.getActualChequeCount();
		}
		if (allBatchCheques != null && !allBatchCheques.isEmpty()) {
			return allBatchCheques.size();
		}
		return scanChequeList == null ? 0 : scanChequeList.size();
	}

	private int countCompletedMakerCheques() {
		int count = 0;
		if (allBatchCheques == null)
			return 0;
		for (OutwardCheque cheque : allBatchCheques) {
			if (cheque == null)
				continue;
			String status = validator.normalizeStatus(cheque.getChequeStatus());
			if (STATUS_PENDING_VERIFICATION.equals(status) || STATUS_MAKER_RETURNED.equals(status)
					|| validator.isRejectRequest(status)) {
				count++;
			}
		}
		return count;
	}

	private int countMicrPending() {
		int count = 0;
		if (allBatchCheques == null)
			return 0;
		for (OutwardCheque cheque : allBatchCheques) {
			if (cheque != null && validator.isMicrPending(cheque.getChequeStatus())) {
				count++;
			}
		}
		return count;
	}

	private void updateSubmitButton() {
		if (outwardChequeDataEntryBtnSubmit == null)
			return;
		boolean ready = false;

		if (this.isReworkBatch) {
			long resolvedRework = activeQueue.stream()
					.filter(c -> STATUS_MAKER_RETURNED.equals(validator.normalizeStatus(c.getChequeStatus()))
							|| validator.isRejectRequest(c.getChequeStatus()))
					.count();
			ready = !activeQueue.isEmpty() && resolvedRework == activeQueue.size() && !hasUnsavedChanges
					&& !batchSubmitted;
		} else {
			ready = validator.isBatchReadyForChecker(allBatchCheques, getBatchTotal(), batchSubmitted,
					hasUnsavedChanges);
		}

		outwardChequeDataEntryBtnSubmit.setDisabled(!ready);
	}

	/**
	 * SINGLE DYNAMIC ALERT BANNER CONTROLLER (INWARD PATTERN): Accurately detects
	 * whether the current item is returned by Checker. When the batch is in rework
	 * (isReworkBatch) and the cheque is pending modification, it displays the
	 * orange Checker Return alert banner.
	 */
	private void updateCurrentChequeStatus(OutwardCheque cheque) {
		hideBanner();
		if (cheque == null)
			return;

		String status = validator.normalizeStatus(cheque.getChequeStatus());

		// 1. Orange Box: Returned by Checker (ON_HOLD / SEND_BACK or batch is in
		// Rework)
		if (validator.isOnHold(status) || (this.isReworkBatch && !STATUS_MAKER_RETURNED.equals(status))) {
			if (outwardChequeDataEntryLblChequeStatus != null) {
				outwardChequeDataEntryLblChequeStatus.setValue("Sent Back");
			}

			String details = getRejectionDetails(cheque);
			String message = details.isEmpty()
					? "This cheque was returned by Checker for modification. Please verify and save."
					: details;

			showBanner("cts-alert-box-single state-orange", "CHECKER RETURNED FOR MODIFICATION", message);
			setChequeFieldsEditable(true);
			updateActionButtons();
			updateNavigationButtons();
			return;
		}

		if (outwardChequeDataEntryLblChequeStatus != null) {
			outwardChequeDataEntryLblChequeStatus.setValue(validator.formatDisplayStatus(status));
		}

		// 2. Green Box: Corrected by Maker (MAKER_RETURNED)
		if (STATUS_MAKER_RETURNED.equals(status)) {
			showBanner("cts-alert-box-single state-green", "MAKER RETURNED",
					"Cheque corrected and marked as Maker Returned.");
			setChequeFieldsEditable(true);
			updateActionButtons();
			updateNavigationButtons();
			return;
		}

		// 3. Green Box: Freshly Verified (PENDING_VERIFICATION)
		if (STATUS_PENDING_VERIFICATION.equals(status)) {
			showBanner("cts-alert-box-single state-green", "CHEQUE VERIFIED",
					"Cheque verified and marked as Pending Verification.");
			setChequeFieldsEditable(!this.isReworkBatch);
			updateActionButtons();
			updateNavigationButtons();
			return;
		}

		// 4. Red Box: MICR Rejected
		if (validator.isMicrRejected(status)) {
			showBanner("cts-alert-box-single state-red", "MICR REJECTED",
					"This cheque was rejected during MICR. Click 'Save Cheque' to record as Reject Request.");
			setChequeFieldsEditable(false);
			updateActionButtons();
			updateNavigationButtons();
			return;
		}

		// 5. Red Box: Reject Request
		if (validator.isRejectRequest(status)) {
			String details = getRejectionDetails(cheque);
			showBanner("cts-alert-box-single state-red", "REJECTION REQUEST",
					details.isEmpty() ? "Cheque marked for rejection." : details);
			setChequeFieldsEditable(false);
			updateActionButtons();
			updateNavigationButtons();
			return;
		}

		// 6. Orange Box: Pending MICR Repair
		if (validator.isMicrPending(status)) {
			showBanner("cts-alert-box-single state-orange", "AWAITING MICR REPAIR",
					"All fields are disabled until MICR is resolved.");
			setChequeFieldsEditable(false);
			updateActionButtons();
			updateNavigationButtons();
			return;
		}

		setChequeFieldsEditable(true);
		updateActionButtons();
		updateNavigationButtons();
	}

	private void showBanner(String sclass, String title, String message) {
		if (dataEntryAlertBox != null) {
			dataEntryAlertBox.setVisible(true);
			dataEntryAlertBox.setSclass(sclass);
		}
		if (lblDataEntryAlertTitle != null) {
			lblDataEntryAlertTitle.setValue(title);
		}
		if (lblDataEntryAlertMessage != null) {
			lblDataEntryAlertMessage.setValue(message);
		}
	}

	private void hideBanner() {
		if (dataEntryAlertBox != null) {
			dataEntryAlertBox.setVisible(false);
		}
	}

	private String getRejectionDetails(OutwardCheque cheque) {
		if (cheque == null)
			return "";
		String chequeId = safeValue(cheque.getOutwardChequeId()).trim();
		if (chequeId.isEmpty())
			return "";

		try {
			OutwardChequeRequest request = outwardChequeService.getRejectionRequestByChequeId(chequeId);
			if (request != null) {
				String reason = safeValue(request.getReason()).trim();
				String remarks = safeValue(request.getRemarks()).trim();
				if (!remarks.isEmpty())
					return reason + " (Remarks: " + remarks + ")";
				return reason;
			}
		} catch (Exception ignored) {
		}
		return "";
	}

	private void openRejectWindow() {
		OutwardCheque cheque = getCurrentCheque();
		if (cheque == null || !isChequeActionable(cheque)) {
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
		if (cheque == null || !isChequeActionable(cheque)) {
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

		if (reason.isEmpty()) {
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
			boolean saved = outwardChequeService.saveMakerRejectionRequest(batchForSave, cheque, reasonId, reason,
					remarks);

			if (!saved) {
				closeRejectWindow();
				showStatus("Unable to create rejection request.");
				return;
			}

			cheque.setChequeStatus(STATUS_REJECT_REQUEST);
			syncChequeToFullList(cheque);

			hasUnsavedChanges = false;
			closeRejectWindow();
			refreshBatch();

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
				String name = safeValue(reason.getRejectedReasonName()).trim();
				if (name.isEmpty())
					name = safeValue(reason.getRejectedReasonCode()).trim();

				Comboitem item = new Comboitem();
				item.setLabel(name);
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
				setLabel(outwardChequeDataEntryLblMicrPendingSubmitMessage,
						"Batch cannot be submitted yet. There are still " + micrPending
								+ " cheques pending in MICR Repair.");
				showModalSafely(outwardChequeDataEntryMicrPendingModal);
			}
			return;
		}

		if (!validator.isBatchReadyForChecker(allBatchCheques, getBatchTotal(), batchSubmitted, hasUnsavedChanges)) {
			showStatus("All cheques must be completed before submission.");
			return;
		}

		int total = this.isReworkBatch ? activeQueue.size() : getBatchTotal();
		showSubmitConfirmation(total, countCompletedMakerCheques());
	}

	private void showSubmitConfirmation(int total, int completed) {
		if (outwardChequeDataEntrySubmitConfirmModal == null) {
			completeBatchSubmission();
			return;
		}

		setLabel(outwardChequeDataEntryLblConfirmBatchId, isBlank(outwardBatchId) ? "-" : outwardBatchId);
		setLabel(outwardChequeDataEntryLblConfirmTotal,
				(this.isReworkBatch ? activeQueue.size() : completed) + " / " + total);

		showModalSafely(outwardChequeDataEntrySubmitConfirmModal);
	}

	private void completeBatchSubmission() {
		try {
			int total = getBatchTotal();
			if (total <= 0) {
				showStatus("No cheques are available for submission.");
				return;
			}

			if (!validator.isBatchReadyForChecker(allBatchCheques, total, batchSubmitted, hasUnsavedChanges)) {
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

			String notificationMessage = validator.buildCheckerNotificationMessage(outwardBatchId, allBatchCheques);

			boolean notificationSent = false;
			try {
				notificationSent = notificationService.sendNotification(NOTIFICATION_ROLE_OUTWARD_CHECKER, null,
						notificationMessage);
			} catch (Exception ignored) {
			}

			showSubmissionSuccess(notificationSent, total);

		} catch (Exception exception) {
			showStatus("Unable to submit batch to Checker: " + safeExceptionMessage(exception));
		}
	}

	private void showSubmissionSuccess(boolean notificationSent, int total) {
		String details = "Total Cheques: " + total + " / " + total + " | Batch Status: Pending Checker Process";
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

		if (outwardChequeDataEntryLblTopRecord != null)
			outwardChequeDataEntryLblTopRecord.setValue("0 of 0");

		if (outwardChequeDataEntryLblImageEmpty != null) {
			int pending = countMicrPending();
			outwardChequeDataEntryLblImageEmpty.setValue(pending + " cheque(s) awaiting MICR processing.");
			outwardChequeDataEntryLblImageEmpty.setVisible(true);
		}

		if (outwardChequeDataEntryChequeImage != null)
			outwardChequeDataEntryChequeImage.setVisible(false);

		showBanner("cts-alert-box-single state-orange", "AWAITING MICR REPAIR",
				"All fields are disabled until MICR is resolved.");
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
		if (countMicrPending() == 0)
			return;

		try {
			List<OutwardCheque> latest = outwardChequeService.getChequesByBatchId(outwardBatchId);
			if (latest == null)
				return;

			allBatchCheques = new ArrayList<>(latest);
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
		if (cheque == null || allBatchCheques == null)
			return;

		String id = safeValue(cheque.getOutwardChequeId()).trim();
		for (int i = 0; i < allBatchCheques.size(); i++) {
			OutwardCheque item = allBatchCheques.get(i);
			if (item != null && !id.isEmpty() && id.equalsIgnoreCase(safeValue(item.getOutwardChequeId()).trim())) {
				allBatchCheques.set(i, cheque);
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
		setLabel(outwardChequeDataEntryLblRecord, "0/0");
		setLabel(outwardChequeDataEntryLblTopRecord, "0 of 0");
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
		showValidation(message);
	}

	private void showValidation(String message) {
		if (outwardChequeDataEntryLblValidationStatus == null) {
			return;
		}
		outwardChequeDataEntryLblValidationStatus.setValue(message);
		outwardChequeDataEntryLblValidationStatus.setVisible(true);
	}

	private void hideValidation() {
		if (outwardChequeDataEntryLblValidationStatus != null) {
			outwardChequeDataEntryLblValidationStatus.setVisible(false);
		}
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