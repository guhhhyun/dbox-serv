package com.dongkuksystems.dbox.services.reqdisposal;

import java.util.List;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.deletemanage.DeleteManageDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqClosedDetailDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqDisposalDetailDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqDisposalFilterDto;
import com.dongkuksystems.dbox.models.type.request.ReqDelete;

public interface ReqDisposalService {
  
  List<ReqClosedDetailDto> reqClosedList() throws Exception;

  ReqClosedDetailDto getClosedRequest(String rObjectId) throws Exception;
  
  String approveReqClosed(String closedRequestId, UserSession userSession, String ip, DeleteManageDto dto) throws Exception;

  String rejectReqClosed(String closedRequestId, UserSession userSession, String ip, DeleteManageDto dto) throws Exception;

  List<ReqDisposalDetailDto> reqDisposalList(String deptCode, ReqDisposalFilterDto dto);

  void registReqDisposal(UserSession userSession, ReqDelete dto, String ip) throws Exception;

  Boolean deleteReqDisposal(UserSession userSession, String rObjectId, ReqDisposalDetailDto dto, String ip) throws Exception;

  void patchReqDisposal(UserSession userSession, String rObjectId, String ip) throws Exception;

}
