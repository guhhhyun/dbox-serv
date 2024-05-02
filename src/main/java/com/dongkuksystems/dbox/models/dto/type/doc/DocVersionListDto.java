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
public class DocVersionListDto {
	@ApiModelProperty(value = "문서 아이디")
	private String rObjectId;
	@ApiModelProperty(value = "파일명")
	private String objectName;
	@ApiModelProperty(value = "문서 크기")
	private String contentsize;
	@ApiModelProperty(value = "작성자")
	private String uEditorNames;
	@ApiModelProperty(value = "결재자")
	private String modifier;
	@ApiModelProperty(value = "수정일")
	private String modifyDate;
	@ApiModelProperty(value = "복호화반출여부")
	private boolean uTakeoutFlag;
	@ApiModelProperty(value = "버전유지여부")
	private boolean uVerKeepFlag;
	@ApiModelProperty(value = "버전")
  	private String rVersionLabel;

	@ApiModelProperty(value = "최종결재자")
	private String approver;

}

