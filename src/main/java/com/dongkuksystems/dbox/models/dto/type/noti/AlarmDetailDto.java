package com.dongkuksystems.dbox.models.dto.type.noti;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;



@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AlarmDetailDto {
  @ApiModelProperty(value = "Object 키")
  private String rObjectId;
  @ApiModelProperty(value = "메세지타입")
  private String uMsgType;
  @ApiModelProperty(value = "메세지타입명")
  private String uMsgName;
  @ApiModelProperty(value = "발송자 ID")
  private String uSenderId;
  @ApiModelProperty(value = "발송자 이름")
  private String uSenderName;
  @ApiModelProperty(value = "발송자 직책")
  private String uSenderJobTitleName;
  @ApiModelProperty(value = "발송자 부서명")
  private String uSenderDeptName;
  @ApiModelProperty(value = "발송자 회사명")
  private String uSenderComName;
  @ApiModelProperty(value = "수신자 ID")
  private String uReceiverId;
  @ApiModelProperty(value = "수신자 이름")
  private String uReceiverName;
  @ApiModelProperty(value = "메세지 내용")
  private String uMsg;
  @ApiModelProperty(value = "처리대상 ID")
  private String uObjId;
  @ApiModelProperty(value = "자료 Key")
  private String uDocKey;
  @ApiModelProperty(value = "처리 여부")
  private String uActionYn;
  @ApiModelProperty(value = "처리자 ID")
  private String uPerformerId;
  @ApiModelProperty(value = "처리자 이름")
  private String uPerformerName;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "처리 일시")
  private LocalDateTime uActionDate;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "발송 일시")
  private LocalDateTime uSentDate;

  private String uDelYn;
  private String uActionNeedYn;
  private String uGroupKey;
}
