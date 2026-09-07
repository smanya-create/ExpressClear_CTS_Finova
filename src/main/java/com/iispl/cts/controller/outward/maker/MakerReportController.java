package com.iispl.cts.controller.outward.maker;

import java.text.SimpleDateFormat;
import java.util.Date;

import org.zkoss.zk.ui.Component;
import org.zkoss.zk.ui.Sessions;
import org.zkoss.zk.ui.util.GenericForwardComposer;
import org.zkoss.zul.Button;
import org.zkoss.zul.Datebox;
import org.zkoss.zul.Filedownload;
import org.zkoss.zul.Messagebox;

import com.iispl.cts.service.outward.MakerReportService;
import com.iispl.cts.serviceimpl.outward.MakerReportServiceImpl;

public class MakerReportController extends GenericForwardComposer<Component> {
	private static final long serialVersionUID = 1L;

    private Datebox dtFromDate;
    private Datebox dtToDate;
    private Button btnGenerate;

    private final MakerReportService makerReportService = new MakerReportServiceImpl();

    @Override
    public void doAfterCompose(Component comp) throws Exception {
        super.doAfterCompose(comp);
        Date today = new Date();
        if (dtFromDate != null) dtFromDate.setValue(today);
        if (dtToDate != null) dtToDate.setValue(today);
    }

    public void onClick$btnGenerate() {
        Date fromDate = dtFromDate.getValue();
        Date toDate = dtToDate.getValue();

        if (fromDate == null || toDate == null) {
            Messagebox.show("Please select both From Date and To Date.", "Validation Error", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        if (fromDate.after(toDate)) {
            Messagebox.show("From Date cannot be later than To Date.", "Validation Error", Messagebox.OK, Messagebox.EXCLAMATION);
            return;
        }

        String makerId = (String) Sessions.getCurrent().getAttribute("userId");
        if (makerId == null || makerId.trim().isEmpty()) {
            makerId = "USR1001";
        }

        try {
            byte[] htmlBytes = makerReportService.generateMakerReportHtml(makerId, fromDate, toDate);

            SimpleDateFormat fileSdf = new SimpleDateFormat("yyyyMMdd");
            String fileName = "Maker_Report_" + makerId + "_" + fileSdf.format(fromDate) + "_to_" + fileSdf.format(toDate) + ".html";

            Filedownload.save(htmlBytes, "text/html", fileName);

        } catch (Throwable t) {
            t.printStackTrace();
            Messagebox.show("Failed to generate Maker Report: " + t.getMessage(), "Error", Messagebox.OK, Messagebox.ERROR);
        }
    }

}
