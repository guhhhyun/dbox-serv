package com.dongkuksystems.dbox.services.noti;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import org.modelmapper.ModelMapper;
import org.springframework.stereotype.Service;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.constants.NotiItem;
import com.dongkuksystems.dbox.daos.table.takeout.ReqTakeoutDao;
import com.dongkuksystems.dbox.daos.table.useusb.ReqUseUsbDao;
import com.dongkuksystems.dbox.daos.type.manager.noticonfig.NotiConfigDao;
import com.dongkuksystems.dbox.daos.type.manager.userlock.UserLockDao;
import com.dongkuksystems.dbox.daos.type.noti.AlarmDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.noti.AlarmDetailDto;
import com.dongkuksystems.dbox.models.dto.type.user.LockUserDto;
import com.dongkuksystems.dbox.models.dto.type.noti.AlarmDto;
import com.dongkuksystems.dbox.models.dto.type.noti.AlarmOneDto;
import com.dongkuksystems.dbox.models.table.etc.GwJobTitle;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.type.manager.noticonfig.NotiConfig;
import com.dongkuksystems.dbox.models.type.noti.Alarm;
import com.dongkuksystems.dbox.models.type.request.ReqUseUsb;
import com.dongkuksystems.dbox.models.type.user.UserLock;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.code.CodeService;
import com.dongkuksystems.dbox.services.manager.userlock.UserLockService;
import com.dongkuksystems.dbox.services.notification.NotificationService;
import com.dongkuksystems.dbox.services.req.ReqAuthService;
import com.dongkuksystems.dbox.services.reqdisposal.ReqDisposalService;
import com.dongkuksystems.dbox.services.seclvl.LvlDownService;
import com.dongkuksystems.dbox.services.takeout.TakeoutReqService;
import com.dongkuksystems.dbox.services.user.UserService;
import com.dongkuksystems.dbox.services.useusb.UseUsbReqService;
import com.dongkuksystems.dbox.utils.MailSenderUtils;


@Service
public class AlarmsServiceImpl extends AbstractCommonService implements AlarmsService {
	private final AlarmDao alarmDao;
	private final CodeService codeService;
	private final ReqUseUsbDao reqUseUsbDao;
	private final UseUsbReqService reqUseUsbService;
	private final TakeoutReqService reqTakeoutService;
	private final ReqTakeoutDao reqTakeoutDao;
	private final UserLockService userLockService;
	private final UserLockDao userLockDao;
	private final UserService userService;
	private final NotiConfigDao notiConfigDao;
	private final ReqAuthService reqAuthService;
	private final LvlDownService lvlDownService;
	private final ReqDisposalService reqClosedDelService;
	private final NotificationService notificationService;

	public AlarmsServiceImpl(AlarmDao alarmDao, CodeService codeService, ReqUseUsbDao reqUseUsbDao, UseUsbReqService reqUseUsbService
	    , TakeoutReqService reqTakeoutService, ReqTakeoutDao reqTakeoutDao, UserLockService userLockService, UserLockDao userLockDao
	    , UserService userService, ReqAuthService reqAuthService, LvlDownService lvlDownService, ReqDisposalService reqClosedDelService
	    , NotiConfigDao notiConfigDao, NotificationService notificationService) {
		this.alarmDao = alarmDao;
		this.codeService = codeService;
		this.reqUseUsbDao = reqUseUsbDao;
		this.reqUseUsbService = reqUseUsbService;
		this.reqTakeoutService = reqTakeoutService;
		this.reqTakeoutDao = reqTakeoutDao;
		this.userLockService = userLockService;
		this.userLockDao = userLockDao;
		this.userService = userService;
		this.reqAuthService = reqAuthService;
		this.lvlDownService = lvlDownService;		
		this.reqClosedDelService = reqClosedDelService;
		this.notiConfigDao = notiConfigDao;
		this.notificationService = notificationService;
	}

