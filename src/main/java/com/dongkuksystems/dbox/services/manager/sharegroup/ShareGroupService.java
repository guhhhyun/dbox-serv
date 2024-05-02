package com.dongkuksystems.dbox.services.manager.sharegroup;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.sharegroup.PatchDeptDto;
import com.dongkuksystems.dbox.models.dto.type.manager.sharegroup.PatchShareGroupDto;
import com.dongkuksystems.dbox.models.dto.type.manager.sharegroup.ShareGroupCreateDto;
import com.dongkuksystems.dbox.models.type.manager.sharegroup.ShareGroup;

public interface ShareGroupService {
	List<ShareGroup> selectShareGroups();

	List<ShareGroup> selectDeptInShareGroup(String rObjectId);

	String createShareGroup(UserSession userSession, ShareGroupCreateDto dto) throws Exception;

	String patchDept(String rObjectId, UserSession userSession, PatchDeptDto dto) throws Exception;

	String deleteShareGroup(String rObjectId, UserSession userSession ) throws Exception;

	String deleteDept(String rObjectId, UserSession userSession, String uDeptCode) throws Exception;

	String patchShareGroup(String rObjectId, UserSession userSession, PatchShareGroupDto dto) throws Exception;

	
	
}
