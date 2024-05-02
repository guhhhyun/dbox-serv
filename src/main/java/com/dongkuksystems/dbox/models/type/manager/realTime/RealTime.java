package com.dongkuksystems.dbox.models.type.manager.realTime;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RealTime {	
	
	@ApiModelProperty(value = "ObjecId")
	private String rObjectId;	
	
	@ApiModelProperty(value = "구분코드")
	private String uCodeType;
	
	@ApiModelProperty(value = "구분명")
	private String uTypeName;
	
	@ApiModelProperty(value = "코드1")
	private String uCodeVal1;
	
	@ApiModelProperty(value = "코드2")
	private String uCodeVal2;
	
	@ApiModelProperty(value = "코드3")
	private String uCodeVal3;
	
	@ApiModelProperty(value = "코드명1")
	private String uCodeName1;
	
	@ApiModelProperty(value = "코드명2")
	private String uCodeName2;
	
	@ApiModelProperty(value = "코드명3")
	private String uCodeName3;
	
	@ApiModelProperty(value = "설명")
	private String uCodeDesc;
	
	@ApiModelProperty(value = "생성자")
	private String uCreateUser;
	
	@ApiModelProperty(value = "생성일")
	private String uCreateDate;
	
	@ApiModelProperty(value = "수정자")
	private String uUpdateUser;
	
	@ApiModelProperty(value = "수정일")
	private String uUpdateDate;
		
	
	@ApiModelProperty(value = "회사코드")
	private String uComCode;
	
	@ApiModelProperty(value = "조직정보")
	private String uOrgInfo;
}