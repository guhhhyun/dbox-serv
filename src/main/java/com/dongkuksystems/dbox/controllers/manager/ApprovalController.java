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
import com.dongkuksystems.dbox.services.code.CodeService;
import com.dongkuksystems.dbox.services.manager.approval.ApprovalService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "자료이관 승인설정 APIs")
public class ApprovalController extends AbstractCommonController {
  private final ApprovalService approvalService;
  
  public ApprovalController(ApprovalService approvalService) {
    this.approvalService = approvalService;
  }

  @GetMapping("/approval/{uCodeVal1}")
  @ApiOperation(value = "사별 자료이관승인설정 조회")
  public ApiResult<List<Code>> selectApproval(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String uCodeVal1) {    
    List<Code> tree = approvalService.selectApproval(uCodeVal1);
    return OK(tree);
  }

  @PatchMapping("/approval/{rObjectId}/patch/{uCodeVal2}")
  @ApiOperation(value = "사별 자료이관승인설정 수정")
  public ApiResult<Code> patchApproval(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId, @PathVariable String uCodeVal2) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = approvalService.patchApproval(rObjectId, uCodeVal2, userSession);
    return OK(null);
  }

}
