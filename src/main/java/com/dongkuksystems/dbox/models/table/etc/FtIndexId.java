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
public class FtIndexId {
  @ApiModelProperty(value = "문서 ID", required = true)
  private String uDocId;
  @ApiModelProperty(value = "처리여부", required = true)
  private String uIndexYn;
}
