package com.dongkuksystems.dbox.daos.type.manager.authrequest;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.manager.authrequest.AuthRequestUserDto;
import com.dongkuksystems.dbox.models.type.manager.authrequest.AuthRequest;

@Primary
@Repository
public class AuthRequestDaoImpl implements AuthRequestDao {
	private AuthRequestMapper authRequestMapper;
	
	public AuthRequestDaoImpl(AuthRequestMapper authRequestMapper) {
		this.authRequestMapper = authRequestMapper;
	}
	
	@Override
	  public List<AuthRequest> selectAuthRequest(AuthRequestUserDto authRequestUserDto){
		return authRequestMapper.selectAuthRequest(authRequestUserDto);
	}
	
	@Override
	  public List<AuthRequest> selectAuthWithdrawal(AuthRequestUserDto authRequestUserDto){
		return authRequestMapper.selectAuthWithdrawal(authRequestUserDto);
	}
	
	@Override
	  public List<AuthRequest> selectObjectId(String rObjectId){
		return authRequestMapper.selectObjectId(rObjectId);
	}
}
