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
public class ReqDisposalFilterDto {
  @ApiModelProperty(value = "상태")
  private String type;
  @ApiModelProperty(value = "작성자 요청자 구분")
  private String userType;
  @ApiModelProperty(value = "문서 이름 + 확장자")
  private String title;
  @ApiModelProperty(value = "등록자")
  private String uRegUser;
  @ApiModelProperty(value = "요청상태") // R:요청중, A:승인, D:반려, C:회수
  private String uReqStatus;
  @ApiModelProperty(value = "요청구분(보존년한/개별)")
  private String uReqType;
  @ApiModelProperty(value = "요청자")
  private String uReqUser;
  @ApiModelProperty(value = "소속 부서코드")
  private String deptCode;
  @ApiModelProperty(value = "폐기 요청 시작일")
  private String reqStartDate;
  @ApiModelProperty(value = "폐기 요청 종료일")
  private String reqEndDate;
}
