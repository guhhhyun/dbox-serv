package com.dongkuksystems.dbox.models.table.etc;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GwAddJob {
  @ApiModelProperty(value = "겸직 Id")
  private int ajId;
  @ApiModelProperty(value = "직책코드")
  private String titleCode;
  @ApiModelProperty(value = "직위코드")
  private String pstnCode;
  @ApiModelProperty(value = "직위명")
  private String pstnName;
  @ApiModelProperty(value = "유저아이디")
  private String personCode;
  @ApiModelProperty(value = "직책명")
  private String titleName;
  @ApiModelProperty(value = "겸직부서 (org_id)")
  private String unitCode;
  @ApiModelProperty(value = "팀이름")
  private String unitName;
  
  @ApiModelProperty(value = "겸직부서 cabinetcode", notes = "임의추가")
  private String unitCabinetCode;
}
