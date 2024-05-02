package com.dongkuksystems.dbox.services.manager.alarm;

import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.daos.type.manager.noticonfig.NotiConfigDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.alarm.PatchAlarmDto;
import com.dongkuksystems.dbox.models.type.manager.noticonfig.NotiConfig;
import com.dongkuksystems.dbox.services.AbstractCommonService;

@Service
public class AlarmServiceImpl extends AbstractCommonService implements AlarmService {

  private final NotiConfigDao alarmDao;

  public AlarmServiceImpl(NotiConfigDao alarmDao) {
    this.alarmDao = alarmDao;
  }

  @Override
  public List<NotiConfig> selectAlarm(String uComCode) {
    return alarmDao.selectAll(uComCode);
  }

  @Override  
  public String patchAlarm(String rObjectId, UserSession userSession, PatchAlarmDto dto) throws Exception {
    IDfSession idfSession = null;
    try {
      idfSession = this.getIdfSession(userSession);
      IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(rObjectId));
      idf_PObj.setString("u_alarm_yn", dto.getUAlarmYn());
      idf_PObj.setString("u_email_yn", dto.getUEmailYn());
      idf_PObj.setString("u_mms_yn", dto.getUMmsYn());
      idf_PObj.setString("u_update_user", idfSession.getLoginUserName());
      idf_PObj.setString("u_update_date", (new DfTime()).toString());
      idf_PObj.save();
    } catch (Exception e) {
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    return userSession.getUser().getUserId();
  }

}
