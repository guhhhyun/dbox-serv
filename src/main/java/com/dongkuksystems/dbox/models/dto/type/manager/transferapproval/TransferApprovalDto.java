package com.dongkuksystems.dbox.models.dto.type.manager.transferapproval;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.common.ManagerCommonDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;

@Data
@EqualsAndHashCode(callSuper = false)
public class TransferApprovalDto extends ManagerCommonDto {

    private String uReqUser;
    private String uReqTitle;
    private String startDate;
    private String endDate;

    private List<String> rObjectIds;
    private String action;

    public TransferApprovalDto rObjectId(String rObjectId) {
        setRObjectId(rObjectId);
        return this;
    }

    public TransferApprovalDto userSession(UserSession userSession) {
        setUserSession(userSession);
        return this;
    }

    public TransferApprovalDto action(String action) {
        setAction(action);
        return this;
    }

}
