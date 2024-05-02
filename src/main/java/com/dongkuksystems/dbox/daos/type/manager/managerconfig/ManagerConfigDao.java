package com.dongkuksystems.dbox.daos.type.manager.managerconfig;

import java.util.List;
import java.util.Optional;

import com.dongkuksystems.dbox.models.type.manager.Mgr;

public interface ManagerConfigDao {
	public List<Mgr> selectManagerConfig(String uUserId);
	public List<Mgr> selectDeptManagerConfig(String uUserId);
	public List<Mgr> selectMgrList(String uMgrType, String uComCode);
	
}
