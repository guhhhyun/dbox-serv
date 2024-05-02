package com.dongkuksystems.dbox.models.dto.type.noti;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AlarmOneDto {
  @ApiModelProperty(value = "알람 ID")
  private List<String> alarmIds;
}
