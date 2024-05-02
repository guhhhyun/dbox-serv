package com.dongkuksystems.dbox.models.type.user;

import java.time.LocalDateTime;

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
public class Noti {
  @ApiModelProperty(value = "알람구분", required = true)
  private String uMsgType;
  @ApiModelProperty(value = "발송자 ID")
  private String uSenderId;
  @ApiModelProperty(value = "수신자 ID")
  private String uReceiverId;
  @ApiModelProperty(value = "메시지")
  private String uMsg;
  @ApiModelProperty(value = "문서 ID")
  private String uDocId;
  @ApiModelProperty(value = "문서 키")
  private String uDocKey;
  @ApiModelProperty(value = "처리여부")
  private String uActionYn;
  @ApiModelProperty(value = "처리자 ID")
  private String uPerformerId;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "처리 일시")
  private LocalDateTime uActionDate; 
}
