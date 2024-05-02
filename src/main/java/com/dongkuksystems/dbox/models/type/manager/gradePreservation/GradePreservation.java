package com.dongkuksystems.dbox.models.type.manager.gradePreservation;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GradePreservation {
  @ApiModelProperty(value = "ObjecId")
  private String rObjectId;

  @ApiModelProperty(value = "회사코드")
  private String uComCode;

  @ApiModelProperty(value = "제한 기간")
  private String uSecSYear;

  @ApiModelProperty(value = "팀내")
  private String uSecTYear;

  @ApiModelProperty(value = "사내")
  private String uSecCYear;

  @ApiModelProperty(value = "그룹사내")
  private String uSecGYear;

  @ApiModelProperty(value = "영구설정")
  private String uPjtEverFlag;

  @ApiModelProperty(value = "자동연장")
  private String uAutoExtend;

}
