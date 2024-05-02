package com.dongkuksystems.dbox.models.type.noti;

import java.time.LocalDateTime;

import com.dongkuksystems.dbox.models.table.etc.VUser;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Alarm {
	private String rObjectId;
	private String uMsgType;
	private String uSenderId;
	private String uReceiverId;
	private String uMsg;
	private String uObjId;
	private String uDocKey;
	private String uActionYn;
	private String uPerformerId;
	private LocalDateTime uActionDate;
	private LocalDateTime uSentDate;

	private String uDelYn;
	private String uActionNeedYn;
	private String uGroupKey;

	private VUser senderDetail;
	private VUser receiverDetail;
	private VUser performerDetail;
}
