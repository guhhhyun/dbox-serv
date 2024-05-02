package com.dongkuksystems.dbox.models.dto.type.docbox.project;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProjectRepeatDto {

  @ApiModelProperty(value = "프로젝트코드")
  private String uPjtCode;  
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
