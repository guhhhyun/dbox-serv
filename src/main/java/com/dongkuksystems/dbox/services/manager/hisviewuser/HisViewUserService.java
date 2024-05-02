package com.dongkuksystems.dbox.services.manager.hisviewuser;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.hisviewuser.HisViewUserDto;
import com.dongkuksystems.dbox.models.dto.type.manager.hisviewuser.HisViewUserFilterDto;
import com.dongkuksystems.dbox.models.type.manager.hisviewuser.HisViewUser;

public interface HisViewUserService {
	List<HisViewUser> selectAll(HisViewUserFilterDto dto);
	void registHisViewUser(UserSession userSession, HisViewUserDto dto) throws Exception;
	void deleteHisViewUser(UserSession userSession, String rObjectId) throws Exception;

}
