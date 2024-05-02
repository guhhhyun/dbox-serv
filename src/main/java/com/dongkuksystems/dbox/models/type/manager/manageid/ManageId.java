package com.dongkuksystems.dbox.models.type.manager.manageid;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ManageId {

	@ApiModelProperty(value = "ID")
	private String socialPerId;	
	@ApiModelProperty(value = "이름")
	private String displayName;
	@ApiModelProperty(value = "이메일")
	private String email;
	@ApiModelProperty(value = "회사코드")
	private String comOrgId;
	@ApiModelProperty(value = "부서이름")
	private String orgNm;
	@ApiModelProperty(value = "부서코드")
	private String orgId;
	@ApiModelProperty(value = "임직원 유형")
	private String empType;
	@ApiModelProperty(value = "직급")
	private String name;
	@ApiModelProperty(value = "회사이름")
	private String uCodeName1;
	@ApiModelProperty(value = "본부(상위그룹)")
	private String parentGroup;
	@ApiModelProperty(value = "잠금상태")
	private String userState;
	@ApiModelProperty(value = "user_lock 값")
	private String uLockStatus;
	@ApiModelProperty(value = "이력조회 id")
	private String uUserId;
	@ApiModelProperty(value = "로그인 일시 이력")
	private String uLoginDate;
	@ApiModelProperty(value = "로그인 ip 이력")
	private String uUserIp;
	@ApiModelProperty(value = "상위부서코드(부서문서함 코드 없을경우)")
	private String uDeptCode;
	@ApiModelProperty(value = "문서함코드")
	private String uCabinetCode;
	@ApiModelProperty(value = "objectId")
	private String rObjectId;
	@ApiModelProperty(value = "현채인구분")
	private String localEmpYn;	
}
