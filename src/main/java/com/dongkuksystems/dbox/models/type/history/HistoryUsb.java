package com.dongkuksystems.dbox.models.type.history;

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
public class HistoryUsb {
	
	 // 변경정보
	  @ApiModelProperty(value = "rObjectId")
	  private String rObjectId;
	  @ApiModelProperty(value = "신청자")
	  private String uReqUserId;
	  @ApiModelProperty(value = "문서 건수")
	  private String uFileCount;
	  @ApiModelProperty(value = "파일 명")
	  private String uFileName;
	  @ApiModelProperty(value = "등록자")
	  private String uRegistUser;
	  @ApiModelProperty(value = "신청자부서코드")
	  private String UReqDeptCode;
	  
	  @JsonFormat(pattern = "yyyy-MM-dd")
	  @JsonSerialize(using = LocalDateTimeSerializer.class)
	  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	  @ApiModelProperty(value = "신청일")
	  private LocalDateTime uReqDate;
	  @ApiModelProperty(value = "신청상태")
	  private String uReq_Status;
	  @ApiModelProperty(value = "이동식 드라이브 사용 여부")
	  private String uAllowUsb;
	  @ApiModelProperty(value = "CD/DVD 드라이브 사용 여부")
	  private String uAllowCd;
	  @ApiModelProperty(value = "사용시간")
	  private String uUseTime;
	  @ApiModelProperty(value = "신청사유")
	  private String uReqReason;
	  
	  @JsonFormat(pattern = "yyyy-MM-dd")
	  @JsonSerialize(using = LocalDateTimeSerializer.class)
	  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	  @ApiModelProperty(value = "사용만료일시")
	  private LocalDateTime uExpiredDate;
	  @ApiModelProperty(value = "승인자")
	  private String uApprover;
	  
	  @JsonFormat(pattern = "yyyy-MM-dd")
	  @JsonSerialize(using = LocalDateTimeSerializer.class)
	  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	  @ApiModelProperty(value = "승인일")
	  private LocalDateTime uApproveDate;
	  @ApiModelProperty(value = "반려사유")
	  private String uRejectReason;
	  
	  @ApiModelProperty(value = "신청자 명")
	  private String reqUserName;
	  @ApiModelProperty(value = "등록자 명")
	  private String registUserName;
	  @ApiModelProperty(value = "신청자 회사명")
	  private String logComName;
	  @ApiModelProperty(value = "부서명")
	  private String logDeptName;
	  @ApiModelProperty(value = "사용시간")
	  private String logUseTimeName;
	  @ApiModelProperty(value = "파일명 리스트")
	  private String listName;
	
	  
	  
}
