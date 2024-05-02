package com.dongkuksystems.dbox.daos.type.manager.rolemanagement;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.type.manager.rolemanagement.RoleManagement;

@Primary
@Repository
public class RoleManagementDaoImpl implements RoleManagementDao{
	private RoleManagementMapper roleManagementMapper;
	
	public RoleManagementDaoImpl(RoleManagementMapper roleManagementMapper) {
		this.roleManagementMapper = roleManagementMapper;
	}
	
	  @Override
	  public List<RoleManagement> selectRoleManagement(String uDocFlag){
		  return roleManagementMapper.selectRoleManagement(uDocFlag);
	  }
	  
	  
	  @Override
	  public Optional<RoleManagement> selectUnPolicyGroup(String rObjectId, String uOptionVal){
		  return roleManagementMapper.selectUnPolicyGroup(rObjectId, uOptionVal);
	  }

}
