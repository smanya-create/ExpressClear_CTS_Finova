package com.iispl.cts.controller.outward.checker;

import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.iispl.cts.dao.outward.OutwardBatchDashboardDAO;
import com.iispl.cts.daoimpl.outward.OutwardBatchDashboardDAOImpl;
import com.iispl.cts.entity.outward.OutwardBatch;
import com.iispl.cts.service.outward.OutwardBatchService;
import com.iispl.cts.serviceimpl.outward.OutwardBatchServiceImpl;

public class OutwardCheckerDashboardController
        extends GenericForwardComposer<Component> {

    private Textbox txtBatchId;
    private Datebox fromDate;
    private Datebox toDate;

    private Button btnSearch;
    private Button btnClear;

    private Listbox lstBatches;

    private Button btnPrevious;
    private Button btnNext;
    private Button btnFirst;
    private Button btnLast;

    private Label lblPage;

    private OutwardBatchService outwardBatchService =
            new OutwardBatchServiceImpl();

    private OutwardBatchDashboardDAO outwardBatchDashboardDAO =
            new OutwardBatchDashboardDAOImpl();

    int pageNumber = 1;
    int pageSize = 5;

    @Override
    public void doAfterCompose(Component comp) throws Exception {

        super.doAfterCompose(comp);

        String role =
                (String) Sessions.getCurrent()
                        .getAttribute("CTS_USER_ROLE");

        btnPrevious.addEventListener(
                Events.ON_CLICK,
                event -> {

                    if (pageNumber > 1) {
                        pageNumber--;
                        loadDashboard();
                    }
                });

        btnNext.addEventListener(
                Events.ON_CLICK,
                event -> {

                    int totalBatches =
                            getTotalBatchCount();

                    int totalPages =
                            (int) Math.ceil(
                                    (double) totalBatches / pageSize);

                    if (pageNumber < totalPages) {
                        pageNumber++;
                        loadDashboard();
                    }
                });

        btnFirst.addEventListener(
                Events.ON_CLICK,
                event -> {

                    if (pageNumber > 1) {
                        pageNumber = 1;
                        loadDashboard();
                    }
                });

        btnLast.addEventListener(
                Events.ON_CLICK,
                event -> {

                    int totalBatches =
                            getTotalBatchCount();

                    int totalPages =
                            (int) Math.ceil(
                                    (double) totalBatches / pageSize);

                    if (totalPages > 0
                            && pageNumber < totalPages) {

                        pageNumber = totalPages;
                        loadDashboard();
                    }
                });

        btnSearch.addEventListener(
                Events.ON_CLICK,
                event -> {

                    pageNumber = 1;
                    loadDashboard();
                });

        btnClear.addEventListener(
                Events.ON_CLICK,
                event -> {

                    txtBatchId.setValue("");
                    fromDate.setValue(null);
                    toDate.setValue(null);

                    pageNumber = 1;

                    loadDashboard();
                });

        loadDashboard();
    }

    private boolean isSearchActive() {

        String batchId = txtBatchId.getValue();

        return (batchId != null
                && !batchId.trim().isEmpty())
                || fromDate.getValue() != null
                || toDate.getValue() != null;
    }

    private int getTotalBatchCount() {

        if (isSearchActive()) {

            return outwardBatchDashboardDAO
                    .getSearchPendingBatchCount(
                            txtBatchId.getValue(),
                            fromDate.getValue(),
                            toDate.getValue());
        }

        return outwardBatchService
                .getPendingBatchCount();
    }

    private void loadDashboard() {

        try {

            List<OutwardBatch> pendingBatches;
            int totalBatches;

            if (isSearchActive()) {

                String batchId =
                        txtBatchId.getValue();

                Date searchFromDate =
                        fromDate.getValue();

                Date searchToDate =
                        toDate.getValue();

                pendingBatches =
                        outwardBatchDashboardDAO
                                .searchPendingBatches(
                                        pageNumber,
                                        pageSize,
                                        batchId,
                                        searchFromDate,
                                        searchToDate);

                totalBatches =
                        outwardBatchDashboardDAO
                                .getSearchPendingBatchCount(
                                        batchId,
                                        searchFromDate,
                                        searchToDate);

            } else {

                pendingBatches =
                        outwardBatchService
                                .getPendingBatches(
                                        pageNumber,
                                        pageSize);

                totalBatches =
                        outwardBatchService
                                .getPendingBatchCount();
            }

            int totalPages =
                    (int) Math.ceil(
                            (double) totalBatches / pageSize);

            if (totalPages == 0) {

                pageNumber = 0;

                lblPage.setValue("0/0");

                btnFirst.setDisabled(true);
                btnPrevious.setDisabled(true);
                btnNext.setDisabled(true);
                btnLast.setDisabled(true);

            } else {

                if (pageNumber < 1) {
                    pageNumber = 1;
                }

                if (pageNumber > totalPages) {
                    pageNumber = totalPages;
                }

                lblPage.setValue(
                        pageNumber + "/" + totalPages);

                btnFirst.setDisabled(
                        pageNumber == 1);

                btnPrevious.setDisabled(
                        pageNumber == 1);

                btnNext.setDisabled(
                        pageNumber == totalPages);

                btnLast.setDisabled(
                        pageNumber == totalPages);
            }

            ListModelList<OutwardBatch> model =
                    new ListModelList<>(pendingBatches);

            lstBatches.setModel(model);

            lstBatches.setItemRenderer(
                    new ListitemRenderer<OutwardBatch>() {

                @Override
                public void render(
                        Listitem item,
                        OutwardBatch batch,
                        int index)
                        throws Exception {

                    item.setValue(batch);

                    item.appendChild(
                            new Listcell(
                                    batch.getOutwardBatchId()));

                    item.appendChild(
                            new Listcell(
                                    String.valueOf(
                                            batch.getActualChequeCount())));

                    BigDecimal amount =
                            batch.getActualTotalAmount();

                    String amountText =
                            amount == null
                                    ? "₹0.00"
                                    : "₹"
                                      + amount.toPlainString();

                    item.appendChild(
                            new Listcell(amountText));

                    String submittedBy =
                            batch.getUploadedBy();

                    item.appendChild(
                            new Listcell(
                                    submittedBy == null
                                            ? "-"
                                            : submittedBy));

                    String submittedAt = "-";

                    if (batch.getUploadedAt() != null) {

                        SimpleDateFormat formatter =
                                new SimpleDateFormat(
                                        "dd-MMM-yyyy HH:mm a");

                        submittedAt =
                                formatter.format(
                                        batch.getUploadedAt());
                    }

                    item.appendChild(
                            new Listcell(submittedAt));

                    Listcell statusCell =
                            new Listcell();

                    statusCell.setStyle(
                            "text-align:center;"
                            + "vertical-align:middle;");

                    Label statusLabel =
                            new Label(
                                    batch.getBatchStatus());

                    statusLabel.setSclass(
                            "status-pending");

                    statusLabel.setStyle(
                            "display:inline-block;"
                            + "background:#fff7ed;"
                            + "color:#b45309;"
                            + "border:1px solid #f59e0b;"
                            + "border-radius:16px;"
                            + "font-size:8px;"
                            + "font-weight:800;"
                            + "text-align:center;"
                            + "white-space:nowrap;");

                    statusCell.appendChild(
                            statusLabel);

                    item.appendChild(
                            statusCell);

                    Listcell actionCell =
                            new Listcell();

                    Button queueButton =
                            new Button("QUEUE");

                    queueButton.setSclass(
                            "queue-button");

                    queueButton.addEventListener(
                            Events.ON_CLICK,
                            event -> {

                        OutwardBatch selectedBatch =
                                item.getValue();

                        Map<String, Object> args =
                                new HashMap<>();

                        args.put(
                                "batchId",
                                selectedBatch
                                        .getOutwardBatchId());

                        Window popup =
                                (Window) Executions
                                        .createComponents(
                                                "/outward/checker/batch-proceed-popup.zul",
                                                null,
                                                args);

                        popup.doModal();
                    });

                    actionCell.appendChild(
                            queueButton);

                    item.appendChild(
                            actionCell);
                }
            });

        } catch (Exception e) {

            e.printStackTrace();

            Messagebox.show(
                    "Unable to load Checker Dashboard.\n\n"
                            + e.getMessage(),
                    "Dashboard Error",
                    Messagebox.OK,
                    Messagebox.ERROR);
        }
    }
}