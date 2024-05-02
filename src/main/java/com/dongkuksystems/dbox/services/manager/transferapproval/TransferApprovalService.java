package com.dongkuksystems.dbox.services.manager.transferapproval;

import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.dongkuksystems.dbox.daos.type.manager.transferapproval.TransferApprovalMapper;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.transferapproval.TransferApprovalDto;
import com.dongkuksystems.dbox.services.AbstractCommonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class TransferApprovalService extends AbstractCommonService {

    public static final String APPROVAL = "approval";

    @Autowired
    private TransferApprovalMapper transferApprovalMapper;

    public List<Map<String, Object>> selectTransferApprovals(TransferApprovalDto transferApprovalDto) {
        return transferApprovalMapper.selectTransferApprovals(transferApprovalDto);
    }

    public List<Map<String, Object>> selectReqData(String uReqId) {
        return transferApprovalMapper.selectReqData(uReqId);
    }

    public List<Map<String, Object>> selectReqUsers() {
        return transferApprovalMapper.selectReqUsers();
    }

    public void patchTransferApproval(TransferApprovalDto dto) {
        try {
            UserSession userSession = dto.getUserSession();
            String userId = userSession.getUser().getUserId();
            String action = APPROVAL.equals(dto.getAction()) ? "A" : "D";
            for (String rObjectId : dto.getRObjectIds()) {
                IDfPersistentObject idf_PObj = this.getIdfSession(userSession).getObject(new DfId(rObjectId));
                idf_PObj.setString("u_req_status", action);
                idf_PObj.setString("u_approver", userId);
                idf_PObj.setString("u_approve_date", new DfTime().toString());
                idf_PObj.save();
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
