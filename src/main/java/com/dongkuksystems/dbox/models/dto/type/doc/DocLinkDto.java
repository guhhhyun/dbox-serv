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
public class DocLinkDto {
	@ApiModelProperty(value = "문서 id", required = true)
  private String rObjectId;
	@ApiModelProperty(value = "문서함 코드")
  private String uCabinetCode;
	@ApiModelProperty(value = "문서 ID")
  private String uDocId;
	@ApiModelProperty(value = "문서 키")
  private String uDocKey;
	@ApiModelProperty(value = "폴더 ID")
  private String uFolId;
	@ApiModelProperty(value = "링크 종류 (W: 결재)")
  private String uLinkType;
	@ApiModelProperty(value = "생성자")
  private String uCreateUser;
	@ApiModelProperty(value = "생성일")
  private String uCreateDate;
}
