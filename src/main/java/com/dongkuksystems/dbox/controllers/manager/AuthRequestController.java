package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.authrequest.AuthRequestCollectDto;
import com.dongkuksystems.dbox.models.dto.type.manager.authrequest.AuthRequestPatchDto;
import com.dongkuksystems.dbox.models.dto.type.manager.authrequest.AuthRequestUserDto;
import com.dongkuksystems.dbox.models.type.manager.authrequest.AuthRequest;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.authrequest.AuthRequestService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "조회권한 요청 목록 APIS")
public class AuthRequestController extends AbstractCommonController {
  private final AuthRequestService authRequestService;

  public AuthRequestController(AuthRequestService authRequestService) {
    this.authRequestService = authRequestService;
  }

  @GetMapping("/authRequest")
  @ApiOperation(value = "조회권한 요청 목록 조회")
  public ApiResult<List<AuthRequest>> selectAuthRequest(AuthRequestUserDto authRequestUserDto, Model model,
      @AuthenticationPrincipal JwtAuthentication authentication) {
    List<AuthRequest> list = authRequestService.selectAuthRequest(authRequestUserDto);
    return OK(list);
  }

  @GetMapping("/authWithdrawal")
  @ApiOperation(value = "조회권한 요청결과 조회")
  public ApiResult<List<AuthRequest>> selectAuthWithdrawal(AuthRequestUserDto authRequestUserDto, Model model,
      @AuthenticationPrincipal JwtAuthentication authentication) {
    List<AuthRequest> list = authRequestService.selectAuthWithdrawal(authRequestUserDto);
    return OK(list);
  }

  @PatchMapping("/authRequest/update")
  @ApiOperation(value = "조회권한 수정")
  public void updateAuthWithdrawal(@AuthenticationPrincipal JwtAuthentication authentication,
      @RequestBody AuthRequestPatchDto authRequestPatchDto, HttpServletRequest request) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String ip = getClientIp(request);
    authRequestService.updateAuthWithdrawal(authRequestPatchDto, userSession, ip);
  }

  @PatchMapping("/authWithdrawal/update")
  @ApiOperation(value = "조회권한 회수")
  public void collectAuthWithdrawal(@AuthenticationPrincipal JwtAuthentication authentication,
      @RequestBody AuthRequestCollectDto authRequestCollectDto) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    authRequestService.collectAuthWithdrawal(authRequestCollectDto, userSession);
  }

}
