package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.code.CodeDetailDto;
import com.dongkuksystems.dbox.models.dto.type.manager.deptinformconfig.DeptInformConfigDto;
import com.dongkuksystems.dbox.models.dto.type.user.LockUserDto;
import com.dongkuksystems.dbox.models.dto.type.user.UserLockFilterDto;
import com.dongkuksystems.dbox.models.type.manager.deptinformconfig.DeptInformConfig;
import com.dongkuksystems.dbox.models.type.user.UserLock;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.userlock.UserLockService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@RequestMapping("/api")
@Api(tags = "특이사용자 APIs")
public class UserLockController extends AbstractCommonController {
	private final UserLockService userLockService;

	public UserLockController(UserLockService userLockService) {
		this.userLockService = userLockService;
	}

	@GetMapping("/unusuals")
	@ApiOperation(value = "특이사용자 조회")
	public ApiResult<List<UserLock>> selectUserLocks(UserLockFilterDto dto, Model model, @AuthenticationPrincipal JwtAuthentication authentication) {
		List<UserLock> list = userLockService.selectUserLocks(dto);
		return OK(list);
	}
	
	@PatchMapping("/unusuals/{rObjectId}/users/{userObjectId}")
	@ApiOperation(value = "특이사용자 잠금처리")
	public ApiResult<UserLock> patchUserLock(@AuthenticationPrincipal JwtAuthentication authentication,
			@PathVariable String rObjectId, @PathVariable String userObjectId, @RequestBody LockUserDto dto) throws Exception {
		
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		userLockService.patchUserLock(rObjectId, userObjectId, userSession, dto);
		return OK(null);
	}
	
	@PostMapping("/unusuals")
	@ApiOperation(value = "요청에 의한 잠금 처리")
	public ApiResult registUserLock(@AuthenticationPrincipal JwtAuthentication authentication,  @RequestBody LockUserDto dto) throws Exception {

		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);

		userLockService.registUserLock(userSession, dto);
		return OK(null);
	}
	
	
	@GetMapping("/deptInform/{uCodeCode}/{uDeptCode}")
	@ApiOperation(value = "사전Warning 기준값 조회")
	public ApiResult<DeptInformConfig> selectDeptInform(@AuthenticationPrincipal JwtAuthentication authentication, @PathVariable String uCodeCode, @PathVariable String uDeptCode) {

		DeptInformConfig deptInformConfig = userLockService.selectListByOrgId(uCodeCode, uDeptCode).orElseThrow(() -> new NotFoundException(DeptInformConfig.class, uCodeCode, uDeptCode));
		return OK(deptInformConfig);
	}
	
	@PatchMapping("/codes/{rObjectId}")
	@ApiOperation(value = "특이사용자 식별 수정")
	public ApiResult<UserLock> patchCode(@AuthenticationPrincipal JwtAuthentication authentication,
			 @PathVariable String rObjectId, @RequestBody CodeDetailDto dto) throws Exception {
		
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		userLockService.patchCode(rObjectId, userSession, dto);
		return OK(null);
	}
	
	@PatchMapping("/deptInform")
	@ApiOperation(value = "Warning 기준값 수정")
	public ApiResult<DeptInformConfig> patchDeptInform(@AuthenticationPrincipal JwtAuthentication authentication,
			 @RequestBody DeptInformConfigDto dto) throws Exception {
		
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
		userLockService.patchDeptInform(userSession, dto);
		return OK(null);
	}
	
	@GetMapping("/deptInform/excel/{uCodeCode}")
	@ApiOperation(value = "사전Warning 기준값 조회 excel 다운로드")
	public ApiResult<List<DeptInformConfigDto>> selectListDept(@AuthenticationPrincipal JwtAuthentication authentication, @PathVariable String uCodeCode) {

		List<DeptInformConfigDto> list = userLockService.selectListDept(uCodeCode);
		return OK(list);
	}

}
