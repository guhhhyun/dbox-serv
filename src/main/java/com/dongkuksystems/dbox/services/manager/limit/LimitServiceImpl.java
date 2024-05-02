package com.dongkuksystems.dbox.services.manager.limit;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.daos.type.manager.limit.LimitDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.type.manager.limit.Limit;
import com.dongkuksystems.dbox.services.AbstractCommonService;

@Service
public class LimitServiceImpl extends AbstractCommonService implements LimitService {

  private final LimitDao limitDao;

  public LimitServiceImpl(LimitDao limitDao) {
    this.limitDao = limitDao;
  }

  @Override
  public List<Limit> selectLimitValue(String uComCode) {
    return limitDao.selectLimitValue(uComCode);
  }

  @Override  
  @CacheEvict(value = "getConfigDocHandleLimitMap", allEntries = true)
  public String patchLimitValue(UserSession userSession, String rObjectId, String uCodeVal) throws Exception {
    IDfSession idfSession = null;
    try {
      idfSession = this.getIdfSession(userSession);
      IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(rObjectId));
      idf_PObj.setString("u_code_val3", uCodeVal);
      idf_PObj.setString("u_update_user", idfSession.getLoginUserName());
      idf_PObj.setString("u_update_date", (new DfTime()).toString());
      idf_PObj.save();
    } catch (Exception e) {
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    return rObjectId;
  }
}
