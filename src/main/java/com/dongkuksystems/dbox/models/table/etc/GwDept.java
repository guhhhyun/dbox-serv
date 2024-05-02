package com.dongkuksystems.dbox.models.table.etc;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GwDept {
  @ApiModelProperty(value = "회사 ID")
  private String comOrgId;
  @ApiModelProperty(value = "회사 Nm")
  private String comOrgNm;
  @ApiModelProperty(value = "부서코드", required = true)
  private String orgId;
  @ApiModelProperty(value = "부서명")
  private String orgNm;
  @ApiModelProperty(value = "지역코드")
  private String regionCode;
  @ApiModelProperty(value = "상위부서코드")
  private String upOrgId;
  @ApiModelProperty(value = "팀장사용자 ID")
  private String managerPerId;
  @ApiModelProperty(value = "정렬키")
  private String sortKey;
  @ApiModelProperty(value = "사용여부")
  private String usageState;
  @ApiModelProperty(value = "전체부서고유키값")
  private String unitFullId;
  @ApiModelProperty(value = "전체부서명")
  private String unitFullName;
  @ApiModelProperty(value = "전체부서정렬")
  private String unitFullsortKey;
  @ApiModelProperty(value = "사이트ID")
  private String siteId;
  @ApiModelProperty(value = "부서구분코드")
  private String unitTypeCd;
  @ApiModelProperty(value = "해외근무여부")
  private String overseaWork;
  @ApiModelProperty(value = "부서타입")
  private String orgType;
  @Default
  @ApiModelProperty(value = "4")
  private String communityId = "d4c5862ab1c05245b7863757d659d4ae";
}
