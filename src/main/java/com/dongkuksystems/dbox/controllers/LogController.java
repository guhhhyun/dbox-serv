package com.dongkuksystems.dbox.controllers;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.etc.DocLogDto;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.log.LogService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "Log APIs")
public class LogController extends AbstractCommonController {
  private final LogService logService;
  
  public LogController(LogService logService) {

    this.logService = logService;
  }

  @PostMapping("/log/doc")
  @ApiOperation(value = "log insert")
  public ApiResult<String> createDocLog(@AuthenticationPrincipal JwtAuthentication authentication,
      HttpServletRequest request, @RequestBody DocLogDto dto) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String ip = getClientIp(request);
    logService.insertDocLog(userSession, dto.getObjectId(), dto.getJobCode(), dto.getJobGubun(), ip);
    return OK(null);
  }
}









