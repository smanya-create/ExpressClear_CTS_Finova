package com.iispl.cts.controller.outward.maker;

import java.util.ArrayList;
import java.util.List;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.MouseEvent;
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

public class OutwardMakerMicrRepairController extends GenericForwardComposer<Component> {
	private final OutwardMakerService outwardMakerService;

	@Wire
	private Component outwardMicrRepairRoot;
	@Wire
	private Label lblBatchId;
	@Wire
	private Label lblTotalCheques;
	@Wire
	private Label lblRecordPosition;
	@Wire
	private Button btnPrevious;
	@Wire
	private Button btnNext;
	@Wire
	private Button btnBackToList;
	@Wire
	private Image chequeImage;
	@Wire
	private Label lblChequeImageTitle;
	@Wire
	private org.zkoss.zul.Groupbox emptyImageState;
	@Wire
	private Button btnZoom;
	@Wire
	private Button btnZoomOut;
	@Wire
	private Button btnZoomReset;
	@Wire
	private Button btnRotate;
	@Wire
	private Button btnViewFront;
	@Wire
	private Button btnViewBack;
	@Wire
	private Label lblHeaderItemStatus;
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
	private Div micrAlertBox;
	@Wire
	private Label lblMicrReasonCode;
	@Wire
	private Label lblMicrReasonName;
	@Wire
	private Label lblMicrRemarks;
	@Wire
	private Button btnRejectRequest;
	@Wire
	private Button btnSaveAndNext;
	@Wire
	private Button btnSubmitToDataEntry;
	@Wire
	private org.zkoss.zul.Progressmeter progressMeter;
	@Wire
	private Label lblProgress;
	@Wire
	private Window rejectRequestWindow;
	@Wire
	private Combobox cmbRejectReason;
	@Wire
	private Textbox txtModalRejectionRemark;
	@Wire
	private Button btnCancelReject;
	@Wire
	private Button btnConfirmReject;

	private List<MicrRepairChequeDTO> micrRepairCheques = new ArrayList<MicrRepairChequeDTO>();
	private String source;
	private String batchId;
	private String chequeId;
	private int currentIndex = 0;

	private static final String STATUS_PENDING_MICR_REPAIR = "PENDING_MICR_REPAIR";
	private static final String STATUS_MICR_REPAIRED = "MICR_REPAIRED";
	private static final String STATUS_MICR_REJECTED = "MICR_REJECTED";
	private static final String STATUS_MICR_REJECTION_PENDING = "MICR_REJECTION_PENDING";
	private static final String STATUS_PENDING_DATA_ENTRY = "PENDING_DATA_ENTRY";
	private static final String STATUS_REJECT_REQUESTED = "REJECT_REQUESTED";

	private boolean showingBackImage = false;
	private double zoomLevel = 1.0;
	private int rotation = 0;
	private boolean draggingImage = false;
	private int lastMouseX = 0;
	private int lastMouseY = 0;
	private int imageX = 0;
	private int imageY = 0;
	private boolean syncingMicrFields = false;
	private String frontImagePath = "";
	private String backImagePath = "";

	public OutwardMakerMicrRepairController() {
		outwardMakerService = new OutwardMakerServiceImpl();
	}

	@Override
	public void doAfterCompose(Component window) throws Exception {
		super.doAfterCompose(window);
		resolveRejectPopupComponents(window);
		loadParameters(window);
		if (source == null || source.trim().isEmpty()) {
			showError("MICR repair source is missing.");
			return;
		}
		source = source.trim().toUpperCase();
		if (!"SCAN".equals(source) && !"OUTWARD".equals(source)) {
			showError("Invalid MICR repair source.");
			return;
		}
		if (batchId == null || batchId.trim().isEmpty()) {
			showError("Batch ID is missing.");
			return;
		}
		batchId = batchId.trim();
		lblBatchId.setValue(batchId);
		loadRejectedReasons();
		btnSubmitToDataEntry.setDisabled(true);
		loadMicrRepairCheques();
		int totalCheques = getTotalCheques();
		if (totalCheques == 0) {
			showError("No MICR repair cheques found for batch " + batchId);
			btnSaveAndNext.setDisabled(true);
			btnRejectRequest.setDisabled(true);
			btnSubmitToDataEntry.setDisabled(true);
			btnPrevious.setDisabled(true);
			btnNext.setDisabled(true);
			updateProgress();
			return;
		}
		currentIndex = findOpeningChequeIndex();
		if (currentIndex < 0) {
			currentIndex = 0;
		}
		loadCurrentCheque();
		updateSubmitButton();
	}

