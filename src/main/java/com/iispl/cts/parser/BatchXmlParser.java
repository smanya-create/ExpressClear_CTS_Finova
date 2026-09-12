package com.iispl.cts.parser;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;
import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

public class BatchXmlParser {

    /*
     * =========================================================
     * PARSED BATCH DATA
     * =========================================================
     *
     * Parser returns TWO things:
     *
     * 1. ScanBatch
     * 2. List<ScanCheque>
     *
     * No database operation is performed here.
     */
    public static class ParsedBatchData {

        private final ScanBatch scanBatch;
        private final List<ScanCheque> chequeList;

        public ParsedBatchData(
                ScanBatch scanBatch,
                List<ScanCheque> chequeList) {

            this.scanBatch = scanBatch;
            this.chequeList = chequeList;
        }

        public ScanBatch getScanBatch() {
            return scanBatch;
        }

        public List<ScanCheque> getChequeList() {
            return chequeList;
        }
    }

    /*
     * =========================================================
     * PARSE
     * =========================================================
     *
     * Controller sends:
     *
     * 1. expectedTotalCheques
     * 2. expectedTotalAmount
     * 3. uploadedZipPath
     *
     * Parser returns:
     *
     * 1. ScanBatch
     * 2. List<ScanCheque>
     *
     * IMPORTANT:
     * No DB save happens here.
     */
    public ParsedBatchData parse(
            Integer expectedTotalCheques,
            BigDecimal expectedTotalAmount,
            String zipFilePath)
            throws Exception {

        /*
         * =====================================================
         * VALIDATE INPUT
         * =====================================================
         */

        if (expectedTotalCheques == null) {
            throw new IllegalArgumentException(
                    "Expected cheque count is required.");
        }

        if (expectedTotalAmount == null) {
            throw new IllegalArgumentException(
                    "Expected total amount is required.");
        }

        if (zipFilePath == null
                || zipFilePath.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "ZIP file path is empty.");
        }

        File zipFile =
                new File(zipFilePath);

        if (!zipFile.exists()) {
            throw new IllegalArgumentException(
                    "ZIP file does not exist: "
                            + zipFilePath);
        }

        if (!zipFile.isFile()) {
            throw new IllegalArgumentException(
                    "Invalid ZIP file: "
                            + zipFilePath);
        }

        /*
         * =====================================================
         * RESULT OBJECTS
         * =====================================================
         */

        ScanBatch scanBatch = null;

        List<ScanCheque> chequeList =
                new ArrayList<ScanCheque>();

        /*
         * =====================================================
         * OPEN ZIP
         * =====================================================
         */

