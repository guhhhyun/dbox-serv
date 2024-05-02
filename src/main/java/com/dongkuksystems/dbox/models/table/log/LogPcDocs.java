package com.dongkuksystems.dbox.models.table.log;

import static com.google.common.base.Preconditions.checkNotNull;

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
@NoArgsConstructor
@Builder 
public class LogPcDocs {
  @ApiModelProperty(value = "작업자 회사코드", required = true)
  private String uComCode;
  @ApiModelProperty(value = "작업자 부서", required = true)
  private String uDeptCode;
  @ApiModelProperty(value = "사용자", required = true)
  private String uUserId;
  @ApiModelProperty(value = "파일명", required = true)
  private String uFileName;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "접근 일시")
  private LocalDateTime uCreateDate;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "접근 일시")
  private LocalDateTime uModifyDate;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "접근 일시")
  private LocalDateTime uAccessDate;
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "로그기록일")
  private LocalDateTime uLogDate;
  @ApiModelProperty(value = "파일크기", required = true)
  private int uFileSize;
  
  public LogPcDocs(String uComCode, String uDeptCode, String uUserId, String uFileName, LocalDateTime uCreateDate,
      LocalDateTime uModifyDate, LocalDateTime uAccessDate, LocalDateTime uLogDate, int uFileSize) {
    checkNotNull(uComCode, "uComCode must be provided.");
    checkNotNull(uDeptCode, "uDeptCode must be provided.");
    checkNotNull(uUserId, "uUserId must be provided.");
    checkNotNull(uFileName, "uFileName must be provided.");
    checkNotNull(uFileSize, "uFileSize must be provided.");
    checkNotNull(uCreateDate, "uCreateDate must be provided.");
    // TODO: (유두연) 파일생성일, 접근일이 필수값인지 무슨값인지~?
    this.uComCode = uComCode;
    this.uDeptCode = uDeptCode;
    this.uUserId = uUserId;
    this.uFileName = uFileName;
    this.uCreateDate = uCreateDate;
    this.uModifyDate = uModifyDate;
    this.uAccessDate = uAccessDate;
    this.uLogDate = uLogDate;
    this.uFileSize = uFileSize;
  } 
}
