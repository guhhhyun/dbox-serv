package com.dongkuksystems.dbox.models.table.log;

import static com.google.common.base.Preconditions.checkNotNull;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import io.swagger.annotations.ApiModelProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data 
@NoArgsConstructor
@Builder 
public class LogUsb {
  @ApiModelProperty(value = "문서명", required = true)
  private String uFileName;
  @ApiModelProperty(value = "파일 확장자", required = true)
  private String uFileExt;
  @ApiModelProperty(value = "파일크기", required = true)
  private int uFileSize;
  @ApiModelProperty(value = "작업자", required = true)
  private String uJobUser;
  @ApiModelProperty(value = "작업자 부서", required = true)
  private String uDeptCode;
  @ApiModelProperty(value = "작업자 회사코드", required = true)
  private String uComCode;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "작업 일시")
  private LocalDateTime uJobDate;
  @ApiModelProperty(value = "작업자 IP", required = true)
  private String uUserIp;
  
  public LogUsb(String uFileName, String uFileExt, int uFileSize, String uJobUser, String uDeptCode, String uComCode,
      LocalDateTime uJobDate, String uUserIp) {
    checkNotNull(uFileName, "uFileName must be provided.");
    checkNotNull(uFileExt, "uFileExt must be provided.");
    checkNotNull(uFileSize, "uFileSize must be provided.");
    checkNotNull(uJobUser, "uJobUser must be provided.");
    checkNotNull(uDeptCode, "uDeptCode must be provided.");
    checkNotNull(uComCode, "uComCode must be provided.");
    checkNotNull(uUserIp, "uUserIp must be provided.");
    this.uFileName = uFileName;
    this.uFileExt = uFileExt;
    this.uFileSize = uFileSize;
    this.uJobUser = uJobUser;
    this.uDeptCode = uDeptCode;
    this.uComCode = uComCode;
    this.uJobDate = uJobDate;
    this.uUserIp = uUserIp;
  }
  
}
