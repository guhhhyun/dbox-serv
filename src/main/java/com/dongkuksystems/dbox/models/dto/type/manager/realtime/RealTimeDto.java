package com.dongkuksystems.dbox.models.dto.type.manager.realtime;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RealTimeDto {

  @ApiModelProperty
  private String uCompCode;
  @ApiModelProperty
  private String uDeptCode;
  @ApiModelProperty
  private String uCompanyName;
  @ApiModelProperty
  private String uDeptName;

}
