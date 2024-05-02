package com.dongkuksystems.dbox.models.table.etc;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GwJobTitle {
	@ApiModelProperty(value = "직책 코드", required = true)
	private String titleCode;
	@ApiModelProperty(value = "이름")
	private String name;

}