    @Override
    public List<AlarmDetailDto> getAlarmList(String userId, boolean isRequestedFromExternal) throws Exception {
        final ModelMapper modelMapper = getModelMapper();

        Map<String, String> notiItemMap = codeService.getNotiItemMap();
        Map<String, String> comCodeMap = codeService.getComCodeMap();

        List<Alarm> alarmList = alarmDao.selectAlarmDetailList(userId, isRequestedFromExternal);

        return alarmList.stream()
                .map((item) -> {
                    AlarmDetailDto alarmDetailDto = modelMapper.map(item, AlarmDetailDto.class);
                    alarmDetailDto.setUMsgName(notiItemMap.get(item.getUMsgType()));
                    VUser sender = Optional.ofNullable(item.getSenderDetail()).orElse(new VUser());
                    alarmDetailDto.setUSenderName(sender.getDisplayName());
                    alarmDetailDto.setUSenderJobTitleName(Optional.ofNullable(sender.getJobTitleDetail()).orElse(new GwJobTitle()).getName());
                    alarmDetailDto.setUSenderDeptName(sender.getOrgNm());
                    alarmDetailDto.setUSenderComName(comCodeMap.get(sender.getComOrgId()));
                    alarmDetailDto.setUReceiverName(Optional.ofNullable(item.getReceiverDetail()).orElse(new VUser()).getDisplayName());
                    alarmDetailDto.setUPerformerName(Optional.ofNullable(item.getPerformerDetail()).orElse(new VUser()).getDisplayName());
                    return alarmDetailDto;
                })
                .collect(Collectors.toList());
    }
	
	@Override
	public int getAlarmCount(String userId) {
		return alarmDao.selectAlarmCount(userId);
	}

