package com.dongkuksystems.dbox.models.dto.type.manager.attachpolicy;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatchAttachPolicyDto {
  @ApiModelProperty(value = "시스템명")
  private String uSystemName;
  @ApiModelProperty(value = "시스템 구분 코드1")
  private String uSystemKey1;
  @ApiModelProperty(value = "시스템 구분 코드2")
  private String uSystemKey2;
  @ApiModelProperty(value = "시스템 구분 코드3")
  private String uSystemKey3;
  @ApiModelProperty(value = "연동형태(HTML, 원문)")
  private String uAttachType;
  @ApiModelProperty(value = "보안등급구분 (~까지보이기)")
  private String uLimitSecLevel;
  @ApiModelProperty(value = "허용 문서상태")
  private String uDocStatus;
  @ApiModelProperty(value = "암호화여부")
  private boolean uEncryptFlag;
  @ApiModelProperty(value = "정책 비활성화 여부")
  private boolean uInactiveFlag;

  @ApiModelProperty(value = "외부/내부 여부")
  private boolean uExternalFlag;
  @ApiModelProperty(value = "메신저 여부")
  private boolean uMessengerFlag;
  @ApiModelProperty(value = "사용자별 적용 여부")
  private boolean uForUserFlag;
  @ApiModelProperty(value = "파일암호화 여부")
  private boolean uDrmFlag;
  @ApiModelProperty(value = "파일암호화 여부")
  private boolean uDocComplete;

}
