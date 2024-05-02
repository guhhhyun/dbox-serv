package com.dongkuksystems.dbox.models.type.manager.limit;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Limit {

	@ApiModelProperty(value = "ObjecId")
	private String rObjectId;
	
	@ApiModelProperty(value = "구분 코드")
	private String uCodeVal1;
	
	@ApiModelProperty(value = "구분 이름")
	private String uCodeName;
	
	@ApiModelProperty(value = "회사 코드")
	private String uComCode;
	
	@ApiModelProperty(value = "설정 값")
	private String uCodeVal;
}
