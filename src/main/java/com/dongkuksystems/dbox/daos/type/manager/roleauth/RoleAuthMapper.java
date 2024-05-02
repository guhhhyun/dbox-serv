package com.dongkuksystems.dbox.daos.type.manager.roleauth;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.dto.type.manager.rolemanagement.RoleManagementDto;
import com.dongkuksystems.dbox.models.type.manager.roleauth.RoleAuth;


public interface RoleAuthMapper {
	
	public List<RoleAuth> selectRoleAuthGroups(@Param("type") String type);
	
	public List<RoleAuth> selectRoleAuthGroups(@Param("type") String type, @Param("roleManagement") RoleManagementDto roleManagementDto);

	public List<RoleAuth> selectRoleAuthGroupUsers(@Param("uAuthGroup") String uAuthGroup, @Param("uConfigFlag") String uConfigFlag, @Param("uGroupScope") String uGroupScope);
	public List<RoleAuth> selectMgrUsers(@Param("uComCode") String uComCode, @Param("uGroupScope") String uGroupScope);
	
	public List<RoleAuth> selectRoleAuthGroupUser2(@Param("uAuthGroup") String uAuthGroup, @Param("uConfigFlag") String uConfigFlag, @Param("uGroupScope") String uGroupScope, @Param("uDocFlag") String uDocFlag);

	public List<RoleAuth> selectEntMgrUsers();
	
	public List<RoleAuth> selectCompanyMgrUsers(@Param("userId") String userId);
	
	public List<RoleAuth> selectDeptMgrGroup(@Param("uDocFlag") String uDocFlag, @Param("uOptionVal") String uOptionVal);

	public List<RoleAuth> selectDeptMgrUser(@Param("groupName") String groupName);
	
	public List<RoleAuth> selectCompanyAclName(@Param("uComCode") String uComCode);
	
	public List<RoleAuth> selectDeleteMgrUser(@Param("mgrType") String mgrType, @Param("userId") String userId);	
}


