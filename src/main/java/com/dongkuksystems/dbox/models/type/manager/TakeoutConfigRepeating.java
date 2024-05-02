package com.dongkuksystems.dbox.models.type.manager;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
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
@JsonIgnoreProperties(value = {"handler"})
@Builder 
public class TakeoutConfigRepeating {
	private String rObjectId;
	@ApiModelProperty(value = "정렬")
  private String iPosition;
  @ApiModelProperty(value = "자동승인 항목명")
  private String uAutoName;
  @ApiModelProperty(value = "자동승인 등록자")
  private String uAutoRegister;
  @ApiModelProperty(value = "자동승인 등록자명")
  private String uAutoRegisterName;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "자동승인 등록일")
  private LocalDateTime uAutoRegistDate;
  @ApiModelProperty(value = "프리패스 항목명")
  private String uFreeName;
  @ApiModelProperty(value = "프리패스 등록자")
  private String uFreeRegister;
  @ApiModelProperty(value = "프리패스 등록자명")
  private String uFreeRegisterName;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "프리패스 등록일")
  private LocalDateTime uFreeRegistDate;
}
