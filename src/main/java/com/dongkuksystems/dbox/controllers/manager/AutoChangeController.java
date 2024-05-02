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
import com.dongkuksystems.dbox.models.type.code.Code;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.autochange.AutoChangeService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "Live Closed 자동변환기간 설정 APIs")
public class AutoChangeController extends AbstractCommonController {
  private final AutoChangeService autoChangeService;

  public AutoChangeController(AutoChangeService autoChangeService) {
    this.autoChangeService = autoChangeService;
  }

  @GetMapping("/autoChange/{uCodeVal1}")
  @ApiOperation(value = "자동 변환 기간 조회")
  public ApiResult<List<Code>> selectAutoChange(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String uCodeVal1) {
    List<Code> tree = autoChangeService.selectAutoChange(uCodeVal1);
    return OK(tree);
  }

  @PatchMapping("/autoChange/{rObjectId}/patch/{uCodeVal2}")
  @ApiOperation(value = "자동 변환 기간 수정")
  public ApiResult<Code> patchAutoChange(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId,@PathVariable String uCodeVal2) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = autoChangeService.patchAutoChange(rObjectId, uCodeVal2, userSession);
    return OK(null);
  }

}
