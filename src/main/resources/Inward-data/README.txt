INWARD TEST DATA - 2026-09-04

Each batch contains:
  NPCI_Inward.xml  = canonical inbound/NPCI data
  OCR_Mock.xml     = mock OCR values read from the corresponding cheque images
  images/          = normalized front/back images

NPCI_Inward.xml uses the project's current InwardBatchXmlParser structure:
ChequeBatchTransmission -> BatchHeader -> Cheques -> ChequeItem.

OCR_Mock.xml is intentionally a separate OCR payload. Its values include OCR defects
to drive the comparison/exception logic. Data Entry remains mandatory for every cheque.

IMPORTANT:
The uploaded images are the test source. Where an image was ambiguous, the NPCI
canonical value was chosen from the supplied batch XML/master-style data or the
clearest visible value. Review the README/field mapping before using as production data.
