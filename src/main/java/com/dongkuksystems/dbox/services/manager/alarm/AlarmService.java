package com.dongkuksystems.dbox.services.manager.alarm;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.alarm.PatchAlarmDto;
import com.dongkuksystems.dbox.models.type.manager.noticonfig.NotiConfig;


public interface AlarmService {
	List<NotiConfig> selectAlarm(String uComCode);

	String patchAlarm(String rObjectId, UserSession userSession, PatchAlarmDto dto) throws Exception;

}
