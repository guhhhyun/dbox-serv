package com.dongkuksystems.dbox.models.dto.type.manager.duplication;

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
public class DuplicationDto {
	@ApiModelProperty(value = "회사코드")
	private String comOrgId;
  @ApiModelProperty(value = "부서코드")
  private String orgId;
  @ApiModelProperty(value = "부서 코드 리스트")
  private List<String> deptCodeList;
	@ApiModelProperty(value = "프로젝트/연구과제 구분")
  private String uPrType;
	@ApiModelProperty(value = "파일명")
  private String title;
	@ApiModelProperty(value = "rContentHash")
	private String rContentHash;
	@ApiModelProperty(value = "contentHashList")
  private List<String> rContentHashList;
  @ApiModelProperty(value = "데이터를 가져오기 시작할 위치")
  private String offset;
  @ApiModelProperty(value = "페이지당 갯수")
  private String limit;
}
