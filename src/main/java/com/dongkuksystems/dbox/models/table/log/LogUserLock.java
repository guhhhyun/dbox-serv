package com.dongkuksystems.dbox.models.table.log;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor
@Builder 
public class LogUserLock {
  @ApiModelProperty(value = "작업자 회사코드", required = true)
  private String uComCode;
  @ApiModelProperty(value = "작업자 부서", required = true)
  private String uDeptCode;
  @ApiModelProperty(value = "작업자", required = true)
  private String uUserId;
  @ApiModelProperty(value = "잠금/해제 구분 L:잠금, U:해제", required = true)
  private String uJobType;
  @ApiModelProperty(value = "처리자", required = true)
  private String uJobUser;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "작업 일시")
  private LocalDateTime uJobDate;
  
  public LogUserLock(String uComCode, String uDeptCode, String uUserId, String uJobType, String uJobUser,
      LocalDateTime uJobDate) {
    checkNotNull(uComCode, "uComCode must be provided.");
    checkNotNull(uDeptCode, "uDeptCode must be provided.");
    checkNotNull(uUserId, "uUserId must be provided.");
    checkNotNull(uJobUser, "uJobUser must be provided.");
    checkNotNull(uJobType, "uJobType must be provided.");
    this.uComCode = uComCode;
    this.uDeptCode = uDeptCode;
    this.uUserId = uUserId;
    this.uJobType = uJobType;
    this.uJobUser = uJobUser;
    this.uJobDate = uJobDate;
  }
}
