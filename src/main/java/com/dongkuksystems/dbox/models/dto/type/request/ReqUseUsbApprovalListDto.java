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
public class ReqUseUsbApprovalListDto {
	@ApiModelProperty(value = "Object 키")
	  private String rObjectId;
	
	  @ApiModelProperty(value = "신청자 ID")
	  private String uReqUserId;	
	  
	  @ApiModelProperty(value = "신청자 이름")
	  private String uReqUserName;

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
	  @ApiModelProperty(value = "신청일")
	  private LocalDateTime uReqDate; 

}