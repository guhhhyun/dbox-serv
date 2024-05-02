package com.dongkuksystems.dbox.models.table.etc;

import java.time.LocalDateTime;

import com.dongkuksystems.dbox.models.type.code.Code;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.deser.LocalDateTimeDeserializer;
import com.fasterxml.jackson.datatype.jsr310.ser.LocalDateTimeSerializer;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Builder.Default;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VDept {
  @ApiModelProperty(value = "ObjecId")
  private String rObjectId;
  @ApiModelProperty(value = "6", required = true)
  private String uCabinetCode;
  @ApiModelProperty(value = "7")
  private int uCloseState;

  @JsonProperty("uCreateDate")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "8")
  private LocalDateTime uCreateDate;

  @JsonProperty("uUpdateDate")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "9")
  private LocalDateTime uUpdateDate;

  @JsonProperty("insertDate")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd kk:mm:ss")
  @JsonSerialize(using = LocalDateTimeSerializer.class)
  @JsonDeserialize(using = LocalDateTimeDeserializer.class)
  @ApiModelProperty(value = "2")
  private LocalDateTime insertDate;
  @ApiModelProperty(value = "회사 ID")
  private String comOrgId;
  @ApiModelProperty(value = "회사 명")
  private String comOrgNm;
  @ApiModelProperty(value = "부서코드", required = true)
  private String orgId;
  @ApiModelProperty(value = "부서명")
  private String orgNm;
  @ApiModelProperty(value = "1")
  private String shortName;
  @ApiModelProperty(value = "지역코드")
  private String regionCode;
  @ApiModelProperty(value = "상위부서코드")
  private String upOrgId;
  @ApiModelProperty(value = "팀장사용자 ID")
  private String managerPerId;
  @ApiModelProperty(value = "정렬키")
  private String sortKey;
  @ApiModelProperty(value = "사용여부")
  private String usageState;
  @ApiModelProperty(value = "전체부서고유키값")
  private String unitFullId;
  @ApiModelProperty(value = "전체부서명")
  private String unitFullName;
  @ApiModelProperty(value = "전체부서정렬")
  private String siteId;
  @ApiModelProperty(value = "부서구분코드")
  private String unitTypeCd;
  @ApiModelProperty(value = "해외근무여부")
  private String overseaWork;
  @ApiModelProperty(value = "3")
  private int sortNo;
  @ApiModelProperty(value = "부서타입")
  private String orgType;
  @ApiModelProperty(value = "4")
  private String bandYn;

  @ApiModelProperty(value = "회사")
  private Code companyDetail;
  @Default
  @ApiModelProperty(value = "4")
  private String communityId = "d4c5862ab1c05245b7863757d659d4ae";
}
