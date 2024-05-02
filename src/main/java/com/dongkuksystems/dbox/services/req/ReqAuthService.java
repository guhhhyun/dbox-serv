package com.dongkuksystems.dbox.services.req;

import java.util.List;
import java.util.Map;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.request.ReqApproveDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqAuthDetailDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqCreateDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqRejectDto;
import com.dongkuksystems.dbox.models.type.request.ReqAuth;


public interface ReqAuthService {
	
	List<ReqAuthDetailDto> reqAuthList() throws Exception;

	ReqAuthDetailDto getReqAuth(String rObjectId) throws Exception;

	String createReqAuth(UserSession userSession, ReqCreateDto dto, String ip) throws Exception;
	
	String approveReqAuth(String uReqDocId, UserSession userSession, String ip)throws Exception;
	Map<String, Integer> approveAllReqAuth(List<String> authRequestIdList, UserSession userSession, ReqApproveDto reqApproveDto, String ip) throws Exception;
	String rejectReqAuth(String authRequestId, UserSession userSession, String uRejectReason)throws Exception;
	Map<String, Integer> rejectAllReqAuth(List<String> authRequestIdList, UserSession userSession, String uRejectReason)throws Exception;

}
