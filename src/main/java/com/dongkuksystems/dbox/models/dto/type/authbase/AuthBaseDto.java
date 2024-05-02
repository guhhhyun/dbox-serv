package com.dongkuksystems.dbox.models.dto.type.authbase;

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
public class AuthBaseDto {
  @ApiModelProperty(value = "문서 키/폴더 ID", required = true)
  private String uObjId;
  @ApiModelProperty(value = "문서/폴더 구분", required = true)
  private String uObjType;
  @ApiModelProperty(value = "문서상태")
  private String uDocStatus; 
  @ApiModelProperty(value = "권한구분(조회/편집)")
  private String uPermitType; 
  @ApiModelProperty(value = "소유부서여부")
  private String uOwnDeptYn; 
  @ApiModelProperty(value = "권한자(부서,사용자) ID")
  private String uAuthorId; 
  @ApiModelProperty(value = "권한자타입(부서/사용자)")
  private String uAuthorType; 
  @ApiModelProperty(value = "생성자")
  private String uCreateUser;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "생성 일시")
  private LocalDateTime uCreateDate; 
}
