package com.dongkuksystems.dbox.models.dto.type.manager.managerconfig;

import java.util.List;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ManagerConfigDto {
	@ApiModelProperty(value = "그룹관리자 회사코드")
	private String groupComCode;
	@ApiModelProperty(value = "전사관리자 회사코드")
	private String companyComCode;
	@ApiModelProperty(value = "부서관리자 부서코드")
	private List<String> companyDeptCode;
	@ApiModelProperty(value = "부서관리자 회사코드")
	private String deptComCode;
}
