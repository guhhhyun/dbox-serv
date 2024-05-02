package com.dongkuksystems.dbox.models.type.manager.graderedefinition;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GradeRedefinition {
		
	@ApiModelProperty(value = "보안등급코드타입")
	private String uCodeType;	
	
	@ApiModelProperty(value = "ObjecId")
	private String rObjectId;
	
	@ApiModelProperty(value = "보안등급")
	private String uCodeVal1;	
	
	@ApiModelProperty(value = "보안등급명칭")
	private String uCodeName1;

}
