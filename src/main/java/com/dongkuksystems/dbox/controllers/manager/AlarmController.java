package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.alarm.PatchAlarmDto;
import com.dongkuksystems.dbox.models.type.manager.noticonfig.NotiConfig;

import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.alarm.AlarmService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "알람/통보 방식 관리 APIs")
public class AlarmController extends AbstractCommonController {
  private final AlarmService alarmService;

  public AlarmController(AlarmService alarmService) {
    this.alarmService = alarmService;
  }

  @GetMapping("/alarm/{uComCode}")
  @ApiOperation(value = "알람/통보 방식 조회")
  public ApiResult<List<NotiConfig>> selectAlarm(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String uComCode) {
    List<NotiConfig> tree = alarmService.selectAlarm(uComCode);
    return OK(tree);
  }

  @PatchMapping("/alarm/patch/{rObjectId}")
  @ApiOperation(value = "알람/통보 방식 수정")
  public ApiResult patchAlarm(@AuthenticationPrincipal JwtAuthentication authentication, @PathVariable String rObjectId,
      @RequestBody PatchAlarmDto patchAlarmDto) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = alarmService.patchAlarm(rObjectId, userSession, patchAlarmDto);
    return OK(null);
  }
}