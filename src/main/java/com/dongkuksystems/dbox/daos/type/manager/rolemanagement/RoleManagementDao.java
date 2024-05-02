package com.dongkuksystems.dbox.daos.type.manager.rolemanagement;

import java.util.List;
import java.util.Optional;

import com.dongkuksystems.dbox.models.type.manager.rolemanagement.RoleManagement;

public interface RoleManagementDao {
	public List<RoleManagement> selectRoleManagement(String uDocFlag);
	
	public Optional<RoleManagement> selectUnPolicyGroup(String rObjectId, String uOptionVal);


}
