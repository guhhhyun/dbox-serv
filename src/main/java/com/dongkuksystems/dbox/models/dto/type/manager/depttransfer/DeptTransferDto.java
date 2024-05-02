package com.dongkuksystems.dbox.models.dto.type.manager.depttransfer;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.common.ManagerCommonDto;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.util.List;


@Data
@EqualsAndHashCode(callSuper = false)
public class DeptTransferDto extends ManagerCommonDto {

    private String uFolType;
    private String uCabinetCode;
    private String uUpFolId;
    private String uFolName;

    private String uSendDeptCode;
    private String uRecvDeptCode;
    private String uSendCabinetCode;
    private String uRecvCabinetCode;

    private String uRecvFolId;

    private List<String> uSendFolIds;
    private List<String> uTransReqId;

    public DeptTransferDto rObjectId(String rObjectId) {
        setRObjectId(rObjectId);
        return this;
    }

    public DeptTransferDto userSession(UserSession userSession) {
        setUserSession(userSession);
        return this;
    }

    public DeptTransferDto uUpFolId(String uUpFolId) {
        setUUpFolId(uUpFolId);
        return this;
    }
}
