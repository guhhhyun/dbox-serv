package com.dongkuksystems.dbox.models.dto.table.statodd;

import java.time.LocalDateTime;
import java.util.List;

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
public class StatOddFilterDto {
	@ApiModelProperty(value = "회사코드")
	  private String uComCode;
	  @ApiModelProperty(value = "부서코드")
	  private String uDeptCode;
	  @ApiModelProperty(value = "부서 코드 리스트")
	  private List<String> deptCodeList;
	  @ApiModelProperty(value = "사용자 ID")
	  private String uUserId;
	  @ApiModelProperty(value = "다운로드 기준초과 건수")
	  private int uDownloadCntOver;
	  @ApiModelProperty(value = "다운로드 경고 건수")
	  private int uDownloadCntWarn;
	  @ApiModelProperty(value = "다운로드 개인 전월 이력")
	  private int uDownloadCntLmonUser;
	  @ApiModelProperty(value = "다운로드 부서 전월 이력")
	  private int uDownloadCntLmonDept;
	  @ApiModelProperty(value = "반출 기준초과 건수")
	  private int uTakeoutCntOver;
	  @ApiModelProperty(value = "반출 경고 건수")
	  private int uTakeoutCntWarn;
	  @ApiModelProperty(value = "반출 개인 전월 이력")
	  private int uTakeoutCntLmonUser;
	  @ApiModelProperty(value = "반출 부서 전월 이력")
	  private int uTakeoutCntLmonDept;
	  @ApiModelProperty(value = "권한신청 기준초과 건수")
	  private int uAuthreqCnt_Over;
	  @ApiModelProperty(value = "권한신청 경고 건수")
	  private int uAuthreqCntWarn;
	  @ApiModelProperty(value = "권한신청 개인 전월 이력")
	  private int uAuthreqCntLmonUser;
	  @ApiModelProperty(value = "권한신청 부서 전월 이력")
	  private int uAuthreqCntLmonDept;
	  @ApiModelProperty(value = "출력 기준초과 건수")
	  private int uPrintCntOver;
	  @ApiModelProperty(value = "출력 경고 건수")
	  private int uPrintCntWarn;
	  @ApiModelProperty(value = "출력 개인 전월 이력")
	  private int uPrintCntLmonUser;
	  @ApiModelProperty(value = "출력 부서 전월 이력")
	  private int uPrintCntLmonDept;
	  @ApiModelProperty(value = "삭제 기준초과 건수")
	  private int uDeleteCntOver;
	  @ApiModelProperty(value = "삭제 경고 건수")
	  private int uDeleteCntWarn;
	  @ApiModelProperty(value = "삭제 개인 전월 이력")
	  private int uDeleteCntLmonUser;
	  @ApiModelProperty(value = "삭제 부서 전월 이력")
	  private int uDeleteCntLmonDept;
	  @JsonSerialize(using = LocalDateTimeSerializer.class)
	  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
	  @ApiModelProperty(value = "지정일")
	  private LocalDateTime uDesigDate;
	  @ApiModelProperty(value = "상태")
	  private String uDesigStatus;
	  @ApiModelProperty(value = "취합일")
	  private String uLogDate;
	  @ApiModelProperty(value = "검색 시작일")
	  private String statStartDate;
	  @ApiModelProperty(value = "검색 종료일")
	  private String statEndDate;
}
