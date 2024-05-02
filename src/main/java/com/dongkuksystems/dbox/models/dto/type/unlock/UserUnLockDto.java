package com.dongkuksystems.dbox.models.dto.type.unlock;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class UserUnLockDto {
	@ApiModelProperty(value = "변경 사유")
	private String unLockReason;
	@ApiModelProperty(value = "퇴사 여부")
	private boolean planToRetire;
	@ApiModelProperty(value = "휴직예정여부")
	private boolean planToLeave;
	@ApiModelProperty(value = "보안인지여부")
	private boolean knowOfSec;

}
