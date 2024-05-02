package com.dongkuksystems.dbox.controllers.manager;

import static com.dongkuksystems.dbox.models.api.response.ApiResult.OK;

import java.util.List;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.dongkuksystems.dbox.controllers.AbstractCommonController;
import com.dongkuksystems.dbox.models.api.response.ApiResult;
import com.dongkuksystems.dbox.models.common.UserSession;
import com.dongkuksystems.dbox.models.dto.type.manager.roleauth.RoleAuthDto;
import com.dongkuksystems.dbox.models.type.manager.roleauth.RoleAuth;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.roleauth.RoleAuthService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;

@RestController
@Api(tags = "업무역할자 관리 APIs")
public class RoleAuthController extends AbstractCommonController {
  private final RoleAuthService roleAuthService;

  public RoleAuthController(RoleAuthService roleAuthService) {
    this.roleAuthService = roleAuthService;
  }

  @GetMapping("/roleAuthGroup")
  @ApiOperation(value = "업무역할그룹 조회")
  public ApiResult<List<RoleAuth>> selectRoleAuthGroups(@AuthenticationPrincipal JwtAuthentication authentication) {    
    String type = "A";
    List<RoleAuth> list = roleAuthService.selectRoleAuthGroups(type);
    return OK(list);
  }

  @GetMapping("/roleAuthGroup/{uAuthGroup}/users/{uConfigFlag}/{uGroupScope}")
  @ApiOperation(value = "업무역할그룹 사용자 조회")
  public ApiResult<List<RoleAuth>> selectRoleAuthGroupUsers(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String uAuthGroup, @PathVariable String uConfigFlag, @PathVariable String uGroupScope) {    
    List<RoleAuth> list = roleAuthService.selectRoleAuthGroupUsers(uAuthGroup, uConfigFlag, uGroupScope);
    return OK(list);
  }
  
  @GetMapping("/roleAuthGroup/{uComCode}/mgr/{uGroupScope}")
  @ApiOperation(value = "edms_mgr 관리자 조회")
  public ApiResult<List<RoleAuth>> selectMgrUsers(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String uComCode, @PathVariable String uGroupScope) {    
    List<RoleAuth> list = roleAuthService.selectMgrUsers(uComCode, uGroupScope);
    return OK(list);
  }  

  @PostMapping("/roleAuthGroup/createUser")
  @ApiOperation(value = "사용자 추가")
  public ApiResult<RoleAuth> createRoleAuthUser(@AuthenticationPrincipal JwtAuthentication authentication,
      @RequestBody RoleAuthDto dto) throws Exception {    
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    roleAuthService.createRoleAuthUser(dto, userSession);
    return OK(null);
  }

  @DeleteMapping("/roleAuthGroup/deleteUser")
  @ApiOperation(value = "사용자 삭제")
  public ApiResult<RoleAuth> deleteRoleAuthUser(@AuthenticationPrincipal JwtAuthentication authentication,
      @RequestBody RoleAuthDto dto) throws Exception {    
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    roleAuthService.deleteRoleAuthUser(dto, userSession);//
    return OK(null);
  }

  @GetMapping("/roleAuthDeptGroup/{uDocFlag}/select/{uOptionVal}")
  @ApiOperation(value = "부서문서함 조회")
  public ApiResult<List<RoleAuth>> selectDeptMgrGroup(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String uDocFlag, @PathVariable String uOptionVal) {
    List<RoleAuth> list = roleAuthService.selectDeptMgrGroup(uDocFlag, uOptionVal);
    return OK(list);
  }

}
