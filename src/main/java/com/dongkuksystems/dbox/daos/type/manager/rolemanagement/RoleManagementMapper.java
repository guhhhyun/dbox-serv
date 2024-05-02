package com.dongkuksystems.dbox.daos.type.manager.rolemanagement;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.type.manager.rolemanagement.RoleManagement;

public interface RoleManagementMapper {
	public List<RoleManagement> selectRoleManagement(@Param("uDocFlag") String uDocFlag);
	
	public Optional<RoleManagement> selectUnPolicyGroup(@Param("rObjectId") String rObjectId, @Param("uOptionVal") String uOptionVal);
	

}
