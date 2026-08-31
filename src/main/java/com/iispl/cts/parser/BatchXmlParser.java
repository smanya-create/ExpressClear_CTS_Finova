package com.iispl.cts.parser;

import java.io.ByteArrayInputStream;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;


/**
 * Parses the batch XML file.
 *
 * This class:
 *
 * - Does not access ZUL
 * - Does not access the Controller
 * - Does not contain UI logic
 *
 * Controller -> BatchXmlParser -> ParsedBatchData
 */
public class BatchXmlParser {


    // =========================================================
    // PARSE XML
    // =========================================================

    public ParsedBatchData parse(
            byte[] xmlBytes) throws Exception {


        if (xmlBytes == null
                || xmlBytes.length == 0) {

            throw new Exception(
                    "XML file is empty.");
        }


        // =====================================================
        // DOCUMENT BUILDER
        // =====================================================

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();


        // =====================================================
        // XML SECURITY
        // =====================================================

        factory.setFeature(
                "http://apache.org/xml/features/disallow-doctype-decl",
                true);

        factory.setFeature(
                "http://xml.org/sax/features/external-general-entities",
                false);

        factory.setFeature(
                "http://xml.org/sax/features/external-parameter-entities",
                false);

        factory.setFeature(
                "http://apache.org/xml/features/nonvalidating/load-external-dtd",
                false);

        factory.setXIncludeAware(false);

        factory.setExpandEntityReferences(false);


        // =====================================================
        // PARSE XML
        // =====================================================

        Document document =
                factory
                        .newDocumentBuilder()
                        .parse(
                                new ByteArrayInputStream(
                                        xmlBytes));


        document.getDocumentElement()
                .normalize();


        // =====================================================
        // ROOT
        // =====================================================

        Element root =
                document.getDocumentElement();


        if (root == null) {

            throw new Exception(
                    "XML document has no root element.");
        }


        // =====================================================
        // BATCH HEADER
        // =====================================================

        Element batchHeader =
                findFirstElement(
                        root,
                        "BatchHeader");


        if (batchHeader == null) {

            throw new Exception(
                    "BatchHeader is missing from XML.");
        }


        // =====================================================
        // BATCH ID
        // =====================================================

        String batchId =
                getXmlValue(
                        batchHeader,
                        "BatchID");


        if (isEmpty(batchId)) {

            throw new Exception(
                    "BatchID is missing from XML.");
        }


        // =====================================================
        // TOTAL COUNT
        // =====================================================

        String totalCountValue =
                getXmlValue(
                        batchHeader,
                        "TotalCount");


        if (isEmpty(totalCountValue)) {

            throw new Exception(
                    "TotalCount is missing from XML.");
        }


        int totalCount;


        try {

            totalCount =
                    Integer.parseInt(
                            totalCountValue.trim());

        } catch (NumberFormatException exception) {

            throw new Exception(
                    "Invalid TotalCount in XML: "
                            + totalCountValue,
                    exception);
        }


        // =====================================================
        // TOTAL AMOUNT
        // =====================================================

        String totalAmountValue =
                getXmlValue(
                        batchHeader,
                        "TotalAmount");


        if (isEmpty(totalAmountValue)) {

            throw new Exception(
                    "TotalAmount is missing from XML.");
        }


        BigDecimal totalAmount;


        try {

            totalAmount =
                    new BigDecimal(
                            totalAmountValue.trim());

        } catch (NumberFormatException exception) {

            throw new Exception(
                    "Invalid TotalAmount in XML: "
                            + totalAmountValue,
                    exception);
        }


        // =====================================================
        // CHEQUES
        // =====================================================

        List<ChequeData> cheques =
                new ArrayList<>();


        NodeList chequeNodes =
                root.getElementsByTagName(
                        "Cheque");


        for (int i = 0;
             i < chequeNodes.getLength();
             i++) {


            Element chequeElement =
                    (Element) chequeNodes.item(i);


            ChequeData cheque =
                    parseCheque(
                            chequeElement,
                            i + 1);


            cheques.add(
                    cheque);
        }


        // =====================================================
        // VERIFY XML COUNT
        // =====================================================

        if (totalCount != cheques.size()) {

            throw new Exception(
                    "XML TotalCount does not match "
                            + "the number of cheque records."
                            + "\n\nTotalCount: "
                            + totalCount
                            + "\nCheque Records: "
                            + cheques.size());
        }


        // =====================================================
        // RETURN
        // =====================================================

        return new ParsedBatchData(
                batchId,
                totalCount,
                totalAmount,
                cheques);
    }


