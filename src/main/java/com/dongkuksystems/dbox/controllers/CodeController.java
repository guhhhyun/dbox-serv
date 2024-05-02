package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;
import java.util.Set;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.code.CodeDetailDto;
import com.dongkuksystems.dbox.models.dto.type.code.CodeFilterDto;
import com.dongkuksystems.dbox.models.type.code.CodeLogviewAuth;
import com.dongkuksystems.dbox.models.type.manager.hisviewuser.HisViewUser;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.code.CodeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api")
@Api(tags = "코드 APIs")
public class CodeController extends AbstractCommonController {
	private final CodeService codeService;

	public CodeController(CodeService codeService) {
		this.codeService = codeService;
	}

	@GetMapping("/codes")
	@ApiOperation(value = "코드 리스트 조회")
	public ApiResult<List<CodeDetailDto>> getCodeList(@ModelAttribute CodeFilterDto filter) throws Exception {
		List<CodeDetailDto> result = codeService.getCodeList(filter);

		return OK(result);
	}

  @GetMapping("/codes/SPECIAL_USER/cached")
  @ApiOperation(value = "특별사용자 캐시값으로 조회")
  public ApiResult<Set<String>> getSpecialUserCached(@ModelAttribute CodeFilterDto filter) throws Exception {
    Set<String> result = codeService.getSpecialUserIdSet();

    return OK(result);
  }
	
	@GetMapping("/codes/logviewAuth/{codeType}")
	@ApiOperation(value = "사별 이력조회 권한 조회")
	public ApiResult<List<CodeLogviewAuth>> getCodeLogviewAuthList(@PathVariable String codeType, @ModelAttribute CodeFilterDto filter) throws Exception {
		List<CodeLogviewAuth> result = codeService.getCodeLogviewAuthList(codeType, filter);

		return OK(result);
	}
	
	@PatchMapping("/codes/logviewAuth/{rObjectId}")
	@ApiOperation(value = "이력조회 권한 수정")
	public ApiResult<HisViewUser> patchCodeLogviewAuth(@AuthenticationPrincipal JwtAuthentication authentication,
			@PathVariable String rObjectId, @RequestBody CodeDetailDto dto) throws Exception {
		
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		codeService.patchCodeLogviewAuth(rObjectId, userSession, dto);
		return OK(null);
	}
	
	@GetMapping("/codes/menu-category")
  @ApiOperation(value = "메뉴 리스트 조회")
  public ApiResult<List<CodeDetailDto>> getMenuList(@AuthenticationPrincipal JwtAuthentication authentication,  @ModelAttribute CodeFilterDto filter) throws Exception {
	  UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
	  List<CodeDetailDto> result = codeService.getMenuList(userSession, filter);

    return OK(result);
  }
	
	@GetMapping("/codes/history-category")
	@ApiOperation(value = "이력 메뉴 리스트 조회")
	public ApiResult<List<CodeDetailDto>> getHistoryMenuList(@AuthenticationPrincipal JwtAuthentication authentication,  @ModelAttribute CodeFilterDto filter) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		List<CodeDetailDto> result = codeService.getHistoryMenuList(userSession, filter);

	    return OK(result);
	}
	
	
	
}
