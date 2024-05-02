package com.dongkuksystems.dbox.services.manager.stopword;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.stopword.StopWordDto;
//import com.dongkuksystems.dbox.models.dto.type.manager.rolemanagement.RoleManagementDto;
import com.dongkuksystems.dbox.models.type.manager.stopword.StopWord;

public interface StopWordService {
	
	List<StopWord> selectStopWord(String companyCode);
	
	List<StopWord> selectStopWordGroup(String companyCode);
	
	List<StopWord> selectBlindDept(String companyCode);
	
	
	
	


}
