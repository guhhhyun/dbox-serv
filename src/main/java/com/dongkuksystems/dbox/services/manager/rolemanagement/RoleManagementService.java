package com.dongkuksystems.dbox.services.manager.rolemanagement;

import java.util.List;
import java.util.Optional;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.rolemanagement.RoleManagementDto;
import com.dongkuksystems.dbox.models.type.manager.rolemanagement.RoleManagement;

public interface RoleManagementService {
	List<RoleManagement> selectRoleManagement(String uDocFlag);
		

	void updatePolicy(UserSession userSession, String rObjectId, RoleManagementDto dto) throws Exception;
	
	Optional<RoleManagement> selectUnPolicyGroup(String rObjectId, String uOptionVal);


	
}
