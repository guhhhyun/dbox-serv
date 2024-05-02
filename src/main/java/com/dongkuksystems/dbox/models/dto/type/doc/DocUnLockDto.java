package com.dongkuksystems.dbox.models.dto.type.doc;

import com.dongkuksystems.dbox.models.dto.type.doc.DocDescendantDto.DocDescendantDtoBuilder;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder

public class DocUnLockDto {
	@ApiModelProperty(value = "변경 사유")
	private String docReason;
	
}
