package com.dongkuksystems.dbox.models.dto.type.data;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class DataViewCheckoutDto {
  
	@ApiModelProperty(value = "문서 id", required = true)
	private String objId;
	
	@ApiModelProperty(value = "문서 id", required = true)
	private String docKey;
	
	@ApiModelProperty(value = "문서 id", required = true)
	private String sOpenContent;
	
	
	@ApiModelProperty(value = "문서 id", required = true)
	private String approveId;
	
	@ApiModelProperty(value = "문서 id", required = true)
	private String sMenu;
	
	@ApiModelProperty(value = "문서 id", required = true)
	private String rObjectId;
	
	@ApiModelProperty(value = "자동권한부여필요여부 Y N")
	private String sAutoPermitSystemYn;
	
	@ApiModelProperty(value = "문서명")
	private String objectName;
	
	@ApiModelProperty(value = "문서 Doc Key")
	private String uDocKey;
	
	@ApiModelProperty(value = "버전")
	private String versionLabel;

	@ApiModelProperty(value = "문서 최신 id", required = true)
	private String currentObjectId;
	
	@ApiModelProperty(value = "최신 id 문서명")
	private String currentObjectName;
	
	@ApiModelProperty(value = "최신 id 버전")
	private String currentVersionLabel;
	
	@ApiModelProperty(value = "선택된 id objectId")
	private String selectedObjectId;
	
	@ApiModelProperty(value = "선택된 id 문서명 (버전리스트에서 보기, 편집시)")
	private String selectedObjectName;
	
	@ApiModelProperty(value = "선택된 id 버전 (버전리스트에서 보기, 편집시)")
	private String selectedVersionLabel;
	
	@ApiModelProperty(value = "저장방식 ( 1 : 덮어쓰기, 2 : 버전갱신, 9 : 중간저장 + 덮어쓰기, 10: 중간저장 + 버전갱신 ) ")
	private String flag;
}
