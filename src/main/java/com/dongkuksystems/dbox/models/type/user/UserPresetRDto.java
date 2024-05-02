package com.dongkuksystems.dbox.models.type.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class UserPresetRDto {
  @ApiModelProperty(value = "rObjectId")
  private String rObjectId;
	@ApiModelProperty(value = "권한자")
  private String author;
  @ApiModelProperty(value = "권한자 cabinetcode")
  private String authorCabinetCode;
}
