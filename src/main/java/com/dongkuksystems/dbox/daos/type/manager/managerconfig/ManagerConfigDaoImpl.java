package com.dongkuksystems.dbox.daos.type.manager.managerconfig;

import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.manager.Mgr;

@Primary
@Repository
public class ManagerConfigDaoImpl implements ManagerConfigDao{
	private final ManagerConfigMapper managerConfigMapper;
	
	public ManagerConfigDaoImpl (ManagerConfigMapper managerConfigMapper) {
		this.managerConfigMapper = managerConfigMapper;
	}
	
	@Override
	public List<Mgr> selectManagerConfig(String uUserId){
		return managerConfigMapper.selectManagerConfig(uUserId);
	}
	
	@Override
	public List<Mgr> selectDeptManagerConfig(String uUserId){
		return managerConfigMapper.selectDeptManagerConfig(uUserId);
	}

  @Override
  public List<Mgr> selectMgrList(String uMgrType, String uComCode) {   
    return managerConfigMapper.selectMgrList(uMgrType, uComCode);
  }
	

}
