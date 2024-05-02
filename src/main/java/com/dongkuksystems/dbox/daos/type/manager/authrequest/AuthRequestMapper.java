package com.dongkuksystems.dbox.daos.type.manager.authrequest;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.manager.authrequest.AuthRequestUserDto;
import com.dongkuksystems.dbox.models.type.manager.authrequest.AuthRequest;

public interface AuthRequestMapper {
	
	public List<AuthRequest> selectAuthRequest(@Param("authRequestUser") AuthRequestUserDto authRequestUserDto);
	public List<AuthRequest> selectAuthWithdrawal(@Param("authRequestUser") AuthRequestUserDto authRequestUserDto);
	public List<AuthRequest> selectObjectId(@Param("rObjectId") String rObjectId);
}
