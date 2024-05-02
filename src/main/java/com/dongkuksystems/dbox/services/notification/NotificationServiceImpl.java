package com.dongkuksystems.dbox.services.notification;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.dongkuksystems.dbox.constants.NotiItem;
import com.dongkuksystems.dbox.daos.table.etc.gwdept.GwDeptDao;
import com.dongkuksystems.dbox.daos.table.etc.gwuser.GwUserDao;
import com.dongkuksystems.dbox.daos.table.kakao.KakaoDao;
import com.dongkuksystems.dbox.daos.table.mobile.device.MobileDeviceDao;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.table.mobile.MobileDevice;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.code.CodeService;
import com.dongkuksystems.dbox.services.kakao.KakaoService;
import com.dongkuksystems.dbox.utils.MailSenderUtils;
import com.dongkuksystems.dbox.utils.PushUtils;

@Service
public class NotificationServiceImpl extends AbstractCommonService implements NotificationService {
  private final GwDeptDao gwDeptDao;
  private final GwUserDao gwUserDao;
  private final KakaoService kakaoService;
  @Autowired
  private final MailSenderUtils mailSenderUtils;

	public NotificationServiceImpl(GwDeptDao gwDeptDao, GwUserDao gwUserDao, MobileDeviceDao mobileDeviceDao,
	    MailSenderUtils mailSenderUtils, KakaoService kakaoService) {
		this.gwDeptDao = gwDeptDao;
		this.gwUserDao = gwUserDao;
		this.mailSenderUtils = mailSenderUtils;
		this.kakaoService = kakaoService;
	}
	
	@Override
	public void sendNotification(List<String> toUserIds, NotiItem notiItem, String ...params) throws Exception {
	  switch (notiItem) {
    case FB_W:
      //등록 작성자 List
      break;
    case FB_C:
      //등록 작성자 List
      break;
    case TR:
      //자료이관, 괒 ㅔ주관부서 변경  -> 송수신 팀장
      break;
    case SC:
      break;
    case OU:
      break;
    case DR:
      //closed 폐기 요청/ 승인 -> 요청자, 승인자      
      break;
    case ER:
      //복호화 반출 요청/승인 -> 요청자 및 승인자
      break;
    case UR:
      //외부 저장매체 요청 / 승인 -> 요청자 및 승인자
      break;
    case OR:
      break;
    case SR:
      break;
    case PR:
      break;
    case SH:
      break;
    default:
      break;
    }
	  logger.info("sendNotification!!");
	}

	@Override
	public void sendMail(String fromUserId, List<String> toUserIds, String title, String content) throws Exception {
		mailSenderUtils.sendMailForHtml(toUserIds.toArray(new String[toUserIds.size()]), title, content, fromUserId);
	}

  @Override
	public void sendKakao(String reqUserId, String callphone, String templatecode, String msg) throws Exception {
		kakaoService.insertKakao(reqUserId, callphone, templatecode, msg);
	}

  @Override
  public void sendAlarm(List<String> toUserIds, String title, String content, String pushType) throws Exception {
    
  }
	

}
