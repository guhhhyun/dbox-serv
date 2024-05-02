package com.dongkuksystems.dbox.models.table.log;

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
import static com.google.common.base.Preconditions.checkNotNull;

@Data
//@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class LogDoc {
  @ApiModelProperty(value = "작업 Code", required = true)
  private String uJobCode;
  @ApiModelProperty(value = "문서 ID", required = true)
  private String uDocId;
  @ApiModelProperty(value = "문서 키", required = true)
  private String uDocKey;
  @ApiModelProperty(value = "문서명", required = true)
  private String uDocName;
  @ApiModelProperty(value = "문서버전", required = true)
  private String uDocVersion;
  @ApiModelProperty(value = "파일크기", required = true)
  private long uFileSize;
  @ApiModelProperty(value = "소유부서", required = true)
  private String uOwnDeptcode;
  @ApiModelProperty(value = "실행부서", required = true)
  private String uActDeptCode;
  @ApiModelProperty(value = "작업자", required = true)
  private String uJobUser;
  @ApiModelProperty(value = "작업자구분, P:개인, D:부서관리자, E:사별관리자, G:그룹관리자, S:시스템", required = true)
  private String uJobUserType; 
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "작업 일시", required = true)
  private LocalDateTime uJobDate;
  @ApiModelProperty(value = "문서상태  L:Live, C:Closed", required = true)
  private String uDocStatus; 
  @ApiModelProperty(value = "보안등급", required = true)
  private String uSecLevel; 
  @ApiModelProperty(value = "문서함코드", required = true)
  private String uCabinetCode; 
  @ApiModelProperty(value = "작업구분, 등록=[D:Dbox, P:PC], 수정=[W:덮어쓰기, V:버전생성], 조회=[D:Dbox, L:링크], 첨부=[O:원문, H:HTML], 보안등급 변경=[U:상향, D:하향], 복호화 반출=[S:")
  private String uJobGubun; 
  @ApiModelProperty(value = "변경전 값")
  private String uBeforeChangeVal; ; 
  @ApiModelProperty(value = "변경 후 값")
  private String uAfterChangeVal; 
  @ApiModelProperty(value = "공유대상")
  private String uShareTarget; 
  @ApiModelProperty(value = "공유대상 구분, D:부서, P:개인")
  private String uShareTargetType;   
  @ApiModelProperty(value = "사용자 IP", required = true)
  private String uUserIp; 
  @ApiModelProperty(value = "조회 파일 다운로드 시간")
  private String uViewDownTime;
  @ApiModelProperty(value = "첨부 시스템")
  private String uAttachSystem;
  
  
  
  public LogDoc(String uJobCode, String uDocId, String uDocKey, String uDocName, String uDocVersion, long uFileSize,
      String uOwnDeptcode, String uActDeptCode, String uJobUser, String uJobUserType, LocalDateTime uJobDate,
      String uDocStatus, String uSecLevel, String uCabinetCode, String uJobGubun, String uBeforeChangeVal,
      String uAfterChangeVal, String uShareTarget, String uShareTargetType, String uUserIp, String uViewDownTime, String uAttachSystem) {

    checkNotNull(uJobCode, "uJobCode must be provided.");
    checkNotNull(uDocId, "uDocId must be provided.");
    checkNotNull(uDocKey, "uDocKey must be provided.");
    checkNotNull(uDocName, "uDocName must be provided.");
//    checkNotNull(uDocVersion, "uDocVersion must be provided.");
    checkNotNull(uFileSize, "uFileSize must be provided.");
    checkNotNull(uOwnDeptcode, "uOwnDeptcode must be provided.");
    checkNotNull(uActDeptCode, "uActDeptCode must be provided.");
    checkNotNull(uJobUser, "uJobUser must be provided.");
    checkNotNull(uJobUserType, "uJobUserType must be provided.");
    checkNotNull(uDocStatus, "uDocStatus must be provided.");
    checkNotNull(uSecLevel, "uSecLevel must be provided.");
    checkNotNull(uCabinetCode, "uCabinetCode must be provided.");
    checkNotNull(uUserIp, "uUserIp must be provided.");
    
    this.uJobCode = uJobCode;
    this.uDocId = uDocId;
    this.uDocKey = uDocKey;
    this.uDocName = uDocName;
    this.uDocVersion = uDocVersion;
    this.uFileSize = uFileSize;
    this.uOwnDeptcode = uOwnDeptcode;
    this.uActDeptCode = uActDeptCode;
    this.uJobUser = uJobUser;
    this.uJobUserType = uJobUserType;
    this.uJobDate = uJobDate;
    this.uDocStatus = uDocStatus;
    this.uSecLevel = uSecLevel;
    this.uCabinetCode = uCabinetCode;
    this.uJobGubun = uJobGubun;
    this.uUserIp = uUserIp;
    
	this.uBeforeChangeVal 	= uBeforeChangeVal;
	this.uAfterChangeVal 	= uAfterChangeVal;
	this.uShareTarget 		= uShareTarget;
	this.uShareTargetType 	= uShareTargetType;
	this.uViewDownTime 		= uViewDownTime;
	this.uAttachSystem 		= uAttachSystem;
  } 
  
  
}
