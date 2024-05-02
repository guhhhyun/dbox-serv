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
public class AttachPolicyUserDto {

  @ApiModelProperty(value = "첨부정책의 r_object_id")
  private String uPolicyId;
  @ApiModelProperty(value = "서약/동의자")
  private String uUserId;
  @ApiModelProperty(value = "회사코드")
  private String uComCode;
}
