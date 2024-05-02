package com.dongkuksystems.dbox.models.type.manager.deptinformconfig;

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
public class DeptInformConfig {	
	@ApiModelProperty(value = "ObjecId")
	private String rObjectId;	
	@ApiModelProperty(value = "deptObjectId")
	private String deptObjectId;	
	@ApiModelProperty(value = "comObjectId")
	private String comObjectId;	
	@ApiModelProperty(value = "회사코드")
	private String uComCode;	
	@ApiModelProperty(value = "부서코드")
	private String uDeptCode;
	@ApiModelProperty(value = "다운로드 건수")
	private String uCountDownload;	
	@ApiModelProperty(value = "반출 건수")
	private String uCountTakeout;
	@ApiModelProperty(value = "권한 신청 건수")
	private String uCountReqPermit;	
	@ApiModelProperty(value = "출력 건수")
	private String uCountPrint;
	@ApiModelProperty(value = "삭제 건수")
	private String uCountDelete;	
    @ApiModelProperty(value = "생성자")
	private String uCreateUser;	
	@JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@ApiModelProperty(value = "생성일")
	private LocalDateTime uCreateDate;
	@ApiModelProperty(value = "수정자")
	private String uModifyUser;	
	@JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
	@JsonSerialize(using = LocalDateTimeSerializer.class)
	@JsonDeserialize(using = LocalDateTimeDeserializer.class)
	@ApiModelProperty(value = "수정일")
	private LocalDateTime uModifyDate;

	
	
}
