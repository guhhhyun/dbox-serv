package com.dongkuksystems.dbox.models.type.doc;


import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DocVersionList{
	private String rObjectId;
	private String rcontentSize;
	private String uEditorNames;
	private String approver;
	private String modifier;
	private String modifyDate;

}