        try (ZipFile zip =
                new ZipFile(zipFile)) {

            /*
             * =================================================
             * FIND XML
             * =================================================
             */

            ZipEntry xmlEntry =
                    findXmlEntry(zip);

            if (xmlEntry == null) {

                throw new Exception(
                        "No XML file found inside ZIP.");
            }

            /*
             * =================================================
             * OPEN XML
             * =================================================
             */

            try (InputStream xmlInputStream =
                    zip.getInputStream(xmlEntry)) {

                /*
                 * =============================================
                 * READ XML
                 * =============================================
                 */

                byte[] xmlBytes =
                        readXmlBytes(
                                xmlInputStream);

                /*
                 * =============================================
                 * VTD XML
                 * =============================================
                 */

                VTDGen vtdGen =
                        new VTDGen();

                vtdGen.setDoc(xmlBytes);

                /*
                 * true = namespace aware
                 */
                vtdGen.parse(true);

                VTDNav vn =
                        vtdGen.getNav();

                /*
                 * =============================================
                 * XML NAMESPACE
                 * =============================================
                 */

                AutoPilot namespacePilot =
                        new AutoPilot(vn);

                namespacePilot.declareXPathNameSpace(
                        "cts",
                        "urn:iso:std:iso:20022:tech:xsd:cts.cheque.clearing");

                /*
                 * =============================================
                 * VALIDATE ROOT
                 * =============================================
                 */

                vn.toElement(
                        VTDNav.ROOT);

                String rootName =
                        vn.toString(
                                vn.getCurrentIndex());

                if (!"ChequeBatchTransmission"
                        .equals(rootName)) {

                    throw new Exception(
                            "Invalid XML root element: "
                                    + rootName);
                }

                /*
                 * =============================================
                 * READ BATCH HEADER
                 * =============================================
                 */

                String scannedBatchId =
                        getValue(
                                vn,
                                namespacePilot,
                                "/cts:ChequeBatchTransmission/cts:BatchHeader/cts:ScannedBatchId");

                String batchReferenceId =
                        getValue(
                                vn,
                                namespacePilot,
                                "/cts:ChequeBatchTransmission/cts:BatchHeader/cts:BatchReferenceId");

                String actualChequeCountText =
                        getValue(
                                vn,
                                namespacePilot,
                                "/cts:ChequeBatchTransmission/cts:BatchHeader/cts:ActualChequeCount");

                String actualTotalAmountText =
                        getValue(
                                vn,
                                namespacePilot,
                                "/cts:ChequeBatchTransmission/cts:BatchHeader/cts:ActualTotalAmount");

                String stagingStatus =
                        getValue(
                                vn,
                                namespacePilot,
                                "/cts:ChequeBatchTransmission/cts:BatchHeader/cts:StagingStatus");

                String batchStatus =
                        getValue(
                                vn,
                                namespacePilot,
                                "/cts:ChequeBatchTransmission/cts:BatchHeader/cts:BatchStatus");

                String uploadedBy =
                        getValue(
                                vn,
                                namespacePilot,
                                "/cts:ChequeBatchTransmission/cts:BatchHeader/cts:UploadedBy");

                String uploadedAtText =
                        getValue(
                                vn,
                                namespacePilot,
                                "/cts:ChequeBatchTransmission/cts:BatchHeader/cts:UploadedAt");

                /*
                 * =============================================
                 * CONVERT BATCH HEADER VALUES
                 * =============================================
                 */

                int actualChequeCount =
                        parseInteger(
                                actualChequeCountText);

                BigDecimal actualTotalAmount =
                        parseBigDecimal(
                                actualTotalAmountText);

                Timestamp uploadedAt =
                        parseTimestamp(
                                uploadedAtText);

                /*
                 * =============================================
                 * CREATE SCAN BATCH
                 * =============================================
                 */

                scanBatch =
                        new ScanBatch();

                scanBatch.setScannedBatchId(
                        scannedBatchId);

                scanBatch.setBatchReferenceId(
                        batchReferenceId);

                scanBatch.setActualChequeCount(
                        actualChequeCount);

                scanBatch.setActualTotalAmount(
                        actualTotalAmount);

                scanBatch.setStagingStatus(
                        stagingStatus);

                scanBatch.setBatchStatus(
                        batchStatus);

                scanBatch.setUploadedBy(
                        uploadedBy);

                scanBatch.setUploadedAt(
                        uploadedAt);

                /*
                 * =============================================
                 * READ ALL CHEQUES
                 * =============================================
                 */

                vn.toElement(
                        VTDNav.ROOT);

                AutoPilot chequePilot =
                        new AutoPilot(vn);

                chequePilot.declareXPathNameSpace(
                        "cts",
                        "urn:iso:std:iso:20022:tech:xsd:cts.cheque.clearing");

                chequePilot.selectXPath(
                        "/cts:ChequeBatchTransmission/cts:Cheques/cts:ChequeItem");

                /*
                 * =============================================
                 * PARSE EACH CHEQUE
                 * =============================================
                 */

                while (chequePilot.evalXPath()
                        != -1) {

                    /*
                     * Current VTDNav position:
                     *
                     * <ChequeItem>
                     */

                    ScanCheque cheque =
                            parseCheque(
                                    vn,
                                    namespacePilot);

                    /*
                     * Set batch ID from BatchHeader.
                     */

                    cheque.setScannedBatchId(
                            scannedBatchId);

                    chequeList.add(
                            cheque);
                }

                /*
                 * =============================================
                 * CHEQUE LIST MUST NOT BE EMPTY
                 * =============================================
                 */

                if (chequeList.isEmpty()) {

                    throw new Exception(
                            "No ChequeItem found in XML.");
                }

                /*
                 * =============================================
                 * PARSER-LEVEL COUNT CHECK
                 * =============================================
                 *
                 * This only verifies XML consistency:
                 *
                 * BatchHeader ActualChequeCount
                 *              =
                 * Number of ChequeItem nodes
                 *
                 * Expected count comparison will be done
                 * later by the validation layer.
                 */

                if (actualChequeCount
                        != chequeList.size()) {

                    throw new Exception(
                            "Cheque count mismatch. "
                                    + "XML ActualChequeCount = "
                                    + actualChequeCount
                                    + ", actual cheque count = "
                                    + chequeList.size());
                }
            }
        }

