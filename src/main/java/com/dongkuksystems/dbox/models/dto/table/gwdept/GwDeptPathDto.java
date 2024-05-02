package com.dongkuksystems.dbox.models.dto.table.gwdept;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GwDeptPathDto {
  @ApiModelProperty(value = "부서 경로")
  private String deptPath;  
  @ApiModelProperty(value = "부서 아이디 경로")
  private String deptIdPath;  
}
