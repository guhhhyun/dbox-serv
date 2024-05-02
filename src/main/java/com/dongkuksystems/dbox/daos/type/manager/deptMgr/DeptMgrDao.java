package com.dongkuksystems.dbox.daos.type.manager.deptMgr;

import java.util.List;

import com.dongkuksystems.dbox.models.type.manager.deptMgr.DeptMgrs;

public interface DeptMgrDao {
	public List<DeptMgrs> selectByDeptCode(String uDeptCode);
	public DeptMgrs kingByDeptCode(String uDeptCode);
}
