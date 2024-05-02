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
public class AuthGroup {
  @ApiModelProperty(value = "권한 코드", example = "dm_group의 object_name")
  private String groupName;
  @ApiModelProperty(value = "회사코드")
  private String uComCode;
  @ApiModelProperty(value = "권한명")
  private String uAuthName;
  @ApiModelProperty(value = "할당 권한")
  private int uPermit;
  @ApiModelProperty(value = "권한설명")
  private String uAuthDesc;
}
