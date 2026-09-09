package com.iispl.cts.controller.outward.maker;

import com.iispl.cts.common.util.SecurityUtil;
import com.iispl.cts.service.outward.MakerReportService;
import com.iispl.cts.serviceimpl.outward.MakerReportServiceImpl;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import java.text.SimpleDateFormat;
import java.util.Date;

public class MakerReportController extends GenericForwardComposer<Component> {

    private static final long serialVersionUID = 1L;

    private Datebox dtFromDate;
    private Datebox dtToDate;
    private Button btnExportCsv;
    private Button btnExportPdf;

    private final MakerReportService makerReportService = new MakerReportServiceImpl();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        
        super.doAfterCompose(comp);

        Date today = new Date();
        if (dtFromDate != null) dtFromDate.setValue(today);
        if (dtToDate != null) dtToDate.setValue(today);
    }

    private String getMakerId() {
        String makerId = (String) Sessions.getCurrent().getAttribute("userId");
        if (makerId == null || makerId.trim().isEmpty()) {
            makerId = "USR1001";
        }
        return makerId;
    }

    public void onClick$btnExportCsv() {
        Date fromDate = dtFromDate.getValue();
        Date toDate = dtToDate.getValue();

        if (fromDate == null || toDate == null) {
            Messagebox.show("Please select both From Date and To Date.", "Validation", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }
        if (fromDate.after(toDate)) {
            Messagebox.show("From Date cannot be later than To Date.", "Validation", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        String makerId = getMakerId();
        try {
            byte[] csvBytes = makerReportService.generateMakerReportCsv(makerId, fromDate, toDate);

            SimpleDateFormat fileSdf = new SimpleDateFormat("yyyyMMdd");
            String fileName = "Maker_Report_" + makerId + "_" + fileSdf.format(fromDate) + "_to_" + fileSdf.format(toDate) + ".csv";

            Filedownload.save(csvBytes, "text/csv", fileName);

        } catch (Throwable t) {
            t.printStackTrace();
            Messagebox.show("Failed to export CSV: " + t.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
        }
    }

    public void onClick$btnExportPdf() {
        Date fromDate = dtFromDate.getValue();
        Date toDate = dtToDate.getValue();

        if (fromDate == null || toDate == null) {
            Messagebox.show("Please select both From Date and To Date.", "Validation", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }
        if (fromDate.after(toDate)) {
            Messagebox.show("From Date cannot be later than To Date.", "Validation", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        String makerId = getMakerId();
        try {
            byte[] pdfBytes = makerReportService.generateMakerReportPdf(makerId, fromDate, toDate);

            SimpleDateFormat fileSdf = new SimpleDateFormat("yyyyMMdd");
            String fileName = "Maker_Report_" + makerId + "_" + fileSdf.format(fromDate) + "_to_" + fileSdf.format(toDate) + ".pdf";

            Filedownload.save(pdfBytes, "application/pdf", fileName);

        } catch (Throwable t) {
            t.printStackTrace();
            Messagebox.show("Failed to export PDF: " + t.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
        }
    }
}