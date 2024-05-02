package com.dongkuksystems.dbox.models.type.docbox;

import com.dongkuksystems.dbox.models.table.etc.VDept;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class ResearchRepeating {
  @ApiModelProperty(value = "objectId")
  private String rObjectId;
  @ApiModelProperty(value = "index")
  private String iPosition;
  @ApiModelProperty(value = "참여부서(조회/다운로드)")
  private String uJoinDeptRead;
  @ApiModelProperty(value = "참여부서(편집/삭제)")
  private String uJoinDeptDel;
  @ApiModelProperty(value = "분류폴더 아이디")
  private String uFolId;
  private String uJoinDeptReadOrgId;
  @ApiModelProperty(value = "참여부서(편집/삭제)")
  private String uJoinDeptDelOrgId;

  @ApiModelProperty(value = "참여부서(조회/다운로드) 상세")
  private VDept joinDeptReadDetail;
  @ApiModelProperty(value = "참여부서(편집/삭제) 상세")
  private VDept joinDeptDelDetail;
}
