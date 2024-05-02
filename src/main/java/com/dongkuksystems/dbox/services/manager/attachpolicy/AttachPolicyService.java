package com.dongkuksystems.dbox.services.manager.attachpolicy;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.attachpolicy.CreateAttachPolicyDto;
import com.dongkuksystems.dbox.models.dto.type.manager.attachpolicy.PatchAttachPolicyDto;
import com.dongkuksystems.dbox.models.type.manager.AttachPolicy;

public interface AttachPolicyService {

  List<AttachPolicy> selectAll();

  String patchAttachPolicy(String rObjectId, UserSession userSession, PatchAttachPolicyDto dto) throws Exception;

  String createAttachPolicy(CreateAttachPolicyDto dto, UserSession userSession) throws Exception;

  String deleteAttachPolicy(String rObjectId, UserSession userSession) throws Exception;
}
