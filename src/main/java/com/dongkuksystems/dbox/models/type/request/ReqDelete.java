package com.dongkuksystems.dbox.models.type.request;

import java.time.LocalDateTime;

import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.type.doc.Doc;
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
public class ReqDelete {
  private String rObjectId;
  @ApiModelProperty(value = "요청문서 ID", required = true)
  private String uReqDocId;
  @ApiModelProperty(value = "문서함 코드")
  private String uCabinetCode;
  @ApiModelProperty(value = "요청문서 키")
  private String uReqDocKey;
  @ApiModelProperty(value = "요청구분(보존년한/개별)")
  private String uReqType;
  @ApiModelProperty(value = "요청상태")
  private String uReqStatus;
  @ApiModelProperty(value = "요청자부서코드")
  private String uReqDeptCode;
  @ApiModelProperty(value = "요청자")
  private String uReqUser;
  
  @JsonFormat(pattern = "yyyy-MM-dd")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "요청일")
  private LocalDateTime uReqDate; 
  @ApiModelProperty(value = "요청사유")
  private String uReqReason;
  @ApiModelProperty(value = "승인자")
  private String uApprover;
  
  @JsonFormat(pattern = "yyyy-MM-dd")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "승인일")
  private LocalDateTime uApproveDate; 
  
  @JsonFormat(pattern = "yyyy-MM-dd")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "폐기일")
  private LocalDateTime uDeleteDate;
  
  @ApiModelProperty(value = "경로")
  private String uFolderPath;
  @ApiModelProperty(value = "문서명")
  private String uDocName;
  @ApiModelProperty(value = "보안등급")
  private String uSecLevel;
  @ApiModelProperty(value = "생성연도")
  private String uCreateYear;
  
  @JsonFormat(pattern = "yyyy")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "보존연한")
  private LocalDateTime uExpiredDate; 
  
  // 추가.
  @ApiModelProperty(value = "구분 명")
  private String reqTypeName;
  @ApiModelProperty(value = "소유부서 명")
  private String cabinetCodeName;
  @ApiModelProperty(value = "보안등급 명")
  private String secLevelName;
  @ApiModelProperty(value = "요청자 명")
  private String reqUserName;
  @ApiModelProperty(value = "승인자 명")
  private String approverName;
  
  private VUser reqUserDetail;
  private VUser approverDetail;
  private Doc docDetail;

}
