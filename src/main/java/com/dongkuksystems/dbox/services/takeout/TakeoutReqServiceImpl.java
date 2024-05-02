package com.dongkuksystems.dbox.services.takeout;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.AgreeType;
import com.dongkuksystems.dbox.constants.DocLogItem;
import com.dongkuksystems.dbox.constants.GrantedLevels;
import com.dongkuksystems.dbox.constants.TakeoutType;
import com.dongkuksystems.dbox.daos.table.etc.gwdept.GwDeptDao;
import com.dongkuksystems.dbox.daos.table.takeout.ReqTakeoutDao;
import com.dongkuksystems.dbox.daos.type.agree.AgreeDao;
import com.dongkuksystems.dbox.daos.type.doc.DocDao;
import com.dongkuksystems.dbox.daos.type.manager.deptMgr.DeptMgrDao;
import com.dongkuksystems.dbox.daos.type.manager.noticonfig.NotiConfigDao;
import com.dongkuksystems.dbox.daos.type.noti.AlarmDao;
import com.dongkuksystems.dbox.errors.BadRequestException;
import com.dongkuksystems.dbox.errors.ForbiddenException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.agree.AgreeFilter;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutApproveDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutConfigDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutCreateDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutDetailDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutRejectDto;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.table.log.LogDoc;
import com.dongkuksystems.dbox.models.type.agree.Agree;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.doc.DocRepeating;
import com.dongkuksystems.dbox.models.type.manager.TakeoutConfig;
import com.dongkuksystems.dbox.models.type.manager.TakeoutConfigRepeating;
import com.dongkuksystems.dbox.models.type.manager.deptMgr.DeptMgrs;
import com.dongkuksystems.dbox.models.type.manager.noticonfig.NotiConfig;
import com.dongkuksystems.dbox.models.type.noti.Alarm;
import com.dongkuksystems.dbox.models.type.request.ReqTakeout;
import com.dongkuksystems.dbox.models.type.request.ReqTakeoutDoc;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.auth.AuthService;
import com.dongkuksystems.dbox.services.notification.NotificationService;
import com.dongkuksystems.dbox.services.user.UserService;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

@Service
public class TakeoutReqServiceImpl extends AbstractCommonService implements TakeoutReqService {
  
  @Value("${dbox.url}")
  private String dboxUrl;
  
  private final ReqTakeoutDao reqTakeoutDao;
  private final DocDao docDao;
  private final GwDeptDao deptDao;
  private final DeptMgrDao deptMgrDao;
  private final UserService userService;
  private final AuthService authService;
  private final AgreeDao agreeDao;
  private final NotificationService notificationService;
  private final NotiConfigDao notiConfigDao;
  private final AlarmDao alarmDao;

