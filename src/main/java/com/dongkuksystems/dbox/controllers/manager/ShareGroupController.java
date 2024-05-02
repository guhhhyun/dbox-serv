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
import com.dongkuksystems.dbox.models.dto.type.manager.sharegroup.PatchDeptDto;
import com.dongkuksystems.dbox.models.dto.type.manager.sharegroup.PatchShareGroupDto;
import com.dongkuksystems.dbox.models.dto.type.manager.sharegroup.ShareGroupCreateDto;
import com.dongkuksystems.dbox.models.type.manager.sharegroup.ShareGroup;
import com.dongkuksystems.dbox.securities.JwtAuthentication;
import com.dongkuksystems.dbox.services.manager.sharegroup.ShareGroupService;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;

@RestController
@Api(tags = "공유 그룹 APIs")
public class ShareGroupController extends AbstractCommonController {
  private final ShareGroupService shareGroupService;

  public ShareGroupController(ShareGroupService shareGroupService) {
    this.shareGroupService = shareGroupService;
  }

  @GetMapping("/sharegroups")
  @ApiOperation(value = "전체 공유 그룹 조회")
  public ApiResult<List<ShareGroup>> selectShareGroups(@AuthenticationPrincipal JwtAuthentication authentication) {
    List<ShareGroup> tree = shareGroupService.selectShareGroups();
    return OK(tree);
  }

  @GetMapping("/sharegroups/depts/{rObjectId}")
  @ApiOperation(value = "공유 그룹 안에 부서 조회")
  public ApiResult<List<ShareGroup>> selectDeptInShareGroup(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId) {
    List<ShareGroup> tree = shareGroupService.selectDeptInShareGroup(rObjectId);
    return OK(tree);
  }

  @PostMapping("/sharegroups/create")
  @ApiOperation(value = "공유 그룹 추가")
  public ApiResult<ShareGroup> createShareGroup(@AuthenticationPrincipal JwtAuthentication authentication,
      @ApiParam(value = "공유 그룹 정보") @RequestBody ShareGroupCreateDto sharegroup) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = shareGroupService.createShareGroup(userSession, sharegroup);
    return OK(null);
  }

  @PostMapping("/sharegroups/{rObjectId}")
  @ApiOperation(value = "부서 추가")
  public ApiResult patchDept(@AuthenticationPrincipal JwtAuthentication authentication, @PathVariable String rObjectId,
      @RequestBody PatchDeptDto patchDeptDto) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = shareGroupService.patchDept(rObjectId, userSession, patchDeptDto);//
    return OK(null);
  }

  @DeleteMapping("/sharegroups/delete/{rObjectId}")
  @ApiOperation(value = "공유 그룹 삭제")
  public ApiResult<ShareGroup> deleteShareGroup(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    shareGroupService.deleteShareGroup(rObjectId, userSession);
    return OK(null);
  }

  @DeleteMapping("/sharegroups/{rObjectId}/depts/{uDeptCode}/delete")
  @ApiOperation(value = "공유 그룹에 속한 부서 삭제")
  public ApiResult<ShareGroup> deleteDept(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId, @PathVariable String uDeptCode) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    shareGroupService.deleteDept(rObjectId, userSession, uDeptCode);
    return OK(null);
  }

  @PatchMapping("/sharegroups/update/{rObjectId}")
  @ApiOperation(value = "공유 그룹 수정")
  public ApiResult<ShareGroup> patchShareGroup(@AuthenticationPrincipal JwtAuthentication authentication,
      @PathVariable String rObjectId, @RequestBody PatchShareGroupDto patchShareGroupDto) throws Exception {
    UserSession userSession = (UserSession) getRedisRepository().getObject(authentication.loginId, UserSession.class);
    String rst = shareGroupService.patchShareGroup(rObjectId, userSession, patchShareGroupDto);
    return OK(null);
  }
}
