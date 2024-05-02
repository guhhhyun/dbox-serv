package com.dongkuksystems.dbox.models.type.manager.duplication;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class Duplication {
	
	@ApiModelProperty(value = "r_object_id")
	private String rObjectId;	
	@ApiModelProperty(value = "rContentHash")
	private String rContentHash;
	@ApiModelProperty(value = "회사코드")
	private String comOrgId;
	@ApiModelProperty(value = "폴더 ID")
  private String uFolId;
	@ApiModelProperty(value = "파일명")
	private String title;
	@ApiModelProperty(value = "파일size")
	private String rContentSize;
	@ApiModelProperty(value = "작성자")
	private String uRegUser;
	@ApiModelProperty(value = "작성자명+직급")
	private String uRegUserName;
	@ApiModelProperty(value = "경로")
	private String uFolderPath;
	@ApiModelProperty(value = "문서상태")
	private String uDocStatus;
	@ApiModelProperty(value = "삭제상태")
	private String uDeleteStatus;
	@ApiModelProperty(value = "Closed 처리일시")
	private String uClosedDate;
	@ApiModelProperty(value = "Closed 처리자")
	private String uCloser;
	@ApiModelProperty(value = "휴지통으로 삭제일")
	private String uRecycleDate;
	@ApiModelProperty(value = "마지막 파일 편집자")
	private String uLastEditor;

}
