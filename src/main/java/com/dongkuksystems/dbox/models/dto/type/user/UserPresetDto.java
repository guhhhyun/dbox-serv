package com.dongkuksystems.dbox.models.dto.type.user;

import java.time.LocalDateTime;
import java.util.List;

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
public class UserPresetDto {
	@ApiModelProperty(value = "프리셋 id", required = true)
  private String rObjectId;
  @ApiModelProperty(value = "사용자 ID")
  private String uUserId;
  @ApiModelProperty(value = "등록 기본 여부 ( BOOLEAN ) ")
  private String uRegBaseFlag;
  @ApiModelProperty(value = "등급 기본 여부 ( BOOLEAN ) ")
  private String uSecBaseFlag;
  @ApiModelProperty(value = "설정명")
  private String uConfigName;
  @ApiModelProperty(value = "설명")
  private String uConfigDesc;
  @ApiModelProperty(value = "공개여부 DEFAULT 1")
  private String uOpenFlag;
  @ApiModelProperty(value = "보존년한")
  private int uPreserveFlag;
  @ApiModelProperty(value = "보안등급")
  private String uSecLevel;
  @ApiModelProperty(value = "PC문서 등록 옵션 (D:대화창오픈, V:자동버전업, C:복사본, S:건너뛰기)")
  private String uPcRegFlag;
  @ApiModelProperty(value = "문서복사 옵션 (K:원본권한유지, P:등급별 Preset 적용)")
  private String uCopyFlag;
  @ApiModelProperty(value = "문서 수정 후 저장 옵션 (V:자동 버전업, U:직전 수정자가 다를 경우에만 버전업, O:덮어쓰기) ")
  private String uEditSaveFlag;
  @ApiModelProperty(value = "메일 자동 권한 여부 (Y:적용, N:적용하지 않음) ")
  private String uMailPermitFlag;
  
  @ApiModelProperty(value = "생성자")
  private String uCreateUser;
  
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "생성일")
  private LocalDateTime uCreateDate;
  
  @ApiModelProperty(value = "수정자")
  private String uModifyUser;
  
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "수정일")
  private LocalDateTime uModifyDate;

  @ApiModelProperty(value = "등급명")
  private String levelName;
  
  @ApiModelProperty(value = "리피팅")
  private List<UserPresetRepeatingDto> userPresetRepeatings;


  
  
  
  
}
