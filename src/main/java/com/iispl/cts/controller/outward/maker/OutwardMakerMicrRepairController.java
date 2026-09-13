package com.iispl.cts.controller.outward.maker;

import java.util.ArrayList;

import java.util.List;

import org.zkoss.zk.ui.Component;

import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;

import org.zkoss.zk.ui.Executions;

import org.zkoss.zk.ui.select.annotation.Wire;

import org.zkoss.zk.ui.util.Clients;

import org.zkoss.zk.ui.util.GenericForwardComposer;

import org.zkoss.zul.Button;

import org.zkoss.zul.Combobox;

import org.zkoss.zul.Div;

import org.zkoss.zul.Image;

import org.zkoss.zul.Include;

import org.zkoss.zul.Label;

import org.zkoss.zul.Comboitem;

import org.zkoss.zul.Textbox;

import org.zkoss.zul.Window;

import com.iispl.cts.dto.MicrRepairChequeDTO;

import com.iispl.cts.entity.outward.RejectedReason;

import com.iispl.cts.service.outward.OutwardMakerService;

import com.iispl.cts.serviceimpl.outward.OutwardMakerServiceImpl;

public class OutwardMakerMicrRepairController extends GenericForwardComposer<Window> {

	private static final long serialVersionUID = 1L;

	private final OutwardMakerService outwardMakerService;

	// =========================================================

	// MAIN COMPONENTS

	// =========================================================

	@Wire

	private Window winMicrRepair;

	@Wire

	private Label lblBatchId;

	@Wire

	private Label lblMicrRepairCount;

	@Wire

	private Label lblCompletedCount;

	@Wire

	private Label lblRemainingCount;

	@Wire

	private Label lblChequePosition;

	@Wire

	private Button btnPrevious;

	@Wire

	private Button btnNext;

	@Wire

	private Button btnBackToList;

	// =========================================================

	// IMAGE COMPONENTS

	// =========================================================

	@Wire

	private Image imgCheque;

	@Wire

	private Label lblChequeImageTitle;

	@Wire

	private Label lblImageRecordPosition;

	@Wire

	private Label lblImagePosition;

	@Wire

	private Label lblChequeImagePlaceholder;

	@Wire

	private Button btnZoom;

	@Wire

	private Button btnZoomOut;

	@Wire

	private Button btnZoomReset;

	@Wire

	private Button btnRotate;

	@Wire

	private Button btnImageToggle;

	@Wire

	private Button btnImagePrevious;

	@Wire

	private Button btnImageNext;

	// =========================================================

	// MICR DETAILS

	// =========================================================

	@Wire

	private Label lblChequeStatus;

	@Wire

	private Textbox txtChequeNumber;

	@Wire

	private Textbox txtCityCode;

	@Wire

	private Textbox txtBankCode;

	@Wire

	private Textbox txtBranchCode;

	@Wire

	private Textbox txtCurrentMicr;

	@Wire

	private Textbox txtCorrectedMicr;

	@Wire

	private Label lblMicrValidationMessage;

	// =========================================================

	// CHECKER RETURN / REJECTION INFORMATION

	// =========================================================

	@Wire

	private Div divCheckerReturnInformation;

	@Wire

	private Label lblCheckerReasonId;

	@Wire

	private Label lblCheckerReason;

	@Wire

	private Label lblCheckerRemarks;

	// =========================================================

	// ACTION BUTTONS

	// =========================================================

	@Wire

	private Button btnRejectRequest;

	@Wire

	private Button btnSaveNext;

	@Wire

	private Button btnSubmit;

	// =========================================================

	// PROGRESS

	// =========================================================

	@Wire

	private Label lblProgressText;

	@Wire

	private org.zkoss.zul.Progressmeter prgMicrRepair;

	// =========================================================

	// REJECT POPUP

	// =========================================================

	@Wire

	private Window winRejectRequest;

	@Wire

	private Combobox cmbRejectReason;

	@Wire

	private Textbox txtRejectRemarks;

	@Wire

	private Button btnCancelReject;

	@Wire

	private Button btnProceedReject;

	// =========================================================

	// SAVE CONFIRMATION POPUP

	// =========================================================

	@Wire

	private Window winConfirmSave;

	@Wire

	private Label lblConfirmSaveMessage;

	@Wire

	private Button btnCancelSave;

	@Wire

	private Button btnConfirmSave;

	// =========================================================

	// INFORMATION POPUP

	// =========================================================

	@Wire

	private Window winMicrInformation;

	@Wire

	private Label lblMicrInformationMessage;

	@Wire

	private Button btnCloseMicrInformation;

	// =========================================================

	// DATA

	// =========================================================

	private List<MicrRepairChequeDTO> micrRepairCheques = new ArrayList<MicrRepairChequeDTO>();

	/*
	 * 
	 * source is deliberately NOT part of MicrRepairChequeDTO.
	 *
	 * 
	 * 
	 * It remains page/request context and continues to come from the existing
	 * 
	 * Include / URL flow.
	 * 
	 */

	private String source;

	private String batchId;

	/*
	 * 
	 * Optional chequeId used when opening/resuming a particular cheque.
	 * 
	 */

	private String chequeId;

	private int currentIndex = 0;

	// =========================================================

	// STATUS VALUES

	// =========================================================

	private static final String STATUS_PENDING_MICR_REPAIR = "PENDING_MICR_REPAIR";

	private static final String STATUS_MICR_REPAIRED = "MICR_REPAIRED";

	private static final String STATUS_MICR_REJECTED = "MICR_REJECTED";

	private static final String STATUS_PENDING_DATA_ENTRY = "PENDING_DATA_ENTRY";

	private static final String STATUS_REJECT_REQUESTED = "REJECT_REQUESTED";

	// =========================================================

