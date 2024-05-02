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
public class ReqTakeout {
  @ApiModelProperty(value = "문서함 id", required = true)
  private String rObjectId;
  @ApiModelProperty(value = "요청이름")
  private String uReqTitle;
  @ApiModelProperty(value = "요청자")
  private String uReqUser;
  @ApiModelProperty(value = "요청자부서코드")
  private String uReqDeptCode;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "요청일")
  private LocalDateTime uReqDate; 
  @ApiModelProperty(value = "반출사유")
  private String uReqReason;
  @ApiModelProperty(value = "요청상태")
  private String uReqStatus;
  @ApiModelProperty(value = "소유부서코드")
  private String uOwnDeptCode;
  @ApiModelProperty(value = "승인자")
  private String uApprover;
  @ApiModelProperty(value = "반출기간")
  private String uLimitFlag;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "반출만료일")
  private LocalDateTime uLimitDate; 
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "처리일")
  private LocalDateTime uActionDate; 
  @ApiModelProperty(value = "반려사유")
  private String uRejectReason;
  @ApiModelProperty(value = "승인방식")
  private String uApprType;
  @ApiModelProperty(value = "limitdate filter 용도")
  private String uLimitDateStr; 
  @ApiModelProperty(value = "반출 요청 문서 용량")
  private String sumContentSize;
  @ApiModelProperty(value = "반출 요청 문서 개수")
  private String cnt;
 
  private VUser reqUserDetail;
  private VUser approverDetail;
  private Doc docDetail;
  private ReqTakeoutDoc takeoutDoc;
  
}
