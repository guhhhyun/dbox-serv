package com.dongkuksystems.dbox.models.type.request;

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
public class ReqTransUser {
  @ApiModelProperty(value = "요청자")
  private String uReqUser;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "요청일")
  private LocalDateTime uReqDate; 
  @ApiModelProperty(value = "요청사유")
  private String uReqReason;
  @ApiModelProperty(value = "요청구분")
  private String uReqType;
  @ApiModelProperty(value = "요청상태")
  private String uReqStatus;
  @ApiModelProperty(value = "요청 문서/폴더 ID")
  private String uReqObjId;
  @ApiModelProperty(value = "요청 제목")
  private String uReqTitle;
  @ApiModelProperty(value = "송신부서코드")
  private String uSendDeptCode;
  @ApiModelProperty(value = "수신부서코드")
  private String uReceiveDeptCode;
  @ApiModelProperty(value = "송신부서명")
  private String uSendDeptName;
  @ApiModelProperty(value = "수신부서명")
  private String uReceiveDeptName;
  @ApiModelProperty(value = "송신부서함코드")
  private String uSendCabinetCode;
  @ApiModelProperty(value = "수신부서함코드")
  private String uReceiveCabinetCode;
  @ApiModelProperty(value = "승인자")
  private String uApprover;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "승인일")
  private LocalDateTime uApprovDate; 
}
