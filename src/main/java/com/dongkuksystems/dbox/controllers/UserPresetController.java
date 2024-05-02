package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.user.UserPresetDetailDto;
import com.dongkuksystems.dbox.models.dto.type.user.UserPresetRepeatingDto;
import com.dongkuksystems.dbox.models.type.user.UserPreset;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.userpreset.UserPresetService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@RequestMapping("/api")
@Api(tags = "사전설정 APIs")
public class UserPresetController extends AbstractCommonController {
	private static final String USER_PRESET_LIST_MODE_DETAIL = "detail";
	
	private final UserPresetService userPresetService;

	public UserPresetController(UserPresetService userPresetService) {
		this.userPresetService = userPresetService;
	}

	@GetMapping("/userpreset")
	@ApiOperation(value = "사전 설정 조회")
	public ApiResult<List<UserPreset>> selectOneByUserId(
			@AuthenticationPrincipal JwtAuthentication authentication,
			@ApiParam(value = "모드 (null: 기본, detail: 상세포함)") @RequestParam(required = false) String mode
			) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		
		List<UserPreset> list = null;
		if (USER_PRESET_LIST_MODE_DETAIL.equals(mode)) {
			list = userPresetService.selectDetailList(userSession);
		} else {
			list = userPresetService.selectList(userSession);
		}
		
		return OK(list);
	}
	
	@GetMapping("/userpreset/{rObjectId}")
	@ApiOperation(value = "사전 설정 조회 detail")
	public ApiResult<List<UserPreset>> selectList(@AuthenticationPrincipal JwtAuthentication authentication, @PathVariable String rObjectId) throws Exception {
		List<UserPreset> list = userPresetService.selectAllList(rObjectId);
		return OK(list);
	}
	
	
	@PatchMapping("/userpreset/{rObjectId}")
	@ApiOperation(value = "사전 설정 수정")
	public ApiResult<UserPreset> patchUserPreset(@AuthenticationPrincipal JwtAuthentication authentication,
			@PathVariable String rObjectId, @RequestBody UserPresetDetailDto dto) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		userPresetService.patchUserPreset(rObjectId, userSession, dto);
		return OK(null);
	}
	
	@GetMapping("/userpreset/count/{rObjectId}")
	@ApiOperation(value = "preset repeating 중복 조회")
	public ApiResult<Integer> getUserPresetDetailCount(@AuthenticationPrincipal JwtAuthentication authentication, @PathVariable String rObjectId, @ModelAttribute UserPresetRepeatingDto dto) throws Exception {
		int result = userPresetService.getUserPresetDetailCount(rObjectId, dto);
		return OK(result);
	}
	
	@PostMapping("/userpreset")
	@ApiOperation(value = "preset 추가")
	public ApiResult<String> createUserPreset(@AuthenticationPrincipal JwtAuthentication authentication,
			@RequestBody UserPresetDetailDto dto) throws Exception{
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		String rst = userPresetService.createUserPreset(dto, userSession);
		return OK(rst);
	}
	
	
  @DeleteMapping("/userpreset/{rObjectId}")
  @ApiOperation(value = "사용자 설정 preset 삭제")
  public ApiResult<Boolean> deleteUserPreset(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    userPresetService.deleteUserPreset(rObjectId, userSession);
    return OK(true);
  }
	
	
}
