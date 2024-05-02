package com.dongkuksystems.dbox.models.type.manager;

import com.documentum.fc.client.search.convertor.JsonProperty;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Mgr {
  @ApiModelProperty(value = "ObjecId")
  private String rObjectId;
  @JsonProperty("uMgrType")
  @ApiModelProperty(value = "관리자 구분")
  private String uMgrType;
  @JsonProperty("uComCode")
  @ApiModelProperty(value = "회사코드")
  private String uComCode;
  @JsonProperty("uDeptCode")
  @ApiModelProperty(value = "부서코드")
  private String uDeptCode;
  @JsonProperty("deptName")
  @ApiModelProperty(value = "부서명")
  private String deptName;
  @JsonProperty("uUserId")
  @ApiModelProperty(value = "관리자 id")
  private String uUserId;
}
