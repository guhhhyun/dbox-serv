package com.dongkuksystems.dbox.services.approval;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;

import com.documentum.fc.common.IDfId;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.doc.DocDetailDto;

public interface DboxApprovalService {

  String TakeOutApproval(String reqId, UserSession userSession, String reqDeptCode, String approveReason,  List<String> docIdList) throws Exception;

  String CreateUseUsbApproval(String reqId) throws Exception;
  



}
