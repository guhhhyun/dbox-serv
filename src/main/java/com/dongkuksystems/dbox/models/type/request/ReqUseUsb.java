package com.dongkuksystems.dbox.models.type.request;

import java.time.LocalDateTime;
import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.data.DataUpdateReqDto;
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
public class ReqUseUsb {
  @ApiModelProperty(value = "문서함 id", required = true)
  private String rObjectId;
  
  @ApiModelProperty(value = "신청자 ID")
  private String uReqUserId;
  @ApiModelProperty(value = "등록자")
  private String uRegistUser;
  @ApiModelProperty(value = "신청자 부서코드")
  private String uReqDeptCode;

  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "신청일")
  private LocalDateTime uReqDate; 
  
  private List<String> rObjectIds;
  
  
  @ApiModelProperty(value = "신청상태")
  private String uReqStatus;
  
  @ApiModelProperty(value = "이동식 드라이브 사용 여부")
  private boolean uAllowUsb;
  @ApiModelProperty(value = "CD/DVD 드라이브 사용 여부")
  private boolean uAllowCd;
  @ApiModelProperty(value = "사용시간")
  private int uUseTime;
  @ApiModelProperty(value = "신청사유")
  private String uReqReason; 
  
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "사용만료일시")
  private LocalDateTime uExpiredDate; 

  @ApiModelProperty(value = "승인자")
  private String uApprover;
  
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "승인일")
  private LocalDateTime uApproveDate; 
  
  @ApiModelProperty(value = "반려사유")
  private String uRejectReason;
}
