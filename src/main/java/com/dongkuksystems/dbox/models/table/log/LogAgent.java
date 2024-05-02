package com.dongkuksystems.dbox.models.table.log;

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
public class LogAgent {
  @ApiModelProperty(value = "이벤트 구분  W:Web, L:Local", required = true)
  private String uAgentStatus;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "로그인 일시", required = true)
  private LocalDateTime uLogDate;
  @ApiModelProperty(value = "사용자ID", required = true)
  private String uUserId;
  @ApiModelProperty(value = "사용자명")
  private String uUserName;
  @ApiModelProperty(value = "부서")
  private String uDeptCode;
  @ApiModelProperty(value = "부서명")
  private String uDeptName;
  @ApiModelProperty(value = "회사")
  private String uComCode;
  @ApiModelProperty(value = "회사명")
  private String uComName;
  @ApiModelProperty(value = "사용자 Mac Address")
  private String uUserMacAddress;
  @ApiModelProperty(value = "사용자 Agent 버전")
  private String uUserAgentVersion;
}