	// IMAGE STATE

	// =========================================================

	private boolean showingBackImage = false;

	private double zoomLevel = 1.0;

	private int rotation = 0;

	/*
	 * 
	 * Image paths are not part of MicrRepairChequeDTO.
	 *
	 * 
	 * 
	 * They therefore remain empty until the separate image retrieval flow is
	 * 
	 * connected.
	 * 
	 */

	private String frontImagePath = "";

	private String backImagePath = "";

	// =========================================================

	// CONSTRUCTOR

	// =========================================================

	public OutwardMakerMicrRepairController() {

		outwardMakerService = new OutwardMakerServiceImpl();

	}

	// =========================================================

	// AFTER COMPOSE

	// =========================================================

	@Override

	public void doAfterCompose(Window window) throws Exception {

		super.doAfterCompose(window);

		System.out.println("[MICR-TRACE] doAfterCompose: super COMPLETE");

		// The reject popup is a nested Window/ID space. Resolve its components
		// explicitly so event handling does not depend on nested ID-space forwarding.
		if (winRejectRequest == null) {
			Component popup = window.getFellowIfAny("winRejectRequest", true);
			if (popup instanceof Window) {
				winRejectRequest = (Window) popup;
			}
		}

		if (winRejectRequest != null) {
			if (cmbRejectReason == null) {
				Component component = winRejectRequest.getFellowIfAny("cmbRejectReason");
				if (component instanceof Combobox) {
					cmbRejectReason = (Combobox) component;
				}
			}

			if (txtRejectRemarks == null) {
				Component component = winRejectRequest.getFellowIfAny("txtRejectRemarks");
				if (component instanceof Textbox) {
					txtRejectRemarks = (Textbox) component;
				}
			}

			if (btnCancelReject == null) {
				Component component = winRejectRequest.getFellowIfAny("btnCancelReject");
				if (component instanceof Button) {
					btnCancelReject = (Button) component;
				}
			}

			if (btnProceedReject == null) {
				Component component = winRejectRequest.getFellowIfAny("btnProceedReject");
				if (component instanceof Button) {
					btnProceedReject = (Button) component;
				}
			}

			registerRejectPopupListeners();
		}

		loadParameters(window);

		// -----------------------------------------------------

		// Validate source

		// -----------------------------------------------------

		if (source == null || source.trim().isEmpty()) {

			showError("MICR repair source is missing.");

			return;

		}

		source = source.trim().toUpperCase();

		if (!"SCAN".equals(source) && !"OUTWARD".equals(source)) {

			showError("Invalid MICR repair source.");

			return;

		}

		// -----------------------------------------------------

		// Validate batch

		// -----------------------------------------------------

		if (batchId == null || batchId.trim().isEmpty()) {

			showError("Batch ID is missing.");

			return;

		}

		batchId = batchId.trim();

		lblBatchId.setValue(batchId);

		loadRejectedReasons();

		// Submit is always disabled until all cheques

		// have been individually processed.

		btnSubmit.setDisabled(true);

		// -----------------------------------------------------

		// Load DTO list

		// -----------------------------------------------------

		loadMicrRepairCheques();

		int totalCheques = getTotalCheques();

		if (totalCheques == 0) {

			showError("No MICR repair cheques found for batch " + batchId);

			btnSaveNext.setDisabled(true);

			btnRejectRequest.setDisabled(true);

			btnSubmit.setDisabled(true);

			btnPrevious.setDisabled(true);

			btnNext.setDisabled(true);

			updateProgress();

			return;

		}

		// -----------------------------------------------------

		// Determine opening position

		// -----------------------------------------------------

		currentIndex = findOpeningChequeIndex();

		if (currentIndex < 0) {

			currentIndex = 0;

		}

		loadCurrentCheque();

		updateSubmitButton();

	}

	// =========================================================

