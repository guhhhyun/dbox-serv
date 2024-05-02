package com.dongkuksystems.dbox.models.dto.type.auth;

import java.util.List;

import com.dongkuksystems.dbox.constants.DrmSecLevelType;
import com.dongkuksystems.dbox.models.dto.etc.DrmCompanyDto;
import com.dongkuksystems.dbox.models.dto.etc.DrmDeptDto;
import com.dongkuksystems.dbox.models.dto.etc.DrmUserDto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class DrmAuthDto {
  @ApiModelProperty(value = "authDeptList")
  List<DrmDeptDto> authDeptList;
  @ApiModelProperty(value = "authUserList")
  List<DrmUserDto> authUserList;
  @ApiModelProperty(value = "company")
  DrmCompanyDto company;
  @ApiModelProperty(value = "secLevelType")
  DrmSecLevelType secLevelType;
}
