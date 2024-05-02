package com.dongkuksystems.dbox.daos.type.manager.deptinformconfig;

import java.util.Optional;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.type.manager.deptinformconfig.DeptInformConfig; 

public interface DeptInformConfigMapper { 
  public Optional<DeptInformConfig> selectListByOrgId(@Param("uComCode") String uComCode, @Param("uDeptCode") String uDeptCode);
  public Optional<DeptInformConfig> selectList( @Param("uDeptCode") String uDeptCode);
}
