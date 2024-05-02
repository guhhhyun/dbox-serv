package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.hisviewuser.HisViewUserDto;
import com.dongkuksystems.dbox.models.dto.type.manager.hisviewuser.HisViewUserFilterDto;
import com.dongkuksystems.dbox.models.dto.type.manager.roleauth.RoleAuthDto;
import com.dongkuksystems.dbox.models.type.manager.hisviewuser.HisViewUser;
import com.dongkuksystems.dbox.models.type.manager.roleauth.RoleAuth;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.hisviewuser.HisViewUserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api")
@Api(tags = "이력조회 권한 추가 사용자 APIs")
public class HisViewUserController extends AbstractCommonController {
	private final HisViewUserService hisViewUserService;

	public HisViewUserController(HisViewUserService hisViewUserService) {
		this.hisViewUserService = hisViewUserService;
	}

	@GetMapping("/hisViewUsers")
	@ApiOperation(value = "이력조회 권한 추가 사용자 조회")
	public ApiResult<List<HisViewUser>> selectAll(@ModelAttribute HisViewUserFilterDto dto,  @AuthenticationPrincipal JwtAuthentication authentication) {
		List<HisViewUser> list = hisViewUserService.selectAll(dto);
		return OK(list);
	}
	
	@PostMapping("/hisViewUsers")
	@ApiOperation(value = "이력조회 권한 추가 사용자 추가")
	public ApiResult<HisViewUser> registHisViewUser(@AuthenticationPrincipal JwtAuthentication authentication,
			@RequestBody HisViewUserDto dto) throws Exception{
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		
		hisViewUserService.registHisViewUser(userSession, dto);
		return OK(null);
	}
	
	@DeleteMapping("/hisViewUsers/{rObjectId}")
	@ApiOperation(value = "이력조회 권한 추가 사용자 삭제")
	public ApiResult<HisViewUser> deleteHisViewUser(@AuthenticationPrincipal JwtAuthentication authentication,
			 @PathVariable String rObjectId) throws Exception{
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);
		
		hisViewUserService.deleteHisViewUser(userSession, rObjectId);
		
		return OK(null);
	}
	
}