        /*
         * =====================================================
         * IMPORTANT
         * =====================================================
         *
         * NO scanService.saveScanBatch()
         * NO INSERT
         * NO UPDATE
         * NO DATABASE OPERATION
         *
         * Parser only returns parsed objects.
         */

        return new ParsedBatchData(
                scanBatch,
                chequeList);
    }

    /**
     * =========================================================
     * PARSE ONE CHEQUE
     * =========================================================
     */
    private ScanCheque parseCheque(
            VTDNav vn,
            AutoPilot namespacePilot)
            throws Exception {

        ScanCheque cheque =
                new ScanCheque();

        /*
         * =====================================================
         * SCANNED CHEQUE ID
         * =====================================================
         */

        String scannedChequeId =
                getValue(
                        vn,
                        namespacePilot,
                        "./cts:ScannedChequeId");

        cheque.setScannedChequeId(
                scannedChequeId);

        /*
         * =====================================================
         * CHEQUE NUMBER
         * =====================================================
         */

        String chequeNumber =
                getValue(
                        vn,
                        namespacePilot,
                        "./cts:ChequeNumber");

        cheque.setChequeNumber(
                chequeNumber);

        /*
         * =====================================================
         * CHEQUE DATE
         * =====================================================
         */

        String chequeDateText =
                getValue(
                        vn,
                        namespacePilot,
                        "./cts:ChequeDate");

        cheque.setChequeDate(
                parseDate(
                        chequeDateText));

        /*
         * =====================================================
         * CHEQUE AMOUNT
         * =====================================================
         */

        String chequeAmountText =
                getValue(
                        vn,
                        namespacePilot,
                        "./cts:Amount");

        cheque.setChequeAmount(
                parseBigDecimal(
                        chequeAmountText));

        /*
         * =====================================================
         * CHEQUE STATUS
         * =====================================================
         */

        String chequeStatus =
                getValue(
                        vn,
                        namespacePilot,
                        "./cts:ChequeStatus");

        cheque.setChequeStatus(
                chequeStatus);

        /*
         * =====================================================
         * MICR DETAILS
         * =====================================================
         */

        String fullMicr =
                getValue(
                        vn,
                        namespacePilot,
                        "./cts:MICRDetails/cts:FullMICR");

        String cityCode =
                getValue(
                        vn,
                        namespacePilot,
                        "./cts:MICRDetails/cts:CityCode");

        String bankCode =
                getValue(
                        vn,
                        namespacePilot,
                        "./cts:MICRDetails/cts:BankCode");

        String branchCode =
                getValue(
                        vn,
                        namespacePilot,
                        "./cts:MICRDetails/cts:BranchCode");

        cheque.setMicrCode(
                fullMicr);

        cheque.setCityCode(
                cityCode);

        cheque.setBankCode(
                bankCode);

        cheque.setBranchCode(
                branchCode);

        /*
         * =====================================================
         * DRAWEE
         * =====================================================
         *
         * Drawee/AccountHolderName
         *          -> draweeName
         *
         * Drawee/AccountNumber
         *          -> draweeAccountNumber
         */

        String draweeName =
                getValue(
                        vn,
                        namespacePilot,
                        "./cts:Drawee/cts:AccountHolderName");

        String draweeAccountNumber =
                getValue(
                        vn,
                        namespacePilot,
                        "./cts:Drawee/cts:AccountNumber");

        cheque.setDraweeName(
                draweeName);

        cheque.setDraweeAccountNumber(
                draweeAccountNumber);

        /*
         * =====================================================
         * PAYEE
         * =====================================================
         */

        String payeeName =
                getValue(
                        vn,
                        namespacePilot,
                        "./cts:Payee/cts:AccountHolderName");

        String payeeAccountNumber =
                getValue(
                        vn,
                        namespacePilot,
                        "./cts:Payee/cts:AccountNumber");

        cheque.setPayeeName(
                payeeName);

        cheque.setPayeeAccountNumber(
                payeeAccountNumber);

        /*
         * =====================================================
         * FRONT IMAGE
         * =====================================================
         *
         * XML:
         *
         * <FrontImage
         *     path="Batch1002-images/cheque004_front.png"
         *     type="FRONT"/>
         */

        String frontImagePath =
                getAttributeValue(
                        vn,
                        namespacePilot,
                        "./cts:ChequeImages/cts:FrontImage",
                        "path");

        cheque.setChequeImageFront(
                frontImagePath);

        /*
         * =====================================================
         * BACK IMAGE
         * =====================================================
         */

        String backImagePath =
                getAttributeValue(
                        vn,
                        namespacePilot,
                        "./cts:ChequeImages/cts:BackImage",
                        "path");

        cheque.setChequeImageBack(
                backImagePath);

        /*
         * =====================================================
         * ACCOUNT ID
         * =====================================================
         *
         * Account ID is not present in XML.
         */

        cheque.setAccountId(
                null);

        /*
         * =====================================================
         * CREATED AT
         * =====================================================
         *
         * CreatedAt is not present in XML.
         */

        cheque.setCreatedAt(
                null);

        return cheque;
    }

    /**
     * =========================================================
     * GET XML ELEMENT VALUE
     * =========================================================
     */
    private String getValue(
            VTDNav vn,
            AutoPilot namespacePilot,
            String xpath)
            throws Exception {

        vn.push();

        try {

            namespacePilot.bind(vn);

            namespacePilot.selectXPath(
                    xpath);

            int index =
                    namespacePilot.evalXPath();

            if (index == -1) {
                return null;
            }

            int textIndex =
                    vn.getText();

            if (textIndex == -1) {
                return null;
            }

            return vn.toString(
                    textIndex).trim();

        } finally {

            vn.pop();
        }
    }

    /**
     * =========================================================
     * GET XML ATTRIBUTE VALUE
     * =========================================================
     */
    private String getAttributeValue(
            VTDNav vn,
            AutoPilot namespacePilot,
            String xpath,
            String attributeName)
            throws Exception {

        vn.push();

        try {

            namespacePilot.bind(vn);

            namespacePilot.selectXPath(
                    xpath);

            int index =
                    namespacePilot.evalXPath();

            if (index == -1) {
                return null;
            }

            int attrIndex =
                    vn.getAttrVal(
                            attributeName);

            if (attrIndex == -1) {
                return null;
            }

            return vn.toString(
                    attrIndex).trim();

        } finally {

            vn.pop();
        }
    }

    /**
     * =========================================================
     * FIND XML FILE INSIDE ZIP
     * =========================================================
     */
    private ZipEntry findXmlEntry(
            ZipFile zip) {

        Enumeration<? extends ZipEntry> entries =
                zip.entries();

        while (entries.hasMoreElements()) {

            ZipEntry entry =
                    entries.nextElement();

            if (entry.isDirectory()) {
                continue;
            }

            String name =
                    entry.getName()
                            .toLowerCase();

            if (name.endsWith(".xml")) {
                return entry;
            }
        }

        return null;
    }

    /**
     * =========================================================
     * PARSE INTEGER
     * =========================================================
     */
    private int parseInteger(
            String value) {

        if (value == null
                || value.trim().isEmpty()) {

            return 0;
        }

        try {

            return Integer.parseInt(
                    value.trim());

        } catch (NumberFormatException e) {

            return 0;
        }
    }

    /**
     * =========================================================
     * PARSE BIG DECIMAL
     * =========================================================
     */
    private BigDecimal parseBigDecimal(
            String value) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        try {

            return new BigDecimal(
                    value.trim());

        } catch (NumberFormatException e) {

            return null;
        }
    }

    /**
     * =========================================================
     * PARSE DATE
     * =========================================================
     *
     * XML example:
     *
     * 2026-06-30
     */
    private Date parseDate(
            String value) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        try {

            return Date.valueOf(
                    value.trim());

        } catch (IllegalArgumentException e) {

            return null;
        }
    }

    /**
     * =========================================================
     * PARSE TIMESTAMP
     * =========================================================
     *
     * XML example:
     *
     * 2026-08-31T15:14:01
     */
    private Timestamp parseTimestamp(
            String value) {

        if (value == null
                || value.trim().isEmpty()) {

            return null;
        }

        try {

            return Timestamp.valueOf(
                    value.trim()
                            .replace(
                                    "T",
                                    " "));

        } catch (IllegalArgumentException e) {

            return null;
        }
    }

    /**
     * =========================================================
     * READ XML BYTES
     * =========================================================
     */
    private byte[] readXmlBytes(
            InputStream inputStream)
            throws Exception {

        ByteArrayOutputStream outputStream =
                new ByteArrayOutputStream();

        byte[] buffer =
                new byte[4096];

        int length;

        while ((length =
                inputStream.read(buffer))
                != -1) {

            outputStream.write(
                    buffer,
                    0,
                    length);
        }

        return outputStream.toByteArray();
    }
}
