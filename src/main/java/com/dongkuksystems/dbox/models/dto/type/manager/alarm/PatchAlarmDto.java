package com.dongkuksystems.dbox.models.dto.type.manager.alarm;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class PatchAlarmDto {

  @ApiModelProperty(value = "alarm")
  String uAlarmYn;
  @ApiModelProperty(value = "email")
  String uEmailYn;
  @ApiModelProperty(value = "mms")
  String uMmsYn;

}