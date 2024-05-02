package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.realtime.RealTimeDto;
import com.dongkuksystems.dbox.models.type.manager.realTime.RealTime;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.code.CodeService;
import com.dongkuksystems.dbox.services.manager.realtime.RealTimeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "실시간 저장 기능 사용 조직 관리 APIs")
public class RealTimeController extends AbstractCommonController {
  private final RealTimeService realTimeService;
  
  public RealTimeController(RealTimeService realTimeService) {
    this.realTimeService = realTimeService;    
  }

  @GetMapping("managepolicy/realtime/getData/{uComCode}")
  @ApiOperation(value = "실시간 저장 기능 사용 조직 관리 부서 조회")
  public ApiResult<List<RealTime>> selectGradePreservation(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String uComCode) {
    List<RealTime> list = realTimeService.selectRealTime(uComCode);
    return OK(list);
  }

  @PostMapping("managepolicy/realtime/deptSave")
  @ApiOperation(value = "실시간 저장 기능 사용 조직 관리 부서 추가")
  public ApiResult postDeptSava(@AuthenticationPrincipal JwtAuthentication authentication,
      @RequestBody RealTimeDto postDeptSaveDto) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = realTimeService.postDeptSava(userSession, postDeptSaveDto);
    return OK(null);
  }

  @DeleteMapping("managepolicy/realtime/deleteDept/{rObjectId}")
  @ApiOperation(value = "실시간 저장 기능 사용 조직 관리 부서 삭제")
  public ApiResult deleteDept(@AuthenticationPrincipal JwtAuthentication authentication, @PathVariable String rObjectId)
      throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = realTimeService.deleteDept(rObjectId, userSession);
    return OK(null);
  }

}
