package com.dongkuksystems.dbox.services.manager.managerconfig;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.manager.managerconfig.ManagerConfigDto;
import com.dongkuksystems.dbox.models.type.manager.Mgr;

public interface ManagerConfigService {
	ManagerConfigDto selectManagerConfig(String uUserId);
	Boolean selectManagerChk(String uUserId);
  List<Mgr> selectDeptManagerConfig(String uUserId);
}
