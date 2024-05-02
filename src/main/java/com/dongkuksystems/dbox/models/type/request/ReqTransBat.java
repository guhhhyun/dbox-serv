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
public class ReqTransBat {
  @ApiModelProperty(value = "등록자")
  private String uRegister;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "등록일")
  private LocalDateTime uRegistDate; 
  @ApiModelProperty(value = "이관구분")
  private String uTransType;
  @ApiModelProperty(value = "이관상태")
  private String uTransStatus;
  @ApiModelProperty(value = "요청상태")
  private String uReqStatus;
  @ApiModelProperty(value = "송신부서코드")
  private String uSendDeptCode;
  @ApiModelProperty(value = "수신부서코드")
  private String uReceiveDeptCode;
  @ApiModelProperty(value = "송신문서함코드")
  private String uSendCabinetCode;
  @ApiModelProperty(value = "수신문서함코드")
  private String uReceiveCabinetCode;
  @ApiModelProperty(value = "송신부서 폴더 ID")
  private String uSendFolId;
  @ApiModelProperty(value = "수신부서 폴더 ID")
  private String uReceiveFolId;
  @ApiModelProperty(value = "송신부서명")
  private String uSendDeptName;
  @ApiModelProperty(value = "수신부서명")
  private String uReceiveDeptName;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "이관일")
  private LocalDateTime uTransDate;
  @ApiModelProperty(value = "이관결과메세지")
  private String uTransResult;
}
