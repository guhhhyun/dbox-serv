package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.unlock.UserUnLockDto;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.unlockuser.UnLockUserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;


@RestController
@Api(tags = "사용자잠금해제신청 APIs")
public class UnLockUserController extends AbstractCommonController{
	private final UnLockUserService unLockUserService;
	
	public UnLockUserController(UnLockUserService unLockUserService) {
		this.unLockUserService = unLockUserService;
	}
	
	
	@PostMapping(value = "/api/unlock-requests")
	@ApiOperation(value = "잠금해제신청") 
	public ApiResult<Boolean> postUnlockUser( @AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "해제사유") @RequestBody UserUnLockDto userUnLockDto) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);

		String result = unLockUserService.postUnlockUser(userSession, userUnLockDto);

		boolean rst ;
		if(result.equals("success")) {
			rst = true;
		}else {
			rst = false;
		}
		return OK(rst);
	}
}

