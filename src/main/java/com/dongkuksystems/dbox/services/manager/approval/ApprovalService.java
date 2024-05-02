package com.dongkuksystems.dbox.services.manager.approval;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.type.code.Code;

public interface ApprovalService {

  List<Code> selectApproval(String uCodeVal1);

  String patchApproval(String rObjectId, String uCodeVal2, UserSession userSession) throws Exception;

}
