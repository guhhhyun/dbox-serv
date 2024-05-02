package com.dongkuksystems.dbox.models.type.code;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class CodeLogviewAuth{
	@ApiModelProperty(value = "Code")
	private Code code;
	@ApiModelProperty(value = "이력 항목명")
	private String codeName;
	@ApiModelProperty(value = "추가 사용자 명수")
	private int cnt;
}


