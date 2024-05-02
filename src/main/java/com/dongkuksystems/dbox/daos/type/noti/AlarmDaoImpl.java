package com.dongkuksystems.dbox.daos.type.noti;

import java.util.List;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Repository;

import com.dongkuksystems.dbox.models.type.noti.Alarm;

@Primary
@Repository
public class AlarmDaoImpl implements AlarmDao {
	
	private AlarmMapper alarmMapper;

	public AlarmDaoImpl(AlarmMapper alarmMapper) {
		this.alarmMapper = alarmMapper;
	}

	@Override
	public List<Alarm> selectAlarmList(String uReceiverId) {
		return alarmMapper.selectAlarmList(uReceiverId);
	}
	
	@Override
	public List<Alarm> selectAlarmDetailList(String uReceiverId, boolean isRequestedFromExternal) {
		return alarmMapper.selectAlarmDetailList(uReceiverId, isRequestedFromExternal);
	}
	
  @Override
  public int selectAlarmCount(String uReceiverId) {
  	return alarmMapper.selectAlarmCount(uReceiverId);
  }

  @Override
  public Alarm selectAlarmByObjId(String rObjectId) {

    return alarmMapper.selectAlarmByObjId(rObjectId);
  }

  @Override
  public List<Alarm> selectOneByObjId(String uObjId, String sender, String uMsgType) {

    return alarmMapper.selectOneByObjId(uObjId, sender, uMsgType);
  }
  
  
}
