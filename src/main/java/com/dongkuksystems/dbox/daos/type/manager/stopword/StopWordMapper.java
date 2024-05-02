package com.dongkuksystems.dbox.daos.type.manager.stopword;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.manager.stopword.StopWordDto;
import com.dongkuksystems.dbox.models.type.manager.stopword.StopWord; 


public interface StopWordMapper {
	
	public List<StopWord> selectStopWord(@Param("companyCode") String companyCode); 
	
	public List<StopWord> selectStopWordGroup(@Param("companyCode") String companyCode );

	public List<StopWord> selectBlindDept(@Param("companyCode") String companyCode );

	 

}


