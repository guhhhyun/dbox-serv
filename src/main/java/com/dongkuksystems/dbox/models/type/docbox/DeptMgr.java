package com.dongkuksystems.dbox.models.type.docbox;

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
public class DeptMgr {
  @ApiModelProperty(value = "부서코드", required = true)
  private String uDeptCode;
  @ApiModelProperty(value = "문서함코드", required = true)
  private String uCabinetCode;
  @ApiModelProperty(value = "사용자ID")
  private String uUserId; 
  @ApiModelProperty(value = "관리자타입")
  private String uMgrType; 
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
}
