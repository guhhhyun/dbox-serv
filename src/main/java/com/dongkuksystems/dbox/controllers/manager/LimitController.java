package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.type.manager.limit.Limit;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.limit.LimitService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "기능별 최대 처리량 설정 APIs")
public class LimitController extends AbstractCommonController {
  private final LimitService limitService;

  public LimitController(LimitService limitService) {
    this.limitService = limitService;
  }

  @GetMapping("/limit/{uComCode}")
  @ApiOperation(value = "기능별 최대 처리량(사별) 조회")
  public ApiResult<List<Limit>> selectLimitValue(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String uComCode) {
    List<Limit> list = limitService.selectLimitValue(uComCode);
    return OK(list);
  }

  @PatchMapping("/limit/{rObjectId}/update/{uCodeVal}")
  @ApiOperation(value = "기능별 최대 처리량(사별) 수정")
  public ApiResult<List<Limit>> patchLimitValue(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId, @PathVariable String uCodeVal) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = limitService.patchLimitValue(userSession, rObjectId, uCodeVal);
    return OK(null);
  }

}