    // =========================================================
    // PARSE CHEQUE
    // =========================================================

    private ChequeData parseCheque(
            Element chequeElement,
            int chequeIndex)
            throws Exception {


        if (chequeElement == null) {

            throw new Exception(
                    "Invalid cheque record at position "
                            + chequeIndex);
        }


        // =====================================================
        // CHEQUE DETAILS
        // =====================================================

        Element details =
                findFirstElement(
                        chequeElement,
                        "ChequeDetails");


        if (details == null) {

            throw new Exception(
                    "ChequeDetails is missing for cheque "
                            + chequeIndex);
        }


        ChequeData cheque =
                new ChequeData();


        // =====================================================
        // ITEM NUMBER
        // =====================================================

        cheque.setItemNumber(
                getXmlValue(
                        chequeElement,
                        "ItemNumber"));


        // =====================================================
        // FRONT IMAGE
        // =====================================================

        cheque.setFrontImage(
                getXmlValue(
                        details,
                        "FrontImage"));


        // =====================================================
        // BACK IMAGE
        // =====================================================

        cheque.setBackImage(
                getXmlValue(
                        details,
                        "BackImage"));


        // =====================================================
        // MICR CODE
        // =====================================================

        cheque.setMicrCode(
                getXmlValue(
                        details,
                        "MICRCode"));


        // =====================================================
        // DRAWEE ACCOUNT
        // =====================================================

        cheque.setDraweeAccount(
                getXmlValue(
                        details,
                        "DraweeAccountNumber"));


        // =====================================================
        // PAYEE ACCOUNT
        // =====================================================

        cheque.setPayeeAccount(
                getXmlValue(
                        details,
                        "PayeeAccountNumber"));


        // =====================================================
        // PAYEE NAME
        // =====================================================

        cheque.setPayeeName(
                getXmlValue(
                        details,
                        "PayeeName"));


        // =====================================================
        // AMOUNT
        // =====================================================

        String amountValue =
                getXmlValue(
                        details,
                        "Amount");


        if (isEmpty(amountValue)) {

            throw new Exception(
                    "Amount is missing for cheque "
                            + chequeIndex);
        }


        try {

            cheque.setAmount(
                    new BigDecimal(
                            amountValue.trim()));

        } catch (NumberFormatException exception) {

            throw new Exception(
                    "Invalid amount for cheque "
                            + chequeIndex
                            + ": "
                            + amountValue,
                    exception);
        }


        // =====================================================
        // CHEQUE DATE
        // =====================================================

        cheque.setChequeDate(
                getXmlValue(
                        details,
                        "ChequeDate"));


        // =====================================================
        // STATUS
        // =====================================================

        String status =
                getXmlValue(
                        details,
                        "Status");


        cheque.setStatus(
                status);


        // =====================================================
        // DISPLAY STATUS
        // =====================================================

        if ("PENDING_MICR_REPAIR"
                .equalsIgnoreCase(status)) {

            cheque.setUiStatus(
                    "MICR REPAIR");

        } else {

            cheque.setUiStatus(
                    "NORMAL");
        }


        return cheque;
    }


    // =========================================================
    // FIND FIRST ELEMENT
    // =========================================================

