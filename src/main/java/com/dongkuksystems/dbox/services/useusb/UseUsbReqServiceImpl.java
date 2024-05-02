package com.dongkuksystems.dbox.services.useusb;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.NotiItem;
import com.dongkuksystems.dbox.daos.table.etc.gwdept.GwDeptDao;
import com.dongkuksystems.dbox.daos.table.useusb.ReqUseUsbDao;
import com.dongkuksystems.dbox.daos.type.agree.AgreeDao;
import com.dongkuksystems.dbox.daos.type.doc.DocDao;
import com.dongkuksystems.dbox.daos.type.manager.deptMgr.DeptMgrDao;
import com.dongkuksystems.dbox.daos.type.manager.noticonfig.NotiConfigDao;
import com.dongkuksystems.dbox.daos.type.noti.AlarmDao;
import com.dongkuksystems.dbox.errors.BadRequestException;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.request.ReqUseUsbApprovalListDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqUseUsbDto;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.type.manager.deptMgr.DeptMgrs;
import com.dongkuksystems.dbox.models.type.manager.noticonfig.NotiConfig;
import com.dongkuksystems.dbox.models.type.noti.Alarm;
import com.dongkuksystems.dbox.models.type.request.ReqUseUsb;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.auth.AuthService;
import com.dongkuksystems.dbox.services.dept.GwDeptService;
import com.dongkuksystems.dbox.services.notification.NotificationService;
import com.dongkuksystems.dbox.services.user.UserService;
import com.dongkuksystems.dbox.utils.MailSenderUtils;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

@Service
public class UseUsbReqServiceImpl extends AbstractCommonService implements UseUsbReqService {

  private final ReqUseUsbDao reqUseUsbDao;
  private final DocDao docDao;
  private final GwDeptService deptService;
  private final GwDeptDao deptDao;
  private final DeptMgrDao deptMgrDao;
  private final UserService userService;
  private final AuthService authService;
  private final AgreeDao agreeDao;
  private final NotiConfigDao notiConfigDao;
  private final NotificationService notificationService;
  private final AlarmDao alarmDao;

  public UseUsbReqServiceImpl(ReqUseUsbDao reqUseUsbDao, DocDao docDao, GwDeptService deptService,
      DeptMgrDao deptMgrDao, UserService userService, AuthService authService, AgreeDao agreeDao,
      NotificationService notificationService, GwDeptDao deptDao, NotiConfigDao notiConfigDao,
      AlarmDao alarmDao) {
    this.reqUseUsbDao = reqUseUsbDao;
    this.docDao = docDao;
    this.deptService = deptService;
    this.deptMgrDao = deptMgrDao;
    this.userService = userService;
    this.authService = authService;
    this.agreeDao = agreeDao;
    this.notificationService = notificationService;
    this.deptDao = deptDao;
    this.notiConfigDao = notiConfigDao;
    this.alarmDao = alarmDao;
  }

  @Override
  public List<ReqUseUsbApprovalListDto> selectReqUseUsbApprovList(String userId) throws Exception {

    List<ReqUseUsbApprovalListDto> reqUseUseApprovList = reqUseUsbDao.selectReqUseUsbApprovList(userId);
    return reqUseUseApprovList;
  }

