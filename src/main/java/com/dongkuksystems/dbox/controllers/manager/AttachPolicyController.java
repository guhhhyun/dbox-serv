package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.attachpolicy.CreateAttachPolicyDto;
import com.dongkuksystems.dbox.models.dto.type.manager.attachpolicy.PatchAttachPolicyDto;
import com.dongkuksystems.dbox.models.type.manager.AttachPolicy;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.attachpolicy.AttachPolicyService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "문서 첨부 정책 설정 APIs")
public class AttachPolicyController extends AbstractCommonController {
  private final AttachPolicyService attachPolicyService;

  public AttachPolicyController(AttachPolicyService attachPolicyService) {
    this.attachPolicyService = attachPolicyService;
  }

  @GetMapping("/attachpolicy")
  @ApiOperation(value = "문서 첨부 정책 설정 조회")
  public ApiResult<List<AttachPolicy>> selectAll(@AuthenticationPrincipal JwtAuthentication authentication) {
    List<AttachPolicy> list = attachPolicyService.selectAll();
    return OK(list);
  }

  @PatchMapping("/attachpolicy/update/{rObjectId}")
  @ApiOperation(value = "문서 첨부 정책 수정")
  public ApiResult<AttachPolicy> patchAttachPolicy(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId, @RequestBody PatchAttachPolicyDto patchAttachPolicyDto) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = attachPolicyService.patchAttachPolicy(rObjectId, userSession, patchAttachPolicyDto);
    return OK(null);
  }

  @PostMapping("/attachpolicy/create")
  @ApiOperation(value = "문서 첨부 정책 추가")
  public ApiResult<AttachPolicy> createAttachPolicy(@AuthenticationPrincipal JwtAuthentication authentication,
      @RequestBody CreateAttachPolicyDto dto) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = attachPolicyService.createAttachPolicy(dto, userSession);
    return OK(null);
  }

  @DeleteMapping("/attachpolicy/delete/{rObjectId}")
  @ApiOperation(value = "문서 첨부 정책 삭제")
  public ApiResult<AttachPolicy> deleteAttachPolicy(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    attachPolicyService.deleteAttachPolicy(rObjectId, userSession);
    return OK(null);
  }

}
