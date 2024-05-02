package com.dongkuksystems.dbox.services.manager.realtime;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.realtime.RealTimeDto;
import com.dongkuksystems.dbox.models.type.manager.realTime.RealTime;

public interface RealTimeService {
  List<RealTime> selectRealTime(String uComCode);

  String postDeptSava(UserSession userSession, RealTimeDto postDeptSaveDto) throws Exception;

  String deleteDept(String rObjectId, UserSession userSession) throws Exception;

}
