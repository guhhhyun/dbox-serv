package com.dongkuksystems.dbox.services.reqdisposal;

import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.DocLogItem;
import com.dongkuksystems.dbox.daos.table.etc.gwdept.GwDeptDao;
import com.dongkuksystems.dbox.daos.table.reqdisposal.ReqDisposalDao;
import com.dongkuksystems.dbox.daos.type.code.CodeDao;
import com.dongkuksystems.dbox.daos.type.doc.DocDao;
import com.dongkuksystems.dbox.daos.type.folder.FolderDao;
import com.dongkuksystems.dbox.daos.type.manager.deptMgr.DeptMgrDao;
import com.dongkuksystems.dbox.daos.type.manager.noticonfig.NotiConfigDao;
import com.dongkuksystems.dbox.daos.type.manager.preservationperiod.PreservationPeriodDao;
import com.dongkuksystems.dbox.daos.type.noti.AlarmDao;
import com.dongkuksystems.dbox.daos.type.path.PathDao;
import com.dongkuksystems.dbox.errors.BadRequestException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.deletemanage.DeleteManageDto;
import com.dongkuksystems.dbox.models.dto.type.manager.preservationperiod.PreservationPeriodDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqClosedDetailDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqDisposalDetailDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqDisposalFilterDto;
import com.dongkuksystems.dbox.models.table.etc.GwJobTitle;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.table.log.LogDoc;
import com.dongkuksystems.dbox.models.type.code.Code;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.doc.DocRepeating;
import com.dongkuksystems.dbox.models.type.folder.Folder;
import com.dongkuksystems.dbox.models.type.manager.deptMgr.DeptMgrs;
import com.dongkuksystems.dbox.models.type.manager.noticonfig.NotiConfig;
import com.dongkuksystems.dbox.models.type.noti.Alarm;
import com.dongkuksystems.dbox.models.type.request.ReqDelete;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.folder.FolderService;
import com.dongkuksystems.dbox.services.isdelete.IsDeleteService;
import com.dongkuksystems.dbox.services.notification.NotificationService;
import com.dongkuksystems.dbox.services.user.UserService;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

@Service
public class ReqDisposalServiceImpl extends AbstractCommonService implements ReqDisposalService {
 
  @Value("${dbox.url}")
  private String dboxUrl; 
  
  private final ReqDisposalDao reqDisposalDao;
  private final DocDao docDao;
  private final FolderDao folderDao;
  private final UserService userService;
  private final GwDeptDao deptDao;
  private final PreservationPeriodDao preservationPeriodDao;
  private final NotiConfigDao notiConfigDao;
  private final DeptMgrDao deptMgrDao;
  private final NotificationService notificationService;
  private final AlarmDao alarmDao;
  private final CodeDao codeDao;
  private final PathDao pathDao;
  private final IsDeleteService isDeleteService;
  private final FolderService folderService;
  
  
  public ReqDisposalServiceImpl(ReqDisposalDao reqDisposalDao, DocDao docDao, FolderDao folderDao, UserService userService, GwDeptDao deptDao, PreservationPeriodDao preservationPeriodDao
      ,NotiConfigDao notiConfigDao, DeptMgrDao deptMgrDao, NotificationService notificationService, AlarmDao alarmDao, CodeDao codeDao, PathDao pathDao, IsDeleteService isDeleteService
      ,FolderService folderService) {
    this.reqDisposalDao = reqDisposalDao;
    this.docDao = docDao;
    this.folderDao = folderDao;
    this.userService = userService;
    this.deptDao = deptDao;
    this.preservationPeriodDao = preservationPeriodDao;
    this.notiConfigDao = notiConfigDao;
    this.deptMgrDao = deptMgrDao;
    this.notificationService = notificationService;
    this.alarmDao = alarmDao;
    this.codeDao = codeDao;
    this.pathDao = pathDao;
    this.isDeleteService = isDeleteService;
    this.folderService = folderService;
  }



