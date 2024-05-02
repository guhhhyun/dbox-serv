package com.dongkuksystems.dbox.services.log;


import com.dongkuksystems.dbox.models.common.UserSession;


public interface LogService {
  void insertDocLog(UserSession userSession, String objectId, String jobCode, String jobGubun, String ip) throws Exception;
}
