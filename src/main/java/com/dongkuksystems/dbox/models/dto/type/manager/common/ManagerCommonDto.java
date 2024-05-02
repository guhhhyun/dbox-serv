package com.dongkuksystems.dbox.models.dto.type.manager.common;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.google.common.base.CaseFormat;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ManagerCommonDto {

    private String rObjectId;
    private UserSession userSession;

}
