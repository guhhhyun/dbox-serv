package com.dongkuksystems.dbox.services.manager.usbpolicy;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.daos.type.manager.usbpolicy.UsbPolicyDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.type.manager.usbpolicy.UsbPolicyType;
import com.dongkuksystems.dbox.services.AbstractCommonService;

@Service
public class UsbPolicyServiceImpl extends AbstractCommonService implements UsbPolicyService {

  private final UsbPolicyDao usbPolicyDao;

  public UsbPolicyServiceImpl(UsbPolicyDao usbPolicyDao) {
    this.usbPolicyDao = usbPolicyDao;
  }

  @Override
  public List<UsbPolicyType> selectUsbPolicyComp(String uComCode) {
    return usbPolicyDao.selectUsbPolicyComp(uComCode);
  }

  @Override
  public List<UsbPolicyType> selectUsbPolicyDept(String uComCode) {
    return usbPolicyDao.selectUsbPolicyDept(uComCode);
  }

  @Override
  public List<UsbPolicyType> selectUsbPolicyUser(String uComCode) {
    return usbPolicyDao.selectUsbPolicyUser(uComCode);
  }

  @Override
  @CacheEvict(value = "getConfigUsbBasePolicyMap", allEntries = true)
  public String patchCompValue(String rObjectId, String uCodeVal2, UserSession userSession) throws Exception {
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
    return uCodeVal2;
  }

  @Override
  public String postDeptSave(String uDeptCode, String uComCode, UserSession userSession) throws Exception {
    IDfSession idfSession = null;
    List<UsbPolicyType> EndDeptList = usbPolicyDao.selectEndDeptList();

    try {
      idfSession = this.getIdfSession(userSession);

      Calendar calStartDt = Calendar.getInstance();
      calStartDt.setTime(new Date());

      Calendar calEndDt = Calendar.getInstance();
      calEndDt.setTime(new Date());
      calEndDt.add(Calendar.DATE, 7);

      // 특정 형태의 날짜로 값을 뽑기
      DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      DateFormat edf = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
      String startDate = sdf.format(calStartDt.getTime());
      String endDate = edf.format(calEndDt.getTime());
      int count = 0;

      // 기존데이터 있는데 적용기간 지난경우 (수정)
      for (int i = 0; i < EndDeptList.size(); i++) {
        if (EndDeptList.get(i).getUTargetId().equals(uDeptCode)) {
          String objectId = EndDeptList.get(i).getRObjectId();
          IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(objectId));
          idf_PObj.setString("u_policy", "RW"); // 정책 저장시 기본값 : Read/Write
          idf_PObj.setString("u_start_date", startDate); // 시작일 저장시 기본값 : SYSDATE
          idf_PObj.setString("u_end_date", endDate); // 종료일 기본값 : SYSDATE+7
          idf_PObj.save();
          count = count + 1;
        }
      }
      // 기존데이터 없을경우 등록
      if (count == 0) {
        IDfPersistentObject idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_usb_policy");
        idf_PObj.setString("u_target_type", "D");
        idf_PObj.setString("u_target_id", uDeptCode);
        idf_PObj.setString("u_policy", "RW"); // 정책 저장시 기본값 : Read/Write
        idf_PObj.setString("u_start_date", startDate); // 시작일 저장시 기본값 : SYSDATE
        idf_PObj.setString("u_end_date", endDate); // 종료일 기본값 : SYSDATE+7
        idf_PObj.setString("u_com_code", uComCode);
        idf_PObj.save();
      }
    } catch (Exception e) {
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    return uDeptCode;
  }

  @Override
  public String deleteDept(String rObjectId, UserSession userSession) throws Exception {
    IDfSession idfSession = this.getIdfSession(userSession);
    try {
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

  @Override
  public String postUserSave(String userId, String uComCode, UserSession userSession) throws Exception {
    IDfSession idfSession = null;
    List<UsbPolicyType> EndUserList = usbPolicyDao.selectEndUserList();

    try {
      idfSession = this.getIdfSession(userSession);

      Calendar calStartDt = Calendar.getInstance();
      calStartDt.setTime(new Date());

      Calendar calEndDt = Calendar.getInstance();
      calEndDt.setTime(new Date());
      calEndDt.add(Calendar.DATE, 7);

      // 특정 형태의 날짜로 값을 뽑기
      DateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
      DateFormat edf = new SimpleDateFormat("yyyy-MM-dd 23:59:59");
      String startDate = sdf.format(calStartDt.getTime());
      String endDate = edf.format(calEndDt.getTime());
      int count = 0;

      // 기존데이터 있는데 적용기간 지난경우 (수정)
      for (int i = 0; i < EndUserList.size(); i++) {
        if (EndUserList.get(i).getUTargetId().equals(userId)) {
          String objectId = EndUserList.get(i).getRObjectId();
          IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(objectId));
          idf_PObj.setString("u_policy", "RW"); // 정책 저장시 기본값 : Read/Write
          idf_PObj.setString("u_start_date", startDate); // 시작일 저장시 기본값 : SYSDATE
          idf_PObj.setString("u_end_date", endDate); // 종료일 기본값 : SYSDATE+7
          idf_PObj.save();
          count = count + 1;
        }
      }
      // 기존데이터 없을경우 등록
      if (count == 0) {
        IDfPersistentObject idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_usb_policy");
        idf_PObj.setString("u_target_type", "U");
        idf_PObj.setString("u_target_id", userId);
        idf_PObj.setString("u_policy", "RW"); // 정책 저장시 기본값 : Read/Write
        idf_PObj.setString("u_start_date", startDate); // 시작일 저장시 기본값 : SYSDATE
        idf_PObj.setString("u_end_date", endDate); // 종료일 기본값 : SYSDATE+7
        idf_PObj.setString("u_com_code", uComCode);
        idf_PObj.save();
      }
    } catch (Exception e) {
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    return userId;

  }

  @Override
  public String deleteUser(String rObjectId, UserSession userSession) throws Exception {
    IDfSession idfSession = this.getIdfSession(userSession);
    try {
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

  @Override
  public String patchUserValue(String rObjectId, String uPolicy, String uStartDate, String uEndDate,
      UserSession userSession) throws Exception {
    IDfSession idfSession = null;
    try {
      idfSession = this.getIdfSession(userSession);
      String s_ObjId = rObjectId;
      IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));
      idf_PObj.setString("u_policy", uPolicy);
      idf_PObj.setString("u_start_date", uStartDate);
      idf_PObj.setString("u_end_date", uEndDate);
      idf_PObj.save();
    } catch (Exception e) {
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    return uPolicy;
  }

  @Override
  public String patchDeptValue(String rObjectId, String uPolicy, String uStartDate, String uEndDate,
      UserSession userSession) throws Exception {
    IDfSession idfSession = null;
    try {
      idfSession = this.getIdfSession(userSession);
      String s_ObjId = rObjectId;
      IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(s_ObjId));
      idf_PObj.setString("u_policy", uPolicy);
      idf_PObj.setString("u_start_date", uStartDate);
      idf_PObj.setString("u_end_date", uEndDate);
      idf_PObj.save();
    } catch (Exception e) {
      throw e;
    } finally {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
    }
    return uPolicy;
  }

}