  @Override
  public List<ReqClosedDetailDto> reqClosedList() throws Exception {
    final ModelMapper modelMapper = getModelMapper();

    List<ReqDelete> reqAuthList = reqDisposalDao.reqClosedDelDetailAll();

    List<ReqClosedDetailDto> reqAuthDetailList = reqAuthList.stream().map((item) -> {
      ReqClosedDetailDto reqClosedDetailDto = modelMapper.map(item, ReqClosedDetailDto.class);
      
      VUser requser = Optional.ofNullable(item.getReqUserDetail()).orElse(new VUser());
      VUser approver = Optional.ofNullable(item.getApproverDetail()).orElse(new VUser());
      Doc docName = Optional.ofNullable(item.getDocDetail()).orElse(new Doc());
      reqClosedDetailDto.setUReqUserName(requser.getDisplayName());
      reqClosedDetailDto.setUReqUserJobTitleName(
          Optional.ofNullable(requser.getJobTitleDetail()).orElse(new GwJobTitle()).getName());
      reqClosedDetailDto.setUReqUserDeptName(requser.getOrgNm());
      reqClosedDetailDto.setUApproverName(
          Optional.ofNullable(item.getApproverDetail()).orElse(new VUser()).getDisplayName());
      reqClosedDetailDto.setUApproverDeptName(approver.getOrgNm());
      reqClosedDetailDto.setUDocSize(docName.getRContentSize());
      VDept requserDept = Optional.ofNullable(requser.getDeptDetail()).orElse(new VDept());
      VDept approverDept = Optional.ofNullable(approver.getDeptDetail()).orElse(new VDept());
      reqClosedDetailDto.setUReqUserComName(
          Optional.ofNullable(requserDept.getCompanyDetail()).orElse(new Code()).getUCodeName1());
      reqClosedDetailDto.setUApproverComName(
          Optional.ofNullable(approverDept.getCompanyDetail()).orElse(new Code()).getUCodeName1());
      reqClosedDetailDto.setUApproverJobTitleName(
          Optional.ofNullable(approver.getJobTitleDetail()).orElse(new GwJobTitle()).getName());
      return reqClosedDetailDto;

    }).collect(Collectors.toList());

    return reqAuthDetailList;
  }

  @Override
  public ReqClosedDetailDto getClosedRequest(String rObjectId) throws Exception {
    final ModelMapper modelMapper = getModelMapper();
    ReqDelete reqDelete = reqDisposalDao.dataByObjId(rObjectId);
    return modelMapper.map(reqDelete, ReqClosedDetailDto.class);
  }

