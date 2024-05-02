package com.dongkuksystems.dbox.services.manager.disposalrequest;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.deletemanage.DeleteManageDto;
import com.dongkuksystems.dbox.models.dto.type.manager.disposalrequest.DisposalRequestDto;
import com.dongkuksystems.dbox.models.type.manager.disposalrequest.DisposalRequest;

public interface DisposalRequestService {
  List<DisposalRequest> selectDisposalRequest(DisposalRequestDto dto);
  
  String restoreReqClosed(String closedRequestId, UserSession userSession, String userIp, DeleteManageDto dto) throws Exception;
}
