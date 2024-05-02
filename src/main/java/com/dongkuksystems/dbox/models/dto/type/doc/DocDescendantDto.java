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
public class DocDescendantDto {
	@ApiModelProperty(value = "문서 아이디")
	private String rObjectId;
	@ApiModelProperty(value = "문서 이름")
	private String objectName;
	@ApiModelProperty(value = "문서 크기")
	private String rContentSize;
	@ApiModelProperty(value = "경로")
	private String path;
}
