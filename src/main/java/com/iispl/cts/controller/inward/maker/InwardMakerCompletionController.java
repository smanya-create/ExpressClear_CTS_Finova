package com.iispl.cts.controller.inward.maker;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.stream.Collectors;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Div;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

import com.iispl.cts.entity.inward.InwardBatch;
import com.iispl.cts.entity.inward.InwardCheque;
import com.iispl.cts.service.inward.InwardBatchService;
import com.iispl.cts.service.inward.InwardChequeService;
import com.iispl.cts.serviceimpl.AuditServiceImpl;
import com.iispl.cts.serviceimpl.inward.InwardBatchServiceImpl;
import com.iispl.cts.serviceimpl.inward.InwardChequeServiceImpl;

public class InwardMakerCompletionController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // Services
    private final InwardBatchService batchService = new InwardBatchServiceImpl();
    private final InwardChequeService chequeService = new InwardChequeServiceImpl();

    // Top Metadata Card
    private Label lblBatchId;
    private Label lblSource;
    private Label lblTotalCheques;
    private Label lblReceivedDate;
    private Label lblReceivedSession;
    private Label lblBatchStatus;

    // Summary KPI Tiles
    private Label kpiTotalCount;
    private Label kpiTotalAmount;
    private Label kpiAcceptedCount;
    private Label kpiAcceptedAmount;
    private Label kpiRejectedCount;
    private Label kpiRejectedAmount;
    private Label kpiCorrectionsCount;

    // Checklist Labels
    private Label chkMicrStatus;
    private Label chkDataEntryStatus;
    private Label chkRejectionStatus;
    private Label chkBalanceStatus;

    // Instrument Table & Filter Buttons
    private Listbox lbBatchInstruments;
    private Button btnTabAll;
    private Button btnTabAccepted;
    private Button btnTabRejected;

    // Action Buttons
    private Button btnBackToReview;
    private Button btnSaveAndExit;
    private Button btnSubmitToChecker;

    // Modal Components
    private Div winSubmitConfirmModal;
    private Label lblModalBatchId;
    private Label lblModalAcceptedCount;
    private Label lblModalRejectedCount;
    private Textbox txtMakerSignoffRemarks;
    private Button btnCancelModalSubmit;
    private Button btnConfirmModalSubmit;

    private String currentBatchId = "BAT1001";
    private List<InwardCheque> batchCheques;
    private String currentTabFilter = "ALL";

    private final DecimalFormat currencyFormat = new DecimalFormat("₹ #,##0.00");

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        String paramBatch = execution.getParameter("batchId");
        if (paramBatch != null && !paramBatch.trim().isEmpty()) {
            currentBatchId = paramBatch.trim();
        } else {
            Object sessionBatch = Sessions.getCurrent().getAttribute("ACTIVE_BATCH_ID");
            if (sessionBatch != null) {
                currentBatchId = sessionBatch.toString();
            }
        }

        loadBatchData();
    }

    private void loadBatchData() {
        InwardBatch batch = batchService.getBatchById(currentBatchId);
        if (batch == null) {
            Messagebox.show("Batch record not found: " + currentBatchId, "Error", Messagebox.OK, Messagebox.ERROR);
            return;
        }

        // Fetch Cheques for the active Batch
        this.batchCheques = chequeService.getChequesByBatchAndStatus(currentBatchId, null);

        // Top Header Metadata
        if (lblBatchId != null) lblBatchId.setValue(batch.getInwardBatchId());
        if (lblSource != null) lblSource.setValue("CHI");
        if (lblReceivedSession != null) lblReceivedSession.setValue("Session 1");

        if (lblTotalCheques != null) {
            int total = (batchCheques != null) ? batchCheques.size() : batch.getActualChequeCount();
            lblTotalCheques.setValue(String.valueOf(total));
        }

        if (lblReceivedDate != null && batch.getUploadedAt() != null) {
            lblReceivedDate.setValue(new SimpleDateFormat("dd-MM-yyyy").format(batch.getUploadedAt()));
        }

        if (lblBatchStatus != null) {
            String bStatus = batch.getBatchStatus() != null ? batch.getBatchStatus() : "PROCESSING";
            lblBatchStatus.setValue(bStatus.replace("_", " "));
            if ("READY_FOR_COMPLETION".equalsIgnoreCase(bStatus) || "READY_FOR_CHECKER".equalsIgnoreCase(bStatus)) {
                lblBatchStatus.setSclass("cts-badge-ready");
            } else {
                lblBatchStatus.setSclass("cts-badge-pending");
            }
        }

        // Checklist Dynamic Checks: MICR Repair Status
        if (chkMicrStatus != null) {
            long micrPending = (batchCheques != null) ? batchCheques.stream()
                .filter(c -> "MICR_REPAIR_REQUIRED".equalsIgnoreCase(c.getChequeStatus()))
                .count() : 0;
            if (micrPending > 0) {
                chkMicrStatus.setValue("! " + micrPending + " Items in MICR Repair");
                chkMicrStatus.setStyle("font-size: 12px; font-weight: 700; color: #dc2626;");
            } else {
                chkMicrStatus.setValue("✔ Completed");
                chkMicrStatus.setStyle("font-size: 12px; font-weight: 700; color: #16a34a;");
            }
        }

        calculateAndRenderSummaries(batch);
        populateInstrumentTable();
    }

    private void calculateAndRenderSummaries(InwardBatch batch) {
        if (batchCheques == null) return;

        int totalCount = batchCheques.size();
        int acceptedCount = 0;
        int rejectedCount = 0;
        int pendingCount = 0;

        BigDecimal acceptedAmt = BigDecimal.ZERO;
        BigDecimal rejectedAmt = BigDecimal.ZERO;
        BigDecimal totalAmt = BigDecimal.ZERO;

        for (InwardCheque c : batchCheques) {
            BigDecimal amt = c.getChequeAmount() != null ? c.getChequeAmount() : BigDecimal.ZERO;
            totalAmt = totalAmt.add(amt);

            String status = c.getChequeStatus();
            if ("ACCEPTED".equalsIgnoreCase(status) || "DATA_ENTRY_COMPLETED".equalsIgnoreCase(status)) {
                acceptedCount++;
                acceptedAmt = acceptedAmt.add(amt);
            } else if ("REJECTED".equalsIgnoreCase(status)) {
                rejectedCount++;
                rejectedAmt = rejectedAmt.add(amt);
            } else {
                pendingCount++;
            }
        }

        // Summary KPI counts and amounts
        if (kpiTotalCount != null) kpiTotalCount.setValue(String.valueOf(totalCount));
        if (kpiTotalAmount != null) kpiTotalAmount.setValue(currencyFormat.format(totalAmt));

        if (kpiAcceptedCount != null) kpiAcceptedCount.setValue(String.valueOf(acceptedCount));
        if (kpiAcceptedAmount != null) kpiAcceptedAmount.setValue(currencyFormat.format(acceptedAmt));

        if (kpiRejectedCount != null) kpiRejectedCount.setValue(String.valueOf(rejectedCount));
        if (kpiRejectedAmount != null) kpiRejectedAmount.setValue(currencyFormat.format(rejectedAmt));

        if (kpiCorrectionsCount != null) kpiCorrectionsCount.setValue(String.valueOf(acceptedCount));

        // Checklist Data Entry Status
        boolean hasPending = (pendingCount > 0);
        if (chkDataEntryStatus != null) {
            if (hasPending) {
                chkDataEntryStatus.setValue("! " + pendingCount + " Items Pending");
                chkDataEntryStatus.setStyle("font-size: 12px; font-weight: 700; color: #dc2626;");
            } else {
                chkDataEntryStatus.setValue("✔ Completed");
                chkDataEntryStatus.setStyle("font-size: 12px; font-weight: 700; color: #16a34a;");
            }
        }

        // Checklist Rejection Count
        if (chkRejectionStatus != null) {
            chkRejectionStatus.setValue(rejectedCount + " require Checker decision");
        }

        // Checklist Balance Control Total
        if (chkBalanceStatus != null) {
            if (!hasPending) {
                chkBalanceStatus.setValue("✔ Reconciled");
                chkBalanceStatus.setStyle("font-size: 12px; font-weight: 700; color: #16a34a;");
            } else {
                chkBalanceStatus.setValue("! Pending Decisions");
                chkBalanceStatus.setStyle("font-size: 12px; font-weight: 700; color: #d97706;");
            }
        }

        // Submission Gate
        if (btnSubmitToChecker != null) {
            btnSubmitToChecker.setDisabled(hasPending);
            if (hasPending) {
                btnSubmitToChecker.setStyle("background: #94a3b8; border: 1px solid #94a3b8; color: #ffffff; font-size: 13px; font-weight: 700; border-radius: 6px; padding: 8px 22px; cursor: not-allowed;");
            } else {
                btnSubmitToChecker.setStyle("background: #2563eb; border: 1px solid #2563eb; color: #ffffff; font-size: 13px; font-weight: 700; border-radius: 6px; padding: 8px 22px; cursor: pointer;");
            }
        }

        highlightTab(btnTabAll);
    }

    private void populateInstrumentTable() {
        if (lbBatchInstruments == null || batchCheques == null) return;
        lbBatchInstruments.getItems().clear();

        List<InwardCheque> filtered = batchCheques.stream().filter(c -> {
            if ("ACCEPTED".equalsIgnoreCase(currentTabFilter)) {
                return "ACCEPTED".equalsIgnoreCase(c.getChequeStatus()) || "DATA_ENTRY_COMPLETED".equalsIgnoreCase(c.getChequeStatus());
            } else if ("REJECTED".equalsIgnoreCase(currentTabFilter)) {
                return "REJECTED".equalsIgnoreCase(c.getChequeStatus());
            }
            return true;
        }).collect(Collectors.toList());

        for (InwardCheque item : filtered) {
            Listitem li = new Listitem();

            li.appendChild(new Listcell(item.getChequeNumber() != null ? item.getChequeNumber() : "-"));
            li.appendChild(new Listcell(item.getDraweeAccountNumber() != null ? item.getDraweeAccountNumber() : "-"));
            li.appendChild(new Listcell(item.getDraweeName() != null ? item.getDraweeName() : "-"));
            li.appendChild(new Listcell(item.getPayeeName() != null ? item.getPayeeName() : "-"));

            String amtStr = item.getChequeAmount() != null ? currencyFormat.format(item.getChequeAmount()) : "₹ 0.00";
            li.appendChild(new Listcell(amtStr));

            Listcell cellStatus = new Listcell();
            String st = item.getChequeStatus() != null ? item.getChequeStatus() : "PENDING";
            Label lbl = new Label(st);
            if ("ACCEPTED".equalsIgnoreCase(st) || "DATA_ENTRY_COMPLETED".equalsIgnoreCase(st)) {
                lbl.setStyle("background-color: #dcfce7; color: #166534; padding: 3px 8px; border-radius: 10px; font-size: 11px; font-weight: 700;");
            } else if ("REJECTED".equalsIgnoreCase(st)) {
                lbl.setStyle("background-color: #fee2e2; color: #991b1b; padding: 3px 8px; border-radius: 10px; font-size: 11px; font-weight: 700;");
            } else {
                lbl.setStyle("background-color: #fef3c7; color: #b45309; padding: 3px 8px; border-radius: 10px; font-size: 11px; font-weight: 700;");
            }
            cellStatus.appendChild(lbl);
            li.appendChild(cellStatus);

            String remark = "REJECTED".equalsIgnoreCase(st) ? "Requested return to Presenting Bank" : "Verified";
            li.appendChild(new Listcell(remark));

            lbBatchInstruments.appendChild(li);
        }
    }

    public void onClick$btnTabAll() {
        currentTabFilter = "ALL";
        highlightTab(btnTabAll);
        populateInstrumentTable();
    }

    public void onClick$btnTabAccepted() {
        currentTabFilter = "ACCEPTED";
        highlightTab(btnTabAccepted);
        populateInstrumentTable();
    }

    public void onClick$btnTabRejected() {
        currentTabFilter = "REJECTED";
        highlightTab(btnTabRejected);
        populateInstrumentTable();
    }

    private void highlightTab(Button activeBtn) {
        String defaultStyle = "font-size: 11px; padding: 5px 14px; border-radius: 4px; border: 1px solid #cbd5e1; background: #ffffff; color: #334155; font-weight: 600; cursor: pointer;";
        if (btnTabAll != null) btnTabAll.setStyle(defaultStyle);
        if (btnTabAccepted != null) btnTabAccepted.setStyle(defaultStyle);
        if (btnTabRejected != null) btnTabRejected.setStyle(defaultStyle);

        if (activeBtn != null) {
            activeBtn.setStyle("font-size: 11px; padding: 5px 14px; border-radius: 4px; border: 1px solid #2563eb; background: #eff6ff; color: #2563eb; font-weight: 700; cursor: pointer;");
        }
    }

    public void onClick$btnBackToReview() {
        Executions.sendRedirect("/inward/maker/data-entry/data-entry.zul?batchId=" + currentBatchId);
    }

    public void onClick$btnSaveAndExit() {
        Executions.sendRedirect("/inward/maker/index.zul");
    }

    public void onClick$btnSubmitToChecker() {
        long accepted = (batchCheques != null) ? batchCheques.stream()
            .filter(c -> "ACCEPTED".equalsIgnoreCase(c.getChequeStatus()) || "DATA_ENTRY_COMPLETED".equalsIgnoreCase(c.getChequeStatus()))
            .count() : 0;
        long rejected = (batchCheques != null) ? batchCheques.stream()
            .filter(c -> "REJECTED".equalsIgnoreCase(c.getChequeStatus()))
            .count() : 0;

        if (lblModalBatchId != null) lblModalBatchId.setValue(currentBatchId);
        if (lblModalAcceptedCount != null) lblModalAcceptedCount.setValue(String.valueOf(accepted));
        if (lblModalRejectedCount != null) lblModalRejectedCount.setValue(String.valueOf(rejected));

        if (winSubmitConfirmModal != null) {
            winSubmitConfirmModal.setVisible(true);
        }
    }

    public void onClick$btnCancelModalSubmit() {
        if (winSubmitConfirmModal != null) {
            winSubmitConfirmModal.setVisible(false);
        }
    }

    public void onClick$btnConfirmModalSubmit() {
        if (winSubmitConfirmModal != null) {
            winSubmitConfirmModal.setVisible(false);
        }

        // Transition status to Checker Queue
        batchService.updateBatchStatus(currentBatchId, "READY_FOR_VERIFICATION");

        // Audit Trail Record
        String remarks = (txtMakerSignoffRemarks != null && txtMakerSignoffRemarks.getValue() != null)
            ? txtMakerSignoffRemarks.getValue().trim() : "";
        AuditServiceImpl.getInstance().log("INWARD_MAKER", "BATCH_SUBMISSION", 
            "Batch " + currentBatchId + " submitted to Checker. Remarks: " + remarks, "SUCCESS");

        Messagebox.show("Batch " + currentBatchId + " has been successfully submitted to the Checker.", 
            "Submission Successful", Messagebox.OK, Messagebox.INFORMATION, e -> {
                Executions.sendRedirect("/inward/maker/index.zul");
            });
    }
}