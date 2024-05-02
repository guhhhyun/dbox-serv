package com.dongkuksystems.dbox.models.type.manager;

import java.time.LocalDateTime;

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
public class UsbPolicy {
	
	@ApiModelProperty(value = "r_object_id")
	private String r_object_id;
	@ApiModelProperty(value = "사용자/부서 구분  U:사용자, D:부서")
	private String u_target_type;
	@ApiModelProperty(value = "사용자/부서  사용자ID 또는 부서코드")
	private String u_target_id;
	@ApiModelProperty(value = "정책 RO/RW")
	private String u_policy;
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@ApiModelProperty(value = "적용시작일")
	private LocalDateTime u_start_date; 
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@ApiModelProperty(value = "적용종료일")
	private LocalDateTime u_end_date;
	
	@ApiModelProperty(value = "정책설명")
	private String policy_nm;
	@ApiModelProperty(value = "부서명")
	private String dept_nm;
}
