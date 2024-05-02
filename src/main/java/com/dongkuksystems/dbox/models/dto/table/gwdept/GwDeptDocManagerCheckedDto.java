package com.dongkuksystems.dbox.models.dto.table.gwdept;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.table.gwdept.GwDeptDocManagerCheckedDto.GwDeptDocManagerCheckedDtoBuilder;
import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GwDeptDocManagerCheckedDto {
  	@ApiModelProperty(value = "체크 여부")
  	private boolean docManagerChecked;

  	
 
  
  
}
