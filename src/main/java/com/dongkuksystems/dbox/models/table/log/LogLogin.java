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
public class LogLogin {
  @ApiModelProperty(value = "이벤트 구분  W:Web, L:Local", required = true)
  private String uLoginSource;
  @ApiModelProperty(value = "이벤트 구분", required = true)
  private String uUserId;
  @ApiModelProperty(value = "이벤트 구분", required = true)
  private String uDeptCode;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "로그인 일시")
  private LocalDateTime uLoginDate;
  @ApiModelProperty(value = "사용자 IP")
  private String uUserIp;
  @ApiModelProperty(value = "사용자 직급 -- trigger로 자동 등록")
  private String uTitleName;
  @ApiModelProperty(value = "사용자 유형 -- trigger로 자동 등록")
  private String uEmpType;
}
