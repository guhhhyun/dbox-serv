package com.dongkuksystems.dbox.daos.type.manager.deptinformconfig;

import java.util.Optional;

import com.dongkuksystems.dbox.models.type.manager.deptinformconfig.DeptInformConfig; 


public interface DeptInformConfigDao {
	public Optional<DeptInformConfig> selectListByOrgId(String uComCode, String uDeptCode);
	public Optional<DeptInformConfig> selectList(String uDeptCode);

}
