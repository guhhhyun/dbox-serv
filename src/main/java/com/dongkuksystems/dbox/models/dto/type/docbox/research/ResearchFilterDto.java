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
public class ResearchFilterDto {
  @ApiModelProperty(value = "기준 부서 코드")
	private String rDeptCode;
  @ApiModelProperty(value = "완료 여부 (완료: Y, 진행중: N), count 조회시 무시됨")
	private String uFinishYn;
  @ApiModelProperty(value = "분류폴더 아이디")
	private String uFolId;
  @ApiModelProperty(value = "이름")
  private String uRschName;
  @ApiModelProperty(value = "주관/참여 여부 (주관: O, 참여: J, 전체: 지정하지 않음), count 조회시 무시됨")
	private String ownJoin;
  @ApiModelProperty(value = "목록보기 활성화 대상 조회 여부")
	private Boolean withListOpen;
}