  @Override
  public String approveReqClosed(String closedRequestId, UserSession userSession, String ip, DeleteManageDto dto) throws Exception {
    ReqDelete reqClosedData = reqDisposalDao.dataByObjId(closedRequestId);
    Doc reqDoc = docDao.selectOne(reqClosedData.getUReqDocId())
        .orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
    String comCode = deptDao.selectComCodeByCabinetCode(reqDoc.getUCabinetCode());
    String cabinetCode = reqDoc.getUCabinetCode();
    String reqDeptCode = deptDao.selectOrgIdByCabinetcode(cabinetCode);
    LocalDateTime today = LocalDateTime.now();
 //   Code codeData = codeDao.selectOneByOther("CONFIG_DELETE_PERIOD", comCode, "DEL_EACH");
    List<DocRepeating> docV2 = docDao.selRepeatingOne(reqDoc.getRObjectId());
    IDfSession idfSession = this.getIdfSession(userSession);
    IDfPersistentObject idf_PObj = null;
    IDfPersistentObject idf_PObj2 = null;
    IDfPersistentObject idf_PObj3 = null;
    IDfSession adSess = null;
    VUser reqUser = userService.selectOneByUserId(reqClosedData.getUReqUser()).orElse(null);
    String recipients[] = new String[] { reqUser.getEmail() };
    List<String> reciever = new ArrayList<>();
    if(null != reqUser.getEmail()  && !reqUser.getEmail().equals(""))
        reciever.add(reqUser.getEmail());
  
    idfSession.beginTrans();
    try {

      if (idfSession == null || !idfSession.isConnected()) {
        throw new Exception("DCTM Session 가져오기 실패");
      }
      
      String userTypeData = null;
      if(dto == null || dto.getUserType().equals("") || dto.getUserType().equals(null)) {
        userTypeData = "D";
      } else if (dto.getUserType().equals("DKG")) {
        userTypeData = "G";
      } else {
        userTypeData = "C";
      }

      idf_PObj = (IDfPersistentObject)idfSession.getObject(new DfId(closedRequestId));

      idf_PObj.setString("u_req_status", "A");
      idf_PObj.setString("u_approver", userSession.getDUserId());
      idf_PObj.setString("u_approve_date",  (new DfTime()).toString());    
      idf_PObj.save();
     
      adSess = DCTMUtils.getAdminSession();
        if (!adSess.isTransactionActive()) {
         adSess.beginTrans();
        }
      IDfDocument idf_Doc = (IDfDocument) adSess.getObject(new DfId(reqClosedData.getUReqDocId()));
      idf_Doc.setString("u_delete_status", "A");
      idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
      idf_Doc.save();
      
      List<Alarm> alarmData = alarmDao.selectOneByObjId(closedRequestId, reqClosedData.getUReqUser(), "DR");
      for(int i=0;i<alarmData.size(); i++) {
        idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(alarmData.get(i).getRObjectId()));
        idf_PObj3.setString("u_action_yn", "Y");
        idf_PObj3.setString("u_action_date", new DfTime().toString());
        idf_PObj3.save();
      }
      
      NotiConfig notiData = notiConfigDao.selectOneByCodes(comCode, "DR");
      
      if("Y".equals(notiData.getUAlarmYn())) {
        idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
        idf_PObj2.setString("u_msg_type", "DR");
        idf_PObj2.setString("u_sender_id", userSession.getDUserId());
        idf_PObj2.setString("u_receiver_id", reqClosedData.getUReqUser());
        idf_PObj2.setString("u_msg", "'" + reqDoc.getTitle() + "'" + " 문서의 폐기요청이 승인되었습니다.");
        idf_PObj2.setString("u_obj_id", closedRequestId);
        idf_PObj2.setString("u_sent_date", new DfTime().toString());
        idf_PObj2.setString("u_action_date", new DfTime().toString());
        idf_PObj2.setString("u_action_yn", "Y");
        idf_PObj2.setString("u_action_need_yn", "N");
        idf_PObj2.save();
      } 
      if("Y".equals(notiData.getUEmailYn())) {
    	if(null !=reqUser.getEmail()){
	        StringBuffer content = new StringBuffer();       
	        content.append("<html> ");   
	        content.append("<body>");           
	        content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");           
	        content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; "+reqDoc.getTitle()+" 문서의 폐기요청이 승인되었습니다.</font>");             
	        content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>"); 
	        content.append(" </body></html>");          
	        notificationService.sendMail("dbox@dongkuk.com", reciever, "[D'Box]" + "'" + reqDoc.getTitle() + "'" + " 문서의 폐기요청이 승인되었습니다."
	        , content.toString());
    	}else {
    		System.out.println("#폐기요청ID:" +reqUser.getUserId() +" 이메일 주소 불량");
    	}
      }
      if("Y".equals(notiData.getUMmsYn())) {     
    	if(null !=reqUser.getMobileTel() && !reqUser.getMobileTel().equals("")) {
	        String mobileTel = reqUser.getMobileTel().replace("-", "");
	        notificationService.sendKakao(reqUser.getUserId(), mobileTel, "dbox_alarm_005", reqDoc.getTitle() + " 문서의 폐기요청이 " + "승인되었습니다.");      
    	}else {
    		System.out.println("#폐기요청ID_카카오:" +reqUser.getUserId() +" 핸드폰 폰번호 불량");
    	}
      }
      LogDoc logDoc = LogDoc.builder()          
            .uJobCode(DocLogItem.DA.getValue())
            .uDocId(reqDoc.getRObjectId())
            .uDocKey(reqDoc.getUDocKey())
            .uDocName(reqDoc.getTitle())
            .uDocVersion(docV2.get(0).getRVersionLabel())
            .uOwnDeptcode(reqDeptCode)
            .uActDeptCode(userSession.getUser().getOrgId())
            .uFileSize(Integer.valueOf(reqDoc.getRContentSize()))
            .uJobUser(userSession.getDUserId())            
            .uJobUserType(userTypeData)
            .uDocStatus(reqDoc.getUDocStatus())                          
            .uSecLevel(reqDoc.getUSecLevel())
            .uCabinetCode(reqDoc.getUCabinetCode())
            .uUserIp(ip)
            .build();
          insertLog(logDoc);
     
      adSess.commitTrans();
      idfSession.commitTrans();
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
  public String rejectReqClosed(String closedRequestId, UserSession userSession, String ip, DeleteManageDto dto) throws Exception {
    IDfSession idfSession = this.getIdfSession(userSession);
    ReqDelete reqClosedData = reqDisposalDao.dataByObjId(closedRequestId);
    Doc reqDoc = docDao.selectOne(reqClosedData.getUReqDocId())
        .orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
    String cabinetCode = reqDoc.getUCabinetCode();
    String reqDeptCode = deptDao.selectOrgIdByCabinetcode(cabinetCode);
    String reqComCode = deptDao.selectComCodeByCabinetCode(cabinetCode);
    VUser reqUser = userService.selectOneByUserId(reqClosedData.getUReqUser()).orElse(null);
    List<DocRepeating> docV2 = docDao.selRepeatingOne(reqDoc.getRObjectId());
    PreservationPeriodDto getAutoPreserve = preservationPeriodDao.selectOneByComCode(reqComCode);    
    
    String expiredDate = null;
    LocalDateTime today = LocalDateTime.now();
    LocalDateTime delDate = LocalDateTime.of(
        today.getYear()+ Integer.valueOf(getAutoPreserve.getUAutoExtend()), 
        today.getMonth(), 
        today.getDayOfMonth(), 
        23, 59, 59, 0);
    expiredDate = delDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));  
    List<String> reciever = new ArrayList<>();
    IDfSession adSess = null;
    if(null != reqUser.getEmail()  && !reqUser.getEmail().equals(""))
        reciever.add(reqUser.getEmail());

