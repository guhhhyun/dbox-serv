package com.dongkuksystems.dbox.models.dto.table.gwdept;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class GwManagerListDto {
	 @ApiModelProperty(value = "유저ID")
	  private String userId;
	 @ApiModelProperty(value = "유저이름")
	  private String displayName;
	 @ApiModelProperty(value = "회사ID")
	  private String comOrgId;
	 @ApiModelProperty(value = "부서ID")
	  private String ordId;
	 @ApiModelProperty(value = "하위부서ID")
	  private String gwOrgId;
	 @ApiModelProperty(value = "직책코드")
	  private String pstnCode;
	 @ApiModelProperty(value = "직책명")
	  private String pstnName;
	 
	 

	 
	 
}
