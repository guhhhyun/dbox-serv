package com.dongkuksystems.dbox.daos.type.manager.roleauth;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.manager.deptmanager.DeptMgrAclDto;
import com.dongkuksystems.dbox.models.dto.type.manager.rolemanagement.RoleManagementDto;
import com.dongkuksystems.dbox.models.type.manager.roleauth.RoleAuth;

public interface RoleAuthDao {
	public List<RoleAuth> selectRoleAuthGroups(String type);
	
	public List<RoleAuth> selectRoleAuthGroupUsers(String uAuthGroup, String uConfigFlag, String uGroupScope);
	public List<RoleAuth> selectMgrUsers(String uComCode, String uGroupScope);

	public List<RoleAuth> selectRoleAuthGroupUser2(String uAuthGroup, String uConfigFlag, String uGroupScope, String uDocFlag);

	
	List<RoleAuth> selectMgrGroups(String type, RoleManagementDto roleManagementDto);
	
	public List<RoleAuth> selectEntMgrUsers();
	
	public List<RoleAuth> selectCompanyMgrUsers(String userId);
	
	public List<RoleAuth> selectDeptMgrGroup(String uDocFlag, String uOptionVal);
	
	public List<RoleAuth> selectDeptMgrUser(String groupName);

	public List<RoleAuth> selectCompanyAclName(String uComCode);
	
	public List<RoleAuth> selectDeleteMgrUser(String mgrType, String userId);
	
	public List<DeptMgrAclDto> selectDeptMgrAcl();
}
