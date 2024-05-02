package com.dongkuksystems.dbox.services.unlockuser;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.DocLogItem;
import com.dongkuksystems.dbox.daos.type.manager.deptMgr.DeptMgrDao;
import com.dongkuksystems.dbox.daos.type.manager.managerconfig.ManagerConfigDao;
import com.dongkuksystems.dbox.daos.type.manager.noticonfig.NotiConfigDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.unlock.UserUnLockDto;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.table.log.LogDoc;
import com.dongkuksystems.dbox.models.type.manager.Mgr;
import com.dongkuksystems.dbox.models.type.manager.deptMgr.DeptMgrs;
import com.dongkuksystems.dbox.models.type.manager.noticonfig.NotiConfig;
import com.dongkuksystems.dbox.models.type.noti.Alarm;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.notification.NotificationService;
import com.dongkuksystems.dbox.services.user.UserService;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

@Service
public class UnLockUserServiceImpl extends AbstractCommonService implements UnLockUserService {
  @Value("${jwt.token.header}")
  private String tokenHeader;
  @Value("${kupload.base-path}")
  private String kuploadBasePath;
  private final NotiConfigDao notiConfigDao;
  private final UserService userService;
  private final DeptMgrDao deptMgrDao;
  private final NotificationService notificationService;
  private final ManagerConfigDao managerConfigDao;

  public UnLockUserServiceImpl(NotiConfigDao notiConfigDao, UserService userService, DeptMgrDao deptMgrDao,
      NotificationService notificationService, ManagerConfigDao managerConfigDao) {
    this.notiConfigDao = notiConfigDao;
    this.userService = userService;
    this.deptMgrDao = deptMgrDao;
    this.notificationService = notificationService;
    this.managerConfigDao = managerConfigDao;
  }

