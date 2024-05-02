package com.dongkuksystems.dbox.models.dto.type.doc;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocPathDto {
	@ApiModelProperty(value = "상위폴더경로")
	private String folPath;
	@ApiModelProperty(value = "문서 이름")
	private String objectName;
	@ApiModelProperty(value = "총 경로")
	private String fullPath;
}
