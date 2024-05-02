package com.dongkuksystems.dbox.daos.type.manager.depttransfer;

import com.dongkuksystems.dbox.models.dto.type.manager.depttransfer.DeptTransferDto;

import java.util.List;
import java.util.Map;

public interface DeptTransferMapper {

    List<Map<String, Object>> selectDeptTransfers();

    List<Map<String, Object>> selectFolders(DeptTransferDto deptTransferDto);

    List<Map<String, Object>> selectDeptTransfersRequested(String uDeptCode);

    Map<String, Object> selectAggregateDataToTransfer(List<String> rObjectIds);

    int deleteDeptTransfersRequested(String rObjectId);

}