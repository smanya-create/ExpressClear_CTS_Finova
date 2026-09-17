package com.iispl.cts.controller.outward.maker;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.zkoss.util.media.Media;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Path;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.event.Event;
import org.zkoss.zk.ui.event.EventListener;
import org.zkoss.zk.ui.event.Events;
import org.zkoss.zk.ui.event.InputEvent;
import org.zkoss.zk.ui.event.UploadEvent;
import org.zkoss.zk.ui.util.Composer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Combobox;
import org.zkoss.zul.Decimalbox;
import org.zkoss.zul.Include;
import org.zkoss.zul.Intbox;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import org.zkoss.zul.Textbox;
import org.zkoss.zul.Vlayout;
import org.zkoss.zul.Window;

import com.iispl.cts.dto.BatchValidationData;
import com.iispl.cts.dto.ValidationResult;
import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.outward.batchvalidator.MicrCodeHelper;
import com.iispl.cts.parser.BatchXmlParser;
import com.iispl.cts.service.outward.BatchValidationService;
import com.iispl.cts.service.outward.OutwardMakerService;
import com.iispl.cts.service.outward.ScanService;
import com.iispl.cts.serviceimpl.outward.BatchValidationServiceImpl;
import com.iispl.cts.serviceimpl.outward.OutwardMakerServiceImpl;
import com.iispl.cts.serviceimpl.outward.ScanServiceImpl;

public class OutwardMakerBatchUploadController implements Composer<Component> {

    private static final long serialVersionUID = 1L;
    private static final String SESSION_CURRENT_BATCH_ID = "OUTWARD_MAKER_CURRENT_BATCH_ID";
    private static final int CHEQUES_PER_PAGE = 5;

    // =========================================================
    // ZUL COMPONENTS
    // =========================================================

    private Intbox txtExpectedTotalCheques;
    private Decimalbox txtExpectedTotalChequeAmount;
    private Textbox txtChequeFolder;

    private Button btnBrowse;
    private Button btnValidateBatch;

    private Vlayout vltBatchResult;

    private Label lblBatchIdValue;
    private Label lblTotalChequesValue;
    private Label lblTotalAmountValue;
    private Label lblChequeListCount;

    private Textbox txtChequeSearch;
    private Combobox cmbChequeFilter;
    private Button btnClearChequeFilter;

    private Listbox lstCheques;
    private Button btnChequePrevious;
    private Label lblChequePage;
    private Button btnChequeNext;

    // =========================================================
    // STATE & SERVICES
    // =========================================================

    private List<ScanCheque> currentChequeList = new ArrayList<ScanCheque>();
    private int currentChequePage = 0;
    private Component pageRoot;

    private ScanService scanService;
    private BatchValidationService batchValidationService;
    private OutwardMakerService outwardMakerService;

    private File uploadedZipFile;
    private String batchId;

    // =========================================================
    // INITIALIZATION
    // =========================================================

