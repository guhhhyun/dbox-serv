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
public class CreateAttachPolicyUserDto {

  @ApiModelProperty(value = "r_object_id")
  private String rObjectId;
  @ApiModelProperty(value = "첨부정책의 r_object_id")
  private String uPolicyId;
  @ApiModelProperty(value = "사용자")
  private String uUserId;
  @ApiModelProperty(value = "회사코드")
  private String uComCode;
  @ApiModelProperty(value = "부서코드")
  private String uDeptCode;
  @ApiModelProperty(value = "적용 시작일")
  private String uStartDate;
  @ApiModelProperty(value = "적용 종료일")
  private String uEndDate;
  @ApiModelProperty(value = "등록자")
  private String uCreateUser;
  @ApiModelProperty(value = "등록일")
  private String uCreateDate;

}
