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
public class ReqLvlDown {
  @ApiModelProperty(value = "Object ID")
  private String rObjectId;
  @ApiModelProperty(value = "요청문서 ID", required = true)
  private String uReqDocId;
  @ApiModelProperty(value = "요청문서 키")
  private String uReqDocKey;
  @ApiModelProperty(value = "요청상태")
  private String uReqStatus;
  @ApiModelProperty(value = "소유부서코드")
  private String uOwnDeptCode;
  @ApiModelProperty(value = "요청자")
  private String uReqUser;  
  @JsonFormat(pattern = "yyyy-MM-dd")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "요청일")
  private LocalDateTime uReqDate; 
  @JsonFormat(pattern = "yyyy-MM-dd")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "처리일")
  private LocalDateTime uActionDate; 
  @ApiModelProperty(value = "요청사유")
  private String uReqReason;
  @ApiModelProperty(value = "승인자")
  private String uApprover;
  @ApiModelProperty(value = "변경전 등급")
  private String uBeforeLevel;
  @ApiModelProperty(value = "변경후 등급")
  private String uAfterLevel;
  @ApiModelProperty(value = "반려 사유")
  private String uRejectReason;
  
  private VUser reqUserDetail;
  private VUser approverDetail;
  private Doc docDetail;
}
