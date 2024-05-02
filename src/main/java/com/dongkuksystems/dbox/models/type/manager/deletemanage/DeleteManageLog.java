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
public class DeleteManageLog {
  @ApiModelProperty(value = "작업 코드")
  private String uJobCode;
  @ApiModelProperty(value = "문서ID")
  private String uDocId;
  @ApiModelProperty(value = "문서 키")
  private String uDocKey;
  @ApiModelProperty(value = "문서명")
  private String uDocName;
  @ApiModelProperty(value = "파일크기")
  private String uFileSize;
  @ApiModelProperty(value = "완전 삭제자")
  private String uJobUser;
  @ApiModelProperty(value = "완전 삭제일")
  private String uJobDate;
  @ApiModelProperty(value = "삭제자 이름")
  private String displayName;
  @ApiModelProperty(value = "삭제자 부서코드")
  private String orgId;  
  @ApiModelProperty(value = "직급(삭제자)")
  private String name;
  @ApiModelProperty(value = "이름 + 직급")
  private String userName;  
  @ApiModelProperty(value = "회사코드")
  private String comOrgId;
  @ApiModelProperty(value = "부서이름")
  private String orgNm;
}
