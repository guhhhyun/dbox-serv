package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.data.DataUpdateReqDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqApproveDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqAuthDetailDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqCreateDto;
import com.dongkuksystems.dbox.models.dto.type.request.ReqRejectDto;
import com.dongkuksystems.dbox.models.type.request.ReqAuth;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.req.ReqAuthService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;


@RestController
@Api(tags = "권한신청 APIs")
public class ReqAuthController extends AbstractCommonController{
	private final ReqAuthService reqAuthService;
	
	public ReqAuthController(ReqAuthService reqAuthService) {
		this.reqAuthService = reqAuthService;
	}
	
@GetMapping("/auth-requests")
@ApiOperation(value = "권한신청 조회")
public ApiResult<List<ReqAuthDetailDto>> reqAuthList(@AuthenticationPrincipal JwtAuthentication authentication) throws Exception {
	
	List<ReqAuthDetailDto> tree = reqAuthService.reqAuthList();
	return OK(tree);
}

	@GetMapping("/auth-requests/{rObjectId}")
	@ApiOperation(value = "권한신청 조회")
	public ApiResult<ReqAuthDetailDto> getReqAuth(@PathVariable String rObjectId) throws Exception {
		return OK(reqAuthService.getReqAuth(rObjectId));
	}

@PostMapping("/auth-requests")
@ApiOperation(value = "권한신청 생성")
public ApiResult<ReqAuth> createReqAuth(@AuthenticationPrincipal JwtAuthentication authentication,
//		@ApiParam(value = "권한신청 생성정보") @ModelAttribute ReqCreateDto reqCreateDto
		@RequestBody ReqCreateDto reqCreateDto,  HttpServletRequest request
) throws Exception {
	UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
								UserSession.class);
	String ip = getClientIp(request);
	String rst = reqAuthService.createReqAuth(userSession, reqCreateDto, ip);

	return OK(null);
 }

@PostMapping("/auth-requests/{authRequestId}/approve")
@ApiOperation(value = "권한신청 승인")
public ApiResult<ReqAuth> approveReqAuth(@AuthenticationPrincipal JwtAuthentication authentication,
		 @PathVariable String authRequestId,  HttpServletRequest request)throws Exception {
	UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
			UserSession.class);
	String ip = getClientIp(request);
	String rst = reqAuthService.approveReqAuth(authRequestId, userSession, ip);
	
	return OK(null);
}

@PostMapping("/auth-requests/approve")
@ApiOperation(value = "권한신청 일괄승인")
public ApiResult<Map<String, Integer>> approveAllReqAuth(@AuthenticationPrincipal JwtAuthentication authentication,
		@RequestParam String authRequestIds, @RequestBody ReqApproveDto reqApproveDto,  HttpServletRequest request)throws Exception {
	List<String> authRequestIdList = new ObjectMapper().readValue(authRequestIds,
			new TypeReference<List<String>>() {
	});
	UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
			UserSession.class);
	String ip = getClientIp(request);
	Map<String, Integer> result = reqAuthService.approveAllReqAuth(authRequestIdList, userSession, reqApproveDto, ip);
	
	return OK(result);
}

@PostMapping("/auth-requests/{authRequestId}/reject")
@ApiOperation(value = "권한신청 반려")
public ApiResult<ReqAuth> rejectReqAuth(@AuthenticationPrincipal JwtAuthentication authentication,
		 @PathVariable String authRequestId, 
		 String uRejectReason)throws Exception {

	UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
			UserSession.class);
	
	String rst = reqAuthService.rejectReqAuth(authRequestId, userSession, uRejectReason);
	
	return OK(null);
}

@PostMapping("/auth-requests/reject")
@ApiOperation(value = "권한신청 일괄반려")
public ApiResult<Map<String, Integer>> rejectAllReqAuth(@AuthenticationPrincipal JwtAuthentication authentication,
		@RequestParam String authRequestIds, String uRejectReason)throws Exception {
	List<String> authRequestIdList = new ObjectMapper().readValue(authRequestIds,
			new TypeReference<List<String>>() {
	});

	UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
			UserSession.class);
	
	Map<String, Integer> result = reqAuthService.rejectAllReqAuth(authRequestIdList, userSession, uRejectReason);
	
	return OK(result);
}

}

