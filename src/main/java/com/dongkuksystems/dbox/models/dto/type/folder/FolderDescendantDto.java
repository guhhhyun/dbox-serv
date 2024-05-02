package com.dongkuksystems.dbox.models.dto.type.folder;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class FolderDescendantDto {
	@ApiModelProperty(value = "폴더 아이디")
	private String rObjectId;
	@ApiModelProperty(value = "폴더이름")
	private String uFolName;
	@ApiModelProperty(value = "폴더태그")
	private String uFolTag;
	@ApiModelProperty(value = "경로")
	private String path;
}
