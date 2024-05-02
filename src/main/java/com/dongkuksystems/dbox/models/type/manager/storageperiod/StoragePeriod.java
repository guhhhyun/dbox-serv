package com.dongkuksystems.dbox.models.type.manager.storageperiod;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class StoragePeriod {
  @ApiModelProperty(value = "ObjecId")
  private String rObjectId;
  @ApiModelProperty(value = "회사코드")
  private String uCodeVal1;
  @ApiModelProperty(value = "설정항목")
  private String uCodeVal2;
  @ApiModelProperty(value = "설정값")
  private String uCodeVal3;
  @ApiModelProperty(value = "삭제스케줄구분")
  private String methodName;
  @ApiModelProperty(value = "시작시간")
  private String startDate;
  @ApiModelProperty(value = "설정시간")
  private String aNextInvocation;
  @ApiModelProperty(value = "회사코드")
  private String methodArguments;
  @ApiModelProperty(value = "시간값")
  private String hourtime;  
}
