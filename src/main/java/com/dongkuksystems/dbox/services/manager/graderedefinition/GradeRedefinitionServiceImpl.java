package com.dongkuksystems.dbox.services.manager.graderedefinition;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.daos.type.manager.grade.GradeRedefinitionDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.type.manager.graderedefinition.GradeRedefinition;
import com.dongkuksystems.dbox.services.AbstractCommonService;

@Service
public class GradeRedefinitionServiceImpl extends AbstractCommonService implements GradeRedefinitionService {

  private final GradeRedefinitionDao gradeRedefinitionDao;

  public GradeRedefinitionServiceImpl(GradeRedefinitionDao gradeRedefinitionDao) {
    this.gradeRedefinitionDao = gradeRedefinitionDao;
  }

  @Override
  public List<GradeRedefinition> selectGradeRedefinition() {
    return gradeRedefinitionDao.selectGradeRedefinition();
  }

  @Override
  @CacheEvict(value = "getSecLevelMap", allEntries = true)
  public String patchGradeRedefinition(String rObjectId, String uCodeName1, UserSession userSession) throws Exception {
    IDfSession idfSession = null;
    try {
      idfSession = this.getIdfSession(userSession);
      String s_ObjId = rObjectId;
      IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));
      idf_PObj.setString("u_code_name1", uCodeName1);
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
