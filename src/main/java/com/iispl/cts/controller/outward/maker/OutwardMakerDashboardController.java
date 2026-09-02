package com.iispl.cts.controller.outward.maker;

import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Desktop;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Cell;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Grid;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Row;
import org.zkoss.zul.Rows;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.service.outward.OutwardBatchService;
import com.iispl.cts.service.outward.OutwardChequeService;
import com.iispl.cts.serviceimpl.outward.OutwardBatchServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardChequeServiceImpl;

public class OutwardMakerDashboardController extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;
    private static final long BATCH_DETAILS_TIMEOUT = 10000L;

    private Textbox outwardMakerTxtBatchId;
    private Combobox outwardMakerCmbStatus;
    private Button outwardMakerBtnSearch;
    private Button outwardMakerBtnClear;
    private Button outwardMakerBtnCloseBatchDetails;

    private Rows outwardMakerRowsBatchDetails;
    private Vlayout outwardMakerVlayoutEmptyState;

    private Window outwardMakerWinBatchDetails;

    private Label outwardMakerLblBatchId;
    private Label outwardMakerLblBatchReference;
    private Label outwardMakerLblBatchStatus;
    private Label outwardMakerLblUploadedBy;
    private Label outwardMakerLblUploadedAt;
    private Label outwardMakerLblModalTotalCheques;
    private Label outwardMakerLblModalTotalAmount;
    private Label outwardMakerLblBatchLoading;

    private Grid outwardMakerGridChequeDetails;

    private final OutwardBatchService outwardBatchService;
    private final OutwardChequeService outwardChequeService;

    private ListModelList<String> outwardMakerStatusModel;

    public OutwardMakerDashboardController() {
        outwardBatchService = new OutwardBatchServiceImpl();
        outwardChequeService = new OutwardChequeServiceImpl();
    }

    @Override
    public void doAfterCompose(Component component) throws Exception {

        super.doAfterCompose(component);

        outwardMakerTxtBatchId =
                (Textbox) component.getFellow(
                        "outwardMakerTxtBatchId"
                );

        outwardMakerCmbStatus =
                (Combobox) component.getFellow(
                        "outwardMakerCmbStatus"
                );

        outwardMakerBtnSearch =
                (Button) component.getFellow(
                        "outwardMakerBtnSearch"
                );

        outwardMakerBtnClear =
                (Button) component.getFellow(
                        "outwardMakerBtnClear"
                );

        outwardMakerRowsBatchDetails =
                (Rows) component.getFellow(
                        "outwardMakerRowsBatchDetails"
                );

        outwardMakerVlayoutEmptyState =
                (Vlayout) component.getFellow(
                        "outwardMakerVlayoutEmptyState"
                );

        outwardMakerWinBatchDetails =
                (Window) component.getFellow(
                        "outwardMakerWinBatchDetails"
                );

        outwardMakerLblBatchId =
                (Label) outwardMakerWinBatchDetails.getFellow(
                        "outwardMakerLblBatchId"
                );

        outwardMakerLblBatchReference =
                (Label) outwardMakerWinBatchDetails.getFellow(
                        "outwardMakerLblBatchReference"
                );

        outwardMakerLblBatchStatus =
                (Label) outwardMakerWinBatchDetails.getFellow(
                        "outwardMakerLblBatchStatus"
                );

        outwardMakerLblUploadedBy =
                (Label) outwardMakerWinBatchDetails.getFellow(
                        "outwardMakerLblUploadedBy"
                );

        outwardMakerLblUploadedAt =
                (Label) outwardMakerWinBatchDetails.getFellow(
                        "outwardMakerLblUploadedAt"
                );

        outwardMakerLblModalTotalCheques =
                (Label) outwardMakerWinBatchDetails.getFellow(
                        "outwardMakerLblModalTotalCheques"
                );

        outwardMakerLblModalTotalAmount =
                (Label) outwardMakerWinBatchDetails.getFellow(
                        "outwardMakerLblModalTotalAmount"
                );

        outwardMakerLblBatchLoading =
                (Label) outwardMakerWinBatchDetails.getFellow(
                        "outwardMakerLblBatchLoading"
                );

        outwardMakerBtnCloseBatchDetails =
                (Button) outwardMakerWinBatchDetails.getFellow(
                        "outwardMakerBtnCloseBatchDetails"
                );

        outwardMakerGridChequeDetails =
                (Grid) outwardMakerWinBatchDetails.getFellow(
                        "outwardMakerGridChequeDetails"
                );

        initializeStatusFilter();

        outwardMakerBtnSearch.addEventListener(
                "onClick",
                event -> searchBatches()
        );

        outwardMakerBtnClear.addEventListener(
                "onClick",
                event -> clearSearch()
        );

        outwardMakerBtnCloseBatchDetails.addEventListener(
                "onClick",
                event -> closeBatchDetails()
        );

        loadRecentBatches();
    }

    private void initializeStatusFilter() {

        outwardMakerStatusModel =
                new ListModelList<>(
                        Arrays.asList(
                                "All Status",
                                "Pending",
                                "Processing",
                                "Completed",
                                "Rejected"
                        )
                );

        outwardMakerStatusModel
                .addToSelection("All Status");

        outwardMakerCmbStatus
                .setModel(
                        outwardMakerStatusModel
                );

        outwardMakerCmbStatus
                .setPopupWidth("280px");

        outwardMakerCmbStatus
                .setReadonly(true);

        outwardMakerCmbStatus
                .setAutodrop(false);
    }

    private void searchBatches() {

        String batchId =
                outwardMakerTxtBatchId.getValue();

        if (batchId != null) {
            batchId = batchId.trim();
        }

        String status =
                getSelectedStatus();

        try {

            List<OutwardBatch> batches =
                    outwardBatchService.searchBatches(
                            batchId,
                            status
                    );

            renderBatches(batches);

        } catch (Exception e) {

            renderBatches(null);
        }
    }

    private String getSelectedStatus() {

        if (outwardMakerStatusModel == null) {
            return "";
        }

        if (outwardMakerStatusModel
                .getSelection()
                .isEmpty()) {

            return "";
        }

        String selectedStatus =
                outwardMakerStatusModel
                        .getSelection()
                        .iterator()
                        .next();

        if (selectedStatus == null ||
                selectedStatus.equals("All Status")) {

            return "";
        }

        return selectedStatus.toUpperCase();
    }

    private void clearSearch() {

        outwardMakerTxtBatchId
                .setValue("");

        outwardMakerStatusModel
                .clearSelection();

        outwardMakerStatusModel
                .addToSelection(
                        "All Status"
                );

        loadRecentBatches();
    }

    private void loadRecentBatches() {

        try {

            List<OutwardBatch> batches =
                    outwardBatchService
                            .getRecentBatches();

            renderBatches(batches);

        } catch (Exception e) {

            renderBatches(null);
        }
    }

    private void renderBatches(
            List<OutwardBatch> batches) {

        outwardMakerRowsBatchDetails
                .getChildren()
                .clear();

        if (batches == null ||
                batches.isEmpty()) {

            outwardMakerVlayoutEmptyState
                    .setVisible(true);

            return;
        }

        outwardMakerVlayoutEmptyState
                .setVisible(false);

        for (OutwardBatch batch : batches) {

            createBatchRow(batch);
        }
    }

    private void createBatchRow(
            final OutwardBatch batch) {

        Row row =
                new Row();

        Label batchIdLabel =
                new Label(
                        getValue(
                                batch.getOutwardBatchId()
                        )
                );

        batchIdLabel.setSclass(
                "outward-maker-batch-id"
        );

        Label chequeCountLabel =
                new Label(
                        String.valueOf(
                                batch.getActualChequeCount()
                        )
                );

        Label totalAmountLabel =
                new Label(
                        formatAmount(
                                batch.getActualTotalAmount()
                        )
                );

        Label statusLabel =
                new Label(
                        getValue(
                                batch.getBatchStatus()
                        )
                );

        statusLabel.setSclass(
                "outward-maker-status "
                + getStatusClass(
                        batch.getBatchStatus()
                )
        );

        Button viewButton =
                new Button(
                        "View Batch"
                );

        viewButton.setSclass(
                "outward-maker-view-button"
        );

        viewButton.addEventListener(
                "onClick",
                event ->
                        openBatchDetails(
                                batch
                        )
        );

        row.appendChild(
                batchIdLabel
        );

        row.appendChild(
                chequeCountLabel
        );

        row.appendChild(
                totalAmountLabel
        );

        row.appendChild(
                statusLabel
        );

        row.appendChild(
                viewButton
        );

        outwardMakerRowsBatchDetails
                .appendChild(row);
    }

    private void openBatchDetails(
            final OutwardBatch selectedBatch) {

        if (selectedBatch == null ||
                selectedBatch.getOutwardBatchId() == null) {

            return;
        }

        final String batchId =
                selectedBatch
                        .getOutwardBatchId();

        prepareLoadingState(
                batchId
        );

        outwardMakerWinBatchDetails
                .setVisible(true);

        outwardMakerWinBatchDetails
                .doModal();

        final Desktop desktop =
                outwardMakerWinBatchDetails
                        .getDesktop();

        final AtomicBoolean completed =
                new AtomicBoolean(false);

        Thread batchDetailsThread =
                new Thread(() -> {

                    BatchDetailsResult result =
                            new BatchDetailsResult();

                    try {

                        OutwardBatch batch =
                                outwardBatchService
                                        .getBatchById(
                                                batchId
                                        );

                        List<OutwardCheque> cheques =
                                outwardChequeService
                                        .getChequesByBatchId(
                                                batchId
                                        );

                        int totalChequeCount =
                                outwardChequeService
                                        .getTotalChequeCountByBatchId(
                                                batchId
                                        );

                        BigDecimal totalChequeAmount =
                                outwardChequeService
                                        .getTotalChequeAmountByBatchId(
                                                batchId
                                        );

                        result.batch =
                                batch;

                        result.cheques =
                                cheques;

                        result.totalChequeCount =
                                totalChequeCount;

                        result.totalChequeAmount =
                                totalChequeAmount;

                    } catch (Exception e) {

                        result.error =
                                e.getMessage();

                        if (result.error == null ||
                                result.error.trim().isEmpty()) {

                            result.error =
                                    "Unable to load batch details.";
                        }
                    }

                    if (completed.compareAndSet(
                            false,
                            true)) {

                        if (desktop != null &&
                                desktop.isAlive()) {

                            Executions.schedule(
                                    desktop,
                                    event ->
                                            populateBatchDetails(
                                                    result
                                            ),
                                    new Event(
                                            "onBatchDetailsLoaded"
                                    )
                            );
                        }
                    }

                });

        batchDetailsThread.setName(
                "OutwardMakerBatchDetails-"
                + batchId
        );

        batchDetailsThread.setDaemon(true);

        batchDetailsThread.start();

        Thread timeoutThread =
                new Thread(() -> {

                    try {

                        Thread.sleep(
                                BATCH_DETAILS_TIMEOUT
                        );

                        if (completed.compareAndSet(
                                false,
                                true)) {

                            if (desktop != null &&
                                    desktop.isAlive()) {

                                Executions.schedule(
                                        desktop,
                                        event ->
                                                showBatchTimeout(),
                                        new Event(
                                                "onBatchDetailsTimeout"
                                        )
                                );
                            }
                        }

                    } catch (InterruptedException e) {

                        Thread.currentThread()
                                .interrupt();
                    }

                });

        timeoutThread.setName(
                "OutwardMakerBatchTimeout-"
                + batchId
        );

        timeoutThread.setDaemon(true);

        timeoutThread.start();
    }

    private void prepareLoadingState(
            String batchId) {

        outwardMakerLblBatchLoading
                .setValue(
                        "Loading batch details..."
                );

        outwardMakerLblBatchLoading
                .setVisible(true);

        outwardMakerLblBatchId
                .setValue(batchId);

        outwardMakerLblBatchReference
                .setValue("Loading...");

        outwardMakerLblBatchStatus
                .setValue("Loading...");

        outwardMakerLblBatchStatus
                .setSclass(
                        "outward-maker-summary-value"
                );

        outwardMakerLblUploadedBy
                .setValue("Loading...");

        outwardMakerLblUploadedAt
                .setValue("Loading...");

        outwardMakerLblModalTotalCheques
                .setValue("0");

        outwardMakerLblModalTotalAmount
                .setValue("0.00");

        Rows rows =
                outwardMakerGridChequeDetails
                        .getRows();

        rows.getChildren()
                .clear();

        Row loadingRow =
                new Row();

        Cell loadingCell =
                new Cell();

        loadingCell.setColspan(7);

        Label loadingLabel =
                new Label(
                        "Loading cheque details..."
                );

        loadingLabel.setSclass(
                "outward-maker-empty-cheque-message"
        );

        loadingCell.appendChild(
                loadingLabel
        );

        loadingRow.appendChild(
                loadingCell
        );

        rows.appendChild(
                loadingRow
        );
    }

    private void populateBatchDetails(
            BatchDetailsResult result) {

        outwardMakerLblBatchLoading
                .setVisible(false);

        if (result == null) {

            showBatchError(
                    "Unable to load batch details."
            );

            return;
        }

        if (result.error != null) {

            showBatchError(
                    result.error
            );

            return;
        }

        if (result.batch == null) {

            showBatchError(
                    "Batch details were not found."
            );

            return;
        }

        OutwardBatch batch =
                result.batch;

        outwardMakerLblBatchId
                .setValue(
                        getValue(
                                batch.getOutwardBatchId()
                        )
                );

        outwardMakerLblBatchReference
                .setValue(
                        getValue(
                                batch.getBatchReferenceId()
                        )
                );

        outwardMakerLblBatchStatus
                .setValue(
                        getValue(
                                batch.getBatchStatus()
                        )
                );

        outwardMakerLblBatchStatus
                .setSclass(
                        "outward-maker-modal-status "
                        + getStatusClass(
                                batch.getBatchStatus()
                        )
                );

        outwardMakerLblUploadedBy
                .setValue(
                        getValue(
                                batch.getUploadedBy()
                        )
                );

        outwardMakerLblUploadedAt
                .setValue(
                        formatDate(
                                batch.getUploadedAt()
                        )
                );

        outwardMakerLblModalTotalCheques
                .setValue(
                        String.valueOf(
                                result.totalChequeCount
                        )
                );

        outwardMakerLblModalTotalAmount
                .setValue(
                        formatAmount(
                                result.totalChequeAmount
                        )
                );

        renderChequeDetails(
                result.cheques
        );
    }

    private void showBatchError(
            String message) {

        outwardMakerLblBatchLoading
                .setValue(message);

        outwardMakerLblBatchLoading
                .setVisible(true);

        Rows rows =
                outwardMakerGridChequeDetails
                        .getRows();

        rows.getChildren()
                .clear();

        Row errorRow =
                new Row();

        Cell errorCell =
                new Cell();

        errorCell.setColspan(7);

        Label errorLabel =
                new Label(message);

        errorLabel.setSclass(
                "outward-maker-empty-cheque-message"
        );

        errorCell.appendChild(
                errorLabel
        );

        errorRow.appendChild(
                errorCell
        );

        rows.appendChild(
                errorRow
        );
    }

    private void showBatchTimeout() {

        showBatchError(
                "Loading timed out. Please close and try again."
        );
    }

    private void renderChequeDetails(
            List<OutwardCheque> cheques) {

        Rows chequeRows =
                outwardMakerGridChequeDetails
                        .getRows();

        chequeRows.getChildren()
                .clear();

        if (cheques == null ||
                cheques.isEmpty()) {

            Row emptyRow =
                    new Row();

            Cell emptyCell =
                    new Cell();

            emptyCell.setColspan(7);

            Label emptyLabel =
                    new Label(
                            "No cheque details available."
                    );

            emptyLabel.setSclass(
                    "outward-maker-empty-cheque-message"
            );

            emptyCell.appendChild(
                    emptyLabel
            );

            emptyRow.appendChild(
                    emptyCell
            );

            chequeRows.appendChild(
                    emptyRow
            );

            return;
        }

        DecimalFormat decimalFormat =
                new DecimalFormat(
                        "#,##0.00"
                );

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "dd-MM-yyyy"
                );

        for (OutwardCheque cheque :
                cheques) {

            Row row =
                    new Row();

            row.appendChild(
                    new Label(
                            getValue(
                                    cheque.getChequeNumber()
                            )
                    )
            );

            row.appendChild(
                    new Label(
                            getValue(
                                    cheque.getMicrCode()
                            )
                    )
            );

            row.appendChild(
                    new Label(
                            getValue(
                                    cheque.getDraweeName()
                            )
                    )
            );

            row.appendChild(
                    new Label(
                            getValue(
                                    cheque.getPayeeName()
                            )
                    )
            );

            BigDecimal amount =
                    cheque.getChequeAmount();

            String amountValue =
                    amount == null
                            ? "0.00"
                            : decimalFormat.format(
                                    amount
                            );

            row.appendChild(
                    new Label(
                            amountValue
                    )
            );

            String chequeDate =
                    "-";

            if (cheque.getChequeDate() != null) {

                chequeDate =
                        dateFormat.format(
                                cheque.getChequeDate()
                        );
            }

            row.appendChild(
                    new Label(
                            chequeDate
                    )
            );

            Label statusLabel =
                    new Label(
                            getValue(
                                    cheque.getChequeStatus()
                            )
                    );

            statusLabel.setSclass(
                    "outward-maker-status "
                    + getStatusClass(
                            cheque.getChequeStatus()
                    )
            );

            row.appendChild(
                    statusLabel
            );

            chequeRows.appendChild(
                    row
            );
        }
    }

    private void closeBatchDetails() {

        if (outwardMakerWinBatchDetails != null) {

            outwardMakerWinBatchDetails
                    .setVisible(false);
        }
    }

    private String formatAmount(
            BigDecimal amount) {

        if (amount == null) {
            return "0.00";
        }

        DecimalFormat decimalFormat =
                new DecimalFormat(
                        "#,##0.00"
                );

        return decimalFormat.format(
                amount
        );
    }

    private String formatDate(
            java.util.Date date) {

        if (date == null) {
            return "-";
        }

        SimpleDateFormat dateFormat =
                new SimpleDateFormat(
                        "dd-MM-yyyy"
                );

        return dateFormat.format(date);
    }

    private String getValue(
            Object value) {

        if (value == null) {
            return "-";
        }

        String text =
                String.valueOf(value);

        if (text.trim().isEmpty()) {
            return "-";
        }

        return text;
    }

    private String getStatusClass(
            String status) {

        if (status == null) {
            return "pending";
        }

        String normalizedStatus =
                status.toLowerCase();

        if (normalizedStatus.contains(
                "completed")
                || normalizedStatus.contains(
                        "success")
                || normalizedStatus.contains(
                        "validated")) {

            return "completed";
        }

        if (normalizedStatus.contains(
                "rejected")
                || normalizedStatus.contains(
                        "failed")) {

            return "rejected";
        }

        if (normalizedStatus.contains(
                "processing")
                || normalizedStatus.contains(
                        "validating")) {

            return "processing";
        }

        return "pending";
    }

    private static class BatchDetailsResult {

        private OutwardBatch batch;
        private List<OutwardCheque> cheques;
        private int totalChequeCount;
        private BigDecimal totalChequeAmount;
        private String error;
    }
}