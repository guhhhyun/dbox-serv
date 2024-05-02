package com.dongkuksystems.dbox.models.dto.etc;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class SimpleUserDto {
  @ApiModelProperty(value = "그룹웨어 ID(외부사용자는 메일주소형태)", required = true)
  private String userId;
  @ApiModelProperty(value = "이름", required = true)
  private String displayName;
  @ApiModelProperty(value = "회사코드")
  private String comOrgId;
  @ApiModelProperty(value = "부서코드")
  private String orgId;
  @ApiModelProperty(value = "부서명")
  private String orgNm;
  @ApiModelProperty(value = "직위코드")
  private String pstnCode; 
  @ApiModelProperty(value = "직위명")
  private String pstnName; 
  @ApiModelProperty(value = "직급코드")
  private String levelCode; 
  @ApiModelProperty(value = "직급명")
  private String levelName; 
  @ApiModelProperty(value = "직책코드")
  private String titleCode; 
  @ApiModelProperty(value = "직책명")
  private String titleName;
}
