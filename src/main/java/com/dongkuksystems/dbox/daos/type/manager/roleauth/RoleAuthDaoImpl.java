package com.dongkuksystems.dbox.daos.type.manager.roleauth;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.dto.type.manager.rolemanagement.RoleManagementDto;
import com.dongkuksystems.dbox.models.type.manager.roleauth.RoleAuth;


@Primary
@Repository
public class RoleAuthDaoImpl implements RoleAuthDao {
	private RoleAuthMapper roleAuthMapper;

	public RoleAuthDaoImpl(RoleAuthMapper roleAuthMapper) {
		this.roleAuthMapper = roleAuthMapper;
	}

	@Override
	public List<RoleAuth> selectRoleAuthGroups(String type) {
		return roleAuthMapper.selectRoleAuthGroups(type);
	}

	@Override
	public List<RoleAuth> selectMgrGroups(String type, RoleManagementDto roleManagementDto) {
		return roleAuthMapper.selectRoleAuthGroups(type, roleManagementDto);
	}
	
	@Override
	public List<RoleAuth> selectRoleAuthGroupUsers(String uAuthGroup, String uConfigFlag, String uGroupScope) {
		return roleAuthMapper.selectRoleAuthGroupUsers(uAuthGroup, uConfigFlag, uGroupScope);
	}
	
	@Override
	public List<RoleAuth> selectMgrUsers(String uComCode, String uGroupScope) {
	  return roleAuthMapper.selectMgrUsers(uComCode, uGroupScope);
	}
	
	@Override
	public List<RoleAuth> selectRoleAuthGroupUser2(String uAuthGroup, String uConfigFlag, String uGroupScope, String uDocFlag) {
		return roleAuthMapper.selectRoleAuthGroupUser2(uAuthGroup, uConfigFlag, uGroupScope, uDocFlag);
	}

	@Override
	public List<RoleAuth> selectEntMgrUsers() {
		return roleAuthMapper.selectEntMgrUsers();
	}

	@Override
	public List<RoleAuth> selectCompanyMgrUsers(String userId) {
		return roleAuthMapper.selectCompanyMgrUsers(userId);
	}
	
	@Override
	public List<RoleAuth> selectDeptMgrGroup(String uDocFlag, String uOptionVal) {
		return roleAuthMapper.selectDeptMgrGroup(uDocFlag, uOptionVal);
	}
	
	@Override
	public List<RoleAuth> selectDeptMgrUser(String groupName) {
		return roleAuthMapper.selectDeptMgrUser(groupName);
	}
	
  @Override
  public List<RoleAuth> selectCompanyAclName(String uComCode) {
    return roleAuthMapper.selectCompanyAclName(uComCode);
  }	
  
  @Override
  public List<RoleAuth> selectDeleteMgrUser(String mgrType, String userId) {
    return roleAuthMapper.selectDeleteMgrUser(mgrType, userId);
  }
  
	
}
