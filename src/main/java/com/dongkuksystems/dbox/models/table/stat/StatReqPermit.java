package com.dongkuksystems.dbox.models.table.stat;

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
public class StatReqPermit {
  @ApiModelProperty(value = "회사코드", required = true)
  private String uComCode;
  @ApiModelProperty(value = "부서코드", required = true)
  private String uDeptCode; 
  @ApiModelProperty(value = "건수", required = true)
  private int uJobcount; 
  @ApiModelProperty(value = "용량", required = true)
  private long uFileSize; 
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "취합일")
  private LocalDateTime uGatherDate;
}
