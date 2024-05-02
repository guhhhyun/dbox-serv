package com.dongkuksystems.dbox.services.manager.graderedefinition;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.type.manager.graderedefinition.GradeRedefinition;

public interface GradeRedefinitionService {

  List<GradeRedefinition> selectGradeRedefinition();

  String patchGradeRedefinition(String rObjectId, String uCodeName1, UserSession userSession) throws Exception;
}
