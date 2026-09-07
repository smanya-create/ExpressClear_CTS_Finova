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
import org.zkoss.zul.Label;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.iispl.cts.entity.RejectedReason;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.service.RejectedReasonService;
import com.iispl.cts.service.outward.OutwardChequeService;
import com.iispl.cts.serviceimpl.RejectedReasonServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardChequeServiceImpl;

public class OutwardChequeDataEntryController extends SelectorComposer<Component> {

	private static final long serialVersionUID = 1L;

	private Label outwardChequeDataEntryLblBatchId;
	private Label outwardChequeDataEntryLblTotal;
	private Label outwardChequeDataEntryLblEntered;
	private Label outwardChequeDataEntryLblRemaining;
	private Label outwardChequeDataEntryLblRecord;
	private Label outwardChequeDataEntryLblValidationStatus;
	private Label outwardChequeDataEntryLblImage;
	private Label outwardChequeDataEntryLblAccountValidation;

	private Textbox outwardChequeDataEntryTxtChequeNumber;
	private Textbox outwardChequeDataEntryTxtPayeeAccount;
	private Textbox outwardChequeDataEntryTxtAmount;
	private Textbox outwardChequeDataEntryTxtChequeDate;
	private Textbox outwardChequeDataEntryTxtMicrCode;
	private Textbox outwardChequeDataEntryTxtDraweeName;
	private Textbox outwardChequeDataEntryTxtPayeeName;

	private Button outwardChequeDataEntryBtnValidateAccount;
	private Button outwardChequeDataEntryBtnSaveNext;
	private Button outwardChequeDataEntryBtnReject;
	private Button outwardChequeDataEntryBtnReverse;

	private Window outwardChequeDataEntryRejectWin;
	private Combobox outwardChequeDataEntryCmbRejectReason;
	private Textbox outwardChequeDataEntryTxtRejectRemarks;
	private Button outwardChequeDataEntryBtnCancelReject;
	private Button outwardChequeDataEntryBtnConfirmReject;

	private final OutwardChequeService outwardChequeService;
	private final RejectedReasonService rejectedReasonService;

	private String outwardBatchId;
	private List<OutwardCheque> outwardChequeList;
	private int currentChequeIndex;
	private boolean accountValidated;

