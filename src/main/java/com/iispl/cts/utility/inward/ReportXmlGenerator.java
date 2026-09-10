package com.iispl.cts.utility.inward;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

import com.iispl.cts.dto.InwardReportChequeDTO;
import com.iispl.cts.enums.inward.InwardBatchStatus;
import com.iispl.cts.enums.inward.InwardChequeStatus;

public class ReportXmlGenerator {

    private ReportXmlGenerator() {
    }

    public static String generateBatchSummaryXml(String batchId,List<InwardReportChequeDTO> cheques,
            String generatedBy) {

        StringBuilder xml = new StringBuilder();

        List<InwardReportChequeDTO> approvedCheques = cheques.stream()
                .filter(c -> c != null && InwardChequeStatus.ACCEPTED.toString().equalsIgnoreCase(c.getChequeStatus()))
                .collect(Collectors.toList());

        List<InwardReportChequeDTO> rejectedCheques = cheques.stream()
                .filter(c -> c != null && InwardChequeStatus.REJECTED.toString().equalsIgnoreCase(c.getChequeStatus()))
                .collect(Collectors.toList());

        BigDecimal totalAmount = cheques.stream()
                .filter(c -> c != null)
                .map(c -> c.getChequeAmount() != null ? c.getChequeAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        BigDecimal approvedAmount = approvedCheques.stream()
                .map(c -> c.getChequeAmount() != null ? c.getChequeAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, (a, b) -> a.add(b));

        BigDecimal rejectedAmount = rejectedCheques.stream()
                .map(c -> c.getChequeAmount() != null ? c.getChequeAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO,(a, b) -> a.add(b));

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<BatchSummaryReport>\n");
        xml.append("    <ReportInformation>\n");
        appendElement(xml, "BatchId", batchId, 8);
        appendElement(xml, "GeneratedAt", LocalDate.now().toString(), 8);
        appendElement(xml, "GeneratedBy", generatedBy, 8);
        xml.append("    </ReportInformation>\n");
        xml.append("    <Summary>\n");
        appendElement(xml, "TotalCheques", String.valueOf(cheques.size()), 8);
        appendElement(xml, "ApprovedCheques", String.valueOf(approvedCheques.size()), 8);
        appendElement(xml, "RejectedCheques", String.valueOf(rejectedCheques.size()), 8);
        appendElement(xml, "TotalAmount", formatAmount(totalAmount), 8);
        appendElement(xml, "ApprovedAmount", formatAmount(approvedAmount), 8);
        appendElement(xml, "RejectedAmount", formatAmount(rejectedAmount), 8);
        xml.append("    </Summary>\n");
        xml.append("    <ApprovedCheques>\n");

        for (InwardReportChequeDTO cheque : approvedCheques) {
            appendCheque(xml, cheque, false);
        }

        xml.append("    </ApprovedCheques>\n");

        xml.append("    <RejectedCheques>\n");

        for (InwardReportChequeDTO cheque : rejectedCheques) {
            appendCheque(xml, cheque, true);
        }

        xml.append("    </RejectedCheques>\n");

        xml.append("</BatchSummaryReport>");

        return xml.toString();
    }

    public static String generateRrfXml(
            String batchId,
            List<InwardReportChequeDTO> rejectedCheques,
            String generatedBy) {

        BigDecimal totalAmount = rejectedCheques.stream()
                .filter(c -> c != null)
                .map(c -> c.getChequeAmount() != null ? c.getChequeAmount() : BigDecimal.ZERO)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        StringBuilder xml = new StringBuilder();

        xml.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        xml.append("<RRFReport>\n");
        xml.append("    <ReportInformation>\n");
        appendElement(xml, "RRFReferenceNo", "RRF-" + batchId, 8);
        appendElement(xml, "BatchId", batchId, 8);
        appendElement(xml, "GenerationDate", LocalDate.now().toString(), 8);
        appendElement(xml, "GeneratedBy", generatedBy, 8);
        xml.append("    </ReportInformation>\n");
        xml.append("    <Summary>\n");
        appendElement(xml, "TotalRejectedCheques", String.valueOf(rejectedCheques.size()), 8);
        appendElement(xml, "TotalRejectedAmount", formatAmount(totalAmount), 8);
        xml.append("    </Summary>\n");
        xml.append("    <RejectedCheques>\n");

        for (InwardReportChequeDTO cheque : rejectedCheques) {
            appendCheque(xml, cheque, true);
        }

        xml.append("    </RejectedCheques>\n");

        xml.append("</RRFReport>");

        return xml.toString();
    }

    private static void appendCheque(
            StringBuilder xml,
            InwardReportChequeDTO cheque,
            boolean includeRejection) {

        xml.append("        <Cheque>\n");

        appendElement(xml, "ChequeId", cheque.getInwardChequeId(), 12);
        appendElement(xml, "ChequeNumber", cheque.getChequeNumber(), 12);
        appendElement(xml, "MICRCode", cheque.getMicrCode(), 12);
        appendElement(xml, "DraweeName", cheque.getDraweeName(), 12);
        appendElement(xml, "DraweeAccountNumber", cheque.getDraweeAccountNumber(), 12);
        appendElement(xml, "DraweeBank", cheque.getDraweeBank(), 12);
        appendElement(xml, "PayeeName", cheque.getPayeeName(), 12);
        appendElement(xml, "PayeeAccountNumber", cheque.getPayeeAccountNumber(), 12);
        appendElement(xml, "PresentingBank", cheque.getPresentingBank(), 12);
        appendElement(xml, "ChequeAmount", formatAmount(cheque.getChequeAmount()), 12);
        appendElement(xml, "ChequeDate", formatDate(cheque.getChequeDate()), 12);
        appendElement(xml, "Status", cheque.getChequeStatus(), 12);

        if (includeRejection) {

            xml.append("            <Rejection>\n");

            String reasonCode = cheque.getRejectedReasonCode();

            if (reasonCode == null || reasonCode.trim().isEmpty()) {
                reasonCode = cheque.getRejectedReasonId() != null
                        ? cheque.getRejectedReasonId().toString()
                        : "";
            }

            appendElement(xml, "ReasonCode", reasonCode, 16);
            appendElement(xml, "Reason", cheque.getRejectedReasonName(), 16);
            appendElement(xml, "Remarks", cheque.getRemarks(), 16);
            appendElement(xml, "RejectedBy", cheque.getRejectedBy(), 16);
            appendElement(xml, "RejectedAt", formatDate(cheque.getRejectedAt()), 16);

            xml.append("            </Rejection>\n");
        }

        xml.append("        </Cheque>\n");
    }

    private static void appendElement(StringBuilder xml,String name,String value,int spaces) {

        String indentation = " ".repeat(spaces);

        xml.append(indentation).append("<").append(name).append(">");

        if (value != null) {
            xml.append(escapeXml(value));
        }

        xml.append("</").append(name).append(">\n");
    }

    private static String formatAmount(BigDecimal amount) {
        return amount != null
                ? amount.setScale(2).toPlainString()
                : "0.00";
    }

    private static String formatDate(LocalDateTime date) {
        return date != null
                ? date.toLocalDate().toString()
                : "";
    }

    private static String escapeXml(String value) {
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }
}