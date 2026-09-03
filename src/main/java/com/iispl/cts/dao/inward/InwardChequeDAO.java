package com.iispl.cts.dao.inward;

import java.util.List;

import com.iispl.cts.entity.inward.InwardCheque;

public interface InwardChequeDAO {

    List<InwardCheque> getAllCheques();

    InwardCheque getChequeById(String inwardChequeId);

    List<InwardCheque> getChequesByBatchId(String inwardBatchId);

    boolean saveCheque(InwardCheque inwardCheque);

    boolean deleteCheque(String inwardChequeId);
 
    boolean updatecheque(InwardCheque cheque);
    
    
    
    
    
    
     // Fetch all inward cheques require MICR repair.
     
    List<InwardCheque> getMicrRepairRequiredCheques();
    
     // Fetch a inward cheque by its ID.
    
    InwardCheque findById(String inwardChequeId);
   
     // Update the MICR code and status after MICR repair.
   
  boolean updateMicrRepair(String inwardChequeId,String correctedMicrCode,String chequeStatus);
  
    List<InwardCheque> findByBatchAndStatus(String batchId, String status);
  
    boolean updateCheque(InwardCheque cheque);
    
    
    
}