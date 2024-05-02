package com.dongkuksystems.dbox.models.dto.type.manager.managerconfig;

import java.util.List;

import com.dongkuksystems.dbox.models.dto.type.code.CodeFilterDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AutoClosingDto extends CodeFilterDto {

    private String rObjectId;
    private String comOrgId;
    private String orgId;
    private String uRegUser;
    private String objectName;
    private String startDate;
    private String endDate;
    private List<String> deptCodeList;
    private int versionCount = 1;

    public AutoClosingDto rObjectId(String rObjectId) {
        this.rObjectId = rObjectId;
        return this;
    }
}