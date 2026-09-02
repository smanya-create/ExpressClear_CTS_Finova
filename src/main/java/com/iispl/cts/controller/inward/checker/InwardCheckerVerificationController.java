package com.iispl.cts.controller.inward.checker;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;

public class InwardCheckerVerificationController extends GenericForwardComposer<Component> {
	
	private Label lblBatchId;
	private Label lblTotalCheques;
	private Label lblChequeNumber;
	private Label lblChequeStatus;
	private Label lblReceivedDate;
	private Label lblVerification;

	private Label lblMicrCode;
	private Label lblBankCode;
	private Label lblBranchCode;

	private Label lblPresentingBank;
	private Label lblVerificationChequeNumber;
	private Label lblAmount;

	private Label lblAccountNumber;
	private Label lblAccountHolder;
	private Label lblAccountBalance;

	private Button btnPrevious;
	private Button btnNext;
	private Button btnRunCbsValidation;
	private Button btnAccept;
	private Button btnReturn;
	private Button btnSendBack;

	private Label lblChequePosition;
	

	
	
	
}