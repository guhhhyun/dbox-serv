package com.dongkuksystems.dbox.models.type.manager.usbpolicy;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UsbPolicyType {	
	
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
	
	@ApiModelProperty(value = "사용자 부서 구분")
	private String uTargetType;
	
	@ApiModelProperty(value = "사용자 부서ID")
	private String uTargetId;
	
	@ApiModelProperty(value = "정책")
	private String uPolicy;
	
	@ApiModelProperty(value = "적용일자")
	private String uPolicyDate;
	
	@ApiModelProperty(value = "부서명")
	private String uDeptNm;
	
	@ApiModelProperty(value = "정책명")
	private String uPolicyNm;
	
	@ApiModelProperty(value = "사용자명")
	private String uDisplayName;
	
	@ApiModelProperty(value = "직급")
	private String uPstnName;
	
	@ApiModelProperty(value = "정책시작일")
	private String uStartDate;
	
	@ApiModelProperty(value = "정책종료일")
	private String uEndDate;
	
}