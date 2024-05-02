package com.dongkuksystems.dbox.daos.type.manager.stopword;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.manager.stopword.StopWordDto;
import com.dongkuksystems.dbox.models.type.manager.stopword.StopWord;

public interface StopWordDao {
	public List<StopWord> selectStopWord(String companyCode);
	public List<StopWord> selectStopWordGroup(String companyCode);
	public List<StopWord> selectBlindDept(String companyCode);
	
	 


}
