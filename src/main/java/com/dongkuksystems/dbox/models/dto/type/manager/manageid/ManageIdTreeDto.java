package com.dongkuksystems.dbox.models.dto.type.manager.manageid;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.etc.GwAddJobDetail;
import com.dongkuksystems.dbox.models.table.etc.GwDept;
import com.dongkuksystems.dbox.models.type.manager.manageid.ManageId;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class ManageIdTreeDto {
  @ApiModelProperty(value = "부서")
  private GwDept dept;
  @ApiModelProperty(value = "부서 소속인원")
  private List<ManageId> deptUsers;  
  @ApiModelProperty(value = "겸직정보")
  private List<GwAddJobDetail> addJob;
  @ApiModelProperty(value = "하위 부서")
  private List<ManageIdTreeDto> children;
}
