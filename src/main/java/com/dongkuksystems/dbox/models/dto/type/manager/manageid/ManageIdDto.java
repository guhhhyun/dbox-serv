package com.dongkuksystems.dbox.models.dto.type.manager.manageid;

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
public class ManageIdDto {
	  private String orgId;
	  private String socialPerId;
	  private String comOrgId;		  	  
	  private List<String> deptCodeList;
	  @ApiModelProperty(value = "데이터를 가져오기 시작할 위치")
	  private String offset;
	  @ApiModelProperty(value = "페이지당 갯수")
	  private String limit;
}
