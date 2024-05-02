package com.dongkuksystems.dbox.models.type.manager.deletemanage;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DeleteManage {
  @ApiModelProperty(value = "휴지통 rObjecId")
  private String rObjectId;
  @ApiModelProperty(value = "문서함 코드")
  private String uCabinetCode;
  @ApiModelProperty(value = "문서함 유형")
  private String uCabinetType;
  @ApiModelProperty(value = "자료유형")
  private String uObjType;
  @ApiModelProperty(value = "자료 id(doc-rObjectId)")
  private String uObjId;
  @ApiModelProperty(value = "삭제자")
  private String uDeleteUser;
  @ApiModelProperty(value = "삭제일")
  private String uDeleteDate;
  @ApiModelProperty(value = "doc rObjectId")
  private String docRObjectId;
  @ApiModelProperty(value = "문서명")
  private String objectName;
  @ApiModelProperty(value = "문서크기")
  private String rContentSize;
  @ApiModelProperty(value = "폴더경로")
  private String uFolderPath;
  @ApiModelProperty(value = "문서상태")
  private String uDocStatus;
  @ApiModelProperty(value = "이름(삭제자)")
  private String displayName;
  @ApiModelProperty(value = "부서코드(삭제자)")
  private String orgId;
  @ApiModelProperty(value = "직급(삭제자)")
  private String name;
  @ApiModelProperty(value = "이름 + 직급")
  private String userName;
  @ApiModelProperty(value = "회사코드")
  private String comOrgId;
  @ApiModelProperty(value = "부서이름")
  private String orgNm;
  @ApiModelProperty(value = "폴더이름")
  private String uFolName;  
  @ApiModelProperty(value = "프로젝트이름")
  private String uPjtName;
  @ApiModelProperty(value = "연구과제이름")
  private String uRschName;
  @ApiModelProperty(value = "폴더 ID")
  private String uFolId;
  @ApiModelProperty(value = "파일이름+확장자")
  private String title;  
}
