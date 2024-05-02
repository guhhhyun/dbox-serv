package com.dongkuksystems.dbox.services.noti;

import java.util.List;
import java.util.Map;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.noti.AlarmDetailDto;
import com.dongkuksystems.dbox.models.dto.type.noti.AlarmDto;
import com.dongkuksystems.dbox.models.dto.type.noti.AlarmOneDto;

public interface AlarmsService {
	public List<AlarmDetailDto> getAlarmList(String userId, boolean isApproval) throws Exception;
	public int getAlarmCount(String userId) throws Exception;
	public Map<String, Integer> deleteAlarm(UserSession userSession, AlarmOneDto dto) throws Exception;
	public String approveNoti(UserSession userSession, String rObjectId, String actionYn, String unLockReason,
      String rejectReason) throws Exception;

	void patchAlarmApproval(AlarmDto alarmDto);

}
