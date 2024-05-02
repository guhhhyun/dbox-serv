package com.dongkuksystems.dbox.models.dto.type.user;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class LockUserDto {
	
  @ApiModelProperty(value = "사용자 ID")
  private String uUserId;
  @ApiModelProperty(value = "잠금구분")
  private String uLockType;
  @ApiModelProperty(value = "잠금상태")
  private String uLockStatus;
  @ApiModelProperty(value = "지정사유")
  private String uDeigReason;
  @ApiModelProperty(value = "지정자")
  private String uDeigSetter;
  @ApiModelProperty(value = "지정일")
  private String uDesigDate;
  @ApiModelProperty(value = "해제사유")
  private String uUndesigReason;
  @ApiModelProperty(value = "해제자")
  private String uUndesigSetter;
  @ApiModelProperty(value = "사용자 r_object_id")
  public String getUserObjectId;
  
}
