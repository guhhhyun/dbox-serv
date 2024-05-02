package com.dongkuksystems.dbox.services.manager.realtime;

import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.daos.type.manager.realTime.RealTimeDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.realtime.RealTimeDto;
import com.dongkuksystems.dbox.models.type.manager.realTime.RealTime;
import com.dongkuksystems.dbox.services.AbstractCommonService;

@Service
public class RealTimeServiceImpl extends AbstractCommonService implements RealTimeService {

  private final RealTimeDao realTimeDao;

  public RealTimeServiceImpl(RealTimeDao realTimeDao) {
    this.realTimeDao = realTimeDao;
  }

  @Override
  public List<RealTime> selectRealTime(String uComCode) {
    return realTimeDao.selectRealTime(uComCode);
  }

  @Override
  @CacheEvict(value = "getConjfigMidSaveDeptMap", allEntries = true)
  public String postDeptSava(UserSession userSession, RealTimeDto dto) throws Exception {
    IDfSession idfSession = null;
    try {
      idfSession = this.getIdfSession(userSession);
      IDfPersistentObject idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_code");
      idf_PObj.setString("u_code_type", "CONFIG_MID_SAVE_DEPT");
      idf_PObj.setString("u_type_name", "중간저장(Ctrl-S) 허용부서 설정");
      idf_PObj.setString("u_code_val1", dto.getUCompCode());
      idf_PObj.setString("u_code_val2", dto.getUDeptCode());
      idf_PObj.setString("u_code_name1", dto.getUCompanyName());
      idf_PObj.setString("u_code_name2", dto.getUDeptName());
      idf_PObj.setString("u_create_user", idfSession.getLoginUserName());
      idf_PObj.setString("u_create_date", (new DfTime()).toString());
      idf_PObj.save();
    } catch (Exception e) {
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    return userSession.getDUserId();
  }

  @Override
  public String deleteDept(String rObjectId, UserSession userSession) throws Exception {
    IDfSession idfSession = null;
    try {
      idfSession = this.getIdfSession(userSession);
      String s_ObjId = rObjectId;
      IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));
      idf_PObj.destroy();
    } catch (Exception e) {
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    return rObjectId;
  }

}
