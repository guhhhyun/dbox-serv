package com.dongkuksystems.dbox.models.type.feedback;

import java.time.LocalDateTime;

import com.dongkuksystems.dbox.models.table.etc.VUser;
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
public class Feedback {
  @ApiModelProperty(value = "문서 키", required = true)
  private String uDocKey;
  @ApiModelProperty(value = "objectID")
  private String rObjectId;
  @ApiModelProperty(value = "피드백 내용")
  private String uFeedback;
  @ApiModelProperty(value = "문답그룹")
  private int uGroup;
  @ApiModelProperty(value = "레벨")
  private int uLevel;
  @ApiModelProperty(value = "정렬순서")
  private int uOrder;
  @ApiModelProperty(value = "비공개 여부")
  private String uOpenFlag;
  @ApiModelProperty(value = "생성자")
  private String uCreateUser;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "생성 일시")
  private LocalDateTime uCreateDate;
  
  private VUser userDetail;
}
