package com.dongkuksystems.dbox.controllers.manager;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.preservationperiod.PreservationPeriodDto;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.preservationperiod.PreservationPeriodService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

@RestController
@RequestMapping("/api/preservation-periods")
public class PreservationPeriodController extends AbstractCommonController {

	@Autowired
	private PreservationPeriodService preservationPeriodService;

	@GetMapping("/{comCode}")
	public ApiResult<Map<String, Object>> selectPreservationPeriodByComCode(@PathVariable String comCode) {
		return OK(preservationPeriodService.selectPreservationPeriodByComCode(comCode));
	}

	@GetMapping("/depts")
	public ApiResult<List<Map<String, Object>>> selectDepts() {
		return OK(preservationPeriodService.selectDepts());
	}

	@PatchMapping("/{rObjectId}")
	public ApiResult<Boolean> patchPreservationPeriod(
			@AuthenticationPrincipal JwtAuthentication authentication,
			@PathVariable String rObjectId,
			@RequestBody PreservationPeriodDto preservationPeriodDto) {
		UserSession userSession = getUserSession(authentication);
		preservationPeriodService.patchPreservationPeriod(preservationPeriodDto.rObjectId(rObjectId).userSession(userSession));
		return OK(null);
	}

	@PatchMapping("/{rObjectId}/auto-extend")
	public ApiResult<Boolean> patchAutoExtend(
			@AuthenticationPrincipal JwtAuthentication authentication,
			@PathVariable String rObjectId,
			@RequestBody PreservationPeriodDto preservationPeriodDto) {
		UserSession userSession = getUserSession(authentication);
		preservationPeriodService.patchAutoExtend(preservationPeriodDto.rObjectId(rObjectId).userSession(userSession));
		return OK(null);
	}

	@PatchMapping("/dept/{rObjectId}/disuse-auto-extend")
	public ApiResult<Boolean> patchDeptToUseAutoExtend(
			@AuthenticationPrincipal JwtAuthentication authentication,
			@PathVariable String rObjectId,
			@RequestBody PreservationPeriodDto preservationPeriodDto) {
		UserSession userSession = getUserSession(authentication);
		preservationPeriodService.patchDeptNotToUseAutoExtend(preservationPeriodDto.rObjectId(rObjectId).userSession(userSession));
		return OK(null);
	}

	@PatchMapping("/dept/{rObjectId}/use-auto-extend")
	public ApiResult<Boolean> patchDeptNotToUseAutoExtend(
			@AuthenticationPrincipal JwtAuthentication authentication,
			@PathVariable String rObjectId,
			@RequestBody PreservationPeriodDto preservationPeriodDto) {
		UserSession userSession = getUserSession(authentication);
		preservationPeriodService.patchDeptToUseAutoExtend(preservationPeriodDto.rObjectId(rObjectId).userSession(userSession));
		return OK(null);
	}

	private UserSession getUserSession(JwtAuthentication authentication) {
		return (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
	}

}
