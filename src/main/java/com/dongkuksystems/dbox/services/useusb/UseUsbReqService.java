package com.dongkuksystems.dbox.services.useusb;

import java.util.List;
import java.util.Map;

import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.request.ReqUseUsbApprovalListDto;
import com.dongkuksystems.dbox.models.type.request.ReqUseUsb;

public interface UseUsbReqService {
	List<ReqUseUsbApprovalListDto> selectReqUseUsbApprovList(String userId) throws Exception;
	
	String createReqUseUsb(UserSession userSession,ReqUseUsb reqUseUsb) throws Exception;

	String approveReqUseUsb(String useUsbRequestId, UserSession userSession, ReqUseUsb reqUseUsb)throws Exception;
	String rejectReqUseUsb(String useUsbRequestId, UserSession userSession, ReqUseUsb reqUseUsb)throws Exception;

	Map<String, Integer> approveAllReqUseUsb(UserSession userSession, ReqUseUsb reqUseUsb, List<String> useUsbRobjectIdList)throws Exception;
	Map<String, Integer> rejectAllReqUseUsb(UserSession userSession, ReqUseUsb reqUseUsb, List<String> useUsbRobjectIdList) throws Exception;
}
