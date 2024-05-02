package com.dongkuksystems.dbox.services.manager.approval;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.daos.type.manager.approval.ApprovalDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.type.code.Code;
import com.dongkuksystems.dbox.services.AbstractCommonService;

@Service
public class ApprovalServiceImpl extends AbstractCommonService implements ApprovalService {

  private final ApprovalDao approvalDao;

  public ApprovalServiceImpl(ApprovalDao approvalDao) {
    this.approvalDao = approvalDao;
  }

  @Override
  public List<Code> selectApproval(String uCodeVal1) {
    return approvalDao.selectApproval(uCodeVal1);
  }

  @Override
  @CacheEvict(value = "getConfigTransWfMap", allEntries = true)
  public String patchApproval(String rObjectId, String uCodeVal2, UserSession userSession) throws Exception {
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
