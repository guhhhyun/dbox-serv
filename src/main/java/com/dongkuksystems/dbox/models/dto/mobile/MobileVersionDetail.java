package com.dongkuksystems.dbox.models.dto.mobile;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class MobileVersionDetail {
  @ApiModelProperty(value = "ObjecId")
  private String rObjectId;
  @ApiModelProperty(value = "구분코드")
  private String uCodeType;
  @ApiModelProperty(value = "구분명")
  private String uTypeName;
  @ApiModelProperty(value = "버전")
  private String version;
  @ApiModelProperty(value = "ios url")
  private String iosUrl;
  @ApiModelProperty(value = "android url")
  private String androidUrl;
  @ApiModelProperty(value = "refresh")
  private String refreshYn;
}