  @Override
  public String postUnlockUser(UserSession userSession, UserUnLockDto userUnLockDto) throws Exception {
    // TODO Auto-generated method stub

    IDfSession idfSession = this.getIdfSession(userSession);
    IDfPersistentObject idf_PObj = null;
    IDfPersistentObject idf_PObj2 = null;
    final String userId = userSession.getUser().getUserId();
    final String mgrType = "C";
    VUser userData = userService.selectOneByUserId(userId).orElse(new VUser());
    DeptMgrs deptMgr = deptMgrDao.kingByDeptCode(userData.getOrgId());
    VUser kingData = userService.selectOneByUserId(deptMgr.getUUserId()).orElse(new VUser());
    List<Mgr> mgrList = managerConfigDao.selectMgrList(mgrType, userData.getComOrgId());

    String result = null;
    List<String> deptKingMail = new ArrayList<>();
    if (userId.equals(deptMgr.getUUserId()) || "0".equals(userData.getEmpType())) {
      for (int i = 0; i < mgrList.size(); i++) {
        VUser cMgr = userService.selectOneByUserId(mgrList.get(i).getUUserId()).orElse(new VUser());
        deptKingMail.add(cMgr.getEmail());
      }
    } else {
      deptKingMail.add(kingData.getEmail());
    }
    idfSession.beginTrans();
    try {

      if (idfSession == null || !idfSession.isConnected()) {
        throw new Exception("DCTM Session 가져오기 실패");
      }
      try {
        idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_req_user_unlock");
        idf_PObj.setString("u_req_user_id", userId);
        idf_PObj.setString("u_req_date", (new DfTime()).toString());
        idf_PObj.setString("u_req_status", "R");
        idf_PObj.setString("u_req_reason", userUnLockDto.getUnLockReason());
        idf_PObj.setBoolean("u_plan_to_retire", userUnLockDto.isPlanToRetire());
        idf_PObj.setBoolean("u_plan_to_leave", userUnLockDto.isPlanToLeave());
        idf_PObj.setBoolean("u_know_of_sec", userUnLockDto.isKnowOfSec());
        idf_PObj.save();
        result = "success";
      } catch (Exception e) {
        e.printStackTrace();
        result = "fail";
      }

      if (userId.equals(deptMgr.getUUserId()) || "0".equals(userData.getEmpType())) {
        for (int i = 0; i < mgrList.size(); i++) {
          NotiConfig notiData2 = notiConfigDao.selectOneByCodes(userData.getComOrgId(), "OU");

          if ("Y".equals(notiData2.getUAlarmYn())) {
            idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
            idf_PObj2.setString("u_msg_type", "OU");
            idf_PObj2.setString("u_sender_id", userSession.getDUserId());
            idf_PObj2.setString("u_receiver_id", mgrList.get(i).getUUserId());
            idf_PObj2.setString("u_msg", "특이사용자" + "'" + userData.getDisplayName() + "'" + "이(가) 잠금 해제 요청을 하였습니다.");
            idf_PObj2.setString("u_obj_id", idf_PObj.getString("r_object_id"));
            idf_PObj2.setString("u_sent_date", new DfTime().toString());
            idf_PObj2.setString("u_action_yn", "N");
            idf_PObj2.setString("u_action_need_yn", "N");
            idf_PObj2.save();
          }

          if ("Y".equals(notiData2.getUEmailYn())) {
            StringBuffer content = new StringBuffer();
            content.append("<html> ");
            content.append("<body>");
            content
                .append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");
            content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 특이사용자 '"
                + userData.getDisplayName() + "이/가 잠금 해제 요청을 하였습니다." + "</font>");
            content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>");
            content.append(" </body></html>");
            notificationService.sendMail("dbox@dongkuk.com", deptKingMail,
                "[D'Box]특이사용자" + "'" + userData.getDisplayName() + "'" + "이/가 잠금 해제 요청을 하였습니다.", content.toString());
          }
          if ("Y".equals(notiData2.getUMmsYn())) {
            String mobileTel = null;
            if (userId.equals(deptMgr.getUUserId()) || "0".equals(userData.getEmpType())) {
              VUser cMgr = userService.selectOneByUserId(mgrList.get(i).getUUserId()).orElse(new VUser());
              mobileTel = cMgr.getMobileTel().replace("-", "");
              notificationService.sendKakao(cMgr.getUserId(), mobileTel, "dbox_alarm_001-1",
                  "[D'Box]특이사용자 " + userData.getDisplayName() + "이/가 잠금 해제 요청을 하였습니다.");
            }

          }
        }
      } else {
        NotiConfig notiData = notiConfigDao.selectOneByCodes(userData.getComOrgId(), "OR");
        if ("Y".equals(notiData.getUAlarmYn())) {
          idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
          idf_PObj2.setString("u_msg_type", "OR");
          idf_PObj2.setString("u_sender_id", userSession.getDUserId());
          idf_PObj2.setString("u_receiver_id", deptMgr.getUUserId());
          idf_PObj2.setString("u_msg", "특이사용자" + "'" + userData.getDisplayName() + "'" + "이/가 잠금 해제 요청을 하였습니다.");
          idf_PObj2.setString("u_obj_id", idf_PObj.getString("r_object_id"));
          idf_PObj2.setString("u_sent_date", new DfTime().toString());
          idf_PObj2.setString("u_action_yn", "N");
          idf_PObj2.setString("u_action_need_yn", "N");
          idf_PObj2.save();
        }

        if ("Y".equals(notiData.getUEmailYn())) {
          StringBuffer content = new StringBuffer();
          content.append("<html> ");
          content.append("<body>");
          content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");
          content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 특이사용자 '"
              + userData.getDisplayName() + "이/가 잠금 해제 요청을 하였습니다." + "</font>");
          content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>");
          content.append(" </body></html>");
          notificationService.sendMail("dbox@dongkuk.com", deptKingMail,
              "[D'Box]특이사용자" + "'" + userData.getDisplayName() + "'" + "이/가 잠금 해제 요청을 하였습니다.", content.toString());
        }
        if ("Y".equals(notiData.getUMmsYn())) {
          String mobileTel = null;
          mobileTel = kingData.getMobileTel().replace("-", "");
          notificationService.sendKakao(kingData.getUserId(), mobileTel, "dbox_alarm_001-1",
              "[D'Box]특이사용자 " + userData.getDisplayName() + "이/가 잠금 해제 요청을 하였습니다.");
        }
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
    return result;

  }

}
