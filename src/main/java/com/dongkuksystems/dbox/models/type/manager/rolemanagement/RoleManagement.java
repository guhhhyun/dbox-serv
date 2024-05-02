package com.dongkuksystems.dbox.models.type.manager.rolemanagement;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleManagement {
	@ApiModelProperty(value = "ObjectId")
	private String rObjectId;
	@ApiModelProperty(value = "권한종류")
	private String uAuthType;
	@ApiModelProperty(value = "권한범위")
	private String uAuthScope;
	@ApiModelProperty(value = "선택정책")
	private String uOptionVal;
	@ApiModelProperty(value = "선택여부")
	private String uSelected;
	@ApiModelProperty(value = "권한 Desc")
	private String uAuthDesc;
	@ApiModelProperty(value = "문서구분")
	private String uDocFlag;
	@ApiModelProperty(value = "Live 권한")
	private String uAuthL;
	@ApiModelProperty(value = "제한 권한")
	private String uAuthS;
	@ApiModelProperty(value = "팀내 권한")
	private String uAuthT;
	@ApiModelProperty(value = "사내 권한")
	private String uAuthC;
	@ApiModelProperty(value = "그룹사내 권한")
	private String uAuthG;
	@ApiModelProperty(value = "정렬 순서")
	private String uSortOrder;
	
	
	
	@ApiModelProperty(value = "업무 그룹")
	private String groupName;
	@ApiModelProperty(value = "업무 그룹(개인정보)")
	private String groupName2;
	@ApiModelProperty(value = "사용자 id")
	private String userId;

}
