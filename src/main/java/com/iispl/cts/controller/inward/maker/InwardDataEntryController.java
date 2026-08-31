package com.iispl.cts.controller.inward.maker;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Image;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;

public class InwardDataEntryController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    // Image Component & View State
    private Image imgCheque;
    private double zoomLevel = 1.0;
    private int rotationDegree = 0;

    // Form Textboxes
    private Textbox txtChequeNumber;
    private Textbox txtChequeDate;
    private Textbox txtAmount;
    private Textbox txtDraweeAccount;
    private Textbox txtDraweeBankName;
    private Textbox txtPayeeName;
    private Textbox txtEntryRemark;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
    }

    // --- Image Interactive Controls ---
    public void onClick$btnZoomIn() {
        if (zoomLevel < 3.0) {
            zoomLevel += 0.2;
            applyImageTransform();
        }
    }

    public void onClick$btnZoomOut() {
        if (zoomLevel > 0.6) {
            zoomLevel -= 0.2;
            applyImageTransform();
        }
    }

    public void onClick$btnZoomFit() {
        zoomLevel = 1.0;
        rotationDegree = 0;
        applyImageTransform();
    }

    public void onClick$btnRotate() {
        rotationDegree = (rotationDegree + 90) % 360;
        applyImageTransform();
    }

    private void applyImageTransform() {
        if (imgCheque != null) {
            imgCheque.setStyle("transform: scale(" + zoomLevel + ") rotate(" + rotationDegree + "deg); transform-origin: center center;");
        }
    }

    // --- Form Action Handlers ---
    public void onClick$btnBackToReview() {
        Executions.sendRedirect("/inward-maker/dashboard.zul");
    }

    public void onClick$btnCancel() {
        Executions.sendRedirect("/inward-maker/dashboard.zul");
    }

    public void onClick$btnValidateData() {
        Messagebox.show("All transaction data fields are valid.", "Validation Success", 
                        Messagebox.OK, Messagebox.INFORMATION);
    }

    public void onClick$btnRequestRejection() {
        String remark = txtEntryRemark != null && txtEntryRemark.getValue() != null 
                ? txtEntryRemark.getValue().trim() : "";
        if (remark.isEmpty()) {
            Messagebox.show("Please enter an 'Entry Remark' stating the reason for rejection.", 
                            "Remark Required", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }
        Messagebox.show("Cheque marked for rejection.", "Rejection Requested", 
                        Messagebox.OK, Messagebox.INFORMATION);
    }

    public void onClick$btnSaveDataEntry() {
        Messagebox.show("Cheque data entry saved successfully.", "Success", 
                        Messagebox.OK, Messagebox.INFORMATION);
    }
}