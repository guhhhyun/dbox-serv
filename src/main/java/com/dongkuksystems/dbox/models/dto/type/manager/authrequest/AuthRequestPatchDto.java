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
public class AuthRequestPatchDto {
  @ApiModelProperty(value = "rObjectId")
  String rObjectId;
  @ApiModelProperty(value = "uReqStatus")
  String uReqStatus;
  @ApiModelProperty(value = "uRejectReason")
  String uRejectReason;
  @ApiModelProperty(value = "email")
  String email;
  @ApiModelProperty(value = "uReqDocId")
  String uReqDocId;
  @ApiModelProperty(value = "uReqDocName")
  String uReqDocName;
  @ApiModelProperty(value = "uReqDeptCode")
  String uReqDeptCode;
  @ApiModelProperty(value = "uOwnDeptCode")
  String uOwnDeptCode;
  @ApiModelProperty(value = "uReqUser")
  String uReqUser;
}
