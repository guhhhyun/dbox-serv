package com.dongkuksystems.dbox.daos.type.noti;

import java.util.List;

import com.dongkuksystems.dbox.models.type.noti.Alarm;
import org.apache.ibatis.annotations.Param;

public interface AlarmDao {
	public List<Alarm> selectAlarmList(String uReceiverId);
	public List<Alarm> selectAlarmDetailList(@Param("uReceiverId") String uReceiverId, @Param("isRequestedFromExternal") boolean isRequestedFromExternal);
	public int selectAlarmCount(String uReceiverId);
	public Alarm selectAlarmByObjId(String rObjectId);
	public List<Alarm> selectOneByObjId(String uObjId, String sender, String uMsgType);
}
