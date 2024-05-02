package com.dongkuksystems.dbox.models.dto.type.request;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class ReqTakeoutDto {
  @ApiModelProperty(value = "rObjectId")
  private String rObjectId;
  @ApiModelProperty(value = "문서명")
  private String objectName;
  @ApiModelProperty(value = "버전")
  private String rVersionLabel;
  @ApiModelProperty(value = "반려 사유")
  private String uRejectReason;
  @ApiModelProperty(value = "문서 용량")
  private String rContentSize;
  @ApiModelProperty(value = "문서 소유자")
  private String displayName;
  @ApiModelProperty(value = "요청자")
  private String uReqUser;
  @ApiModelProperty(value = "요청상태")
  private String uReqStatus;
  @ApiModelProperty(value = "요청 시작일")
  private String reqStartDate;
  @ApiModelProperty(value = "요청 종료일")
  private String reqEndDate;
  

  
}
