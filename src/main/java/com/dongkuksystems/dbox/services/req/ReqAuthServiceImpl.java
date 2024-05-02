package com.dongkuksystems.dbox.services.req;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.DocLogItem;
import com.dongkuksystems.dbox.daos.table.etc.gwdept.GwDeptDao;
import com.dongkuksystems.dbox.daos.table.req.ReqAuthDao;
import com.dongkuksystems.dbox.daos.type.auth.CommonAuthDao;
import com.dongkuksystems.dbox.daos.type.doc.DocDao;
import com.dongkuksystems.dbox.daos.type.manager.deptMgr.DeptMgrDao;
import com.dongkuksystems.dbox.daos.type.manager.noticonfig.NotiConfigDao;
import com.dongkuksystems.dbox.daos.type.noti.AlarmDao;
import com.dongkuksystems.dbox.errors.BadRequestException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.path.DPath;
import com.dongkuksystems.dbox.models.dto.type.request.ReqApproveDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqAuthDetailDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqCreateDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqRejectDto;
import com.dongkuksystems.dbox.models.table.etc.GwJobTitle;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.table.log.LogDoc;
import com.dongkuksystems.dbox.models.type.code.Code;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.doc.DocRepeating;
import com.dongkuksystems.dbox.models.type.manager.deptMgr.DeptMgrs;
import com.dongkuksystems.dbox.models.type.manager.noticonfig.NotiConfig;
import com.dongkuksystems.dbox.models.type.noti.Alarm;
import com.dongkuksystems.dbox.models.type.request.ReqAuth;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.data.DataPathService;
import com.dongkuksystems.dbox.services.doc.DocService;
import com.dongkuksystems.dbox.services.notification.NotificationService;
import com.dongkuksystems.dbox.services.user.UserService;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

@Service
public class ReqAuthServiceImpl extends AbstractCommonService implements ReqAuthService {
  
  @Value("${dbox.url}")
  private String dboxUrl;
  
  private final ReqAuthDao reqAuthDao;
  private final DocDao docDao;
  private final DeptMgrDao deptMgrDao;
  private final UserService userService;
  private final GwDeptDao deptDao;
  private final NotificationService notificationService;
  private final NotiConfigDao notiConfigDao;
  private final AlarmDao alarmDao;
  private final DataPathService dataPathService;
  private final CommonAuthDao commonAuthDao;

  public ReqAuthServiceImpl(ReqAuthDao reqAuthDao, DocDao docDao, DeptMgrDao deptMgrDao,
      UserService userService, GwDeptDao deptDao,NotificationService notificationService, 
      NotiConfigDao notiConfigDao, AlarmDao alarmDao, DataPathService dataPathService
      , CommonAuthDao commonAuthDao) {
    this.reqAuthDao = reqAuthDao;
    this.docDao = docDao;
    this.deptMgrDao = deptMgrDao;
    this.userService = userService;
    this.deptDao = deptDao;
    this.notificationService = notificationService;
    this.notiConfigDao = notiConfigDao;
    this.alarmDao = alarmDao;
    this.dataPathService = dataPathService;
    this.commonAuthDao = commonAuthDao;
  }

