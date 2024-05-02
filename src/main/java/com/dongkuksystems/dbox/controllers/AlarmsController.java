
package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;
import java.util.Map;

import com.dongkuksystems.dbox.models.dto.type.noti.AlarmDto;
import com.dongkuksystems.dbox.models.dto.type.noti.AlarmOneDto;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.noti.AlarmDetailDto;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.noti.AlarmsService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(tags = "알림 APIs")
public class AlarmsController extends AbstractCommonController {

	private final AlarmsService alarmsService;

	public AlarmsController(AlarmsService alarmsService) {
		this.alarmsService = alarmsService;
	}

	@GetMapping("/alarms")
	@ApiOperation(value = "알림 리스트 조회")
	public ApiResult<List<AlarmDetailDto>> getAlarmList(@AuthenticationPrincipal JwtAuthentication authentication)  throws Exception {
		return OK(alarmsService.getAlarmList(authentication.loginId, false));
	}

	@GetMapping("/alarms/approvals")
	public ApiResult<List<AlarmDetailDto>> getApprovalAlarmList(@AuthenticationPrincipal JwtAuthentication authentication) throws Exception {
		return OK(alarmsService.getAlarmList(authentication.loginId, true));
	}

	@GetMapping("/alarms/count")
	@ApiOperation(value = "알림 개수 조회")
	public ApiResult<Integer> getAlarmCount(@AuthenticationPrincipal JwtAuthentication authentication) throws Exception {
		int result = alarmsService.getAlarmCount(authentication.loginId);

		return OK(result);
	}

	@PatchMapping("/alarms/{action}/{rObjectId}")
	public ApiResult<Boolean> approveAlarm(
			@AuthenticationPrincipal JwtAuthentication authentication,
			@PathVariable String action,
			@PathVariable String rObjectId) {
		System.out.println("action = " + action + ", rObjectId = " + rObjectId);
		UserSession userSession = getUserSession(authentication);
		alarmsService.patchAlarmApproval(new AlarmDto().userSession(userSession).rObjectId(rObjectId).action(action));
		return OK(null);
	}

	@PostMapping("/alarms")
	@ApiOperation(value = "알림 삭제")
	public ApiResult<Map<String, Integer>> deleteAlarm(@AuthenticationPrincipal JwtAuthentication authentication, @RequestBody AlarmOneDto dto) throws Exception {
		UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
				UserSession.class);

		Map<String, Integer> result = alarmsService.deleteAlarm(userSession, dto);

		return OK(result);
	}
	
	@PostMapping("/alarms/{rObjectId}/approve")
  @ApiOperation(value = "알림 승인/반려")
  public ApiResult<String> approveAlarm(@AuthenticationPrincipal JwtAuthentication authentication, 
      @PathVariable String rObjectId, 
      @ApiParam(value = "승인(Y),반려(N)", example = "Y") @RequestParam String actionYn, 
      @ApiParam(value = "잠금해제사유", example = "test") @RequestParam String unLockReason, 
      @ApiParam(value = "반려사유", example = "test") @RequestParam String rejectReason) throws Exception {

    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId,
        UserSession.class);

    String result = alarmsService.approveNoti(userSession, rObjectId, actionYn, unLockReason, rejectReason);

    return OK(result);
  }

	private UserSession getUserSession(JwtAuthentication authentication) {
		return (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
	}

}