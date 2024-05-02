package com.dongkuksystems.dbox.models.dto.type.manager.authrequest;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class AuthRequestCollectDto {

  @ApiModelProperty(value = "rObjectId")
  String rObjectId;
  @ApiModelProperty(value = "uReqStatus")
  String uReqStatus;
  @ApiModelProperty(value = "uReqDocId")
  String uReqDocId;
  @ApiModelProperty(value = "uReqUser")
  String uReqUser;

}
