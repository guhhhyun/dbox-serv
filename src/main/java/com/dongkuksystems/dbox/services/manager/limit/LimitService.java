package com.dongkuksystems.dbox.services.manager.limit;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.type.manager.limit.Limit;

public interface LimitService {
	
	List<Limit> selectLimitValue(String uComCode); 
	
	String patchLimitValue(UserSession userSession, String rObjectId, String uCodeVal) throws Exception;

}