    IDfPersistentObject idf_PObj = null;
    IDfPersistentObject idf_PObj2 = null;
    IDfPersistentObject idf_PObj3 = null;
    Folder moveFolData = null;
    String moveCabinetCode = null;

    idfSession.beginTrans();
    try {

      if (idfSession == null || !idfSession.isConnected()) {
        throw new Exception("DCTM Session 가져오기 실패");
      }
      boolean isDelete = isDeleteService.isDelete(reqDoc.getRObjectId());
      if (dto != null) {
        if (DfId.isObjectId(dto.getTargetId())) {
          moveFolData = folderService.selectOne(dto.getTargetId()).orElse(new Folder());
        } else {
          moveCabinetCode = dto.getTargetId();
        }
      }
      String userTypeData = null;     
      if(dto == null || dto.getUserType().equals("") || dto.getUserType().equals(null)) {
        userTypeData = "D";
      } else if(dto.getUserType().equals("DKG")) {
        userTypeData = "G";
      } else {
        userTypeData = "C";
      }

      idf_PObj = idfSession.getObject(new DfId(closedRequestId));
      idf_PObj.setString("u_approver", userSession.getDUserId());
      idf_PObj.setString("u_req_status", "D");
      idf_PObj.setString("u_approve_date",  (new DfTime()).toString());
      idf_PObj.save();
      
      adSess = DCTMUtils.getAdminSession();
      if (!adSess.isTransactionActive()) {
       adSess.beginTrans();
      }
      IDfDocument idf_Doc = (IDfDocument) adSess.getObject(new DfId(reqClosedData.getUReqDocId()));
      idf_Doc.setString("u_delete_status", " ");
      idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
      if("E".equals(reqClosedData.getUReqType())) {
        idf_Doc.setInt("u_preserve_flag", Integer.valueOf(getAutoPreserve.getUAutoExtend()));
        idf_Doc.setString("u_expired_date", expiredDate); 
      }
      idf_Doc.save(); 
      if(isDelete == true) {
        idf_Doc.setString("u_pr_code", " ");
        idf_Doc.setString("u_pr_type", " ");
        if(dto != null && DfId.isObjectId(dto.getTargetId())) {
          idf_Doc.setString("u_fol_id", moveFolData.getRObjectId());
        }else {
          idf_Doc.setString("u_fol_id", " ");
        }
        idf_Doc.save();          
      }
            
      LogDoc logDoc = LogDoc.builder()          
            .uJobCode(DocLogItem.DC.getValue())
            .uDocId(reqDoc.getRObjectId())
            .uDocKey(reqDoc.getUDocKey())
            .uDocName(reqDoc.getTitle())
            .uDocVersion(docV2.get(0).getRVersionLabel())
            .uOwnDeptcode(reqDeptCode)
            .uActDeptCode(userSession.getUser().getOrgId())
            .uFileSize(Integer.valueOf(reqDoc.getRContentSize()))
            .uJobUser(userSession.getDUserId())            
            .uJobUserType(userTypeData)
            .uDocStatus(reqDoc.getUDocStatus())                          
            .uSecLevel(reqDoc.getUSecLevel())
            .uCabinetCode(reqDoc.getUCabinetCode())
            .uUserIp(ip)
            .build();
          insertLog(logDoc);       
      
      List<Alarm> alarmData = alarmDao.selectOneByObjId(closedRequestId, reqClosedData.getUReqUser(), "DR");
      for(int i=0;i<alarmData.size(); i++) {
        idf_PObj3 = (IDfPersistentObject) idfSession.getObject(new DfId(alarmData.get(i).getRObjectId()));
        idf_PObj3.setString("u_action_yn", "N");
        idf_PObj3.setString("u_action_date", new DfTime().toString());
        idf_PObj3.save();
      }
      
      NotiConfig notiData = notiConfigDao.selectOneByCodes(reqComCode, "DR");
      if("Y".equals(notiData.getUAlarmYn())) {
        idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
        idf_PObj2.setString("u_msg_type", "DR");
        idf_PObj2.setString("u_sender_id", userSession.getDUserId());
        idf_PObj2.setString("u_receiver_id", reqClosedData.getUReqUser());
        idf_PObj2.setString("u_msg", "'" + reqDoc.getTitle() + "'" + " 문서의 폐기요청이 반려되었습니다.");
        idf_PObj2.setString("u_obj_id", closedRequestId);        
        idf_PObj2.setString("u_action_date", new DfTime().toString());
        idf_PObj2.setString("u_sent_date", new DfTime().toString());
        idf_PObj2.setString("u_action_yn", "N");
        idf_PObj2.setString("u_action_need_yn", "N");
        idf_PObj2.save();
      } 
      if("Y".equals(notiData.getUEmailYn())) {
    	if(null !=reqUser.getEmail()){    	  
	        StringBuffer content = new StringBuffer();       
	        content.append("<html> ");   
	        content.append("<body>");           
	        content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");           
	        content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; "+reqDoc.getTitle()+" 문서의 폐기요청이 반려되었습니다.</font>");             
	        content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>"); 
	        content.append(" </body></html>");          
	        notificationService.sendMail("dbox@dongkuk.com", reciever, "[D'Box]" + "'" + reqDoc.getTitle() + "'" + " 문서의 폐기요청이 반려되었습니다."
	        , content.toString());
    	}
      }
      if("Y".equals(notiData.getUMmsYn())) {  
    	if(null !=reqUser.getMobileTel() && !reqUser.getMobileTel().equals("")) {
            String mobileTel = reqUser.getMobileTel().replace("-", "");
            notificationService.sendKakao(reqUser.getUserId(), mobileTel, "dbox_alarm_005", reqDoc.getTitle() + " 문서의 폐기요청이 " + "반려되었습니다.");
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
        if (adSess!=null && adSess.isTransactionActive()) {
          adSess.abortTrans();         
          adSess.disconnect();
        }
        if (idfSession.isConnected()) {
          sessionRelease(userSession.getUser().getUserId(), idfSession);          
        }
      }
    }

    return idf_PObj.getObjectId().getId();
  }



