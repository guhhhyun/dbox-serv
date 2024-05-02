package com.dongkuksystems.dbox.models.dto.type.user;

import java.util.ArrayList;
import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.manager.managerconfig.ManagerConfigDto;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Builder.Default;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder 
public class UserDSearchAuthDto {
  @Default
  @ApiModelProperty(value = "그룹")
  private List<String> groups = new ArrayList<>();
  @Default
  @ApiModelProperty(value = "관리자")
  private ManagerConfigDto mgr = new ManagerConfigDto();
}
