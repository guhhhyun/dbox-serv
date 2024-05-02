package com.dongkuksystems.dbox.models.table.log;

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
public class LogOdd {
	 @ApiModelProperty(value = "회사코드")
	  private String uComCode;
	  @ApiModelProperty(value = "부서코드")
	  private String uDeptCode;
	  @ApiModelProperty(value = "사용자 ID")
	  private String uUserId;
	  @ApiModelProperty(value = "다운로드 건수")
	  private int uDownloadCnt;
	  @ApiModelProperty(value = "다운로드 기준초과 여부")
	  private boolean uDownloadOver;
	  @ApiModelProperty(value = "다운로드 안내 여부")
	  private boolean uDownloadWarn;
	  @ApiModelProperty(value = "반출 건수")
	  private int uTakeoutCnt;
	  @ApiModelProperty(value = "반출 기준초과 여부")
	  private boolean uTakeoutOver;
	  @ApiModelProperty(value = "반출 안내 여부")
	  private boolean uTakeoutWarn;
	  @ApiModelProperty(value = "권한신청 건수")
	  private int uAuthreqCnt;
	  @ApiModelProperty(value = "권한신청 기준초과 여부")
	  private boolean uAuthreqOver;
	  @ApiModelProperty(value = "권한신청 안내 여부")
	  private boolean uAuthreqWarn;
	  @ApiModelProperty(value = "출력 건수")
	  private int uPrintCnt;
	  @ApiModelProperty(value = "출력 기준초과 여부")
	  private boolean uPrintOver;
	  @ApiModelProperty(value = "출력 안내 여부")
	  private boolean uPrintWarn;
	  @ApiModelProperty(value = "삭제 건수")
	  private int uDeleteCnt;
	  @ApiModelProperty(value = "삭제 기준초과 여부")
	  private boolean uDeleteOver;
	  @ApiModelProperty(value = "삭제 안내 여부")
	  private boolean uDeleteWarn;
		@JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss", timezone="Asia/Seoul")
		@JsonSerialize(using = LocalDateTimeSerializer.class)
		@JsonDeserialize(using = LocalDateTimeDeserializer.class)
		@ApiModelProperty(value = "취합일")
		private LocalDateTime uLogDate;
		@ApiModelProperty(value = "일자 추출")
	  private String day;
}
