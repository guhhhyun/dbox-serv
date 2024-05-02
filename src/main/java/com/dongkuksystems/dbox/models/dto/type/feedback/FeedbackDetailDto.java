package com.dongkuksystems.dbox.models.dto.type.feedback;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import com.dongkuksystems.dbox.constants.Commons;
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
public class FeedbackDetailDto {
  private String rObjectId;
  @ApiModelProperty(value = "문서 키", required = true)
  private String uDocKey;
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
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "생성 일시")
  private LocalDateTime uCreateDate;
  @ApiModelProperty(value = "생성자 이름")
  private String uCreateUserName;
  @ApiModelProperty(value = "생성자 부서아이디")
  private String uCreateUseOrgId;
  @ApiModelProperty(value = "생성자 부서명")
  private String uCreateUserOrgName;
  @ApiModelProperty(value = "생성자 직위")
  private String uCreateUserJobTitleCode;
  @ApiModelProperty(value = "생성자 직위명")
  private String uCreateUserJobTitleName;
  @ApiModelProperty(value = "피드백 건수")
  private String uFeedbackNm;

  public void setUCreateDate(LocalDateTime uCreateDate) {
    if (uCreateDate == null) this.uCreateDate = null;
    else {
      ZonedDateTime zdt = uCreateDate.atZone(ZoneId.of("Asia/Seoul"));
      long milli = zdt.toInstant().toEpochMilli();
      this.uCreateDate = Commons.NULL_DATE == milli  ? null : uCreateDate;
    }
  }
  
}
