package com.dongkuksystems.dbox.services.manager.autochange;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.type.code.Code;

public interface AutoChangeService {

  List<Code> selectAutoChange(String uCodeVal1);

  String patchAutoChange(String rObjectId, String uCodeVal2, UserSession userSession) throws Exception;

}
