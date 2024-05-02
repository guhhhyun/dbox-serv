package com.dongkuksystems.dbox.models.table.stat;

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
public class StatStrager {
  @ApiModelProperty(value = "회사코드", required = true)
  private String uComCode;
  @ApiModelProperty(value = "부서코드", required = true)
  private String uDeptCode;
  @ApiModelProperty(value = "문서함코드", required = true)
  private String uUserId;
  @ApiModelProperty(value = "다운로드 건수", required = true)
  private int uDownloadCount;
  @ApiModelProperty(value = "반출 건수", required = true)
  private int uExportCount;
  @ApiModelProperty(value = "권한신청 건수", required = true)
  private int uPermitReqCount;
  @ApiModelProperty(value = "출력 건수", required = true)
  private int uPrintCount;
  @ApiModelProperty(value = "삭제 건수", required = true)
  private int uDeleteCount;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "취합일")
  private LocalDateTime uGatherDate;
}
