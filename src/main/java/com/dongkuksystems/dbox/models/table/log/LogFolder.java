package com.dongkuksystems.dbox.models.table.log;

import static com.google.common.base.Preconditions.checkNotNull;

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
@NoArgsConstructor
@Builder 
public class LogFolder {
  @ApiModelProperty(value = "작업 Code", required = true)
  private String uJobCode;
  @ApiModelProperty(value = "폴더 ID", required = true)
  private String uFolId;
  @ApiModelProperty(value = "폴더명", required = true)
  private String uFoldName;
  @ApiModelProperty(value = "소유부서", required = true)
  private String uOwnDeptcode;
  @ApiModelProperty(value = "실행부서", required = true)
  private String uActDeptCode;
  @ApiModelProperty(value = "문서함코드", required = true)
  private String uCabinetCode; 
  @ApiModelProperty(value = "작업자", required = true)
  private String uJobUser;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "작업 일시", required = true)
  private LocalDateTime uJobDate;
  @ApiModelProperty(value = "잠금상태", required = true)
  private String uLockStatus; 
  @ApiModelProperty(value = "보안등급", required = true)
  private String uSecLevel; 
  @ApiModelProperty(value = "변경전 값")
  private String uBeforeChangeVal; ; 
  @ApiModelProperty(value = "변경 후 값")
  private String uAfterChangeVal; 
  @ApiModelProperty(value = "하위폴더 포함여부", required = true)
  private boolean uIncludeSubFol; 
  @ApiModelProperty(value = "하위문서 포함여부", required = true)
  private boolean uIncludeSubDoc; 
  @ApiModelProperty(value = "공유대상")
  private String uShareTarget; 
  @ApiModelProperty(value = "공유대상 구분, (부서/사용자)")
  private String uShareTargetType;   
  @ApiModelProperty(value = "사용자 IP", required = true)
  private String uUserIp;
  
  public LogFolder(String uJobCode, String uFolId, String uFoldName, String uOwnDeptcode, String uActDeptCode,
      String uCabinetCode, String uJobUser, LocalDateTime uJobDate, String uLockStatus, String uSecLevel,
      String uBeforeChangeVal, String uAfterChangeVal, boolean uIncludeSubFol, boolean uIncludeSubDoc,
      String uShareTarget, String uShareTargetType, String uUserIp) {
    checkNotNull(uJobCode, "uJobCode must be provided.");
    checkNotNull(uFolId, "uFolId must be provided.");
    checkNotNull(uFoldName, "uFoldName must be provided.");
    checkNotNull(uOwnDeptcode, "uOwnDeptcode must be provided.");
    checkNotNull(uActDeptCode, "uActDeptCode must be provided.");
    checkNotNull(uCabinetCode, "uCabinetCode must be provided.");
    checkNotNull(uJobUser, "uJobUser must be provided.");
    checkNotNull(uLockStatus, "uLockStatus must be provided.");
    checkNotNull(uSecLevel, "uSecLevel must be provided.");
    checkNotNull(uIncludeSubFol, "uIncludeSubFol must be provided.");
    checkNotNull(uIncludeSubDoc, "uIncludeSubDoc must be provided.");
    checkNotNull(uUserIp, "uUserIp must be provided.");
    this.uJobCode = uJobCode;
    this.uFolId = uFolId;
    this.uFoldName = uFoldName;
    this.uOwnDeptcode = uOwnDeptcode;
    this.uActDeptCode = uActDeptCode;
    this.uCabinetCode = uCabinetCode;
    this.uJobUser = uJobUser;
    this.uJobDate = uJobDate;
    this.uLockStatus = uLockStatus;
    this.uSecLevel = uSecLevel;
    this.uIncludeSubFol = uIncludeSubFol;
    this.uIncludeSubDoc = uIncludeSubDoc;
    this.uUserIp = uUserIp;
    
    this.uShareTarget = uShareTarget;
    this.uShareTargetType = uShareTargetType;
    this.uBeforeChangeVal = uBeforeChangeVal;
    this.uAfterChangeVal = uAfterChangeVal;
  } 
  
}
