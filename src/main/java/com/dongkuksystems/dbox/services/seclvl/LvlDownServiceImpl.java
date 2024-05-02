package com.dongkuksystems.dbox.services.seclvl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.daos.table.etc.gwdept.GwDeptDao;
import com.dongkuksystems.dbox.daos.table.lvldown.LvlDownDao;
import com.dongkuksystems.dbox.daos.type.doc.DocDao;
import com.dongkuksystems.dbox.daos.type.manager.noticonfig.NotiConfigDao;
import com.dongkuksystems.dbox.daos.type.noti.AlarmDao;
import com.dongkuksystems.dbox.errors.BadRequestException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.request.LvlDownDetail;
import com.dongkuksystems.dbox.models.table.etc.GwJobTitle;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.type.code.Code;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.manager.noticonfig.NotiConfig;
import com.dongkuksystems.dbox.models.type.noti.Alarm;
import com.dongkuksystems.dbox.models.type.request.ReqLvlDown;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.notification.NotificationService;
import com.dongkuksystems.dbox.services.user.UserService;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

@Service
public class LvlDownServiceImpl extends AbstractCommonService implements LvlDownService{

  private final LvlDownDao lvlDownDao;
  private final DocDao docDao;
  private final UserService userService;
  private final GwDeptDao deptDao;
  private final NotificationService notificationService;
  private final NotiConfigDao notiConfigDao;
  private final AlarmDao alarmDao;
   
  public LvlDownServiceImpl(LvlDownDao lvlDownDao, DocDao docDao, UserService userService, GwDeptDao deptDao
      , NotificationService notificationService, NotiConfigDao notiConfigDao, AlarmDao alarmDao) {
    this.lvlDownDao = lvlDownDao;
    this.docDao = docDao;
    this.userService = userService;
    this.deptDao = deptDao;
    this.notificationService = notificationService;
    this.notiConfigDao = notiConfigDao;
    this.alarmDao = alarmDao;
  }

  @Override
  public List<LvlDownDetail> lvlDownList() throws Exception {
    final ModelMapper modelMapper = getModelMapper();

    List<ReqLvlDown> reqLvlDownList = lvlDownDao.reqLvlDownDetailAll();

    List<LvlDownDetail> lvlDownDetailList = reqLvlDownList.stream().map((item) -> {
      LvlDownDetail lvlDownDetail = modelMapper.map(item, LvlDownDetail.class);
      
      VUser requser = Optional.ofNullable(item.getReqUserDetail()).orElse(new VUser());
      VUser approver = Optional.ofNullable(item.getApproverDetail()).orElse(new VUser());
      Doc docName = Optional.ofNullable(item.getDocDetail()).orElse(new Doc());
      lvlDownDetail.setUReqUserName(requser.getDisplayName());
      lvlDownDetail.setUReqUserJobTitleName(
          Optional.ofNullable(requser.getJobTitleDetail()).orElse(new GwJobTitle()).getName());
      lvlDownDetail.setUReqUserDeptName(requser.getOrgNm());
      lvlDownDetail.setUApproverName(
          Optional.ofNullable(item.getApproverDetail()).orElse(new VUser()).getDisplayName());
      lvlDownDetail.setUApproverDeptName(approver.getOrgNm());
      lvlDownDetail.setUReqDocName(docName.getObjectName());
      lvlDownDetail.setUDocSize(docName.getRContentSize());
      VDept requserDept = Optional.ofNullable(requser.getDeptDetail()).orElse(new VDept());
      VDept approverDept = Optional.ofNullable(approver.getDeptDetail()).orElse(new VDept());
      lvlDownDetail.setUReqUserComName(
          Optional.ofNullable(requserDept.getCompanyDetail()).orElse(new Code()).getUCodeName1());
      lvlDownDetail.setUApproverComName(
          Optional.ofNullable(approverDept.getCompanyDetail()).orElse(new Code()).getUCodeName1());
      lvlDownDetail.setUApproverJobTitleName(
          Optional.ofNullable(approver.getJobTitleDetail()).orElse(new GwJobTitle()).getName());
      return lvlDownDetail;

    }).collect(Collectors.toList());

    return lvlDownDetailList;
  }

