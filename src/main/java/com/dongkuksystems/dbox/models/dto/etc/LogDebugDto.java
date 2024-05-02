package com.dongkuksystems.dbox.models.dto.etc;

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
public class LogDebugDto {
  @ApiModelProperty(value = "사용자 아이디")
  private String userId;
  @ApiModelProperty(value = "로그 내용")
  private String logData;
  @ApiModelProperty(value = "아이피", hidden = true)
  private String ip;
}