	public OutwardChequeDataEntryController() {
		outwardChequeService = new OutwardChequeServiceImpl();
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

		outwardChequeDataEntryLblImage = (Label) component.getFellow("outwardChequeDataEntryLblImage");

		outwardChequeDataEntryLblAccountValidation = (Label) component
				.getFellow("outwardChequeDataEntryLblAccountValidation");

		outwardChequeDataEntryTxtChequeNumber = (Textbox) component.getFellow("outwardChequeDataEntryTxtChequeNumber");

		outwardChequeDataEntryTxtPayeeAccount = (Textbox) component.getFellow("outwardChequeDataEntryTxtPayeeAccount");

		outwardChequeDataEntryTxtAmount = (Textbox) component.getFellow("outwardChequeDataEntryTxtAmount");

		outwardChequeDataEntryTxtChequeDate = (Textbox) component.getFellow("outwardChequeDataEntryTxtChequeDate");

		outwardChequeDataEntryTxtMicrCode = (Textbox) component.getFellow("outwardChequeDataEntryTxtMicrCode");

		outwardChequeDataEntryTxtDraweeName = (Textbox) component.getFellow("outwardChequeDataEntryTxtDraweeName");

		outwardChequeDataEntryTxtPayeeName = (Textbox) component.getFellow("outwardChequeDataEntryTxtPayeeName");

		outwardChequeDataEntryBtnValidateAccount = (Button) component
				.getFellow("outwardChequeDataEntryBtnValidateAccount");

		outwardChequeDataEntryBtnSaveNext = (Button) component.getFellow("outwardChequeDataEntryBtnSaveNext");

		outwardChequeDataEntryBtnReject = (Button) component.getFellow("outwardChequeDataEntryBtnReject");

		outwardChequeDataEntryBtnReverse = (Button) component.getFellow("outwardChequeDataEntryBtnReverse");

		outwardChequeDataEntryRejectWin = (Window) component.getFellow("outwardChequeDataEntryRejectWin");

		outwardChequeDataEntryCmbRejectReason = (Combobox) outwardChequeDataEntryRejectWin
				.getFellow("outwardChequeDataEntryCmbRejectReason");

		outwardChequeDataEntryTxtRejectRemarks = (Textbox) outwardChequeDataEntryRejectWin
				.getFellow("outwardChequeDataEntryTxtRejectRemarks");

		outwardChequeDataEntryBtnCancelReject = (Button) outwardChequeDataEntryRejectWin
				.getFellow("outwardChequeDataEntryBtnCancelReject");

		outwardChequeDataEntryBtnConfirmReject = (Button) outwardChequeDataEntryRejectWin
				.getFellow("outwardChequeDataEntryBtnConfirmReject");

		outwardChequeDataEntryRejectWin.setVisible(false);

		outwardBatchId = resolveBatchId();

		if (outwardBatchId.isEmpty()) {

			outwardChequeDataEntryLblBatchId.setValue("-");

			outwardChequeDataEntryLblTotal.setValue("0");
			outwardChequeDataEntryLblEntered.setValue("0");
			outwardChequeDataEntryLblRemaining.setValue("0");
			outwardChequeDataEntryLblRecord.setValue("Record 0 of 0");

			clearChequeFields();

			return;
		}

		outwardChequeDataEntryLblBatchId.setValue(outwardBatchId);

		outwardChequeDataEntryBtnValidateAccount.addEventListener("onClick", event -> validateAccount());

		outwardChequeDataEntryBtnSaveNext.addEventListener("onClick", event -> saveAndNext());

		outwardChequeDataEntryBtnReject.addEventListener("onClick", event -> openRejectWindow());

		outwardChequeDataEntryBtnCancelReject.addEventListener("onClick", event -> closeRejectWindow());

		outwardChequeDataEntryBtnConfirmReject.addEventListener("onClick", event -> requestReject());

		outwardChequeDataEntryBtnReverse.addEventListener("onClick", event -> showReverseImage());

		outwardChequeDataEntryTxtPayeeAccount.addEventListener("onChange", event -> resetAccountValidation());

		loadRejectedReasons();
		loadCheques();
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

	private void loadCheques() {

		if (outwardBatchId.isEmpty()) {

			outwardChequeList = new ArrayList<>();

			updateSummary();

			return;
		}

		try {

			outwardChequeList = outwardChequeService.getChequesByBatchId(outwardBatchId);

		} catch (Exception e) {

			outwardChequeList = new ArrayList<>();

			updateSummary();

			return;
		}

		if (outwardChequeList == null) {

			outwardChequeList = new ArrayList<>();
		}

		currentChequeIndex = 0;

		updateSummary();

		if (!outwardChequeList.isEmpty()) {

			loadCurrentCheque();

		} else {

			clearChequeFields();
		}
	}

	private void updateSummary() {

		int total = outwardChequeList == null ? 0 : outwardChequeList.size();

		int entered = 0;

		if (outwardChequeList != null) {

			for (OutwardCheque cheque : outwardChequeList) {

				String status = cheque.getChequeStatus();

				if (status != null && !status.trim().isEmpty()
						&& !"PENDING_DATA_ENTRY".equalsIgnoreCase(status.trim())) {

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

		if (total > 0) {

			outwardChequeDataEntryLblRecord.setValue("Record " + (currentChequeIndex + 1) + " of " + total);

		} else {

			outwardChequeDataEntryLblRecord.setValue("Record 0 of 0");
		}
	}

	private void loadCurrentCheque() {

		if (outwardChequeList == null || outwardChequeList.isEmpty() || currentChequeIndex < 0
				|| currentChequeIndex >= outwardChequeList.size()) {

			clearChequeFields();

			return;
		}

		OutwardCheque cheque = outwardChequeList.get(currentChequeIndex);

		outwardChequeDataEntryTxtChequeNumber.setValue(getValue(cheque.getChequeNumber()));

		outwardChequeDataEntryTxtPayeeAccount.setValue(getValue(cheque.getPayeeAccountNumber()));

		outwardChequeDataEntryTxtAmount
				.setValue(cheque.getChequeAmount() == null ? "" : cheque.getChequeAmount().toPlainString());

		outwardChequeDataEntryTxtChequeDate.setValue(formatDate(cheque));

		outwardChequeDataEntryTxtMicrCode.setValue(getValue(cheque.getMicrCode()));

		outwardChequeDataEntryTxtDraweeName.setValue(getValue(cheque.getDraweeName()));

		outwardChequeDataEntryTxtPayeeName.setValue(getValue(cheque.getPayeeName()));

		outwardChequeDataEntryLblImage.setValue("Cheque Image");

		resetAccountValidation();

		outwardChequeDataEntryLblValidationStatus.setVisible(false);

		updateSummary();
	}

	private void validateAccount() {

		String accountNumber = outwardChequeDataEntryTxtPayeeAccount.getValue();

		if (accountNumber == null) {
			accountNumber = "";
		}

		accountNumber = accountNumber.trim();

		if (accountNumber.isEmpty()) {

			showAccountValidation(false, "✗ Account is Invalid");

			return;
		}

		boolean numeric = Pattern.matches("^[0-9]+$", accountNumber);

		boolean validLength = accountNumber.length() >= 9 && accountNumber.length() <= 18;

		boolean valid = numeric && validLength;

		if (valid) {

			showAccountValidation(true, "✓ Account is Verified");

		} else {

			showAccountValidation(false, "✗ Account is Invalid");
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

		outwardChequeDataEntryLblValidationStatus.setVisible(false);
	}

	private void saveAndNext() {

		if (outwardChequeList == null || outwardChequeList.isEmpty()) {

			return;
		}

		if (!accountValidated) {

			validateAccount();

			return;
		}

		String amount = outwardChequeDataEntryTxtAmount.getValue();

		if (amount == null || amount.trim().isEmpty()) {

			return;
		}

		BigDecimal chequeAmount;

		try {

			chequeAmount = new BigDecimal(amount.trim());

			if (chequeAmount.signum() < 0) {
				return;
			}

		} catch (NumberFormatException e) {

			return;
		}

		String accountNumber = outwardChequeDataEntryTxtPayeeAccount.getValue();

		if (accountNumber == null || accountNumber.trim().isEmpty()) {

			return;
		}

		OutwardCheque cheque = outwardChequeList.get(currentChequeIndex);

		cheque.setPayeeAccountNumber(accountNumber.trim());

		cheque.setChequeAmount(chequeAmount);

		if (currentChequeIndex < outwardChequeList.size() - 1) {

			currentChequeIndex++;

			loadCurrentCheque();

		} else {

			outwardChequeDataEntryLblValidationStatus.setValue("COMPLETED");

			outwardChequeDataEntryLblValidationStatus.setVisible(true);
		}
	}

	private void loadRejectedReasons() {

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
	}

	private void openRejectWindow() {

		if (outwardChequeList == null || outwardChequeList.isEmpty()) {

			return;
		}

		outwardChequeDataEntryCmbRejectReason.setSelectedItem(null);

		outwardChequeDataEntryTxtRejectRemarks.setValue("");

		outwardChequeDataEntryRejectWin.setVisible(true);

		outwardChequeDataEntryRejectWin.doModal();
	}

	private void closeRejectWindow() {

		outwardChequeDataEntryRejectWin.setVisible(false);
	}

	private void requestReject() {

		if (outwardChequeDataEntryCmbRejectReason.getSelectedItem() == null) {

			return;
		}

		String remarks = outwardChequeDataEntryTxtRejectRemarks.getValue();

		if (remarks == null || remarks.trim().isEmpty()) {

			return;
		}

		closeRejectWindow();
	}

	private void showReverseImage() {

		outwardChequeDataEntryLblImage.setValue("Cheque Back Image");
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

		outwardChequeDataEntryLblValidationStatus.setVisible(false);
	}

	private String formatDate(OutwardCheque cheque) {

		if (cheque.getChequeDate() == null) {
			return "";
		}

		return new SimpleDateFormat("dd-MM-yyyy").format(cheque.getChequeDate());
	}

	private String getValue(String value) {

		return value == null ? "" : value;
	}
}