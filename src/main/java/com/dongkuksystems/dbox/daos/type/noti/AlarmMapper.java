package com.dongkuksystems.dbox.daos.type.noti;

import java.util.List;

import org.apache.ibatis.annotations.Param;

import com.dongkuksystems.dbox.models.type.noti.Alarm;

public interface AlarmMapper {
	public List<Alarm> selectAlarmList(@Param("uReceiverId") String uReceiverId);
	public List<Alarm> selectAlarmDetailList(@Param("uReceiverId") String uReceiverId, @Param("isRequestedFromExternal") boolean isRequestedFromExternal);
	public int selectAlarmCount(@Param("uReceiverId") String uReceiverId);
  public Alarm selectAlarmByObjId(@Param("rObjectId") String rObjectId);
  public List<Alarm> selectOneByObjId(@Param("uObjId")String uObjId, @Param("uSenderId")String sender, @Param("uMsgType")String uMsgType);
	
}
