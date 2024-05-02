package com.dongkuksystems.dbox.services.unlockuser;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.unlock.UserUnLockDto;



public interface UnLockUserService {


	String postUnlockUser(UserSession userSession, UserUnLockDto userUnLockDto) throws Exception;
	
	
}
