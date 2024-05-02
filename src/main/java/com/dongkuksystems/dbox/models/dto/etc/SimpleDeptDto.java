package com.dongkuksystems.dbox.models.dto.etc;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class SimpleDeptDto {
  @ApiModelProperty(value = "부서코드", required = true)
  private String orgId;
  @ApiModelProperty(value = "부서명")
  private String orgNm;
  @ApiModelProperty(value = "회사 ID")
  private String comOrgId;
}
