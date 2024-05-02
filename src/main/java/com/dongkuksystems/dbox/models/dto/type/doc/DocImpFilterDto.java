package com.dongkuksystems.dbox.models.dto.type.doc;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class DocImpFilterDto {
  @ApiModelProperty(value = "캐비넷코드")
	private String uCabinetCode;
  @ApiModelProperty(value = "상위 폴더 아이디 ")
  private String uFolId;
  @ApiModelProperty(value = "프로젝트 코드", hidden = true)
  private String uPrCode;
  @ApiModelProperty(value = "프로젝트 타입", hidden = true)
  private String uPrType;
  @ApiModelProperty(value = "상위 함 타입", example = "P:프로젝트, R:연구과제, D:부서, F:folder", required = true)
  private String hamType;
  
}
