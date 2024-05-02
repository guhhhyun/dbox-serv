package com.dongkuksystems.dbox.daos.type.manager.deptinformconfig;

import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.manager.deptinformconfig.DeptInformConfig; 

@Primary
@Repository
public class DeptInformConfigDaoImpl implements DeptInformConfigDao {
  private DeptInformConfigMapper deptInformConfigMapper;

  public DeptInformConfigDaoImpl(DeptInformConfigMapper deptInformConfigMapper) {
    this.deptInformConfigMapper = deptInformConfigMapper;
  }

  @Override
  public Optional<DeptInformConfig> selectListByOrgId(String uComCode, String uDeptCode) {
    return deptInformConfigMapper.selectListByOrgId(uComCode, uDeptCode);
  }

  @Override
  public Optional<DeptInformConfig> selectList(String uDeptCode) {
	// TODO Auto-generated method stub
	return deptInformConfigMapper.selectList(uDeptCode);
  }

  
}