    @Override
    public void doAfterCompose(Component component) throws Exception {

        pageRoot = component.getPage().getFirstRoot();

        // UI Components - Upload
        txtExpectedTotalCheques = (Intbox) component.getFellow("txtExpectedTotalCheques");
        txtExpectedTotalChequeAmount = (Decimalbox) component.getFellow("txtExpectedTotalChequeAmount");
        txtChequeFolder = (Textbox) component.getFellow("txtChequeFolder");
        btnBrowse = (Button) component.getFellow("btnBrowse");
        btnValidateBatch = (Button) component.getFellow("btnValidateBatch");

        // UI Components - Summary Details
        vltBatchResult = (Vlayout) component.getFellow("vltBatchResult");
        lblBatchIdValue = (Label) component.getFellow("lblBatchIdValue");
        lblTotalChequesValue = (Label) component.getFellow("lblTotalChequesValue");
        lblTotalAmountValue = (Label) component.getFellow("lblTotalAmountValue");
        lblChequeListCount = (Label) component.getFellow("lblChequeListCount");

        // UI Components - Filter Controls
        txtChequeSearch = (Textbox) component.getFellow("txtChequeSearch");
        cmbChequeFilter = (Combobox) component.getFellow("cmbChequeFilter");
        btnClearChequeFilter = (Button) component.getFellow("btnClearChequeFilter");

        // UI Components - List & Pagination
        lstCheques = (Listbox) component.getFellow("lstCheques");
        btnChequePrevious = (Button) component.getFellow("btnChequePrevious");
        lblChequePage = (Label) component.getFellow("lblChequePage");
        btnChequeNext = (Button) component.getFellow("btnChequeNext");

        // Service Instantiation
        scanService = new ScanServiceImpl();
        batchValidationService = new BatchValidationServiceImpl();
        outwardMakerService = new OutwardMakerServiceImpl();

        // Initial State Setup
        vltBatchResult.setVisible(false);
        lstCheques.getItems().clear();
        currentChequeList.clear();
        currentChequePage = 0;
        lblChequeListCount.setValue("0 Cheques");

        if (cmbChequeFilter.getValue() == null || cmbChequeFilter.getValue().trim().isEmpty()) {
            cmbChequeFilter.setValue("All Cheques");
        }

        updateChequePagination();
        loadCurrentSessionBatch();
        btnValidateBatch.setDisabled(true);

        // Event Listeners Setup
        btnBrowse.addEventListener(Events.ON_UPLOAD, new EventListener<UploadEvent>() {
            @Override
            public void onEvent(UploadEvent event) {
                handleZipUpload(event);
            }
        });

        btnValidateBatch.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) {
                validateBatch();
            }
        });

        // LIVE SEARCH: Passes typed value directly on key press
        txtChequeSearch.addEventListener(Events.ON_CHANGING, new EventListener<InputEvent>() {
            @Override
            public void onEvent(InputEvent event) {
                applyChequeFilter(event.getValue());
            }
        });

        cmbChequeFilter.addEventListener(Events.ON_CHANGE, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) {
                currentChequePage = 0;
                displayFilteredChequeList();
            }
        });

        btnClearChequeFilter.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) {
                txtChequeSearch.setValue("");
                cmbChequeFilter.setValue("All Cheques");
                currentChequePage = 0;
                displayFilteredChequeList("");
            }
        });

        btnChequePrevious.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) {
                if (currentChequePage > 0) {
                    currentChequePage--;
                    displayFilteredChequeList();
                }
            }
        });

        btnChequeNext.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
            @Override
            public void onEvent(Event event) {
                int totalPages = getTotalChequePages();
                if (currentChequePage < totalPages - 1) {
                    currentChequePage++;
                    displayFilteredChequeList();
                }
            }
        });
    }

    // =========================================================
    // SESSION BATCH LOADING
    // =========================================================

    private void loadCurrentSessionBatch() {
        Session session = Sessions.getCurrent();
        if (session == null) return;

        Object sessionBatchId = session.getAttribute(SESSION_CURRENT_BATCH_ID);
        if (sessionBatchId == null) return;

        String currentSessionBatchId = sessionBatchId.toString().trim();
        if (currentSessionBatchId.isEmpty()) return;

        ScanBatch makerBatch = outwardMakerService.getMakerBatch(currentSessionBatchId);
        if (makerBatch == null) {
            batchId = null;
            vltBatchResult.setVisible(false);
            lstCheques.getItems().clear();
            currentChequeList.clear();
            currentChequePage = 0;
            updateChequePagination();
            return;
        }

        batchId = currentSessionBatchId;
        List<ScanCheque> batchCheques = outwardMakerService.getMakerBatchCheques(batchId);
        if (batchCheques == null) {
            batchCheques = new ArrayList<ScanCheque>();
        }

        currentChequeList = new ArrayList<ScanCheque>(batchCheques);
        displayBatchInformation(makerBatch, batchCheques);
        displayChequeList(batchCheques);
    }

    // =========================================================
    // ZIP UPLOAD HANDLING
    // =========================================================

    private void handleZipUpload(UploadEvent uploadEvent) {
        Media media = uploadEvent.getMedia();
        if (media == null) return;

        String fileName = media.getName();
        if (fileName == null || !fileName.toLowerCase().endsWith(".zip")) {
            showErrorMessage("Please upload a ZIP file.");
            return;
        }

        String tempDataPath = Executions.getCurrent().getDesktop().getWebApp().getRealPath("/TempData");
        if (tempDataPath == null) {
            showErrorMessage("Unable to access TempData directory.");
            return;
        }

        File tempDataDirectory = new File(tempDataPath);
        if (!tempDataDirectory.exists() && !tempDataDirectory.mkdirs()) {
            showErrorMessage("Unable to create TempData directory.");
            return;
        }

        File destinationFile = new File(tempDataDirectory, fileName);

        try (InputStream inputStream = media.getStreamData();
             FileOutputStream outputStream = new FileOutputStream(destinationFile)) {

            byte[] buffer = new byte[8192];
            int bytesRead;
            while ((bytesRead = inputStream.read(buffer)) != -1) {
                outputStream.write(buffer, 0, bytesRead);
            }
            outputStream.flush();

        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Unable to save uploaded ZIP file.");
            return;
        }

        uploadedZipFile = destinationFile;
        txtChequeFolder.setValue(fileName);

        // Reset display lists for the new upload
        vltBatchResult.setVisible(false);
        lstCheques.getItems().clear();
        currentChequeList.clear();
        currentChequePage = 0;
        lblChequeListCount.setValue("0 Cheques");
        updateChequePagination();
        btnValidateBatch.setDisabled(false);
    }

    // =========================================================
    // BATCH VALIDATION
    // =========================================================

    private void validateBatch() {

        if (uploadedZipFile == null || !uploadedZipFile.exists()) {
            showErrorMessage("Please upload a ZIP file before validating the batch.");
            return;
        }

        Integer expectedTotalCheques = txtExpectedTotalCheques.getValue();
        if (expectedTotalCheques == null || expectedTotalCheques <= 0) {
            showErrorMessage("Please enter a valid expected cheque count.");
            return;
        }

        BigDecimal expectedTotalAmount = txtExpectedTotalChequeAmount.getValue();
        if (expectedTotalAmount == null || expectedTotalAmount.signum() <= 0) {
            showErrorMessage("Please enter a valid expected total amount.");
            return;
        }

        try {
            // Step 1: Parse
            BatchXmlParser parser = new BatchXmlParser();
            BatchXmlParser.ParsedBatchData parsedData = parser.parse(
                    expectedTotalCheques,
                    expectedTotalAmount,
                    uploadedZipFile.getAbsolutePath());

            // Step 2: Extract Parsed Entities
            ScanBatch scanBatch = parsedData.getScanBatch();
            if (scanBatch == null) {
                throw new RuntimeException("Batch information could not be parsed.");
            }

            List<ScanCheque> chequeList = parsedData.getChequeList();
            if (chequeList == null || chequeList.isEmpty()) {
                throw new RuntimeException("No cheques were found in the uploaded batch.");
            }

            batchId = scanBatch.getScannedBatchId();
            if (batchId == null || batchId.trim().isEmpty()) {
                throw new RuntimeException("Batch ID was not found in the XML.");
            }
            batchId = batchId.trim();

            // Step 3: Service Validation
            BatchValidationData validationData = new BatchValidationData();
            validationData.setBatch(scanBatch);
            validationData.setChequeList(chequeList);
            validationData.setExpectedTotalCheques(expectedTotalCheques);
            validationData.setExpectedTotalAmount(expectedTotalAmount);

            ValidationResult validationResult = batchValidationService.validateBatch(validationData);
            if (!validationResult.isValid()) {
                showErrorMessage(validationResult.getMessage());
                return;
            }

            // Step 4: MICR Code Checking & Database Persist
            MicrCodeHelper micrCodeHelper = new MicrCodeHelper();
            List<ScanCheque> micrChequeList = micrCodeHelper.checkMicrCode(chequeList);

            String savedBatchId = scanService.saveScanBatch(scanBatch, micrChequeList);
            if (savedBatchId == null || savedBatchId.trim().isEmpty()) {
                throw new RuntimeException("Batch could not be saved.");
            }

            batchId = savedBatchId.trim();

            // Step 5: Session & UI Updates
            Session session = Sessions.getCurrent();
            if (session != null) {
                session.setAttribute(SESSION_CURRENT_BATCH_ID, batchId);
            }

            currentChequeList = new ArrayList<ScanCheque>(chequeList);
            displayBatchInformation(scanBatch, chequeList);
            displayChequeList(chequeList);

            vltBatchResult.setVisible(true);

            // Step 6: Clear Inputs
            txtExpectedTotalCheques.setValue(null);
            txtExpectedTotalChequeAmount.setValue(BigDecimal.ZERO);
            txtChequeFolder.setValue("");
            uploadedZipFile = null;
            btnValidateBatch.setDisabled(true);

        } catch (Exception e) {
            e.printStackTrace();
            String errorMessage = e.getMessage();
            if (errorMessage == null || errorMessage.trim().isEmpty()) {
                errorMessage = "Something went wrong while processing the batch.";
            }
            showErrorMessage(errorMessage);
        }
    }

    // =========================================================
    // UI DISPLAY & SEARCH FILTERING
    // =========================================================

    private void displayBatchInformation(ScanBatch scanBatch, List<ScanCheque> chequeList) {
        if (scanBatch == null) return;

        String displayBatchId = scanBatch.getScannedBatchId();
        if (displayBatchId == null || displayBatchId.trim().isEmpty()) {
            displayBatchId = batchId;
        }

        lblBatchIdValue.setValue(safe(displayBatchId));
        int totalCheques = (chequeList == null) ? 0 : chequeList.size();
        lblTotalChequesValue.setValue(String.valueOf(totalCheques));

        BigDecimal totalAmount = calculateChequeTotal(chequeList);
        lblTotalAmountValue.setValue(formatAmount(totalAmount));

        vltBatchResult.setVisible(true);
    }

    private void displayChequeList(List<ScanCheque> chequeList) {
        if (chequeList == null) {
            currentChequeList = new ArrayList<ScanCheque>();
        } else {
            currentChequeList = new ArrayList<ScanCheque>(chequeList);
        }
        currentChequePage = 0;
        displayFilteredChequeList();
    }

    private void applyChequeFilter(String searchValue) {
        currentChequePage = 0;
        displayFilteredChequeList(searchValue);
    }

    private void displayFilteredChequeList() {
        displayFilteredChequeList(txtChequeSearch.getValue());
    }

    private void displayFilteredChequeList(String searchKeyword) {
        lstCheques.getItems().clear();

        String searchText = (searchKeyword == null) ? "" : searchKeyword.trim().toLowerCase();
        String filterValue = cmbChequeFilter.getValue();
        if (filterValue == null || filterValue.trim().isEmpty()) {
            filterValue = "All Cheques";
        }

        List<ScanCheque> filteredCheques = new ArrayList<ScanCheque>();

        for (ScanCheque cheque : currentChequeList) {
            if (cheque == null) continue;

            String chequeNumber = cheque.getChequeNumber();
            String accountNo = cheque.getDraweeAccountNumber();

            // Search matches either Cheque Number or Drawee Account Number
            boolean matchesSearch = searchText.isEmpty() || 
                    (chequeNumber != null && chequeNumber.toLowerCase().contains(searchText)) ||
                    (accountNo != null && accountNo.toLowerCase().contains(searchText));

            if (!matchesSearch) continue;

            String status = cheque.getChequeStatus();
            if (status == null) status = "";

            boolean matchesType = true;

            if ("MICR Repair".equalsIgnoreCase(filterValue)) {
                matchesType = "PENDING_MICR_REPAIR".equalsIgnoreCase(status) 
                           || "MICR_REJECTED".equalsIgnoreCase(status)
                           || "MICR_REJECTION_PENDING".equalsIgnoreCase(status);
            } else if ("Data Entry".equalsIgnoreCase(filterValue) || "Pending Data Entry".equalsIgnoreCase(filterValue)) {
                matchesType = "PENDING_DATA_ENTRY".equalsIgnoreCase(status) 
                           || "MICR_REPAIRED".equalsIgnoreCase(status);
            }

            if (matchesType) {
                filteredCheques.add(cheque);
            }
        }

        // Pagination calculations
        int totalPages = filteredCheques.isEmpty() ? 1 : (int) Math.ceil((double) filteredCheques.size() / CHEQUES_PER_PAGE);
        if (currentChequePage >= totalPages) {
            currentChequePage = totalPages - 1;
        }
        if (currentChequePage < 0) {
            currentChequePage = 0;
        }

        int startIndex = currentChequePage * CHEQUES_PER_PAGE;
        int endIndex = Math.min(startIndex + CHEQUES_PER_PAGE, filteredCheques.size());
        int serialNumber = startIndex + 1;

        for (int i = startIndex; i < endIndex; i++) {
            ScanCheque cheque = filteredCheques.get(i);
            if (cheque == null) continue;

            Listitem item = new Listitem();

            // Item Columns
            item.appendChild(new Listcell(String.valueOf(serialNumber++)));
            item.appendChild(new Listcell(safe(cheque.getChequeNumber())));
            item.appendChild(new Listcell(safe(cheque.getDraweeAccountNumber())));
            item.appendChild(new Listcell(formatDate(cheque.getChequeDate())));
            item.appendChild(new Listcell(formatAmount(cheque.getChequeAmount())));

            // Status Cell
            String rawStatus = cheque.getChequeStatus();
            if (rawStatus == null || rawStatus.trim().isEmpty()) {
                rawStatus = "";
            }

            String displayStatus = getDisplayStatus(rawStatus);

            Listcell statusCell = new Listcell();
            Label statusLabel = new Label(displayStatus);

            boolean isMicrRepair = "PENDING_MICR_REPAIR".equalsIgnoreCase(rawStatus) 
                                || "MICR_REJECTED".equalsIgnoreCase(rawStatus)
                                || "MICR_REJECTION_PENDING".equalsIgnoreCase(rawStatus);

            if (isMicrRepair) {
                statusLabel.setSclass("chequeStatusMicrRepair");
            } else {
                statusLabel.setSclass("chequeStatusDataEntry");
            }

            statusCell.appendChild(statusLabel);
            item.appendChild(statusCell);

            // Action Buttons
            Listcell actionCell = new Listcell();
            Button actionButton = new Button();

            final String selectedBatchId = (batchId != null) ? batchId : "";
            final String selectedChequeId = cheque.getScannedChequeId();

            if (isMicrRepair) {
                actionButton.setLabel("MICR Repair");
                actionButton.setSclass("btnChequeAction");
                actionButton.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
                    @Override
                    public void onEvent(Event event) {
                        openMicrRepairPopup("SCAN", selectedBatchId, selectedChequeId);
                    }
                });
            } else {
                actionButton.setLabel("Data Entry");
                actionButton.setSclass("btnChequeAction");
                actionButton.addEventListener(Events.ON_CLICK, new EventListener<Event>() {
                    @Override
                    public void onEvent(Event event) {
                        openDataEntryPopup(selectedBatchId, selectedChequeId);
                    }
                });
            }

            actionCell.appendChild(actionButton);
            item.appendChild(actionCell);

            lstCheques.appendChild(item);
        }

        lblChequeListCount.setValue(filteredCheques.size() + " Cheques");
        updateChequePagination();
    }

    // Placeholder for Data Entry Popup
    private void openDataEntryPopup(String batchId, String chequeId) {
        // Handled by your data entry view integration
    }

    // =========================================================
    // PAGINATION HELPERS
    // =========================================================

    private void updateChequePagination() {
        int totalPages = getTotalChequePages();
        if (totalPages <= 0) {
            totalPages = 1;
        }

        lblChequePage.setValue("Page " + (currentChequePage + 1) + " of " + totalPages);
        btnChequePrevious.setDisabled(currentChequePage <= 0);
        btnChequeNext.setDisabled(currentChequePage >= totalPages - 1);
    }

    private int getTotalChequePages() {
        if (currentChequeList == null || currentChequeList.isEmpty()) {
            return 1;
        }
        return (int) Math.ceil((double) currentChequeList.size() / CHEQUES_PER_PAGE);
    }

    // =========================================================
    // DIALOG POPUPS & ACTION BUTTON NAVIGATION
    // =========================================================

    public void openMicrRepairPopup(String source, String batchId, String chequeId) {
        try {
            String src = source;
            if (src == null || src.trim().isEmpty()) {
                src = (String) Sessions.getCurrent().getAttribute("MICR_REPAIR_SOURCE");
            }
            if (src == null || src.trim().isEmpty()) {
                src = "SCAN";
            } else {
                src = src.trim();
            }

            String bId = (batchId != null) ? batchId.trim() : "";
            String cId = (chequeId != null) ? chequeId.trim() : "";

            if (bId.isEmpty()) return;

            // A. Store in Session
            Sessions.getCurrent().setAttribute("MICR_REPAIR_SOURCE", src);
            Sessions.getCurrent().setAttribute("MICR_REPAIR_BATCH_ID", bId);
            Sessions.getCurrent().setAttribute("MICR_REPAIR_CHEQUE_ID", cId);

            Include mainInclude = findMainInclude();

            if (mainInclude != null) {
                // B. Store in Execution attributes & URL query string
                Executions.getCurrent().setAttribute("targetSourceCode", src);
                Executions.getCurrent().setAttribute("targetBatchId", bId);
                Executions.getCurrent().setAttribute("targetChequeId", cId);

                mainInclude.setAttribute("MICR_REPAIR_SOURCE", src);
                mainInclude.setAttribute("MICR_REPAIR_BATCH_ID", bId);
                mainInclude.setAttribute("MICR_REPAIR_CHEQUE_ID", cId);

                mainInclude.setSrc(null);
                mainInclude.setSrc(
                    "/outward/maker/micr-repair/micr-repair.zul"
                    + "?source=" + src
                    + "&batchId=" + bId
                    + "&chequeId=" + cId
                );
            } else {
                // C. Pass into Modal Window Map
                Map<String, Object> args = new HashMap<String, Object>();
                args.put("source", src);
                args.put("batchId", bId);
                args.put("chequeId", cId);

                Component parent = (pageRoot != null) ? pageRoot : Executions.getCurrent().getDesktop().getFirstPage().getFirstRoot();
                Window win = (Window) Executions.createComponents("/outward/maker/micr-repair/micr-repair.zul", parent, args);
                win.doModal();
            }
        } catch (Exception e) {
            e.printStackTrace();
            showErrorMessage("Unable to open MICR Repair view.");
        }
    }

    public void openMicrRepairPopup(String batchId, String chequeId) {
        openMicrRepairPopup("SCAN", batchId, chequeId);
    }

    private Include findMainInclude() {
        try {
            Include inc = (Include) Path.getComponent("/outwardMakerRootWin/mainContentArea");
            if (inc != null) return inc;
        } catch (Exception ignored) {}

        if (Executions.getCurrent() != null) {
            for (org.zkoss.zk.ui.Page page : Executions.getCurrent().getDesktop().getPages()) {
                Component comp = page.getFellowIfAny("mainContentArea", true);
                if (comp == null) {
                    comp = page.getFellowIfAny("mainInclude", true);
                }
                if (comp instanceof Include) {
                    return (Include) comp;
                }
            }
        }
        return null;
    }

    // =========================================================
    // UTILITY METHODS
    // =========================================================

    private String getDisplayStatus(String backendStatus) {
        if (backendStatus == null || backendStatus.trim().isEmpty()) {
            return "-";
        }

        String status = backendStatus.trim().toUpperCase();

        switch (status) {
            case "PENDING_MICR_REPAIR":
                return "MICR Repair";

            case "PENDING_DATA_ENTRY":
                return "Data Entry";

            case "MICR_REJECTED":
                return "MICR Rejected";

            case "MICR_REJECTION_PENDING":
                return "MICR Rejection Pending";

            case "MICR_REPAIRED":
                return "MICR Repaired";

            default:
                String readable = status.replace("_", " ");
                return readable.substring(0, 1).toUpperCase() + readable.substring(1).toLowerCase();
        }
    }

    private BigDecimal calculateChequeTotal(List<ScanCheque> chequeList) {
        BigDecimal total = BigDecimal.ZERO;
        if (chequeList == null) return total;

        for (ScanCheque cheque : chequeList) {
            if (cheque != null && cheque.getChequeAmount() != null) {
                total = total.add(cheque.getChequeAmount());
            }
        }
        return total;
    }

    private String safe(String value) {
        return (value == null || value.trim().isEmpty()) ? "-" : value.trim();
    }

    private String formatDate(Date date) {
        if (date == null) return "-";
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
            return sdf.format(date);
        } catch (Exception e) {
            return "-";
        }
    }

    private String formatAmount(BigDecimal amount) {
        if (amount == null) return "0.00";
        return String.format("%.2f", amount);
    }

    private void showErrorMessage(String message) {
        Messagebox.show(
                message,
                "Error",
                Messagebox.OK,
                Messagebox.ERROR);
    }
}