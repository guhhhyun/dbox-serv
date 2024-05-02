package com.dongkuksystems.dbox.services.manager.disposalrequest;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.DocLogItem;
import com.dongkuksystems.dbox.daos.table.etc.gwdept.GwDeptDao;
import com.dongkuksystems.dbox.daos.table.reqdisposal.ReqDisposalDao;
import com.dongkuksystems.dbox.daos.type.doc.DocDao;
import com.dongkuksystems.dbox.daos.type.manager.disposalrequest.DisposalRequestDao;
import com.dongkuksystems.dbox.daos.type.manager.noticonfig.NotiConfigDao;
import com.dongkuksystems.dbox.daos.type.manager.preservationperiod.PreservationPeriodDao;
import com.dongkuksystems.dbox.daos.type.noti.AlarmDao;
import com.dongkuksystems.dbox.daos.type.path.PathDao;
import com.dongkuksystems.dbox.errors.BadRequestException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.deletemanage.DeleteManageDto;
import com.dongkuksystems.dbox.models.dto.type.manager.disposalrequest.DisposalRequestDto;
import com.dongkuksystems.dbox.models.dto.type.manager.preservationperiod.PreservationPeriodDto;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.table.log.LogDoc;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.doc.DocRepeating;
import com.dongkuksystems.dbox.models.type.folder.Folder;
import com.dongkuksystems.dbox.models.type.manager.disposalrequest.DisposalRequest;
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
public class DisposalRequestServiceImpl extends AbstractCommonService implements DisposalRequestService {
  private final DisposalRequestDao disposalRequestDao;
  private final ReqDisposalDao reqDisposalDao;
  private final DocDao docDao;
  private final UserService userService;
  private final GwDeptDao deptDao;
  private final PreservationPeriodDao preservationPeriodDao;
  private final NotiConfigDao notiConfigDao;
  private final NotificationService notificationService;  
  private final PathDao pathDao;
  private final AlarmDao alarmDao;
  private final IsDeleteService isDeleteService;
  private final FolderService folderService;

  public DisposalRequestServiceImpl(DisposalRequestDao disposalRequestDao, ReqDisposalDao reqDisposalDao, DocDao docDao,
      UserService userService, GwDeptDao deptDao, PreservationPeriodDao preservationPeriodDao,
      NotiConfigDao notiConfigDao, NotificationService notificationService, PathDao pathDao, AlarmDao alarmDao, IsDeleteService isDeleteService, FolderService folderService) {
    this.disposalRequestDao = disposalRequestDao;
    this.reqDisposalDao = reqDisposalDao;
    this.docDao = docDao;
    this.userService = userService;
    this.deptDao = deptDao;
    this.preservationPeriodDao = preservationPeriodDao;
    this.notiConfigDao = notiConfigDao;
    this.notificationService = notificationService;
    this.pathDao = pathDao;
    this.alarmDao = alarmDao;
    this.isDeleteService = isDeleteService;
    this.folderService = folderService;
  } 
  
  @Override
  public String restoreReqClosed(String closedRequestId, UserSession userSession, String userIp, DeleteManageDto dto) throws Exception {
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
      
      // edms_req_delete 에서 데이터 삭제
      idf_PObj = idfSession.getObject(new DfId(closedRequestId));
      idf_PObj.destroy();
      
      adSess = DCTMUtils.getAdminSession();
      if (!adSess.isTransactionActive()) {
       adSess.beginTrans();
      }
  
      IDfDocument idf_Doc = (IDfDocument) adSess.getObject(new DfId(reqClosedData.getUReqDocId()));
      idf_Doc.setString("u_delete_status", " ");
      idf_Doc.setString("u_update_date", (new DfTime()).toString());
      if ("E".equals(reqClosedData.getUReqType())) {
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
          .uUserIp(userIp)
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
      if ("Y".equals(notiData.getUAlarmYn())) {
        idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_noti");
        idf_PObj2.setString("u_msg_type", "DR");
        idf_PObj2.setString("u_sender_id", userSession.getDUserId());
        idf_PObj2.setString("u_receiver_id", reqClosedData.getUReqUser());
        idf_PObj2.setString("u_msg", "'" + reqDoc.getTitle() + "'" + " 문서가 기존문서로 복원되었습니다.");
        idf_PObj2.setString("u_obj_id", closedRequestId);
        idf_PObj2.setString("u_action_date", new DfTime().toString());
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
        content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; " + reqDoc.getTitle()
            + " 문서가 기존문서로 복원되었습니다.</font>");
        content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>");
        content.append(" </body></html>");        
        notificationService.sendMail("dbox@dongkuk.com", reciever, "[D'Box]" + "'" + reqDoc.getTitle() + "'" + " 문서가 기존문서로 복원되었습니다."
        , content.toString());
      }
      if ("Y".equals(notiData.getUMmsYn())) {
        String mobileTel = reqUser.getMobileTel().replace("-", "");
        notificationService.sendKakao(reqUser.getUserId(), mobileTel, "dbox_alarm_005", reqDoc.getObjectName() + " 문서의 폐기요청이 " + "복원되었습니다.");             
      }
      idfSession.commitTrans();
      adSess.commitTrans();
    } catch (Exception e) {
      throw e;
    } finally {
      if (idfSession!=null && idfSession.isTransactionActive()) {
        idfSession.abortTrans();
      }
      if (adSess!=null && adSess.isTransactionActive()) {
        adSess.abortTrans();         
        adSess.disconnect();
      }
      if (idfSession!=null && idfSession.isConnected()) {
        sessionRelease(userSession.getUser().getUserId(), idfSession);        
      }
    }
    return idf_PObj.getObjectId().getId();
  }


  @Override
  public List<DisposalRequest> selectDisposalRequest(DisposalRequestDto dto) {
    List<DisposalRequest> list = disposalRequestDao.selectDisposalRequest(dto);
    for(DisposalRequest dis : list) {
      String result = pathDao.selectFolderPath(dis.getUFolId());
      dis.setUFolderPath(result);
    }
    return list;
  }


}
