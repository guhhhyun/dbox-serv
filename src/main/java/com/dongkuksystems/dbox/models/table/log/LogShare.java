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
public class LogShare {
  @ApiModelProperty(value = "문서 키/폴더 ID", required = true)
  private String uObjId;
  @ApiModelProperty(value = "문서/폴더 구분     D:문서, F:폴더", required = true)
  private String uObjType;
  @ApiModelProperty(value = "문서상태    L:Live, C:Closed")
  private String uDocStatus;
  @ApiModelProperty(value = "권한구분  R:조회, D:삭제 ")
  private String uPermitType;
  @ApiModelProperty(value = "소유부서여부")
  private String uOwnDeptYn;
  @ApiModelProperty(value = "권한자 ID")
  private String uAuthorId; 
  @ApiModelProperty(value = "권한자 타입   D:부서, P:개인")
  private String uAuthorType;
  @ApiModelProperty(value = "작업자")
  private String uJobUser;
  @JsonFormat(pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "작업 일시")
  private LocalDateTime uJobDate;
  @ApiModelProperty(value = "사용자 IP")
  private String uUserIp;
  @ApiModelProperty(value = "작업구분")
  private String uJobGubun;
  
  public LogShare(String uObjId, String uObjType, String uDocStatus, String uPermitType, String uOwnDeptYn,
      String uAuthorId, String uAuthorType, String uJobUser, LocalDateTime uJobDate, String uUserIp, String uJobGubun) {
    checkNotNull(uObjId, "uObjId must be provided.");
    checkNotNull(uObjType, "uObjType must be provided.");
    checkNotNull(uDocStatus, "uDocStatus must be provided.");
    checkNotNull(uPermitType, "uPermitType must be provided.");
    checkNotNull(uOwnDeptYn, "uOwnDeptYn must be provided.");
    checkNotNull(uAuthorId, "uAuthorId must be provided.");
    checkNotNull(uAuthorType, "uAuthorType must be provided.");
    checkNotNull(uJobUser, "uJobUser must be provided.");
    checkNotNull(uUserIp, "uUserIp must be provided.");
    this.uObjId = uObjId;
    this.uObjType = uObjType;
    this.uDocStatus = uDocStatus;
    this.uPermitType = uPermitType;
    this.uOwnDeptYn = uOwnDeptYn;
    this.uAuthorId = uAuthorId;
    this.uAuthorType = uAuthorType;
    this.uJobUser = uJobUser;
    this.uJobDate = uJobDate;
    this.uUserIp = uUserIp;
    this.uJobGubun = uJobGubun;
  }
  
}