  public TakeoutReqServiceImpl(ReqTakeoutDao reqTakeoutDao, DocDao docDao,
      DeptMgrDao deptMgrDao, UserService userService, AuthService authService, AgreeDao agreeDao,
      NotificationService notificationService, GwDeptDao deptDao, NotiConfigDao notiConfigDao, AlarmDao alarmDao) {    
    this.reqTakeoutDao = reqTakeoutDao;
    this.docDao = docDao;
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
  public TakeoutConfig seletOneByDeptCode(String deptCode, String mode) throws Exception {
    
    TakeoutConfig result = null;
    
    if(mode.equals("M")) {
      result = reqTakeoutDao.selectReasonOneByDeptCode(deptCode).orElse(null);
      if (result != null) {
        List<TakeoutConfigRepeating> rp = reqTakeoutDao.selectRepeatingList(result.getRObjectId());
        result.setTakeoutConfigRepeatings(rp);
      }
      
    }else {
      result = reqTakeoutDao.selectReasonOneByDeptCode(deptCode).orElse(null);
      if (result != null) {
        if (result.getAutoNames() == null) result.setAutoNames(reqTakeoutDao.selectAuthNameListByObjId(result.getRObjectId()));
        if (result.getFreeNames() == null) result.setFreeNames(reqTakeoutDao.selectFreeNameListByObjId(result.getRObjectId()));
      }
    }
    
    return result; 
  }

  @Override
  public List<ReqTakeoutDoc> selectOneByReqId(String reqId) throws Exception {

    return reqTakeoutDao.selectOneByReqId(reqId);
  }

  @Override
  public List<ReqTakeoutConfigDto> nameListByDeptCode(String orgId) throws Exception {
    // TODO Auto-generated method stub
    return reqTakeoutDao.nameListByOrgId(orgId);
  }

  @Override
  public List<ReqTakeoutDetailDto> takeoutDetailList() throws Exception {

    List<ReqTakeoutDetailDto> reqTakeoutDetailList = reqTakeoutDao.selectReqTakeoutDetails(null);

    return reqTakeoutDetailList;
  }

  @Override
  public List<ReqTakeoutDetailDto> takeoutDetailListByObjId(String takeoutRequestId) throws Exception {

    List<ReqTakeoutDetailDto> reqTakeoutDetailList = reqTakeoutDao.reqTakeoutListByObjId(takeoutRequestId);

    return reqTakeoutDetailList;
  }

  @Override
  public List<ReqTakeoutDetailDto> takeoutDetailList(ReqTakeout takeout) throws Exception {
    List<ReqTakeoutDetailDto> reqTakeoutDetailList = reqTakeoutDao.selectReqTakeoutDetails(takeout);
    LocalDateTime today = LocalDateTime.now();
    final ModelMapper modelMapper = getModelMapper();
    reqTakeoutDetailList.stream().filter(item -> {
      ReqTakeoutDetailDto reqTakeoutDetailDto = modelMapper.map(item, ReqTakeoutDetailDto.class);
      
      if("D".equals(reqTakeoutDetailDto.getULimitFlag()) && reqTakeoutDetailDto.getULimitDate().compareTo(today) < 0) 
        return false;
      else if("U".equals(reqTakeoutDetailDto.getULimitFlag()))
        return false;    
      else 
        return true;
    }).collect(Collectors.toList());
    return reqTakeoutDetailList;
  }

  // 복호화반출 --> 동국제강: 알림/이메일, 인터지스: 알림/이메일/카톡, 동국시스템즈: 알림/이메일, 페럼인프라: 알림/이메일
  @Override
  public String createReqTakeout(UserSession userSession, ReqTakeoutCreateDto takeoutCreateDto, List<String> docIdList, String ip) throws Exception {
    IDfSession idfSession = this.getIdfSession(userSession);

//    List<ReqTakeoutConfigDto> autoReasonList = null;
    List<Agree> agree = null;
    TakeoutConfig takoutConfig = null;
    // 부서관리장 이메일 받아오기
    //MailSenderUtils mail = new MailSenderUtils();
    IDfPersistentObject idf_PObj = null;
    IDfPersistentObject idf_PObj2 = null;
    IDfPersistentObject idf_PObj3 = null;
    IDfPersistentObject idf_Doc2 = null;
    IDfDocument idf_Doc = null;
    IDfSession adSess = null;
    boolean isReasonIn = false;
    boolean isAgree = false;
    try {

      idfSession.beginTrans();
      
        Doc reqDoc = docDao.selectOne(docIdList.get(0))
            .orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
        VUser userData = userSession.getUser();
        String cabinetCode = reqDoc.getUCabinetCode();
        String reqDeptCode = deptDao.selectOrgIdByCabinetcode(cabinetCode);
        DeptMgrs deptMgr = deptMgrDao.kingByDeptCode(reqDeptCode);
        
        List<DocRepeating> docV2 = docDao.selRepeatingOne(reqDoc.getRObjectId());
        // 부서코드를 이용해 회사코드 가져오기
        String reqComeCode = deptDao.selectComCodeByCabinetCode(cabinetCode);
        for(String docId: docIdList) {
          Doc reqDocs = docDao.selectOne(docId)
              .orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
          Boolean isDocAuth = authService.checkDocAuth(docId, userSession.getDUserId(),
              GrantedLevels.READ.getLevel());
          if (!(isDocAuth)) {
            throw new ForbiddenException("권한이 없습니다.");
          }
          if ("C".equals(reqDocs.getUDocStatus()) && "S".equals(reqDocs.getUSecLevel())) {
            throw new ForbiddenException("제한 등급입니다.");
          }
        }
       
 //       takoutConfig = reqTakeoutDao.selectReasonOneByDeptCode(userData.getOrgId()).orElseThrow(() -> new RuntimeException("해당 부서에 반출 설정이 없습니다."));

        switch (TakeoutType.findByValue(takeoutCreateDto.getUApprType())) {
        case PRE:
          if (idfSession == null || !idfSession.isConnected()) {
            throw new RuntimeException("DCTM Session 가져오기 실패");
          }
          
          // 수정 완료.
          adSess = DCTMUtils.getAdminSession();
          if (!adSess.isTransactionActive()) {
            adSess.beginTrans();
          }
          idf_PObj = ReqTakeoutCreateDto.CreateReqTakeout(idfSession, takeoutCreateDto);
          if(docIdList.size() > 1) {
            idf_PObj.setString("u_req_title", reqDoc.getObjectName()+" 외 "+(docIdList.size()-1)+"건");
          }else {
            idf_PObj.setString("u_req_title", reqDoc.getObjectName());
          }        
          idf_PObj.setString("u_own_dept_code", reqDeptCode);
          idf_PObj.setString("u_req_dept_code", userData.getOrgId());
          idf_PObj.save();
          for(String docId: docIdList) {
            Doc reqDocs = docDao.selectOne(docId).orElse(new Doc());
            idf_Doc2 = (IDfPersistentObject) idfSession.newObject("edms_req_takeout_doc");
            idf_Doc2.setString("u_req_id", idf_PObj.getString("r_object_id"));
            idf_Doc2.setString("u_req_doc_id", docId);
            idf_Doc2.setString("u_req_doc_key", reqDocs.getUDocKey());
            idf_Doc2.save();
          
          }
          LogDoc logDoc = LogDoc.builder()
                .uJobCode(DocLogItem.ER.getValue())
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
          try {
            if(null != deptMgr.getRObjectId()) {
              for(int i=0;i<docIdList.size();i++) {
                String deptMgrId = deptMgr.getUUserId();
                VUser deptMgrData = userService.selectOneByUserId(deptMgrId).orElse(new VUser());
                List<String> deptMgrEmail = new ArrayList<>();
                deptMgrEmail.add(deptMgrData.getEmail());
                Doc reqDocData = docDao.selectOne(docIdList.get(i)).orElse(new Doc());
                NotiConfig notiData = notiConfigDao.selectOneByCodes(reqComeCode, "ER");
                if("Y".equals(notiData.getUAlarmYn())) {             
                  idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
                  idf_PObj2.setString("u_msg_type", "ER");
                  idf_PObj2.setString("u_sender_id", userSession.getDUserId());
                  idf_PObj2.setString("u_receiver_id", deptMgrId);
                  idf_PObj2.setString("u_msg", "'" + reqDocData.getTitle() + "'" + " 문서의 복호화반출이 사전승인 요청되었습니다.");
                  idf_PObj2.setString("u_obj_id", idf_PObj.getString("r_object_id"));
                  idf_PObj2.setString("u_sent_date", new DfTime().toString());
                  idf_PObj2.setString("u_action_need_yn", "Y");
                  idf_PObj2.save();
                } 
                if("Y".equals(notiData.getUEmailYn())) {
                  StringBuffer content = new StringBuffer();       
                  content.append("<html> ");   
                  content.append("<body>");           
                  content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");           
                  content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; "+reqDocData.getTitle()+" 문서의 복호화반출이 사전승인 요청되었습니다.</font>");  
                  content.append("<br><br>                         <font face='굴림' size=3>      <a href='"+  dboxUrl  +"/#/manager/dept/takeout-control?tab=1'>승인화면 바로가기</a></font>");
                  content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>"); 
                  content.append(" </body></html>");          
                  notificationService.sendMail("dbox@dongkuk.com", deptMgrEmail, "[D'Box]" + "'" + reqDocData.getTitle() + "'" + " 문서의 복호화반출이 사전승인 요청되었습니다."
                  , content.toString());
                }
                if("Y".equals(notiData.getUMmsYn())) {
                  String mobileTel = deptMgrData.getMobileTel().replace("-", "");
                  notificationService.sendKakao(deptMgrId, mobileTel, "dbox_alarm_006", reqDoc.getTitle() + " 문서의 복호화반출이 "+"사전승인"+" 요청되었습니다.");               
                } 
              }
            }
            
          }catch(Exception e){
            System.out.print("알림 error");
          }
  
          break;
        case AUTO:
          takoutConfig = reqTakeoutDao.selectReasonOneByDeptCode(userData.getOrgId()).orElseThrow(() -> new RuntimeException("해당 부서에 반출 설정이 없습니다."));
          idf_Doc2 = (IDfPersistentObject) idfSession.newObject("edms_req_takeout_doc");

          if ("C".equals(reqDoc.getUDocStatus()) && "S".equals(reqDoc.getUSecLevel())) {
            throw new ForbiddenException("제한 등급입니다.");
          }
          for (String str: takoutConfig.getAutoNames()) {
            if (takeoutCreateDto.getUReqReason().equals(str)) {
              isReasonIn = true;
              break;
            }
          }
          if (!isReasonIn) {
            throw new RuntimeException("일치하는 자동승인 사유가 없습니다.");
          }

          agree = agreeDao.selectList(AgreeFilter.builder().uDeptCode(reqDeptCode).uAgreeType(AgreeType.AUTO.getValue()).build());
          if (agree == null) {
            new BadRequestException("사용자에 대한 동의 정보가 존재하지압습니다.");
          }
          for (Agree ag : agree) {
            if (AgreeType.AUTO.getValue().equals(ag.getUAgreeType())) {
              if ("Y".equals(ag.getUAgreeYn())) {
                isAgree = true;
                break;
              }
            }
          }
          if (!isAgree) {
            new BadRequestException("해당 부서장이 자동승인에 대한 서약서에 동의해야합니다.");
          }
          idf_PObj = ReqTakeoutCreateDto.CreateReqTakeoutFree(idfSession, takeoutCreateDto, userSession);
          if(docIdList.size() > 1) {
            idf_PObj.setString("u_req_title", reqDoc.getTitle()+" 외 "+(docIdList.size()-1)+"건");
          }else {
            idf_PObj.setString("u_req_title", reqDoc.getTitle());
          }       
          idf_PObj.setString("u_own_dept_code", reqDeptCode);
          idf_PObj.setString("u_req_reason", takeoutCreateDto.getUReqReason());
          idf_PObj.setString("u_req_dept_code", userData.getOrgId());
          idf_PObj.setString("u_req_status", "A");
          idf_PObj.setString("u_appr_type", "A");

          // TODO M:메일발송 후 삭제, D:지정일 삭제, U:삭제안함
          if ("M".equals(takoutConfig.getUDeleteOption())) {
            idf_PObj.setString("u_limit_flag", "M");
            idf_PObj.setString("u_limit_date", null);
          } else if ("D".equals(takoutConfig.getUDeleteOption())) {
            // delDate 날짜 계산 에러 수정 by ddoc4.
            LocalDateTime now = LocalDateTime.now(); // 현재시간
            LocalDateTime delDate = now.plusDays(takoutConfig.getUDeleteDays());
            String formattedDelDate = delDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59"));

            idf_PObj.setString("u_limit_flag", "D");
            idf_PObj.setString("u_limit_date", formattedDelDate);
          } else if ("U".equals(takoutConfig.getUDeleteOption())) {
            idf_PObj.setString("u_limit_flag", "U");
            idf_PObj.setString("u_limit_date", null);
          }
          idf_PObj.save();

          adSess = DCTMUtils.getAdminSession();
          if (!adSess.isTransactionActive()) {
            adSess.beginTrans();
          }
          
          for(String docId: docIdList) {
            Doc reqDocs = docDao.selectOne(docId).orElse(new Doc());
            
            if(reqDocs.getRLockOwner() != null && !reqDocs.getRLockOwner().equals(" ")) {
              throw new RuntimeException("편집 중인 문서는 반출 승인처리를 할 수 없습니다.");
            }
            idf_Doc = (IDfDocument) adSess.getObject(new DfId(docId));
            idf_Doc.setBoolean("u_takeout_flag", true);
            idf_Doc.setBoolean("u_ver_keep_flag", true);
            idf_Doc.save();
            
            idf_Doc2 = (IDfPersistentObject) idfSession.newObject("edms_req_takeout_doc");
            idf_Doc2.setString("u_req_id", idf_PObj.getString("r_object_id"));
            idf_Doc2.setString("u_req_doc_id", docId);
            idf_Doc2.setString("u_req_doc_key", reqDocs.getUDocKey());
            idf_Doc2.save();
            
          }       
          LogDoc logDoc2 = LogDoc.builder()
                .uJobCode(DocLogItem.EA.getValue())
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
          insertLog(logDoc2);
          try {
            if(null != deptMgr.getRObjectId()) {
              for(int i=0;i<docIdList.size();i++) {
                String deptMgrId = deptMgr.getUUserId();
                VUser deptMgrData = userService.selectOneByUserId(deptMgrId).orElse(new VUser());
                List<String> deptMgrEmail = new ArrayList<>();
                deptMgrEmail.add(deptMgrData.getEmail());
                Doc reqDocData = docDao.selectOne(docIdList.get(i)).orElse(new Doc());
                NotiConfig notiData = notiConfigDao.selectOneByCodes(reqComeCode, "ER");
                if("Y".equals(notiData.getUAlarmYn())) {
                  idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
                  idf_PObj2.setString("u_msg_type", "ER");
                  idf_PObj2.setString("u_sender_id", deptMgrId);
                  idf_PObj2.setString("u_receiver_id", userSession.getDUserId());
                  idf_PObj2.setString("u_msg", "'" + reqDocData.getTitle() + "'" + " 문서의 복호화반출이 자동승인 요쳥되었습니다.");
                  idf_PObj2.setString("u_obj_id", idf_PObj.getString("r_object_id"));
                  idf_PObj2.setString("u_sent_date", new DfTime().toString());
                  idf_PObj2.setString("u_action_date", new DfTime().toString());
                  idf_PObj2.setString("u_action_yn", "Y");
                  idf_PObj2.setString("u_action_need_yn", "N");
                  idf_PObj2.save();
                  
                  idf_PObj3 = (IDfPersistentObject) idfSession.newObject("edms_noti");
                  idf_PObj3.setString("u_msg_type", "ER");
                  idf_PObj3.setString("u_sender_id", userSession.getDUserId());
                  idf_PObj3.setString("u_receiver_id", deptMgrId);
                  idf_PObj3.setString("u_msg", "'" + reqDocData.getTitle() + "'" + " 문서의 복호화반출이 자동승인 요쳥되었습니다.");
                  idf_PObj3.setString("u_obj_id", idf_PObj.getString("r_object_id"));
                  idf_PObj3.setString("u_action_date", new DfTime().toString());
                  idf_PObj2.setString("u_sent_date", new DfTime().toString());
                  idf_PObj3.setString("u_action_yn", "Y");
                  idf_PObj3.setString("u_action_need_yn", "N");
                  idf_PObj3.save();
                } 
                if("Y".equals(notiData.getUEmailYn())) {
                  List<String> userEmail = new ArrayList<>();
                  userEmail.add(userSession.getUser().getEmail());
                  userEmail.add(deptMgrData.getEmail());
                  StringBuffer content = new StringBuffer();       
                  content.append("<html> ");   
                  content.append("<body>");           
                  content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");           
                  content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; "+reqDocData.getTitle()+" 문서의 복호화반출이 자동승인 요쳥되었습니다.</font>");  
                  content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>"); 
                  content.append(" </body></html>");          
                  notificationService.sendMail("dbox@dongkuk.com", userEmail, "[D'Box]" + "'" + reqDocData.getTitle() + "'" + " 문서의 복호화반출이 자동승인 요쳥되었습니다."
                  , content.toString());           
                }
                if("Y".equals(notiData.getUMmsYn())) {
                  String mobileTel = deptMgrData.getMobileTel().replace("-", "");
                  String mobileTel2 = userSession.getUser().getMobileTel().replace("-", "");
                  notificationService.sendKakao(deptMgrId, mobileTel, "dbox_alarm_006", reqDoc.getTitle() + " 문서의 복호화반출이 "+"자동승인"+" 요청되었습니다.");             
                  notificationService.sendKakao(userSession.getDUserId(), mobileTel2, "dbox_alarm_006", reqDoc.getTitle() + " 문서의 복호화반출이 "+"자동승인"+" 요청되었습니다.");             
                } 
              }
            }
          }catch(Exception e) {
            System.out.print("알림 error");
          }
          
          break;
        case FREE:
          takoutConfig = reqTakeoutDao.selectReasonOneByDeptCode(userData.getOrgId()).orElseThrow(() -> new RuntimeException("해당 부서에 반출 설정이 없습니다."));
          idf_Doc2 = (IDfPersistentObject) idfSession.newObject("edms_req_takeout_doc");
          for (String str: takoutConfig.getFreeNames()) {
            if (takeoutCreateDto.getUReqReason().equals(str)) {
              isReasonIn = true;
              break;
            }
          }
          if (!isReasonIn) {
            throw new RuntimeException("일치하는 자동승인 사유가 없습니다.");
          }

          agree = agreeDao.selectList(AgreeFilter.builder().uUserId(userData.getUserId())
              .uAgreeType(AgreeType.FREE.getValue()).uAgreeName(takeoutCreateDto.getUReqReason()).build());
          if (agree == null || agree.size() == 0) {
            new BadRequestException("사용자에 대한 동의 정보가 존재하지압습니다.");
          }
          for (Agree ag : agree) {
            if (AgreeType.FREE.getValue().equals(ag.getUAgreeType())) {
              if ("Y".equals(ag.getUAgreeYn())) {
                isAgree = true;
              }
            }
          }
          if (!isAgree) {
            new BadRequestException("프리패스 사용에 대한 서약서에 동의해야합니다.");
          }
          adSess = DCTMUtils.getAdminSession();
          if (!adSess.isTransactionActive()) {
            adSess.beginTrans();
          }
          idf_PObj = ReqTakeoutCreateDto.CreateReqTakeoutFree(idfSession, takeoutCreateDto, userSession);
          if(docIdList.size() > 1) {
            idf_PObj.setString("u_req_title", reqDoc.getObjectName()+" 외 "+(docIdList.size()-1)+"건");
          }else {
            idf_PObj.setString("u_req_title", reqDoc.getObjectName());
          }        
          idf_PObj.setString("u_own_dept_code", reqDeptCode);
          idf_PObj.setString("u_req_reason", takeoutCreateDto.getUReqReason());
          idf_PObj.setString("u_req_dept_code", userData.getOrgId());
          idf_PObj.setString("u_req_status", "A");
          idf_PObj.setString("u_appr_type", "F");
       // TODO M:메일발송 후 삭제, D:지정일 삭제, U:삭제안함
          if ("M".equals(takoutConfig.getUDeleteOption())) {
            idf_PObj.setString("u_limit_flag", "M");
            idf_PObj.setString("u_limit_date", null);
          } else if ("D".equals(takoutConfig.getUDeleteOption())) {
            LocalDateTime today = LocalDateTime.now();
            LocalDateTime newToday = today.plusDays(Integer.valueOf(takoutConfig.getUDeleteDays()));
            LocalDateTime delDate = LocalDateTime.of(newToday.getYear(), 
                                                     newToday.getMonth(), 
                                                     newToday.getDayOfMonth(), 
                                                     23, 59, 59, 0);
            idf_PObj.setString("u_limit_flag", "D");
            idf_PObj.setString("u_limit_date", delDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
          } else if ("U".equals(takoutConfig.getUDeleteOption())) {
            idf_PObj.setString("u_limit_flag", "U");
            idf_PObj.setString("u_limit_date", null);
          }
          idf_PObj.save();

          for(String docId: docIdList) {
            Doc reqDocs = docDao.selectOne(docId).orElse(new Doc());
         
            if(reqDocs.getRLockOwner() != null && !reqDocs.getRLockOwner().equals(" ")) {
              throw new RuntimeException("편집 중인 문서는 반출 승인처리를 할 수 없습니다.");
            }
            idf_Doc = (IDfDocument) adSess.getObject(new DfId(docId));
            idf_Doc.setBoolean("u_takeout_flag", true);
            idf_Doc.setBoolean("u_ver_keep_flag", true);
            idf_Doc.save();
            
            idf_Doc2 = (IDfPersistentObject) idfSession.newObject("edms_req_takeout_doc");
            idf_Doc2.setString("u_req_id", idf_PObj.getString("r_object_id"));
            idf_Doc2.setString("u_req_doc_id", docId);
            idf_Doc2.setString("u_req_doc_key", reqDocs.getUDocKey());
            idf_Doc2.save();
            
          }
          LogDoc logDoc3 = LogDoc.builder()
                .uJobCode(DocLogItem.EA.getValue())
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
          insertLog(logDoc3);
          try {
            if(null != deptMgr.getRObjectId()) {
              for(int i=0;i<docIdList.size();i++) {
                String deptMgrId = deptMgr.getUUserId();
                VUser deptMgrData = userService.selectOneByUserId(deptMgrId).orElse(new VUser());
                List<String> deptMgrEmail = new ArrayList<>();
                deptMgrEmail.add(deptMgrData.getEmail());
                Doc reqDocData = docDao.selectOne(docIdList.get(i)).orElse(new Doc());
                NotiConfig notiData = notiConfigDao.selectOneByCodes(reqComeCode, "ER");
                if("Y".equals(notiData.getUAlarmYn())) {
                  idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
                  idf_PObj2.setString("u_msg_type", "ER");
                  idf_PObj2.setString("u_sender_id", deptMgrId);
                  idf_PObj2.setString("u_receiver_id", userSession.getDUserId());
                  idf_PObj2.setString("u_msg", "'" + reqDocData.getTitle() + "'" + " 문서의 복호화반출이 프리패스 요쳥되었습니다.");
                  idf_PObj2.setString("u_obj_id", idf_PObj.getString("r_object_id"));
                  idf_PObj2.setString("u_sent_date", new DfTime().toString());
                  idf_PObj2.setString("u_action_date", new DfTime().toString());
                  idf_PObj2.setString("u_action_yn", "Y");
                  idf_PObj2.setString("u_action_need_yn", "N");
                  idf_PObj2.save();                     
                } 
                if("Y".equals(notiData.getUEmailYn())) {
                  List<String> userEmail = new ArrayList<>();
                  userEmail.add(userSession.getUser().getEmail());
                  StringBuffer content = new StringBuffer();       
                  content.append("<html> ");   
                  content.append("<body>");           
                  content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");           
                  content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; "+reqDocData.getTitle()+" 문서의 복호화반출이 프리패스 요쳥되었습니다.</font>");  
                  content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>"); 
                  content.append(" </body></html>");          
                  notificationService.sendMail("dbox@dongkuk.com", userEmail, "[D'Box]" + "'" + reqDocData.getTitle() + "'" + " 문서의 복호화반출이 프리패스 요쳥되었습니다."
                  , content.toString());
                }
                if("Y".equals(notiData.getUMmsYn())) {
                  String mobileTel = deptMgrData.getMobileTel().replace("-", "");
                  notificationService.sendKakao(deptMgrId, mobileTel, "dbox_alarm_006", reqDoc.getTitle() + " 문서의 복호화반출이 "+"프리패스"+" 요청되었습니다.");               
                } 
              }
            }
          }catch(Exception e) {
            System.out.print("알림 error");
          }
          
          break;
        default:
          throw new RuntimeException();
        }
    
      idfSession.commitTrans();
      adSess.commitTrans();
      
    } catch (Exception e) {
      e.printStackTrace();
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
          adSess.disconnect();
        }
      }
    }

    // 부서문서관리자.
    /*
     * List<DeptMgrs> reqDeptMgr = deptMgrDao.selectByDeptCode(reqDeptCode);
     * DeptMgrs reqDeptMgrOne = reqDeptMgr.get(0); VUser reqMgr =
     * userService.selectOneByUserId(reqDeptMgrOne.getUUserId()).orElse(null);
     * String recipients[] = new String[] { reqMgr.getEmail() };// reqMgr.getEmail()
     * List<String> reciever = new ArrayList<>(); reciever.add(reqMgr.getUserId());
     */

    return idf_PObj.getObjectId().getId();
  }

