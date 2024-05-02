package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.deletemanage.DeleteManageDto;
import com.dongkuksystems.dbox.models.dto.type.manager.disposalrequest.DisposalRequestDto;
import com.dongkuksystems.dbox.models.type.manager.disposalrequest.DisposalRequest;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.disposalrequest.DisposalRequestService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "문서 폐기 승인/반려 -관리자 APIs")
public class DisposalRequestController extends AbstractCommonController {

  private final DisposalRequestService disposalRequestService;

  public DisposalRequestController(DisposalRequestService disposalRequestService) {
    this.disposalRequestService = disposalRequestService;
  }

  @GetMapping(value = "/disposal-request")
  @ApiOperation(value = "폐기 요청 조회")
  public ApiResult<List<DisposalRequest>> selectDisposalRequest(
      @AuthenticationPrincipal JwtAuthentication authentication, DisposalRequestDto dto, Model model) {
    List<DisposalRequest> list = disposalRequestService.selectDisposalRequest(dto);
    return OK(list);
  }

  @PostMapping("/disposal-request/{closedRequestId}/restore")
  @ApiOperation(value = "폐기 승인문서 복원")
  public ApiResult<String> restoreReqClosed(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String closedRequestId, HttpServletRequest request,  @RequestBody(required = false) DeleteManageDto dto) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String ip = getClientIp(request);
    String rst = disposalRequestService.restoreReqClosed(closedRequestId, userSession, ip, dto);
    return OK(rst);
  }

}