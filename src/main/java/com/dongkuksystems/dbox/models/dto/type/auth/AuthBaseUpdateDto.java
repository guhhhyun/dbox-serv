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
public class AuthBaseUpdateDto {
  @ApiModelProperty(value = "rObjectId")
  private String rObjectId;
  @ApiModelProperty(value = "userId Or orgId")
  private String targetId;
  @ApiModelProperty(value = "'U': 사용자, 'D': 부서  ")
  private String type;
  @ApiModelProperty(value = "'R': 조회/다운로드, 'D': 조회/다운로드/편집/삭제")
  private String permitType;
  @ApiModelProperty(value = "추가 소스 구분 (P:속성화면추가, W:결재, S:공유/협업, G:보안등급기본)")
  private String addGubun;
}
