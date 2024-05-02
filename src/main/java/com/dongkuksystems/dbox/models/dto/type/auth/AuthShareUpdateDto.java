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
public class AuthShareUpdateDto {
  @ApiModelProperty(value = "rObjectId")
  private String rObjectId;
  @ApiModelProperty(value = "userId Or orgId")
  private String targetId;
  @ApiModelProperty(value = "'U': 사용자, 'D': 부서  ")
  private String type;
  @ApiModelProperty(value = "'R': 조회/다운로드, 'D': 조회/다운로드/편집/삭제")
  private String permitType;
}
