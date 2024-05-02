package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.Map;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.constants.EntCode;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.code.CodeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api")
@Api(tags = "기능별 제한값 APIs")
public class LimitValueController extends AbstractCommonController {
	private final CodeService codeService;

	public LimitValueController(CodeService codeService) {
		this.codeService = codeService;
	}

	@GetMapping("/limit-values")
	@ApiOperation(value = "기능별 제한값 리스트 조회")
	public ApiResult<Map<String, String>> getCodeList(@AuthenticationPrincipal JwtAuthentication authentication) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		
		String comOrgId = userSession.getUser().getComOrgId();
		EntCode entCode = EntCode.valueOf(comOrgId);
		Map<String, String> result = codeService.getConfigDocHandleLimitMap(entCode);

		return OK(result);
	}
}
