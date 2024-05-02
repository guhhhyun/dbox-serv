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
public class GwSelectDeptManagerDto {
	 @ApiModelProperty(value = "부서ID")
	  private String orgId;
	 @ApiModelProperty(value = "부서이름")
	  private String orgNm;
	 @ApiModelProperty(value = "팀장ID")
	  private String managerPerId;
	 @ApiModelProperty(value = "상태값")
	  private String usageState;
	 @ApiModelProperty(value = "IDPath")
	  private String unitFullId;
	 @ApiModelProperty(value = "IDPathname")
	  private String unitFullName;
}
