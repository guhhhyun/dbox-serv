package com.dongkuksystems.dbox.models.type.manager.roleauth;



import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RoleAuth {
	@ApiModelProperty(value = "ObjecId")
	private String rObjectId;	
	@ApiModelProperty(value = "그룹코드")
	private String uAuthGroup;	
	@ApiModelProperty(value = "회사코드")
	private String uComCode;
	@ApiModelProperty(value = "그룹이름")
	private String uAuthName;
	@ApiModelProperty(value = "그룹설명")
	private String uAuthDesc;
	@ApiModelProperty(value = "설정대상여부")
	private String uGroupScope;
	
	@ApiModelProperty(value = "회사이름1", notes = "회사이름 같이 조회하기 위해 추가")
	private String uCodeName1;

	
	@ApiModelProperty(value = "사용자ID", notes = "같이 조회하기 위해 추가")
	private String userName;
	@ApiModelProperty(value = "사용자이름", notes = "같이 조회하기 위해 추가")
	private String displayName;
	@ApiModelProperty(value = "회사코드2", notes = "같이 조회하기 위해 추가")
	private String comOrgId;
	@ApiModelProperty(value = "부서코드", notes = "같이 조회하기 위해 추가")
	private String orgId;
	@ApiModelProperty(value = "부서이름", notes = "같이 조회하기 위해 추가")
	private String orgNm;
	@ApiModelProperty(value = "사용자ID", notes = "같이 조회하기 위해 추가")
	private String userId;
	@ApiModelProperty(value = "직책", notes = "같이 조회하기 위해 추가")
	private String name;
	@ApiModelProperty(value = "flag값", notes = "같이 조회하기 위해 추가")
	private String uConfigFlag;
	@ApiModelProperty(value = "groupName", notes = "같이 조회하기 위해 추가")
	private String groupName;
	@ApiModelProperty(value = "groupName2", notes = "flag 1인경우 사용하기 위해")
	private String groupName2;
	@ApiModelProperty(value = "groupNameG", notes = "일반문서 정책적용 안된 그룹")
	private String groupNameG;
	@ApiModelProperty(value = "groupNameP", notes = "개인정보문서 정책적용 안된 그룹")
	private String groupNameP;
	
	@ApiModelProperty(value = "usersNames", notes = "부서문서함 속한 인원")
	private String usersNames;

}
