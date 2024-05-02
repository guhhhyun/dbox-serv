package com.dongkuksystems.dbox.models.dto.type.manager.deptmanager;

import java.util.List;

import lombok.Data;

@Data
public class DeptManagerDto {

    private String rObjectId;
    private String comCode;
    private String deptCode;
    private String pstnName;
    private String userId;
    private List<String> deptCodeList;
}
