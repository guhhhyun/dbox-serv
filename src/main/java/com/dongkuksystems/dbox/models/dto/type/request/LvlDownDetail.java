package com.dongkuksystems.dbox.models.dto.type.request;

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
public class LvlDownDetail {
  @ApiModelProperty(value = "Object 키")
  private String rObjectId;
  @ApiModelProperty(value = "요청문서 ID")
  private String uReqDocId;
  @ApiModelProperty(value = "요청문서 이름")
  private String uReqDocName;
  @ApiModelProperty(value = "요청문서 키")
  private String uReqDocKey;
  @ApiModelProperty(value = "소유부서 코드")
  private String uOwnDeptCode;
  @ApiModelProperty(value = "요청자")
  private String uReqUser;
  @ApiModelProperty(value = "요청자 이름")
  private String uReqUserName;
  @ApiModelProperty(value = "요청자부서코드")
  private String uReqDeptCode;
  @ApiModelProperty(value = "문서 사이즈")
  private String uDocSize;
  @ApiModelProperty(value = "요청자 직책")
  private String uReqUserJobTitleName;
  @ApiModelProperty(value = "승인자 직책")
  private String uApproverJobTitleName;
  @ApiModelProperty(value = "요청자 부서명")
  private String uReqUserDeptName;
  @ApiModelProperty(value = "요청자 회사명")
  private String uReqUserComName;
  @ApiModelProperty(value = "승인자")
  private String uApprover;
  @ApiModelProperty(value = "승인자 이름")
  private String uApproverName;
  @ApiModelProperty(value = "승인자 부서이름")
  private String uApproverDeptName;
  @ApiModelProperty(value = "승인자 회사명")
  private String uApproverComName;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "요청일")
  private LocalDateTime uReqDate; 
  @ApiModelProperty(value = "사유")
  private String uReqReason;
  @ApiModelProperty(value = "요청상태") // R:요청중, A:승인, D:반려, C:회수
  private String uReqStatus;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "처리일")
  private LocalDateTime uActionDate; 
  @ApiModelProperty(value = "반려사유")
  private String uRejectReason;
}