    /*
     * IMPORTANT:
     *
     * This method accepts Element.
     *
     * The Document is converted to its root Element before
     * calling this method.
     *
     * Therefore there is no:
     *
     * getFirstElement(Document, String)
     *
     * mismatch anymore.
     */
    private Element findFirstElement(
            Element parent,
            String tagName) {


        if (parent == null) {
            return null;
        }


        NodeList nodes =
                parent.getElementsByTagName(
                        tagName);


        if (nodes.getLength() == 0) {
            return null;
        }


        return (Element) nodes.item(0);
    }


    // =========================================================
    // GET XML VALUE
    // =========================================================

    private String getXmlValue(
            Element parent,
            String tagName) {


        if (parent == null) {
            return "";
        }


        NodeList nodes =
                parent.getElementsByTagName(
                        tagName);


        if (nodes.getLength() == 0) {
            return "";
        }


        String value =
                nodes.item(0)
                        .getTextContent();


        if (value == null) {
            return "";
        }


        return value.trim();
    }


    // =========================================================
    // EMPTY CHECK
    // =========================================================

    private boolean isEmpty(
            String value) {


        return value == null
                || value.trim().isEmpty();
    }


    // =========================================================
    // PARSED BATCH DATA
    // =========================================================

    public static class ParsedBatchData {

        private final String batchId;

        private final int totalCount;

        private final BigDecimal totalAmount;

        private final List<ChequeData> cheques;


        public ParsedBatchData(
                String batchId,
                int totalCount,
                BigDecimal totalAmount,
                List<ChequeData> cheques) {


            this.batchId =
                    batchId;

            this.totalCount =
                    totalCount;

            this.totalAmount =
                    totalAmount;

            this.cheques =
                    cheques;
        }


        public String getBatchId() {

            return batchId;
        }


        public int getTotalCount() {

            return totalCount;
        }


        public BigDecimal getTotalAmount() {

            return totalAmount;
        }


        public List<ChequeData> getCheques() {

            return cheques;
        }
    }


    // =========================================================
    // CHEQUE DATA
    // =========================================================

    public static class ChequeData {

        private String itemNumber;

        private String frontImage;

        private String backImage;

        private String micrCode;

        private String draweeAccount;

        private String payeeAccount;

        private String payeeName;

        private BigDecimal amount;

        private String chequeDate;

        private String status;

        private String uiStatus;


        public String getItemNumber() {

            return itemNumber;
        }


        public void setItemNumber(
                String itemNumber) {

            this.itemNumber =
                    itemNumber;
        }


        public String getFrontImage() {

            return frontImage;
        }


        public void setFrontImage(
                String frontImage) {

            this.frontImage =
                    frontImage;
        }


        public String getBackImage() {

            return backImage;
        }


        public void setBackImage(
                String backImage) {

            this.backImage =
                    backImage;
        }


        public String getMicrCode() {

            return micrCode;
        }


        public void setMicrCode(
                String micrCode) {

            this.micrCode =
                    micrCode;
        }


        public String getDraweeAccount() {

            return draweeAccount;
        }


        public void setDraweeAccount(
                String draweeAccount) {

            this.draweeAccount =
                    draweeAccount;
        }


        public String getPayeeAccount() {

            return payeeAccount;
        }


        public void setPayeeAccount(
                String payeeAccount) {

            this.payeeAccount =
                    payeeAccount;
        }


        public String getPayeeName() {

            return payeeName;
        }


        public void setPayeeName(
                String payeeName) {

            this.payeeName =
                    payeeName;
        }


        public BigDecimal getAmount() {

            return amount;
        }


        public void setAmount(
                BigDecimal amount) {

            this.amount =
                    amount;
        }


        public String getChequeDate() {

            return chequeDate;
        }


        public void setChequeDate(
                String chequeDate) {

            this.chequeDate =
                    chequeDate;
        }


        public String getStatus() {

            return status;
        }


        public void setStatus(
                String status) {

            this.status =
                    status;
        }


        public String getUiStatus() {

            return uiStatus;
        }


        public void setUiStatus(
                String uiStatus) {

            this.uiStatus =
                    uiStatus;
        }
    }
}