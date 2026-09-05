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
import com.iispl.cts.service.outward.ScanService;
import com.ximpleware.AutoPilot;
import com.ximpleware.VTDGen;
import com.ximpleware.VTDNav;

public class BatchXmlParser {

    private final ScanService scanService;

    public BatchXmlParser(ScanService scanService) {
        this.scanService = scanService;
    }

    /**
     * Receives the uploaded ZIP path.
     *
     * Flow:
     *
     * Controller
     *      ↓
     * BatchXmlParser
     *      ↓
     * ScanBatch
     * List<ScanCheque>
     *      ↓
     * ScanService
     *      ↓
     * DB
     *
     * @param zipFilePath uploaded ZIP file path
     * @return scannedBatchId returned by service
     */
    public String parse(String zipFilePath) throws Exception {

        if (zipFilePath == null || zipFilePath.trim().isEmpty()) {
            throw new IllegalArgumentException(
                    "ZIP file path is empty");
        }

        File zipFile = new File(zipFilePath);

        if (!zipFile.exists()) {
            throw new IllegalArgumentException(
                    "ZIP file does not exist: " + zipFilePath);
        }

        if (!zipFile.isFile()) {
            throw new IllegalArgumentException(
                    "Invalid ZIP file: " + zipFilePath);
        }

        /*
         * Lists which will be sent to service.
         */
        ScanBatch scanBatch;

        List<ScanCheque> chequeList =
                new ArrayList<ScanCheque>();

        /*
         * =========================================================
         * OPEN ZIP
         * =========================================================
         */
        try (ZipFile zip = new ZipFile(zipFile)) {

            /*
             * Find XML inside ZIP.
             */
            ZipEntry xmlEntry = findXmlEntry(zip);

            if (xmlEntry == null) {
                throw new Exception(
                        "No XML file found inside ZIP");
            }

            /*
             * =====================================================
             * OPEN XML AS STREAM
             * =====================================================
             *
             * XML is read directly from ZIP.
             *
             * We do NOT extract XML to another file.
             */
            try (InputStream xmlInputStream =
                    zip.getInputStream(xmlEntry)) {

                /*
                 * =================================================
                 * VTD-XML
                 * =================================================
                 */
                VTDGen vtdGen = new VTDGen();

                /*
                 * Read XML bytes.
                 */
                byte[] xmlBytes =
                        readXmlBytes(xmlInputStream);

                /*
                 * Give XML document to VTDGen.
                 */
                vtdGen.setDoc(xmlBytes);

                /*
                 * Parse XML.
                 *
                 * true = namespace aware
                 *
                 * Your XML has:
                 *
                 * xmlns="urn:iso:std:iso:20022:tech:xsd:cts.cheque.clearing"
                 */
                vtdGen.parse(true);

                VTDNav vn = vtdGen.getNav();

                /*
                 * =================================================
                 * SET XML NAMESPACE
                 * =================================================
                 */
                AutoPilot namespacePilot =
                        new AutoPilot(vn);

                namespacePilot.declareXPathNameSpace(
                        "cts",
                        "urn:iso:std:iso:20022:tech:xsd:cts.cheque.clearing");

                /*
                 * =================================================
                 * VALIDATE ROOT
                 * =================================================
                 */
                vn.toElement(VTDNav.ROOT);

                String rootName =
                        vn.toString(vn.getCurrentIndex());

                if (!"ChequeBatchTransmission".equals(rootName)) {
                    throw new Exception(
                            "Invalid XML root element: "
                                    + rootName);
                }

                /*
                 * =================================================
                 * READ BATCH HEADER
                 * =================================================
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

                int actualChequeCount =
                        parseInteger(actualChequeCountText);

                BigDecimal actualTotalAmount =
                        parseBigDecimal(actualTotalAmountText);

                Timestamp uploadedAt =
                        parseTimestamp(uploadedAtText);

                /*
                 * =================================================
                 * CREATE ScanBatch
                 * =================================================
                 */
                scanBatch = new ScanBatch();

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
                 * =================================================
                 * READ ALL CHEQUES
                 * =================================================
                 */

                vn.toElement(VTDNav.ROOT);

                AutoPilot chequePilot =
                        new AutoPilot(vn);

                chequePilot.declareXPathNameSpace(
                        "cts",
                        "urn:iso:std:iso:20022:tech:xsd:cts.cheque.clearing");

                chequePilot.selectXPath(
                        "/cts:ChequeBatchTransmission/cts:Cheques/cts:ChequeItem");

                int chequeCount = 0;

                while (chequePilot.evalXPath() != -1) {

                    chequeCount++;

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

                    chequeList.add(cheque);
                }

                /*
                 * =================================================
                 * VALIDATE CHEQUE COUNT
                 * =================================================
                 */

                if (chequeList.isEmpty()) {
                    throw new Exception(
                            "No ChequeItem found in XML");
                }

                if (actualChequeCount != chequeList.size()) {
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
         * =========================================================
         * SEND TO SERVICE
         * =========================================================
         *
         * Service handles the transaction.
         *
         * The service should:
         *
         * 1. Open one Connection
         * 2. setAutoCommit(false)
         * 3. Save ScanBatch
         * 4. Save ScanCheque records
         * 5. Commit
         *
         * If anything fails:
         *
         * 6. Rollback everything
         */
        String resultScannedBatchId =
                scanService.saveScanBatch(
                        scanBatch,
                        chequeList);

        /*
         * Return ID to controller.
         */
        return resultScannedBatchId;
    }

    /**
     * =============================================================
     * PARSE ONE CHEQUE
     * =============================================================
     */
    private ScanCheque parseCheque(
            VTDNav vn,
            AutoPilot namespacePilot)
            throws Exception {

        ScanCheque cheque =
                new ScanCheque();

        /*
         * =========================================================
         * SCANNED CHEQUE ID
         * =========================================================
         */
        String scannedChequeId =
                getValue(
                        vn,
                        namespacePilot,
                        "./cts:ScannedChequeId");

        cheque.setScannedChequeId(
                scannedChequeId);

        /*
         * =========================================================
         * CHEQUE NUMBER
         * =========================================================
         */
        String chequeNumber =
                getValue(
                        vn,
                        namespacePilot,
                        "./cts:ChequeNumber");

        cheque.setChequeNumber(
                chequeNumber);

        /*
         * =========================================================
         * CHEQUE DATE
         * =========================================================
         */
        String chequeDateText =
                getValue(
                        vn,
                        namespacePilot,
                        "./cts:ChequeDate");

        cheque.setChequeDate(
                parseDate(chequeDateText));

        /*
         * =========================================================
         * CHEQUE AMOUNT
         * =========================================================
         */
        String chequeAmountText =
                getValue(
                        vn,
                        namespacePilot,
                        "./cts:Amount");

        cheque.setChequeAmount(
                parseBigDecimal(chequeAmountText));

        /*
         * =========================================================
         * CHEQUE STATUS
         * =========================================================
         */
        String chequeStatus =
                getValue(
                        vn,
                        namespacePilot,
                        "./cts:ChequeStatus");

        cheque.setChequeStatus(
                chequeStatus);

        /*
         * =========================================================
         * MICR DETAILS
         * =========================================================
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
         * =========================================================
         * DRAWEE
         * =========================================================
         *
         * Drawee/AccountHolderName → draweeName
         *
         * Drawee/AccountNumber → draweeAccountNumber
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
         * =========================================================
         * PAYEE
         * =========================================================
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
         * =========================================================
         * FRONT IMAGE
         * =========================================================
         *
         * XML:
         *
         * <FrontImage
         *      path="Batch1002-images/cheque004_front.png"
         *      type="FRONT"/>
         *
         * We need the path attribute.
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
         * =========================================================
         * BACK IMAGE
         * =========================================================
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
         * =========================================================
         * ACCOUNT ID
         * =========================================================
         *
         * accountId is not present in XML.
         *
         * Leave it null.
         */
        cheque.setAccountId(null);

        /*
         * =========================================================
         * CREATED AT
         * =========================================================
         *
         * createdAt is not present in XML.
         *
         * We can leave it null if DB handles it.
         */
        cheque.setCreatedAt(null);

        return cheque;
    }

    /**
     * =============================================================
     * GET XML ELEMENT VALUE
     * =============================================================
     */
    private String getValue(
            VTDNav vn,
            AutoPilot namespacePilot,
            String xpath)
            throws Exception {

        vn.push();

        try {

            namespacePilot.bind(vn);

            namespacePilot.selectXPath(xpath);

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

            return vn.toString(textIndex).trim();

        } finally {

            vn.pop();
        }
    }

    /**
     * =============================================================
     * GET XML ATTRIBUTE VALUE
     * =============================================================
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

            namespacePilot.selectXPath(xpath);

            int index =
                    namespacePilot.evalXPath();

            if (index == -1) {
                return null;
            }

            int attrIndex =
                    vn.getAttrVal(attributeName);

            if (attrIndex == -1) {
                return null;
            }

            return vn.toString(attrIndex).trim();

        } finally {

            vn.pop();
        }
    }

    /**
     * =============================================================
     * FIND XML FILE INSIDE ZIP
     * =============================================================
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
                    entry.getName().toLowerCase();

            if (name.endsWith(".xml")) {
                return entry;
            }
        }

        return null;
    }

    /**
     * =============================================================
     * PARSE INTEGER
     * =============================================================
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
     * =============================================================
     * PARSE BIG DECIMAL
     * =============================================================
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
     * =============================================================
     * PARSE DATE
     *
     * Current XML format:
     *
     * 2026-06-30
     * =============================================================
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
     * =============================================================
     * PARSE TIMESTAMP
     *
     * Example:
     *
     * 2026-08-31T15:14:01
     * =============================================================
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
                         .replace("T", " "));

        } catch (IllegalArgumentException e) {

            return null;
        }
    }

    /**
     * =============================================================
     * READ XML BYTES
     * =============================================================
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
                inputStream.read(buffer)) != -1) {

            outputStream.write(
                    buffer,
                    0,
                    length);
        }

        return outputStream.toByteArray();
    }
}