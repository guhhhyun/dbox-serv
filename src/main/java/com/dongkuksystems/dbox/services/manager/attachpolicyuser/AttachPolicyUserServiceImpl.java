package com.dongkuksystems.dbox.services.manager.attachpolicyuser;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.daos.type.manager.attachpolicyuser.AttachPolicyUserDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.attachpolicy.AttachPolicyUserDto;
import com.dongkuksystems.dbox.models.dto.type.manager.attachpolicy.CreateAttachPolicyUserDto;
import com.dongkuksystems.dbox.models.dto.type.manager.attachpolicy.UpdateUserDateDto;
import com.dongkuksystems.dbox.models.type.manager.AttachPolicyUser;
import com.dongkuksystems.dbox.services.AbstractCommonService;

@Service
public class AttachPolicyUserServiceImpl extends AbstractCommonService implements AttachPolicyUserService {

  private final AttachPolicyUserDao attachPolicyUserDao;

  public AttachPolicyUserServiceImpl(AttachPolicyUserDao attachPolicyUserDao) {
    this.attachPolicyUserDao = attachPolicyUserDao;
  }

  @Override
  public List<AttachPolicyUser> selectAll(AttachPolicyUserDto dto) {
    return attachPolicyUserDao.selectAll(dto);
  }

  @Override
  public String createAttachPolicyUser(CreateAttachPolicyUserDto dto, UserSession userSession) throws Exception {
    IDfSession idfSession = null;
    List<AttachPolicyUser> EndAttachUserList = attachPolicyUserDao.selectEndAttachUser(dto.getUPolicyId());

    try {
      idfSession = this.getIdfSession(userSession);
      Calendar calStartDt = Calendar.getInstance();
      calStartDt.setTime(new Date());

      Calendar calEndDt = Calendar.getInstance();
      calEndDt.setTime(new Date());
      calEndDt.add(Calendar.MONTH, 1);

      // 특정 형태의 날짜로 값을 뽑기
      DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      DateFormat edf = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
      String startDate = sdf.format(calStartDt.getTime());
      String endDate = edf.format(calEndDt.getTime());
      int count = 0;

      // 기존데이터 있을경우 수정
      for (int i = 0; i < EndAttachUserList.size(); i++) {
        if (EndAttachUserList.get(i).getUUserId().equals(dto.getUUserId())) {
          String objectId = EndAttachUserList.get(i).getRObjectId();
          IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(objectId));
          idf_PObj.setString("u_start_date", startDate);
          idf_PObj.setString("u_end_date", endDate);
          idf_PObj.setString("u_create_user", idfSession.getLoginUserName());
          idf_PObj.setString("u_create_date", (new DfTime()).toString());
          idf_PObj.save();
          count = count + 1;
        }
      }
      // 기존데이터 없을경우 등록
      if (count == 0) {
        IDfPersistentObject idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_attach_policy_user");
        idf_PObj.setString("u_policy_id", dto.getUPolicyId());
        idf_PObj.setString("u_user_id", dto.getUUserId());
        idf_PObj.setString("u_com_code", dto.getUComCode());
        idf_PObj.setString("u_dept_code", dto.getUDeptCode());
        idf_PObj.setString("u_start_date", startDate);
        idf_PObj.setString("u_end_date", endDate);
        idf_PObj.setString("u_create_user", idfSession.getLoginUserName());
        idf_PObj.setString("u_create_date", (new DfTime()).toString());
        idf_PObj.save();
      }
    } catch (Exception e) {
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    return userSession.getDUserId();
  }

  @Override
  public String deleteAttachPolicyUser(String rObjectId, UserSession userSession) throws Exception {
    IDfSession idfSession = null;
    try {
      String s_ObjId = rObjectId;
      idfSession = this.getIdfSession(userSession);
      IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));
      idf_PObj.destroy();
    } catch (Exception e) {
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    return rObjectId;
  }

  @Override
  public String updateUserDate(UpdateUserDateDto dto, UserSession userSession) throws Exception {
    IDfSession idfSession = null;
    try {
      idfSession = this.getIdfSession(userSession);
      String s_ObjId = dto.getRObjectId();
      IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));      
      idf_PObj.setString("u_start_date", dto.getUStartDate());
      idf_PObj.setString("u_end_date", dto.getUEndDate());
      idf_PObj.save();
    } catch (Exception e) {
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    return userSession.getUser().getUserId();
  }

}
