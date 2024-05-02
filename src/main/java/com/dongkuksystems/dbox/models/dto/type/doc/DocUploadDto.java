package com.dongkuksystems.dbox.models.dto.type.doc;

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
public class DocUploadDto {
  @ApiModelProperty(value = "문서함 코드", required = true)
  private String uCabinetCode;
  @ApiModelProperty(value = "문서 키", required = true)
  private String uDocKey;
  @ApiModelProperty(value = "폴더 ID")
  private String uFolId;
  @ApiModelProperty(value = "보안등급")
  private String uSecLevel;
  @ApiModelProperty(value = "문서상태")
  private String uDocStatus;
  @ApiModelProperty(value = "문서구분자")
  private String uDocFlag;
  @ApiModelProperty(value = "삭제상태")
  private String uDeleteStatus;
  @ApiModelProperty(value = "소유부서")
  private String uOwnDeptCode;
  @ApiModelProperty(value = "생성자부서")
  private String uCreatorDeptCode;
  @ApiModelProperty(value = "결재문서여부")
  private String uWfDocYn;
  @ApiModelProperty(value = "결재정보")
  private String uWfInfo;
  @ApiModelProperty(value = "결재자")
  private String uWfApprover;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "Closed 처리 일시")
  private LocalDateTime uClosedDate;
  @ApiModelProperty(value = "Closed 처리자", example = "사용자ID or 'SYSTEM'")
  private String uCloser;
  @ApiModelProperty(value = "작성자", example = "REPEATING")
  private String uEditor;
  @ApiModelProperty(value = "공개여부")
  private boolean uOpenFlag;
  @ApiModelProperty(value = "개인정부포함여부")
  private boolean uPrivacyFlag;
  @ApiModelProperty(value = "보존년한")
  private int uPreserverFlag;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "보존년한 만료일")
  private LocalDateTime uExpiredDate;
  @ApiModelProperty(value = "복사원본 ID")
  private String uCopyOrgId;
  @ApiModelProperty(value = "파일확장자")
  private String uFileExt;
  @ApiModelProperty(value = "태그")
  private String uDocTag;
  @ApiModelProperty(value = "분류")
  private String uDocClass;
}
