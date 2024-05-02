package com.dongkuksystems.dbox.services.manager.attachpolicyuser;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.attachpolicy.AttachPolicyUserDto;
import com.dongkuksystems.dbox.models.dto.type.manager.attachpolicy.CreateAttachPolicyUserDto;
import com.dongkuksystems.dbox.models.dto.type.manager.attachpolicy.UpdateUserDateDto;
import com.dongkuksystems.dbox.models.type.manager.AttachPolicyUser;

public interface AttachPolicyUserService {

  List<AttachPolicyUser> selectAll(AttachPolicyUserDto dto);

  String createAttachPolicyUser(CreateAttachPolicyUserDto dto, UserSession userSession) throws Exception;

  String deleteAttachPolicyUser(String rObjectId, UserSession userSession) throws Exception;

  String updateUserDate(UpdateUserDateDto dto, UserSession userSession) throws Exception;

}