  @Override
  public String createReqUseUsb(UserSession userSession, ReqUseUsb reqUseUsb) throws Exception {
    IDfSession idfSession = this.getIdfSession(userSession);
    IDfPersistentObject idf_PObj = null;
    IDfPersistentObject idf_PObj2 = null;
    IDfPersistentObject idf_Doc2 = null;
    IDfDocument idf_Doc = null;
    IDfSession adSess = null;

//      VUser reqUser = userService.selectOneByUserId(userSession.getDUserId()).orElse(null);
    VUser user = userSession.getUser();
    VUser reqUser = userService.selectOneByUserId(reqUseUsb.getUReqUserId()).orElse(new VUser());
    DeptMgrs deptMgr = deptMgrDao.kingByDeptCode(reqUseUsb.getUReqDeptCode());
    VUser deptMgrData = userService.selectOneByUserId(deptMgr.getUUserId()).orElse(new VUser());
    List<String> deptMgrEmail = new ArrayList<>();
    deptMgrEmail.add(deptMgrData.getEmail());
    idfSession.beginTrans();
    try {

      if (idfSession == null || !idfSession.isConnected()) {
        throw new Exception("DCTM Session 가져오기 실패");
      }
      adSess = DCTMUtils.getAdminSession();
      if (!adSess.isTransactionActive()) {
        adSess.beginTrans();
      }

      VDept dept = deptDao.selectOneByOrgId(user.getOrgId())
          .orElseThrow(() -> new NotFoundException("There is no such dept"));
      reqUseUsb.setUApprover(dept.getManagerPerId());
      reqUseUsb.setUReqDeptCode(user.getOrgId());

      idf_PObj = ReqUseUsbDto.CreateReqUseUsb(idfSession, reqUseUsb);
      NotiConfig notiData = notiConfigDao.selectOneByCodes(reqUser.getComOrgId(), "UR");
      
      try {
        if ("Y".equals(notiData.getUAlarmYn())) {
          idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
          idf_PObj2.setString("u_msg_type", "UR");
          idf_PObj2.setString("u_sender_id", userSession.getDUserId());
          idf_PObj2.setString("u_receiver_id", deptMgr.getUUserId());
          idf_PObj2.setString("u_msg", reqUser.getDisplayName() + "이/가 외부저장매체 사용요청을 하였습니다.");
          idf_PObj2.setString("u_obj_id", idf_PObj.getString("r_object_id"));
          idf_PObj2.setString("u_sent_date", new DfTime().toString());
          idf_PObj2.setString("u_action_need_yn", "Y");
          idf_PObj2.save();
        }
        if ("Y".equals(notiData.getUEmailYn())) {
          StringBuffer content = new StringBuffer();
          content.append("<html> ");
          content.append("<body>");
          content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");
          content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; "
              + reqUser.getDisplayName() + "가 외부저장매체 사용요청을 하였습니다.</font>");
          content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>");
          content.append(" </body></html>");
          notificationService.sendMail("dbox@dongkuk.com", deptMgrEmail,
              "[D'Box]" + "'" +reqUser.getDisplayName() + "'" + "이/가 외부저장매체 사용요청을 하였습니다.", content.toString());
        }
        if ("Y".equals(notiData.getUMmsYn())) {
          String mobileTel = deptMgrData.getMobileTel().replace("-", "");
          notificationService.sendKakao(deptMgr.getUUserId(), mobileTel, "dbox_alarm_014-1",
              reqUser.getDisplayName() + "이/가 외부저장매체 사용요청을 하였습니다.");
        }
      } catch (Exception e) {
        throw e;
      }
      
      idfSession.commitTrans();
      adSess.commitTrans();
    } catch (Exception e) {
      throw e;
    } finally {
      if (idfSession != null) {
        if (idfSession.isTransactionActive()) {
          idfSession.abortTrans();
        }
        if (idfSession.isConnected() && adSess.isConnected()) {
          sessionRelease(userSession.getUser().getUserId(), idfSession);
          adSess.disconnect();
        }
      }
    }

