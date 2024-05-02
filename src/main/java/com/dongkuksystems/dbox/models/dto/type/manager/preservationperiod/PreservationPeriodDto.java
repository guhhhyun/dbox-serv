package com.dongkuksystems.dbox.models.dto.type.manager.preservationperiod;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.common.ManagerCommonDto;
import lombok.Data;
import lombok.EqualsAndHashCode;


@Data
@EqualsAndHashCode(callSuper = false)
public class PreservationPeriodDto extends ManagerCommonDto {
    private String rObjectId;
    private String uComCode;
    private String uSecSYear;
    private String uSecTYear;
    private String uSecCYear;
    private String uSecGYear;
    private String uPjtEverFlag;
    private String uAutoExtend;
    private String uNoExtDept;
    private String uNoExtRegDate;
    private String uNoExtUnregDate;
    private String type;

    public PreservationPeriodDto rObjectId(String rObjectId) {
        setRObjectId(rObjectId);
        return this;
    }

    public PreservationPeriodDto userSession(UserSession userSession) {
        setUserSession(userSession);
        return this;
    }

}