  @Override
  public List<ReqDisposalDetailDto> reqDisposalList(String deptCode, ReqDisposalFilterDto dto) {
    List<ReqDisposalDetailDto> list = reqDisposalDao.selectListByDeptCode(deptCode, dto);
    for(ReqDisposalDetailDto reqDis : list) {
      String result = pathDao.selectFolderPath(reqDis.getUFolId());
      reqDis.setUFolderPath(result);
    }
    return list;
  }
  
  
  @Override
  public void registReqDisposal(UserSession userSession, ReqDelete dto, String ip) throws Exception {
    IDfSession idfSession = this.getIdfSession(userSession);
    IDfSession idfAdminSession = DCTMUtils.getAdminSession(); // Superuser

    IDfPersistentObject idf_PObj = (IDfPersistentObject) idfSession.newObject("edms_req_delete");
    IDfPersistentObject idf_PObj2 = null;
    IDfPersistentObject idf_Doc = idfAdminSession.getObject(new DfId(dto.getUReqDocId()));
    Doc reqDoc = docDao.selectOne(dto.getUReqDocId())
        .orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
    String cabinetCode = reqDoc.getUCabinetCode();
    String reqDeptCode = deptDao.selectOrgIdByCabinetcode(cabinetCode);
    String reqComeCode = deptDao.selectComCodeByCabinetCode(cabinetCode);
    LocalDateTime today = LocalDateTime.now();
    DeptMgrs deptMgr = deptMgrDao.kingByDeptCode(reqDeptCode);
    String deptMgrId = deptMgr.getUUserId();
    VUser deptMgrData = userService.selectOneByUserId(deptMgrId).orElse(new VUser());
    List<String> deptMgrEmail = new ArrayList<>();
    if(null != deptMgrData.getEmail()  && !deptMgrData.getEmail().equals(""))
        deptMgrEmail.add(deptMgrData.getEmail());
    List<DocRepeating> docV2 = docDao.selRepeatingOne(reqDoc.getRObjectId());
    
    try { 
      String regDate = reqDoc.getURegDate().format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

      idf_PObj.setString("u_cabinet_code", dto.getUCabinetCode());
      idf_PObj.setString("u_req_doc_id", dto.getUReqDocId());
      idf_PObj.setString("u_req_doc_key", dto.getUReqDocKey());
      idf_PObj.setString("u_req_type", "E");
      idf_PObj.setString("u_req_status", "R");
      idf_PObj.setString("u_req_user", idfSession.getLoginUserName());  
      idf_PObj.setString("u_req_date", (new DfTime()).toString());
      idf_PObj.setString("u_req_reason", dto.getUReqReason());
      idf_PObj.setString("u_doc_name", reqDoc.getTitle());
      idf_PObj.setString("u_sec_level", reqDoc.getUSecLevel());
      idf_PObj.setString("u_create_year", regDate.substring(0,4));
      idf_PObj.setString("u_expired_date", reqDoc.getUExpiredDate().toString());
      idf_PObj.save();  
         
      // edms_doc 문서 정보 수정 
      idf_Doc.setString("u_delete_status", "R");
      idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
      idf_Doc.save();   
      
      LogDoc logDoc = LogDoc.builder()
            .uJobCode(DocLogItem.DR.getValue())
            .uDocId(reqDoc.getRObjectId())
            .uDocKey(reqDoc.getUDocKey())
            .uDocName(reqDoc.getTitle())
            .uDocVersion(docV2.get(0).getRVersionLabel())
            .uOwnDeptcode(reqDeptCode)
            .uActDeptCode(reqDeptCode)
            .uFileSize(Integer.valueOf(reqDoc.getRContentSize()))
            .uJobUser(userSession.getDUserId())
            .uJobUserType("D")
            .uDocStatus(reqDoc.getUDocStatus())
            .uSecLevel(reqDoc.getUSecLevel())
            .uCabinetCode(reqDoc.getUCabinetCode())
            .uUserIp(ip)
            .build();
      insertLog(logDoc);
      NotiConfig notiData = notiConfigDao.selectOneByCodes(reqComeCode, "DR");
      if("Y".equals(notiData.getUAlarmYn())) {
        idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
        idf_PObj2.setString("u_msg_type", "DR");
        idf_PObj2.setString("u_sender_id", userSession.getDUserId());
        idf_PObj2.setString("u_receiver_id", deptMgrId);
        idf_PObj2.setString("u_msg", "'" + reqDoc.getTitle() + "'" + " 문서의 폐기요청이 등록되었습니다.");
        idf_PObj2.setString("u_obj_id", idf_PObj.getString("r_object_id"));
        idf_PObj2.setString("u_sent_date", new DfTime().toString());
        idf_PObj2.setString("u_action_need_yn", "Y");
        idf_PObj2.save();
      } 
      if("Y".equals(notiData.getUEmailYn())) {
    	if(null !=deptMgrData.getEmail()){
	        StringBuffer content = new StringBuffer();       
	        content.append("<html> ");   
	        content.append("<body>");           
	        content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");           
	        content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; "+reqDoc.getTitle()+" 문서의 폐기요청이 등록되었습니다.</font>");  
	        content.append("<br><br>                         <font face='굴림' size=3>      <a href='"+  dboxUrl  +"/#/manager/dept/document-status?tab=2'>승인화면 바로가기</a></font>");
	        content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>"); 
	        content.append(" </body></html>");          
	        notificationService.sendMail("dbox@dongkuk.com", deptMgrEmail, "[D'Box]" + "'" + reqDoc.getTitle() + "'" + " 문서의 폐기요청이 등록되었습니다."
	        , content.toString());
    	}
      }
      if("Y".equals(notiData.getUMmsYn())) {
    	if(null !=deptMgrData.getMobileTel() && !deptMgrData.getMobileTel().equals("")) {
            String mobileTel = deptMgrData.getMobileTel().replace("-", "");
            notificationService.sendKakao(deptMgrId, mobileTel, "dbox_alarm_004", reqDoc.getTitle() + " 문서의 폐기요청이 " + "등록되었습니다.");
    	}
      }
    } catch(Exception e) {
      e.printStackTrace();
    } finally {
      if (idfSession != null  && idfSession.isConnected() && idfAdminSession != null  && idfAdminSession.isConnected()) {
        sessionRelease(userSession.getUser().getUserId(), idfSession);
        idfAdminSession.disconnect();
       }
    }
    
  }
  
  
  @Override
  public Boolean deleteReqDisposal(UserSession userSession, String rObjectId, ReqDisposalDetailDto dto, String ip) throws Exception {
    
    Boolean rst = true;
    
    if(dto.getTargetFolId() == null) {
      rst = isDeleteService.isDelete(dto.getRObjectId());
    }else {
      rst = false;
    }
    
    if(!rst) {
      Folder targetFol = new Folder();
      if(dto.getTargetFolId() != null) {
        //target
        targetFol = folderDao.selectOne(dto.getTargetFolId())
            .orElseThrow(() -> new BadRequestException("폴더가 존재하지않습니다."));
      }
      Doc reqDoc = docDao.selectOne(dto.getRObjectId())
          .orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
      String cabinetCode = reqDoc.getUCabinetCode();
      String reqDeptCode = deptDao.selectOrgIdByCabinetcode(cabinetCode);
      LocalDateTime today = LocalDateTime.now();
      List<DocRepeating> docV2 = docDao.selRepeatingOne(reqDoc.getRObjectId());
      
      IDfSession idfSession = this.getIdfSession(userSession);
      IDfSession idfAdminSession = DCTMUtils.getAdminSession(); // Superuser
      try { 
        IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(rObjectId));
        idf_PObj.destroy();
    
        IDfPersistentObject idf_Doc = idfAdminSession.getObject(new DfId(dto.getRObjectId()));
        
        if(dto.getTargetFolId() != null) {
          //target
          idf_Doc.setString("u_fol_id", targetFol.getRObjectId());
          idf_Doc.setString("u_pr_code", targetFol.getUPrCode());
          idf_Doc.setString("u_pr_type", targetFol.getUPrType());
        }
        
        // edms_doc 문서 정보 수정 
        idf_Doc.setString("u_delete_status", null);
        idf_Doc.setString("u_update_date", today.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        idf_Doc.save();   
        
        LogDoc logDoc = LogDoc.builder()
              .uJobCode(DocLogItem.DC.getValue())
              .uDocId(reqDoc.getRObjectId())
              .uDocKey(reqDoc.getUDocKey())
              .uDocName(reqDoc.getTitle())
              .uDocVersion(docV2.get(0).getRVersionLabel())
              .uOwnDeptcode(reqDeptCode)
              .uActDeptCode(reqDeptCode)
              .uFileSize(Integer.valueOf(reqDoc.getRContentSize()))
              .uJobUser(userSession.getDUserId())
              .uJobUserType("D")
              .uDocStatus(reqDoc.getUDocStatus())
              .uSecLevel(reqDoc.getUSecLevel())
              .uCabinetCode(reqDoc.getUCabinetCode())
              .uUserIp(ip)
              .build();
        insertLog(logDoc);
      
        
      } catch(Exception e) {
        e.printStackTrace();
      } finally {
        if (idfSession != null  && idfSession.isConnected() && idfAdminSession != null  && idfAdminSession.isConnected()) {
          sessionRelease(userSession.getUser().getUserId(), idfSession);
          idfAdminSession.disconnect();
         }
      }
    }
    return rst;
  
  }
  
