package com.dongkuksystems.dbox.services.manager.manageid;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.manageid.ManageIdCreateDto;
import com.dongkuksystems.dbox.models.dto.type.manager.manageid.ManageIdDto;
import com.dongkuksystems.dbox.models.dto.type.manager.manageid.ManageIdTreeDto;
import com.dongkuksystems.dbox.models.type.manager.manageid.ManageId;

public interface ManageIdService {
	
	List<ManageId> selectUserId(ManageIdDto dto);
	
	List<ManageId> selectUserIdLog(String uUserId, long offset, int limit);
	
	void createUserId(ManageIdCreateDto dto, UserSession userSession) throws Exception;
	
	void updateIdStatus(ManageIdCreateDto dto, UserSession userSession) throws Exception;
	
	String createNewUserPreset(String userId, String comCode, String orgId, UserSession userSession) throws Exception;

	ManageIdTreeDto selectMangeIdTree(String comOrgId) throws Exception;
	
}
