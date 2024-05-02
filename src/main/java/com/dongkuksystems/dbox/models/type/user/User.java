package com.dongkuksystems.dbox.models.type.user;

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
public class User {
  @ApiModelProperty(value = "사번", required = true)
  private String uUserSabun;
  @ApiModelProperty(value = "이름")
  private String uDisplayName;
  @ApiModelProperty(value = "회사코드")
  private String uComCode;
  @ApiModelProperty(value = "부서코드")
  private String uDeptCode;
  @ApiModelProperty(value = "사용자구분", example = "(EMP/PTN/EXT)")
  private String uUserType;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "퇴사일")
  private LocalDateTime uExpiredDate;
  
  @ApiModelProperty(value = "ObjecId")
  private String rObjectId;
  @ApiModelProperty(value = "사용자 이름")
  private String userName;

}
