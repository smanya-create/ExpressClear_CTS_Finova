package com.iispl.cts.dao.outward;

import java.util.List;

import com.iispl.cts.entity.outward.OutwardChequeImage;

public interface OutwardChequeImageDAO{
	public List<OutwardChequeImage> getImagesByChequeId(String outwardChequeId);
	
	
}