  @Override
  public List<ReqAuthDetailDto> reqAuthList() throws Exception {
    final ModelMapper modelMapper = getModelMapper();

    List<ReqAuth> reqAuthList = reqAuthDao.reqAuthDetailAll();

    List<ReqAuthDetailDto> reqAuthDetailList = reqAuthList.stream().map((item) -> {
      ReqAuthDetailDto reqAuthDetailDto = modelMapper.map(item, ReqAuthDetailDto.class);
      
      VUser requser = Optional.ofNullable(item.getReqUserDetail()).orElse(new VUser());
      VUser approver = Optional.ofNullable(item.getApproverDetail()).orElse(new VUser());
      Doc docName = Optional.ofNullable(item.getDocDetail()).orElse(new Doc());
      reqAuthDetailDto.setUReqUserName(requser.getDisplayName());
      reqAuthDetailDto.setUReqUserJobTitleName(
          Optional.ofNullable(requser.getJobTitleDetail()).orElse(new GwJobTitle()).getName());
      reqAuthDetailDto.setUReqUserDeptName(requser.getOrgNm());
      reqAuthDetailDto.setUApproverName(
          Optional.ofNullable(item.getApproverDetail()).orElse(new VUser()).getDisplayName());
      reqAuthDetailDto.setUApproverDeptName(approver.getOrgNm());
      reqAuthDetailDto.setUReqDocName(docName.getObjectName());
      reqAuthDetailDto.setUDocSize(docName.getRContentSize());
      VDept requserDept = Optional.ofNullable(requser.getDeptDetail()).orElse(new VDept());
      VDept approverDept = Optional.ofNullable(approver.getDeptDetail()).orElse(new VDept());
      reqAuthDetailDto.setUReqUserComName(
          Optional.ofNullable(requserDept.getCompanyDetail()).orElse(new Code()).getUCodeName1());
      reqAuthDetailDto.setUApproverComName(
          Optional.ofNullable(approverDept.getCompanyDetail()).orElse(new Code()).getUCodeName1());
      reqAuthDetailDto.setUApproverJobTitleName(
          Optional.ofNullable(approver.getJobTitleDetail()).orElse(new GwJobTitle()).getName());
      return reqAuthDetailDto;

    }).collect(Collectors.toList());

    return reqAuthDetailList;
  }

  @Override
  public ReqAuthDetailDto getReqAuth(String rObjectId) throws Exception {
    final ModelMapper modelMapper = getModelMapper();
    ReqAuth reqAuth = reqAuthDao.dataByObjId(rObjectId);
    return modelMapper.map(reqAuth, ReqAuthDetailDto.class);
  }

