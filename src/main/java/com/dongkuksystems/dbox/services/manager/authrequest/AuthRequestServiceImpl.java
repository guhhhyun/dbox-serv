package com.dongkuksystems.dbox.services.manager.authrequest;

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
import com.dongkuksystems.dbox.daos.table.req.ReqAuthDao;
import com.dongkuksystems.dbox.daos.type.doc.DocDao;
import com.dongkuksystems.dbox.daos.type.manager.authrequest.AuthRequestDao;
import com.dongkuksystems.dbox.daos.type.manager.noticonfig.NotiConfigDao;
import com.dongkuksystems.dbox.daos.type.noti.AlarmDao;
import com.dongkuksystems.dbox.errors.BadRequestException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.authrequest.AuthRequestCollectDto;
import com.dongkuksystems.dbox.models.dto.type.manager.authrequest.AuthRequestPatchDto;
import com.dongkuksystems.dbox.models.dto.type.manager.authrequest.AuthRequestUserDto;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.table.log.LogDoc;
import com.dongkuksystems.dbox.models.type.doc.Doc;
import com.dongkuksystems.dbox.models.type.doc.DocRepeating;
import com.dongkuksystems.dbox.models.type.manager.authrequest.AuthRequest;
import com.dongkuksystems.dbox.models.type.manager.noticonfig.NotiConfig;
import com.dongkuksystems.dbox.models.type.noti.Alarm;
import com.dongkuksystems.dbox.models.type.request.ReqAuth;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.notification.NotificationService;
import com.dongkuksystems.dbox.services.user.UserService;
import com.dongkuksystems.dbox.utils.MailSenderUtils;
import com.dongkuksystems.dbox.utils.dctm.DCTMUtils;

@Service
public class AuthRequestServiceImpl extends AbstractCommonService implements AuthRequestService {

	private final AuthRequestDao authRequestDao;
	private final MailSenderUtils mailSenderUtils;
  private final NotificationService notificationService;
  private final DocDao docDao;
  private final ReqAuthDao reqAuthDao;
  private final UserService userService;
  private final AlarmDao alarmDao;
  private final NotiConfigDao notiConfigDao;
  private final GwDeptDao deptDao;
  
	public AuthRequestServiceImpl(AuthRequestDao authRequestDao, MailSenderUtils mailSenderUtils, NotificationService notificationService, DocDao docDao,
	    ReqAuthDao reqAuthDao, UserService userService, AlarmDao alarmDao, NotiConfigDao notiConfigDao, GwDeptDao deptDao) {
		this.authRequestDao = authRequestDao;
		this.mailSenderUtils = mailSenderUtils;
		this.notificationService = notificationService;
		this.docDao = docDao;
		this.reqAuthDao = reqAuthDao;
		this.userService = userService;
		this.alarmDao = alarmDao;
		this.notiConfigDao = notiConfigDao;
		this.deptDao = deptDao;
	}
	
	@Override
	public List<AuthRequest> selectAuthRequest(AuthRequestUserDto authRequestUserDto) {
		return authRequestDao.selectAuthRequest(authRequestUserDto);
	}
	
	@Override
	public List<AuthRequest> selectAuthWithdrawal(AuthRequestUserDto authRequestUserDto) {
		return authRequestDao.selectAuthWithdrawal(authRequestUserDto);
	}
	
