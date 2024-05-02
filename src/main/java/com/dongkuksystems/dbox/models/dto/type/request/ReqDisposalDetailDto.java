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
public class ReqDisposalDetailDto {
  @ApiModelProperty(value = "Object 키")
  private String rObjectId;
  @ApiModelProperty(value = "문서 이름 + 확장자")
  private String title;
  @ApiModelProperty(value = "문서함코드")
  private String uCabinetCode;
  @ApiModelProperty(value = "문서 키")
  private String uDocKey;
  @ApiModelProperty(value = "폴더경로")
  private String uFolderPath;
  @ApiModelProperty(value = "등록자")
  private String uRegUser;
  @ApiModelProperty(value = "보안등급")
  private String uSecLevel;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "등록일")
  private LocalDateTime uRegDate;
  @ApiModelProperty(value = "사유")
  private String uReqReason;
  @ApiModelProperty(value = "요청상태") // R:요청중, A:승인, D:반려, C:회수
  private String uReqStatus;
  @ApiModelProperty(value = "보존년한 만료일")
  private String uExpiredDate;
  @ApiModelProperty(value = "req_delete Object 키")
  private String dRObjectId;
  @ApiModelProperty(value = "요청문서 ID")
  private String uReqDocId;
  @ApiModelProperty(value = "요청구분(보존년한/개별)")
  private String uReqType;
  @ApiModelProperty(value = "요청자")
  private String uReqUser;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "요청일")
  private LocalDateTime uReqDate;
  @ApiModelProperty(value = "요청자 이름")
  private String uReqUserName;
  @ApiModelProperty(value = "소속 부서코드")
  private String orgId;
  @ApiModelProperty(value = "소속 부서명")
  private String orgNm;
  @ApiModelProperty(value = "폴더 ID")
  private String uFolId;
  @ApiModelProperty(value = "target 폴더 ID")
  private String targetFolId;
}
