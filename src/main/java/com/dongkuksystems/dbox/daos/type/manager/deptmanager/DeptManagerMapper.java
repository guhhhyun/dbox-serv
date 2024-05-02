package com.dongkuksystems.dbox.daos.type.manager.deptmanager;

import com.dongkuksystems.dbox.models.dto.type.manager.deptmanager.DeptManagerDto;

import java.util.List;
import java.util.Map;

public interface DeptManagerMapper {

    List<Map<String, Object>> selectDeptManagers(DeptManagerDto deptManagerDto);

    List<Map<String, Object>> selectPositions();
}