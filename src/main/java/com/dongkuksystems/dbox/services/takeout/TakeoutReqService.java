package com.dongkuksystems.dbox.services.takeout;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import com.documentum.fc.common.DfException;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.code.CodeDetailDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutApproveDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutConfigDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutCreateDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutCreateFreeDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutDetailDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqTakeoutRejectDto;
import com.dongkuksystems.dbox.models.type.manager.TakeoutConfig;
import com.dongkuksystems.dbox.models.type.request.ReqTakeout;
import com.dongkuksystems.dbox.models.type.request.ReqTakeoutDoc;

public interface TakeoutReqService {

  int selectCountByReqDocId(String uReqId) throws Exception;
  
	List<ReqTakeoutDoc> selectOneByReqId(String reqId) throws Exception;
	
	TakeoutConfig seletOneByDeptCode(String objectId, String mode) throws Exception;
	
	List<ReqTakeoutConfigDto> nameListByDeptCode(String deptCode) throws Exception;
	
	List<ReqTakeoutDetailDto> takeoutDetailList(ReqTakeout takeout) throws Exception;
	
	List<ReqTakeoutDetailDto> takeoutDetailList() throws Exception;
	
	List<ReqTakeoutDetailDto> takeoutDetailListByObjId(String takeoutRequestId) throws Exception;
	
	String createReqTakeout(UserSession userSession, ReqTakeoutCreateDto takeoutCreateDto, List<String> docIdList, String ip)throws Exception;

	String approveReqTakeout(String takeoutRequestId, UserSession userSession, String ip)throws Exception;

	Map<String, Integer> approveAllReqTakeout(UserSession userSession, List<String> takeoutRequestIdList, String ip)throws Exception;

	String rejectReqTakeout(String takeoutRequestId, UserSession userSession, String rejectReason) throws Exception;

	Map<String, Integer> rejectAllReqTakeout(UserSession userSession, String rejectReason,
			List<String> takeoutRequestIdList) throws Exception;

	void patchTakeoutConfig(UserSession userSession, ReqTakeoutConfigDto dto) throws Exception;

  void deleteTakeoutConfig(UserSession userSession, ReqTakeoutConfigDto dto) throws Exception;

  List<ReqTakeout> takeoutListByDeptCode(String deptCode, ReqTakeoutDto dto) throws Exception;

  List<ReqTakeoutDto> takeoutListByReqId(String reqId);

}
