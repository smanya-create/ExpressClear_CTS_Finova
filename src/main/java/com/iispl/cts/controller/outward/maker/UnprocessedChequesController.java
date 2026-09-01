package com.iispl.cts.controller.outward.maker;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Label;
import org.zkoss.zul.ListModelList;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.ListitemRenderer;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;

import com.iispl.cts.dao.outward.UnprocessedChequeDAO;
import com.iispl.cts.daoimpl.outward.UnprocessedChequeDAOImpl;
import com.iispl.cts.dto.UnprocessedChequeDTO;

public class UnprocessedChequesController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    private Label lblTotalCount;
    private Label lblRepairCount;
    private Label lblDataEntryCount;
    private Combobox cmbStatusFilter;
    private Textbox txtSearchBatch;
    private Listbox lstUnprocessed;

    private final UnprocessedChequeDAO unprocessedDAO = new UnprocessedChequeDAOImpl();
    private List<UnprocessedChequeDTO> masterList = new ArrayList<>();
    private final DecimalFormat df = new DecimalFormat("##,##,##0.00");

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        if (cmbStatusFilter != null) {
            cmbStatusFilter.setSelectedIndex(0);
        }

        initListboxRenderer();
        loadUnprocessedCheques();
    }

    private void initListboxRenderer() {
        lstUnprocessed.setItemRenderer(new ListitemRenderer<UnprocessedChequeDTO>() {
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

                // 5. Maker Task Stage (MICR Repair vs Data Entry)
                Listcell cellStage = new Listcell();
                Label lblStage = new Label();
                if ("PENDING_REPAIR".equals(dto.getStatus()) || "RAW".equals(dto.getStatus())) {
                    lblStage.setValue("MICR Repair Pending");
                    lblStage.setSclass("badge-repair");
                } else if ("PENDING_DATA_ENTRY".equals(dto.getStatus())) {
                    lblStage.setValue("Data Entry Pending");
                    lblStage.setSclass("badge-entry");
                }
                cellStage.appendChild(lblStage);

                // 6. Reason / Remarks
                Listcell cellRemarks = new Listcell();
                if (dto.getSendBackReason() != null) {
                    Label lblReason = new Label(dto.getSendBackReason() + (dto.getRemarks() != null ? " (" + dto.getRemarks() + ")" : ""));
                    lblReason.setSclass("list-remarks");
                    cellRemarks.appendChild(lblReason);
                } else {
                    Label lblAuto = new Label("EOD Cutoff Rollover");
                    lblAuto.setStyle("font-size: 11px; color: #718096; font-style: italic;");
                    cellRemarks.appendChild(lblAuto);
                }

                // 7. Action Button
                Listcell cellAction = new Listcell();
                Button btnAction = new Button("Process");
                btnAction.setIconSclass("z-icon-pencil");
                btnAction.setSclass("btn-process");
                btnAction.addEventListener("onClick", event -> routeToMakerModule(dto));
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

    public void loadUnprocessedCheques() {
        this.masterList = unprocessedDAO.getUnprocessedCheques("MAKER");
        updateCounters();
        applyFilters();
    }

    private void updateCounters() {
        long total = masterList.size();
        long repair = masterList.stream().filter(c -> "PENDING_REPAIR".equals(c.getStatus()) || "RAW".equals(c.getStatus())).count();
        long dataEntry = masterList.stream().filter(c -> "PENDING_DATA_ENTRY".equals(c.getStatus())).count();

        if (lblTotalCount != null) lblTotalCount.setValue(String.valueOf(total));
        if (lblRepairCount != null) lblRepairCount.setValue(String.valueOf(repair));
        if (lblDataEntryCount != null) lblDataEntryCount.setValue(String.valueOf(dataEntry));
    }

    public void onFilterChanged() {
        applyFilters();
    }

    private void applyFilters() {
        String stageFilter = (cmbStatusFilter != null && cmbStatusFilter.getSelectedItem() != null)
                ? cmbStatusFilter.getSelectedItem().getValue().toString()
                : "ALL";
        String searchKeyword = (txtSearchBatch != null && txtSearchBatch.getValue() != null)
                ? txtSearchBatch.getValue().trim().toLowerCase()
                : "";

        List<UnprocessedChequeDTO> filtered = masterList.stream().filter(item -> {
            boolean matchesStage = true;
            if (!"ALL".equalsIgnoreCase(stageFilter)) {
                if ("PENDING_REPAIR".equalsIgnoreCase(stageFilter)) {
                    matchesStage = "PENDING_REPAIR".equals(item.getStatus()) || "RAW".equals(item.getStatus());
                } else if ("PENDING_DATA_ENTRY".equalsIgnoreCase(stageFilter)) {
                    matchesStage = "PENDING_DATA_ENTRY".equals(item.getStatus());
                }
            }

            boolean matchesSearch = true;
            if (!searchKeyword.isEmpty()) {
                boolean bMatch = item.getBatchNo() != null && item.getBatchNo().toLowerCase().contains(searchKeyword);
                boolean cMatch = item.getChequeNo() != null && item.getChequeNo().toLowerCase().contains(searchKeyword);
                matchesSearch = bMatch || cMatch;
            }

            return matchesStage && matchesSearch;
        }).collect(Collectors.toList());

        lstUnprocessed.setModel(new ListModelList<>(filtered));
    }

    private void routeToMakerModule(UnprocessedChequeDTO dto) {
        String targetUrl;
        if ("PENDING_DATA_ENTRY".equals(dto.getStatus())) {
            targetUrl = "/WEB-INF/views/maker/data-entry.zul?chequeId=" + dto.getChequeId();
        } else {
            targetUrl = "/WEB-INF/views/maker/micr-repair.zul?chequeId=" + dto.getChequeId();
        }
        Executions.sendRedirect(targetUrl);
    }
}