  @Override
  public void patchReqDisposal(UserSession userSession, String rObjectId, String ip) throws Exception {
    IDfSession idfAdminSession = DCTMUtils.getAdminSession(); // Superuser
    IDfPersistentObject idf_PObj = idfAdminSession.getObject(new DfId(rObjectId));
    Doc reqDoc = docDao.selectOne(rObjectId)
        .orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));
    String cabinetCode = reqDoc.getUCabinetCode();
    String reqDeptCode = deptDao.selectOrgIdByCabinetcode(cabinetCode);
    String reqComeCode = deptDao.selectComCodeByCabinetCode(cabinetCode);
    List<DocRepeating> docV2 = docDao.selRepeatingOne(reqDoc.getRObjectId());
    PreservationPeriodDto getAutoPreserve = preservationPeriodDao.selectOneByComCode(reqComeCode);
    
    String expiredDate = null;
    LocalDateTime today = LocalDateTime.now();
    LocalDateTime delDate = LocalDateTime.of(
        today.getYear()+ Integer.valueOf(getAutoPreserve.getUAutoExtend()), 
        today.getMonth(), 
        today.getDayOfMonth(), 
        23, 59, 59, 0);
    expiredDate = delDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));  
    
    try {   
      idf_PObj.setInt("u_preserve_flag", Integer.valueOf(getAutoPreserve.getUAutoExtend()));
      idf_PObj.setString("u_expired_date", expiredDate); 
      idf_PObj.save();  
      
      LogDoc logDoc = LogDoc.builder()
            .uJobCode(DocLogItem.RC.getValue())
            .uDocId(reqDoc.getRObjectId())
            .uDocKey(reqDoc.getUDocKey())
            .uDocName(reqDoc.getTitle())
            .uDocVersion(docV2.get(0).getRVersionLabel())
            .uOwnDeptcode(reqDeptCode)
            .uActDeptCode(reqDeptCode)
            .uFileSize(Integer.valueOf(reqDoc.getRContentSize()))
            .uJobUser(userSession.getDUserId())
            .uJobUserType("D")
            .uDocStatus(reqDoc.getUDocStatus())
            .uSecLevel(reqDoc.getUSecLevel())
            .uCabinetCode(reqDoc.getUCabinetCode())
            .uUserIp(ip)
            .build();
      insertLog(logDoc);
      
    } catch(Exception e) {
      e.printStackTrace();
    } finally {
      if (idfAdminSession != null  && idfAdminSession.isConnected()) {
        idfAdminSession.disconnect();
       }
    }
    
  }



}
