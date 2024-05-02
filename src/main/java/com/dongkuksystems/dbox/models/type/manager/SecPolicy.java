package com.dongkuksystems.dbox.models.type.manager;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class SecPolicy {
  @ApiModelProperty(value = "문서상태")
  private String uDocStatus;
  @ApiModelProperty(value = "보안등급")
  private String uEncryptFlag;
  @ApiModelProperty(value = "공개여부")
  private boolean uInactiveFlag;
  @ApiModelProperty(value = "개인정보포함여부")
  private boolean uPrivacyFlag;
  @ApiModelProperty(value = "권한등급")
  private String uPermitLevel;
  @ApiModelProperty(value = "DRM권한")
  private String uDrmPermit;
}