	private void registerRejectPopupListeners() {
		if (btnCancelReject != null) {
			btnCancelReject.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					onClick$btnCancelReject(event);
				}
			});
		}
		if (btnConfirmReject != null) {
			btnConfirmReject.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
				@Override
				public void onEvent(Event event) throws Exception {
					onClick$btnConfirmReject(event);
				}
			});
		}
	}

	private void resolveRejectPopupComponents(Component root) {
		if (root == null) {
			return;
		}
		if (rejectRequestWindow == null) {
			Component popup = findComponentById(root, "rejectRequestWindow");
			if (popup instanceof Window) {
				rejectRequestWindow = (Window) popup;
			}
		}
		if (rejectRequestWindow == null) {
			return;
		}
		if (cmbRejectReason == null) {
			Component component = findComponentById(rejectRequestWindow, "cmbRejectReason");
			if (component instanceof Combobox) {
				cmbRejectReason = (Combobox) component;
			}
		}
		if (txtModalRejectionRemark == null) {
			Component component = findComponentById(rejectRequestWindow, "txtModalRejectionRemark");
			if (component instanceof Textbox) {
				txtModalRejectionRemark = (Textbox) component;
			}
		}
		if (btnCancelReject == null) {
			Component component = findComponentById(rejectRequestWindow, "btnCancelReject");
			if (component instanceof Button) {
				btnCancelReject = (Button) component;
			}
		}
		if (btnConfirmReject == null) {
			Component component = findComponentById(rejectRequestWindow, "btnConfirmReject");
			if (component instanceof Button) {
				btnConfirmReject = (Button) component;
			}
		}
		registerRejectPopupListeners();
	}

	private Component findComponentById(Component root, String id) {
		if (root == null || id == null) {
			return null;
		}
		if (id.equals(root.getId())) {
			return root;
		}
		for (Component child : root.getChildren()) {
			Component match = findComponentById(child, id);
			if (match != null) {
				return match;
			}
		}
		return null;
	}

	private void loadParameters(Component window) {
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
		if (source == null || source.trim().isEmpty()) {
			source = Executions.getCurrent().getParameter("source");
		}
		if (batchId == null || batchId.trim().isEmpty()) {
			batchId = Executions.getCurrent().getParameter("batchId");
		}
		if (chequeId == null || chequeId.trim().isEmpty()) {
			chequeId = Executions.getCurrent().getParameter("chequeId");
		}
	}

	private void loadRejectedReasons() {
		try {
			if (cmbRejectReason == null) {
				resolveRejectPopupComponents(outwardMicrRepairRoot);
			}
			if (cmbRejectReason == null) {
				showError("Rejection reason field is unavailable.");
				return;
			}
			List<RejectedReason> reasons = outwardMakerService.getRejectedReasons();
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
		} catch (Exception e) {
			e.printStackTrace();
			showError("Unable to load rejection reasons.");
		}
	}

	private void loadMicrRepairCheques() {
		try {
			if ("SCAN".equals(source)) {
				micrRepairCheques = outwardMakerService.getScanMicrRepairCheques(batchId);
			} else {
				micrRepairCheques = outwardMakerService.getOutwardMicrRepairCheques(batchId);
			}
			if (micrRepairCheques == null) {
				micrRepairCheques = new ArrayList<MicrRepairChequeDTO>();
			}
		} catch (Exception e) {
			e.printStackTrace();
			micrRepairCheques = new ArrayList<MicrRepairChequeDTO>();
			showError("Unable to load MICR repair cheques.");
		}
	}

	private int getTotalCheques() {
		if (micrRepairCheques == null) {
			return 0;
		}
		return micrRepairCheques.size();
	}

	private int findOpeningChequeIndex() {
		if (micrRepairCheques == null || micrRepairCheques.isEmpty()) {
			return -1;
		}
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

	private void loadCurrentCheque() {
		int totalCheques = getTotalCheques();
		if (currentIndex < 0 || currentIndex >= totalCheques) {
			return;
		}
		txtCityCode.setValue("");
		txtBankCode.setValue("");
		txtBranchCode.setValue("");
		txtCurrentMicr.setValue("");
		txtCorrectedMicr.setValue("");
		micrAlertBox.setVisible(false);
		lblMicrReasonCode.setValue("");
		lblMicrReasonName.setValue("");
		lblMicrRemarks.setValue("");
		showingBackImage = false;
		zoomLevel = 1.0;
		rotation = 0;
		draggingImage = false;
		lastMouseX = 0;
		lastMouseY = 0;
		imageX = 0;
		imageY = 0;
		frontImagePath = "";
		backImagePath = "";

		MicrRepairChequeDTO cheque = micrRepairCheques.get(currentIndex);
		if (cheque == null) {
			return;
		}
		populateCheque(cheque);
		updateProgress();
		updateNavigationButtons();
		updateActionButtons();
		updateSubmitButton();
	}

	private void populateCheque(MicrRepairChequeDTO cheque) {
		if (cheque == null) {
			return;
		}
		txtChequeNumber.setValue(safe(cheque.getChequeNumber()));
		txtCityCode.setValue(safe(cheque.getCityCode()));
		txtBankCode.setValue(safe(cheque.getBankCode()));
		txtBranchCode.setValue(safe(cheque.getBranchCode()));
		
		String currentMicr = safe(cheque.getFullMicr());
		txtCurrentMicr.setValue(currentMicr);
		txtCorrectedMicr.setValue(currentMicr);
		refreshMicrValidationHighlight();
		
		String formattedStatus = formatStatusDisplay(cheque.getChequeStatus());
		lblHeaderItemStatus.setValue(formattedStatus);

		String reasonId = safe(cheque.getReasonId());
		String reason = safe(cheque.getReason());
		String remarks = safe(cheque.getRemarks());

		if (!reasonId.isEmpty() || !reason.isEmpty() || !remarks.isEmpty()) {
			micrAlertBox.setVisible(true);
			lblMicrReasonCode.setValue(reasonId);
			lblMicrReasonName.setValue(reason);
			lblMicrRemarks.setValue(remarks);
		} else {
			micrAlertBox.setVisible(false);
		}

		frontImagePath = safe(cheque.getChequeImageFront());
		backImagePath = safe(cheque.getChequeImageBack());
		showFrontImage();
	}

	private String formatStatusDisplay(String rawStatus) {
		if (rawStatus == null || rawStatus.trim().isEmpty()) {
			return "";
		}
		String[] words = rawStatus.trim().split("_+");
		StringBuilder formatted = new StringBuilder();

		for (String word : words) {
			if (!word.isEmpty()) {
				formatted.append(Character.toUpperCase(word.charAt(0)))
						.append(word.substring(1).toLowerCase())
						.append(" ");
			}
		}
		return formatted.toString().trim();
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
			chequeImage.setVisible(false);
			chequeImage.setSrc("");
			emptyImageState.setVisible(true);
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
			chequeImage.setSrc("");
			chequeImage.setVisible(false);
			emptyImageState.setVisible(true);
			return;
		}
		imagePath = imagePath.trim();
		if (!imagePath.startsWith("/")) {
			imagePath = "/" + imagePath;
		}
		chequeImage.setSrc(imagePath);
		chequeImage.setVisible(true);
		emptyImageState.setVisible(false);
	}

	private void updateImagePosition() {
	}

	public void onClick$btnViewFront(Event event) {
		draggingImage = false;
		imageX = 0;
		imageY = 0;
		zoomLevel = 1.0;
		rotation = 0;
		showFrontImage();
	}

	public void onClick$btnViewBack(Event event) {
		draggingImage = false;
		imageX = 0;
		imageY = 0;
		zoomLevel = 1.0;
		rotation = 0;
		showBackImage();
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
		imageX = 0;
		imageY = 0;
		draggingImage = false;
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
		String transform = "transform: translate(" + imageX + "px, " + imageY + "px) " + "scale(" + zoomLevel + ") "
				+ "rotate(" + rotation + "deg);" + "transform-origin:center center;";
		chequeImage.setStyle(transform);
	}

	public void onMouseDown$divChequeImageContainer(MouseEvent event) {
		if (!chequeImage.isVisible()) {
			return;
		}
		draggingImage = true;
		lastMouseX = event.getX();
		lastMouseY = event.getY();
	}

	public void onMouseMove$divChequeImageContainer(MouseEvent event) {
		if (!draggingImage || !chequeImage.isVisible()) {
			return;
		}
		int currentMouseX = event.getX();
		int currentMouseY = event.getY();
		int deltaX = currentMouseX - lastMouseX;
		int deltaY = currentMouseY - lastMouseY;
		imageX += deltaX;
		imageY += deltaY;
		lastMouseX = currentMouseX;
		lastMouseY = currentMouseY;
		applyImageTransform();
	}

	public void onMouseUp$divChequeImageContainer(MouseEvent event) {
		draggingImage = false;
	}

	public void onClick$btnPrevious(Event event) {
		if (currentIndex <= 0) {
			return;
		}
		currentIndex--;
		loadCurrentCheque();
	}

	public void onClick$btnNext(Event event) {
		int totalCheques = getTotalCheques();
		if (currentIndex >= totalCheques - 1) {
			return;
		}
		currentIndex++;
		loadCurrentCheque();
	}

	private void updateNavigationButtons() {
		int totalCheques = getTotalCheques();
		btnPrevious.setDisabled(currentIndex <= 0);
		btnNext.setDisabled(currentIndex >= totalCheques - 1);
		lblRecordPosition.setValue((currentIndex + 1) + " of " + totalCheques);
	}

	private void updateProgress() {
		int totalCheques = getTotalCheques();
		if (totalCheques <= 0) {
			lblProgress.setValue("0/0 (0%)");
			progressMeter.setValue(0);
			lblTotalCheques.setValue("0");
			return;
		}
		int processed = countProcessedCheques();
		int percentage = (processed * 100) / totalCheques;
		lblProgress.setValue(processed + "/" + totalCheques + " (" + percentage + "%)");
		progressMeter.setValue(percentage);
		lblTotalCheques.setValue(String.valueOf(totalCheques));
	}

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

	private void updateActionButtons() {
		if (currentIndex < 0 || currentIndex >= getTotalCheques()) {
			btnSaveAndNext.setDisabled(true);
			btnRejectRequest.setDisabled(true);
			return;
		}
		MicrRepairChequeDTO cheque = micrRepairCheques.get(currentIndex);
		boolean pending = isPendingMicrRepair(cheque);
		btnSaveAndNext.setDisabled(!pending);
		btnRejectRequest.setDisabled(!pending);
	}

	private void updateSubmitButton() {
		if (btnSubmitToDataEntry == null) {
			return;
		}
		btnSubmitToDataEntry.setDisabled(!areAllChequesProcessed());
	}

	private boolean areAllChequesProcessed() {
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

	public void onClick$btnSaveAndNext(Event event) {
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
		if (!isPendingMicrRepair(cheque)) {
			showWarning("This cheque has already been processed.");
			updateSubmitButton();
			return;
		}
		if (!validateMicrFields()) {
			return;
		}
		updateChequeFromScreen(cheque);
		cheque.setChequeStatus(STATUS_MICR_REPAIRED);
		try {
			if ("SCAN".equals(source)) {
				outwardMakerService.saveScanMicrRepair(cheque);
			} else {
				outwardMakerService.saveOutwardMicrRepair(cheque);
			}
			updateProgress();
			updateSubmitButton();
			if (currentIndex < totalCheques - 1) {
				currentIndex++;
				loadCurrentCheque();
				Clients.showNotification("MICR repair saved successfully.", Clients.NOTIFICATION_TYPE_INFO, null,
						"top_center", 1500);
			} else {
				updateSubmitButton();
				if (areAllChequesProcessed()) {
					Clients.showNotification(
							"All MICR repair cheques have been processed. Click Submit to complete.",
							Clients.NOTIFICATION_TYPE_INFO, null, "top_center", 2500);
				} else {
					Clients.showNotification("MICR repair saved successfully.", Clients.NOTIFICATION_TYPE_INFO, null,
							"top_center", 1500);
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
			cheque.setChequeStatus(STATUS_PENDING_MICR_REPAIR);
			updateProgress();
			updateSubmitButton();
			showError("Failed to save MICR repair: " + safe(e.getMessage()));
		}
	}

	private void updateChequeFromScreen(MicrRepairChequeDTO cheque) {
		cheque.setCityCode(safe(txtCityCode.getValue()).trim());
		cheque.setBankCode(safe(txtBankCode.getValue()).trim());
		cheque.setBranchCode(safe(txtBranchCode.getValue()).trim());
		cheque.setFullMicr(safe(txtCorrectedMicr.getValue()).trim());
	}

	private boolean validateMicrFields() {
		String city = safe(txtCityCode.getValue()).trim().toUpperCase();
		String bank = safe(txtBankCode.getValue()).trim().toUpperCase();
		String branch = safe(txtBranchCode.getValue()).trim().toUpperCase();
		String micr = safe(txtCorrectedMicr.getValue()).trim().toUpperCase();

		boolean cityValid = isValidMicrPart(city);
		boolean bankValid = isValidMicrPart(bank);
		boolean branchValid = isValidMicrPart(branch);
		boolean micrFormatValid = isValidCorrectedMicrFormat(micr);
		boolean micrMatchesParts = cityValid && bankValid && branchValid && micrFormatValid
				&& micr.equals(city + bank + branch);

		setMicrFieldError(txtCityCode, !cityValid);
		setMicrFieldError(txtBankCode, !bankValid);
		setMicrFieldError(txtBranchCode, !branchValid);
		setMicrFieldError(txtCorrectedMicr, !micrMatchesParts);

		if (city.isEmpty()) {
			showValidationMessage("City Code is required.");
			txtCityCode.setFocus(true);
			return false;
		}
		if (!cityValid) {
			showValidationMessage("City Code must contain exactly 3 numeric characters and cannot be 000.");
			txtCityCode.setFocus(true);
			return false;
		}
		if (bank.isEmpty()) {
			showValidationMessage("Bank Code is required.");
			txtBankCode.setFocus(true);
			return false;
		}
		if (!bankValid) {
			showValidationMessage("Bank Code must contain exactly 3 numeric characters and cannot be 000.");
			txtBankCode.setFocus(true);
			return false;
		}
		if (branch.isEmpty()) {
			showValidationMessage("Branch Code is required.");
			txtBranchCode.setFocus(true);
			return false;
		}
		if (!branchValid) {
			showValidationMessage("Branch Code must contain exactly 3 numeric characters and cannot be 000.");
			txtBranchCode.setFocus(true);
			return false;
		}
		if (micr.isEmpty()) {
			showValidationMessage("Corrected MICR Code is required.");
			txtCorrectedMicr.setFocus(true);
			return false;
		}
		if (!micrFormatValid) {
			showValidationMessage("Corrected MICR Code must contain exactly 9 numeric characters.");
			txtCorrectedMicr.setFocus(true);
			return false;
		}
		if (!micrMatchesParts) {
			showValidationMessage("Corrected MICR Code must match City Code + Bank Code + Branch Code.");
			txtCorrectedMicr.setFocus(true);
			return false;
		}
		return true;
	}

	public void onChange$txtCityCode(Event event) {
		if (syncingMicrFields) {
			return;
		}
		syncCorrectedMicrFromParts();
	}

	public void onChange$txtBankCode(Event event) {
		if (syncingMicrFields) {
			return;
		}
		syncCorrectedMicrFromParts();
	}

	public void onChange$txtBranchCode(Event event) {
		if (syncingMicrFields) {
			return;
		}
		syncCorrectedMicrFromParts();
	}

	public void onChange$txtCorrectedMicr(Event event) {
		if (syncingMicrFields) {
			return;
		}
		syncPartsFromCorrectedMicr();
	}

	private void syncCorrectedMicrFromParts() {
		if (syncingMicrFields) {
			return;
		}
		syncingMicrFields = true;
		try {
			String city = normalizeMicrValue(txtCityCode.getValue());
			String bank = normalizeMicrValue(txtBankCode.getValue());
			String branch = normalizeMicrValue(txtBranchCode.getValue());
			if (isValidMicrPart(city) && isValidMicrPart(bank) && isValidMicrPart(branch)) {
				txtCorrectedMicr.setValue(city + bank + branch);
			}
		} finally {
			syncingMicrFields = false;
		}
		refreshMicrValidationHighlight();
	}

	private void syncPartsFromCorrectedMicr() {
		if (syncingMicrFields) {
			return;
		}
		String micr = normalizeMicrValue(txtCorrectedMicr.getValue());
		syncingMicrFields = true;
		try {
			if (isValidCorrectedMicrFormat(micr)) {
				txtCityCode.setValue(micr.substring(0, 3));
				txtBankCode.setValue(micr.substring(3, 6));
				txtBranchCode.setValue(micr.substring(6, 9));
			}
		} finally {
			syncingMicrFields = false;
		}
		refreshMicrValidationHighlight();
	}
	private void refreshMicrValidationHighlight() {
		String city = normalizeMicrValue(txtCityCode.getValue());
		String bank = normalizeMicrValue(txtBankCode.getValue());
		String branch = normalizeMicrValue(txtBranchCode.getValue());
		String micr = normalizeMicrValue(txtCorrectedMicr.getValue());
		boolean cityValid = isValidMicrPart(city);
		boolean bankValid = isValidMicrPart(bank);
		boolean branchValid = isValidMicrPart(branch);
		boolean micrFormatValid = isValidCorrectedMicrFormat(micr);
		boolean micrMatchesParts = cityValid && bankValid && branchValid && micrFormatValid
				&& micr.equals(city + bank + branch);
		setMicrFieldError(txtCityCode, !cityValid);
		setMicrFieldError(txtBankCode, !bankValid);
		setMicrFieldError(txtBranchCode, !branchValid);
		setMicrFieldError(txtCorrectedMicr, !micrMatchesParts);
	}
	private void setMicrFieldError(Textbox textbox, boolean error) {
		if (textbox == null) {
			return;
		}
		if (textbox == txtCorrectedMicr) {
			textbox.setSclass(error ? "cts-input-text correction-field micr-code-field error-field"
					: "cts-input-text correction-field micr-code-field");
		} else {
			textbox.setSclass(error ? "cts-input-text error-field" : "cts-input-text");
		}
	}
	private String normalizeMicrValue(String value) {
		return safe(value).trim().toUpperCase();
	}
	private boolean isValidCorrectedMicrFormat(String value) {
		if (value == null || value.length() != 9) {
			return false;
		}
		return value.matches("[0-9]{9}");
	}
	private void showValidationMessage(String message) {
		Clients.showNotification(message, Clients.NOTIFICATION_TYPE_WARNING, null, "top_center", 2000);
	}
	private boolean isValidMicrPart(String value) {
		if (value == null) {
			return false;
		}
		if (value.length() != 3) {
			return false;
		}
		if (!value.matches("[0-9]{3}")) {
			return false;
		}
		if ("000".equals(value)) {
			return false;
		}
		return true;
	}
	private boolean isPendingMicrRepair(MicrRepairChequeDTO cheque) {
		if (cheque == null) {
			return false;
		}
		return STATUS_PENDING_MICR_REPAIR.equalsIgnoreCase(safe(cheque.getChequeStatus()));
	}
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
		txtModalRejectionRemark.setValue("");
		rejectRequestWindow.setVisible(true);
	}
	public void onClick$btnCancelReject(Event event) {
		cmbRejectReason.setValue("");
		txtModalRejectionRemark.setValue("");
		rejectRequestWindow.setVisible(false);
	}
	public void onClick$btnConfirmReject(Event event) {
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
			rejectRequestWindow.setVisible(false);
			showWarning("This cheque has already been processed.");
			return;
		}
		Comboitem selectedItem = (Comboitem) cmbRejectReason.getSelectedItem();
		if (selectedItem == null) {
			showWarning("Rejected reason is required.");
			cmbRejectReason.setFocus(true);
			return;
		}
		String reasonId = safe(selectedItem.getValue() == null ? null : selectedItem.getValue().toString()).trim();
		String reason = safe(selectedItem.getLabel()).trim();
		String remarks = safe(txtModalRejectionRemark.getValue()).trim();
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
			txtModalRejectionRemark.setFocus(true);
			return;
		}
		cheque.setReasonId(reasonId);
		cheque.setReason(reason);
		cheque.setRemarks(remarks);
		cheque.setChequeStatus(STATUS_MICR_REJECTION_PENDING);
		if (lblHeaderItemStatus != null) {
			lblHeaderItemStatus.setValue(STATUS_MICR_REJECTION_PENDING);
		}
		try {
			if ("SCAN".equals(source)) {
				outwardMakerService.saveScanMicrRepair(cheque);
			} else {
				outwardMakerService.saveOutwardMicrRepair(cheque);
			}
			rejectRequestWindow.setVisible(false);
			updateProgress();
			updateSubmitButton();
			updateActionButtons();
			Clients.showNotification("Rejection request saved successfully.", Clients.NOTIFICATION_TYPE_INFO, null,
					"top_center", 2000);
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
			if (lblHeaderItemStatus != null) {
				lblHeaderItemStatus.setValue(STATUS_PENDING_MICR_REPAIR);
			}
			updateProgress();
			updateSubmitButton();
			updateActionButtons();
			showError("Failed to save rejection request: " + safe(e.getMessage()));
		}
	}
	public void onClick$btnSubmitToDataEntry(Event event) {
		System.out.println("========== SUBMIT BUTTON CLICKED ==========");
		if (micrRepairCheques == null || micrRepairCheques.isEmpty()) {
			showError("No MICR repair cheques available.");
			return;
		}
		if (!areAllChequesProcessed()) {
			updateSubmitButton();
			showWarning("Please process all MICR repair cheques " + "before submitting.");
			return;
		}
		for (MicrRepairChequeDTO cheque : micrRepairCheques) {
			if (cheque == null) {
				continue;
			}
			String status = safe(cheque.getChequeStatus());
			if (STATUS_MICR_REJECTION_PENDING.equalsIgnoreCase(status)) {
				cheque.setChequeStatus(STATUS_MICR_REJECTED);
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
			btnSubmitToDataEntry.setDisabled(true);
			btnSaveAndNext.setDisabled(true);
			btnRejectRequest.setDisabled(true);
			Clients.showNotification("MICR repair submitted successfully.", Clients.NOTIFICATION_TYPE_INFO, null,
					"top_center", 2500);
			redirectToMicrRepairView();
		} catch (Exception e) {
			e.printStackTrace();
			for (MicrRepairChequeDTO cheque : micrRepairCheques) {
				if (cheque == null) {
					continue;
				}
				String status = safe(cheque.getChequeStatus());
				if (STATUS_PENDING_DATA_ENTRY.equalsIgnoreCase(status)) {
					cheque.setChequeStatus(STATUS_MICR_REPAIRED);
				} else if (STATUS_MICR_REJECTED.equalsIgnoreCase(status)) {
					cheque.setChequeStatus(STATUS_MICR_REJECTION_PENDING);
				}
			}
			updateProgress();
			updateSubmitButton();
			updateActionButtons();
			showError("Failed to submit MICR repair: " + safe(e.getMessage()));
		}
	}
	public void onClick$btnBackToList(Event event) {
		System.out.println("========== BACK TO LIST CLICKED ==========");
		redirectToMicrRepairView();
	}
	private void redirectToMicrRepairView() {
		Component root = outwardMicrRepairRoot.getDesktop().getFirstPage().getFirstRoot();
		Component mainContentArea = root.getFellowIfAny("mainContentArea", true);
		if (mainContentArea instanceof Include) {
			Include include = (Include) mainContentArea;
			include.setSrc("/outward/maker/micr-repair/micr-repair-view.zul");
			return;
		}
		Executions.sendRedirect("/outward/maker/micr-repair/micr-repair-view.zul");
	}
	private String safe(String value) {
		if (value == null) {
			return "";
		}
		return value;
	}
	private void showError(String message) {
		Clients.showNotification(message, Clients.NOTIFICATION_TYPE_ERROR, null, "top_center", 4000);
	}
	private void showWarning(String message) {
		Clients.showNotification(message, Clients.NOTIFICATION_TYPE_WARNING, null, "top_center", 0);
	}
}
