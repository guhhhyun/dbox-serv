package com.dongkuksystems.dbox.models.type.user;

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
public class UserLock {
  @ApiModelProperty(value = "특이사용자 id", required = true)
  private String rObjectId;
  @ApiModelProperty(value = "사용자 ID", required = true)
  private String uUserId;
  @ApiModelProperty(value = "잠금 구분")
  private String uLockType;
  @ApiModelProperty(value = "자동/수동 구분")
  private String uAutoYn;
  @ApiModelProperty(value = "잠금상태")
  private String uLockStatus;
  @ApiModelProperty(value = "결재정보")
  private String uWfInfo;
  @ApiModelProperty(value = "결재여부")
  private String uWfFlag;
  @ApiModelProperty(value = "경고 횟수")
  private String uWarnCount;
  @ApiModelProperty(value = "지정사유")
  private String uDeigReason;
  @ApiModelProperty(value = "지정자")
  private String uDeigSetter;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss", timezone="Asia/Seoul")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "지정일")
  private LocalDateTime uDesigDate; 
  @ApiModelProperty(value = "해제사유")
  private String uUndesigReason;
  @ApiModelProperty(value = "해제자")
  private String uUndesigSetter;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss", timezone="Asia/Seoul")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "해제일")
  private LocalDateTime uUndesigDate; 
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss", timezone="Asia/Seoul")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "기준초과일")
  private LocalDateTime uLimitOverDate; 
  @ApiModelProperty(value = "사용자 구분")
  private String uUserType;
  @ApiModelProperty(value = "사용자 ObjecId")  
  private String userObjectId;
  @ApiModelProperty(value = "사용자 아이디")
  private String userName;
  @ApiModelProperty(value = "부서 이름")
  private String orgNm;
  @ApiModelProperty(value = "사용자 이름")
  private String uDisplayName;
  @ApiModelProperty(value = "코드명1")
  private String uCodeName1;  
  @ApiModelProperty(value = "잠금 처리자 이름")
  private String lockUserName;  
  @ApiModelProperty(value = "안내 수")
  private String informCnt;  
  @ApiModelProperty(value = "잠금해제 처리자 이름")
  private String unLockUserName;  
  @ApiModelProperty(value = "관리자 메뉴 구분")
  private String type;  
  @ApiModelProperty(value = "부서 children여부")
  private String children;  
  
  
}
