package com.iispl.cts.controller.outward.checker;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

import com.iispl.cts.dao.outward.CheckerUnprocessedChequeDAO;
import com.iispl.cts.daoimpl.outward.CheckerUnprocessedChequeDAOImpl;
import com.iispl.cts.dto.UnprocessedChequeDTO;

public class CheckerUnprocessedChequesController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    private Label lblTotalVerifyCount;
    private Label lblTotalVerifyAmount;
    private Textbox txtSearchBatch;
    private Listbox lstCheckerUnprocessed;

    private final CheckerUnprocessedChequeDAO checkerDAO = new CheckerUnprocessedChequeDAOImpl();
    private List<UnprocessedChequeDTO> masterList = new ArrayList<>();
    private final DecimalFormat df = new DecimalFormat("##,##,##0.00");

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        initListboxRenderer();
        loadCheckerUnprocessedCheques();
    }

    private void initListboxRenderer() {
        lstCheckerUnprocessed.setItemRenderer(new ListitemRenderer<UnprocessedChequeDTO>() {
            @Override
            public void render(Listitem item, UnprocessedChequeDTO dto, int index) {
                item.setValue(dto);

                // 1. Batch & Session Info
                Listcell cellBatch = new Listcell();
                Vlayout vBatch = new Vlayout();
                vBatch.setSpacing("2px");
                Label lblBNo = new Label(dto.getBatchNo());
                lblBNo.setSclass("list-batch-title");
                Label lblSName = new Label("Rollover: " + (dto.getOriginalSessionName() != null ? dto.getOriginalSessionName() : "Prior EOD"));
                lblSName.setSclass("list-batch-sub");
                vBatch.appendChild(lblBNo);
                vBatch.appendChild(lblSName);
                cellBatch.appendChild(vBatch);

                // 2. Cheque No
                Listcell cellChq = new Listcell(dto.getChequeNo() != null ? dto.getChequeNo() : "------");
                cellChq.setSclass("list-monospace");

                // 3. Sort Code
                Listcell cellSort = new Listcell(dto.getSortCode() != null ? dto.getSortCode() : "------");
                cellSort.setSclass("list-monospace");

                // 4. Amount
                Listcell cellAmt = new Listcell(dto.getAmount() != null ? "₹ " + df.format(dto.getAmount()) : "₹ 0.00");
                cellAmt.setSclass("list-amount");

                // 5. Status Badge
                Listcell cellStage = new Listcell();
                Label lblStage = new Label("Pending Verification");
                lblStage.setSclass("badge-verify");
                cellStage.appendChild(lblStage);

                // 6. Reason / Remarks
                Listcell cellRemarks = new Listcell();
                if (dto.getSendBackReason() != null) {
                    Label lblReason = new Label(dto.getSendBackReason() + (dto.getRemarks() != null ? " (" + dto.getRemarks() + ")" : ""));
                    lblReason.setSclass("list-remarks");
                    cellRemarks.appendChild(lblReason);
                } else {
                    Label lblAuto = new Label("Rolled over awaiting Checker verification");
                    lblAuto.setStyle("font-size: 11px; color: #718096; font-style: italic;");
                    cellRemarks.appendChild(lblAuto);
                }

                // 7. Action Button
                Listcell cellAction = new Listcell();
                Button btnAction = new Button("Verify");
                btnAction.setIconSclass("z-icon-check-square-o");
                btnAction.setSclass("btn-process");
                btnAction.addEventListener("onClick", event -> routeToCheckerVerification(dto));
                cellAction.appendChild(btnAction);

                item.appendChild(cellBatch);
                item.appendChild(cellChq);
                item.appendChild(cellSort);
                item.appendChild(cellAmt);
                item.appendChild(cellStage);
                item.appendChild(cellRemarks);
                item.appendChild(cellAction);
            }
        });
    }

    public void loadCheckerUnprocessedCheques() {
        this.masterList = checkerDAO.getCheckerUnprocessedCheques();
        updateCounters();
        applyFilters();
    }

    private void updateCounters() {
        long count = masterList.size();
        double sum = masterList.stream()
                               .filter(c -> c.getAmount() != null)
                               .mapToDouble(c -> c.getAmount().doubleValue())
                               .sum();

        if (lblTotalVerifyCount != null) lblTotalVerifyCount.setValue(String.valueOf(count));
        if (lblTotalVerifyAmount != null) lblTotalVerifyAmount.setValue("₹ " + df.format(sum));
    }

    public void onFilterChanged() {
        applyFilters();
    }

    private void applyFilters() {
        String searchKeyword = (txtSearchBatch != null && txtSearchBatch.getValue() != null)
                ? txtSearchBatch.getValue().trim().toLowerCase()
                : "";

        List<UnprocessedChequeDTO> filtered = masterList.stream().filter(item -> {
            if (searchKeyword.isEmpty()) return true;
            boolean bMatch = item.getBatchNo() != null && item.getBatchNo().toLowerCase().contains(searchKeyword);
            boolean cMatch = item.getChequeNo() != null && item.getChequeNo().toLowerCase().contains(searchKeyword);
            return bMatch || cMatch;
        }).collect(Collectors.toList());

        lstCheckerUnprocessed.setModel(new ListModelList<>(filtered));
    }

    private void routeToCheckerVerification(UnprocessedChequeDTO dto) {
        Executions.sendRedirect("/WEB-INF/views/checker/verification.zul?chequeId=" + dto.getChequeId());
    }
}