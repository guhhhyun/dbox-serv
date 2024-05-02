package com.dongkuksystems.dbox.models.dto.type.noti;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.common.ManagerCommonDto;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = false)
public class AlarmDto extends ManagerCommonDto {

  private String uPerformerId;
  private String action;

  public AlarmDto rObjectId(String rObjectId) {
    setRObjectId(rObjectId);
    return this;
  }

  public AlarmDto userSession(UserSession userSession) {
    setUserSession(userSession);
    return this;
  }

  public AlarmDto action(String action) {
    setAction(action);
    return this;
  }

}
