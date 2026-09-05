package com.iispl.cts.controller.outward.checker;

import java.text.SimpleDateFormat;

import java.util.List;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Comboitem;
import org.zkoss.zul.Image;
import org.zkoss.zul.Label;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Window;

import com.iispl.cts.entity.outward.OutwardCheque;
import com.iispl.cts.entity.outward.OutwardChequeImage;
import com.iispl.cts.entity.outward.SendBackReason;
import com.iispl.cts.service.outward.OutwardCheckerQueueService;
import com.iispl.cts.serviceimpl.outward.OutwardCheckerQueueServiceImpl;



public class OutwardCheckerQueueController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

   
    private Label lblBatchNo;
    private Label lblChequeNo;
    private Label lblQueueStatus;
    private Label lblCurrentCheque;
    private Label lblRemaining;
    private Label lblZoom;
    private Label lblAccountValidation;

    private Label lblCurrentChequeNavigation;

    private Image imgCheque;
    
    private Button btnImageSide;

    private boolean showingBackImage = false;

    private Textbox txtChequeNo;
    private Textbox txtMicr;
    private Textbox txtAccountNo;
    private Textbox txtAmount;
    private Textbox txtChequeDate;
    private Textbox txtpayeeName;

    private Button btnPrevious;
    private Button btnNext;

    private Button btnVerified;
    private Button btnReturn;
    private Button btnReject;

    private Button btnZoomIn;
    private Button btnZoomOut;
    private Button btnZoomReset;
    
    private Window returnMakerWindow;

    private Label lblReturnBatch;
    private Label lblReturnCheque;

    private Combobox cmbSendBackReason;    
    private Textbox txtReturnRemarks;
    
    private void createReturnMakerWindow() {

        try {

            // Create Return to Maker window and ATTACH it
            // to the current checker queue page
            returnMakerWindow =
                    (Window) Executions.createComponents(
                            "/outward/checker/return-to-maker.zul",
                            self,
                            null
                    );

            // Get popup components
            lblReturnBatch =
                    (Label) returnMakerWindow.getFellow(
                            "lblReturnBatch"
                    );

            lblReturnCheque =
                    (Label) returnMakerWindow.getFellow(
                            "lblReturnCheque"
                    );

            cmbSendBackReason =
                    (Combobox) returnMakerWindow.getFellow(
                            "cmbSendBackReason"
                    );

            txtReturnRemarks =
                    (Textbox) returnMakerWindow.getFellow(
                            "txtReturnRemarks"
                    );

            btnReturnConfirm =
                    (Button) returnMakerWindow.getFellow(
                            "btnReturnConfirm"
                    );

            btnReturnCancel =
                    (Button) returnMakerWindow.getFellow(
                            "btnReturnCancel"
                    );

            // Important:
            // Dynamically created buttons need explicit listeners
            btnReturnConfirm.addEventListener(
                    "onClick",
                    event -> onClick$btnReturnConfirm(event)
            );

            btnReturnCancel.addEventListener(
                    "onClick",
                    event -> onClick$btnReturnCancel(event)
            );

            // Keep popup hidden until Return to Maker is clicked
            returnMakerWindow.setVisible(false);

            System.out.println(
                    "Return Maker window created and attached successfully."
            );

        } catch (Exception e) {

            e.printStackTrace();

            Messagebox.show(
                    "Unable to create Return to Maker window.\n\n"
                            + e.getMessage(),
                    "Error",
                    Messagebox.OK,
                    Messagebox.ERROR
            );
        }
    }

    private Button btnReturnConfirm;
    private Button btnReturnCancel;


    private List<OutwardCheque> cheques;

    private int currentIndex = 0;

    private String batchId;
    private String batchNo;
    
    private String frontImagePath;
    private String backImagePath;

    private OutwardCheckerQueueService outwardCheckerQueueService;
    
    private double zoomLevel = 1.0;


    @Override
    public void doAfterCompose(Component comp) throws Exception {

        super.doAfterCompose(comp);

        outwardCheckerQueueService = new OutwardCheckerQueueServiceImpl();
        
        createReturnMakerWindow();


        batchId = "BAT1003";
        batchNo = "BAT1003";

        String batchIdParam =
                Executions.getCurrent().getParameter("batchId");

        String batchNoParam =
                Executions.getCurrent().getParameter("batchNo");

        if (batchIdParam != null
                && !batchIdParam.trim().isEmpty()) {

            try {

            	 batchId = batchIdParam.trim();
            	 
            } catch (NumberFormatException e) {

                System.out.println(
                        "Invalid batchId: " + batchIdParam
                );
            }
        }

        if (batchNoParam != null
                && !batchNoParam.trim().isEmpty()) {

            batchNo = batchNoParam.trim();
        }

        System.out.println("=================================");
        System.out.println("Batch ID = " + batchId);
        System.out.println("Batch No = " + batchNo);
        System.out.println("=================================");

        // DISPLAY BATCH

        if (lblBatchNo != null) {

            lblBatchNo.setValue(
                    batchNo != null ? batchNo : "-"
            );
        }

        loadCheques();
    }

    private void loadCheques() throws Exception {

        cheques =
        		outwardCheckerQueueService.getChequesByBatchId(batchId);

        if (cheques == null || cheques.isEmpty()) {

            Messagebox.show(
                    "No cheques found for batch " + batchNo,
                    "Checker Queue",
                    Messagebox.OK,
                    Messagebox.INFORMATION
            );

            return;
        }

        currentIndex = 0;

        displayCheque();
    }

        // DISPLAY CHEQUE

    private void displayCheque() {

        if (cheques == null || cheques.isEmpty()) {
            return;
        }

        if (currentIndex < 0
                || currentIndex >= cheques.size()) {
            return;
        }

        OutwardCheque cheque =
                cheques.get(currentIndex);

        showingBackImage = false;
          // LOAD FRONT / BACK IMAGE PATHS
     
     frontImagePath = null;
     backImagePath = null;

     try {

         List<OutwardChequeImage> images =
                 outwardCheckerQueueService.getImagesByChequeId(
                         cheque.getOutwardChequeId()
                 );

         if (images != null) {

             for (OutwardChequeImage image : images) {

                 if ("FRONT".equalsIgnoreCase(
                         image.getImageType())) {

                     frontImagePath = image.getImagePath();

                 } else if ("BACK".equalsIgnoreCase(
                         image.getImageType())) {

                     backImagePath = image.getImagePath();
                 }
             }
         }

     } catch (Exception e) {

         e.printStackTrace();

         Messagebox.show(
                 "Unable to load cheque images.",
                 "Image Error",
                 Messagebox.OK,
                 Messagebox.ERROR
         );
     }

     System.out.println("=================================");
     System.out.println(
             "Cheque ID = " + cheque.getOutwardChequeId()
     );
     System.out.println(
             "Cheque No = " + cheque.getChequeNumber()
     );
     System.out.println(
             "Front Image Path = " + frontImagePath
     );
     System.out.println(
             "Back Image Path = " + backImagePath
     );
     System.out.println("=================================");
        // CHEQUE NUMBER



  String chequeNumber = nullSafe(cheque.getChequeNumber());

  // Cheque number in details section
  if (txtChequeNo != null) {
      txtChequeNo.setValue(chequeNumber);
  }

  // Cheque number in header / summary
  if (lblChequeNo != null) {
      lblChequeNo.setValue(
              chequeNumber.isEmpty() ? "-" : chequeNumber
      );
  }
  
//CHEQUE / QUEUE STATUS

String chequeStatus = nullSafe(cheque.getChequeStatus());

if (lblQueueStatus != null) {

   if (chequeStatus.isEmpty()) {
       lblQueueStatus.setValue("-");
   } else {
       lblQueueStatus.setValue(chequeStatus);
   }
}
        // MICR
        
        if (txtMicr != null) {

            txtMicr.setValue(
                    nullSafe(cheque.getMicrCode())
            );
        }
        
        if(txtpayeeName != null) {
        	txtpayeeName.setValue(nullSafe(cheque.getPayeeName()));
        }

        // ACCOUNT NUMBER

        if (txtAccountNo != null) {

            txtAccountNo.setValue(
                    nullSafe(cheque.getDraweeAccountNumber())
            );
        }
        
     // ACCOUNT VALIDATION
     if (lblAccountValidation != null) {

         if ("VALID".equalsIgnoreCase(cheque.getChequeStatus())) {
             lblAccountValidation.setValue("VALID");
             lblAccountValidation.setSclass("valid-badge");
         } else {
             lblAccountValidation.setValue("INVALID");
             lblAccountValidation.setSclass("invalid-badge");
         }
     }

        // AMOUNT

        if (txtAmount != null) {

            if (cheque.getChequeAmount() != null) {

                txtAmount.setValue(
                        "₹ "
                        + cheque.getChequeAmount().toPlainString()
                );

            } else {

                txtAmount.setValue("");
            }
        }

                // DATE
        
        if (txtChequeDate != null) {

            if (cheque.getChequeDate() != null) {

                SimpleDateFormat sdf = new SimpleDateFormat("dd-MMM-yyyy");

                txtChequeDate.setValue(
                        sdf.format(cheque.getChequeDate()));

            } else {

                txtChequeDate.setValue("");
            }
        }
        
   

     // SHOW FRONT IMAGE

     if (imgCheque != null) {

         if (frontImagePath != null
                 && !frontImagePath.trim().isEmpty()) {

             String imageUrl =
                     convertImagePath(frontImagePath);

             System.out.println(
                     "Final Front Image URL = " + imageUrl
             );

             imgCheque.setSrc(imageUrl);
             imgCheque.setVisible(true);

         } else {

             imgCheque.setSrc(null);
             imgCheque.setVisible(false);

             System.out.println(
                     "Front image path is EMPTY"
             );
         }
     }

        // =================================================
        // BUTTON
        // =================================================

        if (btnImageSide != null) {

            btnImageSide.setLabel(
                    "BACK SIDE"
            );
        }

        // =================================================
        // RESET ZOOM
        // =================================================

        zoomLevel = 1.0;

        applyZoom();

        // =================================================
        // UPDATE NAVIGATION
        // =================================================

        updateNavigation();

        System.out.println("=================================");
    }

    // =====================================================
    // FRONT / BACK IMAGE
    // =====================================================

    public void onClick$btnImageSide(Event event) {

        if (cheques == null || cheques.isEmpty()) {
            return;
        }

        if (currentIndex < 0 || currentIndex >= cheques.size()) {
            return;
        }

        // =================================================
        // SHOW BACK IMAGE
        // =================================================

        if (!showingBackImage) {

            if (backImagePath == null
                    || backImagePath.trim().isEmpty()) {

                Messagebox.show(
                        "Back side image is not available.",
                        "Cheque Image",
                        Messagebox.OK,
                        Messagebox.INFORMATION
                );

                return;
            }

            String imageUrl =
                    convertImagePath(backImagePath);

            System.out.println(
                    "Showing BACK image = " + imageUrl
            );

            imgCheque.setSrc(imageUrl);
            imgCheque.setVisible(true);

            showingBackImage = true;

            btnImageSide.setLabel("FRONT SIDE");

        }

        // =================================================
        // SHOW FRONT IMAGE
        // =================================================

        else {

            if (frontImagePath == null
                    || frontImagePath.trim().isEmpty()) {

                Messagebox.show(
                        "Front side image is not available.",
                        "Cheque Image",
                        Messagebox.OK,
                        Messagebox.INFORMATION
                );

                return;
            }

            String imageUrl =
                    convertImagePath(frontImagePath);

            System.out.println(
                    "Showing FRONT image = " + imageUrl
            );

            imgCheque.setSrc(imageUrl);
            imgCheque.setVisible(true);

            showingBackImage = false;

            btnImageSide.setLabel("BACK SIDE");
        }

        applyZoom();
    }

    private String convertImagePath(String path) {

        if (path == null || path.trim().isEmpty()) {
            return null;
        }

        path = path.trim();

        if (path.startsWith("http://")
                || path.startsWith("https://")
                || path.startsWith("/")) {
            return path;
        }

        return "/" + path;
    }


    private void updateNavigation() {

        if (cheques == null || cheques.isEmpty()) {
        	return;
        }

        int total = cheques.size();

        int current = currentIndex + 1;

        int remaining = total - current;
        
        if (lblCurrentCheque != null) {

            lblCurrentCheque.setValue(current + " of " + total);
        }

        if (lblRemaining != null) {

            lblRemaining.setValue(
                    remaining + " Cheques"
            );
        }

        if (lblCurrentChequeNavigation != null) {

            lblCurrentChequeNavigation.setValue(
                    "Cheque "
                    + current
                    + " of "
                    + total
                    + " · "
                    + remaining
                    + " remaining"
            );
        }

     
        if (btnPrevious != null) {

            btnPrevious.setDisabled(currentIndex == 0);
        }

        
        if (btnNext != null) {

            btnNext.setDisabled(currentIndex >= total - 1);
        }
    }


    public void onClick$btnPrevious(Event event) {

        if (cheques == null
                || cheques.isEmpty()) {

            return;
        }

        if (currentIndex > 0) {

            currentIndex--;

            displayCheque();
        }
    }


    public void onClick$btnNext(Event event) {

        if (cheques == null
                || cheques.isEmpty()) {

            return;
        }

        if (currentIndex
                < cheques.size() - 1) {

            currentIndex++;

            displayCheque();
        }
    }

    public void onClick$btnVerified(Event event) {

        int result =
                Messagebox.show(
                        "Are you sure you want to verify this cheque?",
                        "Verify Cheque",
                        Messagebox.YES | Messagebox.NO,
                        Messagebox.QUESTION
                );

        if (result != Messagebox.YES) {
            return;
        }

        try {

        	OutwardCheque cheque =
                    cheques.get(currentIndex);

            outwardCheckerQueueService.verifyCheque(
                    cheque.getChequeNumber()
            );

            cheque.setChequeStatus("VERIFIED");

            moveToNextCheque();

        } catch (Exception e) {

            showError(
                    "Unable to verify cheque.",
                    e
            );
        }
    }

 // =====================================================
 // RETURN TO MAKER POPUP
 // =====================================================

    public void onClick$btnReturn(Event event) {

        if (cheques == null || cheques.isEmpty()) {
            return;
        }

        if (currentIndex < 0 || currentIndex >= cheques.size()) {
            return;
        }

        OutwardCheque cheque = cheques.get(currentIndex);

        // Set batch number
        if (lblReturnBatch != null) {

            lblReturnBatch.setValue(
                    batchNo != null ? batchNo : "-"
            );
        }

        // Set cheque number
        if (lblReturnCheque != null) {

            lblReturnCheque.setValue(
                    cheque.getChequeNumber() != null
                            ? cheque.getChequeNumber()
                            : "-"
            );
        }

        // Clear previous selected reason
        if (cmbSendBackReason != null) {

            cmbSendBackReason.getItems().clear();
            cmbSendBackReason.setSelectedItem(null);
            cmbSendBackReason.setValue("");
        }

        // Clear old remarks
        if (txtReturnRemarks != null) {

            txtReturnRemarks.setValue("");
        }

        // Load reasons from database
        loadSendBackReasons();

        // Open popup
        if (returnMakerWindow != null) {

            returnMakerWindow.setVisible(true);

            returnMakerWindow.doModal();
        }
    }
    private void loadSendBackReasons() {

        try {

            // Safety check
            if (cmbSendBackReason == null) {

                System.out.println(
                        "ERROR: cmbSendBackReason is NULL"
                );

                return;
            }


            // Clear old items

            cmbSendBackReason.getItems().clear();


            // Get reasons from database

            List<SendBackReason> reasons = outwardCheckerQueueService.getSendBackReasons();


            System.out.println("Reasons loaded = "+ (reasons == null ? 0 : reasons.size()));


            if (reasons == null || reasons.isEmpty()) {

                System.out.println("No send back reasons found.");

                return;
            }


            // Add reasons to combobox

            for (SendBackReason reason : reasons) {

                Comboitem item = new Comboitem();

                item.setLabel(
                        reason.getReasonName()
                );

                // Store complete object

                item.setValue(reason);

                cmbSendBackReason
                        .appendChild(item);
            }


            System.out.println(
                    "Send back reasons added to combobox."
            );

        } catch (Exception e) {

            e.printStackTrace();

            Messagebox.show(

                    "Unable to load send back reasons.\n\n"
                    + e.getClass().getName()
                    + "\n"
                    + e.getMessage(),

                    "Error",

                    Messagebox.OK,

                    Messagebox.ERROR
            );
        }
    }
    public void onClick$btnReturnConfirm(Event event) {

        // Validate reason
        if (cmbSendBackReason == null
                || cmbSendBackReason.getSelectedItem() == null) {

            Messagebox.show(
                    "Please select a reason for sending back.",
                    "Validation",
                    Messagebox.OK,
                    Messagebox.EXCLAMATION
            );

            return;
        }

        try {

            if (cheques == null
                    || cheques.isEmpty()
                    || currentIndex < 0
                    || currentIndex >= cheques.size()) {

                return;
            }

            OutwardCheque cheque = cheques.get(currentIndex);

            Comboitem selectedItem =
                    cmbSendBackReason.getSelectedItem();

            SendBackReason selectedReason =
                    (SendBackReason) selectedItem.getValue();

            String remarks =
                    txtReturnRemarks != null
                            ? txtReturnRemarks.getValue()
                            : "";

            if (remarks == null) {
                remarks = "";
            }

            System.out.println("================================");
            System.out.println("RETURN TO MAKER");
            System.out.println("Batch No       : " + batchNo);
            System.out.println(
                    "Cheque Number  : "
                            + cheque.getChequeNumber()
            );
            System.out.println(
                    "Reason ID      : "
                            + selectedReason.getReasonId()
            );
            System.out.println(
                    "Reason Code    : "
                            + selectedReason.getReasonCode()
            );
            System.out.println(
                    "Reason Name    : "
                            + selectedReason.getReasonName()
            );
            System.out.println(
                    "Remarks        : "
                            + remarks
            );
            System.out.println("================================");

            // TODO:
            // Call service method here to update database
            //
            // outwardCheckerQueueService.returnToMaker(
            //         cheque.getChequeNumber(),
            //         selectedReason.getReasonId(),
            //         remarks
            // );

            // Close popup
            returnMakerWindow.setVisible(false);

            Messagebox.show(
                    "Cheque has been returned to Maker successfully.",
                    "Success",
                    Messagebox.OK,
                    Messagebox.INFORMATION
            );

        } catch (Exception e) {

            showError(
                    "Unable to return cheque to Maker.",
                    e
            );
        }
    }
    public void onClick$btnReturnCancel(Event event) {

        if (returnMakerWindow != null) {

            returnMakerWindow.setVisible(false);
        }
    }
    
    public void onClick$btnReject(Event event) {

        int result =
                Messagebox.show(
                        "Are you sure you want to reject this cheque?",
                        "Reject Cheque",
                        Messagebox.YES | Messagebox.NO,
                        Messagebox.QUESTION);

        if (result != Messagebox.YES) {
            return;
        }

        try {

        	OutwardCheque cheque = cheques.get(currentIndex);

            outwardCheckerQueueService.rejectCheque(
                    cheque.getChequeNumber()
            );

            cheque.setChequeStatus("REJECTED");

            moveToNextCheque();

        } catch (Exception e) {

            showError(
                    "Unable to reject cheque.",
                    e
            );
        }
    }


    private void moveToNextCheque() {

        if (cheques == null
                || cheques.isEmpty()) {

            return;
        }

        if (currentIndex
                < cheques.size() - 1) {

            currentIndex++;

            displayCheque();

        } else {

            displayCheque();

            Messagebox.show(
                    "All cheques in batch "
                    + batchNo
                    + " have been processed.",
                    "Checker Queue",
                    Messagebox.OK,
                    Messagebox.INFORMATION
            );
        }
    }


    public void onClick$btnZoomIn() {

        if (zoomLevel < 3.0) {

            zoomLevel += 0.25;
        }

        applyZoom();
    }


    public void onClick$btnZoomOut() {

        if (zoomLevel > 0.5) {

            zoomLevel -= 0.25;
        }

        applyZoom();
    }


    public void onClick$btnZoomReset() {

        zoomLevel = 1.0;

        applyZoom();
    }

    // APPLY ZOOM

    private void applyZoom() {

        if (imgCheque == null) {
            return;
        }

        String zoomStyle =
                "transform: scale("
                + zoomLevel
                + ");"
                + "transform-origin: center center;"
                + "transition: transform 0.2s ease;";

        imgCheque.setStyle(
                zoomStyle
        );

        if (lblZoom != null) {

            lblZoom.setValue(
                    ((int) (zoomLevel * 100))
                    + "%"
            );
        }
    }

 
    private void showError(
            String message,
            Exception e) {

        e.printStackTrace();

        Messagebox.show(
                message
                + "\n"
                + e.getMessage(),
                "Error",
                Messagebox.OK,
                Messagebox.ERROR
        );
    }

    // NULL SAFE

    private String nullSafe(String value) {

        return value == null
                ? ""
                : value;
    }
}
