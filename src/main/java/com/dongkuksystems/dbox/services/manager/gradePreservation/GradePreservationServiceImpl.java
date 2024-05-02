package com.dongkuksystems.dbox.services.manager.gradePreservation;

import java.util.List;

import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.daos.type.manager.gradePreservation.GradePreservationDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.type.manager.gradePreservation.GradePreservation;
import com.dongkuksystems.dbox.services.AbstractCommonService;

@Service
public class GradePreservationServiceImpl extends AbstractCommonService implements GradePreservationService {

  private final GradePreservationDao gradePreservationDao;

  public GradePreservationServiceImpl(GradePreservationDao gradePreservationDao) {
    this.gradePreservationDao = gradePreservationDao;
  }

  @Override
  public List<GradePreservation> selectGradePreservation(String uComCode) {
    return gradePreservationDao.selectGradePreservation(uComCode);
  }

  @Override
  public String patchGradePreservation(String rObjectId, String uLimitValue, String uTeamValue, String uCompValue,
      String uGroupValue, String uPjtEverFlag, UserSession userSession) throws Exception {
    IDfSession idfSession = null;
    try {
      idfSession = this.getIdfSession(userSession);
      String s_ObjId = rObjectId;
      IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));
      idf_PObj.setString("u_sec_s_year", uLimitValue); // 제한
      idf_PObj.setString("u_sec_t_year", uTeamValue); // 팀내
      idf_PObj.setString("u_sec_c_year", uCompValue); // 사내
      idf_PObj.setString("u_sec_g_year", uGroupValue); // 그룹사내
      idf_PObj.setString("u_pjt_ever_flag", uPjtEverFlag);// 문서보존연한 영구 설정
      idf_PObj.save();
    } catch (Exception e) {
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    return userSession.getUser().getUserId();
  }

  @Override
  public String patchGradeAutoExtend(String rObjectId, String uAutoExtendValue, UserSession userSession) throws Exception {
    IDfSession idfSession = null;
    try {
      idfSession = this.getIdfSession(userSession);
      String s_ObjId = rObjectId;
      IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));
      idf_PObj.setString("u_auto_extend", uAutoExtendValue);
      idf_PObj.save();
    } catch (Exception e) {
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    return userSession.getUser().getUserId();
  }

  @Override
  public String patchGradeSaveDept(String rObjectId, String uDeptCodeValue, UserSession userSession) throws Exception {
    IDfSession idfSession = null;
    try {
      idfSession = this.getIdfSession(userSession);
      String s_ObjId = rObjectId;
      IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));   
      int i_ValIdx = idf_PObj.findString("u_no_ext_dept", uDeptCodeValue);
      
      if(i_ValIdx<0)
      {
        idf_PObj.appendString("u_no_ext_dept", uDeptCodeValue);
        idf_PObj.appendString("u_no_ext_reg_date" , (new DfTime()).toString());
        idf_PObj.save();
      }            
    } catch (Exception e) {
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    return userSession.getUser().getUserId();
  }
}