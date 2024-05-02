package com.dongkuksystems.dbox.models.type.manager.disposalrequest;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DisposalRequest {
  @ApiModelProperty(value = "폐기요청 rObjecId")
  private String rObjectId;
  @ApiModelProperty(value = "문서함 코드")
  private String uCabinetCode;
  @ApiModelProperty(value = "문서 ID")
  private String uReqDocId;
  @ApiModelProperty(value = "문서 key")
  private String uReqDocKey;
  @ApiModelProperty(value = "요청구분")
  private String uReqType;
  @ApiModelProperty(value = "요청상태")
  private String uReqStatus;
  @ApiModelProperty(value = "요청자")
  private String uReqUser;
  @ApiModelProperty(value = "요청일")
  private String uReqDate;
  @ApiModelProperty(value = "요청사유")
  private String uReqReason;
  @ApiModelProperty(value = "승인자")
  private String uApprover;
  @ApiModelProperty(value = "승인일")
  private String uApproveDate;
  @ApiModelProperty(value = "폐기일")
  private String uDeleteDate;
  @ApiModelProperty(value = "경로")
  private String uFolderPath;
  @ApiModelProperty(value = "문서명")
  private String uDocName;
  @ApiModelProperty(value = "보안등급")
  private String uSecLevel;
  @ApiModelProperty(value = "생성연도")
  private String uCreateYear;
  @ApiModelProperty(value = "보존연한")
  private String uExpiredDate;
  @ApiModelProperty(value = "요청자 이름")
  private String displayName;
  @ApiModelProperty(value = "요청자 부서코드")
  private String orgId;
  @ApiModelProperty(value = "승인자 이름")
  private String approverName;
  @ApiModelProperty(value = "회사 코드")
  private String comOrgId;
  @ApiModelProperty(value = "부서이름")
  private String orgNm;
  @ApiModelProperty(value = "폴더 ID")
  private String uFolId;
  @ApiModelProperty(value = "문서명 + 확장자")
  private String title;
}
