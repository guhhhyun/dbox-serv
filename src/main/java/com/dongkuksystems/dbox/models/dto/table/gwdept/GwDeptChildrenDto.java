package com.dongkuksystems.dbox.models.dto.table.gwdept;

import java.util.List;

import com.dongkuksystems.dbox.models.table.etc.GwAddJob;
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
public class GwDeptChildrenDto { 
  @ApiModelProperty(value = "부서")
  private VDept vDept;
  @ApiModelProperty(value = "하위 부서 리스트")
  private List<VDept> deptList;  
  @ApiModelProperty(value = "부서 소속인원")
  private List<VUser> deptUsers;  
  @ApiModelProperty(value = "겸직 정보")
  private List<GwAddJob> addJob;
  
}
