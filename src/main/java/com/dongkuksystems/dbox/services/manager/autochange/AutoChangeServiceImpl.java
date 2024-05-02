package com.dongkuksystems.dbox.services.manager.autochange;

import java.util.List;

import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.daos.type.manager.autochange.AutoChangeDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.type.code.Code;
import com.dongkuksystems.dbox.services.AbstractCommonService;

@Service
public class AutoChangeServiceImpl extends AbstractCommonService implements AutoChangeService {

  private final AutoChangeDao autoChangeDao;

  public AutoChangeServiceImpl(AutoChangeDao autoChangeDao) {
    this.autoChangeDao = autoChangeDao;
  }

  @Override
  public List<Code> selectAutoChange(String uCodeVal1) {
    return autoChangeDao.selectAutoChange(uCodeVal1);
  }

  @Override
  public String patchAutoChange(String rObjectId, String uCodeVal2, UserSession userSession) throws Exception {
    IDfSession idfSession = null;   
  try {
    idfSession = this.getIdfSession(userSession);      
    String s_ObjId = rObjectId;
    IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));
    idf_PObj.setString("u_code_val2", uCodeVal2);
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