package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.request.ReqUseUsbApprovalListDto;
import com.dongkuksystems.dbox.models.table.etc.VUser;
import com.dongkuksystems.dbox.models.type.request.ReqUseUsb;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.user.UserService;
import com.dongkuksystems.dbox.services.useusb.UseUsbReqService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(tags = "저장매체 사용 신청")
public class UseUsbReqController extends AbstractCommonController {
	private final UseUsbReqService useUsbReqService;
	private final UserService userService;

	public UseUsbReqController(UseUsbReqService useUsbReqService, UserService userService) {
		this.useUsbReqService = useUsbReqService;
		this.userService = userService;
	}

	@PostMapping("/usb-request/info")
	@ApiOperation(value = "외부저장매체 기본정보 조회")
	public ApiResult<Map<String, Object>> useUsbInfo(
			@AuthenticationPrincipal JwtAuthentication authentication,
			@RequestBody Map<String, Object> param 
	) throws Exception {
		
	List<String> userIds = (List<String>) param.get("userIds");
    List<ReqUseUsbApprovalListDto> approveList = null;
    List<VUser> users = null;
	    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
	        UserSession.class);
//	    result = useUsbReqService.selectReqUseUsbApprovList("changmyoun.ji");
	    approveList = useUsbReqService.selectReqUseUsbApprovList(userSession.getUser().getUserId());
	    
	    users = userService.selectUserListByUserIds(userIds);
	    
	    Map<String, Object> rtn = new HashMap<String, Object>();
	    rtn.put("approveList",approveList);
	    rtn.put("users",users);
		return OK(rtn);
	}

	@PostMapping("/usb-request")
	@ApiOperation(value = "외부저장매체 승인요청")
	public ApiResult<String> createUseUsb(
		@AuthenticationPrincipal JwtAuthentication authentication,
		@RequestBody ReqUseUsb reqUseUsb
	) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		return OK(useUsbReqService.createReqUseUsb(userSession, reqUseUsb));
	}

	@PatchMapping("/usb-request/{useUsbRobjectId}/approve")
	@ApiOperation(value = "외부저장매체 승인")
	public ApiResult<String> approveUseUsb(
		@AuthenticationPrincipal JwtAuthentication authentication,
		@ApiParam(value = "use usb rObjectId", example = "000004d280006113") @PathVariable String useUsbRobjectId
	) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		String rst = useUsbReqService.approveReqUseUsb(useUsbRobjectId, userSession, new ReqUseUsb());
		return OK(rst);
	}
	
	@PatchMapping("/usb-request/{useUsbRobjectId}/reject")
	@ApiOperation(value = "외부저장매체 반려")
	public ApiResult<String> rejectUseUsb(
		@AuthenticationPrincipal JwtAuthentication authentication,
		@ApiParam(value = "use usb rObjectId", example = "000004d280006113") @PathVariable String useUsbRobjectId
	) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		String rst = useUsbReqService.rejectReqUseUsb(useUsbRobjectId, userSession, new ReqUseUsb());
		return OK(rst);
	}
	
	@PatchMapping("/usb-request/approve")
	@ApiOperation(value = "외부저장매체 일괄승인")
	public ApiResult<Map<String, Integer>> approveAllUseUsb(
		@AuthenticationPrincipal JwtAuthentication authentication,
//		@RequestParam String useUsbRobjectIds,
		@RequestBody ReqUseUsb reqUseUsb
	) throws Exception {
//		List<String> useUsbRobjectIdList = new ObjectMapper().readValue(useUsbRobjectIds,
//				new TypeReference<List<String>>() {
//				});
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);

		Map<String, Integer> result = useUsbReqService.approveAllReqUseUsb(userSession, reqUseUsb, reqUseUsb.getRObjectIds());
		return OK(result);
	}

	@PatchMapping("/usb-request/reject")
	@ApiOperation(value = "외부저장매체 일괄반려")
	public ApiResult<Map<String, Integer>> rejectAllUseUsb(
		@AuthenticationPrincipal JwtAuthentication authentication,
//		@RequestParam String takeoutRequestIds,
		@RequestBody ReqUseUsb reqUseUsb
	) throws Exception {
//		List<String> useUsbRobjectIdList = new ObjectMapper().readValue(takeoutRequestIds,
//				new TypeReference<List<String>>() {
//				});
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);

		Map<String, Integer> result = useUsbReqService.rejectAllReqUseUsb(userSession, reqUseUsb, reqUseUsb.getRObjectIds());
		return OK(result);
	}

}
