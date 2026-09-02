package com.iispl.cts.parser;

import java.io.FileInputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.iispl.cts.entity.outward.ScanBatch;
import com.iispl.cts.entity.outward.ScanCheque;
import com.iispl.cts.entity.outward.ScanChequeImage;
import com.iispl.cts.service.outward.ScanService;

public class BatchXmlParser {

    private final ScanService scanService;

    public BatchXmlParser(ScanService scanService) {
        this.scanService = scanService;
    }

    /**
     * Receives the ZIP file path.
     *
     * Controller
     *      ↓
     * BatchXmlParser
     *      ↓
     * ScanService
     *      ↓
     * DAO
     *      ↓
     * DB
     *
     * @param zipFilePath path of uploaded ZIP file
     * @return scannedBatchId
     * @throws Exception if parsing fails
     */
    public String parse(String zipFilePath) throws Exception {

        if (zipFilePath == null
                || zipFilePath.trim().isEmpty()) {

            throw new IllegalArgumentException(
                    "ZIP file path cannot be null or empty");
        }

        /*
         * =========================================================
         * 1. Create DOM parser
         * =========================================================
         */

        DocumentBuilderFactory factory =
                DocumentBuilderFactory.newInstance();

        /*
         * XML contains namespace:
         *
         * urn:iso:std:iso:20022:tech:xsd:cts.cheque.clearing
         */
        factory.setNamespaceAware(true);

        /*
         * Secure XML parser configuration.
         */
        try {

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

        } catch (Exception e) {

            /*
             * Parser implementation may not support
             * all features.
             */
        }

        DocumentBuilder builder =
                factory.newDocumentBuilder();

        /*
         * =========================================================
         * 2. Open ZIP file
         * =========================================================
         */

        boolean xmlFound = false;

        String batchId = null;

        try (
                InputStream fileInputStream =
                        new FileInputStream(zipFilePath);

                ZipInputStream zipInputStream =
                        new ZipInputStream(
                                fileInputStream)) {

            ZipEntry entry;

            while ((entry =
                    zipInputStream.getNextEntry()) != null) {

                /*
                 * Ignore directories.
                 */
                if (entry.isDirectory()) {

                    zipInputStream.closeEntry();

                    continue;
                }

                String entryName =
                        entry.getName();

                /*
                 * =================================================
                 * 3. Find XML file
                 * =================================================
                 */

                if (entryName != null
                        && entryName
                                .toLowerCase()
                                .endsWith(".xml")) {

                    if (xmlFound) {

                        throw new IllegalArgumentException(
                                "Batch ZIP must contain only one XML file");
                    }

                    xmlFound = true;

                    /*
                     * =================================================
                     * 4. Parse XML directly from ZIP entry
                     * =================================================
                     */

                    Document document =
                            builder.parse(
                                    zipInputStream);

                    document.getDocumentElement()
                            .normalize();

                    Element root =
                            document.getDocumentElement();

                    /*
                     * =================================================
                     * 5. Validate root
                     * =================================================
                     */

                    if (!"ChequeBatchTransmission"
                            .equals(
                                    root.getLocalName())) {

                        throw new IllegalArgumentException(
                                "Invalid root element: "
                                        + root.getNodeName());
                    }

                    /*
                     * =================================================
                     * 6. Read BatchHeader
                     * =================================================
                     */

                    Element batchHeader =
                            getFirstElement(
                                    root,
                                    "BatchHeader");

                    if (batchHeader == null) {

                        throw new IllegalArgumentException(
                                "BatchHeader is missing");
                    }

                    String scannedBatchId =
                            getElementValue(
                                    batchHeader,
                                    "ScannedBatchId");

                    String batchReferenceId =
                            getElementValue(
                                    batchHeader,
                                    "BatchReferenceId");

                    String actualChequeCountValue =
                            getElementValue(
                                    batchHeader,
                                    "ActualChequeCount");

                    String actualTotalAmountValue =
                            getElementValue(
                                    batchHeader,
                                    "ActualTotalAmount");

                    String stagingStatus =
                            getElementValue(
                                    batchHeader,
                                    "StagingStatus");

                    String batchStatus =
                            getElementValue(
                                    batchHeader,
                                    "BatchStatus");

                    String uploadedBy =
                            getElementValue(
                                    batchHeader,
                                    "UploadedBy");

                    String uploadedAtValue =
                            getElementValue(
                                    batchHeader,
                                    "UploadedAt");

                    /*
                     * =================================================
                     * 7. Convert BatchHeader values
                     * =================================================
                     */

                    int actualChequeCount = 0;

                    if (actualChequeCountValue != null) {

                        actualChequeCount =
                                Integer.parseInt(
                                        actualChequeCountValue);
                    }

                    BigDecimal actualTotalAmount = null;

                    if (actualTotalAmountValue != null) {

                        actualTotalAmount =
                                new BigDecimal(
                                        actualTotalAmountValue);
                    }

                    Timestamp uploadedAt = null;

                    if (uploadedAtValue != null) {

                        uploadedAt =
                                Timestamp.valueOf(
                                        LocalDateTime.parse(
                                                uploadedAtValue));
                    }

                    /*
                     * =================================================
                     * 8. Create ScanBatch
                     * =================================================
                     */

                    ScanBatch scanBatch =
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
                     * =================================================
                     * 9. Find ChequeItem NodeList
                     * =================================================
                     */

                    NodeList chequeNodes =
                            root.getElementsByTagNameNS(
                                    "*",
                                    "ChequeItem");

                    if (chequeNodes == null
                            || chequeNodes.getLength() == 0) {

                        throw new IllegalArgumentException(
                                "No ChequeItem found in XML");
                    }

                    /*
                     * =================================================
                     * 10. Create lists
                     * =================================================
                     */

                    List<ScanCheque> chequeList =
                            new ArrayList<>();

                    /*
                     * Image list is kept because the current
                     * ScanService method expects it.
                     *
                     * Image database processing is NOT being
                     * implemented now.
                     */
                    List<ScanChequeImage> imageList =
                            new ArrayList<>();

                    Timestamp createdAt =
                            new Timestamp(
                                    System.currentTimeMillis());

                    /*
                     * =================================================
                     * 11. Parse every ChequeItem
                     * =================================================
                     */

                    for (int i = 0;
                            i < chequeNodes.getLength();
                            i++) {

                        Node node =
                                chequeNodes.item(i);

                        if (node.getNodeType()
                                != Node.ELEMENT_NODE) {

                            continue;
                        }

                        Element chequeElement =
                                (Element) node;

                        ScanCheque scanCheque =
                                new ScanCheque();

                        /*
                         * -------------------------------------------------
                         * Basic cheque details
                         * -------------------------------------------------
                         */

                        String chequeId =
                                getElementValue(
                                        chequeElement,
                                        "ScannedChequeId");

                        scanCheque.setScannedChequeId(
                                chequeId);

                        scanCheque.setScannedBatchId(
                                scannedBatchId);

                        scanCheque.setChequeNumber(
                                getElementValue(
                                        chequeElement,
                                        "ChequeNumber"));

                        /*
                         * -------------------------------------------------
                         * Cheque Date
                         * -------------------------------------------------
                         */

                        String chequeDateValue =
                                getElementValue(
                                        chequeElement,
                                        "ChequeDate");

                        if (chequeDateValue != null) {

                            scanCheque.setChequeDate(
                                    Date.valueOf(
                                            chequeDateValue));
                        }

                        /*
                         * -------------------------------------------------
                         * Amount
                         * -------------------------------------------------
                         */

                        String amountValue =
                                getElementValue(
                                        chequeElement,
                                        "Amount");

                        if (amountValue != null) {

                            scanCheque.setChequeAmount(
                                    new BigDecimal(
                                            amountValue));
                        }

                        /*
                         * -------------------------------------------------
                         * Cheque Status
                         * -------------------------------------------------
                         */

                        scanCheque.setChequeStatus(
                                getElementValue(
                                        chequeElement,
                                        "ChequeStatus"));

                        /*
                         * -------------------------------------------------
                         * Created At
                         * -------------------------------------------------
                         */

                        scanCheque.setCreatedAt(
                                createdAt);

                        /*
                         * =================================================
                         * MICR Details
                         * =================================================
                         */

                        Element micrElement =
                                getFirstElement(
                                        chequeElement,
                                        "MICRDetails");

                        if (micrElement != null) {

                            /*
                             * FullMICR → micrCode
                             */
                            scanCheque.setMicrCode(
                                    getElementValue(
                                            micrElement,
                                            "FullMICR"));

                            /*
                             * CityCode → cityCode
                             */
                            scanCheque.setCityCode(
                                    getElementValue(
                                            micrElement,
                                            "CityCode"));

                            /*
                             * BankCode → bankCode
                             */
                            scanCheque.setBankCode(
                                    getElementValue(
                                            micrElement,
                                            "BankCode"));

                            /*
                             * BranchCode → branchCode
                             */
                            scanCheque.setBranchCode(
                                    getElementValue(
                                            micrElement,
                                            "BranchCode"));
                        }

                        /*
                         * =================================================
                         * Drawee
                         * =================================================
                         */

                        Element draweeElement =
                                getFirstElement(
                                        chequeElement,
                                        "Drawee");

                        if (draweeElement != null) {

                            scanCheque.setDraweeName(
                                    getElementValue(
                                            draweeElement,
                                            "AccountHolderName"));

                            scanCheque.setDraweeAccountNumber(
                                    getElementValue(
                                            draweeElement,
                                            "AccountNumber"));
                        }

                        /*
                         * =================================================
                         * Payee
                         * =================================================
                         */

                        Element payeeElement =
                                getFirstElement(
                                        chequeElement,
                                        "Payee");

                        if (payeeElement != null) {

                            scanCheque.setPayeeName(
                                    getElementValue(
                                            payeeElement,
                                            "AccountHolderName"));

                            scanCheque.setPayeeAccountNumber(
                                    getElementValue(
                                            payeeElement,
                                            "AccountNumber"));
                        }

                        /*
                         * =================================================
                         * Account ID
                         * =================================================
                         *
                         * XML does not contain accountId.
                         * Therefore it remains null.
                         */

                        /*
                         * Add cheque to list.
                         */
                        chequeList.add(
                                scanCheque);

                        /*
                         * =================================================
                         * Images
                         * =================================================
                         *
                         * We are intentionally NOT processing images
                         * for database storage now.
                         *
                         * The empty imageList is passed to ScanService
                         * because its current method signature requires it.
                         */
                    }

                    /*
                     * =================================================
                     * 12. Send to ScanService
                     * =================================================
                     *
                     * Parser
                     *     ↓
                     * ScanService
                     *     ↓
                     * ScanBatchDAO
                     *     ↓
                     * ScanChequeDAO
                     *     ↓
                     * Database
                     */

                    batchId =
                            scanService.saveScanBatch(
                                    scanBatch,
                                    chequeList,
                                    imageList);

                    /*
                     * We have received the batch ID.
                     */
                    break;
                }

                /*
                 * Do NOT close the ZIP stream here manually
                 * after parsing the XML. The try-with-resources
                 * block owns the stream.
                 */
            }
        }

        /*
         * =========================================================
         * 13. XML must exist
         * =========================================================
         */

        if (!xmlFound) {

            throw new IllegalArgumentException(
                    "No XML file found inside the ZIP");
        }

        /*
         * =========================================================
         * 14. Return Batch ID to Controller
         * =========================================================
         */

        return batchId;
    }

    /**
     * Returns the first matching element.
     */
    private Element getFirstElement(
            Element parent,
            String localName) {

        NodeList nodes =
                parent.getElementsByTagNameNS(
                        "*",
                        localName);

        if (nodes != null
                && nodes.getLength() > 0) {

            return (Element) nodes.item(0);
        }

        return null;
    }

    /**
     * Returns text value of the first matching element.
     */
    private String getElementValue(
            Element parent,
            String localName) {

        Element element =
                getFirstElement(
                        parent,
                        localName);

        if (element == null) {
            return null;
        }

        String value =
                element.getTextContent();

        if (value == null) {
            return null;
        }

        value = value.trim();

        if (value.isEmpty()) {
            return null;
        }

        return value;
    }
}