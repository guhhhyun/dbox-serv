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
public class UpdateUserDateDto {

  @ApiModelProperty(value = "r_object_id")
  private String rObjectId;
  @ApiModelProperty(value = "적용 시작일")
  private String uStartDate;
  @ApiModelProperty(value = "적용 종료일")
  private String uEndDate;
}
