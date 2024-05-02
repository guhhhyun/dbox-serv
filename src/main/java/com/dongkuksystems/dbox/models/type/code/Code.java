package com.dongkuksystems.dbox.models.type.code;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class Code {
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
	@ApiModelProperty(value = "정렬순서")
	private int uSortOrder;
	@ApiModelProperty(value = "생성자")
	private String uCreateUser;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@ApiModelProperty(value = "생성 일시")
	private LocalDateTime uCreateDate;
	@ApiModelProperty(value = "수정자")
	private String uUpdateUser;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@ApiModelProperty(value = "수정 일시")
	private LocalDateTime uUpdateDate;

}


