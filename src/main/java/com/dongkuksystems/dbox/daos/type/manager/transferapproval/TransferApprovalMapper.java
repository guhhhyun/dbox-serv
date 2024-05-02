package com.dongkuksystems.dbox.daos.type.manager.transferapproval;

import com.dongkuksystems.dbox.models.dto.type.manager.transferapproval.TransferApprovalDto;

import java.util.List;
import java.util.Map;

public interface TransferApprovalMapper {

    List<Map<String, Object>> selectTransferApprovals(TransferApprovalDto transferApprovalDto);

    List<Map<String, Object>> selectReqData(String uReqId);

    List<Map<String, Object>> selectReqUsers();

}