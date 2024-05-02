package com.dongkuksystems.dbox.models.dto.etc;

import lombok.Data;

@Data
public class DBoxSearch {

    private String userId;
    private String searchName;
    private String deptCode;
    private String folderCode;
    private String folderType;

    public DBoxSearch(String userId, String searchName, String deptCode, String folderCode, String folderType) {
        this.userId = userId;
        this.searchName = searchName;
        this.deptCode = deptCode;
        this.folderCode = folderCode;
        this.folderType = folderType;
    }
}