	@Override
	public void updateAuthWithdrawal(AuthRequestPatchDto dto, UserSession userSession, String ip) throws Exception {
		IDfSession idfSession = this.getIdfSession(userSession);
    IDfSession idfAdminSession = null;
    List<String> reciever = new ArrayList<>();    
    reciever.add(dto.getEmail());
    Doc reqDoc = docDao.selectOne(dto.getUReqDocId()).orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));    
    List<DocRepeating> docV2 = docDao.selRepeatingOne(reqDoc.getRObjectId());
    ReqAuth reqData = reqAuthDao.dataByObjId(dto.getRObjectId());
    VUser reqUser = userService.selectOneByUserId(reqData.getUReqUser()).orElse(null);
    String cabinetCode = reqDoc.getUCabinetCode();
    String reqDeptCode = deptDao.selectOrgIdByCabinetcode(cabinetCode);
    String reqComeCode = deptDao.selectComCodeByCabinetCode(cabinetCode);
    
    IDfPersistentObject idf_PObj = null;
    IDfPersistentObject idf_PObj2 = null;
    IDfPersistentObject idf_PObj3 = null;
    IDfPersistentObject idf_PObj4 = null;
    
    try {
      idfAdminSession = DCTMUtils.getAdminSession(); // Superuser

      // A:승인 일 때
      if (dto.getUReqStatus().equals("A")) {
        String rObjectId = dto.getRObjectId();
        // edms_req_auth 값 update
        idf_PObj = (IDfPersistentObject) idfSession.getObject(new DfId(rObjectId));
        idf_PObj.setString("u_req_status", "A");
        idf_PObj.setString("u_approver", userSession.getDUserId());
        idf_PObj.setString("u_action_date", (new DfTime()).toString());
        idf_PObj.save();

        // edms_auth_base에 추가
        idf_PObj2 = (IDfPersistentObject) idfSession.newObject("edms_auth_base");
        idf_PObj2.setString("u_obj_id", dto.getUReqDocId());
        idf_PObj2.setString("u_obj_type", "D");
        idf_PObj2.setString("u_doc_status", reqDoc.getUDocStatus());
        if (idf_PObj.getInt("u_req_permit") == 3) {
          idf_PObj2.setString("u_permit_type", "R");
        }
        if (dto.getUReqDeptCode().equals(dto.getUOwnDeptCode())) {
          idf_PObj2.setString("u_own_dept_yn", "Y");
        } else if (!(dto.getUReqDeptCode().equals(dto.getUOwnDeptCode()))) {
          idf_PObj2.setString("u_own_dept_yn", "N");
        }
        idf_PObj2.setString("u_author_id", dto.getUReqUser());
        idf_PObj2.setString("u_author_type", "U");
        idf_PObj2.setString("u_create_user", userSession.getDUserId());
        idf_PObj2.setString("u_create_date", (new DfTime()).toString());
        idf_PObj2.save();

        // acl 추가
        IDfDocument idf_Doc = (IDfDocument) idfAdminSession.getObject(new DfId(reqData.getUReqDocId()));
        if(reqData.getUReqDocId() != null) {
        idf_Doc.grant(dto.getUReqUser(), reqData.getUReqPermit(), "");
        idf_Doc.save();
        }
        
        LogDoc logDoc = LogDoc.builder()
            .uJobCode(DocLogItem.PA.getValue())
            .uDocId(reqDoc.getRObjectId())
            .uDocKey(reqDoc.getUDocKey())
            .uDocName(reqDoc.getTitle())
            .uDocVersion(docV2.get(0).getRVersionLabel())
            .uOwnDeptcode(dto.getUOwnDeptCode())
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

        
        List<Alarm> alarmData = alarmDao.selectOneByObjId(dto.getRObjectId(), reqData.getUReqUser(), "PR");
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
           idf_PObj4 = (IDfPersistentObject) idfSession.newObject("edms_noti");
           idf_PObj4.setString("u_msg_type", "PR");
           idf_PObj4.setString("u_sender_id", userSession.getDUserId());
           idf_PObj4.setString("u_receiver_id", reqData.getUReqUser());
           idf_PObj4.setString("u_performer_id", userSession.getDUserId());
           idf_PObj4.setString("u_action_yn", "Y");
           idf_PObj4.setString("u_action_need_yn", "N");
           idf_PObj4.setString("u_msg", "'" + reqDoc.getTitle() + "'" + " 문서의 조회/다운로드 권한요청이 승인되었습니다.");
           idf_PObj4.setString("u_obj_id", dto.getUReqDocId());
           idf_PObj4.setString("u_action_date", new DfTime().toString());
           idf_PObj4.setString("u_sent_date", new DfTime().toString());
           idf_PObj4.save();
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
      } else if (dto.getUReqStatus().equals("D")) {
        // D:반려 일 때
        String rObjectId = dto.getRObjectId();
        idf_PObj = (IDfPersistentObject) idfSession.getObject(new DfId(rObjectId));
        idf_PObj.setString("u_req_status", "D");
        idf_PObj.setString("u_reject_reason", dto.getURejectReason());
        idf_PObj.setString("u_approver", userSession.getDUserId());
        idf_PObj.setString("u_action_date", (new DfTime()).toString());
        idf_PObj.save();

        List<Alarm> alarmData = alarmDao.selectOneByObjId(dto.getRObjectId(), reqData.getUReqUser(), "PR");
        for(int i=0;i<alarmData.size(); i++) {
          idf_PObj2 = (IDfPersistentObject) idfSession.getObject(new DfId(alarmData.get(i).getRObjectId()));
          idf_PObj2.setString("u_action_yn", "N");
          idf_PObj2.setString("u_action_date", new DfTime().toString());
          idf_PObj2.save();
        }
        
        NotiConfig notiData = notiConfigDao.selectOneByCodes(reqComeCode, "PR");
        if("Y".equals(notiData.getUAlarmYn())) {
           idf_PObj3 = (IDfPersistentObject) idfSession.newObject("edms_noti");
           idf_PObj3.setString("u_msg_type", "PR");
           idf_PObj3.setString("u_sender_id", userSession.getDUserId());
           idf_PObj3.setString("u_receiver_id", reqData.getUReqUser());
           idf_PObj3.setString("u_performer_id", userSession.getDUserId());
           idf_PObj3.setString("u_action_yn", "N");
           idf_PObj3.setString("u_action_need_yn", "N");
           idf_PObj3.setString("u_msg", "'" + reqDoc.getTitle() + "'" + " 문서의 조회/다운로드 권한요청이 반려되었습니다.");
           idf_PObj3.setString("u_obj_id", dto.getUReqDocId());
           idf_PObj3.setString("u_action_date", new DfTime().toString());
           idf_PObj3.setString("u_sent_date", new DfTime().toString());
           idf_PObj3.save();
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
      }
    } catch (Exception e) {
      throw e;
    } finally {
      if (idfAdminSession != null && idfAdminSession.isConnected()) {
        idfAdminSession.disconnect();
      }
      if(idfSession != null && idfSession.isConnected()) {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
      }
    }		
	}
	
	
	@Override
	public void collectAuthWithdrawal(AuthRequestCollectDto dto, UserSession userSession) throws Exception {
		IDfSession idfSession = this.getIdfSession(userSession);
		IDfSession idfAdminSession = null;
    List<AuthRequest> objectIdList = authRequestDao.selectObjectId(dto.getUReqDocId());

    Doc reqDoc = docDao.selectOne(dto.getUReqDocId()).orElseThrow(() -> new BadRequestException("문서가 존재하지않습니다."));    
    List<DocRepeating> docV2 = docDao.selRepeatingOne(reqDoc.getRObjectId());
    ReqAuth reqData = reqAuthDao.dataByObjId(dto.getRObjectId());
    VUser reqUser = userService.selectOneByUserId(reqData.getUReqUser()).orElse(null);
    String cabinetCode = reqDoc.getUCabinetCode();
    String reqDeptCode = deptDao.selectOrgIdByCabinetcode(cabinetCode);
    String reqComeCode = deptDao.selectComCodeByCabinetCode(cabinetCode);
    List<String> reciever = new ArrayList<>();    
    reciever.add(reqUser.getEmail());
    
    try {
      idfAdminSession = DCTMUtils.getAdminSession(); // Superuser

      if (dto.getUReqStatus().equals("A")) {
        String rObjectId = dto.getRObjectId();
        IDfPersistentObject idf_PObj = idfSession.getObject(new DfId(rObjectId));
        idf_PObj.setString("u_req_status", "C");
        idf_PObj.setString("u_approver", userSession.getDUserId());
        idf_PObj.setString("u_action_date", (new DfTime()).toString());
        idf_PObj.save();

        // edms_auth_base에서 삭제
        String s_ObjId = objectIdList.get(0).getRObjectId();
        IDfPersistentObject idf_PObj2 = idfSession.getObject(new DfId(s_ObjId));
        idf_PObj2.destroy();

        // acl 제거
        IDfDocument idf_Doc = (IDfDocument) idfAdminSession.getObject(new DfId(dto.getUReqDocId()));
        idf_Doc.revoke(dto.getUReqUser(), null);
        idf_Doc.save();
        
        List<Alarm> alarmData = alarmDao.selectOneByObjId(dto.getRObjectId(), reqData.getUReqUser(), "PR");
        for(int i=0;i<alarmData.size(); i++) {
          IDfPersistentObject idf_PObj3 = idfSession.getObject(new DfId(alarmData.get(i).getRObjectId()));
          idf_PObj3.setString("u_action_yn", "N");
          idf_PObj3.setString("u_action_date", new DfTime().toString());
          idf_PObj3.save();
        }
        
        NotiConfig notiData = notiConfigDao.selectOneByCodes(reqComeCode, "PR");
        if("Y".equals(notiData.getUAlarmYn())) {
           IDfPersistentObject idf_PObj4 = idfSession.newObject("edms_noti");
           idf_PObj4.setString("u_msg_type", "PR");
           idf_PObj4.setString("u_sender_id", userSession.getDUserId());
           idf_PObj4.setString("u_receiver_id", reqData.getUReqUser());
           idf_PObj4.setString("u_performer_id", userSession.getDUserId());
           idf_PObj4.setString("u_action_yn", "N");
           idf_PObj4.setString("u_action_need_yn", "N");
           idf_PObj4.setString("u_msg", "'" + reqDoc.getTitle() + "'" + " 문서의 조회/다운로드 권한이 회수되었습니다.");
           idf_PObj4.setString("u_obj_id", dto.getUReqDocId());
           idf_PObj4.setString("u_action_date", new DfTime().toString());
           idf_PObj4.setString("u_sent_date", new DfTime().toString());
           idf_PObj4.save();
        }
        if("Y".equals(notiData.getUEmailYn())) {
          StringBuffer content = new StringBuffer();       
          content.append("<html> ");   
          content.append("<body>");           
          content.append("                                 <font face='굴림' size=3>      &nbsp; D'Box에서 알려드립니다.</font>");           
          content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; "+reqDoc.getTitle()+" 문서의 조회/다운로드 권한이 회수되었습니다.</font>");             
          content.append("<br><br>                         <font face='굴림' size=3>      &nbsp; 감사합니다.</font>"); 
          content.append(" </body></html>");          
          notificationService.sendMail("dbox@dongkuk.com", reciever, "[D'Box]" + "'" + reqDoc.getTitle() + "'" + " 문서의 조회/다운로드 권한이 회수되었습니다."
          , content.toString());
        }
        if("Y".equals(notiData.getUMmsYn())) {
          String mobileTel = reqUser.getMobileTel().replace("-", "");
          notificationService.sendKakao(reqData.getUReqUser(), mobileTel, "dbox_alarm_011", reqDoc.getTitle() + " 문서의 권한이"+" 회수"+"되었습니다.");               
      }                 
      }
    } catch (Exception e) {
      throw e;
    } finally {
      if (idfAdminSession != null && idfAdminSession.isConnected()) {
        idfAdminSession.disconnect();
      }
      if(idfSession != null && idfSession.isConnected()) {
      sessionRelease(userSession.getUser().getUserId(), idfSession);
      }
    }
  }		        	
}
