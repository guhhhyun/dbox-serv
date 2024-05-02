package com.dongkuksystems.dbox.models.dto.type.manager.hisviewuser;

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
public class HisViewUserFilterDto {
	@ApiModelProperty(value = "ObjecId")
	private String rObjectId;	
	@ApiModelProperty(value = "항목코드")
	private String uHisCode;	
	@ApiModelProperty(value = "회사코드")
	private String uComCode;
	@ApiModelProperty(value = "사용자 ID")
	private String uUserId;
	@ApiModelProperty(value = "생성자")
	private String uCreateUser;
	@JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@ApiModelProperty(value = "생성 일시")
	private LocalDateTime uCreateDate;
	@ApiModelProperty(value = "사용자 이름")
	private String displayName;
	@ApiModelProperty(value = "회사명")
	private String uCodeName;
}