	// =========================================================
	// REJECT POPUP EVENT LISTENERS
	// =========================================================
	private void registerRejectPopupListeners() {
		if (btnCancelReject != null) {
			btnCancelReject.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					onClick$btnCancelReject(event);
				}
			});
		}

		if (btnProceedReject != null) {
			btnProceedReject.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					onClick$btnProceedReject(event);
				}
			});
		}

		System.out.println("[MICR-TRACE] Reject popup listeners registered. " + "Cancel=" + (btnCancelReject != null)
				+ ", Proceed=" + (btnProceedReject != null));
	}

	// LOAD PARAMETERS

	// =========================================================

	/*
	 * 
	 * IMPORTANT: Existing source handling is preserved.
	 * 
	 */

	private void loadParameters(Window window) {

		Component parent = window.getParent();

		while (parent != null && !(parent instanceof Include)) {

			parent = parent.getParent();

		}

		if (parent instanceof Include) {

			Include include = (Include) parent;

			Object sourceAttribute = include.getAttribute("MICR_REPAIR_SOURCE");

			Object batchAttribute = include.getAttribute("MICR_REPAIR_BATCH_ID");

			Object chequeAttribute = include.getAttribute("MICR_REPAIR_CHEQUE_ID");

			if (sourceAttribute != null) {

				source = sourceAttribute.toString();

			}

			if (batchAttribute != null) {

				batchId = batchAttribute.toString();

			}

			if (chequeAttribute != null) {

				chequeId = chequeAttribute.toString();

			}

		}

		// -----------------------------------------------------

		// Existing direct URL fallback

		// -----------------------------------------------------

		if (source == null || source.trim().isEmpty()) {

			source = Executions.getCurrent().getParameter("source");

		}

		if (batchId == null || batchId.trim().isEmpty()) {

			batchId = Executions.getCurrent().getParameter("batchId");

		}

		if (chequeId == null || chequeId.trim().isEmpty()) {

			chequeId = Executions.getCurrent().getParameter("chequeId");

		}

		System.out.println("MICR REPAIR source = [" + source + "]");

		System.out.println("MICR REPAIR batchId = [" + batchId + "]");

		System.out.println("MICR REPAIR chequeId = [" + chequeId + "]");

	}

	// =========================================================

	// =========================================================

	// LOAD REJECTED REASONS

	// =========================================================

	private void loadRejectedReasons() {

		try {

			List<RejectedReason> reasons =

					outwardMakerService.getRejectedReasons();

			cmbRejectReason.getItems().clear();

			if (reasons == null) {

				return;

			}

			for (RejectedReason reason : reasons) {

				Comboitem item = new Comboitem();

				item.setLabel(safe(reason.getRejectedReasonName()));

				item.setValue(reason.getRejectedReasonId());

				cmbRejectReason.appendChild(item);

			}

			System.out.println("Rejected reasons loaded = " + reasons.size());

		} catch (Exception e) {

			e.printStackTrace();

			showError("Unable to load rejection reasons.");

		}

	}

	// LOAD MICR REPAIR CHEQUES

	// =========================================================

	// =========================================================

	// LOAD MICR REPAIR CHEQUES

	// =========================================================

	private void loadMicrRepairCheques() {

		System.out.println("\n========== LOAD MICR REPAIR CHEQUES ==========");

		System.out.println("Source = [" + source + "]");

		System.out.println("Batch ID = [" + batchId + "]");

		try {

			if ("SCAN".equals(source)) {

				System.out.println("Calling SCAN cheque service...");

				micrRepairCheques =

						outwardMakerService.getScanMicrRepairCheques(batchId);

			} else {

				System.out.println("Calling OUTWARD cheque service...");

				micrRepairCheques =

						outwardMakerService.getOutwardMicrRepairCheques(batchId);

			}

			if (micrRepairCheques == null) {

				System.out.println("Cheque list returned = NULL");

				micrRepairCheques =

						new ArrayList<MicrRepairChequeDTO>();

			}

			System.out.println("Cheque list returned = "

					+ micrRepairCheques.size());

			for (MicrRepairChequeDTO cheque : micrRepairCheques) {

				System.out.println(

						"Cheque ID = [" + cheque.getChequeId() + "]"

								+ " | Cheque No = [" + cheque.getChequeNumber() + "]"

								+ " | Status = [" + cheque.getChequeStatus() + "]");

			}

			System.out.println(

					"========== LOAD MICR REPAIR CHEQUES COMPLETE ==========\n");

		} catch (Exception e) {

			System.out.println(

					"========== LOAD MICR REPAIR CHEQUES ERROR ==========");

			e.printStackTrace();

			micrRepairCheques =

					new ArrayList<MicrRepairChequeDTO>();

			showError("Unable to load MICR repair cheques.");

		}

	}

	// =========================================================

	// GET TOTAL CHEQUES

	// =========================================================

	private int getTotalCheques() {

		if (micrRepairCheques == null) {

			return 0;

		}

		return micrRepairCheques.size();

	}

	// =========================================================

	// FIND OPENING CHEQUE

	// =========================================================

	private int findOpeningChequeIndex() {

		if (micrRepairCheques == null || micrRepairCheques.isEmpty()) {

			return -1;

		}

		// -----------------------------------------------------

		// Specific cheque requested

		// -----------------------------------------------------

		if (chequeId != null && !chequeId.trim().isEmpty()) {

			String requestedId = chequeId.trim();

			for (int i = 0; i < micrRepairCheques.size(); i++) {

				MicrRepairChequeDTO cheque = micrRepairCheques.get(i);

				if (cheque == null) {

					continue;

				}

				if (requestedId.equals(safe(cheque.getChequeId()))) {

					return i;

				}

			}

		}

		// -----------------------------------------------------

		// Resume at first unfinished cheque

		// -----------------------------------------------------

		for (int i = 0; i < micrRepairCheques.size(); i++) {

			MicrRepairChequeDTO cheque = micrRepairCheques.get(i);

			if (cheque == null) {

				continue;

			}

			if (isPendingMicrRepair(cheque)) {

				return i;

			}

		}

		return 0;

	}

	// =========================================================

	// LOAD CURRENT CHEQUE

	// =========================================================

	private void loadCurrentCheque() {

		System.out.println("\n========== LOAD CURRENT CHEQUE ==========");

		System.out.println("[MICR-TRACE] loadCurrentCheque ENTER");

		System.out.println("Current index = " + currentIndex);

		System.out.println("Total cheques = " + (micrRepairCheques == null ? "NULL" : micrRepairCheques.size()));

		System.out.println("[MICR-TRACE] 01. Calling getTotalCheques()");

		int totalCheques = getTotalCheques();

		System.out.println("[MICR-TRACE] 01. getTotalCheques COMPLETE = " + totalCheques);

		if (currentIndex < 0 || currentIndex >= totalCheques) {

			System.out.println("[MICR-TRACE] INVALID currentIndex - returning");

			return;

		}

		System.out.println("[MICR-TRACE] 02. Resetting screen state");

		txtCityCode.setValue("");

		txtBankCode.setValue("");

		txtBranchCode.setValue("");

		txtCurrentMicr.setValue("");

		txtCorrectedMicr.setValue("");

		lblMicrValidationMessage.setVisible(false);

		lblMicrValidationMessage.setValue("");

		divCheckerReturnInformation.setVisible(false);

		lblCheckerReasonId.setValue("");

		lblCheckerReason.setValue("");

		lblCheckerRemarks.setValue("");

		showingBackImage = false;

		zoomLevel = 1.0;

		rotation = 0;

		frontImagePath = "";

		backImagePath = "";

		System.out.println("[MICR-TRACE] 02. Screen state reset COMPLETE");

		System.out.println("[MICR-TRACE] 03. Reading DTO");

		MicrRepairChequeDTO cheque = micrRepairCheques.get(currentIndex);

		if (cheque == null) {

			System.out.println("[MICR-TRACE] DTO IS NULL - returning");

			return;

		}

		System.out.println("Current cheque ID = [" + cheque.getChequeId() + "]");

		System.out.println("Current cheque number = [" + cheque.getChequeNumber() + "]");

		System.out.println("Current cheque status = [" + cheque.getChequeStatus() + "]");

		System.out.println("[MICR-TRACE] 04. Calling populateCheque()");

		populateCheque(cheque);

		System.out.println("[MICR-TRACE] 04. populateCheque COMPLETE");

		System.out.println("[MICR-TRACE] 05. Calling updateProgress()");

		updateProgress();

		System.out.println("[MICR-TRACE] 05. updateProgress COMPLETE");

		System.out.println("[MICR-TRACE] 06. Calling updateNavigationButtons()");

		updateNavigationButtons();

		System.out.println("[MICR-TRACE] 06. updateNavigationButtons COMPLETE");

		System.out.println("[MICR-TRACE] 07. Calling updateActionButtons()");

		updateActionButtons();

		System.out.println("[MICR-TRACE] 07. updateActionButtons COMPLETE");

		System.out.println("[MICR-TRACE] 08. Calling updateSubmitButton()");

		updateSubmitButton();

		System.out.println("[MICR-TRACE] 08. updateSubmitButton COMPLETE");

		System.out.println("========== LOAD CURRENT CHEQUE COMPLETE ==========");

	}

	private void populateCheque(MicrRepairChequeDTO cheque) {

		System.out.println("\n========== POPULATE CHEQUE ==========");

		System.out.println("[MICR-TRACE] populateCheque ENTER");

		if (cheque == null) {

			System.out.println("[MICR-TRACE] populateCheque: DTO NULL - returning");

			return;

		}

		System.out.println("Populate cheque ID = [" + cheque.getChequeId() + "]");

		System.out.println("Populate cheque number = [" + cheque.getChequeNumber() + "]");

		System.out.println("Populate cheque status = [" + cheque.getChequeStatus() + "]");

		System.out.println("Populate MICR = [" + cheque.getFullMicr() + "]");

		System.out.println("1. Setting cheque number");

		txtChequeNumber.setValue(safe(cheque.getChequeNumber()));

		System.out.println("1. cheque number COMPLETE");

		System.out.println("2. Setting city code");

		txtCityCode.setValue(safe(cheque.getCityCode()));

		System.out.println("2. city code COMPLETE = [" + safe(cheque.getCityCode()) + "]");

		System.out.println("3. Setting bank code");

		txtBankCode.setValue(safe(cheque.getBankCode()));

		System.out.println("3. bank code COMPLETE = [" + safe(cheque.getBankCode()) + "]");

		System.out.println("4. Setting branch code");

		txtBranchCode.setValue(safe(cheque.getBranchCode()));

		System.out.println("4. branch code COMPLETE = [" + safe(cheque.getBranchCode()) + "]");

		System.out.println("5. Setting current MICR");

		String currentMicr = safe(cheque.getFullMicr());

		txtCurrentMicr.setValue(currentMicr);

		System.out.println("5. current MICR COMPLETE = [" + currentMicr + "]");

		System.out.println("6. Setting corrected MICR");

		txtCorrectedMicr.setValue(currentMicr);

		System.out.println("6. corrected MICR COMPLETE = [" + currentMicr + "]");

		System.out.println("7. Setting cheque status");

		lblChequeStatus.setValue(safe(cheque.getChequeStatus()));

		System.out.println("7. cheque status COMPLETE");

		System.out.println("========== MICR DETAILS POPULATED ==========");

		System.out.println("8. Reading rejection information");

		String reasonId = safe(cheque.getReasonId());

		String reason = safe(cheque.getReason());

		String remarks = safe(cheque.getRemarks());

		System.out.println("Reason ID = [" + reasonId + "]");

		System.out.println("Reason = [" + reason + "]");

		System.out.println("Remarks = [" + remarks + "]");

		if (!reasonId.isEmpty() || !reason.isEmpty() || !remarks.isEmpty()) {

			System.out.println("9. Rejection information is available - showing section");

			divCheckerReturnInformation.setVisible(true);

			System.out.println("10. Setting checker reason ID");

			lblCheckerReasonId.setValue(reasonId);

			System.out.println("10. checker reason ID COMPLETE");

			System.out.println("11. Setting checker reason");

			lblCheckerReason.setValue(reason);

			System.out.println("11. checker reason COMPLETE");

			System.out.println("12. Setting checker remarks");

			lblCheckerRemarks.setValue(remarks);

			System.out.println("12. checker remarks COMPLETE");

		} else {

			System.out.println("9. No rejection information - hiding section");

			divCheckerReturnInformation.setVisible(false);

		}

		frontImagePath = safe(cheque.getChequeImageFront());

		backImagePath = safe(cheque.getChequeImageBack());

		System.out.println("========== CHECKER INFORMATION COMPLETE ==========");

		System.out.println("13. Calling showFrontImage()");

		showFrontImage();

		System.out.println("13. showFrontImage COMPLETE");

		System.out.println("========== POPULATE CHEQUE COMPLETE ==========");

	}

	private void showFrontImage() {

		showingBackImage = false;

		lblChequeImageTitle.setValue("Cheque Front Image");

		loadChequeImage(frontImagePath);

		updateImagePosition();

		applyImageTransform();

	}

	private void showBackImage() {

		showingBackImage = true;

		lblChequeImageTitle.setValue("Cheque Back Image");

		if (backImagePath == null || backImagePath.trim().isEmpty()) {

			imgCheque.setVisible(false);

			imgCheque.setSrc("");

			lblChequeImagePlaceholder.setVisible(true);

			lblChequeImageTitle.setValue("Cheque Back Image Not Available");

			updateImagePosition();

			return;

		}

		loadChequeImage(backImagePath);

		updateImagePosition();

		applyImageTransform();

	}

	private void loadChequeImage(String imagePath) {

		if (imagePath == null || imagePath.trim().isEmpty()) {

			imgCheque.setSrc("");

			imgCheque.setVisible(false);

			lblChequeImagePlaceholder.setVisible(true);

			return;

		}

		imagePath = imagePath.trim();

		if (!imagePath.startsWith("/")) {

			imagePath = "/" + imagePath;

		}

		imgCheque.setSrc(imagePath);

		imgCheque.setVisible(true);

		lblChequeImagePlaceholder.setVisible(false);

	}

	private void updateImagePosition() {

		if (showingBackImage) {

			lblImagePosition.setValue("2 of 2");

		} else {

			lblImagePosition.setValue("1 of 2");

		}

		lblImageRecordPosition.setValue("Record " + (currentIndex + 1) + " of " + getTotalCheques());

	}

	// =========================================================

	// FRONT / BACK IMAGE

	// =========================================================

	public void onClick$btnImageToggle(Event event) {

		if (showingBackImage) {

			zoomLevel = 1.0;

			rotation = 0;

			showFrontImage();

			btnImageToggle.setLabel("View Back");

		} else {

			zoomLevel = 1.0;

			rotation = 0;

			showBackImage();

			btnImageToggle.setLabel("View Front");

		}

	}

	public void onClick$btnZoom(Event event) {

		zoomLevel += 0.25;

		if (zoomLevel > 3.0) {

			zoomLevel = 3.0;

		}

		applyImageTransform();

	}

	public void onClick$btnZoomOut(Event event) {

		zoomLevel -= 0.25;

		if (zoomLevel < 0.5) {

			zoomLevel = 0.5;

		}

		applyImageTransform();

	}

	public void onClick$btnZoomReset(Event event) {

		zoomLevel = 1.0;

		rotation = 0;

		applyImageTransform();

	}

	public void onClick$btnRotate(Event event) {

		rotation += 90;

		if (rotation >= 360) {

			rotation = 0;

		}

		applyImageTransform();

	}

	private void applyImageTransform() {

		String transform = "transform: scale(" + zoomLevel + ") rotate(" + rotation + "deg);"

				+ "transform-origin:center center;";

		imgCheque.setStyle(transform);

	}

	public void onClick$btnImagePrevious(Event event) {

		if (showingBackImage) {

			zoomLevel = 1.0;

			rotation = 0;

			showFrontImage();

			btnImageToggle.setLabel("View Back");

		}

	}

	public void onClick$btnImageNext(Event event) {

		if (!showingBackImage) {

			zoomLevel = 1.0;

			rotation = 0;

			showBackImage();

			btnImageToggle.setLabel("View Front");

		}

	}

	// =========================================================

	// PREVIOUS CHEQUE

	// =========================================================

	public void onClick$btnPrevious(Event event) {

		if (currentIndex <= 0) {

			return;

		}

		currentIndex--;

		loadCurrentCheque();

	}

	// =========================================================

	// NEXT CHEQUE

	// =========================================================

	public void onClick$btnNext(Event event) {

		int totalCheques = getTotalCheques();

		if (currentIndex >= totalCheques - 1) {

			return;

		}

		currentIndex++;

		loadCurrentCheque();

	}

	// =========================================================

	// NAVIGATION BUTTONS

	// =========================================================

	private void updateNavigationButtons() {

		System.out.println("[MICR-TRACE] private void updateNavigationButtons ENTER");

		int totalCheques = getTotalCheques();

		btnPrevious.setDisabled(currentIndex <= 0);

		btnNext.setDisabled(currentIndex >= totalCheques - 1);

		lblChequePosition.setValue((currentIndex + 1) + " of " + totalCheques);

	}

	// =========================================================

	// PROGRESS

	// =========================================================

	private void updateProgress() {

		System.out.println("[MICR-TRACE] private void updateProgress ENTER");

		int totalCheques = getTotalCheques();

		if (totalCheques <= 0) {

			lblProgressText.setValue("Record 0 of 0");

			prgMicrRepair.setValue(0);

			lblMicrRepairCount.setValue("0");

			lblCompletedCount.setValue("0");

			lblRemainingCount.setValue("0");

			return;

		}

		int processed = countProcessedCheques();

		int pending = countPendingCheques();

		int recordNumber = currentIndex + 1;

		int percentage = (processed * 100) / totalCheques;

		lblProgressText.setValue("Record " + recordNumber + " of " + totalCheques);

		prgMicrRepair.setValue(percentage);

		lblMicrRepairCount.setValue(String.valueOf(totalCheques));

		lblCompletedCount.setValue(String.valueOf(processed));

		lblRemainingCount.setValue(String.valueOf(pending));

	}

	// =========================================================

	// COUNT PROCESSED

	// =========================================================

	private int countProcessedCheques() {

		if (micrRepairCheques == null) {

			return 0;

		}

		int count = 0;

		for (MicrRepairChequeDTO cheque : micrRepairCheques) {

			if (cheque == null) {

				continue;

			}

			if (!isPendingMicrRepair(cheque)) {

				count++;

			}

		}

		return count;

	}

	// =========================================================

	// COUNT PENDING

	// =========================================================

	private int countPendingCheques() {

		if (micrRepairCheques == null) {

			return 0;

		}

		int count = 0;

		for (MicrRepairChequeDTO cheque : micrRepairCheques) {

			if (cheque == null) {

				continue;

			}

			if (isPendingMicrRepair(cheque)) {

				count++;

			}

		}

		return count;

	}

	// =========================================================

	// ACTION BUTTON STATE

	// =========================================================

	private void updateActionButtons() {

		System.out.println("[MICR-TRACE] private void updateActionButtons ENTER");

		if (currentIndex < 0 || currentIndex >= getTotalCheques()) {

			btnSaveNext.setDisabled(true);

			btnRejectRequest.setDisabled(true);

			return;

		}

		MicrRepairChequeDTO cheque = micrRepairCheques.get(currentIndex);

		boolean pending = isPendingMicrRepair(cheque);

		btnSaveNext.setDisabled(!pending);

		btnRejectRequest.setDisabled(!pending);

	}

	// =========================================================

	// SUBMIT BUTTON

	// =========================================================

	private void updateSubmitButton() {

		System.out.println("[MICR-TRACE] private void updateSubmitButton ENTER");

		if (btnSubmit == null) {

			return;

		}

		btnSubmit.setDisabled(!areAllChequesProcessed());

	}

	private boolean areAllChequesProcessed() {

		System.out.println("[MICR-TRACE] private boolean areAllChequesProcessed ENTER");

		if (micrRepairCheques == null || micrRepairCheques.isEmpty()) {

			return false;

		}

		for (MicrRepairChequeDTO cheque : micrRepairCheques) {

			if (cheque == null) {

				return false;

			}

			if (isPendingMicrRepair(cheque)) {

				return false;

			}

		}

		return true;

	}

	// =========================================================

	// SAVE / NEXT

	// =========================================================

	public void onClick$btnSaveNext(Event event) {

		System.out.println("========== SAVE BUTTON CLICKED ==========");

		int totalCheques = getTotalCheques();

		if (currentIndex < 0 || currentIndex >= totalCheques) {

			showError("Invalid cheque selection.");

			return;

		}

		MicrRepairChequeDTO cheque = micrRepairCheques.get(currentIndex);

		if (cheque == null) {

			showError("Cheque information is unavailable.");

			return;

		}

		// -----------------------------------------------------

		// Already processed

		// -----------------------------------------------------

		if (!isPendingMicrRepair(cheque)) {

			showWarning("This cheque has already been processed.");

			updateSubmitButton();

			return;

		}

		// -----------------------------------------------------

		// Validate MICR

		// -----------------------------------------------------

		if (!validateMicrFields()) {

			return;

		}

		// -----------------------------------------------------

		// Update DTO from screen

		// -----------------------------------------------------

		updateChequeFromScreen(cheque);

		/*
		 * 
		 * Individual Save status.
		 *
		 * 
		 * 
		 * Final status is NOT assigned here.
		 * 
		 */

		cheque.setChequeStatus(STATUS_MICR_REPAIRED);

		try {

			if ("SCAN".equals(source)) {

				outwardMakerService.saveScanMicrRepair(cheque);

			} else {

				outwardMakerService.saveOutwardMicrRepair(cheque);

			}

			updateProgress();

			updateSubmitButton();

			// -------------------------------------------------

			// Move to next cheque

			// -------------------------------------------------

			if (currentIndex < totalCheques - 1) {

				currentIndex++;

				loadCurrentCheque();

				Clients.showNotification("MICR repair saved successfully.", Clients.NOTIFICATION_TYPE_INFO, null,

						"top_center", 1500);

			} else {

				/*
				 * 
				 * Last cheque saved.
				 *
				 * 
				 * 
				 * DO NOT automatically submit.
				 * 
				 */

				updateSubmitButton();

				if (areAllChequesProcessed()) {

					Clients.showNotification(

							"All MICR repair cheques have been processed. " + "Click Submit to complete.",

							Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 2500);

				} else {

					Clients.showNotification("MICR repair saved successfully.", Clients.NOTIFICATION_TYPE_INFO, null,

							"top_center", 1500);

				}

			}

		} catch (Exception e) {

			e.printStackTrace();

			/*
			 * 
			 * Restore the DTO status so the cheque remains available for processing if the
			 * 
			 * database save failed.
			 * 
			 */

			cheque.setChequeStatus(STATUS_PENDING_MICR_REPAIR);

			updateProgress();

			updateSubmitButton();

			showError("Failed to save MICR repair: " + safe(e.getMessage()));

		}

	}

	// =========================================================

	// UPDATE DTO FROM SCREEN

	// =========================================================

	private void updateChequeFromScreen(MicrRepairChequeDTO cheque) {

		System.out.println("[MICR-TRACE] private void updateChequeFromScreen ENTER");

		cheque.setCityCode(safe(txtCityCode.getValue()).trim());

		cheque.setBankCode(safe(txtBankCode.getValue()).trim());

		cheque.setBranchCode(safe(txtBranchCode.getValue()).trim());

		cheque.setFullMicr(safe(txtCorrectedMicr.getValue()).trim());

	}

	// =========================================================

	// MICR VALIDATION

	// =========================================================

	private boolean validateMicrFields() {

		System.out.println("[MICR-TRACE] private boolean validateMicrFields ENTER");

		String city = safe(txtCityCode.getValue()).trim();

		String bank = safe(txtBankCode.getValue()).trim();

		String branch = safe(txtBranchCode.getValue()).trim();

		String micr = safe(txtCorrectedMicr.getValue()).trim();

		lblMicrValidationMessage.setVisible(false);

		lblMicrValidationMessage.setValue("");

		// -----------------------------------------------------

		// City

		// -----------------------------------------------------

		if (city.isEmpty()) {

			showValidationMessage("City Code is required.");

			txtCityCode.setFocus(true);

			return false;

		}

		if (!isValidMicrPart(city)) {

			showValidationMessage("City Code must contain exactly 3 " + "alphanumeric characters and cannot be 000.");

			txtCityCode.setFocus(true);

			return false;

		}

		// -----------------------------------------------------

		// Bank

		// -----------------------------------------------------

		if (bank.isEmpty()) {

			showValidationMessage("Bank Code is required.");

			txtBankCode.setFocus(true);

			return false;

		}

		if (!isValidMicrPart(bank)) {

			showValidationMessage("Bank Code must contain exactly 3 " + "alphanumeric characters and cannot be 000.");

			txtBankCode.setFocus(true);

			return false;

		}

		// -----------------------------------------------------

		// Branch

		// -----------------------------------------------------

		if (branch.isEmpty()) {

			showValidationMessage("Branch Code is required.");

			txtBranchCode.setFocus(true);

			return false;

		}

		if (!isValidMicrPart(branch)) {

			showValidationMessage("Branch Code must contain exactly 3 " + "alphanumeric characters and cannot be 000.");

			txtBranchCode.setFocus(true);

			return false;

		}

		// -----------------------------------------------------

		// Corrected MICR

		// -----------------------------------------------------

		if (micr.isEmpty()) {

			showValidationMessage("Corrected MICR Code is required.");

			txtCorrectedMicr.setFocus(true);

			return false;

		}

		if (!micr.matches("[A-Za-z0-9]{9}")) {

			showValidationMessage("Corrected MICR Code must contain exactly 9 " + "alphanumeric characters.");

			txtCorrectedMicr.setFocus(true);

			return false;

		}

		if (!micr.equals(city + bank + branch)) {

			showValidationMessage("Corrected MICR Code must match " + "City Code + Bank Code + Branch Code.");

			txtCorrectedMicr.setFocus(true);

			return false;

		}

		return true;

	}

	private void showValidationMessage(String message) {

		lblMicrValidationMessage.setValue(message);

		lblMicrValidationMessage.setVisible(true);

	}

	// =========================================================

	// VALID MICR PART

	// =========================================================

	private boolean isValidMicrPart(String value) {

		if (value == null) {

			return false;

		}

		if (value.length() != 3) {

			return false;

		}

		if (!value.matches("[A-Za-z0-9]{3}")) {

			return false;

		}

		if ("000".equals(value)) {

			return false;

		}

		return true;

	}

	// =========================================================

	// IS PENDING MICR REPAIR

	// =========================================================

	private boolean isPendingMicrRepair(MicrRepairChequeDTO cheque) {

		if (cheque == null) {

			return false;

		}

		return STATUS_PENDING_MICR_REPAIR.equalsIgnoreCase(safe(cheque.getChequeStatus()));

	}

	// =========================================================

	// REQUEST REJECT

	// =========================================================

	public void onClick$btnRejectRequest(Event event) {
		System.out.println("========== REQUEST REJECT CLICKED ==========");

		int totalCheques = getTotalCheques();

		if (currentIndex < 0 || currentIndex >= totalCheques) {

			return;

		}

		MicrRepairChequeDTO cheque = micrRepairCheques.get(currentIndex);

		if (cheque == null) {

			return;

		}

		if (!isPendingMicrRepair(cheque)) {

			showWarning("This cheque has already been processed " + "and cannot be rejected again.");

			return;

		}

		cmbRejectReason.setValue("");

		txtRejectRemarks.setValue("");

		winRejectRequest.setVisible(true);

	}

	// =========================================================

	// CANCEL REJECT

	// =========================================================

	public void onClick$btnCancelReject(Event event) {

		cmbRejectReason.setValue("");

		txtRejectRemarks.setValue("");

		winRejectRequest.setVisible(false);

	}

	// =========================================================

	// PROCEED REJECT

	// =========================================================

	public void onClick$btnProceedReject(Event event) {
		System.out.println("========== PROCEED REJECT CLICKED ==========");

		if (currentIndex < 0 || currentIndex >= getTotalCheques()) {

			return;

		}

		MicrRepairChequeDTO cheque = micrRepairCheques.get(currentIndex);

		if (cheque == null) {

			showError("Cheque information is unavailable.");

			return;

		}

		if (!isPendingMicrRepair(cheque)) {

			winRejectRequest.setVisible(false);

			showWarning("This cheque has already been processed.");

			return;

		}

		// -----------------------------------------------------

		// Validate reason

		// -----------------------------------------------------

		Comboitem selectedItem = (Comboitem) cmbRejectReason.getSelectedItem();

		if (selectedItem == null) {

			showWarning("Rejected reason is required.");

			cmbRejectReason.setFocus(true);

			return;

		}

		String reasonId = safe(selectedItem.getValue() == null ? null : selectedItem.getValue().toString()).trim();

		String reason = safe(selectedItem.getLabel()).trim();

		String remarks = safe(txtRejectRemarks.getValue()).trim();

		if (reasonId.isEmpty()) {

			showWarning("Rejected reason is required.");

			return;

		}

		if (reason.isEmpty()) {

			showWarning("Rejected reason is required.");

			return;

		}

		if (remarks.isEmpty()) {

			showWarning("Remarks are required.");

			txtRejectRemarks.setFocus(true);

			return;

		}

		// -----------------------------------------------------

		// Update DTO

		// -----------------------------------------------------

		cheque.setReasonId(reasonId);

		cheque.setReason(reason);

		cheque.setRemarks(remarks);

		/*
		 * 
		 * Rejection request is saved individually. Final status will be
		 * 
		 * REJECT_REQUESTED when the complete list is submitted.
		 * 
		 */

		cheque.setChequeStatus(STATUS_MICR_REJECTED);

		try {

			if ("SCAN".equals(source)) {

				outwardMakerService.saveScanMicrRepair(cheque);

			} else {

				outwardMakerService.saveOutwardMicrRepair(cheque);

			}

			winRejectRequest.setVisible(false);

			updateProgress();

			updateSubmitButton();

			updateActionButtons();

			Clients.showNotification("Rejection request saved successfully.", Clients.NOTIFICATION_TYPE_INFO, null,

					"top_center", 2000);

			// -------------------------------------------------

			// Move to next cheque

			// -------------------------------------------------

			if (currentIndex < getTotalCheques() - 1) {

				currentIndex++;

				loadCurrentCheque();

			} else {

				updateSubmitButton();

				if (areAllChequesProcessed()) {

					Clients.showNotification(

							"All MICR repair cheques have been processed. " + "Click Submit to complete.",

							Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 2500);

				}

			}

		} catch (Exception e) {

			e.printStackTrace();

			cheque.setChequeStatus(STATUS_PENDING_MICR_REPAIR);

			updateProgress();

			updateSubmitButton();

			updateActionButtons();

			showError("Failed to save rejection request: " + safe(e.getMessage()));

		}

	}

	// =========================================================

	// FINAL SUBMIT

	// =========================================================

	public void onClick$btnSubmit(Event event) {
		System.out.println("========== SUBMIT BUTTON CLICKED ==========");

		if (micrRepairCheques == null || micrRepairCheques.isEmpty()) {

			showError("No MICR repair cheques available.");

			return;

		}

		// -----------------------------------------------------

		// All cheques must be individually processed

		// -----------------------------------------------------

		if (!areAllChequesProcessed()) {

			updateSubmitButton();

			showWarning("Please process all MICR repair cheques " + "before submitting.");

			return;

		}

		/*
		 * 
		 * ----------------------------------------------------- Set final statuses.
		 *
		 * 
		 * 
		 * MICR_REPAIR -> PENDING_DATA_ENTRY
		 *
		 * 
		 * 
		 * MICR_REJECTED -> REJECT_REQUESTED
		 * 
		 * -----------------------------------------------------
		 * 
		 */

		for (MicrRepairChequeDTO cheque : micrRepairCheques) {

			if (cheque == null) {

				continue;

			}

			String status = safe(cheque.getChequeStatus());

			if (STATUS_MICR_REJECTED.equalsIgnoreCase(status)) {

				cheque.setChequeStatus(STATUS_REJECT_REQUESTED);

			} else {

				cheque.setChequeStatus(STATUS_PENDING_DATA_ENTRY);

			}

		}

		try {

			if ("SCAN".equals(source)) {

				outwardMakerService.submitScanMicrRepair(micrRepairCheques);

			} else {

				outwardMakerService.submitOutwardMicrRepair(micrRepairCheques);

			}

			btnSubmit.setDisabled(true);

			btnSaveNext.setDisabled(true);

			btnRejectRequest.setDisabled(true);

			Clients.showNotification("MICR repair submitted successfully.", Clients.NOTIFICATION_TYPE_INFO, null,

					"top_center", 2500);

			redirectToMicrRepairView();

		} catch (Exception e) {

			e.printStackTrace();

			/*
			 * 
			 * Restore the in-memory final status to the individual/stage status if final
			 * 
			 * Submit failed.
			 * 
			 */

			for (MicrRepairChequeDTO cheque : micrRepairCheques) {

				if (cheque == null) {

					continue;

				}

				String status = safe(cheque.getChequeStatus());

				if (STATUS_PENDING_DATA_ENTRY.equalsIgnoreCase(status)) {

					cheque.setChequeStatus(STATUS_MICR_REPAIRED);

				} else if (STATUS_REJECT_REQUESTED.equalsIgnoreCase(status)) {

					cheque.setChequeStatus(STATUS_MICR_REJECTED);

				}

			}

			updateProgress();

			updateSubmitButton();

			updateActionButtons();

			showError("Failed to submit MICR repair: " + safe(e.getMessage()));

		}

	}

	// =========================================================

	// SAVE CONFIRMATION POPUP

	// =========================================================

	public void onClick$btnCancelSave(Event event) {

		winConfirmSave.setVisible(false);

	}

	// =========================================================

	// INFORMATION POPUP

	// =========================================================

	public void onClick$btnCloseMicrInformation(Event event) {

		winMicrInformation.setVisible(false);

	}

	private void showInformation(String message) {

		lblMicrInformationMessage.setValue(message);

		winMicrInformation.setVisible(true);

	}

	// =========================================================

	// BACK TO LIST

	// =========================================================

	public void onClick$btnBackToList(Event event) {

		System.out.println("========== BACK TO LIST CLICKED ==========");

		redirectToMicrRepairView();

	}

	// =========================================================

	// REDIRECT TO MICR REPAIR VIEW

	// =========================================================

	private void redirectToMicrRepairView() {

		Component root = winMicrRepair.getDesktop().getFirstPage().getFirstRoot();

		Component mainContentArea = root.getFellowIfAny("mainContentArea", true);

		if (mainContentArea instanceof Include) {

			Include include = (Include) mainContentArea;

			include.setSrc("/outward/maker/micr-repair/micr-repair-view.zul");

			return;

		}

		Executions.sendRedirect("/outward/maker/micr-repair/micr-repair-view.zul");

	}

	// =========================================================

	// SAFE STRING

	// =========================================================

	private String safe(String value) {

		if (value == null) {

			return "";

		}

		return value;

	}

	// =========================================================

	// ERROR

	// =========================================================

	private void showError(String message) {

		Clients.showNotification(message, Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 4000);

	}

	// =========================================================

	// WARNING

	// =========================================================

	private void showWarning(String message) {

		Clients.showNotification(message, Clients.NOTIFICATION_TYPE_WARNING, null, "top_center", 3000);

	}

}