    return idf_PObj.getObjectId().getId();
  }

  @Override
  public String approveReqUseUsb(String useUsbrobjectId, UserSession userSession, ReqUseUsb reqUseUsb)
      throws Exception {
    IDfSession idfSession = this.getIdfSession(userSession);
    IDfPersistentObject idf_PObj = null;
    IDfPersistentObject idf_PObj2 = null;
    IDfPersistentObject idf_PObj3 = null;
    ReqUseUsb req = reqUseUsbDao.selectOneByObjectId(useUsbrobjectId).orElse(null);
    VUser user = userSession.getUser();
    VUser reqUser = userService.selectOneByUserId(req.getUReqUserId()).orElse(new VUser());
    DeptMgrs deptMgr = deptMgrDao.kingByDeptCode(req.getUReqDeptCode());
    VUser deptMgrData = userService.selectOneByUserId(deptMgr.getUUserId()).orElse(new VUser());
    List<String> reqUserEmail = new ArrayList<>();
    reqUserEmail.add(reqUser.getEmail());
    
    if (!user.getUserId().equals(req.getUApprover())) {
      throw new BadRequestException("승인자가 아닙니다.");
    }

    idfSession.beginTrans();
    try {

      if (idfSession == null || !idfSession.isConnected()) {
        throw new Exception("DCTM Session 가져오기 실패");
      }

      reqUseUsb.setUUseTime(req.getUUseTime());
      idf_PObj = ReqUseUsbDto.ApproveReqUseUsb(useUsbrobjectId, idfSession, reqUseUsb, userSession);

      List<Alarm> alarmData = alarmDao.selectOneByObjId(useUsbrobjectId, reqUser.getUserId(), "UR");
      for(int i=0;i<alarmData.size(); i++) {
        idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(alarmData.get(i).getRObjectId()));
        idf_PObj3.setString("u_action_yn", "Y");
        idf_PObj3.setString("u_performer_id", userSession.getDUserId());
        idf_PObj3.setString("u_action_date", new DfTime().toString());
        idf_PObj3.save();
      }
      
      try {
        NotiConfig notiData = notiConfigDao.selectOneByCodes(reqUser.getComOrgId(), "UR");
        if("Y".equals(notiData.getUAlarmYn())) {
          String now = new DfTime().toString();
          idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
          idf_PObj2.setString("u_msg_type", "UR");
          idf_PObj2.setString("u_sender_id", userSession.getDUserId());
          idf_PObj2.setString("u_receiver_id", req.getUReqUserId());
          idf_PObj2.setString("u_msg", reqUser.getDisplayName()+ "이/가 외부저장매체 사용요청이 승인되었습니다.");
          idf_PObj2.setString("u_obj_id", req.getRObjectId());
          idf_PObj2.setString("u_sent_date", now);
          idf_PObj2.setString("u_action_date", now);
          idf_PObj2.setString("u_action_yn", "Y");
          idf_PObj2.setString("u_action_need_yn", "N");
          idf_PObj2.save();
        }
        if("Y".equals(notiData.getUEmailYn())) {
          StringBuffer content = new StringBuffer();       
          content.append("<html> ");   
          content.append("<body>");           
          content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");           
          content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; "+reqUser.getDisplayName()+"이/가 외부저장매체 사용요청이 승인되었습니다.</font>");  
          content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>"); 
          content.append(" </body></html>");          
          notificationService.sendMail("dbox@dongkuk.com", reqUserEmail, "[D'Box]" + "'" + reqUser.getDisplayName() + "'" + "이/가 외부저장매체 사용요청이 승인되었습니다."
                  , content.toString());
        }
        if("Y".equals(notiData.getUMmsYn())) {
          String mobileTel = reqUser.getMobileTel().replace("-", "");
          notificationService.sendKakao(reqUser.getUserId(), mobileTel, "dbox_alarm_015-1", reqUser.getDisplayName()+ "이/가 외부저장매체 사용요청이 승인되었습니다.");
        }
        
      }catch (Exception e) {
        throw e;
      }
      

      idfSession.commitTrans();

    } catch (Exception e) {
      throw e;
    } finally {
      if (idfSession != null) {
        if (idfSession.isTransactionActive()) {
          idfSession.abortTrans();
        }
        if (idfSession.isConnected()) {
          sessionRelease(userSession.getUser().getUserId(), idfSession);
        }
      }
    }

    return idf_PObj.getObjectId().getId();
  }

  @Override
  public String rejectReqUseUsb(String useUsbrobjectId, UserSession userSession, ReqUseUsb reqUseUsb) throws Exception {

    IDfSession idfSession = this.getIdfSession(userSession);
    IDfPersistentObject idf_PObj = null;
    IDfPersistentObject idf_PObj2 = null;
    IDfPersistentObject idf_PObj3 = null;
    VUser user = userSession.getUser();

    ReqUseUsb req = reqUseUsbDao.selectOneByObjectId(useUsbrobjectId).orElse(null);
    VUser reqUser = userService.selectOneByUserId(req.getUReqUserId()).orElse(new VUser());
    DeptMgrs deptMgr = deptMgrDao.kingByDeptCode(req.getUReqDeptCode());
    VUser deptMgrData = userService.selectOneByUserId(deptMgr.getUUserId()).orElse(new VUser());
    List<String> reqUserEmail = new ArrayList<>();
    reqUserEmail.add(reqUser.getEmail());

    if (!user.getUserId().equals(req.getUApprover())) {
      throw new RuntimeException();
    }

    idfSession.beginTrans();
    try {

      if (idfSession == null || !idfSession.isConnected()) {
        throw new Exception("DCTM Session 가져오기 실패");
      }
      idf_PObj = ReqUseUsbDto.RejectReqUseUsb(useUsbrobjectId, idfSession, reqUseUsb, userSession);
      
      List<Alarm> alarmData = alarmDao.selectOneByObjId(useUsbrobjectId, reqUser.getUserId(), "UR");
      for(int i=0;i<alarmData.size(); i++) {
        idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(alarmData.get(i).getRObjectId()));
        idf_PObj3.setString("u_action_yn", "N");
        idf_PObj3.setString("u_performer_id", userSession.getDUserId());
        idf_PObj3.setString("u_action_date", new DfTime().toString());
        idf_PObj3.save();
      }     
      try {
        NotiConfig notiData = notiConfigDao.selectOneByCodes(reqUser.getComOrgId(), "UR");
        if("Y".equals(notiData.getUAlarmYn())) {
          idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
          idf_PObj2.setString("u_msg_type", "UR");
          idf_PObj2.setString("u_sender_id", userSession.getDUserId());
          idf_PObj2.setString("u_receiver_id", req.getUReqUserId());
          idf_PObj2.setString("u_msg", reqUser.getDisplayName()+ "가 외부저장매체 사용요청이 반려되었습니다.");
          idf_PObj2.setString("u_obj_id", req.getRObjectId());
          idf_PObj2.setString("u_action_date", new DfTime().toString());
          idf_PObj2.setString("u_action_yn", "N");
          idf_PObj2.setString("u_action_need_yn", "N");
          idf_PObj2.save();
        } 
        if("Y".equals(notiData.getUEmailYn())) {
          StringBuffer content = new StringBuffer();       
          content.append("<html> ");   
          content.append("<body>");           
          content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");           
          content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; "+reqUser.getDisplayName()+"가 외부저장매체 사용요청이 반려되었습니다.</font>");  
          content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>"); 
          content.append(" </body></html>");          
          notificationService.sendMail("dbox@dongkuk.com", reqUserEmail, reqUser.getDisplayName()+ "가 외부저장매체 사용요청이 반려되었습니다."
          , content.toString());
        }
        if("Y".equals(notiData.getUMmsYn())) {           
          String mobileTel = reqUser.getMobileTel().replace("-", "");
          notificationService.sendKakao(reqUser.getUserId(), mobileTel, "dbox_alarm_015", reqUser.getDisplayName()+ "가 외부저장매체 사용요청이 반려되었습니다.");      
        }
      }catch (Exception e) {
        throw e;
      }
      idfSession.commitTrans();
    } catch (Exception e) {
      throw e;
    } finally {
      if (idfSession != null) {
        if (idfSession.isTransactionActive()) {
          idfSession.abortTrans();
        }
        if (idfSession.isConnected()) {
          sessionRelease(userSession.getUser().getUserId(), idfSession);
        }
      }
    }

    return idf_PObj.getObjectId().getId();
  }

  @Override
  public Map<String, Integer> approveAllReqUseUsb(UserSession userSession, ReqUseUsb reqUseUsb,
      List<String> useUsbRobjectIdList) throws Exception {
    int successCnt = 0;
    int failCnt = 0;
    try {
      for (String objId : useUsbRobjectIdList) {
        try {
          approveReqUseUsb(objId, userSession, reqUseUsb);
          successCnt++;
        } catch (Exception e) {
          failCnt++;
          continue;
        } finally {
        }
      }
    } catch (Exception e) {
      throw e;
    } finally {
    }
    Map<String, Integer> result = new HashMap<String, Integer>();
    result.put("success", successCnt);
    result.put("fail", failCnt);

    return result;
  }

  @Override
  public Map<String, Integer> rejectAllReqUseUsb(UserSession userSession, ReqUseUsb reqUseUsb,
      List<String> useUsbRobjectIdList) throws Exception {

    int successCnt = 0;
    int failCnt = 0;
    try {
      for (String objId : useUsbRobjectIdList) {
        try {
          rejectReqUseUsb(objId, userSession, reqUseUsb);
          successCnt++;
        } catch (Exception e) {
          failCnt++;
          continue;
        } finally {
        }
      }
    } catch (Exception e) {
      throw e;
    } finally {
    }
    Map<String, Integer> result = new HashMap<String, Integer>();
    result.put("success", successCnt);
    result.put("fail", failCnt);

    return result;
  }

}
