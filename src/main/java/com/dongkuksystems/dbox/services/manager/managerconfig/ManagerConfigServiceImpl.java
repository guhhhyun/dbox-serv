package com.dongkuksystems.dbox.services.manager.managerconfig;

import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.dongkuksystems.dbox.daos.type.manager.managerconfig.ManagerConfigDao;
import com.dongkuksystems.dbox.models.dto.type.manager.managerconfig.ManagerConfigDto;
import com.dongkuksystems.dbox.models.type.manager.Mgr;
import com.dongkuksystems.dbox.services.AbstractCommonService;

@Service
public class ManagerConfigServiceImpl extends AbstractCommonService implements ManagerConfigService {

	private final ManagerConfigDao managerConfigDao;

	public ManagerConfigServiceImpl(ManagerConfigDao managerConfigDao) {
		this.managerConfigDao = managerConfigDao;
	}

	@Override
	public ManagerConfigDto selectManagerConfig(String uUserId) {
		ManagerConfigDto dto = new ManagerConfigDto();
		List<Mgr> list = managerConfigDao.selectManagerConfig(uUserId);
		List<Mgr> deptList = managerConfigDao.selectDeptManagerConfig(uUserId);


		for (Mgr mgr : list) {
			if (mgr.getUMgrType().equals("C")) {
				dto.setCompanyComCode(mgr.getUComCode());
			}
			if (mgr.getUMgrType().equals("G")) {
				dto.setGroupComCode(mgr.getUComCode());
			}

		}
		
		
		List<String> comDeptCode = new ArrayList<>();
		
		for(int i = 0; i < deptList.size(); i++) {
			if (deptList.get(i).getUMgrType().equals("D")) {
				comDeptCode.add(deptList.get(i).getUDeptCode());							
				dto.setDeptComCode(deptList.get(i).getUComCode());
			}
			if (deptList.get(i).getUMgrType().equals("M")) {
				
				comDeptCode.add(deptList.get(i).getUDeptCode());				
				dto.setDeptComCode(deptList.get(i).getUComCode());
		}
			
			dto.setCompanyDeptCode(comDeptCode);
		}

		return dto;
	}

	@Override
	public Boolean selectManagerChk(String uUserId) {
		ManagerConfigDto dto = new ManagerConfigDto();
		List<Mgr> list = managerConfigDao.selectManagerConfig(uUserId);
		Boolean type = false;
		type = (list.size() > 0) ? true : false;

		return type;
	}
	
	
	@Override
  public List<Mgr> selectDeptManagerConfig(String uUserId) {
    return managerConfigDao.selectDeptManagerConfig(uUserId);
  }
	
	
	
	
}
