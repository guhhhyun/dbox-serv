package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.daos.type.agree.AgreeDao;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.type.agree.Agree;
import com.dongkuksystems.dbox.securities.JwtAuthentication;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "동의서 APIs")
public class AgreementController extends AbstractCommonController {
  private final AgreeDao agreeDao;

  public AgreementController(AgreeDao agreeDao) {
    this.agreeDao = agreeDao;
  }

  @GetMapping("/agreement")
  @ApiOperation(value = "본인 동의서 조회")
  public ApiResult<List<Agree>> getAgreement(@AuthenticationPrincipal JwtAuthentication authentication)
      throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);

    List<Agree> result = agreeDao.selectListByUserId(userSession.getUser().getUserId());
    return OK(result);
  }
}
