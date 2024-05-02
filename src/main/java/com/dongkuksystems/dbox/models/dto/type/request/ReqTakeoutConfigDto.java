package com.dongkuksystems.dbox.models.dto.type.request;

import java.time.LocalDateTime;
import java.util.List;

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
public class ReqTakeoutConfigDto {

	private String rObjectId;
  @ApiModelProperty(value = "회사코드")
  private String uComCode;
  @ApiModelProperty(value = "부서코드")
  private String uDeptCode;
  @ApiModelProperty(value = "자동승인 사용여부")
  private String uAutoApprYn;
  @ApiModelProperty(value = "프리패스 사용여부")
  private String uFreePassYn;
  @ApiModelProperty(value = "삭제옵션")
  private String uDeleteOption;
  @ApiModelProperty(value = "삭제기간 일수")
  private int uDeleteDays;
  @ApiModelProperty(value = "자동승인 항목명")
  private String uAutoName;
  @ApiModelProperty(value = "자동승인 등록자")
  private String uAutoRegister;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "자동승인 등록일")
  private String uAutoRegistDate;
  @ApiModelProperty(value = "프리패스 항목명")
  private String uFreeName;
  @ApiModelProperty(value = "프리패스 등록자")
  private String uFreeRegister;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "프리패스 등록일")
  private String uFreeRegistDate;
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
  
  @ApiModelProperty(value = "모드")
  private String mode;
  @ApiModelProperty(value = "type")
  private String type;
  @ApiModelProperty(value = "repeating valueIndex")
  private String valueIndex;
	
}
