package com.dongkuksystems.dbox.models.dto.type.docbox.research;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ResearchRepeatDto {

  @ApiModelProperty(value = "리서치 코드")
  private String uRschCode;  
	@ApiModelProperty(value = "ObjectId")
	private String rObjectId;
	
	@ApiModelProperty(value = "참여부서 읽기권한 orgId")
	private String uJoinDeptRead;	
	@ApiModelProperty(value = "참여부서 읽기권한 cabinet")
	private String uJoinDeptReadCabinet;	
	
  @ApiModelProperty(value = "참여부서 쓰기권한 orgId")
  private String uJoinDeptDel;	
  @ApiModelProperty(value = "참여부서 쓰기권한 cabinet")
  private String uJoinDeptDelCabinet;
}
