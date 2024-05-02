package com.dongkuksystems.dbox.models.dto.table.gwdept;

import io.swagger.annotations.ApiModelProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class GwDeptListManagerDto {

    private String rObjectId;

    @ApiModelProperty(value = "부서")
    private String deptId;

    @ApiModelProperty(value = "유저ID")
    private String userId;

    @ApiModelProperty(value = "성명")
    private String disPlayName;

    @ApiModelProperty(value = "사원번호")
    private String saBun;

    @ApiModelProperty(value = "회사ID")
    private String comOrgId;

    @ApiModelProperty(value = "부서ID")
    private String orgId;

    @ApiModelProperty(value = "부서명")
    private String orgNm;

    @ApiModelProperty(value = "직책")
    private String titleName;

    @ApiModelProperty(value = "관리자타입")
    private String uMgrType;

    @ApiModelProperty(value = "지정자")
    private String uAssignUser;

    @ApiModelProperty(value = "지정일")
    private String uAssignDate;

    @ApiModelProperty(value = "지정자타입")
    private String uAssignUserType;

    @ApiModelProperty(value = "지정자 명")
    private String assignUserName;

}