	@Override
	public Map<String, Integer> deleteAlarm(UserSession userSession, AlarmOneDto dto) throws Exception {
		IDfSession idfSession = this.getIdfSession(userSession);
		IDfPersistentObject idfPObj = null;
		idfSession.beginTrans();
		int successCnt =0 ;
    int failCnt = 0;
    try {

      if (idfSession == null || !idfSession.isConnected()) {
        throw new Exception("DCTM Session 가져오기 실패");
      }     
      
      for(String objId : dto.getAlarmIds()) {
          try {
          idfPObj = (IDfPersistentObject) idfSession.getObject(new DfId(objId));
          idfPObj.destroy();
          if(idfPObj.isDeleted() == true) {
            successCnt++;
            continue;
          }else{
            failCnt++;
          }
          
        } catch (Exception e) {
          failCnt++;
          continue;
        }finally{
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
	@Override
	public String approveNoti(UserSession userSession, String rObjectId, String actionYn, String unLockReason,
	    String rejectReason) throws Exception {
	  // TODO 사용하지 않은 메소드
    /*
     * IDfSession idfSession = this.getIdfSession(userSession); IDfPersistentObject
     * idf_PObj2 = null; Alarm alarmData = alarmDao.selectAlarmByObjId(rObjectId);
     * VUser userData =
     * userService.selectOneByUserId(userSession.getDUserId()).orElse(new VUser());
     * String senderUserEmail = userData.getEmail(); NotiItem type =
     * NotiItem.findByValue(alarmData.getUMsgType()); switch(type) { case DR:
     * if("Y".equals(actionYn)) {
     * reqClosedDelService.approveReqClosed(alarmData.getUObjId(), userSession, "",
     * ""); }else if("N".equals(actionYn)) {
     * reqClosedDelService.rejectReqClosed(alarmData.getUObjId(), userSession, "",
     * ""); } break; case ER: if("Y".equals(actionYn)) { try {
     * reqTakeoutService.approveReqTakeout(alarmData.getUObjId(), userSession, "");
     * } catch (Exception e) { e.printStackTrace(); } }else if("N".equals(actionYn))
     * { try { reqTakeoutService.rejectReqTakeout(alarmData.getUObjId(),
     * userSession, rejectReason); } catch (Exception e) { e.printStackTrace(); } }
     * reqTakeoutDao.selectOneByReqId(actionYn); break; case UR: ReqUseUsb req =
     * reqUseUsbDao.selectOneByObjectId(alarmData.getUObjId()).orElse(new
     * ReqUseUsb()); if("Y".equals(actionYn)) { try {
     * reqUseUsbService.approveReqUseUsb(alarmData.getUObjId(), userSession, req);
     * 
     * } catch (Exception e) { e.printStackTrace(); } }else if("N".equals(actionYn))
     * { try { req.setURejectReason(rejectReason);
     * reqUseUsbService.rejectReqUseUsb(alarmData.getUObjId(), userSession, req); }
     * catch (Exception e) { e.printStackTrace(); } } break; case OR: UserLock lock
     * = userLockDao.selectOneByObjId(alarmData.getUObjId());
     * 
     * if("Y".equals(actionYn)) { try { LockUserDto lockUserDto = new LockUserDto();
     * lockUserDto.setULockStatus("U"); lockUserDto.setUUndesigReason(unLockReason);
     * VUser reqUserData =
     * userService.selectOneByUserId(lock.getUUserId()).orElse(new VUser());
     * userLockService.patchUserLock(alarmData.getUObjId(),
     * reqUserData.getRObjectId(), userSession, lockUserDto); } catch (Exception e)
     * {
     * 
     * e.printStackTrace(); } }else if("N".equals(actionYn)) { VUser reqUserData =
     * userService.selectOneByUserId(lock.getUUserId()).orElse(new VUser());
     * List<String> reqUserEmail = new ArrayList<>();
     * reqUserEmail.add(senderUserEmail); String comCode =
     * reqUserData.getComOrgId(); NotiConfig notiData =
     * notiConfigDao.selectOneByCodes(comCode, "OR");
     * if("Y".equals(notiData.getUEmailYn())) {
     * notificationService.sendMail(userSession.getUser().getEmail(), reqUserEmail,
     * "잠금해제 요청 반려건", "특이사용자" + lock.getUUserId() + "의"+ "잠금해제 요청이 반려되었습니다."); }
     * if("Y".equals(notiData.getUAlarmYn())) { idf_PObj2 = (IDfPersistentObject)
     * idfSession.newObject("edms_noti"); idf_PObj2.setString("u_msg_type", "OR");
     * idf_PObj2.setString("u_sender_id", userSession.getDUserId());
     * idf_PObj2.setString("u_receiver_id", reqUserData.getUserId());
     * idf_PObj2.setString("u_performer_id", userSession.getDUserId());
     * idf_PObj2.setString("u_action_yn", "N"); idf_PObj2.setString("u_msg", "특이사용자"
     * + lock.getUUserId() + "의"+ "잠금해제 요청이 반려되었습니다.");
     * idf_PObj2.setString("u_obj_id", alarmData.getUObjId());
     * idf_PObj2.setString("u_sent_date", new DfTime().toString());
     * idf_PObj2.save(); } // TODO 카카오톡 수정되면 바꿔야함
     * if("Y".equals(notiData.getUMmsYn())) { String mobileTel =
     * reqUserData.getMobileTel().replace("-", "");
     * notificationService.sendKakao(reqUserData.getUserId(), mobileTel,"",""); } }
     * break; case SR: if("Y".equals(actionYn)) {
     * lvlDownService.approveLvlDown(alarmData.getUObjId(), userSession); }else
     * if("N".equals(actionYn)) {
     * lvlDownService.rejectLvlDown(alarmData.getUObjId(), userSession,
     * rejectReason); } break; case PR: if("Y".equals(actionYn)) {
     * reqAuthService.approveReqAuth(alarmData.getUObjId(), userSession, "");
     * 
     * }else if("N".equals(actionYn)) {
     * reqAuthService.rejectReqAuth(alarmData.getUObjId(), userSession,
     * rejectReason); }
     * 
     * break; default: } alarmData.setUActionYn(actionYn);
     * alarmData.setUPerformerId(userSession.getDUserId());
     * alarmData.setUActionDate(LocalDateTime.now());
     */
	  return "";
	}
	@Override
	public void patchAlarmApproval(AlarmDto dto) {
		boolean isActionApproval = "approval".equals(dto.getAction());
		boolean isActionDisapproval = "disapproval".equals(dto.getAction());
		try {
			UserSession userSession = dto.getUserSession();
			String userId = userSession.getUser().getUserId();
			String action = isActionApproval ? "Y" : isActionDisapproval ? "N" : "";
			String rObjectId = dto.getRObjectId();
			IDfPersistentObject idf_PObj = this.getIdfSession(userSession).getObject(new DfId(rObjectId));
			idf_PObj.setString("u_action_yn", action);
			idf_PObj.setString("u_performer_id", userId);
			idf_PObj.setString("u_action_date", new DfTime().toString());
			idf_PObj.save();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

}
