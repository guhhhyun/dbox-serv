package com.dongkuksystems.dbox.services.seclvl;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.request.LvlDownDetail;

public interface LvlDownService {

  List<LvlDownDetail> lvlDownList() throws Exception;
  
  String approveLvlDown(String lvlDownId, UserSession userSession) throws Exception;

  String rejectLvlDown(String lvlDownId, UserSession userSession, String rejectReason) throws Exception;

}
