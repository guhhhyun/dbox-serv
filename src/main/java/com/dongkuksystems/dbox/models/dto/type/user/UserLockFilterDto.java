package com.dongkuksystems.dbox.models.dto.type.user;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class UserLockFilterDto {
  @ApiModelProperty(value = "관리자 메뉴 구분")
  private String type;
  @ApiModelProperty(value = "잠금 정보")
  private String uLockInfo;
  @ApiModelProperty(value = "잠금 구분")
  private String uLockType;
  @ApiModelProperty(value = "잠금 상태")
  private String uLockStatus;
  @ApiModelProperty(value = "회사 코드")
  private String uCodeValue1;
  @ApiModelProperty(value = "부서 코드")
  private String orgId;
  @ApiModelProperty(value = "부서 코드 리스트")
  private List<String> deptCodeList;
  @ApiModelProperty(value = "사용자")
  private String uUserId;
  @ApiModelProperty(value = "사용자 구분")
  private String uUserType;
  @ApiModelProperty(value = "기준 초과일 검색 시작일")
  private String overStartDate;
  @ApiModelProperty(value = "기준 초과일 검색 시작일")
  private String overEndDate;
  @ApiModelProperty(value = "잠금일 검색 시작일")
  private String desigStartDate;
  @ApiModelProperty(value = "잠금일 검색 종료일")
  private String desigEndDate;
  @ApiModelProperty(value = "해제일 검색 시작일")
  private String undesigStartDate;
  @ApiModelProperty(value = "해제일 검색 종료일")
  private String undesigEndDate;
  
  
  
  
  
  
  
  
  
}