  @Override
  public String approveReqTakeout(String takeoutRequestId, UserSession userSession, String ip)
      throws Exception {
    IDfSession idfSession = this.getIdfSession(userSession);
    IDfPersistentObject idf_PObj = null;
    IDfPersistentObject idf_PObj2 = null;
    IDfPersistentObject idf_PObj3 = null;
    IDfSession adSess = null;
    idfSession.beginTrans();
    
    ReqTakeout req = reqTakeoutDao.selectOneByObjectId(takeoutRequestId).orElse(null);
    List<ReqTakeoutDoc> reqDocDataList = reqTakeoutDao.selectOneByReqId(takeoutRequestId);
   
      try {
        if (idfSession == null || !idfSession.isConnected()) {
          throw new Exception("DCTM Session 가져오기 실패");
        }
        
        for(ReqTakeoutDoc reqDocData : reqDocDataList){
          Doc reqDoc = docDao.selectOne(reqDocData.getUReqDocId())
              .orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
          
          VDept deptData = deptDao.selectOneByOrgId(reqDoc.getUDeptCode()).orElse(new VDept());
          String reqDeptCode = deptDao.selectOrgIdByCabinetcode(reqDoc.getUCabinetCode());
          String reqComCode = deptDao.selectComCodeByCabinetCode(reqDoc.getUCabinetCode());
          List<DocRepeating> docV2 = docDao.selRepeatingOne(reqDoc.getRObjectId());
          TakeoutConfig takoutConfig = reqTakeoutDao.selectReasonOneByDeptCode(reqDoc.getUDeptCode()).orElse(new TakeoutConfig());

          LocalDateTime now = LocalDateTime.now(); // 현재시간
          LocalDateTime delDate = now.plusDays(takoutConfig.getUDeleteDays());
          LocalDateTime nullDelDate = now.plusDays(5);
          String limitDate = delDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59"));
          String nullLimitDate = nullDelDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd 23:59:59"));

          idf_PObj = ReqTakeoutApproveDto.ApproveTakeout(takeoutRequestId, idfSession, userSession);
          adSess = DCTMUtils.getAdminSession();
          if (!adSess.isTransactionActive()) {
            adSess.beginTrans();
          }
          if(reqDoc.getRLockOwner() != null && !reqDoc.getRLockOwner().equals(" ")) {
            throw new RuntimeException("편집 중인 문서는 반출 승인처리를 할 수 없습니다.");
          }
          IDfDocument idf_Doc = (IDfDocument) adSess.getObject(new DfId(reqDocData.getUReqDocId()));
          idf_Doc.setBoolean("u_takeout_flag", true);
          idf_Doc.setBoolean("u_ver_keep_flag", true);
          idf_Doc.save();
          if(takoutConfig.getUDeleteOption() == null || " ".equals(takoutConfig.getUDeleteOption())) {
            idf_PObj.setString("u_limit_flag", "D");
            idf_PObj.setString("u_limit_date", nullLimitDate);
            idf_PObj.save();
          }else {
            // TODO M:메일발송 후 삭제, D:지정일 삭제, U:삭제안함
            if ("M".equals(takoutConfig.getUDeleteOption())) {
              idf_PObj.setString("u_limit_flag", "M");
              idf_PObj.setString("u_limit_date", null);
            } else if ("D".equals(takoutConfig.getUDeleteOption())) {
              idf_PObj.setString("u_limit_flag", "D");
              idf_PObj.setString("u_limit_date", limitDate);
            } else if ("U".equals(takoutConfig.getUDeleteOption())) {
              idf_PObj.setString("u_limit_flag", "U");
              idf_PObj.setString("u_limit_date", null);
            }
            idf_PObj.save();
          }
        

          
          LogDoc logDoc = LogDoc.builder()
                .uJobCode(DocLogItem.EA.getValue())
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
          VUser reqUser = userService.selectOneByUserId(req.getUReqUser()).orElse(null);
          List<String> reciever = new ArrayList<>();
          reciever.add(reqUser.getUserId());
          
          List<Alarm> alarmData = alarmDao.selectOneByObjId(takeoutRequestId, req.getUReqUser(), "ER");
          for(int i=0;i<alarmData.size(); i++) {
            idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(alarmData.get(i).getRObjectId()));
            idf_PObj3.setString("u_action_yn", "Y");
            idf_PObj3.setString("u_action_date", new DfTime().toString());
            idf_PObj3.save();
          }
          try {
            NotiConfig notiData = notiConfigDao.selectOneByCodes(reqComCode, "ER");
            if("Y".equals(notiData.getUAlarmYn())) {
              idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
              idf_PObj2.setString("u_msg_type", "ER");
              idf_PObj2.setString("u_sender_id", userSession.getDUserId());
              idf_PObj2.setString("u_receiver_id", req.getUReqUser());
              idf_PObj2.setString("u_msg", "'" + reqDoc.getTitle() + "'" + " 문서의 복호화반출요청이 승인되었습니다.");
              idf_PObj2.setString("u_obj_id", takeoutRequestId);
              idf_PObj2.setString("u_sent_date", new DfTime().toString());
              idf_PObj2.setString("u_action_date", new DfTime().toString());
              idf_PObj2.setString("u_action_yn", "Y");
              idf_PObj2.setString("u_action_need_yn", "N");
              idf_PObj2.save();
            }
            if("Y".equals(notiData.getUEmailYn())) {
              List<String> userEmail = new ArrayList<>();
              userEmail.add(reqUser.getEmail());
              StringBuffer content = new StringBuffer();       
              content.append("<html> ");   
              content.append("<body>");           
              content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");           
              content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; "+reqDoc.getTitle()+" 문서의 복호화반출요청이 승인되었습니다.</font>");  
              content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>"); 
              content.append(" </body></html>");          
              notificationService.sendMail("dbox@dongkuk.com", userEmail, "[D'Box]" + "'" + reqDoc.getTitle() + "'" + " 문서의 복호화반출요청이 승인되었습니다."
              , content.toString());
            }
            if("Y".equals(notiData.getUMmsYn())) {
              String mobileTel = reqUser.getMobileTel().replace("-", "");
              notificationService.sendKakao(reqUser.getUserId(), mobileTel, "dbox_alarm_007", reqDoc.getTitle() + " 문서의 복호화반출이 "+"승인"+"되었습니다.");               
            } 
          }catch(Exception e) {
            System.out.print("알림 error");
          }
          
        }
        
       
  
        idfSession.commitTrans();
        adSess.commitTrans();
      } catch (Exception e) {
        e.printStackTrace();
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
  public Map<String, Integer> approveAllReqTakeout(UserSession userSession, List<String> takeoutRequestIdList, String ip)
      throws Exception {
//
//    IDfSession idfSession = this.getIdfSession(userSession);
//    IDfPersistentObject idf_PObj = null;
//    IDfSession adSess = null;
//    MailSenderUtils mail = new MailSenderUtils();
//
//    int successCnt = 0;
//    int failCnt = 0;
//    idfSession.beginTrans();
//    try {
//      if (idfSession == null || !idfSession.isConnected()) {
//        throw new Exception("DCTM Session 가져오기 실패");
//      }
//
//      for (String objId : takeoutRequestIdList) {
//        try {
//          ReqTakeout req = reqTakeoutDao.selectOneByObjectId(objId).orElse(null);
//          ReqTakeoutDoc reqDocData = reqTakeoutDao.selectOneByReqId(objId).orElse(null);
//          Doc reqDoc = docDao.selectOne(reqDocData.getUReqDocId())
//              .orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
//          VDept deptData = deptDao.selectOneByOrgId(reqDoc.getUDeptCode()).orElse(new VDept());
//          String comCode = deptData.getComOrgId();
//          TakeoutConfig takeoutCfg = reqTakeoutDao.selectReasonOneByDeptCode(reqDoc.getUDeptCode()).orElse(null);
//          IDfTime startDate = new DfTime();
//          GregorianCalendar cal = new GregorianCalendar(startDate.getYear(), startDate.getMonth(), startDate.getDay(),
//              startDate.getHour(), startDate.getMinutes());
//          cal.add(GregorianCalendar.DATE, 7);
//          IDfTime expireDate = new DfTime(cal.getTime());
//          VUser reqUser = userService.selectOneByUserId(req.getUReqUser()).orElse(null);
//          String recipients[] = new String[] { reqUser.getEmail() };// reqUser.getEmail()
//          List<String> reciever = new ArrayList<>();
//          reciever.add(reqUser.getUserId());
//
//          idf_PObj = (IDfPersistentObject) idfSession.getObject(new DfId(objId));
//          idf_PObj.setString("u_req_status", "A");
//          idf_PObj.setString("u_action_date", (new DfTime()).toString());
//          idf_PObj.setString("u_approver", idfSession.getLoginUserName());
//          idf_PObj.save();
//
//          adSess = DCTMUtils.getAdminSession();
//          if (!adSess.isTransactionActive()) {
//            adSess.beginTrans();
//          }
//          IDfDocument idf_Doc = (IDfDocument) adSess.getObject(new DfId(reqDocData.getUReqDocId()));
//
//          if (idf_PObj.getString("u_action_date") != null) {
//            successCnt++;
//            idf_Doc.setBoolean("u_takeout_flag", true);
//            idf_Doc.setBoolean("u_ver_keep_flag", true);
//            idf_Doc.save();
//
//            if ("UNC".equals(comCode) || "DKS".equals(comCode) || "FEI".equals(comCode)) {
//              mail.sendMailForHtml(recipients, "외부반출 승인완료건", "내용은 뭐라해야징", userSession.getUser().getEmail());
//              notificationService.sendAlarm(reciever, "제목", "내용", "ER");
//
//            } else if ("ITG".equals(comCode)) {
//              // TODO 카톡추가
//              mail.sendMailForHtml(recipients, "외부반출 승인완료건", "내용은 뭐라해야징", userSession.getUser().getEmail());
//              notificationService.sendAlarm(reciever, "제목", "내용", "ER");
//
//            }
//            cal.add(GregorianCalendar.DATE, 7);
//            if ("M".equals(takeoutCfg.getUDeleteOption())) {
//              idf_PObj.setString("u_limit_flag", "M");
//              idf_PObj.setString("u_limit_date", (new DfTime()).toString());
//              idf_PObj.save();
//            } else if ("D".equals(takeoutCfg.getUDeleteOption())) {
//              idf_PObj.setString("u_limit_flag", "D");
//              idf_PObj.setString("u_limit_date", expireDate.toString());
//              idf_PObj.save();
//            } else if ("U".equals(takeoutCfg.getUDeleteOption())) {
//              idf_PObj.setString("u_limit_flag", "U");
//              idf_PObj.setString("u_limit_date", null);
//              idf_PObj.save();
//            }
//            continue;
//          } else {
//            failCnt++;
//          }
//        } catch (Exception e) {
//
//          failCnt++;
//          continue;
//        } finally {
//        }
//      }
//      idfSession.commitTrans();
//      adSess.commitTrans();
//    } catch (Exception e) {
//      throw e;
//    } finally {
//      if (idfSession != null) {
//        if (idfSession.isTransactionActive()) {
//          idfSession.abortTrans();
//        }
//        if (idfSession.isConnected() && adSess.isConnected()) {
//          sessionRelease(userSession.getUser().getUserId(), idfSession);
//          adSess.disconnect();
//        }
//      }
//    }
//    Map<String, Integer> result = new HashMap<String, Integer>();
//    result.put("success", successCnt);
//    result.put("fail", failCnt);
//
//    return result;
    return null;
  }

  @Override
  public String rejectReqTakeout(String takeoutRequestId, UserSession userSession, String rejectReason)
      throws Exception {
    IDfPersistentObject idf_PObj = null;
    IDfPersistentObject idf_PObj2 = null;
    IDfPersistentObject idf_PObj3 = null;
    IDfSession idfSession = this.getIdfSession(userSession);
    ReqTakeout req = reqTakeoutDao.selectOneByObjectId(takeoutRequestId).orElse(null);
    List<ReqTakeoutDoc> reqDocDataList = reqTakeoutDao.selectOneByReqId(takeoutRequestId);
 
      idfSession.beginTrans();
      try {
        if (idfSession == null || !idfSession.isConnected()) {
          throw new Exception("DCTM Session 가져오기 실패");
        }
        for(ReqTakeoutDoc reqDocData : reqDocDataList){
          Doc reqDoc = docDao.selectOne(reqDocData.getUReqDocId())
              .orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
          String reqComCode = deptDao.selectComCodeByCabinetCode(reqDoc.getUCabinetCode());
          VUser reqUser = userService.selectOneByUserId(req.getUReqUser()).orElse(null);
          List<String> reciever = new ArrayList<>();
          reciever.add(reqUser.getUserId());
          
          List<Alarm> alarmData = alarmDao.selectOneByObjId(takeoutRequestId, req.getUReqUser(), "ER");
          for(int i=0;i<alarmData.size(); i++) {
            idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(alarmData.get(i).getRObjectId()));
            idf_PObj3.setString("u_action_yn", "N");
            idf_PObj3.setString("u_action_date", new DfTime().toString());
            idf_PObj3.save();
          }
          try {
            NotiConfig notiData = notiConfigDao.selectOneByCodes(reqComCode, "ER");
            if("Y".equals(notiData.getUAlarmYn())) {
              idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
              idf_PObj2.setString("u_msg_type", "ER");
              idf_PObj2.setString("u_sender_id", userSession.getDUserId());
              idf_PObj2.setString("u_receiver_id", req.getUReqUser());
              idf_PObj2.setString("u_msg", "'" + reqDoc.getTitle() + "'" + " 문서의 복호화반출요청이 반려되었습니다.");
              idf_PObj2.setString("u_obj_id", takeoutRequestId);
              idf_PObj2.setString("u_sent_date", new DfTime().toString());
              idf_PObj2.setString("u_action_date", new DfTime().toString());
              idf_PObj2.setString("u_action_yn", "N");
              idf_PObj2.setString("u_action_need_yn", "N");
              idf_PObj2.save();
            } 
            if("Y".equals(notiData.getUEmailYn())) {
              List<String> userEmail = new ArrayList<>();
              userEmail.add(reqUser.getEmail());
              StringBuffer content = new StringBuffer();       
              content.append("<html> ");   
              content.append("<body>");           
              content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");           
              content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; "+reqDoc.getTitle()+" 문서의 복호화반출요청이 반려되었습니다.</font>");  
              content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>"); 
              content.append(" </body></html>");          
              notificationService.sendMail("dbox@dongkuk.com", userEmail, "[D'Box]" + "'" + reqDoc.getTitle() + "'" + " 문서의 복호화반출요청이 반려되었습니다."
              , content.toString());
            }
            if("Y".equals(notiData.getUMmsYn())) {
              String mobileTel = reqUser.getMobileTel().replace("-", "");
              notificationService.sendKakao(reqUser.getUserId(), mobileTel, "dbox_alarm_007", reqDoc.getTitle() + "문서의 복호화반출이 "+"반려"+"되었습니다.");               
            } 
          }catch(Exception e) {
            System.out.print("알림 error");
          }
          
        }
        VUser reqUser = userService.selectOneByUserId(req.getUReqUser()).orElse(null);
        List<String> reciever = new ArrayList<>();
        reciever.add(reqUser.getUserId());
        idf_PObj = ReqTakeoutRejectDto.RejectTakeout(takeoutRequestId, idfSession, rejectReason);

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
  public Map<String, Integer> rejectAllReqTakeout(UserSession userSession, String rejectReason,
      List<String> takeoutRequestIdList) throws Exception {

//    IDfSession idfSession = this.getIdfSession(userSession);
//    IDfPersistentObject idf_PObj = null;
//    MailSenderUtils mail = new MailSenderUtils();
//    int successCnt = 0;
//    int failCnt = 0;
//    idfSession.beginTrans();
//    try {
//      if (idfSession == null || !idfSession.isConnected()) {
//        throw new Exception("DCTM Session 가져오기 실패");
//      }
//      for (String objId : takeoutRequestIdList) {
//        try {
//
//          ReqTakeout req = reqTakeoutDao.selectOneByObjectId(objId).orElse(null);
//          ReqTakeoutDoc reqDocData = reqTakeoutDao.selectOneByReqId(objId).orElse(null);
//          Doc reqDoc = docDao.selectOne(reqDocData.getUReqDocId())
//              .orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
//          VDept deptData = deptDao.selectOneByOrgId(reqDoc.getUDeptCode()).orElse(new VDept());
//          String comCode = deptData.getComOrgId();
//          VUser reqUser = userService.selectOneByUserId(req.getUReqUser()).orElse(null);
//          List<String> reciever = new ArrayList<>();
//          reciever.add(reqUser.getUserId());
//          String recipients[] = new String[] { reqUser.getEmail() };
//
//          idf_PObj = (IDfPersistentObject) idfSession.getObject(new DfId(objId));
//          idf_PObj.setString("u_req_status", "J");
//          idf_PObj.setString("u_reject_reason", reqTakeoutRejectDto.getURejectReason());
//          idf_PObj.setString("u_action_date", (new DfTime()).toString());
//          idf_PObj.setString("u_approver", userSession.getDUserId());
//          idf_PObj.save();
//
//          if (req.getUApprover() != null) {
//            successCnt++;
//            if ("UNC".equals(comCode) || "DKS".equals(comCode) || "FEI".equals(comCode)) {
//              notificationService.sendAlarm(reciever, "외부반출 승인 반려건", "내용", "ER");
//              mail.sendMailForHtml(recipients, "외부반출 승인 반려건", "내용은 뭐라해야징", userSession.getUser().getEmail());
//            } else if ("ITG".equals(comCode)) {
//              mail.sendMailForHtml(recipients, "외부반출 승인 반려건", "내용은 뭐라해야징", userSession.getUser().getEmail());
//              notificationService.sendAlarm(reciever, "외부반출 승인 반려건", "내용", "ER");
//              // 카톡 추가
//            }
//            continue;
//          } else {
//            failCnt++;
//          }
//        } catch (Exception e) {
//          failCnt++;
//          continue;
//        } finally {
//        }
//      }
//      idfSession.commitTrans();
//    } catch (Exception e) {
//      throw e;
//    } finally {
//      if (idfSession != null) {
//        if (idfSession.isTransactionActive()) {
//          idfSession.abortTrans();
//        }
//        if (idfSession.isConnected()) {
//          sessionRelease(userSession.getUser().getUserId(), idfSession);
//        }
//      }
//    }
//    Map<String, Integer> result = new HashMap<String, Integer>();
//    result.put("success", successCnt);
//    result.put("fail", failCnt);
//
//    return result;
    return null;
  }

  @Override
  public int selectCountByReqDocId(String uReqId) throws Exception {
    return reqTakeoutDao.selectCountByReqDocId(uReqId);
  }

  public void patchTakeoutConfig(UserSession userSession, ReqTakeoutConfigDto dto) throws Exception {
    IDfSession idfSession = this.getIdfSession(userSession);
    IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(dto.getRObjectId()));
    
    try {         
      switch(dto.getMode()) {
        case "I" : 
          if(dto.getType().equals("A")) {
            idf_PObj.appendString("u_auto_name", dto.getUAutoName());
            idf_PObj.appendString("u_auto_register", idfSession.getLoginUserName());
            idf_PObj.appendString("u_auto_regist_date", (new DfTime()).toString());
          }else {
            idf_PObj.appendString("u_free_name", dto.getUFreeName());
            idf_PObj.appendString("u_free_register", idfSession.getLoginUserName());
            idf_PObj.appendString("u_free_regist_date", (new DfTime()).toString());
          }
          break;
        case "U" : 
          if(dto.getType().equals("A")) {
            idf_PObj.setRepeatingString("u_auto_name",  Integer.parseInt(dto.getValueIndex()), dto.getUAutoName());
          }else if(dto.getType().equals("F")) {
            idf_PObj.setRepeatingString("u_free_name",  Integer.parseInt(dto.getValueIndex()), dto.getUFreeName());
          }else {
            idf_PObj.setString("u_auto_appr_yn", dto.getUAutoApprYn());
            idf_PObj.setString("u_free_pass_yn", dto.getUFreePassYn());
            idf_PObj.setString("u_delete_option", dto.getUDeleteOption());
            if(dto.getUDeleteOption().equals("D")) idf_PObj.setInt("u_delete_days", dto.getUDeleteDays());
          }
          break;
        default :
          break;
      }
      idf_PObj.setString("u_update_user", idfSession.getLoginUserName());
      idf_PObj.setString("u_update_date", (new DfTime()).toString());
      idf_PObj.save(); 
    } catch(Exception e) {
      e.printStackTrace();
    } finally {
      if (idfSession != null  && idfSession.isConnected()) {
        sessionRelease(userSession.getUser().getUserId(), idfSession);
          }
    }
    
  }

  @Override
  public void deleteTakeoutConfig(UserSession userSession, ReqTakeoutConfigDto dto) throws Exception {    
    IDfSession idfSession = this.getIdfSession(userSession);
    IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(dto.getRObjectId()));
    
    try {         
      if(dto.getType().equals("A")) {
        int i_ValIdx = idf_PObj.findString("u_auto_name", dto.getUAutoName());
        idf_PObj.remove("u_auto_name", i_ValIdx);
        idf_PObj.remove("u_auto_register", i_ValIdx);
        idf_PObj.remove("u_auto_regist_date", i_ValIdx);
      }else {
        int i_ValIdx = idf_PObj.findString("u_free_name", dto.getUFreeName());
        idf_PObj.remove("u_free_name", i_ValIdx);
        idf_PObj.remove("u_free_register", i_ValIdx);
        idf_PObj.remove("u_free_regist_date", i_ValIdx);
      }
      idf_PObj.save(); 
    } catch(Exception e) {
      e.printStackTrace();
    } finally {
      if (idfSession != null  && idfSession.isConnected()) {
        sessionRelease(userSession.getUser().getUserId(), idfSession);
       }
    }

  }
  
  
  @Override
  public List<ReqTakeout> takeoutListByDeptCode(String deptCode, ReqTakeoutDto dto) throws Exception {
    List<ReqTakeout> reqTakeoutList = reqTakeoutDao.selectListByDeptCode(deptCode, dto);
    return reqTakeoutList;
  }

  @Override
  public List<ReqTakeoutDto> takeoutListByReqId(String reqId) {
    List<ReqTakeoutDto> reqTakeoutDetailList = reqTakeoutDao.selectListByReqId(reqId);
    return reqTakeoutDetailList;
  }


}
