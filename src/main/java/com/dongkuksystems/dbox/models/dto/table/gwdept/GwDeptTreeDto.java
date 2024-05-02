package com.dongkuksystems.dbox.models.dto.table.gwdept;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.etc.GwAddJobDetail;
import com.dongkuksystems.dbox.models.table.etc.VDept;
import com.dongkuksystems.dbox.models.table.etc.VUser;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GwDeptTreeDto {
  @ApiModelProperty(value = "부서")
  private VDept dept;
  @ApiModelProperty(value = "부서 소속인원")
  private List<VUser> deptUsers;  
  @ApiModelProperty(value = "겸직정보")
  private List<GwAddJobDetail> addJob;
  @ApiModelProperty(value = "하위 부서")
  private List<GwDeptTreeDto> children;
}
