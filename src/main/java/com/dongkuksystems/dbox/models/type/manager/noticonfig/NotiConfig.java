package com.dongkuksystems.dbox.models.type.manager.noticonfig;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class NotiConfig {
	@ApiModelProperty(value = "ObjectId")
	private String rObjectId;
	@ApiModelProperty(value = "회사코드")
	private String uComCode;
	@ApiModelProperty(value = "항목코드")
	private String uEventCode;
	@ApiModelProperty(value = "알림여부")
	private String uAlarmYn;
	@ApiModelProperty(value = "email여부")
	private String uEmailYn;
	@ApiModelProperty(value = "모바일여부")
	private String uMmsYn;
	@ApiModelProperty(value = "생성자")
	private String uCreateUser;
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@ApiModelProperty(value = "생성 일시")
	private LocalDateTime uCreateDate;
	@ApiModelProperty(value = "수정자")
	private String uModifyUser;
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@ApiModelProperty(value = "수정 일시")
	private LocalDateTime uModifyDate;
	
	@ApiModelProperty(value = "알림항목", notes = "알림항목 같이 조회하기 위해 추가")
	private String uCodeName1;
	@ApiModelProperty(value = "알림대상", notes = "알림대상 같이 조회하기 위해 추가")
	private String uCodeName2;
	
}
