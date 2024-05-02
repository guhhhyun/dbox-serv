package com.dongkuksystems.dbox.services.manager.deptmanager;

import com.dongkuksystems.dbox.daos.type.manager.deptmanager.DeptManagerMapper;
import com.dongkuksystems.dbox.models.dto.type.manager.deptmanager.DeptManagerDto;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import com.dongkuksystems.dbox.services.manager.common.ManagerCommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class DeptManagerService extends AbstractCommonService {

    @Autowired
    private DeptManagerMapper deptManagerMapper;

    @Autowired
    private ManagerCommonService managerCommonService;

    public List<Map<String, Object>> selectDeptManagers(DeptManagerDto deptManagerDto) {
        return deptManagerMapper.selectDeptManagers(deptManagerDto);
    }

    public List<Map<String, Object>> selectPositions() {
        return deptManagerMapper.selectPositions();
    }

    public void deleteDeptManager(String rObjectId, String deptId, String userId) {
        managerCommonService.deleteDeptManager(rObjectId, deptId, userId);
    }
}