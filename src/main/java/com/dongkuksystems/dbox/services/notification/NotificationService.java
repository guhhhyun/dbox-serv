package com.dongkuksystems.dbox.services.notification;

import java.util.List;

import com.dongkuksystems.dbox.constants.NotiItem;

public interface NotificationService {
  public void sendNotification(List<String> toUserIds, NotiItem notiItem, String ...params) throws Exception;
	public void sendMail(String fromUserId, List<String> toUserIds, String title, String content) throws Exception;
	public void sendKakao(String reqUserId, String callphone, String templatecode, String msg) throws Exception;
	public void sendAlarm(List<String> toUserIds, String title, String content, String pushType) throws Exception;
}
