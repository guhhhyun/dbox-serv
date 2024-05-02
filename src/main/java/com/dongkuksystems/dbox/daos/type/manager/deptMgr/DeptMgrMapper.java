package com.dongkuksystems.dbox.daos.type.manager.deptMgr;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.type.manager.deptMgr.DeptMgrs;

public interface DeptMgrMapper {

	List<DeptMgrs> selectByDeptCode(@Param("uDeptCode") String uDeptCode);

  DeptMgrs kingByDeptCode(@Param("uDeptCode") String uDeptCode);

}
