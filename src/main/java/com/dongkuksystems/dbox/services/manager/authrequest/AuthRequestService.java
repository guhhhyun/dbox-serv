package com.dongkuksystems.dbox.services.manager.authrequest;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.authrequest.AuthRequestCollectDto;
import com.dongkuksystems.dbox.models.dto.type.manager.authrequest.AuthRequestPatchDto;
import com.dongkuksystems.dbox.models.dto.type.manager.authrequest.AuthRequestUserDto;
import com.dongkuksystems.dbox.models.type.manager.authrequest.AuthRequest;

public interface AuthRequestService {
	
	List<AuthRequest> selectAuthRequest(AuthRequestUserDto authRequestUserDto);
	List<AuthRequest> selectAuthWithdrawal(AuthRequestUserDto authRequestUserDto);
	
	void updateAuthWithdrawal(AuthRequestPatchDto authRequestPatchDto, UserSession userSession, String ip) throws Exception;
	
	void collectAuthWithdrawal(AuthRequestCollectDto authRequestCollectDto, UserSession userSession) throws Exception;

}
