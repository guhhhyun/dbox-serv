package com.dongkuksystems.dbox.services.userpreset;

import java.util.List;
import java.util.Optional;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.user.UserPresetDetailDto;
import com.dongkuksystems.dbox.models.dto.type.user.UserPresetRepeatingDto;
import com.dongkuksystems.dbox.models.type.user.UserPreset;

public interface UserPresetService {
	Optional<UserPreset> selectOneByUserId(UserSession userSession);
	List<UserPreset> selectList(UserSession userSession) throws Exception;
	List<UserPreset> selectDetailList(UserSession userSession);
	List<UserPreset> selectAllList(String rObjectId) throws Exception;
	void patchUserPreset(String rObjectId, UserSession userSession, UserPresetDetailDto dto) throws Exception;
	public int getUserPresetDetailCount(String rObjectId, UserPresetRepeatingDto dto) throws Exception;
	String createUserPreset(UserPresetDetailDto dto, UserSession userSession) throws Exception;
  void deleteUserPreset(String rObjectId, UserSession userSession) throws Exception;
}
