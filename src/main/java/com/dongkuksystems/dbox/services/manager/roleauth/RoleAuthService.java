package com.dongkuksystems.dbox.services.manager.roleauth;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.roleauth.RoleAuthDto;
import com.dongkuksystems.dbox.models.dto.type.manager.rolemanagement.RoleManagementDto;
import com.dongkuksystems.dbox.models.type.manager.roleauth.RoleAuth;

public interface RoleAuthService {
	
	List<RoleAuth> selectRoleAuthGroups(String type);
	
	List<RoleAuth> selectMgrGroups(String type, RoleManagementDto roleManagementDto);
	
	List<RoleAuth> selectRoleAuthGroupUsers(String uAuthGroup, String uConfigFlag, String uGroupScope);
	
	List<RoleAuth> selectMgrUsers(String uComCode, String uGroupScope);	
	
	void createRoleAuthUser(RoleAuthDto dto, UserSession userSession) throws Exception;
	
	void deleteRoleAuthUser(RoleAuthDto dto, UserSession userSession) throws Exception;
	
	List<RoleAuth> selectDeptMgrGroup(String uDocFlag ,String uOptionVal);


}
