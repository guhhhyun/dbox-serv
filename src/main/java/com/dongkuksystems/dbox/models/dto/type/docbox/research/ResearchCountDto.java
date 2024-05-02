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
public class ResearchCountDto {
  @ApiModelProperty(value = "주관 진행중 개수")
	private int ownDoing;
  @ApiModelProperty(value = "주관 완료 개수")
	private int ownDone;
  @ApiModelProperty(value = "참여 진행중 개수")
	private int joinDoing;
  @ApiModelProperty(value = "참여 완료 개수")
	private int joinDone;
}
