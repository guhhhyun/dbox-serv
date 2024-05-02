package com.dongkuksystems.dbox.models.dto.type.manager.managerconfig;

import java.util.List;

import lombok.Data;

@Data
public class LockedDataDto {

    private String comOrgId;
    private String orgId;
    private String uRegUser;
    private String startDate;
    private String endDate;
    private List<String> deptCodeList;
}
