package com.iispl.cts.controller.inward.checker;

import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Executions;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Label;
import org.zkoss.zul.Listbox;
import org.zkoss.zul.Listcell;
import org.zkoss.zul.Listitem;
import org.zkoss.zul.Messagebox;
import com.iispl.cts.dto.InwardReportChequeDTO;
import com.iispl.cts.dto.ReportSummaryRow;
import com.iispl.cts.service.inward.InwardBatchService;
import com.iispl.cts.serviceimpl.inward.InwardBatchServiceImpl;
import net.sf.jasperreports.engine.JasperCompileManager;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;
import net.sf.jasperreports.engine.JasperReport;
import net.sf.jasperreports.engine.data.JRBeanCollectionDataSource;

public class InwardCheckerReportsController extends GenericForwardComposer<Component> {

    private Listbox reportListbox;
    private InwardBatchService service;

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        service = new InwardBatchServiceImpl();
        loadBatchSummary();
    }

    private void loadBatchSummary() {
        try {
            List<InwardReportChequeDTO> chequeList = service.getChequesByBatch();
            if (chequeList == null || chequeList.isEmpty()) {
                System.out.println("No cheque data returned from service.");
                return;
            }
            System.out.println("Cheque records received: " + chequeList.size());

            Map<String, ReportSummaryRow> batchMap = new LinkedHashMap<String, ReportSummaryRow>();
            for (InwardReportChequeDTO cheque : chequeList) {
                if (cheque == null) {
                    continue;
                }
                String batchId = cheque.getInwardBatchId();
                String chequeStatus = cheque.getChequeStatus();
                System.out.println("Batch ID: " + batchId + " | Status: " + chequeStatus);

                if (batchId == null || batchId.trim().isEmpty()) {
                    continue;
                }

                ReportSummaryRow summary = batchMap.get(batchId);
                if (summary == null) {
                    summary = new ReportSummaryRow();
                    summary.setBatchId(batchId);
                    batchMap.put(batchId, summary);
                }

                summary.setTotalCheques(summary.getTotalCheques() + 1);
                if ("ACCEPTED".equalsIgnoreCase(chequeStatus)) {
                    summary.setApprovedCheques(summary.getApprovedCheques() + 1);
                } else if ("REJECTED".equalsIgnoreCase(chequeStatus)) {
                    summary.setRejectedCheques(summary.getRejectedCheques() + 1);
                }
            }

            System.out.println("Batches prepared: " + batchMap.size());
            for (ReportSummaryRow summary : batchMap.values()) {
                renderBatchRow(summary);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void renderBatchRow(ReportSummaryRow summary) {
        Listitem item = new Listitem();

        Listcell batchIdCell = new Listcell();
        Label batchIdLabel = new Label(String.valueOf(summary.getBatchId()));
        batchIdCell.appendChild(batchIdLabel);
        item.appendChild(batchIdCell);

        Listcell totalCell = new Listcell();
        Label totalLabel = new Label(String.valueOf(summary.getTotalCheques()));
        totalCell.appendChild(totalLabel);
        item.appendChild(totalCell);

        Listcell approvedCell = new Listcell();
        Label approvedLabel = new Label(String.valueOf(summary.getApprovedCheques()));
        approvedCell.appendChild(approvedLabel);
        item.appendChild(approvedCell);

        Listcell rejectedCell = new Listcell();
        Label rejectedLabel = new Label(String.valueOf(summary.getRejectedCheques()));
        rejectedCell.appendChild(rejectedLabel);
        item.appendChild(rejectedCell);

        // ---- RRF button ----
        Listcell rrfCell = new Listcell();
        Button generateRrfButton = new Button("Generate RRF");

        boolean hasRejected = summary != null && summary.getRejectedCheques() != 0;

        if (hasRejected) {
            generateRrfButton.setStyle(
                "background-color: red; color: white; border-radius: 4px; cursor: pointer;");
        } else {
            generateRrfButton.setStyle(
                "background-color: #ABA2A1; color: white; cursor: not-allowed;");
        }

        String rrfBatchId = (summary != null && summary.getBatchId() != null) ? summary.getBatchId() : "";
        generateRrfButton.setAttribute("batchId", rrfBatchId);

        generateRrfButton.addEventListener("onClick", event -> {
            String clickedBatchId = (String) generateRrfButton.getAttribute("batchId");
            if (hasRejected && clickedBatchId != null && !clickedBatchId.isEmpty()) {
                try {
                    generateRrfReport(clickedBatchId);
                } catch (Exception e) {
                    e.printStackTrace();
                    Messagebox.show("Failed to generate RRF report: " + e.getMessage(),
                        "Error", Messagebox.OK, Messagebox.ERROR);
                }
            } else {
                Messagebox.show("No rejected cheques found for this batch.",
                    "Information", Messagebox.OK, Messagebox.INFORMATION);
            }
        });

        rrfCell.appendChild(generateRrfButton);
        item.appendChild(rrfCell);

        // ---- Download button ----
        Listcell downloadCell = new Listcell();
        Button downloadButton = new Button("download");
        downloadButton.setStyle("background-color: green; color: white;");

        String downloadBatchId = (summary != null && summary.getBatchId() != null) ? summary.getBatchId() : "";
        downloadButton.setAttribute("batchId", downloadBatchId);

        downloadButton.addEventListener("onClick", event -> {
            String clickedBatchId = (String) downloadButton.getAttribute("batchId");
            Messagebox.show("Download for Batch ID: " + clickedBatchId,
                "Information", Messagebox.OK, Messagebox.INFORMATION);
        });

        downloadCell.appendChild(downloadButton);
        item.appendChild(downloadCell);

        reportListbox.appendChild(item);
    }

    private List<InwardReportChequeDTO> getRejectedCheques(String batchId) {
        return service.getChequesByBatch().stream()
                .filter(cheque -> batchId.equals(cheque.getInwardBatchId())
                        && "REJECTED".equalsIgnoreCase(cheque.getChequeStatus())
                        && cheque.getRejectionId() != null)
                .collect(Collectors.toList());
    }

    public void generateRrfReport(String batchId) throws Exception {
        List<InwardReportChequeDTO> rejectedCheques = getRejectedCheques(batchId);
        System.out.println("Rejected cheques found for RRF (batch " + batchId + "): " + rejectedCheques.size());

        String jrxmlPath = Executions.getCurrent().getDesktop().getWebApp()
                .getRealPath("/ireports/inward/RRF.jrxml");
        JasperReport jasperReport = JasperCompileManager.compileReport(jrxmlPath);

        double totalAmount = rejectedCheques.stream()
                .mapToDouble(c -> c.getChequeAmount() != null ? Double.parseDouble(c.getChequeAmount().toString()) : 0.0)
                .sum();

        Map<String, Object> parameters = new HashMap<>();
        parameters.put("RRF_REFERENCE_NO", "RRF-" + batchId);
        parameters.put("BATCH_ID", batchId);
        parameters.put("SESSION_DATE", LocalDate.now().toString());
        parameters.put("GENERATION_DATE", new java.util.Date());
        parameters.put("GENERATED_BY", Sessions.getCurrent().getAttribute("LOGGED_USER"));
        parameters.put("TOTAL_REJECTED_CHEQUES", rejectedCheques.size());
        parameters.put("TOTAL_REJECTED_AMOUNT", totalAmount);

        JRBeanCollectionDataSource dataSource = new JRBeanCollectionDataSource(rejectedCheques);
        JasperPrint jasperPrint = JasperFillManager.fillReport(jasperReport, parameters, dataSource);

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        JasperExportManager.exportReportToPdfStream(jasperPrint, baos);

        String downloadFileName = "RRF_" + batchId + ".pdf";
        Filedownload.save(baos.toByteArray(), "application/pdf", downloadFileName);
    }
}