package com.dongkuksystems.dbox.daos.type.manager.authrequest;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.manager.authrequest.AuthRequestUserDto;
import com.dongkuksystems.dbox.models.type.manager.authrequest.AuthRequest;

public interface AuthRequestDao {
	
	public List<AuthRequest> selectAuthRequest(AuthRequestUserDto authRequestUserDto);		
	
	public List<AuthRequest> selectAuthWithdrawal(AuthRequestUserDto authRequestUserDto);		

	public List<AuthRequest> selectObjectId(String rObjectId);		

}
