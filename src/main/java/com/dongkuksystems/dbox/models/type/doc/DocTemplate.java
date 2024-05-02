package com.dongkuksystems.dbox.models.type.doc;

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
public class DocTemplate {
  @ApiModelProperty(value = "회사 코드", required = true)
  private String uComCode;
  @ApiModelProperty(value = "탬플릿 구분")
  private String uTemplateType;
  @ApiModelProperty(value = "탬플릿 명")
  private String uTemplateName;
  @ApiModelProperty(value = "탬플릿 순서")
  private String uSortOrder;
  
  @ApiModelProperty(value = "파일 명")
  private String objectName;
  
  @ApiModelProperty(value = "확장자 명")
  private String contentExtension;
  @ApiModelProperty(value = "object id")
  private String rObjectId;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "생성 일시")
  private LocalDateTime rCreationDate;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "수정 일시")
  private LocalDateTime rModifyDate;
  
  @ApiModelProperty(value = "삭제 상태")
  private String uDeleteStatus;
}
