package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.ui.Model;
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
import com.dongkuksystems.dbox.models.dto.type.manager.attachpolicy.AttachPolicyUserDto;
import com.dongkuksystems.dbox.models.dto.type.manager.attachpolicy.CreateAttachPolicyUserDto;
import com.dongkuksystems.dbox.models.dto.type.manager.attachpolicy.UpdateUserDateDto;
import com.dongkuksystems.dbox.models.type.manager.AttachPolicyUser;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.attachpolicyuser.AttachPolicyUserService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "사용자별 첨부 정책 설정 APIs")
public class AttachPolicyUserController extends AbstractCommonController {
  private final AttachPolicyUserService attachPolicyUserService;

  public AttachPolicyUserController(AttachPolicyUserService attachPolicyUserService) {
    this.attachPolicyUserService = attachPolicyUserService;
  }

  @GetMapping("/attachpolicyuser")
  @ApiOperation(value = "사용자별 첨부 정책 설정 조회")
  public ApiResult<List<AttachPolicyUser>> selectAll(@AuthenticationPrincipal JwtAuthentication authentication,
      AttachPolicyUserDto dto, Model model) {
    List<AttachPolicyUser> list = attachPolicyUserService.selectAll(dto);
    return OK(list);
  }

  @PostMapping("/attachpolicyuser/create")
  @ApiOperation(value = "사용자 추가")
  public ApiResult<AttachPolicyUser> createAttachPolicyUser(@AuthenticationPrincipal JwtAuthentication authentication,
      @RequestBody CreateAttachPolicyUserDto dto) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = attachPolicyUserService.createAttachPolicyUser(dto, userSession);
    return OK(null);
  }

  @DeleteMapping("/attachpolicyuser/delete/{rObjectId}")
  @ApiOperation(value = "사용자 삭제")
  public ApiResult<AttachPolicyUser> deleteAttachPolicyUser(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    attachPolicyUserService.deleteAttachPolicyUser(rObjectId, userSession);
    return OK(null);
  }

  @PatchMapping("/attachpolicyuser/update")
  @ApiOperation(value = "사용자 적용기간 수정")
  public ApiResult<AttachPolicyUser> updateUserDate(@AuthenticationPrincipal JwtAuthentication authentication,
      @RequestBody UpdateUserDateDto dto) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = attachPolicyUserService.updateUserDate(dto, userSession);
    return OK(null);
  }
}