  // 알림 --> 알림, 이메일 모든 회사 공통인듯
  @Override
  public String createReqAuth(UserSession userSession, ReqCreateDto dto, String ip) throws Exception {

    IDfSession idfSession = this.getIdfSession(userSession);

    Doc reqDoc = docDao.selectOne(dto.getUReqDocId()).orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
    VUser user = userService.selectOneByUserId(userSession.getDUserId()).orElse(null);
    String cabinetCode = reqDoc.getUCabinetCode();
    List<DocRepeating> docV2 = docDao.selRepeatingOne(reqDoc.getRObjectId());
    String reqDeptCode = deptDao.selectOrgIdByCabinetcode(cabinetCode);
    String reqComCode = deptDao.selectComCodeByCabinetCode(cabinetCode);
    List<DeptMgrs> reqDeptMgr = deptMgrDao.selectByDeptCode(reqDeptCode);
    
    idfSession.beginTrans();
    IDfPersistentObject idf_PObj = null;
    IDfPersistentObject idf_PObj2 = null;
    try {

      if (idfSession == null || !idfSession.isConnected()) {
        throw new Exception("DCTM Session 가져오기 실패");
      }

      idf_PObj = ReqCreateDto.CreateReqAuth(idfSession, dto);
      idf_PObj.setString("u_req_doc_key", reqDoc.getUDocKey());
      idf_PObj.setString("u_own_dept_code", reqDeptCode);
      idf_PObj.setString("u_req_dept_code", user.getOrgId());
      idf_PObj.save();
      try {
        for(int i=0;i<reqDeptMgr.size();i++) {
          String mgrId = reqDeptMgr.get(i).getUUserId();
          VUser reqMgr = userService.selectOneByUserId(mgrId).orElse(new VUser());
          List<String> mgrsEmail = new ArrayList<>();
//          mgrsEmail.add(reqMgr.getEmail());  
          mgrsEmail.add(reqMgr.getEmail()); 
          // TODO 알림 insert
          // TODO 부서문서관리자에게 발송
          NotiConfig notiData = notiConfigDao.selectOneByCodes(reqComCode, "PR");
          if("Y".equals(notiData.getUAlarmYn())) {
            idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
            idf_PObj2.setString("u_msg_type", "PR");
            idf_PObj2.setString("u_sender_id", userSession.getDUserId());
            idf_PObj2.setString("u_receiver_id", mgrId);
            idf_PObj2.setString("u_msg", "'" + reqDoc.getTitle() + "'" + " 문서의 조회/다운로드 권한요청이 등록되었습니다.");
            idf_PObj2.setString("u_obj_id", idf_PObj.getString("r_object_id"));
            idf_PObj2.setString("u_sent_date", new DfTime().toString());
            idf_PObj2.setString("u_action_need_yn", "Y");
            idf_PObj2.setInt("u_group_key", 1);
            idf_PObj2.save();
          } 
          if("Y".equals(notiData.getUEmailYn())) {      
            StringBuffer content = new StringBuffer();       
            content.append("<html> ");   
            content.append("<body>");           
            content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");           
            content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; "+reqDoc.getTitle()+" 문서의 조회/다운로드 권한요청이 등록되었습니다.</font>");             
            content.append("<br><br>                         <font face='굴림' size=3>     <a href='"+  dboxUrl  +"/#/manager/dept/auth-manage?tab=0'>승인화면 바로가기</a></font>");
            content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>"); 
            content.append(" </body></html>");
            notificationService.sendMail("dbox@dongkuk.com", mgrsEmail, "[D'Box]" + "'" + reqDoc.getObjectName() + "'" + " 문서의 조회/다운로드 권한요청이 등록되었습니다.", new String(content));            
            
          }
          if("Y".equals(notiData.getUMmsYn())) {           
              String mobileTel = reqMgr.getMobileTel().replace("-", "");
              notificationService.sendKakao(mgrId, mobileTel, "dbox_alarm_010", reqDoc.getTitle() + " 문서의 권한요청이 " + "등록되었습니다.");      
          }          
        }
      
      } catch (Exception e) {
        throw new Exception("부서문서관리장 이메일이 없습니다.");
      }
      LogDoc logDoc = LogDoc.builder()
             .uJobCode(DocLogItem.PR.getValue())
             .uDocId(reqDoc.getRObjectId())
             .uDocKey(reqDoc.getUDocKey())
             .uDocName(reqDoc.getTitle())
             .uDocVersion(docV2.get(0).getRVersionLabel())
             .uOwnDeptcode(reqDeptCode)
             .uActDeptCode(reqDeptCode)
             .uFileSize(Integer.valueOf(reqDoc.getRContentSize()))
             .uJobUser(userSession.getDUserId())
             .uJobUserType("P").uDocStatus(reqDoc.getUDocStatus())
             .uSecLevel(reqDoc.getUSecLevel())
             .uCabinetCode(reqDoc.getUCabinetCode())
             .uJobGubun("")
             .uUserIp(ip)
             .build();  
      insertLog(logDoc);
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
  public String approveReqAuth(String uReqDocId, UserSession userSession, String ip)
      throws Exception {
    // uReqDocId = rObjectId
    // docId 불러와서 넣기
    ReqAuth reqData = reqAuthDao.dataByObjId(uReqDocId);
    Doc reqDoc = docDao.selectOne(reqData.getUReqDocId())
        .orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
    String cabinetCode = reqDoc.getUCabinetCode();
    String reqDeptCode = deptDao.selectOrgIdByCabinetcode(cabinetCode);
    String reqComeCode = deptDao.selectComCodeByCabinetCode(cabinetCode);
    List<DocRepeating> docV2 = docDao.selRepeatingOne(reqDoc.getRObjectId());
    IDfSession idfSession = this.getIdfSession(userSession);
    IDfPersistentObject idf_PObj = null;
    IDfPersistentObject idf_PObj3 = null;
    IDfSession adSess = null;
    VUser reqUser = userService.selectOneByUserId(reqData.getUReqUser()).orElse(null);
    List<String> reciever = new ArrayList<>();
    reciever.add(reqUser.getEmail());
  
    idfSession.beginTrans();
    try {

      if (idfSession == null || !idfSession.isConnected()) {
        throw new Exception("DCTM Session 가져오기 실패");
      }
      adSess = DCTMUtils.getAdminSession();
      if (!adSess.isTransactionActive()) {
       adSess.beginTrans();
      }

      idf_PObj = ReqApproveDto.approveReqAuth(uReqDocId, idfSession, userSession);
      idf_PObj.save();
      
      DCTMUtils.setAllDocACL(adSess, reqData.getUReqDocId());
      IDfPersistentObject idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_auth_base");
      idf_PObj2.setString("u_obj_id", reqData.getUReqDocId());
      idf_PObj2.setString("u_obj_type", "D");
      idf_PObj2.setString("u_doc_status", reqDoc.getUDocStatus());
        if(reqData.getUReqPermit() == 3) {
          idf_PObj2.setString("u_permit_type", "R");
        }else if(reqData.getUReqPermit() == 7) {
          idf_PObj2.setString("u_permit_type", "D");
        }

      if (reqData.getUReqDeptCode().equals(reqData.getUOwnDeptCode())) {
        idf_PObj2.setString("u_own_dept_yn", "Y");
      } else if (!(reqData.getUReqDeptCode().equals(reqData.getUOwnDeptCode()))) {
        idf_PObj2.setString("u_own_dept_yn", "N");
      }
      idf_PObj2.setString("u_author_id", reqData.getUReqUser());
      idf_PObj2.setString("u_author_type", "U");
      idf_PObj2.setString("u_create_user", userSession.getDUserId());
      idf_PObj2.setString("u_create_date", (new DfTime()).toString());
      idf_PObj2.save();   
      
      IDfDocument idf_Doc = (IDfDocument) adSess.getObject(new DfId(reqData.getUReqDocId()));
      if(reqData.getUReqDocId() != null) {
        idf_Doc.grant(reqData.getUReqUser(), reqData.getUReqPermit(), "");
        idf_Doc.save();    
      }

      LogDoc logDoc = LogDoc.builder()
            .uJobCode(DocLogItem.PA.getValue())
            .uDocId(reqDoc.getRObjectId())
            .uDocKey(reqDoc.getUDocKey())
            .uDocName(reqDoc.getTitle())
            .uDocVersion(docV2.get(0).getRVersionLabel())
            .uOwnDeptcode(reqDeptCode)
            .uActDeptCode(reqDeptCode)
            .uFileSize(Integer.valueOf(reqDoc.getRContentSize()))
            .uJobUser(userSession.getDUserId())
            .uJobUserType("D").uDocStatus(reqDoc.getUDocStatus())
            .uSecLevel(reqDoc.getUSecLevel())
            .uCabinetCode(reqDoc.getUCabinetCode())
            .uJobGubun("")
            .uUserIp(ip)
            .build();
      insertLog(logDoc);
      
      List<Alarm> alarmData = alarmDao.selectOneByObjId(uReqDocId, reqData.getUReqUser(), "PR");
      for(int i=0;i<alarmData.size(); i++) {
        idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(alarmData.get(i).getRObjectId()));
        idf_PObj3.setString("u_action_yn", "Y");
        idf_PObj3.setString("u_action_date", new DfTime().toString());
        idf_PObj3.save();
      }
     // TODO: 요청자에게 알림, 이메일 보내기
     // TODO 알림 insert
     NotiConfig notiData = notiConfigDao.selectOneByCodes(reqComeCode, "PR");
      if("Y".equals(notiData.getUAlarmYn())) {
         idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
         idf_PObj2.setString("u_msg_type", "PR");
         idf_PObj2.setString("u_sender_id", userSession.getDUserId());
         idf_PObj2.setString("u_receiver_id", reqData.getUReqUser());
         idf_PObj2.setString("u_performer_id", userSession.getDUserId());
         idf_PObj2.setString("u_action_yn", "Y");
         idf_PObj2.setString("u_action_need_yn", "N");
         idf_PObj2.setString("u_msg", "'" + reqDoc.getTitle() + "'" + " 문서의 조회/다운로드 권한요청이 승인되었습니다.");
         idf_PObj2.setString("u_obj_id", uReqDocId);
         idf_PObj2.setString("u_action_date", new DfTime().toString());
         idf_PObj2.setString("u_sent_date", new DfTime().toString());
         idf_PObj2.save();
      }
      if("Y".equals(notiData.getUEmailYn())) {
        StringBuffer content = new StringBuffer();       
        content.append("<html> ");   
        content.append("<body>");           
        content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");           
        content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; "+reqDoc.getTitle()+" 문서의 조회/다운로드 권한요청이 승인되었습니다.</font>");             
        content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>"); 
        content.append(" </body></html>");

        notificationService.sendMail("dbox@dongkuk.com", reciever, "[D'Box]" + "'" + reqDoc.getTitle() + "'" + " 문서의 조회/다운로드 권한요청이 승인되었습니다."
        , content.toString());
      }
      if("Y".equals(notiData.getUMmsYn())) {
          String mobileTel = reqUser.getMobileTel().replace("-", "");
          notificationService.sendKakao(reqData.getUReqUser(), mobileTel, "dbox_alarm_011", reqDoc.getTitle() +" 문서의 권한요청이"+" 승인"+"되었습니다.");               
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
  public Map<String, Integer> approveAllReqAuth(List<String> authRequestIdList, UserSession userSession,
      ReqApproveDto reqApproveDto, String ip) throws Exception {
    IDfSession idfSession = this.getIdfSession(userSession);
    IDfSession adSess = null;
    IDfPersistentObject idf_PObj = null;
    IDfPersistentObject idf_PObj3 = null;
    
    ReqAuth reqData = null;
    Doc reqDoc = null;
    String cabinetCode = null;
    String reqDeptCode = null;
    List<DocRepeating> docV2 = null;
    String reqComeCode = null;

    int successCnt = 0;
    int failCnt = 0;

    try {
      idfSession.beginTrans();
      for (String objId : authRequestIdList) {
        try {
          if (idfSession == null || !idfSession.isConnected()) {
            throw new Exception("DCTM Session 가져오기 실패");
          }
          reqData = reqAuthDao.dataByObjId(objId);
          reqDoc = docDao.selectOne(reqData.getUReqDocId())
              .orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
          cabinetCode = reqDoc.getUCabinetCode();
          reqComeCode = deptDao.selectComCodeByCabinetCode(cabinetCode);
          reqDeptCode = deptDao.selectOrgIdByCabinetcode(cabinetCode);
          docV2 = docDao.selRepeatingOne(reqDoc.getRObjectId());
          VUser reqUser = userService.selectOneByUserId(reqData.getUReqUser()).orElse(null);
          List<String> reciever = new ArrayList<>();
          reciever.add(reqUser.getUserId());

          idf_PObj = ReqApproveDto.approveReqAuth(objId, idfSession, userSession);
          idf_PObj.save();

          IDfPersistentObject idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_auth_base");
          idf_PObj2.setString("u_obj_id", reqData.getUReqDocId());
          idf_PObj2.setString("u_obj_type", "D");
          idf_PObj2.setString("u_doc_status", reqDoc.getUDocStatus());
          if(idf_PObj.getInt("u_req_permit") == 3) {
            idf_PObj2.setString("u_permit_type", "R");
          }else if(idf_PObj.getInt("u_req_permit") == 7) {
            idf_PObj2.setString("u_permit_type", "D");
          }
//          코드 간결하게.
//          idf_PObj2.setString("u_permit_type", GrantedLevels.findLabelByLevel(idf_PObj.getInt("u_req_permit")));
          if (reqData.getUReqDeptCode().equals(reqData.getUOwnDeptCode())) {
            idf_PObj2.setString("u_own_dept_yn", "Y");
          } else if (!(reqData.getUReqDeptCode().equals(reqData.getUOwnDeptCode()))) {
            idf_PObj2.setString("u_own_dept_yn", "N");
          }
          idf_PObj2.setString("u_author_id", reqData.getUReqUser());
          idf_PObj2.setString("u_author_type", "U");
          idf_PObj2.setString("u_create_user", userSession.getDUserId());
          idf_PObj2.setString("u_create_date", (new DfTime()).toString());
          idf_PObj2.save();
          adSess = DCTMUtils.getAdminSession();
            if (!adSess.isTransactionActive()) {
             adSess.beginTrans();
            }
          // TODO 권한 추가
          IDfDocument idf_Doc = (IDfDocument) adSess.getObject(new DfId(reqDoc.getRObjectId()));
          idf_Doc.grant(reqData.getUReqUser(), idf_PObj.getInt("u_req_permit"), null);
          idf_Doc.save();
          LogDoc logDoc = LogDoc.builder()
                .uJobCode(DocLogItem.PA.getValue())
                .uDocId(reqDoc.getRObjectId())
                .uDocKey(reqDoc.getUDocKey())
                .uDocName(reqDoc.getTitle())
                .uDocVersion(docV2.get(0).getRVersionLabel())
                .uOwnDeptcode(reqDeptCode)
                .uActDeptCode(reqDeptCode)
                .uFileSize(Integer.valueOf(reqDoc.getRContentSize()))
                .uJobUser(userSession.getDUserId())
                .uJobUserType("D").uDocStatus(reqDoc.getUDocStatus())
                .uSecLevel(reqDoc.getUSecLevel())
                .uCabinetCode(reqDoc.getUCabinetCode())
                .uJobGubun("")
                .uUserIp(ip)
                .build();
          insertLog(logDoc);
          if ("A".equals(idf_PObj.getString("u_req_status"))) {
            successCnt++;
              
            List<Alarm> alarmData = alarmDao.selectOneByObjId(objId, reqData.getUReqUser(), "PR");
            for(int i=0;i<alarmData.size(); i++) {
              idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(alarmData.get(i).getRObjectId()));
              idf_PObj3.setString("u_action_yn", "Y");
              idf_PObj3.setString("u_action_date", new DfTime().toString());
              idf_PObj3.save();
            }
             // TODO 알림 insert
             NotiConfig notiData = notiConfigDao.selectOneByCodes(reqComeCode, "PR");
              if("Y".equals(notiData.getUAlarmYn())) {
                 idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
                 idf_PObj2.setString("u_msg_type", "PR");
                 idf_PObj2.setString("u_sender_id", userSession.getDUserId());
                 idf_PObj2.setString("u_receiver_id", reqData.getUReqUser());
                 idf_PObj2.setString("u_performer_id", userSession.getDUserId());
                 idf_PObj2.setString("u_action_yn", "Y");
                 idf_PObj2.setString("u_action_need_yn", "N");
                 idf_PObj2.setString("u_msg", "'" + reqDoc.getTitle() + "'" + " 문서의 조회/다운로드 권한요청이 승인되었습니다.");
                 idf_PObj2.setString("u_obj_id", objId);
                 idf_PObj2.setString("u_sent_date", new DfTime().toString());
                 idf_PObj2.save();
              }
              if("Y".equals(notiData.getUEmailYn())) {
                notificationService.sendMail("dbox@dongkuk.com", reciever, "[D'Box]" + "'" + reqDoc.getTitle() + "'" + " 문서의 조회/다운로드 권한요청이 승인되었습니다.",
                    "'" + reqDoc.getObjectName() + "'" + " 문서의 조회/다운로드 권한요청이 승인되었습니다.");
              }
              if("Y".equals(notiData.getUMmsYn())) {
                  String mobileTel = reqUser.getMobileTel().replace("-", "");
                  notificationService.sendKakao(reqData.getUReqUser(), mobileTel, "dbox_alarm_011", reqDoc.getTitle() + " 문서의 권한요청이"+" 승인"+"되었습니다.");               
              }         
          } 
          else {
            failCnt++;
            continue;
          }

        } catch (Exception e) {
          failCnt++;
          continue;
        } finally {
        }
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
        if (idfSession.isConnected()) {
          sessionRelease(userSession.getUser().getUserId(), idfSession);
        }
      }
    }
    Map<String, Integer> result = new HashMap<String, Integer>();
    result.put("success", successCnt);
    result.put("fail", failCnt);

    return result;
  }

  @Override
  public String rejectReqAuth(String authRequestId, UserSession userSession, String uRejectReason)
      throws Exception {
    IDfSession idfSession = this.getIdfSession(userSession);
    ReqAuth reqData = reqAuthDao.dataByObjId(authRequestId);
    Doc reqDoc = docDao.selectOne(reqData.getUReqDocId())
        .orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
    String cabinetCode = reqDoc.getUCabinetCode();
    String reqComeCode = deptDao.selectComCodeByCabinetCode(cabinetCode);
    VUser reqUser = userService.selectOneByUserId(reqData.getUReqUser()).orElse(null);
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

      idf_PObj = ReqRejectDto.rejectReqAuth(authRequestId, idfSession, uRejectReason, userSession);
      idf_PObj.save();
      
      List<Alarm> alarmData = alarmDao.selectOneByObjId(authRequestId, reqData.getUReqUser(), "PR");
      for(int i=0;i<alarmData.size(); i++) {
        idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(alarmData.get(i).getRObjectId()));
        idf_PObj3.setString("u_action_yn", "N");
        idf_PObj3.setString("u_action_date", new DfTime().toString());
        idf_PObj3.save();
      }
      
      NotiConfig notiData = notiConfigDao.selectOneByCodes(reqComeCode, "PR");
      if("Y".equals(notiData.getUAlarmYn())) {
         idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
         idf_PObj2.setString("u_msg_type", "PR");
         idf_PObj2.setString("u_sender_id", userSession.getDUserId());
         idf_PObj2.setString("u_receiver_id", reqData.getUReqUser());
         idf_PObj2.setString("u_performer_id", userSession.getDUserId());
         idf_PObj2.setString("u_action_yn", "N");
         idf_PObj2.setString("u_action_need_yn", "N");
         idf_PObj2.setString("u_msg", "'" + reqDoc.getTitle() + "'" + " 문서의 조회/다운로드 권한요청이 반려되었습니다.");
         idf_PObj2.setString("u_obj_id", authRequestId);
         idf_PObj2.setString("u_action_date", new DfTime().toString());
         idf_PObj2.setString("u_sent_date", new DfTime().toString());
         idf_PObj2.save();
      }
      if("Y".equals(notiData.getUEmailYn())) {
        StringBuffer content = new StringBuffer();       
        content.append("<html> ");   
        content.append("<body>");           
        content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");           
        content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; "+reqDoc.getTitle()+" 문서의 조회/다운로드 권한요청이 반려되었습니다.</font>");             
        content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>"); 
        content.append(" </body></html>");          
        notificationService.sendMail("dbox@dongkuk.com", reciever, "[D'Box]" + "'" + reqDoc.getTitle() + "'" + " 문서의 조회/다운로드 권한요청이 반려되었습니다."
        , content.toString());
      }
      if("Y".equals(notiData.getUMmsYn())) {
        String mobileTel = reqUser.getMobileTel().replace("-", "");
        notificationService.sendKakao(reqData.getUReqUser(), mobileTel, "dbox_alarm_011", reqDoc.getTitle() + " 문서의 권한요청이"+" 반려"+"되었습니다.");               
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
  public Map<String, Integer> rejectAllReqAuth(List<String> authRequestIdList, UserSession userSession,
      String uRejectReason) throws Exception {
    IDfSession idfSession = this.getIdfSession(userSession);
    IDfPersistentObject idf_PObj = null;
    IDfPersistentObject idf_PObj2 = null;
    IDfPersistentObject idf_PObj3 = null;

    ReqAuth reqData = null;
    Doc reqDoc = null;
    String cabinetCode = null;
    String reqComeCode = null;
    VUser reqUser = null;
    List<String> reciever = null;

    int successCnt = 0;
    int failCnt = 0;

    try {
      idfSession.beginTrans();

      if (idfSession == null || !idfSession.isConnected()) {
        throw new Exception("DCTM Session 가져오기 실패");
      }

      for (String objId : authRequestIdList) {
        try {
          if (idfSession == null || !idfSession.isConnected()) {
            throw new Exception("DCTM Session 가져오기 실패");
          }
          reqData = reqAuthDao.dataByObjId(objId);
          reqDoc = docDao.selectOne(reqData.getUReqDocId())
              .orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
          cabinetCode = reqDoc.getUCabinetCode();
          reqComeCode = deptDao.selectComCodeByCabinetCode(cabinetCode);
          reqUser = userService.selectOneByUserId(reqData.getUReqUser()).orElse(null);
          reciever = new ArrayList<>();
          reciever.add(reqUser.getEmail());
          idf_PObj = ReqRejectDto.rejectReqAuth(objId, idfSession, uRejectReason, userSession);
          idf_PObj.save();
          
          if ("D".equals(idf_PObj.getString("u_req_status"))) {
            successCnt++;
            
            List<Alarm> alarmData = alarmDao.selectOneByObjId(objId, reqData.getUReqUser(), "PR");
            for(int i=0;i<alarmData.size(); i++) {
              idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(alarmData.get(i).getRObjectId()));
              idf_PObj3.setString("u_action_yn", "N");
              idf_PObj3.setString("u_action_date", new DfTime().toString());
              idf_PObj3.save();
            }
            
            NotiConfig notiData = notiConfigDao.selectOneByCodes(reqComeCode, "PR");
            if("Y".equals(notiData.getUAlarmYn())) {
               idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
               idf_PObj2.setString("u_msg_type", "PR");
               idf_PObj2.setString("u_sender_id", userSession.getDUserId());
               idf_PObj2.setString("u_receiver_id", reqData.getUReqUser());
               idf_PObj2.setString("u_performer_id", userSession.getDUserId());
               idf_PObj2.setString("u_action_yn", "N");
               idf_PObj2.setString("u_action_need_yn", "N");
               idf_PObj2.setString("u_msg", "'" + reqDoc.getTitle() + "'" + " 문서의 조회/다운로드 권한요청이 반려되었습니다.");
               idf_PObj2.setString("u_obj_id", objId);
               idf_PObj2.setString("u_action_date", new DfTime().toString());
               idf_PObj2.save();
            }
            if("Y".equals(notiData.getUEmailYn())) {
              StringBuffer content = new StringBuffer();       
              content.append("<html> ");   
              content.append("<body>");           
              content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");           
              content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; "+reqDoc.getTitle()+" 문서의 조회/다운로드 권한요청이 반려되었습니다.</font>");             
              content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>"); 
              content.append(" </body></html>");          
              notificationService.sendMail("dbox@dongkuk.com", reciever, "[D'Box]" + "'" + reqDoc.getTitle() + "'" + " 문서의 조회/다운로드 권한요청이 반려되었습니다."
              , content.toString());
            }
            if("Y".equals(notiData.getUMmsYn())) {
              String mobileTel = reqUser.getMobileTel().replace("-", "");
              notificationService.sendKakao(reqData.getUReqUser(), mobileTel, "dbox_alarm_011", reqDoc.getTitle() + " 문서의 권한요청이"+" 반려"+"되었습니다.");               
            }         
          } else {
            failCnt++;
            continue;
          }

        } catch (Exception e) {
          failCnt++;
          continue;
        } finally {
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
    Map<String, Integer> result = new HashMap<String, Integer>();
    result.put("success", successCnt);
    result.put("fail", failCnt);
    
    
    
    return result;
  }
  
}
