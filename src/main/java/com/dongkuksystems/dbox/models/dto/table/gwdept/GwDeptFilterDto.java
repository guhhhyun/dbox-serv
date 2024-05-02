package com.dongkuksystems.dbox.models.dto.table.gwdept;

import java.util.Objects;

import com.dongkuksystems.dbox.constants.EntCode;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GwDeptFilterDto {
  @Builder.Default
  @ApiModelProperty(value = "임직원 포함 여부")
  boolean userYn = true; 
  @Builder.Default
  @ApiModelProperty(value = "임직원 상태(재직/etc..)")
  String userStatus = "A"; 
  @Builder.Default
  @ApiModelProperty(value = "겸직")
  boolean addJobYn = false; 
  @Builder.Default
  @ApiModelProperty(value = "부서코드")
  String deptId = EntCode.DKG.name();
  
  @Override
  public int hashCode() {
      return Objects.hash(this.userYn, this.userStatus, this.addJobYn, this.deptId);
  }
}