  @Override
  public String approveLvlDown(String lvlDownId, UserSession userSession) throws Exception {
    IDfSession idfSession = this.getIdfSession(userSession);
    IDfPersistentObject idf_PObj = null;
    IDfPersistentObject idf_PObj2 = null;
    IDfPersistentObject idf_PObj3 = null;
    IDfSession adSess = null;
    ReqLvlDown lvlData = lvlDownDao.dataByObjId(lvlDownId);
    Doc reqDoc = docDao.selectOne(lvlData.getUReqDocId())
        .orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
    String comCode = deptDao.selectComCodeByCabinetCode(reqDoc.getUCabinetCode());
    VUser reqUser = userService.selectOneByUserId(lvlData.getUReqUser()).orElse(null);
    List<String> reciever = new ArrayList<>();
    reciever.add(reqUser.getEmail());
    
    idfSession.beginTrans();
    try {

      if (idfSession == null || !idfSession.isConnected()) {
        throw new Exception("DCTM Session 가져오기 실패");
      }

      idf_PObj = idfSession.getObject(new DfId(lvlDownId));
      
      idf_PObj.setString("u_req_status", "A");
      idf_PObj.setString("u_approver", userSession.getDUserId());
      idf_PObj.setString("u_action_date",  (new DfTime()).toString());      
      idf_PObj.save();
      
      adSess = DCTMUtils.getAdminSession();
      if (!adSess.isTransactionActive()) {
       adSess.beginTrans();
      }
      IDfDocument idf_Doc = (IDfDocument) adSess.getObject(new DfId(lvlData.getUReqDocId()));
      idf_Doc.setString("u_sec_level", lvlData.getUAfterLevel());
      idf_Doc.save();
      
      List<Alarm> alarmData = alarmDao.selectOneByObjId(lvlDownId, lvlData.getUReqUser(), "SR");
      for(int i=0;i<alarmData.size(); i++) {
        idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(alarmData.get(i).getRObjectId()));
        idf_PObj3.setString("u_action_yn", "Y");
        idf_PObj3.setString("u_action_date", new DfTime().toString());
        idf_PObj3.save();
      }
      
      NotiConfig notiData = notiConfigDao.selectOneByCodes(comCode, "SR");
      if("Y".equals(notiData.getUAlarmYn())) {
         idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
         idf_PObj2.setString("u_msg_type", "SR");
         idf_PObj2.setString("u_sender_id", userSession.getDUserId());
         idf_PObj2.setString("u_receiver_id", lvlData.getUReqUser());
         idf_PObj2.setString("u_performer_id", userSession.getDUserId());
         idf_PObj2.setString("u_action_yn", "A");
         idf_PObj2.setString("u_action_need_yn", "N");
         idf_PObj2.setString("u_msg", "'" + reqDoc.getTitle() + "'" + " 문서의 등급변경이 승인되었습니다.");
         idf_PObj2.setString("u_obj_id", lvlDownId);
         idf_PObj2.setString("u_action_date", new DfTime().toString());
         idf_PObj2.save();
      }
      if("Y".equals(notiData.getUEmailYn())) {
        StringBuffer content = new StringBuffer();       
        content.append("<html> ");   
        content.append("<body>");           
        content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");           
        content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; "+reqDoc.getTitle()+" 문서의 등급변경이 승인되었습니다./font>");             
        content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>"); 
        content.append(" </body></html>");          
        notificationService.sendMail("dbox@dongkuk.com", reciever, "[D'Box]" + "'" + reqDoc.getTitle() + "'" + " 문서의 등급변경이 승인되었습니다."
        , content.toString());
      }
      if("Y".equals(notiData.getUMmsYn())) {           
        String mobileTel = reqUser.getMobileTel().replace("-", "");
        notificationService.sendKakao(reqUser.getUserId(), mobileTel, "dbox_alarm_013", reqDoc.getTitle() + " 문서의 등급변경이 승인되었습니다.");      
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
        if (adSess.isTransactionActive()) {
          adSess.abortTrans();         
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
  public String rejectLvlDown(String lvlDownId, UserSession userSession, String rejectReason) throws Exception {
    IDfSession idfSession = this.getIdfSession(userSession);
    ReqLvlDown lvlData = lvlDownDao.dataByObjId(lvlDownId);
    Doc reqDoc = docDao.selectOne(lvlData.getUReqDocId())
        .orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
    VUser reqUser = userService.selectOneByUserId(lvlData.getUReqUser()).orElse(null);
    String comCode = deptDao.selectComCodeByCabinetCode(reqDoc.getUCabinetCode());
    List<String> reciever = new ArrayList<>();
    reciever.add(reqUser.getEmail());

    IDfPersistentObject idf_PObj = null;
    IDfPersistentObject idf_PObj2 = null;
    IDfPersistentObject idf_PObj3 = null;

    idfSession.beginTrans();
    try {

      if (idfSession == null || !idfSession.isConnected()) {
        throw new Exception("DCTM Session 가져오기 실패");
      }

      idf_PObj = idfSession.getObject(new DfId(lvlDownId));
      idf_PObj.setString("u_reject_reason", rejectReason);
      idf_PObj.setString("u_approver", userSession.getDUserId());
      idf_PObj.setString("u_req_status", "D");
      idf_PObj.setString("u_action_date",  (new DfTime()).toString());
      idf_PObj.save();
      
      List<Alarm> alarmData = alarmDao.selectOneByObjId(lvlDownId, lvlData.getUReqUser(), "SR");
      for(int i=0;i<alarmData.size(); i++) {
        idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(alarmData.get(i).getRObjectId()));
        idf_PObj3.setString("u_action_yn", "N");
        idf_PObj3.setString("u_action_date", new DfTime().toString());
        idf_PObj3.save();
      }
      
      NotiConfig notiData = notiConfigDao.selectOneByCodes(comCode, "SR");
      if("Y".equals(notiData.getUAlarmYn())) {
         idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
         idf_PObj2.setString("u_msg_type", "SR");
         idf_PObj2.setString("u_sender_id", userSession.getDUserId());
         idf_PObj2.setString("u_receiver_id", lvlData.getUReqUser());
         idf_PObj2.setString("u_performer_id", userSession.getDUserId());
         idf_PObj2.setString("u_action_yn", "N");
         idf_PObj2.setString("u_action_need_yn", "N");
         idf_PObj2.setString("u_msg", "'" + reqDoc.getTitle() + "'" + " 문서의 등급변경이 반려되었습니다.");
         idf_PObj2.setString("u_obj_id", lvlDownId);
         idf_PObj2.setString("u_action_date", new DfTime().toString());
         idf_PObj2.save();
      }
      if("Y".equals(notiData.getUEmailYn())) {
        StringBuffer content = new StringBuffer();       
        content.append("<html> ");   
        content.append("<body>");           
        content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");           
        content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; "+reqDoc.getTitle()+" 문서의 등급변경이 반려되었습니다./font>");             
        content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>"); 
        content.append(" </body></html>");          
        notificationService.sendMail("dbox@dongkuk.com", reciever, "[D'Box]" + "'" + reqDoc.getTitle() + "'" + " 문서의 등급변경이 반려되었습니다."
        , content.toString());
      }
      if("Y".equals(notiData.getUMmsYn())) {           
        String mobileTel = reqUser.getMobileTel().replace("-", "");
        notificationService.sendKakao(reqUser.getUserId(), mobileTel, "dbox_alarm_013", reqDoc.getTitle() + " 문서의 등급변경이 반려되었습니다.");      
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
  
}
