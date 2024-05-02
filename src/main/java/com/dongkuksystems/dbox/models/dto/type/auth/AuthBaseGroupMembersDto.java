package com.dongkuksystems.dbox.models.dto.type.auth;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class AuthBaseGroupMembersDto {
  @ApiModelProperty(value = "orgID")
  private String orgId;
  @ApiModelProperty(value = "orgNm")
  private String orgNm;
  @ApiModelProperty(value = "org Members ")
  private String groupMembers; 
}
