package com.dongkuksystems.dbox.daos.type.manager.deptMgr;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.manager.deptMgr.DeptMgrs;

@Primary
@Repository
public class DeptMgrDaoImpl implements DeptMgrDao{
	
	private DeptMgrMapper deptMgrMapper;
	
	public DeptMgrDaoImpl(DeptMgrMapper deptMgrMapper) {
		this.deptMgrMapper = deptMgrMapper;
	}

	@Override
	public List<DeptMgrs> selectByDeptCode(String uDeptCode) {
	
		return deptMgrMapper.selectByDeptCode(uDeptCode);
	}

  @Override
  public DeptMgrs kingByDeptCode(String uDeptCode) {

    return deptMgrMapper.kingByDeptCode(uDeptCode);
  }
	
	
}

