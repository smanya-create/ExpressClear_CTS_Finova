package com.iispl.cts.controller.inward.maker;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.select.SelectorComposer;
import org.zkoss.zk.ui.select.annotation.Listen;
import org.zkoss.zk.ui.select.annotation.Wire;
import org.zkoss.zul.Button;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Textbox;

public class InwardMakerDashboardController extends SelectorComposer<Component> {

    private static final long serialVersionUID = 1L;

    @Wire("#lblPartiallyProcessed")
    private Label lblPartiallyProcessed;

    @Wire("#lblSentBackToMaker")
    private Label lblSentBackToMaker;

    @Wire("#lblSentToRrf")
    private Label lblSentToRrf;

    @Wire("#txtSearchBatchId")
    private Textbox txtSearchBatchId;

    @Wire("#batchStatusListbox")
    private Listbox batchStatusListbox;

    @Wire("#btnRefresh")
    private Button btnRefresh;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);

        Sessions.getCurrent().setAttribute("USER_ROLE", "INWARD_MAKER");

        loadDashboardMetrics();
        populateBatchTable();
    }

    private void loadDashboardMetrics() {
        if (lblPartiallyProcessed != null) lblPartiallyProcessed.setValue("2");
        if (lblSentBackToMaker != null) lblSentBackToMaker.setValue("1");
        if (lblSentToRrf != null) lblSentToRrf.setValue("3");
    }

    private void populateBatchTable() {
        if (batchStatusListbox == null) return;

        batchStatusListbox.getItems().clear();

        Listitem item = new Listitem();
        item.setSelected(false);

        // 1. Batch ID (Left aligned)
        Listcell c1 = new Listcell("IN-B004");
        c1.setStyle("color: #3182ce; font-weight: 700; font-size: 13px; cursor: pointer; text-align: left; padding: 12px 14px;");
        item.appendChild(c1);

        // 2. Date (Left aligned)
        Listcell c2 = new Listcell("21-08-2026");
        c2.setStyle("color: #2d3748; font-size: 13px; font-weight: 500; text-align: left; padding: 12px 14px;");
        item.appendChild(c2);

        // 3. Source (Center aligned)
        Listcell c3 = new Listcell("CHI");
        c3.setStyle("color: #2d3748; font-size: 13px; font-weight: 500; text-align: center; padding: 12px;");
        item.appendChild(c3);

        // 4. Cheques (Center aligned)
        Listcell c4 = new Listcell("99");
        c4.setStyle("color: #2d3748; font-size: 13px; font-weight: 500; text-align: center; padding: 12px;");
        item.appendChild(c4);

        // 5. Accepted (Center aligned)
        Listcell c5 = new Listcell("84");
        c5.setStyle("color: #38a169; font-weight: 700; font-size: 13px; text-align: center; padding: 12px;");
        item.appendChild(c5);

        // 6. Back to Maker (Center aligned)
        Listcell c6 = new Listcell("5");
        c6.setStyle("color: #dd6b20; font-weight: 700; font-size: 13px; text-align: center; padding: 12px;");
        item.appendChild(c6);

        // 7. RRF (Center aligned)
        Listcell c7 = new Listcell("10");
        c7.setStyle("color: #e53e3e; font-weight: 700; font-size: 13px; text-align: center; padding: 12px;");
        item.appendChild(c7);

        // 8. Status Pill (Center aligned)
        Listcell c8 = new Listcell();
        c8.setStyle("text-align: center; padding: 12px;");
        Label pill = new Label("Partially Processed");
        pill.setStyle("background-color: #feebc8; color: #c05621; border: 1px solid #fbd38d; border-radius: 12px; padding: 3px 12px; font-size: 11px; font-weight: 700; display: inline-block;");
        c8.appendChild(pill);
        item.appendChild(c8);

        batchStatusListbox.appendChild(item);
    }

    @Listen("onClick = #btnRefresh")
    public void onRefresh() {
        loadDashboardMetrics();
        populateBatchTable();
    }

    @Listen("onClick = #btnSearch")
    public void onSearch() {
        if (txtSearchBatchId == null || batchStatusListbox == null) return;
        String val = txtSearchBatchId.getValue() != null ? txtSearchBatchId.getValue().trim() : "";
        if (!val.isEmpty() && !"IN-B004".equalsIgnoreCase(val)) {
            batchStatusListbox.getItems().clear();
        } else {
            populateBatchTable();
        }
    }

    @Listen("onClick = #btnClearSearch")
    public void onClearSearch() {
        if (txtSearchBatchId != null) txtSearchBatchId.setValue("");
        populateBatchTable();
    }

    @Listen("onClick = #btnViewBatch")
    public void onViewBatch() {
        Executions.getCurrent().getSession().setAttribute("ACTIVE_BATCH_ID", "IN-B004");
    }

    @Listen("onClick = #btnViewHistory")
    public void onViewHistory() {
        // history navigation
    }
}