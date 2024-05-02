package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.errors.NotFoundException;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.rolemanagement.RoleManagementDto;
import com.dongkuksystems.dbox.models.type.manager.rolemanagement.RoleManagement;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.rolemanagement.RoleManagementService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "업무역할별 관리 권한 APIs")
public class RoleManagementController extends AbstractCommonController {
  private final RoleManagementService roleManagementService;

  public RoleManagementController(RoleManagementService roleManagementService) {
    this.roleManagementService = roleManagementService;
  }

  @GetMapping("/rolemanagement/{uDocFlag}")
  @ApiOperation(value = "업무역할 조회")
  public ApiResult<List<RoleManagement>> selectRoleManagement(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String uDocFlag) {
    List<RoleManagement> list = roleManagementService.selectRoleManagement(uDocFlag);
    return OK(list);
  }

  @PatchMapping("/rolemanagement/update/{rObjectId}")
  @ApiOperation(value = "업무역할 정책 변경")
  public ApiResult<RoleManagement> updatePolicy(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId, @RequestBody RoleManagementDto dto) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    roleManagementService.updatePolicy(userSession, rObjectId, dto);
    return null;
  }

  @GetMapping("/rolemanagement/{rObjectId}/select/{uOptionVal}")
  @ApiOperation(value = "정책 미적용 그룹 조회")
  public ApiResult<RoleManagement> selectUnPolicyGroup(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId, @PathVariable String uOptionVal) {
    RoleManagement result = roleManagementService.selectUnPolicyGroup(rObjectId, uOptionVal)
        .orElseThrow(() -> new NotFoundException(RoleManagement.class, rObjectId, uOptionVal));
    return OK(result);
  }

}
